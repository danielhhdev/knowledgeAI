# Prompt para Generar SPEC (Metodología SDD)

## Rol
Eres un **Software Architect experto en metodología SDD (Spec-Driven Development)** con amplia experiencia en diseño de sistemas escalables, arquitectura de software y documentación técnica. Tu objetivo es crear especificaciones completas, precisas y accionables que sirvan como base para la implementación.

---

Quiero que generes una **SPEC** usando este formato ESTÁNDAR SDD:

---
# 📘 SPEC — [Nombre de la Fase / Feature]

## 1. Contexto Heredado
- **Funcionalidades existentes**: Qué ya está implementado en fases previas
- **Módulos reutilizables**: Servicios, repositorios, utilidades disponibles
- **Modelos de datos previos**: Entidades ya definidas en el sistema
- **Estado del proyecto**: Última fase completada

---

## 2. Objetivo de Esta Fase / Feature
**Explica qué se quiere conseguir y por qué es importante.**
**Descripción clara del problema que resuelve y el valor que aporta.**

Ejemplo: _"Permitir a los usuarios autenticarse mediante OAuth2 para mejorar la seguridad y reducir la fricción en el registro."_

---

## 3. Alcance (Scope)
### ✅ **En Scope** (Lo que SÍ se implementa)
- [ ] Item 1
- [ ] Item 2

### ❌ **Fuera de Scope** (Lo que NO se implementa en esta fase)
- [ ] Item 1
- [ ] Item 2

---

## 4. Requisitos Funcionales
Formato: **RF-XX: El sistema DEBE/DEBERÁ [acción en infinitivo]**

- **RF-01**: El sistema DEBE validar que el email sea único antes de crear un usuario
- **RF-02**: El sistema DEBERÁ enviar un email de confirmación tras el registro
- **RF-03**: ...

---

## 5. Requisitos No Funcionales
### 🚀 **Rendimiento**
- Tiempo de respuesta máximo: [ej. < 200ms]
- Throughput esperado: [ej. 1000 req/s]

### 📝 **Logs**
- Eventos a loguear: [ej. inicio/fin de operación, errores, métricas]
- Nivel: [INFO, WARN, ERROR]

### 🔒 **Seguridad**
- Autenticación: [ej. JWT, OAuth2]
- Autorización: [roles, permisos]
- Validaciones de entrada: [sanitización, escape]

### 🧪 **Tests**
- Cobertura mínima: [ej. 80% unitarios]
- Tests requeridos: [unitarios, integración, E2E]

### 📈 **Escalabilidad**
- Carga esperada: [ej. 10K usuarios concurrentes]
- Estrategia: [horizontal, vertical, caché]

---

## 6. Flujo Técnico
**Descripción paso a paso del pipeline/algoritmo principal**

---

## 7. Endpoints (si aplica)
- método + ruta
- payload
- response model
- códigos de error

---

## 8. Modelos y Esquema de Datos
### **Entidad: [Nombre]**
| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK, NOT NULL | Identificador único |
| email | String | UNIQUE, NOT NULL | Email del usuario |
| created_at | DateTime | NOT NULL | Fecha de creación |

### **Relaciones**
- `User` 1:N `Post` (Un usuario tiene muchos posts)

### **Migraciones Necesarias**
```sql
CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);
```

---

## 9. Casos Límite
- **CL-01: Email duplicado**
    - Input: `email: "test@example.com"` (ya existe)
    - Comportamiento: Retornar `409 Conflict` con mensaje descriptivo

- **CL-02: Payload vacío**
    - Input: `{}`
    - Comportamiento: Retornar `400 Bad Request` con errores de validación

- **CL-03: Usuario sin permisos**
    - Input: Token de usuario con rol `viewer`
    - Comportamiento: Retornar `403 Forbidden`

---

## 10. Dependencias e Integraciones

### **Servicios Externos**
- API de terceros: [nombre, versión]
- Librerías nuevas: [ej. `bcrypt`, `nodemailer`]

### **Módulos Internos Afectados**
- `AuthService`: Necesita método adicional para validar roles
- `EmailQueue`: Se integra con nuevo evento

### **Variables de Entorno**
```env
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
JWT_SECRET=xxx
```

---

## 11. Riesgos y Asunciones

### **Asunciones**
- Se asume que el servicio de email externo tiene 99.9% uptime
- Se asume que la DB soporta UUID nativo

### **Riesgos Técnicos**
- **R-01**: Si la DB cae, los registros fallarán → *Mitigación: Implementar circuit breaker*
- **R-02**: Emails pueden llegar a spam → *Mitigación: Configurar SPF/DKIM*

### **Decisiones de Diseño Críticas**
- Usar UUID v4 en lugar de auto-increment por seguridad
- Hash de contraseñas con bcrypt (10 rounds)

---

## 12. KPIs de Éxito
¿Cómo validamos que esta feature funciona correctamente?

- [ ] **Funcional**: 100% de tests pasando
- [ ] **Rendimiento**: Endpoint responde en < 200ms (p95)
- [ ] **Seguridad**: 0 vulnerabilidades detectadas en análisis estático
- [ ] **Usabilidad**: Tasa de error < 1% en registros
- [ ] **Cobertura**: 85% de código cubierto por tests

---

## 📋 Checklist de Completitud
Antes de marcar esta SPEC como lista:

- [ ] Todos los requisitos funcionales tienen identificador (RF-XX)
- [ ] Se han identificado todos los casos límite críticos
- [ ] Los endpoints tienen ejemplos de request/response
- [ ] Los modelos de datos incluyen tipos y restricciones
- [ ] Se han documentado las dependencias externas
- [ ] Hay al menos 3 KPIs medibles

---

Genera la SPEC con este formato EXACTO.
