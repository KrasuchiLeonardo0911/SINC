# API Móvil - Alertas Meteorológicas (V1)

Este documento detalla la funcionalidad relacionada con el Sistema de Alertas Tempranas (SAT) Meteorológicas para los productores a través de la API móvil.

---

## 1. Obtener Alertas Activas para el Productor

Este endpoint permite a la aplicación móvil consultar en tiempo real las alertas meteorológicas vigentes que afectan a las Unidades Productivas (UPs) del productor autenticado.

- **Endpoint:** `GET /api/movil/alertas-meteorologicas`
- **Autenticación:** Requiere Bearer Token.
- **Método:** `GET`

### Respuesta Exitosa (200 OK)

La respuesta es un objeto JSON que incluye un array con todas las alertas vigentes que se cruzan con las ubicaciones de las UPs del productor. Si no hay alertas, el array `alertas` estará vacío.

**Ejemplo de Respuesta:**

```json
{
  "success": true,
  "alertas": [
    {
      "up_id": 1,
      "up_nombre": "chacra pollos",
      "municipio": "Oberá",
      "evento": "GRANIZO FUERTE (PRUEBA)",
      "nivel": "rojo",
      "inicio": "2026-02-24T18:47:51.000000Z",
      "fin": "2026-02-25T18:47:51.000000Z",
      "descripcion": "Esto es una prueba de API para el municipio 15"
    }
  ],
  "count": 1
}
```

### Campos del Objeto Alerta:

| Campo         | Tipo     | Descripción                                                                 |
|---------------|----------|-----------------------------------------------------------------------------|
| `up_id`       | `integer`| ID de la Unidad Productiva del productor afectada por la alerta.              |
| `up_nombre`   | `string` | Nombre de la UP para fácil identificación.                                  |
| `municipio`   | `string` | Nombre del municipio donde se ubica la UP y donde aplica la alerta.           |
| `evento`      | `string` | Tipo de fenómeno meteorológico (ej: "Tormentas fuertes", "Vientos", "Granizo"). |
| `nivel`       | `string` | Nivel de severidad de la alerta: `amarillo`, `naranja`, `rojo`.               |
| `inicio`      | `string` | Fecha y hora (ISO 8601 UTC) de inicio de vigencia de la alerta.               |
| `fin`         | `string` | Fecha y hora (ISO 8601 UTC) de fin de vigencia de la alerta.                  |
| `descripcion` | `string` | Texto descriptivo detallado de la alerta proporcionado por el SMN.            |


---

## 2. Notificación Push de Nueva Alerta

Cuando el sistema detecta una nueva alerta que afecta a un productor, o cuando una alerta existente cambia de nivel de severidad, se envía una notificación Push (FCM).

### Estructura del Payload de Datos (FCM Data)

La aplicación recibirá un mensaje de **solo datos** (`data-only`). La app es responsable de construir y mostrar la notificación local si está en primer plano.

**Ejemplo de Payload `data`:**

```json
{
  "title": "⚠️ Alerta Meteorológica: GRANIZO FUERTE (PRUEBA)",
  "body": "Se ha detectado una alerta nivel rojo en su zona.",
  "type": "weather_alert",
  "alerta_id": "35",
  "nivel": "rojo",
  "notification_type_key": "weather_alert_new"
}
```

### Claves del Payload de Datos:

| Clave                     | Descripción                                                                         |
|---------------------------|-------------------------------------------------------------------------------------|
| `title`                   | Título que debe mostrar la notificación.                                            |
| `body`                    | Cuerpo del mensaje de la notificación.                                              |
| `type`                    | Identificador (`weather_alert`) para que la app sepa qué tipo de notificación es.   |
| `alerta_id`               | ID de la alerta en la base de datos (puede ser útil para futuras consultas).        |
| `nivel`                   | Nivel de severidad (`amarillo`, `naranja`, `rojo`) para styling en la app.          |
| `notification_type_key`   | Clave (`weather_alert_new`) usada por la app para procesar el panel local.          |
