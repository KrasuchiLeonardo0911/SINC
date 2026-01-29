# API Móvil: Sistema de Tickets (Consultas)

Este documento describe los endpoints de la API móvil para gestionar el sistema de tickets de soporte, que funciona como un chat conversacional.

## Flujo General

El sistema permite a un usuario autenticado (productor) crear tickets, ver sus tickets existentes y mantener una conversación (múltiples mensajes de ida y vuelta) con el equipo de administración o soporte.

1.  **Autenticación**: Todos los endpoints requieren un token de autenticación de Sanctum enviado en el header `Authorization`.
2.  **Creación**: El usuario crea un ticket con un mensaje inicial.
3.  **Conversación**: El usuario puede añadir más mensajes al ticket. El equipo de soporte/admin es notificado y puede responder, añadiendo también mensajes a la conversación.
4.  **Sincronización**: La API está diseñada para ser eficiente. Cada vez que se crea un ticket o se añade un mensaje, la respuesta de la API devuelve el objeto del ticket completo y actualizado. **La aplicación móvil no necesita hacer una segunda llamada para volver a descargar los datos**.

---

## Endpoints

### 1. Listar Tickets del Usuario

Obtiene una lista de todos los tickets pertenecientes al usuario autenticado. Cada ticket en la respuesta incluye su historial completo de mensajes.

*   **Método:** `GET`
*   **URL:** `/api/movil/tickets`
*   **Headers:**
    *   `Authorization`: `Bearer <token>`
    *   `Accept`: `application/json`

*   **Respuesta de Éxito (200 OK):**
    Un array de objetos `Ticket`.

    ```json
    [
        {
            "id": 1,
            "user_id": 4,
            "tipo": "problema_tecnico",
            "status": "en_progreso",
            "mensaje": "La app no funciona en mi nuevo celular.",
            "responder_id": 1,
            "created_at": "2026-01-25T10:00:00.000000Z",
            "updated_at": "2026-01-25T10:05:00.000000Z",
            "messages": [
                {
                    "id": 1,
                    "user_id": 4,
                    "message": "La app no funciona en mi nuevo celular.",
                    "created_at": "2026-01-25T10:00:00.000000Z",
                    "user": { "id": 4, "name": "Nombre del Productor" }
                },
                {
                    "id": 2,
                    "user_id": 1,
                    "message": "Gracias por reportarlo. ¿Podría indicarnos el modelo de su celular?",
                    "created_at": "2026-01-25T10:05:00.000000Z",
                    "user": { "id": 1, "name": "Soporte Técnico" }
                }
            ],
            "responder": { "id": 1, "name": "Soporte Técnico" }
        }
    ]
    ```

---

### 2. Crear un Nuevo Ticket

Crea un nuevo ticket de consulta. El primer mensaje de la conversación se crea automáticamente.

*   **Método:** `POST`
*   **URL:** `/api/movil/tickets`
*   **Headers:**
    *   `Authorization`: `Bearer <token>`
    *   `Accept`: `application/json`
    *   `Content-Type`: `application/json`

*   **Cuerpo de la Petición (JSON):**
    ```json
    {
        "mensaje": "Mi consulta es sobre el próximo ciclo de logística.",
        "tipo_solicitud": "consulta_negocio"
    }
    ```
    *   `mensaje` (string, requerido): El contenido del primer mensaje.
    *   `tipo_solicitud` (string, opcional): El tipo de consulta. Si se omite, por defecto será `consulta_general`. Valores posibles: `problema_tecnico`, `problema_cuenta`, `sugerencia`, `consulta_negocio`, etc.

*   **Respuesta de Éxito (201 Created):**
    Devuelve el objeto `Ticket` recién creado, con su ID asignado y el primer mensaje.

    ```json
    {
        "message": "Tu consulta ha sido enviada con éxito...",
        "ticket": {
            "id": 2,
            "user_id": 4,
            "tipo": "consulta_negocio",
            "status": "abierto",
            "mensaje": "Mi consulta es sobre el próximo ciclo de logística.",
            "created_at": "2026-01-25T11:00:00.000000Z",
            "updated_at": "2026-01-25T11:00:00.000000Z",
            "messages": [
                {
                    "id": 3,
                    "user_id": 4,
                    "message": "Mi consulta es sobre el próximo ciclo de logística.",
                    "created_at": "2026-01-25T11:00:00.000000Z",
                    "user": { "id": 4, "name": "Nombre del Productor" }
                }
            ]
        }
    }
    ```
*   **Respuesta de Error (422 Unprocessable Entity):**
    Si la validación falla (ej. `mensaje` está vacío).

---

### 3. Añadir un Mensaje a un Ticket Existente

Permite al usuario continuar la conversación en uno de sus tickets.

*   **Método:** `POST`
*   **URL:** `/api/movil/tickets/{ticket_id}/messages`
    *(Reemplazar `{ticket_id}` con el ID del ticket)*.
*   **Headers:**
    *   `Authorization`: `Bearer <token>`
    *   `Accept`: `application/json`
    *   `Content-Type`: `application/json`

*   **Cuerpo de la Petición (JSON):**
    ```json
    {
        "message": "Gracias por la respuesta. El modelo es un Samsung S25."
    }
    ```
    *   `message` (string, requerido): El contenido del nuevo mensaje.

*   **Respuesta de Éxito (200 OK):**
    Devuelve el objeto `Ticket` completo y actualizado, con el nuevo mensaje añadido al array `messages`.

    ```json
    {
        "id": 1,
        "status": "esperando_respuesta",
        "messages": [
            { "id": 1, "message": "La app no funciona...", ... },
            { "id": 2, "message": "Gracias por reportarlo...", ... },
            { "id": 4, "message": "Gracias por la respuesta. El modelo es un Samsung S25.", ... }
        ],
        ...
    }
    ```
*   **Respuesta de Error (403 Forbidden):**
    Si el usuario intenta añadir un mensaje a un ticket que no le pertenece.

---

### 4. Obtener un Ticket Específico

Aunque `GET /api/movil/tickets` ya devuelve los mensajes, este endpoint se puede usar para refrescar un solo ticket si fuera necesario.

*   **Método:** `GET`
*   **URL:** `/api/movil/tickets/{ticket_id}`
*   **Headers:**
    *   `Authorization`: `Bearer <token>`
    *   `Accept`: `application/json`

*   **Respuesta de Éxito (200 OK):**
    Devuelve el objeto `Ticket` completo con su array `messages`.

---

### 5. Marcar un Ticket como Resuelto

Permite al usuario cerrar su propia consulta si considera que ha sido resuelta.

*   **Método:** `POST`
*   **URL:** `/api/movil/tickets/{ticket_id}/resolve`
*   **Headers:**
    *   `Authorization`: `Bearer <token>`
    *   `Accept`: `application/json`

*   **Respuesta de Éxito (200 OK):**
    Devuelve el objeto `Ticket` actualizado, con el `status` cambiado a `resuelto`.