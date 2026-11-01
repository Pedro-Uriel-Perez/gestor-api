-- =====================================================================
-- Esquema de la base de datos dbgestor para PostgreSQL
-- Migrado desde el dump de MariaDB 11.3 (dump-dbgestor-202406261453.sql)
--
-- Uso:
--   psql -U postgres -h localhost -c "CREATE DATABASE dbgestor;"
--   psql -U postgres -h localhost -d dbgestor -f sql/postgres/01_schema.sql
-- =====================================================================

DROP TABLE IF EXISTS asignaciones, comentarios, tareas, proyectos, usuario;
DROP SEQUENCE IF EXISTS hibernate_sequence;

-- Hibernate 5 (GenerationType.AUTO) toma los ids de esta secuencia global,
-- igual que hacia con la tabla hibernate_sequence en MariaDB.
CREATE SEQUENCE hibernate_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE usuario (
    id_usuario INTEGER PRIMARY KEY DEFAULT nextval('hibernate_sequence'),
    nombre VARCHAR(100),
    correo_electronico VARCHAR(100),
    contrasena VARCHAR(100),
    rol VARCHAR(100),
    imagen_usuario VARCHAR(255)
);

CREATE TABLE proyectos (
    id_proyecto INTEGER PRIMARY KEY DEFAULT nextval('hibernate_sequence'),
    nombre VARCHAR(100),
    descripcion VARCHAR(100),
    fecha_inicio DATE,
    fecha_finalizacion DATE,
    estado_proyecto VARCHAR(100),
    fondo VARCHAR(100)
);

-- Llaves foraneas (el esquema original de MariaDB no las tenia y permitia registros huerfanos).
-- ON DELETE CASCADE conserva el comportamiento de la API: al borrar un proyecto se borran sus tareas,
-- y al borrar una tarea, sus comentarios y asignaciones.
CREATE TABLE tareas (
    id_tarea INTEGER PRIMARY KEY DEFAULT nextval('hibernate_sequence'),
    nombre VARCHAR(100),
    descripcion VARCHAR(100),
    fecha_vencimiento DATE,
    estado VARCHAR(255),
    lista VARCHAR(255),
    id_proyecto INTEGER REFERENCES proyectos (id_proyecto) ON DELETE CASCADE
);

-- id_usuario guarda el NOMBRE del usuario que comenta (asi lo envia el cliente web),
-- por eso no es llave foranea a usuario.
CREATE TABLE comentarios (
    id_comentario INTEGER PRIMARY KEY DEFAULT nextval('hibernate_sequence'),
    texto_comentario VARCHAR(250),
    fecha_hora TIMESTAMP,
    id_usuario VARCHAR(100),
    id_tarea INTEGER REFERENCES tareas (id_tarea) ON DELETE CASCADE
);

CREATE TABLE asignaciones (
    id_asignacion INTEGER PRIMARY KEY DEFAULT nextval('hibernate_sequence'),
    id_usuario INTEGER REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    id_tarea INTEGER REFERENCES tareas (id_tarea) ON DELETE CASCADE,
    fecha_asignacion DATE
);

-- Indices sobre las llaves foraneas (PostgreSQL no los crea solo)
CREATE INDEX asignaciones_usuario_fk ON asignaciones (id_usuario);
CREATE INDEX asignaciones_tareas_fk ON asignaciones (id_tarea);
CREATE INDEX tareas_proyecto_idx ON tareas (id_proyecto);
CREATE INDEX comentarios_tarea_idx ON comentarios (id_tarea);
