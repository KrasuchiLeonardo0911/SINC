# Monitor de Red (NetworkMonitor)

Este componente permite a la aplicación reaccionar en tiempo real a los cambios en la conectividad a Internet. Es fundamental para la estrategia **Offline-First**, permitiendo que la interfaz de usuario se adapte proactivamente (ej. ocultando mapas o habilitando modos de carga manual) sin esperar a que una petición falle.

## Estructura

- **Interfaz (Domain):** `com.sinc.mobile.domain.util.NetworkMonitor`
- **Implementación (Data):** `com.sinc.mobile.data.util.ConnectivityManagerNetworkMonitor`
- **Inyección:** Se provee a través de Hilt en `NetworkModule`.

## Uso en ViewModels

Para observar el estado de la red en un ViewModel:

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val isOnline = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true // O un valor inicial sensato
        )
}
```

## Uso en Repositorios

Puede usarse para decidir si intentar una sincronización o usar directamente la caché local:

```kotlin
class MyRepository @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    // ...
) {
    suspend fun syncData() {
        if (networkMonitor.isOnline.first()) {
            // Intentar API
        } else {
            // Usar local
        }
    }
}
```

## Beneficios
1. **Reactividad:** La UI responde instantáneamente cuando el usuario entra o sale de una zona con cobertura.
2. **Eficiencia:** Evita disparar peticiones de red innecesarias cuando sabemos de antemano que no hay conexión.
3. **UX Mejorada:** Permite mostrar banners o indicadores de "Modo Offline" de forma consistente.
