# Especificación Técnica de Nuevos Endpoints - API Móvil

**Fecha:** 15 de Enero de 2026
**Estado:** Implementado y Probado

Este documento detalla los nuevos endpoints disponibles para la gestión de ventas y la inicialización de la aplicación móvil.

---

## 1. Inicialización de la App (`/init`)

Este endpoint debe ser el **primero** en llamarse tras la autenticación. Proporciona el contexto del usuario y banderas de configuración para decidir qué datos sincronizar.

*   **Método:** `GET`
*   **Ruta:** `/api/movil/init`
*   **Auth:** Bearer Token (Sanctum)

### Respuesta Exitosa (200 OK)
```json
{
    "app_control": {
        "min_version": "1.0.0",
        "latest_version": "1.0.0",
        "update_required": false, // Si es true, bloquear la app y pedir actualizar
        "store_url": "https://play.google.com/store/apps/details?id=...",
        "maintenance_mode": false,
        "message": ""
    },
    "user_context": {
        "id": 3,
        "name": "Juan Productor",
        "email": "productor@test.com",
        "role": "productor",
        "setup_completed": true,
        "productor_id": 1, // Guardar este ID para futuras llamadas
        "default_farm_id": 1
    },
    "features": {
        "module_stock": true,
        "module_labors": true,
        "module_sales": false, // IMPORTANTE: Verificar si este flag oculta el módulo de ventas
        "allow_offline_sync": true
    },
    "configuration": {
        "sync_interval_minutes": 15,
        "catalogs_version": "2024-01-15-v1" // Usar para saber si re-descargar catálogos
    }
}
```

---

## 2. Gestión de Declaraciones de Venta

### A. Listar Declaraciones (Historial y Validación)
Utilice este endpoint para poblar la lista de ventas y, **crucialmente**, para calcular el "Stock Comprometido" localmente.

*   **Método:** `GET`
*   **Ruta:** `/api/movil/declaraciones-venta`

#### Respuesta (200 OK)
```json
[
    {
        "id": 1,
        "productor_id": 1,
        "unidad_productiva_id": 1,
        "especie_id": 1,
        "raza_id": 1,
        "categoria_animal_id": 1,
        "cantidad": 5, // Cantidad comprometida
        "estado": "pendiente", // Solo las 'pendiente' descuentan stock virtualmente
        "fecha_declaracion": "2026-01-15T21:43:18.000000Z",
        "observaciones": "Venta de prueba",
        "especie": { "id": 1, "nombre": "Ovino" },
        "raza": { "id": 1, "nombre": "Merino" },
        "categoria_animal": { "id": 1, "nombre": "Cordero" },
        "unidad_productiva": { "id": 1, "nombre": "Campo Conejos" }
    }
]
```

#### Estrategia de Validación UX (App Móvil)
Antes de permitir enviar una nueva declaración, la App debe realizar la siguiente validación local:
1.  Obtener `StockTotal` actual (del endpoint `/stock`).
2.  Sumar la `cantidad` de todas las declaraciones en la lista anterior cuyo `estado` sea `"pendiente"` para esa misma categoría/raza.
3.  **Disponible = StockTotal - SumaPendientes**.
4.  Si `CantidadNueva > Disponible`, mostrar error al usuario y bloquear el envío.

---

### B. Crear Declaración de Venta
*   **Método:** `POST`
*   **Ruta:** `/api/movil/declaraciones-venta`

#### Cuerpo de la Petición (JSON)
```json
{
    "unidad_productiva_id": 1,
    "especie_id": 1,
    "raza_id": 1,
    "categoria_animal_id": 1,
    "cantidad": 5, // Entero > 0
    "observaciones": "Opcional"
}
```

#### Respuestas
*   **201 Created:** Venta registrada exitosamente.
*   **422 Unprocessable Content:** Error de validación (ej. Stock insuficiente).
    ```json
    {
        "message": "La cantidad a declarar (5) supera el stock disponible (4). Posee 5 y ya tiene 1 comprometidos en otras declaraciones.",
        "errors": {
            "cantidad": [...]
        }
    }
    ```

**Recomendación de Robustez:** Si el backend devuelve un 422 por stock insuficiente, se recomienda disparar automáticamente una sincronización en segundo plano de las declaraciones de venta (`GET`) para asegurar que la App tenga la información más reciente sobre lo que está "pendiente".

---

## 3. Actualización de Unidad Productiva (UP)

Permite editar detalles operativos de una UP. Los datos geográficos y legales críticos (nombre, ubicación) están protegidos.

*   **Método:** `PUT`
*   **Ruta:** `/api/movil/unidades-productivas/{id}`

#### Cuerpo de la Petición (JSON)
Campos opcionales (solo enviar lo que se modifica):
```json
{
    "superficie": 15.5,
    "condicion_tenencia_id": 2, // Actualiza tabla pivote
    "agua_animal_fuente_id": 2,
    "observaciones": "Nueva observación"
}
```

#### Respuesta (200 OK)
Devuelve el objeto UP actualizado. Nota: Los campos de la tabla pivote (como `condicion_tenencia_id`) se reflejan en las consultas de listado, no siempre en la respuesta inmediata de este endpoint.

---

## Estrategia de Sincronización Eficiente (Recomendación)

Para evitar el consumo excesivo de datos y batería:

1.  **Inicio App:** Llamar a `/init`.
2.  **Catálogos:** Comparar `configuration.catalogs_version` con la versión local. Si es diferente, llamar a `/catalogos`. Si es igual, usar caché local.
3.  **Datos Transaccionales (Stock/Ventas):** Usar un enfoque de "Delta Sync" o "Pull on Demand".
    *   Al entrar al módulo de ventas, descargar solo las declaraciones (`GET /declaraciones-venta`).
    *   Al entrar al módulo de stock, descargar el stock (`GET /stock`).
4.  **Recuperación de Errores:** Si una operación de escritura (`POST`/`PUT`) falla por inconsistencia de datos (409 Conflict o 422 relacionado con estado), invalidar la caché local de esa entidad y forzar una re-descarga fresca del backend.
