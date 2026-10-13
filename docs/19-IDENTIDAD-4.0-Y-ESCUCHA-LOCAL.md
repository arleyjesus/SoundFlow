# SoundFlow 4.0.0-rc.1 — identidad visual y escucha local

## Objetivos y autorización

El usuario solicitó iconos diferentes de las primeras versiones, cristal sutil,
onda renovada, mejores títulos y playlists, fondo personalizado, Smart DJ más
útil, mensajes musicales y revisión de notificación/bloqueo. Autorizó organizar
commits y publicar las actualizaciones en la rama refactor/soundflow-4.
Se conserva la validación con APK para el final del bloque.

## Consola aportada: diagnóstico real

El pegado incluye varios intentos históricos. El primero falló por 11 errores de
Lint; después se corrigieron y hay varias ejecuciones BUILD SUCCESSFUL. La última
entrega completa es build/validation/20261013-004530-UTC: app, APK instrumentado,
JVM, Lint e informes de dependencias. XML actual: 114 pruebas JVM, cero fallos y
errores; Lint: cero errores, 81 advertencias y 3 hints. Instrumentación compilada,
sin evidencia de ejecución Android.

Avisos más numerosos: UseTomlInstead (27), GradleDependency (25), UnusedResources
(9), IconDuplicates (5), UseKtx (5), ModifierParameter (3), InlinedApi (2),
NewerVersionAvailable (2), AndroidGradlePluginVersion (1). Opciones de compatibilidad
de Gradle obsoletas requieren mantenimiento futuro. No ocultar avisos ni actualizar
AGP/Kotlin/dependencias de forma masiva como parte del rediseño. local.properties
ya tiene el escape correcto; no fue necesario volver a modificarlo.

## Cambios implementados

- SoundFlowIcons: familia vectorial propia, reutilizada en navegación, reproductor,
  mini, playlist y Smart DJ. Trazos coherentes, estados de favorito por relleno y
  color, acciones con descripciones existentes. Sin imágenes remotas o dependencias.
- Navegación: separadores verticales de 1 dp con degradado tenue. Mantiene etiquetas
  y objetivos táctiles, sin vidrio animado o desenfoque continuo.
- Títulos de SoundFlow, Biblioteca, Buscar y Perfil con más peso.
- Inicio: tarjetas de Tu música y playlists en panel de cristal sutil, medidas más
  holgadas, mosaico coherente y nuevos mensajes musicales ligeramente motivacionales.
- Reproductor: controles propios de reproducir/pausar, saltos, avance/retroceso,
  favoritos, letras, temporizador, información y edición. Onda con cinta central,
  barras centradas y envolvente suave, reutilizando Path y Brush. Sigue siendo
  decorativa; no mide audio, no pide micrófono y no promete una tasa de cuadros.
- Colección de playlists: mosaicos reales, duración, espacio y borde sutil. Detalle
  con portada más compacta, metadatos legibles y botones coherentes; conserva
  gestión y orden visible, sin cambiar entidades o relaciones.
- Apariencia: elegir/cambiar/quitar fondo mediante selector de documentos, acceso
  persistente e imagen acotada a 1080×1920 para la presentación. Un único fondo
  detrás de la app con velo según tema; superficies de lectura encima. Desactivar
  Transparencias oculta la imagen sin borrar la elección. Restablecer Apariencia
  quita el fondo; favoritos, playlists, orden, audio e historial se conservan.
  Una imagen cuyo permiso deje de ser válido no impide usar el tema de fondo.
- Smart DJ: panel destacado, filtros horizontales y filas de sesión reproducibles
  manteniendo su cola. No se añadió análisis de audio ni mezcla automática.
- Más escuchadas: opt-in apagado inicialmente; ranking de hasta 10 canciones desde
  su activación. Se registra una vez por reproducción después de 30 segundos de
  avance observado, o la mitad de una pista corta. Pausas, saltos de posición y
  intervalos largos de muestreo no se suman como escucha. El servicio mantiene
  el seguimiento en segundo plano; una nueva transición reinicia la acreditación.
  Es un contador conservador, no una medida de audibilidad ni un registro histórico
  reconstruido. Guardar el contador no reinicia la observación de activación.
- Privacidad: contadores acotados a 2000 IDs en DataStore separado
  listening_private.preferences_pb, excluido de cloud-backup y device-transfer
  configurados. Puede pausarse la recogida o borrarse el historial sin borrar música.
  No cambia copias de Room, favoritos ni ajustes generales; sin envío a servidores.
- Sesión multimedia: carátula local en metadatos de Media3; bytes acotados para la
  pista actual y retirada de carátulas incrustadas anteriores para contener memoria.
  Android/HyperOS determina composición, colores y distribución final de controles
  del panel y bloqueo. No se promete replicar la interfaz interna en System UI.
- Versión preparada: 4.0.0-rc.1 / código 40. El comparador admite actualizar desde
  este RC hacia 4.0.0 estable, sin ofrecer RC remotos como versiones estables.
  El diálogo de novedades muestra la versión real y elimina las afirmaciones
  antiguas de espectro audio-reactivo y 120 FPS sin medición.

## Archivos y arquitectura

Nuevos: SoundFlowIcons.kt, AppBackdrop.kt, PlaylistCover.kt, ListeningTracker.kt,
ListeningTrackerTest.kt, ListeningPreferencesTest.kt y ReleaseCandidateTest.kt.
Actualizados: MainActivity, HomeScreen y sus tarjetas, LibraryScreen,
PlaylistsScreen, PlaylistDetailScreen, SearchScreen, ProfileScreen, SmartLibraryScreen,
AppearanceScreen, SettingsScreen, FullPlayerSheet, MiniPlayerCard, AudioVisualizerView,
Screen, AppNavigation, LibraryViewModel, SettingsRepository, AppearanceSettings,
AudioPlayerHandler, SoundFlowMediaService, ReleaseVersion y ChangelogDialog.
Configuración: solo versión de app y exclusión del nuevo historial en reglas XML
de backup. No cambian dependencias, SDK, configuración global, Room, entidades ni
soundflow_database (versión 2). No se genera una migración ni recreación destructiva.

## Validación

- Comprobación directa de 151 fuentes Kotlin main/test/androidTest, con artefactos
  locales existentes. No sustituye KSP, recursos, empaquetado ni Lint de Gradle.
- Suite JVM directa ampliada a 130 pruebas: registro, pausas, saltos, repetición,
  límites, opt-in, aislamiento, borrado, reapertura y transición RC → estable.
- XML de backup/extracción parseados. git diff --check y revisión de diff/status.
- Intento Gradle: assembleDebug, testDebugUnitTest, assembleDebugAndroidTest,
  lintDebug, offline. Bloqueado antes de tareas por
  java.io.IOException: Unable to establish loopback connection.
- Registros: build/phase5-design-check/rework-kotlin.txt, rework-junit.txt,
  rework-gradle.txt. No confundir los 114 JVM/Lint del APK anterior con la
  validación Gradle pendiente del candidato nuevo.
- No se ejecutó instrumentación ni se instaló/probó el APK nuevo desde el agente.

## Única comprobación final del candidato

Desde C:\SoundFlow-Git ejecutar .\tools\Validate-SoundFlow.ps1. Tras éxito, instalar
app\build\outputs\apk\debug\app-debug.apk sin borrar datos y comprobar:

1. Versión 4.0.0-rc.1, iconos y navegación con separadores, títulos y tarjetas.
2. Playlist vacía, una canción, 2–4 portadas y nombre largo; abrir, renombrar,
   añadir/quitar y reproducir siguiendo la lista visible.
3. Fondo claro/oscuro; elegir, reabrir app, cambiar, quitar y restablecer. Comparar
   Transparencias apagadas/encendidas y verificar legibilidad, incluido texto grande.
4. Onda en reproducción/pausa y Animaciones apagadas. Favorito seleccionado visible.
5. Activar historial, reproducir al menos 35 segundos y ver una escucha. Pausar no
   suma; saltar 60 segundos no acredita por sí solo. Repetir canción, desactivar
   recogida y borrar historial; favoritos/playlists permanecen. Verificar con la
   pantalla apagada y al volver a Smart DJ.
6. Dos canciones con carátulas distintas desde panel de notificación y bloqueo;
   comprobar metadatos, pausa, siguiente/anterior y carátula correspondiente.

## Viabilidad: playlists por estado de ánimo

| Enfoque | Dificultad y resultado |
|---|---|
| Etiquetas personales Alegría, Calma, Energía o Melancolía | Baja/media. El usuario etiqueta canciones; Smart DJ crea propuestas con esas etiquetas. Explicable, privado y adecuado como siguiente mejora. |
| Inferencia con título/género | Media, precisión limitada. El género y el nombre no determinan emoción. Solo serviría para sugerencias revisables y nunca como clasificación fiable. |
| Analizar audio con un modelo local | Alta. Requiere evaluar modelo, licencia, formatos, consumo, batería y dependencias antes de integrarlo. El ánimo sigue siendo subjetivo. |

Estas listas de ánimo todavía no se implementan. Próximo paso propuesto:
4.0.0 estable después de validar este candidato; 4.0.1 para etiquetas personales y
sesiones por ánimo revisables. Versiones posteriores según cambios concretos,
sin subir versión por cada archivo o commit. No se publican Releases, etiquetas,
APK firmados ni se modifica main en esta intervención.
