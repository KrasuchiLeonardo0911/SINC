# SINC Mobile

Aplicación móvil nativa de Android para el sistema SINC, diseñada para la gestión y consulta de información relacionada con ovinos.

---

## Arquitectura

Este proyecto sigue los principios de **Arquitectura Limpia (Clean Architecture)** para garantizar un código desacoplado, escalable y fácil de mantener y testear. La estructura está dividida en tres módulos de Gradle:

- **`:app` (Capa de Presentación)**
  - Responsable de toda la interfaz de usuario (UI) y la interacción con el usuario.
  - Construida 100% con **Jetpack Compose**.
  - Utiliza el patrón **MVVM** (Model-View-ViewModel) para separar la lógica de la UI de su estado.

- **`:domain` (Capa de Dominio)**
  - Contiene la lógica de negocio pura y las reglas de la aplicación.
  - Es un módulo de Kotlin puro, sin dependencias del framework de Android.
  - Define los Casos de Uso, los modelos de negocio y las interfaces de los Repositorios.

- **`:data` (Capa de Datos)**
  - Implementa las interfaces de repositorio definidas en el dominio.
  - Se encarga de obtener los datos, ya sea de una fuente remota (API REST) o local (base de datos).

---

## Stack Tecnológico y Librerías Clave

- **Lenguaje**: [Kotlin](https://kotlinlang.org/)
- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Arquitectura**: Clean Architecture + MVVM
- **Asincronía**: [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- **Inyección de Dependencias**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Red**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
- **Diseño**: [Material 3](https://m3.material.io/)
- **Pantalla de Inicio**: [SplashScreen API](https://developer.android.com/develop/ui/views/launch/splash-screen)

---

## Primeros Pasos

Sigue estos pasos para configurar y ejecutar el proyecto en tu entorno local.

### Prerrequisitos

- Android Studio (versión Iguana o superior recomendada).
- JDK 11 o superior.
- El backend de Laravel del proyecto SINC corriendo localmente.

### Configuración

1.  **Clonar el repositorio:**
    ```bash
    git clone <URL_DEL_REPOSITORIO>
    ```

2.  **Iniciar el Backend de Laravel:**
    Es crucial que el servidor de Laravel esté corriendo y accesible para el emulador de Android. Para ello, inicia el servidor con el siguiente comando:
    ```bash
    php artisan serve --host=0.0.0.0
    ```
    La aplicación Android está configurada para conectarse a `http://10.0.2.2:8000`, que es la dirección que usa el emulador para acceder al `localhost` de la máquina anfitriona.

3.  **Abrir en Android Studio:**
    - Abre Android Studio.
    - Selecciona "Open an Existing Project" y elige la carpeta del proyecto clonado.
    - Espera a que Gradle sincronice todas las dependencias.

4.  **Ejecutar la Aplicación:**
    - Selecciona un emulador o conecta un dispositivo físico.
    - Haz clic en el botón "Run 'app'" (el triángulo verde) en la barra de herramientas.

---

## Contribuir


