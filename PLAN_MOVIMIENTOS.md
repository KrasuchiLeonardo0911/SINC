# Plan de Implementación: Movimientos de Stock Offline

Este documento describe los pasos para implementar la funcionalidad de registro y sincronización de movimientos de stock, siguiendo la arquitectura offline-first.

### Nota sobre `declaracion_id`
Aunque la tabla `stock_animals` en la base de datos del backend requiere un `declaracion_id`, la API móvil (`POST /api/movil/cuaderno/movimientos`) no lo incluye en su contrato. La API está diseñada para calcular este ID internamente, probablemente buscando el "período de declaración activo" para la unidad productiva. Por lo tanto, la aplicación móvil **no necesita** gestionar ni enviar este ID. Nos adheriremos al contrato de la API.

---

## Pasos de Implementación

1.  **Modificar la Entidad de Room (`MovimientoPendienteEntity`)**
    -   **Objetivo**: Asegurar que la entidad local pueda almacenar toda la información necesaria para un movimiento.
    -   **Acción**: Añadir el campo opcional `observaciones: String?` a `MovimientoPendienteEntity.kt` para alinearlo con el backend y permitir futuras funcionalidades.

2.  **Verificar el DAO (`MovimientoPendienteDao`)**
    -   **Objetivo**: Confirmar que el DAO existente tiene los métodos necesarios.
    -   **Acción**: Revisar `MovimientoPendienteDao.kt`. Necesitaremos métodos para:
        -   Insertar un nuevo movimiento (`insert`).
        -   Obtener todos los movimientos pendientes de sincronización (`getPendientes`).
        -   Actualizar movimientos a "sincronizado" (probablemente por ID).
        -   Limpiar la tabla (útil para tests).

3.  **Crear y Aprobar los Tests del DAO**
    -   **Objetivo**: Garantizar que el almacenamiento local de movimientos es 100% fiable.
    -   **Acción**: Crear `MovimientoPendienteDaoTest.kt` en `data/src/androidTest`. Se probarán los siguientes escenarios:
        -   Insertar un movimiento y recuperarlo.
        -   La base de datos maneja correctamente los campos opcionales (nulos).
        -   La actualización del estado `sincronizado` funciona.
        -   El borrado de datos funciona.

4.  **Implementar la Lógica en el Repositorio (`MovimientoRepositoryImpl`)**
    -   **Objetivo**: Orquestar la lógica de guardado local y sincronización con la API.
    -   **Acción**: Crear/modificar `MovimientoRepository.kt` (interfaz en `:domain`) y `MovimientoRepositoryImpl.kt` (implementación en `:data`) para incluir:
        -   `fun saveMovimientoLocal(movimiento: Movimiento): Result<Unit>`: Guarda un movimiento en la base de datos Room.
        -   `fun syncMovimientosPendientes(): Result<Unit>`:
            1.  Obtiene los movimientos no sincronizados del DAO.
            2.  Los agrupa por `upId`.
            3.  Construye el JSON según la especificación de la API.
            4.  Llama al endpoint `POST /api/movil/cuaderno/movimientos`.
            5.  En caso de éxito, actualiza los registros locales como sincronizados.

5.  **Crear los Casos de Uso (`:domain`)**
    -   **Objetivo**: Exponer la lógica del repositorio a la capa de presentación de forma limpia.
    -   **Acción**: Crear los siguientes casos de uso:
        -   `SaveMovimientoLocalUseCase`: Llama al método correspondiente del repositorio.
        -   `SyncMovimientosPendientesUseCase`: Llama al método de sincronización del repositorio.

6.  **Integrar con el ViewModel y la UI (`:app`)**
    -   **Objetivo**: Permitir al usuario registrar movimientos desde la pantalla y disparar la sincronización.
    -   **Acción**:
        -   Crear un `MovimientoViewModel` que utilice los nuevos casos de uso.
        -   Diseñar un formulario en la UI para capturar los datos del movimiento.
        -   Al "Guardar", llamar al `SaveMovimientoLocalUseCase`.
        -   Implementar la lógica para llamar al `SyncMovimientosPendientesUseCase` (automáticamente con conexión, o manualmente con un botón).

7.  **Testear el Repositorio y Casos de Uso**
    -   **Objetivo**: Verificar la lógica de negocio de extremo a extremo (sin UI).
    -   **Acción**: Crear tests unitarios/instrumentados para `MovimientoRepositoryImpl` usando una API mockeada (`MockWebServer`) y la base de datos en memoria para simular el flujo completo de sincronización.
