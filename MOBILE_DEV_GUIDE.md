# Guía de Desarrollo y Arquitectura para Funcionalidades Móviles

Este documento sirve como una guía para el desarrollo de nuevas funcionalidades, asegurando consistencia y siguiendo las mejores prácticas.

## 1. Creación de Unidad Productiva (Formulario Multi-paso)

### 1.1. Análisis del Flujo Web

El flujo web existente para crear una unidad productiva se basa en un asistente de 3 pasos:
1.  **Datos Básicos:** Formulario con campos de texto y desplegables. Lógica JS para carga dinámica y formato.
2.  **Ubicación:** Pantalla intermedia que redirige a una página de mapa interactivo.
3.  **Detalles Adicionales:** Formulario con campos opcionales.

### 1.2. Propuesta de Adaptación a Móvil (Jetpack Compose)

Se creará una experiencia nativa y fluida en una única pantalla, gestionando el flujo internamente.

-   **Pantalla Única (`CreateUnidadProductivaScreen`):**
    -   Gestionará todo el flujo de creación.
    -   El contenido se actualizará dinámicamente para cada paso, evitando la navegación entre múltiples pantallas.

-   **Componentes de la Pantalla:**
    1.  **Indicador de Pasos Superior:** Un componente visual simple y compacto en la parte superior para mostrar el progreso (Ej: `[ 1 ]----[ 2 ]----[ 3 ]`).
    2.  **Contenido Dinámico por Paso (`LazyColumn`):**
        -   **Paso 1 (Formulario Básico):** Campos de texto y menús desplegables (`DropdownMenu`) para los catálogos locales. La lógica de campos dependientes (ej. Paraje -> Municipio) se manejará en el ViewModel.
        -   **Paso 2 (Ubicación):** Un mapa interactivo (usando `maps-compose`) se integrará directamente en la vista. Un marcador móvil permitirá al usuario seleccionar las coordenadas, que se mostrarán en tiempo real.
        -   **Paso 3 (Formulario Opcional):** Campos adicionales marcados claramente como opcionales.
    3.  **Barra de Navegación Inferior:**
        -   Botones fijos "Anterior" y "Siguiente".
        -   El botón "Siguiente" cambiará su texto a "Finalizar" en el último paso.

-   **Gestión de Estado (`CreateUnidadProductivaViewModel`):**
    -   **Responsabilidades:**
        -   Mantener el estado del paso actual (1, 2, o 3).
        -   Almacenar los datos del formulario en memoria a medida que el usuario avanza.
        -   Contener la lógica de validación para cada paso antes de permitir avanzar.
        -   Orquestar la llamada al `CreateUnidadProductivaUseCase` al finalizar el flujo.

### 1.3. Resumen de la Arquitectura Propuesta

| Característica | Implementación Web | Propuesta Móvil (Compose) | Ventajas de la Propuesta Móvil |
| :--- | :--- | :--- | :--- |
| **Flujo** | 3 páginas separadas + página de mapa | 1 pantalla con contenido dinámico | Más rápido, fluido, menos navegación. |
| **Indicador** | Círculos y texto | `Row` con `Steps` (componente a crear) | Visualmente limpio y adaptado a móvil. |
| **Ubicación** | Redirección a página de mapa | Mapa integrado en el paso 2 | Experiencia de usuario más directa. |
| **Navegación** | Botones en cada página | Barra de botones fija en la parte inferior | Consistente y siempre visible. |
| **Estado** | Sesión de Laravel | `State` en un `ViewModel` | Patrón de estado estándar en Android. |

