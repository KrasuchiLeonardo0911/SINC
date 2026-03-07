# Avances de la Sesión Actual (06 de Marzo de 2026)

## 1. Módulo de Stock: Rediseño y Venta Directa

- **Nuevo Selector de Campo**: Se sustituyó el sistema de chips por un componente `CampoSelector` con estética de barra de búsqueda. 
    - Diseño limpio: borde delineado oscuro, `RoundedCornerShape(8.dp)`, sin sombras iniciales.
    - Interacción pulida: efecto *ripple* perfectamente redondeado que sigue la forma del componente.
    - Modal Simplificado: El `UpSelectorBottomSheet` ahora es una lista limpia que resalta la selección únicamente con el color principal y un check, eliminando distracciones visuales.
- **Flujo de Venta Directa**: Se implementó el registro de ventas directamente desde la tabla de detalles de stock.
    - Icono de carrito de compras en color verde oscuro, sin fondo, para una interfaz más aireada.
    - Modal de registro rápido con cierre instantáneo al confirmar (UX mejorada).
    - Validación robusta: El sistema bloquea nuevas ventas si detecta que el camión ya recogió el lote o este se encuentra en planta.

## 2. Módulo de Seguimiento Logístico: Hoja de Ruta Unificada

- **Visión de Ciclo Único**: Se transformó la pantalla de seguimiento de una lista de tarjetas a una **Hoja de Ruta Vertical** a pantalla completa.
    - Eje vertical continuo que conecta los 4 hitos logísticos (Publicado -> En Viaje -> En Planta -> Finalizado).
    - Se agrupan todas las declaraciones activas en un solo "Lote Actual" con totales destacados.
- **Interactividad del Lote**: 
    - Cabecera interactiva para desplegar el detalle completo del lote mediante un `ModalBottomSheet`.
    - Sistema de **Gestión de Lote Selectiva**: Permite al productor cancelar registros individuales del lote actual antes de que el camión inicie la recogida.
- **Cierre Inteligente de Ciclo**: 
    - Implementación de captura y persistencia del `historial_ciclo_id` desde el backend.
    - Lógica de reinicio: La pantalla de seguimiento se limpia automáticamente cuando todos los integrantes del lote alcanzan un estado terminal (Entregado, Rechazado o Devuelto), moviendo la información al historial.

## 3. Infraestructura y Estabilidad

- **Base de Datos**: Se incrementó la versión a **16** para soportar el campo `historialCicloId`.
- **Capa de Datos**: Actualización de DTOs, Entidades y Mapeadores para integrar la trazabilidad por ciclo logístico.
- **Validación de Stock**: Se actualizó el DAO para incluir todos los estados logísticos intermedios en el cálculo de "Stock Comprometido", evitando la sobreventa de animales.
- **Componentes Reutilizables**: Se flexibilizó el componente `MinimalHeader` permitiendo el ajuste dinámico del tamaño de fuente para títulos largos.

---
**Estado del Proyecto**: Compilación exitosa (`./gradlew assembleDebug`). El flujo logístico es ahora cíclico, profesional y centrado en el concepto de Lote Unificado.
