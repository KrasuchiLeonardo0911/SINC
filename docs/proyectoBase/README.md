# Sistema de Gestión para la Cuenca Ovino-Caprina

Este es un sistema de información web diseñado para centralizar, gestionar y analizar datos productivos y ambientales de la Cuenca Ovino-Caprina. La plataforma conecta a productores, técnicos y administradores para facilitar la toma de decisiones, el monitoreo sostenible y la trazabilidad del ganado.

---

## ✨ Características Principales

- **Gestión por Roles:** Tres paneles de usuario diferenciados con permisos específicos:
  - **Panel de Superadmin:** Control total sobre usuarios, instituciones, configuraciones y acceso a estadísticas globales.
  - **Panel de Productor:** Permite a los productores gestionar sus unidades productivas, registrar movimientos de stock y visualizar su historial.
  - **Panel Institucional:** Un portal para que instituciones como el INTA o cooperativas accedan a datos agregados.

- **Autenticación Flexible:** Soporte para inicio de sesión web tradicional y un flujo sin contraseña (vía código por Email/SMS) para acceso móvil.

- **Módulo de Importación Asíncrono:** Herramienta de importación masiva que procesa archivos en segundo plano, valida los datos en un área de "staging" y permite la gestión interactiva de los lotes.

- **Cuaderno de Campo Digital:** Interfaz para que los productores registren altas y bajas de su ganado, con actualizaciones de stock total en tiempo real gracias a un sistema de observadores.

- **Estadísticas y Reportes Avanzados:** Dashboards con gráficos y tablas dinámicas que se actualizan vía AJAX, permitiendo el análisis de datos históricos y la exportación de reportes a PDF/CSV.

- **Mapas Interactivos:** Visualización georreferenciada de campos con superposición de capas de datos (ej. límites municipales).

---

## 🚀 Stack Tecnológico

- **Backend:**
  - **Framework:** Laravel (PHP)
  - **Autenticación:** Laravel Jetstream & Sanctum
  - **Base de Datos:** MySQL
  - **Tareas Asíncronas:** Laravel Queues & Jobs

- **Frontend:**
  - **Motor de Vistas:** Blade
  - **Interactividad:** Alpine.js
  - **Estilos:** TailwindCSS
  - **Gráficos:** Chart.js
  - **Mapas:** Leaflet.js

- **Servidor de Aplicaciones:** Apache/Nginx con PHP
- **Gestor de Dependencias:** Composer (PHP), NPM (JavaScript)

---

## ⚙️ Instalación y Puesta en Marcha

Para levantar un entorno de desarrollo local, sigue estos pasos:

1.  **Clonar el repositorio:**
    ```bash
    git clone <URL_DEL_REPOSITORIO>
    cd Proyecto-ovino-caprinos
    ```

2.  **Instalar dependencias de Backend:**
    ```bash
    composer install
    ```

3.  **Instalar dependencias de Frontend:**
    ```bash
    npm install
    ```

4.  **Configurar el entorno:**
    - Copia el archivo de ejemplo `.env.example` a `.env`.
      ```bash
      copy .env.example .env
      ```
    - Genera una clave de aplicación para Laravel:
      ```bash
      php artisan key:generate
      ```
    - Configura las credenciales de tu base de datos local en el archivo `.env` (parámetros `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD`).

5.  **Crear la Base de Datos:**
    - Ejecuta las migraciones para crear la estructura de la base de datos.
      ```bash
      php artisan migrate
      ```
    - (Opcional) Ejecuta los seeders para poblar la base de datos con datos de prueba.
      ```bash
      php artisan db:seed
      ```

6.  **Compilar los assets de Frontend:**
    - Para desarrollo (con recarga automática):
      ```bash
      npm run dev
      ```
    - Para producción:
      ```bash
      npm run build
      ```

7.  **Iniciar el servidor:**
    - Puedes usar el servidor de desarrollo de Laravel:
      ```bash
      php artisan serve
      ```
    - O configurar un Virtual Host en tu servidor local (XAMPP, WAMP, etc.) apuntando a la carpeta `public` del proyecto.

---