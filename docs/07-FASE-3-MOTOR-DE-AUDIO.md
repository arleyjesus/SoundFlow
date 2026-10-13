# Fase 3 - Motor de audio y diseno interno

Fecha: 2026-10-11. Rama refactor/soundflow-4.
Estado: primer bloque compilado con Gradle desde la terminal del usuario y
APK con funcionamiento correcto reportado. Aceptacion manual general, sin
detalle por caso/dispositivo. Cambios locales; la fase 3 sigue abierta.

Fase 3B implementada localmente: efectos asociados al servicio y panel renovado.
Validacion posterior: 29 JVM pasan con Gradle y APK con funcionamiento correcto
reportado por el usuario. 49 instrumentados compilados, sin ejecutar.
Aceptacion manual general; propuesta 3C para ajustes finales antes del cierre.
Detalle actualizado en [08-FASE-3B-EFECTOS-Y-DISENO.md](08-FASE-3B-EFECTOS-Y-DISENO.md).

## Respaldo previo de fase 2

Carpeta local: C:\SoundFlow-Git\local-backups\fase2-validada-20261011.
Contiene soundflow-fase2-fuentes-205409.zip (134 entradas),
soundflow-fase2-debug.apk, fase2-tracked.patch y manifest.json.
Se comparo SHA-256 del APK original y la copia: iguales. El ZIP contiene
fuentes/documentacion del estado aceptado antes de editar reproduccion, sin
local.properties, cache, .git ni imagenes/informes personales sin seguimiento.
La carpeta se ignora en Git y sobrevive a Gradle clean. No es un commit ni push.

## Objetivos

Mantener reproduccion y controles coherentes al recrear la Activity, gestionar
interrupciones y desconexion de auriculares y evitar reintentos sin control.
Conservar cola, favoritos, playlists y biblioteca. Sin actualizar dependencias.

## Hallazgos iniciales confirmados en codigo

| Prioridad | Evidencia | Riesgo por verificar |
|---|---|---|
| Alta | MainActivity construye LibraryViewModel y AudioPlayerHandler directamente en onCreate | Cada recreacion puede dejar controlador y corrutinas anteriores vivos |
| Alta | AudioPlayerHandler conserva el future solo en init y no ofrece release; scope sin cancelacion de propietario | Conexion fallida o pendiente, listeners y recursos sin cierre coordinado |
| Alta | onPlayerError llama prepare() para cualquier PlaybackException | Reintentos repetidos ante permiso perdido, archivo borrado o error permanente |
| Alta | Service crea ExoPlayer sin configurar audio focus ni becoming noisy; atributos se configuran desde controlador | Conducta de foco dependiente de conexion de UI; desconexion de auriculares sin politica explicita |
| Media | onMediaItemTransition lee MediaMetadataRetriever sincronicamente en listener principal | Pausas de UI con caratulas grandes o proveedor lento |
| Media | Saltos manuales lanzan fades independientes y cambian volumen | Pulsaciones rapidas compiten entre si; volumen no restaurado si se cancela |
| Media | Listener temporal de fin de cancion no se retira al cancelar temporizador | Un temporizador cancelado podria seguir pausando reproduccion |
| Media | setupAudioEffects no tiene llamadas y EqualizerHandler es otra implementacion | Controles visuales y efectos reales pueden no corresponder |

El servicio ya libera player y MediaSession en onDestroy. No asumir dos
ExoPlayer activos: el handler usa MediaController. Las consecuencias indicadas
son riesgos derivados del codigo, no fallos reproducidos en dispositivo.

## Primer bloque implementado

1. Propiedad de ViewModel mediante ViewModelProvider/Factory, conservando contratos.
2. Propiedad y cierre del future/controlador, listener y jobs desde onCleared;
   liberar cliente sin detener el player del servicio durante una recreacion.
3. Configurar AudioAttributes, gestion de foco y becoming noisy en ExoPlayer
   dentro del servicio. Comprobar disponibilidad con Media3 1.5.1 instalada.
4. Capturar errores de conexion y de reproduccion; estado legible y reintento
   explicito, sin prepare() incondicional. La ultima seleccion anterior a
   conectar se conserva y se aplica al completar la conexion.

Tambien implementado:
- Observacion de canciones Room para resolver la cola del servicio por ID al
  reconectar; no sustituye la cola por abrir de nuevo la interfaz.
- Caratula extraida en IO, decodificacion muestreada a un maximo de 1024 px
  y paleta en Default; cancelar peticiones antiguas antes de publicar.
- Un job de atenuacion con cancelacion y restauracion de volumen. Se mantiene
  el fade manual de 300 ms; no es mezcla simultanea de dos pistas.
- Temporizador conectado a su dialogo, respetando minutos antes de esperar
  el final de pista; listener retirado al cancelar o liberar el cliente.
- Duracion desconocida y posicion limitadas sin negativos ni overflow.
- Minirreproductor Material 3 con jerarquia tipografica, progreso, controles
  de 48 dp y mensajes accesibles de conexion, preparacion y error.
- Reproductor expandido con cabecera Ahora suena, portada acotada, contraste
  basado en el tema, scroll en modo normal y etiquetas de avance/retroceso
  de diez segundos acordes con la accion. Favorito y temporizador conservados.
- ViewModel retenido al girar y apertura del reproductor con rememberSaveable.

Archivos de produccion: MainActivity.kt, LibraryViewModel.kt,
AudioPlayerHandler.kt, SoundFlowMediaService.kt, AudioPlayerState.kt,
PlaybackPosition.kt, MiniPlayerCard.kt y FullPlayerSheet.kt.
Sin cambios adicionales en Room, entidades o dependencias.

Pruebas nuevas: PlaybackPositionTest.kt, PlaybackControlsTest.kt y
PlayerConnectionTest.kt. Documentacion actualizada: README.md, auditoria,
roadmap, arquitectura, plan de pruebas, changelog y este documento.
.gitignore incorpora unicamente la carpeta de respaldos locales.

## Validacion real de este bloque

- Gradle falla antes de tareas: Unable to establish loopback connection.
  No se genero un APK nuevo de fase 3 desde este entorno.
- Compilacion directa Kotlin 2.2.10 + plugin Compose y bibliotecas locales:
  pasan los ocho archivos de produccion, el test JVM y los dos archivos de
  instrumentacion nuevos. Es una comprobacion parcial con clases previas del
  resto de la app; no sustituye Gradle, KSP, empaquetado ni APK androidTest.
  Registro: build/phase3-check/compile-result.txt.
- JUnit directo, fuentes actuales: OK (16 tests), 11 de SongIdentityTest y
  cinco de PlaybackPositionTest. Registro: build/phase3-unit-check/test-result.txt.
- Escritos cuatro tests Compose de controles/estados y uno de conexion y
  liberacion idempotente real de MediaController. Ninguno ejecutado.
- No se probaron visualmente los nuevos componentes ni audio en dispositivo.

## Bloques siguientes

### Validacion posterior y siguiente intervencion propuesta

Usuario confirma que compila y el APK funciona correctamente. XML Gradle:
SongIdentityTest 11, PlaybackPositionTest 5, ExampleUnitTest 1; cero fallos,
errores u omitidos. Timestamps UTC 2026-10-12T02:22:36-37. APK app y tests
regenerados el 2026-10-11 a las 21:22 America/Bogota. Los 41 instrumentados
nuevos estan compilados; no se registra ejecucion de instrumentacion.
El bloqueo anterior corresponde al entorno del agente. No atribuir resultados
individuales a foco, auriculares, temporizador, TalkBack o rotacion.

Diagnostico confirmado para el siguiente bloque (sin modificar codigo):
- AudioFxScreen accede a playerHandler.equalizer, pero setupAudioEffects no
  tiene consumidores. EqualizerHandler.initEqualizer tampoco tiene llamadas.
- Los sliders bassStrength y virtualizerStrength solo actualizan estado Compose;
  no llaman al motor. El switch permite indicar activacion con equalizer nulo.
- No concluir falta de soporte del dispositivo porque no se inicializo el efecto.
- crossfadeDurationMs se asigna pero no se usa para una mezcla entre pistas.
- isHiRes mezcla extension lossless, sampleRate y bitrate: etiqueta por revisar.

Propuesta de fase 3B: efectos propiedad del servicio y conectados a su sesion
real de audio, estado observable de soporte/error/activacion, controles operativos
y panel Material 3 renovado. Mantener DSP apagado por defecto y ganancias
neutras; controles sin soporte deshabilitados con explicacion. Disenar contrato
servicio/controlador antes de editar, sin dependencias ni cambios Room.
Archivos candidatos: SoundFlowMediaService.kt, AudioPlayerHandler.kt,
AudioFxScreen.kt y modelo de estado/pruebas asociados. Revisar usos antes de
consolidar EqualizerHandler; no borrar implementaciones sin autorizacion.
Despues revisar etiquetas de calidad y transiciones, documentar limites y
validar casos de reproduccion pendientes antes del cierre de fase 3.

- Revisar AudioFX y controles reales sin habilitar procesamiento nuevo por defecto.
- Evaluar gapless y transiciones sobre formatos/dispositivos reales. El fade
  manual actual no demuestra crossfade entre dos pistas.
- Corregir etiquetas Hi-Res basandose en datos; archivo lossless, decodificacion,
  frecuencia de salida y bit-perfect son conceptos distintos.
- Validar este recorrido antes de extender mejoras visuales a otras pantallas.
- Temporizador sigue perteneciendo al cliente ViewModel, no al servicio:
  se cancela si se destruye definitivamente su propietario. Mientras espera
  fin de pista el contador muestra 1; falta un estado visual especifico.
- Modo letras conserva su disposicion anterior con pesos; verificar fuente
  grande, paisaje y TalkBack. No afirmar accesibilidad completa por cambiar dp.

## Validacion

Compilar app, unit tests y APK androidTest. Probar en el dispositivo del usuario:

1. Reproducir, girar pantalla y volver de segundo plano: misma pista/cola y
   controles sincronizados; no crear conexiones extra por recreacion.
2. Pantalla bloqueada y notificacion: pausa, reanudacion y saltos coherentes.
3. Desconectar auriculares cableados y Bluetooth; probar interrupcion de audio
   de otra app. Registrar API/dispositivo; no suponer igual comportamiento.
4. Quitar acceso a carpeta y reproducir pista afectada: error visible y sin
   bucle de reintentos; recuperar permiso y reintentar de forma controlada.
5. Abrir/cerrar pantalla, saltar rapidamente y cancelar temporizador; volumen
   correcto y ninguna pausa tardia causada por listener anterior.
6. Modo claro/oscuro, fuente grande, paisaje y TalkBack: controles alcanzables,
   textos legibles, scroll normal y controles mini que no abren el panel al pausar.
7. Seleccionar pista al abrir la app, avanzar/retroceder cerca de los extremos,
   y temporizador normal/al final de pista, incluido cancelar despues del plazo.

Instrumentacion de estados, conexion y cierre preparada para cuando el usuario
decida ejecutarla. Automatizar errores reales y temporizador sigue pendiente;
los tests escritos no prueban foco, auriculares, sonido ni cierre del proceso.
No confundir compilacion con pruebas de audio en dispositivo.

## Revision de Git de esta entrega

Rama refactor/soundflow-4, HEAD ce6f621. git diff --check pasa y se revisaron
git diff --stat y git status --short. Diferencia acumulada respecto a HEAD:
22 archivos seguidos modificados (1085 inserciones, 278 eliminaciones),
ocho archivos nuevos de fuentes/tests/documentacion, y cinco imagenes/informes
personales sin seguimiento conservados. El stat no cuenta archivos nuevos.
Sin archivos preparados, commits ni push nuevos. Sin diferencias adicionales
en MusicDatabase, entidades ni configuracion de build/dependencias. El respaldo
local ignorado contiene el estado anterior a los cambios de fase 3.

## Referencias primarias

- [MediaSessionService y reproduccion en segundo plano](https://developer.android.com/media/media3/session/background-playback).
- [Configuracion de ExoPlayer y ciclo de MediaController](https://developer.android.com/media/implement/playback-app).

Las referencias vigentes orientan el diseno; verificar cada API frente a
Media3 1.5.1 del proyecto antes de editar, sin asumir compatibilidad por version.
