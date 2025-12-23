<?php
// db_connect.php
// Configuración de la conexión a PostgreSQL

$host = "localhost"; // O la IP local si la BD está en otro contenedor/servidor
$port = "5432";
$dbname = "miautomotriz_db"; // Asegúrate de crear esta BD en Postgres
$user = "postgres"; // Tu usuario de Postgres
$password = "tu_password"; // Tu contraseña de Postgres

$conn_string = "host=$host port=$port dbname=$dbname user=$user password=$password";

$dbconn = pg_connect($conn_string);

if (!$dbconn) {
    // En producción, no mostrar el error detallado al cliente
    echo json_encode(["status" => "error", "message" => "Error de conexión a la base de datos"]);
    exit;
}
?>
