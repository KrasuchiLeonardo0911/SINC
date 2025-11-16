# Fallas Detectadas

## 1. La aplicación se cierra al iniciar si el token es inválido

**- Síntoma:** 
La aplicación se detiene en la pantalla de carga (splash screen) y luego se cierra inesperadamente.

**- Solución temporal:** 
Limpiar los datos y el caché de la aplicación desde los ajustes del sistema operativo permite que la app inicie correctamente, pero el problema reaparece eventualmente.

**- Hipótesis:**
El error parece ocurrir cuando el token de autenticación guardado en la sesión local expira o se vuelve inválido. El flujo de inicio de la aplicación, en lugar de detectar el token inválido y redirigir al usuario a la pantalla de Login, intenta realizar una operación que requiere autenticación, falla y provoca un cierre abrupto (crash). Esto es especialmente problemático en un enfoque "offline-first", donde la app debe ser robusta ante la falta de conectividad o sesiones expiradas.
