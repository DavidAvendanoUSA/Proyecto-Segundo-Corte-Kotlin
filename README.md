# PROYECTO SEGUNDO CORTE: SIMULADOR REGRESIÓN LINEAL
---
## ¿Qué es la regresión lineal?

La regresión lineal es una técnica estadística que nos ayuda a encontrar una relación matemática entre dos variables.
- Una es la variable independiente (representada en el eje *x*), aquella que conocemos y controlamos.
- La otra es la independiente (representada en el eje *y*), ya que depende de la primera.

El modelo más sencillo es una línea resta de la forma 

$$ y = mx + b $$

Donde:
    - *m* es la pendiente: Indica cómo cambia *y* cuando *x* cambia en una unidad.
     *b* la intersección: El valor de *y* cuando *x* = 0.

En pocas palabras: la regresión lineal busca la “línea que mejor se ajusta” a los datos para predecir valores nuevos.

---

### Cómo calcular cada componente

### Para *m*

$$ m = \frac{(n \sum_ xy - \sum_ x \sum_ y)}{(n \sum_ x^2 - (\sum_ x)^2)} $$

### Para *b*

$$
b = \frac{(\sum_ y - m \sum_ x)}{(n)}
$$



---



# Guía para ejecutar en local.


## Requisitos
- JDK 17 (no JRE) — verifica con `java -version` que sea 17.x
- Git
- Internet para descargar dependencias (usa Gradle Wrapper)

## Clonar el proyecto

```bash
git clone https://github.com/DavidAvendanoUSA/Proyecto-Segundo-Corte-Kotlin.git
cd Proyecto-Segundo-Corte-Kotlin
```

Reemplaza `<owner>/<repo>` por el repositorio real (por ejemplo `DylanDD17/kotlin-linear-regression-web`).

## Ejecutar

Copia los comandos en orden.

### Windows (PowerShell)
```powershell
cd "C:\ruta\al\proyecto\kotlin-linear-regression-web"

.\gradlew.bat clean build

$env:PORT="8080"; .\gradlew.bat run
```
Abre el navegador: http://localhost:8080 -      Detener: Ctrl + C

### macOS / Linux
```bash
cd /ruta/al/proyecto/kotlin-linear-regression-web

./gradlew clean build

PORT=8080 ./gradlew run
```
Abre el navegador: http://localhost:8080  -     Detener: Ctrl + C

Nota: En Windows usa `.\gradlew.bat`. Si tienes varios Java, asegúrate de usar JDK 17.
____

## Comunicacion entre el Front y el Back 
- El frontend (navegador) toma los puntos que escribe el usuario y los convierte a JSON.
- El frontend envía ese JSON al backend mediante una petición HTTP.
- El backend recibe el JSON, calcula la regresión lineal y devuelve un JSON con el resultado.
- El frontend muestra la ecuación y dibuja la gráfica con los datos recibidos.
- Si se guarda un dataset, el backend lo persiste en SQLite y ofrece endpoints para listar, obtener y borrar datasets.

### Formato de datos (contrato JSON)
- Punto:
```json
{ "x": 1.0, "y": 2.0 }
```
- Request para cálculo de regresión:
```json
{ "points": [ { "x": 1.0, "y": 2.0 }, { "x": 2.0, "y": 3.5 } ] }
```
- Response del servidor (RegressionResult):
```json
{
  "n": 2,
  "slope": 1.5,
  "intercept": 0.3,
  "equation": "y = 1.5x + 0.3",
  "r2": 0.98,
  "minX": 1.0,
  "maxX": 2.0,
  "linePoints": [{ "x": 1.0, "y": 1.8 }, { "x": 2.0, "y": 3.3 }]
}
```

### Endpoints relevantes
- POST /api/regression  
  - Entrada: `{ "points": [PointDTO] }`  
  - Salida: `RegressionResult` (JSON)  
  - Uso: calcula la recta de regresión con los puntos enviados.
- POST /api/datasets  
  - Entrada: `{ "name": "nombre", "points": [PointDTO] }`  
  - Uso: guarda un dataset en la base de datos.
- GET /api/datasets  
  - Uso: lista los datasets guardados (resumenes).
- GET /api/datasets/{id}  
  - Uso: devuelve el dataset completo y su regresión.
- DELETE /api/datasets/{id}  
  - Uso: elimina un dataset por id.

  _______

  ## Carpetas principales
- src/main/resources/static/  
  Archivos que ve el navegador: index.html (página), app.js (lógica del cliente), styles.css (estilos).
- src/main/kotlin/com/example/  
  Código del servidor (Kotlin + Ktor).
  - routes/ — Define las rutas HTTP (ej. /api/regression, /api/datasets).
  - service/ — Lógica de cálculo (RegressionService).
  - models/ — Formatos de datos (PointDTO, RegressionRequest, RegressionResult).
  - plugins/ — Configuración (routing, CORS, serialización).
  - db/ — Inicialización y migraciones de SQLite.
- build files (gradle, scripts)  
  Para compilar y ejecutar la aplicación.

_____

## Dependencias principales 

Esta sección lista las dependencias más importantes usadas en el proyecto y para qué sirven.

- Kotlin / Ktor  
  - Qué es: el lenguaje (Kotlin) y el marco web (Ktor) que ejecuta el servidor.  
  - Para qué se usa: recibir peticiones HTTP, gestionar rutas y devolver respuestas JSON.

- kotlinx.serialization  
  - Qué es: la herramienta que convierte objetos Kotlin a JSON y viceversa.  
  - Para qué se usa: recibir el JSON que manda el frontend y enviar el JSON con la regresión.

- HikariCP (conector de base de datos)  
  - Qué es: un componente que administra las conexiones a la base de datos.  
  - Para qué se usa: conectar el servidor con SQLite de forma eficiente.

- SQLite (driver JDBC)  
  - Qué es: la base de datos ligera que guarda los datasets localmente.  
  - Para qué se usa: almacenar nombre, fecha y puntos de cada dataset guardado.

- Exposed / Flyway (migraciones y acceso a BD)  
  - Qué son: utilidades para trabajar con la base de datos y aplicar cambios de estructura (migraciones).  
  - Para qué se usan: crear tablas y leer/escribir los datos sin escribir SQL manualmente en cada operación.

- Chart.js (biblioteca del frontend)  
  - Qué es: librería JavaScript para dibujar gráficos en la página.  
  - Para qué se usa: mostrar el scatter plot de los puntos y la línea de regresión.  
  - Dónde está: incluida en index.html vía CDN:
  ```html
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  ```
_____

## Ejemplos de uso y casos extremos 

A continuación hay ejemplos de uso de la aplicacion donde se ve la entrada y el resultado esperado. 


### 1) Ejemplo válido — calcular regresión
Texto para pegar en la caja (frontend):
```
1,2
2,3.5
3,5
```

curl (sin usar la UI):
```bash
curl -s -X POST http://localhost:8080/api/regression \
  -H "Content-Type: application/json" \
  -d '{"points":[{"x":1,"y":2},{"x":2,"y":3.5},{"x":3,"y":5}]}'
```

Resultado esperado (HTTP 200) — ejemplo de JSON:
```json
{
  "n": 3,
  "slope": 1.5,
  "intercept": 0.16666666666666696,
  "equation": "y = 1.5x + 0.16666666666666696",
  "r2": 0.9923076923076923,
  "minX": 1.0,
  "maxX": 3.0,
  "linePoints": [{ "x": 1.0, "y": 1.666666666666667 }, { "x": 3.0, "y": 4.666666666666667 }]
}
```

---

### 2) Caso extremo: menos de 2 puntos
Texto para la UI:
```
1,2
```

curl:
```bash
curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/api/regression \
  -H "Content-Type: application/json" \
  -d '{"points":[{"x":1,"y":2}]}'
```

Resultado esperado:
- HTTP status: 400
- Body:
```json
{ "error": "Se requieren al menos 2 puntos para una regresión lineal." }
```

En la UI, app.js mostrará el mismo mensaje de error.

---

### 3) Caso extremo: todos los X iguales (no se puede calcular la pendiente)
Texto para la UI:
```
1,2
1,3
1,4
```

curl:
```bash
curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/api/regression \
  -H "Content-Type: application/json" \
  -d '{"points":[{"x":1,"y":2},{"x":1,"y":3},{"x":1,"y":4}]}'
```

Resultado esperado:
- HTTP status: 400
- Body (uno de estos mensajes según validación):
```json
{ "error": "Todos los valores de X son idénticos. No se puede calcular la pendiente." }
```
o
```json
{ "error": "No se puede calcular la pendiente: denominador 0." }
```

---

### 4) Datos no numéricos (formato inválido)
Texto para la UI (entrada libre):
```
1,2
dos,3
```
Al pegar esto en la UI y pulsar "Calcular", el frontend validará y mostrará:
- Mensaje: `Línea 2: x e y deben ser numéricos` (o similar)

curl con JSON mal formado (no numérico en JSON):
```bash
curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/api/regression \
  -H "Content-Type: application/json" \
  -d '{"points":[{"x":1,"y":2},{"x":"dos","y":3}]}'
```

Resultado esperado:
- HTTP status: 400
- Body:
```json
{ "error": "Punto #2 inválido: x e y deben ser números finitos." }
```

---

### 5) JSON malformado (syntax error)
curl:
```bash
curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/api/regression \
  -H "Content-Type: application/json" \
  -d '{"points": [
