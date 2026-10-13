# Preparación de fase 7: revisión de lanzamiento

Fecha: 2026-10-12. Revisión inicial, sin release firmado ni publicación.
La app conserva versionName 3.7.0, versionCode 37, minSdk 24 y targetSdk 35.
La numeración del roadmap no modifica automáticamente la versión distribuida.

## Comprobaciones realizadas y correcciones

Actualización: controles persistentes para imágenes, letras y actualizaciones
implementados y conectados a consultas activas; nuevo Prepare-SoundFlowRelease.ps1
para candidato local e informes, sin firmar/publicar. 137 fuentes Kotlin y 98 JVM
directos correctos; Gradle nuevo bloqueado por loopback. [Detalles del bloque](17-FASES-6-Y-7-IMPLEMENTACION-ESENCIAL.md).
Los hallazgos siguientes conservan el diagnóstico previo a estos controles.

- Revisados Manifest, dependencias declaradas, reglas de backup y llamadas de red.
- GitHubUpdateChecker contenía un repositorio de ejemplo, descarga sin límites
  ni cierre explícito y comparaba versiones con !=. Ahora consulta el repositorio
  declarado del proyecto, usa BoundedHttpReader (256 KiB), propaga cancelación,
  omite borradores/previews y compara tres componentes numéricos estables.
- La acción abre la página oficial de la publicación en el navegador. No elige
  automáticamente el primer asset ni descarga/instala un APK desde la app.
  Ausencia de release, respuesta inválida, límite de API o red fallida no impide
  escuchar. Cuatro JVM cubren comparación; recorrido real GitHub/Android pendiente.
- No se cambió nombre/versión/esquema Room, dependencia, permiso ni firma.
  Se comparan once archivos protegidos con el respaldo del APK aceptado.
- tools/Validate-SoundFlow.ps1 prepara compilación, JVM, lint, dependencias,
  hashes y revisión Git. No ejecuta instrumentación ni publica nada.

## Privacidad y permisos: hallazgos del código

| Área | Situación observada / pendiente |
|---|---|
| Audio Inspector | Lectura a demanda de URI; sin envío de audio ni escritura de biblioteca |
| Imágenes de artistas | Consulta Deezer por nombre de artista; caché local acotada. Informar y ofrecer control de red en una intervención de privacidad |
| Letras | Consulta LRCLIB por artista/título cuando se abre Letras y no hay letra local; sin subida del audio |
| Actualizaciones | Consulta GitHub al iniciar; servicio remoto recibe datos normales de conexión. No instala automáticamente |
| Permisos | READ_MEDIA_AUDIO, READ_EXTERNAL_STORAGE hasta 32, notificaciones, foreground playback, Internet y Bluetooth declarados; revisar necesidad y solicitudes reales en la matriz Android |
| Backup Android | allowBackup=true y reglas aún de plantilla. Diseñar exclusiones y restauración con permisos SAF antes de cambiar esta política |
| Biblioteca y Room | No hay recreación destructiva. Compatibilidad histórica requiere fixtures reales; tests sintéticos no prueban todos los upgrades |

No se afirma que la aplicación sea totalmente offline, que no haga consultas
externas o que el backup pueda restaurar acceso a archivos en otro teléfono.
No se modificó una política de backup ni se solicitó ningún permiso adicional.

## Puertas de lanzamiento aún abiertas

Actualización posterior: el usuario acepta el último APK y cierra fases 1–5,
excluyendo las pruebas inaccesibles al agente. Instrumentación, perfilado y matriz
visual/accesibilidad exhaustiva de esas fases no se trasladan automáticamente
como bloqueos de lanzamiento. La lista siguiente conserva la propuesta previa;
sus verificaciones inaccesibles quedan como referencia opcional, no ejecutadas.
Las decisiones reales de alcance, datos, privacidad, firma y publicación de 6/7
se mantienen separadas de esa exclusión; no hay autorización nueva de publicación.

1. Base verificada con Gradle: app/androidTest, 88 JVM, Lint sin errores e informes
   resueltos revisados en build/validation/20261012-163921-UTC. Regenerar tras los
   tres ajustes finales de UI; intento del agente bloqueado por loopback.
2. Probar APK nuevo y matriz visual/funcional documentada. Instrumentación
   pendiente, no confundir su compilación con ejecución.
3. Compatibilidad al menos en dispositivo principal Android 16, Redmi identificado
   y versiones relevantes desde Android 7. Notificaciones, SAF, reproducción
   en segundo plano, auriculares, foco y DSP con resultados por caso.
4. Medir memoria y cuadros con efectos activos/inactivos, biblioteca real y
   repetición prolongada; no extrapolar rendimiento desde pruebas JVM.
5. Diseñar/probar migraciones históricas y backup/restore; completar controles
   de privacidad para servicios externos antes de comprometer garantías públicas.
6. Elegir alcance final de fase 6, versión, canal, firma de release y distribución;
   generar artefacto firmado y validar instalación/actualización conservando datos.
7. Revisar diff y autorizar cada commit/publicación. Sin push, tag, release o
   modificación de main por el mero hecho de terminar un bloque local.

El trabajo preparatorio de esta fase está iniciado. Fases 6 y 7 no están cerradas:
hardware, validación Android, decisiones de persistencia y publicación siguen
siendo requisitos reales, no tareas que una compilación pueda acreditar.
