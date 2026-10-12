# Roadmap de SoundFlow 4.0

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
- [ ] Cerrar fase y publicar en GitHub

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

Estado: no iniciada ni implementada en esta intervencion.

- [ ] Detectar canciones existentes antes de insertar
- [ ] Conservar IDs de canciones registradas
- [ ] Evitar duplicados por URI o identidad estable
- [ ] Gestionar escaneos vacios, parciales y fallidos
- [ ] Revisar eliminacion segura por carpeta
- [ ] Revisar MediaStore y SAF
- [ ] Probar permisos persistentes
- [ ] Probar bibliotecas grandes
- [ ] Publicar fase en GitHub

## Fase 3 - Motor de audio

- [ ] Revisar AudioPlayerHandler
- [ ] Revisar SoundFlowMediaService
- [ ] Comprobar MediaSession y segundo plano
- [ ] Revisar audio focus y becoming noisy
- [ ] Evaluar ecualizador y efectos
- [ ] Revisar crossfade y gapless cuando aplique
- [ ] Documentar limitaciones reales de Hi-Res
- [ ] Publicar fase en GitHub

## Fase 4 - Rendimiento y arquitectura

- [ ] Optimizar consultas Room y Flows
- [ ] Revisar cache de caratulas
- [ ] Revisar uso de memoria y bitmaps
- [ ] Revisar concurrencia y cancelacion de tareas
- [ ] Reducir trabajo innecesario de UI
- [ ] Revisar separacion de capas
- [ ] Modernizar APIs gradualmente
- [ ] Incorporar pruebas automatizadas
- [ ] Publicar fase en GitHub

## Fase 5 - Experiencia

- [ ] Optimizar pantallas Jetpack Compose
- [ ] Mejorar navegacion y estados de carga
- [ ] Mejorar accesibilidad
- [ ] Mejorar gestion de playlists
- [ ] Mejorar letras sincronizadas
- [ ] Personalizacion y temas
- [ ] Publicar fase en GitHub

## Fase 6 - Innovacion

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

- [ ] Pruebas de regresion completas
- [ ] Validacion en multiples versiones Android
- [ ] Revision de permisos y privacidad
- [ ] Comprobacion de migraciones y actualizaciones
- [ ] Actualizar README y changelog publico
- [ ] Generar y validar artefactos de lanzamiento
- [ ] Publicar version 4.0 cuando este lista
