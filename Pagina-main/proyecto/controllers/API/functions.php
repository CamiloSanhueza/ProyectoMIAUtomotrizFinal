<?php
/**
 * Biblioteca de Funciones Auxiliares para la API.
 *
 * Contiene funciones comunes para estandarización de respuestas,
 * validación de autenticación y autorización, y servicios externos (FCM).
 *
 * Ubicación: controllers/API/functions.php
 */

// Función auxiliar para emitir respuestas JSON estandarizadas.
function response_json($code, $data) {
    // Limpieza de búfer de salida para prevenir corrupción de JSON.
    if (ob_get_length()) ob_clean();
    
    http_response_code($code);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit();
}

// Verificación de autenticación de usuario.
// Interrumpe la ejecución si no existe una sesión activa.
function require_auth() {
    if (!isset($_SESSION['usuario_id'])) {
        response_json(401, [
            'success' => false,
            'message' => 'Acceso no autorizado. Se requiere inicio de sesión.'
        ]);
    }
}

// Verificación de rol de usuario (Control de Acceso Basado en Roles).
// Requiere que el usuario tenga el rol especificado.
function require_role($role) {
    if (!isset($_SESSION['usuario_rol']) || strtolower($_SESSION['usuario_rol']) !== strtolower($role)) {
        response_json(403, [
            'success' => false,
            'message' => 'Acceso denegado. Se requiere el rol: ' . $role
        ]);
    }
}


// Obtención de token de acceso para Google Firebase Cloud Messaging (FCM).
// Utiliza una cuenta de servicio (JSON Key) para firmar un JWT y solicitar un OAuth2 token.
function get_google_access_token($key_file_path) {
    if (!file_exists($key_file_path)) {
        error_log("FCM Error: Archivo de credenciales no encontrado: " . $key_file_path);
        return false;
    }

    $data = json_decode(file_get_contents($key_file_path), true);
    if (!$data || !isset($data['client_email']) || !isset($data['private_key'])) {
        error_log("FCM Error: Estructura de archivo JSON de credenciales inválida.");
        return false;
    }

    // Codificación de encabezado JWT.
    $header = json_encode(['alg' => 'RS256', 'typ' => 'JWT']);
    
    $now = time();
    $claim = json_encode([
        'iss' => $data['client_email'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud' => 'https://oauth2.googleapis.com/token',
        'exp' => $now + 3600,
        'iat' => $now
    ]);

    // Codificación Base64Url segura para URL.
    $base64Url = function($text) {
        return str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($text));
    };

    // Firma Digital.
    $input = $base64Url($header) . "." . $base64Url($claim);
    $signature = '';
    
    // Firma utilizando OpenSSL y la clave privada de la cuenta de servicio.
    if (!openssl_sign($input, $signature, $data['private_key'], "SHA256")) {
        error_log("FCM Error: Falló la generación de firma OpenSSL.");
        return false;
    }

    $jwt = $input . "." . $base64Url($signature);

    // Solicitud de token de acceso a Google OAuth2.
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'https://oauth2.googleapis.com/token');
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query([
        'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
        'assertion' => $jwt
    ]));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    
    $response = curl_exec($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($http_code !== 200) {
        error_log("FCM Error: Autenticación con Google fallida. Respuesta: " . $response);
        return false;
    }

    $token_data = json_decode($response, true);
    return $token_data['access_token'] ?? false;
}

// Envío de notificaciones push mediante Firebase Cloud Messaging V1.
function send_fcm_notification($token, $title, $body) {
    // Ubicación del archivo de cuenta de servicio.
    $key_path = __DIR__ . '/service_account.json';
    
    // Obtención del Token de Acceso.
    $access_token = get_google_access_token($key_path);
    if (!$access_token) {
        return "Error: No se pudo generar el token de acceso. Verifique configuración de service_account.json.";
    }

    // Extracción de ID de Proyecto.
    $json_data = json_decode(file_get_contents($key_path), true);
    $project_id = $json_data['project_id']; 

    $url = "https://fcm.googleapis.com/v1/projects/$project_id/messages:send";
    
    // Construcción del mensaje.
    $message = [
        'message' => [
            'token' => $token,
            'notification' => [
                'title' => $title,
                'body' => $body
            ]
        ]
    ];
    
    $json_payload = json_encode($message);
    
    $headers = [
        'Content-Type: application/json',
        'Authorization: Bearer ' . $access_token
    ];
    
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $json_payload);
    
    $response = curl_exec($ch);
    $err = curl_error($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    if ($err) {
        error_log("Error enviando FCM: " . $err);
        return $err;
    }
    
    if ($http_code !== 200) {
        error_log("FCM respondió con error ($http_code): " . $response);
        return "Error API: $response";
    }
    
    return true;
}
?>
