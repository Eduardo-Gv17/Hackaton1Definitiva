# Hackathon #1: Oreo Insight Factory 🍪📈

Este proyecto es la implementación de un backend para la Hackathon #1, diseñado para registrar ventas de Oreo, gestionar usuarios por roles y generar reportes asíncronos de ventas utilizando IA (GitHub Models).

---

## 1. 👥 Información del Equipo

* **Nombre Completo:** `Eduardo Salvador Guevara Vargas`
    * **Código UTEC:** `202410096`
* **Nombre Completo:** `(Nombre Apellido)`
    * **Código UTEC:** `(Código)`
* **Nombre Completo:** `(Nombre Apellido)`
    * **Código UTEC:** `(Código)`
* **Nombre Completo:** `(Nombre Apellido)`
    * **Código UTEC:** `(Código)`


---

## 2. 🚀 Instrucciones para Ejecutar el Proyecto

### Pre-requisitos
* Java 21+
* Maven 3.x
* Docker (para la base de datos PostgreSQL)

### Pasos para Ejecutar
1.  **Clonar el Repositorio:**
    ```bash
    git clone (URL_DE_TU_REPO)
    cd Hackaton1Definitiva
    ```

2.  **Crear el archivo `.env`:**
    En la raíz del proyecto, crea un archivo llamado `.env`. Copia la plantilla de variables de entorno (entregada por Canvas) y llénala con tus credenciales (GitHub Token, Gmail App Password, etc.).

3.  **Iniciar la Base de Datos (Docker Compose):**
    ```bash
    docker-compose up -d
    ```

4.  **Ejecutar la Aplicación Spring Boot:**
    La aplicación leerá automáticamente el archivo `.env` gracias a la dependencia `spring-dotenv`.
    ```bash
    mvn spring-boot:run
    ```
    El servidor estará corriendo en `http://localhost:8080`.

---

## 3. 🤖 Instrucciones para Correr el Postman Flow

1.  **Importar la Colección:**
    Importa el archivo `OreoHackathon.json` (incluido en este repositorio) en tu Postman.

2.  **Configurar Variables de Entorno (Opcional):**
    La colección está diseñada para guardar automáticamente los tokens (`centralToken`, `branchToken`) después de los requests de Login.

3.  **Ejecutar el Flow (o la Colección):**
    Busca el "Runner" de Postman y ejecuta la colección completa. El flow validará la secuencia E2E:
    * `Register CENTRAL` -> `Login CENTRAL` (Guarda token)
    * `Register BRANCH` -> `Login BRANCH` (Guarda token)
    * Crear ventas (con token CENTRAL)
    * Listar ventas (con token BRANCH, valida permisos)
    * Solicitar resumen asíncrono (valida 202 Accepted)
    * Intentar crear venta en otra sucursal (valida 403 Forbidden)
    * Eliminar venta (con token CENTRAL, valida 204)

---

## 4. ⚡ Explicación de la Implementación Asíncrona

El procesamiento de resúmenes de ventas es una tarea pesada que no debe bloquear al usuario. Se implementó de la siguiente manera:

1.  **Controlador (Respuesta Inmediata):** El `SalesController` recibe la solicitud en `/sales/summary/weekly`. En lugar de procesarla, publica un evento (`ReportRequestedEvent`) usando `ApplicationEventPublisher` y retorna inmediatamente un `202 Accepted`.

2.  **Listener (Proceso en Background):** Una clase `ReportProcessingListener` escucha ese evento. Su método `handleReportRequest` está anotado con `@EventListener` y `@Async`.

3.  **Ejecución:** Spring Boot toma este método y lo ejecuta en un *thread* separado (gestionado por el `ThreadPool` de `@EnableAsync`). Este *thread* es el que realiza el trabajo pesado:
    * Calcula los agregados (`SalesAggregationService`).
    * Llama a la API de GitHub Models (`GitHubModelsClient`).
    * Envía el email (`EmailService`).

---

## 5. 🪄 (Reto Extra) Documentación Endpoint Premium

Se implementó exitosamente el Reto Extra.

| Método | Endpoint | Descripción | Roles Permitidos |
|--------|----------|-------------|-----------------|
| POST | `/sales/summary/weekly/premium` | Solicita un reporte asíncrono con email en formato HTML, gráficos embebidos (vía QuickChart.io) y un PDF adjunto. | CENTRAL, BRANCH |

**Request Body (Ejemplo):**
```json
{
  "from": "2025-09-01",
  "to": "2025-09-07",
  "branch": "Miraflores", 
  "emailTo": "gerente@oreo.com",
  "format": "PREMIUM",
  "includeCharts": true,
  "attachPdf": true
}
```

***Response Body (202 Accepted):**
```json

{
  "requestId": "req_premium_abcdef12",
  "status": "PROCESSING",
  "message": "Su reporte premium está siendo generado.",
  "estimatedTime": "60-90 segundos",
  "features": ["HTML_FORMAT", "CHARTS", "PDF_ATTACHMENT"],
  "requestedAt": "2025-11-01T19:30:00Z"
}
```

