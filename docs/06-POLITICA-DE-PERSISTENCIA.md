# Politica de persistencia y compatibilidad Room

Revision: 2026-10-11. Rama: refactor/soundflow-4.

## Objetivo y decision de fase 1

Preservar canciones, carpetas, favoritos y playlists ante una apertura
incompatible. Se conserva soundflow_database, esquema version 2, entidades
y dependencias. No se incorpora ninguna migracion especulativa.

MusicDatabase.buildDatabase centraliza la configuracion usada por produccion
y por las pruebas aisladas. Se retira fallbackToDestructiveMigration():
si falta una migracion, Room rechaza la apertura en lugar de recrear tablas.
Una base incompatible puede impedir el uso de la app hasta que se implemente
una recuperacion verificada. No hay todavia una pantalla de recuperacion.
No capturar ese fallo para borrar la base, reinstalar o reiniciar sus datos.

Esta proteccion no repara una base incompatible ni recupera datos perdidos
anteriormente. Tampoco evita huerfanos por operaciones de escaneo o carpetas,
que corresponden a la fase 2.

## Evidencia del historial local

| Revision | Estado observado | Consecuencia |
|---|---|---|
| 433008d | Esquema 1, nombre aether_music_db, canciones y carpetas | Es otro archivo; no se importa ni renombra automaticamente |
| 61ccd52 | soundflow_database, version 2, favoritos y playlists; SongEntity sin path | Posible base version 2 incompatible con el modelo actual |
| c5c8101 | SongEntity incorpora path y MusicDatabase conserva version 2 | La version por si sola no identifica todos los esquemas historicos |
| eae836d y base a69bd59 | Modelo version 2 con path y cinco entidades actuales | Referencia de trabajo actual; no demuestra todas las instalaciones anteriores |

Estos hallazgos proceden de git show de las clases correspondientes.
El historial del codigo no sustituye muestras de bases reales ni permite
afirmar que cada revision se instalo en dispositivos.

## Matriz de apertura esperada

| Base | Comportamiento esperado | Cobertura escrita |
|---|---|---|
| Nueva o version 2 con identidad actual | Crear o reabrir conservando filas | Reapertura de cinco entidades |
| Version inferior sin migracion | Rechazar sin recreacion destructiva | Version sintetica 1 |
| Version superior sin migracion | Rechazar sin recreacion destructiva | Version sintetica 3 |
| Version 2 con identidad desconocida | Rechazar sin sustituir identidad ni filas | Hash sintetico incompatible |
| Archivo antiguo aether_music_db | No tocarlo automaticamente | Revision documental; importacion pendiente |

DatabaseCompatibilityTest agrega cuatro pruebas instrumentadas. Cambian
version o hash solo en bases aleatorias de prueba, nunca en soundflow_database.
Las pruebas de version sintetica y hash no son migraciones historicas reales.
Las 12 pruebas de fase 1A tambien usan ahora el constructor de produccion.
Total: 16 pruebas nuevas de datos/interfaz/compatibilidad, aun sin ejecutar.

## Requisitos para futuras migraciones

1. Inventariar versiones, columnas, restricciones e identidades de bases
   reales, sin incluir datos personales en el repositorio.
2. Definir el esquema destino y exportar esquemas Room reproducibles.
   exportSchema sigue desactivado en esta intervencion.
3. Disenar migraciones explicitas y probar cada origen soportado con copias
   aisladas. Conservar IDs, contenido y relaciones; revisar huerfanos.
4. Probar apertura, actualizacion, rollback y reapertura; documentar los
   origenes no soportados y una recuperacion que preserve datos.
5. Solo despues aprobar el cambio de entidades o version y su publicacion.

## Validacion y cierre

El usuario compilo y probo satisfactoriamente el APK de fase 1A anterior.
La proteccion nueva y las cuatro pruebas aun no compilaron: Gradle fallo
antes de las tareas por Unable to establish loopback connection, causado
por SocketException: Invalid argument: connect en UnixDomainSockets/PipeImpl.
El intento con IPv4 temporal y sin daemon persistente produjo el mismo fallo.
No se modificaron ajustes globales ni dependencias.

Para validar desde la terminal local que previamente funciono:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest --offline --console=plain --no-configuration-cache
```

Instalar el APK nuevo como actualizacion, conservar datos y comprobar
favoritos y playlists tras forzar cierre y reabrir. No reutilizar el resultado
manual del APK anterior para declarar validado este ajuste.
La instrumentacion permanece aplazada por preferencia del usuario.
Actualizacion posterior del 2026-10-11: el usuario confirma BUILD SUCCESSFUL
in 1m 14s, 77 tareas (15 ejecutadas y 62 actualizadas). APK regenerado y
un test unitario de plantilla aprobado segun el informe XML inspeccionado.
La proteccion ya compilo; no se afirma que la instrumentacion se ejecutara.
La comprobacion manual del ultimo APK sigue pendiente. El usuario autoriza
entrega y publicacion del desarrollo de fase 1 y comenzar fase 2 con estas
limitaciones; compatibilidad historica y validacion exhaustiva no resueltas.
