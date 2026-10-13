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
- Importación musical mediante carpetas SAF. El escáner MediaStore existe, pero todavía no está conectado al recorrido activo.
- Selección y escaneo de carpetas.
- Organización por canciones, álbumes y artistas.
- Creación y administración de playlists.
- Funcionalidad de canciones favoritas.
- Visualización de carátulas y metadatos.
- Edición de título, artista y álbum en la biblioteca local; no reescribe etiquetas del archivo.

### 🎨 Experiencia visual
- Interfaz desarrollada con Jetpack Compose.
- Componentes basados en Material Design 3.
- Reproductor expandido y minirreproductor.
- Opciones de personalización.
- Visualización de audio y letras musicales.

### ⚙️ Herramientas de reproducción
- Smart DJ local: sugerencias por género, artistas y favoritos, con vista previa antes de cambiar la cola; sin mezcla ni análisis del audio.
- Biblioteca inteligente: posibles duplicados por URI/metadatos, con escucha de versiones y sin borrado automático.
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

**Estado vigente:** fases 1–5 cerradas para el alcance acordado, tras la prueba
satisfactoria del último APK confirmada por el usuario. Por su indicación, las
pruebas inaccesibles al agente quedan excluidas del cierre y no bloquean el avance
a 6/7. Se conservan sin presentarlas como ejecutadas; no se certifica compatibilidad
universal. Los pendientes de los bloques anteriores que siguen son históricos.
Fases 6 y 7 abiertas; todavía no hay lanzamiento de SoundFlow 4.0.

Las siguientes características representan objetivos de desarrollo. **No deben interpretarse como funcionalidades terminadas.**

El detalle y los criterios de cierre están en [docs/02-ROADMAP-4.0.md](docs/02-ROADMAP-4.0.md).

### Fase 1 — Persistencia y estabilidad
- Favoritos persistentes, renombrado de playlists y operaciones transaccionales implementados.
- Compilación de fase 1A y prueba manual satisfactoria reportadas por el usuario.
- Protección contra recreación destructiva de Room implementada y compilada.
- Pruebas instrumentadas escritas; ejecución aplazada.
- Política de compatibilidad en [docs/06-POLITICA-DE-PERSISTENCIA.md](docs/06-POLITICA-DE-PERSISTENCIA.md).

### Fase 2 — Biblioteca y escaneo (desarrollo aceptado)
- Primer bloque implementado: reimportación por URI exacta sin cambiar IDs ni etiquetas editadas, y retirada transaccional de carpetas.
- Carpetas: progreso, resultados, conteo, actualización y confirmación antes de retirar.
- Compilación confirmada desde la terminal del usuario y prueba manual satisfactoria reportada; instrumentación aplazada.
- Primer bloque publicado en `ce6f621` sobre `refactor/soundflow-4`.
- Segundo bloque local: identidad SAF por proveedor e ID de documento para detectar alias y carpetas superpuestas, conservando la URI guardada. Compilación y funcionamiento correcto confirmados por el usuario; diez pruebas de identidad pasan también con Gradle. Instrumentación sin ejecutar.
- Ajuste final: recuperación de acceso a la misma carpeta, rechazo de otra selección y reutilización de metadatos conocidos al actualizar. Compilación final y funcionamiento del APK confirmados por el usuario; once pruebas de identidad pasan en Gradle.
- Política de integridad: conservar duplicados históricos y sus referencias; mantener separadas las identidades SAF y MediaStore.
- Escaneos vacíos o incompletos conservan canciones anteriores; retirada de carpetas y relaciones transaccional.

### Fase 3 — Motor de audio y diseño interno (desarrollo aceptado)
- Diagnóstico y plan en [docs/07-FASE-3-MOTOR-DE-AUDIO.md](docs/07-FASE-3-MOTOR-DE-AUDIO.md). Incluye mejoras visuales y de comportamiento del reproductor.
- Primer bloque local: ViewModel retenido, cierre del controlador, errores con reintento explícito, foco de audio en servicio, carátulas asíncronas y transiciones coordinadas.
- Minirreproductor y pantalla de reproducción renovados: estados visibles, controles de 48 dp, contraste del tema, avance/retroceso de diez segundos y scroll en modo normal.
- Compilación posterior con Gradle confirmada: 17 tests JVM sin fallos (16 específicos y uno de plantilla), APK app y tests regenerados. El usuario reporta funcionamiento correcto; 41 instrumentados nuevos compilados, sin ejecución registrada.
- Fase 3B: efectos del servicio asociados a su sesión y panel renovado, DSP apagado/neutro por defecto y restablecimiento. Compilación posterior verificada: 29 JVM sin fallos (28 específicas y una plantilla); funcionamiento correcto del APK reportado por el usuario. Detalles en [docs/08-FASE-3B-EFECTOS-Y-DISENO.md](docs/08-FASE-3B-EFECTOS-Y-DISENO.md).
- Ajuste final 3C implementado: información del archivo, estado real de espera del temporizador y límites de transiciones documentados. Gradle posterior: 41 JVM sin fallos, APK regenerado y aceptado por el usuario. Desarrollo de fase 3 aceptado; cobertura específica pendiente. [Detalles y archivos](docs/09-CIERRE-FASE-3.md).
- [ ] Revisar Media3, MediaSession y reproducción en segundo plano.
- [ ] Comprobar interrupciones, desconexión de auriculares y transiciones.
- [ ] Validar efectos y limitaciones reales de salida de audio.

### Fase 4 — Desarrollo esencial y revisión de dependencias realizados
- Gradle del bloque final verificado: 88 JVM sin fallos, Lint sin errores e informes de dependencias revisados. APK aceptado en general; memoria/cuadros sin medir. Último ajuste de ciclo de vida del visualizador pendiente de APK regenerado. [Estado y evidencias](docs/14-CIERRE-TECNICO-FASES-4-Y-5.md).
- Diagnóstico y primer bloque 4A implementado: portadas muestreadas/atómicas sin caché RAM duplicada, lectura compartida para el controlador y trabajo de listas/letras fuera del hilo principal. [Auditoría y pendientes esenciales](docs/10-FASE-4-RENDIMIENTO-Y-ARQUITECTURA.md).
- Bloque 4B implementado: caché/red de artistas acotadas, biblioteca compartida con playlists, búsqueda cancelable en Default, ciclo de vida y contrato real de repositorio. [Evidencias, archivos y pruebas manuales](docs/12-CIERRE-FASE-4-E-INICIO-FASE-5.md).
- Validación posterior: 62 JVM Gradle sin fallos, APK app/tests regenerados a las 23:23 del 2026-10-11 y aceptación funcional general del usuario. Instrumentación, mediciones y regresión específica pendientes; el bloqueo de loopback corresponde al intento anterior del agente.
- [x] Interfaz real de repositorio y carpetas sin entidades Room en presentación.
- [x] Compartir biblioteca y derivar listas/búsqueda fuera del hilo principal.
- [x] Eliminar caché RAM duplicada y acotar carga/caché de imágenes.
- [x] Limitar concurrencia, descargas y reintentos de artistas.
- [x] APK del estado final compilado y con aceptación manual general.
- [ ] Completar regresión específica e instrumentación.
- [x] Revisar dependencias resueltas y clasificar advertencias sin actualizarlas.
- [ ] Medir memoria/fluidez en Android.

### Fase 5 — Desarrollo esencial aceptado en general; validación específica pendiente
- Seguir tema del sistema, restablecer Apariencia y política de movimiento ya incluidos en el APK compilado y aceptado en general. Revisión visual estática completada; cabecera flexible y formato de tiempo ajustados después, pendientes de APK regenerado. Matriz visual/accesibilidad y mediciones Android aún pendientes.
- Renovar todas las pantallas, canciones, playlists abiertas, reproductor, filtros, iconos, ajustes y diálogos con un sistema visual coherente.
- Paletas correctas en claro/oscuro y panel **Apariencia** con interruptores independientes para animaciones y transparencias, persistentes y aplicados globalmente.
- Base Apariencia y rediseño aceptados funcionalmente en general: negro puro, superficies neutras, minirreproductor con degradado, Perfil/Ajustes y paneles comunes; portadas, pestañas y letras respetan efectos. Validación visual por caso y rendimiento pendientes. [Archivos, límites y pruebas](docs/13-FASE-5-REDISENO-VISUAL.md).
- Detalle y criterios de aceptación en [docs/11-FASE-5-DISENO-INTEGRAL.md](docs/11-FASE-5-DISENO-INTEGRAL.md); aceptación final de fase 4 aún pendiente.
- [ ] Unificar superficies, tipografía, iconos y controles de todos los apartados.
- [ ] Mejorar presentación de letras, playlists y estados de reproducción.
- [ ] Validar accesibilidad y rendimiento con efectos visuales activados/desactivados.

### Fase 6 — Innovación
- Primer bloque funcional local: Smart DJ y vista de coincidencias conectados a Inicio. 137 fuentes Kotlin comprobadas y 98 JVM directos correctos; compilación Gradle/APK de este bloque pendiente por loopback. [Implementación y límites](docs/17-FASES-6-Y-7-IMPLEMENTACION-ESENCIAL.md).
- Base local de **Audio Inspector** conectada al menú del reproductor: inspección de fuente a demanda y clasificación conservadora. Sin medición de salida ni garantía bit-perfect. [Implementación y viabilidad](docs/15-AUDIO-INSPECTOR-Y-FASE-6.md).
- [ ] **Audio Inspector Pro:** información avanzada de archivos y salida de audio.
- [ ] **SoundFlow Smart DJ:** creación inteligente de sesiones musicales.
- [ ] **SoundFlow Replay:** estadísticas privadas de escucha.
- [ ] **Biblioteca inteligente:** detección de duplicados y herramientas de organización.
- [ ] **Perfiles por dispositivo:** ajustes específicos para auriculares y salidas de audio.
- [ ] **USB DAC Studio:** capacidades avanzadas para dispositivos de audio compatibles.
- [ ] **Transferencia Wi-Fi:** intercambio local de archivos musicales.

### Fase 7 — Lanzamiento
- Controles persistentes de privacidad en Ajustes para Deezer, LRCLIB y GitHub; herramienta de preparación release local sin firma/publicación. Versión de aplicación conservada. [Estado real](docs/17-FASES-6-Y-7-IMPLEMENTACION-ESENCIAL.md).
- Revisión inicial de permisos, privacidad y backup; comprobación de actualizaciones corregida y herramientas de validación preparadas. [Puertas de lanzamiento pendientes](docs/16-PREPARACION-FASE-7.md).
- [ ] Regresión, compatibilidad Android, permisos y actualizaciones.
- [ ] Validar artefactos y publicar SoundFlow 4.0 cuando esté listo.

## 🔒 Privacidad

SoundFlow está diseñado alrededor de una biblioteca musical local.

El objetivo es que sus funciones esenciales no requieran una cuenta en la nube y que los datos musicales del usuario permanezcan bajo su control.

Actualmente se consulta Deezer por artista para imágenes, LRCLIB por artista/título para letras y GitHub para actualizaciones. No se sube el archivo de audio en esos recorridos. La revisión de controles de red y backup figura en el documento de preparación de fase 7; no se presenta la app como totalmente offline.
Los tres servicios pueden desactivarse por separado en Ajustes. Por compatibilidad
están activados si no existía preferencia; desactivarlos no borra música ni caché.

## 🧪 Estado del desarrollo

**Versión base:** 3.7.0

**Rama de modernización:** `refactor/soundflow-4`

**Próximo objetivo:** SoundFlow 4.0

Fase 1 publicada en `7c19fd4` y primer bloque de fase 2 en `ce6f621`. Último APK aceptado: rediseño de las 10:15:34 del 2026-10-12 Bogotá, 65 JVM Gradle correctos. El bloque local posterior añade ajustes de fases 4/5, Audio Inspector y preparación de fase 7: **131 fuentes Kotlin comprobadas directamente y 88 JVM correctos**, fuera de Gradle. Gradle sigue bloqueado antes de tareas por loopback; no hay APK nuevo de este bloque. **73 casos instrumentados nuevos acumulados más la plantilla**, sin ejecución registrada. Matriz visual, accesibilidad, mediciones, dependencias y lanzamiento siguen pendientes. Sin nuevo commit ni push.

Validación del bloque local desde PowerShell: `.\tools\Validate-SoundFlow.ps1`. No requiere teléfono para compilar, ejecutar JVM/lint y generar informes; las pruebas Android reales son una comprobación aparte.

## 🤝 Contribuciones

Las mejoras deben desarrollarse en ramas independientes y validarse antes de integrarse en la rama principal.

Se recomienda acompañar las correcciones importantes con pruebas y documentación técnica.

## 📄 Licencia

Consultar el archivo `LICENSE` del repositorio para conocer los términos de distribución y uso.

---

**SoundFlow — Una nueva forma de disfrutar tu biblioteca musical.**

Pulido adicional local: orden visible de reproducción, filtros persistentes,
búsqueda por canciones/artistas/álbumes y controles visuales renovados. Estado y
validación final: [orden y pulido 4.0](docs/18-DIAGNOSTICO-ORDEN-Y-PULIDO-4.0.md).
