# Plan de pruebas

## Reglas

Una compilacion correcta no demuestra funcionamiento.

Cada fase debe probarse antes de darse por terminada.
Usar biblioteca de prueba o copia de seguridad cuando
haya operaciones que puedan alterar datos.

## Compilacion

Desde la raiz del repositorio:

.\gradlew.bat :app:assembleDebug

Registrar:
- Fecha
- Rama y commit
- Resultado de la compilacion
- Advertencias relevantes

## Persistencia

- [ ] Marcar un favorito
- [ ] Cerrar completamente la app
- [ ] Abrirla y comprobar el favorito
- [ ] Desmarcar y repetir prueba
- [ ] Crear playlist con canciones
- [ ] Renombrar y verificar createdAt
- [ ] Eliminar playlist sin borrar canciones
- [ ] Verificar ausencia de relaciones huerfanas

## Biblioteca

- [ ] Agregar carpeta SAF
- [ ] Escanear dos veces
- [ ] Verificar ausencia de duplicados
- [ ] Comprobar estabilidad de IDs
- [ ] Verificar favoritos despues del escaneo
- [ ] Verificar canciones de playlists
- [ ] Simular carpeta vacia
- [ ] Simular permiso revocado
- [ ] Eliminar una carpeta
- [ ] Verificar que otras carpetas no se alteren

## Reproductor

- [ ] Reproducir, pausar, adelantar y retroceder
- [ ] Cambiar canciones
- [ ] Reproducir en segundo plano
- [ ] Probar notificaciones y pantalla bloqueada
- [ ] Desconectar auriculares
- [ ] Probar interrupciones de audio
- [ ] Probar formatos compatibles

## Rendimiento

- [ ] Biblioteca pequena
- [ ] Biblioteca grande
- [ ] Revisar consumo de memoria
- [ ] Revisar tiempos de escaneo
- [ ] Revisar fluidez de navegacion
- [ ] Revisar rendimiento de caratulas

## Criterios de cierre de una fase

- Compilacion correcta
- Pruebas principales satisfactorias
- Sin regresiones criticas conocidas
- Documentacion actualizada
- Commit realizado
- Push confirmado a GitHub
