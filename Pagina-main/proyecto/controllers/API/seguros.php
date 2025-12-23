<?php
/**
 * Controlador de Gestión de Seguros.
 *
 * Administra la asociación de pólizas de seguro a los clientes.
 * Permite consultar seguros existentes y registrar nuevas pólizas.
 *
 * Ubicación: controllers/API/seguros.php
 */

session_start();
require_once 'functions.php'; // Utilidad para estandarizar respuestas JSON.
require_once '../conexion.php';

// Verificación de autenticación de usuario.
require_auth();

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    // Consulta de Seguros por Cliente (RUN).
    $run = $_GET['run'] ?? '';
    if (empty($run)) {
        response_json(400, ['success' => false, 'message' => 'El RUN del cliente es obligatorio para la consulta.']);
    }

    try {
        $stmt = $conexion->prepare("SELECT IDSeguro as \"IDSeguro\", EmpresaSeguro as \"EmpresaSeguro\", nombreSeguro as \"nombreSeguro\", numeroPoliza as \"numeroPoliza\" FROM Seguro WHERE RUNCliente = ?");
        $stmt->execute([$run]);
        $seguros = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        response_json(200, ['success' => true, 'data' => $seguros]);
    } catch (Exception $e) {
        error_log("Error Seguros GET: " . $e->getMessage());
        response_json(500, ['success' => false, 'message' => 'Error Interno al recuperar seguros.']);
    }

} elseif ($method === 'POST') {
    // Registro de nueva Póliza de Seguro.
    $input = json_decode(file_get_contents('php://input'), true);
    
    $run = $input['run_cliente'] ?? '';
    $empresa = $input['empresa'] ?? '';
    $poliza = $input['poliza'] ?? '';
    
    // Sanitización y Normalización del Número de Póliza.
    // Se eliminan caracteres no alfanuméricos y se ajusta la longitud para cumplir restricciones de BD (10-20 caracteres).
    $poliza = preg_replace('/[^a-zA-Z0-9]/', '', $poliza);
    $poliza = strtoupper($poliza);
    if (strlen($poliza) < 10) {
        $poliza = str_pad($poliza, 10, '0', STR_PAD_LEFT);
    }
    if (strlen($poliza) > 20) {
        $poliza = substr($poliza, 0, 20);
    }
    // Asignación de valores por defecto para campos opcionales no provistos.
    $telefono = !empty($input['telefono']) ? $input['telefono'] : '999999999'; // Patrón válido genérico.
    $correo = !empty($input['email']) ? $input['email'] : 'sin_info@seguro.cl'; // Correo genérico de sistema.
    $ejecutivo = 'Ejecutivo Asignado';
    $tel_ejec = '999999999';
    $mail_ejec = 'sin_info@ejecutivo.cl';

    if (empty($run) || empty($empresa) || empty($poliza)) {
        response_json(400, ['success' => false, 'message' => 'Datos incompletos (RUN, Empresa y Póliza son obligatorios).']);
    }

    try {
        $sql = "INSERT INTO Seguro (EmpresaSeguro, nombreSeguro, numeroPoliza, correoSeguro, TelefonoSeguro, nombreEjecutivo, TelefonoEjecutivo, correoEjecutivo, RUNCliente) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        $stmt = $conexion->prepare($sql);
        // Se utiliza el nombre de la empresa también como nombre del seguro por defecto.
        $stmt->execute([$empresa, $empresa, $poliza, $correo, $telefono, $ejecutivo, $tel_ejec, $mail_ejec, $run]);
        
        $id = $conexion->lastInsertId();
        response_json(201, ['success' => true, 'data' => ['id' => $id, 'nombre' => "$empresa - $poliza"]]);

    } catch (Exception $e) {
        error_log("Error Seguros POST: " . $e->getMessage());
        response_json(500, ['success' => false, 'message' => 'Error interno al registrar la póliza de seguro.']);
    }

} else {
    response_json(405, ['success' => false, 'message' => 'Método HTTP no permitido.']);
}
?>
