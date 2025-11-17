## Comentarios de Arquitectura - Sesión del 16 de Noviembre de 2025

Esta sección detalla cómo los avances recientes en la gestión de Unidades Productivas (UPs) y la extensión del sistema de catálogos se integran y refuerzan los principios de arquitectura limpia del proyecto.

### 1. Refuerzo de la Separación de Responsabilidades

-   **Capa de Datos (`:data`)**:
    -   La creación de `UnidadProductivaApiService` y la refactorización de `AuthApiService` demuestran la especialización de los servicios de red por dominio de negocio, manteniendo la cohesión y el bajo acoplamiento.
    -   La extensión del sistema de catálogos con nuevas entidades Room y un `CatalogosDao` unificado centraliza la lógica de persistencia para datos de referencia, siguiendo el principio de "Single Source of Truth" (SSOT) para la UI.
    -   Los DTOs (`CreateUnidadProductivaRequest`, `UnidadProductivaDto`) y las funciones de mapeo (`toEntity()`, `toDomain()`) aseguran que la capa de datos sea la única que "conoce" la estructura de la API y la base de datos local, protegiendo las capas superiores de estos detalles de implementación.
-   **Capa de Dominio (`:domain`)**:
    -   La introducción de `CreateUnidadProductivaData` como modelo de dominio para la creación de UPs asegura que la lógica de negocio opere con objetos puros de Kotlin, independientes de cualquier detalle de UI o de infraestructura de datos.
    -   Los nuevos casos de uso (`CreateUnidadProductivaUseCase`, `SyncUnidadesProductivasUseCase`) encapsulan operaciones de negocio específicas, manteniendo la capa de dominio como el corazón de las reglas de la aplicación.
-   **Capa de Presentación (`:app`)**:
    -   La creación de la feature "Campos" (`CamposScreen`, `CamposViewModel`) y el rediseño de `CreateUnidadProductivaScreen` demuestran cómo la UI se construye sobre los casos de uso del dominio, sin interactuar directamente con repositorios o servicios de red.
    -   La lógica de navegación condicional en `MainViewModel` y `MainScreen` orquesta el flujo de usuario basándose en el estado del dominio (existencia de UPs), manteniendo la UI reactiva y desacoplada de la lógica de decisión.

### 2. Adherencia al Patrón MVVM y Flujo de Datos Unidireccional

-   Los `ViewModels` (`MainViewModel`, `CreateUnidadProductivaViewModel`) actúan como intermediarios entre la UI y los casos de uso del dominio, exponiendo el estado de la UI a través de `StateFlow`s y manejando los eventos del usuario.
-   La UI (`MainScreen`, `CreateUnidadProductivaScreen`) observa estos `StateFlow`s y reacciona a los cambios, manteniendo un flujo de datos unidireccional y predecible.

### 3. Estrategia Offline-First Reforzada

-   La extensión del sistema de catálogos con entidades Room y DAOs dedicados refuerza la capacidad offline-first, permitiendo que la aplicación funcione con datos de referencia incluso sin conexión.
-   La lógica de sincronización en `CatalogosRepositoryImpl` y `MainViewModel` asegura que los datos locales se mantengan actualizados con el backend cuando hay conexión, y que la UI siempre consuma de la base de datos local.

### 4. Modularidad y Escalabilidad

-   La creación de nuevas features (ej. "Campos") en paquetes separados dentro de `:app` mantiene la modularidad del código de presentación.
-   La extensión de los catálogos y la gestión de UPs se realizó añadiendo nuevos componentes a las capas existentes, demostrando la escalabilidad de la arquitectura para incorporar nuevas funcionalidades sin romper las existentes.