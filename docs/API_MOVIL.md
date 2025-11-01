
# Documentación de la API para la Aplicación Móvil

## Introducción

Este documento describe la API RESTful para la aplicación móvil del sistema de gestión ovino-caprino. La API permite a los usuarios autenticarse, y registrar movimientos de stock en su cuaderno de campo digital.

La aplicación móvil debe permitir a los productores (rol "productor") registrar movimientos de stock (altas/bajas) para sus unidades productivas (UP). Estos datos se almacenarán localmente en el dispositivo (usando SQLite) y se sincronizarán con el servidor cuando haya conexión a internet.

## 1. Autenticación

Para interactuar con los endpoints protegidos, el usuario primero debe obtener un token de API a través del endpoint de login.

### 1.1. Login de Usuario

Este endpoint permite a un usuario autenticarse usando su email y contraseña. Si las credenciales son válidas y el usuario tiene el rol de "productor", la API devolverá un token de acceso.

- **Endpoint:** `POST /api/movil/login`
- **Headers:**
  - `Accept: application/json`
  - `Content-Type: application/json`
- **Body (Request):**

```json
{
  "email": "productor@example.com",
  "password": "password123",
  "device_name": "MiCelularSamsung" 
}
```
*Nota: `device_name` es un nombre que el dispositivo le da al token, útil para que el usuario pueda reconocerlo y revocarlo desde la aplicación web si es necesario.*

- **Body (Response - Éxito 200 OK):**

```json
{
  "token": "1|aBcDeFgHiJkLmNoPqRsTuVwXyZ1234567890"
}
```

- **Body (Response - Error 401 Unauthorized):**

```json
{
  "message": "Credenciales inválidas."
}
```

### 1.2. Uso del Token

Una vez obtenido, el token debe ser incluido en la cabecera `Authorization` de todas las solicitudes a endpoints protegidos.

- **Header:** `Authorization: Bearer {token}`

**Ejemplo:**
`Authorization: Bearer 1|aBcDeFgHiJkLmNoPqRsTuVwXyZ1234567890`

## 2. Cuaderno de Campo

### 2.1. Guardar Movimientos de Stock

Este endpoint permite al productor registrar uno o más movimientos de stock (altas o bajas) para una de sus unidades productivas.

- **Endpoint:** `POST /api/movil/cuaderno/movimientos`
- **Protección:** Requiere autenticación (token Sanctum).
- **Headers:**
  - `Accept: application/json`
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`

- **Body (Request):**

```json
{
  "upId": 1,
  "movimientos": [
    {
      "especie_id": 1,
      "categoria_id": 2,
      "raza_id": 3,
      "cantidad": 10,
      "motivo_movimiento_id": 1,
      "destino_traslado": null
    },
    {
      "especie_id": 2,
      "categoria_id": 5,
      "raza_id": 8,
      "cantidad": 5,
      "motivo_movimiento_id": 7,
      "destino_traslado": "Establecimiento vecino"
    }
  ]
}
```

- **Body (Response - Éxito 200 OK):**

```json
{
  "message": "Movimientos guardados exitosamente."
}
```

- **Body (Response - Error 422 Unprocessable Entity):**
  Si hay errores de validación (ej. falta un campo, un ID no existe).

```json
{
  "message": "The given data was invalid.",
  "errors": {
    "upId": [
      "El ID de la unidad productiva es inválido."
    ],
    "movimientos.0.cantidad": [
      "La cantidad debe ser un número entero."
    ]
  }
}
```

- **Body (Response - Error 400 Bad Request):**
  Si ocurre un error de lógica de negocio (ej. no hay período activo).

```json
{
  "error": "No hay un período de declaración activo. Por favor, contacte a un administrador."
}
```

## 3. Endpoints de Datos de Soporte

Estos endpoints proporcionan la información necesaria para poblar los selectores y las opciones en la aplicación móvil.

### 3.1. Obtener Unidades Productivas

Devuelve una lista de las unidades productivas (UPs) asociadas al productor autenticado.

- **Endpoint:** `GET /api/movil/unidades-productivas`
- **Protección:** Requiere autenticación (token Sanctum).
- **Headers:**
  - `Accept: application/json`
  - `Authorization: Bearer {token}`

- **Body (Response - Éxito 200 OK):**

```json
[
    {
        "id": 1,
        "nombre": "Mi Campo Principal",
        "latitud": "-34.5875",
        "longitud": "-58.6742"
    },
    {
        "id": 2,
        "nombre": "Campo Secundario",
        "latitud": "-34.6037",
        "longitud": "-58.3816"
    }
]
```

### 3.2. Obtener Catálogos

Devuelve todos los catálogos de datos necesarios para los formularios de la aplicación (especies, razas, categorías y motivos de movimiento).

- **Endpoint:** `GET /api/movil/catalogos`
- **Protección:** Requiere autenticación (token Sanctum).
- **Headers:**
  - `Accept: application/json`
  - `Authorization: Bearer {token}`

- **Body (Response - Éxito 200 OK):**

```json
{
    "especies": [
        { "id": 1, "nombre": "Ovino" },
        { "id": 2, "nombre": "Caprino" }
    ],
    "razas": [
        { "id": 1, "nombre": "Criolla", "especie_id": 1 },
        { "id": 2, "nombre": "Corriedale", "especie_id": 1 },
        { "id": 8, "nombre": "Criolla", "especie_id": 2 },
        { "id": 9, "nombre": "Anglo-Nubian", "especie_id": 2 }
    ],
    "categorias": [
        { "id": 1, "nombre": "Cordero/a", "especie_id": 1 },
        { "id": 2, "nombre": "Borrego/a (1-2 años)", "especie_id": 1 },
        { "id": 8, "nombre": "Cabrito/a", "especie_id": 2 }
    ],
    "motivos_movimiento": [
        { "id": 1, "nombre": "Nacimiento", "tipo": "alta" },
        { "id": 2, "nombre": "Compra", "tipo": "alta" },
        { "id": 5, "nombre": "Muerte", "tipo": "baja" },
        { "id": 6, "nombre": "Venta", "tipo": "baja" }
    ]
}
```

## 4. Esquema para Base de Datos Local (SQLite)

Se sugiere una tabla simple para almacenar los movimientos de forma local antes de enviarlos a la API.

**Tabla: `movimientos_pendientes`**

| Columna              | Tipo    | Descripción                                           |
| -------------------- | ------- | ----------------------------------------------------- |
| `id`                 | INTEGER | Primary Key, Autoincremental.                         |
| `upId`               | INTEGER | ID de la Unidad Productiva.                           |
| `especie_id`         | INTEGER | Foreign Key a la tabla local de especies.             |
| `categoria_id`       | INTEGER | Foreign Key a la tabla local de categorías.           |
| `raza_id`            | INTEGER | Foreign Key a la tabla local de razas.                |
| `cantidad`           | INTEGER | Cantidad de animales en el movimiento.                |
| `motivo_movimiento_id`| INTEGER | Foreign Key a la tabla local de motivos.              |
| `destino_traslado`   | TEXT    | (Opcional) Destino si el motivo es traslado.          |
| `fecha_creacion`     | TEXT    | Fecha y hora de creación del registro (ISO 8601).     |
| `sincronizado`       | INTEGER | Bandera (0 = no, 1 = sí) para indicar si ya se envió. |

### Flujo de Sincronización

1.  El usuario crea un nuevo movimiento en la app.
2.  La app guarda el movimiento en la tabla `movimientos_pendientes` con `sincronizado = 0`.
3.  Periódicamente, o cuando el usuario lo inicie, la app comprueba si hay conexión a internet.
4.  Si hay conexión, la app agrupa todos los movimientos con `sincronizado = 0` por `upId`.
5.  Para cada `upId`, la app construye el payload JSON y lo envía al endpoint `POST /api/cuaderno/movimientos`.
6.  Si la API responde con éxito (código 200), la app actualiza los registros correspondientes en `movimientos_pendientes` a `sincronizado = 1`.
7.  Si la API responde con un error, la app mantiene los registros como no sincronizados para reintentar más tarde.

