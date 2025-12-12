# SPEC - Fase 3 / Motor Generativo RAG

## 1. Contexto Heredado
- **Funcionalidades existentes**: Health check operativo; ingesta via `/api/ingest` con chunking y embeddings en PgVector; retrieval en `/api/v1/query` que devuelve pasajes ordenados usando `Retriever`/`VectorStoreRetriever` de Spring AI.
- **Modulos reutilizables**: `DocumentRepository`, `VectorStoreRepository`, `TextChunker`, `DocumentParser`, `QueryService` (retrieval), `QueryMapper`, configuracion de Spring AI/Ollama y PgVector, DTOs `QueryRequest`/`QueryResponse`.
- **Modelos de datos previos**: Tablas `documents`, `chunks` y almacenamiento vectorial gestionado por Spring AI con metadata (documentId, chunkIndex, source, title, tags). Dominio `QueryResult` y value object de pasajes recuperados.
- **Estado del proyecto**: Fases 0, 1 y 2 completadas; ya se puede consultar y obtener contexto pero aun no se genera respuesta natural con LLM.

---

## 2. Objetivo de Esta Fase / Feature
Habilitar el motor generativo RAG que tome el contexto recuperado y produzca respuestas naturales con citas usando los objetos RAG de Spring AI (`RagClient`, `RagQuery`, `RagResponse`, `Document`). El objetivo es entregar respuestas completas, con fuentes trazables, usando el LLM configurado en Spring AI (Ollama) y un prompt controlado.

---

## 3. Alcance (Scope)
### a. En Scope (Lo que SI se implementa)
- [ ] Endpoint de generacion que recibe una consulta y retorna respuesta con citas (`answer`, `sources`, `contextUsed`), manteniendo `/api/v1/query` de retrieval intacto.
- [ ] Servicio generativo que usa `RagClient` de Spring AI (configurado con `Retriever`/`VectorStoreRetriever`, `ChatClient`/`ChatModel` y `PromptTemplate`) para orquestar retrieval + LLM.
- [ ] Plantilla de prompt RAG (instrucciones, formato de cita, tono) y mapping a dominio/DTO.
- [ ] Soporte opcional de streaming de tokens (SSE/NDJSON) usando `RagClient.stream` o equivalente.
- [ ] Manejo de errores y respuestas vacias (sin contexto suficiente) con mensajes controlados.
- [ ] Logs/metricas basicas (latencia total, tokens, topK aplicado) y pruebas unitarias/integracion.

### b. Fuera de Scope (Lo que NO se implementa en esta fase)
- [ ] Historial conversacional, reescritura de queries o chain-of-thought.
- [ ] Reranking avanzado, guardrails, moderacion de contenido o filtros de seguridad LLM.
- [ ] Cache de respuestas, feedback de usuario o evaluacion automatica.
- [ ] Autenticacion/autorizacion o cuotas multi-tenant.
- [ ] UI o canal web; solo API REST.

---

## 4. Requisitos Funcionales
- **RF-01**: El sistema DEBE exponer `POST /api/v1/query/answer` que reciba `query` y filtros opcionales (`topK`, `source`, `tags`, `similarityThreshold`, `stream`).
- **RF-02**: El sistema DEBE construir un `RagQuery` y ejecutarlo via `RagClient` configurado con `VectorStoreRetriever` y `ChatClient`/`ChatModel` de Spring AI.
- **RF-03**: El sistema DEBE usar un `PromptTemplate` RAG que inserte contexto (chunks) y genere respuesta en lenguaje natural citando fuentes (`[source:documentId#chunkIndex]` o similar).
- **RF-04**: El sistema DEBE retornar `answer` (texto), `sources` (lista con documentId, title, chunkIndex, score, source, tags) y `contextUsed` (snippets) en la respuesta JSON.
- **RF-05**: El sistema DEBE soportar streaming opcional (SSE o NDJSON) para la respuesta generada; cuando `stream=true` debe emitir fragmentos incrementales.
- **RF-06**: El sistema DEBE manejar casos sin contexto suficiente devolviendo mensaje explicito y `sources` vacio sin levantar excepcion.
- **RF-07**: El sistema DEBE validar payload (query no vacio; rangos de `topK`/`similarityThreshold`; `stream` boolean) y devolver 400 ante errores de entrada.
- **RF-08**: El sistema DEBERA registrar logs INFO de inicio/fin, DEBUG opcional con prompt/polish y ERROR con detalles de fallos del LLM o vector store.

---

## 5. Requisitos No Funcionales
### Rendimiento
- Latencia objetivo p95: <= 4s para `topK<=8` en entorno local con modelo Ollama ligero.
- Streaming debe comenzar a emitir en < 1.5s tras recibir la peticion.

### Logs
- INFO con queryId, topK, modo streaming, cantidad de contextos usados y latencia total.
- DEBUG opcional con IDs de documentos y prompt final (sin datos sensibles).
- ERROR con excepcion y estado del vector store/LLM.

### Seguridad
- Limitar longitud de `query` y numero de contextos incluidos.
- Sanitizar metadatos usados en el prompt para evitar prompt injection basica.
- Sin autenticacion en esta fase; limitar a perfil dev/local.

### Tests
- Unit tests del servicio generativo mockeando `RagClient`/`ChatClient` para validar prompt binding, manejo de sin-contexto y logging basico.
- Tests de integracion del endpoint (modo normal y streaming) con PgVector y modelo simulado/mock para asegurar contrato DTO.

### Escalabilidad
- Parametrizar `topK` maximo, token limits y modelo LLM via propiedades.
- Diseñar el servicio para intercambiar `ChatModel` (Ollama -> otro) sin romper el contrato.

---

## 6. Flujo Tecnico
1) Controller valida `QueryRequest` (misma estructura base que retrieval) y bandera `stream`.
2) Mapper crea objeto de dominio (query + filtros) y delega en `QueryService` generativo.
3) Servicio arma `RagQuery` con `topK`, threshold y filtros, y lo envia a `RagClient` configurado con `VectorStoreRetriever` y `ChatClient`.
4) `RagClient` ejecuta retrieval y construye el prompt RAG (via `PromptTemplate`) incorporando los `Document` recuperados.
5) El `ChatClient` genera la respuesta; en modo streaming se emiten tokens incrementales.
6) Servicio mapea `RagResponse` a dominio `QueryResult` (answer, sources, contextUsed, tokens/latencia).
7) Mapper devuelve `QueryResponse` con campos nuevos; controller retorna `200 OK` o stream de eventos.

---

## 7. Endpoints (si aplica)
- **POST /api/v1/query/answer** (JSON)
  - Request:
    ```json
    {
      "query": "Como habilitar PgVector en Postgres?",
      "topK": 5,
      "similarityThreshold": 0.65,
      "source": "internal-wiki",
      "tags": ["postgres", "vector"],
      "stream": false
    }
    ```
  - Response `200 OK`:
    ```json
    {
      "answer": "Para habilitar PgVector ejecuta CREATE EXTENSION vector; ...",
      "sources": [
        {
          "documentId": "e7f5c9c8-1b6a-4a9d-92c7-123456789abc",
          "title": "Guia PgVector",
          "chunkIndex": 2,
          "source": "internal-wiki",
          "tags": ["postgres", "vector"],
          "score": 0.82
        }
      ],
      "contextUsed": [
        {
          "chunkIndex": 2,
          "snippet": "CREATE EXTENSION IF NOT EXISTS vector...",
          "documentId": "e7f5c9c8-1b6a-4a9d-92c7-123456789abc"
        }
      ],
      "latencyMs": 1500
    }
    ```
  - Errores: `400` por payload invalido; `500` por fallo LLM/vector store.

- **POST /api/v1/query/answer/stream** (opcion SSE/NDJSON)
  - Mismo payload con `stream=true`.
  - Response: eventos incrementales con campos `delta` y `sources` solo en el primer/ultimo evento.

---

## 8. Modelos y Esquema de Datos
### Entidad: QueryResult (dominio enriquecido)
| Campo | Tipo | Restricciones | Descripcion |
|-------|------|---------------|-------------|
| answer | String | puede ser vacio cuando no hay contexto | Respuesta generada |
| sources | List<Citation> | puede ser vacio | Fuentes/citas usadas |
| contextUsed | List<ContextSnippet> | puede ser vacio | Pasajes incluidos en el prompt |
| latencyMs | long | opcional | Tiempo total de retrieval + generacion |
| promptTokens | Integer | opcional | Tokens de entrada al modelo |
| completionTokens | Integer | opcional | Tokens generados por el modelo |

### Value Object: Citation
| Campo | Tipo | Restricciones | Descripcion |
|-------|------|---------------|-------------|
| documentId | UUID | NOT NULL | Id del documento |
| title | String | NOT NULL | Titulo de la fuente |
| chunkIndex | int | NOT NULL | Chunk referenciado |
| score | double | NOT NULL | Score de similitud |
| source | String | opcional | Origen declarado |
| tags | List<String> | opcional | Tags asociadas |

### Value Object: ContextSnippet
| Campo | Tipo | Restricciones | Descripcion |
|-------|------|---------------|-------------|
| documentId | UUID | NOT NULL | Id del documento |
| chunkIndex | int | NOT NULL | Orden del chunk |
| snippet | String | NOT NULL | Texto del fragmento incluido |

### Migraciones Necesarias
No se requieren nuevas tablas; se reutiliza storage de documentos y PgVector. Solo se agregan campos en DTOs/respuestas (no persistidos).

---

## 9. Casos Limite
- **CL-01: Sin contexto recuperable**  
  - Input: query valida sin hits o con scores < threshold  
  - Comportamiento: `answer` vacio o mensaje controlado, `sources` vacio, `200 OK`.
- **CL-02: Modelo LLM no disponible**  
  - Input: query normal con Ollama caido  
  - Comportamiento: `500 Internal Server Error` con mensaje generico y log ERROR.
- **CL-03: Streaming solicitado con payload invalido**  
  - Input: `stream=true` y `query` vacio  
  - Comportamiento: `400 Bad Request`.
- **CL-04: Prompt injection en metadatos**  
  - Input: metadatos maliciosos en `source`/`title`  
  - Comportamiento: sanitizar y seguir; no ejecutar instrucciones en prompt.
- **CL-05: TopK o threshold fuera de rango**  
  - Input: `topK` negativo o `similarityThreshold>1`  
  - Comportamiento: `400 Bad Request`.

---

## 10. Dependencias e Integraciones

### Servicios Externos
- Ollama (modelos locales, ej. `llama3.2`, `phi3.5`) via Spring AI `ChatModel`.

### Modulos Internos Afectados
- `controller/impl`: nuevo endpoint `QueryControllerImpl` para generacion y streaming.
- `service/impl`: `QueryService`/`QueryServiceImpl` ampliado o nuevo servicio generativo usando `RagClient`.
- `dto`: ampliar `QueryRequest` con `stream`; nueva respuesta `QueryAnswerResponse` (o extendida) con `answer`, `sources`, `contextUsed`.
- `mapper`: `QueryMapper` para mapear `RagResponse`/dominio a DTO.
- `config`: bean `RagClient` armado con `VectorStoreRetriever` y `ChatClient`/`ChatModel`, propiedades de prompt y topK/threshold/streaming.

### Variables de Entorno
```env
RAG_MODEL_NAME=llama3.2
RAG_TOPK_DEFAULT=5
RAG_TOPK_MAX=10
RAG_SIMILARITY_THRESHOLD=0.6
RAG_PROMPT_TEMPLATE_PATH=classpath:prompts/rag-answer.st
RAG_STREAM_ENABLED=true
```

---

## 11. Riesgos y Asunciones

### Asunciones
- El vector store ya esta poblado y consistente con metadata.
- El LLM (Ollama) responde en tiempos razonables y soporta streaming.
- Los objetos `RagClient`/`RagQuery` de Spring AI estan disponibles en la version usada.

### Riesgos Tecnicos
- **R-01**: Hallucination o respuesta sin citar fuentes. *Mitigacion*: prompt explicito y post-procesar citas; fallback cuando no hay contexto.*
- **R-02**: Latencia alta del LLM. *Mitigacion*: topK acotado, modelos ligeros y streaming por defecto.*
- **R-03**: Cambios de API en Spring AI RAG. *Mitigacion*: encapsular en servicio y versionar dependencias; tests de contrato.*
- **R-04**: Prompt injection via metadata. *Mitigacion*: sanitizar metadatos antes del prompt y limitar longitud.*
- **R-05**: Respuestas inconsistentes entre modo streaming y no streaming. *Mitigacion*: reusar mismo `RagClient` y parser para ambos modos.*

### Decisiones de Diseno Criticas
- Usar `RagClient` de Spring AI para orquestar retrieval + generacion en vez de construir prompts manuales.
- Mantener endpoint de retrieval existente y crear endpoint dedicado para respuesta generativa para no romper contratos previos.
- Formato de citas explicito en la respuesta para trazabilidad.

---

## 12. KPIs de Exito
- [ ] 95% de respuestas generativas se entregan en < 4s p95 con `topK<=8`.
- [ ] 100% de respuestas incluyen `sources` cuando hay contexto recuperado.
- [ ] `mvn test` pasa con nuevos tests unitarios e integracion del endpoint generativo (incluyendo streaming).
- [ ] En modo streaming, primer token emitido en < 1.5s en dev.
- [ ] Errores controlados (4xx/5xx) logueados con correlacion y sin filtrar datos sensibles.

---

## Checklist de Completitud
- [ ] Todos los requisitos funcionales tienen identificador (RF-XX)
- [ ] Se han identificado todos los casos limite criticos
- [ ] Los endpoints tienen ejemplos de request/response
- [ ] Los modelos de datos incluyen tipos y restricciones
- [ ] Se han documentado las dependencias externas
- [ ] Hay al menos 3 KPIs medibles
