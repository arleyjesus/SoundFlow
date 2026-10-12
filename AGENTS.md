
# AGENTS.md — SoundFlow 4.0

## Identidad del proyecto

SoundFlow es un reproductor de música local para Android.

Repositorio:
https://github.com/arleyjesus/SoundFlow

Rama principal de desarrollo:
refactor/soundflow-4

Tecnologías:
- Kotlin
- Jetpack Compose / Material Design 3
- Media3 / ExoPlayer
- Room
- Coroutines y Flow
- MediaStore y Storage Access Framework
- Gradle

## Misión

Modernizar SoundFlow hacia la versión 4.0,
priorizando estabilidad, integridad de los datos,
rendimiento, calidad de reproducción, experiencia
de usuario y funcionalidades innovadoras.

No sacrificar estabilidad por implementar
funcionalidades nuevas rápidamente.

## Contexto obligatorio

Antes de cualquier cambio importante, leer:

1. README.md
2. docs/00-VISION-Y-OBJETIVOS.md
3. docs/01-AUDITORIA-ACTUAL.md
4. docs/02-ROADMAP-4.0.md
5. docs/03-ARQUITECTURA.md
6. docs/04-PLAN-DE-PRUEBAS.md
7. docs/05-FLUJO-GIT-Y-RELEASES.md
8. docs/CHANGELOG-DESARROLLO.md

Comprobar el estado real del código.
La documentación puede estar desactualizada.

## Estado inicial conocido

Fase 0:
- Documentación creada.
- Commit a69bd59.
- Publicada en GitHub.

Fase 1:
- Implementados favoritos Room, playlists transaccionales
  y apertura sin recreación destructiva.
- El usuario reportó compilación correcta del ajuste final
  y prueba manual satisfactoria del APK anterior de fase 1A.
- Instrumentación aplazada; no afirmar que los 16 tests pasaron.
- El usuario autorizó commit y publicación de fase 1,
  y comenzar fase 2 por identidad, escaneo y carpetas.
- Consultar Git y docs para el estado actual de entrega.

Antes de editar, ejecutar:
- git status --short
- git branch --show-current
- git log -3 --oneline

Nunca sobrescribir modificaciones existentes
sin revisarlas primero.

## Reglas de arquitectura

1. Mantener contratos compatibles entre capas.
2. Conservar la funcionalidad existente.
3. Evitar cambios de paquetes innecesarios.
4. Mantener separación data/domain/presentation.
5. Preferir operaciones transaccionales en Room.
6. Usar Flow para observar datos persistentes.
7. Evitar trabajo pesado en el hilo principal.
8. Controlar errores y cancelación de corrutinas.
9. No agregar dependencias sin justificar.
10. No afirmar que algo funciona sin probarlo.

## Reglas críticas de persistencia

Nombre actual de la base:
soundflow_database

Versión inicial del esquema:
2

No cambiar el nombre de la base de datos.

No modificar entidades ni subir la versión
sin diseñar y probar las migraciones necesarias.

Se retiró fallbackToDestructiveMigration() en fase 1.
No reintroducir recreación destructiva para ocultar
errores de esquema o migraciones ausentes.

Preservar identificadores de canciones cuando
sea posible para proteger favoritos y playlists.

No eliminar masivamente canciones ante un
escaneo vacío, incompleto o fallido.

Distinguir MediaStore de SAF y no asumir que
sus rutas o identificadores son equivalentes.

## Prioridades

1. Persistencia de favoritos y playlists.
2. Identidad estable y sincronización musical.
3. Integridad de datos y migraciones.
4. Motor de reproducción y servicios Media3.
5. Rendimiento, caché y memoria.
6. Interfaz Compose y accesibilidad.
7. Nuevas funcionalidades innovadoras.
8. Pruebas integrales y lanzamiento.

Consultar docs/02-ROADMAP-4.0.md para el detalle.

## Desarrollo por fases

Para cada tarea:

### 1. Objetivos
Explicar qué se quiere mejorar y por qué.

### 2. Diagnóstico
Examinar clases relacionadas, contratos,
usos, pruebas y riesgos.

Separar:
- Problemas confirmados
- Riesgos por verificar
- Mejoras propuestas

### 3. Plan
Indicar archivos afectados y cambios previstos.

Para cambios grandes, presentar el plan
antes de editar.

### 4. Implementación
Realizar cambios pequeños y coordinados.

No modificar archivos ajenos a la tarea
sin necesidad.

Mantener imports, modelos, constructores,
interfaces y usos compatibles.

### 5. Validación
Ejecutar cuando el entorno lo permita:

./gradlew :app:assembleDebug

En Windows:
.\gradlew.bat :app:assembleDebug

Ejecutar pruebas relevantes.

No interpretar BUILD SUCCESSFUL como prueba
de funcionamiento en un dispositivo.

### 6. Documentación
Actualizar roadmap, auditoría o changelog
cuando corresponda.

### 7. Entrega
Informar:
- Objetivos alcanzados
- Archivos modificados
- Motivos de cada cambio
- Compilación y pruebas realizadas
- Riesgos pendientes
- Pasos de prueba manual

## Git y seguridad

Trabajar sobre refactor/soundflow-4
o una rama derivada aprobada.

No modificar main directamente.

No hacer push, merge, reset --hard,
force push ni eliminar archivos sin
autorización explícita.

No incluir secretos, claves o credenciales.

Antes de cualquier commit:
- git status
- git diff --check
- git diff --stat
- revisión de archivos preparados

Crear commits descriptivos por funcionalidad.

El usuario debe revisar y autorizar
cada commit y publicación.

## Audio de alta fidelidad

No confundir:
- Archivo lossless
- Archivo Hi-Res
- Capacidad del decodificador
- Frecuencia de salida Android
- Reproducción bit-perfect

Toda afirmación técnica sobre calidad
de salida debe estar fundamentada.

Evitar procesamiento de audio innecesario
que pueda introducir distorsión.

## Funcionalidades futuras

Explorar progresivamente:

- Audio Inspector Pro
- SoundFlow Smart DJ
- SoundFlow Replay
- Biblioteca inteligente
- Perfiles de audio por dispositivo
- USB DAC Studio
- Transferencia local mediante Wi-Fi

Estas funciones son propuestas, no
implementaciones terminadas.

Priorizar viabilidad, permisos, consumo de
recursos, privacidad y experiencia de usuario.

## Comunicación

Responder preferentemente en español.

Explicar los objetivos al comienzo de
cada intervención.

Dar instrucciones claras y verificables.

Distinguir lo implementado de lo pendiente.

Si no es posible compilar o ejecutar
pruebas, indicarlo expresamente.
