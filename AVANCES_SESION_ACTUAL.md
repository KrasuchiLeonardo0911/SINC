# Avances de la Sesión Actual

Esta sesión se centró en añadir una validación crítica de negocio, solucionar bugs de datos obsoletos y corregir el comportamiento de los formularios dinámicos.

### 1. Implementación de Validación de Stock para Movimientos de Baja

-   **Funcionalidad**: Se implementó una validación para prevenir que los usuarios registren movimientos de "baja" (ej. muerte, venta) por una cantidad mayor al stock disponible.
-   **Lógica de Negocio**: La validación se ejecuta en `MovimientoStepperViewModel` y es robusta, ya que considera dos factores para calcular el "stock disponible real":
    1.  El último stock total conocido para esa categoría de animal.
    2.  La suma de otros movimientos de baja para el mismo animal que ya están en la lista de pendientes de sincronización.
-   **Experiencia de Usuario (UX)**:
    *   Si la validación falla, se impide que el movimiento se añada a la lista.
    *   Se muestra un `Snackbar` con un mensaje de error claro para el usuario (ej. "Stock insuficiente. Disponible: X (Actual: Y, Pendientes: Z)").
-   **Implementación Técnica**:
    *   Se inyectó `StockRepository` en `MovimientoStepperViewModel` para tener acceso a los datos de stock.
    *   Se modificó la función `onAddToList` para incluir la lógica de cálculo y validación.
    *   Se refactorizó el mecanismo de notificación de error para usar una propiedad en el `State` de la UI y un `LaunchedEffect` (siguiendo el patrón ya existente en la app para el manejo de errores de sincronización), asegurando que el `Snackbar` se muestre de forma fiable.

### 2. Sincronización Automática de Stock y Unidades Productivas

Se solucionaron dos bugs relacionados con datos obsoletos que ocurrían después de realizar acciones importantes en la app. El principio de "Fuente Única de Verdad" (la base de datos local) se reforzó asegurando que se actualiza después de cada mutación en el servidor.

-   **Refresco de Stock tras Sincronizar Movimientos**:
    *   **Problema**: Después de sincronizar movimientos (altas o bajas), la pantalla de validación de stock seguía usando los valores antiguos.
    *   **Solución**: Se inyectó `SyncStockUseCase` en `MovimientoSyncManager` y ahora se invoca automáticamente después de que una sincronización de movimientos es exitosa. Esto asegura que la app obtiene inmediatamente los nuevos totales de stock recalculados por el backend.

-   **Refresco de Unidades Productivas (Campos) tras Creación**:
    *   **Problema**: Después de crear un nuevo "Campo" (Unidad Productiva), éste no aparecía en la lista de selección de campos en la pantalla de "Cargar Stock" sin reiniciar la app.
    *   **Solución**: Se aplicó el mismo patrón. Se inyectó `SyncUnidadesProductivasUseCase` en `CreateUnidadProductivaViewModel` y se invoca automáticamente después de que un nuevo campo se guarda con éxito en el servidor.

### 3. Corrección del Formulario Dinámico de Creación de Campo

-   **Problema**: El campo para el identificador (ej. RNSPA) en el formulario de creación de campos mostraba un texto genérico "Identificador" en lugar de usar la información dinámica (`tipo` y `label`) proporcionada por el backend.
-   **Investigación y Causa Raíz**:
    *   Tras añadir logs, se descubrió que el proceso de sincronización de las configuraciones de identificadores estaba fallando silenciosamente.
    *   La causa era una `kotlinx.serialization.MissingFieldException`, ya que el DTO (`IdentifierConfigDto`) esperaba un campo `hint` que la respuesta de la API no incluía.
-   **Solución**:
    1.  **Robustez del DTO**: Se modificó `IdentifierConfigDto` para que el campo `hint` sea nulable con un valor por defecto (`val hint: String? = null`). Esto solucionó el error de deserialización y permitió que los datos se guardaran correctamente en la base de datos local.
    2.  **Ajuste de UI**: Se modificó el Composable `Step2FormularioBasico` para que, por petición del usuario, utilice el campo `type` ("RNSPA") como etiqueta del campo, manteniendo la interfaz limpia y consistente.
# Avances de la Sesión Actual (14 de Enero de 2026)

## 1. Configuración Inicial de Firebase Cloud Messaging (FCM)

-   **Objetivo**: Habilitar la recepción de notificaciones push desde el backend.
-   **Detalles**:
    -   **Configuración de Gradle**: Se añadió el plugin `com.google.gms.google-services` a los archivos `build.gradle.kts` de nivel de proyecto y de aplicación. Se incluyó la BOM de Firebase (`firebase-bom:34.7.0`) y la dependencia `firebase-messaging`.
    -   **Obtención y Registro del Token**: Se implementó lógica en `MainActivity.kt` para obtener el token FCM del dispositivo y loguearlo en Logcat para verificación inicial.
    -   **Configuración del Servicio de Mensajería**:
        -   Se creó `MyFirebaseMessagingService.kt` (`app/src/main/java/com/sinc/mobile/app/firebase/`) para extender `FirebaseMessagingService`, con métodos para manejar la recepción de mensajes (`onMessageReceived`) y la actualización de tokens (`onNewToken`).
        -   Se añadió un ID de canal de notificación por defecto (`fcm_default_channel`) en `strings.xml`.
        -   Se incluyó la lógica para la creación de este canal de notificación en `SincMobileApp.kt` para compatibilidad con Android 8.0+.
        -   Se declaró `MyFirebaseMessagingService` en `AndroidManifest.xml` con su `intent-filter` correspondiente.

## 2. Depuración de la Conexión y Envío de Token FCM al Backend

-   **Problema Inicial**: La aplicación no lograba conectar con el backend local (Laravel).
-   **Resolución**:
    -   Se verificó la configuración del servidor Laravel, recomendando `php artisan serve --host=0.0.0.0`.
    -   Se clarificó el uso de la IP `10.0.2.2` para el emulador y `127.0.0.1` para peticiones desde el host.
    -   Se confirmó la accesibilidad del backend y se corrigió el nombre del campo del token en el cuerpo de la petición (`fcm_token` a `token`) que esperaba el backend.
    -   Se resolvió un problema de `401 No autorizado` obteniendo un nuevo token de autenticación para el usuario `productora@test.com`.
    -   Se envió con éxito el token FCM al backend local, asociándolo al `userId` correcto.
    -   **Configuración de URL Base**: Se alternó la `BASE_URL` en `NetworkModule.kt` entre la dirección local (`http://10.0.2.2:8000/`) para depuración y la de producción (`https://sicsurmisiones.online/`).

## 3. Depuración de la Recepción de Notificaciones en Segundo Plano

-   **Problema**: Las notificaciones aparecían en Logcat con la app en primer plano, pero no en la barra de estado con la app en segundo plano.
-   **Resolución**:
    -   **Requisitos del Canal de Notificación**: Se explicó la obligatoriedad de un canal de notificación para Android 8.0+ para mensajes en segundo plano.
    -   **Configuración del Backend**: Se modificó el comando de Laravel (`php artisan app:send-fcm-test`) para incluir `android_channel_id: 'fcm_default_channel'` en el payload del mensaje, utilizando `AndroidConfig` de `kreait/firebase-php`.
    -   **Icono de Notificación por Defecto**:
        -   Se estableció `default_notification_icon` en `AndroidManifest.xml` inicialmente a `@drawable/ic_launcher_foreground` y `default_notification_color` a `@color/bordeaux`.
        -   Se solucionó un error de compilación (`AAPT: error: resource mipmap/ic_launcher_foreground not found`) al usar la ruta correcta `@drawable/ic_launcher_foreground` en `AndroidManifest.xml`.
        -   Se cambió el icono en `MyFirebaseMessagingService.kt` a `R.mipmap.ic_launcher` temporalmente para depuración.
    -   **Problema de Integración de Comando Laravel**: Se corrigió un error de `PSR-4 autoloading standard` en el comando de Laravel (`TestFcmNotificationCommand`) para asegurar que el comando fuera reconocido y ejecutable.
    -   **Causa Raíz de No Visualización**: Finalmente se identificó que la principal causa de que las notificaciones no aparecieran en segundo plano era que estaban **deshabilitadas para la aplicación en la configuración del dispositivo** del usuario. Una vez habilitadas, las notificaciones empezaron a llegar correctamente en segundo plano.

## 4. Problema Pendiente: Icono de Notificación Incorrecto

-   **Problema Actual**: A pesar de las correcciones y la habilitación de notificaciones, el icono que aparece en la barra de estado es el predeterminado de Android, no `logoovinos`, incluso con `default_notification_icon` configurado en el `AndroidManifest.xml`.
-   **Análisis**: Esto se debe a las estrictas directrices de Android para los iconos pequeños de notificación (deben ser monocromáticos, blancos y transparentes). Si `logoovinos.png` no cumple con esto, el sistema lo sustituye por un icono genérico de Android (a menudo un cuadrado blanco o el icono de la aplicación por defecto).
-   **Acción Pendiente**: Se intentará configurar el `default_notification_icon` en `AndroidManifest.xml` para que apunte directamente a `R.drawable.logoovinos`. Se ha explicado que si `logoovinos.png` es un icono a color, su renderizado como icono pequeño de notificación será problemático y requerirá la creación de una versión monocromática de `logoovinos.png` diseñada específicamente para este propósito.