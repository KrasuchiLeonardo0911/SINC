# Principios de Arquitectura: Integración de Mapas (`osmdroid`)

Esta sección documenta las decisiones de arquitectura y los hallazgos técnicos relacionados con la implementación de la funcionalidad de mapas en el proyecto.

## 1. Selección de Librería: `osmdroid`

Se optó por `osmdroid` como la librería para la visualización de mapas.

- **Justificación:** Es una alternativa de código abierto y sin costo a servicios como Google Maps, lo cual es ideal para las necesidades del proyecto. Ofrece la flexibilidad necesaria para la visualización de mapas, marcadores y manejo de eventos.

## 2. Configuración y Centralización

La configuración inicial de `osmdroid` es un paso crítico que se ha centralizado para mantener la consistencia y evitar errores.

- **Permisos:** Se requiere el permiso `android.permission.INTERNET` en `AndroidManifest.xml` para la descarga de las teselas (tiles) del mapa.
- **User-Agent:** `osmdroid` exige la configuración de un `User-Agent` para evitar ser bloqueado por los servidores de teselas. Esta configuración se ha centralizado en el método `onCreate` de la clase `SincMobileApp.kt`, asegurando que se ejecute una sola vez al iniciar la aplicación.

```kotlin
// En SincMobileApp.kt
Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
```

## 3. Componentización en Jetpack Compose

Para integrar la vista nativa de `osmdroid` en el entorno declarativo de Compose, se adoptó un enfoque de componentización.

- **`OsmdroidMapView` Composable:** Se creó un componente reutilizable (`app/src/main/java/com/sinc/mobile/app/ui/components/OsmdroidMapView.kt`) que encapsula la `MapView` de `osmdroid`.
- **`AndroidView` Bridge:** Este componente utiliza el composable `AndroidView` para actuar como un puente entre el sistema de vistas de Android y Jetpack Compose.
- **API Declarativa:** El componente expone una API declarativa y amigable con Compose, permitiendo controlar el mapa a través de parámetros como `initialCenter`, `initialZoom`, `animateToLocation` y callbacks como `onMapMove`.

## 4. Manejo de Estado (UDF)

El estado de la pantalla del mapa sigue el patrón de **Flujo de Datos Unidireccional (UDF)**, gestionado por el `ViewModel` de la feature.

- **`CreateUnidadProductivaViewModel`:** Centraliza toda la lógica de negocio y el estado de la UI, incluyendo:
    -   `isMapVisible`: Controla la visibilidad del diálogo del mapa.
    -   `isFetchingLocation`: Gestiona el estado de carga.
    -   `animateToLocation`: Dispara la animación de zoom hacia una nueva ubicación.
- **`StateFlow`:** El `ViewModel` expone el estado a través de un `StateFlow<CreateUnidadProductivaState>`, y la UI (Composable) observa este flujo para redibujarse de forma reactiva.
- **Composables sin Estado (`Stateless`):** El `MapDialog` y sus componentes internos son `stateless`, recibiendo el estado desde el `ViewModel` y notificando eventos hacia arriba (`hoisting events`), como `onConfirmLocation` o `onDismiss`.

## 5. Hallazgos Técnicos y de Rendimiento

- **Rendimiento de Tile Source:** La fuente de las teselas tiene un impacto directo en el rendimiento. Se comprobó que `TileSourceFactory.MAPNIK` (mapa estándar) es significativamente más fluido en el emulador que las imágenes satelitales (`ESRI_WORLD_IMAGERY`). Esto es una consideración clave para la compatibilidad con dispositivos de gama baja.
- **Bug de Redibujado (`invalidate`):** Se descubrió un comportamiento inesperado en `osmdroid` dentro de Compose: tras una animación programática (`controller.animateTo()`), el mapa no se actualizaba y quedaba en blanco.
    -   **Solución:** La solución fue forzar un redibujado manual de la vista llamando a `mapView.invalidate()` inmediatamente después de la llamada a la animación. Este ajuste se encapsuló dentro del `OsmdroidMapView` para que sea transparente a las features que lo consumen.

## 6. Arquitectura de la UI del Mapa

- **`Scaffold` como base:** La pantalla del mapa (`MapDialog`) se estructura con un `Scaffold`, permitiendo una fácil composición de la barra superior (`TopAppBar`) y una barra inferior (`bottomBar`).
- **Panel Inferior Fijo:** Se implementó un panel de información fijo en la `bottomBar` utilizando un `Surface`. Este enfoque es preferible a un `ModalBottomSheet` cuando la información debe estar permanentemente visible. Además, gestiona correctamente los *insets* del sistema para evitar superposiciones con la barra de navegación de Android.
- **Indicador de Carga Contextual:** El indicador de carga se diseñó como una `Card` centrada y no intrusiva, mejorando la experiencia al permitir que el mapa (aunque no interactivo) permanezca visible de fondo.
