<?php
// test_firebase.php
// Script para probar la integración con Firebase

require_once 'FirebaseService.php';

header('Content-Type: application/json');

// Ajusta la ruta a donde tengas el archivo service_account.json
// Si está en la carpeta padre de server_backend:
$jsonPath = __DIR__ . '/../service_account.json';

try {
    $firebase = new FirebaseService($jsonPath);

    // Obtener token (para pruebas puedes enviarlo por GET)
    // Ejemplo: test_firebase.php?token=EL_TOKEN_DEL_DISPOSITIVO
    $token = $_GET['token'] ?? null;

    if ($token) {
        $title = "Prueba de Integración";
        $body = "¡La conexión con Firebase desde PHP funciona!";
        
        $result = $firebase->sendNotification($token, $title, $body, ['tipo' => 'prueba']);
        echo json_encode($result);
    } else {
        echo json_encode([
            "status" => "info",
            "message" => "Servicio iniciado correctamente. Proporciona un 'token' por GET para enviar una notificación de prueba.",
            "json_path" => $jsonPath
        ]);
    }

} catch (Exception $e) {
    echo json_encode([
        "status" => "error",
        "message" => $e->getMessage()
    ]);
}
?>
