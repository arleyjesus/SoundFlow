# 🎵 SoundFlow

**Tu música. Tu sonido. Tu experiencia.**

SoundFlow es un reproductor de música local para Android, desarrollado en Kotlin con Jetpack Compose y Jetpack Media3 (ExoPlayer).

Su objetivo es ofrecer una experiencia musical moderna, rápida, personalizable y enfocada en la privacidad, con especial atención a la reproducción de archivos de alta calidad.

## 🚀 Características

### 🎧 Reproducción musical
- Reproducción de música almacenada localmente.
- Compatibilidad con formatos de audio admitidos por Android y sus decodificadores.
- Reproducción en segundo plano mediante Media3.
- Controles multimedia en notificaciones y pantalla de bloqueo.
- Gestión de la cola de reproducción.
- Manejo de interrupciones y desconexión de auriculares.

### 📂 Biblioteca musical
- Exploración de archivos musicales mediante MediaStore.
- Selección y escaneo de carpetas.
- Organización por canciones, álbumes y artistas.
- Creación y administración de playlists.
- Funcionalidad de canciones favoritas.
- Visualización de carátulas y metadatos.
- Edición básica de etiquetas musicales.

### 🎨 Experiencia visual
- Interfaz desarrollada con Jetpack Compose.
- Componentes basados en Material Design 3.
- Reproductor expandido y minirreproductor.
- Opciones de personalización.
- Visualización de audio y letras musicales.

### ⚙️ Herramientas de reproducción
- Funcionalidades de ecualización.
- Temporizador de apagado.
- Opciones de transición entre canciones.
- Consulta de información de archivos musicales.

**Nota:** algunas funcionalidades están en proceso de revisión y estabilización. La presencia de una función en el código no garantiza que todos sus escenarios estén completamente validados.

## 🛠️ Tecnologías

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje principal |
| Jetpack Compose | Interfaz de usuario |
| Material Design 3 | Componentes visuales |
| Media3 / ExoPlayer | Motor multimedia |
| Room | Persistencia local |
| Kotlin Coroutines y Flow | Programación asíncrona |
| MediaStore y SAF | Acceso a archivos musicales |
| DataStore | Preferencias |
| Gradle | Compilación y dependencias |

## 🏗️ Arquitectura

SoundFlow utiliza una organización por capas inspirada en Clean Architecture y el patrón MVVM.

El código principal se encuentra bajo:

`com.example.comarleyaetheraudio`

**Data:** implementaciones de almacenamiento, escaneo de archivos, servicios multimedia y repositorios.

**Domain:** modelos de negocio, contratos y casos de uso.

**Presentation:** pantallas Compose, ViewModels, componentes y estado de la interfaz.

La arquitectura continúa en revisión para mejorar la separación de responsabilidades, la facilidad de pruebas y el mantenimiento del proyecto.

## 🎼 Alta fidelidad

SoundFlow busca proporcionar información útil sobre la calidad de los archivos reproducidos, incluyendo formato, bitrate y frecuencia de muestreo cuando dichos metadatos estén disponibles.

La reproducción efectiva depende de los decodificadores Android, la ruta de audio del dispositivo y el hardware conectado.

**La detección de un archivo Hi-Res no constituye por sí sola una garantía de salida bit-perfect.**

## 💻 Compilación local

Requisitos generales:

- Android Studio compatible con la configuración Gradle del proyecto.
- Android SDK correspondiente a la versión de compilación configurada.
- JDK compatible.
- Dispositivo Android o emulador para pruebas.

Clonar el repositorio:

`git clone https://github.com/arleyjesus/SoundFlow.git`

Entrar al proyecto:

`cd SoundFlow`

Compilar desde Windows:

`.\gradlew.bat :app:assembleDebug`

El APK de depuración se genera normalmente en:

`app/build/outputs/apk/debug/`

Antes de compilar, comprueba que Android Studio reconoce correctamente la ubicación del Android SDK.

## 🗺️ Roadmap — SoundFlow 4.0

Las siguientes características representan objetivos de desarrollo. **No deben interpretarse como funcionalidades terminadas.**

El detalle y los criterios de cierre están en [docs/02-ROADMAP-4.0.md](docs/02-ROADMAP-4.0.md).

### Fase 1 — Persistencia y estabilidad
- Favoritos persistentes, renombrado de playlists y operaciones transaccionales implementados.
- Compilación de fase 1A y prueba manual satisfactoria reportadas por el usuario.
- Protección contra recreación destructiva de Room implementada y compilada.
- Pruebas instrumentadas escritas; ejecución aplazada.
- Política de compatibilidad en [docs/06-POLITICA-DE-PERSISTENCIA.md](docs/06-POLITICA-DE-PERSISTENCIA.md).

### Fase 2 — Biblioteca y escaneo (en curso)
- Primer bloque implementado: reimportación por URI exacta sin cambiar IDs ni etiquetas editadas, y retirada transaccional de carpetas.
- Carpetas: progreso, resultados, conteo, actualización y confirmación antes de retirar.
- Compilación confirmada desde la terminal del usuario y prueba manual satisfactoria reportada; instrumentación aplazada.
- [ ] Resolver alias SAF/MediaStore, duplicados históricos y solapamientos generales.
- [ ] Gestionar escaneos vacíos, parciales o fallidos sin perder datos.
- [ ] Revisar MediaStore, SAF y permisos persistentes.
- [ ] Revisar la retirada de carpetas y sus relaciones.

### Fase 3 — Motor de audio
- [ ] Revisar Media3, MediaSession y reproducción en segundo plano.
- [ ] Comprobar interrupciones, desconexión de auriculares y transiciones.
- [ ] Validar efectos y limitaciones reales de salida de audio.

### Fase 4 — Rendimiento y arquitectura
- [ ] Mejorar la separación entre capas.
- [ ] Optimizar consultas y flujos de datos.
- [ ] Reducir consumo innecesario de memoria.
- [ ] Mejorar carga y caché de carátulas.
- [ ] Fortalecer el manejo de errores del reproductor.
- [ ] Revisar dependencias y APIs obsoletas.

### Fase 5 — Experiencia musical
- [ ] Perfeccionar el ecualizador y sus perfiles.
- [ ] Mejorar el manejo de letras sincronizadas.
- [ ] Revisar transiciones musicales y crossfade.
- [ ] Mejorar la gestión de playlists.
- [ ] Ampliar las opciones de personalización.

### Fase 6 — Innovación
- [ ] **Audio Inspector Pro:** información avanzada de archivos y salida de audio.
- [ ] **SoundFlow Smart DJ:** creación inteligente de sesiones musicales.
- [ ] **SoundFlow Replay:** estadísticas privadas de escucha.
- [ ] **Biblioteca inteligente:** detección de duplicados y herramientas de organización.
- [ ] **Perfiles por dispositivo:** ajustes específicos para auriculares y salidas de audio.
- [ ] **USB DAC Studio:** capacidades avanzadas para dispositivos de audio compatibles.
- [ ] **Transferencia Wi-Fi:** intercambio local de archivos musicales.

### Fase 7 — Lanzamiento
- [ ] Regresión, compatibilidad Android, permisos y actualizaciones.
- [ ] Validar artefactos y publicar SoundFlow 4.0 cuando esté listo.

## 🔒 Privacidad

SoundFlow está diseñado alrededor de una biblioteca musical local.

El objetivo es que sus funciones esenciales no requieran una cuenta en la nube y que los datos musicales del usuario permanezcan bajo su control.

Cualquier futura integración de red deberá documentar sus permisos, finalidad y tratamiento de datos.

## 🧪 Estado del desarrollo

**Versión base:** 3.7.0

**Rama de modernización:** `refactor/soundflow-4`

**Próximo objetivo:** SoundFlow 4.0

Fase 1 entregada en el commit 7c19fd4 y publicada en refactor/soundflow-4. Compilación final correcta reportada por el usuario (BUILD SUCCESSFUL, 1m 14s, 77 tareas), un test unitario de plantilla aprobado y prueba manual satisfactoria del APK anterior de fase 1A. El primer bloque de fase 2 también compiló desde su terminal (42s, 77 tareas) y el usuario reportó funcionamiento correcto del APK. Las 27 pruebas instrumentadas nuevas siguen pendientes. Fase 2 local, sin publicar; quedan pendientes de identidad y permisos descritos en el roadmap.

## 🤝 Contribuciones

Las mejoras deben desarrollarse en ramas independientes y validarse antes de integrarse en la rama principal.

Se recomienda acompañar las correcciones importantes con pruebas y documentación técnica.

## 📄 Licencia

Consultar el archivo `LICENSE` del repositorio para conocer los términos de distribución y uso.

---

**SoundFlow — Una nueva forma de disfrutar tu biblioteca musical.**
