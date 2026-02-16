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
