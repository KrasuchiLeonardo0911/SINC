### Avances de la Sesión Actual (17 de enero de 2026)

Esta sesión se centró en la implementación de la interfaz de usuario para la funcionalidad de **Declaraciones de Venta** y su integración completa en la arquitectura de la aplicación, así como una refactorización significativa de un campo clave.

#### 1. Implementación de la Interfaz de Usuario de Declaraciones de Venta (`VentasScreen`)
*   **ViewModel (`VentasViewModel.kt`)**: Se creó el ViewModel encargado de gestionar el estado del formulario y la lista de declaraciones. Incluye la lógica para cargar unidades productivas y catálogos, manejar la selección de opciones, validar el stock localmente antes del envío y procesar el registro de ventas.
    *   Se optimizó la carga inicial de datos utilizando `Flow.combine` para evitar bloqueos y asegurar que la interfaz se cargue rápidamente, incluso con datos inicialmente vacíos.
    *   Se implementó la sincronización proactiva de UPs, Catálogos y Stock al iniciar el ViewModel para garantizar que la validación y los selectores usen datos frescos.
*   **Pantalla (`VentasScreen.kt`)**: Se diseñó la pantalla principal de Ventas con una estructura de pestañas:
    *   **"Nueva Venta" (Formulario)**:
        *   Se implementó un formulario de registro de ventas, alineado con el estilo de los formularios de "Registrar Movimiento" y "Actualizar UP".
        *   Los selectores de "Campo", "Categoría" y "Raza" utilizan `ModalBottomSheet` para una selección de opciones mejorada.
        *   El selector de "Especie" utiliza **Chips horizontales** para una selección rápida.
        *   El campo de "Cantidad" utiliza un **Stepper (botón +/-)** para facilitar la entrada de datos.
        *   Se añadió un botón "Declarar Venta" para enviar el formulario.
    *   **"Pendientes" (Lista)**:
        *   Se implementó una lista (`LazyColumn`) para mostrar las declaraciones de venta pendientes.
        *   Se integró la funcionalidad **"Pull-to-Refresh" (Deslizar para actualizar)** en la lista para permitir la sincronización manual.
        *   Las declaraciones se muestran en tarjetas (`DeclaracionCard`) con un diseño claro y conciso.
*   **Integración de Navegación**:
    *   Se añadió una nueva ruta `Routes.VENTAS` en `AppNavigation.kt`.
    *   Se incluyó un botón "Declarar Venta" en `StockScreen.kt` que navega a `VentasScreen`.
    *   Se actualizó `MainScreen.kt` para manejar la navegación a la nueva pantalla de Ventas.

#### 2. Refactorización del Campo "Observaciones" a "Peso Aproximado en Kg"
*   Se realizó una refactorización completa para reemplazar el campo `observaciones: String?` por `pesoAproximadoKg: Float?` en todas las capas de la aplicación:
    *   **Capa de Datos:** `CreateDeclaracionVentaRequest.kt`, `DeclaracionVentaDto.kt`, `DeclaracionVentaEntity.kt`, `DeclaracionVentaMapper.kt`.
    *   **Capa de Dominio:** `DeclaracionVenta.kt` (modelo), `VentasRepository.kt`, `CreateDeclaracionVentaUseCase.kt`.
    *   **Capa de Presentación:** `VentasViewModel.kt` (estado y lógica), `VentasScreen.kt` (campo de entrada y visualización en tarjetas).
*   El campo de entrada en la UI ahora acepta números flotantes y la tarjeta muestra el peso en Kg.