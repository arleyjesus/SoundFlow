# SoundFlow 4.0 — fluidez y cristal más discreto

## Objetivo y diagnóstico

Petición del usuario: reducir el cristal en Inicio, simplificar iconos y separadores,
renovar la onda y reducir demoras al cambiar canción/buscar una posición.
Se conserva la prueba del APK para el final del bloque.

Confirmado en código, sin medir latencia en el teléfono:

- Siguiente/anterior esperaba 300 ms para atenuar volumen antes de saltar. Era una
  atenuación manual, no mezcla de dos pistas; no utilizaba la duración configurada.
- Elegir una canción reconstruía toda la cola con stop, clear, set y prepare,
  incluso al elegir otra de la misma lista.
- El aviso de buffering aparecía inmediatamente y cambiaba la altura del mini.
- El visualizador leía su fase animada durante composición, en cada cuadro.
- La ordenación alfabética normalizaba texto y construía Regex en comparaciones.
- El refresco visual de posición seguía cada 500 ms en segundo plano.
- La carátula retenida/publicada procedía del original incrustado sin límite de bytes.

La búsqueda en el deslizador ya se envía al soltar, no por cada movimiento.
No se ha demostrado un problema de buffers de ExoPlayer, ni se atribuye la demora
al tamaño del archivo por sí solo. Formato, índices de búsqueda, proveedor SAF,
almacenamiento y decodificador pueden influir; su efecto real necesita el dispositivo.

## Implementación

- Inicio: variante de panel con mucho menos tinte, opacidad 98–99 % y borde de
  0,5 dp tenue en Smart DJ, Tu música y mosaicos. Conserva las otras superficies,
  temas y opción de desactivar transparencias. Contratos anteriores conservados.
- Navegación: líneas de 0,5 × 18 dp, centro con alfa 0,12. Iconos sin pequeñas
  ondas interiores; Biblioteca más sencilla y Pausa con dos trazos claros.
- Onda: tres curvas superpuestas con degradado, Path/Brush reutilizados y fase
  leída únicamente al dibujar. Sin bucle al pausar, ocultar pantalla o desactivar
  animaciones. Decorativa; no mide audio ni pide permisos nuevos.
- Saltos manuales inmediatos: retirada la espera de 300 ms. Se conserva la
  atenuación de apagado del temporizador y su cancelación al intervenir el usuario.
- Cola idéntica: buscar el índice existente sin reconstruir MediaItems. Se exige
  igualdad del snapshot completo (incluidos URI y metadatos), cantidad e IDs en
  el reproductor. Cambio de orden/origen/metadatos/timeline reemplaza la cola.
  Error o estado IDLE permite preparar de nuevo. Conserva aleatorio explícito,
  posición inicial cero y repetición de la lista.
- Cola nueva: setMediaItems reemplaza directamente; retirados stop/clear redundantes.
- Buffering: indicador tras 450 ms continuos; errores y conexión siguen inmediatos.
  No modifica el buffering real ni oculta una preparación prolongada.
- Orden alfabético: claves normalizadas una vez por elemento y Regex reutilizada;
  mantiene desempates por ID, acentos y orden visible de reproducción.
- onStart/onStop activa/suspende solo el sondeo visual de posición. Al volver se
  refresca el estado. MediaSession, reproducción, escucha opcional y temporizador
  conservan su funcionamiento independiente.
- Carátula: decodificación muestreada hasta 1024 px y JPEG retenido/publicado de
  hasta 512 KiB; una imagen que exceda ese límite no se incrusta. Extracción y
  compresión fuera del hilo principal. El retriever aún obtiene transitoriamente
  los bytes originales; no se afirma que esa lectura esté limitada a 512 KiB.

Referencia del contrato de reemplazo y actualización de cola:
[Media3: playlists](https://developer.android.com/media/media3/exoplayer/playlists).

No cambian dependencias, SDK, versión de app, ajustes globales, entidades,
soundflow_database ni versión 2 de Room. Continúa el candidato 4.0.0-rc.1;
no es un nuevo lanzamiento público.

## Validación

- Comprobación directa Kotlin de 153 fuentes main/test/androidTest: correcta.
- Suite JVM directa: 137 pruebas correctas. Siete nuevas comprueban reutilización,
  reordenación, cambio de URI/metadatos, cambios externos de timeline y cola vacía.
  Contraste ampliado a la variante sutil en todos los temas claro/oscuro.
- El primer intento directo detectó una referencia binaria antigua de panelColors
  en el classpath de comprobación. Se conservó la firma previa y se retiraron las
  clases antiguas de la aplicación del classpath de compilación; repetición correcta.
- Gradle assembleDebug/testDebugUnitTest/assembleDebugAndroidTest/lintDebug offline:
  bloqueado antes de tareas por java.io.IOException:
  Unable to establish loopback connection. No se generó un APK nuevo desde el agente.
- Logs locales ignorados: build/phase5-design-check/fluency-kotlin.txt,
  fluency-junit.txt y fluency-gradle.txt.
- La comprobación directa usa recursos/configuración generados existentes; no
  sustituye Gradle/KSP/empaquetado/Lint. Instrumentación solo comprobada como fuente.
- Sin mediciones de FPS, memoria, batería, latencia audible o prueba Android nueva.

## Comprobación final manual

Ejecutar desde C:\SoundFlow-Git: .\tools\Validate-SoundFlow.ps1.
Después del éxito, instalar app\build\outputs\apk\debug\app-debug.apk conservando datos.

1. Comparar Inicio, iconos, separadores y contraste en claro/oscuro, con/sin fondo
   personal y transparencias. Revisar con texto grande.
2. Seleccionar varias canciones de la misma lista; siguiente/anterior rápidamente,
   aleatorio apagado/encendido; cambiar filtro/orden y playlist. La cola debe seguir
   el contexto elegido y los controles responder sin atenuación manual previa.
3. Buscar posiciones cortas/largas en archivos habituales y grandes. Una espera
   menor a 450 ms no muestra banner; preparación prolongada y error deben informarse.
4. Onda reproduciendo/pausada, animaciones desactivadas, abrir/cerrar reproductor
   y desplazarse por listas. Valorar la fluidez real; no inferirla de las pruebas JVM.
5. Pantalla apagada/segundo plano: reproducción, controles de bloqueo y temporizador;
   regresar y comprobar canción/posición actualizadas, carátula y ranking opt-in.
6. Favoritos/playlists conservados después de actualizar y reabrir.

Git: revisar diff --check, diff --stat y status; conservar los cinco archivos
personales sin seguimiento y excluirlos de los commits.
