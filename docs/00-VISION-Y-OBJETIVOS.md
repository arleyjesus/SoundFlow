# SoundFlow 4.0 - Vision y objetivos

## Vision

Construir un reproductor de musica local para Android que
combine estabilidad, fidelidad de audio, rapidez, privacidad,
personalizacion y una experiencia moderna.

## Objetivos principales

1. Corregir problemas de integridad de datos.
2. Preservar favoritos, playlists y biblioteca musical.
3. Mejorar la reproduccion y el manejo de audio.
4. Modernizar progresivamente la arquitectura.
5. Optimizar rendimiento, memoria y almacenamiento.
6. Incorporar funcionalidades musicales innovadoras.
7. Mantener documentacion, pruebas y versiones trazables.

## Principios del desarrollo

- Priorizar estabilidad antes de agregar funciones.
- Mantener compatibilidad con los datos existentes.
- No modificar esquemas Room sin migraciones verificadas.
- Entregar cambios completos por conjunto de archivos.
- Distinguir compilacion de pruebas funcionales.
- Documentar decisiones y cambios relevantes.
- Desarrollar en ramas, sin modificar main directamente.
- Crear commits y publicar al cerrar fases verificadas.

## Base tecnologica

- Android y Kotlin
- Jetpack Compose y Material Design 3
- Media3 / ExoPlayer
- Room
- Kotlin Coroutines y Flow
- MediaStore y Storage Access Framework
- Gradle y Git

## Alcance

SoundFlow es principalmente un reproductor local.
Cualquier funcion con acceso a servicios externos debe
documentar permisos, dependencias, privacidad y fallos de red.

## Estado

Plan de modernizacion hacia SoundFlow 4.0.
Las funciones futuras son propuestas, no funciones terminadas.
