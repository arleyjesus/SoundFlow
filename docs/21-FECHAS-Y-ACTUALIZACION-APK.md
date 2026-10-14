# Fechas de biblioteca y actualización APK — 2026-10-13

## Objetivo, diagnóstico y alcance

Corregir el orden reciente y permitir actualizar desde la app mediante Releases
de GitHub. El usuario autorizó obtener/publicar un APK cuando sea posible.

Problemas confirmados: LibraryOrder ordenaba por ID Room; este ID depende del
recorrido de importación, no del momento de descarga. SongEntity no tiene fechas.
GitHubUpdateChecker solo abría la página de la release; no descargaba/instalaba.

No se modifican entidades, identificadores, favoritos, relaciones, nombre de base
soundflow_database ni versión Room 2. No se añaden dependencias o SDK.

## Orden implementado y límites

- Song incorpora addedAtMs transitorio, sin columna Room. LibraryDateStore consulta
  en IO DATE_ADDED (segundos convertidos a milisegundos) si hay una URI MediaStore
  disponible, incluida la conversión del documento que permita Android 10+.
- Si MediaStore no responde con fecha válida, consulta COLUMN_LAST_MODIFIED de SAF.
  No se identifica una ruta SAF con un ID MediaStore ni se cambia la URI de acceso.
- Fechas válidas positivas y no futuras. Ausencia/permiso perdido/proveedor sin
  soporte deja cero; no se inventa una fecha a partir del ID ni la hora del escaneo.
- Cachea el primer resultado en library_dates.preferences_pb, por hash de identidad
  canónica. Solo persiste fechas válidas: las ausentes pueden recuperarse en otro
  snapshot tras restaurar permisos. Evita consultas de proveedor al editar metadatos y
  evita que una modificación posterior de etiquetas cambie el orden cacheado.
  Alias SAF equivalentes comparten fecha; no se combinan canciones Room.
- Orden reciente por fecha descendente e ID como desempate; antiguo ascendente.
  Desconocidos al final en ambos sentidos. Inicio consume también la lista reciente.
  Biblioteca conserva los otros filtros y envía su selección ordenada al reproductor.
- La escritura de fechas solo ocurre después de leer el snapshot completo;
  cancelación no guarda resultados parciales. Un fallo IO del caché no impide abrir Room.
- La nueva preferencia es metadata auxiliar; no supone una migración de biblioteca.
  Su almacenamiento es proporcional a las identidades consultadas. No se elimina
  música por fallos de fechas ni se cambia el escáner o la detección de nuevos archivos.

No todos los proveedores exponen la fecha de descarga/creación. Última modificación
puede diferir, especialmente al copiar/restaurar archivos. Las fechas ya cacheadas
no se recalculan en cada escaneo. Sin datos del proveedor no se puede reconstruir
el orden histórico exacto. No prometer que ID, tamaño o fecha de etiquetas lo resuelven.

Contratos: [MediaStore DATE_ADDED](https://developer.android.com/reference/android/provider/MediaStore.MediaColumns#DATE_ADDED),
[SAF LAST_MODIFIED](https://developer.android.com/reference/android/provider/DocumentsContract.Document#COLUMN_LAST_MODIFIED).

## Actualizador implementado

- Sigue releases estables, publicadas, de arleyjesus/SoundFlow. Subir código a Git
  no crea una actualización: debe existir una release con versión superior y APK.
- Ajustes permite buscar ahora, respetando el control de privacidad. Distingue
  fallo de consulta de ausencia de una nueva versión. Comprobación automática conservada.
- Selecciona un único APK oficial subido (máximo 150 MiB). Cero/múltiples APK:
  ofrece la página sin adivinar arquitectura o splits.
- Descarga iniciada por el usuario, en IO, a caché privada con progreso, cancelación,
  180 segundos de presupuesto y máximo 150 MiB incluso sin Content-Length.
  HTTPS y redirecciones limitadas a los hosts oficiales usados por assets de GitHub.
- Compara SHA-256 del asset cuando GitHub lo informa; siempre comprueba paquete,
  código superior, nombre de versión y certificados coincidentes con la instalación.
  Android comprueba e instala finalmente el paquete; no se afirma instalación silenciosa.
- FileProvider privado expone solo cache/updates. Permiso REQUEST_INSTALL_PACKAGES
  y ajuste por aplicación desde Android 8; API 24–25 usa el instalador del sistema.
  El usuario vuelve a pulsar Instalar APK después de conceder el permiso. Una firma
  distinta se rechaza con mensaje y sin recomendar desinstalar/borrar datos.
- Retiene como máximo dos candidatos previos propios y la descarga actual; los
  parciales fallidos se eliminan. No se accede al resto de archivos personales.
- No se soporta rotación de certificados automáticamente; requiere diseñar su
  compatibilidad explícita. El flujo Android real sigue pendiente de prueba.

## Publicación, firma y bloqueo real

Versión fuente preparada: 4.0.0 / código 41. No equivale a versión publicada.
Última release pública consultada: v3.7.0, asset app-release.apk.

Certificado público del APK v3.7.0 verificado con apksigner:
31cbd14903f1fb7fe086efe8650d2465fbf7cfb6b9ffeaeef7adf01e1d91419b.
APK debug local: certificado Android Debug distinto, SHA-256
159fa6ca3fbe495c75851850e1b0a9930aa9149f2aa67801f7967c60249612b6.

mis_claves.jks existe (2550 bytes), PKCS12, alias soundflow_key visible sin
contraseña. Sin contraseña no se verificó la integridad ni se pudo leer su cadena
de certificados; no se afirma todavía que corresponda a la release pública.
No se creó, reemplazó o modificó ninguna clave. No pedir contraseñas en el chat.

signing.properties.example documenta la configuración local; signing.properties
queda ignorado. Gradle usa esa configuración solo cuando existe; no sustituye la
firma de release por debug. La configuración local real todavía no se creó porque
faltan las contraseñas originales. Si se perdieron, no se pueden deducir del APK.

El usuario no recuerda la contraseña. La revisión limitada de .idea/workspace.xml
encontró KEY_STORE_PATH, KEY_ALIAS y REMEMBER_PASSWORDS=true; sin contraseñas en
el archivo. Android Studio podría conservarlas en su almacén seguro, pero no se
comprobó ni se accedió a ese almacén. Alternativa: Build → Generate Signed App
Bundle / APK → APK, seleccionar la clave existente y comprobar si los campos
guardados permiten generar el APK. No crear otra clave. El APK resultante debe
pasar la revisión del certificado antes de publicar. No se modificó .idea.

Prepare-SoundFlowRelease.ps1 requiere la configuración local y compila release,
JVM/Lint y snapshot/hash. Publish-SoundFlowRelease.ps1 revisa por defecto, sin
publicar: árbol de fuentes limpio, rama, paquete, versión, código y certificado
original. Con -Publish autorizado, usa las credenciales Git existentes en memoria,
comprueba HEAD remoto y crea primero un borrador, carga un único APK y publica
solo tras comprobar tamaño/digest. No sobrescribe releases existentes.
Sintaxis revisada; no se ejecutó el flujo de publicación ni se creó borrador remoto.

Bloqueos actuales: Gradle del agente falla antes de tareas con
java.io.IOException: Unable to establish loopback connection; y falta configurar
las contraseñas de la clave original. No se generó/publicó APK 4.0.0 desde aquí.
El APK local anterior es 4.0.0-rc.1/código 40; no contiene esta intervención.

Una instalación debug solo admite actualizaciones con su misma firma. Una release
de producción firmada como v3.7.0 puede actualizar las instalaciones de esa cadena,
pero no la instalación debug local. Resolverlo sin desinstalar exige conservar
canales de firma compatibles; no se implementó transferencia de datos entre firmas.

## Validación y pasos finales

- Kotlin directo: 158 fuentes main/test/androidTest correctas con artefactos locales.
- JVM directos: 151 correctos. Nuevos casos: orden por fecha pese a IDs opuestos,
  empates/desconocidos, reapertura, alias SAF, cancelación, URL/downgrade,
  identidad, versión y firma del paquete.
- XML Manifest/FileProvider y sintaxis de los scripts PowerShell revisados.
- Gradle intentado: assembleDebug, testDebugUnitTest, assembleRelease, lintDebug,
  offline. Falló antes de tareas por loopback; no APK/KSP/Lint nuevo aprobado.
- No se ejecutaron instrumentados, instalación, permisos, descarga ni rendimiento
  real Android. La comprobación directa no sustituye ese recorrido.
- Registros ignorados: build/phase5-design-check/update-*.txt;
  certificados públicos consultados en build/release-review.

Prueba final: compilar con tools/Validate-SoundFlow.ps1. Actualizar la biblioteca
desde Carpetas, ordenar recientes/antiguas, comparar la descarga de hoy y fechas
del administrador de archivos. Comprobar cola, favoritos/playlists y reapertura.

Para release, completar signing.properties localmente con las contraseñas originales
y alias soundflow_key; ejecutar tools/Prepare-SoundFlowRelease.ps1. Con APK firmado
y validado, revisar antes de publicar:

    .\tools\Publish-SoundFlowRelease.ps1 -ApkPath app\build\outputs\apk\release\app-release.apk -NotesFile docs\RELEASE-NOTES-4.0.0.md

Solo añadir -Publish tras comprobar la revisión y el APK. El script requiere el
commit de fuentes guardado y subido; no hace commits, merges o push automáticamente.

Comprobar actualización desde una versión anterior de la misma firma: buscar,
descargar, conceder permiso, instalar, reabrir y verificar datos. Rechazar firma
distinta, APK corrupto, cancelación/desconexión y falta de espacio. Una versión
antigua que solo abre GitHub necesitará instalar una vez el nuevo actualizador;
las actualizaciones posteriores podrán usar el recorrido integrado.
