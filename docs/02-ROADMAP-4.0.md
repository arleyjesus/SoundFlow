# Roadmap de SoundFlow 4.0

## Fase 0 - Documentacion y organizacion

- [ ] Crear documentacion tecnica
- [ ] Documentar auditoria y riesgos
- [ ] Definir fases y criterios de cierre
- [ ] Validar README
- [ ] Crear commit de documentacion

## Fase 1 - Persistencia y estabilidad

- [x] Preparar favoritos persistentes en Room
- [x] Preparar renombrado de playlists sin alterar createdAt
- [x] Preparar limpieza de relaciones al eliminar playlists
- [x] Confirmar compilacion de estos cambios
- [ ] Probar favoritos despues de reiniciar
- [ ] Probar creacion, renombrado y eliminacion de playlists
- [ ] Probar integridad de datos
- [ ] Revisar estrategia de migraciones Room
- [ ] Cerrar fase y publicar en GitHub

## Fase 2 - Biblioteca y escaneo

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
