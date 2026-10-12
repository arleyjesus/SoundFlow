# Arquitectura de SoundFlow

## Organizacion actual

Codigo principal:
com.example.comarleyaetheraudio

### data/local

- MusicDatabase
- DAO de musica y playlists
- Entidades Room
- Utilidades de escaneo, etiquetas, letras y portadas

### data/player

- AudioPlayerHandler
- EqualizerHandler
- SoundFlowMediaService

### data/remote

- GitHubUpdateChecker
- LrcLibService

### data/repository

- AudioRepositoryImpl
- SettingsRepository

### domain

- Modelos Song, Playlist, AudioPlayerState y LyricLine
- Contratos
- Casos de uso

### presentation

- Pantallas y componentes Compose
- ViewModels
- Navegacion
- Reproductor, biblioteca, carpetas y ajustes

### ui/theme

- Temas, colores y tipografia

## Reglas de arquitectura

1. Mantener responsabilidades claras entre capas.
2. Evitar duplicar estados que puedan observarse desde Room.
3. Las operaciones relacionadas deben ser transaccionales.
4. No hacer trabajo intensivo en el hilo principal.
5. No introducir dependencias innecesarias en ViewModels.
6. Mantener contratos compatibles durante cada fase.
7. Escribir pruebas para cambios que afecten datos.

## Decisiones vigentes

- Mantener el nombre de BD: soundflow_database.
- Conservar el esquema Room version 2 inicialmente.
- Evitar modificaciones de entidades sin migraciones.
- Mantener el paquete real de MusicDao: data.local.
- No mover paquetes solo para igualar carpetas fisicas.
- Trabajar sobre C:\SoundFlow-Git.
- No copiar archivos de la carpeta antigua.
- Apertura sin recreacion destructiva de Room: MusicDatabase.buildDatabase
  comparte configuracion con las pruebas aisladas. Ante incompatibilidad,
  conservar los datos y rechazar apertura hasta definir recuperacion.
- Politica y limites de compatibilidad en 06-POLITICA-DE-PERSISTENCIA.md.

## Decisiones pendientes

- Identidad estable de canciones.
- Implementacion de migraciones historicas con fixtures reales; estrategia
  de preservacion definida en fase 1, compatibilidad historica no resuelta.
- Uso de indices y restricciones de unicidad.
- Manejo de referencias cuando desaparecen canciones.
- Estrategia de cache y persistencia de preferencias.
- Diseño definitivo de servicios e interfaces de dominio.
