# Vinilos — Mobile App

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="100" alt="Vinilos icon"/>
</p>

<p align="center">
  <img src="https://github.com/joseph-burbano/TSDC_VinilosMobileAPP/actions/workflows/ci.yml/badge.svg" alt="CI"/>
  <img src="https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/API-21%2B-brightgreen" alt="API 21+"/>
</p>

**Ingeniería de Software para Aplicaciones Móviles** — Universidad de los Andes (MISO).  
The Software Design Company.

Aplicación Android para navegar y gestionar un catálogo de álbumes de música en vinilo, artistas y coleccionistas.

---

## Tabla de contenido

1. [Stack](#stack)
2. [Arquitectura](#arquitectura)
3. [Estructura del proyecto](#estructura-del-proyecto)
4. [Prerrequisitos](#prerrequisitos)
5. [Levantar el backend](#levantar-el-backend)
6. [Correr la app](#correr-la-app)
7. [Pruebas unitarias (JVM)](#pruebas-unitarias-jvm)
8. [Pruebas instrumentadas (Compose / Espresso)](#pruebas-instrumentadas-compose--espresso)
9. [Pruebas E2E con Kraken](#pruebas-e2e-con-kraken)
10. [Optimizaciones aplicadas](#optimizaciones-aplicadas)
11. [Análisis estático con Android Lint](#análisis-estático-con-android-lint)
12. [Perfilado en dispositivos físicos](#perfilado-en-dispositivos-físicos)
13. [Notas para Windows](#notas-para-windows)
14. [Solución de problemas](#solución-de-problemas)

---

## Stack

| Capa            | Herramienta                             | Versión              |
| --------------- | --------------------------------------- | -------------------- |
| Lenguaje        | Kotlin                                  | 2.2.10               |
| Build           | AGP / Gradle wrapper                    | 9.1.1                |
| UI              | Jetpack Compose (BOM)                   | 2026.02.01           |
| Navegación      | Navigation Compose                      | 2.8.0                |
| Red             | Retrofit + Gson + OkHttp Logging        | 3.0.0 / 4.12.0       |
| Persistencia    | Room + KSP                              | 2.7.0 / 2.2.10-2.0.2 |
| Imágenes        | Coil                                    | 2.7.0                |
| Async           | Coroutines                              | 1.10.2               |
| Mocks JVM       | MockK                                   | 1.13.17              |
| Tests HTTP      | MockWebServer                           | 5.3.2                |
| Tests E2E       | Kraken-Node + Appium 2.x + UIAutomator2 | 1.0.24 / 2.11.5+     |
| Preferencias    | Jetpack DataStore                       | 1.1.1                |
| Inyección de DI | Hilt                                    | 2.59.2               |
| Splash          | Core SplashScreen                       | 1.0.1                |

---

## Arquitectura

El proyecto sigue el patrón **MVVM (Model - View - ViewModel)** con organización **por feature**.

Para más detalles sobre la arquitectura MVVM en Android, consultar (usar traductor ya que la fuente está en francés):  
[Comprendre l'architecture MVVM sur Android](https://medium.com/androidmood/comprendre-larchitecture-mvvm-sur-android-aa285e4fe9dd)

### Diagrama de capas

```
┌─────────────────────────────────────┐
│              VIEW                   │
│   ui/[feature]/[Feature]Screen.kt   │
│   (Jetpack Compose)                 │
└────────────────┬────────────────────┘
                 │ observa
┌────────────────▼────────────────────┐
│           VIEWMODEL                 │
│   AppViewModel (roles + tema)       │
│   ui/[feature]/[Feature]ViewModel   │
│   (StateFlow)                       │
└────────────────┬────────────────────┘
                 │ solicita datos
┌────────────────▼────────────────────┐
│           REPOSITORY                │
│   PreferencesRepository (DataStore) │
│   repository/[Feature]Repository    │
│   cache-first: Room → API REST      │
└──────────┬─────────────┬────────────┘
           │             │
┌──────────▼───┐   ┌─────▼──────────┐
│   ROOM DB    │   │   RETROFIT     │
│  database/   │   │   network/     │
│  DAO + Entity│   │   VinilosApi   │
└──────────────┘   └────────────────┘
```

---

## Estructura del proyecto

```
TSDC_VinilosMobileAPP/
├── app/
│   ├── build.gradle.kts                # deps + plugin KSP + Room schema
│   └── src/
│       ├── main/java/com/uniandes/vinilos/
│       │   ├── MainActivity.kt
│       │   ├── AppViewModel.kt
│       │   ├── VinilosApplication.kt
│       │   ├── di/
│       │   ├── database/               # Room: entities, DAOs, mappers
│       │   ├── model/                  # data classes de dominio
│       │   ├── network/                # Retrofit (VinilosApi, NetworkServiceAdapter)
│       │   ├── repository/             # cache-first repositories
│       │   ├── ui/{albums,artists,collectors,home,navigation,theme,role}
│       │   └── util/                   # Constants, FakeData
│       ├── test/                       # JVM unit tests (MockK + MockWebServer)
│       └── androidTest/                # Compose / Espresso tests
├── kraken/                             # tests E2E (Node + Appium)
├── gradle/libs.versions.toml           # catálogo único de versiones
├── build.gradle.kts                    # root: hook de adb reverse
├── settings.gradle.kts
└── gradle.properties                   # android.disallowKotlinSourceSets=false (KSP + AGP 9)
```

### ¿Por qué organización por feature?

En lugar de agrupar todos los `Screen.kt` juntos y todos los `ViewModel.kt` juntos, cada feature tiene su propia carpeta con todo lo que necesita. Esto permite navegar el proyecto de forma más intuitiva, modificar una feature sin tocar otras carpetas, y escalar el equipo asignando features a personas distintas.

---

## Prerrequisitos

| Herramienta             | Versión                                    | Para qué                                   |
| ----------------------- | ------------------------------------------ | ------------------------------------------ |
| JDK                     | 17                                         | Compilar la app                            |
| Android Studio          | Otter 3 Feature Drop (2025.2.3) o superior | Recomendado                                |
| Android SDK + ADB       | platform-tools                             | Instalar APK / `adb`                       |
| Emulador o device       | API 24+ (Android 7+)                       | Correr la app                              |
| Docker + Docker Compose | cualquiera                                 | Backend NestJS                             |
| Node.js                 | ≥ 12                                       | Solo para Kraken                           |
| Appium                  | 2.11.5 o 3.x                               | Solo para Kraken (`npm install -g appium`) |

> **JDK 17 obligatorio**. CI lo pinea a Temurin 17. El wrapper `./gradlew` se incluye — usa siempre ese, no un Gradle de sistema.

---

## Levantar el backend

El backend NestJS + Postgres es un repositorio independiente. Clónalo en una carpeta paralela al proyecto:

```bash
git clone https://github.com/TheSoftwareDesignLab/BackVynils
cd BackVynils
docker-compose up -d            # arranca Postgres + NestJS en background
docker-compose logs -f web      # opcional: ver los logs
docker-compose ps               # estado
docker-compose down             # detener
docker-compose down -v          # detener y borrar volumen (datos limpios)
```

Comprobar que responde desde tu PC:

```bash
curl http://localhost:3000/albums
curl http://localhost:3000/albums/1
```

**Configuración de red según donde corras la app:**

| Entorno                      | URL que ve la app                                               | Configuración                                                          |
| ---------------------------- | --------------------------------------------------------------- | ---------------------------------------------------------------------- |
| Backend desplegado           | `https://vinyls-backend-miso-g-013-5fad2b4cf522.herokuapp.com/` | Configuración por defecto en `Constants.BASE_URL`                      |
| Emulador o dispositivo local | `http://10.0.2.2:3000/` / `http://localhost:3000/`              | Sobrescribe `Constants.BASE_URL` si quieres apuntar a un backend local |

La URL base vive en `app/src/main/java/com/uniandes/vinilos/util/Constants.kt`.

---

## Correr la app

Con backend arriba y emulador o device conectado:

```bash
./gradlew installDebug             # instala el debug APK
./gradlew assembleDebug            # solo compila APK -> app/build/outputs/apk/debug/
```

O **Run** desde Android Studio (dispara `installDebug`). Cada `installDebug` ejecuta automáticamente `adb reverse tcp:3000 tcp:3000` vía el hook configurado en `build.gradle.kts` — útil en device físico, inocuo en emulador.

---

## Pruebas unitarias (JVM)

Ubicación: `app/src/test/java/com/uniandes/vinilos/`.

**No requieren** emulador, device, ni backend. Tardan < 5 segundos.

### Qué cubren

| Suite                        | Archivo                                         | Casos | Qué valida                                                                                                                         |
| ---------------------------- | ----------------------------------------------- | ----- | ---------------------------------------------------------------------------------------------------------------------------------- |
| ArtistViewModelTest          | `ui/artists/ArtistViewModelTest.kt`             | 13    | Carga, paginación, refresh, errores, findById                                                                                      |
| ArtistViewModelDetailTest    | `ui/artists/ArtistViewModelDetailTest.kt`       | 5     | findById con álbumes, null, diferenciación                                                                                         |
| ArtistRepositoryTest         | `repository/ArtistRepositoryTest.kt`            | 4     | Cache-first (DAO → API), combina músicos y bandas, refresh borra y refetchea                                                       |
| AlbumRepositoryTest          | `repository/AlbumRepositoryTest.kt`             | 8     | Patrón cache-first, mapeo DTO → modelo, `releaseDate` ISO truncado al año, `refreshAlbums()`                                       |
| AlbumViewModelTest           | `ui/albums/AlbumViewModelTest.kt`               | 9     | Estados Loading/Success/Error, `IOException`, `HttpException`, refresh, `findById`                                                 |
| AlbumViewModelDetailTest     | `ui/albums/AlbumViewModelDetailTest.kt`         | 7     | findById con tracks, performers, null                                                                                              |
| CollectorViewModelTest       | `ui/collectors/CollectorViewModelTest.kt`       | 12    | Carga, paginación, refresh, errores, loadCollector                                                                                 |
| CollectorViewModelDetailTest | `ui/collectors/CollectorViewModelDetailTest.kt` | 7     | findById con álbumes, performers, null                                                                                             |
| CollectorRepositoryTest      | `repository/CollectorRepositoryTest.kt`         | 9     | Cache-first, `getCollector(id)` individual, refresh, excepciones del API, null si falla                                            |
| VinilosApiContractTest       | `network/VinilosApiContractTest.kt`             | 3     | Contrato HTTP `GET /albums` con MockWebServer                                                                                      |
| AppViewModelTest             | `ui/AppViewModelTest.kt`                        | 8     | userRole inicial null, setUserRole VISITOR/COLLECTOR, clearUserRole, isDarkTheme default, toggleDarkTheme, isReady                 |
| ExampleUnitTest              | `ExampleUnitTest.kt`                            | 1     | Smoke del runner                                                                                                                   |
| CreateAlbumViewModelTest     | `ui/albums/CreateAlbumViewModelTest.kt`         | 11    | Estado inicial, validaciones de campos, construcción de fecha ISO, placeholder de cover, flujo exitoso, flujo de error, resetState |

### Cómo correrlas

```bash
./gradlew testDebugUnitTest                                          # todos
./gradlew testDebugUnitTest --tests "*ArtistRepositoryTest"          # una clase
./gradlew testDebugUnitTest --tests "*AlbumViewModelTest.*refresh*"  # un método (glob)
./gradlew testDebugUnitTest --rerun-tasks                            # forzar re-ejecución
```

### Dónde ver los resultados

```
app/build/reports/tests/testDebugUnitTest/index.html
app/build/test-results/testDebugUnitTest/*.xml
```

### Tecnologías de testing

| Librería        | Versión | Uso                         |
| --------------- | ------- | --------------------------- |
| JUnit 4         | 4.13.2  | Framework base              |
| MockK           | 1.13.17 | Mocking en Kotlin           |
| Coroutines Test | 1.10.2  | Soporte para `runTest`      |
| MockWebServer   | 5.3.2   | Simulación de servidor HTTP |

---

## Pruebas instrumentadas (Compose / Espresso)

Ubicación: `app/src/androidTest/java/com/uniandes/vinilos/`.

**Requieren** emulador o device conectado. **No** requieren backend (la API se mockea con MockK).

### Qué cubren

| HU      | Suite                                 | Casos | Qué valida                                                                                                                                                                                                                                      |
| ------- | ------------------------------------- | ----- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| HU01    | AlbumListScreenInstrumentedTest       | 6     | Skeleton durante carga, render del listado, mensaje de error, botón "Reintentar", estado vacío, render con rol Collector                                                                                                                        |
| HU02    | AlbumDetailScreenInstrumentedTest     | 6     | Spinner mientras carga, render completo del detalle (título, género, año, sello, secciones ARTISTAS y CANCIONES), botón Volver, mensaje de error, "Álbum no encontrado", render con rol Collector                                               |
| HU03    | ArtistListScreenInstrumentedTest      | 13    | Spinner durante carga, nombres de artistas, grilla con testTags, mensaje de error, búsqueda (filtro, case-insensitive, sin resultados, limpiar), navegación por click, paginación (visible, oculto, oculto al buscar), render con rol Collector |
| HU04    | ArtistDetailScreenInstrumentedTest    | 6     | Loading, nombre, discografía, botón volver, performer null, render con rol Collector                                                                                                                                                            |
| HU05    | CollectorListScreenInstrumentedTest   | 14    | Loading, coleccionistas, artista favorito, error, navegación, búsqueda (nombre, case-insensitive, artista favorito, sin resultados, limpiar), paginación (visible, oculto, oculto al buscar), render con rol Collector                          |
| HU06    | CollectorDetailScreenInstrumentedTest | 7     | Loading (isLoading=true), loading (collector null), nombre, stats, vault, botón volver, render con rol Collector                                                                                                                                |
| HU07    | CreateAlbumScreenInstrumentedTest     | 6     | Elementos del formulario, validaciones al submit vacío, submit exitoso invoca onSuccess, botón descartar, error del repositorio, botón deshabilitado durante carga                                                                              |
| ISSUE01 | HomeScreenInstrumentedTest            | 9     | Header, secciones álbumes/artistas/coleccionistas, navegación por click, coleccionistas en pantalla, artistas en pantalla, render con rol Collector                                                                                             |
| ISSUE02 | (todos los anteriores)                | +7    | Parámetro userRole propagado a VinilosTopBar en todas las pantallas, tag top_bar_back_button en VinilosTopBar, un test \_conRolCollector por cada suite                                                                                         |

### Cómo correrlas

```bash
adb devices                                                # verificar device disponible
./gradlew connectedDebugAndroidTest                        # full suite
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.uniandes.vinilos.ui.albums.AlbumListScreenInstrumentedTest
./gradlew assembleDebugAndroidTest                         # solo compilar APK de tests, sin ejecutar
```

### Dónde ver los resultados

```
app/build/reports/androidTests/connected/debug/index.html
app/build/outputs/androidTest-results/connected/debug/*.xml
```

---

## Pruebas E2E con Kraken

Ubicación: `kraken/`. Ver detalles técnicos en `kraken/README.md`.

Pruebas de **caja negra** sobre el APK real, dirigidas con Cucumber + Kraken-Node + Appium 2.x + UIAutomator2.

**Requieren**: device conectado, backend levantado.

> **Importante:** mantén la pantalla del device activa durante la ejecución.  
> Activa **Ajustes → Opciones de desarrollador → Mantener pantalla activa**.

### Qué cubren

| HU      | Feature                   | Archivo                                    | Qué valida                                                                                                             |
| ------- | ------------------------- | ------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------- |
| HU01    | Listado de álbumes        | `kraken/features/album_list.feature`       | Navegar al catálogo, ver el header, contador de resultados y sección de artistas                                       |
| HU02    | Detalle de álbum          | `kraken/features/album_detail.feature`     | Tap en un álbum, verificar género, secciones ARTISTAS y CANCIONES, volver con Volver                                   |
| HU03    | Listado de artistas       | `kraken/features/artist_list.feature`      | Navega al listado de artistas y verifica su contenido                                                                  |
| HU04    | Detalle de artista        | `kraken/features/artist_detail.feature`    | Tap en artista, ver secciones, volver                                                                                  |
| HU05    | Listado de coleccionistas | `kraken/features/collector_list.feature`   | Navegar al listado, ver header y coleccionista                                                                         |
| HU06    | Detalle de coleccionista  | `kraken/features/collector_detail.feature` | Tap en coleccionista, ver ELITE CURATOR y The Vault, volver                                                            |
| HU07    | Crear álbum               | `kraken/features/create_album.feature`     | Cambio a rol Collector, FAB visible, formulario completo, submit y retorno al listado                                  |
| ISSUE01 | Página principal          | `kraken/features/home.feature`             | Header, secciones ÚLTIMOS ÁLBUMES, ARTISTAS CONSULTADOS y COLECCIONISTAS sin depender de datos específicos del backend |
| —       | Navegación principal      | `kraken/features/navbar.feature`           | Las 4 tabs son visibles y navegables                                                                                   |

### Setup inicial (una sola vez por máquina)

```bash
cd kraken
chmod +x setup.sh run.sh
./setup.sh
```

`setup.sh` instala dependencias Node, parchea `kraken-node` 1.0.24 para Appium 2.x e instala el server APK de UIAutomator2 en el device.

### Correr los tests

> **Importante sobre datos del backend:** si los tests E2E usan el backend de Heroku
> compartido con el curso. Si otros estudiantes han modificado los datos, algunos
> features pueden fallar. Para garantizar datos limpios, usa el backend local:
>
> 1. `cd BackVynils && docker-compose down -v && docker-compose up -d`
> 2. Cambia `Constants.BASE_URL` a `http://10.0.2.2:3000/` (emulador) o la IP local (device)
> 3. Corre los tests
> 4. Restaura `Constants.BASE_URL` al valor de Heroku antes del PR

```bash
# 1. Backend arriba (en otra terminal, ver sección "Levantar el backend")

# 2. APK de la app compilado
./gradlew assembleDebug

# 3. Ejecutar
cd kraken
./run.sh
```

`run.sh` reinicia la app, aplica `adb reverse tcp:3000 tcp:3000` y lanza la suite.

### Dónde ver los resultados

```
kraken/reports/<timestamp>/index.html
```

El reporte HTML incluye cada step ejecutado y screenshots automáticos.

---

## Optimizaciones aplicadas

La aplicación incluye un conjunto de optimizaciones orientadas a tres frentes: **caché**, **programación asíncrona** y **uso eficiente de memoria**. Cada cambio responde a uno de los criterios de evaluación del entregable.

### 1. Caché HTTP en disco (OkHttp)

`NetworkServiceAdapter` monta una `okhttp3.Cache` de **10 MiB** en `context.cacheDir/vinilos_http_cache`. Un _network interceptor_ reescribe `Cache-Control: max-age=60` en todas las respuestas (el backend NestJS no envía ese header) y un _application interceptor_ activa modo `only-if-cached` con `max-stale = 7 días` cuando no hay red. Los timeouts del cliente (`connect`/`read`/`write = 15 s`, `callTimeout = 30 s`) y `retryOnConnectionFailure = true` completan la configuración.

La inicialización ocurre en `VinilosApplication.onCreate()` para inyectar el `Context` necesario para el directorio de caché y la detección de conectividad. Se añadió el permiso `ACCESS_NETWORK_STATE` al manifiesto.

**Efecto:** dentro de la ventana de 60 s las llamadas idénticas se sirven desde disco — 0 latencia, 0 datos móviles. En modo avión, la app puede servir contenido cacheado por hasta 7 días en vez de fallar con `IOException`.

### 2. `stateIn(Eagerly)` en flujos derivados con `combine`

Los tres ViewModels (`AlbumViewModel`, `ArtistViewModel`, `CollectorViewModel`) exponen `visibleX` y `hasMore` como `StateFlow` caliente compartido vía `stateIn(viewModelScope, SharingStarted.Eagerly, initialValue)`, en lugar del `Flow` frío resultado del `combine` original. Una sola suscripción upstream se comparte entre todos los `collectAsStateWithLifecycle` y mantiene el último valor cacheado en memoria.

**Efecto:** N collectors → 1 suscripción upstream (antes N evaluaciones de la lambda en cada cambio).

### 3. `collectAsStateWithLifecycle` en todas las pantallas

Se reemplazó `collectAsState()` por `collectAsStateWithLifecycle()` en las 6 pantallas Compose. La dependencia `androidx.lifecycle:lifecycle-runtime-compose:2.10.0` se declara explícitamente en `gradle/libs.versions.toml`.

**Efecto:** la recolección se cancela cuando el `LifecycleOwner` pasa a `STOPPED` (app a background) y se reanuda al volver a `STARTED`. Ahorra batería y previene escenarios pre-ANR si el ViewModel sigue emitiendo en background.

### 4. Coil `ImageRequest` con `scale(Scale.FILL)` y `crossfade(true)`

Donde se usaba `AsyncImage(model = url)` directamente, ahora se construye un `ImageRequest` explícito dentro de `remember(url)` con `.scale(Scale.FILL).crossfade(true)`. Aplica a `HomeScreen` (`AlbumCard`, `ArtistCard`), `CollectorDetailScreen` (`HeroSection`, `VaultAlbumCard`) y `ArtistListScreen` (`PerformerGridItem`).

**Efecto:** Coil decodifica el bitmap al tamaño del composable en vez del tamaño nativo del servidor — bitmaps típicamente 10–40× más pequeños en heap.

### 5. `remember` y `derivedStateOf` en cálculos derivados

Cálculos como `flatMap+distinctBy+take` en `HomeScreen`, filtros de búsqueda en las pantallas de listado y agregaciones (`avgGrade` en `CollectorDetailScreen`) que antes corrían en cada recomposition ahora se memorizan con `remember(...)` y `derivedStateOf`.

**Efecto:** Compose puede recomponer un screen muchas veces por segundo durante una animación; sin memoización, cada recomposition recalcula filtros sobre listas completas.

### 6. Keys estables en items de listas Lazy

Todos los `items(list)` y `itemsIndexed(list)` en `AlbumListScreen`, `ArtistListScreen`, `CollectorListScreen` y `HomeScreen` reciben `key = { it.id }` (o `key = { it }` para colecciones de strings).

**Efecto:** al filtrar o reordenar la lista, Compose mueve el composable existente sin recrearlo. Reduce el trabajo de composición y evita re-decode de imágenes con Coil.

### 7. Limpieza derivada del lint

Se eliminaron los 7 colores legacy del template (`purple_*`, `teal_*`, `black`, `white`) que no se referenciaban en ningún lado, y el `android:label` redundante de la `<activity>` en el manifiesto (ya está en `<application>`).

---

## Análisis estático con Android Lint

Lint detecta problemas de correctness, performance, accesibilidad y seguridad sin necesidad de ejecutar la app. La app se valida sobre cero errores y se actúa sobre los warnings accionables.

### Cómo correrlo

```bash
./gradlew lint              # corre lint sobre debug + release
./gradlew lintDebug         # solo variant debug (más rápido)
```

### Dónde ver los resultados

Cada ejecución genera los reportes en `app/build/reports/`:

```
app/build/reports/lint-results-debug.html      # reporte navegable
app/build/reports/lint-results-debug.xml       # formato máquina
app/build/reports/lint-results-debug.txt       # formato consola
```

El HTML agrupa los issues por categoría (Correctness, Performance, Security, Accessibility, etc.) y muestra el archivo + línea + extracto del código afectado.

> Para conservar un snapshot del lint junto con el código (en vez del path efímero de `app/build/`), el repo incluye una copia versionada de la última ejecución en [`reports/lint/`](reports/lint/).

### Estado actual

La última ejecución reporta **21 warnings, 0 errors**. Las decisiones tomadas se resumen en la siguiente tabla:

| Issue                        | Categoría   | Detalle                                                                | Decisión                                |
| ---------------------------- | ----------- | ---------------------------------------------------------------------- | --------------------------------------- |
| `RedundantLabel`             | Correctness | `MainActivity` tiene `android:label` duplicado del `<application>`     | Corregido                               |
| `UnusedResources`            | Performance | 7 colores legacy en `colors.xml` (purple*\*/teal*\*/black/white)       | Corregido                               |
| `UnusedResources`            | Performance | `ic_launcher_background.xml` / `ic_launcher_foreground.xml` (drawable) | Falso positivo (los usa el wizard)      |
| `AndroidGradlePluginVersion` | Correctness | AGP 9.1.1 → 9.2.0 disponible                                           | Diferido (cambio amplio fuera de scope) |
| `GradleDependency`           | Correctness | Compose BOM, Room, Navigation obsoletos                                | Diferido (cambio amplio fuera de scope) |
| `NewerVersionAvailable`      | Correctness | Kotlin 2.2.10, Mockk, OkHttp logging-interceptor                       | Diferido (cambio amplio fuera de scope) |
| `Aligned16KB`                | Correctness | `libmockkjvmtiagent.so` no alineado a 16 KB                            | Ignorado (solo afecta `androidTest`)    |

Las actualizaciones de versiones de dependencias requieren _regression testing_ dedicado y se manejan en una rama separada.

---

## Perfilado en dispositivos físicos

Para medir el impacto real de las optimizaciones, el repositorio incluye un script PowerShell que captura métricas vía `adb dumpsys` mientras se recorre manualmente la app. Los reportes se generan por dispositivo y luego se consolidan en un informe `.docx` con gráficas.

### Pre-requisitos

- ADB en `PATH` (incluido en Android SDK platform-tools).
- Dispositivo Android con depuración USB activada y autorizado (`adb devices` debe listarlo).
- PowerShell 5.1+ (incluido en Windows). En otras plataformas, ver "Solución de problemas".
- Para regenerar el `.docx`: Python 3.10+, `python-docx` y `matplotlib`.

### 1. Capturar métricas en un dispositivo

Desde la raíz del repositorio:

```powershell
.\scripts\profile-device.ps1 -DeviceLabel "<nombre-del-telefono>"
```

`-DeviceLabel` es libre; sirve para nombrar el reporte (ej. `samsung-a52`, `pixel-7`, `redmi-note-13-pro`).

El script ejecuta:

1. Verifica que haya un dispositivo conectado y captura su modelo, versión de Android y ABI.
2. Reinstala el APK debug (`./gradlew installDebug`). Se puede saltar con `-SkipInstall`.
3. Resetea contadores de batería (`dumpsys batterystats --reset`) y GPU (`dumpsys gfxinfo <pkg> reset`) para obtener una línea base limpia.
4. Lanza la app y muestra el recorrido manual a ejecutar.
5. Cuando se presiona Enter, captura cuatro snapshots vía `adb dumpsys`:
   - **`meminfo`** — desglose de memoria PSS por categoría (Java/Native/Code/Graphics/Stack).
   - **`gfxinfo`** — frames totales, % de janky frames, percentiles P50/P90/P95/P99 del tiempo de render.
   - **`batterystats --charged`** — tiempo de CPU acumulado y energía estimada.
   - **`top -n 1 -p <pid>`** — %CPU instantáneo del proceso.

### Recorrido manual a ejecutar

Mientras el script espera el `Enter`, se debe recorrer:

1. Pantalla **Inicio** (Vinilos).
2. Tab **Álbumes** → entrar a un álbum → volver.
3. Tab **Artistas** → entrar a un artista → volver.
4. Tab **Coleccionistas** → entrar a un coleccionista → volver.
5. Hacer **pull-to-refresh** en alguna lista.

Aproximadamente 5 segundos por pantalla es suficiente para acumular frames representativos.

### 2. Dónde quedan los reportes

```
profile-results/<DeviceLabel>-<timestamp>.txt
```

Los reportes capturados durante esta entrega están versionados en [`profile-results/`](profile-results/) como evidencia (3 dispositivos físicos: Redmi Note 9 Pro / Android 10, Poco F5 Pro / Android 15, Redmi Note 13 Pro / Android 14).

### 3. Generar el informe consolidado en .docx

Con al menos un `.txt` en `profile-results/`, se ejecuta:

```bash
python scripts/generate-report.py
```

El script parsea todos los `.txt`, genera 4 gráficas con `matplotlib` (PSS apilado por categoría, % de jank, frames saludables vs jankerados, percentiles de frame time) y construye `Informe-Optimizaciones-Vinilos.docx` en la raíz del repo. Las gráficas intermedias se guardan en `build/report-charts/`.

El informe `.docx` cubre las optimizaciones aplicadas, los hallazgos del lint y la comparación entre los dispositivos perfilados (con tablas y gráficas). El archivo no se versiona (está en `.gitignore`); se regenera bajo demanda a partir de los `.txt` versionados en `profile-results/`.

### Cómo opera la métrica de batería

Mientras el dispositivo está conectado por USB, `dumpsys batterystats` reporta `Time on battery: 0 ms` porque no acumula stats durante la carga. Para una medición cuantitativa de mAh hay que repetir el perfilado con el dispositivo desconectado y la app en uso continuo durante varios minutos.

---

## Notas para Windows

PowerShell **no ejecuta** comandos Unix (`chmod`, `./script.sh`, etc.) ni los scripts de Kraken. Para todo lo de la sección Kraken usa **Git Bash** (viene con Git for Windows) o WSL. Para los comandos `./gradlew` puedes usar PowerShell sin problema.

```powershell
# En PowerShell los comandos Gradle van igual:
./gradlew testDebugUnitTest
./gradlew installDebug
```

Para Kraken, abre **Git Bash** (clic derecho en carpeta → "Git Bash Here"):

```bash
cd kraken
./setup.sh   # solo la primera vez
./run.sh
```

### `adb` no se reconoce / `ANDROID_HOME not exported`

```powershell
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
[Environment]::SetEnvironmentVariable("Path", $env:Path + ";$env:LOCALAPPDATA\Android\Sdk\platform-tools;$env:LOCALAPPDATA\Android\Sdk\emulator", "User")
# cierra y reabre TODAS las terminales
adb devices
```

Solo para la sesión actual de Git Bash:

```bash
export ANDROID_HOME="/c/Users/Usuario/AppData/Local/Android/Sdk"
export PATH="$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"
```

### File lock al compilar

Pasa cuando un Gradle daemon previo dejó abierto un `.jar`:

```powershell
./gradlew --stop
./gradlew testDebugUnitTest
```

---

## Solución de problemas

### "Failed to connect to /127.0.0.1:3000" en logcat

Si estás usando el backend local, la constante en `Constants.kt` debe ser `http://10.0.2.2:3000/` para emulador. Si se sobreescribió a `127.0.0.1`, restáurala o aplica `adb reverse tcp:3000 tcp:3000` después de cada reinicio del adb daemon.

### El listado se queda vacío y no muestra error

La caché de Room guardó una respuesta vacía. Limpia los datos:

```bash
adb shell pm clear com.uniandes.vinilos
```

### KSP rompe con AGP 9 ("Using kotlin.sourceSets DSL is not allowed")

`gradle.properties` ya tiene `android.disallowKotlinSourceSets=false`. Si lo borras, vuelve a ponerlo.

### Tests instrumentados: diálogo "Android App Compatibility / 16 KB"

No es un error — la app corre en "page size compatible mode" por una dependencia de MockK. Toca **"Don't Show Again"** una vez y vuelve a correr la suite.

### Kraken: features fallan porque los datos del backend cambiaron

El backend de Heroku es compartido. Si otro estudiante agregó o eliminó álbumes,
los features que verifican nombres específicos fallarán. Solución: resetea el
backend local con `docker-compose down -v && docker-compose up -d` y apunta
`Constants.BASE_URL` al backend local para los tests E2E.

### Kraken: "Cannot find module" o falla al conectar Appium

Re-ejecuta `kraken/setup.sh`. Verifica que `adb devices` muestre tu device.

### Kraken: `Could not find a driver for automationName 'UiAutomator2'`

```bash
appium driver install uiautomator2@2.29.0
appium driver list --installed
appium
```

### Kraken: el feature siguiente falla porque la app quedó en otra pantalla

`hooks.js` usa `appium:noReset: true`, así que **la app NO se cierra entre escenarios**. Cada feature debe terminar dejando la app en HomeScreen. Como atajo:

```bash
adb shell am force-stop com.uniandes.vinilos
cd kraken && ./run.sh
```

### Kraken: orden alfabético entre features

Cucumber/Kraken corre los `.feature` en orden alfabético: `album_detail.feature` → `album_list.feature` → `artist_list.feature` → `navbar.feature`. Prefiere la convención de terminar en Home.

### Kraken: `'one unique @user tag for each scenario'`

Cada `Scenario:` debe tener un tag `@userN` distinto. Si solo tienes un device, mantén **un único Scenario por feature**.

### Kraken: `The instrumentation process cannot be initialized`

El UIAutomator2 server APK no está instalado. Re-ejecuta `kraken/setup.sh` o instala manualmente:

```bash
SERVER_APK=$(find "$HOME/.appium" -name "appium-uiautomator2-server-v*.apk" | grep -v androidTest | head -1)
TEST_APK=$(find "$HOME/.appium" -name "*androidTest*.apk" | head -1)
adb install -r "$SERVER_APK"
adb install -r "$TEST_APK"
```

---

## Backlog y equipo

Backlog completo, sprints e historias de usuario en la [Wiki del repositorio](../../../wiki).

Proyecto académico — Universidad de los Andes 2026.
