# SPEC · Fase 0 / Setup de entorno y esqueleto

## 1. Contexto Heredado
- **Funcionalidades existentes**: Ninguna funcionalidad productiva; repositorio inicial con documentos de arquitectura (`AGENTS.md`, `constitution.md`) y README.
- **Modulos reutilizables**: Ninguno implementado; solo scripts Maven Wrapper (`mvnw`) y estructura base de proyecto Spring Boot sin capas completas.
- **Modelos de datos previos**: No hay entidades persistidas ni esquemas de DB definidos.
- **Estado del proyecto**: Fase inicial previa a desarrollo de pipelines (ingesta, query, health).

---

## 2. Objetivo de Esta Fase / Feature
Preparar el entorno de desarrollo y el esqueleto técnico mínimo para habilitar las siguientes fases RAG. Incluye configuración de infraestructura local (PostgreSQL + PgVector, Ollama), arranque de proyecto Spring Boot con capas definidas y verificación básica de salud.

---

## 3. Alcance (Scope)
### En Scope (Lo que SÍ se implementa)
- [ ] Configurar proyecto Spring Boot 3.4 con Java 21 y dependencias base (Spring Web, Spring Data, PgVector, Spring AI, Lombok).
- [ ] Configurar Docker Compose para PostgreSQL + PgVector y, opcionalmente, Ollama service placeholder.
- [ ] Crear estructura de paquetes según `AGENTS.md` (controller, service, dto, mapper, domain, repository, config).
- [ ] Implementar endpoint `/api/health` mínimo que verifique arranque de la app (sin dependencias externas aún).
- [ ] Configurar perfiles de entorno (dev/local) y variables principales (DB, vector store, Ollama endpoint).

### Fuera de Scope (Lo que NO se implementa en esta fase)
- [ ] Pipelines de ingesta, chunking, embeddings o almacenamiento vectorial.
- [ ] Endpoint `/api/query` y lógica RAG.
- [ ] Gestión de documentos (`/api/documents`) y mappers asociados.
- [ ] Hardening de seguridad (authz/authn); se deja para fases posteriores.

---

## 4. Requisitos Funcionales
- **RF-01**: La aplicación DEBE iniciar con perfil `dev` y exponer `/api/health` respondiendo `200 OK`.
- **RF-02**: El sistema DEBE incluir Docker Compose listo para levantar PostgreSQL con extensión PgVector.
- **RF-03**: El proyecto DEBE compilar y ejecutar con `./mvnw spring-boot:run` sin errores de dependencias.
- **RF-04**: Se DEBEN definir paquetes y clases “placeholder” por capa según `AGENTS.md` para guiar implementaciones futuras.

---

## 5. Requisitos No Funcionales
### Rendimiento
- Tiempo de arranque aceptable (< 30s en entorno local con DB levantada).

### Logs
- Log INFO en arranque indicando perfil activo y puerto.
- Log de `/api/health` en nivel INFO al ser llamado (una línea).

### Seguridad
- Sin autenticación en esta fase; limitar a entorno local con perfiles y CORS opcionalmente abierto para dev.
- Validaciones básicas de configuración (fail-fast si faltan variables requeridas de DB).

### Tests
- Cobertura mínima: 1 prueba de integración para `/api/health`.
- Permitir ejecución de `./mvnw test` en < 30s en local.

### Escalabilidad
- No aplica en esta fase; mantener config preparada para contenedores (Docker Compose).

---

## 6. Flujo Técnico
1) Arranque de Spring Boot con perfil `dev`.
2) Carga de propiedades de conexión a PostgreSQL/PgVector desde `application-dev.yml`.
3) Inicialización de beans mínimos (controller/service stub de health).
4) Respuesta HTTP 200 en `/api/health` con payload simple `{status: "UP"}`.
5) Docker Compose levanta PostgreSQL con extensión PgVector; la app puede conectarse, aunque no ejecuta consultas aún.

---

## 7. Endpoints (si aplica)
- `GET /api/health`
  - **Response**: `200 OK`, body `{ "status": "UP", "timestamp": "<ISO-8601>" }`
  - **Errores**: `503` si no arranca contexto (Spring actuará con error de arranque; fuera de scope implementar checks externos).

---

## 8. Modelos y Esquema de Datos
No se crean entidades aún. Se define solo configuración de datasource hacia PostgreSQL con PgVector habilitado.

---

## 9. Casos Límite
- **CL-01: DB no disponible**  
  - Comportamiento: la app debe fallar rápido al iniciar si no puede conectarse (fail-fast), registrando el error en logs.
- **CL-02: Falta variable de entorno obligatoria (DB URL)**  
  - Comportamiento: Spring debe impedir el arranque y registrar configuración faltante.
- **CL-03: Llamada a `/api/health` sin DB**  
  - Comportamiento: si la app arrancó sin DB (perfil sin datasource), debe responder `UP` sólo para contexto de app; los checks de componentes se añaden en fases siguientes.

---

## 10. Dependencias e Integraciones

### Servicios Externos
- PostgreSQL 16 + extensión PgVector (Docker Compose).
- Ollama: placeholder de endpoint para futuras fases; no requerido para pasar los checks de esta fase.

### Módulos Internos Afectados
- `config`: propiedades de datasource, profiles.
- `controller/impl`: `HealthControllerImpl` placeholder.
- `service/impl`: `HealthServiceImpl` básico.

### Variables de Entorno
```env
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/devknowledge
SPRING_DATASOURCE_USERNAME=dev
SPRING_DATASOURCE_PASSWORD=dev
SPRING_AI_OLLAMA_BASE_URL=http://localhost:11434   # placeholder
```

---

## 11. Riesgos y Asunciones

### Asunciones
- Docker Desktop disponible y con recursos suficientes.
- Desarrolladores tienen Java 21 y Maven Wrapper operativo.

### Riesgos Técnicos
- **R-01**: PgVector no carga en la imagen de PostgreSQL → *Mitigación*: usar imagen oficial con extensión preinstalada o script de habilitación en `docker-compose`.
- **R-02**: Tiempo de arranque alto por dependencia a DB → *Mitigación*: permitir perfil sin DB para desarrollo de UI o mocks.

### Decisiones de Diseño Críticas
- Usar perfil `dev` con datasource real para validar conexión desde el inicio.
- Exponer `/api/health` como primer contrato para smoke tests.

---

## 12. KPIs de Éxito
- [ ] Aplicación arranca en < 30s con `./mvnw spring-boot:run`.
- [ ] `GET /api/health` responde `200 OK` y payload esperado.
- [ ] Docker Compose levanta PostgreSQL + PgVector sin errores.
- [ ] `./mvnw test` pasa con al menos una prueba para health.

---

## Checklist de Completitud
- [ ] Todos los requisitos funcionales tienen identificador (RF-XX).
- [ ] Se han identificado casos límite clave (DB caída, config faltante).
- [ ] Endpoint documentado con ejemplo de response.
- [ ] Dependencias externas documentadas con variables de entorno.
- [ ] Hay KPIs medibles (arranque, health, tests, DB up).
