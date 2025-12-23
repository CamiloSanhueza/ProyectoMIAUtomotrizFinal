<?php
// login.php
header('Content-Type: application/json');
require_once 'db_connect.php';

// Recibir datos POST
$usuario = $_POST['usuario'] ?? '';
$password = $_POST['password'] ?? '';

if (empty($usuario) || empty($password)) {
    echo json_encode(["status" => "error", "message" => "Faltan credenciales"]);
    exit;
}

// Consulta segura usando parámetros preparados para evitar SQL Injection
// Consulta segura usando parámetros preparados para evitar SQL Injection
$query = "SELECT * FROM usuarios WHERE correo = $1 AND password = $2";
$result = pg_query_params($dbconn, $query, array($usuario, $password));

if ($result && pg_num_rows($result) > 0) {
    $row = pg_fetch_assoc($result);
    
    // Si se envía un token FCM, actualizarlo en la base de datos
    $fcm_token = $_POST['fcm_token'] ?? null;
    if ($fcm_token) {
        $updateQuery = "UPDATE usuarios SET fcm_token = $1 WHERE IDUsuario = $2";
        pg_query_params($dbconn, $updateQuery, array($fcm_token, $row['idusuario']));
    }

    echo json_encode([
        "status" => "success", 
        "message" => "Login exitoso",
        "rol" => $row['rol']
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Credenciales incorrectas"]);
}

pg_close($dbconn);
?>
