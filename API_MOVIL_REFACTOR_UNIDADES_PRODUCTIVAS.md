# Documento para Equipo Móvil: Refactorización del Endpoint de Unidades Productivas (UP)

Este documento detalla los cambios implementados en el backend para la gestión de Unidades Productivas (UPs) y su impacto en la API móvil. Esta refactorización permite a las UPs tener múltiples tipos de suelo y recursos forrajeros, cada uno con un porcentaje asociado.

---

## 1. Visión General de la Refactorización

La principal motivación de esta refactorización fue pasar de un modelo de "tipo de suelo predominante" y "tipo de pasto predominante" (que solo permitía un elemento) a un modelo de relaciones **muchos a muchos** para **Tipos de Suelo** y **Recursos Forrajeros**, permitiendo asociar múltiples elementos con porcentajes específicos a cada Unidad Productiva.

---

## 2. Endpoint GET `/api/movil/unidades-productivas`

El endpoint para obtener las Unidades Productivas de un productor autenticado (`GET /api/movil/unidades-productivas`) ha sido actualizado para incluir las nuevas relaciones.

**Estructura de la Respuesta JSON:**

Cada objeto de `UnidadProductiva` en la respuesta ahora incluirá dos nuevos arrays: `tipos_suelo` y `recursos_forrajeros`.

```json
[
  {
    "id": 1,
    "nombre": "Nombre de la UP",
    "identificador_local": "XX.XXX.X.XXXXX/XX",
    // ... otros campos de la Unidad Productiva ...
    "pivot": {
      "productor_id": 1,
      "unidad_productiva_id": 1,
      "condicion_tenencia_id": 1,
      "fecha_inicio": "YYYY-MM-DD",
      "fecha_fin": null
    },
    "tipos_suelo": [
      {
        "id": 1,
        "nombre": "Arenoso",
        "pivot": {
          "unidad_productiva_id": 1,
          "tipo_suelo_id": 1,
          "porcentaje": 60
        }
      },
      {
        "id": 2,
        "nombre": "Arcilloso",
        "pivot": {
          "unidad_productiva_id": 1,
          "tipo_suelo_id": 2,
          "porcentaje": 40
        }
      }
    ],
    "recursos_forrajeros": [
      {
        "id": 1,
        "nombre": "Pastizal natural",
        "pivot": {
          "unidad_productiva_id": 1,
          "tipo_pasto_id": 1,
          "porcentaje": 70
        }
      },
      {
        "id": 4,
        "nombre": "Rastrojo",
        "pivot": {
          "unidad_productiva_id": 1,
          "tipo_pasto_id": 4,
          "porcentaje": 30
        }
      }
    ]
  }
  // ... más Unidades Productivas
]
```

-   **`tipos_suelo`**: Un array de objetos que representan los tipos de suelo asociados a la UP.
    -   Cada objeto incluye `id`, `nombre` (del catálogo de `TipoSuelo`) y un objeto `pivot` con `porcentaje`.
-   **`recursos_forrajeros`**: Un array de objetos que representan los recursos forrajeros (antiguos "tipos de pasto") asociados a la UP.
    -   Cada objeto incluye `id`, `nombre` (del catálogo de `TipoPasto`) y un objeto `pivot` con `porcentaje`.

---

## 3. Endpoints POST/PUT `/api/movil/unidades-productivas`

Los endpoints para crear (`POST /api/movil/unidades-productivas`) y actualizar (`PUT /api/movil/unidades-productivas/{id}`) una Unidad Productiva ahora esperan la nueva estructura de datos para `tipos_suelo` y `recursos_forrajeros` en el cuerpo de la solicitud JSON.

**Ejemplo de Payload JSON (para POST o PUT):**

```json
{
  "nombre": "Mi Finca Actualizada",
  "identificador_local": "12.345.6.78901/23",
  "superficie": 180.0,
  "latitud": -27.3672,
  "longitud": -55.8966,
  "municipio_id": 1,
  "paraje_id": 1,
  "condicion_tenencia_id": 1,
  "fecha_inicio": "2023-01-01",
  "fuente_agua_id": null,
  "observaciones": "Nuevas observaciones aquí.",
  "tipos_suelo": [
    {
      "tipo_suelo_id": 1,
      "porcentaje": 70
    },
    {
      "tipo_suelo_id": 2,
      "porcentaje": 30
    }
  ],
  "recursos_forrajeros": [
    {
      "tipo_pasto_id": 1,
      "porcentaje": 50
    },
    {
      "tipo_pasto_id": 4,
      "porcentaje": 50
    }
  ]
}
```

**Reglas de Validación Importantes:**

-   **`tipos_suelo`**:
    -   Debe ser un array de hasta 3 objetos.
    -   Cada objeto debe contener `tipo_suelo_id` (existente en el catálogo de tipos de suelo) y `porcentaje` (numérico entre 0 y 100).
    -   La suma de los `porcentaje` de todos los `tipos_suelo` DEBE ser exactamente 100 (solo para `POST` si la regla `required` está activa, para `PUT` solo si se envía `tipos_suelo`).
-   **`recursos_forrajeros`**:
    -   Debe ser un array de hasta 4 objetos.
    -   Cada objeto debe contener `tipo_pasto_id` (existente en el catálogo de tipos de pasto) y `porcentaje` (opcional, numérico entre 0 y 100).
    -   Si se especifican porcentajes, la suma DEBE ser 100.
-   **Autorización (PUT):** El usuario autenticado SÓLO puede actualizar Unidades Productivas de su propiedad. Intentar actualizar una UP ajena resultará en una respuesta `403 Forbidden`.

---

## 4. Endpoints de Catálogos

-   El endpoint `GET /api/movil/catalogos` ya incluye y seguirá incluyendo los catálogos `tipos_suelo` y `tipos_pasto` (que corresponden a los `recursos_forrajeros`).
-   La aplicación móvil debe utilizar estos catálogos para obtener los nombres asociados a los IDs de los suelos y recursos forrajeros recibidos en los detalles de la Unidad Productiva.

---

## 5. Endpoint de Inicialización (`/api/movil/init`)

-   La acción `GetAppConfigAction` ahora incluye `TipoSuelo` y `TipoPasto` en el cálculo de la `catalogs_version`.
-   Esto significa que si hay una actualización en los catálogos de Tipos de Suelo o Tipos de Pasto, la `catalogs_version` cambiará, indicando a la aplicación móvil que debe sincronizar nuevamente sus catálogos.

---

## 6. Modelos y Relaciones Clave

-   **`UnidadProductiva` Model:**
    -   Se eliminaron las columnas `tipo_suelo_predominante_id`, `tipo_pasto_predominante_id` y `forrajeras_predominante` de la tabla `unidades_productivas`.
    -   Se añadieron nuevas relaciones muchos a muchos:
        -   `tiposSuelo()`: Relaciona `UnidadProductiva` con `TipoSuelo` a través de la tabla pivote `unidad_productiva_tipo_suelo`, incluyendo el campo `porcentaje`.
        -   `recursosForrajeros()`: Relaciona `UnidadProductiva` con `TipoPasto` a través de la tabla pivote `recursos_forrajeros`, incluyendo el campo `porcentaje`.

---

**Acciones para el Equipo Móvil:**

-   Actualizar los modelos de datos en la aplicación para reflejar la nueva estructura de arrays para suelos y forrajes en los objetos de Unidad Productiva.
-   Adaptar los formularios de creación y edición de Unidades Productivas para enviar la nueva estructura de datos al backend.
-   Implementar la lógica para re-sincronizar los catálogos de suelos y forrajes cuando la `catalogs_version` en el endpoint `/init` cambie.
-   Asegurarse de manejar la respuesta `403 Forbidden` al intentar actualizar UPs no propias.
