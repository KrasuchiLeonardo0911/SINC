# Documentación API: Bitácora Diaria (v1)

Esta documentación detalla los endpoints necesarios para implementar la funcionalidad de Bitácora Diaria en la aplicación Android SINC.

## Información General
*   **Base URL:** http://[server-url]/api/movil/bitacoras-diarias
*   **Autenticación:** Requerida (Bearer Token / Sanctum).
*   **Headers Obligatorios:** 
    *   Accept: application/json
    *   Content-Type: application/json
    *   Authorization: Bearer {token}

## Endpoints

### 1. Listar Bitácoras (GET)
Obtiene el historial de registros del usuario autenticado.
*   **URL:** GET /
*   **Parámetros Opcionales (Query):**
    *   echa: Filtra por una fecha específica (Formato: YYYY-MM-DD).
*   **Respuesta Exitosa (200 OK):**
    `json
    [
      {
        "id": 1,
        "user_id": 4,
        "fecha": "2026-03-13 00:00:00",
        "contenido": "Texto de la bitácora...",
        "created_at": "2026-03-13 18:42:11",
        "updated_at": "2026-03-13 18:42:11"
      }
    ]
    `

### 2. Crear Registro (POST)
Crea una nueva entrada en la bitácora.
*   **URL:** POST /
*   **Cuerpo (JSON):**
    `json
    {
      "fecha": "2026-03-13",
      "contenido": "Descripción de la actividad realizada."
    }
    `
*   **Validaciones:**
    *   echa: Requerido, formato fecha.
    *   contenido: Requerido, string, máximo 1000 caracteres.
*   **Respuesta Exitosa (201 Created):** Devuelve el objeto creado.

### 3. Ver Detalle (GET)
Obtiene un registro específico.
*   **URL:** GET /{id}
*   **Respuesta:** Objeto JSON de la bitácora. (403 si no pertenece al usuario).

### 4. Actualizar Registro (PUT/PATCH)
Edita una entrada existente.
*   **URL:** PUT /{id}
*   **Cuerpo (JSON):** Se pueden enviar uno o ambos campos.
    `json
    {
      "contenido": "Contenido actualizado"
    }
    `
*   **Respuesta Exitosa (200 OK):** Objeto actualizado.

### 5. Eliminar Registro (DELETE)
Borra una entrada de la bitácora.
*   **URL:** DELETE /{id}
*   **Respuesta Exitosa (204 No Content).**

## Notas de Implementación (Android)
1.  **Formato de Fecha:** El servidor devuelve la fecha con hora (YYYY-MM-DD 00:00:00), se recomienda parsear solo la parte de la fecha para la UI.
2.  **Seguridad:** El servidor bloquea automáticamente cualquier intento de acceder a un id que no pertenezca al usuario autenticado (403 Forbidden).

## Detalles de Implementación y Estructura (Android)

La implementación sigue los principios de **Clean Architecture** y el patrón **Offline-First**.

### 1. Estructura de Datos Local (Room)
*   **Tabla:** `bitacoras`
*   **Clave Primaria:** `id` (proporcionado por el servidor).
*   **Campos:** `id`, `userId`, `fecha` (LocalDateTime), `contenido`, `createdAt`, `updatedAt`.
*   **DAO:** `BitacoraDao` incluye una transacción `clearAndInsert` para asegurar la consistencia de la caché local tras la sincronización.

### 2. Flujo de Usuario (UI/UX)
La interfaz se ha diseñado para ser minimalista y eficiente en el campo:
*   **Listado Principal:** Agrupado por mes y año. Utiliza un selector de fecha horizontal para la navegación temporal.
*   **Asistente de Registro (Stepper):**
    *   **Paso 1:** Selección de fecha mediante un calendario personalizado (unificado con el módulo de Agenda).
    *   **Paso 2:** Entrada de texto libre (máx. 1000 caracteres) con guardado atómico.
*   **Feedback Visual:** Pantalla de carga dedicada durante el proceso de guardado con alternancia de mensajes para mejorar la percepción de respuesta del sistema.

### 3. Estrategia de Sincronización
*   **Lectura:** Se realiza una sincronización completa (`GET /`) al entrar al módulo si es la carga inicial, actualizando la base de datos local (SSOT).
*   **Escritura:** Cada creación o modificación se envía inmediatamente al servidor. Si tiene éxito, se actualiza la copia local para permitir el uso sin conexión inmediato de ese registro.

### 4. Componentes Clave
*   **ViewModel:** `BitacoraViewModel` gestiona el estado complejo del flujo (ordinales de vista) y la lógica de filtrado por mes.
*   **Transiciones:** Uso de `AnimatedContent` con `slideInHorizontally` para emular una navegación de asistente nativo entre pasos.

