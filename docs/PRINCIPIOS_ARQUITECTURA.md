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

