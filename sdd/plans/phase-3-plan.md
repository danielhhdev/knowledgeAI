# PLAN - Fase 3 / Motor Generativo RAG

## 1. Resumen del Objetivo
Construir el motor generativo RAG que orquesta retrieval + LLM usando objetos RAG de Spring AI (`RagClient`, `RagQuery`, `RagResponse`, `Document`) para producir respuestas naturales con citas, incluyendo soporte opcional de streaming.

## 2. Arquitectura Afectada
- Clases nuevas/modificadas:
  - `QueryController`/`QueryControllerImpl`: nuevo endpoint `POST /api/v1/query/answer` (y opcion de streaming) reutilizando validaciones.
  - `QueryService`/`QueryServiceImpl`: ampliar con metodo generativo o crear servicio dedicado para `RagClient`.
  - `QueryMapper`: mapear `RagResponse`/dominio a DTO con `answer`, `sources`, `contextUsed`, tokens/latencia.
  - DTOs: extender `QueryRequest` con `stream`; nueva `QueryAnswerResponse` (o version extendida de `QueryResponse`) con `answer`, `sources`, `contextUsed`, `latencyMs`, tokens.
  - Dominio: enriquecer `QueryResult` con `answer`, `Citation`, `ContextSnippet`, tokens y latencia.
  - Config: bean `RagClient` armado con `VectorStoreRetriever`, `ChatClient`/`ChatModel`, `PromptTemplate` para RAG, propiedades (`topK`, threshold, modelo, prompt path, streaming flag).
  - Prompts: archivo de plantilla para respuesta RAG (por ejemplo `src/main/resources/prompts/rag-answer.st`).
- Endpoints: `POST /api/v1/query/answer` (JSON) y variante de streaming (`/api/v1/query/answer/stream` o mismo endpoint con `stream=true`).
- Tablas: sin cambios (reutiliza vector store y documentos).
- Paquetes backend: `config`, `controller`, `controller.impl`, `service`, `service.impl`, `dto.request/response`, `mapper`, `domain`, `resources/prompts`.

## 3. Checklist de Implementacion (orden recomendado)
1. Definir propiedades `RagProperties` (modelo, topK default/max, threshold, streaming enable, prompt path, token limits).
2. Configurar beans: `PromptTemplate` (cargar archivo), `Retriever` ya existente, `ChatModel`/`ChatClient` (Ollama), y `RagClient` componiendo retriever + chat + prompt.
3. Extender DTO `QueryRequest` con `stream` y validaciones; definir `QueryAnswerResponse` (answer, sources, contextUsed, latencyMs, tokens).
4. Enriquecer dominio `QueryResult` con campos de respuesta generativa (`answer`, `Citation`, `ContextSnippet`, tokens, latencia).
5. Actualizar `QueryMapper` para transformar `RagResponse`/`Document` a dominio y DTO (citas, contexto, scores).
6. Implementar en `QueryServiceImpl` un metodo generativo: construir `RagQuery` con filtros/topK/threshold, invocar `RagClient`, manejar sin-contexto y errores; soportar streaming (wrap de flujo a DTO).
7. Implementar endpoint en `QueryControllerImpl`: ruta `/api/v1/query/answer`, manejo de `stream` (SSE/NDJSON), validaciones, logging y mapping a response/stream.
8. Agregar plantilla de prompt RAG en resources con instrucciones de citas y formato de salida.
9. Tests unitarios: servicio generativo mockeando `RagClient`/`ChatClient` (modo normal y sin contexto), mapper, propiedades.
10. Tests de integracion: endpoint generativo (no streaming y streaming) con vector store de prueba y modelo simulado/mock; verificar contrato DTO y fuentes.
11. Ajustar OpenAPI/anotaciones, logs y configuraciones en `application.yml`/README si aplica.

## 4. Secuencia de Commits
- feat: add rag properties and client configuration
- feat: extend query dto/domain for generative answers
- feat: add rag prompt template and mapper for citations/context
- feat: implement generative query service with streaming support
- feat: expose query answer endpoint
- test: add rag service and controller tests

## 5. Dependencias
- Fases 1 y 2 listas: ingesta y retrieval funcional con PgVector y metadata consistente.
- Beans existentes: `PgVectorStore`/`Retriever`, configuracion de Ollama/Spring AI.
- Prompt file y modelo LLM disponibles (ej. `llama3.2`).

## 6. Validacion Final
- Pruebas manuales:
  - `POST /api/v1/query/answer` con query conocida; verificar `answer`, `sources` citadas y `contextUsed`.
  - `stream=true` verificando emision temprana de tokens y cierre correcto.
  - Caso sin contexto: respuesta vacia/controlada y `sources` vacio.
- Pruebas automaticas: `mvn test` incluyendo unitarios y integracion del endpoint generativo (stream/no stream) con dataset minimo.
- Criterio DONE: endpoint entrega respuestas generativas con citas trazables, validaciones 4xx correctas, manejo de errores LLM/vector store, y configuracion parametrizada (`topK`, threshold, modelo, prompt, streaming).
