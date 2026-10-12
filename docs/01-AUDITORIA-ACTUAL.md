# Auditoria del estado actual

Base: codigo compartido de C:\SoundFlow-Git.

Este documento diferencia hallazgos observados,
riesgos tecnicos y funcionalidades pendientes de probar.

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
- ArtworkLoader y CoverCacheManager mantienen caches
  de imagenes en memoria.
- CoverCacheManager contempla hasta 25% del heap.
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
Los cambios estan locales, con compilacion posterior confirmada por el usuario
(42s, 77 tareas) y prueba manual general satisfactoria reportada. Instrumentacion
pendiente. No se declaran
resueltos alias entre proveedores, duplicados antiguos ni solapamientos generales.
El escaneo conserva las canciones ausentes; no es una reconciliacion completa.
Las etiquetas de canciones ya registradas se conservan para no sobrescribir
ediciones locales. Los cambios de metadatos externos quedan para otra revision.

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

Critica: integridad de datos y migraciones seguras.
Alta: escaneo, favoritos, playlists y reproduccion.
Media: cache, letras, metadatos y APIs obsoletas.
Posterior: innovaciones y funciones experimentales.
