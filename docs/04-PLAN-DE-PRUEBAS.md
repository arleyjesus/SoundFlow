# Plan de pruebas

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
