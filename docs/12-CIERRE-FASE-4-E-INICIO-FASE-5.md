# Cierre de implementación de fase 4 e inicio de fase 5

Fecha: 2026-10-11, America/Bogota. Rama: `refactor/soundflow-4`.

**Estado actualizado:** implementación esencial 4A/4B compilada con Gradle;
usuario confirma funcionamiento correcto del APK. Verificados APK app/tests
regenerados a las 23:23:57/58 America/Bogota del 2026-10-11, posteriores a las
fuentes, y XML Gradle con **62 JVM sin fallos, errores ni omitidos**, UTC
2026-10-12T04:23:59–04:24:00. Incluye 61 específicos y uno de plantilla.

Aceptación funcional general registrada; mediciones de memoria/cuadros, informe
resuelto de dependencias, regresión por caso e instrumentación pendientes.
Fase 5 iniciada con base Apariencia aceptada en general; rediseño integral pendiente.
Los bloqueos y pendientes de APK que siguen describen el intento anterior del agente.

## Objetivo y diagnóstico

Antes de ampliar efectos visuales, acotar recursos y eliminar trabajo redundante.
Se confirmaron descargas de artistas sin límite, claves hashCode con colisiones,
una segunda observación fría de canciones desde playlists, búsqueda en composición
y observación de progreso en la raíz de navegación. El ViewModel dependía del
repositorio concreto y las carpetas de presentación eran entidades Room.

## Implementación de 4B

- HTTP de artistas: HTTPS sin redirecciones, JSON máximo 128 KiB, imagen máxima
  2 MiB incluso sin Content-Length, connect/read timeout de 5 s y presupuesto
  cooperativo de 10 s por solicitud. Una lectura bloqueante puede sobrepasar ese
  presupuesto hasta su timeout; cancelar no interrumpe instantáneamente I/O nativa.
- Máximo dos solicitudes concurrentes, 16 mutex de tamaño fijo, backoff de 60 s
  con máximo 128 claves. Una búsqueda completa puede requerir dos solicitudes.
- Clave SHA-256 de nombre normalizado con Locale.ROOT. Publicación AtomicFile,
  imagen muestreada a objetivo 250 px, renovación tras siete días con fallback
  al archivo anterior válido si falla la red. Cuota de 32 MiB, poda a 80% tras
  publicación; solo JPG generados, incluidos nombres antiguos, nunca canciones.
  Las claves antiguas no se reutilizan por sus posibles colisiones. El primer
  resultado de Deezer puede corresponder a otro artista: riesgo semántico existente.
- Playlists reciben el StateFlow de biblioteca del ViewModel. El repositorio no
  crea scope propio. El contrato frío allPlaylists se conserva para otros usos;
  la ruta activa comparte la observación y el mapeo de canciones.
- Búsqueda en Default, cancelación cooperativa cada 64 elementos, orden estable,
  resultados vinculados a consulta y fuente actuales, claves de lista por ID.
- Índice del controlador construido en Default. Raíz de navegación observa estado
  sin posición ni bytes de portada; minirreproductor y reproductor abierto observan
  su progreso localmente. Colectores de UI activos usan ciclo de vida.
- AudioRepositoryImpl implementa AudioRepository; ViewModel consume la interfaz.
  LibraryFolder y FolderImportResult separan carpetas/resultados de entidades Room.
  Uri Android y algunas excepciones data siguen presentes: separación gradual,
  no se afirma independencia total de Android ni una arquitectura completamente nueva.

## Inicio de fase 5

Entrada **Perfil → Ajustes → Apariencia**. Cuatro paletas con modo claro/oscuro,
guardadas mediante las claves existentes de settings_prefs. Nuevas claves
appearance_animations y appearance_transparencies independientes, por defecto true.
La primera composición de la app espera el snapshot persistido para evitar un
cambio fugaz de paleta. No cambia el DSP, biblioteca ni preferencias de audio.

ColorScheme usa una única fuente de paletas: MainActivity ya no sobrescribe sus
superficies. Pares primary/onPrimary, contenedores y superficies tienen contraste
comprobado. El reproductor usa el acento del tema para controles; el color de portada
queda para decoración de fondo. Barra inferior usa roles del tema.

Animaciones controla las cuatro transiciones de navegación y el visualizador
decorativo. Su bucle no se crea al pausar o apagar la opción. Transparencias
controla la portada de fondo, blur y degradado del reproductor, con alternativa
opaca y sin extraer paleta decorativa al apagarla. La portada principal se conserva.

**Pendiente de fase 5:** aplicar esta política al resto de crossfades, paneles,
tarjetas y superficies locales; renovar inicio, biblioteca/filtros, artistas,
carpetas, playlists abiertas, canción/opciones, letras, perfil y todos los ajustes.
Los interruptores describen su alcance actual en el panel. No se afirma que ya
intercepten todas las animaciones de Material, Coil o del sistema. Seguir sistema,
restablecer apariencia y revisión visual integral siguen pendientes.

## Archivos de esta intervención

Rutas relativas a la raíz del repositorio, conservando el paquete existente:

- data/local/SongImportResult.kt; data/local/util/ArtistImageFetcher.kt,
  ArtistCachePolicy.kt; data/remote/BoundedHttpReader.kt.
- data/repository/AudioRepositoryImpl.kt, SettingsRepository.kt;
  data/player/AudioPlayerHandler.kt.
- domain/model/LibraryFolder.kt; domain/repository/AudioRepository.kt;
  domain/library/SearchLibrary.kt.
- MainActivity.kt; presentation/library/LibraryViewModel.kt;
  presentation/search/SearchScreen.kt; presentation/folders/FoldersScreen.kt;
  presentation/profile/ProfileScreen.kt; presentation/navigation/AppNavigation.kt,
  Screen.kt; presentation/settings/SettingsScreen.kt, AudioFxScreen.kt,
  AppearanceScreen.kt; presentation/player/FullPlayerSheet.kt, AudioVisualizerView.kt.
- ui/theme/AppearanceSettings.kt, Theme.kt; AppTheme.kt se reutiliza sin cambios.
- JVM: BoundedHttpReaderTest, ArtistCachePolicyTest, SearchLibraryTest,
  AppearancePersistenceTest, ThemeContrastTest.
- Android: RepositorySharingTest (dos casos), SearchResultsTest (uno),
  AppearancePanelTest (uno); FolderActionsTest adaptado al modelo LibraryFolder.
- README, auditoría, roadmap, arquitectura, plan de pruebas, changelog y documentos
  10, 11 y este registro actualizados con resultados reales.

## Validación inicial del agente (histórico; resultado posterior al comienzo)

| Comprobación | Resultado de este estado |
|---|---|
| Gradle assembleDebug / testDebugUnitTest / assembleDebugAndroidTest offline | Bloqueado antes de tareas: java.io.IOException: Unable to establish loopback connection |
| Kotlin 2.2.10 + plugin Compose, classpath local existente | 71 fuentes seleccionadas pasan, incluidos todos los AndroidTest; advertencias de iconos obsoletos y Virtualizer |
| JUnit directo | **61 tests pasan**, cero fallos; 40 anteriores + 17 nuevos fase 4 + 4 nuevos fase 5 |
| Persistencia Apariencia | Tres JVM con archivos temporales aislados: compatibilidad de claves, cuatro combinaciones de efectos, reapertura y conservación de otras claves |
| Contraste | Un JVM recorre ocho combinaciones de paleta/modo, cinco pares cada una, mínimo 4.5:1 |
| Instrumentación | 59 casos nuevos acumulados escritos, ninguno ejecutado; los 55 anteriores tenían APK Gradle, cuatro nuevos solo comprobados con K2 |
| APK nuevo / Room KSP / empaquetado | **Pendientes**, K2 directo no los valida ni genera APK |
| Memoria, batería y cuadros | **Pendientes**; no se promete reducción cuantificada ni FPS |

Los intentos iniciales del ejecutor directo requirieron completar su classpath de
DataStore/Okio/Room ya descargados. No se añadieron dependencias ni se modificó
configuración global. Logs locales: build/phase4b-phase5-check/compile-result.txt
y junit-result.txt. Los XML Gradle de 41 tests y APK de las 22:41 corresponden
al estado anterior 3C/4A, no a esta intervención.

## Verificación manual y aceptación pendiente

Desde PowerShell en C:\SoundFlow-Git:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest --console=plain
Get-Item .\app\build\outputs\apk\debug\app-debug.apk | Select-Object FullName, LastWriteTime, Length
```

No requiere un teléfono conectado para generar ambos APK. Instalar el app-debug.apk
recién generado como actualización, conservando datos. No usar el APK anterior
para aceptar cambios nuevos ni borrar datos/desinstalar para estas comprobaciones.

1. Confirmar favoritos y playlists existentes. Añadir C a A/B, renombrar, cerrar
   y reabrir app. Comprobar importación y acceso a carpetas sin quitar datos reales.
2. Buscar rápidamente título/artista/álbum; cambiar consulta y vaciarla. Abrir un
   resultado y comprobar canción e ID correctos, sin resultados anteriores retenidos.
3. Recorrer artistas con red y sin red. La biblioteca y reproducción deben funcionar
   si Deezer falla. Revisar crecimiento de artist_covers y album_covers por separado.
4. Abrir Apariencia desde Perfil/Ajustes; probar cuatro paletas en claro/oscuro.
   Cambiar cada interruptor por separado, forzar cierre y verificar persistencia.
   Reproducir y navegar: sin movimiento decorativo al apagar Animaciones y fondo
   opaco de reproductor al apagar Transparencias. Comprobar botones legibles.
5. Reproducción en segundo plano, notificación, regreso a app, avance/pausa,
   favoritos, temporizador y AudioFX. Las opciones visuales no deben cambiar sonido.
6. En el mismo dispositivo, biblioteca y build registrar arranque frío/caliente,
   primer/segundo scroll, 20 cambios de pista y 20 aperturas/cierres de reproductor.
   Usar Android Studio Profiler: heap Java/native inicial, pico y recuperación tras
   GC; cuadros lentos y cache en disco. Comparar antes/después con efectos on/off.
   Criterio: sin crecimiento sostenido ni regresión funcional; investigar si aparece.

Informe de dependencias resueltas pendiente cuando Gradle funcione:
`.\gradlew.bat :app:dependencies --configuration debugRuntimeClasspath`.
No se actualizan versiones dentro de esta intervención.

## Conservación y Git

Respaldo previo aceptado 3C/4A conservado en local-backups/fase3C-fase4A-aceptada-20261011-2241.
Checkpoint de fuentes 4B previo al inicio visual en
local-backups/fase4B-fuentes-pendientes-validacion-20261011; contiene logs directos
57 JVM, no APK validado. Son respaldos locales ignorados por Git.

Estado final de fuentes y logs guardado en
local-backups/fase4B-inicio-fase5-pendiente-20261011, con manifiesto SHA-256;
sin copiar ni etiquetar el APK anterior como APK nuevo.

Se conservan cambios previos y archivos personales. No cambian entidades, esquema 2,
nombre soundflow_database ni dependencias. La revisión final usa git diff --check,
git diff --stat y git status; no hay staging, commit ni push de esta intervención.
