# SPEC - Fase 1 / Ingesta inicial de documentos

## 1. Contexto Heredado
- **Funcionalidades existentes**: Arranque de Spring Boot con perfil `dev`, endpoint `/api/health` basico.
- **Modulos reutilizables**: Estructura de capas definida (controller/service/dto/mapper/domain/repository/config); Docker Compose con PostgreSQL + PgVector; configuracion inicial de perfiles.
- **Modelos de datos previos**: Ninguno persistido; solo placeholders de capas.
- **Estado del proyecto**: Fase 0 completada; proyecto listo para implementar primeros flujos RAG.

---

## 2. Objetivo de Esta Fase / Feature
Habilitar la ingesta de documentos en el sistema RAG, incluyendo subida de contenido, particionado en chunks, generacion de embeddings con Spring AI/Ollama y almacenamiento en PgVector. Esto permite crear el corpus inicial para consultas posteriores y validar el pipeline extremo a extremo.

---

## 3. Alcance (Scope)
### a. En Scope (Lo que SI se implementa)
- [ ] Endpoint `POST /api/ingest` que recibe metadatos basicos y contenido del documento (texto plano o URL).
- [ ] DTOs, mappers y dominio para Document y Chunk; validaciones de entrada.
- [ ] Servicio de ingesta que realiza chunking configurable, genera embeddings y persiste documento y vectores en PgVector.
- [ ] Repositorios para documentos y vector store (upsert/batch insert).
- [ ] Configuracion de Spring AI/Ollama para embeddings (modelo y endpoint configurables por perfil).
- [ ] Logging y metricas minimas del pipeline de ingesta.

### b. Fuera de Scope (Lo que NO se implementa en esta fase)
- [ ] Endpoint de consulta RAG (`/api/query`) y ranking de resultados.
- [ ] Eliminacion/borrado masivo de documentos y vectores.
- [ ] Versionado de documentos o control de concurrencia avanzada.
- [ ] Autenticacion/autorizacion.
- [ ] UI o portal de carga de archivos binarios (solo texto/URL en esta fase).

---

## 4. Requisitos Funcionales
- **RF-01**: El sistema DEBE exponer `POST /api/ingest` que acepte metadatos (`source`, `title`, `tags`) y contenido (`text` o `url`) con validaciones.
- **RF-02**: El sistema DEBE dividir el contenido en chunks segun tamanio configurado (ej. 512-1024 tokens) y solapamiento configurable.
- **RF-03**: El sistema DEBE generar embeddings para cada chunk usando Spring AI apuntando a Ollama (modelo configurable).
- **RF-04**: El sistema DEBE almacenar metadatos del documento y embeddings de chunks en PgVector en una sola transaccion o flujo atomico (fail-fast).
- **RF-05**: El sistema DEBE retornar un identificador de documento y resumen del proceso (cantidad de chunks y tokens procesados).
- **RF-06**: El sistema DEBERA registrar errores de ingesta con detalles para troubleshooting y retornar 4xx/5xx apropiados.

---

## 5. Requisitos No Funcionales
### Rendimiento
- Tiempo de respuesta esperado: < 5s para documentos cortos (<= 5k tokens) en entorno local.
- Soportar al menos 10 documentos por minuto en entorno dev sin degradacion severa.

### Logs
- Log INFO al iniciar y finalizar la ingesta con documentId y conteo de chunks.
- Log DEBUG opcional con tamanios de chunks y latencias de embedding.
- Log ERROR con causa y documentId cuando falle alguna etapa.

### Seguridad
- Validacion de payload (campos obligatorios, tamanio maximo de texto).
- Rechazar URLs no soportadas o contenido vacio.
- Sin auth en esta fase; limitar a perfil dev/local.

### Tests
- Cobertura minima: pruebas unitarias de servicio de ingesta (chunking y llamada a embeddings mockeada) y pruebas de mapper.
- Prueba de integracion del endpoint `POST /api/ingest` con repositorios mock/in-memory o testcontainers basico para PgVector.

### Escalabilidad
- Preparar configuracion para procesamiento batch (chunks insertados en lotes).
- Parametrizar tamanios/solapamientos y modelo de embedding via propiedades.

---

## 6. Flujo Tecnico
1) Controller valida `IngestRequest` (texto o URL requerido, tamanio maximo).
2) Mapper convierte DTO a dominio `Document`/`ChunkInput`.
3) Service genera chunks con tamanio y solapamiento configurables.
4) Service invoca Spring AI EmbeddingClient/Ollama para cada chunk (en serie o lote) y recibe vectores.
5) Service persiste documento y embeddings en PgVector mediante repositories (transaccion o flujo atomico).
6) Service construye `IngestResponse` con `documentId`, `chunksProcessed`, `tokensCount`, `status`.
7) Controller retorna `200 OK` o error con detalle; logs y metricas emitidas.

---

## 7. Endpoints (si aplica)
- **POST /api/ingest**
  - Request: `application/json`
    ```json
    {
      "source": "internal-wiki",
      "title": "Guia RAG",
      "tags": ["rag", "kb"],
      "text": "contenido en texto plano...",
      "url": "https://example.com/articulo" // opcional, usar solo uno: text o url
    }
    ```
  - Response `200 OK`:
    ```json
    {
      "documentId": "e7f5c9c8-1b6a-4a9d-92c7-123456789abc",
      "chunksProcessed": 12,
      "tokensCount": 4800,
      "status": "INGESTED"
    }
    ```
  - Errores:
    - `400 Bad Request` por payload invalido (sin texto/url, texto muy grande, url invalida).
    - `502 Bad Gateway` si falla servicio de embeddings.
    - `500 Internal Server Error` si falla persistencia.

---

## 8. Modelos y Esquema de Datos
### Entidad: Document
| Campo | Tipo | Restricciones | Descripcion |
|-------|------|---------------|-------------|
| id | UUID | PK, NOT NULL | Identificador unico del documento |
| source | VARCHAR(100) | NOT NULL | Origen declarado (ej: wiki, manual) |
| title | VARCHAR(255) | NOT NULL | Titulo o nombre del documento |
| tags | VARCHAR[] o JSONB | NULL | Lista de etiquetas |
| created_at | TIMESTAMP | NOT NULL | Fecha de ingesta |

### Entidad: Chunk
| Campo | Tipo | Restricciones | Descripcion |
|-------|------|---------------|-------------|
| id | UUID | PK, NOT NULL | Identificador unico del chunk |
| document_id | UUID | FK -> Document.id, NOT NULL | Documento asociado |
| index | INT | NOT NULL | Orden del chunk |
| text | TEXT | NOT NULL | Texto del chunk |
| embedding | VECTOR | NOT NULL | Vector PgVector del chunk |

### Relaciones
- `Document` 1:N `Chunk` (un documento tiene muchos chunks).

### Migraciones Necesarias
```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE documents (
  id UUID PRIMARY KEY,
  source VARCHAR(100) NOT NULL,
  title VARCHAR(255) NOT NULL,
  tags JSONB,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE chunks (
  id UUID PRIMARY KEY,
  document_id UUID NOT NULL REFERENCES documents(id),
  index INT NOT NULL,
  text TEXT NOT NULL,
  embedding vector(1536) NOT NULL
);

CREATE INDEX idx_chunks_document_id ON chunks(document_id);
CREATE INDEX idx_chunks_embedding ON chunks USING ivfflat (embedding vector_cosine_ops);
```

---

## 9. Casos Limite
- **CL-01: Texto excede limite**  
  - Input: `text` > tamanio maximo permitido.  
  - Comportamiento: Retornar `400` con mensaje descriptivo y sin procesar.

- **CL-02: URL inaccesible**  
  - Input: `url` que responde 404 o timeouts.  
  - Comportamiento: Retornar `400/502` segun causa; no crear documento.

- **CL-03: Servicio de embeddings caido**  
  - Input: Ingesta normal pero Ollama no responde.  
  - Comportamiento: Retornar `502`, log ERROR y no persistir registros.

- **CL-04: Persistencia falla**  
  - Input: Insercion en PgVector falla (constraint).  
  - Comportamiento: Retornar `500`, rollback y log ERROR con causa.

- **CL-05: Documento duplicado por source+title**  
  - Input: Misma combinacion repetida.  
  - Comportamiento: Definir estrategia (rechazar con `409` o permitir duplicado); documentar en implementacion (preferido: `409`).

---

## 10. Dependencias e Integraciones

### Servicios Externos
- Ollama (modelo de embeddings, ej. `nomic-embed-text`), accesible via Spring AI.
- PostgreSQL 16 con extension PgVector habilitada (ya definida en Docker Compose).

### Modulos Internos Afectados
- `controller`/`impl`: nuevo `IngestControllerImpl`.
- `service`/`impl`: `IngestServiceImpl` con pipeline completo.
- `dto` request/response: `IngestRequest`, `IngestResponse`.
- `mapper`: `DocumentMapper`/`IngestMapper` (segun nombre final) para DTO <-> dominio.
- `domain`: modelos `Document`, `Chunk`, `Embedding` si aplica.
- `repository`: `DocumentRepository`, `VectorStoreRepository` (PgVector).
- `config`: configuracion de Spring AI y propiedades de ingesta (chunk size, overlap, modelo).

### Variables de Entorno
```env
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/devknowledge
SPRING_DATASOURCE_USERNAME=dev
SPRING_DATASOURCE_PASSWORD=dev
SPRING_AI_OLLAMA_BASE_URL=http://localhost:11434
SPRING_AI_OLLAMA_EMBEDDING_MODEL=nomic-embed-text
INGEST_CHUNK_SIZE=800
INGEST_CHUNK_OVERLAP=200
INGEST_MAX_TEXT_LENGTH=20000
```

---

## 11. Riesgos y Asunciones

### Asunciones
- Ollama esta disponible localmente con el modelo de embeddings descargado.
- Los desarrolladores cuentan con recursos suficientes para generar embeddings en local.
- Los clientes de Spring AI estan correctamente configurados para PgVector y Ollama.

### Riesgos Tecnicos
- **R-01**: Latencia alta o fallos en embeddings de Ollama. - Mitigacion: timeouts configurables y reintentos limitados.
- **R-02**: Vector dimension mismatch con PgVector. - Mitigacion: parametrizar dimension y validar en arranque.
- **R-03**: Chunks muy grandes o muy pequenos impactando calidad. - Mitigacion: tamanios configurables y validacion en configuracion.
- **R-04**: Transaccion parcial dejando datos inconsistentes. - Mitigacion: usar transaccion o flujo atomico; rollback en error.

### Decisiones de Diseno Criticas
- Uso de chunking simple (tamanio + overlap) en memoria; sin colas en esta fase.
- Modelo de embeddings via Ollama para entorno local (sin cloud).
- Uso de PgVector con indice `ivfflat` para futuros queries.

---

## 12. KPIs de Exito
- [ ] 95% de ingestiones dev completan sin error para documentos <= 5k tokens.
- [ ] Tiempo medio de ingesta < 5s para documentos de 5k tokens en entorno local.
- [ ] `mvn test` pasa incluyendo pruebas de servicio de ingesta y endpoint.
- [ ] Integracion con Ollama responde con embeddings de dimension esperada.
- [ ] Tablas `documents` y `chunks` contienen registros coherentes (1:N) tras ingesta.

---

## Checklist de Completitud
- [ ] Todos los requisitos funcionales tienen identificador (RF-XX).
- [ ] Se han identificado todos los casos limite criticos.
- [ ] Los endpoints tienen ejemplos de request/response.
- [ ] Los modelos de datos incluyen tipos y restricciones.
- [ ] Se han documentado las dependencias externas.
- [ ] Hay al menos 3 KPIs medibles.
