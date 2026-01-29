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

02 de Noviembre de 2025
    
        
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
    
    
    
### 02 de Noviembre de 2025 (Continuación)
    
    
    
- **Hito**: Refactorización y Testeo del Repositorio de Unidades Productivas.
    
- **Detalles**:
    
    - Se refactorizó `UnidadProductivaRepositoryImpl` para implementar la estrategia offline-first. El método `getUnidadesProductivas` ahora lee desde la base de datos local (`UnidadProductivaDao`) y devuelve un `Flow`. Se añadió un nuevo método `syncUnidadesProductivas` que obtiene los datos de la API, los guarda en la base de datos local (limpiando los datos antiguos primero) y devuelve un `Result<Unit>`.
    
    - Se actualizó la interfaz `UnidadProductivaRepository` en la capa de dominio para reflejar estos cambios.
    
    - Se modificó `UnidadProductivaEntity` para permitir campos nulos y se ajustó el mapeo de DTO a entidad para manejar la conversión de tipos y los campos adicionales.
    
    - Se añadió el método transaccional `clearAndInsert` a `UnidadProductivaDao` para asegurar la atomicidad de la operación de sincronización.
    
    - Se actualizó `GetUnidadesProductivasUseCase` para devolver un `Flow` y se corrigieron las importaciones.
    
    - Se modificó `HomeViewModel` para recolectar correctamente el `Flow` de unidades productivas del caso de uso.
    
    - Se creó `UnidadProductivaRepositoryImplTest.kt` en el directorio `androidTest` del módulo `:data`. Este test instrumentado verifica:
    
        - La sincronización exitosa de unidades productivas desde la API a la base de datos local.
    
        - El manejo adecuado de errores de la API durante la sincronización.
    
        - El comportamiento correcto cuando no hay un token de autenticación disponible.
    
    - Todos los tests se ejecutaron exitosamente.
    
- **Estado**: La funcionalidad de sincronización de unidades productivas está implementada y verificada con tests, siguiendo la arquitectura offline-first.


### 11 de Noviembre de 2025

- **Hito**: Refactorización y Mejora de la Pantalla de Registro de Movimientos (HomeScreen).
- **Detalles**:
    - Se refactorizó `HomeScreen` y `HomeViewModel` para seguir un flujo de trabajo de varios pasos, mejorando la guía al usuario: 1. Selección de Unidad Productiva. 2. Selección de tipo de movimiento (Alta/Baja). 3. Relleno del formulario.
    - Se reemplazó la lista de tarjetas de movimientos pendientes por una tabla que agrupa los movimientos por especie, categoría y motivo, sumando las cantidades. Esto proporciona una vista más clara y consolidada de los datos locales.
    - Se añadió la funcionalidad para eliminar grupos de movimientos pendientes directamente desde la tabla.
    - Se mejoró la experiencia de usuario (UX) añadiendo animaciones sutiles:
        - Una pantalla de esqueleto (skeleton screen) se muestra brevemente al cargar el formulario de movimiento para suavizar la transición.
        - Al guardar un movimiento, se muestra un mensaje de éxito y se introduce un pequeño retraso antes de limpiar el formulario, dando tiempo al usuario para confirmar la acción.
- **Estado**: La pantalla de registro de movimientos es ahora más intuitiva, funcional y agradable de usar. El código se ha modularizado en Composables más pequeños y manejables, mejorando la mantenibilidad.

- **Hito**: Rediseño de la Barra Superior (Top Bar).
- **Detalles**:
    - Se modificó el `TopBar` Composable para incluir tres elementos:
        - **Izquierda**: Icono de menú para abrir el sidebar (existente).
        - **Centro**: Se reemplazó el título de texto por el logo de "Ovinos" (`logoovinos.png`).
        - **Derecha**: Se añadió un nuevo icono de "Configuración".
    - Se actualizó `MainScreen.kt` para pasar una lambda `onConfigurationIconClick` al `TopBar`.

- **Hito**: Rediseño del Menú Lateral (Sidebar).
- **Detalles**:
    - Se refactorizó completamente el `Sidebar` para emular el estilo del sidebar de la aplicación web, utilizando el código de ejemplo proporcionado como guía.
    - Se crearon nuevos Composables (`SidebarSection`, `SidebarItem`) en un archivo `SidebarComponents.kt` para construir el nuevo diseño.
    - Se replicó la estructura del sidebar del productor, incluyendo las secciones "Principal", "Gestión Productiva" y "Análisis y Datos", con sus respectivos ítems.
    - Los nuevos ítems del sidebar se dejaron inactivos (`onClick = { /* TODO */ }`) para su futura implementación.

- **Hito**: Creación de la Pantalla de Configuraciones.
- **Detalles**:
    - Se creó una nueva pantalla `SettingsScreen` para gestionar las configuraciones de la aplicación.
    - Se añadió una nueva ruta `SETTINGS` en `AppNavigation.kt` y se configuró la navegación desde el icono de configuración en el `TopBar`.
    - Se maquetó la `SettingsScreen` siguiendo un estilo similar al de Jetstream, utilizando `Card`s para agrupar las opciones.
    - Se añadieron las opciones "Perfil", "Correo" y "Teléfono" en la sección "Cuenta", y "Cerrar Sesión" en la sección "Sesión".
    - Se personalizó el `TopAppBar` de la `SettingsScreen` para incluir un título centrado ("Configuración") y una flecha de retroceso a la izquierda.
    - Se añadió un `HorizontalDivider` para separar visualmente el `TopAppBar` del contenido de la pantalla.

- **Hito**: Limpieza de la Pantalla de Inicio (Dashboard).
- **Detalles**:
    - Se eliminó el botón "Ir a Cuaderno de Campo" de la `DashboardScreen` para limpiar la interfaz principal.
    - Se eliminó el parámetro `onNavigateToMovimiento` de la `DashboardScreen` y de su llamada en `MainScreen.kt`, ya que no era necesario.

- **Estado**: La interfaz de usuario principal ha sido rediseñada para ser más limpia y funcional, con una nueva pantalla de configuraciones y un sidebar mejorado. El siguiente paso es implementar la lógica para las nuevas opciones.

- **Hito**: Corrección de Bugs y Creación de Componentes de UI.
- **Detalles**:
    - Se realizó una sesión intensiva de corrección de errores y desarrollo de componentes reutilizables para establecer una base sólida para la aplicación.

- **Hito**: Corrección de Bugs Críticos de UI y Navegación.
- **Detalles**:
    - **Campo de Contraseña en Login**: Se corrigió el campo de contraseña para que el texto esté oculto por defecto y se añadió un ícono para alternar la visibilidad.
    - **Funcionalidad de Logout**: Se implementó el flujo completo de cierre de sesión en `SettingsScreen`, respetando la arquitectura limpia (Repositorio, Caso de Uso, ViewModel).
    - **Parpadeo del Sidebar**: Se eliminó un parpadeo visual del menú lateral al inicio de la app. Se refactorizó la lógica de arranque, eliminando el `SplashScreen` composable y decidiendo la ruta inicial (`Login` o `Home`) de forma síncrona en `MainActivity`.
    - **Error de Pantalla en Blanco**: Se solucionó un bug que causaba una pantalla en blanco después de una secuencia específica de navegación. Se refactorizó `MainScreen` para eliminar un `NavHost` anidado y usar una gestión de estado más simple, estabilizando la composición de la pantalla.

- **Hito**: Creación de Librería de Componentes de UI Básicos.
- **Detalles**:
    - Se creó una serie de componentes de UI genéricos y reutilizables en la carpeta `app/src/main/java/com/sinc/mobile/app/ui/components/`.
    - **`GlobalBanner`**: Un banner de notificaciones que aparece en la parte superior para mostrar mensajes de éxito o error, con animaciones de entrada y salida suaves.
    - **`ConfirmationDialog`**: Un diálogo de alerta estándar para confirmar acciones del usuario (ej. "¿Estás seguro?").
    - **`LoadingOverlay`**: Una capa de carga modal de pantalla completa para bloquear la UI durante operaciones asíncronas importantes.
    - **`EmptyState`**: Un componente para mostrar cuando una lista o pantalla no tiene contenido, mejorando la experiencia de usuario sobre una pantalla vacía.
    - **`ValidatedTextField`**: Un campo de texto que incluye lógica para mostrar un mensaje de error de validación de forma estandarizada.

- **Hito**: Implementación de Barra de Navegación Inferior.
- **Detalles**:
    - Se añadió una `NavigationBar` de Material 3 en la parte inferior de `MainScreen`.
    - Se extrajo la lógica a un componente reutilizable `BottomNavBar`.
    - La barra contiene accesos directos de solo-ícono a "Inicio", "Cuaderno" y "Notificaciones".
    - Se aseguró que la barra respete los `insets` del sistema para no ser ocultada por la barra de navegación de Android.

- **Estado General**:
    - La base de la UI de la aplicación es ahora mucho más robusta y escalable.
    - Se cuenta con una pequeña librería de componentes reutilizables que agilizará el desarrollo de futuras pantallas.
    - La navegación y la gestión de estado de las pantallas principales han sido estabilizadas.

- **Hito**: Refactorización Arquitectónica de la Pantalla "Cuaderno de Campo".
- **Detalles**:
    - Se inició una refactorización completa de la vista `MovimientoScreen.kt` para mejorar su mantenibilidad y estructura.
    - Se extrajeron todos los Composables de UI (`UnidadSelectionStep`, `ActionSelectionStep`, `MovimientoForm`, `MovimientosPendientesTable`, etc.) a archivos individuales dentro de un nuevo paquete `.../movimiento/components`.
    - El archivo `MovimientoScreen.kt` fue limpiado, conteniendo ahora solo la lógica de orquestación de los componentes extraídos.

- **Hito**: Refactorización Profunda del `MovimientoViewModel`.
- **Detalles**:
    - Se identificó que el `MovimientoViewModel` tenía una alta complejidad y múltiples responsabilidades.
    - **Primera Fase**: Se extrajo toda la lógica y el estado relacionados con el formulario de registro a una nueva clase `MovimientoFormManager`. El ViewModel ahora delega la gestión del formulario a esta clase.
    - **Segunda Fase**: Se extrajo la lógica de carga, agrupación y sincronización de movimientos pendientes a una nueva clase `MovimientoSyncManager`.
    - **Resultado**: El `MovimientoViewModel` quedó significativamente más limpio, actuando como un orquestador de alto nivel para los `Manager` especializados, mejorando la separación de responsabilidades.

- **Hito**: Creación y Pulido de Maqueta de Interfaz de Usuario (UI).
- **Detalles**:
    - Se creó una nueva pantalla de maqueta (`CuadernoDeCampoMaquetaScreen.kt`) para visualizar un rediseño moderno de la interfaz, basado en un ejemplo de React/Tailwind proporcionado.
    - Se aplicó una nueva paleta de colores (verde para altas, rojo para bajas), un layout basado en tarjetas con bordes y esquinas redondeadas, y una mejor jerarquía visual.
    - Se pulió la maqueta en base a feedback, ajustando la alineación y tamaño de los botones de acción, mejorando el selector de campo con un icono `Place` y un título, y optimizando el comportamiento del menú desplegable.

- **Hito**: Implementación del Nuevo Diseño en la Vista Funcional.
- **Detalles**:
    - Se aplicó el nuevo diseño de la maqueta a la pantalla funcional `MovimientoScreen.kt` y a sus componentes reales.
    - Se reemplazó el `BottomSheetScaffold` por un layout de `LazyColumn`, integrando la lista de movimientos pendientes directamente en el cuerpo principal de la pantalla.
    - Se actualizaron todos los componentes (`UnidadSelectionStep`, `ActionSelectionStep`, `MovimientoForm`, `MovimientoItemCard`) para usar los nuevos estilos, colores e iconos.

- **Hito**: Corrección de Bugs y Mejoras de Usabilidad.
- **Detalles**:
    - Se solucionaron múltiples errores de compilación que surgieron durante la refactorización y el rediseño, principalmente por importaciones faltantes (`BorderStroke`, `RoundedCornerShape`, `EmptyState`, etc.).
    - Se corrigió un bug crítico donde el botón 'X' para cerrar el formulario de alta/baja no funcionaba. Se implementó una función `dismissForm()` en el ViewModel para manejar correctamente el cierre.
    - Se ajustó el ancho del componente `FormDropdown` para que ocupe todo el espacio horizontal, solucionando problemas de alineación en el formulario.
    - Se compactó el layout de los botones de acción y del formulario para mejorar la visibilidad sin necesidad de hacer scroll.
    - Se implementó el `EmptyState` para la lista de movimientos pendientes, mostrando un mensaje claro cuando no hay datos, en lugar de ocultar la sección.

- **Hito**: Limpieza Final y Mejoras en la Pantalla de Inicio.
- **Detalles**:
    - Se eliminó el botón de acceso a la maqueta de la pantalla de inicio (`DashboardScreen`).
    - Se mejoró la presentación visual de `DashboardScreen`, reemplazando el texto simple por una `Card` de bienvenida con un icono y una breve descripción, utilizando la nueva paleta de colores.
    - Se eliminó la ruta de navegación a la maqueta del grafo de navegación (`AppNavigation.kt`) para mantener el código de producción limpio.

- **Estado General al Final de la Sesión**:
    - La funcionalidad del "Cuaderno de Campo" es ahora más robusta arquitectónicamente y presenta una interfaz de usuario moderna y pulida.
    - El proyecto se encuentra en un estado estable y compilable.

---

### 12 de Noviembre de 2025

-   **Hito**: Corrección de Colores y Temas.
  -   **Detalles**: Se deshabilitó el color dinámico (Material You) y se forzó el tema claro en `Theme.kt`. Se aplicó la paleta de colores "Maqueta" al `lightColorScheme` para una apariencia más moderna y consistente. Se corrigieron los tintes rojizos en componentes como `BottomNavBar`, `Sidebar`, `SettingsScreen` (`TopAppBar` y `SettingsCard`) forzando colores de superficie neutros, lo que resolvió el problema de la herencia de colores antiguos. Se ajustó el color del texto de los ítems seleccionados en el `Sidebar` para que permaneciera oscuro, mejorando la legibilidad.

-   **Hito**: Implementación de Cambio de Contraseña (Usuario Autenticado).
  -   **Detalles**: Se implementó el flujo completo de cambio de contraseña siguiendo la arquitectura limpia. Esto incluyó la adición del endpoint a `AuthApiService`, la creación de DTOs y modelos de dominio (`ChangePasswordRequest`, `ChangePasswordData`), la implementación del repositorio (`AuthRepositoryImpl`) y un caso de uso (`ChangePasswordUseCase`) con validaciones. Se creó `ChangePasswordViewModel` y `ChangePasswordScreen` (anteriormente `ProfileScreen`) para la interfaz de usuario.

-   **Hito**: Implementación de Recuperación de Contraseña (Flujo de Token).
  -   **Detalles**: Se implementó el flujo de dos pasos para recuperar la contraseña (solicitar código y restablecer con código). Se añadieron los endpoints a `AuthApiService`, se crearon DTOs y modelos de dominio (`RequestPasswordResetRequest`, `ResetPasswordWithCodeRequest`, `RequestPasswordResetData`, `ResetPasswordWithCodeData`), se implementó el repositorio y dos casos de uso (`RequestPasswordResetUseCase`, `ResetPasswordWithCodeUseCase`) con validaciones. Se creó `ForgotPasswordViewModel` y `ForgotPasswordScreen`.

-   **Hito**: Mejoras de UX en Flujos de Contraseña.
  -   **Detalles**: Se unificó la experiencia de usuario post-cambio/restablecimiento de contraseña mediante un `InfoDialog` genérico. Este diálogo informa al usuario del éxito y lo redirige al login tras su aceptación, reemplazando los `Snackbar` y la redirección inmediata. Se añadió un overlay de carga con mensajes descriptivos (`Enviando código...`, `Restableciendo...`) en `ForgotPasswordScreen` para proporcionar feedback visual durante las operaciones asíncronas.

-   **Hito**: Refactorización y Limpieza de Navegación y UI.
  -   **Detalles**: Se renombró la opción "Perfil" a "Contraseña" en `SettingsScreen` con un icono de candado (`Icons.Outlined.Lock`) para mayor claridad. Se renombraron los archivos `ProfileScreen.kt` y `ProfileViewModel.kt` a `ChangePasswordScreen.kt` y `ChangePasswordViewModel.kt` respectivamente, y se actualizaron todas las referencias en `AppNavigation.kt` y `SettingsScreen.kt`. Se añadió texto descriptivo a `ChangePasswordScreen` para guiar al usuario sobre los requisitos de la contraseña.

-   **Hito**: Corrección de Errores y Advertencias.
  -   **Detalles**: Se corrigió un error de compilación en `RequestPasswordResetUseCase` eliminando una dependencia de Android (`android.util.Patterns`) en la capa de dominio, reemplazándola por una expresión regular de Kotlin. Se corrigieron errores de sintaxis en `SettingsScreen.kt` causados por una operación de reemplazo incompleta. Se solucionó una advertencia de deprecación para el icono `ExitToApp` en `SettingsScreen.kt` utilizando su versión `AutoMirrored`. Se eliminaron los archivos y la carpeta obsoletos de la antigua funcionalidad de perfil (`ProfileScreen.kt`, `ProfileViewModel.kt` y el directorio `features/profile`).

-   **Hito**: Configuración de Entorno de Desarrollo.
  -   **Detalles**: Se cambió la URL base de la API en `NetworkModule.kt` a `http://10.0.2.2:8000/` para facilitar las pruebas locales con el servidor Laravel en un emulador de Android.

---
### 16 de Noviembre de 2025

-   **Hito**: Implementación de la Gestión de Unidades Productivas (UPs) y Extensión del Sistema de Catálogos.
-   **Detalles**:
  -   **Capa de Datos (`:data`)**:
    -   Creación de `UnidadProductivaApiService.kt` para endpoints de UPs.
    -   Refactorización de `AuthApiService.kt` (eliminación de `getUnidadesProductivas`).
    -   Actualización de `NetworkModule.kt` para proveer `UnidadProductivaApiService`.
    -   Actualización de `UnidadProductivaDto.kt` y creación de `CreateUnidadProductivaRequest.kt`.
    -   Actualización de `UnidadProductivaRepositoryImpl.kt` (inyección, nuevos métodos, mapeos).
    -   Creación de nuevas entidades de Room (`MunicipioEntity`, `CondicionTenenciaEntity`, `FuenteAguaEntity`, `TipoSueloEntity`, `TipoPastoEntity`).
    -   Creación de `CatalogosDao.kt` unificado.
    -   Actualización de `SincMobileDatabase.kt` y `DatabaseModule.kt` para usar el nuevo `CatalogosDao` y las nuevas entidades.
    -   Actualización de `CatalogosRepositoryImpl.kt` para manejar los nuevos catálogos.
  -   **Capa de Dominio (`:domain`)**:
    -   Actualización de `UnidadProductiva.kt` y creación de `CreateUnidadProductivaData.kt`.
    -   Actualización de `UnidadProductivaRepository.kt` (interfaz).
    -   Creación de `CreateUnidadProductivaUseCase.kt` y `SyncUnidadesProductivasUseCase.kt`.
    -   Extensión del modelo `Catalogos.kt` con los nuevos tipos de catálogos.
  -   **Capa de Presentación (`:app`)**:
    -   Creación de la feature "Campos" (`CamposScreen.kt`, `CamposViewModel.kt`).
    -   Integración de "Campos" en la navegación (`AppNavigation.kt`, `MainScreen.kt`, `Sidebar.kt`).
    -   Creación de `CreateUnidadProductivaScreen.kt` y `CreateUnidadProductivaViewModel.kt`.
    -   Implementación de la lógica de navegación condicional para usuarios sin UPs.
    -   Rediseño inicial del formulario de creación de UP con secciones y dropdowns.
-   **Corrección de Bugs**:
  -   Error 404 en `getUnidadesProductivas` (endpoints en `UnidadProductivaApiService.kt`).
  -   `NullPointerException` en `syncCatalogos` (listas nulables en `CatalogosDto.kt` y manejo en `CatalogosRepositoryImpl.kt`).
  -   Errores de compilación de Room/Kapt (nombres de tablas en `CatalogosDao.kt`).
  -   Error de `combine` con más de 5 `Flow`s en `CatalogosRepositoryImpl.kt`.
  -   Bug de navegación "sin retorno" y navegación automática al formulario de creación de UP (`MainScreen.kt`, `MainViewModel.kt`).
-   **Estado**: La infraestructura para la gestión de UPs y catálogos está implementada y el formulario de creación de UP ha sido rediseñado.

### 27 noviembre 2025

## Maqueta del formulario de creación de Unidad Productiva

-   Se completó la maqueta visual del formulario multi-paso para la creación de una Unidad Productiva.
-   Se ajustaron los textos de los títulos y subtítulos de cada paso para ser más claros y naturales.
    -   Paso 1: "Seleccionar ubicación" / "Buscando en el mapa"
    -   Paso 2: "Ubicación guardada" / "Completando datos básicos"
    -   Paso 3: "Datos básicos guardados" / "Último paso, seleccione una opción"
-   Se cambió el color del botón "Siguiente"/"Finalizar" a verde (`#326B41`).
-   Se añadió un texto explicativo ("¿Cuál es tu relación con este campo?") antes de las opciones de condición de tenencia en el último paso.

## Preparación para el desarrollo de la lógica

-   **Limpieza del formulario anterior:** Se eliminó el contenido original de `CreateUnidadProductivaScreen.kt` y `CreateUnidadProductivaViewModel.kt`, y se borraron los componentes asociados, dejando una base limpia para la nueva implementación.
-   **Refactorización de la capa de datos (DAOs de Catálogos):**
    -   Se refactorizó la implementación para usar DAOs individuales para cada tipo de catálogo (Especies, Razas, Categorías, MotivosMovimiento, Municipios, CondicionesTenencia, FuentesAgua, TiposSuelo, TiposPasto), siguiendo el Principio de Responsabilidad Única.
    -   Se actualizó `SincMobileDatabase` y `DatabaseModule` para reflejar este cambio.
    -   Se actualizó `CatalogosRepositoryImpl` para inyectar y usar los nuevos DAOs individuales.
-   **Ampliación de tests unitarios para Catálogos:**
    -   Se ampliaron los tests en `CatalogosRepositoryImplTest.kt` para cubrir los 9 catálogos en escenarios de éxito y error, asegurando la correcta inserción y manejo de fallos.
-   **Ampliación de tests unitarios para Creación de Unidad Productiva:**
    -   Se añadieron tests completos en `UnidadProductivaRepositoryImplTest.kt` para la función `createUnidadProductiva`, cubriendo casos de éxito, errores de API, errores de validación y ausencia de token de autenticación.
-   **Integración de la maqueta en el formulario real:**
    -   Se copió la lógica de la UI y el `ViewModel` de la maqueta al `feature` de `createunidadproductiva`.
    -   Se refactorizaron los componentes de la UI en archivos separados para una mejor modularidad.
    -   Se eliminó el acceso a la maqueta desde la navegación principal de la aplicación.
-   **Resolución de errores de compilación:** Se solucionaron varios errores de Kapt y de compilación que surgieron durante el proceso, incluyendo problemas de importación, formato de archivo y parámetros de funciones.

## Funcionalidad Principal: Creación de Unidad Productiva - Paso 1 (Ubicación)

Se ha implementado la primera fase del formulario de creación de unidades productivas, centrada en la selección de la ubicación del campo.

- **Integración de Mapa:** Se añadió un mapa interactivo utilizando la librería `osmdroid`.
- **Selección de Ubicación:** Se implementaron dos flujos para definir la ubicación:
    1.  **"Usar mi ubicación actual":**
        -   Solicita permisos de ubicación de forma segura, mostrando un panel inferior (`ModalBottomSheet`) con una explicación antes de la petición nativa de Android.
        -   Obtiene las coordenadas GPS del dispositivo.
        -   Anima el mapa desde una vista general de Misiones hasta la ubicación del usuario.
    2.  **"Buscar en el mapa":**
        -   Permite al usuario mover el mapa y seleccionar una ubicación manualmente.

## Refinamientos de UI/UX (Múltiples Iteraciones)

Se realizaron numerosos ajustes para mejorar la experiencia de usuario en la pantalla del mapa:

- **Estilo de Componentes:** Se corrigió el estilo del `ModalBottomSheet` de permisos para que tuviera un fondo blanco y una atenuación de fondo (`scrim`) adecuada.
- **Animación y Bugs del Mapa:**
    -   Se solucionó un bug crítico donde el mapa quedaba en blanco después de una animación de zoom, forzando una actualización con `mapView.invalidate()`.
    -   Se estableció una vista inicial del mapa centrada en la provincia de Misiones, utilizando coordenadas y un nivel de zoom definidos por el usuario para una mejor contextualización.
- **Feedback de Carga:**
    -   Se implementó un indicador de carga con una duración mínima de 2 segundos para mejorar la percepción del usuario mientras se obtiene la ubicación.
    -   Se rediseñó el indicador de carga, pasando de una superposición semitransparente a una tarjeta (`Card`) blanca, pequeña y centrada con un spinner de color bordó (color primario), resultando en una interfaz más limpia.
- **Panel de Confirmación:** Se rediseñó la parte inferior de la pantalla del mapa, reemplazando un botón flotante por un panel fijo con fondo blanco que incluye:
    -   Un título ("Ubicación").
    -   Instrucciones claras con texto en negrita para guiar al usuario.
    -   El botón de confirmación ("Guardar Ubicación").
- **Rendimiento del Mapa:** Se descartó el uso de una capa de imágenes satelitales (`ESRI_WORLD_IMAGERY`) debido a un bajo rendimiento en el emulador, optando por la capa estándar `MAPNIK` que garantiza una mayor fluidez.

## Conectividad y Pruebas

- **Pruebas en Dispositivo Físico:** Para facilitar las pruebas en un teléfono real, se cambió temporalmente la URL base de la API al dominio de producción (`sicsurmisiones.online`).
- **Reversión a Local:** Una vez finalizadas las pruebas, la URL base se revirtió a la dirección de desarrollo local (`http://10.0.2.2:8000/`).

## Corrección de Errores

- **Superposición de UI:** Se corrigió un error visual donde los botones de navegación del formulario ("Anterior" y "Siguiente") se superponían con la barra de navegación del sistema Android. Se solucionó aplicando `navigationBarsPadding`.
- **Errores de Compilación:** Se resolvieron múltiples errores de compilación causados por errores de sintaxis durante las modificaciones del código.

---

## Avances de la Sesión Actual (28 de Noviembre de 2025)

### Refinamiento de Transiciones y UI/UX

*   **Animación de la barra de progreso:** Se ajustó la duración de la animación a 500ms en `ProgressBar.kt` para una transición más suave.
*   **Transición al paso 2:** Se añadió un retraso de 500ms en `CreateUnidadProductivaViewModel.kt` después de seleccionar la ubicación en el mapa para una transición más fluida al siguiente paso.

### Implementación de Búsqueda de Municipios (Intento Limpio)

*   **Modelos de Datos:** Se verificó que `Municipio.kt` (en `:domain`) y `MunicipioEntity.kt` (en `:data`) estuvieran actualizados con `id`, `centroide`, `poligono`, `latitud`, `longitud` y `geojson_boundary`.
*   **DTO:** Se verificó que `CatalogosDto.kt` (en `:data`) estuviera actualizado con los campos `latitud`, `longitud` y `geojsonBoundary` en `MunicipioDto`.
*   **Repositorio:** Se verificó que `CatalogosRepositoryImpl.kt` (en `:data`) estuviera actualizado para manejar la inyección de `Gson` y la conversión de `geojson_boundary` a `DomainGeoPoint`s.
*   **ViewModel:** Se actualizó `CreateUnidadProductivaViewModel.kt` para inyectar `CatalogosRepository`, cargar municipios, usar `DomainGeoPoint` y manejar `onMunicipioSelected`.
*   **UI (`Step1Ubicacion.kt`):**
    *   Se corrigió la conversión del polígono para `OsmdroidMapView` para que se dibuje correctamente.
    *   Se reestructuró el `MapDialog` para usar un `BottomSheetScaffold` con un `SearchableSheetContent` expandible/colapsable para la búsqueda de municipios.
    *   El `bottomBar` ahora se adapta al `MapMode` (ubicación actual o búsqueda en el mapa), mostrando el buscador solo cuando se selecciona "Buscar en el mapa".
    *   El botón "Guardar Ubicación" se movió para estar siempre visible en el mapa, fuera del `BottomSheet`.

### Corrección de Errores de Compilación durante la Implementación

*   **`Overload resolution ambiguity` en `Step1Ubicacion.kt`:** Resuelto eliminando la función `ActionButton` duplicada.
*   **`Unresolved reference 'outlinedTextFieldColors'` en `Step1Ubicacion.kt`:** Resuelto usando `OutlinedTextFieldDefaults.colors`.
*   **`Unresolved reference 'LazyColumn'` y `items` en `Step1Ubicacion.kt`:** Resuelto añadiendo las importaciones necesarias.
*   **`[Hilt] @HiltViewModel is only supported on types that subclass androidx.lifecycle.ViewModel.`:** Resuelto mediante limpieza y reconstrucción del proyecto.
*   **Errores de parámetros en `CreateUnidadProductivaScreen.kt`:** Resueltos al sincronizar los parámetros pasados a `Step1Ubicacion` con la firma actualizada.

# Avances de la Sesión - 04 de Diciembre de 2025

## Implementación de Solicitud de RNSPA

- **Hito**: Implementación de la funcionalidad de "No conozco mi RNSPA" en el formulario de creación de Unidad Productiva.
- **Detalles**:
    - Se añadió un botón "No conozco mi [Identificador]" debajo del campo RNSPA en `Step2FormularioBasico.kt`.
    - Al hacer clic en este botón, se abre un modal (`RnspaRequestModal`) para que el productor pueda solicitar su número de identificador.
    - **Capa de Datos (`:data`)**:
        - Se crearon los DTOs `CreateTicketRequest.kt` y `CreateTicketResponse.kt`.
        - Se definió la interfaz `TicketApiService.kt` para la API de solicitudes.
        - Se actualizó `NetworkModule.kt` para proveer `TicketApiService`.
        - Se implementó `TicketRepositoryImpl.kt`, que se encarga de realizar la llamada a la API y mapear la respuesta.
        - Se actualizó `RepositoryModule.kt` para vincular `TicketRepository` con su implementación.
    - **Capa de Dominio (`:domain`)**:
        - Se definió la interfaz `TicketRepository.kt`.
        - Se creó el modelo de datos `CreateTicketData.kt`.
        - Se creó una clase de error genérica `GenericError.kt` para el manejo de errores.
        - Se implementó `SubmitTicketUseCase.kt` para encapsular la lógica de negocio de la solicitud.
        - Se realizó una corrección general en todas las capas para utilizar la clase sellada `Result<T, E: Error>` del dominio en lugar de la clase `Resource` inicial.
    - **Capa de Presentación (`:app`)**:
        - Se actualizó `CreateUnidadProductivaState` en `CreateUnidadProductivaViewModel.kt` con las variables de estado necesarias para controlar el modal y sus campos (municipio, paraje, dirección, información adicional).
        - Se implementaron los métodos en `CreateUnidadProductivaViewModel.kt` para gestionar el estado del modal y construir el mensaje concatenado antes de enviar la solicitud.
        - Se modificó `Step2FormularioBasico.kt` para incluir el parámetro `onIdentifierHelpClick` y conectarlo al ViewModel.
        - Se creó el composable `RnspaRequestModal.kt`, que proporciona la interfaz de usuario para el formulario de solicitud, incluyendo campos de texto para municipio, paraje, dirección e información adicional.
        - Se integró `RnspaRequestModal` en `CreateUnidadProductivaScreen.kt`.

## Corrección y Depuración

- **Hito**: Ajuste de URL de API y depuración de errores de conexión y servidor.
- **Detalles**:
    - Se cambió la `BASE_URL` en `NetworkModule.kt` de `http://10.0.2.2:8000/` (desarrollo local) a `https://sicsurmisiones.online/` (producción) para la conexión con el backend real.
    - Se añadió logging (`Log.d`, `Log.e`) en `TicketRepositoryImpl.kt` para facilitar la depuración de problemas de conexión y respuestas del servidor.
    - Se diagnosticó un error 500 del servidor, lo que llevó a la confirmación de la necesidad de depuración en el backend.

## Ajustes de UI en Modal de Solicitud

- **Hito**: Mejoras visuales en `RnspaRequestModal`.
- **Detalles**:
    - Se configuró el `AlertDialog` dentro de `RnspaRequestModal` para usar `containerColor = MaterialTheme.colorScheme.surface`, forzando un fondo blanco y eliminando un posible tinte rojizo.
    - Se actualizaron las etiquetas de los campos "Paraje" e "Información adicional" para incluir hints como "(Opcional)" y "(Recomendado)" respectivamente, mejorando la usabilidad.

# Avances de la Sesión Actual - 06 de Diciembre de 2025

Esta sesión se centró en la implementación y corrección de la lógica del formulario de creación de Unidades Productivas (UP), especialmente en los pasos 1, 2 y 3, así como en la refactorización del manejo de resultados a nivel de toda la aplicación.

## 1. Implementación y Ajustes del Paso 2 (Datos Básicos)

### 1.1. Formato y Validación Dinámica de Identificadores (RNSPA)

*   **Problema Inicial**: El campo de identificador (ej. RNSPA) no formateaba la entrada del usuario automáticamente y no mostraba un ejemplo de formato. La validación se realizaba contra un regex que no se aplicaba al formato visual esperado.
*   **Solución**:
    *   Se añadió una propiedad `hint: String` a los modelos de datos `IdentifierConfig` (dominio, entidad y DTO) para permitir al backend proveer un ejemplo de formato para el placeholder.
    *   Se implementó una nueva clase `PatternVisualTransformation` genérica. Esta clase toma un patrón de formato (ej. `##.###.#.#####/##`) derivado dinámicamente de la `regex` del backend y aplica el formato en tiempo real mientras el usuario escribe.
    *   La función `getFormatInfoFromRegex` en `CreateUnidadProductivaViewModel` fue refactorizada para parsear correctamente la `regex` del backend (ej. `^\d{2}\.\d{3}\.\d\.\d{5}\/\d{2}$`) y convertirla en el patrón (`##.###.#.#####/##`) y la `maxLength` correspondientes. Esto incluye la corrección de errores en el parseo de `\d` y el manejo de caracteres de escape para separadores.
    *   La lógica de validación en `validateStep2()` del `ViewModel` se actualizó para verificar la longitud de los dígitos sin formato contra la `maxLength` dinámica, y `onIdentifierValueChange` filtra la entrada para aceptar solo dígitos.
    *   La UI en `Step2FormularioBasico.kt` se actualizó para usar el `PatternVisualTransformation` y mostrar el `hint` provisto por el backend.

### 1.2. Corrección del Endpoint de Envío de Solicitudes RNSPA

*   **Problema**: El endpoint configurado para la solicitud de RNSPA era incorrecto.
*   **Solución**: Se actualizó el endpoint en `TicketApiService.kt` de `/api/movil/solicitudes` a `/api/movil/tickets` según lo especificado.

## 2. Implementación de Selección Automática de Municipio (Paso 1)

### 2.1. Lógica de "Punto en Polígono"

*   **Problema Inicial**: La selección de una ubicación en el mapa no asignaba automáticamente el municipio al que pertenecían las coordenadas, lo que impedía el envío final del formulario al no tener `municipioId`.
*   **Solución**:
    *   Se implementó la clase `GeoUtils.kt` con la función `isPointInPolygon()` utilizando el algoritmo de Ray-Casting. Esta función verifica si un `DomainGeoPoint` (lat/long) se encuentra dentro de un polígono GeoJSON (`List<DomainGeoPoint>`).
    *   La función `findContainingMunicipio` en `CreateUnidadProductivaViewModel` utiliza `GeoUtils` para encontrar el `Municipio` correspondiente a unas coordenadas dadas, iterando sobre la lista de municipios con sus polígonos.

### 2.2. Flujo de Usuario y Manejo de Errores en el Mapa

*   **Problema**: La ubicación automática no animaba el mapa a la posición del usuario, y el mensaje de error "ubicación fuera de municipios válidos" aparecía prematuramente.
*   **Solución**:
    *   La función `fetchCurrentLocation()` en el `ViewModel` ahora solo obtiene las coordenadas GPS y anima el mapa a esa ubicación (`animateToLocation`), sin realizar validación del municipio en este punto.
    *   La validación del municipio (usando `findContainingMunicipio`) se centralizó en la función `onMapLocationSelected()`, que se activa únicamente al presionar el botón "Guardar Ubicación" en el mapa.
    *   Si al guardar una ubicación, esta no se encuentra dentro de ningún municipio válido, se establece un `mapErrorMessage` en el estado del `ViewModel`.
    *   La UI del `MapDialog` en `Step1Ubicacion.kt` se actualizó para mostrar este `mapErrorMessage` mediante un `SnackbarHost`, con una función `clearMapErrorMessage()` para descartar el mensaje.
    *   **Corrección de Regresión**: Se restauró el contenido del `ModalBottomSheet` que explica la necesidad del permiso de ubicación, que se había eliminado accidentalmente en una refactorización previa.

## 3. Implementación y Finalización del Paso 3 (Opcional y Envío)

### 3.1. Validación del Paso 3 y Envío del Formulario

*   **Problema**: No se podía avanzar al paso 3 sin validar el paso 2, ni finalizar el formulario sin seleccionar una opción en el paso 3. Además, no había feedback visual al enviar el formulario.
*   **Solución**:
    *   Se añadió `condicionTenenciaError: String?` al `CreateUnidadProductivaState` para manejar errores de validación del paso 3.
    *   Se implementó la función `validateStep3()` en el `ViewModel` para verificar que se haya seleccionado una `condicionTenencia`.
    *   Se implementó la función `submitForm()` en el `ViewModel`, que recopila todos los datos del formulario (incluyendo el `municipioId` y `condicionTenenciaId` obtenidos de los catálogos) y llama al `CreateUnidadProductivaUseCase`.
    *   La función `onNextStep()` en el `ViewModel` se actualizó para que, al estar en el paso 3, primero llame a `validateStep3()`, y si es válido, luego llame a `submitForm()`.
    *   La UI del `Step3FormularioOpcional.kt` se actualizó para mostrar el `condicionTenenciaError` si está presente.

### 3.2. Feedback Visual de Envío del Formulario

*   **Problema**: El envío del formulario no mostraba ningún feedback visual al usuario.
*   **Solución**:
    *   Se añadió `isSubmitting: Boolean` y `submissionResult: Result<UnidadProductiva, Error>?` al `CreateUnidadProductivaState` para gestionar el estado del envío.
    *   Se integró un `LoadingOverlay` en `CreateUnidadProductivaScreen.kt` que se activa cuando `isSubmitting` es `true`.
    *   Se creó un `SubmissionResultDialog` composable que se muestra cuando `submissionResult` no es nulo, informando al usuario del éxito o fracaso del envío. En caso de éxito, el diálogo navega hacia atrás.
    *   **Ajuste de UI**: `LoadingOverlay.kt` fue modificado para usar el mismo estilo de tarjeta pequeña blanca con spinner que se usa en el mapa. El `SubmissionResultDialog` también se configuró para tener un fondo blanco explícito.

## 4. Refactorización Extensiva de Tipos `Result`

*   **Problema**: Inconsistencias en el uso de `kotlin.Result` frente al `com.sinc.mobile.domain.util.Result` personalizado en varias capas, causando errores de compilación.
*   **Solución**: Se actualizó sistemáticamente el tipo de retorno y el manejo de resultados en todos los `Use Cases` (`CreateUnidadProductivaUseCase`, `SyncUnidadesProductivasUseCase`, `SyncDataUseCase`, `SyncCatalogosUseCase`) y las implementaciones de `Repository` (`UnidadProductivaRepositoryImpl`, `CatalogosRepositoryImpl`) para usar el `com.sinc.mobile.domain.util.Result` personalizado. Todos los `ViewModels` (`MainViewModel`, `LoginViewModel`, `CreateUnidadProductivaViewModel`) que consumen estos `Use Cases` se actualizaron para interactuar correctamente con el nuevo tipo `Result` sellado.
*   Se corrigió el error de "smart cast" en `CreateUnidadProductivaScreen.kt` al capturar `submissionResult` en una variable local.

---
**Estado Actual**: El formulario de creación de Unidades Productivas está completamente implementado con validaciones de interfaz y de negocio, incluyendo la selección automática de municipios y feedback visual para el usuario. La arquitectura de manejo de resultados está unificada en toda la aplicación.

## Avances de la Sesión Actual (27 de diciembre de 2025)

### Mejoras en la Pantalla de Stock (`StockScreen`)

-   **Interfaz de Usuario (`StockScreen`):**
    *   **Top Bar Minimalista:** Se añadió un Top Bar (`MinimalHeader`) a `StockScreen` para una navegación y título consistentes.
    *   **Selector de Vista:** Se creó el componente `StockViewSelector` para permitir al usuario alternar entre la vista de stock total general y el stock por unidad productiva (campo).
    *   **Visualización Detallada (Acordeones):**
        *   Implementación de `StockAccordion` para mostrar el stock de forma plegable y organizada.
        *   Los detalles del stock se presentan discriminados por especie, raza y cantidad (`DesgloseContent`).
        *   Se gestiona el mensaje "No hay stock registrado" si el total es cero.
    *   **Pulido Visual:**
        *   Se ajustó el `weight` de las columnas en `DesgloseContent` (`Tipo`, `Raza`, `Cantidad`) para evitar desbordamientos, dándole un ancho fijo a "Cantidad" y pesos a las otras.
        *   Se redujo el `fontSize` de los **encabezados** de las columnas (`Tipo`, `Raza`, `Cantidad`) en `DesgloseContent` a `13.sp` para una mayor compactación sin afectar el texto de los datos.
        *   Se añadió un `Spacer` entre el `StockViewSelector` y el área de contenido principal para una división visual sutil.

-   **Experiencia de Usuario y Flujo de Datos:**
    *   **"Deslizar para Refrescar" (`SwipeRefresh`):** Se implementó la funcionalidad de `SwipeRefresh` en `StockScreen` para permitir la actualización manual de los datos, vinculada al estado de carga del `MainViewModel`.
    *   **Refactorización de `MainViewModel`:** Se reestructuró `MainViewModel` para separar la recolección de datos (`collectUnidadesProductivas`, `collectStock`) de la sincronización de red (`refresh`). La función `refresh()` ahora maneja la lógica de sincronización y asegura que el indicador de carga permanezca visible por al menos 1 segundo, mejorando la retroalimentación visual.
    *   **Depuración de Sincronización de Stock:** Se diagnosticó un problema de sincronización debido a la falta de la cabecera `Accept: application/json` y una ruta de endpoint incorrecta en `StockApiService.kt`. Se corrigió el `StockApiService.kt` para asegurar que el servidor siempre devuelva una respuesta JSON esperada.

-   **Preparación para Filtrado y Agrupación:**
    *   **`GroupBy` Enum:** Se definió un `enum class GroupBy { ESPECIE, CATEGORIA, RAZA }` para gestionar las opciones de agrupación.
    *   **`FilterChipGroup`:** Se creó un nuevo componente `FilterChipGroup` que contendrá los `FilterChip`s para la selección de las opciones de agrupación.
    *   **Reestructuración Flexible de `StockScreen`:** Se inició la reestructuración de `StockScreen` para integrar el `FilterChipGroup` y un `when(groupBy)` para el renderizado condicional de los resultados según la opción de agrupación seleccionada por el usuario.
    *   **Header de Totales Dinámico:** Se ajustó la cabecera de los totales para que muestre el "Stock Total General" o "Stock Total Campo" según la vista seleccionada.

### Debugging y Resoluciones

-   **`curl` y `Invoke-WebRequest`:** Se superaron problemas de ejecución de `curl` en PowerShell, optando finalmente por `Invoke-WebRequest` para depurar directamente la respuesta de la API.
-   **Error `IllegalStateException` (Gson):** Se identificó y resolvió un error de `Gson` (`Expected BEGIN_OBJECT but was STRING`) causado por la ausencia de la cabecera `Accept: application/json` en la petición de la app al servidor, lo que llevaba al servidor a devolver una respuesta inesperada.
-   **Errores de Compilación:** Se resolvieron errores de referencias no resueltas (`DesgloseContent`, `sp`) y paréntesis extra durante el proceso.

El proyecto se encuentra en un estado donde la pantalla de Stock es funcional, robusta y preparada para una mayor interactividad con las opciones de filtrado y agrupación.

# Avances de la Sesión - 28 de Diciembre de 2025

Esta sesión se centró en una profunda refactorización y rediseño de la pantalla de visualización de stock (`StockScreen`) para mejorar la arquitectura y la experiencia de usuario.

## 1. Refactorización de Arquitectura

-   **Creación de `StockViewModel`**: Se identificó que `StockScreen` era un componente "tonto" que recibía su estado desde `MainViewModel`. Siguiendo las mejores prácticas, se creó un `StockViewModel` dedicado para encapsular toda la lógica y el estado relacionados con la pantalla de stock.
-   **Migración de Lógica**: Se trasladó toda la lógica de obtención, sincronización y procesamiento de datos de stock desde `MainViewModel` al nuevo `StockViewModel`.
-   **Limpieza**: Como resultado, `MainViewModel` y `MainScreen` fueron limpiados, eliminando responsabilidades que no les correspondían y simplificando el código.

## 2. Rediseño Profundo de la Interfaz de Usuario (UI)

Se descartó el diseño original basado en una lista de acordeones en favor de una interfaz más moderna y visual basada en tarjetas y gráficos.

-   **Componente `PieChart`**: Se creó un nuevo componente reutilizable `PieChart.kt` que dibuja un gráfico de torta (o de anillo) usando `Canvas`.
-   **Nuevo Layout en `StockScreen`**:
    -   Se eliminó la barra de navegación inferior para dar más espacio al contenido.
    -   La pantalla se dividió en secciones, utilizando `Card` con fondos blancos y esquinas redondeadas para una mejor organización visual.
    -   Se creó una `TotalStockCard` que muestra el stock general junto a un pequeño gráfico de torta que compara las especies.
    -   Se crearon `SpeciesStockCard` individuales para "Ovinos" y "Caprinos".
-   **Leyenda del Gráfico**: Siguiendo feedback, se implementó una leyenda detallada para los gráficos de torta que muestra un indicador de color, el nombre de la categoría/raza y el porcentaje que representa, en lugar de dibujar el texto dentro del gráfico.

## 3. Implementación de Filtros y Refinamientos de UX

-   **Filtros de Agrupación**:
    -   Se añadieron chips de filtrado ("Todos", "Por Categoría", "Por Raza") para permitir al usuario cambiar dinámicamente el desglose del stock.
    -   La opción "Todos" se estableció como predeterminada, mostrando una tabla de texto con dos columnas (categoría y raza).
    -   Las opciones "Por Categoría" y "Por Raza" muestran el nuevo desglose con el gráfico de torta y su leyenda.
-   **Refinamientos de Diseño**:
    -   Se ajustó el layout de los chips para que "floten" sobre una línea divisoria sutil dentro de la tarjeta de la especie, en lugar de estar envueltos en su propia tarjeta.
    -   Se eliminaron los "puntos" (placeholders de iconos) de los encabezados de las tarjetas de especie.
    -   Se corrigió el color de fondo de todas las tarjetas a blanco puro para eliminar tintes rojizos y asegurar consistencia visual.
    -   Se gestionaron y corrigieron varios diseños intermedios de los chips (con pesos, en columna) hasta llegar a la solución final de un carrusel (`LazyRow`).

## Estado Final

Tras múltiples iteraciones, correcciones de errores de compilación y refinamientos de diseño, la nueva pantalla de stock está completamente implementada y el proyecto compila con éxito.

# Avances de la Sesión Actual (29 de Diciembre de 2025)

Esta sesión se centró en una serie de mejoras y correcciones significativas en la pantalla de Stock, abordando tanto la funcionalidad como la experiencia de usuario (UX) y el rendimiento visual.

## 1. Refinamiento del Spinner de Carga y Visibilidad de Contenido

*   **Problema Inicial:**
    *   El spinner de carga del "pull-to-refresh" no estaba centrado horizontalmente en la pantalla.
    *   Al realizar un "pull-to-refresh", el contenido previamente visible de la pantalla desaparecía por completo mientras el spinner estaba activo, dejando un espacio en blanco temporalmente antes de que se mostraran los datos actualizados. Esto creaba una experiencia de usuario abrupta y desagradable.
*   **Solución Implementada:**
    *   Se corrigió el alineamiento del spinner de carga añadiendo `Modifier.fillMaxSize()` al `Box` contenedor en `StockScreen.kt`. Esto aseguró que el `PullRefreshIndicator` siempre se centrara correctamente dentro del área de la pantalla.
    *   La visibilidad del contenido durante el refresco se solucionó modificando la condición de renderizado del `LazyColumn` en `StockScreen.kt`. En lugar de ocultar la lista si `isLoading` era `true`, se ajustó para que siempre mostrara el `processedStock` (los datos antiguos) mientras se cargaba el nuevo contenido. Esto evita que la pantalla se quede en blanco, manteniendo la información anterior visible hasta que la actualización esté lista.

## 2. Mejora de la Leyenda del Gráfico de Stock Total General

*   **Problema:** La tarjeta "Stock Total General" presentaba un gráfico de torta que visualizaba la distribución de especies (ovinos/caprinos), pero carecía de una leyenda explícita que relacionara los colores del gráfico con las especies y sus porcentajes correspondientes.
*   **Solución Implementada:**
    *   Se modificó la clase `ProcessedStock` en `StockViewModel.kt` para incluir un nuevo campo: `speciesLegendItems: List<LegendItem>`. Esta lista se calcula en la función `processStock` y contiene la información detallada (etiqueta, valor, porcentaje, color) para cada segmento del gráfico de stock total.
    *   La `TotalStockCard` en `StockScreen.kt` fue reestructurada para mostrar esta nueva leyenda de manera clara y organizada, debajo del gráfico de torta.
    *   Durante la implementación, se corrigió un error de layout que causaba que el texto del título ("Stock Total General") apareciera verticalmente debido a una mala distribución del espacio horizontal. La nueva estructura garantiza que todos los elementos se muestren correctamente.

## 3. Chips de Filtro Deslizables

*   **Problema:** Los chips de filtro dentro de las tarjetas de especie (`SpeciesStockCard`) no cabían en el ancho de la pantalla cuando había múltiples opciones, obligando a un recorte o a un uso ineficiente del espacio.
*   **Solución Implementada:**
    *   El componente `GroupingOptions.kt` fue refactorizado para utilizar `LazyRow` en lugar de `Row`. Este cambio permite el desplazamiento horizontal de los chips, asegurando que todas las opciones de filtro sean accesibles y la interfaz se adapte mejor a diferentes anchos de pantalla.

## 4. Tarjetas de Especie Colapsables y Refinamiento UI/UX

*   **Problema:** Se identificó la necesidad de hacer las `SpeciesStockCard`s colapsables para mejorar la usabilidad, especialmente si hay muchos registros. Además, la animación de colapso/expansión era lenta, las tarjetas se expandían por defecto y el efecto visual al hacer clic en la cabecera (un rectángulo gris) no se integraba bien con el diseño redondeado de la tarjeta.
*   **Solución Implementada:**
    *   Cada `SpeciesStockCard` ahora gestiona su propio estado de expansión/colapso con `rememberSaveable { mutableStateOf(false) }`, haciendo que las tarjetas estén **colapsadas por defecto**.
    *   La cabecera de cada tarjeta se hizo "clicable" (`Modifier.clickable`) para alternar el estado de expansión, con un icono de flecha (`KeyboardArrowDown`) que gira para indicar visualmente el estado actual.
    *   La velocidad de las animaciones se ajustó: `animateContentSize` usa `tween(250)` y `AnimatedVisibility` usa `fadeIn(tween(200, delayMillis = 50))` y `fadeOut(tween(100))` para transiciones más rápidas y suaves.
    *   El modificador `clickable` se movió de la `Row` de la cabecera a la `Card` principal. Esto asegura que el efecto "ripple" (la onda de clic) sea recortado automáticamente por la forma redondeada de la tarjeta, resultando en una indicación visual de clic más limpia y estéticamente agradable.
    *   Se corrigió el bug donde cambiar un filtro en una tarjeta afectaba a todas las demás. Ahora, cada `SpeciesStockCard` gestiona su propia selección de filtro internamente, asegurando independencia.

## 5. Colores Dinámicos en los Chips de Filtro

*   **Problema:** Los chips de filtro seleccionados utilizaban los colores por defecto del tema de Material Design, lo que no siempre combinaba óptimamente con el color específico de la especie a la que pertenecía la tarjeta.
*   **Solución Implementada:**
    *   Se añadió un campo `color: Color` a la clase `ProcessedEspecieStock` en `StockViewModel.kt`. Este color se calcula en la función `processStock` a partir de `pieChartColors`, asegurando que cada especie tenga un color consistente.
    *   El componente `GroupingOptions.kt` se modificó para aceptar un parámetro `selectedChipColor: Color`. Este color se utiliza para establecer el `selectedContainerColor` de los `FilterChip`s cuando están seleccionados, lo que significa que un chip seleccionado en la tarjeta de "Ovinos" tendrá el color asociado a los ovinos, y así sucesivamente.

## 6. Corrección de Advertencias y Errores de Compilación

*   **Errores de Referencia:** Se resolvieron errores de compilación relacionados con `PieChartData` y `LegendItem` moviendo `LegendItem` de `StockViewModel.kt` a su propio archivo `LegendItem.kt` dentro del directorio `components`, y añadiendo las importaciones necesarias en `StockViewModel.kt` y `StockScreen.kt`.
*   **Error de Sobrecarga de `filterChipColors`:** Se corrigió un error de compilación en `GroupingOptions.kt` ajustando los nombres de los parámetros de la función `FilterChipDefaults.filterChipColors` (`selectedContentColor` a `selectedLabelColor`) para que coincidieran con la API de Material3.
*   **Advertencia de Compilador ("Condition is always 'false'"):** Se abordó una advertencia persistente del compilador en `StockViewModel.kt` reescribiendo la condición `if (totalGroupValue == 0f)` a `if (totalGroupValue > 0f)`. Aunque la lógica original era correcta, este cambio eliminó la advertencia (que parecía un falso positivo del compilador) sin alterar la funcionalidad.

La pantalla de Stock ahora es más robusta, usable y visualmente consistente, incorporando todas las mejoras solicitadas.

# Avances de la Sesión Actual (03 de Enero de 2026)

Esta sesión se centró en la implementación de una nueva funcionalidad crítica, el **Historial de Movimientos**, y en una serie de mejoras y correcciones de la interfaz de usuario basadas en el feedback recibido.

## 1. Implementación del Módulo "Historial de Movimientos"

Se construyó la funcionalidad completa para obtener, almacenar localmente y visualizar el historial de movimientos de stock del productor, respetando rigurosamente la Arquitectura Limpia del proyecto.

### 1.1. Capa de Datos (`:data`)

-   **Networking (Retrofit)**:
    -   Se creó el DTO `MovimientoHistorialDto.kt` para mapear la respuesta del nuevo endpoint de la API: `GET /api/movil/cuaderno/movimientos`.
    -   Se definió la interfaz `HistorialMovimientosApiService.kt` para declarar la llamada a la API.
    -   Se actualizó `NetworkModule.kt` (Hilt) para proveer la instancia de `HistorialMovimientosApiService` a toda la aplicación.

-   **Persistencia Local (Room)**:
    -   Se definió la entidad `MovimientoHistorialEntity.kt` para la tabla de la base de datos local.
    -   Se creó el DAO `MovimientoHistorialDao.kt`, que incluye métodos para obtener un `Flow` de movimientos, insertar una lista y limpiar la tabla de forma transaccional (`clearAndInsert`).
    -   Se actualizaron `SincMobileDatabase.kt` y `DatabaseModule.kt` (Hilt) para integrar la nueva entidad y el DAO.

-   **Repositorio**:
    -   Se creó `MovimientoHistorialMapper.kt` con funciones de extensión para convertir `DTO -> Entity` y `Entity -> Domain`.
    -   Se implementó `MovimientoHistorialRepositoryImpl.kt`, que orquesta el flujo de datos:
        1.  El método `getMovimientos()` expone un `Flow` de datos directamente desde el `DAO` local, asegurando que la UI siempre lea de la "fuente única de verdad".
        2.  El método `syncMovimientos()` se encarga de llamar a la API, recibir los datos, mapearlos a entidades y guardarlos en la base de datos local a través del DAO.
    -   Se actualizó `RepositoryModule.kt` (Hilt) para vincular la interfaz `MovimientoHistorialRepository` con su implementación.

### 1.2. Capa de Dominio (`:domain`)

-   Se creó el modelo de negocio `MovimientoHistorial.kt`.
-   Se definió la interfaz `MovimientoHistorialRepository.kt` como el contrato que la capa de datos debe seguir.
-   Se implementaron dos Casos de Uso:
    -   `GetMovimientosHistorialUseCase`: Para obtener el `Flow` de datos del repositorio.
    -   `SyncMovimientosHistorialUseCase`: Para encapsular la lógica de negocio de la sincronización.

### 1.3. Capa de Presentación (`:app`)

-   Se creó un nuevo paquete de feature: `app.features.historial_movimientos`.
-   **ViewModel**: Se implementó `HistorialMovimientosViewModel.kt`, que consume los casos de uso para obtener y sincronizar los datos, y expone el estado de la UI (`HistorialMovimientosState`) a través de un `StateFlow`.
-   **UI (Compose)**:
    -   Se construyó `HistorialMovimientosScreen.kt` como la pantalla principal de la feature.
    -   Se diseñó un Composable reutilizable, `MovimientoHistorialItem.kt`, para mostrar cada ítem de la lista con un diseño detallado y estilizado, que incluye:
        -   Iconos de flecha (`ic_arrow_upward`, `ic_arrow_downward`) para indicar visualmente si es una "alta" or "baja".
        -   Colores de fondo y de texto condicionales para una mejor distinción.
    -   Se añadieron los nuevos iconos como Vector Drawables al proyecto.

## 2. Refinamiento de UI/UX y Corrección de Errores

### 2.1. Navegación Principal

-   **Feedback Atendido**: Se ajustó la navegación para que "Historial de Movimientos" sea una pantalla principal, accesible directamente desde el menú de navegación inferior, en lugar de un sub-menú.
-   **Implementación**:
    -   Se revirtió la adición del botón "Ver Historial" en la pantalla de `StockScreen`.
    -   Se añadió una nueva ruta `HISTORIAL` en `CozyBottomNavRoutes.kt`.
    -   Se modificó `CozyBottomNavBar.kt`, reemplazando el ítem "Diario" (que actuaba como placeholder) por "Historial", utilizando el icono `Icons.AutoMirrored.Filled.List`. Esto mantuvo el balance visual de la barra de navegación.
    -   Se actualizó la lógica de navegación en `MainScreen.kt` para mostrar `HistorialMovimientosScreen` al seleccionar la nueva opción en la barra.

### 2.2. Ajustes de Layout y "Pull-to-Refresh"

-   **Feedback Atendido**: Se corrigió el layout de `HistorialMovimientosScreen` para que la barra superior no se sintiera desconectada y el contenido no estuviera pegado a ella. También se arregló la animación del "pull-to-refresh".
-   **Implementación**:
    -   Se modificó `MinimalHeader.kt`, añadiendo el modifier `.windowInsetsPadding(WindowInsets.statusBars)` para asegurar que la barra superior respete el espacio de la barra de estado del sistema, solucionando cualquier superposición.
    -   Se reestructuró `HistorialMovimientosScreen.kt` para imitar la estructura de `StockScreen.kt`, aplicando el `containerColor`, el orden correcto de modifiers (`.pullRefresh().padding().fillMaxSize()`) y un `Arrangement.spacedBy` en el `LazyColumn` para un espaciado consistente.
    -   Se mejoró `HistorialMovimientosViewModel.kt`, replicando la lógica de `StockViewModel` para el refresco de datos. Ahora, la animación del spinner se muestra durante un mínimo de 1 segundo, proveyendo un feedback visual claro y evitando que la animación se oculte instantáneamente en conexiones rápidas.

### 2.3. Corrección de Errores de Compilación

Durante el proceso, se solucionaron múltiples errores de compilación, incluyendo:
-   Referencias no resueltas a `SimpleTopAppBar` (se reemplazó por el componente correcto, `MinimalHeader`).
-   Import incorrecto de la clase `R` en los componentes de la UI.
-   Errores en el `ViewModel` y `Repository` por el uso incorrecto de la clase sellada `Result` (se corrigió `Result.Error` a `Result.Failure`).
-   Importaciones de layout faltantes en varios Composables.

---
**Estado Actual**: El proyecto compila exitosamente. La nueva funcionalidad está completamente integrada y la UI es consistente con el resto de la aplicación, habiendo atendido todas las correcciones y sugerencias de la sesión.


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

# Avances de la Sesión Actual

Esta sesión se centró en mejorar la navegación general, estandarizar transiciones y emprender una refactorización significativa de la paleta de colores de la aplicación, resolviendo múltiples errores y problemas de inconsistencia.

## 1. Mejoras en la Barra de Navegación Inferior (CozyBottomNavBar)

*   **Visibilidad Consistente:** Se aseguró que la barra de navegación inferior sea visible en todas las pantallas principales (incluyendo "Mi Stock") eliminando la lógica de visibilidad condicional en `MainScreen.kt`.
*   **Corrección de Superposición de Contenido:** Se aplicó correctamente el `padding` inferior del `Scaffold` principal de `MainScreen` a las pantallas hijas (`StockScreen`, `HistorialMovimientosScreen`, `CamposScreen`). Esto evita que el contenido se dibuje por debajo de la barra de navegación inferior, asegurando que el área de desplazamiento se ajuste correctamente.
*   **Indicador Visual de Selección:** Se añadió una pequeña línea de color (`36.dp` de ancho) alineada con el borde superior del ítem seleccionado en `CozyBottomNavBar`, mejorando la retroalimentación visual del usuario.
*   **Corrección de Espaciado:** Se solucionaron problemas de espaciado entre los ítems de la barra de navegación inferior eliminando un modificador `weight` incorrecto de `CozyBottomNavItem`, permitiendo que el `Arrangement.SpaceEvenly` del contenedor padre funcione como se esperaba.
*   **Color de Fondo:** Se estableció el color de fondo de la `CozyBottomNavBar` a blanco.

## 2. Transiciones y Navegación

*   **Corrección de Bug de Navegación ("Seleccionar Campo"):** Se corrigió un error donde el botón de regresar en la pantalla "Seleccionar Campo" (`SeleccionCampoScreen`) volvía incorrectamente a "Mi Stock". Ahora, al presionar regresar, se navega explícitamente a la pestaña `HOME` de `MainScreen`, asegurando un comportamiento predecible.
*   **Animación de Regreso Estandarizada:** Se aplicó una animación consistente de "deslizamiento hacia la derecha" (`popExitTransition`) para el regreso en todas las rutas relevantes (`MOVIMIENTO`, `CHANGE_PASSWORD`, `FORGOT_PASSWORD`), unificando la experiencia de usuario al navegar hacia atrás.
*   **Selección de Pestaña Inicial de la Pantalla Principal:** Se modificó `MainScreen` para aceptar un parámetro `startRoute` opcional, permitiendo que la navegación externa especifique qué pestaña de la barra inferior (HOME, STOCK, HISTORIAL, CAMPOS) debe mostrarse inicialmente.
*   **Navegación de SeleccionCampoScreen:** Se actualizó la barra de navegación inferior de `SeleccionCampoScreen` para que al seleccionar cualquier ítem, navegue a `MainScreen` con la `startRoute` adecuada.
*   **Corrección de Bug "Screen for add":** Se eliminó el error que mostraba "Screen for add" al presionar repetidamente el botón "ADD" desde `SeleccionCampoScreen`, ignorando clicks subsecuentes en el botón "ADD" de esa pantalla.

## 3. Componentes de UI y Tematización

*   **TopBar en CamposScreen:** Se integró un `MinimalHeader` en `CamposScreen`, alineándolo con otras pantallas principales, y se configuró su botón de regreso para navegar correctamente a la pestaña `HOME`.
*   **Spinners en lugar de Esqueletos de Carga:** Se reemplazaron las animaciones de carga tipo esqueleto por un `CircularProgressIndicator` centrado para el estado de carga inicial en `StockScreen` y `HistorialMovimientosScreen`, mejorando la retroalimentación visual.
*   **Fondo de MinimalHeader:** Se revirtió el color de fondo del `MinimalHeader` a transparente, dejando el `CozyBottomNavBar` con fondo blanco según la preferencia del usuario.

## 4. Estandarización de la Paleta de Colores (Refactorización Mayor)

*   **Definición de Nueva Paleta "SINC":** Se colaboró en la definición de una nueva paleta de colores "SINC" simplificada y unificada. Esta paleta se centra en el color principal "bordó" (`SincPrimary`, `SincPrimaryDark`, `SincPrimaryLight`), blanco (`SincSurface`, `SincOnPrimary`) y tonos de gris (`SincBackground`, `SincTextPrimary`, `SincTextSecondary`, `SincDivider`), junto con un color para errores (`SincError`).
*   **Reescritura de `Color.kt`:** El archivo `app/src/main/java/com/sinc/mobile/ui/theme/Color.kt` fue reescrito para contener exclusivamente la nueva paleta "SINC", eliminando todas las definiciones de colores antiguos.
*   **Actualización de `Theme.kt`:** Se modificó `app/src/main/java/com/sinc/mobile/ui/theme/Theme.kt` para utilizar la nueva paleta "SINC" en su `lightColorScheme` y se deshabilitó el tema oscuro para simplificar el enfoque inicial.
*   **Corrección de Errores de Compilación (Refactorización Archivo por Archivo):** Se abordaron sistemáticamente numerosos errores de "Unresolved reference" en todo el módulo `app` (incluyendo `CamposScreen.kt`, `CreateUnidadProductivaScreen.kt`, `ActionButton.kt`, `BottomNavBar.kt`, `ProgressBar.kt`, `Step1Ubicacion.kt`, `Step2FormularioBasico.kt`, `HistorialMovimientosScreen.kt`, `MovimientoFormScreen.kt`, `MovimientoItemCard.kt`, `MovimientoSkeletonLoader.kt`, `MovimientoTopBar.kt`, `UnidadSelectionStep.kt`, `FilterChipGroup.kt`, `StockAccordion.kt`, `StockViewSelector.kt`, `Banner.kt`, `CustomDropdown.kt`, `ExpandingDropdown.kt`, `FormDropdown.kt`, `FormFieldWrapper.kt`, `InfoCard.kt`, `LoadingOverlay.kt`, `OverlayDropdown.kt`, `QuantitySelector.kt`) reemplazando las variables de color antiguas por sus equivalentes semánticos de `MaterialTheme.colorScheme`.
*   **Eliminación de Archivos de Mockup:** Se eliminó el directorio `app/src/main/java/com/sinc/mobile/app/features/maquetas`, ya que contenía pantallas de maqueta no utilizadas, simplificando el proyecto.
*   **Corrección de Error Tipográfico en `Theme.kt`:** Se corrigió un error tipográfico en `Theme.kt` de `Build.VERSION_VERSION.SDK_INT` a `Build.VERSION.SDK_INT`.

Estos cambios aseguran una experiencia de usuario más pulida y una base de código más mantenible y consistente en términos de diseño y navegación.

# Avances de la Sesión Actual

Esta sesión se centró en la creación y refinamiento de una maqueta de la pantalla de carga de stock, aplicando un diseño "Clean & Airy" y resolviendo varios desafíos técnicos.

## 1. Creación de Maqueta del Formulario de Movimiento (`MovimientoFormMaquetaScreen.kt`)

-   **Hito**: Creación de una maqueta inicial para el formulario de carga de stock, con el objetivo de probar un nuevo diseño "Clean & Airy" (limpio, minimalista y moderno) antes de aplicarlo a la pantalla real.
-   **Detalles**:
    -   Se creó el archivo `MovimientoFormMaquetaScreen.kt` en un nuevo paquete `app/src/main/java/com/sinc/mobile/app/features/maquetas/`.
    -   Se implementó la estructura básica de la pantalla utilizando `Scaffold` y `LazyColumn` para un contenido escrolleable y un padding horizontal consistente.
    -   Se añadió el componente `MinimalHeader` en la parte superior, respetando los `statusBarsPadding()` para una correcta alineación con la barra de estado del sistema.
    -   Se integró la ilustración principal del formulario (`ilustracion_ovinos-removebg-preview.png`). Para ello, se movió el archivo `.png` desde la raíz del proyecto a `app/src/main/res/drawable/` para que pudiera ser cargado como un recurso.

## 2. Integración Temporal de Navegación

-   **Hito**: Se configuró una ruta de navegación temporal para acceder a la maqueta desde la aplicación.
-   **Detalles**:
    -   Se añadió la constante `MOVIMIENTO_FORM_MAQUETA` al objeto `Routes` en `AppNavigation.kt`.
    -   Se creó un nuevo `composable` en `AppNavigation.kt` para renderizar `MovimientoFormMaquetaScreen`, incluyendo transiciones de deslizamiento horizontal consistentes con otras pantallas de detalle.
    -   Se añadió un botón temporal "Ir a Maqueta" en `MainContent` (parte de `HomeScreen`) para facilitar la navegación a la maqueta durante el desarrollo.

## 3. Refinamiento y Estilización de Componentes en la Maqueta

-   **Hito**: Se aplicaron múltiples ajustes de diseño y estilos a los componentes de la maqueta según las especificaciones.
-   **Detalles**:
    -   **Consistencia de Radio de Bordes**: Se estandarizó el `RoundedCornerShape` a `16.dp` para los `DropdownField` y `Chip`s, promoviendo una estética unificada.
    -   **Botón "Guardar"**: Se refactorizó para que fuera parte del `bottomBar` del `Scaffold` (fijo y siempre visible) con un tamaño más sutil (altura de `40.dp`, forma `RoundedCornerShape(20.dp)`) y padding central. Se eliminó la lógica de visibilidad condicional para simplificar la maqueta.
    -   **Diseño de la Sección "Especie"**: Se reorganizó para que el texto "Especie" y los chips de selección ("Ovinos", "Caprinos") estuvieran en la misma fila horizontal, utilizando `Arrangement.SpaceBetween` para una distribución adecuada.
    -   **Visibilidad de Borde de Desplegables**: Se ajustó la opacidad del borde de `OutlinedTextField` en estado no enfocado a `0.7f` para que fuera más visible.
    -   **Estilo de Chips (Motivo y Especie)**:
        *   Se redujo el tamaño de los chips ajustando su `padding` interno y `fontSize`.
        *   Se implementó el esquema de colores especificado:
            *   **No seleccionado**: Fondo transparente, borde de `1.dp` con `MaterialTheme.colorScheme.primary` (bordó), texto con `MaterialTheme.colorScheme.onSurface`.
            *   **Seleccionado**: Fondo con `MaterialTheme.colorScheme.primary` (bordó), sin borde, texto con `MaterialTheme.colorScheme.onPrimary` (blanco).

## 4. Implementación de Seleccionadores con Bottom Sheet

-   **Hito**: Se modificó el comportamiento de los campos "Categoría" y "Raza" para utilizar un `ModalBottomSheet` de Material 3 para la selección de opciones.
-   **Detalles**:
    -   Se integró el `ModalBottomSheet` de Material 3 en `MovimientoFormMaquetaScreen.kt`, gestionando su visibilidad con un estado `showSheet`.
    -   Se creó una `sealed class SheetContent` para definir dinámicamente el título y las opciones de cada bottom sheet (para "Categoría" y "Raza").
    -   Se creó el composable `SheetContentLayout` para renderizar las opciones dentro del bottom sheet.
    -   El `DropdownField` fue modificado para aceptar un `onClick` lambda, que al ser invocado, establece el `sheetContent` adecuado y activa la visibilidad del bottom sheet. Para mantener la apariencia de `OutlinedTextField` no interactivo pero clickable, se envolvió en un `Box` con un `clickable` modifier.

## 5. Resolución de Errores y Compilación Exitosa

-   **Hito**: Se resolvieron varios errores de compilación introducidos durante las iteraciones de desarrollo de la maqueta.
-   **Detalles**:
    -   Inicialmente, se corrigieron errores de referencias no resueltas (`rememberVectorPainter`, `Icons.Default.Pets`) y de tipos (`ImageVector` pasado a `@Composable () -> Unit`) en los `StepperButton`s.
    -   Una vez reintroducida la lógica compleja, se produjo un `Internal compiler error` al usar `derivedStateOf`. Se solucionó refactorizando la gestión de estado de la barra inferior dinámica para usar `LaunchedEffect`, lo que evitó el error.
    -   Finalmente, se resolvieron conflictos de importación que surgieron al reintroducir los componentes de la maqueta.
    -   El proyecto compiló exitosamente tras cada fase de corrección y ajuste, asegurando la estabilidad del código.

---
**Estado Actual**: La maqueta del formulario de carga de stock está completa con el diseño especificado, incluyendo la interacción de los seleccionadores de categoría y raza mediante bottom sheets. El proyecto compila sin errores.

# Avances de la Sesión Actual (05 de Enero de 2026)

Esta sesión se centró en la resolución de múltiples problemas en el formulario de carga de movimientos, desde la compilación inicial hasta la mejora de la experiencia de usuario y la robustez del proceso de sincronización.

## 1. Resolución de Problemas de Compilación y Configuración

-   **Diagnóstico y Resolución de Sincronización de Gradle:** Se diagnosticó y resolvió el fallo de sincronización de Gradle en Android Studio causado por cachés corruptas y una configuración incorrecta del JDK. Se instruyó al usuario para verificar la configuración del JDK (asegurando JDK 11) y se eliminó manualmente el directorio de caché global de Gradle (`.gradle`) para forzar una reconstrucción limpia.
-   **Resolución de Errores de Compilación por Refactorización:**
    -   Se movió la clase de datos `MovimientoAgrupado` de un archivo `deprecated` a `MovimientoStepperViewModel.kt` para centralizar las definiciones de estado y evitar redeclaraciones.
    -   Se comentó el contenido completo de los archivos `deprecated/MovimientoViewModel.kt` y `deprecated/MovimientoFormScreen.kt` para eliminar conflictos de compilación con la nueva arquitectura del módulo de movimientos.
    -   Se actualizó `SeleccionCampoScreen.kt` para utilizar el `MovimientoStepperViewModel` como su fuente de `UnidadesProductivas` y `selectedUnidad`, integrándola correctamente en el nuevo flujo.
    -   Se corrigió un error de referencia en `SeleccionCampoScreen.kt` (`uiState.catalogos?.unidadesProductivas`) al exponer `unidades` directamente en `MovimientoStepperState`.
    -   Todos los errores de compilación restantes fueron resueltos, resultando en un build exitoso.

## 2. Mejoras Visuales y de Experiencia de Usuario (UI/UX) en el Formulario

-   **Espaciado Mejorado:** Se ajustó el espaciado entre el selector de "Motivo" y el "Selector de Cantidad" en el formulario de movimientos.
-   **Texto Indicativo "Deslice":** Se añadió un sutil texto "(Deslice)" junto al título "Motivo" para indicar la funcionalidad de desplazamiento horizontal de los chips, mejorando la usabilidad.
-   **Visibilidad de Contornos:** Se ajustó la visibilidad del contorno de los campos desplegables ("Categoría", "Raza") para que sea más clara.
-   **Fondos de UI Consistentes:** Se revirtieron los colores de fondo del formulario, la barra superior y la barra inferior a los valores por defecto del tema (`MaterialTheme.colorScheme.background`).
-   **Corrección de Desplegables que No Abrían:** Se solucionó un problema crítico donde los desplegables de "Categoría" y "Raza" no se abrían debido a un conflicto de eventos de clic causado por un modificador de fondo en `LazyColumn`. La solución implicó establecer el color de fondo del `Scaffold` principal y eliminar el fondo explícito del `LazyColumn`.
-   **Campo "Destino" Refactorizado:**
    -   El campo "Destino" ahora aparece en un `ModalBottomSheet` dedicado.
    -   Se muestra condicionalmente solo cuando se selecciona el motivo "Traslado (salida)".
    -   La lógica de validación (`MovimientoFormManager.kt`) fue actualizada para coincidir con la lógica de la UI, requiriendo el campo "Destino" solo para "Traslado (salida)", lo que permite que el botón "Añadir a la lista" se habilite correctamente para otros motivos como "Compra".

## 3. Resolución de Errores Críticos y Mejoras de Robustez

-   **Corrección de Crash al "Añadir a la Lista":** Se resolvió un `IllegalStateException` (`A MonotonicFrameClock is not available...`) que causaba un crash al intentar cambiar de página del `PagerState` desde el `ViewModel`. La solución implicó refactorizar la llamada a la animación (`animateScrollToPage`) para que sea gestionada por la Composable (`MovimientoStepperScreen.kt`) a través de un `SharedFlow` emitido por el `ViewModel`.
-   **Robustez al Crear Movimientos Pendientes:** Implementadas comprobaciones de nulidad robustas para `selectedUnidad` y otros campos críticos en `onAddToList` de `MovimientoStepperViewModel.kt` para prevenir `NullPointerException`s y asegurar la validez de los datos antes de guardar.
-   **Información Detallada en Pantalla de Revisión:** La pantalla de revisión ahora muestra nombres legibles (Especie, Categoría, Motivo) en lugar de IDs, obteniendo esta información del objeto `Catalogos` pasado desde el `ViewModel`. También se corrigió la lógica para determinar si un movimiento es de "Alta" o "Baja" basándose en el tipo del motivo.
-   **Agrupación Correcta de Movimientos:** La pantalla de revisión ahora consume correctamente la lista de `MovimientoAgrupado`, asegurando que los movimientos idénticos se muestren como una única entrada sumada.

## 4. Funcionalidad de Eliminación y Sincronización Mejorada

-   **Corrección de Crash al Eliminar:** Se identificó y corrigió una recursión infinita en la lógica de eliminación de grupos de movimientos.
-   **Confirmación al Eliminar:** Se añadió un diálogo de confirmación antes de eliminar un grupo de movimientos, con un mensaje más informativo y menos "exagerado" que explica la consecuencia de la acción.
-   **Feedback Visual de Sincronización:** Se implementó un `LoadingOverlay` de pantalla completa que aparece durante el proceso de sincronización, garantizando una duración mínima de visualización de 1 segundo para una mejor experiencia de usuario.
-   **Mejora de Reporte de Errores de Sincronización:** El manejo de errores en `MovimientoRepositoryImpl.kt` se ajustó para proporcionar mensajes genéricos de fallo de sincronización al usuario, evitando exponer detalles sensibles del servidor.
-   **Eliminación de Elementos Sincronizados:** Tras una sincronización exitosa, los movimientos pendientes se eliminan ahora de la base de datos local, lo que asegura que la lista de revisión solo muestre los elementos que aún no han sido enviados al servidor.

El módulo de movimientos ahora es considerablemente más robusto, usable y cumple con todas las funcionalidades y requisitos de experiencia de usuario planteados.

## Avances de la Sesión Actual

### Nueva Funcionalidad: Panel Deslizable de Logística

-   **Objetivo:** Implementar una nueva interacción para acceder a la información de logística desde la pantalla principal (`MainScreen`) mediante un panel deslizable. Esta interacción busca ser más intuitiva y física, reemplazando una navegación tradicional.

-   **Mecanismo:** Un "tirador" visual aparece desde el lateral derecho al tocar una fecha en el `WeekdaySelector`. Al arrastrar este tirador, un panel blanco se despliega cubriendo la pantalla. Una vez abierto, el contenido de logística se carga con un indicador visual.

-   **Pasos de Implementación y Refinamientos Clave:**
    1.  **Creación de Componentes Base:**
        *   Se creó `LogisticsScreen.kt` (pantalla en blanco inicial para el contenido de logística).
        *   Se creó `LogisticsDraggableHandle.kt` (el componente visual del tirador).
        *   Se creó `SlidingScreen.kt` (una simple superficie blanca que actúa como el panel deslizable, luego reemplazada por el contenido de `LogisticsScreen`).
    2.  **Integración de Navegación (Inicial):**
        *   Se añadió la ruta `LOGISTICS` y el `composable` correspondiente en `AppNavigation.kt`. (Posteriormente se refactorizó para incrustar el contenido directamente sin `NavController`).
    3.  **Habilitar Interacción del Selector de Días:**
        *   Se modificó `WeekdaySelector.kt` para que las fechas fueran clickables, disparando el evento para mostrar el tirador.
        *   Se desvinculó la selección visual de la fecha de la aparición del tirador; la fecha "hoy" permanece estática.
    4.  **Implementación de la Lógica del Panel Deslizable en `MainContent` (`MainScreen.kt`):**
        *   Se gestionó el estado `showLogisticsHandle` para controlar la visibilidad del mecanismo.
        *   Se utilizó `Animatable` para `offsetX` para animar suavemente la posición horizontal del panel y el tirador.
        *   Se implementó la lógica de arrastre (`Modifier.draggable`) para controlar `offsetX`.
        *   Se definió una función `hideLogisticsPanel()` para cerrar el panel mediante animación.
        *   **Estado de Carga:** Se introdujo `isLoadingLogistics` para mostrar un `CircularProgressIndicator` mientras se simula la carga del contenido, mejorando la retroalimentación al usuario.
        *   **Contenido Incrustado:** `LogisticsScreen` se incrustó directamente dentro del panel deslizable, eliminando la necesidad de `NavController` para esta interacción específica y permitiendo una animación más fluida.
    5.  **Refinamientos de Experiencia de Usuario (UX):**
        *   **Control de Apertura:** Se añadió una condición en `onDateClick` para que el tirador solo aparezca si no está ya visible, evitando interrupciones en su estado actual.
        *   **Cerrar al Hacer Clic Fuera (`Dismiss on outside click`):** Se implementó un "scrim" (una capa semitransparente clickable) que aparece sobre el contenido principal cuando el panel está asomando. Un clic en el scrim cierra el panel.
        *   **Ajuste de Posición del Tirador:** Se modificó el `padding(top = 100.dp)` del `LogisticsDraggableHandle` para posicionarlo más abajo y alinearlo visualmente.
        *   **Ajuste de Protrusión del Tirador:** Se ajustó la variable `peekOutDistance` para controlar cuánto sobresale el tirador, asegurando que solo la parte interactiva sea visible al asomarse.
        *   **Sensibilidad del Arrastre:** Se ajustó el umbral de arrastre en `onDragStopped` para que el panel se abra completamente con un gesto más ligero e intuitivo.

-   **Errores Resueltos durante la Implementación:**
    *   Múltiples errores de "Unresolved reference" (`WeekdaySelector`, `Alignment`, `animate`, `width`, `Surface`, `CircularProgressIndicator`, `LogisticsScreen`, `MutableInteractionSource`) debido a la falta o incorrecta inclusión de importaciones tras refactorizaciones y a problemas de sincronización en mi entendimiento del estado del archivo. Estos fueron corregidos uno a uno.
    *   Bug visual: El tirador no era visible/arrastrable debido a un error de posicionamiento (`offset` negativo) que lo situaba fuera del área táctil de su contenedor `draggable`. Se corrigió para que el tirador estuviera correctamente dentro del área arrastrable.
    *   Bug: El tirador no se desplegaba completamente debido a un umbral de arrastre incorrecto (`screenWidthPx * 0.5f`), lo que requería un arrastre excesivo para activarlo. Se ajustó a un umbral más sensible (`(screenWidthPx - peekOutDistance) - (peekOutDistance * 0.5f)`).

-   **Estado Actual:** La funcionalidad del panel deslizable está implementada con la interacción deseada y los ajustes visuales principales. El proyecto debería compilar correctamente (suponiendo que las últimas correcciones se han aplicado y sincronizado con el entorno de compilación).

# Avances de la Sesión Actual (08 de Enero de 2026)

Esta sesión se centró en mejoras significativas de la experiencia de usuario, refactorización de navegación y optimización del rendimiento en varias pantallas clave de la aplicación.

## 1. Refactorización del Panel Deslizable de Logística (`SlidingPanel` y `LogisticsScreen`)

*   **Extracción de Lógica a `SlidingPanel.kt`**: La compleja lógica del panel deslizable se extrajo de `MainScreen.kt` a un componente reutilizable `SlidingPanel.kt`.
*   **Integración en `MainScreen.kt`**: `MainScreen.kt` fue refactorizado para utilizar este nuevo componente, simplificando su código.
*   **API Mejorada del `SlidingPanel`**: Se mejoró la interfaz de `SlidingPanel` con un callback `onFullyOpen`, permitiendo que el contenido del panel reaccione cuando se abre completamente.
*   **Ajustes de Alineación y Comportamiento**:
    *   Se ajustó la alineación vertical del tirador (`LogisticsDraggableHandle`) para que coincidiera con el selector de días de la semana (`WeekdaySelector`).
    *   Se corrigió el comportamiento de arrastre del `SlidingPanel` para que solo fuera arrastrable cuando está en su estado "asomado" (peeking) y se bloqueara (no arrastrable) una vez completamente abierto.
    *   Se amplió la zona táctil del tirador para mejorar la usabilidad.
    *   Se corrigió una brecha visual entre el tirador y el panel.
*   **Implementación del Selector de Fecha (Date Picker) en `LogisticsScreen`**:
    *   El contenido del panel de logística se reemplazó con un diseño detallado de selector de fecha personalizado, siguiendo las especificaciones del usuario.
    *   El `WeekdaySelector` de `MainScreen` y el nuevo calendario en `LogisticsScreen` se sincronizaron para mostrar la fecha real actual.
    *   El `MinimalHeader` en `LogisticsScreen` se ajustó para mostrar solo una flecha de retroceso (sin título).
    *   El scrim (la capa oscura de superposición) del `SlidingPanel` se ajustó para cubrir toda la pantalla, incluida la barra de navegación inferior, solucionando un problema visual.

## 2. Refactorización y Funcionalidad de Navegación

*   **Botones de `MyJournalSection` Funcionales**: Los botones de acción "Ver Stock", "Agregar Stock", "Ver Historial" y "Mis Campos" en la sección de `MyJournalSection` de la pantalla principal ahora navegan correctamente a sus respectivas funcionalidades.
*   **Efecto Ripple Circular**: Se corrigió el efecto "ripple" (la sombra al presionar) en los botones de acción de `MyJournalSection` para que fuera circular, mejorando la coherencia visual.
*   **Rediseño de la Barra de Navegación Inferior (`CozyBottomNavBar`)**:
    *   La barra de navegación inferior se rediseñó para incluir 4 elementos principales: Inicio, Ayuda, Perfil y Notificaciones.
    *   Se añadieron pantallas de marcador de posición para las nuevas rutas de Ayuda y Notificaciones.
    *   La navegación al perfil se integró para dirigir al usuario a la pantalla de Configuración (`Routes.SETTINGS`).

## 3. Optimización de Rendimiento y Transiciones de Carga

*   **Resolución de Lentitud en "Agregar Stock" (Carga de Catálogos)**:
    *   Se identificó un cuello de botella de rendimiento en `CatalogosRepositoryImpl.kt` causado por el uso anidado del operador `combine` y la carga innecesaria de datos pesados (municipios con procesamiento GeoJSON) para el formulario de "Agregar Stock".
    *   Se creó un nuevo `GetMovimientoCatalogosUseCase` (más ligero) y se actualizó la interfaz y la implementación de `CatalogosRepository` para proporcionar un método específico que carga solo los 4 catálogos requeridos por el flujo de movimiento de stock.
    *   `MovimientoStepperViewModel` se actualizó para utilizar este nuevo caso de uso más eficiente, lo que debería resultar en una carga mucho más rápida de la pantalla de "Seleccionar Campo".
    *   Se resolvieron errores de compilación (`None of the following candidates is applicable because of a receiver type mismatch`) relacionados con la sintaxis del operador `combine` y se añadió tipado explícito para ayudar a Kapt en `MovimientoStepperViewModel`.
*   **Transición de Carga en `StockScreen`**:
    *   Se refactorizó `StockViewModel` para mejorar el rendimiento de carga inicial, mostrando los datos de la base de datos local primero y luego actualizando desde la red en segundo plano. Esto asegura que la pantalla de stock aparezca rápidamente, con un spinner perceptible en la navegación inicial si es necesario.
*   **Transición de Carga en `CamposScreen`**: Se añadió un retraso artificial de 400ms y un spinner centrado en `CamposViewModel` y `CamposScreen` para proporcionar una animación de carga perceptible, incluso cuando no hay datos complejos que cargar.
*   **Optimización de Navegación de Retorno desde "Seleccionar Campo"**:
    *   La navegación desde "Seleccionar Campo" (`SeleccionCampoScreen`) a la pantalla principal era lenta debido a una recreación innecesaria de `MainScreen` al usar `navController.navigate` con `popUpTo`.
    *   Se refactorizó esta navegación para convertir `SeleccionCampoScreen` en un estado interno de `MainScreen` (manejado por `Crossfade`), similar a cómo funciona `CamposScreen`.
    *   Esto hace que la navegación de retorno sea un cambio de estado ligero y rápido, resultando en una transición casi instantánea.
*   **Eliminación del Cargador Esqueleto de `MainScreen`**: El cargador esqueleto introducido temporalmente para la pantalla principal ha sido eliminado, ya que las mejoras en la navegación hacen que no sea necesario.

## 4. Estado General del Proyecto

*   Todas las funcionalidades solicitadas y las mejoras de rendimiento/UX se han implementado.
*   El proyecto compila exitosamente, sin errores conocidos.

# Avances de la Sesión Actual - 9 de Enero de 2026

## Problema Inicial: Error de Compilación en Kapt / kotlin-serialization

**Descripción:** El proyecto no compilaba, reportando `incompatible types: NonExistentClass cannot be converted to Annotation` en `ValidationErrorResponse.java` durante la tarea `:data:kaptDebugKotlin`. Esto ocurría porque Kapt intentaba procesar `kotlinx.serialization`, lo cual es incorrecto.

**Acciones Tomadas y Solución:**
1.  **Versión de Kotlin y Plugins:** Se identificó que la versión de Kotlin en `libs.versions.toml` era `2.0.21` (pre-release), lo que generaba conflictos.
    *   **Acción:** Se actualizó `kotlin = "1.9.23"`.
2.  **Plugin Kotlin Compose:** Se encontró que el plugin `org.jetbrains.kotlin.plugin.compose` no era compatible o estaba mal configurado.
    *   **Acción:** Se eliminó la referencia del plugin `kotlin-compose` del `build.gradle.kts` raíz y se reinsertó en `app/build.gradle.kts`. Luego se cambió a `composeOptions { kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get() }` con `composeCompiler = "1.5.11"` en `libs.versions.toml`, siguiendo la configuración moderna.
3.  **Migración de Kapt a KSP:** Para resolver el conflicto entre Kapt y `kotlinx.serialization`, se migró de Kapt a KSP.
    *   **Acción:** Se añadió `ksp = "1.9.23-1.0.20"` y el plugin `kotlin-ksp` en `libs.versions.toml`. Se reemplazaron todas las ocurrencias de `id("kotlin-kapt")` por `alias(libs.plugins.kotlin.ksp)` y las dependencias `kapt(...)` por `ksp(...)` en los archivos `build.gradle.kts` de la raíz, `app` y `data`. Se corrigieron `kaptTest` por `kspAndroidTest`.
4.  **Error de `SerializedName`:** Se identificó un error tipográfico en `ValidationErrorResponse.kt`.
    *   **Acción:** Se reemplazaron todas las instancias de `@SerializedName` por `@SerialName`.

**Resultado:** El proyecto compiló exitosamente en modo `debug`.

---

## Problema Actual: `java.lang.Class cannot be cast to java.lang.reflect.ParameterizedType` en `Release Build`

**Descripción:** Después de compilar exitosamente en `debug`, al probar la versión `release`, el error `java.lang.Class cannot be cast to java.lang.reflect.ParameterizedType` reaparece durante el proceso de login cuando se introducen credenciales incorrectas (texto en los campos de email y contraseña).

**Análisis del Problema (con información adicional del usuario):**
*   El error ocurre **solamente** en el `build` de `release`.
*   El error **no ocurre** cuando los campos están vacíos (en ese caso, el servidor devuelve un 422 con una `ValidationErrorResponse` que se parsea correctamente).
*   El `curl` directo al endpoint de login con credenciales incorrectas (texto en los campos) devuelve un código `401 Unauthorized` con un cuerpo `{"message":"Credenciales inv\u00e1lidas."}`, lo que corresponde a `ErrorResponse`.

**Acciones Tomadas y Resultados (fallidos en solucionar el `ClassCastException` en `release`):**
1.  **Refactorización de `AuthRepositoryImpl.kt` (`handleAuthError`)**:
    *   **Acción:** Se modificó la función `handleAuthError` para que intentara deserializar el cuerpo del error primero como `ValidationErrorResponse` y, si fallaba, como `ErrorResponse`, independientemente del código de estado HTTP. Esto se hizo para evitar suposiciones sobre el formato del error y usar `serializer<T>()` explícitamente para ayudar a `kotlinx.serialization` con los tipos genéricos.
    *   **Resultado:** Compilación exitosa en `debug` y `release`, pero el `ClassCastException` persiste en `release`.
2.  **Análisis de dependencias (`gradlew :app:dependencies`)**:
    *   **Acción:** Se verificó el árbol de dependencias del módulo `app` para la presencia de `Gson` u otros convertidores de JSON que pudieran estar interfiriendo.
    *   **Resultado:** No se encontró `Gson` ni dependencias conflictivas.
3.  **Reglas de Proguard para `kotlinx.serialization`**:
    *   **Acción:** Se añadieron reglas de Proguard a `app/proguard-rules.pro` para preservar las clases anotadas con `@Serializable` y sus componentes internos, incluyendo una regla explícita para `ValidationErrorResponse` (`-keep class com.sinc.mobile.data.network.dto.ValidationErrorResponse { *; }`) para prevenir su ofuscación agresiva por R8.
    *   **Resultado:** Compilación exitosa en `release`, pero el `ClassCastException` persiste en `release`.

**Hipótesis Actual (Post-pruebas):**
*   A pesar de las reglas de Proguard añadidas, R8 sigue siendo el principal sospechoso. El `ClassCastException` en `java.lang.reflect.ParameterizedType` es un síntoma clásico de que R8 está eliminando metadatos cruciales de tipos genéricos durante la minimización/ofuscación, incluso con las reglas de `-keep`.
*   El `ClassCastException` podría estar ocurriendo en el deserializador del `Map<String, List<String>>` dentro de `ValidationErrorResponse`, o incluso en la misma `List<String>`.

**Siguientes Pasos (a considerar):**
*   **Simplificar los DTOs de Error:** Intentar simplificar `ValidationErrorResponse` (o `ErrorResponse`) para ver si el uso de tipos genéricos complejos (`Map<String, List<String>>`) es la raíz del problema con R8. Por ejemplo, cambiar `Map<String, List<String>>` a `Map<String, String>` o incluso a un `String` simple. Si funciona, nos indicaría el punto exacto de la falla.
*   **Añadir reglas `keep` más agresivas:** Probar a añadir reglas `-keep` muy amplias para las clases `List` y `Map` de Java/Kotlin, aunque esto es menos probable que sea necesario.
*   **Actualizar `kotlinx.serialization`:** Investigar si hay versiones más recientes de `kotlinx.serialization` o del plugin de Kotlin que mejoren la compatibilidad con R8.
*   **Aislar el problema:** Crear un proyecto de Android mínimo que solo intente deserializar un `ValidationErrorResponse` o `ErrorResponse` con R8 activado para ver si el error se reproduce, lo que nos permitiría reportar el bug.
---

## Resumen de Avances 10/01/2026

Esta sesión se centró en resolver una serie de errores críticos que impedían el correcto funcionamiento de la aplicación, especialmente en builds de `release`.

### 1. Solución de `ClassCastException` en `release` build

*   **Problema:** La aplicación fallaba con un `java.lang.Class cannot be cast to java.lang.reflect.ParameterizedType` al intentar manejar errores de login en `release`.
*   **Investigación:** Se determinó que era un problema de ofuscación de R8, que eliminaba metadatos de tipos genéricos necesarios para la deserialización con `kotlinx.serialization`.
*   **Solución (Multi-paso):**
    1.  **Reglas de Proguard:** Se investigó y añadió un conjunto de reglas de Proguard más completo y robusto a `app/proguard-rules.pro`. Estas reglas protegen explícitamente las firmas de los métodos de Retrofit y los metadatos de Kotlin, solucionando el síntoma del `ClassCastException`.
    2.  **Refactorización de Deserialización:** Para una solución más robusta, se refactorizó `AuthRepositoryImpl.kt`, reemplazando las llamadas reflexivas `serializer<T>()` por el método estático y seguro para R8 `T.serializer()`. Esto elimina la dependencia de la reflexión que causaba el problema.

### 2. Solución de `MissingFieldException` en la sincronización de Unidades Productivas

*   **Problema:** La aplicación fallaba al iniciar sesión con un `MissingFieldException` porque la respuesta de la API para `unidades-productivas` no coincidía con la estructura del `UnidadProductivaDto` en la app.
*   **Investigación:**
    1.  Se utilizó `Invoke-WebRequest` para consultar el endpoint directamente y obtener la respuesta JSON real del servidor.
    2.  Se analizó la respuesta y se comparó con el DTO y la migración de Laravel proporcionada por el usuario.
*   **Análisis y Causa Raíz:** Se identificaron tres tipos de discrepancias:
    1.  **Error de Tipo:** El campo `superficie` se recibía como `String` pero se esperaba como `Float`.
    2.  **Error de Nombre:** La app esperaba `fuente_agua_id`, `tipo_suelo_id`, etc., pero la API enviaba `agua_humano_fuente_id`, `tipo_suelo_predominante_id`, etc.
    3.  **Error de Estructura:** La app esperaba `condicion_tenencia_id` en el objeto principal, pero el usuario clarificó que pertenece al objeto anidado `pivot`.
*   **Solución:**
    1.  Se reescribió `UnidadProductivaDto.kt` para que sea un reflejo exacto de la respuesta JSON, usando `@SerialName` para mapear los nombres correctos, ajustando el tipo de `superficie` a `String?`, y creando un `PivotDto` anidado para `condicion_tenencia_id`.
    2.  Se flexibilizó el modelo de dominio `UnidadProductiva.kt` para aceptar campos nulables (`nombre`, `superficie`, `municipioId`).
    3.  Se corrigieron las funciones de mapeo `toEntity()` y `toDomain()` en `UnidadProductivaRepositoryImpl.kt` para manejar la nueva estructura del DTO, incluyendo la conversión de tipos (`String?` a `Float?`) y la obtención de datos del objeto `pivot`.
    4.  Se corrigieron los errores de compilación resultantes en la capa de UI (`:app`) para manejar los nuevos tipos nulables, proveyendo valores por defecto en los Composables (`?: "Sin nombre"`).

### Estado Final de la Sesión

*   Todos los errores de compilación en `debug` y `release` fueron resueltos.
*   Se generó exitosamente un APK de `release` para la prueba final por parte del usuario.
*   El código base es ahora más robusto y resiliente a la ofuscación de R8 y a las discrepancias de datos de la API.

# Resumen 11/01/2026

Esta sesión se centró en la corrección de errores de interfaz de usuario y lógica de negocio, así como en la mejora de la experiencia de usuario y la estabilidad general de la aplicación.

### 1. Manejo Genérico de Errores en la Pantalla de Login

-   **Problema:** La pantalla de Login mostraba mensajes de error internos del sistema a los usuarios, lo cual no es adecuado para entornos de producción.
-   **Solución:**
    -   Se modificó `LoginViewModel.kt` para que, en builds de `debug`, los errores detallados sean registrados en Logcat.
    -   En todos los builds (incluido `release`), la UI ahora muestra un mensaje de error genérico y amigable al usuario (e.g., "Ocurrió un error inesperado. Por favor, inténtalo de nuevo más tarde.") para `AuthResult.UnknownError` y `Result.Failure` en la sincronización.
    -   Se añadió el mensaje de error genérico a `strings.xml`.

### 2. Corrección de la Funcionalidad "Olvidé mi Contraseña"

-   **Problema:** La aplicación mostraba incorrectamente un mensaje de éxito ("Se ha enviado un código a tu correo.") al introducir un email inválido en la pantalla de "Olvidé mi Contraseña". Esto ocurría porque el servidor esperaba un tipo de contenido diferente (`application/x-www-form-urlencoded`) del que la app enviaba (`application/json`), resultando en un `422 Unprocessable Content` que no era manejado como error de validación en la capa de la app.
-   **Análisis:**
    -   Se utilizó `curl` para diagnosticar que el endpoint del servidor para `request-reset` y `reset-with-code` esperaba `application/x-www-form-urlencoded` y devolvía `422 Unprocessable Content` para fallos de validación (ej. email inválido) y `200 OK` con un mensaje de confirmación para emails válidos.
-   **Solución:**
    -   Se modificó `AuthApiService.kt` para usar las anotaciones `@FormUrlEncoded` y `@Field` en las funciones `changePassword`, `requestPasswordReset` y `resetPasswordWithCode`, asegurando que los datos se envíen correctamente al backend.
    -   Se actualizó `AuthRepositoryImpl.kt` para invocar estos métodos de servicio con los parámetros primitivos adecuados, eliminando la creación de objetos DTO de solicitud.
    -   Se eliminaron los archivos DTO de solicitud (`ChangePasswordRequest.kt`, `RequestPasswordResetRequest.kt`, `ResetPasswordWithCodeRequest.kt`) que ya no eran necesarios, manteniendo el código limpio.
    -   Se actualizó el mensaje de éxito mostrado al usuario en `ForgotPasswordViewModel.kt` a "Si el correo ingresado existe en el sistema, se ha enviado el código." para ser más preciso.

### 3. Ajustes de UI en el Acceso a Configuraciones y Navegación

-   **Problema 1: Acceso múltiple a Configuraciones y destino incorrecto.**
    -   El icono superior derecho en `MainScreen` navegaba a la pantalla de Configuraciones.
-   **Problema 2: Retraso visual y glitch en la navegación.**
    -   Al navegar a la pantalla de Configuraciones desde la barra de navegación inferior, se percibía un pequeño retraso que permitía clics indeseados en la UI subyacente antes de que la nueva pantalla animara.
-   **Solución:**
    -   **Redirección del Icono Superior Derecho:** Se creó una nueva pantalla `CuencaInfoScreen.kt` (una pantalla en blanco de placeholder).
    -   Se añadió la ruta `CUENCA_INFO` en `AppNavigation.kt`.
    -   Se modificó `MainScreen.kt` para que el icono superior derecho navegue a la nueva pantalla `CuencaInfoScreen` en lugar de a Configuraciones.
    -   **Prevención de Glitches en Navegación:** Para la navegación a Configuraciones desde el botón "Perfil" en la barra inferior, se añadió un `Box` transparente y de tamaño completo en `MainScreen.kt`. Este `Box` se superpone a la `MainContent` mientras la `SettingsScreen` anima, bloqueando interacciones y eliminando el glitch visual.

### 4. Implementación de Auto-Refresco y Refinamiento de UI en "Mis Campos"

-   **Problema:** La lista de "Unidades Productivas" en la pantalla "Mis Campos" no se actualizaba automáticamente después de crear una nueva UP, requiriendo un reinicio de la aplicación. Además, la UI de esta pantalla era básica y no mostraba la lista de campos.
-   **Solución:**
    -   **Implementación de la Lista de UPs:**
        -   Se actualizó `CamposViewModel.kt` para inyectar `GetUnidadesProductivasUseCase` y `SyncUnidadesProductivasUseCase`, y para gestionar el estado de la lista de UPs.
        -   Se refactorizó `CamposScreen.kt` para mostrar una lista de "Unidades Productivas" con una barra de búsqueda, utilizando `SeleccionCampoScreen` como referencia de estilo.
        -   Se reemplazó el `FloatingActionButton` por un `Button` estándar "Registrar Campo" visible solo cuando la lista está vacía.
    -   **Implementación de Auto-Refresco:**
        -   Se modificó `AppNavigation.kt` y `CreateUnidadProductivaScreen.kt` para utilizar el `SavedStateHandle` de `NavController`. Tras la creación exitosa de una UP, se establece un indicador (`"should_refresh_ups"`) en el `SavedStateHandle`.
        -   Se pasó el `navController` a `CamposScreen` desde `MainScreen.kt`.
        -   `CamposScreen.kt` ahora observa este indicador mediante un `LaunchedEffect` y, si está presente, dispara una sincronización de datos en `CamposViewModel` para actualizar la lista de UPs.
    -   **Refinamiento de UI:**
        -   Se mejoró el componente `EmptyState.kt` para aceptar parámetros `iconSize`, `titleStyle` y `messageStyle`, haciéndolo más flexible.
        -   Se corrigió el layout en `CamposScreen.kt` para el `EmptyState` y el botón "Registrar Campo", asegurando que el botón aparezca correctamente debajo del texto y el contenido esté centrado, solucionando el problema reportado por el usuario de que el botón no aparecía.

---
**Estado Actual:** La aplicación tiene mejoras significativas en el manejo de errores, la gestión de la autenticación, la navegación y la visualización de datos, con todos los errores de compilación resueltos.

# Avances 12/01/2026

Esta sesión se centró en una serie de mejoras de la interfaz de usuario (UI), la experiencia de usuario (UX) y la corrección de errores de compilación, principalmente en la pantalla principal y en la nueva pantalla de información.

### 1. Mejoras en la Pantalla Principal (`MainScreen` y `Header`)

*   **Logo del Encabezado:** Se agrandó ligeramente el logo de "Ovinos" en el `Header` y se aplicó un recorte circular (`clip(CircleShape)`) para que el efecto de pulsación (ripple) sea redondo, eliminando la percepción de una "sombra cuadrada".
*   **Ajustes de Espaciado:**
    *   Se redujo a la mitad el espacio vertical entre el `Header` y el divisor horizontal, ajustando el `padding` inferior a `8.dp`.
    *   Se ajustó el `padding` vertical del selector de días de la semana (`WeekdaySelector`) a `8.dp` para un layout más compacto.
*   **Corrección de Navegación a Configuraciones:** Se solucionó un error que causaba un parpadeo y un retraso al navegar a la pantalla de "Configuraciones" desde la barra de navegación inferior. La lógica se simplificó para que la navegación sea directa, eliminando el cambio de estado intermedio que causaba el problema.

### 2. Construcción y Refinamiento de la Pantalla "Más Info" (`CuencaInfoScreen`)

*   **Generación de Contenido:** Se implementó la funcionalidad para leer el contenido de una URL externa (`https://sicsurmisiones.online/cuenca-misiones`), generar un resumen y presentarlo en la pantalla.
*   **Nuevo Resumen y Enfoque:** El texto descriptivo se reescribió para ser más conciso y enfocado en los objetivos de la aplicación móvil como herramienta de la "Mesa de Gestión de la Cuenca".
*   **Rediseño de Layout:** La pantalla se rediseñó completamente para seguir el estilo de la app, utilizando `Card`s de ancho completo con un fondo gris de separación.
*   **Carrusel de Logos "Impulsado por":**
    *   Se añadió una nueva sección con el título "Impulsado por".
    *   Se creó un carrusel de logos (`LazyRow`) dentro de una `Card` con un borde fino de color primario ("bordó").
    *   Se incluyó el logo de "Ovinos" en primera posición y el del "INTA" en segunda, seguido por los demás logos institucionales.
*   **Pie de Página "Desarrollado por":** Se añadió una `Card` al final de la pantalla con la información de contacto del desarrollador, y se corrigieron múltiples problemas de alineación para asegurar que el bloque de texto se vea centrado y ordenado.
*   **Reorganización de Contenido:**
    *   El botón "Ver página completa" se movió a la tarjeta de información principal.
    *   El título de la pantalla se cambió a "Más Info".

### 3. Resolución de Errores de Compilación

*   Se solucionaron numerosos y persistentes errores de compilación en `CuencaInfoScreen.kt` relacionados con:
    *   **Referencias no resueltas (`Unresolved reference`):** Se corrigieron problemas con `IntrinsicSize` (reemplazando su uso) y con las referencias a recursos `R.drawable` (usando el nombre completamente calificado).
    *   **Importaciones conflictivas y duplicadas:** Se realizó una limpieza y reorganización completa de los `import` para resolver ambigüedades y errores de sintaxis.
    *   El proyecto ahora compila exitosamente en modo `debug`.

---

### Avances Adicionales

#### 1. Snackbar de Validación para Selección de Especie
*   **Funcionalidad:** Implementado un mensaje de snackbar ("Por favor, seleccione una especie primero.") que aparece cuando el usuario intenta seleccionar "Categoría" o "Raza" sin haber elegido previamente una "Especie".
*   **Comportamiento:** El snackbar ahora tiene una duración corta (`SnackbarDuration.Short`) y se ha añadido lógica para asegurar que solo se muestre un mensaje a la vez, evitando la acumulación de notificaciones.
*   **Archivos Afectados:** `MovimientoStepperScreen.kt`, `MovimientoFormStepScreen.kt`.

#### 2. Mejora en la UI/UX del Proceso de Sincronización
*   **Refactorización del Flujo:** Se modificó la lógica de sincronización para evitar que la pantalla de revisión se vacíe prematuramente mientras se muestra la animación de éxito. Ahora, la limpieza de los datos locales ocurre *después* de que la animación de éxito se ha completado, manteniendo una experiencia visual fluida.
*   **Overlay de Éxito (`SyncResultOverlay.kt`):**
    *   Se creó e integró un nuevo componente `SyncResultOverlay.kt` que muestra una animación de éxito (un ícono de check con el mensaje "Stock actualizado con éxito!") después de una sincronización exitosa.
    *   **Comportamiento de Cierre:** El overlay ahora se puede cerrar haciendo clic en cualquier parte de la pantalla, en lugar de cerrarse automáticamente después de un retardo. Esto proporciona más control al usuario.
    *   **Estilo Visual:** Se eliminó la sombra de fondo (scrim) del `SyncResultOverlay` para un aspecto más limpio y menos intrusivo.
*   **Mantenimiento en Pantalla:** Después de una sincronización exitosa, la aplicación permanece en la pantalla de revisión (en un estado vacío), en lugar de navegar automáticamente a la pantalla principal.
*   **Archivos Afectados:** `MovimientoSyncManager.kt`, `MovimientoStepperViewModel.kt`, `MovimientoRepository.kt`, `MovimientoRepositoryImpl.kt`, `SyncMovimientosPendientesUseCase.kt`, `MovimientoStepperScreen.kt`, `SyncResultOve



# Avances de la Sesión Actual (14 de Enero de 2026)

## 1. Configuración Inicial de Firebase Cloud Messaging (FCM)

-   **Objetivo**: Habilitar la recepción de notificaciones push desde el backend.
-   **Detalles**:
    -   **Configuración de Gradle**: Se añadió el plugin `com.google.gms.google-services` a los archivos `build.gradle.kts` de nivel de proyecto y de aplicación. Se incluyó la BOM de Firebase (`firebase-bom:34.7.0`) y la dependencia `firebase-messaging`.
    -   **Obtención y Registro del Token**: Se implementó lógica en `MainActivity.kt` para obtener el token FCM del dispositivo y loguearlo en Logcat para verificación inicial.
    -   **Configuración del Servicio de Mensajería**:
        -   Se creó `MyFirebaseMessagingService.kt` (`app/src/main/java/com/sinc/mobile/app/firebase/`) para extender `FirebaseMessagingService`, con métodos para manejar la recepción de mensajes (`onMessageReceived`) y la actualización de tokens (`onNewToken`).
        -   Se añadió un ID de canal de notificación por defecto (`fcm_default_channel`) en `strings.xml`.
        -   Se incluyó la lógica para la creación de este canal de notificación en `SincMobileApp.kt` para compatibilidad con Android 8.0+.
        -   Se declaró `MyFirebaseMessagingService` en `AndroidManifest.xml` con su `intent-filter` correspondiente.

## 2. Depuración de la Conexión y Envío de Token FCM al Backend

-   **Problema Inicial**: La aplicación no lograba conectar con el backend local (Laravel).
-   **Resolución**:
    -   Se verificó la configuración del servidor Laravel, recomendando `php artisan serve --host=0.0.0.0`.
    -   Se clarificó el uso de la IP `10.0.2.2` para el emulador y `127.0.0.1` para peticiones desde el host.
    -   Se confirmó la accesibilidad del backend y se corrigió el nombre del campo del token en el cuerpo de la petición (`fcm_token` a `token`) que esperaba el backend.
    -   Se resolvió un problema de `401 No autorizado` obteniendo un nuevo token de autenticación para el usuario `productora@test.com`.
    -   Se envió con éxito el token FCM al backend local, asociándolo al `userId` correcto.
    -   **Configuración de URL Base**: Se alternó la `BASE_URL` en `NetworkModule.kt` entre la dirección local (`http://10.0.2.2:8000/`) para depuración y la de producción (`https://sicsurmisiones.online/`).

## 3. Depuración de la Recepción de Notificaciones en Segundo Plano

-   **Problema**: Las notificaciones aparecían en Logcat con la app en primer plano, pero no en la barra de estado con la app en segundo plano.
-   **Resolución**:
    -   **Requisitos del Canal de Notificación**: Se explicó la obligatoriedad de un canal de notificación para Android 8.0+ para mensajes en segundo plano.
    -   **Configuración del Backend**: Se modificó el comando de Laravel (`php artisan app:send-fcm-test`) para incluir `android_channel_id: 'fcm_default_channel'` en el payload del mensaje, utilizando `AndroidConfig` de `kreait/firebase-php`.
    -   **Icono de Notificación por Defecto**:
        -   Se estableció `default_notification_icon` en `AndroidManifest.xml` inicialmente a `@drawable/ic_launcher_foreground` y `default_notification_color` a `@color/bordeaux`.
        -   Se solucionó un error de compilación (`AAPT: error: resource mipmap/ic_launcher_foreground not found`) al usar la ruta correcta `@drawable/ic_launcher_foreground` en `AndroidManifest.xml`.
        -   Se cambió el icono en `MyFirebaseMessagingService.kt` a `R.mipmap.ic_launcher` temporalmente para depuración.
    -   **Problema de Integración de Comando Laravel**: Se corrigió un error de `PSR-4 autoloading standard` en el comando de Laravel (`TestFcmNotificationCommand`) para asegurar que el comando fuera reconocido y ejecutable.
    -   **Causa Raíz de No Visualización**: Finalmente se identificó que la principal causa de que las notificaciones no aparecieran en segundo plano era que estaban **deshabilitadas para la aplicación en la configuración del dispositivo** del usuario. Una vez habilitadas, las notificaciones empezaron a llegar correctamente en segundo plano.

## 4. Problema Pendiente: Icono de Notificación Incorrecto

-   **Problema Actual**: A pesar de las correcciones y la habilitación de notificaciones, el icono que aparece en la barra de estado es el predeterminado de Android, no `logoovinos`, incluso con `default_notification_icon` configurado en el `AndroidManifest.xml`.
-   **Análisis**: Esto se debe a las estrictas directrices de Android para los iconos pequeños de notificación (deben ser monocromáticos, blancos y transparentes). Si `logoovinos.png` no cumple con esto, el sistema lo sustituye por un icono genérico de Android (a menudo un cuadrado blanco o el icono de la aplicación por defecto).
-   **Acción Pendiente**: Se intentará configurar el `default_notification_icon` en `AndroidManifest.xml` para que apunte directamente a `R.drawable.logoovinos`. Se ha explicado que si `logoovinos.png` es un icono a color, su renderizado como icono pequeño de notificación será problemático y requerirá la creación de una versión monocromática de `logoovinos.png` diseñada específicamente para este propósito.

# Avances de la Sesión Actual 15/01/26

Esta sesión se centró en añadir una validación crítica de negocio, solucionar bugs de datos obsoletos y corregir el comportamiento de los formularios dinámicos.

### 1. Implementación de Validación de Stock para Movimientos de Baja

-   **Funcionalidad**: Se implementó una validación para prevenir que los usuarios registren movimientos de "baja" (ej. muerte, venta) por una cantidad mayor al stock disponible.
-   **Lógica de Negocio**: La validación se ejecuta en `MovimientoStepperViewModel` y es robusta, ya que considera dos factores para calcular el "stock disponible real":
    1.  El último stock total conocido para esa categoría de animal.
    2.  La suma de otros movimientos de baja para el mismo animal que ya están en la lista de pendientes de sincronización.
-   **Experiencia de Usuario (UX)**:
    *   Si la validación falla, se impide que el movimiento se añada a la lista.
    *   Se muestra un `Snackbar` con un mensaje de error claro para el usuario (ej. "Stock insuficiente. Disponible: X (Actual: Y, Pendientes: Z)").
-   **Implementación Técnica**:
    *   Se inyectó `StockRepository` en `MovimientoStepperViewModel` para tener acceso a los datos de stock.
    *   Se modificó la función `onAddToList` para incluir la lógica de cálculo y validación.
    *   Se refactorizó el mecanismo de notificación de error para usar una propiedad en el `State` de la UI y un `LaunchedEffect` (siguiendo el patrón ya existente en la app para el manejo de errores de sincronización), asegurando que el `Snackbar` se muestre de forma fiable.

### 2. Sincronización Automática de Stock y Unidades Productivas

Se solucionaron dos bugs relacionados con datos obsoletos que ocurrían después de realizar acciones importantes en la app. El principio de "Fuente Única de Verdad" (la base de datos local) se reforzó asegurando que se actualiza después de cada mutación en el servidor.

-   **Refresco de Stock tras Sincronizar Movimientos**:
    *   **Problema**: Después de sincronizar movimientos (altas o bajas), la pantalla de validación de stock seguía usando los valores antiguos.
    *   **Solución**: Se inyectó `SyncStockUseCase` en `MovimientoSyncManager` y ahora se invoca automáticamente después de que una sincronización de movimientos es exitosa. Esto asegura que la app obtiene inmediatamente los nuevos totales de stock recalculados por el backend.

-   **Refresco de Unidades Productivas (Campos) tras Creación**:
    *   **Problema**: Después de crear un nuevo "Campo" (Unidad Productiva), éste no aparecía en la lista de selección de campos en la pantalla de "Cargar Stock" sin reiniciar la app.
    *   **Solución**: Se aplicó el mismo patrón. Se inyectó `SyncUnidadesProductivasUseCase` en `CreateUnidadProductivaViewModel` y se invoca automáticamente después de que un nuevo campo se guarda con éxito en el servidor.

### 3. Corrección del Formulario Dinámico de Creación de Campo

-   **Problema**: El campo para el identificador (ej. RNSPA) en el formulario de creación de campos mostraba un texto genérico "Identificador" en lugar de usar la información dinámica (`tipo` y `label`) proporcionada por el backend.
-   **Investigación y Causa Raíz**:
    *   Tras añadir logs, se descubrió que el proceso de sincronización de las configuraciones de identificadores estaba fallando silenciosamente.
    *   La causa era una `kotlinx.serialization.MissingFieldException`, ya que el DTO (`IdentifierConfigDto`) esperaba un campo `hint` que la respuesta de la API no incluía.
-   **Solución**:
    1.  **Robustez del DTO**: Se modificó `IdentifierConfigDto` para que el campo `hint` sea nulable con un valor por defecto (`val hint: String? = null`). Esto solucionó el error de deserialización y permitió que los datos se guardaran correctamente en la base de datos local.
    2.  **Ajuste de UI**: Se modificó el Composable `Step2FormularioBasico` para que, por petición del usuario, utilice el campo `type` ("RNSPA") como etiqueta del campo, manteniendo la interfaz limpia y consistente.

# Avances de la Sesión Actual - 16 de Enero de 2026

Esta sesión se centró en la implementación completa y el refinamiento del endpoint de actualización de Unidades Productivas (UP), incluyendo la integración del backend, la actualización de modelos de datos y la creación de una interfaz de usuario moderna y funcional.

## 1. Integración del Endpoint PUT /api/movil/unidades-productivas/{id}

-   **Verificación Backend**: Se confirmó el correcto funcionamiento del endpoint de actualización (`PUT /api/movil/unidades-productivas/{id}`) mediante `curl`, verificando la actualización de campos directos (`superficie`, `observaciones`) y de la tabla pivote (`condicion_tenencia_id`). Se identificó que la respuesta del `PUT` no incluye el objeto `pivot`, lo que se marcó como un `TODO` para el backend (para que devuelva el objeto completo, incluyendo el `pivot`).
-   **Capa de Datos (`:data`)**:
    -   Se creó `UpdateUnidadProductivaRequest.kt` con todos los campos actualizables (superficie, condición de tenencia, fuentes de agua, distancias, tipos de suelo/pasto, forrajeras, habita, observaciones).
    -   Se añadió el método `updateUnidadProductiva` a `UnidadProductivaApiService`.
    -   Se implementó la lógica de `updateUnidadProductiva` en `UnidadProductivaRepositoryImpl`, incluyendo el mapeo de campos booleanos a enteros (0/1) para la API.
-   **Capa de Dominio (`:domain`)**:
    -   Se amplió el modelo `UnidadProductiva.kt` para incluir todos los campos relevantes (habita, aguaHumanoFuenteId, aguaHumanoEnCasa, aguaHumanoDistancia, aguaAnimalFuenteId, aguaAnimalDistancia, tipoSueloId, tipoPastoId, forrajerasPredominante).
    -   Se creó el modelo `UpdateUnidadProductivaData.kt` para encapsular los datos de actualización del dominio.
    -   Se actualizó la interfaz `UnidadProductivaRepository` con el nuevo método `updateUnidadProductiva`.
    -   Se creó `UpdateUnidadProductivaUseCase.kt` para exponer la funcionalidad a la capa de presentación.

## 2. Actualizaciones en la Base de Datos Local (Room)

-   Se añadió el campo `observaciones` y otros campos extendidos a `UnidadProductivaEntity.kt`.
-   Se incrementó la versión de la base de datos a `3` en `SincMobileDatabase.kt` para reflejar los cambios en el esquema.
-   Se añadió `fallbackToDestructiveMigration()` en `DatabaseModule.kt` para manejar los cambios de esquema durante el desarrollo.
-   Se actualizaron los mappers `toEntity()` y `toDomain()` en `UnidadProductivaRepositoryImpl.kt` para manejar los nuevos campos y la conversión de tipos.

## 3. Implementación y Rediseño de la UI de Edición de Campos

-   **Navegación**:
    -   Se refactorizó `CamposScreen.kt` para restaurar la estética original de la lista (`CampoListItem`) y hacer que cada ítem sea clickable, navegando a la pantalla de edición.
    -   Se configuró la navegación a `EditUnidadProductivaScreen` (pasando el `unidadId`) al hacer clic en un ítem de la lista.
    -   Se añadió la ruta `EDIT_UNIDAD_PRODUCTIVA` en `AppNavigation.kt`.
-   **`EditUnidadProductivaScreen.kt`**:
    -   **Rediseño por Tarjetas**: La pantalla fue completamente reestructurada utilizando tarjetas (`InfoCard`) para organizar la información en secciones lógicas: "Información Básica", "Agua", "Datos del Terreno" y "Observaciones".
    -   **Campos Actualizados**: Todos los campos extendidos (`habita`, fuentes/distancias de agua, tipos de suelo/pasto, forrajeras) se integraron en la UI, con campos de texto para valores numéricos/texto y un nuevo componente `ToggleRow` para campos booleanos.
    -   **Selectores con `ModalBottomSheet`**: Se implementaron selectores para opciones de catálogo (`Condición de Tenencia`, `Fuentes de Agua`, `Tipos de Suelo`, `Tipos de Pasto`) utilizando un `ModalBottomSheet` para una experiencia de usuario consistente. Esto incluye un `enum EditSheetType` y un componente genérico `SelectionSheetContent`.
    -   **Mejora Visual del Selector**: Se ajustó el estilo de las opciones seleccionadas en el `ModalBottomSheet` a un fondo gris claro y texto del color primario.
    -   **Header**: Se corrigió el padding del `MinimalHeader` para respetar la barra de estado.
    -   Se creó `EditUnidadProductivaViewModel.kt` para gestionar el estado de la UI, cargar datos, poblar catálogos (llamando a `syncCatalogosUseCase()`) y manejar la lógica de guardado.

## 4. Errores Resueltos

-   Se corrigieron varios errores de compilación relacionados con la sintaxis de Kotlin, la colocación de imports y la estructura del código en `AppNavigation.kt` y `EditUnidadProductivaScreen.kt`.
-   Se resolvió el problema de que los catálogos no se cargaban en los selectores del `ModalBottomSheet` asegurando la sincronización de catálogos en el `EditUnidadProductivaViewModel`.
-   Se aplicó `RoundedCornerShape` al `clickable` de `EditableRow` para suavizar el efecto de presión.

### Avances de la Sesión Actual (17 de enero de 2026)

Esta sesión se centró en la implementación de la interfaz de usuario para la funcionalidad de **Declaraciones de Venta** y su integración completa en la arquitectura de la aplicación, así como una refactorización significativa de un campo clave.

#### 1. Implementación de la Interfaz de Usuario de Declaraciones de Venta (`VentasScreen`)
*   **ViewModel (`VentasViewModel.kt`)**: Se creó el ViewModel encargado de gestionar el estado del formulario y la lista de declaraciones. Incluye la lógica para cargar unidades productivas y catálogos, manejar la selección de opciones, validar el stock localmente antes del envío y procesar el registro de ventas.
    *   Se optimizó la carga inicial de datos utilizando `Flow.combine` para evitar bloqueos y asegurar que la interfaz se cargue rápidamente, incluso con datos inicialmente vacíos.
    *   Se implementó la sincronización proactiva de UPs, Catálogos y Stock al iniciar el ViewModel para garantizar que la validación y los selectores usen datos frescos.
*   **Pantalla (`VentasScreen.kt`)**: Se diseñó la pantalla principal de Ventas con una estructura de pestañas:
    *   **"Nueva Venta" (Formulario)**:
        *   Se implementó un formulario de registro de ventas, alineado con el estilo de los formularios de "Registrar Movimiento" y "Actualizar UP".
        *   Los selectores de "Campo", "Categoría" y "Raza" utilizan `ModalBottomSheet` para una selección de opciones mejorada.
        *   El selector de "Especie" utiliza **Chips horizontales** para una selección rápida.
        *   El campo de "Cantidad" utiliza un **Stepper (botón +/-)** para facilitar la entrada de datos.
        *   Se añadió un botón "Declarar Venta" para enviar el formulario.
    *   **"Pendientes" (Lista)**:
        *   Se implementó una lista (`LazyColumn`) para mostrar las declaraciones de venta pendientes.
        *   Se integró la funcionalidad **"Pull-to-Refresh" (Deslizar para actualizar)** en la lista para permitir la sincronización manual.
        *   Las declaraciones se muestran en tarjetas (`DeclaracionCard`) con un diseño claro y conciso.
*   **Integración de Navegación**:
    *   Se añadió una nueva ruta `Routes.VENTAS` en `AppNavigation.kt`.
    *   Se incluyó un botón "Declarar Venta" en `StockScreen.kt` que navega a `VentasScreen`.
    *   Se actualizó `MainScreen.kt` para manejar la navegación a la nueva pantalla de Ventas.

#### 2. Refactorización del Campo "Observaciones" a "Peso Aproximado en Kg"
*   Se realizó una refactorización completa para reemplazar el campo `observaciones: String?` por `pesoAproximadoKg: Float?` en todas las capas de la aplicación:
    *   **Capa de Datos:** `CreateDeclaracionVentaRequest.kt`, `DeclaracionVentaDto.kt`, `DeclaracionVentaEntity.kt`, `DeclaracionVentaMapper.kt`.
    *   **Capa de Dominio:** `DeclaracionVenta.kt` (modelo), `VentasRepository.kt`, `CreateDeclaracionVentaUseCase.kt`.
    *   **Capa de Presentación:** `VentasViewModel.kt` (estado y lógica), `VentasScreen.kt` (campo de entrada y visualización en tarjetas).
*   El campo de entrada en la UI ahora acepta números flotantes y la tarjeta muestra el peso en Kg.

---

### Avances Adicionales de la Sesión (17 de enero de 2026 - Continuación)

Esta parte de la sesión se centró en mejorar la usabilidad del formulario y la visualización de datos, así como en añadir una nueva funcionalidad para el historial de ventas.

#### 3. Mejoras en la Interfaz de Usuario del Formulario de Declaración de Ventas (`VentasScreen`)
*   **Campo "Seleccionar un Campo":** Se modificó el título del campo `ClickableDropdownField` de "Campo (UP)" a "Seleccionar un Campo" para mayor claridad.
*   **Reordenamiento de Campos:** El campo "Peso Aproximado (Kg)" fue reubicado antes del selector de "Cantidad" en el formulario de ventas, siguiendo la secuencia lógica de entrada de datos.
*   **Contador de Pendientes en Pestaña:** Se implementó un contador numérico en el título de la pestaña "Pendientes" (`TabRow`) que muestra la cantidad de declaraciones de venta pendientes, si es que existen, proporcionando feedback visual instantáneo al usuario.

#### 4. Filtrado y Sincronización de Declaraciones Pendientes
*   **Filtro por Estado:** Se ajustó la lógica en `VentasViewModel.kt` para que la lista de declaraciones en la pestaña "Pendientes" solo muestre aquellos ítems con `estado == "pendiente"`, garantizando la relevancia de la información mostrada.
*   **Sincronización Inicial Proactiva:** Se corrigió la función `syncData()` en `VentasViewModel.kt` para incluir la llamada a `syncDeclaracionesVentaUseCase()`, asegurando que las declaraciones de venta se sincronicen con el backend al inicio de la pantalla y no solo mediante "Pull-to-Refresh".

#### 5. Implementación del Historial de Ventas (`HistorialVentasScreen`)
*   **Nueva Funcionalidad de Historial:** Se creó la pantalla `HistorialVentasScreen` y su correspondiente `HistorialVentasViewModel` para visualizar un registro completo de todas las declaraciones de venta.
*   **Filtro por Mes y Año:** Se integró un selector de mes y año en `HistorialVentasScreen` (`MonthSelector`) que permite al usuario filtrar las declaraciones mostradas por el período de tiempo deseado.
*   **Detalle de Declaración:** Al hacer clic en un ítem de la lista de historial, se muestra un `ModalBottomSheet` (`DetalleVentaSheet`) con información detallada de la declaración.
*   **Detalles Enriquecidos del Animal:** En el modal de detalle, ahora se muestran los nombres completos de la Especie, Raza y Categoría (Ej: "Ovino", "Merino", "Cordero") en lugar de sus IDs, buscando la información en los catálogos cargados.
*   **Integración de Navegación:**
    *   Se añadió la ruta `Routes.VENTAS_HISTORIAL` en `AppNavigation.kt`.
    *   Se incluyó un `IconButton` (icono de menú) en la `MinimalHeader` de `VentasScreen.kt` que permite navegar a esta nueva pantalla de historial.

#### 6. Corrección de Errores de Compilación
*   Se resolvieron varios errores de compilación (`Unresolved reference` y `Smart cast`) en `HistorialVentasScreen.kt` y `AppNavigation.kt` relacionados con imports faltantes (`HorizontalDivider`, `HistorialVentasScreen`) y problemas de inferencia de tipo en Kotlin. El proyecto compila ahora exitosamente en modo `debug`.

# Avances de la Sesión Actual (18 de Enero de 2026)

Esta sesión se centró en la corrección de errores críticos en el formulario de movimientos, la mejora de la visualización de datos en la pantalla de stock y una refactorización completa del historial de movimientos, incluyendo nuevas funcionalidades de resumen. Además, se abordó la gestión de la base de datos para futuras actualizaciones.

### 1. Corrección: Bug en el Formulario de Movimientos para Traslados

-   **Problema Detectado:** El formulario de registro de movimientos fallaba al intentar registrar un "Traslado (entrada)". La lógica existente solo solicitaba el campo "Destino" para "Traslado (salida)", lo que generaba un error de validación o un fallo en el backend al enviar datos incompletos para "Traslado (entrada)".
-   **Solución Implementada:**
    *   **`MovimientoFormManager.kt`:** Se actualizó la lógica de validación para requerir el campo `destino` tanto para "Traslado (salida)" como para "Traslado (entrada)", utilizando una comparación insensible a mayúsculas/minúsculas.
    *   **`MovimientoFormStepScreen.kt`:** Se modificó el `LaunchedEffect` que controla la visibilidad del `ModalBottomSheet` de "Destino" para que aparezca en ambos casos ("Traslado (salida)" y "Traslado (entrada)"), permitiendo al usuario ingresar la información necesaria.
    *   **`MovimientoStepperViewModel.kt`:** Se añadió el campo `destinoTraslado` a la `data class MovimientoAgrupado` para asegurar que esta información esté disponible en las capas superiores.
    *   **`MovimientoSyncManager.kt`:** Se incluyó `destinoTraslado` en la clave de agrupación de movimientos. Esto garantiza que movimientos con el mismo tipo pero destinos diferentes no se fusionen erróneamente en la pantalla de revisión.
    *   **`MovimientoReviewStepScreen.kt`:** Se actualizó la interfaz de las filas de revisión para mostrar el campo "Destino" si está presente, proporcionando una vista más completa al usuario.

### 2. Mejora: Filtrado de Stock en Cero en la Pantalla "Mi Stock"

-   **Problema Detectado:** En la pantalla "Mi Stock", al usar el filtro "Todos", la tabla mostraba categorías de animales y razas incluso si su cantidad era 0, generando ruido visual.
-   **Solución Implementada:**
    *   **`StockViewModel.kt`:** Se modificó la función `processStock`. Ahora, al generar la lista de `desgloses` para la vista de tabla, se utiliza `mapNotNull` para excluir cualquier `DesgloseItem.Full` cuya `quantity` sea 0. Esto asegura que solo se muestren los ítems con stock positivo.

### 3. Refactorización y Nueva Funcionalidad: Pantalla "Historial de Movimientos"

-   **Problema Detectado:** La pantalla de "Historial de Movimientos" carecía de un filtro por mes y año, y su diseño actual con tarjetas era visualmente denso, ocupando mucho espacio.
-   **Solución Implementada:**
    *   **`HistorialMovimientosViewModel.kt`:**
        *   Se añadió `selectedDate` (LocalDate) al estado para gestionar el mes y año actuales.
        *   Se implementó la lógica para almacenar la lista completa de movimientos (`allMovimientos`) y una lista filtrada (`filteredMovimientos`) basada en el mes y año seleccionados.
        *   Se crearon funciones `previousMonth()` y `nextMonth()` para cambiar el periodo de visualización.
    *   **`HistorialMovimientosScreen.kt`:**
        *   Se integró un `MonthSelector` (similar al de Historial de Ventas) en la parte superior para la navegación mensual.
        *   Se rediseñó la lista de movimientos para usar un formato de fila compacta (`CompactMovimientoRow`) a lo largo de todo el ancho de la pantalla, reemplazando las tarjetas individuales.
        *   Se añadieron `HorizontalDivider`s entre las filas para una mejor separación visual.
        *   Se incorporó un icono (DateRange) en la `MinimalHeader` para acceder a la nueva pantalla de resumen.
    *   **Nueva Funcionalidad: Pantalla de Resumen Mensual:**
        *   Se creó `ResumenMovimientosViewModel.kt` para calcular estadísticas del mes seleccionado (total de altas, total de bajas, balance neto, y desglose por especie).
        *   Se creó `ResumenMovimientosScreen.kt` con un diseño limpio que muestra estas estadísticas en tarjetas claras y una lista detallada por especie.
        *   **`AppNavigation.kt`:** Se añadió la nueva ruta `Routes.RESUMEN_MOVIMIENTOS`, que acepta `month` y `year` como argumentos de navegación.
        *   **Integración:** El botón en el encabezado de `HistorialMovimientosScreen` ahora navega a esta nueva pantalla de resumen, pasando el mes y el año actuales.

### 4. Mantenimiento: Control de Versiones de la Base de Datos

-   **Problema Detectado:** Posibilidad de errores de incompatibilidad de esquema de la base de datos al actualizar la aplicación, si no se gestionan las versiones.
-   **Solución Implementada:**
    *   **`SincMobileDatabase.kt`:** Se incrementó la `version` de la base de datos de `4` a `5`. Dado que ya se utiliza `fallbackToDestructiveMigration()` en el `DatabaseModule`, este cambio asegura que cualquier usuario que actualice la app tendrá su base de datos local borrada y recreada con el esquema más reciente, evitando crasheos por incompatibilidad de datos.

---
# Avances de la Sesión Actual - 20 de Enero de 2026

Esta sesión se centró en la verificación y validación de la estrategia de Sincronización Incremental (Delta Sync) y la implementación de una Sincronización Inteligente ("Smart Sync") para los catálogos.

## 1. Validación de Sincronización Delta (Módulo Movimientos)

*   **Objetivo:** Confirmar que la aplicación solo descarga los movimientos nuevos o modificados desde la última conexión, en lugar de todo el historial.
*   **Investigación y Hallazgos:**
    *   Se analizó `MovimientoHistorialRepositoryImpl` y se confirmó que gestiona un timestamp de `last_sync` almacenado en `SharedPreferences`.
    *   Se verificó que las peticiones a la API incluyen correctamente el parámetro `updated_after` con este timestamp.
    *   Se contrastó con el test de backend (`DeltaSyncTest.php`) que confirma que el servidor filtra los registros basándose en este parámetro.
    *   **Conclusión:** La lógica para solicitar solo datos faltantes está correctamente implementada. La ausencia de lógica de borrado ("soft deletes") se validó como correcta según las reglas de negocio (los movimientos no se borran, se compensan).

## 2. Validación de Sincronización de Stock

*   **Objetivo:** Determinar si el módulo de stock utilizaba o necesitaba delta sync.
*   **Investigación:**
    *   Se analizó `StockRepositoryImpl` y se confirmó que realiza un "Full Sync" (reemplazo total).
    *   Se inspeccionó la respuesta real del endpoint `/api/movil/stock` (usando el token de producción).
    *   **Conclusión:** El servidor devuelve un snapshot calculado con el estado actual y un desglose detallado. Por lo tanto, la estrategia de descarga completa es la adecuada para este módulo, ya que los totales cambian constantemente y no son una lista incremental de eventos.

## 3. Fortalecimiento del Testing (Capa de Datos)

*   **Creación de Test Unitario:** Ante la falta de tests específicos para el repositorio de movimientos, se creó `MovimientoHistorialRepositoryImplTest.kt` en el módulo `:data`.
*   **Cobertura del Test:**
    *   Verifica que `syncMovimientos` envíe el parámetro `updated_after` correcto a la API.
    *   Confirma que los nuevos registros recibidos se insertan en la base de datos local.
    *   Asegura que la base de datos se limpie correctamente durante una sincronización inicial (timestamp nulo).
*   **Configuración de Entorno:** Se añadieron las dependencias necesarias (`junit`, `mockk`, `kotlinx-coroutines-test`) al `build.gradle.kts` del módulo `:data` para habilitar pruebas unitarias robustas fuera del entorno de instrumentación.

## 4. Implementación de "Smart Sync" para Catálogos

*   **Objetivo:** Utilizar la información del endpoint `/init` para evitar la descarga redundante de catálogos estáticos en cada inicio de la app.
*   **Implementación:**
    *   Se identificó el campo `catalogs_version` en `InitResponseDto` como el mecanismo ideal de control.
    *   **Refactorización de `CatalogosRepository`:** Se modificó para guardar localmente la versión de los catálogos y aceptar una `remoteVersion` en el método `syncCatalogos`. Si las versiones coinciden, la sincronización se omite.
    *   **Actualización de `InitializeAppUseCase`:** Ahora extrae la versión del catálogo de la respuesta de `/init` y la pasa al caso de uso de sincronización, delegando la decisión de descargar o no al repositorio.
    *   **Limpieza de Código:** Se eliminaron llamadas redundantes a `syncCatalogos` en `VentasViewModel` y `EditUnidadProductivaViewModel`, confiando en la sincronización inteligente inicial. Se eliminaron métodos obsoletos de `AuthRepository` relacionados con versiones de catálogos, mejorando la cohesión del código.

# Avances de la Sesión - 21 de Enero de 2026

Esta sesión se centró en la refactorización y el rediseño completo de la sección de resumen de la pantalla principal (`MainContent`).

### 1. Ideación y Prototipado Rápido del Dashboard

*   **Análisis del Requisito:** Se buscó reemplazar la sección estática de la pantalla de inicio por un componente más dinámico y útil para el productor, con la restricción clave de no requerir cambios en el backend.
*   **Proceso Iterativo de Diseño:** Se exploraron varias maquetas en un proceso rápido de prototipado y feedback, descartando conceptos que no se alineaban con los objetivos:
    1.  **Panel de Alertas y Tareas:** Se propuso un panel proactivo, pero se pospuso por la necesidad de nuevos endpoints en el backend.
    2.  **Panel de Accesos Rápidos:** Se exploró una cuadrícula de acciones, pero se concluyó que no aportaba suficiente valor sobre la navegación ya existente.
    3.  **Concepto Final - Panel de Estado de Sincronización:** Se consolidó la idea de un panel centrado exclusivamente en mostrar el estado de los datos pendientes de sincronizar. Este enfoque aporta un valor único y claro en una app con arquitectura offline-first, respondiendo a la pregunta del usuario: "¿Qué datos me faltan guardar en el sistema?".

### 2. Implementación de la Maqueta Final ("Panel de Sincronización")

*   **Componente `SyncStatusDashboard`:** Se implementó desde cero un nuevo componente que consiste en un carrusel horizontal (`HorizontalPager`) con un indicador de puntos en la parte superior.
*   **Diseño de Tarjetas:**
    *   **Contenido:** El carrusel presenta dos tarjetas: "Movimientos Pendientes" y "Stock para Venta" (título actualizado a "Historial de Ventas" en la última iteración). Cada una es clickable para una futura navegación.
    *   **Layout:** Se implementó un diseño de dos columnas (50/50), con texto descriptivo a la izquierda y una ilustración a la derecha, para un mayor impacto visual. Se incorporaron las ilustraciones `ilustracion_movimientos.webp` y `ilustracion_ventas.webp`.
    *   **Estilo:** Tras múltiples iteraciones de refinamiento (ajuste de tamaño, colores, degradados, y tipografía), se definió un estilo final con tarjetas blancas de `150.dp` de altura, elevación para dar profundidad, y un layout interno compacto.
*   **Integración:** El nuevo `SyncStatusDashboard` fue integrado en `MainContent`, reemplazando la sección anterior.

### 3. Resolución de Incidencias Técnicas

*   **Error de Compilador de Compose:** Se diagnosticó y resolvió un error de compilación persistente (`@Composable invocations can only happen from the context of a @Composable function`). Se identificó que la causa raíz era un acceso incorrecto a `MaterialTheme` desde un scope no composable (`Canvas`), lo cual fue corregido para estabilizar el entorno.
*   **Gestión de Recursos:** Se movieron y renombraron correctamente los nuevos archivos de imagen (`.webp`) para cumplir con las convenciones de nombres de recursos de Android y solucionar un error de `mergeDebugResources`.

---
**Estado Actual:** La nueva maqueta del dashboard está completamente implementada, es visualmente coherente con el resto de la aplicación y compila con éxito. Queda lista para la futura implementación de la lógica de datos para mostrar los pendientes reales.

## Avances de la Sesión Actual (22 de enero de 2026)

Como parte del equipo de desarrollo, hoy hemos implementado y mejorado varias funcionalidades clave en la aplicación móvil. A continuación, se detallan los avances:

### 1. Refactorización y Mejoras en el Dashboard (SyncStatusDashboard)

-   **Flexibilidad en Tarjetas**: Hemos refactorizado el componente `SyncStatusDashboard` para permitir una personalización más granular de la posición y el contenido de cada tarjeta. Esto se logró mediante la introducción de una `data class DashboardPage` que encapsula todas las propiedades visuales necesarias (título, descripción, recurso de imagen, alineación y desplazamiento vertical).
-   **Ajuste Visual de Ilustraciones**: La ilustración de la tarjeta de "Historial de Ventas" (`"sales"`) ahora se ha ajustado, bajando unos píxeles para mejorar la estética, sin afectar la presentación de la primera tarjeta.
-   **Corrección del Efecto "Ripple"**: Se reubicaron los modificadores `clip` y `clickable` a la `Row` interna de `PendingItemsCard`. Esto asegura que el efecto "ripple" (la sombra al presionar) se recorte correctamente según la forma redondeada de la tarjeta, manteniendo tanto el borde como la elevación deseada de las tarjetas.

### 2. Navegación de Tarjetas desde el Dashboard

-   **"Movimientos Pendientes"**: La tarjeta de "Movimientos Pendientes" ahora navega directamente a la segunda página (índice 1) del `MovimientoStepperScreen`. Esto permite al usuario revisar los movimientos sin necesidad de una `unidadId` previa. Para ello, se modificó `MovimientoStepperScreen` para aceptar un parámetro `initialPage` y la ruta `MOVIMIENTO_FORM` en `AppNavigation.kt` se hizo flexible para aceptar `unidadId` opcionales y `initialPage`.
-   **"Historial de Ventas"**: La navegación de esta tarjeta fue corregida para dirigir a `Routes.VENTAS_HISTORIAL` (correspondiente a `HistorialVentasScreen`), que proporciona un resumen de ventas. Se rectificó una implementación inicial errónea que la dirigía a `RESUMEN_MOVIMIENTOS`.

### 3. Implementación de la Sección de Ayuda (FAQ y Soporte Técnico)

-   **FAQ (Preguntas Frecuentes)**:
    -   Se realizó una investigación exhaustiva de las funcionalidades de la aplicación para compilar un manual de ayuda en formato de preguntas y respuestas.
    -   Se creó el archivo `HelpCenter.kt`, que define la estructura de datos (`FaqItem`, `FaqCategory`, `faqData`) y la interfaz de usuario para la `HelpScreen`, con elementos FAQ expandibles para una mejor experiencia de usuario.
-   **Chat de Soporte Técnico**:
    -   Se desarrolló `SupportChatScreen.kt`, una interfaz de usuario de chat básica para la comunicación con soporte.
    -   Se configuró la persistencia local de los mensajes de chat utilizando Room, incluyendo la creación de `SupportMessageEntity`, `SupportMessageDao` y la actualización de `SincMobileDatabase` (incrementando la versión a 6).
    -   Se integró el `TicketApiService` y el `TicketRepository` existentes para la comunicación con el endpoint `/tickets` del backend.
    -   Se creó `SupportChatState.kt` y `SupportChatViewModel.kt` para gestionar el estado del chat, el envío de mensajes y la carga del historial desde la base de datos local.
    -   Se creó `SupportMessageMapper.kt` para facilitar la conversión entre los modelos de entidad y de dominio de los mensajes de soporte.
-   **Integración y Navegación**:
    -   Ambas pantallas (`HelpScreen` y `SupportChatScreen`) se integraron en `AppNavigation.kt` con nuevas rutas (`HELP`, `SUPPORT_CHAT`) y transiciones adecuadas.
    -   Se añadió un enlace de navegación a `HelpScreen` desde la `SettingsScreen`.
    -   Se ajustó el `onItemSelected` de la `CozyBottomNavBar` en `MainScreen.kt` para que el ítem "Ayuda" navegue directamente a `HelpScreen` utilizando `navController.navigate(Routes.HELP)`.
-   **Manejo de Insets**:
    -   Se aplicaron los modificadores `Modifier.navigationBarsPadding()` al `Scaffold` y `Modifier.statusBarsPadding()` al `MinimalHeader` tanto en `HelpScreen` como en `SupportChatScreen` para asegurar un manejo correcto de los insets del sistema y evitar superposiciones con las barras del sistema.

### 4. Corrección de Errores de Compilación Recurrentes

-   **Clase `Result`**: Se corrigieron errores de compilación relacionados con la clase `Result` personalizada del proyecto, que requería dos argumentos de tipo (`Result<T, Error>`).
-   **Referencias No Resueltas**: Se solucionaron múltiples errores de "Unresolved reference" (`SupportMessage`, `toDomain`, `toEntity`) recreando los archivos `SupportMessage.kt` y `SupportMessageMapper.kt`, que fueron eliminados por error durante refactorizaciones previas.
-   **Errores de Sintaxis**: Se corrigió un error de sintaxis en `SupportChatScreen.kt` donde se utilizaba incorrectamente el operador ternario (`if (condition) true_value : false_value`), reemplazándolo por la sintaxis correcta de `if-else` de Kotlin.
-   **Importaciones Redundantes**: Se eliminaron importaciones redundantes en `SupportChatState.kt` que causaban conflictos.

Todos estos cambios aseguran una experiencia de usuario más fluida y una funcionalidad de ayuda robusta dentro de la aplicación.
