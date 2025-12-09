# Constitucion de IA para el repositorio

## Proposito
Establecer reglas de uso de la IA en este repositorio para maximizar productividad sin romper la arquitectura definida en `AGENTS.md`.

## Alcance
- Aplica solo a este repositorio.
- La IA puede asistir en analisis, diseno, codigo, documentacion, pruebas y operaciones de soporte dentro del repo.

## Principios alineados a `AGENTS.md`
- Respetar la arquitectura en capas: controllers sin logica de negocio; servicios con la logica; mappers sin estado ni reglas complejas.
- Mantener inversion de dependencias, responsabilidad unica e inmutabilidad preferida (`record` en DTOs).
- Fail-fast: validaciones tempranas, excepciones especificas y manejo centralizado de errores.
- Cumplir el ciclo SDD antes de codificar: spec -> plan -> tasks -> generacion -> review.
- Contratos de API estables: cambios deben reflejarse en OpenAPI y DTOs.

## Gobernanza y responsabilidades
- Todos los roles pueden proponer y aprobar cambios. Al menos una revision humana es requerida antes de integrar.
- La IA asiste pero no autoaprueba; las decisiones finales son del equipo humano.

## Uso permitido de la IA
- Generar y refinar codigo, tests, documentacion y scripts siempre que respeten los principios anteriores.
- Consultar, refactorizar y explicar partes del sistema siguiendo las capas definidas.
- No hay areas del repo excluidas segun lo indicado por el equipo.

## Flujo de cambios asistidos por IA
1) Crear o actualizar spec/plan/tasks segun SDD cuando aplique.
2) Abrir PR con descripcion clara y checklist cumplimentado.
3) Revisar con al menos una persona; la IA puede sugerir pero no sustituir la aprobacion.
4) Mantener el historial de decisiones en los artefactos SDD y en el PR.

## Checklist minimo (alineado a `AGENTS.md`)
- [ ] Arquitectura en capas respetada (controllers sin negocio, servicios con logica, mappers sin estado).
- [ ] Interfaces definidas antes que implementaciones; dependencias via constructor.
- [ ] DTOs como `record` cuando aplique; validaciones Bean Validation incluidas.
- [ ] OpenAPI/Swagger actualizado en controllers y contratos consistentes.
- [ ] Ciclo SDD aplicado si es una nueva feature (spec, plan, tasks).
- [ ] Pruebas: unitarias para servicios y mappers; integracion para controllers si cambian flujos.
- [ ] Logging apropiado (INFO/ERROR) y manejo de errores especifico.
- [ ] Documentacion relevante actualizada (README u otros) cuando cambien APIs o flujos.

## Reglas para mappers (IA)
- No incluir logica de negocio.
- Usar `expression` solo para valores simples (fechas, UUIDs).
- Si la transformacion es compleja, moverla al Service.
- Mantener mapeos bidireccionales cuando sea necesario.

## Validaciones en DTOs (IA)
- Usar Bean Validation (`@NotNull`, `@NotBlank`, `@Size`).
- Validaciones de negocio complejas deben ir en el Service.
- Mensajes de error descriptivos.

## Convenciones de nombres para generacion por IA
- Clases y records: `PascalCase` descriptivo (`QueryService`, `DocumentMapper`).
- Metodos y variables: `lowerCamelCase` claros, sin abreviaturas cripticas (`chunkSize`, `buildPrompt`).
- Constantes: `UPPER_SNAKE_CASE` (`DEFAULT_MAX_RESULTS`).
- Paquetes: minusculas y concisos (`com.devknowledge.rag.service.impl`).
- DTOs y mappers: sufijos coherentes (`IngestRequest`, `IngestResponse`, `DocumentMapper`).
- Tests: sufijo `Test` (`QueryServiceImplTest`) y metodos descriptivos (`shouldReturnSourcesWhenContextFound`).

## Checklist de salida para tareas asistidas por IA
- Ejecutar `mvn test` (o el subset relevante) y revisar fallos.
- Pasar linters/formatters configurados si existen.
- Verificar que los contratos de API y OpenAPI sigan alineados.
- Revisar nombres y convenciones segun este documento y `AGENTS.md`.
- Actualizar artefactos SDD (spec/plan/tasks) si cambio el alcance o el comportamiento.
- Resumir cambios y riesgos pendientes en la respuesta de salida.

## Evolucion
Actualizar este documento cuando cambien los principios de `AGENTS.md`, el flujo SDD o la politica de revisiones. Cada cambio debe pasar por revision humana.
