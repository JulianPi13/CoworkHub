# 🏢 CoWorkHub

Aplicación web para gestionar una red de coworking: **sedes, salas, equipamiento, servicios adicionales, planes de membresía, miembros y reservas**.

El sistema evita reservas duplicadas (también con solicitudes simultáneas), controla las horas que consume cada miembro, aplica una regla única para cobrar cancelaciones y deja que cada usuario use solo las funciones y los datos de su rol.

- **Backend:** API REST con Spring Boot, en [`backend/`](backend).
- **Frontend:** HTML, CSS y JavaScript puro que consume la API, en [`frontend/`](frontend).
- **Base de datos:** MySQL 8.

> Proyecto final Spring Boot R2 (módulos 01 a 08). Trabajo individual.

## 👤 Integrantes

| Nombre | Rol |
|---|---|
| Oscar Julian Pinto Uribe | Desarrollo backend y frontend |

## 🧰 Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1.3 (Web MVC, Data JPA, Validation, Security, Actuator) |
| Persistencia | Hibernate 7 / JPA, MySQL 8 (`mysql-connector-j`) |
| Seguridad | Spring Security 7, JWT (jjwt 0.13, HS256, *stateless*), BCrypt |
| Documentación | springdoc-openapi 3.1 (Swagger UI) |
| Build | Maven (incluye Maven Wrapper: `mvnw` / `mvnw.cmd`) |
| Pruebas | JUnit 5 + AssertJ (unitarias), Postman / Newman (aceptación) |
| Frontend | HTML5, CSS3, JavaScript (ES2020, sin frameworks) |

## ✅ Requisitos previos

- **JDK 21.** Para comprobarlo: `java -version`.
- **MySQL 8**, instalado en el equipo o en Docker.
- **Maven** no hace falta instalarlo: el proyecto trae `mvnw`. Si lo tienes instalado, `mvn` también sirve.
- Postman o Insomnia, opcional para probar la colección.
- Para servir el frontend, cualquier servidor estático: la extensión Live Server de VS Code, `python -m http.server` o `npx serve`.

## 🐬 Preparar MySQL

La aplicación se conecta a `jdbc:mysql://localhost:3306/coworkhub` con el usuario `coworkhub` y la contraseña `coworkhub`. Estas credenciales son solo para desarrollo.

### Opción A: MySQL instalado en el equipo

Conéctate como `root` desde MySQL Workbench o la consola y ejecuta [`backend/db/crear-base-de-datos.sql`](backend/db/crear-base-de-datos.sql):

```sql
CREATE DATABASE IF NOT EXISTS coworkhub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'coworkhub'@'localhost' IDENTIFIED BY 'coworkhub';
GRANT ALL PRIVILEGES ON coworkhub.* TO 'coworkhub'@'localhost';
FLUSH PRIVILEGES;
```

Desde la consola:

```powershell
mysql -u root -p < backend/db/crear-base-de-datos.sql
```

### Opción B: Docker

```bash
docker run -d --name coworkhub-mysql \
  -e MYSQL_DATABASE=coworkhub \
  -e MYSQL_USER=coworkhub \
  -e MYSQL_PASSWORD=coworkhub \
  -e MYSQL_ROOT_PASSWORD=root \
  -p 3306:3306 \
  -v coworkhub-datos:/var/lib/mysql \
  mysql:8.4
```

En Windows (PowerShell o CMD) escríbelo en una sola línea, sin las `\`. Antes de arrancar el proyecto, espera a que `docker logs coworkhub-mysql` muestre `ready for connections`.

| Para... | Comando |
|---|---|
| Ver si está corriendo | `docker ps` |
| Detenerlo y volver a iniciarlo | `docker stop coworkhub-mysql` · `docker start coworkhub-mysql` |
| Abrir la consola de MySQL | `docker exec -it coworkhub-mysql mysql -u coworkhub -p coworkhub` |
| Borrarlo con todos sus datos | `docker rm -f coworkhub-mysql` y luego `docker volume rm coworkhub-datos` |

Si el puerto 3306 está ocupado, usa `-p 3307:3306` y define `DB_URL=jdbc:mysql://localhost:3307/coworkhub`.

### Tablas y datos de prueba

No hace falta crear las tablas a mano. Con `spring.jpa.hibernate.ddl-auto=create`, Hibernate hace esto en cada arranque:

1. Borra y vuelve a crear las tablas.
2. Ejecuta [`backend/src/main/resources/import.sql`](backend/src/main/resources/import.sql) con los datos de prueba.

Así siempre se empieza con los mismos datos y sin duplicados.

Las fechas de las reservas se calculan a partir de **hoy**, con `TIMESTAMP(CURDATE() + INTERVAL n DAY, 'HH:MM:SS')`. Por eso las reservas futuras nunca quedan en el pasado.

Además está [`backend/src/main/resources/migration.sql`](backend/src/main/resources/migration.sql), un script independiente con la estructura y los datos de prueba, por si quieres crear la base sin arrancar la aplicación:

```powershell
mysql -u coworkhub -p coworkhub < backend/src/main/resources/migration.sql
```

| Datos | Cargados |
|---|---|
| Sedes | 2 (una abierta 24 h) |
| Salas | 6, una inactiva, con su equipamiento |
| Equipamiento | 4 |
| Servicios adicionales | 3 |
| Planes | 4 |
| Miembros | 6, uno suspendido |
| Usuarios | 4: 1 ADMIN, 1 RECEPTIONIST y 2 MEMBER vinculados a su miembro |
| Reservas | 15 en distintos estados, 5 detalles de servicios |

## ⚙️ Configuración con variables de entorno

Todas tienen un valor por defecto en [`application.properties`](backend/src/main/resources/application.properties), así que el proyecto arranca sin definir ninguna.

| Variable | Por defecto | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/coworkhub` | URL JDBC de MySQL |
| `DB_USERNAME` | `coworkhub` | Usuario de MySQL |
| `DB_PASSWORD` | `coworkhub` | Contraseña de MySQL |
| `JWT_SECRET` | clave de desarrollo | Clave HS256, de **al menos 32 caracteres**. Se valida al arrancar con `@PostConstruct` |
| `JWT_EXPIRATION_MINUTES` | `60` | Vigencia del token |
| `CORS_ORIGINS` | `http://localhost:5500,...` | Orígenes del frontend que pueden llamar a la API |

Ejemplo en PowerShell:

```powershell
$env:DB_PASSWORD="otraClave"; $env:JWT_SECRET="una-clave-muy-larga-de-al-menos-32-caracteres"; ./mvnw.cmd spring-boot:run
```

## ▶️ Ejecución

### Backend

```powershell
cd backend
./mvnw.cmd spring-boot:run      # Windows
# ./mvnw spring-boot:run        # Linux / macOS
# mvn spring-boot:run           # si tienes Maven instalado
```

La API queda en **http://localhost:8080**. Para comprobar que está viva:

```powershell
curl http://localhost:8080/actuator/health      # {"status":"UP"}
```

Para correr las pruebas unitarias:

```powershell
./mvnw.cmd test
```

### Frontend

El frontend es estático y llama a la API definida en [`frontend/js/config.js`](frontend/js/config.js) (`http://localhost:8080`). Sírvelo en el puerto **5500**, que es el que tiene permitido CORS:

```powershell
cd frontend
python -m http.server 5500
```

También puedes usar la extensión **Live Server** de VS Code, que usa el puerto 5500 por defecto.

Luego abre **http://localhost:5500**.

| Vista | Qué permite |
|---|---|
| Inicio | Resumen. Un MEMBER ve su consumo de horas del mes |
| Disponibilidad | Buscar salas libres por sede, fecha, horario, capacidad y equipamiento. Desde ahí se puede reservar |
| Reservas | Crear reservas con servicios; listar con filtros; ver el detalle; agregar o quitar servicios; confirmar, completar y cancelar |
| Miembros | ADMIN y RECEPTIONIST: registrar, editar, suspender, activar, eliminar y ver el consumo |
| Catálogos | Todos consultan. Solo el ADMIN crea, edita y elimina sedes, salas, equipamiento, servicios y planes |

## 🔐 Roles

| Rol | Qué puede hacer |
|---|---|
| `ADMIN` | Todo. Es el único que crea, modifica o elimina catálogos |
| `RECEPTIONIST` | Gestiona los miembros y todas las reservas |
| `MEMBER` | Consulta catálogos y disponibilidad. Crea, ve, modifica y cancela **solo sus** reservas, y ve **solo su** consumo |

Los permisos se aplican con `@PreAuthorize`, que combina roles con reglas de dueño del recurso (`@authz.isSelf(#id)` y `@authz.ownsBooking(#id)`). Si un MEMBER pide datos de otro miembro, recibe `403`.

### Usuarios de prueba

| Usuario | Contraseña | Rol | Miembro |
|---|---|---|---|
| `admin` | `Admin123` | ADMIN | — |
| `recepcion` | `Recep123` | RECEPTIONIST | — |
| `ana` | `Ana12345` | MEMBER | Ana Gómez (id 1, Plan Básico) |
| `luis` | `Luis12345` | MEMBER | Luis Pérez (id 2, Plan Profesional) |

En `import.sql` las contraseñas están guardadas con **BCrypt**.

## 📘 Swagger

- **Swagger UI:** http://localhost:8080/doc/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs. Hay una copia exportada en [`docs/openapi.json`](docs/openapi.json).

La documentación tiene:

- grupos: Toda la API, Autenticación, Catálogos, Miembros, Disponibilidad y reservas;
- descripciones y ejemplos;
- respuestas de error (400, 401, 403, 404, 409) con el cuerpo de error;
- el botón **Authorize**: haz `POST /auth/login`, copia el `token` y pégalo ahí.

## 🧭 Endpoints

Todos, salvo los públicos, llevan `Authorization: Bearer <token>`.

| Método | Ruta | Acceso | RF |
|---|---|---|---|
| POST | `/auth/login` | Público | RF-11 |
| GET | `/auth/perfil` | Autenticado | RF-14 |
| GET | `/actuator/health` | Público | RF-15 |
| GET | `/doc/swagger-ui.html` · `/v3/api-docs` | Público | RF-13 |
| GET · GET /{id} | `/api/{sedes, salas, equipamientos, servicios-adicionales, planes}` | Autenticado (salas acepta `?sedeId=`) | RF-01 |
| POST · PUT · DELETE | `/api/{recurso}` · `/api/{recurso}/{id}` | ADMIN | RF-01 |
| GET | `/api/miembros?nombre=&estado=` | ADMIN, RECEPTIONIST | RF-02 |
| GET | `/api/miembros/{id}` | ADMIN, RECEPTIONIST, MEMBER (solo el suyo) | RF-02 |
| POST · PUT · DELETE | `/api/miembros` · `/api/miembros/{id}` | ADMIN, RECEPTIONIST | RF-02 |
| PATCH | `/api/miembros/{id}/suspender` · `/activar` | ADMIN, RECEPTIONIST | RF-02 |
| GET | `/api/miembros/{id}/consumo?mes=yyyy-MM` | ADMIN, RECEPTIONIST, MEMBER (solo el suyo) | RF-10 |
| GET | `/api/disponibilidad?sedeId=&fecha=&horaInicio=&horaFin=` (+`capacidadMinima`, `equipamientoIds`) | Autenticado | RF-03 |
| POST | `/api/reservas` | ADMIN, RECEPTIONIST, MEMBER (a su nombre) | RF-04 |
| GET | `/api/reservas/{id}` | ADMIN, RECEPTIONIST, MEMBER (si es suya) | RF-05 |
| GET | `/api/reservas?miembroId=` · `?salaId=&fecha=` · `?estado=` | ADMIN, RECEPTIONIST, MEMBER (con su `miembroId`) | RF-05 |
| POST · DELETE | `/api/reservas/{id}/servicios` · `/api/reservas/{id}/servicios/{servicioId}` | ADMIN, RECEPTIONIST, MEMBER (si es suya) | RF-06 |
| POST | `/api/reservas/{id}/confirmar` | ADMIN, RECEPTIONIST | RF-07 |
| POST | `/api/reservas/{id}/cancelar` | ADMIN, RECEPTIONIST, MEMBER (si es suya) | RF-08 |
| POST | `/api/reservas/{id}/completar` | ADMIN, RECEPTIONIST | RF-09 |

Todos los errores responden con el mismo cuerpo. En `codigo` va la regla violada (`RN-xx`) o el tipo de error (`VALIDACION`, `NO_AUTENTICADO`, `ACCESO_DENEGADO`, `NO_ENCONTRADO`):

```json
{"error": "La sala ya está reservada en ese horario", "codigo": "RN-01", "timestamp": "2030-06-03T13:00:00Z"}
```

## 🏗️ Arquitectura

```
backend/src/main/java/com/campus/coworkhub
├── config/          AppConfig (Clock @Primary + Clock @Qualifier("utcClock"), PasswordEncoder), CORS, OpenAPI
├── security/        SecurityConfig, JwtService (@Value + @PostConstruct), JwtAuthenticationFilter,
│                    UserAccountDetailsService, Authz (dueño del recurso), manejadores 401/403
├── presentation/    controller/ (REST, @Valid, @PreAuthorize) · advice/ (@RestControllerAdvice)
├── business/        service/ (interfaz) + service/impl/ (@Transactional) · booking/ (reglas RN) ·
│                    dto/ (record de entrada y salida) · mapper/ · exception/
└── persistence/     entity/ (JPA) · repository/ (consultas derivadas, @EntityGraph, @Lock)
```

| Módulo | Dónde se ve |
|---|---|
| **01** Spring Boot | Spring Initializr con *starters*; DTO como `record`; `Optional` y streams; inyección por constructor; Actuator |
| **02** Beans | Servicios como interfaz + `impl`; `@Configuration` con `@Bean` (`Clock`, `PasswordEncoder`); `@Value`; `@Primary` y `@Qualifier("utcClock")`; `@PostConstruct` en `JwtService` que valida la configuración |
| **03** JPA | Entidades con `@Table`, `@Column` y `@Enumerated`; repositorios con consultas derivadas (`existsByLocationIdAndNameIgnoreCase`, `countByMemberIdAndStatusInAndStartTimeAfter`, ...) |
| **04** Relaciones | 1→N, N↔N (`room_equipment`), 1↔1 (`Member`↔`UserAccount`) con lado dueño definido; `LAZY` en colecciones; `cascade` y `orphanRemoval` en `Booking`→`BookingItem` y `Member`→`UserAccount`; `@EntityGraph` en los listados para evitar N+1 |
| **05** API REST | Capas `presentation → business → persistence`; DTO de entrada con `@Valid` y DTO de salida (nunca entidades); `@Transactional` en las escrituras |
| **06** Swagger | Grupos, descripciones, errores con ejemplos, Authorize y `docs/openapi.json` |
| **07** Excepciones | `BusinessRuleException`, `ResourceNotFoundException` e `InvalidRequestException`; `GlobalExceptionHandler` con cuerpo único; ninguna regla responde 500 |
| **08** Seguridad | Primero Basic Auth (ver historial de commits) y después JWT *stateless*; BCrypt; `@PreAuthorize` con roles y dueño; 401 y 403 con el cuerpo de error |

### Reglas de negocio

| Regla | Implementación |
|---|---|
| RN-01 | `BookingRepository.existsOverlap`: hay cruce si `inicioExistente < finNuevo` y `finExistente > inicioNuevo`, así que dos reservas que se tocan no se cruzan. Para la **concurrencia**, la transacción bloquea la fila de la sala (`@Lock(PESSIMISTIC_WRITE)`, es decir `SELECT ... FOR UPDATE`) con aislamiento `READ_COMMITTED`. La segunda solicitud espera a que termine la primera, ve la reserva recién guardada y recibe 409 |
| RN-02 a RN-05, RN-09, RN-10 | `BookingValidator`, con el `Clock` inyectado |
| RN-06 | Cuenta las reservas `PENDING` o `CONFIRMED` futuras del miembro y las compara con `maxActiveBookings` |
| RN-07 | `BookingCostCalculator`. Las horas incluidas en el mes no se cobran; las excedentes valen `hourlyRate × (1 − descuento/100)`; los servicios suman `quantity × appliedUnitPrice` |
| RN-08 | `StandardCancellationPolicy`: 24 h o más, sin cargo y se devuelven las horas; entre 24 h y 2 h, 50 % del costo de la sala; menos de 2 h o ya empezada, 409. Una cancelación con cargo sigue contando en el consumo (campo `hoursRefunded`) |
| RN-11 | Se valida en el servicio (409) y además con restricciones `UNIQUE` en la base |
| RN-12 | Se comprueba con `exists...` antes de cada `DELETE` |
| RN-13 | `@PreAuthorize` en cada endpoint |

## 🧪 Pruebas y evidencias

### Colección de Postman: los 14 escenarios

El archivo es [`postman/CoWorkHub.postman_collection.json`](postman/CoWorkHub.postman_collection.json).

1. Impórtalo en Postman o Insomnia.
2. Con el backend corriendo, usa el **Runner** sobre toda la colección.
3. La carpeta *0 · Preparación* hace login y guarda los tokens.
4. La carpeta *1 · Escenarios de aceptación* tiene los 14 escenarios con aserciones.
5. La carpeta *2 · Pruebas adicionales* cubre RF-03, RF-06, RF-07, RF-09, RF-10, RN-04, RN-05, RN-10, RN-11 y los 403 de un MEMBER.

Desde consola, con Newman:

```powershell
npx newman run postman/CoWorkHub.postman_collection.json
```

| # | Escenario | Resultado |
|---|---|---|
| 1 | Reservar una sala libre con 2 servicios | `201` con costo calculado ✅ |
| 2 | Reservar en un horario que se cruza | `409 RN-01` ✅ |
| 3 | Reservar de 10:00 a 11:00 cuando otra va de 09:00 a 10:00 | `201` ✅ |
| 4 | Reservar 45 min, o fuera del horario de la sede | `409 RN-02` / `409 RN-03` ✅ |
| 5 | Cancelar con 30 h de anticipación | `200`, cargo 0 ✅ |
| 6 | Cancelar con 5 h de anticipación | `200`, cargo del 50 % ✅ |
| 7 | Cancelar con 1 h de anticipación | `409 RN-08` ✅ |
| 8 | Confirmar una reserva cancelada | `409 RN-09` ✅ |
| 9 | Un miembro consulta la reserva de otro | `403` ✅ |
| 10 | Llamar a un endpoint protegido sin token o con un token alterado | `401` ✅ |
| 11 | Eliminar una sala con reservas futuras | `409 RN-12` ✅ |
| 12 | Crear una sede sin nombre | `400` ✅ |
| 13 | Dos solicitudes simultáneas para la misma sala y horario | una `201` y otra `409` ✅ |
| 14 | Listar miembros con `?estado=SUSPENDED` | `200`, solo suspendidos ✅ |

Notas sobre la colección:

- Las reservas de prueba usan la **sala 5** (sede abierta 24 h) y el **miembro 6** (plan sin horas incluidas, para que el costo y el cargo sean mayores que 0).
- La fecha es aleatoria y futura, así que la colección se puede ejecutar varias veces sin reiniciar.
- El escenario 7 reserva a 70 minutos de la hora actual. Ejecútalo antes de las 22:20, porque más tarde la reserva cruzaría la medianoche.

### Pruebas unitarias

Hay 21 pruebas en `backend/src/test`. Usan un `Clock` fijo y cubren RN-02 a RN-05 y RN-07 a RN-10:

```powershell
cd backend
./mvnw.cmd test
```

### Evidencias de funcionamiento

En [`docs/evidencias/`](docs/evidencias) están:

| Archivo | Contenido |
|---|---|
| `01-swagger-ui.jpg` | Swagger UI con grupos y Authorize |
| `02-frontend-disponibilidad.jpg` | Búsqueda de salas libres |
| `03-frontend-reserva-creada.jpg` | Reserva creada con servicios y costo calculado |
| `04-frontend-miembros.jpg` | Gestión de miembros (RECEPTIONIST) |
| `05-frontend-catalogos-admin.jpg` | Catálogo de salas (ADMIN) |
| `06-postman-newman-resultado.txt` | Ejecución de la colección: **40 requests, 70 aserciones, 0 fallos** |

## 📁 Estructura del repositorio

```
coworkhub/
├── backend/                 Proyecto Spring Boot (pom.xml, mvnw, src/)
│   ├── db/crear-base-de-datos.sql
│   └── src/main/resources/  application.properties, import.sql, migration.sql
├── frontend/                index.html, css/, js/
├── postman/                 Colección con los 14 escenarios
├── docs/                    openapi.json y evidencias
└── README.md
```
