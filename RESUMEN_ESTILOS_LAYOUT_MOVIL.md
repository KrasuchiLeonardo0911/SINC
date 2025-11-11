# Resumen Técnico de Estilos y Layout (Vista Móvil)

Este documento detalla la estructura visual y la guía de estilos de la aplicación web en su versión móvil. El objetivo es que sirva como referencia para replicar la misma experiencia de usuario en la aplicación nativa de Android con Jetpack Compose.

## 1. Estructura General del Layout (Mobile)

La pantalla principal se compone de tres áreas fundamentales: una barra superior fija, una barra lateral deslizable para la navegación y el área de contenido principal.

En Jetpack Compose, esta estructura se puede implementar de la siguiente manera:

```kotlin
// Concepto de implementación en Jetpack Compose
ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
        // Contenido del Sidebar (Menú de navegación)
    }
) {
    Scaffold(
        topBar = {
            // Barra superior (TopAppBar)
        },
        content = { paddingValues ->
            // Contenido principal de la pantalla
            // Usar paddingValues para el espaciado correcto
        }
    )
}
```

### Componentes Clave:

-   **`ModalNavigationDrawer`**: Contenedor principal que gestiona el menú lateral deslizable.
-   **`Scaffold`**: Proporciona la estructura base de Material Design (TopBar, content, etc.).
-   **`TopAppBar`**: La barra superior de la aplicación.
-   **Contenido Principal**: El área donde se muestra la información de cada pantalla.

---

## 2. Análisis de Componentes Visuales (Mobile)

### A. Barra Superior (`TopAppBar`)

Es una barra fija en la parte superior que contiene el control para abrir el menú y el acceso al perfil de usuario.

-   **Estructura y Clases:**
    -   Contenedor principal: `flex items-center justify-between h-16 px-4`
    -   **Izquierda**: Botón de menú (ícono de "hamburguesa").
    -   **Centro**: Logo de la aplicación.
    -   **Derecha**: Ícono de usuario que despliega un menú.
-   **Estilos para Jetpack Compose:**
    -   **Altura**: `64.dp` (equivalente a `h-16`).
    -   **Color de fondo**: `MaterialTheme.colorScheme.surface` (Blanco en tema claro, `gray-800` en oscuro).
    -   **Elevación**: `4.dp` o aplicar una sombra sutil para separarla del contenido.
    -   **Contenido**: Usar `IconButton` para el menú y un `Box` con un `DropdownMenu` para el perfil de usuario. Los íconos son de `24x24 dp` (`h-6 w-6`).

### B. Barra Lateral (`ModalNavigationDrawer`)

Es un menú que se desliza desde la izquierda, oculto por defecto en móviles.

-   **Comportamiento:**
    -   Aparece por encima del contenido principal, oscureciendo el fondo (`bg-black opacity-50`).
    -   Se abre al pulsar el ícono de menú en la `TopAppBar`.
    -   Se cierra al pulsar fuera de él o al seleccionar una opción.
-   **Estilos para Jetpack Compose:**
    -   **Ancho**: `256.dp` (equivalente a `w-64`).
    -   **Color de fondo**: `MaterialTheme.colorScheme.surface` (Blanco en tema claro, `gray-800` en oscuro).
    -   **Elementos del menú (`NavigationDrawerItem`)**:
        -   **Estado Normal**: Ícono y texto con color `gray-600`.
        -   **Estado Activo/Seleccionado**: Fondo con un color de acento suave (ej. `blue-100` o `gray-200`) y texto con color de acento principal (ej. `blue-600`).
        -   **Padding**: `16.dp` horizontal y `12.dp` vertical.

### C. Área de Contenido Principal

Es el espacio flexible donde se renderiza el contenido de cada pantalla.

-   **Estilos para Jetpack Compose:**
    -   **Color de fondo**: Un gris muy claro para diferenciarlo de los elementos "flotantes". `Color(0xFFF3F4F6)` (similar a `bg-gray-100`).
    -   **Padding general**: `16.dp` a `24.dp` (`p-4` a `p-6`) en los bordes del área de contenido.
    -   **Tarjetas (`Card`)**: El contenido se suele agrupar en tarjetas.
        -   **Color de fondo**: `MaterialTheme.colorScheme.surface` (Blanco).
        -   **Bordes redondeados**: `8.dp` a `12.dp` (`rounded-lg`).
        -   **Sombra/Elevación**: `2.dp` a `4.dp` (`shadow-md`).

---

## 3. Guía de Estilos (Design System)

La aplicación utiliza **Tailwind CSS** con su paleta de colores y sistema de espaciado por defecto. La fuente principal es **Figtree**.

### A. Paleta de Colores (Default Tailwind)

Se recomienda definir estos colores en el `Theme.kt` de Jetpack Compose.

| Nombre      | Hex (Ejemplo) | Uso Común                               |
| :---------- | :------------ | :-------------------------------------- |
| `Primary`   | `#4F46E5`     | Botones principales, links, acentos (Indigo-600) |
| `Secondary` | `#6B7280`     | Texto secundario, íconos (Gray-500)     |
| `Surface`   | `#FFFFFF`     | Fondo de tarjetas, barras (White)       |
| `Background`| `#F3F4F6`     | Fondo principal de la app (Gray-100)    |
| `Error`     | `#EF4444`     | Indicadores de error (Red-500)          |
| `Success`   | `#22C55E`     | Indicadores de éxito (Green-500)        |

*Nota: Para el modo oscuro, los colores se invierten. `Surface` pasa a ser un gris oscuro (ej. `gray-800`) y `Background` un gris más oscuro o negro (ej. `gray-900`).*

### B. Tipografía (Fuente: Figtree)

| Estilo      | Tailwind      | Compose (sugerido) | Uso                  |
| :---------- | :------------ | :----------------- | :------------------- |
| Título XL   | `text-2xl`    | `24.sp`            | Títulos de página    |
| Título Lg   | `text-xl`     | `20.sp`            | Subtítulos, Headers  |
| Título Md   | `text-lg`     | `18.sp`            | Títulos de tarjetas  |
| Base/Cuerpo | `text-base`   | `16.sp`            | Texto principal      |
| Pequeño     | `text-sm`     | `14.sp`            | Texto secundario     |
| Extra Pequeño| `text-xs`    | `12.sp`            | Metadatos, etiquetas |

### C. Espaciado y Tamaños

El sistema se basa en una escala de `4dp`.

| Tailwind | dp (aprox) | Uso Común                               |
| :------- | :--------- | :-------------------------------------- |
| `p-2`    | `8.dp`     | Espaciado interno pequeño en botones    |
| `p-4`    | `16.dp`    | Padding de tarjetas, espaciado de items |
| `p-6`    | `24.dp`    | Padding de secciones principales        |
| `p-8`    | `32.dp`    | Espaciado amplio entre secciones        |

### D. Bordes y Sombras

-   **Radios de Borde**:
    -   `rounded-md`: `6.dp`
    -   `rounded-lg`: `8.dp`
    -   `rounded-full`: `CircleShape`
-   **Sombras**:
    -   Utilizar el parámetro `elevation` en los `Card` o `Surface` de Compose. Un valor de `2.dp` a `4.dp` (`shadow-md`) es un buen punto de partida.

---

## 4. Resumen para el Desarrollador Android

1.  **Estructura principal**: `ModalNavigationDrawer` que contiene un `Scaffold`.
2.  **Barra Superior**: `TopAppBar` con altura de `64.dp`, `IconButton` para el menú y un `DropdownMenu` para el perfil.
3.  **Menú Lateral**: `ModalDrawerSheet` con `NavigationDrawerItem` para las opciones. Replicar los estilos de item activo/inactivo.
4.  **Contenido**: Usar `Card` para agrupar la información, con `elevation` de `2.dp` y `roundedCornerShape` de `8.dp`.
5.  **Estilos**: Definir la paleta de colores y la tipografía en `Theme.kt` basándose en la guía. Usar los `MaterialTheme.colorScheme` y `MaterialTheme.typography` en toda la app para consistencia.
6.  **Iconografía**: Los íconos de Heroicons (usados en la web) tienen un equivalente en los `androidx.compose.material.icons`. Usar la variante `Icons.Outlined` o `Icons.Default` según corresponda.
