# Guía de Implementación Offline-First (SINCMOBILE)

Esta guía define el estándar técnico para asegurar que la aplicación sea plenamente funcional sin conexión a internet, garantizando una experiencia de usuario instantánea y sin pérdida de datos.

---

## 1. Filosofía de Diseño
El productor rural trabaja en entornos de baja señal. La aplicación **nunca** debe bloquear la interfaz esperando una respuesta de red ni mostrar mensajes de error de conexión al intentar guardar datos. 

**Flujo Maestro:** UI ↔ ViewModel ↔ Room (Fuente Única de Verdad) ↔ Repositorio ↔ API.

---

## 2. Estructura de Datos (Capa de Datos)

### A. La Entidad (Room Entity)
Cada tabla debe manejar una "Doble Identidad" para permitir datos locales antes de ser asignados por el servidor.

```kotlin
@Entity(tableName = "ejemplo_items")
data class EjemploEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0, // Clave Primaria REAL en el móvil
    val id: Long? = null, // ID que asignará el servidor (opcional hasta sincronizar)
    val sincronizado: Boolean = false, // Flag de control
    // ... campos de datos
)
```

### B. El DAO
Debe incluir soporte para gestionar la cola de sincronización.

```kotlin
@Dao
interface EjemploDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: EjemploEntity): Long // Devuelve el localId generado

    @Query("UPDATE ejemplo_items SET id = :serverId, sincronizado = 1 WHERE localId = :localId")
    suspend fun markAsSynced(localId: Long, serverId: Long)

    @Query("SELECT * FROM ejemplo_items WHERE sincronizado = 0")
    suspend fun getUnsyncedItems(): List<EjemploEntity>
    
    @Query("DELETE FROM ejemplo_items WHERE sincronizado = 1")
    suspend fun deleteSyncedItems()
}
```

---

## 3. Modelo de Negocio (Capa de Dominio)

El dominio no debe preocuparse por la complejidad de los IDs de base de datos. Usaremos el `localId` como el `id` principal del modelo.

```kotlin
data class Ejemplo(
    val id: Long, // Aquí mapeamos el localId de la entidad
    val isSynced: Boolean, // Para mostrar estados visuales en la UI
    // ... datos
)
```

---

## 4. El Repositorio: Guardado Optimista

El repositorio es el cerebro del flujo offline. Debe responder instantáneamente.

1.  **Inserción Local:** Guardar en Room inmediatamente con `sincronizado = false`.
2.  **Retorno Inmediato:** Devolver el objeto guardado (con su `localId`) al ViewModel sin esperar a la red.
3.  **Sincronización Silenciosa:** Lanzar la petición API en un `CoroutineScope` externo que no bloquee el hilo de la UI.

```kotlin
override suspend fun saveItem(item: Ejemplo): Result<Ejemplo, Error> {
    return try {
        // 1. Guardado en local
        val entity = item.toEntity().copy(sincronizado = false)
        val generatedLocalId = dao.insert(entity)
        val savedItem = entity.copy(localId = generatedLocalId).toDomain()

        // 2. Intento de red en segundo plano
        externalScope.launch {
            val response = api.postItem(entity.toDto())
            if (response.isSuccessful) {
                dao.markAsSynced(generatedLocalId, response.body()!!.id)
            }
        }

        Result.Success(savedItem)
    } catch (e: Exception) {
        Result.Failure(GenericError("Error local"))
    }
}
```

---

## 5. Capa de Presentación (UI/UX)

### A. Feedback de Guardado
Incluso si el guardado es instantáneo, el usuario espera feedback.
- Usa un **Overlay de Carga** breve (ej. 500ms) con mensajes como "Guardando registro...".
- Usa `BannerManager.show("Mensaje", BannerType.SUCCESS)` para notificaciones elegantes (fondos de color, letras blancas).

### B. Indicadores de Sincronización
- Si `isSynced` es `false`, muestra un ícono de nube naranja (`CloudUpload`) sutil en la lista.
- Al entrar a un módulo, el Repositorio debe intentar siempre vaciar la cola de pendientes (`uploadPending()`) antes de descargar datos nuevos.

### C. Fallos de Red en Consultas
Para módulos de historial o consulta:
- Si falla el refresh por falta de red, muestra lo que haya en Room.
- Notifica al usuario con un banner: *"Sin conexión. Mostrando datos guardados localmente."*

---

## 6. Reglas de Oro
1.  **Nunca bloquear:** No uses `await()` o esperes respuestas de red en el hilo principal del Repositorio.
2.  **Room es el Jefe:** La UI observa `Flows` de Room. Si Room cambia, la UI cambia.
3.  **IDs Consistentes:** Mapea siempre el `localId` al `id` del dominio para asegurar que ediciones y borrados funcionen localmente antes de sincronizar.
4.  **Banners sobre Snackbars:** Evita mensajes negros que tapen la UI; usa el sistema de Banners personalizados.
