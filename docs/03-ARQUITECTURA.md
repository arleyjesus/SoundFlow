# Arquitectura de SoundFlow

## Herramientas locales y privacidad — fases 6/7

Inicio → AppNavigation → SmartLibraryScreen proyecta Song a SmartTrack y calcula
sesiones/coincidencias en Default con efecto asociado a biblioteca, modo, favoritos
y canción de referencia. Resultados transitorios; IDs se resuelven en la biblioteca
actual al actuar. Solo el botón de reproducción cambia la cola; no escribe Room.

SettingsRepository publica NetworkSettings en el DataStore existente. LibraryViewModel
lo comparte; MainActivity espera preferencias cargadas y proporciona LocalNetworkSettings.
Imágenes, letras y actualizaciones usan políticas independientes en sus efectos UI;
ArtistImageFetcher recibe networkAllowed y permite caché local sin consultas.
Cambios cancelan consumidores; no deshacen solicitudes remotas ya enviadas.
Restablecer Apariencia no elimina preferencias de privacidad. Sin dependencias ni
permisos nuevos. [Estado y límites](17-FASES-6-Y-7-IMPLEMENTACION-ESENCIAL.md).

## Cierre de rendimiento visual (2026-10-12)

AudioVisualizerView observa el Lifecycle de su propietario y solo compone la
animación infinita si está RESUMED, reproduciendo y motionEnabled es true.
Al perder primer plano queda estático; al salir retira su observador. No toca
el servicio, la cola ni el DSP. No implica una medición de señal o ahorro medido.
Cabecera del reproductor distribuye espacio para texto grande; tiempo estable
con Locale.ROOT. Dependencias resueltas revisadas y riesgos en documento 14.

## Ajustes de Apariencia, Inspector y red (2026-10-12)

platformAppearance resuelve el modo efectivo y permiso de animación de Android,
sin persistir estado del sistema. motionEnabled combina ambas políticas y sus
consumidores se actualizan mediante LocalAppearance. Restablecer elimina solo
claves visuales en una operación DataStore. Inicio consume artistGrouped del VM.

Inspector: FullPlayerSheet → AudioInspectorDialog → LibraryViewModel → contrato
AudioRepository.inspectAudio → AndroidAudioInspector en IO con Mutex. Resultado
AudioInspection transitorio, sin Room ni modificación del reproductor. El resultado
se asocia a ID/URI y el efecto se cancela al cerrar/cambiar canción. FLAC nativo
usa lector acotado de cabecera; el resto, MediaExtractor con release en finally.
El flag histórico isHiRes no se propaga a Song y sus filas permanecen intactas.

SyncedLyricsParser comparte cálculo/orden entre letras locales y LRCLIB. Letras,
artistas y actualizaciones usan BoundedHttpReader; ReleaseVersion compara releases
estables. Ninguna nueva dependencia, permiso ni esquema. Detalles en documentos
14–16; llamadas nativas y proveedores aún requieren medición Android.

## Componentes visuales compartidos (2026-10-12)

ColorScheme define superficies neutras y negro puro en modo oscuro; acentos
dependen de la paleta. SoundFlowPanel y panelColors concentran el degradado
estático y la alternativa opaca sin capturar el fondo ni crear bitmaps.
LocalAppearance controla además crossfades y pestañas/letras del recorrido
intervenido. Perfil/Ajustes/DSP reutilizan el panel. MainActivity adapta barras
al modo y consume márgenes del Scaffold. No cambia ningún contrato de datos
ni DSP. [Archivos y validación](13-FASE-5-REDISENO-VISUAL.md).

## Actualizacion 4B y base Apariencia

AudioRepositoryImpl implementa AudioRepository; LibraryViewModel depende de la
interfaz. LibraryFolder y FolderImportResult son modelos sin Room. El contrato
mantiene Uri Android y la gestion de algunas excepciones data: separacion gradual.

ViewModel es propietario de songs compartido y lo entrega a observePlaylists y
al indice del controlador (derivado en Default). allPlaylists frio se mantiene
para compatibilidad; no hay scope global de repositorio. navigationPlayerState
excluye progreso/bytes; hosts de reproduccion recogen el estado completo localmente.
Busquedas usan Default con cancelacion y snapshot de consulta/fuente. UI activa
colecta con ciclo de vida. No se modifica modelo persistente ni esquema.

ArtistImageFetcher usa BoundedHttpReader, politica de disco ArtistCachePolicy,
limites de concurrencia, backoff y AtomicFile. Conserva acceso externo existente;
fallo de red no altera canciones ni datos Room.

AppearanceSettings se publica como snapshot reactivo desde settings_prefs y
LocalAppearance distribuye politica visual. Tema usa ColorScheme unico; panel
configura paletas, modo y dos efectos. Consumidores iniciales: navegacion,
visualizador y fondo del reproductor. Extension al resto de UI queda en fase 5.
No cambia DSP por opciones de apariencia. Detalles/validacion en documento 12.

Las secciones de bloques anteriores conservan el historial de decisiones.

## Organizacion actual

Codigo principal:
com.example.comarleyaetheraudio

### data/local

- MusicDatabase
- DAO de musica y playlists
- Entidades Room
- Utilidades de escaneo, etiquetas, letras y portadas

### data/player

- AudioPlayerHandler
- EqualizerHandler
- SoundFlowMediaService

### data/remote

- GitHubUpdateChecker
- LrcLibService

### data/repository

- AudioRepositoryImpl
- SettingsRepository

### domain

- Modelos Song, Playlist, AudioPlayerState y LyricLine
- Contratos
- Casos de uso

### presentation

- Pantallas y componentes Compose
- ViewModels
- Navegacion
- Reproductor, biblioteca, carpetas y ajustes

### ui/theme

- Temas, colores y tipografia

## Reglas de arquitectura

1. Mantener responsabilidades claras entre capas.
2. Evitar duplicar estados que puedan observarse desde Room.
3. Las operaciones relacionadas deben ser transaccionales.
4. No hacer trabajo intensivo en el hilo principal.
5. No introducir dependencias innecesarias en ViewModels.
6. Mantener contratos compatibles durante cada fase.
7. Escribir pruebas para cambios que afecten datos.

## Decisiones vigentes

- Mantener el nombre de BD: soundflow_database.
- Conservar el esquema Room version 2 inicialmente.
- Evitar modificaciones de entidades sin migraciones.
- Mantener el paquete real de MusicDao: data.local.
- No mover paquetes solo para igualar carpetas fisicas.
- Trabajar sobre C:\SoundFlow-Git.
- No copiar archivos de la carpeta antigua.
- Apertura sin recreacion destructiva de Room: MusicDatabase.buildDatabase
  comparte configuracion con las pruebas aisladas. Ante incompatibilidad,
  conservar los datos y rechazar apertura hasta definir recuperacion.
- Politica y limites de compatibilidad en 06-POLITICA-DE-PERSISTENCIA.md.
- Fase 2: importacion y retirada en transacciones de MusicDao. El repositorio
  valida permiso de lectura y escanea fuera de la transaccion. ViewModel
  serializa operaciones de carpetas con Mutex y expone progreso/resultado.
- SongIdentity centraliza la identidad de comparacion: autoridad + documentId
  para URIs SAF estandar, contentUri exacta para otras fuentes. El documentId
  es opaco: decodificacion unica, sin normalizar rutas ni cambiar mayusculas.
  La URI de acceso almacenada, el ID Room y las etiquetas se conservan.
  No fusionar duplicados historicos ni equiparar SAF con MediaStore.
- Si un documento pertenece a otra carpeta, rechazar toda la importacion
  dentro de la transaccion, incluidos alias de arboles del mismo proveedor.
  Base de la comparacion: [contrato Android de documentId](https://developer.android.com/reference/android/provider/DocumentsContract.Document#COLUMN_DOCUMENT_ID).
  Este contrato no identifica copias ni archivos de proveedores distintos.
- Un permiso perdido no se repara cambiando la URI automaticamente: volver
  a conceder acceso al arbol original. No se implementa reasignacion a otra URI.
- Ajuste final de fase 2: ViewModel conserva la URI del ultimo error de permiso;
  FoldersScreen ofrece Restaurar acceso. AudioRepositoryImpl valida la misma URI
  antes de tomar una concesion persistente o importar. Otra carpeta no sustituye
  pertenencia ni URI de acceso de canciones existentes. Estado de error transitorio:
  si se recrea el proceso hay que actualizar de nuevo para detectar el fallo.
- FolderScanner recibe una instantanea solo de la carpeta seleccionada para
  evitar extraer otra vez metadatos de canciones conocidas. No observa cambios
  externos de etiquetas; el DAO revalida la instantanea durante la escritura.
- MediaStoreAudioScanner es codigo sin consumidor actual. No mezclar su _ID
  con IDs autogenerados de Room ni activar una segunda fuente sin contrato de identidad.

## Ciclo de vida de reproduccion - Fase 3

Fase 3, primer bloque local:
- MainActivity obtiene LibraryViewModel con ViewModelProvider/Factory. Su
  AudioPlayerHandler usa applicationContext y se libera desde onCleared.
  Girar conserva el propietario; liberar MediaController no detiene ni vacia
  ExoPlayer del servicio. Resolver la pista del servicio por ID observado en Room.
- SoundFlowMediaService configura AudioAttributes de musica, audio focus
  y becoming noisy en ExoPlayer, propietario de la reproduccion.
- Handler conserva future/listener y scope cancelable. Errores observables
  en AudioPlayerState; reconexion/reintento explicitos. Peticion antes de conectar
  conserva la ultima seleccion. Caratulas en IO con cancelacion/identidad.
- UI decodifica caratula muestreada y extrae paleta en Default. MiniPlayerCard
  comparte PlaybackStatusBanner con FullPlayerSheet; no conoce repositorios.
- PlaybackPosition contiene limites puros de duracion/seek. Un job coordina
  atenuacion y restaura volumen. Temporizador pertenece todavia al ViewModel;
  moverlo al servicio requiere un contrato posterior para sobrevivir al cierre.

## AudioFX - Fase 3B local

- El servicio posee AudioEffectsEngine y adaptadores Android sobre la sesion
  real de ExoPlayer. Creacion, setters, lectura y release en un executor serial;
  publicacion de estado y callbacks MediaSession en el hilo de aplicacion.
- Nuevo ID de sesion libera efectos anteriores y reinicia neutro/apagado.
  El mismo ID conserva ajustes; preferencias permanentes no implementadas.
- AudioEffectsProtocol transporta valores/capacidades sin objetos AudioFX.
  Comandos solo para UID de la app; controles externos normales conservados.
- AudioPlayerHandler expone StateFlow, ignora revisiones viejas y limita
  espera de comandos a cinco segundos, sin reintentar ni activar por defecto.
  Cancelar future no revierte una operacion nativa ya encolada.
- AudioFxScreen enlaza estado confirmado a AudioFxPanel. Borrador de slider
  solo durante gesto; envio al terminar y lectura de vuelta del motor.
- Soporte parcial y perdida de control se manejan por capacidades/errores.
  Fallos intentan desactivar/liberar efectos, sin detener reproduccion.
- Se retira el inicializador DSP sin consumidores de AudioPlayerHandler,
  incluido el refuerzo LoudnessEnhancer implicito. EqualizerHandler.kt se
  conserva sin llamadas y no participa en este recorrido.

## Temporizador y recursos - Fase 3C / 4A

- SleepTimerState representa cuenta atras, espera, detencion y apagado sin objetos
  Android. El handler conserva minutos para compatibilidad; UI usa estado completo.
- AudioFileInfo representa metadatos guardados, sin inferir salida ni Hi-Res.
- ArtworkSizing puro calcula muestreo; ArtworkDecoder Android lee dimensiones
  antes de asignar. CoverCacheManager entrega archivos atomicos, serializa
  extraccion y deja la cache RAM de UI a Coil. No reciclar bitmaps de UI compartidos.
- LibraryViewModel alimenta el controlador con songs compartido. El repositorio
  sigue exponiendo Flows frios; mapeo y composicion de playlists usan Default.
  La suscripcion adicional en allPlaylists no se elimina en este bloque.
- Lectura local LRC en IO, resultados ligados al efecto de la cancion; conexiones
  LRCLIB cerradas en finally con propagacion de cancelacion.
- No cambian Room, entidades, paquetes ni dependencias. Contrato domain/AudioRepository
  no coincide aun con AudioRepositoryImpl y requiere alineacion gradual en fase 4.
- Politicas de efectos visuales y preferencias globales se planifican para fase 5
  sobre DataStore existente. No se implementan switches sin consumidores globales.

## Decisiones pendientes

- Reconciliacion de duplicados historicos y cambios de documento; comparacion
  estable de documentos SAF ya implementada en fase 2.
- Implementacion de migraciones historicas con fixtures reales; estrategia
  de preservacion definida en fase 1, compatibilidad historica no resuelta.
- Uso de indices y restricciones de unicidad.
- Manejo de referencias cuando desaparecen canciones.
- Estrategia de cache y persistencia de preferencias.
- Diseño definitivo de servicios e interfaces de dominio.

### Contexto de reproducción y orden (2026-10-12)

Las pantallas activas envían canción + snapshot de lista visible + modo aleatorio
al AudioPlayerHandler. PlaybackRequest normaliza IDs y posición inicial; no usa
la lista global para sustituir un subconjunto. Cambiar filtro no reconstruye la
cola activa. LibraryOrder vive en domain y su preferencia en SettingsRepository.
ArtistIdentity valida nombres antes de guardar retratos; ArtistPortrait comparte
fallback y respeta privacidad. SearchScreen calcula coincidencias y grupos en
Dispatchers.Default y descarta snapshots de consulta/categoría anterior.
Room v2, identificadores, entidades y dependencias conservados.

### Historial y presentación 4.0.0-rc.1

El servicio Media3 acredita avance reproducido con ListeningTracker y opt-in local.
SettingsRepository separa listening_private de settings_prefs; counters/flag se
observan por Flow y los contadores no reinician el observador de activación.
DataStore de historial excluido de backups configurados. Un fondo Coil acotado
se dibuja detrás de la app con velo de tema; componentes mantienen contraste.
La sesión recibe carátula de la pista actual y retira bytes de otras pistas.
Room y sus entidades/versiones permanecen iguales. Ver docs/19 para límites.
