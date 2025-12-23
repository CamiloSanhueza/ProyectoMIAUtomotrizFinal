<?php
// FirebaseService.php
// Clase para interactuar con Firebase (FCM) usando Service Account
// No requiere Composer, usa funciones nativas de PHP/OpenSSL

class FirebaseService {
    private $serviceAccountPath;
    private $projectId;
    private $clientEmail;
    private $privateKey;

    public function __construct($jsonPath) {
        $this->serviceAccountPath = $jsonPath;
        $this->loadCredentials();
    }

    private function loadCredentials() {
        if (!file_exists($this->serviceAccountPath)) {
            throw new Exception("Archivo service_account.json no encontrado en: " . $this->serviceAccountPath);
        }

        $content = file_get_contents($this->serviceAccountPath);
        $data = json_decode($content, true);

        if (!$data || !isset($data['project_id'])) {
            throw new Exception("El archivo service_account.json no es válido o está vacío.");
        }

        $this->projectId = $data['project_id'];
        $this->clientEmail = $data['client_email'];
        $this->privateKey = $data['private_key'];
    }

    private function getAccessToken() {
        $header = json_encode(['alg' => 'RS256', 'typ' => 'JWT']);
        $now = time();
        $exp = $now + 3600; // 1 hora de expiración

        $claimSet = json_encode([
            'iss' => $this->clientEmail,
            'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
            'aud' => 'https://oauth2.googleapis.com/token',
            'exp' => $exp,
            'iat' => $now
        ]);

        $base64Header = $this->base64UrlEncode($header);
        $base64ClaimSet = $this->base64UrlEncode($claimSet);

        $signatureInput = $base64Header . "." . $base64ClaimSet;
        $signature = '';

        if (!openssl_sign($signatureInput, $signature, $this->privateKey, 'SHA256')) {
            throw new Exception("Error firmando el JWT.");
        }

        $jwt = $signatureInput . "." . $this->base64UrlEncode($signature);

        // Intercambiar JWT por Access Token
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, 'https://oauth2.googleapis.com/token');
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query([
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion' => $jwt
        ]));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        if ($httpCode != 200) {
            throw new Exception("Error obteniendo Access Token: " . $response);
        }

        $responseData = json_decode($response, true);
        return $responseData['access_token'];
    }

    private function base64UrlEncode($data) {
        return str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($data));
    }

    public function sendNotification($token, $title, $body, $data = []) {
        try {
            $accessToken = $this->getAccessToken();
            $url = "https://fcm.googleapis.com/v1/projects/{$this->projectId}/messages:send";

            $message = [
                'message' => [
                    'token' => $token,
                    'notification' => [
                        'title' => $title,
                        'body' => $body
                    ]
                ]
            ];

            if (!empty($data)) {
                $message['message']['data'] = $data;
            }

            $headers = [
                'Authorization: Bearer ' . $accessToken,
                'Content-Type: application/json'
            ];

            $ch = curl_init();
            curl_setopt($ch, CURLOPT_URL, $url);
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
            curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($message));
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

            $response = curl_exec($ch);
            $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
            
            if (curl_errno($ch)) {
                throw new Exception("Error cURL: " . curl_error($ch));
            }
            
            curl_close($ch);

            return [
                'status' => $httpCode,
                'response' => json_decode($response, true)
            ];

        } catch (Exception $e) {
            return [
                'status' => 500,
                'error' => $e->getMessage()
            ];
        }
    }
}
?>
