# Arquitectura Offline-First para SINCMOBILE

Este documento detalla la estrategia y la implementación de las capacidades offline-first para la aplicación móvil SINCMOBILE, siguiendo los principios de la Arquitectura Limpia establecida en el proyecto. El objetivo principal es permitir que la aplicación funcione de manera robusta en entornos con conectividad limitada o nula, garantizando la persistencia de datos localmente y la sincronización con el backend cuando la conexión esté disponible.

## 1. Principios Fundamentales

*   **Offline-First**: La aplicación debe ser completamente funcional sin conexión a internet para las operaciones de registro de movimientos y consulta de datos de soporte (catálogos, unidades productivas).
*   **Single Source of Truth (SSOT)**: La base de datos local (Room) será la fuente principal de datos para la interfaz de usuario. Todos los datos mostrados al usuario provendrán de Room, garantizando consistencia y disponibilidad inmediata.
*   **Sincronización Bidireccional**:
    *   **Descarga**: Los datos de soporte (catálogos, unidades productivas) se descargarán del servidor y se almacenarán localmente. La UI siempre leerá de la copia local.
    *   **Carga**: Los movimientos de stock registrados offline se almacenarán localmente y se marcarán como "pendientes de sincronización". Se enviarán al servidor cuando haya conexión y se marcarán como "sincronizados" tras el éxito.
*   **Arquitectura Limpia**: La implementación se adherirá estrictamente a la separación de capas (`:app`, `:domain`, `:data`) para mantener la modularidad, la testabilidad y la mantenibilidad.

## 2. Componentes Clave y su Implementación

### 2.1. Capa de Datos (`:data` Module)

Esta capa será la responsable de la persistencia local y la interacción con la API remota.

#### 2.1.1. Base de Datos Local con Room

*   **Tecnología**: Se utilizará la librería [Room Persistence Library](https://developer.android.com/training/data-storage/room) de Android Jetpack.
*   **Entidades (`@Entity`)**: Se definirán clases de datos que representarán las tablas en la base de datos SQLite local. Estas entidades se basarán en la estructura de las migraciones y modelos del backend de Laravel para asegurar la consistencia.
    *   `UnidadProductivaEntity`: Para almacenar las unidades productivas del productor.
    *   `EspecieEntity`: Para almacenar las especies disponibles.
    *   `RazaEntity`: Para almacenar las razas, con una clave foránea a `EspecieEntity`.
    *   `CategoriaAnimalEntity`: Para almacenar las categorías de animales, con una clave foránea a `EspecieEntity`.
    *   `MotivoMovimientoEntity`: Para almacenar los motivos de movimiento (alta/baja), incluyendo el campo `tipo`.
    *   `MovimientoPendienteEntity`: Para almacenar los movimientos de stock creados offline o pendientes de sincronización. Incluirá un campo `sincronizado` (booleano) y `fecha_registro_local` (timestamp).
*   **DAOs (`@Dao` - Data Access Objects)**: Interfaces que definirán los métodos para interactuar con las entidades (insertar, consultar, actualizar, eliminar).
    *   `UnidadProductivaDao`
    *   `CatalogosDao` (o DAOs separados para Especie, Raza, CategoriaAnimal, MotivoMovimiento)
    *   `MovimientoPendienteDao`
*   **Database (`@Database`)**: Una clase abstracta que extiende `RoomDatabase`, configurando las entidades y los DAOs.
*   **Módulos Hilt**: Se creará un `DatabaseModule` para proveer instancias singleton de la base de datos Room y sus DAOs.

#### 2.1.2. Implementaciones de Repositorios

Los repositorios en la capa de datos serán los encargados de orquestar el acceso a los datos, decidiendo si provienen de la red o de la base de datos local.

*   **`AuthRepositoryImpl`**: Se extenderá para incluir la gestión del token de autenticación de forma persistente (ej. usando `SharedPreferences` o `DataStore`).
*   **`UnidadProductivaRepositoryImpl`**:
    *   El método `getUnidadesProductivas()` leerá directamente de `UnidadProductivaDao` y devolverá un `Flow` de datos.
    *   Se añadirá un método `syncUnidadesProductivas()` que:
        1.  Llamará a la API remota para obtener las UPs.
        2.  Borrará las UPs existentes en `UnidadProductivaDao`.
        3.  Insertará las UPs recién obtenidas en `UnidadProductivaDao`.
*   **`CatalogosRepositoryImpl`**: Similar a `UnidadProductivaRepositoryImpl`, leerá de los DAOs de catálogos y tendrá un método `syncCatalogos()` para actualizar desde la red.
*   **`MovimientoRepositoryImpl`**:
    *   Se añadirá `saveMovimientoLocal(movimiento: Movimiento)` que insertará en `MovimientoPendienteDao`.
    *   Se añadirá `getMovimientosPendientes()` que leerá de `MovimientoPendienteDao`.
    *   Se añadirá `marcarMovimientoComoSincronizado(id: Long)` para actualizar el estado en `MovimientoPendienteDao`.
    *   El método `sendMovimientosToApi()` (o similar) se encargará de enviar los movimientos pendientes al backend.

### 2.2. Capa de Dominio (`:domain` Module)

Esta capa contendrá la lógica de negocio pura, independiente de la fuente de datos.

*   **Modelos**: Se mantendrán los modelos de dominio existentes (`UnidadProductiva`, `Catalogos`, `Movimiento`). Se creará un nuevo modelo `MovimientoPendiente` que representará el estado de un movimiento antes de ser sincronizado.
*   **Interfaces de Repositorio**: Se actualizarán las interfaces existentes (`UnidadProductivaRepository`, `CatalogosRepository`, `MovimientoRepository`) para incluir los nuevos métodos de interacción con la persistencia local y la sincronización.
*   **Casos de Uso (`Use Cases`)**:
    *   `GetUnidadesProductivasUseCase`: Ahora obtendrá las UPs del repositorio, que a su vez las leerá de la base de datos local.
    *   `GetCatalogosUseCase`: Similar, obtendrá los catálogos de la base de datos local.
    *   `SaveMovimientoLocalUseCase`: Encapsulará la lógica para guardar un movimiento en la base de datos local.
    *   `SyncDataUseCase`: Un caso de uso general para iniciar la sincronización de catálogos y UPs desde la red.
    *   `SyncMovimientosPendientesUseCase`: Encapsulará la lógica para obtener movimientos pendientes, enviarlos a la API y actualizar su estado.

### 2.3. Capa de Presentación (`:app` Module)

Esta capa se encargará de la UI y la interacción con el usuario.

*   **ViewModels**:
    *   Los ViewModels (ej. `HomeViewModel`, `MovimientoViewModel`) utilizarán los nuevos casos de uso para obtener datos de la base de datos local y para guardar movimientos.
    *   Se gestionará el estado de la UI para reflejar la disponibilidad de conexión y el estado de sincronización de los movimientos.
*   **UI (Jetpack Compose)**:
    *   Las pantallas se construirán para mostrar datos obtenidos de `Flow`s de Room, reaccionando a los cambios en la base de datos local.
    *   Se implementará la lógica para filtrar los `MotivoMovimiento` por `tipo` (alta/baja) en los formularios.
    *   Se añadirán indicadores visuales para movimientos pendientes de sincronización.
    *   Se podría incluir un botón de "Sincronizar ahora" para forzar la carga de movimientos pendientes.
*   **Manejo de Conectividad**: Se implementará un `NetworkMonitor` (ej. usando `ConnectivityManager`) para observar el estado de la red y disparar la sincronización automática cuando la conexión se restablezca.
*   **WorkManager (Futuro)**: Para sincronizaciones en segundo plano más robustas y programadas, se podría integrar `WorkManager` para ejecutar `SyncMovimientosPendientesUseCase` de forma periódica o cuando se cumplan ciertas condiciones (ej. dispositivo cargando, Wi-Fi disponible).

## 3. Flujo de Trabajo Offline

1.  **Inicio de la App**:
    *   Tras el login inicial (que requiere conexión), la app intenta `SyncDataUseCase` para descargar los catálogos y UPs más recientes y almacenarlos en Room.
    *   Si no hay conexión, la app carga los datos de soporte directamente desde Room.
2.  **Registro de Movimientos (Offline/Online)**:
    *   El usuario selecciona si es un "alta" o "baja".
    *   El formulario se carga con datos de catálogos desde Room.
    *   Al guardar, el movimiento se inserta en `MovimientoPendienteEntity` con `sincronizado = false`.
3.  **Sincronización**:
    *   Cuando la app detecta conexión (o el usuario lo activa manualmente), `SyncMovimientosPendientesUseCase` se ejecuta.
    *   Este caso de uso lee los movimientos con `sincronizado = false` de Room.
    *   Para cada movimiento, intenta enviarlo a la API.
    *   Si la API responde con éxito, el movimiento en Room se actualiza a `sincronizado = true` y se elimina de la lista de pendientes.
    *   Si falla, el movimiento permanece como `sincronizado = false` para un reintento posterior.

---

Este documento servirá como nuestra hoja de ruta.

Ahora, el siguiente paso es añadir las dependencias de Room al proyecto.

## 4. Estrategia de Pruebas para la Capa de Persistencia

Para garantizar la robustez y el correcto funcionamiento de la capa de persistencia, se implementará una estrategia de pruebas unitarias y de instrumentación.

*   **Pruebas de DAOs**: Cada DAO (Data Access Object) tendrá su propia clase de prueba (ej. `UnidadProductivaDaoTest`).
*   **Base de Datos en Memoria**: Las pruebas se ejecutarán contra una base de datos Room en memoria (`Room.inMemoryDatabaseBuilder`). Esto asegura que cada prueba se ejecute en un entorno limpio, aislado y rápido, sin depender del estado del dispositivo.
*   **Coroutines de Prueba**: Se utilizará `kotlinx-coroutines-test` para manejar las coroutines y los `Flow`s dentro de las pruebas de manera síncrona y predecible.
*   **Aserciones**: Se usará la librería `Google Truth` para realizar aserciones claras y legibles sobre los resultados de las operaciones de la base de datos.
    *   **Entorno de Prueba de Hilt**: Se configurará Hilt para inyectar dependencias en las clases de prueba, asegurando que la base de datos y los DAOs se creen y se proporcionen correctamente en el entorno de prueba.

- **DAOs Cubiertos por Tests (02 de Noviembre de 2025)**:
    - `UnidadProductivaDao`
    - `EspecieDao`
    - `RazaDao`
    - `CategoriaAnimalDao`
    - `MotivoMovimientoDao`
    - `MovimientoPendienteDao`
