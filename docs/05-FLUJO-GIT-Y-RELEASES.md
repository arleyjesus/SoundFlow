# Flujo Git y publicaciones

## Repositorio

https://github.com/arleyjesus/SoundFlow

Rama de desarrollo:
refactor/soundflow-4

La rama main debe conservarse estable.

## Ciclo de cada fase

1. Definir objetivos.
2. Revisar clases relacionadas.
3. Preparar archivos completos.
4. Compilar.
5. Ejecutar pruebas.
6. Corregir errores.
7. Actualizar documentacion y changelog.
8. Revisar git diff y git status.
9. Crear commit descriptivo.
10. Publicar la rama en GitHub.
11. Continuar con la fase siguiente.

## Comandos habituales

Consultar rama:
git branch --show-current

Consultar cambios:
git status

Revisar diferencias:
git diff

Preparar archivos concretos:
git add ruta/del/archivo

Crear commit:
git commit -m "fix(library): describe el cambio"

Publicar rama:
git push -u origin refactor/soundflow-4

## Convenciones de commits

docs: documentacion
fix: correccion
feat: nueva funcionalidad
refactor: reorganizacion interna
test: pruebas
perf: rendimiento
build: configuracion de compilacion

## Reglas de seguridad

- Nunca publicar credenciales ni claves.
- Revisar archivos antes de git add.
- No incluir reportes temporales.
- No hacer force push sin justificacion.
- No mezclar cambios de multiples fases sin necesidad.
- No integrar a main hasta validar la fase.
- Usar Pull Request para integrar cambios importantes.

## Versiones

Los commits no son necesariamente nuevas versiones publicas.

La version publica 4.0 se preparara cuando todas las
funciones seleccionadas superen las pruebas finales.
