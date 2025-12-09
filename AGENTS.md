# AGENTS.md - Arquitectura en Capas

> Lineamientos arquitectónicos para el desarrollo del sistema DevKnowledge RAG

---

## 📋 Tabla de Contenidos

- [Propósito](#-propósito)
- [Principios de Diseño](#-principios-de-diseño)
- [Estructura de Capas](#-estructura-de-capas)
- [Contratos de API](#-contratos-de-api)
- [Flujo de Responsabilidades](#-flujo-de-responsabilidades)
- [Convenciones de Código](#-convenciones-de-código)
- [Integración con SDD](#-integración-con-sdd)

---

## 🎯 Propósito

Este documento define la arquitectura en capas del sistema **DevKnowledge RAG**, un sistema de Retrieval-Augmented Generation construido con:

- **Spring Boot 3.4** + **Java 21**
- **Spring AI** para integración con LLMs
- **PgVector** como vector store
- **Ollama** para modelos locales

### Objetivos Arquitectónicos

1. **Separación de responsabilidades** clara entre capas
2. **Facilitar el testing** unitario e integración
3. **Permitir evolución** independiente de cada componente
4. **Favorecer el desarrollo asistido por IA** mediante estructura predecible
5. **Mantener contratos estables** mientras la implementación evoluciona

---

## 🏛️ Principios de Diseño

### 1. Inversión de Dependencias
- Las capas superiores dependen de abstracciones, no de implementaciones
- Los servicios definen interfaces que los controladores consumen
- La capa de dominio no conoce detalles de persistencia o presentación

### 2. Responsabilidad Única
- Cada clase tiene un propósito claro y acotado
- Los controladores solo coordinan, no contienen lógica de negocio
- Los servicios encapsulan toda la lógica de dominio

### 3. Inmutabilidad Preferida
- DTOs implementados como `record` cuando sea posible
- Mappers sin estado
- Servicios sin efectos colaterales innecesarios

### 4. Fail-Fast
- Validaciones tempranas en el controller
- Excepciones específicas del dominio
- Manejo centralizado de errores con `@ControllerAdvice`

---

## 📦 Estructura de Capas

```
com.devknowledge.rag/
├── controller/              # Capa de presentación
│   ├── IngestController.java           # Interface
│   ├── QueryController.java            # Interface
│   ├── DocumentController.java         # Interface
│   ├── HealthController.java           # Interface
│   └── impl/                           # Implementaciones
│       ├── IngestControllerImpl.java
│       ├── QueryControllerImpl.java
│       ├── DocumentControllerImpl.java
│       └── HealthControllerImpl.java
│
├── service/                 # Capa de negocio
│   ├── IngestService.java              # Interface
│   ├── QueryService.java               # Interface
│   ├── DocumentService.java            # Interface
│   ├── HealthService.java              # Interface
│   └── impl/                           # Implementaciones
│       ├── IngestServiceImpl.java      # Pipeline completo
│       ├── QueryServiceImpl.java       # RAG engine
│       ├── DocumentServiceImpl.java    # Gestión docs
│       └── HealthServiceImpl.java      # System checks
│
├── dto/                     # Data Transfer Objects
│   ├── request/
│   │   ├── IngestRequest.java
│   │   ├── QueryRequest.java
│   │   └── DocumentFilterRequest.java
│   └── response/
│       ├── IngestResponse.java
│       ├── QueryResponse.java
│       ├── DocumentResponse.java
│       └── HealthResponse.java
│
├── mapper/                  # Transformaciones DTO ↔ Domain
│   ├── DocumentMapper.java
│   ├── QueryMapper.java
│   └── HealthMapper.java
│
├── domain/                  # Modelos de dominio
│   ├── Document.java
│   ├── Chunk.java
│   ├── Embedding.java
│   └── QueryResult.java
│
├── repository/              # Capa de persistencia
│   ├── DocumentRepository.java
│   └── VectorStoreRepository.java
│
└── config/                  # Configuración
    ├── SpringAIConfig.java
    ├── OllamaConfig.java
    └── OpenAPIConfig.java
```

---

**Componentes verificados**:
- Database (PostgreSQL)
- Vector Store (PgVector)
- LLM (Ollama)
- Spring AI Client

---

## 🔄 Flujo de Responsabilidades

```
┌─────────────────────────────────────────────────────────────┐
│                      REQUEST FLOW                            │
└─────────────────────────────────────────────────────────────┘

HTTP Request
    │
    ├─► 1) Controller Interface
    │       ├─ Define contrato del endpoint
    │       ├─ Anotaciones Swagger/OpenAPI
    │       └─ Sin implementación
    │
    ├─► 2) Controller Implementation
    │       ├─ @RestController
    │       ├─ Validación básica (@Valid, @NotNull)
    │       ├─ Logging de entrada
    │       ├─ Llamada al Service
    │       ├─ Mapeo de excepciones
    │       └─ Return ResponseEntity<DTO>
    │
    ├─► 3) Service Interface
    │       ├─ Define operaciones de negocio
    │       ├─ Sin implementación
    │       └─ Documentación JavaDoc
    │
    ├─► 4) Service Implementation
    │       ├─ @Service
    │       ├─ @Transactional (si aplica)
    │       ├─ Pipeline completo de procesamiento
    │       ├─ Lógica de negocio compleja
    │       ├─ Interacción con Spring AI
    │       ├─ Llamadas a repositories
    │       └─ Manejo de errores específicos
    │
    ├─► 5) Mapper (bidireccional)
    │       ├─ @Mapper(componentModel = "spring")
    │       ├─ DTO → Domain (entrada)
    │       ├─ Domain → DTO (salida)
    │       └─ Sin lógica de negocio
    │
    └─► 6) Repository / External Services
            ├─ Spring Data JPA
            ├─ PgVector queries
            ├─ Spring AI clients
            └─ Ollama integration

HTTP Response
```

---

## 🔧 Integración con SDD

- para hacer el documento de spec, plan o task, hay que usar los prompts excpecificos de cada uno.

### Ciclo SDD para Nuevas Funcionalidades

```
┌──────────────────────────────────────────────────────────┐
│  ANTES DE ESCRIBIR CÓDIGO → COMPLETAR CICLO SDD         │
└──────────────────────────────────────────────────────────┘

1. SPEC (Especificación)
   └─► Archivo: specs/[feature]-spec.md
   └─► Define: Qué, Por qué, Alcance, Restricciones

2. PLAN (Diseño Técnico)
   └─► Archivo: plans/[feature]-plan.md
   └─► Define: Cómo, Arquitectura, Decisiones, Dependencias

3. TASKS (Lista de Tareas)
   └─► Archivo: tasks/[feature]-tasks.md
   └─► Define: Pasos atómicos, Orden, Criterios de éxito

4. GENERATION (Desarrollo Asistido)
   └─► Usar prompts en: prompts/
   └─► Generar: Código, Tests, Documentación

5. REVIEW (Validación)
   └─► Testing: Unitario + Integración
   └─► Code review: Arquitectura + Estándares
   └─► Documentación: Actualizar si es necesario
```

---

## ✅ Checklist de Calidad

Antes de considerar una feature completa, verificar:

### Arquitectura
- [ ] Separación clara de responsabilidades
- [ ] Interfaces definidas antes de implementaciones
- [ ] Dependencias inyectadas vía constructor
- [ ] Sin lógica de negocio en controllers
- [ ] Mappers sin estado ni lógica compleja

### Código
- [ ] Nombres descriptivos (clases, métodos, variables)
- [ ] Métodos cortos (< 30 líneas idealmente)
- [ ] Clases cohesivas (< 300 líneas idealmente)
- [ ] Logging apropiado (INFO, ERROR, DEBUG)

### Testing
- [ ] Tests unitarios para service layer (> 80% coverage)
- [ ] Tests de integración para controllers
- [ ] Tests de mappers (edge cases)
- [ ] Manejo de errores testeado

### Documentación
- [ ] JavaDoc en interfaces públicas
- [ ] OpenAPI completo en controllers
- [ ] README actualizado si hay cambios en API
- [ ] Ejemplos de uso disponibles

### SDD
- [ ] Spec creada y revisada
- [ ] Plan técnico documentado
- [ ] Tasks completadas y marcadas
- [ ] Decisiones técnicas justificadas

---

## 📚 Referencias

- [Spring Boot Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [MapStruct Documentation](https://mapstruct.org/documentation/stable/reference/html/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

## 🔄 Evolución de este Documento

Este documento debe evolucionar con el proyecto:

- Agregar nuevos patrones conforme aparezcan
- Documentar decisiones arquitectónicas importantes
- Actualizar ejemplos con casos reales del proyecto
- Incorporar aprendizajes del proceso SDD

**Última actualización**: [Fecha]  
**Versión**: 1.0  
**Mantenedores**: [Equipo de desarrollo]## Convenciones de nombres (IA)
- Seguir los nombres de `constitution.md`: clases y records en `PascalCase`, metodos/variables en `lowerCamelCase`, constantes en `UPPER_SNAKE_CASE`, paquetes en minusculas, DTOs/mappers con sufijos coherentes, tests con sufijo `Test` y metodos descriptivos.

## Checklist de salida (IA)
- Ejecutar `mvn test` (o subset relevante) y revisar fallos.
- Pasar linters/formatters configurados si existen.
- Verificar contratos de API y OpenAPI alineados tras los cambios.
- Revisar nombres y convenciones segun este documento y `constitution.md`.
- Actualizar artefactos SDD (spec/plan/tasks) si cambio el alcance o el comportamiento.
- Resumir cambios y riesgos pendientes en la respuesta de salida.
