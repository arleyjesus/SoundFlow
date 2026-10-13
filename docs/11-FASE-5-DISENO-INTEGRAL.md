# Fase 5: diseño integral y personalización

Actualización de cierre 2026-10-12: Gradle del usuario confirma 88 JVM sin fallos,
Lint 0 errores y APK aceptado en general. Revisión estática del recorrido activo
completada; visualizador ligado a primer plano, cabecera flexible y tiempo con
Locale.ROOT añadidos después. APK de estos tres ajustes y matriz visual/TalkBack
pendientes. No se declara aceptación visual exhaustiva a partir de una captura.

Historial del bloque previo: seguir sistema, restablecer solo Apariencia,
política de animación Android y ajustes para fuente grande implementados.
Letras comparten parser con centésimas corregidas. Comprobación Kotlin/JVM directa
correcta; APK de este ajuste, matriz visual y accesibilidad real pendientes.
[Archivos y recorrido](14-CIERRE-TECNICO-FASES-4-Y-5.md). El estado que sigue
describe el APK aceptado anterior.

Estado: base Apariencia y bloque de rediseño aceptados en general por el usuario.
Validación posterior del 2026-10-12: 65 JVM Gradle sin fallos y APK app/tests
regenerados a las 10:15:34/33 Bogotá, después de las fuentes. Revisión visual por
caso y accesibilidad pendientes. Fase 4 conserva mediciones e instrumentación
pendientes. [Bloque actual y pruebas](13-FASE-5-REDISENO-VISUAL.md);
evidencias del APK anterior en documento 12.

## Base implementada

- Entrada Perfil → Ajustes → Apariencia; cuatro paletas y modo claro/oscuro.
- DataStore existente conserva claves de tema; dos nuevas preferencias booleanas
  para efectos, independientes y por defecto activadas. Tres JVM validan
  compatibilidad, independencia/reapertura y conservacion de otras claves.
- ColorScheme unico sin sobrescritura de MainActivity. Un JVM prueba ocho
  combinaciones, cinco pares de contraste minimo 4.5:1 en cada una.
- Navegacion sin transiciones al apagar Animaciones; visualizador sin bucle al
  pausar o apagarlo. Fondo del reproductor opaco y sin blur/paleta decorativa al
  apagar Transparencias. Controles usan acento del tema, no color arbitrario de portada.
- Comprobacion Kotlin/Compose de panel y AndroidTest correcta; test UI no ejecutado.

Extensión actual: negro puro, superficies neutras, panel con degradado y modo
opaco, minirreproductor, Perfil/Ajustes/DSP y colores semánticos del recorrido
intervenido. Animaciones cubre también crossfades activos, pestañas, letras y
selección. Reproductor sin tirador superior; márgenes del notch pendientes de
revisión real. Distribución y visualizador conservados. Restablecer y seguir
sistema ya implementados; cobertura completa de Material pendiente. No se declara terminada
fase 5 ni control global completo de efectos.

## Objetivo

Una aplicación limpia, moderna y con personalidad, conservando riqueza funcional.
La mejora incluye todos los apartados, no solo portada o reproductor. Mantener
acciones, contenido, IDs de canciones, favoritos, playlists y preferencias.

## Alcance por recorrido

| Apartado | Mejora prevista |
|---|---|
| Inicio y navegación | Jerarquía, tarjetas, accesos, barras y estados consistentes |
| Biblioteca, artistas, carpetas | Filas, portadas, agrupaciones, filtros/orden, selección y permisos |
| Búsqueda | Campo, resultados, filtros y estados vacío/carga/error |
| Playlists y detalle abierto | Cabecera, mosaicos, acciones, selección y canciones dentro de la playlist |
| Canción y opciones | Identidad seleccionada, menú, favoritos, añadir, editar e información técnica |
| Minirreproductor y reproducción abierta | Portada, fondo, progreso, controles, cola, letras y temporizador |
| Ajustes, perfil y AudioFX | Tarjetas, sliders, valores, soporte, avisos y personalización |
| Diálogos y paneles | Tipografía, superficies, botones, iconos, foco y contraste coherentes |

## Panel especial Apariencia

Entrada visible con icono de paleta en Ajustes/Perfil. Un único panel configura:

- Paleta de colores y tema claro/oscuro; evaluar seguir sistema sin romper la
  preferencia explícita actual. Previsualización y persistencia mediante el
  DataStore settings_prefs existente, sin crear otra base de preferencias.
- Interruptor **Animaciones**: controlar navegación, transiciones de tarjetas,
  carátulas y elementos decorativos. Apagado debe evitar crear los loops, no solo
  ocultarlos. Mantener estados de carga y respuesta a acciones comprensibles.
- Interruptor **Transparencias** independiente: controlar superficies traslúcidas,
  superposiciones y desenfoques. Apagado usa superficies opacas con contraste.
- Restablecer apariencia con alcance visible; nunca borrar biblioteca o ajustes
  de audio. Los controles visuales no deben activar DSP ni cambiar calidad de audio.

Paletas y switches tienen implementacion inicial con consumidores reales descritos
arriba. Restablecer y seguir sistema añadidos en el bloque posterior; cobertura global completa pendiente.
Evitar controles sin efecto real en las siguientes extensiones.

## Estrategia de implementación

1. Completar ColorScheme para cada paleta en ambos modos: pares primary/onPrimary,
   surface/onSurface, contenedores, bordes, selección y error. Sustituir colores
   locales de pantallas por roles semánticos. La portada dinámica no debe hacer
   ilegibles textos o iconos. Definir espaciado, formas, tipografía e iconos comunes.
2. Crear estado de Apariencia y política de efectos compartida. Preferencias se
   leen de forma reactiva; cambiar modo actualiza también diálogos y paneles abiertos.
3. Aplicar componentes comunes a biblioteca/búsqueda/playlists y ajustes. Rediseñar
   reproductor y letras con el mismo sistema. Mantener las acciones existentes.
4. Validar el conjunto con captura/revisión por pantalla y matriz de estados,
   además de pruebas de acciones. Evitar una fase separada por cada pantalla.

Compatibilidad: minSdk 24. El blur de Compose requiere Android 12 o posterior;
usar alternativa opaca/degradado donde no esté disponible, sin emulación costosa
por CPU. Esta limitación está documentada en
[Android: personalización de imágenes](https://developer.android.com/develop/ui/compose/graphics/images/customize).
Respetar la reducción de movimiento del sistema según APIs disponibles y comprobar
el comportamiento real de transiciones; no prometer que un switch intercepta
automáticamente toda animación de librerías o del sistema.

## Criterios de aceptación

- Todas las pantallas, menús y diálogos correctos en claro/oscuro y cada paleta.
- Cuatro combinaciones animaciones/transparencias, conservadas al reiniciar.
- Reproductor usable con fuente grande, pantalla pequeña/horizontal y TalkBack;
  objetivos táctiles de 48 dp y etiquetas para acciones.
- Sin parpadeos por preferencias iniciales, texto recortado, controles invisibles
  ni animaciones decorativas activas fuera de pantalla o estando deshabilitadas.
- Sin regresión de navegación, búsqueda, selección, favoritos, playlists o audio.
- Repetir mediciones de fase 4 con efectos activados/desactivados. La apariencia
  no se acepta si introduce crecimiento sostenido de memoria o degradación sin resolver.
- Compilación, pruebas relevantes, documentación y revisión Git por bloque.
