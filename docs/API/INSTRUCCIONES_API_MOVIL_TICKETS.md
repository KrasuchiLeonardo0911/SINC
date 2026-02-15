# Instrucciones para la API Móvil: Creación de Solicitudes de Soporte

Este documento detalla cómo utilizar el nuevo endpoint para que un productor autenticado pueda crear una nueva solicitud de soporte (ticket) desde la aplicación móvil.

## Endpoint de Creación

-   **URL:** `/api/movil/solicitudes`
-   **Método:** `POST`
-   **Autenticación:** **Requerida**. La petición debe incluir el token de autenticación de Sanctum en la cabecera `Authorization`.
    ```
    Authorization: Bearer <token>
    ```

## Cuerpo de la Petición (Request Body)

La petición debe enviar un objeto JSON con el siguiente campo:

-   `mensaje` (string, **requerido**): El texto de la consulta, duda o sugerencia del productor.

#### Ejemplo de Body

```json
{
    "mensaje": "Hola, tengo una duda sobre cómo registrar una venta en el cuaderno de campo. ¿Me podrían ayudar?"
}
```

**Nota:** El tipo de solicitud (`tipo`) se asigna automáticamente en el backend como `'consulta_general'`. La aplicación móvil no necesita enviar este campo.

## Respuestas del Servidor

### Respuesta Exitosa (Código `201 Created`)

Si la solicitud se crea correctamente, el servidor responderá con un código `201` y un objeto JSON que contiene un mensaje de confirmación y el ID del ticket recién creado.

#### Ejemplo de Respuesta Exitosa

```json
{
    "message": "Tu consulta ha sido enviada con éxito. Un administrador la revisará a la brevedad.",
    "ticket_id": 123
}
```

### Respuestas de Error

-   **Código `422 Unprocessable Entity`:** Ocurrirá si hay errores de validación (ej. el campo `mensaje` está vacío). El cuerpo de la respuesta contendrá los detalles de los errores.
    ```json
    {
        "message": "The given data was invalid.",
        "errors": {
            "mensaje": [
                "El campo mensaje es obligatorio."
            ]
        }
    }
    ```
-   **Código `401 Unauthorized`:** Si el token de autenticación no es válido o no se proporciona.
-   **Código `500 Internal Server Error`:** Si ocurre un error inesperado en el servidor.

##PARA SOLICITUDES RNSPA: tipo === 'solicitud_rnspa'