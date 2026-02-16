# Especificaciones API Móvil - Sistema de Gestión Ovino-Caprino

**Versión del Documento:** 1.1 (Actualizado con Ciclo Logístico Robusto)
**Fecha:** 15/02/2026

## 1. Consideraciones Generales

*   **Base URL:** `https://sicsurmisiones.online/api` (Producción) / `http://127.0.0.1:8000/api` (Local)
*   **Autenticación:** Todos los endpoints (excepto Login) requieren el header `Authorization: Bearer {token}`.
*   **Headers Requeridos:**
    *   `Accept: application/json`
    *   `Content-Type: application/json`

---

## 2. Autenticación

### Login
*   **Endpoint:** `POST /movil/login`
*   **Body:**
    ```json
    {
        "email": "productor@test.com",
        "password": "password",
        "device_name": "Samsung S21"
    }
    ```
*   **Respuesta Exitosa (200):**
    ```json
    {
        "token": "1|MqXz3u6p9EUwYt69AMCqtGrM9MDqL4ZzP6rr3pFGeb30002f",
        "user": { ... }
    }
    ```

---

## 3. Módulo de Ventas y Logística

Este módulo gestiona la declaración de intención de venta por parte del productor y el seguimiento del ciclo logístico.

### Ciclo de Vida (Estados)
La app móvil debe mapear el campo `estado` para mostrar el progreso (Stepper/Timeline):

1.  **`comprometido`** (Inicio): El productor declaró la venta. Esperando ruta. (Estado inicial).
2.  **`recogido`** (Transporte): El camión pasó y cargó los animales.
3.  **`en-matadero`** (Procesamiento): Los animales ingresaron a planta.
4.  **`entregado`** (Final): Venta confirmada, cobrada y stock descontado. **(Éxito)**

**Estados de Error/Cancelación:**
*   `rechazado-carga`: El camión no cargó los animales (ej. problema de papeles/sanidad).
*   `matadero-rechazado`: Rechazo sanitario en planta.
*   `rechazado-final`: Rechazo administrativo.
*   `cancelado-regresando`: Cancelación general.

---

### A. Listar Declaraciones (Historial y Seguimiento)
Obtiene todas las declaraciones históricas y activas del productor.

*   **Endpoint:** `GET /movil/declaraciones-venta`
*   **Respuesta Exitosa (200):**
    ```json
    [
        {
            "id": 10,
            "cantidad": 5,
            "estado": "recogido", // Usar este campo para el UI del Stepper
            "fecha_declaracion": "2026-02-15T10:00:00.000000Z",
            "fecha_recogida": "2026-02-15T14:30:00.000000Z", // Si no es null, mostrar fecha
            "fecha_matadero": null,
            "fecha_entrega": null,
            "motivo_rechazo": null, // Si tiene valor, mostrar alerta roja
            "especie": { "nombre": "Ovino" },
            "raza": { "nombre": "Texel" },
            "categoria_animal": { "nombre": "Cordero" },
            "unidad_productiva": { "nombre": "La Estancia" }
        }
    ]
    ```

### B. Crear Declaración de Venta
*   **Endpoint:** `POST /movil/declaraciones-venta`
*   **Regla de Negocio:** El sistema validará automáticamente si el productor tiene stock disponible (`Stock Real` - `Ventas Activas`). Si no alcanza, devuelve error 422.
*   **Body:**
    ```json
    {
        "unidad_productiva_id": 1,
        "especie_id": 1,
        "raza_id": 6,
        "categoria_animal_id": 1,
        "cantidad": 10,
        "peso_aproximado_kg": 450, // Opcional
        "observaciones": "Retirar por la tarde" // Opcional
    }
    ```
*   **Respuesta Exitosa (201):**
    ```json
    {
        "message": "Declaración de venta registrada exitosamente.",
        "data": { "id": 11, "estado": "comprometido", ... }
    }
    ```
*   **Error de Validación (422):** (Stock insuficiente)
    ```json
    {
        "message": "El campo cantidad supera el stock disponible (2).",
        "errors": { "cantidad": [...] }
    }
    ```

### C. Cancelar Declaración
El productor puede cancelar una venta **SOLO** si el estado es `comprometido`. Una vez que el camión recogió la carga (`recogido`), ya no se puede cancelar desde la app.

*   **Endpoint:** `DELETE /movil/declaraciones-venta/{id}`
*   **Respuesta Exitosa (200):**
    ```json
    { "message": "Declaración cancelada exitosamente." }
    ```
*   **Error de Lógica (422):** (Si intenta borrar algo ya recogido)
    ```json
    { "message": "Solo se pueden cancelar declaraciones que aún no han sido recogidas (estado comprometido)." }
    ```

### D. Verificar Stock Disponible (Helper)
Utilidad para consultar cuánto stock "libre" tiene el productor antes de intentar vender.

*   **Endpoint:** `GET /productor/stock-disponible-venta`
*   **Query Params:** `?unidad_productiva_id=1&especie_id=1&raza_id=6&categoria_animal_id=1`
*   **Respuesta Exitosa (200):**
    ```json
    {
        "stock_disponible": 15
    }
    ```

---

## 4. Catálogos (Datos Maestros)
Para llenar los selectores de los formularios.

*   **Endpoint:** `GET /movil/catalogos`
*   **Respuesta:** Devuelve arrays de `especies`, `razas`, `categorias`, `unidades_productivas` (del productor), etc.
