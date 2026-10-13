# Fases 4 y 5: cierre de desarrollo y validación externa pendiente

Fecha: 2026-10-12, America/Bogota. Trabajo local sobre refactor/soundflow-4.
Este documento registra el bloque posterior al APK aceptado de las 10:15:34.

## Decisión final del usuario — prevalece sobre los pendientes históricos

El usuario confirma haber probado el último APK satisfactoriamente y solicita
descartar del cierre las pruebas que el agente no puede ejecutar. Fases 1–5
cerradas para el alcance de desarrollo acordado, incluidas 4/5 y sus ajustes finales.
Instrumentación, perfilado de memoria/cuadros y revisión visual/accesibilidad
exhaustiva quedan excluidos por decisión explícita, no aprobados ni ejecutados.
Se conservan las pruebas escritas; no se borran datos, tests ni evidencias.
No se requieren estas verificaciones para avanzar a 6/7 ni se trasladan como
obligaciones automáticas. Compatibilidad universal y salida efectiva no certificadas.
La confirmación del APK es reporte del usuario, no una nueva ejecución Gradle
observada por el agente. El historial que sigue conserva las evidencias anteriores.

## Actualización de cierre — 2026-10-12

- Revisada la ejecución `build/validation/20261012-163921-UTC`: Gradle terminó
  correctamente en 45 s, 86 tareas. Compiló app y androidTest, ejecutó 88 JVM
  sin fallos, errores ni omitidos y Lint informó 0 errores, 82 advertencias y
  3 sugerencias. Ambos informes de dependencias terminaron correctamente.
- El usuario confirma instalación/funcionamiento y aporta captura de Inspector.
  Es aceptación general; no acredita toda la matriz visual ni benchmarks.
  El informe guardó SHA-256 CBD9FEE3…, pero el APK local se escribió después
  (16:40:32 UTC frente al manifiesto de hashes de 16:40:12 UTC). Su SHA-256
  actual es `3AF7698A6365CA4AF7E3FBDC3C9AC4C5F91417EA234E108B8A46A4DB07AA0390`.
  No se afirma identidad binaria con el APK instalado ni se sobrescribe el
  manifiesto histórico. El APK de tests sí coincide con el manifiesto.
- Se corrigió el fallo Lint anterior: SessionError para códigos del servicio,
  opt-in explícito AndroidX a UnstableApi y escape del SDK en local.properties
  ignorado. Misma ruta SDK, sin cambios globales, baseline ni desactivar Lint.
- Revisión final de código: visualizador retira el bucle decorativo cuando su
  Lifecycle no está RESUMED y libera el observador al salir; cabecera del
  reproductor reserva espacio flexible al título; tiempo usa Locale.ROOT.
  Estos ajustes son posteriores al APK aceptado y requieren APK regenerado.
- El intento Gradle de estos últimos ajustes vuelve a fallar antes de tareas por
  `java.io.IOException: Unable to establish loopback connection`; registro
  `build/phase5-design-check/closure-gradle.txt`. No invalida el build anterior.
- Comprobación directa posterior: 131 fuentes Kotlin/Compose correctas y
  JUnit `OK (88 tests)`, cero fallos; registros closure-kotlin.txt y
  closure-junit-result.txt en build/phase5-design-check. Usa recursos/clases
  generados previamente: no sustituye KSP, Lint, empaquetado o pruebas Android.
- ADB ejecutado fuera del sandbox devuelve lista vacía. No hay dispositivo ni
  emulador disponible: instrumentación, meminfo/gfxinfo y TalkBack sin ejecutar.

## Dependencias resueltas y advertencias revisadas

Fuente: debugRuntimeClasspath/debugCompileClasspath del informe anterior;
sin `FAILED` ni `Could not resolve`. Resolver no prueba compatibilidad completa.

| Componente | Resolución observada / decisión |
|---|---|
| Kotlin / coroutines | stdlib 2.2.10 / core-jvm 1.9.0 |
| Compose | animation/core 1.12.0, UI/foundation 1.10.0, Material3 1.3.1; la versión explícita de animation desborda el BOM 2024.12.01 |
| Core / annotations | core-ktx 1.12.0 y annotation-jvm 1.7.0 forzados por debajo de peticiones transitivas; riesgo de APIs ausentes, sin fallo reproducido |
| Lifecycle | runtime-android 2.9.4 pese a declaraciones 2.8.7 |
| Room / Media3 | 2.7.0-alpha13 / 1.5.1; Room preliminar y migraciones históricas requieren cautela |
| Herramientas | AGP 9.3.1, Kotlin 2.2.10 y KSP 2.1.0-1.0.29 declarados; KSP compila en el build aceptado, sin certificar todas las combinaciones |

Se completa la revisión de dependencias. No se actualiza ni fuerza ninguna versión:
alinear BOM y eliminar downgrades necesita un bloque aprobado y regresión Android.
No se deduce una vulnerabilidad de seguridad del simple aviso de versión nueva.

Clasificación de los 82 avisos del APK aceptado (3 sugerencias aparte):

- 29 de versiones: GradleDependency 25, AGP 1, NewerVersionAvailable 2 y target 1.
- 27 de catálogo, 5 KTX y 3 contrato Modifier: mantenimiento, sin fallo demostrado.
- 9 recursos sin uso y 5 iconos duplicados: limpieza futura; no se borran recursos.
- 2 InlinedApi en scanners: revisar metadatos en Android anterior a 31; no prueba
  un crash por sí solo. No se cambia escaneo en este cierre visual.
- 1 ExportedService: el servicio Media3 admite controladores del sistema; no se
  vuelve privado ni se añade un permiso incompatible para ocultar el aviso.
  Mantener revisión de comandos/identidad y controladores externos en fase 7.
- 1 DefaultLocale corregido en las fuentes de este cierre; Lint actualizado pendiente.

## Revisión visual del código activo

Se trazan MainActivity/AppNavigation → Inicio, Biblioteca, Buscar, Perfil,
Ajustes/Apariencia/AudioFX y sus detalles, menús y diálogos. El recorrido activo
usa ColorScheme y paneles comunes; fondos dinámicos son opcionales, portadas
no definen el color de controles y los crossfades propios respetan motionEnabled.
MainActivity espera la primera preferencia persistida y actualiza barras del sistema.
MiniPlayer.kt, PlayerScreen.kt y MusicComponents.kt conservan implementación
heredada sin llamadas encontradas en producción; no se eliminan ni se usan como
evidencia de cobertura del recorrido activo. Las animaciones internas de Material
y del sistema no quedan universalmente interceptadas por el interruptor propio.

La revisión estática y los JVM de contraste/opacidad no sustituyen capturas de
todas las pantallas, fuente grande, horizontal o TalkBack en Android.

## Objetivos y cambios

- Apariencia permite seguir el tema de Android. La preferencia manual anterior
  se conserva; elegir modo manual desde Perfil desactiva el seguimiento.
- Restablecer Apariencia elimina únicamente las cinco claves visuales. Conserva
  biblioteca, favoritos, playlists y ajustes de audio; requiere confirmación local.
- La política efectiva de movimiento combina la preferencia propia y la de Android.
  Se consulta al componer y al volver a primer plano; no se borra la preferencia.
  No implica control de todas las animaciones internas de Material o del sistema.
- Inicio reutiliza artistas agrupados en ViewModel. El visualizador reutiliza un
  Brush por composición en lugar de crear uno por barra y cuadro. Sigue siendo
  decorativo; no analiza la señal. Impacto en memoria/FPS todavía sin medir.
- Inicio dice «Tu música» y «Tus artistas»: el contenido actual no demuestra
  reproducciones recientes ni un ranking de escucha.
- Cabeceras, menús y diálogos admiten mejor fuente grande: textos con espacio
  flexible, desplazamiento, opciones de temporizador que se distribuyen en filas,
  fila completa de espera seleccionable y botón de playlist vacío deshabilitado.
- Letras locales/remotas comparten parser. Corrige centésimas, admite décimas,
  milésimas, segundos enteros y varias marcas por línea. Se acota la entrada;
  no se implementa interpretación de offset ni acceso LRC hermano mediante SAF.
- Letras y comprobación de actualizaciones reutilizan BoundedHttpReader con
  máximo 256 KiB, tiempos acotados, cierre de conexión y cancelación propagada.
  La lectura nativa de metadatos/jaudiotagger sigue requiriendo perfilado.

## Archivos intervenidos

Raíz Kotlin: app/src/main/java/com/example/comarleyaetheraudio/.

| Grupo | Archivos principales |
|---|---|
| Preferencias | ui/theme/AppearanceSettings.kt, PlatformAppearance.kt, data/repository/SettingsRepository.kt |
| Conexión | MainActivity.kt, presentation/library/LibraryViewModel.kt, navigation/AppNavigation.kt |
| Pantallas | settings/AppearanceScreen.kt, profile/ProfileScreen.kt, home/HomeScreen.kt |
| Efectos | AudioVisualizerView.kt, consumidores de LocalAppearance en carátulas, biblioteca, playlists y letras |
| Diálogos | CreatePlaylistDialog.kt, SleepTimerDialog.kt, TagEditorDialog.kt, SongOptionsMenuSheet.kt |
| Letras/red | LrcParser.kt, LrcLibService.kt, domain/playback/SyncedLyricsParser.kt |
| Pruebas | AppearancePersistenceTest, AppearancePolicyTest, AppearancePanelTest, AppearanceAccessibilityTest, SyncedLyricsParserTest |
| Herramientas | tools/Validate-SoundFlow.ps1, tools/Collect-SoundFlowMetrics.ps1 |

El detalle de audio y actualizaciones se registra en documentos 15 y 16.
Entidades, nombre y versión Room, dependencias y configuración global intactos.

## Historial del primer intento del agente (superado por Gradle del usuario)

- El intento Gradle del agente para assembleDebug, testDebugUnitTest,
  assembleDebugAndroidTest y lintDebug falla antes de tareas con
  `java.io.IOException: Unable to establish loopback connection`.
- La solicitud del informe debugRuntimeClasspath falla por el mismo motivo.
  No hay informe resuelto ni nuevo APK de este bloque.
- La validación directa Kotlin/JUnit se registra en el plan de pruebas. Usa
  caché local y clases Android generadas previamente; no ejecuta KSP/Room,
  recursos, manifest merger, lint ni empaquetado y no sustituye a Gradle.
- Scripts PowerShell comprobados sintácticamente; no se afirma ejecución
  completa de Validate-SoundFlow ni captura real de memoria/cuadros.
- Instrumentación y matriz visual siguen pendientes. Un test parametrizado
  de Apariencia cubre cuatro paletas por dos modos, fuente 2x y acciones de
  volver/restablecer; no equivale a revisar toda la app con TalkBack.

## Dispositivos y recorrido mínimo

Contexto aportado por el usuario: POCO X7 Pro, Android 16, unas 50 canciones
de 20–80 MB; Redmi de su pareja, unas 200 canciones de ~5 MB, modelo/Android
por identificar. Son perfiles de prueba, no resultados nuevos ni benchmarks.
minSdk=24: Android 7 o posterior; falta validar versiones y fabricantes.
El tamaño de una canción no certifica su formato, resolución ni calidad.

Desde PowerShell en C:\SoundFlow-Git:

```powershell
.\tools\Validate-SoundFlow.ps1
```

Compila app y pruebas Android, ejecuta JVM/lint y solicita dependencias resueltas.
Guarda logs bajo build/validation. No necesita un teléfono para esas tareas.
No cambia dependencias, firma, versión, preferencias globales ni Git.
APK esperado tras éxito: app/build/outputs/apk/debug/app-debug.apk.

Prueba manual sobre el APK recién generado:

1. Cambiar tema de Android con «Seguir tema del sistema», volver a la app y
   comprobar Inicio, navegación, mini, reproductor, Perfil, Ajustes y diálogos.
2. Recorrer cuatro paletas en claro/oscuro; comprobar textos, selección, iconos,
   letras y barras. Repetir con animaciones/transparencias activadas y apagadas.
3. Desactivar animaciones de Android, volver y comprobar que la preferencia
   propia se conserva. Restablecer Apariencia: cancelar primero y luego aceptar;
   comprobar que favoritos, playlists y AudioFX siguen intactos.
4. Fuente grande/TalkBack: acciones de cerrar, volver, temporizador, opciones
   y creación/selección de playlists accesibles. Repetir horizontal y con notch.
5. Letras con marcas [00:01.50] y [00:01.500] deben coincidir a 1,5 segundos.
6. Repetir navegación/reproducción 10 minutos, incluyendo archivos grandes y
   segundo plano. Registrar errores y circunstancias, no solo «funciona».

Medición opcional cuando exista dispositivo ADB autorizado:

```powershell
.\tools\Collect-SoundFlowMetrics.ps1 -Serial SERIAL_REAL -Label inicio -ResetFrameStats
# Repetir un recorrido fijo de navegación y reproducción.
.\tools\Collect-SoundFlowMetrics.ps1 -Serial SERIAL_REAL -Label efectos-activos
```

Repetir con efectos desactivados en el mismo dispositivo/APK y duración. El
script solo captura dumpsys meminfo/gfxinfo del paquete; el reinicio opcional
afecta contadores de cuadros, no datos. Sin estas mediciones no se promete
un consumo máximo de RAM, ausencia de fugas, 120 FPS ni ahorro de batería.

## Cierre

Fases 4/5 cerradas conforme a la decisión final indicada arriba. El usuario
acepta el último APK; las verificaciones inaccesibles quedan excluidas del cierre,
sin presentarlas como ejecutadas. Se conservan las evidencias Gradle/JVM/Lint
anteriores y la comprobación directa posterior. No se promete validación exhaustiva.
Audio Inspector avanza como bloque acotado autorizado; las fases 6 y 7 completas
no se declaran terminadas por añadir este panel o por una compilación.
