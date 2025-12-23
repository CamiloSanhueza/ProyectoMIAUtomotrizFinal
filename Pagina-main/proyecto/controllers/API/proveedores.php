<?php
/**
 * Controlador de Gestión de Proveedores.
 *
 * Administra el catálogo de proveedores de repuestos e insumos.
 * Permite realizar operaciones de mantenimiento (CRUD) sobre la entidad Proveedores.
 *
 * Ubicación: controllers/API/proveedores.php
 */

session_start();
// Importación de configuración y utilidades.
require_once 'functions.php';
require_once '../conexion.php';

// Control de Acceso: Verificación de autenticación y rol de Administrador.
require_auth();
require_role('administrador'); 

$method = $_SERVER['REQUEST_METHOD'];
$data = [];
// Parseo de entrada JSON para operaciones de escritura.
if ($method !== 'GET') {
    $data = json_decode(file_get_contents('php://input'), true);
}

// Enrutamiento de operaciones basado en verbos HTTP (RESTful).
switch($method) {
    case 'GET':
        // Operación de Lectura: Listar o Consultar Proveedor.
        $id = $_GET['id'] ?? null;
        if ($id) {
            $stmt = $conexion->prepare("SELECT * FROM Proveedores WHERE id_proveedor = ?");
            $stmt->execute([$id]);
            $item = $stmt->fetch(PDO::FETCH_ASSOC);
            if ($item) response_json(200, ['success' => true, 'data' => $item]);
            else response_json(404, ['success' => false, 'message' => 'Proveedor no encontrado.']);
        } else {
            $stmt = $conexion->query("SELECT * FROM Proveedores ORDER BY id_proveedor DESC");
            $items = $stmt->fetchAll(PDO::FETCH_ASSOC);
            response_json(200, ['success' => true, 'data' => $items]);
        }
        break;

    // Operación de Creación: Registro de nuevo proveedor.
    case 'POST':
        $nombre = $data['nombre'] ?? '';
        
        if (empty($nombre)) response_json(400, ['success' => false, 'message' => 'El nombre del proveedor es obligatorio.']);

        try {
            $sql = "INSERT INTO Proveedores (nombre, contacto_nombre, telefono, email, direccion) VALUES (?, ?, ?, ?, ?)";
            $stmt = $conexion->prepare($sql);
            if($stmt->execute([
                $nombre,
                $data['contacto_nombre'] ?? null,
                $data['telefono'] ?? null,
                $data['email'] ?? null,
                $data['direccion'] ?? null
            ])) {
                response_json(201, ['success' => true, 'message' => 'Proveedor registrado exitosamente.']);
            } else {
                throw new Exception("Fallo en la operación de inserción.");
            }
        } catch(Exception $e) {
            response_json(500, ['success' => false, 'message' => 'Error Interno: ' . $e->getMessage()]);
        }
        break;

    // Operación de Actualización: Modificación de datos de proveedor.
    case 'PUT':
        $id = $data['id'] ?? null;
        if (!$id) response_json(400, ['success' => false, 'message' => 'Identificador de proveedor requerido.']);

        $updates = [];
        $params = [];
        
        // Generación dinámica de setencias de actualización.
        foreach(['nombre', 'contacto_nombre', 'telefono', 'email', 'direccion'] as $field) {
            if (isset($data[$field])) {
                $updates[] = "$field = ?";
                $params[] = $data[$field];
            }
        }
        
        if(empty($updates)) response_json(400, ['success' => false, 'message' => 'No se detectaron cambios para actualizar.']);
        
        $params[] = $id;

        try {
            $sql = "UPDATE Proveedores SET " . implode(", ", $updates) . " WHERE id_proveedor = ?";
            $stmt = $conexion->prepare($sql);
            if($stmt->execute($params)) response_json(200, ['success' => true, 'message' => 'Datos de proveedor actualizados.']);
            else throw new Exception("Fallo al ejecutar actualización.");
        } catch(Exception $e) {
            response_json(500, ['success' => false, 'message' => 'Error Interno: ' . $e->getMessage()]);
        }
        break;

    // Operación de Eliminación: Baja de proveedor.
    case 'DELETE':
        $id = $_GET['id'] ?? null;
        if (!$id) response_json(400, ['success' => false, 'message' => 'Identificador de proveedor requerido.']);

        try {
            $stmt = $conexion->prepare("DELETE FROM Proveedores WHERE id_proveedor = ?");
            if($stmt->execute([$id])) response_json(200, ['success' => true]);
            else response_json(500, ['success' => false, 'message' => 'Error al eliminar el registro.']);
        } catch(Exception $e) {
            response_json(500, ['success' => false, 'message' => 'Error Interno: ' . $e->getMessage()]);
        }
        break;
}
?>

