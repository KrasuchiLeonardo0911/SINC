## Plan de Implementación: Refactorización de la Pantalla Principal

**Objetivo:** Eliminar la sección "Resumen" y "Tarjetas en construcción" de la pantalla principal y reemplazarla por un dashboard dinámico basado en chips de selección, mostrando resúmenes generales y visuales para Stock, Movimientos, Logística y Clima.

---

### Pasos:

1.  **Crear `DashboardComponents.kt` (en `app/src/main/java/com/sinc/mobile/app/features/home/mainscreen/components/`):**
    *   Este archivo contendrá todos los componentes visuales para el nuevo dashboard.
    *   **`enum class DashboardTab`:** Definirá los cuatro chips seleccionables (STOCK, MOVIMIENTOS, LOGISTICA, CLIMA).
    *   **`OperationsSummarySection` Composable:** Será el componente principal que contendrá los chips y el área de contenido dinámico.
    *   **`DashboardTabs` Composable:** La fila de chips seleccionables.
    *   **`StockSummary` Composable:** Mostrará un gráfico de torta de ovinos/caprinos y KPIs debajo (con datos hardcodeados para el diseño).
    *   **`MovementsSummary` Composable:** Mostrará una línea de tiempo simplificada de movimientos (con datos hardcodeados).
    *   **`LogisticsSummary` Composable:** Mostrará información de recolección y stock pendiente (con datos hardcodeados).
    *   **`WeatherSummary` Composable:** Mostrará información climática (lluvias) y un placeholder para el mapa de calor (con datos hardcodeados).
    *   **Estado:** Se utilizará `remember { mutableStateOf(DashboardTab.STOCK) }` para gestionar la pestaña seleccionada, y `AnimatedContent` para transicionar suavemente entre los resúmenes.

2.  **Modificar `MainContent` en `app/src/main/java/com/sinc/mobile/app/features/home/mainscreen/MainScreen.kt`:**
    *   **Mantener:** Las secciones `Header` y `WeekdaySelector` (el calendario).
    *   **Mantener:** La sección `MyJournalSection` (los botones de navegación con la ilustración superior).
    *   **Reemplazar:** La sección `QuickJournalSection()` existente por una llamada al nuevo componente `OperationsSummarySection()`.

---

**Nota:** Toda la lógica de obtención de datos para los resúmenes (gráficos, KPIs, etc.) será implementada **después** de que el diseño visual sea aprobado. Por ahora, se usarán datos y representaciones visuales "mockeadas".
