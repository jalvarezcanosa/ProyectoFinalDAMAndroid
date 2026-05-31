# ContApp - Android📱

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" alt="ContApp Logo" width="100"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-11-orange?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Android-API%2028%2B-green?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Gradle-9.4.1-blue?style=for-the-badge&logo=gradle&logoColor=white"/>
  <img src="https://img.shields.io/badge/Retrofit-3.0.0-blueviolet?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Estado-Finalizado-brightgreen?style=for-the-badge"/>
</p>

---

## Índice

1. [Descripción del proyecto](#descripción-del-proyecto)
2. [Estado del proyecto](#estado-del-proyecto)
3. [Características y pantallas](#características-y-pantallas)
4. [Cómo acceder y ejecutar el proyecto](#cómo-acceder-y-ejecutar-el-proyecto)
5. [Tecnologías utilizadas](#tecnologías-utilizadas)

---

## Descripción del proyecto

**ContApp** es una aplicación Android para la creación y gestión de contadores grupales colaborativos. Permite a grupos de usuarios crear contadores compartidos, invitar a otros participantes mediante un código único, y competir entre sí acumulando puntos en tiempo real.

Un **contador** es la entidad central de la aplicación. Tiene un título, una descripción opcional, una imagen de portada subida desde la galería del dispositivo, y una fecha y hora de cierre opcionales. Una vez superada esa fecha, el contador pasa de estado `ABIERTO` a `CERRADO` y deja de aceptar nuevos puntos. Cada contador muestra tanto el conteo global (suma de todos los participantes) como el conteo individual del usuario autenticado, así como un ranking ordenado por puntuación.

El **sistema de invitación** funciona a través de un código único generado automáticamente por el backend al crear el contador. Este código se muestra en la pantalla de detalle y puede copiarse al portapapeles con un botón. Cualquier usuario registrado puede introducir ese código en la pantalla principal para unirse al contador.

La aplicación se comunica exclusivamente con un backend Django REST Framework a través de llamadas HTTP gestionadas por Retrofit. Toda la autenticación se realiza mediante tokens JWT que se almacenan en `SharedPreferences` y se inyectan automáticamente en cada petición mediante un interceptor de OkHttp.

---

## Estado del proyecto

**✅ Proyecto finalizado — versión 1.0**

Todas las funcionalidades planificadas están implementadas y operativas:

- ✅ Registro e inicio de sesión con JWT
- ✅ Persistencia de sesión entre reinicios de la app
- ✅ Cierre de sesión
- ✅ Creación de contadores con imagen, descripción y fecha de cierre
- ✅ Unirse a contadores mediante código de invitación
- ✅ Botón de incremento en tiempo real (+1)
- ✅ Ranking de participantes ordenado por puntuación
- ✅ Edición de contadores (solo el creador)
- ✅ Eliminación de contadores con confirmación (solo el creador)
- ✅ Pantalla de perfil con datos del usuario
- ✅ Navegación con `BottomNavigationView` y Jetpack Navigation Component
- ✅ Codificación por colores del estado del contador (verde/rojo)
- ✅ Refresco automático de datos al volver a cada pantalla

---

## Características y pantallas

### 🔐 Login y Registro — `LoginActivity`

Pantalla de entrada para usuarios no autenticados. Alterna entre modo login y modo registro mediante un botón de cambio de modo. Al arrancar comprueba si ya existe una sesión activa y salta directamente a la pantalla principal si es así.

**Campos:** email, contraseña (siempre visibles) + nombre de usuario y teléfono (solo en registro).

**Lógica destacada:** validación de campos en cliente, deshabilitación del botón durante la petición para evitar envíos dobles, mensajes de error específicos según el tipo de fallo.

---

### 🏠 Home — `HomeFragment`

Panel principal de la aplicación. Muestra todos los contadores en los que el usuario participa mediante un `RecyclerView`. Se refresca automáticamente cada vez que el usuario vuelve a esta pantalla (`onResume`).

**Acciones disponibles:**
- Botón flotante (FAB) para crear un nuevo contador.
- Botón `Unirse` que abre un `AlertDialog` con campo de texto para introducir el código de invitación.
- Pulsación sobre un contador para navegar al detalle.

Cada ítem del listado muestra la imagen del contador (cargada con Glide), el título, el estado en color (verde = abierto, rojo = cerrado), el conteo global y el conteo individual del usuario.

---

### 📊 Detalle del contador — `CounterDetailFragment`

Pantalla más completa de la aplicación. Muestra toda la información del contador y centraliza la interacción principal.

**Elementos principales:**
- Conteo global destacado en azul y conteo individual en morado.
- Estado del contador con codificación de color y fecha de cierre formateada.
- Código de invitación con botón de copiar al portapapeles.
- `RecyclerView` con el ranking de participantes (posición, nombre de usuario, puntuación).
- FAB de incremento (+1), visible y activo solo cuando el contador está `ABIERTO`.
- Botones de editar y eliminar, visibles únicamente si el usuario es el creador (`isCreator = true`). El borrado requiere confirmación mediante `AlertDialog`.

---

### ➕ Crear contador — `CreateCounterFragment`

Formulario para crear un nuevo contador. Todos los campos opcionales excepto el título.

**Elementos:**
- Campo de título (obligatorio) y descripción (opcional).
- Selector de fecha y hora encadenado: `DatePickerDialog` → `TimePickerDialog`. La fecha se convierte a formato ISO 8601 para enviarla al backend.
- Selector de imagen desde la galería del dispositivo con previsualización.
- La imagen se convierte de `Uri` a `File` mediante un pipeline de caché y se envía como `multipart/form-data`.

---

### ✏️ Editar contador — `EditCounterFragment`

Formulario idéntico al de creación pero precargado con los datos actuales del contador. Accesible únicamente desde el detalle y solo si el usuario es el creador.

Carga los datos actuales con `GET /counters/{id}/` y los envía modificados con `PUT /counters/{id}/` en formato multipart.

---

### 👤 Perfil — `ProfileFragment`

Muestra los datos del usuario autenticado (nombre de usuario, email y teléfono) obtenidos de `GET /auth/profile/`. Incluye un botón de cierre de sesión que borra el token de `SharedPreferences` y redirige a `LoginActivity` limpiando la pila de navegación.

---

## Cómo acceder y ejecutar el proyecto

### Requisitos previos

- **Android Studio** Hedgehog o posterior
- **JDK 11**
- **Android Emulator** con API 28 (Android 9.0) o superior
- El **backend Django** corriendo en `localhost:8000`

> ⚠️ La IP `10.0.2.2` configurada en `ApiClient.java` es el alias que usa el emulador oficial de Android para referirse al `localhost` del equipo host. No funciona con dispositivos físicos en una red diferente. Si se quiere usar con un dispositivo físico, hay que sustituir esa IP por la IP local del equipo en la red.

---

### Pasos para ejecutar

**1. Clonar el repositorio**
```bash
git clone https://github.com/jalvarezcanosa/ProyectoFinalDAMAndroid.git
```

**2. Abrir en Android Studio**

`File → Open` → seleccionar la carpeta clonada. Android Studio detectará `settings.gradle.kts` y ejecutará la sincronización de Gradle automáticamente. Esperar a que descargue todas las dependencias.

**3. Arrancar el backend Django**

El backend debe estar corriendo antes de lanzar la app:
```bash
python manage.py runserver
```

**4. Configurar el emulador**

Crear o iniciar un AVD con API 28 o superior desde el AVD Manager de Android Studio.

**5. Ejecutar la app**

Seleccionar el emulador como dispositivo destino y pulsar Run ▶, o ejecutar:
```bash
./gradlew assembleDebug
```

**6. Registrarse e iniciar sesión**

La app arranca en `LoginActivity`. Cambiar a modo Registro para crear una cuenta y luego iniciar sesión.

---

### Permisos declarados en el manifiesto

| Permiso | Uso |
|---|---|
| `android.permission.INTERNET` | Todas las llamadas a la API REST |
| `android.permission.READ_EXTERNAL_STORAGE` | Selección de imagen desde la galería |

---

## Tecnologías utilizadas

| Tecnología / Librería | Versión | Uso |
|---|---|---|
| **Java** | 11 | Lenguaje principal de la aplicación |
| **Android SDK (minSdk)** | 28 (Android 9.0 Pie) | Versión mínima de Android soportada |
| **Android SDK (targetSdk / compileSdk)** | 36 | Versión objetivo de compilación |
| **Android Gradle Plugin (AGP)** | 9.2.1 | Toolchain de build de Android |
| **Gradle** | 9.4.1 | Sistema de build (vía Gradle Wrapper) |
| **Retrofit 2** | 3.0.0 | Cliente HTTP para llamadas a la API REST |
| **Retrofit Gson Converter** | 3.0.0 | Serialización y deserialización JSON |
| **OkHttp** | 4.12.0 | Cliente HTTP subyacente de Retrofit |
| **OkHttp Logging Interceptor** | 4.12.0 | Logging de peticiones y respuestas en Logcat |
| **Glide** | 5.0.7 | Carga y caché de imágenes desde URL |
| **Jetpack Navigation Component** | 2.9.8 | Navegación entre fragments y gestión de la pila |
| **Material Design 3** | 1.13.0 | Componentes de UI: FAB, Cards, ShapeableImageView |
| **AndroidX AppCompat** | 1.7.1 | Compatibilidad hacia atrás de Activity y Fragment |
| **AndroidX ConstraintLayout** | 2.2.1 | Sistema de layouts flexibles |
| **SharedPreferences** | SDK integrado | Almacenamiento persistente del token JWT |
| **ClipboardManager** | SDK integrado | Copia del código de invitación al portapapeles |
| **DatePickerDialog / TimePickerDialog** | SDK integrado | Selección de fecha y hora de cierre |
| **SimpleDateFormat** | Java stdlib | Formateo de fechas en ISO 8601 para la API |
| **MultipartBody / RequestBody** | OkHttp 4.12.0 | Envío de imágenes y texto en formato multipart |
