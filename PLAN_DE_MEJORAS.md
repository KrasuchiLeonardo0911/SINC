# Plan de Mejoras para la Pantalla de Registro de Movimientos

Este documento detalla las tareas para mejorar la funcionalidad y la experiencia de usuario de la pantalla de registro de movimientos.

## 1. Mejorar la Tabla de Movimientos Pendientes
- [ ] **1.a.** Añadir la columna "Unidad Productiva" a la tabla.
- [ ] **1.b.** Asegurar que se muestren todas las columnas relevantes (Especie, Categoría, Raza, Motivo).
- [ ] **1.c.** Implementar la lógica de anulación/resta: si se añade una "baja" que coincide con una "alta" existente (misma UP, especie, categoría, raza), la cantidad se debe restar en lugar de crear una nueva fila. Los registros con cantidad 0 no deben mostrarse.

## 2. Mejorar la Experiencia de Usuario (UX) al Guardar
- [ ] **2.a.** Eliminar el mensaje de "Éxito" al guardar localmente.
- [ ] **2.b.** Implementar una transición suave con una pantalla de esqueleto (skeleton screen) al guardar un movimiento para evitar el "salto" visual.
- [ ] **2.c.** Después de guardar, la interfaz debe volver al paso de selección "Alta/Baja", con el formulario oculto y la tabla de movimientos pendientes actualizada.

## 3. Ajustar y Hacer Notorias las Animaciones
- [ ] **3.a.** Revisar y ajustar la duración de las animaciones existentes para que sean más perceptibles.
- [ ] **3.b.** Añadir animaciones de entrada/salida (fade in/out) a los diferentes pasos del formulario para suavizar las transiciones.
