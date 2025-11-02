# Registro de Avances del Proyecto

Este archivo documenta los cambios y decisiones importantes tomadas durante el desarrollo.

---

### 01 de Noviembre de 2025

- **Hito**: Inicio del proyecto y definición de la arquitectura.
- **Detalles**:
    - Se estableció el rol de Gemini como mentor senior para el desarrollo en Android.
    - Se definió la **Arquitectura Limpia** como base para el proyecto, separada en tres capas: `Presentación (:app)`, `Dominio (:domain)` y `Datos (:data)`.
    - Se crearon los documentos `PRINCIPIOS_ARQUITECTURA.md` y `AVANCES.md` para guiar y registrar el desarrollo del proyecto.

- **Hito**: Refactorización inicial del módulo `:app` y verificación de compilación.
- **Detalles**:
    - Se movió `MainActivity.kt` al paquete `ui` dentro de `com.SIGICOM.sigi`.
    - Se actualizó la declaración del paquete en `MainActivity.kt` y la referencia en `AndroidManifest.xml`.
    - Se resolvió el problema de configuración de `JAVA_HOME` para la sesión actual.
    - El proyecto compiló exitosamente después de los cambios.

- **Hito**: Creación del módulo de Dominio (`:domain`).
- **Detalles**:
    - Se creó la estructura de carpetas y el archivo `build.gradle.kts` para el módulo `:domain` como una librería pura de Kotlin.
    - Se añadió el módulo a `settings.gradle.kts` y se configuró la dependencia desde el módulo `:app`.
    - Se depuró un error de compilación crítico (`Unresolved reference: material3`) causado por un error de tipeo en el `build.gradle.kts` del módulo `:app`.
    - El proyecto compiló exitosamente con la nueva estructura de dos módulos.

- **Hito**: Creación del módulo de Datos (`:data`) y finalización de la estructura arquitectónica.
- **Detalles**:
    - Se creó la estructura de carpetas y el archivo `build.gradle.kts` para el módulo `:data` como una librería de Android.
    - Se añadió el plugin `android-library` al catálogo de versiones y al `build.gradle.kts` raíz para evitar conflictos.
    - Se añadió el módulo a `settings.gradle.kts` y se configuró la dependencia desde `:data` hacia `:domain`.
    - Se depuró un conflicto de resolución de plugins de Gradle.
    - El proyecto compiló exitosamente con la arquitectura completa de tres módulos. La base del proyecto está finalizada.

- **Hito**: Implementación del Flujo de Autenticación de Usuario.
- **Detalles**:
    - Se implementó la funcionalidad completa para la autenticación de usuarios, siguiendo la arquitectura limpia establecida.
    - **Capa de Datos (`:data`)**:
        - Se añadieron las dependencias de Retrofit y Gson para la comunicación con la API REST.
        - Se crearon los DTOs (`LoginRequest`, `LoginResponse`, `ErrorResponse`) para modelar las respuestas de la API.
        - Se definió la interfaz `AuthApiService` con Retrofit para el endpoint de login.
        - Se configuró Hilt con un `NetworkModule` para proveer las instancias de Retrofit, OkHttpClient y Gson como singletons.
        - Se implementó `AuthRepositoryImpl`, que maneja la llamada a la API, mapea los resultados (éxito/error) a modelos de dominio y gestiona las excepciones de red.
        - Se creó un `RepositoryModule` de Hilt para vincular la interfaz `AuthRepository` con su implementación.
    - **Capa de Dominio (`:domain`)**:
        - Se definió la interfaz `AuthRepository` como el contrato para la obtención de datos de autenticación.
        - Se creó el modelo `AuthResult` (sealed class) para representar de forma robusta los posibles resultados del login (Success, InvalidCredentials, NetworkError, etc.).
        - Se implementó `LoginUseCase` para encapsular la lógica de negocio de la autenticación, dependiendo de la abstracción del repositorio.
        - Se añadió la dependencia `javax.inject` para el uso de la anotación `@Inject`.
    - **Capa de Presentación (`:app`)**:
        - Se creó `LoginViewModel` (anotado con `@HiltViewModel`) para orquestar la llamada al `LoginUseCase` y gestionar el estado de la UI.
        - Se definió la clase de estado `LoginState` para comunicar el estado (cargando, éxito, error) a la UI.
        - Se diseñó `LoginScreen` utilizando Jetpack Compose, que observa el estado del ViewModel y reacciona a los cambios.
        - Se añadió la dependencia `hilt-navigation-compose` para poder inyectar el ViewModel en el Composable con `hiltViewModel()`.
    - **Configuración y Depuración**:
        - Se configuró el `AndroidManifest.xml` para añadir el permiso de `INTERNET` y permitir el tráfico en texto plano (`usesCleartextTraffic`) para el desarrollo local.
        - Se resolvieron múltiples errores de configuración de Gradle relacionados con la compatibilidad de versiones de la JVM entre los módulos, estableciendo explícitamente la versión 11 en todos ellos.
        - Se diagnosticó un comportamiento inesperado en la respuesta de la API del backend (devolución de HTML en lugar de JSON para errores) mediante el uso de un `HttpLoggingInterceptor` en OkHttp.
- **Estado**: La funcionalidad de login está completa y probada. La app se comunica exitosamente con el backend, maneja respuestas de éxito y de error de credenciales.

---

### 01 de Noviembre de 2025 (Continuación)

- **Hito**: Depuración Avanzada de Flujo de Login y Refactorización de Red.
- **Detalles**:
    - Se continuó la depuración de un error que se manifestaba como `IOException` ("Error de red") al intentar un login con datos de formato inválido (email sin formato correcto).
    - **Diagnóstico 1 (Incorrecto)**: Se sospechó de la configuración del servidor (`--host`), firewall y `cleartextTraffic`, pero se descartaron ya que las peticiones con credenciales válidas e inválidas (401) sí funcionaban.
    - **Diagnóstico 2 (Revelador)**: Se añadió logging extensivo al `AuthRepositoryImpl` y se capturó el log de la excepción. La causa real era una `com.google.gson.stream.MalformedJsonException`, un subtipo de `IOException`.
    - **Causa Raíz 1**: Se descubrió que Retrofit intentaba parsear la respuesta de error (ej. 422) usando el modelo de éxito (`LoginResponse`), causando el fallo. La solución fue cambiar el tipo de retorno en `AuthApiService` a `Response<ResponseBody>` y parsear manualmente tanto las respuestas de éxito como las de error en el `AuthRepositoryImpl`.
    - **Causa Raíz 2 (El Problema Real)**: A pesar del arreglo anterior, la app recibía un código `200 OK` en lugar de `422`. El análisis final, confirmado por el usuario, fue que la petición desde Android no incluía la cabecera `Accept: application/json`, causando que el backend de Laravel devolviera una redirección HTML en lugar de un error JSON.
    - **Solución Definitiva**: Se añadió la anotación `@Headers("Accept: application/json")` a la llamada de login en `AuthApiService`. Esto resolvió el flujo de errores de forma definitiva.

- **Hito**: Rediseño de UI y Tema de la Aplicación.
- **Detalles**:
    - Se añadió el logo de la organización (`logoovinos.png`) a la pantalla de login, que está construida con Jetpack Compose.
    - Se cambió el esquema de colores de la aplicación para usar un tono bordó (`#8C2218`) como color primario.
    - **Diagnóstico de Tema de Compose**: Se detectó que los colores no se aplicaban debido a la funcionalidad de **Color Dinámico (Material You)** de Android 12+, que estaba activada por defecto en el tema de Compose (`Theme.kt`).
    - **Solución**: Se deshabilitó el color dinámico (`dynamicColor = false`) en `SINCTheme` para forzar el uso del esquema de colores personalizado de la marca.
    - Se limpió el código de diagnóstico que forzaba el color del botón, dejándolo dependiente del tema general.

- **Hito**: Implementación de Pantalla de Inicio (Splash Screen).
- **Detalles**:
    - Se implementó la pantalla de inicio utilizando la API moderna `SplashScreen` de Android.
    - Se añadió la dependencia `core-splashscreen`.
    - Se creó un tema `Theme.App.Starting` para el splash, configurando un fondo blanco y el logo de la app como ícono central.
    - Se diagnosticó y solucionó un problema de recorte del logo (causado por la máscara circular de la API) mediante el uso de un `inset` drawable (`splash_logo.xml`) que añade un margen interno para asegurar que el logo se muestre completo.
- **Estado**: La aplicación ahora tiene una identidad visual coherente, un flujo de login robusto y una pantalla de inicio profesional.

- **Hito**: Implementación de Navegación y Pantalla Principal (HomeScreen).
- **Detalles**:
    - Se configuró la navegación de la aplicación utilizando Jetpack Navigation para Compose.
    - Se creó un `AppNavigation` composable para gestionar las rutas de la aplicación, con `LoginScreen` como punto de partida.
    - Se implementó la redirección desde `LoginScreen` a una nueva `HomeScreen` tras una autenticación exitosa.
    - Se modificó `LoginViewModel` para emitir un evento de navegación de un solo uso (`SharedFlow`), desacoplando la lógica de negocio de la de navegación.
    - Se creó la `HomeScreen` y su `HomeViewModel` correspondiente para mostrar la lista de Unidades Productivas (UPs).
            - Se implementó la capa de datos y de dominio para obtener las UPs desde la API (`/api/movil/unidades-productivas`), incluyendo DTO, modelo, repositorio y caso de uso.
    - **Estado**: La navegación funciona correctamente. La `HomeScreen` intenta cargar los datos pero falla con un error 401 (Unauthorized) de la API, indicando que el token de autenticación no se está enviando en la petición. El siguiente paso es implementar el manejo del token.
    
    ---
    
    ### 02 de Noviembre de 2025
    
    - **Hito**: Implementación de la Arquitectura de Persistencia Local (Offline-First).
    - **Detalles**:
        - Se definió y documentó la estrategia de persistencia local en `docs/OFFLINE_ARCHITECTURE.md`, adoptando el patrón "Single Source of Truth" (SSOT) con la base de datos Room como fuente única de verdad para la UI.
        - Se analizaron las migraciones y modelos de la base de datos del backend (Laravel) para alinear la estructura de la base de datos local.
        - **Capa de Datos (`:data`)**:
            - Se añadieron las dependencias de Room al catálogo de versiones (`libs.versions.toml`) y al `build.gradle.kts` del módulo `:data`.
            - Se crearon las `Entity` de Room para todas las tablas necesarias, reflejando la estructura del backend:
                - `UnidadProductivaEntity`
                - `EspecieEntity`
                - `RazaEntity`
                - `CategoriaAnimalEntity`
                - `MotivoMovimientoEntity` (incluyendo el campo `tipo` para 'alta'/'baja')
                - `MovimientoPendienteEntity` (para almacenar movimientos a sincronizar)
            - Se implementó un `TypeConverter` para `LocalDateTime` para su correcto almacenamiento en la base de datos.
            - Se crearon las interfaces DAO (`@Dao`) para cada entidad, definiendo las operaciones de acceso a datos (CRUD).
            - Se definió la clase `SincMobileDatabase` que extiende `RoomDatabase`, uniendo todas las entidades, DAOs y el `TypeConverter`.
            - Se creó un `DatabaseModule` de Hilt para proveer las instancias singleton de la base de datos y los DAOs a toda la aplicación.
    
    - **Hito**: Implementación de Tests para la Capa de Persistencia.
    - **Detalles**:
        - Se decidió implementar tests unitarios para la capa de datos para garantizar su correcto funcionamiento antes de continuar con el desarrollo.
        - Se añadieron las dependencias de testing necesarias (`androidx.test.ext:junit`, `androidx.room:room-testing`, `kotlinx-coroutines-test`, `com.google.truth:truth`) al `build.gradle.kts` del módulo `:data`.
        - Se creó la clase de test `UnidadProductivaDaoTest.kt` en el directorio `androidTest` del módulo `:data`.
        - Se implementaron los siguientes casos de prueba para `UnidadProductivaDao`:
            - `insertAllAndGetAllUnidadesProductivas`: Verifica que la inserción de una lista de entidades y su posterior recuperación funcionan correctamente.
            - `clearAllUnidadesProductivas`: Verifica que la operación de borrado elimina todos los registros de la tabla.
            - `insertOnConflictReplacesExisting`: Verifica que la estrategia de conflicto `OnConflictStrategy.REPLACE` actualiza correctamente un registro existente si se inserta con la misma clave primaria.
    - **Estado**: La capa de persistencia local con Room está completamente definida y configurada. Se han escrito los tests unitarios para `UnidadProductivaDao` para validar su funcionalidad.

- **Hito**: Finalización de Tests para todos los DAOs de la Base de Datos Local.
- **Detalles**:
        - Se completó la implementación y verificación de los tests instrumentados para todos los DAOs definidos en `SincMobileDatabase`.
        - Se crearon y pasaron exitosamente los tests para:
            - `UnidadProductivaDao`
            - `EspecieDao`
            - `RazaDao`
            - `CategoriaAnimalDao`
            - `MotivoMovimientoDao`
            - `MovimientoPendienteDao`
        - Durante el proceso, se resolvieron varios problemas de configuración y sintaxis:
            - Se añadió la dependencia `coreLibraryDesugaring` y se limpiaron dependencias de test redundantes en `data/build.gradle.kts`.
            - Se añadió la dependencia `androidx-arch-core-testing` para `InstantTaskExecutorRule`.
            - Se corrigió el uso del `HiltTestRunner` eliminando la anotación `@RunWith(AndroidJUnit4::class)` de los tests.
            - Se adaptaron los métodos de test para usar `runTest` de `kotlinx-coroutines-test` para manejar correctamente las corrutinas y el tipo de retorno `void`.
            - Se corrigieron errores tipográficos en `TestDatabaseModule.kt`.
            - Se ajustaron los métodos de los DAOs (`MovimientoPendienteDao`) y las aserciones en los tests para manejar correctamente los IDs autogenerados y las comparaciones de objetos.
    - **Estado**: Todos los DAOs de la capa de persistencia local están cubiertos por tests instrumentados que verifican su correcto funcionamiento. El siguiente paso es continuar con la implementación de los tests para los DAOs restantes y luego integrar la capa de persistencia con los repositorios y casos de uso.

---

### 02 de Noviembre de 2025

- **Hito**: Corrección y Estabilización de `CatalogosRepositoryImplTest` para Sincronización de Catálogos.
- **Detalles**:
    - Se abordaron y resolvieron múltiples problemas que impedían la ejecución exitosa del test instrumentado `CatalogosRepositoryImplTest`, crucial para verificar la funcionalidad de sincronización de catálogos (especies, razas, categorías, motivos de movimiento) y el uso offline de la aplicación.
    - **Problema 1: Timeout en la Conexión al MockWebServer**:
        - **Causa**: Conflicto en la inyección de dependencias de Hilt, donde el test y el cliente Retrofit usaban instancias diferentes de `MockWebServer`, o el servidor no se iniciaba/configuraba correctamente para el cliente.
        - **Solución**: Se refactorizó el test para gestionar manualmente las dependencias de red (`MockWebServer`, `OkHttpClient`, `Retrofit`, `AuthApiService`) dentro del método `@Before`. Esto asegura que cada test tenga su propio entorno de red aislado y correctamente configurado, eliminando los conflictos de ciclo de vida con Hilt.
    - **Problema 2: `java.lang.IllegalArgumentException: start() already called`**:
        - **Causa**: Un intento previo de solucionar el timeout mediante la anotación `@Singleton` en el `MockWebServer` de Hilt provocó que la misma instancia del servidor se intentara iniciar varias veces en tests consecutivos.
        - **Solución**: La gestión manual de dependencias resolvió este problema al garantizar que una nueva instancia de `MockWebServer` se crea y se inicia para cada test, y se cierra correctamente en el `@After`.
    - **Problema 3: Lógica Incorrecta en `syncCatalogos_apiError_returnsFailure`**:
        - **Causa**: El test estaba diseñado para verificar el manejo de errores de la API, pero contenía una línea que lanzaba una excepción y fallaba el test si la operación resultaba en un fallo (el comportamiento esperado).
        - **Solución**: Se eliminó la línea `if (result.isFailure) { throw result.exceptionOrNull()!! }` de este test, permitiendo que las aserciones posteriores validaran correctamente el manejo del error sin provocar una falla prematura del test.
    - **Limpieza**: Se eliminaron las importaciones y proveedores de Hilt relacionados con las dependencias de red del `TestAppModule.kt` que ya no eran necesarios.
- **Estado**: El `CatalogosRepositoryImplTest` pasa exitosamente, confirmando la robustez de la lógica de sincronización de catálogos. La aplicación está lista para implementar la funcionalidad de sincronización en el sistema.
