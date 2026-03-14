# Especificación Técnica: Módulo Agenda Digital (API Móvil)

**Objetivo:** Implementar un CRUD sencillo para una agenda personal del productor, permitiendo la programación de tareas con notificaciones push.

## 1. Base de Datos (Tabla: `agenda_items`)
Se requiere una migración con los siguientes campos:

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | BigInt (PK) | Identificador único. |
| `user_id` | BigInt (FK) | Relación con la tabla `users`. |
| `titulo` | String(255) | Título de la nota/recordatorio. |
| `descripcion` | Text (Nullable) | Detalles adicionales. |
| `tipo` | String(50) | Categoría (Default: 'general'). |
| `fecha_programada` | DateTime | Cuándo debe ocurrir el evento/recordatorio. |
| `completada_en` | DateTime (Null) | Fecha en que se marcó como realizada. |
| `notificado` | Boolean (Default: 0) | Flag para control de envío de Push Notification. |
| `timestamps` | - | `created_at` y `updated_at`. |

## 2. Endpoints de la API (Sugerencia: `/api/movil/agenda`)
Todos los endpoints deben estar protegidos por el middleware de **Sanctum** y filtrar los datos por el `user_id` autenticado.

*   **GET `/`**: Listar todos los items del usuario (opcional: filtrar por rango de fechas).
*   **POST `/`**: Crear un nuevo item.
    *   *Payload:* `{ "titulo", "descripcion"?, "fecha_programada", "tipo"? }`
*   **PUT `/{id}`**: Actualizar un item o marcar como completado.
    *   *Payload:* `{ "titulo"?, "descripcion"?, "fecha_programada"?, "tipo"?, "completada" (bool)? }`
*   **DELETE `/{id}`**: Eliminar un item.

## 3. Lógica de Notificaciones Push (FCM)
Para que la agenda funcione proactivamente, se sugiere un comando de Artisan que corra cada minuto (en el `Schedule`):

1.  Buscar registros donde `fecha_programada` sea `<= NOW()`.
2.  Que tengan `notificado = 0` y `completada_en` sea `NULL`.
3.  Enviar la notificación vía Firebase (FCM) al usuario (usando su `fcm_token` registrado).
4.  Marcar `notificado = 1`.

## 4. Formato de Respuesta Sugerido (JSON)
```json
{
    "id": 1,
    "titulo": "Vacunación Lote A",
    "descripcion": "Aplicar dosis de refuerzo.",
    "tipo": "sanidad",
    "fecha_programada": "2026-03-15 09:00:00",
    "completada": false,
    "created_at": "2026-03-12 10:00:00"
}
```
