# Guía de Implementación: Sincronización Incremental (Delta Sync)

**Objetivo:** Optimizar el consumo de datos y batería de la aplicación móvil evitando descargas masivas y redundantes. Sincronizar solo lo que ha cambiado (creado, modificado o eliminado) desde la última conexión.

---

## 1. El Concepto: "Commits" de Datos

En lugar de que la App solicite "todo el historial" (snapshot completo), solicitará "los cambios ocurridos desde la fecha X".

*   **Full Sync:** Descargar 10,000 registros. (Lento, pesado).
*   **Delta Sync:** Descargar los 5 registros que cambiaron hoy. (Rápido, ligero).

---

## 2. Arquitectura Propuesta

### A. El Semáforo: Endpoint `/init`
El endpoint de inicialización debe actuar como un "descubridor de cambios". Antes de intentar descargar nada, la App consulta aquí para saber si *necesita* descargar algo.

**Respuesta Sugerida para `/init`:**
```json
{
    // ... config actual ...
    "sync_status": {
      
        "stock_last_update": "2026-01-15T22:30:00Z",
        "movements_last_update": "2026-01-16T09:00:00Z",
        "sales_last_update": "2026-01-14T10:00:00Z"
    }
}
```

**Lógica Cliente:**
1.  La App tiene guardado localmente: `mis_movimientos_actualizados_al: "2026-01-10..."`.
2.  Compara con `movements_last_update` del servidor.
3.  Si `Servidor > Local` -> Ejecutar sincronización.
4.  Si `Servidor <= Local` -> No hacer nada.

### B. El Filtro: Parámetro `after` (o `since`)
Los endpoints de listado (`GET`) deben aceptar un parámetro de fecha.

**Ejemplo de Petición:**
`GET /api/movil/cuaderno/movimientos?after=2026-01-10 14:00:00`

**Implementación Backend (Laravel):**
```php
public function index(Request $request)
{
    $query = Movimiento::where('productor_id', $user->productor_id);

    if ($request->has('after')) {
        // Filtrar por fecha de modificación (no de creación)
        $query->where('updated_at', '>', $request->input('after'));
        
        // CRÍTICO: Incluir eliminados para que la App sepa qué borrar
        $query->withTrashed(); 
    }

    return response()->json($query->get());
}
```

---

## 3. El Desafío de las Eliminaciones (Soft Deletes)

Si un registro se borra físicamente de la base de datos, el Delta Sync falla porque el registro "desaparece" y no entra en la consulta de cambios.

**Solución Obligatoria:** Usar `SoftDeletes` en los modelos sincronizables.

1.  El registro no se borra, solo se marca: `deleted_at = '2026-01-15...'`.
2.  Al actualizarse `deleted_at`, también se actualiza `updated_at`.
3.  La consulta `where('updated_at', '>', $lastSync)` traerá el registro borrado.
4.  **La App recibe el registro**, ve que tiene `deleted_at` no nulo, y procede a eliminarlo de su base de datos local (SQLite/Realm).

---

## 4. Estrategia de Implementación (Paso a Paso)

### Fase 1: Preparación Backend
1.  **Timestamps:** Asegurar que tablas críticas (`stock_animal`, `declaracion_ventas`, `unidades_productivas`) tengan `updated_at` indexado.
2.  **Soft Deletes:** Implementar el Trait `SoftDeletes` en los modelos correspondientes y crear migraciones para añadir la columna `deleted_at`.

### Fase 2: Modificación de Endpoints
1.  **Init:** Modificar `GetAppConfigAction` para realizar consultas de agregación ligeras (`max('updated_at')`) por cada módulo y devolverlas en el JSON.
2.  **Listados:** Modificar los Controllers/Actions de `index` para aplicar el filtro `when($request->after, ...)` e incluir `withTrashed()`.

### Fase 3: Lógica Cliente (App Móvil)
1.  **Almacenamiento:** Crear una tabla o preferencia para guardar `last_successful_sync_date` por cada módulo.
2.  **Lógica:**
    *   Llamar `/init`.
    *   Detectar desfase.
    *   Llamar `GET /endpoint?after=...`.
    *   Procesar respuesta:
        *   Si `deleted_at != null` -> Borrar local.
        *   Si ID existe -> Actualizar (Update).
        *   Si ID no existe -> Insertar (Create).
    *   Actualizar `last_successful_sync_date` con el `timestamp` actual del servidor (o el mayor `updated_at` recibido).

---

## 5. Resguardo (Safety Net)
A veces la sincronización incremental puede fallar o corromperse.
Se recomienda añadir un botón en la configuración de la App: **"Forzar Sincronización Completa"**.
Esto simplemente borra los datos locales y envía la petición sin el parámetro `after`, descargando todo de nuevo.
