# Fase 5: rediseño visual sobre el APK aceptado

Fecha: 2026-10-12. Rama: `refactor/soundflow-4`.
Estado: rediseño compilado con Gradle y aceptado funcionalmente en general por
el usuario. Accesibilidad, matriz visual por caso y mediciones pendientes.

## Aceptación posterior del APK

El usuario confirma que probó el APK y funciona correctamente. Verificación
local: app generado el 2026-10-12 a las 10:15:34 Bogotá, después de las últimas
fuentes Kotlin (10:10:04); APK de pruebas a las 10:15:33. El hash del APK app
difiere del respaldo anterior y contiene SoundFlowPanelKt, PanelStyleKt y la
descripción nueva de Transparencias. No se identifica el artefacto solo por su
tamaño, que coincide con el anterior: 25 662 014 bytes.

XML Gradle: 65 JVM, cero fallos/errores/omitidos; timestamps UTC
2026-10-12T15:15:37.040Z a 15:15:38.303Z. ThemeContrastTest incluye cuatro casos.
Esto supera el pendiente de compilación/APK del intento del agente descrito
más abajo. No se atribuye ejecución de instrumentación ni mediciones al reporte
manual general. Fase 5 tiene este bloque aceptado, sin declarar validados todos
los criterios. No hubo nuevos cambios de código en esta verificación.

Respaldo del estado aceptado: `local-backups/fase5-rediseno-aceptado-20261012-1015/`,
con fuentes, APK app/tests, XML y hashes. Respaldos anteriores conservados.

## Objetivo y diagnóstico

El usuario aporta tres capturas: minirreproductor anterior con degradado,
estado actual con superficie gris plana y reproductor abierto. Se conserva la
estructura de Inicio, la distribución de controles del reproductor abierto y
el visualizador. Se mejora el conjunto sin restaurar archivos antiguos.

En código se confirmó que el contenedor inferior usaba `surface` y elevación
tonal, y que el minirreproductor tenía una superficie elevada opaca. También
había colores locales independientes del tema y crossfades siempre activados.
El reproductor abierto conservaba el tirador predeterminado del bottom sheet.
Su relación con la línea de la captura se deberá confirmar en el teléfono.

## Implementación y archivos

Fuentes bajo `app/src/main/java/com/example/comarleyaetheraudio/` salvo pruebas.

| Archivos | Cambio y finalidad |
|---|---|
| `ui/theme/AppTheme.kt`, `Theme.kt` | Fondo negro puro en las cuatro paletas oscuras, superficies grises neutras y fondos claros; colores de contenido, bordes y formas compartidas. |
| Nuevos `ui/theme/PanelStyle.kt`, `presentation/components/SoundFlowPanel.kt` | Degradado estático con borde fino y alternativa opaca. Componente común y tarjetas de acciones. |
| `MainActivity.kt` | Navegación sin elevación tonal sobre el fondo del tema; márgenes del Scaffold consumidos; barras del sistema adaptadas al modo; crossfade sujeto a Apariencia. |
| `presentation/player/MiniPlayerCard.kt` | Panel con degradado en lugar de superficie elevada; conserva progreso, errores/reintento, apertura y controles de 48 dp. |
| `presentation/player/FullPlayerSheet.kt` | Retira tirador superior, superficie rectangular y márgenes seguros superiores/laterales; botones secundarios semánticos. Conserva distribución, visualizador y acciones. |
| `presentation/home/HomeScreen.kt` | Conserva secciones; ambiente superior discreto y negro en el resto. Pincel recordado y margen inferior sin reserva duplicada. |
| `presentation/home/components/{ArtistCircleCard,PlaylistMosaicCard,RecentPosterCard}.kt` | Crossfade respeta Animaciones; superficies y acentos del tema donde corresponde. |
| `presentation/library/{LibraryScreen,SongsScreen,PlaylistsScreen}.kt` | Título/selección y superficies coherentes; pestañas sin desplazamiento animado si se deshabilita. |
| `presentation/search/SearchScreen.kt` | Resultados con tarjetas y espaciado común. Mantiene consulta cancelable e identidad de canción. |
| `presentation/playlist/{PlaylistDetailScreen,AddSongsToPlaylistDialog}.kt` | Colores coherentes; selección animada opcional. Playlist sin portada usa icono local en vez de imagen remota decorativa. Retira acción duplicada «Modificar fotos» que abría selección; Añadir permanece. |
| `presentation/profile/ProfileScreen.kt`, `presentation/settings/SettingsScreen.kt` | Jerarquía y tarjetas comunes, Apariencia directa y retorno de Ajustes. Conserva carpetas/permisos, DSP y datos reales de almacenamiento. |
| `presentation/settings/{AppearanceScreen,AudioFxPanel}.kt` | Descripciones de efectos actualizadas y secciones DSP con panel común; motor intacto. |
| `presentation/navigation/AppNavigation.kt` | Apariencia desde Perfil y retorno de Ajustes. |
| `presentation/player/LyricsView.kt` | Avance sincronizado sin animación cuando se deshabilita; color secundario del tema. |
| `presentation/folders/{FoldersScreen,FolderDetailScreen}.kt`, `presentation/components/items/{SongItem,SongOptionsMenuSheet}.kt` | Sustituye colores locales intervenidos por roles del tema y aplica política de crossfade. Iconos direccionales adaptables; acciones conservadas. |
| `presentation/components/dialogs/{CreatePlaylistDialog,ChangelogDialog}.kt` | Botones, campos y textos usan roles del tema. |
| `app/src/test/.../ui/theme/ThemeContrastTest.kt` | Tres casos nuevos: negro puro, contraste de paneles y opacidad con efectos apagados. |
| Nuevo `app/src/androidTest/.../presentation/VisualNavigationTest.kt` | Acciones de Apariencia, DSP y Ajustes desde Perfil con efectos apagados. Prueba de callbacks, no de navegación completa en dispositivo. |

El aspecto de cristal se aproxima con colores, transparencia y borde. No incluye
refracción ni captura del fondo. No se añaden desenfoques animados en listas.
El fondo decorativo del reproductor conserva el blur existente, desactivable
con Transparencias. No hay mediciones nuevas de memoria o cuadros.

Animaciones controla navegación, pestañas/letras, crossfades de portadas del
recorrido intervenido, selección y visualizador. No intercepta todas las
animaciones internas de Material, indicadores de carga o del sistema.
Las antiguas `MiniPlayer.kt` y `PlayerScreen.kt`, fuera del recorrido activo,
no se rediseñaron. Seguir sistema/restablecer apariencia continúan propuestos.

## Validación anterior: intento del agente antes del APK aceptado

- Gradle: `:app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest`
  con `--offline --console=plain` falla antes de tareas por
  `java.io.IOException: Unable to establish loopback connection`.
- Kotlin 2.2.10 y plugin Compose, ejecutados directamente con SDK y dependencias
  locales: **116 fuentes Kotlin** de main/test/androidTest pasan. Advertencias
  de APIs deprecadas existentes. No ejecuta KSP/Room, procesa recursos ni empaqueta
  APK; parte del classpath usa salidas previas.
- JUnit directo: **65 pruebas pasan**, cero fallos (64 específicas y plantilla).
  Incluye las tres nuevas y pruebas previas de integridad, caché, reproducción,
  búsqueda y preferencias. Contraste mínimo 4.5:1 de textos sobre colores de
  paneles compuestos sobre `background` y `surface` en ocho combinaciones de
  paleta/modo. No sustituye revisar capturas ni texto sobre fotos.
- Un caso Android nuevo comprobado por compilador; **60 instrumentados nuevos
  acumulados**, más la plantilla, sin ejecución registrada.
- Logs: `build/phase5-design-check/compile-result.txt` y `junit-result.txt`.
- APK de outputs: sigue siendo el aceptado del **2026-10-11 23:23:57 Bogotá**,
  25 662 014 bytes. No contiene este rediseño. Los 62 XML Gradle previos tampoco
  validan estos cambios nuevos.

## Compilar y revisar manualmente

Desde PowerShell en `C:\SoundFlow-Git`:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest
Get-Item .\app\build\outputs\apk\debug\app-debug.apk | Select-Object Length, LastWriteTime
```

Compilar APK no requiere conectar un teléfono. Instalar el nuevo como actualización
compatible, sin desinstalar ni borrar datos. `assembleDebugAndroidTest` compila
el APK de pruebas; no ejecuta instrumentación.

1. Inicio: revisar fondo oscuro, tarjetas y ausencia de bloque gris detrás del
   mini. Probar reproducción/pausa/siguiente y apertura desde la fila.
2. Reproductor: zona del notch, iconos del sistema, retorno y último control con
   gestos y con tres botones. Conservar disposición/visualizador; probar seek,
   favorito, letras, temporizador y menú de información.
3. Perfil → Apariencia: cuatro paletas en claro y oscuro, también con diálogos
   abiertos. Confirmar textos/selecciones/bordes legibles.
4. Las cuatro combinaciones de efectos y reinicio. Sin transparencias: paneles
   opacos y fondo decorativo apagado. Sin animaciones: portadas, pestañas y letras
   sin transición; visualizador estático.
5. Biblioteca, filtros, búsqueda, artistas y carpetas: entrar/salir, cambiar
   orden, abrir opciones de canción correcta y comprobar permisos conservados.
6. Playlist vacía/con portada: abrir, añadir C a A+B, renombrar y quitar una
   canción. Comprobar favoritos y playlists tras cerrar/reabrir, sin pérdida.
7. Perfil, Ajustes y DSP: accesos/retorno; Apariencia no cambia sonido. Revisar
   teclado, fuente grande, horizontal y TalkBack.
8. Repetir recorrido con efectos activados/desactivados; medir memoria/cuadros
   según documento 10 antes de afirmar mejora de rendimiento.

## Git, conservación y pendientes

Se revisaron rama, estado e historial antes de editar; cambios existentes
conservados. Comparación de hashes con respaldo aceptado: ningún archivo previo
eliminado. Este bloque no cambia dependencias, configuración global, entidades,
nombre/versión Room, escáner ni motor de reproducción.

Respaldo anterior: `local-backups/fase4B-apariencia-aceptada-20261012/`, fuentes,
APK aceptado, APK tests, XML y hashes. Respaldo nuevo de fuentes/evidencias:
`local-backups/fase5-rediseno-pendiente-20261012/`; no presenta el APK anterior
como artefacto nuevo. Ambos locales e ignorados por Git.

Revisión: `git diff --check`, `git diff --stat`, `git status` y archivos nuevos.
Las estadísticas incluyen cambios acumulados de fases anteriores. Sin staging,
commit ni push. Fase 5 sigue abierta: APK actualizado, revisión visual/funcional,
accesibilidad e instrumentación pendientes. Fase 4 conserva perfilado real e
informe de dependencias resueltas pendientes.
