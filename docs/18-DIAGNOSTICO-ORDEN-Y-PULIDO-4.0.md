# Orden de reproducción y pulido visual para 4.0

Revisión: 2026-10-12, sobre cambios locales conservados en refactor/soundflow-4.
Diagnóstico inicial y entrega del bloque aprobado. Implementación local: 2026-10-12.
Las fases aceptadas permanecen cerradas; es un bloque adicional previo a distribución.

## Objetivo

Hacer coincidir la reproducción con la lista que ve el usuario y terminar la
coherencia visual de Biblioteca, Buscar, Inicio y reproductor. Mantener datos,
acciones, distribución de controles y ajustes de Apariencia/Privacidad.

## Problemas confirmados al comenzar (histórico)

| Recorrido | Hallazgo |
|---|---|
| Biblioteca → Canciones | LibraryScreen ordena por ID, pero llama onSongClick(song); AppNavigation reproduce con songs original. MusicDao devuelve songs por título. Lista y cola tienen órdenes distintos. |
| Fechas | SongEntity no guarda fecha de descarga ni de incorporación. ID mayor aproxima inserción en Room, no fecha de descarga; tampoco garantiza orden dentro de importación masiva. |
| Playlists abiertas | PlaylistDetailScreen muestra sortedPlaylistSongs, pero los callbacks de LibraryScreen reproducen currentPlaylist.songs original. El botón general también usa el orden original. |
| Artistas | El detalle muestra artistSongs, pero el callback de Biblioteca reproduce con toda la biblioteca. El contexto no queda limitado al artista. |
| Buscar | SearchScreen calcula filteredSongs, pero comunica solo Song; AppNavigation vuelve a usar toda la biblioteca como cola. |
| Aleatorio | playSong conserva shuffleModeEnabled del controlador. Acciones de playlists eligen una primera canción aleatoria, sin expresar de forma explícita el modo de toda la cola. No garantiza reproducción secuencial ni aleatoria según la intención del botón. |
| Orden seleccionado | sortDescending de Biblioteca usa remember; no se conserva como preferencia persistente. |

Estos son desfases de contratos/callbacks, no fallos de Room que exijan cambiar
entidades. No se reproducen en un dispositivo desde el agente; se corroboran con
el reporte del usuario y el recorrido estático del código.

## Estado visual al comenzar (histórico)

- MiniPlayerCard usa FilledIconButton: produce el círculo relleno señalado.
- FullPlayerSheet usa primary/onPrimary en Reproducir. Ya calcula una paleta de
  portada para el fondo, pero ese color no se aplica al botón.
- AudioVisualizerView dibuja barras con una onda sinusoidal decorativa; no mide
  la música. El bucle ya depende de reproducción, primer plano y motionEnabled.
- Navegación y componentes mezclan familias Filled, Rounded y Outlined. Mantener
  semántica reconocible, pero centralizar las elecciones del recorrido activo.
- ArtistImageFetcher toma el primer resultado de Deezer; no verifica que sea
  inequívocamente el artista buscado. La foto puede ser ajena o de un colaborador.
- ArtistCircleCard recibe sampleSong pero no lo usa como portada alternativa;
  Biblioteca sí dispone de alternativa de carátula. Unificar comportamiento.
- Buscar encuentra título/artista/álbum, pero muestra solo filas de canciones;
  no hay selector de categoría ni presentación de resultados agrupados.
- Type.kt usa base SansSerif y pesos fuertes; secundarios de 13 sp y algunos
  metadatos de 11 sp. Conviene ajustar jerarquía y evitar excesos de negrita.

## Intervención propuesta, por prioridad

### A. Corregir comportamiento antes de decorar

Pasar canción + lista visible + intención secuencial/aleatoria hasta el controlador.
Usar la misma lista ordenada/filtrada para dibujar y construir la cola, incluidos
detalle de playlist, artista y resultados de búsqueda. No cambiar el orden de una
cola ya en marcha solo porque se modifica el filtro; aplicarlo al iniciar otra.
Reproducir secuencial desactiva aleatorio; Aleatorio lo activa explícitamente.
Conservar decisiones del usuario mientras continúa una misma cola.

Mostrar «Añadidas recientemente»/«Añadidas antes» en vez de afirmar fecha de
descarga. Añadir orden por título/artista y conservar elección con DataStore.
Una fecha de descarga real no se puede reconstruir con garantías desde SAF ni
deducir de un ID; requeriría diseño de metadatos y, si se persiste en Room, migración
aprobada. Se deja fuera del arreglo esencial.

### B. Biblioteca, Buscar e Inicio

- Canciones: cabecera compacta, conteo, orden claro, fila activa y menú consistente.
- Artistas: lista ordenada, foto verificada cuando haya coincidencia clara,
  alternativa con carátula/Inicial y detalle con reproducción del propio artista.
- Playlists: mosaicos, conteo/duración, acciones claras y detalle con cola coherente.
- Buscar: categorías de canciones/artistas/álbumes, conteos y estados claros;
  mantener cálculo cancelable fuera de composición y selección por ID.
- Inicio: conservar estructura, equilibrar tamaños, jerarquía y tarjetas; dar
  presencia a Smart DJ sin competir con el contenido musical.

### C. Reproductor y sistema visual

- Mini: botón circular transparente con borde e icono contrastado; mantener 48 dp.
- Reproductor abierto: acento de portada para botón principal, texto/icono elegido
  por contraste, alternativa al acento del tema cuando no haya portada válida.
  No cambiar colores globales ni supeditar el acento a Transparencias.
- Visualizador decorativo renovado con amplitudes más suaves, un degradado
  compartido y estado estable en pausa; no añadir captura de audio ni permisos.
- Una familia coherente de iconos de navegación/reproducción, acciones correctas,
  estados seleccionados reconocibles y descripciones accesibles.
- Cristal sutil en mini, cabeceras o paneles elegidos; listas legibles y sobrias.
  Preferir degradado/borde y pocas capas; evitar desenfoque continuo o refacción
  simulada costosa. Transparencias apagadas conserva alternativa opaca.
- Tipografía: aligerar títulos, mejorar secundarios y jerarquía; conservar tamaño
  escalable de Android, contraste claro/oscuro y objetivos táctiles de 48 dp.

## Archivos previstos

LibraryScreen, PlaylistDetailScreen, ArtistDetailBottomSheet, SearchScreen,
HomeScreen, AppNavigation y AudioPlayerHandler para el contexto/orden de cola.
SettingsRepository para preferencia de orden; modelos de política en domain.
MiniPlayerCard, FullPlayerSheet, AudioVisualizerView, MainActivity/Screen,
Type.kt y componentes de tarjetas/artistas para el pulido.
ArtistImageFetcher para resolver coincidencias sin ampliar proveedores o permisos.

Mantener contratos compatibles por defecto, IDs, Room v2, entidades, dependencias,
privacidad, controles y preferencias actuales. No cambiar firma/versión/publicación.

## Verificación prevista

Pruebas JVM útiles para orden/IDs, selección del inicio de cola y política de
shuffle; comprobar ascendentes/descendentes, subconjunto de artista/búsqueda,
lista vacía y cambio de orden entre acciones. Compilar app y Lint cuando sea posible.
El usuario prueba el APK con siguiente/anterior desde listas ordenadas y con
claro/oscuro, portada y efectos activados/desactivados. No reabrir instrumentación
o benchmarks inaccesibles excluidos por el usuario.

El diagnóstico inicial se conserva como referencia. El usuario aprobó después
la implementación del bloque completo; véase la entrega documentada al final.

## Mejoras posteriores, sin ampliar este bloque

Cola visible con contexto y reordenación explícita, orden por número de pista en
álbumes y selección personalizada de foto del artista. Copia portable de favoritos/
playlists continúa siendo útil pero necesita diseñar reidentificación/restauración.
Replay, Wi-Fi y DAC avanzado siguen separados del pulido y de las promesas de audio.

## Entrega implementada tras aprobación

El usuario autorizó ejecutar el bloque completo y reservar la instalación/prueba
Android para el final. No se solicitan pruebas intermedias con APK.

- Cola: AudioPlayerHandler recibe canción, lista visible e intención aleatoria.
  Elimina IDs repetidos conservando orden; si falta la canción seleccionada,
  reproduce solo esa canción. Cada selección normal desactiva aleatorio; el botón
  Aleatorio lo activa para la cola completa. El cambio de filtro no altera una
  cola que ya está reproduciéndose.
- Biblioteca: orden por incorporación aproximada (ID), título o artista. DataStore
  conserva la opción; no se presenta como fecha real de descarga. Fila activa
  resaltada, artistas ordenados y detalle limitado a su lista.
- Playlists: reproducción de filas y botones sigue la lista ordenada del detalle.
  En Inicio, «Ver todas» permite abrir el detalle sin reproducir accidentalmente;
  Renombrar queda conectado. Botones «Reproducir»/«Aleatorio» en español.
- Buscar: filtros Todo/Canciones/Artistas/Álbumes, conteo y resultados agrupados.
  Álbumes separados por artista y nombre; detalle con su propia cola. Búsqueda y
  agrupación fuera del hilo principal; resultados anteriores no se usan durante
  la actualización. El panel de álbum no consulta retratos de artistas por su título.
- Inicio: conserva estructura; panel sutil de Smart DJ y jerarquía tipográfica
  más ligera. Favoritos utiliza el acento del tema.
- Artistas: retrato compartido en Inicio/Biblioteca/detalle, coincidencia exacta
  normalizada y única entre resultados del proveedor; carátula local o inicial
  como alternativa. No certifica identidad ante homónimos. Caché con nueva clave
  para no reutilizar fotos seleccionadas por la heurística anterior. Respeta
  Privacidad, límites de red y caché existentes; sin proveedor nuevo.
- Reproductor: mini con círculo transparente, borde y objetivo de 48 dp; botón
  principal con acento de portada y blanco/negro según contraste. Sin portada,
  utiliza el acento del tema. Transparencias no controla ese color dinámico.
- Onda decorativa: barras centradas, perfil suave y un degradado compartido;
  bucle retirado en pausa, segundo plano o animaciones desactivadas. No mide
  señal sonora ni incorpora captura, permisos o promesas de rendimiento medido.
- Navegación y acciones: iconos Rounded coherentes y letras con icono específico.
  Tipografía: secundarios y metadatos mayores, menos peso en títulos. Los paneles
  reutilizan la alternativa opaca cuando se desactivan transparencias.

## Archivos del bloque

- domain/library: LibraryOrder.kt y ArtistIdentity.kt; domain/playback: PlaybackRequest.kt.
- AudioPlayerHandler.kt, SettingsRepository.kt, LibraryViewModel.kt y ArtistImageFetcher.kt.
- LibraryScreen.kt, PlaylistDetailScreen.kt, ArtistDetailBottomSheet.kt,
  SearchScreen.kt, HomeScreen.kt, PlaylistsBottomSheet.kt y AppNavigation.kt.
- ArtistPortrait.kt, ArtistCircleCard.kt, SongItem.kt, MiniPlayerCard.kt,
  FullPlayerSheet.kt, AudioVisualizerView.kt, MainActivity.kt, Screen.kt y Type.kt.
- LibraryOrderTest.kt, PlaybackRequestTest.kt, ArtistIdentityTest.kt y
  AppearancePersistenceTest.kt (también conserva orden tras reapertura/reset visual).
- README y documentación de auditoría, roadmap, arquitectura, pruebas y changelog.

## Validación y límites de entrega

Comprobación directa del compilador Kotlin sobre 144 fuentes main/test/androidTest
con las dependencias locales y clases generadas existentes. Suite JVM directa:
114 pruebas aprobadas (98 previas y 16 nuevas). Esta comprobación no sustituye
Gradle, KSP, recursos, empaquetado APK ni Lint.

Gradle intentado con assembleDebug, testDebugUnitTest, assembleDebugAndroidTest
 y lintDebug, sin descargar dependencias. Falló antes de tareas con
`java.io.IOException: Unable to establish loopback connection`. La primera
invocación restringida tampoco pudo abrir el bloqueo del caché del wrapper;
al permitir el caché local reapareció el fallo de loopback conocido.

Registros: build/phase5-design-check/polish-kotlin.txt, polish-junit.txt y
polish-gradle.txt. Comprobación Git: diff --check, diff --stat y status; sin
preparar archivos, commits ni publicación. El diff agregado incluye trabajo
anterior del usuario; no atribuir todo su tamaño a esta intervención.

Room conserva nombre, versión 2 y entidades; sin dependencias nuevas ni cambios
de configuración global. Los cambios quedan guardados en C:\SoundFlow-Git.
No se ejecuta instrumentación ni se afirma rendimiento medido en Android.

## Única validación final con APK

En PowerShell, desde C:\SoundFlow-Git:

```powershell
.\tools\Validate-SoundFlow.ps1
```

Después de BUILD SUCCESSFUL, instalar app\build\outputs\apk\debug\app-debug.apk
sin borrar datos. Validar en una sesión:

1. En Canciones, probar los cuatro órdenes; tocar una fila intermedia y pulsar
   siguiente/anterior. Deben seguir el orden visible. Reabrir y comprobar el filtro.
2. En playlist, cambiar orden y repetir; «Reproducir» secuencial, «Aleatorio»
   aleatorio. Volver a una fila normal debe iniciar una cola secuencial.
3. Artista, búsqueda y álbum: siguiente/anterior permanece en el grupo seleccionado.
   Inicio → Ver todas → abrir playlist no inicia reproducción; Renombrar funciona.
4. Probar dos portadas distintas, pausa y ausencia de portada; revisar color y
   contraste del botón principal, círculo con borde del mini e icono de letras.
5. Claro/oscuro y Transparencias/Animaciones encendidas/apagadas; comprobar
   legibilidad y alternativa opaca. Restaurar Apariencia conserva biblioteca,
   orden y ajustes de audio. Sin conexión, artistas conservan alternativa local.

No reabrir como bloqueantes las mediciones e instrumentación excluidas por el
usuario. La aceptación del APK anterior no valida este bloque nuevo.

### Validación real posterior — PowerShell del usuario

El registro aportado contiene intentos históricos y una última validación completa
en build/validation/20261013-004530-UTC. Gradle terminó BUILD SUCCESSFUL para
app, APK de instrumentación, JVM, Lint e informes de dependencias. XML actual:
114 JVM, cero fallos/errores; Lint actual: cero errores, 81 advertencias y 3 hints.
No confundir el fallo histórico de 11 errores con el resultado posterior, ni
compilar instrumentación con ejecutarla. local.properties ya tiene el escape
correcto de sdk.dir; no se vuelve a modificar. La apariencia anterior del APK
recibió observaciones del usuario, que solicita un nuevo rework, no un fallo de
compilación. Próximo bloque: identidad visual propia, presentación de playlists,
fondo personalizado y Smart DJ; publicación de bloques revisados autorizada
por el usuario en la rama de desarrollo.
