# Fase 3C: ajuste final y revisión de cierre

Fecha: 2026-10-11. Rama: refactor/soundflow-4. Trabajo local autorizado.

## Objetivo y estado real

Terminar el alcance de desarrollo del motor de reproducción con información
honesta del archivo y un temporizador cuyo estado sea comprensible. El usuario
autorizó continuar con fase 4 y reservar el rediseño integral para fase 5.

3A y 3B tienen compilación Gradle y aceptación manual general registradas.
3C/4A tienen ahora compilación Gradle y aceptación manual general del APK
confirmada por el usuario. XML revisados: 41 JVM sin fallos, errores ni omitidos,
UTC 2026-10-12T03:41:30-31. APK app y tests regenerados a las 22:41 del 2026-10-11
America/Bogota, después de los últimos cambios Kotlin.
El desarrollo de fase 3 queda aceptado con esas limitaciones. No es regresión
completa, instrumentación ejecutada, medición de memoria ni publicación.
Fase 4 continúa con el bloque 4B; fase 5 sigue planificada.

Respaldo de fuentes y APK aceptados: `local-backups/fase3C-fase4A-aceptada-20261011-2241/`.
Incluye informes XML Gradle y hashes SHA-256. No sustituye los respaldos históricos.

Respaldo previo al cambio: `local-backups/fase3B-validada-20261011-2208/`.
Contiene fuentes, APK de 3B aceptado y manifest.txt con hashes SHA-256. La carpeta
está ignorada por Git; no es una publicación ni incluye el APK nuevo de 3C.

Checkpoint de esta intervención: `local-backups/fase3C-fase4A-pendiente-validacion-20261011/`.
Guarda fuentes y resultados parciales con hashes, sin copiar un APK antiguo como
si correspondiera al código nuevo. Es un respaldo de trabajo pendiente de validación.

## Diagnóstico y cambios de fase 3C

- El temporizador usaba un minuto ficticio al esperar el fin de pista. Ahora
  SleepTimerState distingue apagado, cuenta atrás, espera y detención. Conserva
  sleepTimerMinutes para compatibilidad; la UI observa el estado completo.
  La detención permanece visible durante la atenuación; cancelar retira solo
  la atenuación propiedad del temporizador y restaura su volumen.
- Esperar el final reconoce transición automática y repetición de pista. Cancelar
  retira el listener; el diálogo mantiene disponible la cancelación durante espera.
- Menú del reproductor: Información del archivo muestra MIME, Hz y kbps guardados.
  Si faltan datos se indica No disponible. No se inventa profundidad de bits ni
  se usa el booleano histórico isHiRes como certificación de salida.
- No se cambia el modelo Song, sus entidades ni los valores almacenados. Una
  clasificación histórica errónea no queda reparada por mostrar este diálogo.

## Límites de reproducción revisados

- El cambio manual atenúa volumen durante 300 ms. Hay un ExoPlayer; no se mezclan
  dos pistas. El setter histórico de crossfade no representa una función activa.
- No se promete gapless entre cualquier combinación de archivos, códecs y rutas.
  Hay que comprobar transiciones con archivos reales y el dispositivo de destino.
- La información del archivo no mide salida Android, DAC, remuestreo ni bit-perfect.
- Temporizador y atenuaciones siguen en el cliente del ViewModel: sobreviven a
  rotación, pero no se garantiza supervivencia al cierre de proceso/propietario.
  No se introduce servicio extra ni cambio de protocolo en este ajuste.
- AudioFX conserva soporte dependiente del dispositivo y reinicio neutro/apagado
  al cambiar la sesión. No hay perfiles persistentes nuevos.

## Validación inicial del agente (histórica; completada después con Gradle)

- Gradle offline con assembleDebug, testDebugUnitTest y assembleDebugAndroidTest:
  falló antes de tareas por `java.io.IOException: Unable to establish loopback connection`.
- Compilador Kotlin 2.2.10/Compose local: 36 archivos seleccionados de producción
  y pruebas compilados, sin errores. Comprobación parcial con dependencias locales
  y clases previas: no sustituye Gradle/KSP, empaquetado ni ejecución Android.
- JUnit directo: **40 tests específicos pasan**, 28 anteriores y 12 nuevos.
  Los nuevos son cuatro de temporizador, tres de metadatos y cinco de muestreo.
- Se añaden seis instrumentados: dos de UI y cuatro de imágenes/caché. Sus fuentes
  están incluidas en la comprobación parcial; **no se ejecutaron**. Total acumulado:
  55 instrumentados nuevos escritos. Los 49 anteriores compilaron en el APK de 3B.
- Evidencia local temporal: `build/phase3c-phase4-check/compile-result.txt` y
  `build/phase3c-phase4-check/junit-result.txt`. Estos archivos se pierden con clean.
- En aquel intento el APK seguía siendo 3B. La compilación posterior de las 22:41
  lo sustituyó por 3C/4A; el bloqueo describe el entorno del agente, no ese APK nuevo.

## Archivos de esta intervención

Rutas Kotlin bajo `app/src/main/java/com/example/comarleyaetheraudio/`:

| Archivo | Motivo |
|---|---|
| MainActivity.kt | Observar y pasar el estado completo del temporizador |
| data/player/AudioPlayerHandler.kt | Estado de temporizador y reconocimiento de repetición |
| domain/playback/SleepTimerState.kt (nuevo) | Estados independientes de Android |
| domain/playback/AudioFileInfo.kt (nuevo) | Representación fiel de metadatos disponibles |
| presentation/player/FullPlayerSheet.kt | Información de archivo, temporizador, decodificador común y letras fuera de main |
| presentation/components/dialogs/SleepTimerDialog.kt | Mostrar espera/detención y permitir cancelar; usar color del tema |
| data/local/util/ArtworkSizing.kt (nuevo) | Cálculo seguro de muestreo, probado en JVM |
| data/local/util/ArtworkDecoder.kt (nuevo) | Leer dimensiones antes de decodificar |
| data/local/util/CoverCacheManager.kt | Caché de archivos, límite de extracción y escritura atómica |
| data/local/util/ArtworkLoader.kt | Protección del cargador antiguo sin consumidores: muestreo, techo RAM y cancelación |
| data/remote/LrcLibService.kt | Cerrar conexiones y propagar cancelación |
| data/repository/AudioRepositoryImpl.kt | Transformaciones de listas en Default |
| presentation/library/LibraryViewModel.kt | Compartir songs con el controlador; agrupar fuera de main |
| presentation/settings/SettingsScreen.kt | Describir caché real y mostrar versión desde BuildConfig |

Pruebas bajo `app/src/` con el mismo prefijo de paquete:

- test: domain/playback/SleepTimerStateTest.kt, AudioFileInfoTest.kt y
  data/local/util/ArtworkSizingTest.kt (nuevos).
- androidTest: presentation/PlaybackControlsTest.kt (dos casos añadidos) y
  data/ArtworkCacheTest.kt (nuevo, cuatro casos).

Documentación: README, auditoría, roadmap, arquitectura, plan de pruebas,
changelog y nuevos documentos 09, 10 y 11. Conservar todos los cambios anteriores.

## Comprobación manual y cobertura específica pendiente

Desde C:\SoundFlow-Git, ejecutar:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest --offline --console=plain
```

Después instalar `app/build/outputs/apk/debug/app-debug.apk` como actualización,
sin desinstalar ni borrar datos:

1. Abrir Opciones > Información del archivo. Comprobar archivo conocido y otro
   con metadatos ausentes; ningún valor debe presentarse como salida medida.
2. Iniciar temporizador con Esperar a que termine. Al agotarse el plazo debe
   indicar espera, permitir cancelar y detener al final, también con repetición.
   Cancelado durante espera, la siguiente transición no debe detener la música.
   Sin esperar final de pista, comprobar Deteniendo durante la atenuación y
   cancelarla antes de la pausa; no debe cancelar una transición manual posterior.
3. Comprobar pausa, seek, siguiente/anterior, bloqueo/notificación, foco y retirada
   de auriculares; verificar FX apagados y restablecimiento según capacidades.
4. Abrir biblioteca, artistas, playlists y reproductor; comprobar portadas, letras
   con/sin red, rotación, favoritos y canciones de playlists conservadas.

El usuario confirmó funcionamiento correcto del APK nuevo, sin detallar resultados
por caso, dispositivo o API. Los pasos anteriores sirven para la cobertura específica
pendiente; no se registran como ejecutados individualmente. No es necesario
conectar un dispositivo para compilar; la instrumentación sigue aplazada por
preferencia del usuario. Publicación y commits requieren autorización específica.

## Revisión Git y conservación

- Rama refactor/soundflow-4, HEAD ce6f621; nada preparado, ningún commit/push nuevo.
- git diff --check: código 0. Archivos nuevos de app/src y docs revisados sin
  espacios finales. git diff --stat y git status revisados.
- El diff acumulado incluye 28 archivos tracked modificados; además hay 28 archivos
  nuevos de código/pruebas/docs, que diff --stat no cuenta mientras no están preparados.
  Incluye trabajo anterior, no son 56 archivos modificados en esta intervención.
- Comparación con ZIP previo de 3B: ningún archivo faltante, solo 17 archivos
  anteriores cambiados dentro del alcance y 11 archivos nuevos de esta intervención.
- Icono.jpg, soundflow_logo.png y los tres TXT personales siguen presentes.
- Sin cambios en MusicDatabase, entidades Room, dependencias o wrapper de Gradle.
