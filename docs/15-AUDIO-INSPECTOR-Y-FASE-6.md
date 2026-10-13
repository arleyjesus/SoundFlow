# Audio Inspector y alcance pendiente de fase 6

Fecha: 2026-10-12. Base compilada con Gradle y APK aceptado en general.
La captura aportada por el usuario confirma apertura del panel para «11 PM»:
audio/opus, 48000 Hz, 2 canales, bits/bitrate no disponibles y clasificación
con pérdidas. Los campos de salida siguen explícitamente sin medir/verificar.
La captura no permite conocer bitrate, procedencia, calidad audible, versión de
la app ni formato de otras canciones; no se infiere fidelidad a partir del peso.
Pruebas Android automatizadas y matriz de formatos conocidos siguen pendientes.

## Problema confirmado

El código marcaba Hi-Res por extensión FLAC/WAV, frecuencia >=48 kHz o bitrate
>1411 kbps. Eso confundía contenedor, compresión, resolución y salida efectiva.
El flag Room histórico no contiene profundidad de bits y no es evidencia fiable.

## Implementación

Reproductor → Opciones → Información del archivo abre Audio Inspector. El callback
pasa Song al ViewModel y al repositorio; AndroidAudioInspector consulta la URI en
IO y serializa inspecciones. No escribe datos, cambia DSP ni decodifica la canción.
Cerrar/cambiar canción cancela el consumidor y evita presentar un resultado viejo.
Las llamadas nativas/proveedores pueden bloquear hasta retornar; no se promete
interrupción inmediata ni un timeout estricto del proveedor SAF.

- FLAC nativo: lee solo 42 bytes iniciales, valida firma/STREAMINFO y extrae
  frecuencia, canales y bits. No examina portadas, frames, MD5 ni procedencia.
- Otras fuentes: MediaExtractor devuelve la primera pista de audio y campos
  disponibles. La profundidad PCM solo se toma de audio/raw; no se infiere
  profundidad del archivo a partir del formato de salida de un códec comprimido.
- FLAC encapsulado, ALAC y PCM float pueden carecer de profundidad verificable.
  Campos desconocidos se muestran como «No disponible». No usa extensión,
  tamaño del archivo ni bitrate para certificar alta resolución.
- Convención conservadora propia, explícita en pantalla: codificación sin
  pérdidas, más de 16 bits y más de 48 kHz. No es certificación universal Hi-Res.
- El panel distingue fuente de decodificador activo, salida Android/DAC y
  bit-perfect. Estos tres últimos figuran sin medir/verificar.
- El repositorio deja de propagar el flag Hi-Res histórico. Importadores nuevos
  guardan false mientras no conozcan la profundidad; las filas antiguas se
  conservan sin reescritura masiva ni migración. MediaStore sigue sin activarse.

Archivos: domain/playback/AudioInspection.kt; data/local/util/AndroidAudioInspector.kt
y FlacStreamInfo.kt; AudioRepository/Impl; LibraryViewModel; AudioInspectorDialog.kt;
FullPlayerSheet.kt y MainActivity.kt; ambos scanners. Ninguna entidad modificada.

## Pruebas y recorrido

Diez JVM nuevos cubren clasificación conservadora, metadatos ausentes/inválidos,
cabecera truncada/falsa, lectura acotada y FLAC 16/44,1 frente a 24/96.
Android: fixture WAV PCM aislado y no modificado, URI ausente/remota,
cancelación al cambiar canción, flag histórico sin reescritura; recorrido de menú
existente actualizado para comprobar identidad y lectura solo al solicitarla.
Estas pruebas Android están escritas, sin ejecución registrada.

En el APK nuevo: abrir un FLAC 16/44,1, otro 24/96 y un MP3 conocidos; contrastar
valores con sus metadatos de origen. Un FLAC de calidad CD no debe indicar alta
resolución. Probar URI inaccesible y cambiar canción/cerrar durante carga. No
usar el peso como referencia ni interpretar «sin pérdidas» como prueba de que
una grabación nunca fue convertida desde MP3. No afecta reproducción/favoritos.

## Viabilidad de las siguientes funciones

Actualización: Smart DJ local por metadatos y vista de posibles duplicados
implementados como primer bloque, sin red, escrituras ni análisis del audio.
Controles de privacidad y preparación release en fase 7. Estado real, pruebas
y límites en [documento 17](17-FASES-6-Y-7-IMPLEMENTACION-ESENCIAL.md).

| Propuesta | Estado real y condición para avanzar |
|---|---|
| Audio Inspector Pro | Base de fuente implementada; faltan telemetría del decoder/ruta y pruebas de formatos/hardware |
| Copia/exportación de playlists y favoritos | Viable con documento SAF versionado, previsualización y restauración transaccional; diseñar reidentificación entre dispositivos antes de importar |
| Replay privado | Requiere política de escucha, retención/borrado y persistencia; diseñar y aprobar migración si se usa Room |
| Smart DJ | Sugerencias locales explicables y cola implementadas; mezcla real no implementada |
| Biblioteca inteligente | Candidatos por URI/metadatos implementados sin borrado ni fusión; hash completo no implementado |
| Perfiles por dispositivo | Viable como preferencias, pero identidad de salida, cambios de ruta y soporte DSP necesitan pruebas Android |
| USB DAC Studio | Condicionado a API/dispositivo/DAC; no garantiza control exclusivo ni bit-perfect |
| Transferencia Wi-Fi | Necesita emparejamiento, consentimiento, integridad del archivo, cancelación y diseño de seguridad/permisos |

Las filas pendientes no son implementaciones terminadas. Fases 1–5 ya aceptadas;
el siguiente paso es compilar y probar el bloque nuevo de fases 6/7. Persistencia
de Replay/copias necesita diseño antes de cambios de esquema o dependencias.

## Referencias técnicas

- [Android MediaExtractor](https://developer.android.com/reference/android/media/MediaExtractor#getTrackFormat(int)):
  los campos disponibles dependen del formato y de Android; consulta de metadatos
  no equivale a medir la ruta que usa Media3 durante reproducción.
- [FLAC STREAMINFO, RFC 9639 §8.2](https://www.rfc-editor.org/rfc/rfc9639.html#section-8.2):
  disposición de frecuencia, canales y bits en la cabecera nativa.
- [Android: atributos de mezclador preferidos](https://source.android.com/docs/core/audio/preferred-mixer-attr):
  el soporte de modos de salida exige condiciones de plataforma y hardware.
