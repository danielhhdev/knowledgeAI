# TASKS - Fase 3 / Motor Generativo RAG

| N° | Nombre |
|----|--------|
| 1 | Propiedades y config RagClient |
| 2 | DTOs y dominio enriquecido para respuesta generativa |
| 3 | Plantilla de prompt RAG |
| 4 | Mapper de RagResponse a dominio/DTO |
| 5 | Servicio generativo con RagClient (incluye streaming) |
| 6 | Endpoint `/api/v1/query/answer` (y streaming) |
| 7 | Tests unitarios (servicio/mapper/props) |
| 8 | Tests de integración del endpoint generativo |

---

# TASK 1 — Propiedades y config RagClient

## 1. Objetivo
Definir propiedades y beans necesarios para usar `RagClient` de Spring AI con `VectorStoreRetriever`, `ChatModel/ChatClient`, y `PromptTemplate`, parametrizando topK, threshold, modelo y streaming.

## 2. Inputs
- Beans existentes: `PgVectorStore`/`Retriever`, `ChatModel`/Ollama config previa.
- Ficheros de propiedades (`application.yml`), `config` package.

## 3. Cambios a realizar
- Crear `RagProperties` (modelo, topK default/max, threshold, prompt path, streaming enable, token limits).
- Registrar `@ConfigurationProperties` y binder en config.
- Definir bean `PromptTemplate` cargando plantilla RAG desde resources.
- Definir bean `RagClient` combinando `Retriever`, `ChatClient` y `PromptTemplate`.
- Exponer ajustes en `application.yml` (valores por defecto).

## 4. Criterios de Aceptación
- [ ] Compila
- [ ] Beans disponibles en contexto y enlazan propiedades
- [ ] Se integra con fase previa (retriever/ollama)
- [ ] Tests mínimos pasan

## 5. Notas técnicas
- Validar compatibilidad de versión Spring AI para `RagClient`.
- Asegurar paths de prompt válidos en classpath.

---

# TASK 2 — DTOs y dominio enriquecido para respuesta generativa

## 1. Objetivo
Ampliar DTOs y dominio para incluir `answer`, `sources`, `contextUsed`, `latencyMs`, tokens, y bandera `stream`.

## 2. Inputs
- DTOs actuales `QueryRequest`/`QueryResponse`
- Dominio `QueryResult` y VO de pasajes

## 3. Cambios a realizar
- Extender `QueryRequest` con `stream` (boolean) y validaciones.
- Crear `QueryAnswerResponse` (o extender `QueryResponse`) con `answer`, `sources`, `contextUsed`, `latencyMs`, `promptTokens`, `completionTokens`.
- Enriquecer dominio `QueryResult` con `answer`, `Citation`, `ContextSnippet`, tokens y latencia.
- Ajustar validaciones Bean Validation si aplica (longitud query, rangos topK/threshold).

## 4. Criterios de Aceptación
- [ ] Compila
- [ ] Contrato DTO alineado con SPEC
- [ ] Se integra con fase previa sin romper `/api/v1/query`
- [ ] Tests mínimos pasan

## 5. Notas técnicas
- Mantener compatibilidad backward con endpoint de retrieval (no romper campos existentes).

---

# TASK 3 — Plantilla de prompt RAG

## 1. Objetivo
Proveer un prompt template para `RagClient` que genere respuestas con citas y formato controlado.

## 2. Inputs
- Ruta de prompt desde `RagProperties`
- Lineamientos de citas del SPEC

## 3. Cambios a realizar
- Agregar archivo `src/main/resources/prompts/rag-answer.st` con instrucciones (contexto, formato de citas `[source:documentId#chunkIndex]`, tono neutral).
- Asegurar placeholders esperados por `PromptTemplate` (query, documents/context).

## 4. Criterios de Aceptación
- [ ] Compila
- [ ] Prompt accesible por classpath
- [ ] Cumple formato de citas definido
- [ ] Se integra con RagClient

## 5. Notas técnicas
- Evitar incluir datos sensibles; sanitizar entrada antes de binding.

---

# TASK 4 — Mapper de RagResponse a dominio/DTO

## 1. Objetivo
Transformar `RagResponse`/`Document` de Spring AI en `QueryResult` con `answer`, `sources`, `contextUsed`, scores y tokens.

## 2. Inputs
- `QueryMapper` existente
- Objetos Spring AI: `RagResponse`, `Document`, metadata del retriever

## 3. Cambios a realizar
- Actualizar/crear métodos en `QueryMapper` para mapear citations y context snippets desde metadata.
- Incluir score, documentId, title, source, tags, chunkIndex.
- Mapear métricas (tokens, latencia) si están disponibles en response/metadata.

## 4. Criterios de Aceptación
- [ ] Compila
- [ ] Mapper produce DTOs esperados
- [ ] Cobertura de casos sin contexto
- [ ] Tests mínimos pasan

## 5. Notas técnicas
- Respetar reglas de mappers sin lógica de negocio; cualquier limpieza compleja va al servicio.

---

# TASK 5 — Servicio generativo con RagClient (incluye streaming)

## 1. Objetivo
Implementar lógica de negocio generativa: construir `RagQuery`, ejecutar `RagClient`, manejar sin-contexto y errores, soportar streaming.

## 2. Inputs
- `QueryServiceImpl` o nuevo servicio generativo
- `RagClient`, `RagProperties`, `QueryMapper`

## 3. Cambios a realizar
- Añadir método generativo en servicio: armar `RagQuery` con filtros/topK/threshold/stream.
- Invocar `RagClient` (modo normal y `stream`), mapear respuesta con `QueryMapper`.
- Manejar casos de contexto vacío y excepciones del LLM/vector store, retornando respuesta controlada.
- Logging (INFO/ERROR y DEBUG opcional con ids/scores).

## 4. Criterios de Aceptación
- [ ] Compila
- [ ] Servicio retorna respuesta con citas/contexto
- [ ] Streaming emite sin bloquear y se cierra correctamente
- [ ] Tests mínimos pasan

## 5. Notas técnicas
- Considerar backpressure/flujo si se usa SSE/Reactive; mantener API síncrona si el stack es MVC.

---

# TASK 6 — Endpoint `/api/v1/query/answer` (y streaming)

## 1. Objetivo
Exponer endpoint REST para respuestas generativas, con soporte opcional de streaming sin romper `/api/v1/query`.

## 2. Inputs
- `QueryControllerImpl`, DTOs actualizados
- Servicio generativo implementado

## 3. Cambios a realizar
- Agregar ruta `POST /api/v1/query/answer`; decidir si streaming usa misma ruta con `stream=true` o `/stream`.
- Validar payload (`@Valid`), manejar `stream` y content-type (JSON / SSE/NDJSON).
- Mapear `QueryResult` a `QueryAnswerResponse` y retornar ResponseEntity o stream.
- Añadir anotaciones OpenAPI y logging de entrada/salida.

## 4. Criterios de Aceptación
- [ ] Compila
- [ ] Endpoint responde 200 con answer y citas
- [ ] Streaming funcional en cliente (curl/httpie) si `stream=true`
- [ ] No rompe endpoint de retrieval existente

## 5. Notas técnicas
- Si MVC, usar `SseEmitter` o streaming chunked; documentar modo elegido.

---

# TASK 7 — Tests unitarios (servicio/mapper/props)

## 1. Objetivo
Validar lógica de servicio generativo, mapper y binding de propiedades con mocks.

## 2. Inputs
- Servicio generativo, `QueryMapper`, `RagProperties`

## 3. Cambios a realizar
- Tests del servicio mockeando `RagClient`/`ChatClient` para casos normal, sin contexto, error.
- Tests del mapper con documentos de ejemplo y metadata de scores/ids.
- Tests de propiedades asegurando defaults y límites de topK/threshold.

## 4. Criterios de Aceptación
- [ ] Compila
- [ ] Tests pasan localmente
- [ ] Cobertura de casos límite sin contexto y errores
- [ ] Validan contrato de campos clave

## 5. Notas técnicas
- Evitar dependencias a LLM real en unitarios; usar fixtures/mocks.

---

# TASK 8 — Tests de integración del endpoint generativo

## 1. Objetivo
Probar el flujo extremo a extremo del endpoint generativo (modo normal y streaming) con dataset mínimo.

## 2. Inputs
- Endpoint `/api/v1/query/answer`
- Vector store/DB de prueba (testcontainers o fixtures)

## 3. Cambios a realizar
- Preparar datos de documentos/chunks/embeddings de prueba.
- Testear respuesta JSON: `answer`, `sources`, `contextUsed`, status 200 y payload válido.
- Testear streaming (si aplicable) verificando que lleguen fragmentos y cierre correcto.
- Validar casos de payload inválido (400) y error simulado de LLM (500).

## 4. Criterios de Aceptación
- [ ] Compila
- [ ] Tests pasan en `mvn test`
- [ ] Contrato DTO verificado en respuestas
- [ ] Cobertura de normal/streaming/errores

## 5. Notas técnicas
- Si el modelo LLM no está disponible en CI, mockear/simular capa de generación o aislarla con perfiles de test.
