# Resumen Técnico del Sistema

Este documento describe la arquitectura técnica, los módulos principales y los flujos de datos del sistema de gestión de la Cuenca Ovino-Caprina en su estado actual.

---

## 1. Arquitectura General y Stack Tecnológico

El sistema utiliza una arquitectura **MVC (Modelo-Vista-Controlador)** moderna, organizada a través de patrones de diseño para maximizar la mantenibilidad y escalabilidad.

- **Backend:** **Laravel (PHP)**
  - **Autenticación:** **Laravel Jetstream** (web) y **Laravel Sanctum** (API).
  - **Base de Datos:** **MySQL** con el ORM **Eloquent**.
  - **Tareas en Segundo Plano:** **Colas y Jobs de Laravel** para procesos asíncronos.

- **Frontend:**
  - **Vistas:** **Blade**.
  - **Interactividad:** **Alpine.js** para reactividad en el cliente.
  - **Estilos:** **TailwindCSS**.
  - **Gráficos y Mapas:** **Chart.js** y **Leaflet.js**.

- **Patrones de Diseño Clave:**
  - **Acciones (Actions):** Clases de un solo uso que encapsulan una lógica de negocio específica (ej. `CreateUnidadProductiva`).
  - **Servicios (Services):** Clases que orquestan lógicas complejas o interactúan con APIs externas (ej. `NotificationUIService`).
  - **Observadores (Observers):** Clases que reaccionan a eventos de los modelos Eloquent para ejecutar código automáticamente (ej. `StockAnimalObserver`).

- **Modelos de Configuración:**
  - **`ConfiguracionActualizacion`:** Almacena la configuración para el sistema de actualización de stock (frecuencia, estado activo, etc.).
  - **`ConfiguracionLogistica`:** Almacena la configuración para el ciclo de recogida del camión de logística (frecuencia en días, fecha de inicio del ciclo).

---

## 2. Autenticación y Acceso

El sistema soporta múltiples flujos de autenticación para diferentes casos de uso.

### 2.1. Flujo Web (Sesión con Jetstream)
- **Proceso:** Es el inicio de sesión estándar con email y contraseña.
- **Mecanismo:** Gestionado por Laravel Jetstream. Una vez autenticado, un `LoginResponse` personalizado inspecciona el rol del usuario (`superadmin`, `productor`, etc.) y lo redirige a su panel correspondiente.

### 2.2. Flujo API (Sanctum y Autenticación sin Contraseña)
Diseñado para clientes externos como una aplicación móvil.
- **Proceso:**
  1.  El usuario introduce su email o teléfono en el cliente (app móvil).
  2.  El cliente envía el identificador al endpoint `/api/solicitar-codigo`.
  3.  El backend genera un código de 6 dígitos, lo asocia al ID de usuario en la caché (con 10 min de expiración) y lo envía por Email o SMS. Por seguridad, la respuesta de la API es siempre la misma, se haya encontrado o no al usuario.
  4.  El usuario introduce el código recibido en el cliente.
  5.  El cliente envía el identificador y el código al endpoint `/api/iniciar-sesion`.
  6.  El backend valida el código contra la caché. Si es correcto, elimina todos los tokens de API anteriores del usuario, genera un nuevo token de Sanctum y lo devuelve al cliente.
- **Mecanismo:** `Api/AuthController` gestiona la lógica, `Cache` de Laravel para el almacenamiento temporal del código y `Sanctum` para la generación de tokens.

### 2.3. Flujo de "Reclamar Perfil"
Este flujo es crucial para incorporar a productores cuyos datos se importaron masivamente sin un email.
- **Estado Inicial:** Tras una importación masiva, se crea un registro en la tabla `productors` pero no en `users`. Este es un "perfil de datos", no una "cuenta de usuario".
- **Proceso (Conceptual):**
  1.  El administrador del sistema contacta al productor (ej. por teléfono) y le proporciona un código único o un enlace de registro especial.
  2.  El productor accede a una vista de "Reclamar Perfil".
  3.  Introduce el código único y su DNI/CUIL para verificar su identidad.
  4.  Una vez verificado, el sistema le presenta un formulario para que **él mismo establezca su email y contraseña**.
  5.  Al completar el formulario, el sistema crea el registro correspondiente en la tabla `users`, lo enlaza con su perfil en `productors` y activa su cuenta.

---

## 3. Paneles y Experiencia de Usuario

### 3.1. Arquitectura de Widgets en Dashboards
Los paneles principales (del productor y superadmin) no son vistas monolíticas, sino que se construyen dinámicamente a partir de componentes Blade reutilizables o "widgets".
- **Ejemplo:** La vista `admin.panel.blade.php` puede estar compuesta por:
  - `<x-widgets.stats-cards />`: Tarjetas con KPIs principales.
  - `<x-widgets.map-widget />`: Una vista previa del mapa general.
  - `<x-widgets.recent-activity />`: Una lista de las últimas actividades de auditoría.
- **Ventajas:** Esta arquitectura permite reorganizar, añadir o quitar secciones del dashboard fácilmente y reutilizar la lógica de cada componente en diferentes partes del sistema.

### 3.2. Flujo de Notificaciones
- **Activación:** Un evento en el sistema (ej. una importación finaliza, un usuario pide unirse a una institución) instancia una clase de Notificación específica (ej. `BulkImportCompletedNotification`).
- **Envío y Almacenamiento:** El código invoca el método `notify()` sobre un usuario (ej. `$user->notify(...)`). Como el canal por defecto es `database`, Laravel crea un registro en la tabla `notifications`. El campo `type` guarda el nombre de la clase de la notificación y el campo `data` almacena un JSON con la información relevante.
- **Recuperación y Renderizado:**
  1.  El `NotificationsController` obtiene las notificaciones no leídas de la base de datos para el usuario autenticado.
  2.  Para cada notificación, invoca al `NotificationUIService`.
  3.  Este servicio "traduce" la notificación genérica en un `ViewModel` (un array con `icon`, `title`, `body`, `action_url`) basado en el `type` de la notificación.
  4.  El controlador pasa la lista de `ViewModels` a la vista del panel, que las renderiza en el menú desplegable de notificaciones.

---

## 4. Módulos Principales: Flujos Detallados

### 4.1. Creación de Unidad Productiva (Paso a Paso)
- **Proceso:** Se realiza a través de un asistente de 3 pasos que utiliza la **sesión** para persistir los datos entre pasos.
  1.  **Paso 1 (Datos Básicos):** El productor rellena el nombre del campo, identificador (RNSPA), superficie, municipio y condición de tenencia. Al enviar, los datos se validan y se guardan en `session('form_data')`.
  2.  **Paso 2 (Ubicación):** Se presenta un mapa (Leaflet.js) donde el productor puede colocar un marcador para establecer las coordenadas de su campo. Esta información se captura con JavaScript.
  3.  **Paso 3 (Recursos y Finalización):** Se muestra el último formulario para detallar fuentes de agua, tipo de pasto, suelo, etc. Al hacer clic en "Finalizar", los datos de este formulario, junto con los datos de la sesión (Paso 1) y la ubicación (Paso 2), se envían al método `store` del `UnidadProductivaController`.
- **Guardado Final:** El controlador agrupa toda la información y la delega a la `Action` **`CreateUnidadProductiva`**, que se encarga de crear el registro en la base de datos. Finalmente, se limpia la sesión.

### 4.2. Cuaderno de Campo (Registro de Movimientos)
- **Proceso:**
  1.  El productor selecciona la Unidad Productiva en la que quiere trabajar.
  2.  La interfaz, manejada con **Alpine.js**, le permite añadir múltiples movimientos (ej: "+5 ovejas por nacimiento", "-2 carneros por venta") a una lista temporal en el frontend.
  3.  Al pulsar "Guardar Cambios", el array de movimientos de la lista temporal se convierte en un **string JSON** y se envía al método `store` del `CuadernoDeCampoController`.
  4.  El controlador recibe el JSON, lo decodifica en un array PHP y se lo pasa a la `Action` **`GuardarMovimientosAction`**.
  5.  La `Action` itera sobre el array y crea un registro `StockAnimal` por cada movimiento.
- **Actualización en Tiempo Real:** La creación de cada `StockAnimal` dispara el **`StockAnimalObserver`**, que automáticamente actualiza la cantidad correspondiente en la tabla `stock_actual`. Esto mantiene los totales siempre sincronizados sin necesidad de recálculos.

### 4.3. Módulo de Mapas y Capas
- **Arquitectura:** La generación de mapas se centraliza en `Builders` (ej. `LeafletMapBuilder`).
- **Flujo de Datos:**
  1.  Una vista Blade incluye el componente del mapa.
  2.  El JavaScript del mapa realiza una llamada a un endpoint de la API (ej. `/api/locations`).
  3.  El controlador de la API recopila los datos de las Unidades Productivas (coordenadas, nombre del productor, etc.) y los devuelve en formato **GeoJSON**.
  4.  Leaflet.js procesa este GeoJSON y pinta los marcadores en el mapa.
- **Capas:** El sistema puede superponer capas de datos adicionales (ej. límites municipales). Esto se logra cargando archivos GeoJSON adicionales (almacenados en el proyecto o servidos por otro endpoint) y añadiéndolos al mapa como nuevas capas (`L.geoJSON(layerData).addTo(map)`).

### 4.4. Sistema de Auditoría (Logs)
- **Propósito:** Registrar acciones importantes realizadas por los usuarios para futura referencia. No confundir con los logs de depuración de Laravel.
- **Mecanismo:**
  1.  **`LoggerService`:** Es el punto de entrada centralizado para la auditoría. Ofrece métodos para registrar diferentes tipos de eventos (ej. `logAction($user, $model, 'create')`).
  2.  **Activación:** Cuando ocurre una acción relevante (ej. un admin valida una institución), el controlador o la `Action` correspondiente invoca un método del `LoggerService`.
  3.  **Almacenamiento:** El `LoggerService` crea un nuevo registro en la tabla `logs` a través del modelo `Log` (alias `AppLog`). Este registro contiene el `user_id`, el modelo afectado, la acción realizada y otros metadatos.
  4.  **Visualización:** El panel de superadmin tiene una sección de "Actividad Reciente" que consulta la tabla `logs` para mostrar las últimas acciones. El `LogController` también expone estos datos a través de la API.

---

## 5. Comandos de Consola (Artisan)

El sistema incluye una serie de comandos personalizados de Artisan para realizar tareas de mantenimiento, administración y configuración.

### 5.1. Tareas Programadas y de Mantenimiento
- **`clima:actualizar`**
  - **Propósito:** Actualiza los datos del clima para todos los municipios.
  - **Lógica:** Itera sobre los municipios con coordenadas, consulta la API de OpenWeather y guarda la respuesta JSON en la tabla `clima`.
  - **Uso:** Diseñado para ser ejecutado como una tarea programada (cron job) cada hora para mantener los datos del clima actualizados en los widgets del dashboard.

- **`configuracion:aplicar-programada`**
  - **Propósito:** Gestiona el ciclo de vida de los períodos de declaración de stock.
  - **Lógica:** Verifica si el período de declaración actual ha finalizado. Si es así, lo renueva, calculando la nueva fecha de finalización según la frecuencia configurada. También aplica cualquier cambio de configuración que se haya programado para el siguiente ciclo.
  - **Uso:** Tarea programada esencial que debe ejecutarse diariamente para asegurar la continuidad de los períodos de declaración.

### 5.2. Herramientas Administrativas y de Desarrollo
- **`app:importar-limites-municipios`**
  - **Propósito:** Realiza la carga inicial de los datos geoespaciales para los mapas.
  - **Lógica:** Lee un archivo `municipios.geojson` de la raíz del proyecto y guarda la geometría de cada municipio en la base de datos.
  - **Uso:** Comando de configuración inicial, para ser ejecutado una sola vez.

- **`stock:popular-actual`**
  - **Propósito:** Herramienta de recuperación que reconstruye la tabla de resúmenes `stock_actual` desde cero.
  - **Lógica:** Vacía la tabla `stock_actual` y la repuebla calculando el neto de todos los movimientos históricos (`altas - bajas`) para cada combinación de animal/campo.
  - **Uso:** Para la configuración inicial del sistema o como herramienta de emergencia si la tabla de resúmenes se desincroniza.

- **`stock:add`**
  - **Propósito:** Asistente interactivo de línea de comandos para añadir movimientos de stock.
  - **Lógica:** Guía al usuario a través de una serie de preguntas (ID de productor, UP, especie, cantidad, etc.) para crear un nuevo registro de `StockAnimal`.
  - **Uso:** Herramienta de desarrollo y depuración para insertar datos de prueba rápidamente.
