# Plan de pruebas

## Bloque nuevo 6/7 — resultados independientes del APK anterior

- 137 fuentes Kotlin main/test/androidTest comprobadas directamente con Compose.
- JUnit directo: 98 casos sin fallos; 8 nuevos de SmartLibrary y 2 de privacidad.
  Cubren selección favorita/no favorita, IDs/límites/URI repetida, orden y género,
  metadatos desconocidos, margen temporal sin encadenamiento y reapertura/reset.
- Gradle falla antes de tareas por loopback; no APK nuevo, Lint ni KSP aprobados
  para este bloque. No atribuirle aceptación manual o resultados Gradle anteriores.
- Prepare-SoundFlowRelease.ps1 parseado correctamente; ejecución completa pendiente.
- No se añaden pruebas Android inaccesibles como bloqueos. Recorrido manual breve
  y comandos de compilación en documento 17; publicación/firma no realizadas.

## Historial: validación de cierre 4/5

Decisión posterior del usuario: último APK probado satisfactoriamente; fases 1–5
aceptadas para el alcance acordado. Se excluyen del cierre y del avance a 6/7
las pruebas inaccesibles al agente (instrumentación, memoria/cuadros y matriz
visual/accesibilidad exhaustiva). Los tests permanecen guardados sin ejecutarse;
no se cuentan como aprobados. Los recorridos siguientes quedan como referencia
opcional, no requisitos pendientes obligatorios para cerrar las fases aceptadas.

Evidencia verificada: build/validation/20261012-163921-UTC. Gradle 45 s,
assembleDebug/assembleDebugAndroidTest/testDebugUnitTest/lintDebug correctos;
88 JVM, cero fallos, errores u omitidos. Lint: 0 errores, 82 advertencias,
3 sugerencias. Informes runtime/compile correctos y revisados (documento 14).
Usuario confirma APK funcional y captura de Inspector Opus 48000 Hz/2 canales,
sin profundidad/bitrate disponibles. No acredita salida DAC ni bit-perfect.

Posteriormente se ajustan ciclo de vida del visualizador, cabecera y formato
de tiempo. Gradle del agente bloqueado antes de tareas por loopback; estos
ajustes requieren nueva compilación. ADB devuelve lista vacía. Los 74 casos
Android previstos están compilados en el build aceptado, sin ejecutar.
Comprobación directa posterior: las 131 fuentes Kotlin pasan y JUnit ejecuta
88 casos correctamente (closure-kotlin.txt / closure-junit-result.txt). No
genera APK ni sustituye Lint/KSP/Room o ejecución de las pruebas Android.

Recorrido final único, tras regenerar APK: modo sistema y las ocho combinaciones
paleta/modo; efectos activados/desactivados; reinicio y restablecimiento aislado;
fuente grande/horizontal/TalkBack; abrir/cerrar reproductor y volver desde segundo
plano conservando audio; favoritos, playlist y letras. Medir el mismo recorrido
con y sin efectos en el mismo dispositivo/APK. Registrar resultados por caso;
no convertir aceptación general ni lectura de captura en aprobación de toda la matriz.

## Historial - ajustes de cierre e Inspector (2026-10-12)

- Comprobación directa Kotlin 2.2.10/Compose: **131 fuentes main/test/androidTest**,
  código de salida 0. Advertencia existente: Virtualizer de Android obsoleto.
- **88 JVM pasan**, cero fallos, 1,704 s. Regresión anterior más 23 nuevos desde
  el APK aceptado: cuatro Apariencia, diez Inspector/FLAC, cinco letras y cuatro versiones.
- Logs: build/phase5-design-check/final-compile-result.txt y final-junit-result.txt.
  No confundir con XML Gradle previo de 65 tests o con instrumentación ejecutada.
- Android: 67 métodos @Test, uno parametrizado en ocho combinaciones = 74 casos
  previstos (73 nuevos acumulados + plantilla). Trece nuevos respecto al APK
  aceptado, solo comprobados con compilador directo; sin APK tests actualizado.
  Recorrido de menú existente adaptado a lectura a demanda/identidad.
- Gradle assembleDebug/testDebugUnitTest/assembleDebugAndroidTest/lintDebug:
  fallo antes de tareas por `java.io.IOException: Unable to establish loopback connection`.
  Informe debugRuntimeClasspath bloqueado igual. No hubo ejecución de lint/KSP ni APK nuevo.
- Scripts PowerShell: análisis sintáctico correcto, sin ejecución completa ni
  mediciones ADB. Once archivos protegidos comparados con respaldo aceptado,
  sin diferencias en entidades, base Room, dependencias o configuración de build.
- Principal: POCO X7 Pro / Android 16 / ~50 pistas de 20–80 MB. Secundario:
  Redmi / modelo y Android desconocidos / ~200 pistas de ~5 MB. Datos aportados
  por el usuario; no son resultados de pruebas del código nuevo.
- [Validación/recorrido 4–5](14-CIERRE-TECNICO-FASES-4-Y-5.md),
  [fixtures y recorrido Inspector](15-AUDIO-INSPECTOR-Y-FASE-6.md),
  [puertas de lanzamiento](16-PREPARACION-FASE-7.md).

## Resultado anterior - rediseño fase 5 (2026-10-12)

Validación posterior: usuario confirma funcionamiento correcto del APK. App y
APK tests generados a las 10:15:34/33 Bogotá; contenido nuevo y hash de app
distinto del respaldo anterior. XML Gradle: **65 JVM**, cero fallos, errores u
omitidos; UTC 2026-10-12T15:15:37.040Z–15:15:38.303Z. Los casos instrumentados
están compilados, sin ejecución registrada. Accesibilidad, matriz visual y
memoria/cuadros pendientes. No se repiten pruebas ni se modifica código.

Los puntos siguientes describen el intento anterior del agente:

- Gradle del agente bloqueado antes de tareas por `Unable to establish loopback connection`.
- Comprobación directa Kotlin/Compose de 116 fuentes main/test/androidTest correcta;
  no ejecuta KSP/Room ni empaquetado. JUnit directo: 65 pasan, cero fallos.
- Tres JVM nuevos: negro puro, contraste del degradado compuesto y opacidad.
- Un Android nuevo: callbacks de Perfil a Apariencia, DSP y Ajustes sin efectos.
  Total: 60 nuevos instrumentados acumulados y plantilla, sin ejecutar.
- APK nuevo pendiente; outputs y XML Gradle de 62 pruebas siguen correspondiendo
  al bloque aceptado anterior. Matriz visual y regresión detallada pendientes.
- [Comandos, archivos, evidencias y pasos manuales](13-FASE-5-REDISENO-VISUAL.md).

## Resultado anterior - Gradle y aceptacion general de 4B / Apariencia

Usuario confirma funcionamiento correcto del APK. APK app/tests regenerados a las
23:23:57/58 America/Bogota del 2026-10-11, posteriores a las fuentes. XML Gradle
verificados: 62 JVM (61 especificos + plantilla), cero fallos/errores/omitidos,
UTC 2026-10-12T04:23:59–04:24:00. Los 59 instrumentados nuevos acumulados tienen
APK compilado, sin ejecucion registrada. No atribuir pruebas por caso ni metricas
al reporte manual general. Memoria/cuadros, dependencias resueltas y matriz visual
siguen pendientes. No se cambio codigo ni se repitieron pruebas en esta verificacion.

## Intento anterior del agente - 4B e inicio de fase 5 (2026-10-11)

- Gradle offline bloqueado antes de tareas por java.io.IOException:
  Unable to establish loopback connection. APK nuevo, Room/KSP y empaquetado pendientes.
- Kotlin/Compose directo comprueba 71 fuentes seleccionadas, incluidos todos los
  AndroidTest. Usa dependencias locales; no equivale a compilacion Gradle completa.
- JUnit directo: 61 pasan, cero fallos. 40 previos + 17 fase 4 (7 HTTP, 6 cache,
  4 busqueda) + 4 fase 5 (3 persistencia de preferencias, 1 matriz de contraste).
- RepositorySharingTest añade dos Android: biblioteca suministrada con cambios
  de relaciones y una suscripcion compartida. SearchResultsTest añade uno:
  consulta/fuente rapidas y seleccion del ID actual. AppearancePanelTest añade uno:
  paleta y efectos independientes. Total: 59 instrumentados nuevos acumulados
  escritos, ninguno ejecutado. Cuatro nuevos solo comprobados por K2, sin APK tests.
- Logs: build/phase4b-phase5-check/compile-result.txt y junit-result.txt.
- Pruebas con temporales aislados; no usan soundflow_database ni settings_prefs reales.
- Los 41 JVM/XML y APK previos corresponden a 3C/4A. No hay aceptacion manual
  del estado nuevo ni mediciones de memoria/cuadros.
- [Comandos, archivos y pasos manuales](12-CIERRE-FASE-4-E-INICIO-FASE-5.md).

## Resultado anterior - 3C / 4A (2026-10-11)

Validacion posterior: el usuario confirma funcionamiento correcto del APK.
XML Gradle revisados: 41 tests JVM (40 especificos y una plantilla), cero fallos,
errores u omitidos, UTC 2026-10-12T03:41:30-31. APK app y tests regenerados a las
22:41 America/Bogota; clases nuevas ArtworkCacheTest y PlaybackControlsTest
presentes en debugAndroidTest. Los 55 instrumentados nuevos compilaron, ninguno
ejecutado. Aceptacion manual general sin resultados por caso/dispositivo/API.

Los puntos siguientes registran el intento anterior del agente:

- Gradle assembleDebug/testDebugUnitTest/assembleDebugAndroidTest offline:
  bloqueado antes de tareas por Unable to establish loopback connection.
- Compilacion Kotlin 2.2.10/Compose parcial: 36 fuentes seleccionadas pasan,
  incluidas las seis pruebas Android nuevas. No genera APK ni valida Room/KSP.
- JUnit directo: 40 pasan, sin fallos. 11 identidad, 12 AudioFX, 5 posicion,
  4 SleepTimerState, 3 AudioFileInfo y 5 ArtworkSizing. No incluye la plantilla
  en este conteo; no son resultados de testDebugUnitTest de este nuevo bloque.
- Dos casos UI añadidos a PlaybackControlsTest: cancelar espera sin minuto
  ficticio y menu de informacion con datos desconocidos.
- Cuatro ArtworkCacheTest: tamaño/asignacion acotados, proporcion, datos invalidos
  y hits concurrentes intactos en archivo aislado. No cubren aun extraccion
  nativa, poda por volumen ni recuperacion ante fallo de disco.
- Total acumulado: 55 instrumentados nuevos escritos, sin ejecucion registrada.
  Los 49 anteriores compilaron con Gradle en 3B; los seis nuevos solo por K2 parcial.
- Logs temporales: build/phase3c-phase4-check/compile-result.txt y junit-result.txt.

APK nuevo, pasos manuales y archivos: [09-CIERRE-FASE-3.md](09-CIERRE-FASE-3.md).
Mediciones pendientes y plan minimo: [fase 4](10-FASE-4-RENDIMIENTO-Y-ARQUITECTURA.md).
Matriz visual futura: [fase 5](11-FASE-5-DISENO-INTEGRAL.md).
El APK actual de outputs ya contiene 3C/4A tras la compilacion posterior.

Los registros posteriores de este documento describen intervenciones anteriores.

## Reglas

Una compilacion correcta no demuestra funcionamiento.

Cada fase debe probarse antes de darse por terminada.
Usar biblioteca de prueba o copia de seguridad cuando
haya operaciones que puedan alterar datos.

## Compilacion

Desde la raiz del repositorio:

.\gradlew.bat :app:assembleDebug

Registrar:
- Fecha
- Rama y commit
- Resultado de la compilacion
- Advertencias relevantes

## Persistencia

- [ ] Marcar un favorito
- [ ] Cerrar completamente la app
- [ ] Abrirla y comprobar el favorito
- [ ] Desmarcar y repetir prueba
- [ ] Crear playlist con canciones
- [ ] Renombrar y verificar createdAt
- [ ] Eliminar playlist sin borrar canciones
- [ ] Verificar ausencia de relaciones huerfanas

## Biblioteca

- [ ] Agregar carpeta SAF
- [ ] Escanear dos veces
- [ ] Verificar ausencia de duplicados
- [ ] Comprobar estabilidad de IDs
- [ ] Verificar favoritos despues del escaneo
- [ ] Verificar canciones de playlists
- [ ] Simular carpeta vacia
- [ ] Simular permiso revocado
- [ ] Eliminar una carpeta
- [ ] Verificar que otras carpetas no se alteren

## Reproductor

- [ ] Reproducir, pausar, adelantar y retroceder
- [ ] Cambiar canciones
- [ ] Reproducir en segundo plano
- [ ] Probar notificaciones y pantalla bloqueada
- [ ] Desconectar auriculares
- [ ] Probar interrupciones de audio
- [ ] Probar formatos compatibles

## Rendimiento

- [ ] Biblioteca pequena
- [ ] Biblioteca grande
- [ ] Revisar consumo de memoria
- [ ] Revisar tiempos de escaneo
- [ ] Revisar fluidez de navegacion
- [ ] Revisar rendimiento de caratulas

## Criterios de cierre de una fase

La fase 1 tiene 16 pruebas instrumentadas nuevas escritas pero no ejecutadas.
Hay compilacion correcta y prueba manual general reportadas para fase 1A.
La proteccion posterior de apertura de Room tambien compilo desde la terminal
del usuario; su comprobacion manual especifica sigue pendiente.
No marcar una fase como validada por la sola presencia de tests
o por una compilacion anterior.

- Compilacion correcta
- Pruebas principales satisfactorias
- Sin regresiones criticas conocidas
- Documentacion actualizada
- Commit realizado
- Push confirmado a GitHub

El usuario ha aplazado la instrumentacion. Esa limitacion debe quedar visible
en cualquier cierre y no equivale a tests aprobados. Compilacion y prueba
manual de fase 1A no validan automaticamente el ajuste posterior de Room.

## Fase 1A - Suite automatizada y validacion pendiente (2026-10-10)

Archivos en `app/src/androidTest/java/com/example/comarleyaetheraudio/`:

- `data/PlaylistPersistenceTest.kt`: 10 pruebas de favoritos persistentes
  al cerrar/reabrir Room, emisiones Flow, renombrado sin cambiar createdAt,
  eliminacion sin afectar canciones/favoritos/otras listas, adicion individual
  repetida, seleccion completa/vacia, rollback y concurrencia.
- `presentation/LibrarySongOptionsTest.kt`: 2 recorridos Compose con dos
  canciones de igual titulo y distintos IDs; comprobar que favoritos y
  adicion afectan la cancion elegida y conservan relaciones existentes.
- `testutil/RoomTestLibrary.kt`: base en disco con nombre aleatorio exclusivo
  de tests, cierre y reapertura reales. Nunca abre `soundflow_database`.
- `ExampleInstrumentedTest.kt`: paquete esperado corregido al applicationId.

Los fallos SQL se inyectan mediante triggers exclusivamente en la base de
prueba. Las comprobaciones posteriores a la reapertura permiten detectar
confirmaciones parciales. La prueba concurrente admite cualquiera de las
dos selecciones completas como resultado final; no una mezcla de ambas.
La seleccion completa sigue teniendo semantica de sustitucion: la ultima
operacion confirmada prevalece, no combina selecciones de varias pantallas.

Compilar y ejecutar pruebas locales (sin descargar dependencias):

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest --offline --console=plain
```

Con dispositivo/emulador de prueba conectado, ejecutar la suite instrumentada:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest --offline --console=plain
```

Si faltan dependencias en cache, registrar el error antes de decidir su descarga.
La compilacion de un APK de tests no equivale a ejecutar esos tests.

Resultado del intento de 2026-10-10: bloqueo antes de compilar. Tras el error
de acceso a la cache, el intento con permisos fallo al conectar con Gradle:
`Unable to establish loopback connection`. Stacktrace:
`java.net.SocketException: Invalid argument: connect`, desde
`UnixDomainSockets.connect` / `PipeImpl` / `WEPollSelectorImpl`.
En ese intento no se validaron app, tests unitarios, APK de tests ni instrumentacion.

### Validacion posterior reportada por el usuario - 2026-10-11

- Comando :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest
  con --offline --console=plain: BUILD SUCCESSFUL desde su terminal.
- App y APK de pruebas compilados. Tarea unitaria satisfactoria;
  no se han inspeccionado aqui los informes individuales.
- APK local: app/build/outputs/apk/debug/app-debug.apk.
- Intento :app:connectedDebugAndroidTest en modo offline: fallo al resolver
  com.android.tools.utp:android-test-plugin-host-additional-test-output:32.3.1,
  ausente en cache; no ejecuto las 12 pruebas nuevas.
- El usuario aplaza la instrumentacion y prefiere probar el APK manualmente.
- Reporte manual general: APK probado, funcionamiento correcto. Dispositivo,
  API Android y resultados por caso no especificados. La integridad interna,
  concurrencia, rollback y compatibilidad historica no quedan demostrados.

### Recorrido manual de aceptacion de fase 1A

Usar una biblioteca de prueba; no reinstalar ni borrar datos para comprobar
persistencia. Esta intervencion no corrige duplicados al importar carpetas.

1. Preparar canciones A, B y C, incluyendo dos con el mismo titulo y
   distinto artista. Crear playlist P con A y B y otra playlist Q.
2. Abrir Opciones de C desde Biblioteca > Canciones. Marcarla favorita;
   comprobar que cambia C y no la otra cancion de igual titulo. Forzar cierre
   de la app, reabrir y comprobar; desmarcar y repetir.
3. Desde Opciones de C, elegir Anadir a una lista > P. Comprobar A, B y C.
   Repetir: C debe aparecer una sola vez y Q debe conservar su contenido.
4. En el selector completo de P, desmarcar A y guardar: deben quedar B y C.
   Guardar seleccion vacia: P permanece y ninguna cancion se borra de la biblioteca.
5. Renombrar P; comprobar mismo ID, createdAt y relaciones en Database Inspector.
6. Eliminar P; comprobar ausencia de su fila y sus relaciones, conservando
   canciones, favoritos y Q. Cerrar y reabrir para verificar persistencia.
7. Revisar consultas de integridad en Database Inspector. Registrar cualquier
   huerfano preexistente por retirada de carpetas; no atribuir su limpieza a 1A.

```sql
SELECT f.songId FROM favorite_songs f
LEFT JOIN songs s ON s.id = f.songId WHERE s.id IS NULL;

SELECT r.playlistId, r.songId FROM playlist_song_cross_ref r
LEFT JOIN playlists p ON p.id = r.playlistId
LEFT JOIN songs s ON s.id = r.songId
WHERE p.id IS NULL OR s.id IS NULL;
```

El usuario reporto el 2026-10-11 que probo el APK y funciona correctamente.
No se dispone de resultados desglosados de este recorrido ni de verificaciones
con Database Inspector: no marcar cada caso como ejecutado por ese reporte.
Registrar dispositivo, API Android, revision de trabajo, resultado esperado
y observado antes del cierre.

## Ajuste final de fase 1 - Compatibilidad de apertura (2026-10-11)

DatabaseCompatibilityTest agrega cuatro casos instrumentados usando el mismo
constructor de MusicDatabase que produccion, con nombre aleatorio aislado:

- Reabrir esquema actual conservando canciones, carpetas, favoritos,
  playlists (incluido createdAt) y relaciones.
- Rechazar version sintetica 1 sin migracion y comprobar las cinco tablas.
- Rechazar version sintetica 3 sin migracion y comprobar las cinco tablas.
- Rechazar hash desconocido en version 2 sin sustituir filas ni identidad.

Los casos sinteticos verifican politica de rechazo, no migraciones reales.
RoomTestLibrary tambien usa ahora el constructor de produccion. Total:
16 tests nuevos instrumentados; no ejecutados. El informe unitario existente
se reviso: un test de plantilla addition_isCorrect, sin errores ni fallos.

Compilacion intentada con assembleDebug, testDebugUnitTest y
assembleDebugAndroidTest, offline y sin configuration cache: fallo de loopback
antes de ejecutar tareas. Reintento con preferIPv4Stack solo en el proceso,
--no-daemon y --stacktrace: mismo SocketException: Invalid argument: connect
en UnixDomainSockets/PipeImpl. No se cambiaron ajustes globales.

Pendiente desde la terminal local del usuario:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest --offline --console=plain --no-configuration-cache
```

Tras BUILD SUCCESSFUL, instalar el APK nuevo como actualizacion sin borrar
datos. Comprobar favoritos y playlists tras forzar cierre y reabrir.
El APK existente de 2026-10-10 21:45 (America/Bogota) es anterior al ajuste.
No afirmar que se genero un APK actualizado ni reutilizar su prueba manual.

Fase 2 sin iniciar. Politica detallada en 06-POLITICA-DE-PERSISTENCIA.md.

La anotacion anterior describe el estado antes de la autorizacion posterior.
La fase 1 se publico como entrega de desarrollo en 7c19fd4; sus limitaciones
de validacion permanecen registradas. Fase 2 iniciada segun el bloque siguiente.

### Resultado posterior y entrega autorizada de fase 1

El usuario confirma BUILD SUCCESSFUL in 1m 14s, 77 tareas (15 ejecutadas,
62 actualizadas) para el comando de compilacion indicado. El APK local se
regenero el 2026-10-11 a las 19:11 America/Bogota. Se inspecciono el XML de
testDebugUnitTest: 1 test addition_isCorrect, cero fallos, errores u omitidos.
Las 16 pruebas instrumentadas siguen sin ejecutar; el exito de compilacion
de androidTest no cambia ese estado. La comprobacion manual del ajuste final
sigue pendiente. El usuario autoriza entregar/publicar el desarrollo de fase 1
y avanzar a fase 2 con estas limitaciones explicitas.

## Fase 2 - Primer bloque de biblioteca y carpetas

FolderIntegrityTest: ocho pruebas instrumentadas con Room aislado:

- Reimportacion conserva IDs, etiquetas editadas, favorito y playlist.
- Nueva URI repetida se inserta una sola vez e ignora IDs del escaner.
- Escaneos vacios y parciales conservan canciones y conteos previos.
- Importaciones concurrentes no duplican la misma URI nueva.
- Fallo SQL al registrar carpeta revierte canciones insertadas antes.
- Retirada limpia solo sus canciones, favoritos y relaciones; conserva listas
  y datos de otra carpeta.
- Fallo SQL al retirar restaura canciones, favoritos y relaciones.
- Solapamiento exacto entre carpetas se rechaza sin mover pertenencia.

FolderActionsTest: tres pruebas Compose para confirmar/cancelar retirada,
actualizar la URI elegida y mostrar conteo, y bloquear acciones con progreso.
Las once pruebas nuevas ya compilaron; no se han ejecutado. Total instrumentado
nuevo entre ambas fases: 27, compilados pero ninguno ejecutado.
El intento del agente fallo por loopback. Posteriormente el usuario confirma
BUILD SUCCESSFUL in 42s, 77 tareas: 15 ejecutadas, 62 actualizadas.
APK de fase 2 regenerado el 2026-10-11 a las 19:28 America/Bogota.
XML unitario inspeccionado: 1 test addition_isCorrect, sin errores ni fallos.
El usuario reporta posteriormente funcionamiento correcto del APK. Se registra
aceptacion manual general de este bloque, sin dispositivo/API ni desglose por
caso. No implica ejecucion de instrumentacion ni validacion de toda la fase 2.

Instalar el APK nuevo como actualizacion y completar estos recorridos:

1. Preparar dos carpetas no superpuestas. Anadir A con dos canciones;
   marcar una favorita y agregarla a una playlist. Editar su titulo en la app.
2. Actualizar A dos veces: mismo conteo, titulo editado, favorito y playlist.
   Anadir un archivo nuevo a A y actualizar: solo una cancion nueva.
3. Retirar temporalmente acceso o almacenamiento y actualizar. Debe mostrarse
   error y conservarse la biblioteca anterior. Restaurar permiso seleccionando A.
4. Cancelar la confirmacion de retirada: nada cambia. Confirmar retirada de A:
   desaparecen sus canciones y referencias; listas, B y archivos originales
   siguen presentes. No usar datos valiosos para esta prueba.
5. Verificar progreso, botones deshabilitados y mensajes en pantalla pequena,
   tema claro/oscuro y tamanos de fuente grandes. Inspeccion visual pendiente.

No confundir ausencia de borrado con escaneo completo: siguen pendientes
alias de URI, solapamientos generales, permisos con proveedores reales,
cancelacion durante lectura/escritura y mediciones con bibliotecas grandes.

## Fase 2 - Segundo bloque: identidad SAF (2026-10-11)

Primer bloque publicado en ce6f621; este segundo bloque permanece local.
SongIdentityTest agrega diez pruebas JVM: alias entre arboles, documento
directo/arbol, proveedores distintos, IDs opacos y sensibles a mayusculas,
signo + literal, decodificacion unica, UTF-8, MediaStore/otros esquemas,
consultas/fragmentos/formatos desconocidos y valores invalidos.

Resultado ejecutado: OK (10 tests). Se compilaron SongIdentity.kt y su test
con K2JVMCompiler 2.2.10 y se ejecuto JUnitCore 4.13.2 usando JAR binarios de la
cache existente, sin descargas ni cambios de entorno. Registro local ignorado
por Git: build/phase2-identity-check/junit-result.txt. Esto verifica la funcion
pura; no compila ni valida Room, el escaner Android o toda la aplicacion.

FolderIntegrityTest incorpora cuatro casos instrumentados:

- Alias dentro de la misma carpeta conserva ID, URI, etiquetas y relaciones.
- Alias en otra carpeta rechaza toda la importacion, incluidas canciones nuevas.
- Dos alias en un escaneo insertan una sola cancion.
- Permiso persistente denegado conserva canciones, carpetas, favorito y playlist.

Estos cuatro casos no se compilaron ni ejecutaron. Total nuevo instrumentado:
31 entre ambas fases; 27 compilados anteriormente, ninguno ejecutado.
La denegacion usa una URI sin concesion del sistema: no simula revocacion real
ni un fallo a mitad del escaneo.

Intento Gradle actual con las tres tareas y --offline --console=plain
--no-configuration-cache: Unable to establish loopback connection antes de
compilar. El APK anterior corresponde al primer bloque, no a estos cambios.

Validacion pendiente desde la terminal del usuario:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest --offline --console=plain --no-configuration-cache
```

Instalar como actualizacion sin borrar datos y comprobar:

1. Importar una subcarpeta con musica, marcar favorito y agregar a playlist.
   Seleccionar luego su carpeta padre con el mismo proveedor: debe rechazar
   el solapamiento completo, mantener conteo y conservar favorito/playlist.
2. Actualizar la carpeta original dos veces: sin duplicados nuevos ni cambios
   en etiquetas editadas. Reiniciar app y volver a comprobar referencias.
3. Si el proveedor permite retirar acceso o desconectar almacenamiento,
   actualizar: debe informar error y conservar datos. Restaurar acceso al
   arbol original y actualizar. No desinstalar ni borrar datos para esta prueba.

Pendientes proveedores reales, revocacion durante escaneo y rendimiento.
Conservar la URI anterior no recupera automaticamente una concesion perdida.

### Validacion posterior del segundo bloque

El usuario confirma que compila y funciona correctamente. Se inspecciona XML
Gradle de SongIdentityTest con fecha UTC 2026-10-12T01:08:55: 10 tests, cero
fallos, errores u omitidos. APK app y APK androidTest regenerados el 2026-10-11
a las 20:08 America/Bogota. Los 31 tests instrumentados nuevos estan compilados,
sin ejecutar. El bloqueo de loopback anterior corresponde al entorno del agente.
Se registra aceptacion manual general; no se inventan resultados por recorrido,
proveedor, dispositivo o API. Permisos revocados y biblioteca grande requieren
registro especifico. El segundo bloque sigue local, sin commit ni push.

## Ajuste final de fase 2 - Recuperacion y catalogo grande

SongIdentityTest agrega un caso de 10.000 documentos con dos arboles: once tests
pasan ejecutados con Kotlin/JUnit locales, fuera de Gradle. Registro en
build/phase2-identity-check/junit-result.txt. No es una medicion de rendimiento Android.

Nuevos instrumentados, sin compilar ni ejecutar:
- Room: restaurar otra carpeta se rechaza conservando datos y referencias.
- Room: duplicados historicos mantienen ambos IDs, favoritos y relaciones.
- Room: importar/reimportar 10.000 canciones conserva IDs, conteo y referencias.
- Compose: accion de recuperacion identifica carpeta y entrega URI original.
- Compose: recuperacion deshabilitada mientras se actualiza.

Total nuevo entre ambas fases: 36 instrumentados, ninguno ejecutado. Los 31
anteriores compilaron antes de este ajuste. Compilacion actual del agente con
las tres tareas, offline y sin configuration cache: fallo de loopback antes
de tareas. Usuario solicitado ejecutar el mismo comando en su terminal.

Manual sobre el APK final, como actualizacion sin borrar datos:
1. Actualizar carpeta con favoritos, playlist y etiquetas editadas: conservarlos
   y comprobar ausencia de nuevos duplicados tras dos actualizaciones.
2. Retirar acceso en un proveedor que lo permita o desconectar almacenamiento,
   actualizar y comprobar error con nombre de carpeta y Restaurar acceso.
3. Cancelar selector: no cambia biblioteca. Elegir otra carpeta: mostrar rechazo
   y conservar relaciones. Elegir original y conceder lectura: restaurar acceso,
   desaparecer aviso y conservar favoritos/playlist, tambien despues de reiniciar.
4. Catalogo real grande: registrar canciones, dispositivo/API, proveedor, tiempo
   de importacion inicial y actualizacion, respuesta UI y memoria si se dispone.
   No inventar cifras ni umbrales de dispositivos que no se han medido.
5. Confirmar/cancelar retirada y comprobar datos de otra carpeta y archivos
   originales. Comprobar avisos con fuente grande y TalkBack.

Si el proveedor cambia la URI del arbol, la recuperacion conservadora puede
rechazarla aunque el nombre coincida; no remapear automaticamente. No se
simulo revocacion a mitad de lectura ni se probo visualmente el selector nativo.

Pruebas iniciales de fase 3 en 07-FASE-3-MOTOR-DE-AUDIO.md.

### Aceptacion final de desarrollo de fase 2

Usuario confirma BUILD SUCCESSFUL in 16s, 77 tareas (15 ejecutadas,
62 actualizadas), y funcionamiento correcto del APK. XML SongIdentityTest:
11 pruebas, cero fallos, errores u omitidos, UTC 2026-10-12T01:50:23.
APK app regenerado a las 20:50:19 America/Bogota y APK de tests a las 20:50:24.
Las 36 pruebas instrumentadas nuevas compilaron, ninguna ejecutada. Aceptacion
manual general, sin atribuir casos, tiempos, proveedor, dispositivo o API.
Desarrollo de fase 2 aceptado; mediciones Android y revocacion durante lectura
quedan registradas para regresion. Usuario autoriza comenzar fase 3 y diseno.

## Fase 3 - Primer bloque de motor y diseno (local)

Resultados reales:
- Compilacion completa Gradle intentada, fallo antes de tareas por
  java.io.IOException: Unable to establish loopback connection.
- Compilacion parcial directa con Kotlin 2.2.10/plugin Compose y cache local:
  ocho archivos productivos y tres de pruebas pasan. Usa clases existentes
  del resto de la app; no prueba KSP, recursos ni empaquetado de APK.
  Registro: build/phase3-check/compile-result.txt.
- Compilacion de fuentes puras actuales y ejecucion JUnit directa:
  OK (16 tests), cero fallos. Once SongIdentityTest y cinco PlaybackPositionTest:
  duracion desconocida, posicion fuera de limites, seek sin duracion, saltos
  de ambos extremos y offsets Long extremos sin overflow. Registro:
  build/phase3-unit-check/test-result.txt. No es ejecucion mediante Gradle.
- Cuatro Compose en PlaybackControlsTest: conexion deshabilita controles,
  pausa/siguiente no expanden panel, reintento solo explicito y buffering visible.
- Uno en PlayerConnectionTest: conexion real del cliente y release idempotente,
  sin solicitar reproduccion ni sustituir cola. Ambos archivos comprobados con
  K2, sin APK androidTest nuevo ni ejecucion en dispositivo.
- Total nuevo instrumentado: 41 escritos entre fases 1-3. Los 36 de fases
  anteriores compilaron con Gradle; los cinco nuevos solo comprobacion parcial.
  Ninguno ejecutado. No hay aun aceptacion manual del APK de fase 3.

Compilacion pendiente desde PowerShell en C:\SoundFlow-Git:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest --offline --console=plain --no-configuration-cache
```

APK resultante: app/build/outputs/apk/debug/app-debug.apk. El APK actual y
el respaldo local corresponden a fase 2; copiar codigo no regenera el APK.
Instalar como actualizacion sin borrar datos y probar:

1. Elegir una pista inmediatamente al abrir; comprobar que se reproduce al
   conectar. Girar, bloquear pantalla y volver: pista/cola y controles coherentes.
2. Pausar/siguiente en mini no abre panel; tocar titulo si. Comprobar ambos
   modos de tema, texto grande, paisaje, scroll normal y TalkBack.
3. Avanzar/retroceder diez segundos junto al inicio/final; slider sin negativos
   y botones coherentes. Saltar rapidamente y pausar: volumen restaurado.
4. Desconectar cable/Bluetooth y recibir audio de otra app: registrar dispositivo,
   API y comportamiento de pausa/recuperacion; no afirmar compatibilidad universal.
5. Pista inaccesible: mensaje, sin reintentos continuos. Restaurar acceso original
   en Carpetas y pulsar Reintentar; elegir otra pista debe permitir continuar.
6. Temporizador normal de un minuto, cancelar, y opcion al final de pista:
   respeta plazo antes de esperar final. Cancelar esa espera no pausa despues.
   Mientras espera muestra 1; estado especifico pendiente. Se cancela al cerrar
   definitivamente el ViewModel, pues aun no pertenece al servicio.
7. Comprobar favoritos, playlists, carpetas y etiquetas como regresion.

Pendientes: instrumentacion, pruebas de foco/servicio/errores/temporizador reales,
AudioFX y mediciones de audio; no cubiertos por los 16 tests JVM ni por K2.

### Validacion posterior del primer bloque de fase 3

Usuario confirma compilacion y funcionamiento correcto del APK. XML Gradle
inspeccionados: 17 tests, cero fallos, errores u omitidos (11 identidad,
5 posicion, 1 plantilla); UTC 2026-10-12T02:22:36-37. APK app y androidTest
regenerados a las 21:22 America/Bogota del 2026-10-11. Los 41 instrumentados
nuevos estan compilados, sin ejecucion registrada. La limitacion K2 y el bloqueo
de loopback anteriores corresponden al intento del agente, superado mediante
la terminal del usuario. El APK actual ya incluye el primer bloque de fase 3;
el respaldo local sigue siendo fase 2. Aceptacion manual general, sin inventar
resultados individuales ni dispositivo/API/proveedor.

Siguiente bloque propuesto: AudioFX real y panel renovado. Validar soporte/no
soporte, activacion voluntaria, cambio audible/controlado, reset neutro,
rotacion, segundo plano, nueva sesion y error sin caida. No activar ganancias
por defecto ni presentar sliders locales como efectos aplicados.

## Fase 3B - Efectos y panel de audio

- JUnit directo: 28 especificas pasan, 12 de AudioEffectsEngineTest y 16 de
  regresion identidad/posicion. build/phase3b-check/junit-result.txt.
- Verifican soporte parcial/sesion ausente, neutro/apagado por defecto,
  limites/lectura redondeada, reset, cambio de sesion, fallo al activar/escribir,
  perdida de control, reintento y liberacion idempotente/parcial fallida.
- Comprobacion K2/Compose parcial pasa para produccion y tests afectados.
  No sustituye build completo, empaquetado, DSP Android ni revision visual.
- AudioFxPanelTest agrega seis casos: sesion ausente, activacion confirmada,
  envio de banda/soporte parcial, reset explicito, controles bloqueados al
  aplicar y reintento ante error anterior a obtener sesion.
- AudioEffectsProtocolTest agrega dos Bundle: ida/vuelta y arrays incoherentes.
  PlayerConnectionTest existente comprueba ademas estado inicial AudioFX.
- Ocho instrumentados nuevos, total 49 escritos sin ejecucion. Los 41 previos
  compilaron en fase 3A; su test de conexion fue ampliado en este bloque.
  Ningun APK de tests actualizado aqui: solo compilacion parcial de Kotlin.
- Gradle falla antes de tareas: java.io.IOException: Unable to establish
  loopback connection. APK actual sigue siendo el de fase 3A aceptado.

Compilar desde PowerShell con el comando de las tres tareas anterior; instalar
actualizacion sin borrar datos. Probar activacion voluntaria, cambio audible,
desactivar/reset, disponibilidad parcial, cambio de salida/sesion, rotacion,
segundo plano, errores y panel claro/oscuro/fuente grande/TalkBack.
Pasos y archivos en [08-FASE-3B-EFECTOS-Y-DISENO.md](08-FASE-3B-EFECTOS-Y-DISENO.md).
No afirmar salida bit-perfect, DSP audible ni soporte universal por pasar JVM.

### Validacion posterior de fase 3B

Usuario confirma funcionamiento correcto del APK. XML Gradle verificados:
29 tests (12 AudioFX, 11 identidad, 5 posicion y 1 plantilla), cero fallos,
errores u omitidos, UTC 2026-10-12T03:08:28. APK app y tests regenerados a
las 22:08 America/Bogota del 2026-10-11; los 49 instrumentados nuevos estan
compilados, sin ejecucion registrada. El APK actual incluye fase 3B.
Aceptacion manual general sin resultados por efecto/dispositivo/caso; no
deducir audibilidad universal, foco, rutas Bluetooth ni accesibilidad completa.
La validacion parcial y bloqueo anteriores describen el intento del agente.

Propuesta 3C: comprobar informacion con metadatos presentes/ausentes y archivos
lossless comunes, sin prometer salida medida; temporizador antes del plazo,
espera al final y cancelacion; regresion de pausa/saltos, bloqueo/notificacion,
auriculares/foco, favoritos/playlists y panel. Documentar limites de gapless
y atenuacion manual. No iniciar cambios de 3C en este registro de aceptacion.

### Orden visible y pulido — validación final aplazada (2026-10-12)

Ver docs/18 para lista única de comprobaciones finales y archivos. Añadidos
LibraryOrderTest (5), PlaybackRequestTest (6), ArtistIdentityTest (5). Ampliadas
pruebas de persistencia de Apariencia para conservar el orden tras reapertura y
reset visual. Suite completa directa: 114 tests JVM aprobados. Kotlin directo
comprueba fuentes main/test/androidTest con artefactos generados locales; no
sustituye compilación Gradle, empaquetado, Lint ni ejecución Android.
Gradle falla antes de tareas por Unable to establish loopback connection.
Ejecutar tools/Validate-SoundFlow.ps1 en PowerShell y probar APK solo al final,
según instrucción del usuario. No se ejecutó instrumentación en este bloque.

### Verificación 4.0.0-rc.1

Base anterior validada por PowerShell: 114 JVM y Lint 0 errores/81 advertencias.
Candidato: 130 JVM directos y comprobación Kotlin; nuevo Gradle/APK/Lint pendientes
por loopback en agente. ListeningTrackerTest, ListeningPreferencesTest y
ReleaseCandidateTest cubren políticas, persistencia/aislamiento y actualización RC.
Manual único final en docs/19, incluido fondo, historial en segundo plano,
carátulas de notificación y regresión de orden/favoritos/playlists.
