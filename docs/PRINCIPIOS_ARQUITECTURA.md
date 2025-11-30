# Principios de Arquitectura del Proyecto

Este documento describe la arquitectura limpia que se seguirá para el desarrollo de la aplicación Android. La estructura se basa en la separación de responsabilidades en tres capas principales, organizadas en módulos de Gradle independientes.

## 1. Capa de Dominio (`:domain`)

- **Propósito**: Contiene la lógica de negocio pura y las reglas fundamentales de la aplicación. Es el núcleo de la arquitectura.
- **Contenido**:
    - **Modelos de Negocio**: Representaciones de las entidades centrales (ej. `AuthResult`). Son clases de Kotlin/Java simples (POKOs/POJOs).
    - **Casos de Uso (Use Cases/Interactors)**: Clases que encapsulan una única acción o lógica de negocio específica (ej. `LoginUseCase`).
    - **Interfaces de Repositorio**: Contratos que definen cómo se debe acceder a los datos, sin conocer los detalles de la implementación (ej. `interface AuthRepository`).
- **Regla Clave**: Este módulo debe ser de Kotlin/Java puro. **No debe tener ninguna dependencia del framework de Android** (`android.*`) ni de ninguna otra capa. Esto garantiza que la lógica de negocio sea independiente de la plataforma, reutilizable y fácil de probar unitariamente.

## 2. Capa de Datos (`:data`)

- **Propósito**: Implementar los contratos definidos en la capa de dominio. Se encarga de obtener y almacenar los datos, decidiendo si la fuente es una API remota, una base de datos local, un archivo, etc.
- **Contenido**:
    - **Implementaciones de Repositorio**: Clases que implementan las interfaces de repositorio del dominio (ej. `AuthRepositoryImpl`).
    - **Fuentes de Datos (Data Sources)**:
        - **Remotas**: Clases que interactúan con APIs web (usando librerías como Retrofit o Ktor). En nuestro caso, `AuthApiService`.
        - **Locales**: Clases que interactúan con una base de datos local (usando librerías como Room o SQLDelight).
    - **Modelos de Datos (DTOs)**: Representaciones de los datos tal como vienen de la fuente (ej. `LoginRequest`, `LoginResponse`). Incluye lógica para mapear entre modelos de datos y modelos de dominio.
- **Regla Clave**: La capa de datos conoce y depende de la capa de dominio, pero **el dominio no sabe nada sobre la capa de datos**.

## 3. Capa de Presentación (`:app`)

- **Propósito**: Mostrar la interfaz de usuario (UI) y manejar las interacciones del usuario. Actúa como el punto de entrada a la aplicación.
- **Contenido**:
    - **UI (Vistas)**: Composables de Jetpack Compose (`LoginScreen`), Activities o Fragments que dibujan la pantalla.
    - **ViewModels**: Clases que sobreviven a los cambios de configuración y exponen el estado de la UI (`LoginViewModel`). Orquestan las llamadas a los casos de uso del dominio y preparan los datos para ser mostrados por la UI.
    - **Inyección de Dependencias**: Lógica para proveer las dependencias necesarias a las clases de esta capa (usando Hilt).
- **Regla Clave**: Esta capa depende de la capa de dominio. Es la única capa que puede tener dependencias directas del SDK de Android y de librerías de UI.

---

## 4. Patrones y Librerías Implementadas

Esta sección detalla las herramientas específicas elegidas para implementar la arquitectura.

- **Inyección de Dependencias (DI) con Hilt**:
    - **Rol**: Hilt gestiona la creación y provisión de dependencias a lo largo de la aplicación. Nos permite desacoplar las clases, facilitando las pruebas y la mantenibilidad.
    - **Implementación**: Usamos `@HiltAndroidApp` en la clase Application, `@AndroidEntryPoint` en Activities, `@HiltViewModel` en ViewModels y `@Inject constructor` en las clases que queremos que Hilt sepa construir (como Repositorios y Casos de Uso). Los `@Module` y `@Provides` / `@Binds` (ej. `NetworkModule`, `RepositoryModule`) se usan para enseñar a Hilt a crear instancias de clases que no poseemos, como las de librerías externas (Retrofit) o para vincular interfaces con sus implementaciones.

- **Comunicación de Red con Retrofit y OkHttp**:
    - **Rol**: Retrofit es un cliente HTTP type-safe que usamos para definir nuestras llamadas a la API de forma declarativa a través de interfaces (`AuthApiService`). OkHttp actúa como el motor subyacente que ejecuta las llamadas. Gson se usa como el convertidor para serializar/deserializar objetos Kotlin a/desde JSON.
    - **Implementación**: La instancia de Retrofit se configura y provee a través del `NetworkModule` de Hilt.

- **Patrón MVVM (Model-View-ViewModel)**:
    - **Rol**: Es el patrón principal de la capa de presentación. Separa la lógica de la UI de su estado y de la lógica de negocio.
    - **Implementación**:
        - **View**: `LoginScreen` (Composable de Jetpack Compose). Es una representación pasiva del estado.
        - **ViewModel**: `LoginViewModel`. Contiene la lógica de la UI, maneja los eventos del usuario (`onLoginClick`) y expone el estado (`LoginState`) a la vista a través de objetos `State` de Compose.
        - **Model**: En el contexto de MVVM, el "Model" es la capa de Dominio (Casos de Uso y Repositorios) a la que el ViewModel accede.

- **Manejo de Asincronía con Corrutinas de Kotlin**:
    - **Rol**: Todas las operaciones de I/O (red, base de datos) deben realizarse fuera del hilo principal para no bloquear la UI. Las corrutinas son la herramienta estándar en Android moderno para manejar tareas asíncronas de forma secuencial y legible.
    - **Implementación**: Usamos `suspend fun` en toda la cadena de llamadas (desde la interfaz de Retrofit, pasando por el Repositorio y el Caso de Uso) y `viewModelScope.launch` en el `ViewModel` para iniciar la operación en un hilo de fondo de forma segura.

---

## 5. Patrones y Decisiones Adicionales

Esta sección documenta patrones específicos y decisiones de implementación que surgieron durante el desarrollo.

- **Manejo Robusto de Respuestas de API con `Response<ResponseBody>`**:
    - **Contexto**: Durante la depuración del login, se encontró que Retrofit puede lanzar una `MalformedJsonException` si la API devuelve un cuerpo de error (ej. JSON de validación con código 422) cuando la función está tipada para un cuerpo de éxito (ej. `LoginResponse`).
    - **Decisión**: Para evitar este problema y tener control total sobre el parseo, se adoptó el patrón de tipar las respuestas de Retrofit como `Response<ResponseBody>`.
    - **Implementación**: En el `Repository`, se comprueba `response.isSuccessful`. Si es `true`, se parsea `response.body()` al DTO de éxito. Si es `false`, se parsea `response.errorBody()` al DTO de error correspondiente. Esto previene crashes y permite un manejo de errores explícito y robusto.

- **Theming en Jetpack Compose y "Material You"**:
    - **Contexto**: Se detectó que los colores de la marca no se aplicaban a los componentes de Material 3 (`Button`, `CircularProgressIndicator`) a pesar de estar definidos en el `ColorScheme`.
    - **Causa**: La plantilla estándar de temas de Material 3 activa por defecto el **Color Dinámico (Material You)** (`dynamicColor = true`) en dispositivos con Android 12+. Esta función extrae colores del fondo de pantalla del usuario, ignorando el `ColorScheme` personalizado.
    - **Decisión**: Para aplicaciones con una identidad de marca fuerte que requiere colores específicos, es necesario deshabilitar esta funcionalidad. Se estableció `dynamicColor = false` en la función `SINCTheme` para garantizar que siempre se use la paleta de colores definida en la app.

- **Implementación de Splash Screen Moderna**:
    - **Decisión**: Se optó por usar la API `androidx.core:core-splashscreen` en lugar de una `Activity` dedicada, siguiendo las recomendaciones actuales de Google para el rendimiento y la consistencia en el inicio de la app.
    - **Implementación**: Requiere la creación de un tema específico (`Theme.App.Starting`) que hereda de `Theme.SplashScreen` y define el fondo, el ícono y el tema post-splash. Este tema se aplica a la `Activity` de entrada en el `AndroidManifest.xml` y se activa con `installSplashScreen()` en `MainActivity`.
    - **Ajuste de Icono**: Para evitar el recorte de logos no circulares por la máscara de la API, se utiliza un `inset` drawable como recurso para el ícono (`windowSplashScreenAnimatedIcon`), lo que permite añadir un padding efectivo.

- **Navegación con Jetpack Navigation Compose**:
    - **Rol**: Gestiona el flujo entre las diferentes pantallas (Composables) de la aplicación de una manera idiomática en Compose.
    - **Implementación**: Se define un `NavHost` central en `AppNavigation.kt` que contiene el grafo de navegación. Las acciones de navegación se exponen a las pantallas a través de callbacks (lambdas), como `onLoginSuccess`, para mantener los Composables desacoplados del `NavController` y facilitar las previsualizaciones y pruebas.

- **Persistencia de Datos con Room y patrón "Single Source of Truth" (SSOT)**:
    - **Rol**: Para habilitar la funcionalidad offline-first, se utiliza la librería Room como abstracción sobre una base de datos SQLite local.
    - **Decisión Arquitectónica**: Se adopta el patrón "Single Source of Truth". La capa de datos (`:data`) es la única que accede directamente a las fuentes de datos (API remota y base de datos Room). Los repositorios en esta capa son responsables de mantener la base de datos local sincronizada con el servidor. La capa de dominio y la capa de presentación **siempre** consumen datos desde la base de datos local (a través de `Flow`s de Room), asegurando que la UI sea reactiva, rápida y siempre funcional, incluso sin conexión.
    - **Implementación**:
        - Se definen `Entity`s en la capa de datos que reflejan la estructura del backend.
        - Se crean `DAO`s (Data Access Objects) para definir las operaciones de base de datos.
        - Se utiliza un `TypeConverter` para manejar tipos de datos no soportados nativamente por SQLite, como `LocalDateTime`.
        - Se provee la base de datos y los DAOs como singletons a través de un `DatabaseModule` de Hilt.

- **Estrategia de Pruebas para la Persistencia**:
    - **Decisión Arquitectónica**: Para garantizar la fiabilidad de la capa de persistencia, los DAOs se prueban con tests de instrumentación.
    - **Implementación**:
        - Se utiliza una base de datos Room **en memoria** (`Room.inMemoryDatabaseBuilder`) para cada prueba. Esto garantiza un entorno de prueba limpio, rápido y aislado, sin afectar el almacenamiento real del dispositivo.
        - Se utiliza `HiltAndroidTest` para crear un entorno de prueba que puede inyectar dependencias, como la base de datos en memoria, en las clases de prueba.
        - Se utilizan librerías como `kotlinx-coroutines-test` y `Google Truth` para escribir pruebas de coroutines claras, predecibles y con aserciones legibles.

---

## 6. Estructura de la Interfaz de Usuario (UI)

- **Principio**: Se favorece la creación de Composables pequeños, reutilizables y con un propósito único. Las pantallas complejas se construyen componiendo estas unidades más pequeñas.
- **Implementación**: En lugar de tener una única función Composable monolítica para toda una pantalla (ej. `HomeScreen`), la lógica se divide en sub-componentes más pequeños (ej. `UnidadSelectionStep`, `ActionSelectionStep`, `MovimientoFormStep`, `MovimientosPendientesTable`).
- **Ventajas**:
    - **Legibilidad y Mantenibilidad**: Es más fácil entender y modificar un Composable pequeño que uno grande y complejo.
    - **Reutilización**: Los Composables pequeños pueden ser reutilizados en diferentes partes de la aplicación.
    - **Testabilidad**: Es más sencillo escribir pruebas de UI para Composables pequeños y aislados.

## Comentarios de Arquitectura - Sesión del 16 de Noviembre de 2025

Esta sección detalla cómo los avances recientes en la gestión de Unidades Productivas (UPs) y la extensión del sistema de catálogos se integran y refuerzan los principios de arquitectura limpia del proyecto.

### 1. Refuerzo de la Separación de Responsabilidades

-   **Capa de Datos (`:data`)**:
    -   La creación de `UnidadProductivaApiService` y la refactorización de `AuthApiService` demuestran la especialización de los servicios de red por dominio de negocio, manteniendo la cohesión y el bajo acoplamiento.
    -   La extensión del sistema de catálogos con nuevas entidades Room y un `CatalogosDao` unificado centraliza la lógica de persistencia para datos de referencia, siguiendo el principio de "Single Source of Truth" (SSOT) para la UI.
    -   Los DTOs (`CreateUnidadProductivaRequest`, `UnidadProductivaDto`) y las funciones de mapeo (`toEntity()`, `toDomain()`) aseguran que la capa de datos sea la única que "conoce" la estructura de la API y la base de datos local, protegiendo las capas superiores de estos detalles de implementación.
-   **Capa de Dominio (`:domain`)**:
    -   La introducción de `CreateUnidadProductivaData` como modelo de dominio para la creación de UPs asegura que la lógica de negocio opere con objetos puros de Kotlin, independientes de cualquier detalle de UI o de infraestructura de datos.
    -   Los nuevos casos de uso (`CreateUnidadProductivaUseCase`, `SyncUnidadesProductivasUseCase`) encapsulan operaciones de negocio específicas, manteniendo la capa de dominio como el corazón de las reglas de la aplicación.
-   **Capa de Presentación (`:app`)**:
    -   La creación de la feature "Campos" (`CamposScreen`, `CamposViewModel`) y el rediseño de `CreateUnidadProductivaScreen` demuestran cómo la UI se construye sobre los casos de uso del dominio, sin interactuar directamente con repositorios o servicios de red.
    -   La lógica de navegación condicional en `MainViewModel` y `MainScreen` orquesta el flujo de usuario basándose en el estado del dominio (existencia de UPs), manteniendo la UI reactiva y desacoplada de la lógica de decisión.

### 2. Adherencia al Patrón MVVM y Flujo de Datos Unidireccional

-   Los `ViewModels` (`MainViewModel`, `CreateUnidadProductivaViewModel`) actúan como intermediarios entre la UI y los casos de uso del dominio, exponiendo el estado de la UI a través de `StateFlow`s y manejando los eventos del usuario.
-   La UI (`MainScreen`, `CreateUnidadProductivaScreen`) observa estos `StateFlow`s y reacciona a los cambios, manteniendo un flujo de datos unidireccional y predecible.

### 3. Estrategia Offline-First Reforzada

-   La extensión del sistema de catálogos con entidades Room y DAOs dedicados refuerza la capacidad offline-first, permitiendo que la aplicación funcione con datos de referencia incluso sin conexión.
-   La lógica de sincronización en `CatalogosRepositoryImpl` y `MainViewModel` asegura que los datos locales se mantengan actualizados con el backend cuando hay conexión, y que la UI siempre consuma de la base de datos local.

### 4. Modularidad y Escalabilidad

-   La creación de nuevas features (ej. "Campos") en paquetes separados dentro de `:app` mantiene la modularidad del código de presentación.
-   La extensión de los catálogos y la gestión de UPs se realizó añadiendo nuevos componentes a las capas existentes, demostrando la escalabilidad de la arquitectura para incorporar nuevas funcionalidades sin romper las existentes.

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
