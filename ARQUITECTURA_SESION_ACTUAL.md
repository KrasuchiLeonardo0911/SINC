
### Actualización sobre el Manejo de Window Insets en Diálogos y BottomSheets

Se ha implementado una solución para el manejo de `WindowInsets` en el `MapDialog` (componente `Step1Ubicacion.kt`). La estrategia adoptada consiste en configurar el `Dialog` con `DialogProperties(decorFitsSystemWindows = false)` para permitir el dibujo detrás de las barras del sistema. Posteriormente, el `BottomSheetScaffold` dentro del diálogo es envuelto en un `Surface` al que se le aplica `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)`.

Esta aproximación ha resuelto la superposición del panel desplegable (BottomSheet) con la barra de navegación virtual en su estado colapsado. Sin embargo, se ha identificado que el indicador de carga que aparece en el mismo diálogo aún no respeta estos insets, lo que sugiere que se necesita una solución más específica para el contenido interno del `BottomSheetScaffold` o para componentes que se superponen a la UI principal del diálogo. Este es un punto a considerar para futuras mejoras en la gestión de insets.
