# TASKS Fase 0 / Setup de entorno y esqueleto

| # | Nombre |
| --- | --- |
| TASK 1 | Estructurar paquetes y stubs base |
| TASK 2 | Añadir dependencias Maven base |
| TASK 3 | Configurar perfiles y properties en `application.yml` |
| TASK 4 | Crear `docker-compose.yml` con PostgreSQL + PgVector |
| TASK 5 | Implementar contrato `/api/health` (controller + service + DTO) |
| TASK 6 | Añadir configuración base de OpenAPI y Spring AI/Ollama placeholders |
| TASK 7 | Prueba de integración para `/api/health` |
| TASK 8 | Validación local de arranque y pruebas |

---

# TASK 1 - Estructurar paquetes y stubs base

## 1. Objetivo
Crear la estructura de paquetes conforme a `AGENTS.md`, con interfaces e implementaciones vacías que servirán de guía para fases posteriores.

## 2. Inputs
- Convenciones y capas en `AGENTS.md`
- Lineamientos de nombres en `constitution.md`
- Árbol de paquetes objetivo descrito en plan y spec de fase 0

## 3. Cambios a realizar
- Crear paquetes `com.devknowledge.rag.controller`, `.controller.impl`, `.service`, `.service.impl`, `.dto.request`, `.dto.response`, `.mapper`, `.domain`, `.repository`, `.config`.
- Añadir interfaces vacías para controladores: `IngestController`, `QueryController`, `DocumentController`, `HealthController`.
- Añadir interfaces vacías para servicios: `IngestService`, `QueryService`, `DocumentService`, `HealthService`.
- Crear clases placeholder (sin lógica) en `domain`, `mapper`, `repository` para mantener la estructura (p.ej., `Document`, `Chunk`, `Embedding`, `QueryResult`, `DocumentMapper`, `QueryMapper`, `HealthMapper`, `DocumentRepository`, `VectorStoreRepository`).
- Garantizar que las clases públicas y records sigan `PascalCase` y los paquetes en minúsculas.

## 4. Criterios de Aceptación
- [ ] Estructura de paquetes coincide con el árbol de `AGENTS.md`.
- [ ] Interfaces creadas sin lógica de negocio.
- [ ] Clases placeholder compilan.
- [ ] Nomenclatura alinea con `constitution.md`.

## 5. Notas técnicas
- Usar `record` donde aplique para DTOs/entidades simples aunque estén vacías.
- No incluir lógica ni dependencias; solo stubs.

---

# TASK 2 - Añadir dependencias Maven base

## 1. Objetivo
Incorporar dependencias necesarias para Spring Boot 3.4, Java 21 y componentes base (Web, Data JPA, PostgreSQL, PgVector, Spring AI, Lombok, Validation, Testing).

## 2. Inputs
- `pom.xml` existente
- Requisitos de dependencias del plan y spec

## 3. Cambios a realizar
- Añadir Spring Web, Spring Data JPA, PostgreSQL driver, PgVector starter, Spring AI starter, Lombok, validation (`spring-boot-starter-validation`), y dependencias de test (`spring-boot-starter-test`).
- Verificar compatibilidad con Spring Boot 3.4 y Java 21.
- Asegurar que el wrapper (`mvnw`) sigue operativo.

## 4. Criterios de Aceptación
- [ ] `mvnw dependency:tree` no muestra conflictos críticos.
- [ ] Proyecto compila con `./mvnw test` sin errores de dependencias.
- [ ] Versiones alineadas a Spring Boot 3.4.

## 5. Notas técnicas
- Mantener versiones gestionadas por el BOM de Spring Boot cuando sea posible.
- Evitar dependencias no usadas en esta fase.

---

# TASK 3 - Configurar perfiles y properties en `application.yml`

## 1. Objetivo
Definir perfil `dev` y properties base para datasource PostgreSQL + PgVector y placeholder de Ollama.

## 2. Inputs
- Variables de entorno listadas en la spec (SPRING_DATASOURCE_URL, etc.)
- Requisitos RF-01 a RF-04

## 3. Cambios a realizar
- Crear `src/main/resources/application.yml` con perfil activo por defecto `dev`.
- En `application.yml`, definir perfil `dev` y datasource PostgreSQL (host `localhost`, db `devknowledge`, user `dev`, password `dev`), configuración de PgVector y base URL de Ollama `http://localhost:11434`.
- Incluir logging de arranque (perfil activo, puerto).
- Fail-fast si faltan propiedades obligatorias.

## 4. Criterios de Aceptación
- [ ] Perfil `dev` se activa por defecto.
- [ ] Propiedades requeridas presentes y legibles.
- [ ] Arranque fallará si faltan credenciales obligatorias.
- [ ] No hay warnings de properties desconocidas al iniciar.

## 5. Notas técnicas
- Usar `spring.profiles.active=dev` en `application.yml`.
- Considerar placeholders `${ENV_VAR:default}` solo si no compromete el fail-fast esperado.

---

# TASK 4 - Crear `docker-compose.yml` con PostgreSQL + PgVector

## 1. Objetivo
Proveer infraestructura local para PostgreSQL 16 con extensión PgVector lista para usar.

## 2. Inputs
- Requisitos RF-02 y plan de fase 0
- Credenciales `dev/dev`

## 3. Cambios a realizar
- Añadir `docker-compose.yml` con servicio PostgreSQL 16 y extensión PgVector habilitada.
- Exponer puerto 5432, volumen de datos persistente y red bridge.
- Configurar variables de entorno para base de datos `devknowledge`, usuario `dev`, password `dev`.
- Incluir comandos o imagen que habilite PgVector en el arranque.

## 4. Criterios de Aceptación
- [ ] `docker-compose up -d` levanta el contenedor sin errores.
- [ ] PgVector disponible en la instancia.
- [ ] Persistencia de datos en volumen local.
- [ ] Puertos y credenciales coinciden con `application.yml`.

## 5. Notas técnicas
- Usar imagen oficial con PgVector preinstalado o script `CREATE EXTENSION IF NOT EXISTS vector;`.
- Evitar añadir servicio de Ollama en este compose (fuera de scope).

---

# TASK 5 - Implementar contrato `/api/health` (controller + service + DTO)

## 1. Objetivo
Exponer endpoint `GET /api/health` que devuelva estado `UP` y timestamp ISO, cumpliendo separación de capas.

## 2. Inputs
- Interfaces `HealthController` y `HealthService` (TASK 1)
- Requisito RF-01 y flujo técnico

## 3. Cambios a realizar
- Definir `HealthResponse` como `record` con campos `status` y `timestamp`.
- Implementar `HealthServiceImpl` que construya la respuesta sin lógica de infraestructura externa.
- Implementar `HealthControllerImpl` (`@RestController`) que delegue en el servicio y devuelva `ResponseEntity<HealthResponse>`.
- Añadir logging INFO de entrada al endpoint.

## 4. Criterios de Aceptación
- [ ] `/api/health` responde `200 OK` con payload `{ "status": "UP", "timestamp": "<ISO-8601>" }`.
- [ ] Sin lógica de negocio en controller; servicio encapsula respuesta.
- [ ] Contrato alineado con spec y plan.

## 5. Notas técnicas
- Timestamp en formato ISO-8601 (p.ej., `Instant.now()`).
- Mantener rutas bajo `/api`.

---

# TASK 6 - Añadir configuración base de OpenAPI y Spring AI/Ollama placeholders

## 1. Objetivo
Preparar configuración mínima para OpenAPI y beans placeholder de Spring AI/Ollama para futuras fases.

## 2. Inputs
- Estructura `config` creada en TASK 1
- Plan de fase 0 (OpenAPIConfig, SpringAIConfig, OllamaConfig placeholders)

## 3. Cambios a realizar
- Crear `OpenAPIConfig` básico (si no existe) para exponer documentación mínima.
- Añadir clases `SpringAIConfig` y `OllamaConfig` con beans placeholder o properties necesarias, sin conexiones reales aún.
- Documentar rutas base y metadata del API.

## 4. Criterios de Aceptación
- [ ] Proyecto compila con configuraciones sin fallar por beans faltantes.
- [ ] OpenAPI accesible en perfil `dev` (si Springdoc se incluye) o estructura preparada.
- [ ] Beans placeholder no introducen dependencias externas en arranque.

## 5. Notas técnicas
- Mantener configuración minimalista; no iniciar clientes si no son necesarios aún.
- Alinear nombres de paquetes con `com.devknowledge.rag.config`.

---

# TASK 7 - Prueba de integración para `/api/health`

## 1. Objetivo
Validar que el endpoint `/api/health` responde conforme al contrato en un contexto de aplicación real.

## 2. Inputs
- Implementación de `HealthControllerImpl` y `HealthServiceImpl`
- Dependencias de test agregadas en TASK 2

## 3. Cambios a realizar
- Crear test de integración en `src/test/java/.../controller` usando `@SpringBootTest` o `@WebMvcTest`.
- Verificar que `GET /api/health` retorna `200` y cuerpo con `status="UP"` y `timestamp` no vacío.
- Incluir profile `dev` en el test si aplica.

## 4. Criterios de Aceptación
- [ ] Test pasa consistentemente con `./mvnw test`.
- [ ] Cobertura mínima asegurada para el endpoint de health.
- [ ] El test no depende de servicios externos (DB opcionalmente mockeada/inactiva).

## 5. Notas técnicas
- Usar `MockMvc` o `TestRestTemplate` según convenga.
- Evitar levantar conexiones a DB si no son necesarias.

---

# TASK 8 - Validación local de arranque y pruebas

## 1. Objetivo
Confirmar que la aplicación arranca y las pruebas se ejecutan exitosamente con la configuración de fase 0.

## 2. Inputs
- Configuración y código completados en TASKS previas
- `mvnw`, `docker-compose.yml`

## 3. Cambios a realizar
- Ejecutar `docker-compose up -d` para levantar PostgreSQL+PgVector (solo si aplica a la validación).
- Ejecutar `./mvnw test` y corregir fallos.
- Ejecutar `./mvnw spring-boot:run` con perfil `dev` y verificar `/api/health`.
- Registrar resultados o ajustes necesarios en caso de fallo.

## 4. Criterios de Aceptación
- [ ] `./mvnw test` pasa sin errores.
- [ ] Aplicación arranca en < 30s en perfil `dev`.
- [ ] `GET /api/health` responde como esperado en local.
- [ ] Compose levanta PostgreSQL sin errores (si se prueba).

## 5. Notas técnicas
- En entorno CI puede omitirse levantar Docker si no es requerido.
- Ajustar logging solo si bloquea la validación.
