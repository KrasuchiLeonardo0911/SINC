# Documentación API Móvil - Módulo Logística V2

**Versión:** 2.0
**Fecha:** 17/02/2026
**Impacto:** Medio (Cambio en flujo de estados y visualización)

---

## 1. Resumen del Cambio

El backend ha migrado a un sistema de **Ciclos Logísticos y Lotes**. Aunque la API móvil sigue operando sobre la entidad `DeclaracionVenta` (publicaciones), el comportamiento de los estados y la integridad de los datos ha cambiado.

### Principales Cambios para la App:
1.  **Nuevos Estados:** El flujo ahora es detallado (`recogido`, `en-matadero`, `entregado`).
2.  **Splits (Desdoblamiento):** Un rechazo parcial generará **dos registros** visibles para la app.
3.  **Lotes Implicitos:** Al crear una venta, el sistema la asigna automáticamente al ciclo de logística activo.

---

## 2. Flujo de Estados

La app debe estar preparada para recibir y mostrar los siguientes valores en el campo `estado`:

| Estado | Significado | Color Sugerido | Descripción |
| :--- | :--- | :--- | :--- |
| `comprometido` | **Confirmado** | 🟡 Amarillo | El productor ha declarado la venta. Esperando camión. |
| `recogido` | **En Tránsito** | 🔵 Azul | El camión ha pasado por el campo y cargó los animales. |
| `en-matadero` | **En Proceso** | 🟣 Violeta | Los animales llegaron a la planta frigorífica. |
| `entregado` | **Finalizado** | 🟢 Verde | Venta liquidada y stock descontado. Éxito. |
| `rechazado-carga` | **Rechazado** | 🔴 Rojo | El camionero no aceptó los animales en la puerta. |
| `matadero-rechazado` | **Rechazado** | 🔴 Rojo | Rechazo veterinario en planta. |
| `rechazado-final` | **Rechazado** | 🔴 Rojo | Cancelación administrativa. |
| `cancelado-regresando` | **Devuelto** | 🟠 Naranja | Cancelado después de salir (animales vuelven al campo). |

### Recomendación de UI:
Implementar un "Stepper" o línea de tiempo visual basado en estos estados.
1. Comprometido
2. Recogido
3. Matadero
4. Entregado

---

## 3. Lógica de Rechazos Parciales (Split)

**Escenario:**
El productor declara **10 Ovejas**.
El camión llega, pero **2 están enfermas** y son rechazadas. Las otras 8 suben.

**Comportamiento Anterior:**
El registro cambiaba de estado o se editaba la cantidad confusamente.

**Comportamiento V2 (Actual):**
La API `GET /declaraciones-venta` devolverá **dos registros distintos**:

1.  **Registro A (ID original):**
    *   Cantidad: **8**
    *   Estado: `recogido`
2.  **Registro B (Nuevo ID):**
    *   Cantidad: **2**
    *   Estado: `rechazado-carga`
    *   Motivo: "Animales enfermos"

**Implicación para la App:**
No deben asumir que el ID de una declaración es inmutable o único para una "intención de venta". La lista se refrescará con más filas. Es vital mostrar el campo `motivo_rechazo` si existe.

---

## 4. Endpoints

### Inicialización y Configuración
`GET /api/movil/init`

Ahora el nodo `configuration` incluye un objeto `logistics` con la programación actual:

```json
"configuration": {
    "catalogs_version": "...",
    "logistics": {
        "next_visit_date": "2026-02-20T12:00:00+00:00",
        "order_deadline": "2026-02-19T12:00:00+00:00",
        "frequency_days": 7,
        "is_open": true
    }
}
```

*   **`next_visit_date`**: Fecha y hora programada para la próxima pasada del camión.
*   **`order_deadline`**: Fecha límite calculada.
*   **`is_open`**: **(BOOLEAN) FUENTE DE VERDAD.** Indica si el sistema acepta inscripciones. Puede ser `false` incluso si no se ha cumplido la fecha límite (cierre manual).

### Recomendación de UX para la App:
1.  **Validación Principal**: Verificar `is_open`. Si es `false`, bloquear inscripciones inmediatamente mostrando "Ciclo Cerrado".
2.  **Validación Secundaria**: Si `is_open` es `true`, verificar `order_deadline` para mostrar cuentas regresivas.
3.  **Mensaje de Bloqueo**: "El periodo de inscripciones ha cerrado."

### Listar Ventas
`GET /api/movil/declaraciones-venta`

*   **Sin cambios en parámetros.**
*   **Respuesta:** Incluye los nuevos estados.
*   **Orden:** Se recomienda ordenar por `created_at` descendente.

### Crear Venta
`POST /api/movil/declaraciones-venta`

*   **Sin cambios.** El backend se encarga de buscar el ciclo activo y asignar el lote.
*   **Validación:** El sistema validará que el stock declarado no exceda el (Stock Real - Stock Comprometido).

### Cancelar Venta
`DELETE /api/movil/declaraciones-venta/{id}`

*   **Restricción:** Solo se puede cancelar si el estado es `comprometido`.
*   Si el camión ya pasó (`recogido`), el backend rechazará la cancelación con error 403 o 422. La app debe manejar este error mostrando "No se puede cancelar porque el camión ya recogió la carga".

---

## 5. FAQ Desarrolladores

**Q: ¿Necesito enviar el `historial_ciclo_id`?**
A: No. El backend lo infiere automáticamente.

**Q: ¿Qué pasa si no hay ciclo activo?**
A: El sistema creará uno automáticamente o asignará la venta a un lote "pendiente" hasta que se abra el ciclo. Para la app es transparente.

**Q: ¿Cómo calculo el stock disponible?**
A: Usar el endpoint `GET /api/movil/stock`. El backend ya descuenta lo "Comprometido". No intenten calcularlo localmente restando las ventas al total, confíen en el endpoint de stock.
