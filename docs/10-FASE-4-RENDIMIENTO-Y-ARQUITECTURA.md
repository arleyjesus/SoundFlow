# Fase 4: rendimiento, caché, memoria y arquitectura

Actualización posterior: APK final 4B + Apariencia generado a las 23:23 del
2026-10-11, 62 JVM Gradle sin fallos y aceptación funcional general del usuario.
Instrumentación, mediciones, regresión específica e informe resuelto pendientes.
Los bloqueos de Gradle y pendientes de APK siguientes son históricos.

Fecha: 2026-10-11. Estado inicial: implementación esencial 4A/4B terminada; aceptación
final pendiente. Estado anterior 4A compilado con Gradle y aceptado manualmente:
41 JVM sin fallos y APK de las 22:41 America/Bogota. Estado nuevo 4B + base fase 5:
61 JVM directos pasan, 71 fuentes comprobadas con Kotlin/Compose. Gradle bloqueado
por loopback; APK nuevo, instrumentación, informe resuelto y mediciones pendientes.
La base Apariencia de fase 5 se inicia por autorización sin afirmar cierre validado
de fase 4. [Evidencias, archivos y aceptación pendiente](12-CIERRE-FASE-4-E-INICIO-FASE-5.md).

## Objetivo

Preparar una base eficiente antes del rediseño integral. Resolver primero trabajo
innecesario confirmado; medir antes de prometer FPS, ahorro de batería o memoria.
Mantener identificadores, contratos de importación, esquema Room 2 y datos.

## Diagnóstico de recorridos activos

| Prioridad | Evidencia en código | Acción y estado |
|---|---|---|
| Alta: memoria | CoverCacheManager decodificaba portada original antes de reducir y reservaba un presupuesto RAM de hasta 25% del heap, además de Coil | 4A elimina esa segunda caché y muestrea antes de decodificar |
| Alta: caché | Solicitudes simultáneas podían extraer/escribir el mismo JPG; escritura directa permitía archivos parciales | 4A serializa extracción y publica con AtomicFile; hits de disco independientes |
| Media: biblioteca | ViewModel observaba allSongs por separado para UI y controlador | 4A conecta controlador al StateFlow songs existente |
| Media: CPU/main | Mapeo de entidades, composición de playlists y agrupación se ejecutaban en contexto del colector; LrcParser hacía I/O desde LaunchedEffect | 4A usa Default para listas y IO para lectura local de letras |
| Media: recursos | Conexión HTTP de letras sin disconnect explícito; captura genérica | 4A libera en finally y propaga cancelación |
| Media: información | Ajustes prometía 500 MB RAM y 120 FPS; eran límite de disco y rendimiento sin medir | 4A corrige textos y usa versión real de BuildConfig |
| Alta: artistas | Descarga previa sin límites, hashCode y escritura directa | 4B: HTTPS acotado por bytes/tiempo, recursos cerrados, SHA-256, AtomicFile, concurrencia/backoff y cuota 32 MiB |
| Media: derivaciones | allPlaylists reobservaba allSongs; búsqueda e índice podían trabajar en main | 4B: playlists recibe songs compartido, búsqueda cancelable e índice en Default |
| Media: UI | Raíz recogía posición cada 500 ms con collectAsState | 4B: estado de navegación sin progreso/bytes y hosts locales; colectores con ciclo de vida. Impacto cuantitativo pendiente |
| Media: arquitectura | ViewModel dependía del concreto y carpetas UI eran entidades | 4B: interfaz real implementada y LibraryFolder sin Room. Uri Android permanece en contrato |
| Media: efectos | Blur, transiciones y colores locales repartidos | Inicio fase 5: Apariencia persistente, tema único y consumidores iniciales de política. Extensión global restante pendiente |

ArtworkLoader y EqualizerHandler no tienen consumidores encontrados. El primero
recibe protección defensiva (20 MiB máximo y muestreo), pero no explica el consumo
de la ruta activa. MediaStoreAudioScanner sigue sin activar. No se retiran archivos
antiguos ni se confunden sus rutas con funcionalidades usadas.

## Decisiones y límites de 4A (histórico; 4B en documento 12)

- CoverCacheManager entrega archivos; Coil gestiona memoria de imágenes de la UI.
  Se retira getMemoryBitmap, sin consumidores en el repositorio. El contrato usado
  getOrFetchCover y clearCacheForSong se conserva.
- Miniaturas nuevas: muestreo por potencias de dos con dimensión objetivo 250 px,
  conservando proporción. El reproductor usa el mismo decodificador con 1024 px.
  No se amplían imágenes pequeñas; la UI hace el recorte visual.
- Una extracción a la vez limita retrievers y arrays originales simultáneos.
  Los hits de disco se resuelven antes del Mutex para no esperar a un proveedor
  lento; los misses revalidan al adquirirlo para reutilizar una escritura previa.
  Es una elección conservadora; medir latencia de listas antes de aumentar paralelismo.
- Caché de disco conserva 500 MiB, poda a 400 MiB al excederlo. Solo poda archivos
  cover_ID.jpg del directorio propio; protege el recién escrito y cuenta borrados
  exitosos. No elimina canciones, documentos SAF, Room ni preferencias.
- Los hits validan dimensiones sin crear bitmap RAM. AtomicFile protege publicación
  de nuevas escrituras; no hay test de fallo de disco ni extracción real aún.
- No se reciclan bitmaps compartidos con Compose/Coil. Solo el bitmap privado del
  escritor se libera después de comprimirlo.
- No hay invalidación nueva por cambios externos de etiquetas. La clave sigue
  usando ID Room; arrays originales de metadata y el bitmap del reproductor aún
  requieren perfilado. La extracción nativa bloqueante no se interrumpe inmediatamente.
- stateIn sigue el ciclo del ViewModel: su colector para el controlador mantiene
  songs activo. No se declara eliminada toda consulta duplicada: playlists conserva
  una suscripción adicional del repositorio.
- Letras mantiene el acceso externo previo a LRCLIB. La cancelación evita publicar
  resultados obsoletos y libera conexión al finalizar; no interrumpe de inmediato
  una lectura bloqueante. Corregir parsing LRC/SAF pertenece a su intervención propia.

## Inventario tecnológico declarado, sin actualizaciones

| Área | Configuración revisada |
|---|---|
| Android | minSdk 24, targetSdk 35, compileSdk 37; app 3.7.0 / código 37 |
| Build | Gradle wrapper 9.7.0; AGP 9.3.1; Java/JVM 17 |
| Kotlin | 2.2.10 y plugin Compose 2.2.10; KSP declarado 2.1.0-1.0.29 |
| UI | Compose BOM 2024.12.01; animation explícita 1.12.0; icons extended 1.7.6 |
| Lifecycle/navegación | lifecycle-runtime-ktx y viewmodel-compose 2.8.7; navigation-compose 2.8.5 |
| Persistencia | Room 2.7.0-alpha13, DataStore Preferences 1.1.1 |
| Audio/imágenes | Media3 1.5.1, Coil Compose 2.7.0, Palette 1.0.0 |
| Archivos/tags | DocumentFile 1.1.0, jaudiotagger 3.0.1 |
| Resolución forzada | core-ktx 1.12.0 aunque se declara 1.15.0; annotation 1.7.0 |

Esto es configuración declarada, no informe de dependencias resueltas. Riesgos
por verificar: mezcla Kotlin/KSP, Room prerelease, versiones Compose explícitas
fuera del BOM y overrides antiguos. Las compilaciones anteriores satisfactorias
no prueban que esos riesgos sean fallos actuales. No se cambia ninguna dependencia.
Coroutines se usa mediante dependencias transitivas; no se atribuye una versión
resuelta a todo el grafo por encontrar un JAR en caché.

Cuando Gradle funcione, guardar el informe local de debugRuntimeClasspath y
debugCompileClasspath; revisar dependencyInsight para Kotlin, KSP, Compose,
coroutines, Room y Core. Proponer cualquier actualización como cambio separado
con motivos, compatibilidad y aprobación, manteniendo el esquema actual.

## Plan esencial restante y puerta a fase 5

1. Completado: compilación Gradle y aceptación manual general del APK combinado
   3C/4A. La regresión específica por caso/dispositivo sigue sin resultados detallados.
2. Implementado bloque 4B: acotar caché/red de artistas, compartir biblioteca con playlists,
   derivaciones/búsqueda fuera de composición y contrato real de repositorio.
   Revisar ciclo de vida y aislar progreso del reproductor donde aporte una mejora
   demostrable. Mantener cancelación, orden de resultados y referencias Room.
3. Medir en el mismo dispositivo, biblioteca y build: arranque frío/caliente,
   primer scroll y segundo scroll con caché, abrir/cerrar reproductor 20 veces,
   cambiar 20 pistas, búsqueda rápida y reproducción en segundo plano. Registrar
   heap Java/native, máximo y recuperación tras GC, tiempo de cuadros y tamaño
   de caché. Hoy todas esas métricas están **pendientes**, no hay benchmark.
4. Revisar dependencias resueltas y advertencias; evitar migración tecnológica
   global para rediseñar. Compose/Material3, Coil y DataStore ya cubren la base.
5. Aceptar cierre de fase 4 con compilación, regresión y evidencia de memoria sin
   crecimiento sostenido; pendientes explícitos. Fase 5 comienza su base de tokens
   y Apariencia por autorización; falta aceptar APK nuevo antes de ampliar el rediseño.

Presupuesto de efectos: animaciones solo mientras correspondan a un estado visible;
al apagarlas no mantener loops decorativos. Transparencia apagada debe dar superficie
opaca legible y evitar trabajo de blur. No fijar 120 FPS ni un límite global de heap
sin conocer dispositivo y bibliotecas. No añadir Hilt, nueva caché o librería de blur
solo por cambiar el aspecto.

## Pruebas y fuentes

Resultados reales y archivos: [cierre 3C](09-CIERRE-FASE-3.md). JUnit directo 40/40;
Gradle posterior: 41/41 JVM (incluida plantilla), APK app/tests regenerados y
aceptación manual general. Los 55 instrumentados nuevos están compilados, no
ejecutados. El bloqueo de loopback describe el intento anterior del agente.
Los cuatro nuevos tests Android cubren tamaño/aspecto, bytes inválidos y hits concurrentes.
No cubren todavía extracción nativa, poda por volumen ni fallos de escritura.

Muestrear antes de asignar el bitmap sigue la recomendación de
[Android sobre bitmaps grandes](https://developer.android.com/topic/performance/graphics/load-bitmap.html).
La revisión de colectores se basa en las
[recomendaciones de arquitectura Android](https://developer.android.com/topic/architecture/recommendations).
Las mediciones se harán con las
[herramientas de perfilado Android](https://developer.android.com/studio/profile).
