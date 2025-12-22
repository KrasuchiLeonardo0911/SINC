# Documentación de la API para la Aplicación Móvil (Nuevos Endpoints)

## Introducción

Este documento describe los nuevos endpoints implementados para la aplicación móvil del sistema de gestión ovino-caprino. Estos endpoints permiten a los productores obtener información actualizada sobre su stock, su historial de movimientos y la logística.

## 1. Autenticación

Todos los endpoints descritos a continuación (excepto el de `login`) requieren autenticación mediante un **Bearer Token** obtenido a través del endpoint `POST /api/movil/login`. El token debe ser incluido en la cabecera `Authorization` de todas las solicitudes.

**Header:** `Authorization: Bearer {token}`

---

## 2. Endpoints Principales

### 2.1. Obtener Stock Desglosado del Productor



Este endpoint devuelve el stock completo del productor en un formato estructurado y anidado. La respuesta está diseñada para ser consumida directamente por la aplicación móvil, facilitando la renderización de vistas detalladas del stock por unidad productiva, especie, raza y categoría.



*   **Endpoint:** `GET /api/movil/stock`

*   **Protección:** Requiere autenticación.

*   **Respuesta Exitosa (200 OK):**

    ```json

    {

        "data": {

            "unidades_productivas": [

                {

                    "id": 1,

                    "nombre": "Campo Principal",

                    "stock_total": 15,

                    "especies": [

                        {

                            "nombre": "Ovino",

                            "stock_total": 15,

                            "desglose": [

                                {

                                    "categoria": "Oveja",

                                    "raza": "Merino",

                                    "cantidad": 15

                                }

                            ]

                        }

                    ]

                }

            ],

            "stock_total_general": 15

        },

        "message": "Stock actual obtenido con éxito."

    }

    ```



### 2.2. Obtener Historial de Movimientos (Sincronización Inicial)



Este endpoint está diseñado para la **sincronización inicial** de datos. Devuelve el historial completo de movimientos de stock del productor, permitiendo a la aplicación móvil construir su base de datos local.



*   **Endpoint:** `GET /api/movil/cuaderno/movimientos`

*   **Protección:** Requiere autenticación.

*   **Respuesta Exitosa (200 OK):** Un array de objetos, donde cada objeto es un movimiento.

    ```json

    [

        {

            "id": 1,

            "fecha_registro": "2023-12-21T10:00:00.000000Z",

            "cantidad": 15,

            "especie": "Ovino",

            "categoria": "Cordero/a",

            "raza": "Merino",

            "motivo": "Nacimiento",

            "tipo_movimiento": "alta",

            "unidad_productiva": "Mi Chacra de Prueba",

            "destino_traslado": null

        },

        {

            "id": 2,

            "fecha_registro": "2023-12-20T11:30:00.000000Z",

            "cantidad": 5,

            "especie": "Ovino",

            "categoria": "Oveja",

            "raza": "Merino",

            "motivo": "Venta",

            "tipo_movimiento": "baja",

            "unidad_productiva": "Mi Chacra de Prueba",

            "destino_traslado": "Establecimiento vecino"

        }

    ]

    ```



### 2.3. Registrar Nuevos Movimientos (Actualización Incremental)



Este endpoint permite registrar uno o más movimientos de stock. En lugar de devolver un simple mensaje de éxito, la respuesta contiene una representación completa de los movimientos recién creados, incluyendo sus IDs generados por el servidor. Esto permite a la aplicación móvil actualizar su base de datos local de forma incremental **(delta sync)**.



*   **Endpoint:** `POST /api/movil/cuaderno/movimientos`

*   **Protección:** Requiere autenticación.

*   **Body (JSON - Ejemplo):**

    ```json

    {

        "upId": 1,

        "movimientos": [

            {

                "especie_id": 1,

                "categoria_id": 1,

                "raza_id": 1,

                "cantidad": 10,

                "motivo_movimiento_id": 1,

                "destino_traslado": null

            }

        ]

    }

    ```

*   **Respuesta Exitosa (201 Created):** Un array con los movimientos recién creados.

    ```json

    [

        {

            "id": 101, // Nuevo ID generado por el servidor

            "fecha_registro": "2023-12-21T15:30:00.000000Z",

            "cantidad": 10,

            "especie": "Ovino",

            "categoria": "Cordero/a",

            "raza": "Merino",

            "motivo": "Nacimiento",

            "tipo_movimiento": "alta",

            "unidad_productiva": "Mi Chacra de Prueba",

            "destino_traslado": null

        }

    ]

    ```



### 2.4. Obtener Próxima Visita de Logística



Informa sobre la próxima visita programada del camión de logística.



*   **Endpoint:** `GET /api/movil/logistica/proxima-visita`

*   **Protección:** Requiere autenticación.

*   **Respuesta Exitosa (200 OK - Visita programada):**

    ```json

    {

        "fecha": "2024-01-01T00:00:00.000000Z",

        "dias_restantes": 10,

        "mensaje": "Faltan 10 días para la próxima visita."

    }

    ```

*   **Respuesta Exitosa (200 OK - Sin visita):**

    ```json

    {

        "fecha": null,

        "mensaje": "No hay una próxima visita de logística programada."

    }

    ```



---



## 3. Decisiones de Diseño y Optimizaciones



*   **Sincronización de Datos (Delta Sync):** Se optó por un patrón de "delta sync" para la gestión de movimientos. La aplicación móvil realiza una sincronización completa una única vez (`GET /cuaderno/movimientos`). Posteriormente, cada vez que crea nuevos movimientos (`POST /cuaderno/movimientos`), el servidor le devuelve los registros recién creados. Esto es mucho más eficiente que reenviar el historial completo, ahorrando ancho de banda y mejorando el rendimiento.



*   **Endpoint de Stock Desglosado:** El endpoint `GET /stock` ahora provee un resumen completo y estructurado del stock actual. Esto elimina la necesidad de que la aplicación móvil calcule totales o desgloses a partir del historial de movimientos, simplificando la lógica del cliente y usando el servidor como única fuente de verdad para el estado del stock.



*   **Clases de Acción Específicas para Móvil:** Para evitar conflictos con la lógica de la aplicación web, se crearon "Actions" dedicadas (`GetHistorialMovimientosAction`, `GenerateStockSummaryForApiAction`) para la API móvil. Esto asegura que los cambios en la API móvil no afecten el funcionamiento de la web.
