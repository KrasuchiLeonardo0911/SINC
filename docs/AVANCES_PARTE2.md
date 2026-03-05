# Avances de la Sesión Actual (Fecha actual: viernes, 13 de febrero de 2026)

Esta sesión se centró en resolver problemas críticos de persistencia, visualización y experiencia de usuario en el módulo de Unidades Productivas (UPs), adaptándolo a la nueva estructura de la API que soporta relaciones muchos-a-muchos para suelos y recursos forrajeros.

## 1. Solución de Persistencia y Pérdida de Datos

*   **Diagnóstico de Foreign Keys:** Se identificó que la configuración `CASCADE` en las claves foráneas de las tablas `CrossRef` (suelos y pastos) hacia las tablas de catálogos causaba la eliminación automática de los datos de la UP cada vez que se sincronizaban los catálogos (ya que Room borraba los tipos de suelo/pasto antes de reinsertarlos).
*   **Corrección de Esquema:** Se modificó `UnidadProductivaTipoSueloCrossRef` y `UnidadProductivaTipoPastoCrossRef` para usar `onDelete = ForeignKey.NO_ACTION` en las referencias a catálogos.
*   **Actualización de DB:** Se incrementó la versión de la base de datos a `11` en `SincMobileDatabase.kt` para aplicar los cambios de integridad referencial.

## 2. Refactorización de la Capa de Datos y Dominio

*   **Mapeo de Solicitud (Request):** Se verificó y aseguró que `UpdateUnidadProductivaRequest` utilice los nombres de campos exactos requeridos por el backend (`tipo_suelo_id` y `tipo_pasto_id`) mediante anotaciones `@SerialName`.
*   **Sincronización de Lectura:** Se corrigió `UnidadProductivaRepositoryImpl` para asegurar que el método `getUnidadProductivaById` realice un `combine` de 5 flujos (Unidad, SuelosCrossRef, PastosCrossRef y los dos catálogos), garantizando que los nombres de los suelos y pastos se recuperen correctamente de la base de datos local.
*   **Configuración de JSON:** Se configuró el proveedor de `Json` en `NetworkModule.kt` con `explicitNulls = false` para omitir campos nulos en las peticiones `PUT`, permitiendo actualizaciones parciales en el backend sin sobreescribir datos existentes con nulos.

## 3. Mejoras en la Lógica de Negocio (ViewModel)

*   **Carga Robusta de Datos:** Se refactorizó `EditUnidadProductivaViewModel` para inyectar `GetCatalogosUseCase`. Ahora utiliza `combine` para observar tanto la unidad como los catálogos, eliminando condiciones de carrera y asegurando que la UI se actualice en cuanto los datos estén completos en la DB local.
*   **Guardado Atómico:** Se implementaron métodos específicos `saveSuelos()` y `savePastos()` que realizan validaciones específicas y envían solo la sección modificada al servidor, optimizando la comunicación con el backend.
*   **Validación Estricta:** Se eliminó la lógica de auto-rellenado con "Otros" en favor de una validación estricta del 100% realizada en el cliente para garantizar la integridad de los datos enviados.

## 4. Rediseño y Pulido de la Interfaz de Usuario (UI)

*   **Navegación "Slide-in":** Se implementó un flujo de navegación interna en `EditUnidadProductivaScreen` usando `AnimatedContent`. Al presionar editar en una distribución, el editor se desliza desde la derecha cubriendo la pantalla, mejorando la fluidez visual.
*   **Edición Especializada:** Se separó la lógica de edición de ítems:
    *   **Cambio de Tipo:** Se realiza mediante un `ModalBottomSheet` que muestra directamente el catálogo como tarjetas clicables (sin desplegables internos).
    *   **Cambio de Porcentaje:** Se realiza mediante un diálogo dedicado (`PercentageEditDialog`) con teclado numérico.
*   **Ajustes Visuales:**
    *   Se eliminaron los espacios en blanco excesivos en las tarjetas de resumen.
    *   Se restauraron los gráficos de torta (`PieChart`) y leyendas para una visualización rápida.
    *   El botón "Guardar" de los editores ahora respeta los insets del sistema (`navigationBarsPadding`), posicionándose correctamente sobre la barra de navegación de Android.
    *   Se añadió feedback dinámico de la suma de porcentajes (ej: "Falta 40%") para guiar al usuario.

---
**Estado Actual:** El módulo de edición de Unidades Productivas es ahora completamente funcional, robusto y visualmente coherente con los estándares de diseño de la aplicación. Se verificó con éxito la comunicación bidireccional con el servidor de producción.

# Avances de la Sesión Actual (Fecha actual: viernes, 13 de febrero de 2026)

Esta sesión se ha centrado en refinar la experiencia de usuario y la arquitectura de datos del módulo de edición de campos (Unidades Productivas), implementando un patrón de "Guardado Atómico" y una interfaz de usuario más intuitiva.

## 1. Implementación de Atomicidad en la Edición de Datos

*   **Estrategia de Guardado Atómico:** Se ha abandonado el enfoque de un único botón "Guardar Cambios" para todo el formulario. Ahora, cada sección de datos ("Información Básica", "Suelos", "Pastos") se edita y guarda de forma independiente.
*   **Pantalla de Resumen ("Información del Campo"):** La pantalla principal `EditUnidadProductivaScreen` se ha transformado en un dashboard de solo lectura. Todos los campos interactivos (TextFields, Switches) han sido reemplazados por componentes estáticos `InfoRow` o deshabilitados, eliminando la posibilidad de edición accidental y clarificando el estado de la información.
*   **Métodos de Guardado Específicos:** Se han creado métodos dedicados en el `EditUnidadProductivaViewModel` (`saveBasicInfo`, `saveSuelos`, `savePastos`) que envían al servidor únicamente los datos de la sección correspondiente, manteniendo el resto intacto.

## 2. Nueva Experiencia de Edición "Slide-in"

*   **Navegación Interna Fluida:** Se ha implementado un sistema de navegación interna utilizando `AnimatedContent`. Al pulsar el botón de editar en una tarjeta de resumen, una pantalla de edición dedicada ("Editor") se desliza desde la derecha, cubriendo la vista principal.
*   **Editores Implementados:**
    *   **`BasicInfoEditorScreen`:** Permite editar Superficie, Condición de Tenencia y la opción "Habita".
    *   **`DistributionEditorScreen`:** Un editor reutilizable y potente para las listas de distribución (Suelos y Pastos).

## 3. Refinamiento de la Edición de Distribuciones (Suelos y Pastos)

*   **UI de Resumen:** Las tarjetas de distribución en la pantalla principal ahora muestran un gráfico de torta (`PieChart`) y una leyenda clara, sirviendo como visualización rápida del estado actual.
*   **UI de Edición:** El editor dedicado ofrece una lista limpia de ítems donde se ha separado la lógica de modificación:
    *   **Cambiar Tipo:** Se realiza borrando el ítem y añadiendo uno nuevo desde el catálogo (mostrado en un `ModalBottomSheet` sin desplegables).
    *   **Editar Porcentaje:** Se realiza tocando el icono de lápiz junto al número, lo que abre un diálogo simple (`PercentageEditDialog`).
*   **Validación Robusta:** El botón "Guardar" en el editor de distribuciones permanece deshabilitado hasta que la suma de los porcentajes sea exactamente 100% (o la lista esté vacía/unitaria), forzando la integridad de los datos antes de enviarlos al servidor.

## 4. Ajustes Visuales y Técnicos

*   **Manejo de Insets:** Se aseguró que todos los componentes, especialmente los botones de guardado en la parte inferior, respeten los insets de la barra de navegación del sistema (`navigationBarsPadding`).
*   **Limpieza de UI:** Se eliminaron espacios en blanco innecesarios en las tarjetas y se optimizó la jerarquía de componentes composables.
*   **Corrección de Errores:** Se solucionaron problemas de compilación relacionados con scopes de variables y referencias a iconos.

---
**Estado Actual:** La aplicación cuenta ahora con un flujo de edición de campos moderno, seguro y atómico. La persistencia local y la sincronización con el backend funcionan correctamente, y la interfaz de usuario guía al usuario de manera efectiva a través de la visualización y modificación de datos complejos.

# Avances de la Sesión Actual (Fecha actual: viernes, 13 de febrero de 2026)

Esta sesión se ha centrado en refinar la experiencia de usuario, asegurar la integridad de los datos y estandarizar la navegación en los módulos de gestión de campos y acceso a la aplicación.

## 1. Atomicidad y Refactorización del Módulo de Campos (UPs)

*   **Dashboard Informativo:** La pantalla principal de edición se transformó en "Información del Campo", una vista estática de solo lectura que sirve como resumen.
*   **Guardado Atómico por Sección:** Se implementaron editores independientes que se deslizan desde la derecha ("Slide-in") para:
    *   **Información Básica:** Superficie, Tenencia y Habita.
    *   **Agua:** Fuentes y distancias para consumo humano y animal.
    *   **Suelos y Pastos:** Distribución de tipos con porcentajes.
    *   **Observaciones:** Editor con estética de "cuaderno con renglones".
*   **Validación Estricta:** En los editores de distribución, se bloquea el guardado si la suma no es exactamente 100% o si existen ítems con 0%, garantizando datos consistentes.

## 2. Solución de Persistencia y Sincronización

*   **Integridad Referencial:** Se corrigió un bug crítico donde Room borraba los datos de la UP al sincronizar catálogos. Se cambió el comportamiento de las Foreign Keys a `NO_ACTION` y se actualizó la base de datos a la **versión 11**.
*   **Optimización de Red:** Se configuró el serializador JSON para omitir campos nulos, permitiendo actualizaciones parciales seguras en el backend.

## 3. Mejoras en el Flujo de Acceso (Login)

*   **Primer Ingreso:** Se añadió el botón "Es mi primer ingreso" en la pantalla de login, redirigiendo a los nuevos usuarios al flujo de validación por correo para definir su contraseña inicial.
*   **Interfaz Natural:** Se cambió el texto del botón principal de "Login" a "Entrar" para una mejor localización.

## 4. Experiencia de Usuario (UI/UX)

*   **Feedback de Guardado:** Se implementó un nuevo overlay blanco a pantalla completa con spinner central que transiciona a un mensaje de éxito, permitiendo un retorno manual y suave a la pantalla anterior.
*   **Ayuda Contextual:** Se añadió un icono de información (`i`) en el formulario de creación de campos con diálogos explicativos sobre el RNSPA/Identificador.
*   **Barra de Navegación:** Se cambió la etiqueta "Notificaciones" por "Alertas" para optimizar el espacio y la claridad.
*   **Modales de Selección:** Se rediseñó el contenido de los `ModalBottomSheet` con un estilo delineado (outlined), fondo blanco y marcadores de selección (Check) en el color principal.

## 5. Corrección en Sistema de Tickets

*   **Enrutamiento de RNSPA:** Se ajustó el tipo de consulta a `consulta_negocio` en las solicitudes de identificador desde el formulario de creación, asegurando que lleguen al buzón del Administrador en lugar de Soporte Técnico.

---
**Estado Actual:** La aplicación móvil cuenta con una arquitectura de edición atómica robusta, validaciones de negocio en tiempo real y una interfaz de usuario pulida y coherente. El foco de desarrollo se traslada ahora al backend para complementar estas mejoras.

# Registro de Avances - Sesión 15 de Febrero de 2026

## Módulo de Ventas y Logística (Refactorización Completa)

Se ha adaptado la aplicación al nuevo ciclo de logística robusto, mejorando tanto la integridad de los datos como la experiencia del usuario.

### 1. Capa de Datos y Dominio (Backend Integration)
*   **Actualización de Modelos**: Se añadieron los campos de seguimiento logístico (`fechaRecogida`, `fechaMatadero`, `fechaEntrega`) y `motivoRechazo` en `DeclaracionVentaDto`, `Entity` y `Domain`.
*   **Enum de Estados**: Se implementó `LogisticaStatus` para centralizar la lógica de los nuevos estados (`comprometido`, `recogido`, `en-matadero`, `entregado` y estados de rechazo).
*   **Endpoints de API**: 
    *   Se integró el endpoint `DELETE` para la cancelación de declaraciones.
    *   Se integró el endpoint de consulta de stock disponible para venta.
*   **Repositorios y Casos de Uso**: Implementación de `cancelDeclaracion` y `getStockDisponible`. Creación de los casos de uso correspondientes.

### 2. Persistencia y Resiliencia
*   **Base de Datos**: Se incrementó la versión de DB a **13**.
*   **Sincronización Robusta**: Se solucionó la deuda técnica de las pantallas vacías tras actualizaciones. 
    *   Se añadió `getEspecieCount()` en `EspecieDao`.
    *   Se modificó `CatalogosRepositoryImpl` para que fuerce una sincronización si las tablas están vacías, incluso si la versión de catálogos en `SharedPreferences` coincide con la del servidor.

### 3. Interfaz de Usuario (UI/UX)
*   **Pantalla de Seguimiento**: 
    *   Se renombró la pestaña "Pendientes" a **"Seguimiento"**.
    *   Se rediseñó la `DeclaracionCard` incluyendo un **Stepper (indicador de pasos)** visual que muestra el progreso del animal (Comprometido -> Recogido -> Planta -> Final).
    *   Se añadió el botón **"Cancelar Venta"** visible únicamente cuando la declaración está en estado `comprometido`.
*   **Historial de Ventas**:
    *   Se rediseñó el `DetalleVentaSheet` para mostrar una **Línea de Tiempo** con fechas reales de cada hito logístico.
    *   Se corrigió el problema que mostraba IDs en lugar de nombres de catálogos.
*   **Filtrado Inteligente**: El formulario de "Nueva Venta" ahora filtra automáticamente los catálogos de Especies, Razas y Categorías basándose en el stock real disponible en la Unidad Productiva seleccionada.
*   **Estética y Acabado**:
    *   Se corrigió el efecto **"Ripple" cuadrado** en chips y tarjetas, aplicando el recorte circular/redondeado correspondiente.
    *   Se actualizó el texto informativo a **"Peso Vivo Aproximado"** en toda la aplicación.

### Estado Final
*   **Compilación**: Exitosa (`./gradlew assembleDebug`).
*   **Versión de DB**: 13.
*   **Funcionalidad**: Verificada y robustecida frente a borrado de datos locales.
# Avances de la Sesión Actual (24 de Febrero de 2026)

## Implementación de Alertas Meteorológicas (Infraestructura y Datos)

Se ha implementado la funcionalidad completa para obtener, almacenar y gestionar alertas meteorológicas, siguiendo la arquitectura limpia y el patrón offline-first.

### 1. Capa de Dominio (`:domain`)
- **Modelo de Negocio**: Creación de `WeatherAlert.kt`.
- **Repositorio**: Definición de la interfaz `WeatherAlertRepository.kt`.
- **Casos de Uso**:
    - `GetWeatherAlertsUseCase`: Lectura reactiva desde la DB.
    - `SyncWeatherAlertsUseCase`: Sincronización con la API móvil.
    - `SaveWeatherAlertUseCase`: Guardado manual (útil para Push).

### 2. Capa de Datos (`:data`)
- **Networking**:
    - Creación de `WeatherAlertDto.kt` y `WeatherAlertResponseDto.kt`.
    - Definición de `WeatherAlertApiService.kt` con el endpoint `GET /api/movil/alertas-meteorologicas`.
- **Persistencia (Room)**:
    - Creación de `WeatherAlertEntity.kt`.
    - Implementación de `WeatherAlertDao.kt` con soporte para limpieza automática de alertas expiradas.
    - Actualización de `SincMobileDatabase.kt` a la **Versión 15**.
- **Repositorio**: Implementación de `WeatherAlertRepositoryImpl.kt` y mappers asociados.
- **DI (Hilt)**: Configuración en `NetworkModule`, `DatabaseModule` y `RepositoryModule`.

### 3. Capa de Presentación (Lógica)
- **Firebase Messaging**: Actualización de `MyFirebaseMessagingService.kt` para detectar notificaciones de tipo `weather_alert`. Al recibirlas, se dispara una sincronización automática en segundo plano para actualizar la base de datos local.
- **MainViewModel**:
    - Integración de los casos de uso de clima.
    - Observación continua del flujo de alertas para el Dashboard.
    - Sincronización proactiva durante la inicialización de la App.

### 4. Verificación y Calidad
- **Compilación**: Se verificó la compilación exitosa del módulo `:data`, confirmando la correcta generación de código de Room y Hilt.
- **Testing**: Se ejecutó exitosamente el test unitario `WeatherAlertRepositoryTest.kt`, validando la lógica de sincronización, mapeo y manejo de errores de la API.

---
**Estado Actual**: La infraestructura de datos está 100% operativa. Las alertas ya se descargan y guardan automáticamente. Queda pendiente el diseño visual de la sección "Clima" en el Dashboard para la próxima sesión.

# Registro de Avances - Sesión de Pulido y Estabilización UI (26 de Febrero de 2026)

Hoy se realizó una sesión intensiva de refinamiento visual y técnico, enfocada en la uniformidad, la experiencia de usuario (UX) y la estabilidad de la navegación.

---

## 1. Iconografía y Branding
*   **Icono Adaptativo:** Se corrigió el error de recorte del logo de la aplicación al instalarse.
    *   Se creó `ic_adaptive_foreground.xml` utilizando un `layer-list`.
    *   Se ajustó el tamaño del logo de Ovinos a **52dp** dentro del contenedor estándar de **108dp**, garantizando que permanezca dentro de la "zona segura" circular de Android (66dp).
*   **Reubicación de Clima:** Se restauró el acceso a Clima en la `CozyBottomNavBar` tras explorar otras ubicaciones, manteniendo la navegación original pero mejorada.

## 2. Estandarización del Sistema de Colores
*   **Refactorización de la Paleta:**
    *   Se redefinió `SincBackground` como Blanco Puro (`0xFFFFFFFF`).
    *   Se movió el gris claro anterior a `SincGrayBackground` (`0xFFF5F5F7`), utilizándolo ahora estratégicamente para headers y bloques de separación.
*   **Limpieza Arquitectónica:**
    *   Se eliminó la carpeta de temas duplicada (`app/ui/theme`) que causaba conflictos de importación.
    *   Se unificaron todas las referencias a colores bajo el paquete `com.sinc.mobile.ui.theme`.
    *   Se restauraron variables de compatibilidad (`DarkerGray`, `CozyLavender`, etc.) para asegurar el funcionamiento de componentes heredados.

## 3. Arquitectura de Layout y Navegación
*   **Cabecero Fijo (MainScreen):**
    *   Se implementó el `StickyMainHeader` (Blanco) fijo en la parte superior mediante un layout de `Box`.
    *   Se eliminó el `topBar` del `Scaffold` raíz para estabilizar las transiciones de `Crossfade`, eliminando el efecto de "salto" o "subida" del contenido al navegar.
    *   Se compensó la altura del cabecero (~130dp) con `Spacer`s dinámicos para evitar el corte de las tarjetas del dashboard.
*   **Headers Compactos:**
    *   Se redujo la altura de `MinimalHeader` y `SelectionAppBar` de **64dp** a **48dp**.
    *   Se ajustó el tamaño de los iconos de navegación de **36dp** a **32dp**.
    *   Se integró el manejo de `statusBarsPadding()` dentro del fondo gris del header, logrando que el color sea continuo hasta el borde superior del dispositivo.
*   **Espaciado Uniforme:** Se estandarizó el `contentPadding` superior a **24dp** en todas las pantallas principales para mejorar la jerarquía y legibilidad.
*   **Animaciones de Desplazamiento:**
    *   Se configuraron las rutas `SELECCION_CAMPO` y `MOVIMIENTO_FORM` con el juego completo de transiciones (`enter`, `exit`, `popEnter`, `popExit`).
    *   Se utilizó el ancho dinámico de pantalla `{ it }` en lugar de valores fijos para garantizar un desplazamiento fluido en cualquier resolución.

## 4. Pantallas de Carga y Sincronización
*   **Componente `FullscreenLoader`:** Se desarrolló un nuevo componente de carga inicial (fondo blanco, spinner primario y mensaje descriptivo).
*   **Lógica de Carga Inicial:**
    *   Se refactorizaron los ViewModels de **Stock**, **Clima** e **Historial** para separar el estado `isInitialLoad` del `isLoading` manual.
    *   La sincronización de datos ahora ocurre "bajo" la pantalla blanca de carga con una duración mínima de **1.5 segundos**, evitando parpadeos y ocultando la animación del pull-to-refresh durante la navegación.

## 5. Mejoras Específicas por Pantalla
*   **Clima:**
    *   Rediseño completo a formato "Flat": Se eliminaron las tarjetas contenedoras de alertas.
    *   **Animaciones Lottie:** Se aumentó el tamaño a **140dp** y se reasignaron los assets (Rojo: Warning, Amarillo: Storm, Naranja: Exclamation).
    *   **Alertas Proactivas:** El icono de la barra inferior parpadea y cambia de color según el nivel de alerta. Se implementó lógica para detener la animación automáticamente al entrar a la pantalla.
*   **Stock & Historial:**
    *   Se eliminó la barra de navegación inferior en estas pantallas para una experiencia a pantalla completa.
    *   Se aplicó `navigationBarsPadding()` para asegurar que el fondo blanco cubra de forma sólida el área de botones de Android.
*   **Historial de Movimientos:**
    *   Se fijó el selector de mes en la parte superior.
    *   Se eliminaron fondos circulares en las flechas indicadoras.
    *   Se extendieron los divisores horizontales a todo el ancho de la pantalla.

## 6. Correcciones Técnicas
*   Se resolvieron múltiples errores de compilación relacionados con importaciones mal ubicadas, parámetros faltantes en ViewModels y discrepancias de tipos en animaciones de Compose.
*   Se verificó la funcionalidad de las animaciones de desplazamiento en builds de `release`.

# Avances de la Sesión Actual (03 de Marzo de 2026)

## 1. Corrección Crítica: Límites de Municipios (MultiPolygon)

- **Problema Detectado**: El municipio San José no se dibujaba correctamente en el mapa debido a que su GeoJSON es un `MultiPolygon` que contiene una geometría residual (4 puntos) antes del límite real. La lógica anterior solo tomaba el primer polígono encontrado.
- **Solución Implementada**:
    - Se analizó la estructura real del servidor de producción mediante `curl`.
    - Se refactorizó la lógica de parseo en `CatalogosRepositoryImpl.kt` para que, en el caso de `MultiPolygon`, itere sobre todos los polígonos y seleccione automáticamente aquel que tenga la mayor cantidad de coordenadas en su anillo exterior.
    - Esto garantiza que se visualice siempre el contorno principal del municipio, ignorando ruidos o errores de digitalización.

## 2. Rediseño Integral de la Pantalla "Mi Stock"

Se ha transformado la pantalla para ofrecer una experiencia más limpia, interactiva y profesional.

### 2.1. Nueva Arquitectura de Visualización
- **Simplificación Visual**: Se unificó toda la pantalla con fondo blanco puro, eliminando las franjas grises de separación para un aspecto más moderno e integrado.
- **Flujo de Navegación**: Se eliminaron las listas extensas de la pantalla principal. Ahora el usuario interactúa con el resumen general y profundiza en una nueva pantalla de detalle (`StockDetailScreen`).

### 2.2. Tarjeta de Resumen General Interactiva
- **Gráfico de Dona**: Se restauró el gráfico de anillo original, añadiéndole una sombra sutil (`shadow`) y centrando el contador total de animales en su interior.
- **Leyenda Estilo "Mis Campos"**: Las filas de Ovinos y Caprinos fueron rediseñadas para coincidir con la estética de la lista de campos:
    - Punto de color indicador de especie.
    - Nombre de la especie en negro y negrita.
    - Subtítulo gris indicando el porcentaje de representación en el stock.
    - Icono `ChevronRight` al final de la fila para indicar navegabilidad.
    - Líneas divisorias más marcadas (`outlineVariant`) para una mejor estructura.

### 2.3. Selector de Vistas (ModalBottomSheet)
- Al pulsar sobre una especie, se abre un panel inferior que permite elegir cómo visualizar los datos:
    - **Vista Total**
    - **Por Categoría**
    - **Por Raza**
- Los botones de selección (chips) mantienen el estilo del resto de la aplicación (bordes redondeados, colores institucionales).

### 2.4. Infraestructura de Navegación
- Se actualizó el objeto `Routes` y `AppNavigation.kt` para soportar la nueva ruta `STOCK_DETAIL`, la cual recibe parámetros dinámicos de especie y tipo de agrupación.

## 3. Ajustes Estéticos y de Identidad
- **Paleta de Colores**: Se definieron colores específicos y consistentes: **Verde Oscuro** para Ovinos y el **Bordó Principal** de la app para Caprinos.
- **Consistencia de Datos**: Se implementó el redondeo matemático (`roundToInt`) para todos los porcentajes de la pantalla, evitando decimales innecesarios y mejorando la legibilidad.

---
**Estado del Proyecto**: El sistema compila correctamente y el nuevo flujo de navegación de Stock es funcional y estéticamente superior.
