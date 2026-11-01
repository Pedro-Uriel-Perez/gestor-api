-- =====================================================================
-- Verificacion automatica de la base dbgestor (pipeline gestor-basedatos)
-- Cada bloque lanza un ERROR si la verificacion falla; con ON_ERROR_STOP=1
-- psql termina con codigo distinto de 0 y Jenkins marca el build en rojo.
-- Uso:
--   psql -v ON_ERROR_STOP=1 -U postgres -h localhost -d dbgestor -f sql/postgres/05_verificacion_integridad.sql
-- =====================================================================
SET client_encoding = 'UTF8';
SET client_min_messages = NOTICE;

-- BD-01 Existen las 5 tablas del sistema
DO $$
DECLARE faltantes TEXT;
BEGIN
    SELECT string_agg(esperada, ', ') INTO faltantes
    FROM unnest(ARRAY['usuario', 'proyectos', 'tareas', 'comentarios', 'asignaciones']) AS esperada
    WHERE NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = esperada
    );
    IF faltantes IS NOT NULL THEN
        RAISE EXCEPTION 'BD-01 FALLA: faltan las tablas %', faltantes;
    END IF;
    RAISE NOTICE 'BD-01 OK: existen las 5 tablas';
END $$;

-- BD-02 Cada tabla tiene llave primaria
DO $$
DECLARE sin_pk TEXT;
BEGIN
    SELECT string_agg(t.table_name, ', ') INTO sin_pk
    FROM information_schema.tables AS t
    WHERE t.table_schema = 'public' AND t.table_type = 'BASE TABLE'
      AND NOT EXISTS (
          SELECT 1 FROM information_schema.table_constraints AS c
          WHERE c.table_schema = 'public' AND c.table_name = t.table_name AND c.constraint_type = 'PRIMARY KEY'
      );
    IF sin_pk IS NOT NULL THEN
        RAISE EXCEPTION 'BD-02 FALLA: tablas sin llave primaria: %', sin_pk;
    END IF;
    RAISE NOTICE 'BD-02 OK: todas las tablas tienen llave primaria';
END $$;

-- BD-03 Existen las 4 llaves foraneas (integridad referencial)
DO $$
DECLARE total INTEGER;
BEGIN
    SELECT count(*) INTO total
    FROM information_schema.table_constraints
    WHERE table_schema = 'public' AND constraint_type = 'FOREIGN KEY'
      AND table_name IN ('tareas', 'comentarios', 'asignaciones');
    IF total < 4 THEN
        RAISE EXCEPTION 'BD-03 FALLA: se esperaban 4 llaves foraneas y hay %', total;
    END IF;
    RAISE NOTICE 'BD-03 OK: % llaves foraneas', total;
END $$;

-- BD-04 Toda llave foranea tiene indice (evita recorridos completos al borrar o unir)
DO $$
DECLARE sin_indice TEXT;
BEGIN
    SELECT string_agg(c.conrelid::REGCLASS || '.' || a.attname, ', ') INTO sin_indice
    FROM pg_constraint AS c
    INNER JOIN pg_attribute AS a ON c.conrelid = a.attrelid AND a.attnum = c.conkey[1]
    WHERE c.contype = 'f'
      AND NOT EXISTS (
          SELECT 1 FROM pg_index AS i
          WHERE i.indrelid = c.conrelid AND i.indkey[0] = c.conkey[1]
      );
    IF sin_indice IS NOT NULL THEN
        RAISE EXCEPTION 'BD-04 FALLA: llaves foraneas sin indice: %', sin_indice;
    END IF;
    RAISE NOTICE 'BD-04 OK: todas las llaves foraneas tienen indice';
END $$;

-- BD-05 No hay registros huerfanos
DO $$
DECLARE huerfanos INTEGER;
BEGIN
    SELECT
        (SELECT count(*) FROM tareas AS t
            WHERE t.id_proyecto IS NOT NULL
              AND NOT EXISTS (SELECT 1 FROM proyectos AS p WHERE p.id_proyecto = t.id_proyecto))
        + (SELECT count(*) FROM comentarios AS c
            WHERE c.id_tarea IS NOT NULL
              AND NOT EXISTS (SELECT 1 FROM tareas AS t WHERE t.id_tarea = c.id_tarea))
        + (SELECT count(*) FROM asignaciones AS a
            WHERE (a.id_tarea IS NOT NULL AND NOT EXISTS (SELECT 1 FROM tareas AS t WHERE t.id_tarea = a.id_tarea))
               OR (a.id_usuario IS NOT NULL
                   AND NOT EXISTS (SELECT 1 FROM usuario AS u WHERE u.id_usuario = a.id_usuario)))
    INTO huerfanos;
    IF huerfanos > 0 THEN
        RAISE EXCEPTION 'BD-05 FALLA: % registros huerfanos', huerfanos;
    END IF;
    RAISE NOTICE 'BD-05 OK: sin registros huerfanos';
END $$;

-- BD-06 Datos iniciales cargados y acentos en UTF-8
DO $$
BEGIN
    IF (SELECT count(*) FROM usuario) <> 3 OR (SELECT count(*) FROM proyectos) <> 1
       OR (SELECT count(*) FROM tareas) <> 1 OR (SELECT count(*) FROM comentarios) <> 2 THEN
        RAISE EXCEPTION 'BD-06 FALLA: los datos iniciales no coinciden con 02_data.sql';
    END IF;
    IF (SELECT nombre FROM usuario WHERE id_usuario = 131) <> 'Ana Paola Ramírez Vázquez' THEN
        RAISE EXCEPTION 'BD-06 FALLA: los acentos no se guardaron correctamente';
    END IF;
    RAISE NOTICE 'BD-06 OK: datos iniciales completos y con acentos';
END $$;

-- BD-07 La secuencia de Hibernate entrega ids mayores a los existentes (no habra llaves duplicadas)
DO $$
DECLARE siguiente BIGINT; maximo INTEGER;
BEGIN
    SELECT CASE WHEN is_called THEN last_value + 1 ELSE last_value END INTO siguiente FROM hibernate_sequence;
    SELECT greatest(
        (SELECT max(id_usuario) FROM usuario), (SELECT max(id_proyecto) FROM proyectos),
        (SELECT max(id_tarea) FROM tareas), (SELECT max(id_comentario) FROM comentarios),
        (SELECT max(id_asignacion) FROM asignaciones)
    ) INTO maximo;
    IF siguiente <= maximo THEN
        RAISE EXCEPTION 'BD-07 FALLA: la secuencia entregaria % pero ya existe el id %', siguiente, maximo;
    END IF;
    RAISE NOTICE 'BD-07 OK: el siguiente id sera % (maximo actual %)', siguiente, maximo;
END $$;

-- BD-08 Control negativo: una tarea con proyecto inexistente es rechazada
DO $$
BEGIN
    BEGIN
        INSERT INTO tareas (nombre, id_proyecto) VALUES ('huerfana', -1);
        RAISE EXCEPTION 'BD-08 FALLA: se acepto una tarea con proyecto inexistente';
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE 'BD-08 OK: la llave foranea rechaza la tarea huerfana';
    END;
END $$;

-- BD-09 Control negativo: una fecha invalida es rechazada
DO $$
BEGIN
    BEGIN
        INSERT INTO tareas (nombre, fecha_vencimiento) VALUES ('fecha mala', 'no-es-fecha');
        RAISE EXCEPTION 'BD-09 FALLA: se acepto una fecha invalida';
    EXCEPTION WHEN invalid_datetime_format THEN
        RAISE NOTICE 'BD-09 OK: la columna DATE rechaza la fecha invalida';
    END;
END $$;

-- BD-10 Borrado en cascada: al borrar un proyecto se borran sus tareas y comentarios (se revierte al final)
BEGIN;
DO $$
BEGIN
    DELETE FROM proyectos WHERE id_proyecto = 197;
    IF (SELECT count(*) FROM tareas WHERE id_proyecto = 197) <> 0
       OR (SELECT count(*) FROM comentarios WHERE id_tarea = 199) <> 0 THEN
        RAISE EXCEPTION 'BD-10 FALLA: quedaron tareas o comentarios del proyecto borrado';
    END IF;
    RAISE NOTICE 'BD-10 OK: el borrado en cascada no deja huerfanos';
END $$;
ROLLBACK;

\echo '=== Verificacion de integridad terminada: 10 de 10 verificaciones OK ==='
