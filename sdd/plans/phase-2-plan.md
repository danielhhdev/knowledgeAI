# PLAN - Fase 2 / Motor de Recuperacion

## 1. Resumen del Objetivo
Construir el motor de recuperacion que toma una consulta, aplica filtros y devuelve pasajes relevantes ordenados desde PgVector usando componentes de Spring AI (`Retriever`/`VectorStoreRetriever`), sin generacion LLM.

## 2. Arquitectura Afectada
- Clases nuevas/modificadas:
  - `QueryController`/`QueryControllerImpl`: contrato + endpoint `POST /api/v1/query`.
  - `QueryService`/`QueryServiceImpl`: orquesta retrieval via `Retriever`.
  - `QueryMapper`: DTO <-> dominio (`QueryResult` y fuentes).
  - DTOs: ampliar `QueryRequest` (query, topK, threshold, filtros) y `QueryResponse` (results).
  - Dominio: completar `QueryResult` y VO `Source` (snippet, metadata, score).
  - Config: `QueryProperties` (topK default/max, threshold, mode) y bean `Retriever` basado en `PgVectorStore`.
- Endpoints: `POST /api/v1/query`.
- Tablas: reutiliza `documents`, `chunks` y tabla de vector store (sin cambios).
- Paquetes: `controller`, `controller.impl`, `service`, `service.impl`, `dto.request/response`, `mapper`, `domain`, `config`.

## 3. Checklist de Implementacion (orden recomendado)
1. Definir `QueryProperties` con defaults/max y modo (semantic/hybrid); exponer bean de `Retriever` usando `PgVectorStore`/`VectorStoreRetriever`.
2. Ampliar `QueryRequest` con `topK`, `source`, `tags`, `similarityThreshold` y validaciones Bean Validation.
3. Ampliar `QueryResponse` para incluir `results` (lista de fuentes con metadata y score).
4. Completar dominio `QueryResult` y crear VO `Source`.
5. Crear `QueryMapper` para mapear resultados de retrieval (`List<Document>`) a dominio y DTO.
6. Implementar `QueryServiceImpl`: construir `SearchRequest` con filtros/threshold/topK, invocar `Retriever`, mapear y ordenar resultados; manejar casos vacios y errores de vector store.
7. Implementar `QueryControllerImpl`: endpoint `POST /api/v1/query` con validacion, logging y mapeo a response.
8. Tests unitarios: servicio con retriever mockeado (filtros, orden, vacios); mapper si aplica.
9. Test de integracion del endpoint con vector store embebido/testcontainers y dataset minimo.
10. Ajustar OpenAPI/anotaciones y logs; revisar propiedades en `application.yml` si aplica.

## 4. Secuencia de Commits
- feat: add query properties and retriever config
- feat: expand query dto and domain for retrieval results
- feat: implement query mapper and service retrieval flow
- feat: expose query endpoint controller
- test: add query service and controller tests

## 5. Dependencias
- Ingesta y vector store poblado (fase 1).
- Beans existentes: `PgVectorStore`, `JdbcTemplate`, configuracion de Spring AI.
- Document metadata almacenada en vector store (documentId, title, chunkIndex, source, tags).

## 6. Validacion Final
- Pruebas manuales: `curl`/`http` POST `/api/v1/query` con query sobre documento conocido; verificar lista de resultados ordenados y metadata correcta; probar caso sin resultados.
- Pruebas automáticas: `mvn test` (unitarios + integracion de query). Si no funciona, no se haca, se hará manual por el usuario.
- Criterio DONE: endpoint devuelve resultados coherentes segun corpus, valida payloads incorrectos con 4xx, tests verdes y propiedades configurables (`topK`, threshold, mode).
