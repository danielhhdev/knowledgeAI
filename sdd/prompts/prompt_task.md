Genera un conjunto de TASKs atómicas para una fase, **todas en un único archivo Markdown**, siguiendo este formato y estas reglas:

1. El archivo debe comenzar con un encabezado general de fase y una **tabla de tareas**.
2. Cada TASK debe estar **enumerada** como `TASK 1`, `TASK 2`, etc.
3. Todas las TASKs deben ir en **el mismo MD**, separadas por `---`.

Formato estándar para cada TASK (repetido, numerado y en el mismo archivo):

# TASK N – [Nombre concreto de la tarea]

## 1. Objetivo
Qué hace esta tarea y por qué es necesaria.

## 2. Inputs
- entidades afectadas
- clases previas
- tablas existentes

## 3. Cambios a realizar
Paso a paso muy concreto.
Ejemplo:
- Crear `Document` entity con campos (...)
- Añadir anotaciones JPA (...)
- Crear repositorio (...)

## 4. Criterios de Aceptación
- [ ] Compila
- [ ] Se integra con fase previa
- [ ] Tests mínimos pasan
- [ ] Funciona en local con ejemplo real

## 5. Notas técnicas
(Decisiones, dudas, constraints)

Respeta exactamente esta estructura para cada TASK y asegúrate de:
- Enumerar todas las tareas (`TASK 1`, `TASK 2`, …).
- Incluir una tabla al principio del archivo con el nº de tarea y el nombre.
- No crear múltiples archivos: todo el contenido debe ir en un solo `.md`.
