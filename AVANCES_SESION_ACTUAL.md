
### Manejo de Window Insets en MapDialog

Se identificó un problema en `Step1Ubicacion.kt` donde el `BottomSheetScaffold` dentro de `MapDialog` no respetaba correctamente los `WindowInsets` de la barra de navegación del sistema, causando que el panel desplegable colapsado se superpusiera con la barra inferior en algunos dispositivos.

La solución inicial implicó:
1.  Configurar `DialogProperties(decorFitsSystemWindows = false)` en el `Dialog` para permitir que el contenido se extienda detrás de las barras del sistema.
2.  Envolver el `BottomSheetScaffold` en un `Surface` y aplicar `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)` a este `Surface`.

Esta solución resolvió el problema de superposición para el `BottomSheet` en su estado colapsado. Sin embargo, se observó que el indicador de carga (`CircularProgressIndicator`) que aparece al presionar "Usar mi ubicación actual" no respetaba estos insets, superponiéndose también con la barra de navegación.

Se intentó una solución más granular aplicando `navigationBarsPadding()` directamente al `sheetContent` y al `Box` principal del `content` del `BottomSheetScaffold`, pero esta fue revertida a petición del usuario.

El estado actual de la solución es la implementación con el `Surface` envolviendo el `BottomSheetScaffold`, que corrige la superposición del panel desplegable colapsado. El problema con el indicador de carga aún persiste y será abordado en una iteración futura.
