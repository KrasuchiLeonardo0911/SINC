
---
### 02 de Enero de 2026 (Parte 2) - Mejoras de UI/UX y Navegación

Esta sesión se centró en una serie de mejoras significativas en la experiencia de usuario (UI/UX) y la estandarización de la navegación y la carga de datos en varias pantallas clave de la aplicación.

**1. Uniformidad y Corrección del `MinimalHeader`**
*   **Problema Identificado:** Inconsistencias en el espaciado superior del `MinimalHeader` en distintas pantallas. `HistorialMovimientosScreen` tenía muy poco margen superior, `StockScreen` demasiado, mientras que `MovimientoFormScreen` presentaba el espaciado correcto.
*   **Causa:** El componente `MinimalHeader` aplicaba internamente `statusBarsPadding()`, pero algunas pantallas (como `MovimientoFormScreen`) también lo aplicaban externamente al `MinimalHeader`, generando un doble padding en algunos casos y un manejo inconsistente en otros.
*   **Solución Implementada:**
    *   Se eliminó el `modifier.windowInsetsPadding(WindowInsets.statusBars)` interno de `MinimalHeader.kt`. La responsabilidad de este padding se centralizó en la pantalla que utiliza el componente, promoviendo mayor flexibilidad.
    *   Se añadió `modifier = Modifier.statusBarsPadding()` explícitamente a todas las llamadas a `MinimalHeader` en `StockScreen.kt` y `HistorialMovimientosScreen.kt` para asegurar un espaciado uniforme y correcto debajo de la barra de estado del sistema.

**2. Corrección de Margen Excesivo en `StockScreen`**
*   **Problema:** Tras la estandarización del `MinimalHeader`, la pantalla `Mi Stock` (StockScreen) aún mostraba un margen superior excesivo para su contenido principal.
*   **Causa:** `MainScreen.kt` estaba pasando a `StockScreen` un `modifier` que incluía los `paddingValues` de su propio `Scaffold` (que ya contabilizaban la altura de la `CozyBottomNavBar`). Esto resultaba en una doble aplicación de padding en el contenido de `StockScreen`.
*   **Solución Implementada:** Se eliminó el `modifier = Modifier.padding(paddingValues)` de la llamada a `StockScreen` dentro del bloque `when` de `MainScreen.kt`. Esto eliminó el padding externo redundante, corrigiendo el margen excesivo.

**3. Refactorización de Navegación y UI de `SettingsScreen`**
*   **Objetivo:** Integrar `SettingsScreen` de manera más coherente con la arquitectura de UI y reubicar su acceso.
*   **Cambios Realizados:**
    *   **`SettingsScreen.kt`:** Se refactorizó para reemplazar su encabezado personalizado con el componente `MinimalHeader` (título "Configuración" y botón de retroceso funcional `onNavigateBack`), incluyendo el `modifier = Modifier.statusBarsPadding()` para consistencia.
    *   **`CozyBottomNavRoutes.kt`:** La constante `PROFILE` fue renombrada a `CAMPOS` para reflejar el nuevo propósito del botón en la barra de navegación inferior.
    *   **`CozyBottomNavBar.kt`:** Se modificó el `BottomNavItem` que antes representaba "Perfil" para que ahora fuera "Campos". Se actualizó su `route` a `CozyBottomNavRoutes.CAMPOS` y su icono a `Icons.Outlined.Map` (con `Icons.Filled.Map` para el estado seleccionado).
    *   **`MainScreen.kt`:** Se actualizó el bloque `when(currentRoute)` para manejar la nueva ruta `CozyBottomNavRoutes.CAMPOS`. Ahora, al seleccionar "Campos" en la barra inferior, se renderiza `CamposScreen`, pasándole la lambda necesaria para navegar a `Routes.CREATE_UNIDAD_PRODUCTIVA`.
    *   **`Header.kt` (componente de `MainContent`):** Se modificó para incluir un icono de usuario (`Icons.Default.Person`) envuelto en un círculo clicable. Al pulsar este icono, se navega a la `SettingsScreen` mediante `navController.navigate(Routes.SETTINGS)`.
    *   **`MainContent.kt`:** Se actualizó su firma para aceptar la lambda `onSettingsClick` y pasarla al componente `Header`.

**4. Ocultar Barra Inferior y Funcionalidad del Botón de Retroceso en `StockScreen`**
*   **Problema:** `StockScreen` siempre mostraba la `CozyBottomNavBar`, y el botón de retroceso en su `MinimalHeader` no funcionaba correctamente debido a que la navegación entre las pantallas principales se gestionaba por estado en `MainScreen`, no por el `NavController` convencional.
*   **Solución Implementada:**
    *   **`MainScreen.kt`:** En el `Scaffold` principal, se añadió una condición al `bottomBar`. Ahora, `CozyBottomNavBar` solo se compone si `currentRoute` *no es* `CozyBottomNavRoutes.STOCK`, ocultando la barra de navegación inferior en esta pantalla.
    *   **`StockScreen.kt`:** La firma del composable se modificó para aceptar una lambda `onBack: () -> Unit` en lugar de `navController: NavController`. Esta lambda se pasa al `MinimalHeader` para su acción de retroceso.
    *   **`MainScreen.kt`:** Al llamar a `StockScreen`, se proporcionó la lambda `onBack = { currentRoute = CozyBottomNavRoutes.HOME }`, asegurando que el botón de retroceso cambie el estado de `MainScreen` para volver a la pantalla de inicio.

**5. Implementación de Pantallas de Esqueleto (Skeleton Loaders) para Carga Inicial**
*   **Objetivo:** Reemplazar el `PullRefreshIndicator` por una interfaz de usuario más amigable (`skeleton loader`) durante la carga inicial de datos al navegar a una pantalla. El `PullRefreshIndicator` ahora solo se activará con el gesto manual del usuario.
*   **Cambios Realizados:**
    *   **`StockViewModel.kt` y `HistorialMovimientosViewModel.kt`:** Se añadió una propiedad `isInitialLoad: Boolean = true` a `StockUiState` y `HistorialMovimientosState` respectivamente. Esta propiedad se inicializa en `true` y se establece en `false` una vez que la operación de carga inicial (llamada desde el bloque `init` del ViewModel) ha finalizado, independientemente de si fue exitosa o no.
    *   **`StockScreenSkeletonLoader.kt` y `HistorialMovimientosSkeletonLoader.kt`:** Se crearon nuevos componentes Composable que simulan el diseño de sus pantallas correspondientes (Tarjetas de Stock, Ítems de Historial) utilizando `Box`es con un fondo `shimmerBrush` para indicar un estado de carga.
    *   **`StockScreen.kt` y `HistorialMovimientosScreen.kt`:** La UI principal de ambas pantallas se envolvió en una estructura `if (uiState.isInitialLoad) { ShowSkeletonLoader() } else { ShowRealContent() }`. Esto asegura que el esqueleto se muestre exclusivamente durante la primera carga de la pantalla, y que el `PullRefreshIndicator` (para actualizaciones manuales) solo se componga y sea visible después de que la carga inicial haya terminado.

**6. Estandarización de Animaciones de Transición entre Pantallas**
*   **Estrategia Adoptada:**
    *   **Pantallas Principales (navegadas por Bottom Nav):** Utilizan animaciones de desvanecimiento (`fade`).
    *   **Pantallas Secundarias/Detalle (navegadas a un nivel más profundo):** Utilizan animaciones de deslizamiento horizontal (`slide-in/out`).
*   **Implementación:**
    *   **`MainScreen.kt`:** Se envolvió el bloque `when(currentRoute)` con una animación `Crossfade`. Esto proporciona una transición suave de desvanecimiento al cambiar entre las pantallas principales (Home, Stock, Historial, Campos) a través de la barra de navegación inferior.
    *   **`AppNavigation.kt`:** Se añadieron las animaciones `enterTransition` (deslizamiento horizontal desde la derecha) y `popExitTransition` (deslizamiento horizontal hacia la derecha al salir) a las rutas `SETTINGS` y `CREATE_UNIDAD_PRODUCTIVA`. Estas animaciones se copiaron de la implementación ya existente en `MOVIMIENTO_FORM` para asegurar una experiencia de navegación coherente para todas las pantallas secundarias y de detalle.

---
