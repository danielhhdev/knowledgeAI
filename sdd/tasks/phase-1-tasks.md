# TASKS - Fase 1 Ingesta

| # | Nombre |
|---|---|
| 1 | Definir contratos DTO y controlador de ingesta |
| 2 | Modelar dominio y repositorios de documentos y chunks |
| 3 | Configurar chunking y propiedades de ingesta |
| 4 | Integrar cliente de embeddings con Spring AI/Ollama |
| 5 | Implementar servicio de ingesta y persistencia atomica |
| 6 | Probar endpoint y servicio de ingesta |
| 7 | Ingestar solo ficheros (PDF/DOCX) con Apache Tika |

---

# TASK 1 - Definir contratos DTO y controlador de ingesta

## 1. Objetivo
Exponer el endpoint `POST /api/ingest` con DTOs validados y contrato claro segun el spec.

## 2. Inputs
- Estructura de capas en `AGENTS.md`.
- Package controller/dto existente.
- Reglas de naming en `constitution.md`.

## 3. Cambios a realizar
- Crear DTO `IngestRequest` con campos source, title, tags, text/url y validaciones Bean Validation.
- Crear DTO `IngestResponse` con documentId, chunksProcessed, tokensCount, status.
- Definir interfaz `IngestController` y su implementacion `IngestControllerImpl` con mapping `/api/ingest`.
- Anotar Swagger/OpenAPI en el controller segun patrones del proyecto.
- Inyectar `IngestService` via constructor y delegar la llamada sin logica de negocio adicional.

## 4. Criterios de Aceptacion
- [ ] Compila
- [ ] Se integra con fase previa
- [ ] Tests minimos pasan
- [ ] Funciona en local con ejemplo real

## 5. Notas tecnicas
- Validar que solo uno de text o url sea provisto; longitud maxima configurable.
- Usar records para DTOs si aplica.

---

# TASK 2 - Modelar dominio y repositorios de documentos y chunks

## 1. Objetivo
Definir modelos de dominio y repositorios necesarios para persistir documentos y chunks en PgVector.

## 2. Inputs
- Spec de fase 1 (modelos y migraciones propuestas).
- Estructura de paquetes `domain` y `repository`.

## 3. Cambios a realizar
- Crear modelos `Document`, `Chunk` (y `Embedding` si se usa) con campos segun spec.
- Crear interfaces `DocumentRepository` y `VectorStoreRepository` (o similar) para persistencia JPA/PgVector.
- Preparar scripts/migraciones con tablas documents/chunks y extensiones vector si se usan migraciones.

## 4. Criterios de Aceptacion
- [ ] Compila
- [ ] Se integra con fase previa
- [ ] Tests minimos pasan
- [ ] Funciona en local con ejemplo real

## 5. Notas tecnicas
- Alinear dimension del vector con el modelo de embeddings elegido.
- Considerar indice ivfflat configurado.

---

# TASK 3 - Configurar chunking y propiedades de ingesta

## 1. Objetivo
Permitir chunking configurable (size y overlap) y validar limites de texto para fail-fast.

## 2. Inputs
- Propiedades sugeridas en el spec (INGEST_CHUNK_SIZE, INGEST_CHUNK_OVERLAP, INGEST_MAX_TEXT_LENGTH).
- Configuracion de perfiles dev/local existente.

## 3. Cambios a realizar
- Agregar propiedades de ingesta en `application-*.yml` y `@ConfigurationProperties` correspondiente.
- Implementar util o componente de chunking que reciba texto y devuelva lista de chunks con overlap.
- Validar limites de longitud antes de procesar.

## 4. Criterios de Aceptacion
- [ ] Compila
- [ ] Se integra con fase previa
- [ ] Tests minimos pasan
- [ ] Funciona en local con ejemplo real

## 5. Notas tecnicas
- Mantener chunking sin dependencias pesadas; puede ser split por caracteres o tokens simples en esta fase.

---

# TASK 4 - Integrar cliente de embeddings con Spring AI/Ollama

## 1. Objetivo
Configurar el cliente de embeddings de Spring AI apuntando a Ollama para generar vectores de chunks.

## 2. Inputs
- Config `SpringAIConfig`/`OllamaConfig` existente.
- Variables de entorno de modelo/base URL definidas en spec.

## 3. Cambios a realizar
- Configurar bean de EmbeddingClient de Spring AI con modelo configurable.
- Manejar timeouts y reintentos basicos si la libreria lo permite.
- Exponer interfaz/puerto en `service` para solicitar embeddings (abstraccion sobre el cliente).

## 4. Criterios de Aceptacion
- [ ] Compila
- [ ] Se integra con fase previa
- [ ] Tests minimos pasan
- [ ] Funciona en local con ejemplo real

## 5. Notas tecnicas
- Validar dimension de vector en arranque o en la primera llamada.

---

# TASK 5 - Implementar servicio de ingesta y persistencia atomica

## 1. Objetivo
Implementar el pipeline de ingesta: validacion, chunking, embeddings y persistencia atomica en PgVector.

## 2. Inputs
- DTOs y controller definidos.
- Chunking util/config.
- Cliente de embeddings configurado.
- Repositorios de documentos y vector store.

## 3. Cambios a realizar
- Implementar `IngestServiceImpl` con flujo: validar payload, obtener texto (directo o fetch URL si se soporta), chunkear, llamar a embeddings, construir entidades y persistir en una transaccion.
- Devolver `IngestResponse` con documentId, chunksProcessed, tokensCount, status.
- Manejar errores con excepciones especificas y logging (INFO/ERROR).

## 4. Criterios de Aceptacion
- [ ] Compila
- [ ] Se integra con fase previa
- [ ] Tests minimos pasan
- [ ] Funciona en local con ejemplo real

## 5. Notas tecnicas
- Considerar rollback ante fallos en embeddings o persistencia.
- Estrategia para duplicados source+title (preferido rechazar con 409).

---

# TASK 6 - Probar endpoint y servicio de ingesta

## 1. Objetivo
Asegurar calidad mediante pruebas unitarias y de integracion del endpoint de ingesta.

## 2. Inputs
- Servicios y repositorios implementados.
- Spec de fase 1 (casos limite).

## 3. Cambios a realizar
- Crear pruebas unitarias para chunking y servicio de ingesta (mock de embeddings y repos).
- Crear prueba de integracion para `POST /api/ingest` (mock o testcontainer para PgVector).
- Verificar casos limite: texto demasiado largo, url invalida, embeddings caidos, duplicado.

## 4. Criterios de Aceptacion
- [ ] Compila
- [ ] Se integra con fase previa
- [ ] Tests minimos pasan
- [ ] Funciona en local con ejemplo real

## 5. Notas tecnicas
- Usar perfiles de test y datos efimeros; evitar dependencias reales de Ollama en tests unitarios.

---

# TASK 7 - Ingestar solo ficheros (PDF/DOCX) con Apache Tika

## 1. Objetivo
Restringir la ingesta a ficheros binarios (PDF/DOCX inicialmente), extrayendo texto y metadatos via Apache Tika antes del chunking. Eliminar soporte a texto plano o URL.

## 2. Inputs
- Arquitectura en capas de `AGENTS.md`.
- Estado actual de `IngestServiceImpl` (solo texto/url).
- Convenciones de naming en `constitution.md`.

## 3. Cambios a realizar
- Definir interfaz `DocumentParser` en capa de servicio/soporte y una impl `TikaDocumentParser` que reciba `byte[]` o `InputStream` y devuelva texto normalizado + metadatos basicos (content-type, title).
- Añadir dependencia Apache Tika en `pom.xml` y propiedades de limite de tamaño/mime permitidos.
- Extender `IngestRequest`/controller para aceptar solo archivo (multipart) y resolver a texto via parser; validar content-type y tamaño.
- Ajustar `IngestServiceImpl` para exigir binario, eliminar rutas de texto/url y procesar únicamente ficheros PDF/DOCX soportados.
- Crear pruebas unitarias del parser (PDF y DOCX de muestra) y de la rama nueva de ingesta (mock parser en el servicio) asegurando rechazo de texto/url.

## 4. Criterios de Aceptacion
- [ ] Compila
- [ ] Parser devuelve texto esperado en PDF y DOCX simples
- [ ] Ingesta acepta binarios y sigue funcionando con texto/url
- [ ] Tests de nuevas rutas pasan en CI/local

## 5. Notas tecnicas
- Normalizar saltos de linea y encoding a UTF-8; manejar errores de parsing con excepciones especificas.
- Evitar OOM limitando tamaño de archivo y timeouts de fetch remoto.
