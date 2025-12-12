# SPEC - Fase 2 / Motor de Recuperacion

## 1. Contexto Heredado
- **Funcionalidades existentes**: Health check; ingesta via `/api/v1/documents/ingest` con chunking, validaciones y almacenamiento en Postgres + embeddings en PgVector mediante `VectorStore.add`.
- **Modulos reutilizables**: `DocumentRepository`, `VectorStoreRepository`, `TextChunker`, `DocumentParser`, `PgVectorStore` configurado, DTOs `QueryRequest`/`QueryResponse` y dominio `QueryResult` (stub).
- **Modelos de datos previos**: Tablas `documents`, `chunks` y tabla de vector store gestionada por Spring AI con metadata (documentId, chunkIndex, source, title, tags).
- **Estado del proyecto**: Fase 1 cerrada; capa de query/document pendiente de implementacion real.

---

## 2. Objetivo de Esta Fase / Feature
Implementar el motor de recuperacion que, dado un query, retorna los pasajes mas relevantes del corpus usando el vector store y filtros de metadata. La salida incluye fragmentos, scores y metadatos para ser consumidos por la fase generativa posterior.

---

## 3. Alcance (Scope)
### a. En Scope (Lo que SI se implementa)
- [ ] Endpoint `POST /api/v1/query` que valida payload y permite filtros opcionales (source, tags, topK, threshold).
- [ ] Servicio de retrieval que usa componentes Spring AI (`Retriever`/`VectorStoreRetriever`, `SearchRequest`) sobre PgVector para obtener contextos.
- [ ] Soporte de filtros de metadata (source, tags, documentId) y configuracion de `topK` y umbral de similitud.
- [ ] Respuesta con lista ordenada de pasajes (snippet, documentId, title, chunkIndex, score) sin generacion LLM.
- [ ] Logs/metricas basicas y manejo de errores (sin resultados, vector store caido, payload invalido).
- [ ] Tests unitarios del servicio con retriever mockeado y prueba de integracion del endpoint.

### b. Fuera de Scope (Lo que NO se implementa en esta fase)
- [ ] Generacion de respuestas con LLM o prompts RAG.
- [ ] Historial conversacional, reescritura de queries, reranking avanzado.
- [ ] Cache/feedback de usuario o evaluacion automatica.
- [ ] Autenticacion/autorizacion.

---

## 4. Requisitos Funcionales
- **RF-01**: El sistema DEBE exponer `POST /api/v1/query` que reciba `query` (obligatorio) y parametros opcionales `topK`, `source`, `tags`, `similarityThreshold`.
- **RF-02**: El sistema DEBE recuperar hasta `topK` pasajes relevantes usando `Retriever` de Spring AI (PgVector) aplicando filtros de metadata.
- **RF-03**: El sistema DEBE ordenar los resultados por score descendente y devolver snippet, metadata y score en la respuesta.
- **RF-04**: El sistema DEBE manejar el caso de cero resultados retornando lista vacia sin error y mensaje informativo.
- **RF-05**: El sistema DEBE validar entrada (`query` no vacio, `topK` y `similarityThreshold` en rango) y devolver `400` ante payload invalido.
- **RF-06**: El sistema DEBERA registrar logs INFO de entrada/salida y ERROR en fallos de vector store.

---

## 5. Requisitos No Funcionales
### Rendimiento
- Latencia objetivo: < 1.5s p95 para `topK<=10` en entorno dev.
- Capacidad: al menos 30 QPS en dev limitado por vector store.

### Logs
- INFO al recibir consulta (sin loguear query completa si es larga) y cantidad de resultados.
- DEBUG opcional con scores/filters aplicados.
- ERROR con detalle de excepcion en fallos de vector store.

### Seguridad
- Limitar longitud de `query`; sanitizar filtros.
- No registrar contenido sensible en niveles INFO/ERROR.

### Tests
- Unit tests del servicio de retrieval con retriever mock y validacion de filtros y orden.
- Integracion del endpoint con vector store embebido/testcontainers (dataset minimo) validando estructura de respuesta.

### Escalabilidad
- Parametrizar `topK` maximo, umbral de similitud y modo de busqueda (semantic/hybrid) via propiedades.
- Diseñar el servicio para admitir futuros rerankers sin romper contrato.

---

## 6. Flujo Tecnico
1) Controller valida `QueryRequest` y normaliza filtros (`topK` default/max, threshold).
2) Service construye `SearchRequest` con query, `topK`, threshold y filtros de metadata (source, tags, documentId).
3) Service ejecuta retrieval via `Retriever`/`VectorStoreRetriever` sobre PgVector.
4) Se mapean los `Document` recuperados (Spring AI) a `QueryResult` de dominio con `Source` (snippet, ids, scores).
5) Mapper traduce a `QueryResponse`; controller retorna `200 OK` o error segun validacion/excepcion.

---

## 7. Endpoints (si aplica)
- **POST /api/v1/query**
  - Request `application/json`:
    ```json
    {
      "query": "Como habilitar PgVector?",
      "topK": 5,
      "source": "internal-wiki",
      "tags": ["postgres", "vector"],
      "similarityThreshold": 0.7
    }
    ```
  - Response `200 OK`:
    ```json
    {
      "results": [
        {
          "documentId": "e7f5c9c8-1b6a-4a9d-92c7-123456789abc",
          "title": "Guia PgVector",
          "chunkIndex": 2,
          "snippet": "CREATE EXTENSION IF NOT EXISTS vector...",
          "score": 0.82
        }
      ]
    }
    ```
  - Errores: `400` por payload invalido; `500` por fallo del vector store/conexion.

---

## 8. Modelos y Esquema de Datos
### Entidad: QueryResult (dominio, no persistido)
| Campo | Tipo | Restricciones | Descripcion |
|-------|------|---------------|-------------|
| results | List<Source> | puede ser vacio | Pasajes recuperados |
| latencyMs | long | opcional | Tiempo total del retrieval |

### Value Object: Source
| Campo | Tipo | Restricciones | Descripcion |
|-------|------|---------------|-------------|
| documentId | UUID | NOT NULL | Id del documento |
| title | String | NOT NULL | Titulo del documento |
| chunkIndex | int | NOT NULL | Orden del chunk |
| snippet | String | NOT NULL | Fragmento del chunk |
| score | double | NOT NULL | Relevancia |
| source | String | opcional | Origen declarado |
| tags | List<String> | opcional | Tags asociadas |

### Migraciones Necesarias
No se requieren nuevas tablas; se reutiliza la tabla de vector store y `documents`/`chunks`.

---

## 9. Casos Limite
- **CL-01: Query vacia**  
  - Input: `query` nulo/blanco  
  - Comportamiento: `400 Bad Request` con mensaje descriptivo.
- **CL-02: Sin resultados**  
  - Input: consulta valida sin matches (`topK`=0 o scores < threshold)  
  - Comportamiento: `200 OK` con `results` vacio y mensaje informativo opcional.
- **CL-03: Vector store caido**  
  - Input: consulta normal con DB fuera de servicio  
  - Comportamiento: `500 Internal Server Error`, log ERROR.
- **CL-04: topK fuera de rango**  
  - Input: `topK` negativo o > limite  
  - Comportamiento: `400 Bad Request`.
- **CL-05: Filtros inconsistentes**  
  - Input: tags vacias o source demasiado largo  
  - Comportamiento: `400 Bad Request`.

---

## 10. Dependencias e Integraciones

### Servicios Externos
- PostgreSQL + PgVector ya definido en Docker Compose (sin dependencia a LLM en esta fase).

### Modulos Internos Afectados
- `controller`/`impl`: `QueryControllerImpl` con contrato y validaciones.
- `service`/`impl`: `QueryService`/`QueryServiceImpl` usando `Retriever`/`VectorStoreRetriever`.
- `dto`: ampliar `QueryRequest`/`QueryResponse` para filtros y resultados.
- `mapper`: `QueryMapper` para DTO <-> dominio.
- `config`: propiedades para retrieval (`topK` default/max, threshold, modo de busqueda).

### Variables de Entorno
```env
QUERY_TOPK_DEFAULT=5
QUERY_TOPK_MAX=10
QUERY_SIMILARITY_THRESHOLD=0.6
QUERY_MODE=semantic   # semantic | hybrid
```

---

## 11. Riesgos y Asunciones

### Asunciones
- La tabla de vector store contiene metadata consistente con los documentos ingeridos.
- PgVector esta disponible y poblado con embeddings validos.

### Riesgos Tecnicos
- **R-01**: Respuestas sin contexto por metadata faltante. *Mitigacion*: validar metadata al mapear resultados.
- **R-02**: Cambios de API en `spring-ai-rag`/`Retriever`. *Mitigacion*: encapsular en servicio y tests de contrato.
- **R-03**: Scores no comparables al usar modos hybrid. *Mitigacion*: normalizar o documentar modo actual.
- **R-04**: Queries largas que degradan rendimiento. *Mitigacion*: limitar longitud y registrar advertencia.

### Decisiones de Diseno Criticas
- Usar `Retriever` de Spring AI (PgVector) en vez de consultas manuales SQL/vector.
- Mantener servicio sin estado y preparado para conectar un reranker o generador en fase siguiente.
- Priorizar filtrado por metadata para mejorar relevancia y gobernanza de datos.

---

## 12. KPIs de Exito
- [ ] 95% de consultas dev responden en < 1.5s con `topK<=10`.
- [ ] `POST /api/v1/query` retorna al menos 1 resultado para queries sobre corpus conocido.
- [ ] `mvn test` pasa con unit tests de retrieval y integracion del endpoint.
- [ ] Validaciones retornan 4xx coherentes en payloads invalidos.
- [ ] Logs muestran conteo de resultados y filtros aplicados.

---

## Checklist de Completitud
- [ ] Todos los requisitos funcionales tienen identificador (RF-XX)
- [ ] Se han identificado todos los casos limite criticos
- [ ] Los endpoints tienen ejemplos de request/response
- [ ] Los modelos de datos incluyen tipos y restricciones
- [ ] Se han documentado las dependencias externas
- [ ] Hay al menos 3 KPIs medibles
