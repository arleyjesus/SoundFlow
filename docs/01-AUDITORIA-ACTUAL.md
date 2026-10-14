# Auditoria del estado actual

Base: codigo compartido de C:\SoundFlow-Git.

Este documento diferencia hallazgos observados,
riesgos tecnicos y funcionalidades pendientes de probar.

## Estado actual: primer bloque 6/7 implementado

Smart DJ por metadatos y candidatos a duplicados conectados a Inicio, con acciones
explícitas y sin escrituras en biblioteca. Controles de privacidad persistentes en
Ajustes conectados a imágenes/letras/actualizaciones. Herramienta release local
sin firmar/publicar. 137 fuentes Kotlin comprobadas y 98 JVM directos correctos;
Gradle nuevo bloqueado por loopback, sin nuevo APK validado. Fases 1–5 permanecen
aceptadas; no se reabren pruebas excluidas. Límites y archivos en documento 17.

## Historial de aceptación 4/5

Decisión posterior: usuario confirma el último APK correcto y excluye las pruebas
inaccesibles al agente del cierre. Fases 1–5 aceptadas y cerradas en el alcance
acordado; no se afirma ejecución de instrumentación, benchmarks o revisión visual
exhaustiva. Se conservan evidencias y riesgos técnicos; no bloquean el avance por
la mera ausencia de esas pruebas. Los pendientes que siguen son históricos.

Gradle del usuario verificado en build/validation/20261012-163921-UTC: app y
androidTest compilados, 88 JVM sin fallos/errores/omitidos, Lint 0 errores,
82 avisos/3 sugerencias y dependencias resueltas revisadas. APK instalado y
aceptado en general; captura muestra Inspector leyendo Opus 48 kHz estéreo.
No acredita otros formatos, salida efectiva, TalkBack ni memoria/cuadros.

Auditoría final: Compose animation 1.12.0 frente a UI/foundation 1.10.0 y
Material3 1.3.1; core-ktx/annotation forzados a 1.12.0/1.7.0. Son riesgos de
alineación, no fallos demostrados ni actualización autorizada. Se clasifican
los avisos en documento 14. No se añade baseline ni se desactiva Lint.

Ajustes posteriores al APK aceptado: visualizador ligado a RESUMED, cabecera
flexible y tiempo con Locale.ROOT. Gradle del agente bloqueado por loopback.
ADB sin dispositivos/emuladores. Cierre integral condicionado a validación
externa explícita; no se atribuyen al APK anterior cambios posteriores.

## Historial - ajustes finales e Inspector (2026-10-12)

Implementados: seguir sistema/restablecer apariencia, política efectiva de
animaciones, fuente grande, artistas compartidos y degradado reutilizado.
Corregidos clasificación Hi-Res sin bits, centésimas LRC y descargas de letras/
actualizaciones sin límite; el comprobador ya usa el repositorio real y compara
versiones numéricamente. Audio Inspector lee la fuente a demanda sin escribir Room.

131 fuentes Kotlin comprobadas directamente; 88 JVM pasan. Gradle bloqueado antes
de tareas por loopback, sin APK nuevo ni lint/informe resuelto. 73 instrumentados
nuevos acumulados más plantilla sin ejecutar; trece nuevos respecto al APK aceptado.
Memoria/cuadros, matriz visual, accesibilidad y salida de audio sin medir. La revisión
de privacidad observa consultas Deezer/LRCLIB/GitHub y backup aún de plantilla.
Entidades, esquema, dependencias y configuración global conservados.
Detalles y pendientes reales en documentos 14, 15 y 16. Fases 4–7 sin cierre integral.

## Estado anterior - rediseño fase 5 (2026-10-12)

Actualización posterior: APK app/tests regenerados a las 10:15:34/33 Bogotá,
después de las fuentes; contenido del rediseño y cambio de hash verificados.
65 JVM Gradle sin fallos/errores/omitidos. El usuario confirma funcionamiento
correcto del APK. Bloque aceptado en general; matriz visual, instrumentación,
accesibilidad y perfilado pendientes. El bloqueo que sigue corresponde al
intento anterior del agente. Sin cambios nuevos de código en esta verificación.

Se intervienen superficies activas, minirreproductor, zona superior del
reproductor, Perfil/Ajustes y consumidores de Apariencia. Negro puro, grises
neutros, paneles con degradado estático y alternativa opaca. Se mantienen
distribución de controles y visualizador. Sin cambios de persistencia o audio.

116 fuentes Kotlin pasan comprobación directa; 65 JVM pasan. Gradle del agente
bloqueado antes de tareas por loopback; APK nuevo y revisión visual pendientes.
No atribuir el APK aceptado anterior a este rediseño. Los efectos de Material
no están todos controlados por el interruptor. [Detalle y límites](13-FASE-5-REDISENO-VISUAL.md).

## Estado anterior - 4B y base de fase 5 (2026-10-11)

Actualizacion posterior: 62 JVM Gradle sin fallos, APK app/tests generados a las
23:23 del 2026-10-11 y usuario confirma funcionamiento correcto. Se supera el
pendiente de APK de este bloque; los resultados directos y bloqueo del agente
que siguen son historicos. Instrumentacion, mediciones, dependencias resueltas
y regresion por caso siguen pendientes.

Resueltos en codigo: descargas/cache de artistas sin limites y con claves
hashCode, observacion duplicada de canciones para playlists, busqueda en
composicion, progreso observado desde raiz y contrato de repositorio sin
implementar. ViewModel usa interfaz; carpetas de presentacion no son entidades.

Fase 5 iniciada con Apariencia persistente, tema unificado y consumidores reales
de efectos en navegacion, visualizador y fondo del reproductor. Aplicacion de
tokens/politicas al resto de pantallas pendiente, no se declara rediseño terminado.

61 JVM directos pasan; comprobacion Kotlin/Compose parcial de 71 fuentes correcta.
Gradle bloqueado antes de tareas por loopback; falta APK nuevo, instrumentacion,
mediciones y regresion. Implementacion esencial fase 4 terminada; aceptacion
final pendiente. Riesgos conservados: identificacion semantica de artistas,
metadata nativa bloqueante, compatibilidad historica/migraciones y dependencias
resueltas sin informe actual. [Evidencia completa](12-CIERRE-FASE-4-E-INICIO-FASE-5.md).

Los estados de bloques anteriores que siguen son historicos.

## Hallazgos observados

### Persistencia

- La base de datos se llama soundflow_database.
- MusicDatabase utiliza Room con version 2.
- exportSchema esta desactivado.
- La configuracion inicial utilizaba fallbackToDestructiveMigration();
  se retira en el ajuste de fase 1 del 2026-10-11, pendiente de validacion.
- Existen tablas de canciones, carpetas, favoritos,
  playlists y relaciones entre playlist y cancion.
- En la version inicial auditada, los favoritos se
  mantenian en MutableStateFlow, sin escritura a Room.
- El DAO inicial insertaba canciones con REPLACE.
- Las entidades de canciones tienen ID autogenerado.
- Las relaciones de playlists usan identificadores de canciones.
- El renombrado original de playlists reemplazaba createdAt.
- La eliminacion original de playlists no limpiaba
  explicitamente las relaciones cruzadas.

### Escaneo y biblioteca

- FolderScanner recorre carpetas SAF recursivamente.
- Los archivos escaneados se construyen como SongEntity.
- La insercion original no comprobaba previamente si la
  cancion ya estaba registrada.
- Se requiere verificar la estabilidad del identificador
  entre escaneos sucesivos.
- La eliminacion de carpetas borra canciones por folderUri
  y carpetas por path.
- El escaneo puede resultar costoso con bibliotecas grandes.

### Audio y metadatos

- Se utilizan MediaMetadataRetriever y MediaStore.
- La clasificacion Hi-Res necesita criterios mas precisos.
- Ser FLAC o WAV no garantiza audio Hi-Res.
- El bitrate y el sample rate detectados no garantizan
  salida bit-perfect ni calidad real de la ruta de audio.
- MediaStoreAudioScanner usa la columna DATA, cuyo uso
  debe revisarse para versiones recientes de Android.

### Letras, imagenes y cache

- LrcParser admite letras externas y metadatos.
- Debe revisarse el calculo de fracciones de segundo LRC.
- Inicialmente ArtworkLoader y CoverCacheManager mantenian caches RAM
  adicionales; CoverCacheManager contemplaba hasta 25% del heap.
- Fase 4A retira la cache RAM de CoverCacheManager: entrega miniaturas en disco
  y Coil gestiona imagenes de UI. ArtworkLoader no tiene consumidores encontrados;
  conserva cache defensiva con techo 20 MiB y decodificacion muestreada.
- Se requiere medir consumo, invalidacion y comportamiento
  de cache para bibliotecas grandes.
- ArtistImageFetcher consulta Deezer para caratulas
  de artistas: revisar privacidad, errores de red,
  tiempos de espera y gestion de conexiones.

### Herramientas

- Hay operaciones de edicion de etiquetas musicales.
- Deben probarse permisos y compatibilidad con URIs SAF.
- Hay advertencias de APIs y dependencias obsoletas.

## Cambios recientes

### Estado vigente - 3C y comienzo de fase 4 (2026-10-11)

- 3C implementada: informacion del archivo sin certificar salida e indicador
  real de espera/detencion del temporizador; reconoce repeticion de pista.
- 4A implementado: muestreo antes de decodificar, escritura AtomicFile y
  extracciones serializadas. Los hits no esperan al proveedor ni crean bitmaps completos.
- ViewModel comparte songs con el controlador; mapeo/composicion de listas
  en Default y lectura local de letras en IO. HTTP de letras libera conexion.
- Ajustes corrige promesa infundada de 500 MB RAM/120 FPS y version fija.
- Compilacion parcial Kotlin/Compose pasa para 36 archivos; JUnit directo
  40/40. Gradle falla antes de tareas por Unable to establish loopback connection.
  Seis instrumentados añadidos, compilados parcialmente pero no ejecutados.
- Pendientes confirmados: red/cache de artistas sin limites propios de descarga,
  suscripcion extra allSongs en playlists y contrato de repositorio desacoplado
  de su interfaz domain. Riesgos por medir: memoria total, recomposiciones y blur.
- La fase 3 queda con desarrollo final preparado y APK/regresion pendientes;
  fase 4 autorizada en curso. Fase 5 integral solo planificada.
- Evidencia, archivos, limites y siguiente bloque en documentos 09, 10 y 11.

Actualizacion posterior: Gradle genero APK app/tests a las 22:41 Bogota y XML
con 41 JVM sin fallos (40 especificas y plantilla). Usuario confirma que el APK
funciona correctamente. Los 55 instrumentados nuevos compilaron, sin ejecucion.
Desarrollo de fase 3 aceptado; fase 4A con aceptacion general. Pendientes: 4B,
mediciones y regresion especifica. El bloqueo anterior era del entorno del agente.

Los apartados siguientes conservan el historial de intervenciones anteriores.

- Se prepararon consultas Room para favoritos persistentes.
- Se modifico el repositorio para observar favoritos desde Room.
- Se corrigio el renombrado de playlists mediante UPDATE.
- Se agrego eliminacion transaccional de playlist y relaciones.
- El usuario reporto compilacion correcta.

La compilacion anterior fue reportada por el usuario y corresponde a
los cambios iniciales. No valida automaticamente las modificaciones posteriores.

### Actualizacion de fase 1A - 2026-10-10

Implementado sobre los cambios locales existentes:

- PlaylistDao ejecuta lectura, inserciones y retiradas de la seleccion
  completa dentro de `setPlaylistSongs`, anotado con `@Transaction`.
- AudioRepositoryImpl conserva el contrato `addSongsToPlaylist` para
  seleccion completa y delega en el DAO; `addSongToPlaylist` es aditivo.
- Ambas operaciones comprueban dentro de la transaccion que la playlist
  siga existiendo. No se han agregado claves foraneas ni cambiado entidades.
- AppNavigation usa la adicion individual para el menu de una cancion.
- LibraryScreen conserva el ID seleccionado, obtiene la cancion actual
  y muestra SongOptionsMenuSheet con favoritos observados desde Room.
- Se corrigio el applicationId esperado en el test de plantilla.
- Se escribieron 12 pruebas instrumentadas nuevas: 10 de datos y 2 de UI.
  Incluyen base en disco, reapertura, Flow, createdAt, adicion idempotente,
  seleccion vacia, concurrencia y rollback ante fallos SQL controlados.

Antecedente: el intento del agente no pudo compilar en este entorno.
El primer intento de Gradle fue bloqueado por acceso a su cache.
El intento con permisos y la repeticion con stacktrace fallaron antes de
compilar por `Unable to establish loopback connection`, causado por
`java.net.SocketException: Invalid argument: connect` en Java NIO.
No se modifico la configuracion global del entorno ni las dependencias.

Actualizacion de validacion - 2026-10-11:

- El usuario compartio BUILD SUCCESSFUL desde su terminal para
  :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest
  con --offline --console=plain. App y APK de tests compilados; tarea de
  tests unitarios satisfactoria, sin registrar aqui cantidad de casos.
- El usuario probo el APK y reporto funcionamiento correcto. Es un reporte
  manual general; no se registraron dispositivo, API ni resultados por caso.
- Las 12 pruebas instrumentadas no se ejecutaron. El intento conectado
  offline fallo por una dependencia UTP ausente en cache. El usuario
  prefiere probar el APK manualmente y aplaza la instrumentacion.

Limites: las pruebas de UI montan LibraryScreen con Room y callbacks al
repositorio; no arrancan el servicio multimedia ni prueban toda la navegacion.
La conexion AppNavigation -> ViewModel se reviso en codigo; falta registrar
el resultado manual especifico. Las pruebas de reapertura no sustituyen reinicio de proceso,
actualizacion de APK ni pruebas de migraciones.

Siguen pendientes identidad estable, duplicados, retirada de carpetas
y referencias a canciones eliminadas (fase 2). La fase 1 no esta cerrada.

### Revision de compatibilidad y proteccion de apertura - 2026-10-11

- Se reviso el historial: 433008d usa aether_music_db version 1;
  61ccd52 usa soundflow_database version 2 sin SongEntity.path;
  c5c8101 agrega path conservando version 2. No hay una migracion generica
  segura deducible solo del numero de version.
- MusicDatabase retira la recreacion destructiva y centraliza el constructor
  para que produccion y pruebas usen la misma politica de apertura.
- Se mantienen nombre, version, entidades y dependencias. Una base incompatible
  puede impedir la apertura; se preserva para una recuperacion posterior.
- Se agregan cuatro pruebas instrumentadas de compatibilidad y conservacion
  de filas: version actual, upgrade/downgrade no soportados e identidad
  desconocida. Son casos sinteticos, no migraciones historicas verificadas.
- El informe unitario anterior confirma un unico test de plantilla
  (addition_isCorrect), sin fallos. No prueba integridad de Room.
- Compilacion nueva bloqueada antes de tareas por loopback de Gradle;
  acceso a cache concedido e IPv4 temporal no resolvieron el error.
  Las 16 pruebas instrumentadas nuevas siguen sin ejecutarse.
- El APK existente es anterior a este ajuste; la prueba manual del usuario
  corresponde a fase 1A, no valida el cambio posterior en MusicDatabase.
- Politica y matriz de compatibilidad: 06-POLITICA-DE-PERSISTENCIA.md.

La revision de la estrategia de migraciones queda realizada. La implementacion
de migraciones historicas requiere fixtures y otra intervencion aprobada;
no se declara resuelta la compatibilidad de todas las versiones publicadas.

### Entrega de desarrollo de fase 1 autorizada - 2026-10-11

El usuario confirma compilacion posterior correcta: BUILD SUCCESSFUL in 1m 14s,
77 tareas, 15 ejecutadas y 62 actualizadas. APK local regenerado a las 19:11
(America/Bogota); informe unitario inspeccionado: 1 test, sin fallos ni errores.
Esto supera el bloqueo de compilacion del agente para el ajuste final.
La prueba manual satisfactoria anterior corresponde a fase 1A. Instrumentacion
aplazada (16 tests) y comprobacion manual del ultimo APK siguen pendientes.
El usuario autoriza guardar/publicar fase 1 y comenzar fase 2. La entrega
no se presenta como validacion exhaustiva ni compatibilidad historica resuelta.

## Riesgos por verificar

Actualizacion de fase 2: el primer bloque previene nuevas inserciones de URI
exacta ya registrada y hace atomica la retirada de carpeta y sus referencias.
El primer bloque esta publicado en ce6f621, con compilacion confirmada por el usuario
(42s, 77 tareas) y prueba manual general satisfactoria reportada. Instrumentacion
pendiente. No se declaran
resueltos alias entre proveedores, duplicados antiguos ni solapamientos generales.
El escaneo conserva las canciones ausentes; no es una reconciliacion completa.
Las etiquetas de canciones ya registradas se conservan para no sobrescribir
ediciones locales. Los cambios de metadatos externos quedan para otra revision.

Segundo bloque local - identidad SAF (2026-10-11):

- Confirmado en codigo: comparar solo URI exacta no detectaba el mismo documento
  expuesto mediante dos arboles SAF del mismo proveedor.
- SongIdentity compara autoridad e ID de documento opaco en URIs SAF estandar;
  DAO y escaner comparten la regla. Otras URIs mantienen comparacion exacta.
  No se reescriben URIs, IDs, etiquetas ni relaciones existentes.
- Un documento ya perteneciente a otra carpeta rechaza toda la importacion
  antes de insertar, incluidos los alias entre arboles. No se fusionan filas
  historicas ni se equiparan proveedores distintos con MediaStore.
- Diez pruebas JVM ejecutadas correctamente con compilador Kotlin y JUnit
  locales, fuera de Gradle. Posteriormente el usuario confirma compilacion y
  funcionamiento correcto. XML Gradle inspeccionado: diez pruebas de identidad,
  sin errores ni fallos; APK de app y de tests regenerados a las 20:08
  America/Bogota. Instrumentacion sin ejecutar; el loopback fue un bloqueo
  del entorno del agente, no un fallo demostrado de la aplicacion.
- El test de permiso cubre denegacion antes de escribir, no revocacion real
  durante un escaneo. Esta ultima y proveedores reales requieren prueba manual.
- Conservar la URI anterior protege referencias, pero no repara un permiso
  perdido: hay que volver a conceder acceso al arbol original. No declarar
  resuelta toda la sincronizacion, la recuperacion de acceso ni los duplicados.

- Duplicados al reescanear carpetas.
- Perdida de referencias de favoritos o playlists.
- Registros huerfanos al eliminar canciones o carpetas.
- Eliminacion de canciones ante escaneos incompletos.
- Conflictos al escanear carpetas superpuestas.
- Comportamiento al reiniciar, actualizar o reinstalar.
- Compatibilidad entre versiones antiguas de la base.
- Rendimiento con bibliotecas musicales grandes.

Estos riesgos no se consideran fallos demostrados
hasta reproducirlos mediante pruebas.

## Prioridades

### Ajuste final de biblioteca y apertura de diagnostico Media3 - 2026-10-11

Confirmado:
- MediaStoreAudioScanner no tiene llamadas en app/src/main. README corregido:
  no se presenta como una fuente activa de importacion.
- El error de permiso anterior carecia de accion directa y contexto de carpeta.
  Ahora se conserva URI afectada en estado transitorio, con selector de recuperacion
  y comprobacion de igualdad antes de tomar permiso o escribir. No reasigna canciones.
- La actualizacion releia metadatos de canciones conocidas. Ahora reutiliza la
  instantanea de esa carpeta; la transaccion conserva la autoridad final sobre filas.
- La politica de duplicados historicos conserva todas las filas y referencias.
  Un test Room nuevo cubre favoritos/playlists de ambos duplicados; sin ejecutar.

Validacion: once pruebas JVM ejecutadas, incluida identidad de 10.000 documentos.
Tres tests Room nuevos y dos Compose pendientes de compilar/ejecutar. Gradle
bloqueado antes de tareas por loopback. No atribuir la aceptacion manual anterior
al nuevo boton ni a la optimizacion del escaner. Rendimiento Android pendiente.

Media3: diagnostico y pruebas propuestas en 07-FASE-3-MOTOR-DE-AUDIO.md.
Confirmados en codigo la creacion manual de ViewModel en Activity, ausencia de
liberacion del controlador, lectura sincrona de caratula en listener y prepare()
incondicional ante error. Fugas, bloqueos y bucles reales requieren reproduccion.

Validacion posterior de fase 2: usuario confirma compilacion (16s, 77 tareas)
y funcionamiento del APK. XML de identidad con 11 pruebas sin fallos/errores;
APK de app y tests regenerados a las 20:50 America/Bogota. Se acepta cierre de
desarrollo con 36 instrumentados compilados pero sin ejecutar y sin medicion
exhaustiva de rendimiento Android. Fase 3 autorizada, incluyendo diseno interno.

### Fase 3 - Primer bloque local de motor y diseno

Corregido en codigo: propiedad manual de ViewModel, ausencia de release del
cliente, prepare() incondicional ante error, atributos/foco fuera del servicio,
lectura sincrona de caratula, fades concurrentes y listener de temporizador
sin retirada al cancelar. Se conecto ademas el dialogo de temporizador que
MainActivity dejaba con callbacks por defecto; ahora respeta el plazo elegido.
No se presenta ninguna consecuencia como fallo reproducido en dispositivo.

Diseno implementado: minirreproductor Material 3, estados de conexion/error,
reintento explicito, controles de 48 dp, contraste del tema y scroll en el
reproductor normal. Etiquetas de +/-10s corresponden a sus acciones. La
caratula se muestrea a un maximo de 1024 px; paleta y decode fuera del main.

Comprobacion parcial Kotlin/Compose pasa; 16 JVM pasan con compilador/JUnit
locales (11 identidad + 5 posicion). Gradle sigue bloqueado antes de tareas
por loopback: no hay APK nuevo ni prueba visual/audio de fase 3. Cinco tests
instrumentados nuevos escritos, comprobados parcialmente, sin ejecutar.

Riesgos por verificar: foco/auriculares por dispositivo, errores de proveedor,
rotacion/segundo plano, pulsaciones rapidas, temporizador y accesibilidad real.
Pendiente revisar AudioFX, gapless y etiquetas Hi-Res. Fade manual no demuestra
crossfade. Temporizador sigue cancelandose al cerrar definitivamente el cliente;
no se promete que sobreviva a cierre/proceso. Detalle y plan en documento 07.

Validacion posterior del primer bloque: usuario confirma compilacion y APK
funcionando. XML Gradle: 17 tests sin fallos/errores/omitidos (11 identidad,
5 posicion y 1 plantilla), UTC 2026-10-12T02:22:36-37. Ambos APK regenerados
a las 21:22 Bogota; 41 instrumentados nuevos compilados, sin ejecucion registrada.
Aceptacion manual general; los riesgos particulares anteriores siguen abiertos.

Siguiente problema confirmado: AudioFxScreen muestra activacion sin efecto
inicializado; los sliders de graves/virtualizador solo actualizan estado local.
setupAudioEffects e initEqualizer no tienen consumidores. No atribuirlo a falta
de soporte del dispositivo. Propuesta: efectos asociados a sesion real del
servicio, soporte/error observables y panel renovado, con DSP apagado por defecto.
Este bloque solo diagnosticado, sin cambios de codigo en esta intervencion.

### Fase 3B - Efectos y panel implementados localmente

Problemas del panel corregidos en codigo: efectos ahora propiedad del servicio,
asociados a la sesion de ExoPlayer; sliders envian comandos y presentan valores
confirmados. No activar DSP ni ganancias por defecto. Soporte por efecto,
errores, perdida de control y restablecimiento observables. Recursos y llamadas
AudioFX en worker serial; no depender del ciclo de vida de la pantalla.
Se conserva EqualizerHandler.kt sin consumidores; inicializador anterior de
AudioPlayerHandler retirado, incluyendo ganancia LoudnessEnhancer implicita.

28 JVM pasan (12 AudioFX con dobles + 16 regresion), fuera de Gradle; K2/Compose
parcial pasa. Gradle falla antes de tareas por Unable to establish loopback
connection. Ocho instrumentados nuevos y ampliacion del de conexion, sin
ejecutar ni empaquetar en nuevo APK de tests. Total escrito: 49, ninguno ejecutado.
Soporte/audibilidad y diseno reales requieren APK fase 3B. Las pruebas con
dobles no demuestran procesamiento nativo, calidad de salida ni compatibilidad.
Sin cambios Room/dependencias. Informe y manual en documento 08.

Validacion posterior 3B: usuario reporta funcionamiento correcto; Gradle XML
con 29 JVM sin fallos/errores/omitidos. APK app/tests regenerados a las 22:08
Bogota; 49 instrumentados compilados, sin ejecucion registrada. Aceptacion
manual general, no verificacion especifica por efecto/salida/dispositivo.

Siguiente ajuste propuesto: isHiRes sigue mezclando extension lossless,
sampleRate y bitrate; el modelo no guarda profundidad de bits. El distintivo
HI-RES aparece en MusicComponents, distinto del SongItem del recorrido activo:
verificar consumidores antes de afirmar un problema visual en el APK actual.
El temporizador representa espera del fin de pista con 1 minuto y
crossfadeDurationMs no se usa para mezclar pistas. No modificar datos historicos
ni implementar mezcla nueva para corregir informacion/etiquetas.

Critica: integridad de datos y migraciones seguras.
Alta: escaneo, favoritos, playlists y reproduccion.
Media: cache, letras, metadatos y APIs obsoletas.
Posterior: innovaciones y funciones experimentales.

### Actualización 2026-10-12: orden y presentación

Los desfases lista/cola descritos en docs/18 se corrigieron pasando el contexto
visible al reproductor y fijando aleatorio explícitamente. Añadido orden DataStore,
búsqueda por categorías y retrato conservador; actualizado el pulido visual.
114 tests JVM directos aprobados; Gradle/APK/Lint actuales pendientes por loopback.
La prueba final Android se aplaza por solicitud del usuario. El ID sigue siendo
aproximación de incorporación, nunca fecha de descarga certificada.

### Revisión 4.0.0-rc.1

El registro PowerShell confirma que los errores históricos de Lint fueron corregidos:
última base con 114 JVM y 0 errores/81 avisos. Nuevo candidato implementa identidad
propia, fondo, presentación de playlists y escuchas opt-in conservadoras en servicio.
Ver docs/19 para archivos, pruebas, límites y viabilidad de ánimo. La salida visual
de System UI depende de Android/HyperOS; solo se controlan metadatos y acciones.
Nuevo Gradle/APK pendiente por loopback; no atribuir al candidato el Lint anterior.

## 2026-10-13 — orden de archivos y actualización APK

Confirmado y corregido: el orden reciente utilizaba IDs Room en vez de fechas.
Ahora consulta fecha MediaStore o modificación SAF, cachea valores válidos y deja
sin fecha al final. No se puede reconstruir una fecha de descarga ausente del
proveedor. Room v2 e identidades conservadas. Ver docs/21 para límites y pruebas.

Descarga/validación de APK y búsqueda manual implementadas; instalador Android
con aprobación del usuario. Fuente 4.0.0/código 41 preparada, no publicada.
Firma pública v3.7.0 distinta del debug local. Keystore local con alias
soundflow_key, contraseña pendiente; no generar otra clave ni recomendar desinstalar.
Gradle bloqueado antes de tareas por loopback; 158 fuentes y 151 JVM directos.
No se ejecutó actualización Android ni se generó/publicó APK nuevo.
