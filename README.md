# PROYECTO SEGUNDO CORTE: SIMULADOR DE REGRESIÓN LINEAL

## INTRODUCCIÓN
- Este proyecto implementa un simulador interactivo de regresión lineal desarrollado en Kotlin con Ktor y un frontend web, que permite visualizar cómo se ajusta una recta a un conjunto de puntos.
- El objetivo es comprender de forma práctica cómo se calcula la línea de mejor ajuste en un modelo de regresión lineal simple, mostrando tanto la ecuación obtenida como el coeficiente de determinación (R²), todo de manera visual y sencilla.
- La aplicación también permite guardar datasets, consultarlos y gestionarlos desde una base de datos SQLite, integrando de forma completa el flujo entre frontend, backend y persistencia.
---

### ¿Qué es la regresión lineal?
La regresión lineal es una técnica estadística que busca encontrar la relación matemática entre dos variables:
- Variable independiente (x): aquella que se controla o conoce.
- Variable dependiente (y): aquella cuyo valor depende de x.
El modelo más simple se expresa mediante la ecuación:
$$ y = mx + b $$
Donde:
- *m* es la pendiente: Indica cómo cambia *y* cuando *x* cambia en una unidad.
- *b* la intersección: El valor de *y* cuando *x* = 0.
En resumen, la regresión lineal busca la “línea que mejor se ajusta” a los datos, permitiendo predecir nuevos valores.

### Cálculo de componentes
#### Para *m*
$$ m = \frac{(n \sum_ xy - \sum_ x \sum_ y)}{(n \sum_ x^2 - (\sum_ x)^2)} $$
#### Para *b*
$$
b = \frac{(\sum_ y - m \sum_ x)}{(n)}
$$

---

## Estructura del proyecto
```
src/
├── main/
│ ├── resources/
│ │ └── static/ # Archivos del frontend (index.html, app.js, styles.css)
│ └── kotlin/com/example/
│ ├── routes/ # Define las rutas HTTP
│ ├── service/ # Lógica de cálculo (RegressionService)
│ ├── models/ # Estructuras de datos (DTOs)
│ ├── plugins/ # Configuración (CORS, routing, serialización)
│ └── db/ # Configuración de SQLite y migraciones
├── build.gradle.kts # Scripts y configuración de Gradle
└── settings.gradle.kts
```

## GUÍA PARA EJECUTAR EL PROYECTO EN LOCAL
#### Requisitos previos:
	- JDK 17 (no JRE) — verifica con java -version
	- Git
	- Internet para descargar dependencias (Gradle Wrapper)
#### Clonar el repositorio
```bash
git clone https://github.com/DavidAvendanoUSA/Proyecto-Segundo-Corte.git
cd Proyecto-Segundo-Corte
```
#### Ejecución:
Copia los comandos en orden
- En Windows Powershell:
```powershell
cd "C:\ruta\al\proyecto\kotlin-linear-regression-web"

.\gradlew.bat clean build

$env:PORT="8080"; .\gradlew.bat run
```
Aaccede: http://localhost:8080 -      Detener: Ctrl + C
- macOS / Linux
```bash
cd /ruta/al/proyecto/kotlin-linear-regression-web

./gradlew clean build

PORT=8080 ./gradlew run
```
Accede: http://localhost:8080  -     Detener: Ctrl + C

---

## Comunicación entre el Frontend y el Backend
1. El frontend (navegador) toma los puntos ingresados por el usuario y los convierte a JSON.
2. Envía ese JSON al backend mediante una petición HTTP.
3. El backend calcula la regresión lineal y responde con un JSON que incluye pendiente, intersección y R².
4. El frontend muestra la ecuación y dibuja la gráfica.
5. Si el usuario guarda un dataset, el backend lo persiste en SQLite.


## Formato de datos (JSON)
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


## Endpoints principales
| Método | Endpoint | Descripción |
|:-------|:----------|:-------------|
| **POST** | `/api/regression` | Calcula la regresión lineal con los puntos enviados. |
| **POST** | `/api/datasets` | Guarda un dataset en la base de datos. |
| **GET** | `/api/datasets` | Lista los datasets almacenados. |
| **GET** | `/api/datasets/{id}` | Devuelve el dataset completo y su regresión. |
| **DELETE** | `/api/datasets/{id}` | Elimina un dataset por su ID. |


## Dependencias principales
| Librería | Descripción | Uso |
|:----------|:-------------|:----|
| **Kotlin / Ktor** | Lenguaje y framework backend | Servidor HTTP y rutas |
| **kotlinx.serialization** | Serialización JSON | Enviar y recibir datos JSON |
| **HikariCP** | Pool de conexiones | Optimizar acceso a base de datos |
| **SQLite (JDBC)** | Base de datos local | Guardar datasets |
| **Exposed / Flyway** | ORM y migraciones | Crear y mantener tablas |
| **Chart.js** | Librería JS | Mostrar gráficos en el frontend |

---

## EJEMPLOS & CASOS LÍMITE:
#### Ejemplo válido ✅:
Input:
```bash
1,2
2,3.5
3,5
```
Resultados esperado:
```json
{
  "slope": 1.5,
  "intercept": 0.1666,
  "equation": "y = 1.5x + 0.1666",
  "r2": 0.99
}
```
	- curl (sin usar la UI):
```bash
curl -s -X POST http://localhost:8080/api/regression \
  -H "Content-Type: application/json" \
  -d '{"points":[{"x":1,"y":2},{"x":2,"y":3.5},{"x":3,"y":5}]}'
```
	- (HTTP 200) — ejemplo de JSON:
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
#### Caso extremo: Menos de dos puntos ❌
Input:
```
1,2
```
	- curl:
```bash
curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/api/regression \
  -H "Content-Type: application/json" \
  -d '{"points":[{"x":1,"y":2}]}'
```
	- HTTP status: 400
```json
{ "error": "Se requieren al menos 2 puntos para una regresión lineal." }
```
	En la UI, app.js mostrará el mismo mensaje de error.
#### Caso extremo: todos los X iguales (no se puede calcular la pendiente) ❌
Input:
```
1,2
1,3
1,4
```
	- curl:
```bash
curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/api/regression \
  -H "Content-Type: application/json" \
  -d '{"points":[{"x":1,"y":2},{"x":1,"y":3},{"x":1,"y":4}]}'
```
	- HTTP status: 400 (uno de estos mensajes según validación):
```json
{ "error": "Todos los valores de X son idénticos. No se puede calcular la pendiente." }
```
o
```json
{ "error": "No se puede calcular la pendiente: denominador 0." }
```
#### Datos no numéricos (formato inválido) ❌
Input:
```
1,2
dos,3
```
	- curl con JSON mal formado (no numérico en JSON):
```bash
curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/api/regression \
  -H "Content-Type: application/json" \
  -d '{"points":[{"x":1,"y":2},{"x":"dos","y":3}]}'
```
	- HTTP status: 400
```json
{ "error": "Punto #2 inválido: x e y deben ser números finitos." }
```
#### JSON malformado (syntax error) ❌
	- curl:
```bash
curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/api/regression \
  -H "Content-Type: application/json" \
  -d '{"points": [
```

---

Autores:
- David Alejandro Avendaño López
- Laura Valentina Niño Rosas
- Dylan David Torres Mancipe
