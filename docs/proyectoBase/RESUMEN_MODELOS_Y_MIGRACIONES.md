# Resumen de Modelos y Migraciones

Este documento resume la estructura de la base de datos del proyecto, describiendo los modelos de Eloquent, sus tablas correspondientes y las relaciones más importantes.

---

## Modelos Principales

### `User`
- **Tabla:** `users`
- **Descripción:** Gestiona las cuentas de usuario del sistema.
- **Campos Clave:** `id`, `name`, `email`, `password`, `rol` (enum: 'superadmin', 'institucional', 'productor'), `activo`, `verificado`.
- **Relaciones:**
    - `productor()`: `hasOne(Productor)`
    - `institucionParticipante()`: `hasOne(InstitucionalParticipante)`

### `Productor`
- **Tabla:** `productors`
- **Descripción:** Almacena los datos personales y de contacto de los productores.
- **Campos Clave:** `id`, `usuario_id` (FK a `users`), `nombre`, `dni`, `cuil`, `municipio_id` (FK a `municipios`).
- **Relaciones:**
    - `usuario()`: `belongsTo(User)`
    - `unidadesProductivas()`: `belongsToMany(UnidadProductiva)` (a través de la tabla pivote `productor_unidad_productiva`)
    - `municipio()`: `belongsTo(Municipio)`

### `UnidadProductiva`
- **Tabla:** `unidades_productivas`
- **Descripción:** Representa un campo o establecimiento productivo, usualmente identificado por un RNSPA. Es el núcleo de la información productiva.
- **Campos Clave:** `id`, `nombre`, `identificador_local` (único), `superficie`, `latitud`, `longitud`.
- **Relaciones:**
    - `productores()`: `belongsToMany(Productor)`
    - `stock()`: `hasMany(StockAnimal)`
    - `declaraciones()`: `hasMany(DeclaracionStock)`
    - `municipio()`: `belongsTo(Municipio)`

### `StockAnimal`
- **Tabla:** `stock_animals`
- **Descripción:** Registra cada movimiento de stock (altas y bajas). Es el "libro diario" del ganado, la fuente primaria de la verdad para el historial de movimientos.
- **Campos Clave:** `id`, `unidad_productiva_id`, `especie_id`, `categoria_id`, `raza_id`, `cantidad`, `tipo_registro_id`, `motivo_movimiento_id`, `fecha_registro`.
- **Relaciones:**
    - `unidadProductiva()`: `belongsTo(UnidadProductiva)`
    - `especie()`: `belongsTo(Especie)`
    - `categoria()`: `belongsTo(CategoriaAnimal)`
    - `raza()`: `belongsTo(Raza)`
    - `motivo()`: `belongsTo(MotivoMovimiento)`

### `StockActual`
- **Tabla:** `stock_actual`
- **Descripción:** Tabla de resumen (funciona como una caché materializada) que almacena la cantidad total y actual de animales por cada combinación de especie, categoría y raza en cada unidad productiva. Se actualiza automáticamente mediante el `StockAnimalObserver` para reflejar los cambios de `StockAnimal` en tiempo real.
- **Campos Clave:** `id`, `unidad_productiva_id`, `especie_id`, `categoria_id`, `raza_id`, `cantidad_actual`.
- **Relaciones:**
    - `unidadProductiva()`: `belongsTo(UnidadProductiva)`

### `Institucion`
- **Tabla:** `institucions`
- **Descripción:** Gestiona las instituciones participantes (ej. INTA, Cooperativas).
- **Campos Clave:** `id`, `nombre`, `cuit`, `contacto_email`, `validada`, `eliminada`.
- **Relaciones:**
    - `participantes()`: `hasMany(InstitucionalParticipante)`

---

## Modelos de Soporte y Catálogos

- **`Especie`**, **`Raza`**, **`CategoriaAnimal`**: Catálogos para la clasificación taxonómica del ganado.
- **`Municipio`**, **`Paraje`**: Catálogos para la ubicación geográfica.
- **`CondicionTenencia`**, **`FuenteAgua`**, **`TipoPasto`**, **`TipoSuelo`**: Catálogos que describen las características de las unidades productivas.
- **`MotivoMovimiento`**: Catálogo que define las razones para un alta o baja de stock (Nacimiento, Venta, Muerte, Traslado, etc.).
- **`DeclaracionStock`**: Representa la cabecera de una declaración jurada de stock para un período determinado.
- **`DeclaracionVenta`**: Registra la intención de venta de un productor, sirviendo como input para el módulo de logística.

---

## Módulos Clave Reflejados en la Base de Datos

- **Importación Masiva (Staging Area):**
    - **`ImportBatch`**: Representa un archivo (Excel/CSV) subido al sistema. Contiene metadatos del lote.
    - **`ProducerImportRow`**: Almacena cada fila del archivo subido en un área de "staging". Esto permite validar, corregir y procesar los datos de forma asíncrona sin impactar directamente las tablas de producción.

- **Configuración y Tareas Programadas:**
    - **`ConfiguracionActualizacion`**: Define los períodos y la frecuencia para la declaración de stock.
    - **`ConfiguracionLogistica`**: Define los ciclos de recolección del camión de logística (frecuencia, fecha de inicio).
    - **`Clima`**: Almacena datos del clima (obtenidos de una API externa) para los municipios, actualizados por una tarea programada.

- **Auditoría y Notificaciones:**
    - **`Log`**: Guarda un registro de acciones importantes realizadas por los usuarios para fines de auditoría.
    - **`notifications`**: Tabla estándar de Laravel que almacena las notificaciones generadas por el sistema para los usuarios.

---

Este resumen proporciona una visión de alto nivel del esquema de la base de datos y su conexión con la lógica de la aplicación. Para definiciones detalladas de cada columna, sus tipos de datos y constraints, se debe consultar los archivos de migración individuales en la carpeta `database/migrations`.
