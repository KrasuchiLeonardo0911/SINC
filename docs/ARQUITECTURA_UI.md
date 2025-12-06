# Arquitectura de UI y Componentes

Este documento detalla la estrategia de arquitectura para la interfaz de usuario (UI) de la aplicación, enfocada en la creación de un sistema de componentes reutilizables y un layout principal robusto.

## 1. Filosofía y Ubicación

Con el objetivo de mantener un código limpio, consistente y fácil de mantener, se ha establecido una librería de componentes de UI básicos y reutilizables.

- **Ubicación**: Todos los componentes de UI genéricos se encuentran en el paquete:
  `app/src/main/java/com/sinc/mobile/app/ui/components/`

## 2. Componentes Reutilizables

A continuación se describen los componentes básicos creados y su patrón de uso.

### 2.1. `GlobalBanner`

- **Propósito**: Mostrar notificaciones no intrusivas al usuario (ej. "Movimiento guardado", "Error de sincronización").
- **Arquitectura**: Se implementa utilizando un patrón de **estado global** a través de un `object BannerManager`.
    - `BannerManager`: Es un singleton que mantiene el estado del banner actual (`bannerData`).
    - `BannerManager.show(message, type)`: Puede ser llamado desde cualquier parte de la aplicación (ViewModels, Composables) para mostrar un banner.
    - `GlobalBanner()`: Es el composable que se encarga de observar el estado del `BannerManager` y renderizar el banner con las animaciones de entrada y salida correspondientes. Debe ser colocado una sola vez en la jerarquía de UI, en un nivel superior (ej. en `MainScreen`).

### 2.2. `ConfirmationDialog`

- **Propósito**: Presentar un diálogo modal para que el usuario confirme una acción importante (ej. eliminar un elemento, cerrar sesión).
- **Arquitectura**: Es un componente de UI estándar basado en `AlertDialog` de Material 3.
    - Su visibilidad se controla mediante un parámetro booleano (`showDialog`), que debe ser gestionado por el Composable o ViewModel que lo invoca.
    - Acepta lambdas `onConfirm` y `onDismiss` para manejar las acciones del usuario.

### 2.3. `LoadingOverlay`

- **Propósito**: Bloquear la UI y mostrar un indicador de carga durante operaciones críticas que requieren la atención del usuario (ej. login, sincronización inicial).
- **Arquitectura**: Implementado como un `Dialog` no descartable.
    - Su visibilidad se controla con un parámetro booleano `isLoading`.
    - Al ser un `Dialog`, se superpone a todo el contenido de la pantalla actual y previene la interacción del usuario hasta que se oculta.

### 2.4. `EmptyState`

- **Propósito**: Proveer feedback visual en pantallas o listas que no tienen contenido para mostrar.
- **Arquitectura**: Es un componente puramente visual.
    - Acepta un ícono, un título y un mensaje para adaptarse a diferentes contextos.
    - Se utiliza condicionalmente en la lógica de la UI (ej. `if (list.isEmpty()) { EmptyState(...) } else { LazyColumn(...) }`).

### 2.5. `ValidatedTextField`

- **Propósito**: Estandarizar la apariencia y el comportamiento de los campos de texto con validación de errores.
- **Arquitectura**: Es un "wrapper" sobre el `OutlinedTextField` de Material 3.
    - Contiene un `Column` que agrupa el `OutlinedTextField` y un `Text` para el mensaje de error.
    - El mensaje de error solo se muestra si el parámetro `errorMessage` no es nulo.
    - La propiedad `isError` del `OutlinedTextField` se controla automáticamente.

## 3. Estrategia de Layout Principal (`MainScreen.kt`)

La pantalla principal, `MainScreen`, actúa como el contenedor principal de la aplicación post-autenticación y establece la estructura de layout.

- **`Box` Raíz**: El componente raíz de `MainScreen` es un `Box` con `fillMaxSize()`. Esto permite apilar componentes globales que deben superponerse a todo lo demás.
    - `GlobalBanner()` se coloca aquí para asegurar que siempre aparezca en la parte superior, por encima de la barra de navegación y el contenido.

- **`Scaffold`**: Dentro del `Box`, un `Scaffold` proporciona la estructura de Material Design.
    - **`topBar`**: Contiene el `TopBar` personalizado con el logo y los iconos de menú y configuración.
    - **`bottomBar`**: Contiene el `BottomNavBar` para la navegación principal.
    - **Contenido**: El área de contenido del `Scaffold` es donde se renderizan las pantallas principales (`DashboardScreen`, `MovimientoScreen`, etc.).

- **`ModalNavigationDrawer`**: El `Scaffold` está envuelto en un `ModalNavigationDrawer` para proveer el menú lateral (`Sidebar`) como una forma de navegación secundaria.

- **Navegación Interna**: Se eliminó el `NavHost` anidado para prevenir bugs de estado. La navegación entre las pantallas contenidas en `MainScreen` se gestiona con una variable de estado simple (`currentScreen`), lo que resulta en una arquitectura más estable y predecible para este caso de uso.

