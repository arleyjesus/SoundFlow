# Auditoria del estado actual

Base: codigo compartido de C:\SoundFlow-Git.

Este documento diferencia hallazgos observados,
riesgos tecnicos y funcionalidades pendientes de probar.

## Hallazgos observados

### Persistencia

- La base de datos se llama soundflow_database.
- MusicDatabase utiliza Room con version 2.
- exportSchema esta desactivado.
- Se utiliza fallbackToDestructiveMigration().
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

Estado: CAMBIOS COMPILADOS, PRUEBAS FUNCIONALES PENDIENTES.

## Riesgos por verificar

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
