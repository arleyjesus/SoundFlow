# SoundFlow — MASTER.md (Design System, fuente de verdad)

> Generado con `ui-ux-pro-max --design-system --persist` (variance 6 · motion 5 · density 5) y adaptado a Jetpack Compose / Material 3.
> Copia cruda de la herramienta: `design-system/soundflow/MASTER.md` (orientada a web; **este archivo prevalece**).
> Overrides por pantalla: `design-system/soundflow/pages/<pantalla>.md`.

## 1. Perfil y problema

**Arquetipo:** audiófilo / melómano de biblioteca local (FLAC, ALAC, DSD, 24-bit/96 kHz+). Usa auriculares/DAC, sesiones largas, a menudo con una mano y de noche. Valora fidelidad, metadatos correctos, carátulas, cero anuncios, control fino.

**Cuellos de botella UX en reproductores locales**
| # | Problema | Principio SoundFlow |
|---|----------|--------------------|
| 1 | Escaneo/carga de biblioteca lenta, pantalla vacía | Shimmer con layout reservado; nunca spinner a pantalla completa |
| 2 | Carátulas ausentes o que "saltan" | Placeholder tonal + crossfade + caché Coil/CoverCache |
| 3 | Controles críticos fuera del pulgar | Play/Prev/Next/Seek en la mitad inferior (thumb zone) |
| 4 | Calidad del audio invisible | Badge Hi-Res (kHz/bit/códec) en lista y reproductor |
| 5 | Listas enormes con jank | `LazyColumn` + `key = id` + params estables |
| 6 | Estados ambiguos (¿suena? ¿cuál pista?) | Indicador "ahora suena" y semántica de estado |
| 7 | Color dinámico que rompe el contraste | Color de portada saneado antes de usarlo (§3) |

**Estilo:** *Dark Hi-Fi OLED* + *Material 3 tonal*; glassmorphism **solo** en el fondo del reproductor completo (blur de carátula). Minimalismo moderno en listas.
**Stack:** Jetpack Compose, Material 3, Media3 ExoPlayer, Coil (`coil.compose`), Palette API (`DynamicThemeExtractor`), Room, Clean Architecture (UI solo consume `AudioPlayerState`).

## 2. Diales (1–10)

| Dial | Valor | Justificación |
|------|-------|---------------|
| `--variance` | **6** | Color adaptativo por carátula en el reproductor completo (alto), pero listas/navegación sobrias y consistentes (bajo). Evita asimetría brutalista: se prioriza reconocer la pista rápido. |
| `--motion` | **5** | Resortes cortos (150–350 ms) con significado: play↔pause, expandir mini→full, crossfade de carátula. Sin coreografías largas; respeta "Quitar animaciones" del sistema. |
| `--density` | **5** | Filas de 64–72 dp, 16 dp de margen. Suficiente info (título, artista, calidad) sin saturar; objetivos táctiles ≥48 dp. |

## 3. Tokens de color (semánticos; nunca hex crudo en componentes)

Tema por defecto = `PRINCIPAL` oscuro. Los demás temas (`CLASSIC`, `COOL`, `SIMPLE`) redefinen los mismos roles en `Theme.kt`.

| Rol Compose (`MaterialTheme.colorScheme`) | Dark (OLED) | Light | Uso |
|---|---|---|---|
| `background` | `#000000` | `#FBFBFE` | Fondo app |
| `surface` | `#121212` | `#FFFFFF` | Pantallas, sheets |
| `surfaceContainer` (mini player, tarjetas) | `#1E1E1E` | `#F1EEF8` | Mini player, cards |
| `surfaceVariant` | `#2C2C2C` | `#E7E3EF` | Placeholders, chips |
| `primary` | `#B388FF` (LightLavender) | `#8A2BE2` | Acción principal, progreso |
| `onPrimary` | `#1A0B2E` | `#FFFFFF` | Texto/ícono sobre primary |
| `onSurface` | `#FFFFFF` | `#111111` | Texto principal (≥ 15:1) |
| `onSurfaceVariant` | `#FFFFFFB3` (70 %) | `#49454F` | Texto secundario (≥ 4.5:1) |
| `outlineVariant` | `#FFFFFF1F` | `#0000001F` | Divisores, track inactivo |
| `error` | `#EF4444` | `#B3261E` | Errores |
| Extensión `HiRes` (nuevo) | `#FFD700` / on `#1A1400` | `#B8860B` / on `#FFFFFF` | Badge Hi-Res (único acento dorado) |
| `accent` dinámico | `Palette` de portada | idem | Solo reproductor completo (ver regla) |

**Regla de color dinámico:** `accent = dominantColor.ensureContrast(onSurface/background, 4.5:1)`; si no cumple, ajustar luminancia (HSL L) o caer a `primary`. El color del ícono sobre `accent` se elige por luminancia (`onAccent` blanco/negro), nunca `Color.White` fijo. Gradiente: `accent@0.35 → background` (dark) / `@0.08` (light).
**Prohibido:** `Color(0x…)` dentro de composables, `Color.White/Black` fijos, texto gris sobre gris.

## 4. Tipografía

La herramienta sugirió *Righteous + Poppins* (tono festivo, web). **Override Android/hi-fi:** familia sans de sistema (Roboto/Google Sans) por rendimiento y sobriedad; números en `tabular-nums` (`fontFeatureSettings = "tnum"`) para tiempos y kHz.

| Token (`Typography`) | sp / peso | Uso |
|---|---|---|
| `displaySmall` | 28 / Black, −0.5 | Título pantalla / playlist |
| `titleLarge` | 22 / ExtraBold | Título pista (reproductor) |
| `titleMedium` | 18 / Bold | Encabezado de sección |
| `bodyLarge` | 15 / SemiBold | Título en lista, mini player |
| `bodyMedium` | 13 / Medium | Artista, secundario |
| `labelMedium` | 12 / Medium | Metadatos, duración |
| `labelSmall` | 11 / Normal | Badges (**mínimo 11 sp**; el badge Hi-Res hoy usa 10 sp → subir) |

Reglas: tamaños en `sp` (escala con accesibilidad), máx. 1 línea con elipsis en listas, `basicMarquee` en título del reproductor, line-height ≥ 1.4 en letras.

## 5. Forma, elevación, espaciado

| Token | Valor |
|---|---|
| `shape.extraSmall` | 4 dp (badges) |
| `shape.small` | 8 dp (miniatura en lista) |
| `shape.medium` | 12 dp (cards, mini player cover) |
| `shape.large` | 16 dp (mini player, bottom sheets) |
| `shape.extraLarge` | 24 dp (carátula grande) |
| `CircleShape` | botones play, avatares de artista |
| Elevación | Plano por defecto: nivel por **tono** (`surfaceContainer*`), no sombra. Sombra solo carátula (12–20 dp, spot = accent) y botón play (8 dp). |
| Espaciado (escala) | 4 · 8 · 12 · **16** (margen estándar) · 24 · 32 |
| Margen horizontal pantalla | 16 dp (24 dp en reproductor completo) |
| Altura fila de pista | 64–72 dp; miniatura 48 dp; gap 12 dp |
| Mini player | alto 64 dp, margen 8 dp horizontal, radio 16 dp |
| Objetivo táctil mínimo | **48×48 dp** (Android) y ≥ 8 dp entre objetivos |
| Safe areas | `WindowInsets.navigationBars/statusBars` siempre respetados; mini player flota sobre la barra de navegación |

## 6. Movimiento (`--motion 5`)

| Interacción | Spec |
|---|---|
| Press feedback | escala 0.96, `spring(stiffness = High)` ≈ 100 ms |
| Play ↔ Pause | `AnimatedContent` + `Crossfade` 150 ms |
| Mini → Full | `SharedTransition`/shared bounds de carátula, `spring(dampingRatio = 0.8, stiffness = MediumLow)` |
| Cambio de pista | Crossfade carátula 300 ms + fade del gradiente dinámico 400 ms (`animateColorAsState`) |
| Entrada de lista | stagger ≤ 40 ms/ítem, solo primeros 8, nunca en scroll |
| Salida | 60–70 % de la duración de entrada |
| Reduced motion | si `Settings.Global.ANIMATOR_DURATION_SCALE == 0` → sin animaciones no esenciales |
| Prohibido | animar `width/height` (usar `graphicsLayer`), animaciones infinitas fuera de pantalla, visualizador con el reproductor cerrado |

## 7. Contratos de micro-estados

Todo componente interactivo implementa: **default · pressed · focused · disabled · loading · error · selected**.

| Componente | Estados obligatorios |
|---|---|
| `TrackRow` | default, **nowPlaying** (título en `primary` + ecualizador animado, `isPlaying` pausa la animación), pressed, loading (shimmer: cover 48 dp + 2 líneas), sin carátula (placeholder `MusicNote` tonal) |
| `MiniPlayer` | hidden (sin pista), playing, paused, loading-cover (shimmer), progreso 3 dp; swipe ↔ cambia pista; tap abre full |
| `PlayerControls` | play/pause 72 dp, prev/next 56 dp, secundarios 48 dp; seleccionados (shuffle/repeat) = `accent` + `stateDescription` |
| `SeekBar` | zona táctil ≥ 48 dp, thumb visible solo al arrastrar, muestra tiempo al arrastrar, `semantics { stateDescription = "1:23 de 3:45" }` |
| `HiResBadge` | `HI-RES 24/96`, dorado, `contentDescription = "Audio de alta resolución, 24 bits, 96 kilohercios"` |
| `Shimmer` | `Brush.linearGradient` animado sobre `surfaceVariant`→`surfaceContainer`, 1200 ms lineal, reserva exacta del tamaño final (CLS 0), se desactiva con reduced motion |
| `EmptyState` | ícono + título + acción ("Escanear biblioteca") |
| `ErrorState` | mensaje junto al origen + reintentar |

## 8. Accesibilidad (obligatorio)

- Contraste texto ≥ 4.5:1, íconos/controles ≥ 3:1, también sobre el color dinámico.
- Todo ícono accionable con `contentDescription` **que refleje el estado** ("Pausar"/"Reproducir"), decorativos `null`.
- Carátulas informativas: `contentDescription = "Carátula de {álbum}"`; decorativas: `null`.
- Orden de foco TalkBack: título → artista → controles → seek.
- Soporte de fuente grande (sin alturas fijas en filas de texto), modo claro/oscuro, `Role.Button`/`Role.Slider` en superficies clicables.

## 9. Arquitectura de componentes

```
ui/theme/        → Color.kt, Theme.kt, Type.kt, Shape.kt (nuevo), Dimens.kt (nuevo), SoundFlowColors (extensión: hiRes, accent)
presentation/components/
  common/        → Shimmer.kt, HiResBadge.kt, CoverImage.kt, EqualizerBars.kt, EmptyState.kt
  items/         → TrackRow.kt (stateless)
  player/        → MiniPlayer.kt, PlayerControls.kt, SeekBar.kt, NowPlayingArt.kt
```
Reglas: componentes **stateless** (state hoisting), reciben modelos inmutables (`@Immutable`/`@Stable`), sin acceso a repositorios ni `Context` de datos; progreso por `() -> Float` lambda para evitar recomposición total; `LazyColumn` con `key = song.id` y `contentType`.

## 10. Anti-patrones
Hex crudo en composables · iconos sin etiqueta · spinner global · objetivos < 48 dp · animación de layout · color dinámico sin validar contraste · lógica de negocio en UI · botones que no hacen lo que dice su ícono.

## 11. Checklist previo a entrega
- [ ] Sin `Color(0x…)`/`Color.White` en composables
- [ ] Objetivos ≥ 48 dp, gap ≥ 8 dp
- [ ] Contraste verificado (claro/oscuro/dinámico)
- [ ] Estado de carga con shimmer y tamaño reservado
- [ ] `contentDescription` correcto y dinámico
- [ ] Probado con fuente 200 % y animaciones desactivadas
- [ ] Listas con `key`, sin jank (Layout Inspector → recomposiciones)
