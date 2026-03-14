# API Móvil - Agenda Digital (V1)

Este documento detalla la funcionalidad de la Agenda Digital para los productores, permitiendo gestionar recordatorios, tareas y notas personales con notificaciones push sincronizadas.

---

## 1. Listar Ítems de la Agenda

Obtiene todos los recordatorios y tareas del productor autenticado, ordenados por fecha programada (ascendente).

-   **Endpoint:** `GET /api/movil/agenda`
-   **Autenticación:** Requiere Bearer Token.

### Respuesta Exitosa (200 OK)
Retorna un array de objetos `AgendaItem`.

```json
[
  {
    "id": 1,
    "user_id": 4,
    "titulo": "Vacunación Lote A",
    "descripcion": "Aplicar dosis de refuerzo.",
    "tipo": "sanidad",
    "fecha_programada": "2026-03-20T13:00:00.000000Z",
    "completada_en": null,
    "notificado": false,
    "created_at": "2026-03-12T20:20:14.000000Z",
    "updated_at": "2026-03-12T20:20:14.000000Z"
  }
]
```

---

## 2. Crear un Ítem en la Agenda

Permite al productor programar una nueva tarea o recordatorio.

-   **Endpoint:** `POST /api/movil/agenda`
-   **Método:** `POST`
-   **Payload (JSON):**

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `titulo` | `string` | Sí | Título breve del recordatorio (máx 255). |
| `descripcion` | `string` | No | Detalles adicionales de la tarea. |
| `tipo` | `string` | No | Categoría (ej: `sanidad`, `alimentacion`, `logistica`, `general`). |
| `fecha_programada` | `datetime` | Sí | Fecha y hora del recordatorio (formato ISO 8601). |

---

## 3. Actualizar o Completar un Ítem

Permite editar los datos de un ítem existente o marcarlo como completado/pendiente.

-   **Endpoint:** `PUT /api/movil/agenda/{id}`
-   **Método:** `PUT`
-   **Payload (JSON):** Todos los campos de creación son opcionales (`sometimes`). Se añade:

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `completada` | `boolean` | Si es `true`, el servidor establece `completada_en` al momento actual. Si es `false`, lo limpia (`null`). |

---

## 4. Eliminar un Ítem

Elimina permanentemente un registro de la agenda del productor.

-   **Endpoint:** `DELETE /api/movil/agenda/{id}`
-   **Método:** `DELETE`
-   **Respuesta:** `204 No Content` si es exitoso.

---

## 5. Notificaciones Push (Recordatorios)

El sistema envía automáticamente una notificación Push (FCM) cuando se alcanza la `fecha_programada` de un ítem que no ha sido completado ni notificado aún.

### Estructura del Payload de Datos (FCM Data)

```json
{
  "title": "⏰ Recordatorio: Vacunación Lote A",
  "body": "Aplicar dosis de refuerzo.",
  "type": "agenda_reminder",
  "agenda_item_id": "1",
  "notification_type_key": "agenda_reminder"
}
```

### Comportamiento de Notificación:
-   **Canales:** La notificación se envía por `fcm` (Push) y también queda registrada en la tabla de `notifications` (canal `database`) para consulta histórica en el panel de la app.
-   **Frecuencia:** El servidor chequea ítems pendientes cada minuto.
