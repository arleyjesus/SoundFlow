# 4.0.1 — actualizaciones automáticas y novedades una vez

## Objetivo y diagnóstico (2026-11-02)

Primero de los tres asuntos solicitados por el usuario. Los otros dos ajustes
todavía no se han definido y no se implementan en este bloque.

Confirmado: 4.0.0 consultaba GitHub una vez al crear MainAppStructure, no al volver
del segundo plano, y podía volver a avisar al reiniciar. ChangelogDialog existía
pero no tenía consumidor. Ya existían descarga, firma y comprobación manual.

## Implementación

VersionNoticeStore utiliza el DataStore de ajustes existente con dos claves nuevas:
máximo código instalado presentado y máxima versión estable disponible presentada.
Claims atómicos impiden duplicados concurrentes, reaperturas y avisos de versiones
anteriores tras rollback. No se escriben al restablecer Apariencia ni afectan Room.

VersionNoticeCoordinator se conserva en LibraryViewModel. Separa novedades locales
y actualización remota, difiere el aviso remoto hasta cerrar las novedades y no
marca ese aviso remoto antes de poder ofrecerlo. La información local es incluida
en el APK y funciona sin Internet. Novedades incluidas deben actualizarse al preparar
cada versión; no se usa texto de una release diferente a la instalada.

VersionNoticesHost consulta al alcanzar RESUMED y cada cinco minutos mientras la
app está visible. Un intervalo mínimo de 60 segundos evita ráfagas por cambios
rápidos de pantalla. Al salir se cancela el bucle. Al desactivar actualizaciones,
descarta pendientes y resultados de una consulta anterior. Sin trabajo de sondeo
en segundo plano, nuevas dependencias, FCM, permisos o servicios adicionales.

La red no mantiene bloqueadas las acciones de cerrar ni cambiar privacidad.
La comprobación manual permanece disponible y registra el aviso presentado para
que no aparezca después otro automático de la misma versión. Se puede consultar
manualmente varias veces: solo se limita la presentación automática.

Semántica: como máximo un aviso automático por versión con los ajustes conservados.
Se registra antes de ofrecer la ventana. Cerrar el proceso justo entre escritura
y renderizado podría consumir el aviso sin verlo; la consulta manual sigue accesible.
No se promete entrega instantánea ni notificación con la app cerrada. Rotación
conserva el coordinador; proceso nuevo respeta el estado persistente. Borrar datos
de la app también borra este registro. Se conserva la política actual de backup.

Preparada fuente 4.0.1/código 42. Nombre soundflow_database, esquema Room 2,
entidades, identificadores, favoritos/playlists, dependencias y SDK conservados.
Archivos principales: MainActivity, LibraryViewModel, SettingsRepository,
VersionNoticeStore, VersionNoticeCoordinator, VersionNoticesHost, AppNavigation,
SettingsScreen, ChangelogDialog y app/build.gradle.kts.

## Validación

Pruebas nuevas de persistencia/reapertura, código instalado nuevo, rollback,
normalización de tag, rechazo de RC, claims concurrentes, restablecer Apariencia,
consulta sin red, cola de ventanas, cierre durante las novedades, intervalo de
consulta, reintento tras fallo, privacidad durante red pendiente y búsqueda manual.
Los casos del coordinador usan respuestas inyectadas: no simulan una instalación
real ni certifican el diálogo Android. Resultados de ejecución se anotan al cerrar.

Ejecución final: 163 fuentes Kotlin main/test/androidTest comprobadas con artefactos
locales; 164 pruebas JVM directas correctas (151 previas y 13 nuevas). Incluye
reapertura en disco, claims concurrentes, fallo transitorio, descarte de resultados
tras desactivar red y ausencia de duplicado después de una consulta manual.
Logs: build/phase5-design-check/notices-kotlin.txt, notices-junit.txt y notices-gradle.txt.
La compilación directa usa recursos/configuración generados existentes; no sustituye
Gradle/KSP/empaquetado/Lint y no genera un APK. Cambios de formato posteriores no
modifican la lógica comprobada. Instrumentación no ejecutada.

API pública comprobada el 2 de noviembre: latest devuelve v4.0.0 estable,
no borrador, un APK SoundFlow-4.0.0.apk y notas no vacías. Esta comprobación verifica
el contrato de datos publicado; no prueba por sí sola el recorrido en el teléfono.

Gradle intentado: assembleDebug, testDebugUnitTest, assembleDebugAndroidTest y
lintDebug offline. Bloqueado antes de tareas por
java.io.IOException: Unable to establish loopback connection. No se obtuvo un
APK 4.0.1, informe Lint nuevo o ejecución Android desde el agente.

## Prueba final en dos entregas

1. Ejecutar tools/Validate-SoundFlow.ps1 en el entorno del usuario. Generar APK
   release 4.0.1 desde Android Studio con la clave existente y contraseña recordada.
2. Verificar versión 4.0.1/código 42, firma original y SHA-256. Publicar únicamente
   ese APK nuevo con tools/Publish-SoundFlowRelease.ps1 y estas notas 4.0.1. No
   reutilizar el APK 4.0.0. La release sigue pendiente mientras no exista artefacto.
3. Instalar conservando datos y misma firma. En el primer inicio deben verse
   versión/novedades; cerrar y reabrir no debe repetirlas, tampoco sin conexión.
4. Después, abordar los otros dos ajustes que indique el usuario y preparar una
   versión superior (4.0.2/código 43 propuesto). Con 4.0.1 instalado, publicar 4.0.2
   y abrir/volver a SoundFlow con Internet: verificar aviso con versión y notas.
   Si hubo una consulta hace menos de un minuto, esperar el sondeo de cinco minutos
   o utilizar la consulta manual. Descartar, reabrir y comprobar que no se repite.
5. Buscar desde Ajustes, descargar, conceder permiso si procede e instalar.
   Verificar las novedades de 4.0.2 una vez y datos conservados. Repetir con la
   consulta en línea desactivada para comprobar que no hay solicitudes automáticas.

Un 4.0.0 instalado puede detectar el nuevo APK, pero no adquiere la política de
avisos nuevos hasta instalar 4.0.1. Una instalación debug no admite el certificado
de producción; no desinstalar ni borrar datos para sortear un conflicto de firma.

Git inicial: refactor/soundflow-4 en 5dd0195. Dos perfiles .dm generados y cinco
archivos personales sin seguimiento conservados, sin incluir en los cambios.

## Entrega local y bloqueo de GitHub

Implementación guardada en commit local d5522b9. El push recibió HTTP 403:
Permission to arleyjesus/SoundFlow.git denied to ArleyJ16. La cuenta registrada
por Credential Manager figura como arleyjesus, pero seleccionar ese usuario solo
para el comando devolvió el mismo rechazo. No se borraron credenciales ni se
modificó la configuración global. Se necesita reautenticación con acceso al
repositorio antes de subir/publicar. La referencia remota observada sigue en
5dd0195; 4.0.1 no está publicada. Git diff --check pasa; se conservan ambos
perfiles .dm y los cinco archivos personales. Compilación firmada y prueba Android
siguen pendientes, además de este bloqueo de autenticación.
