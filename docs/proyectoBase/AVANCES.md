# Historial de Avances del Proyecto

Este documento reconstruye la cronología del desarrollo del sistema, desde sus fases iniciales hasta las refactorizaciones y mejoras más recientes.

---

## Fase Inicial: Planificación y Desarrollo con Livewire

*Basado en los archivos `analisis_del_sistema_inicial.txt` y `primeros-avances.txt`.*

### Fase 0: Preparación del Entorno
- **Tecnologías:** Se establece el stack inicial con Laravel, Jetstream (para autenticación) y **Livewire** como framework reactivo principal.
- **Herramientas:** Se configura el entorno de desarrollo con MySQL, PHP, Node.js y Git, estableciendo un flujo de trabajo con ramas `main`, `dev` y `feature/*`.

### Fase 1: Autenticación y Gestión de Usuarios
- **Roles:** Se implementa un sistema de roles (Superadmin, Institucional, Productor) con middlewares para controlar el acceso y la redirección a paneles específicos.
- **Gestión de Cuentas:** Se añade lógica para manejar cuentas activas e inactivas.
- **Importación Masiva v1 (Livewire):** Se desarrolla una funcionalidad para la importación masiva de productores mediante archivos CSV/Excel. La arquitectura inicial se basa en un componente Livewire (`ImportarProductores`) que utiliza un servicio (`CsvExcelProcessor`) para leer los archivos y previsualizar los datos antes de la importación.
- **Registro Individual:** Se crean formularios para el registro manual de productores e instituciones.

### Fase 2: Estructura de Base de Datos
- Se crean las migraciones iniciales a partir del modelo Entidad-Relación.
- Se definen las relaciones Eloquent y se pueblan las tablas de catálogo (especies, razas, etc.) mediante seeders.

---

## Fase de Refactorización y Maduración (Arquitectura sin Livewire)

*Basado en el registro detallado de `avancesProyecto.md`.*

### Septiembre 2025

- **21/09:** Se inicia una refactorización mayor de la arquitectura de mapas, abandonando la implementación original y planificando una nueva basada en Servicios, Acciones y vistas Blade. Se realiza una prueba de concepto exitosa.
- **22/09:** Se implementa el panel general de "Campos" (Unidades Productivas) para el superadmin, siguiendo un enfoque de Desarrollo Guiado por Pruebas (TDD).
- **23/09:** Se finaliza el panel de campos y se diseña la maqueta para la vista de detalles de un campo.
- **24/09:** Se implementa la ficha de "Informe General" de un campo, mostrando datos del establecimiento y un resumen de stock con gráficos.
- **27/09:** Se refactoriza el módulo de estadísticas del administrador, introduciendo "Acciones" para la lógica de negocio y un gráfico de evolución dinámico. Se crea un comando de consola (`stock:add`) para la carga rápida de datos.
- **29/09:** Se realizan múltiples mejoras: se corrige el sistema de actualización de períodos, se mejora el panel de configuración del superadmin y se refactoriza por completo el módulo de notificaciones para hacerlo dinámico y funcional.
- **30/09:** Se reemplazan más componentes Livewire por controladores y vistas Blade, incluyendo la gestión de Unidades Productivas y la vista "Mi Stock" del productor.

### Octubre 2025


- **01/10:** Se reconstruye desde cero el panel de estadísticas del productor para solucionar graves inconsistencias en los datos, adoptando el patrón "Action" para encapsular la lógica y rediseñando la interfaz para mayor claridad.
- **02/10:** Se continúa la refactorización del módulo de estadísticas del administrador. Se implementa **carga asíncrona (AJAX)** para el dashboard, añadiendo spinners de carga para mejorar la UX. Se corrigen errores críticos en el cálculo de stock.
- **09/10:** Se soluciona un **bug crítico** en la lógica de negocio que duplicaba el cálculo del stock al registrar un movimiento. La solución implicó centralizar la lógica en un `StockAnimalObserver`.
- **10/10:** Se realiza una refactorización extensiva de la UI/UX para **mejorar la adaptabilidad móvil (responsive design)** en el panel del productor y del superadmin, afectando menús, tablas y modales.
- **14/10:** **Hito Crítico: Re-arquitectura del Módulo de Importación.**
    - Se abandona por completo el importador basado en Livewire.
    - Se diseña e implementa un nuevo sistema basado en una **arquitectura de "staging"**. Los datos de Excel se cargan primero en tablas temporales (`import_batches`, `producer_import_rows`).
    - Se implementa un flujo de "Reclamar Perfil" para crear productores sin cuenta de usuario, debido a la falta de emails en los datos de origen.
    - La lógica de procesamiento se mueve a un **Job que se ejecuta en segundo plano** (`ProcessProductorImport`) para no bloquear la interfaz.
- **15/10:** Se refina el nuevo módulo de importación, añadiendo acciones en lote (ej. "Crear Cuentas para todos los pendientes") y encapsulando la lógica de negocio en `Actions` y `Jobs`. Se corrigen errores fatales de SQL y de ejecución de Jobs.
- **17/10:** Se finalizan las mejoras en el flujo de importación, añadiendo un estado "omitido" para productores ya existentes, mejores indicadores visuales en la interfaz y clarificando la terminología de la UI para diferenciar entre "perfiles de datos" y "cuentas activadas".
- **23/10:** Se implementan mejoras y correcciones en el módulo de declaración de stock y mapas:
    - **Corrección de Centrado de Mapa:** Se ajustó la inicialización del mapa del superadmin para evitar el "salto" visual al cargar, asegurando que se centre correctamente en las unidades productivas.
    - **Resolución de Errores en Popups de Mapa:** Se corrigieron errores `SyntaxError` y `500 Internal Server Error` en la carga de declaraciones de venta para el mapa, mediante la carga ansiosa de relaciones y el uso de operadores null-safe en la vista Blade.
    - **Validación de Stock en Declaración de Venta:** Se implementó la validación para asegurar que los productores solo puedan declarar stock que realmente poseen, incluyendo:
        - Filtrado de especies a "Ovino" y "Caprino" en el formulario de declaración.
        - Filtrado dinámico de razas y categorías por especie seleccionada en el frontend (Alpine.js).
        - Creación de un endpoint API (`/api/productor/stock-disponible`) para consultar el stock actual.
        - Implementación de una regla de validación personalizada en el backend para verificar la cantidad declarada contra el stock disponible.
    - **Inicio de Configuración de Logística:** Se inició la implementación de la configuración de ciclos de camión de logística, incluyendo:
        - Creación del modelo y migración `ConfiguracionLogistica`.
        - Integración de la configuración en el modal de configuraciones del superadmin, con nuevos campos para frecuencia y fecha de inicio del ciclo.
- **24/10:** Refactorización y finalización del Módulo de Logística para el productor.
    - **Refactorización de UI/UX:** Se transformó el formulario de "Declarar Venta" en un modal asíncrono en la vista "Mi Stock". Se añadió un panel lateral deslizable para gestionar y cancelar declaraciones pendientes. Se pulieron animaciones y la alineación de componentes.
    - **Implementación de "Baja Temporal":** Para prevenir la sobre-declaración de animales, se implementó una lógica de negocio robusta:
        - Al declarar una venta, se crea una **baja automática** en el stock con el motivo "Baja por Declaración".
        - Al cancelar una venta, se genera un **alta automática** con el motivo "Alta por Venta Cancelada".
        - Se añadieron estos nuevos motivos a la base de datos.
    - **Protección de Estadísticas:** Se modificaron las `Actions` de estadísticas (`GetStockMovementsAction`, `CalcularResumenHistorialAction`) para excluir los nuevos motivos operativos, garantizando que los análisis históricos no se vean afectados.
    - **Corrección de Errores Críticos:** Se solucionaron múltiples errores de base de datos (`foreign key`, `not null`) relacionados con la creación de movimientos de stock, asegurando que se enlasen correctamente a la declaración jurada activa, replicando la lógica del Cuaderno de Campo.
- **27/10:** Inicio de una refactorización profunda del frontend para desacoplar Alpine.js de Livewire.
    - **Diagnóstico:** Se identificó que Alpine.js era cargado por Livewire, causando conflictos y errores de JavaScript al intentar crear vistas sin Livewire.
    - **Instalación Standalone:** Se instaló `alpinejs` como dependencia explícita y se refactorizó el sistema de arranque de JS (`bootstrap.js`, `app.js`) para garantizar un orden de carga correcto.
    - **Instalación de Plugins:** Se detectó y se instalaron los plugins faltantes `@alpinejs/persist` y `@alpinejs/collapse`.
    - **Limpieza de Layouts:** Se eliminaron las directivas de Livewire de layouts conflictivos (`layouts/cuaderno.blade.php`), solucionando el error de "múltiples instancias de Alpine".
    - **Resultado:** Se logró un frontend más robusto con una instancia única y explícita de Alpine, eliminando una cascada de errores y dejando el sistema listo para continuar la migración de componentes Livewire a Blade/Alpine puro.

- **28/10:** Refactorización del Módulo de Estadísticas (Admin).
    - **Controlador `ReporteEvolucionController`**: Se creó un controlador dedicado para el reporte visual de evolución de stock, moviendo la lógica de `EstadisticasController` y convirtiéndolo en un controlador de acción única.
    - **API de Movimientos**: La lógica de la API para obtener movimientos por fecha (`movementsForDate`) se movió de `EstadisticasApiController` a una nueva `Action` (`GetMovementsForDateAction`) y se integró en `AdminEstadisticasService`, consolidando la lógica de negocio.
    - **Controlador `AnalisisUPController`**: Se creó un controlador dedicado para el análisis de unidades productivas, moviendo los métodos `analisisUP`, `showAnalisisUP` y sus métodos auxiliares de `EstadisticasController`.
    - **Maquetas de Diseño**: Se crearon dos maquetas de diseño (`analisis-up-maqueta` y `analisis-up-maqueta-2`) para la interfaz de análisis de unidades productivas, con el objetivo de mejorar la presentación y organización de la información.
    - **Corrección de Errores de Carga**: Se corrigieron errores de JavaScript (Chart.js y `id` duplicados) causados por la carga simultánea de scripts de maquetas, ajustando la forma en que se incluyen los scripts en las vistas.
    - **Corrección de Orden de Rutas**: Se corrigió el orden de las rutas en `web.php` para asegurar que las rutas más específicas de las maquetas se procesen antes que las rutas más generales, solucionando errores 404.
- **28/10 (Continuación):** Refactorización del Módulo de Logística y Correcciones en el Mapa.
    - **Mejora de la Lógica de Declaración de Venta:** Se modificó el sistema de declaración de ventas para que funcione como una "lista de venta" editable por unidad productiva.
        - Se eliminó la creación automática de movimientos de stock ("Baja por Declaración" y "Alta por Venta Cancelada") al agregar o quitar animales de la lista de venta. El stock real ahora solo se modificará en la etapa final de la logística.
        - Se ajustó el método de cancelación para que elimine el registro de la declaración en lugar de solo cambiar su estado, permitiendo una gestión más flexible de la lista.
    - **Mejora en la Visualización del Mapa:**
        - Se actualizó el controlador del mapa (`DeclaracionVentaMapController`) para agrupar todas las declaraciones de venta por unidad productiva.
        - Ahora el mapa muestra un único marcador por finca, y el popup asociado lista todos los animales declarados para la venta y el total, en lugar de un marcador por cada declaración individual.
    - **Corrección de Errores en el Mapa:**
        - Se solucionó un error que impedía mostrar los popups de las declaraciones de venta debido a un formato de fecha incorrecto. Se aseguró que el campo `fecha_recogida_estimada` en el modelo `DeclaracionVenta` sea tratado como un objeto de fecha.
        - Se corrigió el problema del icono de marcador rojo roto, reemplazando la URL inestable por un enlace a un CDN confiable (`unpkg.com`).

**29/10:** Refactorización y Pulido Integral del Módulo de "Análisis de Unidades Productivas".
    - **Re-arquitectura del Backend:** Se finalizó la refactorización iniciada el día anterior.
        - Se dividió el "fat controller" `AnalisisUPController` en un controlador web y un nuevo `UnidadProductivaAnalysisController` para la API.
        - Se extrajo toda la lógica de negocio a `Actions` dedicadas (`SearchUnidadesProductivasAction`, `GetUnidadProductivaDetailsAction`, `GetFilteredMovimientosAction`).
        - Se integraron las nuevas acciones en `AdminEstadisticasService`, siguiendo el patrón `Controller -> Service -> Action`.
        - Se desacopló completamente la API para que devuelva únicamente JSON, eliminando la renderización de HTML.
    - **Refactorización del Frontend:** Se re-escribió el componente Alpine.js de la vista `analisis-up.blade.php`.
        - Ahora consume los nuevos endpoints JSON, manejando los datos de forma nativa en lugar de fragmentos HTML.
        - Se implementó un flujo de navegación inteligente que lee parámetros (`rnspa`) de la URL para auto-seleccionar y mostrar una unidad productiva al cargar la página.
    - **Pulido de UI/UX:**
        - **Carga Fantasma (Skeleton Loading):** Se reemplazaron los spinners de carga por una interfaz de "esqueleto" más moderna mientras se cargan los datos.
        - **Paginación Funcional:** Se implementó una paginación completa para el historial de movimientos, mostrando 10 resultados por página y con controles de navegación.
        - **Filtro de Movimientos de Control:** Se excluyeron del historial los movimientos operativos ("Baja por Declaración" y "Alta por Venta Cancelada") para no confundir al usuario.
    - **Corrección de Errores:** Se solucionaron múltiples errores (`Route not defined`, errores de sintaxis y de diseño en tablas) que surgieron durante el proceso de refactorización.
    - **Limpieza de Código:** Se eliminó la vista obsoleta `analisis-up-show.blade.php`.

- **30/10:** Refactorización Completa del Módulo de Estadísticas del Productor.
    - **Rediseño de UI/UX:** Se diseñó e implementó una nueva interfaz de estadísticas para el productor, reemplazando la vista anterior. La nueva interfaz es más robusta y compacta, organizada en dos pestañas:
        - **Stock Actual:** Muestra el desglose del stock actual con gráficos de torta (por especie, raza y categoría) y una tabla jerárquica detallada.
        - **Línea de Tiempo:** Presenta un gráfico de la evolución del stock a lo largo del tiempo y una tabla paginada con el historial de todos los movimientos (altas/bajas), incluyendo filtros por fecha.
    - **Implementación de Backend:**
        - Se actualizó el `EstadisticasController` del productor para orquestar la obtención de todos los datos necesarios para la nueva interfaz.
        - Se modificó `ProductorEstadisticasService` para centralizar la lógica y proveer los datos de resumen, evolución y el nuevo historial de movimientos.
        - Se reutilizó la `FiltrarMovimientosHistorialAction` (existente en el módulo de Cuaderno de Campo) para obtener la lista de movimientos, promoviendo la reutilización de código y asegurando la consistencia de los datos.
    - **Corrección de Bug:** Se solucionó un error fatal (`Call to undefined function str_slug()`) que impedía la correcta renderización de los gráficos, reemplazando la función obsoleta por su equivalente `Str::slug()` en la vista.
- **31/10:** Finalización y Pulido Integral del Módulo de Estadísticas del Productor.
    - **Refactorización a AJAX:** Se transformó la pestaña "Línea de Tiempo" para que sea completamente asíncrona. La carga de datos, el filtrado por fechas y la paginación ahora se realizan mediante `fetch` a una nueva API, eliminando recargas de página.
    - **Corrección de Bugs Críticos:** Se solucionó un bug fundamental en la `Action` de backend (`GenerateStockEvolutionAction`) que impedía la correcta visualización de los datos en el gráfico. Se refactorizó para usar el mismo patrón robusto del panel de administración. También se corrigieron múltiples errores de renderizado en el gráfico (ej. `Maximum call stack size exceeded`) adoptando un patrón de "destruir y recrear" para la instancia del gráfico.
    - **Paridad de Funcionalidades:** Se implementaron todas las características del gráfico del administrador, incluyendo:
        - Un eje X con granularidad de tiempo dinámica (días/meses).
        - Un eje Y que solo muestra enteros y añade un margen superior automático para mejor visibilidad.
        - Líneas del gráfico rectas (`tension: 0`).
        - El modal de configuración (⚙️) para ajustar la altura del gráfico.
    - **Mejoras de UI/UX:** Se añadieron animaciones de esqueleto ("skeleton loading") durante la carga de datos y se ajustó el diseño para una correcta visualización en dispositivos móviles.