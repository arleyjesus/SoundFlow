# Roadmap de SoundFlow 4.0

## Estado vigente al 2026-10-12

Fases 1–5 cerradas para el alcance de desarrollo acordado. El usuario confirma
que probó el último APK y funciona correctamente, y excluye del cierre las pruebas
que el agente no puede ejecutar: instrumentación Android, mediciones de memoria/
cuadros y matriz visual/accesibilidad exhaustiva. No se presentan como aprobadas.
Se conservan los tests escritos y resultados reales: 88 JVM correctos, build/Lint
anterior verificado y comprobación directa posterior de 131 fuentes Kotlin.
Esta aceptación no certifica todos los dispositivos ni salida bit-perfect.
Las casillas pendientes y bloqueos de las secciones anteriores son historial;
las verificaciones excluidas no bloquean ni se trasladan automáticamente como
condición de avance a fases 6/7. Estas fases continúan abiertas; no hay release.
[Cierre técnico](14-CIERRE-TECNICO-FASES-4-Y-5.md),
[Inspector/viabilidad](15-AUDIO-INSPECTOR-Y-FASE-6.md),
[preparación de lanzamiento](16-PREPARACION-FASE-7.md).

## Fase 0 - Documentacion y organizacion

- [x] Crear documentacion tecnica
- [x] Documentar auditoria y riesgos
- [x] Definir fases y criterios de cierre
- [x] Alinear README con las fases y el estado real de validacion
- [x] Crear commit de documentacion (a69bd59, ya existente)

## Fase 1 - Persistencia y estabilidad

Entrega de desarrollo compilada y autorizada por el usuario el 2026-10-11.
La validacion exhaustiva sigue pendiente: 16 tests instrumentados aplazados
y comprobacion manual del ultimo ajuste de Room. No se afirma validacion completa.

- [x] Preparar favoritos persistentes en Room
- [x] Preparar renombrado de playlists sin alterar createdAt
- [x] Preparar limpieza de relaciones al eliminar playlists
- [x] Registrar compilacion reportada por el usuario de los cambios iniciales
- [ ] Probar favoritos despues de reiniciar
- [ ] Probar creacion, renombrado y eliminacion de playlists
- [ ] Probar integridad de datos
- [x] Revisar estrategia de migraciones Room y documentar origenes incompatibles
- [x] Retirar la recreacion destructiva sin cambiar nombre, version ni entidades
- [x] Escribir cuatro pruebas de compatibilidad con la configuracion de produccion
- [x] Registrar aceptacion manual general del APK de fase 1A por el usuario
- [x] Compilar app y APK de tests con la proteccion final de apertura
- [ ] Comprobar manualmente el APK con la proteccion final de apertura
- [x] Autorizar entrega de fase 1 con las limitaciones de validacion registradas
- [x] Entregar y publicar desarrollo de fase 1: commit 7c19fd4 en refactor/soundflow-4

La instrumentacion permanece aplazada por preferencia del usuario; el cierre
debe registrar esa limitacion. Commit y push requieren autorizacion separada.

### Fase 1A - Favoritos y playlists (2026-10-10)

Alcance implementado y compilacion posterior correcta reportada por el usuario.
Prueba manual general satisfactoria reportada el 2026-10-11; instrumentacion pendiente.

- [x] Conservar favoritos Room, UPDATE de nombre y eliminacion transaccional existentes
- [x] Aplicar la seleccion completa de canciones en una transaccion del DAO
- [x] Separar adicion individual de sustitucion de la seleccion
- [x] Conectar el menu de canciones por ID, incluyendo favoritos y adicion a playlist
- [x] Evitar nuevas relaciones desde estas operaciones si la playlist ya no existe
- [x] Corregir el paquete esperado en el test instrumentado de plantilla
- [x] Escribir 10 pruebas de persistencia/integridad y 2 de interfaz Compose
- [x] Compilar app y APK de pruebas: BUILD SUCCESSFUL compartido por el usuario
- [ ] Ejecutar las pruebas nuevas y la regresion existente
- [x] Registrar prueba manual general satisfactoria del APK reportada por el usuario
- [ ] Completar registro por caso y verificaciones internas de integridad
- [ ] Revisar los cambios con el usuario antes de cualquier commit o publicacion

Antecedente del entorno del agente: Gradle fallo antes de compilar con
`Unable to establish loopback connection`; la causa detallada es
`java.net.SocketException: Invalid argument: connect` en Java NIO.

Se mantienen las entidades, version 2, nombre `soundflow_database`
y dependencias. La fase 1 completa sigue abierta. Identidad de canciones,
retirada segura de carpetas y compatibilidad de esquemas historicos
requieren intervenciones posteriores aprobadas.

### Ajuste final de fase 1 - Compatibilidad Room (2026-10-11)

- MusicDatabase comparte constructor con los tests y elimina el fallback
  destructivo. Ante una base incompatible, la apertura falla sin recrearla;
  no se ha implementado una pantalla de recuperacion ni migracion historica.
- Se agregan cuatro pruebas sinteticas: reapertura del esquema actual,
  upgrade y downgrade sin migracion e identidad desconocida en version 2.
  Total: 16 pruebas instrumentadas nuevas, ejecucion aplazada.
- Revision de historial y decisiones en 06-POLITICA-DE-PERSISTENCIA.md.
- Dos intentos de Gradle con acceso a cache fallaron antes de compilar por
  loopback; IPv4 temporal y --no-daemon tampoco resolvieron el problema.
- El usuario confirma BUILD SUCCESSFUL in 1m 14s, 77 tareas (15 ejecutadas,
  62 actualizadas). APK regenerado; informe unitario: 1 test, sin fallos.
- La prueba manual anterior sigue siendo evidencia de fase 1A; falta repetir
  el recorrido sobre el APK con el ajuste final de Room.
- README alineado con las siete fases, sin iniciar cambios de biblioteca.

## Fase 2 - Biblioteca y escaneo

Cierre de desarrollo aceptado por el usuario el 2026-10-11: BUILD SUCCESSFUL
in 16s, 77 tareas (15 ejecutadas, 62 actualizadas), y funcionamiento correcto
del APK final reportado. Once tests de identidad pasan en Gradle; 36 tests
instrumentados compilados, sin ejecutar. Se conservan pendientes de medicion
exhaustiva con dispositivos/proveedores reales. No implica publicacion en Git.

Estado: iniciada por autorizacion del usuario el 2026-10-11.
Se avanza con bloques pequenos de integridad y mejoras de interfaz del mismo
recorrido. Primer bloque implementado y compilado desde la terminal del usuario;
prueba manual satisfactoria reportada por el usuario, instrumentacion aplazada.

- [x] Detectar canciones existentes por URI exacta antes de insertar
- [x] Conservar IDs y etiquetas editadas al reimportar la misma URI
- [x] Evitar duplicados nuevos de URI exacta dentro y entre importaciones
- [x] Implementar comparacion SAF por proveedor/documentId y rechazo de alias entre carpetas; compilacion y aceptacion manual general confirmadas
- [x] Revisar separacion SAF/MediaStore y definir conservacion de duplicados historicos
- [x] Implementar conservacion de datos ante escaneos vacios, parciales y fallidos
- [x] Implementar retirada transaccional con limpieza de favoritos y relaciones
- [x] Revisar MediaStore y SAF: solo SAF participa en la biblioteca activa
- [x] Implementar recuperacion de acceso a la misma carpeta y rechazo de otra seleccion
- [ ] Probar permisos persistentes
- [ ] Probar bibliotecas grandes
- [ ] Publicar fase en GitHub

### Primer bloque de fase 2 - 2026-10-11

- Importacion atomica de canciones nuevas y registro/conteo de carpeta.
  No se eliminan canciones ausentes de un escaneo vacio o parcial.
- URI exacta como identidad minima: no se equiparan documentos SAF distintos
  ni sus IDs con MediaStore. Duplicados preexistentes conservados.
- Solapamiento de URI exacta entre carpetas: se rechaza la importacion,
  conservando la pertenencia y el estado anterior; alias no detectados aun.
- Lectura SAF fuera de la transaccion, validacion de acceso, listado explicito
  del proveedor y cancelacion propagada. Algunos proveedores pueden omitir
  entradas sin error; por eso no se usa la ausencia para borrar canciones.
- Mutex y estado observable en ViewModel para importacion/retirada.
- Pantalla de carpetas con progreso, resultado, conteo, actualizar y confirmar
  retirada. Perfil muestra la version real desde BuildConfig.
- Ocho tests Room y tres Compose escritos y compilados; no ejecutados.
- El intento del agente fallo por loopback. El usuario confirma posteriormente
  BUILD SUCCESSFUL in 42s, 77 tareas (15 ejecutadas, 62 actualizadas).
  APK regenerado; XML unitario: 1 test de plantilla, sin fallos ni errores.
- Sin cambios de entidades, version, nombre Room, dependencias o reproduccion.
- El usuario reporta que el APK funciona correctamente tras los recorridos
  propuestos. No se registraron dispositivo, API ni resultados individuales;
  se conserva como aceptacion manual general del bloque, no de toda la fase 2.
- Publicado con autorizacion del usuario: ce6f621 en origin/refactor/soundflow-4.

### Segundo bloque de fase 2 - Identidad SAF (2026-10-11)

- Comparacion compartida entre DAO y escaner: autoridad + documentId opaco
  para documentos SAF, URI exacta para MediaStore y formatos no reconocidos.
- Conservar la URI almacenada y rechazar la importacion completa ante un
  documento de otra carpeta, aunque cambie el arbol SAF. Sin fusion historica.
- Diez pruebas JVM pasan con Kotlin/JUnit de cache, fuera de Gradle.
  Cuatro pruebas instrumentadas nuevas comprueban alias y permiso denegado;
  no compiladas ni ejecutadas. Gradle bloqueado antes de tareas por loopback.
- Pendiente: compilar APK actualizado, probar carpetas superpuestas y perdida
  de acceso en proveedor real. El APK probado anteriormente no incluye este bloque.
- Cambios guardados localmente, sin nuevo commit ni push. Fase 2 sigue abierta.
- Validacion posterior: el usuario confirma que compila y funciona. XML Gradle
  de SongIdentityTest: 10 tests, cero fallos, errores u omitidos. APK app y tests
  regenerados a las 20:08 America/Bogota. Los 31 tests instrumentados nuevos
  estan compilados, sin ejecutar. Aceptacion manual general sin detalle por caso.
- Siguiente propuesta: recuperacion de acceso SAF y mensajes por carpeta;
  despues revisar la integracion real de MediaStore y probar biblioteca grande.
  No fusionar duplicados historicos sin politica y aprobacion explicitas.

### Ajuste final de fase 2 - Recuperacion y volumen (2026-10-11)

- El error de permiso identifica su URI y muestra accion Restaurar acceso.
  El selector se abre en esa ubicacion; otra URI se rechaza antes de persistir
  permiso o escribir datos. Cancelar conserva el estado. No se cambia el esquema.
- Se leen solo las canciones conocidas de esa carpeta para reutilizar sus
  metadatos durante el recorrido. El DAO vuelve a validar identidad y pertenencia
  en la transaccion; etiquetas e IDs de filas existentes siguen protegidos.
- Revision completa de usos: MediaStoreAudioScanner no tiene consumidores.
  La fase estabiliza SAF; activar MediaStore y fusionar fuentes requiere otro
  diseno. La politica actual conserva duplicados historicos y sus referencias.
- Once pruebas JVM pasan fuera de Gradle, incluida identidad de 10.000 documentos.
  Cinco pruebas instrumentadas adicionales: tres Room y dos Compose. Total 36,
  sin ejecutar; cinco nuevas pendientes de compilar. No es medicion de rendimiento
  Android. Prueba con catalogo y proveedor reales sigue pendiente.
- Gradle del agente falla antes de tareas por loopback. La implementacion final
  queda guardada localmente; no se declara cierre validado ni nuevo APK generado.
- Criterio minimo de cierre: compilar las tres tareas y probar restauracion
  correcta/equivocada/cancelada, reimportacion y referencias en el APK actualizado.
  Registrar aplazamiento de instrumentacion y mediciones exhaustivas.
- Fase 3: diagnostico preparado en 07-FASE-3-MOTOR-DE-AUDIO.md. Implementacion
  del motor despues de validar este ajuste, sin mezclar cambios de ambas fases.

## Fase 3 - Motor de audio

Iniciada con autorizacion del usuario tras la aceptacion de fase 2. Se amplia
el alcance a estilo y diseno interno, empezando por minirreproductor y pantalla
de reproduccion: jerarquia visual, contraste, controles accesibles y estados.
Mantener temas y contratos existentes; seguir con biblioteca/otras pantallas
en bloques posteriores despues de validar este recorrido.

- [x] Diagnostico inicial de AudioPlayerHandler y SoundFlowMediaService
- [x] Implementar propiedad de ViewModel, cierre del cliente y reintento explicito
- [x] Configurar foco/noisy en el servicio, caratulas asincronas y fades coordinados
- [x] Mejorar minirreproductor y panel de reproduccion, estados y controles accesibles
- [x] Ejecutar cinco tests JVM de posicion y once de identidad, fuera de Gradle
- [x] Compilar primer bloque con Gradle; aceptacion manual general del APK reportada
- [x] Implementar efectos del servicio y panel AudioFX renovado, apagados/neutros por defecto
- [x] Ejecutar 12 JVM de politica AudioFX y 16 de regresion, fuera de Gradle
- [x] Compilar fase 3B con Gradle y registrar aceptacion manual general del APK
- [x] Implementar ajuste final 3C: informacion de archivo, espera del temporizador y limites de transiciones
- [x] Compilar APK de 3C/4A y registrar aceptacion manual general del usuario
- [ ] Completar regresion especifica por caso/dispositivo, sin atribuirla a la aceptacion general
- [ ] Completar revision de controladores externos y errores en dispositivo
- [ ] Comprobar MediaSession y segundo plano
- [ ] Revisar audio focus y becoming noisy
- [x] Evaluar ecualizador y efectos; soporte nativo requiere pruebas por dispositivo
- [x] Revisar limites de crossfade y gapless; no prometer mezcla ni continuidad universal
- [x] Documentar limitaciones reales de Hi-Res
- [ ] Publicar fase en GitHub

Fase 3B: [implementacion, archivos y limites](08-FASE-3B-EFECTOS-Y-DISENO.md).
Fase 3C: [desarrollo final y validacion pendiente](09-CIERRE-FASE-3.md).
Desarrollo final de fase 3 aceptado por el usuario con compilacion Gradle posterior:
41 JVM sin fallos y APK app/tests regenerados a las 22:41 Bogota. Instrumentacion,
regresion detallada y publicacion siguen pendientes; fase 4 continua autorizada.

## Fase 4 - Rendimiento, cache, memoria y arquitectura

Estado de cierre de desarrollo (2026-10-12): 88 JVM Gradle, Lint sin errores,
informes resueltos revisados y aceptación general del APK. Auditoría final y
ajustes de visualizador/cabecera en documento 14. Ajustes nuevos pendientes
de APK regenerado; mediciones e instrumentación requieren Android disponible.

- [x] Reutilizar artistas compartidos en Inicio y Brush del visualizador
- [x] Acotar lectura de letras y respuestas de actualizaciones
- [x] Preparar script de captura de memoria/cuadros; ejecución real pendiente

Implementacion esencial 4A/4B compilada con Gradle: 62 JVM sin fallos y APK
actualizado aceptado en general por el usuario. Mediciones y regresion especifica
pendientes. [Cierre y evidencias](12-CIERRE-FASE-4-E-INICIO-FASE-5.md).

- [x] Auditar tecnologias declaradas, rutas activas, caches y limites entre capas
- [x] Implementar primer bloque 4A: portadas muestreadas/atomicas sin cache RAM duplicada
- [x] Compartir songs con controlador y mover transformaciones/lectura de letras fuera de main
- [x] Ejecutar 40 JVM especificas (12 nuevas); comprobacion Kotlin/Compose parcial
- [x] Validar Gradle y APK del bloque 4A: 41 JVM, aceptacion manual general
- [ ] Medir memoria y fluidez en recorrido repetido
- [x] Bloque 4B: limites de cache/red de artistas, streams, busqueda y contrato real

- [x] Compartir consulta/transformacion de canciones en el recorrido activo
- [x] Revisar cache de caratulas y acotar cache/red de artistas
- [x] Revisar asignacion de bitmaps; perfilado de memoria pendiente
- [x] Revisar concurrencia y cancelacion de tareas intervenidas
- [x] Aislar progreso de navegacion y mover busqueda fuera de composicion
- [x] Conectar interfaz real de repositorio y modelos de carpeta sin Room
- [x] Colectar estado UI con ciclo de vida, sin actualizar dependencias
- [x] Incorporar pruebas: 17 JVM nuevos de fase 4; 57 JVM directos pasan
- [x] Validar Gradle y APK del estado final 4B + inicio de fase 5: 62 JVM y aceptacion general
- [x] Revisar informe real de dependencias resueltas; riesgos de alineación documentados sin cambiar versiones
- [ ] Publicar fase en GitHub

## Fase 5 - Diseno integral y personalizacion

- [x] Implementar seguir tema del sistema conservando elección manual anterior
- [x] Restablecer solo preferencias visuales con confirmación
- [x] Combinar animaciones propias con política de Android al volver a primer plano
- [x] Mejorar cabeceras/diálogos con fuente grande; ocho casos parametrizados escritos
- [x] Corregir centésimas y unificar parser de letras local/remoto
- [x] Compilar ajustes de Apariencia e Inspector y registrar aceptación general del APK
- [ ] Validar ajustes posteriores de cierre y completar matriz visual/accesibilidad

Iniciada por autorizacion del usuario: base Apariencia compilada y con aceptacion
manual general del APK. Matriz visual por caso pendiente; fase 4 conserva
pendientes de medicion, instrumentacion e informe de dependencias.
[Alcance de todas las pantallas y Apariencia](11-FASE-5-DISENO-INTEGRAL.md).

Bloque visual del 2026-10-12 implementado: negro puro, grises neutros, mini con
degradado, Perfil/Ajustes y componentes compartidos. Validación posterior:
65 JVM Gradle sin fallos, APK regenerado a las 10:15:34 Bogotá y funcionamiento
general aceptado por el usuario. Matriz visual por caso, accesibilidad,
instrumentación y mediciones pendientes.
[Archivos, límites y recorrido manual](13-FASE-5-REDISENO-VISUAL.md).

- [x] Compilar y registrar aceptación funcional general del APK del rediseño
- [x] Definir superficies, formas y componente de panel compartido
- [x] Revisar tokens, superficies e iconos del recorrido activo; componentes heredados sin llamadas conservados
- [x] Intervenir Inicio, biblioteca, búsqueda, carpetas, playlists y canciones/opciones con roles del tema
- [x] Integrar reproductor, letras, Perfil, Ajustes, AudioFX y diálogos con Apariencia
- [ ] Validar cada pantalla/paleta/modo en Android; la revisión estática no acredita resultados visuales
- [x] Panel Apariencia con paletas e interruptores independientes de animaciones/transparencias
- [x] Persistir preferencias; pruebas JVM de reapertura y combinaciones pasan
- [x] ColorScheme unico y pares de contraste para cuatro paletas en ambos modos
- [x] Consumidores iniciales: navegacion, visualizador y fondo opaco de reproductor
- [x] Extender efectos a crossfades activos, pestañas, letras, selección y paneles intervenidos
- [x] Revisar crossfades activos y política visual propia; animaciones internas de Material/sistema con límites documentados
- [ ] Registrar aceptación por caso de alternativas opacas/sin movimiento, fuente grande y TalkBack
- [x] Incorporar estados de búsqueda, biblioteca y reproducción y mejoras de diálogos/selección de playlists
- [x] Corregir parser compartido y movimiento de letras sincronizadas
- [x] Personalización persistente, seguir sistema y restablecimiento visual aislado
- [ ] Comparar rendimiento con efectos activados/desactivados
- [ ] Publicar fase en GitHub

## Fase 6 - Innovacion

- [x] Implementar Smart DJ local por metadatos con vista previa y reproducción explícita, sin mezcla
- [x] Implementar vista de posibles duplicados por URI/metadatos, sin borrar ni fusionar
- [x] Ejecutar 8 JVM de sugerencias/coincidencias y regresión directa (98 totales con privacidad)
- [ ] Compilar con Gradle y aceptar el APK del bloque nuevo; bloqueo del agente por loopback

Primer alcance y límites en [documento 17](17-FASES-6-Y-7-IMPLEMENTACION-ESENCIAL.md).
Replay, mezcla, perfiles, USB exclusivo, copias y Wi-Fi son propuestas posteriores;
las casillas de ideas no representan funciones comprometidas ni completadas.

- [x] Implementar base Audio Inspector: fuente local a demanda y clasificación conservadora
- [x] Dejar de propagar el flag Hi-Res histórico sin reescribir Room
- [ ] Validar lector Android, fixtures y recorrido del inspector en APK nuevo

Ideas propuestas, sujetas a viabilidad y pruebas:

- [ ] Audio Inspector Pro: informacion tecnica detallada
- [ ] SoundFlow Smart DJ: sesiones y mezclas inteligentes
- [ ] SoundFlow Replay: estadisticas privadas de escucha
- [ ] Biblioteca inteligente: deteccion de duplicados
- [ ] Perfiles de audio por dispositivo
- [ ] USB DAC Studio: explorar capacidades de salida USB
- [ ] Transferencia local mediante Wi-Fi

No prometer bit-perfect, IA avanzada ni USB exclusivo
sin validar las APIs y el hardware requeridos.

## Fase 7 - Lanzamiento

- [x] Controles persistentes de privacidad para imágenes, letras y actualizaciones conectados
- [x] Dos JVM de independencia/reapertura/reset de privacidad; 98 totales directos correctos
- [x] Herramienta Prepare-SoundFlowRelease para candidato local, hashes e informes; sintaxis validada
- [ ] Ejecutar preparación release cuando Gradle funcione; aprobar versión/canal/firma antes de distribuir

- [x] Revisión inicial de permisos, consultas externas y reglas de backup
- [x] Corregir comprobador de versiones y enlace a publicación oficial
- [x] Preparar script reproducible de compilación/JVM/lint/informes; ejecución pendiente

- [ ] Pruebas de regresion completas
- [ ] Validacion en multiples versiones Android
- [ ] Revision de permisos y privacidad
- [ ] Comprobacion de migraciones y actualizaciones
- [ ] Actualizar README y changelog publico
- [ ] Generar y validar artefactos de lanzamiento
- [ ] Publicar version 4.0 cuando este lista

### Bloque adicional aprobado — orden y pulido (2026-10-12)

Implementación local de docs/18 realizada: orden de cola, filtros persistentes,
búsqueda agrupada, retratos, iconos y controles. Mantener el cierre aceptado de
fases anteriores. Queda la única validación final Gradle/APK de este bloque;
no requiere migración, dependencias ni nueva fase. Suite JVM directa: 114 tests.

### Candidato 4.0.0-rc.1

Implementada identidad propia, presentación de playlists, fondo personal y ranking
opt-in de escuchas. Ver docs/19. Mantener la única validación APK al finalizar.
Listas por ánimo propuestas mediante etiquetas personales, todavía sin implementar.
No confundir commit/push de la rama con lanzamiento público firmado de 4.0.0.

Pulido posterior solicitado: cristal de Inicio más tenue, iconos limpios, nueva
onda y reducción de trabajo/esperas evitables de reproducción. Ver docs/20.
137 JVM directos; Gradle/APK final aún pendiente por bloqueo del agente.

### Fechas y actualizador 4.0.0 preparados — 2026-10-13

Corregido orden reciente por fecha disponible y actualizado Inicio. Descarga APK,
comprobación de identidad/firma y consulta manual implementadas. Ver docs/21.
Fuente 4.0.0/código 41; publicación pendiente de build y firma original, no
confundir con una release ya disponible. 151 JVM directos y 158 fuentes Kotlin.

Actualización 2026-10-13: APK firmado generado por el usuario, certificado original
verificado y release v4.0.0 publicada. 151 JVM Gradle locales sin fallos/errores.
API latest y hash remoto correctos. Pendientes: Lint nuevo y recorrido real de
actualización Android. El texto anterior conserva el estado previo a la publicación.
