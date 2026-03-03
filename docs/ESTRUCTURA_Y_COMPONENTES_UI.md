# Guía de Estructura y Componentes UI (Actualizada 2026)

Este documento detalla la lógica y el propósito de los componentes y pantallas estabilizados durante las recientes sesiones de pulido visual.

---

## 1. Sistema de Navegación y Transiciones

La aplicación utiliza un sistema de navegación basado en `Jetpack Navigation Compose` con animaciones de desplazamiento lateral para reforzar la jerarquía de "pantalla principal" vs "pantalla de detalle".

### Rutas y Animaciones (`AppNavigation.kt`)
*   **Pantallas Principales:** (Home, Clima, Ayuda, etc.) Se acceden desde la barra inferior y utilizan transiciones de desvanecimiento o desplazamientos controlados.
*   **Pantallas de Detalle:** Utilizan el juego completo de transiciones laterales:
    *   `enterTransition / popExitTransition`: Desplazamiento desde/hacia la derecha usando el ancho dinámico `{ it }`.
    *   `exitTransition / popEnterTransition`: Desplazamiento desde/hacia la izquierda para mantener el efecto de profundidad.

---

## 2. Componentes de Cabecero (Headers)

Existen dos tipos principales de cabeceros para mantener la coherencia:

### A. `MinimalHeader.kt` (Cabecero Estándar)
*   **Función:** Proporcionar navegación de retorno y título en pantallas secundarias.
*   **Diseño:** Altura compacta de **48dp**, fondo gris (`SincGrayBackground`).
*   **Gestión de Insets:** Aplica internamente `statusBarsPadding()`. Esto permite que el fondo gris sea continuo desde el borde superior físico del teléfono hasta el final del header.

### B. `StickyMainHeader` (Exclusivo de `MainScreen.kt`)
*   **Función:** Cabecero fijo para el Dashboard que incluye el saludo al usuario y el selector de días de la semana.
*   **Diseño:** Fondo blanco (`SincBackground`).
*   **Implementación:** Se coloca dentro de un `Box` en `MainContent` para permanecer fijo mientras el contenido del dashboard scrollea por debajo.

---

## 3. Sistema de Carga y Sincronización

Se ha implementado un patrón de "Carga Silenciosa" para mejorar la percepción de rendimiento.

### `FullscreenLoader.kt`
*   **Propósito:** Pantalla blanca total con un spinner central y texto descriptivo.
*   **Uso:** Se activa mediante la variable `isInitialLoad` en los ViewModels.
*   **Lógica de Sincronización:** Durante la carga inicial, el ViewModel dispara la sincronización con la API móvil. Se garantiza un tiempo mínimo de visualización (1.5s) para evitar parpadeos y asegurar que el usuario vea que los datos se están actualizando.

---

## 4. Gestión de Alertas Meteorológicas

El sistema de alertas es ahora proactivo y visualmente integrado.

*   **Alertas en Barra de Navegación:** El icono de clima en la `CozyBottomNavBar` reacciona a la lista de alertas en el `MainUiState`.
    *   **Animación:** `InfiniteTransition` que varía el alfa (parpadeo) y el color según el nivel (Rojo, Naranja, Amarillo).
    *   **Lógica de Reconocimiento:** Al entrar a la pantalla de Clima, el `MainViewModel` marca las alertas como `seen`, deteniendo inmediatamente la animación en la barra inferior.
*   **Visualización de Alertas (`WeatherAlertCard.kt`):** Diseño plano (sin tarjetas) con animaciones Lottie de gran tamaño (**140dp**) para máximo impacto visual.

---

## 5. Estandarización de Espaciados

Se ha establecido un estándar de espaciado para que la aplicación "respire":
*   **Margen Superior:** **24dp** entre el borde inferior del header (gris o blanco fijo) y el primer elemento de texto (título).
*   **Padding Inferior:** Uso de `navigationBarsPadding()` en los `Scaffold` para asegurar que el fondo cubra la zona de botones del sistema, y `contentPadding` en las listas para que el scroll sea completo.
