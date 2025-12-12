# Fase 2 / Motor de Recuperacion - Tasks

| # | Nombre |
|---|--------|
| 1 | Configurar propiedades y retriever |
| 2 | Ampliar DTOs de query |
| 3 | Completar dominio y mapper de query |
| 4 | Implementar servicio de retrieval |
| 5 | Exponer endpoint de query |
| 6 | Agregar pruebas de servicio y mapper |
| 7 | Prueba de integracion del endpoint |

---

# TASK 1 – Configurar propiedades y retriever

## 1. Objetivo
Definir configuracion para retrieval (topK default/max, threshold, modo) y exponer el bean de `Retriever` sobre `PgVectorStore` para consultas vectoriales.

## 2. Inputs
- Clases existentes: `SpringAIConfig`, `IngestProperties`, `PgVectorStore` bean.
- Dependencias: Spring AI `VectorStoreRetriever`, `SearchRequest`.
- Tablas: vector store ya creada por PgVector.

## 3. Cambios a realizar
- Crear `QueryProperties` con campos `topKDefault`, `topKMax`, `similarityThreshold`, `mode` (semantic/hybrid) y habilitar `@ConfigurationProperties`.
- Registrar `QueryProperties` en config (`@EnableConfigurationProperties`).
- Definir bean `Retriever` usando `PgVectorStore` y `VectorStoreRetriever` con configuracion de modo/filters basica.
- Ajustar `application.yml` si se requieren defaults iniciales (opcional).

## 4. Criterios de Aceptacion
- [ ] Compila
- [ ] Bean `Retriever` disponible en contexto
- [ ] Valores por defecto cargan desde properties
- [ ] Sin alterar ingesta previa

## 5. Notas tecnicas
- Mantener modo semantic por defecto; dejar campo para hybrid para fase futura.

---

# TASK 2 – Ampliar DTOs de query

## 1. Objetivo
Extender los DTOs para soportar parametros de retrieval (filtros, topK, threshold) con validacion.

## 2. Inputs
- Clases existentes: `QueryRequest`, `QueryResponse`.
- Reglas: Bean Validation, `constitution.md` naming.

## 3. Cambios a realizar
- Actualizar `QueryRequest` con campos `query`, `topK`, `source`, `tags`, `similarityThreshold` con validaciones (`@NotBlank`, rangos).
- Actualizar `QueryResponse` para devolver `results` (lista de fuentes con `documentId`, `title`, `chunkIndex`, `snippet`, `score`, `source`, `tags`).
- Alinear mensajes de validacion en castellano coherente con resto del proyecto.

## 4. Criterios de Aceptacion
- [ ] Compila
- [ ] Bean Validation aplicada
- [ ] Contrato refleja filtros y resultados
- [ ] Compatible con controller/service futuros

## 5. Notas tecnicas
- Usar records si aplica, mantener JSON plano sin anidamientos innecesarios.

---

# TASK 3 – Completar dominio y mapper de query

## 1. Objetivo
Modelar `QueryResult` y `Source` en dominio y crear mapper hacia DTO de respuesta.

## 2. Inputs
- Clases: `QueryResult` (stub), `QueryMapper` (existe).
- Datos: `org.springframework.ai.document.Document` con metadata (documentId, title, chunkIndex, source, tags, score).

## 3. Cambios a realizar
- Implementar `QueryResult` con lista de `Source` y `latencyMs` (opcional).
- Crear VO `Source` con campos requeridos.
- Implementar `QueryMapper` para transformar lista de `Document` + scores a `QueryResult` y luego a `QueryResponse`.

## 4. Criterios de Aceptacion
- [ ] Compila
- [ ] Mapper sin logica de negocio
- [ ] Mapea metadata y scores correctamente
- [ ] Maneja lista vacia

## 5. Notas tecnicas
- Metadata esperada en `Document.getMetadata()`: `documentId`, `title`, `chunkIndex`, `source`, `tags`, y score en headers/metadata segun retriever.

---

# TASK 4 – Implementar servicio de retrieval

## 1. Objetivo
Construir el flujo de retrieval en `QueryServiceImpl` usando `Retriever`/`SearchRequest`, aplicando filtros y ordenando resultados.

## 2. Inputs
- Clases: `QueryService`, `QueryServiceImpl` (stub), `Retriever` bean, `QueryProperties`.
- DTOs: `QueryRequest`.
- Mapper: `QueryMapper`.

## 3. Cambios a realizar
- Inyectar `Retriever`, `QueryProperties`, `QueryMapper`.
- Validar/normalizar `topK` y `similarityThreshold` con limites de properties.
- Construir `SearchRequest` con query, `topK`, filters (source, tags, documentId opcional) y threshold.
- Ejecutar `retriever.retrieve(searchRequest)` y mapear con `QueryMapper`.
- Manejar casos sin resultados (retornar lista vacia) y loguear INFO/ERROR apropiado.

## 4. Criterios de Aceptacion
- [ ] Compila
- [ ] Devuelve resultados ordenados por score
- [ ] Caso sin resultados retorna lista vacia sin error
- [ ] Manejo de excepciones de vector store

## 5. Notas tecnicas
- Si `similarityThreshold` no se soporta directo, usar metadata filter o post-filtrado en servicio.

---

# TASK 5 – Exponer endpoint de query

## 1. Objetivo
Publicar el endpoint REST `/api/v1/query` con validacion, logging y mapping a servicio/respuesta.

## 2. Inputs
- `QueryController`, `QueryControllerImpl` (stubs), `QueryService`.
- DTOs actualizados.

## 3. Cambios a realizar
- Anotar endpoint `POST /api/v1/query` (OpenAPI summary/description).
- Validar payload (`@Valid`) y llamar al servicio.
- Retornar `ResponseEntity` con results; manejar errores mediante handler global existente.
- Logging INFO de entrada (sin loguear query completa si es larga) y conteo de resultados.

## 4. Criterios de Aceptacion
- [ ] Compila
- [ ] Endpoint accesible y documentado en OpenAPI
- [ ] Integra con servicio y mapper
- [ ] Logs basicos emitidos

## 5. Notas tecnicas
- Mantener path consistente con ingest (`/api/v1/...`).

---

# TASK 6 – Agregar pruebas de servicio y mapper

## 1. Objetivo
Cubrir unidad para servicio de retrieval (mock retriever) y mapper.

## 2. Inputs
- Servicio `QueryServiceImpl`, `Retriever` mock, `QueryMapper`.
- Datos de ejemplo con metadata.

## 3. Cambios a realizar
- Crear tests unitarios para: filtros aplicados, topK clamp, resultado vacio, orden por score, manejo de excepciones.
- Tests de mapper: transforma `Document` de Spring AI a `Source`/`QueryResult`.

## 4. Criterios de Aceptacion
- [ ] Tests compilan y pasan
- [ ] Cobertura sobre flujos principales y bordes
- [ ] Sin dependencias a LLM reales
- [ ] Mocks/verificaciones claras

## 5. Notas tecnicas
- Usar builders/utilidades para crear `Document` con metadata; evitar dependencias pesadas.

---

# TASK 7 – Prueba de integracion del endpoint

## 1. Objetivo
Validar `/api/v1/query` extremo a extremo con vector store real o embebido y dataset minimo.

## 2. Inputs
- Endpoint implementado, `PgVectorStore` y testcontainers (ya en deps).
- Datos de ejemplo: documento ingerido con 1-2 chunks y metadata.

## 3. Cambios a realizar
- Configurar test de integracion levantando contexto Spring y vector store (puede ser testcontainers o en memoria si disponible).
- Poblar vector store con documentos de prueba y metadata coherente.
- Ejecutar POST `/api/v1/query` y verificar estructura de respuesta, conteo, orden y metadata.
- Cubrir caso sin resultados.

## 4. Criterios de Aceptacion
- [ ] Test de integracion pasa en `mvn test`
- [ ] Respuesta contiene resultados esperados
- [ ] Caso sin resultados retorna lista vacia
- [ ] No requiere LLM/Red externa

## 5. Notas tecnicas
- Reutilizar utilidades de tests existentes para testcontainers si aplican; mantener tiempos razonables.
