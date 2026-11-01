-- =====================================================================
-- Consultas de base de datos de la Práctica 1 (casos P-00, P-25 a P-29, P-35 y P-37)
-- Se ejecutan en psql conectado a dbgestor, copiando cada bloque en el momento indicado.
-- Los registros de prueba son los que crea la colección de Postman
-- (docs/postman/Gestor_Practica1.postman_collection.json).
-- =====================================================================

-- ---------- P-00 Conexión (antes de empezar) ----------
\dt
SELECT
    current_database() AS base,
    current_user AS usuario,
    inet_server_port() AS puerto,
    version() AS servidor;

-- ---------- P-25 a P-29: después de P-24 y ANTES de las eliminaciones ----------
-- P-25 usuario creado (P-01) y modificado (P-06)
SELECT
    id_usuario,
    nombre,
    correo_electronico,
    contrasena,
    rol,
    imagen_usuario
FROM usuario
WHERE correo_electronico LIKE 'prueba.%@cbtis75.edu.mx';

-- P-26 proyecto creado (P-07) y modificado (P-10)
SELECT
    id_proyecto,
    nombre,
    descripcion,
    fecha_inicio,
    fecha_finalizacion,
    estado_proyecto,
    fondo
FROM proyectos
WHERE nombre LIKE 'Proyecto Prueba Postman%';

-- P-27 tarea creada (P-11) y modificada (P-15)
SELECT
    id_tarea,
    nombre,
    descripcion,
    fecha_vencimiento,
    estado,
    lista,
    id_proyecto
FROM tareas
WHERE nombre = 'Tarea Prueba Postman';

-- P-28 comentario creado (P-16) y modificado (P-20)
SELECT
    id_comentario,
    texto_comentario,
    fecha_hora,
    id_usuario,
    id_tarea
FROM comentarios
WHERE texto_comentario LIKE 'Comentario Prueba Postman%';

-- P-29 asignación creada (P-21) y modificada (P-24)
SELECT
    a.id_asignacion,
    a.fecha_asignacion,
    u.nombre AS usuario,
    t.nombre AS tarea
FROM asignaciones AS a
INNER JOIN usuario AS u ON a.id_usuario = u.id_usuario
INNER JOIN tareas AS t ON a.id_tarea = t.id_tarea
WHERE u.correo_electronico LIKE 'prueba.%@cbtis75.edu.mx';

-- ---------- P-35: después de P-30 a P-34 (eliminaciones) ----------
-- Todos los conteos deben ser 0
SELECT
    'usuario' AS tabla,
    count(*) AS registros
FROM usuario
WHERE correo_electronico LIKE 'prueba.%@cbtis75.edu.mx'
UNION ALL
SELECT
    'proyectos' AS tabla,
    count(*) AS registros
FROM proyectos
WHERE nombre LIKE 'Proyecto Prueba Postman%'
UNION ALL
SELECT
    'tareas' AS tabla,
    count(*) AS registros
FROM tareas
WHERE nombre = 'Tarea Prueba Postman'
UNION ALL
SELECT
    'comentarios' AS tabla,
    count(*) AS registros
FROM comentarios
WHERE texto_comentario LIKE 'Comentario Prueba Postman%'
UNION ALL
SELECT
    'asignaciones' AS tabla,
    count(*) AS registros
FROM asignaciones
WHERE fecha_asignacion = '2026-10-08';

-- ---------- P-37: verificación del update con id inexistente ----------
-- ANTES de la corrección: aparece 1 fila (el update CREÓ un usuario nuevo).
-- DESPUÉS de la corrección: (0 filas).
SELECT
    id_usuario,
    nombre,
    correo_electronico,
    contrasena,
    rol,
    imagen_usuario
FROM usuario
WHERE correo_electronico = 'no.existe@prueba.mx';

-- Limpieza del registro que se creó por la falla (ejecutar después de tomar la captura "antes")
DELETE FROM usuario
WHERE correo_electronico = 'no.existe@prueba.mx';
