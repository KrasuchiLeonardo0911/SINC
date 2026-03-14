# Guía de Sincronización de Tiempo y Gestión de OTP (App Móvil)

Esta guía detalla los cambios realizados en el Backend para solucionar el desfase horario entre los dispositivos móviles y el servidor, y cómo la aplicación móvil debe consumir estos nuevos datos.

## 1. El Problema
Detectamos que cuando el reloj de un dispositivo móvil no coincide exactamente con el del servidor (por zona horaria o desajuste manual), las validaciones de expiración de códigos OTP y el tiempo de espera para cambio de contraseña fallaban, bloqueando al usuario injustamente.

## 2. Nuevos Campos en la API

Se han añadido dos campos clave en las respuestas de autenticación y configuración:

1.  **`server_time`**: Hora actual del servidor en formato ISO 8601 (ej: `2024-03-03T15:30:00-03:00`).
2.  **`expires_in_minutes`**: Tiempo de vida útil del código generado o de la restricción, expresado de forma relativa en minutos (ej: `10`).

## 3. Estrategia de Implementación en Android (SINC)

### A. Cálculo del Offset de Tiempo
En lugar de confiar en `System.currentTimeMillis()`, la app debe calcular la diferencia (offset) con el servidor al iniciar sesión o inicializar la app.

```kotlin
// Pseudo-código de ejemplo
val serverTime = Instant.parse(response.serverTime)
val deviceTime = Instant.now()
val timeOffset = Duration.between(deviceTime, serverTime)

// Para obtener la hora "real" del servidor en cualquier momento:
fun getSyncedTime() = Instant.now().plus(timeOffset)
```

### B. Temporizador de Expiración (Countdown)
Para mostrar el tiempo restante del código OTP (ej: "Expira en 09:59"), no se debe calcular contra una hora fija, sino usar el valor relativo:

1.  Recibir `expires_in_minutes` (ej: 10).
2.  Iniciar un `CountDownTimer` de `10 * 60 * 1000` milisegundos.
3.  Esto garantiza que el contador sea exacto independientemente de si la hora del teléfono está bien configurada o no.

### C. Cooldown de Cambio de Contraseña
La configuración se ha cambiado de **días** a **minutos**. El mensaje de error devuelto por la API (422) ahora indicará minutos:
*   *Mensaje anterior:* "Debes esperar 30 días..."
*   *Mensaje nuevo:* "Debes esperar al menos 1 minuto... aún te faltan 1 minuto."

## 4. Endpoints Actualizados

| Endpoint | Campos Añadidos | Propósito |
| :--- | :--- | :--- |
| `POST /api/movil/login` | `server_time` | Sincronización inicial al loguear. |
| `GET /api/movil/init` | `server_time` | Sincronización para usuarios con sesión activa. |
| `POST /api/movil/password/request-reset` | `server_time`, `expires_in_minutes` | Flujo de recuperación de contraseña. |
| `POST /api/solicitar-codigo` | `server_time`, `expires_in_minutes` | Flujo de login sin contraseña (OTP). |
| `POST /api/iniciar-sesion` | `server_time` | Confirmación de tiempo al entrar. |

## 5. Recomendación de UI/UX
*   **No mostrar la hora del servidor**: Solo usarla internamente para cálculos.
*   **Relativismo**: Siempre que sea posible, mostrar mensajes como "hace 5 minutos" o "expira en 2 minutos" en lugar de "expira a las 15:40".
