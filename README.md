# 🎵 Aether Audio Player

Un reproductor de audio local enfocado en **alta fidelidad (Hi-Res Audio)** para Android. Diseñado bajo los principios de **Clean Architecture**, **MVVM** y componentes declarativos con **Jetpack Compose**.

![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple?logo=kotlin)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-blue)
![Media3](https://img.shields.io/badge/Audio-Jetpack%20Media3%2FExoPlayer-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## 🚀 Características Principales

* **Soporte Hi-Res Lossless:** Detección automática de tasa de muestreo (Sample Rate) y Bitrate en formatos FLAC, WAV, ALAC y MP3 con indicador visual en pantalla.
* **Motor Multimedia Nacio:** Basado en `Jetpack Media3 (ExoPlayer)` optimizado para procesamiento de audio en bajo nivel.
* **Reproducción en Segundo Plano:** Implementación de `MediaSessionService` con control multimedia interactivo en la barra de notificaciones y pantalla de bloqueo.
* **Protección contra Interrupciones:** Pausa automática al desconectar auriculares (`Audio Becoming Noisy`).
* **Interfaz Moderna:** Desarrollada 100% en `Jetpack Compose` siguiendo la guía de Material Design 3.

---

## 🛠️ Arquitectura de Software

El proyecto aplica estrictamente **Clean Architecture** dividido en tres capas fundamentales para garantizar la separación de responsabilidades, mantenibilidad y escalabilidad:

```text
com.arleyaetheraudio/
 ├── data/               # Implementación de escáner (MediaStore), ExoPlayer y Servicios
 │    ├── local/         # MediaStoreAudioScanner (Extracción de metadatos)
 │    ├── player/        # AudioPlayerHandler & AudioService (Media3)
 │    └── repository/    # AudioRepositoryImpl
 ├── domain/             # Reglas de negocio puras (Modelos e Interfaces)
 │    ├── model/         # Song & AudioPlayerState
 │    ├── repository/    # AudioRepository
 │    └── usecase/       # GetLocalAudioFilesUseCase
 └── presentation/       # Interfaz de Usuario (Jetpack Compose)
      ├── library/       # LibraryScreen & LibraryViewModel
      └── components/    # MiniPlayer & Widgets
