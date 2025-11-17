# Guía para Desarrolladores Móviles - Integración API Backend

Hola, futuro desarrollador/a móvil del sistema Ovino-Caprino. Esta guía complementa la documentación oficial de la API (`API_MOVIL.md`) y ofrece un resumen del flujo de trabajo, con un enfoque especial en la autenticación y la gestión de las Unidades Productivas (UPs).

## 1. URL Base de la API

Asegúrate de configurar la URL base de la API en tu aplicación móvil.

-   **Entorno Local (Desarrollo):** Típicamente será algo como `http://localhost/Proyecto-ovino-caprinos/public/api/movil` (ajusta según tu configuración de XAMPP o servidor local).
-   **Entorno de Producción:** Será el dominio final del backend, por ejemplo `https://api.tudominio.com/api/movil`.

## 2. Flujo de Autenticación del Usuario Móvil

La API utiliza [Laravel Sanctum](https://laravel.com/docs/11.x/sanctum) para la autenticación basada en tokens (Bearer Tokens).

### 2.1. Iniciar Sesión (`POST /api/movil/login`)

-   **Propósito:** Autenticar al usuario productor y obtener un token de acceso.
-   **Método:** `POST`
-   **Endpoint:** `/api/movil/login`
-   **Cabeceras Requeridas:**
    -   `Accept: application/json`
    -   `Content-Type: application/json`
-   **Cuerpo de la Petición (JSON):**
    ```json
    {
      "email": "productor@ejemplo.com",
      "password": "su_password",
      "device_name": "NombreDelDispositivoMovil" // Un nombre descriptivo para el dispositivo
    }
    ```
    -   **`device_name`**: Ayuda al usuario a identificar y revocar el token desde la interfaz web si lo desea.
-   **Respuesta de Éxito (200 OK):**
    ```json
    {
      "token": "1|abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    }
    ```
-   **Respuesta de Error (401 Unauthorized):**
    ```json
    {
      "message": "Credenciales inválidas."
    }
    ```

### 2.2. Uso del Token

Una vez que obtengas el `token`, debes incluirlo en la cabecera `Authorization` de **todas las peticiones protegidas** (casi todas las demás peticiones que no sean el login).

-   **Cabecera:** `Authorization: Bearer <TU_TOKEN_OBTENIDO>`
-   **Ejemplo:** `Authorization: Bearer 1|aBcDeFgHiJkLmNoPqRsTuVwXyZ1234567890`

## 3. Gestión de Unidades Productivas (UPs)

Este es un flujo crucial, especialmente para los usuarios que ingresan por primera vez al sistema y no tienen UPs registradas.

### 3.1. Verificar Unidades Productivas Existentes (`GET /api/movil/unidades-productivas`)

-   **Propósito:** Obtener la lista de unidades productivas asociadas al productor autenticado.
-   **Método:** `GET`
-   **Endpoint:** `/api/movil/unidades-productivas`
-   **Protección:** Requiere token de autenticación.
-   **Respuesta de Éxito (200 OK):**
    -   Si el productor **tiene UPs**: Un array de objetos UP.
        ```json
        [
            {
                "id": 1,
                "nombre": "Mi Campo Principal",
                "latitud": "-34.5875",
                "longitud": "-58.6742"
            },
            // ... más UPs
        ]
        ```
    -   Si el productor **NO tiene UPs**: Un array vacío.
        ```json
        []
        ```

#### Consideración Crítica para el Flujo Móvil:

**Si esta petición devuelve un array vacío `[]`**, la aplicación móvil debe **dirigir obligatoriamente al usuario a un formulario de creación de Unidad Productiva**. No debe permitirle acceder a otras funcionalidades que dependan de tener una UP.

### 3.2. Crear Nueva Unidad Productiva (`POST /api/movil/unidades-productivas`)

-   **Propósito:** Registrar una nueva unidad productiva para el productor autenticado.
-   **Método:** `POST`
-   **Endpoint:** `/api/movil/unidades-productivas`
-   **Protección:** Requiere token de autenticación.
-   **Cabeceras Requeridas:**
    -   `Accept: application/json`
    -   `Content-Type: application/json`
    -   `Authorization: Bearer <TU_TOKEN_OBTENIDO>`

#### Estructura Visual y Datos del Formulario Móvil:

El formulario en la app móvil para crear una UP debe recoger los siguientes datos, siguiendo esta estructura JSON para la petición `POST`:

| Campo                   | Tipo         | Obligatorio | Detalle y Consideraciones para la App Móvil                                                                                                                  |
| :---------------------- | :----------- | :---------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `nombre`                | String       | SÍ          | Input de texto. Ejemplo: "La Querencia", "Campo Nº3".                                                                                                        |
| `identificador_local`   | String       | SÍ          | Input de texto. Este es un código único (ej. RNSPA). Debe validarse que no exista ya en el backend.                                                            |
| `superficie`            | Number (Float) | SÍ          | Input numérico (decimal). Representa hectáreas.                                                                                                                |
| `latitud`               | Number (Float) | SÍ          | Input numérico (decimal). Preferiblemente obtenido de la API de localización del dispositivo o de un selector de mapa interactivo. Rango -90 a 90.            |
| `longitud`              | Number (Float) | SÍ          | Input numérico (decimal). Preferiblemente obtenido de la API de localización del dispositivo o de un selector de mapa interactivo. Rango -180 a 180.          |
| `municipio_id`          | Integer      | SÍ          | **Dropdown/Selector**. El ID debe obtenerse de una lista de municipios que el backend puede proveer (aunque aún no hay un endpoint específico para esto, se podría añadir o usar el `GET /api/movil/catalogos` si se extienden los catálogos). **Es crucial que este ID corresponda a un municipio existente en la base de datos.** |
| `condicion_tenencia_id` | Integer      | NO          | **Dropdown/Selector** (Opcional). ID de la condición de tenencia (Propia, Alquilada). Se necesitan catálogos desde el backend si se implementa esta selección.  |
| `fuente_agua_id`        | Integer      | NO          | **Dropdown/Selector** (Opcional). ID de la principal fuente de agua.                                                                                         |
| `tipo_suelo_id`         | Integer      | NO          | **Dropdown/Selector** (Opcional). ID del tipo de suelo.                                                                                                      |
| `tipo_pasto_id`         | Integer      | NO          | **Dropdown/Selector** (Opcional). ID del tipo de pastura.                                                                                                    |

-   **Cuerpo de la Petición (JSON de Ejemplo):**
    ```json
    {
      "nombre": "Estancia El Sol Naciente",
      "identificador_local": "RNSPA-APP-002",
      "superficie": 450.0,
      "latitud": -34.7000,
      "longitud": -58.5000,
      "municipio_id": 5,
      "condicion_tenencia_id": 1,
      "fuente_agua_id": 2,
      "tipo_suelo_id": 3,
      "tipo_pasto_id": null
    }
    ```

-   **Respuesta de Éxito (201 Created):**
    Un objeto JSON de la Unidad Productiva recién creada, incluyendo su `id` y otros metadatos.
    ```json
    {
        "id": 16,
        "nombre": "Estancia El Sol Naciente",
        "identificador_local": "RNSPA-APP-002",
        "superficie": "450.0",
        "latitud": "-34.7000",
        "longitud": "-58.5000",
        "municipio_id": 5,
        "condicion_tenencia_id": 1,
        "fuente_agua_id": 2,
        "tipo_suelo_id": 3,
        "tipo_pasto_id": null,
        "created_at": "2025-11-16T12:30:00.000000Z",
        "updated_at": "2025-11-16T12:30:00.000000Z"
    }
    ```

-   **Respuesta de Error (422 Unprocessable Entity - Errores de Validación):**
    ```json
    {
        "message": "The given data was invalid.",
        "errors": {
            "identificador_local": [
                "El identificador local ya ha sido registrado."
            ],
            "municipio_id": [
                "El municipio seleccionado no es válido."
            ]
        }
    }
    ```

### 3.3. Obtener Datos para Dropdowns (Catálogos)

Para poblar los dropdowns de `municipio_id`, `condicion_tenencia_id`, `fuente_agua_id`, `tipo_suelo_id` y `tipo_pasto_id`, necesitarás endpoints que te provean estas listas.

-   Actualmente, el endpoint `GET /api/movil/catalogos` provee especies, razas, categorías y motivos de movimiento.
-   Para los IDs de `municipio`, te sugiero hacer una petición a `GET /api/movil/unidades-productivas` para ver cómo se estructuran las `unidades_productivas` y si hay algún endpoint ya existente que liste municipios. Si no, se podría añadir un endpoint dedicado tipo `GET /api/movil/municipios`.
-   Para los otros catálogos opcionales (`condicion_tenencia_id`, etc.), también necesitarías endpoints específicos o extender el `GET /api/movil/catalogos`.

## 4. Consideraciones de UI/UX para la Creación de UP

-   **Validación en Cliente:** Implementa validación en el cliente para los campos obligatorios y formatos (numéricos, rangos de lat/long) antes de enviar la petición al backend. Esto mejora la experiencia del usuario.
-   **Indicadores de Carga:** Muestra un spinner o indicador de carga mientras se envía la petición.
-   **Mensajes de Error:** Muestra claramente los mensajes de error de validación (del backend) al usuario, asociados a los campos correspondientes.
-   **Selección de Ubicación:** Utiliza un componente de mapa interactivo que permita al usuario seleccionar la latitud y longitud fácilmente, o la detección de ubicación del dispositivo.
-   **Confirmación de Éxito:** Una vez creada la UP, informa al usuario del éxito y dirígelo a la pantalla donde pueda ver sus Unidades Productivas.

## 5. Próximos Pasos (Sugerencias)

1.  Asegúrate de que la aplicación móvil tenga la lógica para manejar tanto el caso de un productor con UPs como el de uno sin ellas.
2.  Desarrolla el formulario de creación de UP, prestando atención a los errores de validación y la experiencia de usuario.
3.  Considera la implementación de endpoints para obtener los catálogos de los campos opcionales (`condicion_tenencia_id`, `fuente_agua_id`, etc.) si tu diseño lo requiere.

¡Mucho éxito con la implementación móvil!

---

## Registro de Cambios - Sesión del 16 de Noviembre de 2025

Esta sección detalla los cambios realizados para implementar la funcionalidad de creación de Unidades Productivas (UPs) y la infraestructura de catálogos, siguiendo la arquitectura limpia del proyecto.

### 1. Implementación de la Lógica de Creación de Unidades Productivas

Se implementó el flujo completo para obtener y crear UPs, abarcando las tres capas de la arquitectura.

#### Capa de Datos (`:data`)

-   **`UnidadProductivaApiService.kt`**:
    -   Se creó esta nueva interfaz de Retrofit para manejar los endpoints de UPs.
    -   Se añadieron los métodos `getUnidadesProductivas()` (`GET api/movil/unidades-productivas`) y `createUnidadProductiva()` (`POST api/movil/unidades-productivas`).
-   **Refactorización de `AuthApiService.kt`**:
    -   Se eliminó el método `getUnidadesProductivas()` para centralizar la lógica de UPs en su propio servicio.
-   **DTOs (Data Transfer Objects)**:
    -   `UnidadProductivaDto.kt`: Se actualizó para incluir todos los campos de una UP (`identificadorLocal`, `superficie`, `municipioId`, etc.).
    -   `CreateUnidadProductivaRequest.kt`: Se creó este nuevo DTO para modelar el cuerpo de la petición `POST` para crear una UP.
-   **Inyección de Dependencias (`NetworkModule.kt`)**:
    -   Se añadió un `provide` para `UnidadProductivaApiService` para que Hilt pueda inyectarla.
-   **`UnidadProductivaRepositoryImpl.kt`**:
    -   Se cambió la dependencia de `AuthApiService` a `UnidadProductivaApiService`.
    -   Se implementó el nuevo método `createUnidadProductiva()`, que mapea el modelo de dominio `CreateUnidadProductivaData` al DTO `CreateUnidadProductivaRequest` antes de llamar a la API.
    -   Se actualizaron las funciones de mapeo `toEntity()` y `toDomain()` para reflejar los nuevos campos.

#### Capa de Dominio (`:domain`)

-   **Modelos**:
    -   `UnidadProductiva.kt`: Se actualizó para incluir todos los campos relevantes de una UP.
    -   `CreateUnidadProductivaData.kt`: Se creó este nuevo modelo para pasar los datos de creación de UP desde la capa de presentación a la de dominio de forma limpia.
-   **Repositorio (`UnidadProductivaRepository.kt`)**:
    -   Se actualizó la interfaz para incluir el nuevo método `createUnidadProductiva(data: CreateUnidadProductivaData)`.
-   **Casos de Uso (`use_case`)**:
    -   `CreateUnidadProductivaUseCase.kt`: Se creó para encapsular la lógica de creación de una UP.
    -   `SyncUnidadesProductivasUseCase.kt`: Se creó para manejar la sincronización de UPs.

### 2. Extensión del Sistema de Catálogos

Para poder poblar los dropdowns del nuevo formulario, se extendió el sistema de catálogos existente.

#### Capa de Dominio (`:domain`)

-   **`Catalogos.kt`**: Se extendió el modelo para incluir:
    -   `municipios: List<Municipio>`
    -   `condicionesTenencia: List<CondicionTenencia>`
    -   `fuentesAgua: List<FuenteAgua>`
    -   `tiposSuelo: List<TipoSuelo>`
    -   `tiposPasto: List<TipoPasto>`
-   Se crearon los nuevos modelos de datos correspondientes (`Municipio`, `CondicionTenencia`, etc.).

#### Capa de Datos (`:data`)

-   **`CatalogosDto.kt`**: Se extendió para incluir los nuevos catálogos nulables (`municipios`, `condiciones_tenencia`, etc.) para que coincida con la respuesta de la API.
-   **Entidades de Room (`entities`)**:
    -   Se crearon las nuevas entidades: `MunicipioEntity`, `CondicionTenenciaEntity`, `FuenteAguaEntity`, `TipoSueloEntity`, `TipoPastoEntity`.
-   **DAO (Data Access Object)**:
    -   Se creó un `CatalogosDao.kt` unificado para manejar las operaciones de base de datos de todos los catálogos.
-   **Base de Datos**:
    -   `SincMobileDatabase.kt`: Se actualizaron las `entities` para incluir las nuevas tablas y se reemplazaron los DAOs de catálogos individuales por el `CatalogosDao` unificado.
    -   `DatabaseModule.kt`: Se actualizó para proveer el `CatalogosDao`.
-   **`CatalogosRepositoryImpl.kt`**:
    -   Se refactorizó para usar el `CatalogosDao` unificado.
    -   Se actualizó el método `getCatalogos()` para usar `combine` con los 9 `Flow`s de catálogos.
    -   Se actualizó `syncCatalogos()` para manejar las nuevas listas de catálogos (incluyendo la comprobación de nulos).

### 3. Implementación de la Interfaz de Usuario (UI) y Navegación

Se creó la infraestructura de UI y navegación para acceder y mostrar el formulario de creación de UP.

#### Capa de Presentación (`:app`)

-   **Nueva Feature "Campos"**:
    -   Se crearon `CamposScreen.kt` y `CamposViewModel.kt` en un nuevo paquete `features/campos`.
    -   `CamposScreen` contiene un botón "Registrar Campo" que navega al formulario de creación.
-   **Navegación**:
    -   Se añadió la ruta `Routes.CAMPOS` a `AppNavigation.kt` y `MainScreenRoutes`.
    -   Se actualizó `Sidebar.kt` para que el ítem "Mis Campos" navegue a la nueva pantalla.
    -   Se integró `CamposScreen` en el `when` de `MainScreen.kt`.
-   **Formulario de Creación de UP**:
    -   `CreateUnidadProductivaViewModel.kt`: Se actualizó para cargar los catálogos necesarios desde el `GetCatalogosUseCase`.
    -   `CreateUnidadProductivaScreen.kt`: Se rediseñó completamente para usar un `LazyColumn` con `Card`s que agrupan los campos en secciones ("Información Básica", "Ubicación"), similar a la referencia web. Se añadieron `ExposedDropdownMenuBox` para los selectores.
-   **Lógica de Navegación Condicional**:
    -   Se implementó la lógica en `MainViewModel.kt` y `MainScreen.kt` para detectar si un usuario no tiene UPs después de la sincronización inicial y, en ese caso, navegarlo automáticamente al formulario de creación.

### 4. Corrección de Bugs

-   **Error 404 en `getUnidadesProductivas`**: Se corrigió un error 404 al no haber añadido el prefijo `api/movil/` a los endpoints en `UnidadProductivaApiService.kt`.
-   **`NullPointerException` en `syncCatalogos`**: Se solucionó un crash que ocurría si la API no devolvía alguna de las listas de los nuevos catálogos. Se hizo que las listas en `CatalogosDto` fueran nulables y se añadió la comprobación de nulos en `CatalogosRepositoryImpl`.
-   **Errores de Compilación de Room/Kapt**: Se corrigieron errores de "no such table" ajustando los nombres de las tablas en las queries de `CatalogosDao.kt`.
-   **Error de `combine` con más de 5 `Flow`s**: Se refactorizó el uso de `combine` en `CatalogosRepositoryImpl.kt` para poder manejar 9 `Flow`s.
-   **Bug de Navegación "sin retorno"**: Se corrigió un bug que impedía volver atrás desde el formulario de creación de UP, eliminando el `popUpTo { inclusive = true }` de la acción de navegación.
-   **Bug de Navegación Automática**: Se mejoró la lógica en `MainViewModel.kt` para que la decisión de navegar al formulario de creación se tome después de que la sincronización inicial haya finalizado, evitando la navegación prematura.