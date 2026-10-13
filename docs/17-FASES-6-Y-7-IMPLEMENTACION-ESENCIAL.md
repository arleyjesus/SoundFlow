# Fases 6 y 7 — implementación esencial local

Fecha: 2026-10-12. Rama: refactor/soundflow-4.
Fases 1–5 aceptadas por el usuario; no se reabren sus pruebas excluidas.

## Objetivo y alcance

Añadir herramientas locales útiles sin modificar Room y preparar la distribución
con controles de privacidad reales. Primer alcance implementado: Audio Inspector
existente, Smart DJ por metadatos, candidatos a duplicados y servicios opcionales.
Las propuestas de innovación no se convierten todas en requisitos de este bloque.

## Diagnóstico y plan aplicado

- Existía inspección de fuente; faltaban sesiones sugeridas y revisión de coincidencias.
- Deezer/LRCLIB/GitHub no tenían interruptores persistentes por servicio.
- La preparación debug existía; faltaba un comando que guardase un candidato release
  con sus informes y hashes, sin gestionar claves o publicar.
- Se mantiene soundflow_database v2, entidades, permisos, dependencias, versión de
  aplicación y configuración global. No se rediseñan las migraciones ni el backup.

## Fase 6: funciones implementadas

Inicio → «Smart DJ y biblioteca inteligente» abre una pantalla nueva.

Smart DJ crea hasta 30 sugerencias por metadatos: Variedad, Favoritos o Explorar
no favoritas. Variedad usa como referencia el género de la canción actual y
favorece favoritos, recorriendo grupos de artistas por turnos. Es determinista;
no aprende hábitos, calcula BPM, analiza señal ni mezcla dos reproductores.
No duplica IDs ni una URI exacta dentro de la sesión. No equipara alias SAF o
fuentes distintas mediante rutas. Solo cambia la cola al pulsar «Reproducir sesión».
No guarda ni modifica playlists/favoritos; las selecciones vacías no reproducen.

Posibles duplicados agrupa la misma URI o título/artista normalizados con duración
positiva y hasta 2 s de diferencia respecto al inicio de cada grupo. No encadena
diferencias que superen ese margen. Omite metadatos desconocidos para comparación
por título/artista. No calcula hashes ni prueba igualdad del audio: versiones,
mezclas y calidad pueden diferir. Solo permite escuchar, nunca borrar o fusionar.
La UI limita la vista a 50 grupos y 10 versiones por grupo e informa del límite.

La proyección y cálculos se ejecutan en Dispatchers.Default; cambiar fuente,
modo o favoritos cancela el consumidor anterior. El cálculo síncrono no promete
interrupción inmediata dentro del ordenamiento. Al salir se cancela el consumidor.

Archivos nuevos: domain/library/SmartLibrary.kt, presentation/library/SmartLibraryScreen.kt.
Conexión: HomeScreen, Screen y AppNavigation. No se añaden lecturas de audio/red.

## Fase 7: privacidad y preparación local

Ajustes → Privacidad y servicios en línea ofrece tres interruptores independientes:

| Servicio | Datos y comportamiento |
|---|---|
| Imágenes / Deezer | Nombre del artista; desactivar conserva lectura de caché local y evita nuevas consultas de los recorridos activos |
| Letras / LRCLIB | Título/artista; desactivar mantiene búsqueda de letras locales y muestra aviso si no hay |
| Actualizaciones / GitHub | Consulta de versiones y datos normales de conexión; sin descarga/instalación automática |

Se guardan tres booleanos nuevos en settings_prefs. Por compatibilidad, valores
ausentes conservan servicios activados; antes de cargar preferencias no se hacen
esas consultas. Restablecer Apariencia no altera privacidad. Cambiar una opción
cancela los efectos Compose asociados; no puede deshacer información enviada ni
garantizar interrupción inmediata de una llamada nativa/HTTP ya iniciada.
No se suben canciones ni se añaden cuentas, analítica, servidores o permisos.

Archivos: NetworkSettings, NetworkPreferences, SettingsRepository, LibraryViewModel,
MainActivity, SettingsScreen, AppNavigation, ArtistImageFetcher, ArtistCircleCard,
LibraryScreen y FullPlayerSheet. Las dos llamadas activas a imágenes pasan la política;
las consultas de letras y actualizaciones quedan condicionadas a su preferencia.

`tools/Prepare-SoundFlowRelease.ps1` construye assembleRelease, testReleaseUnitTest
y lintRelease; copia APK(s), informes y hashes a build/release-check. Compara hash
de copia/original para detectar cambios durante captura. No crea certificados,
firma, instala, cambia versión ni publica. Un build release exitoso no significa
que el APK esté firmado o se pueda distribuir. En esta intervención solo se valida
la sintaxis del script, sin ejecución completa por el bloqueo previo de Gradle.

## Validación real

- Comprobación directa Kotlin 2.2.10/Compose: 137 fuentes main/test/androidTest correctas.
- JUnit directo: **98 tests**, cero fallos. 88 anteriores y 10 nuevos: 8 de sesiones/
  coincidencias y 2 de preferencias de privacidad (independencia, reapertura y reset).
- No sustituyen recursos, manifest merger, KSP/Room, Lint o empaquetado Gradle.
- Gradle assembleDebug/testDebugUnitTest/assembleDebugAndroidTest/lintDebug vuelve
  a fallar antes de tareas por `java.io.IOException: Unable to establish loopback connection`.
- Logs en build/phase5-design-check/phase6-7-kotlin.txt, phase6-7-junit.txt y
  phase6-7-gradle.txt. No APK nuevo ni Lint de este bloque aprobados por el agente.
- Prepare-SoundFlowRelease.ps1 sin errores de sintaxis PowerShell.
- Pruebas Android y perfilado inaccesibles excluidos por el usuario; no ejecutados.

## Comprobación local del usuario

```powershell
.\tools\Validate-SoundFlow.ps1
```

Instalar el APK generado en app/build/outputs/apk/debug/app-debug.apk. Probar Inicio
→ Smart DJ: cambiar modos, revisar lista y reproducir sesión. Escuchar versiones
desde un grupo de coincidencias; favoritos/playlists/archivos deben permanecer.
En Ajustes, apagar los tres servicios, reiniciar y comprobar que siguen apagados;
restablecer Apariencia no debe activarlos. Letras locales siguen disponibles.

Cuando se prepare un candidato release local:

```powershell
.\tools\Prepare-SoundFlowRelease.ps1
```

## Alcance aún no implementado y publicación

Replay, copia/restauración portable, perfiles por salida, USB DAC exclusivo y
transferencia Wi-Fi siguen siendo propuestas. Requieren diseños propios de datos,
identidad, seguridad/hardware; no se simulan ni se califican como terminadas.
Audio Inspector no mide salida/DAC ni certifica bit-perfect.

La versión permanece 3.7.0 (37). Para una distribución 4.0 se debe aprobar versión/
canal y estrategia de firma. Actualizar una instalación existente requiere el mismo
certificado compatible y un versionCode válido; no se resuelve desinstalando y
perdiendo datos. No usar claves debug para una publicación estable ni compartir
contraseñas/claves en el chat o Git. No se crea ni se lee una clave de firma.

Fase 6 cuenta con este primer bloque funcional; fase 7 con controles y herramienta
de preparación. No se declara lanzamiento, firma, commit ni publicación. Las
decisiones de distribución son distintas de las pruebas excluidas por el usuario.

## Revisión de Git

Se conservan modificaciones acumuladas y archivos personales. Cambios locales,
sin staging/commit/push. Diff/check/stat/status se revisan al entregar; sus cifras
incluyen fases anteriores sin confirmar, no solo este bloque. Sin cambios en
Room, Gradle/dependencias, manifiesto, backup, firma ni versión de aplicación.
