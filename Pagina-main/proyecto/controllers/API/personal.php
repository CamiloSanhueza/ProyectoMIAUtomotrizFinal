<?php
/**
 * Controlador de Gestión de Personal.
 *
 * Módulo administrativo para la gestión de usuarios del sistema (Mecánicos y Administradores).
 * Permite listar, crear, modificar y eliminar cuentas de usuario.
 *
 * Ubicación: controllers/API/personal.php
 */

session_start();
// Importación de configuración y funciones auxiliares.
require_once 'functions.php';
require_once '../conexion.php';

// Control de Acceso: Verificación de sesión activa.
require_auth();

// Control de Acceso: Verificación de Rol (Solo Administradores).
if (($_SESSION['usuario_rol'] ?? '') !== 'administrador') {
    response_json(403, ['success' => false, 'message' => 'Acceso Denegado: Privilegios insuficientes.']);
}

$method = $_SERVER['REQUEST_METHOD'];

// Procesamiento de carga útil JSON para solicitudes de escritura.
$data = [];
if ($method !== 'GET') {
    $data = json_decode(file_get_contents('php://input'), true);
}

switch($method) {
    case 'GET':
        // Operación de Lectura: Obtención de información de personal.
        $id = $_GET['id'] ?? null;
        if ($id) {
            // Búsqueda de usuario específico por ID.
            $stmt = $conexion->prepare("SELECT IDUsuario as id, correo, rol, nombre, apellido, telefono, rut FROM usuarios WHERE IDUsuario = ?");
            $stmt->execute([$id]);
            $user = $stmt->fetch(PDO::FETCH_ASSOC);
            if ($user) response_json(200, ['success' => true, 'data' => $user]);
            else response_json(404, ['success' => false, 'message' => 'Usuario no encontrado en el sistema.']);
        } else {
            // Listado general de usuarios (con opción de filtrado por rol).
            $rol_filter = $_GET['rol'] ?? null;
            $sql = "SELECT IDUsuario as id, correo, rol, nombre, apellido, telefono, rut FROM usuarios";
            $params = [];
            
            if ($rol_filter) {
                $sql .= " WHERE LOWER(rol) = LOWER(?)";
                $params[] = $rol_filter;
            }
            
            $sql .= " ORDER BY id DESC";
            
            $stmt = $conexion->prepare($sql);
            $stmt->execute($params);
            $users = $stmt->fetchAll(PDO::FETCH_ASSOC);
            response_json(200, ['success' => true, 'data' => $users]);
        }
        break;

    case 'POST':
        // Operación de Creación: Registro de nuevo miembro del personal.
        $email = $data['email'] ?? '';
        $pass = $data['password'] ?? '';
        $rol = $data['rol'] ?? 'Mecanico';
        $nombre = $data['nombre'] ?? '';
        $apellido = $data['apellido'] ?? '';
        $rut = $data['rut'] ?? '';
        
        // Validación de campos críticos.
        if (empty($email) || empty($pass)) {
            response_json(400, ['success' => false, 'message' => 'Los campos Correo Electrónico y Contraseña son obligatorios.']);
        }
        
        // Verificación de unicidad del correo electrónico.
        $stmt = $conexion->prepare("SELECT IDUsuario FROM usuarios WHERE correo = ?");
        $stmt->execute([$email]);
        if($stmt->rowCount() > 0) response_json(400, ['success' => false, 'message' => 'El correo electrónico ya se encuentra registrado.']);

        // Hashing seguro de la contraseña.
        $hash = password_hash($pass, PASSWORD_DEFAULT);
        
        try {
            $sql = "INSERT INTO usuarios (correo, password, rol, nombre, apellido, rut) VALUES (?, ?, ?, ?, ?, ?)";
            $stmt = $conexion->prepare($sql);
            if($stmt->execute([$email, $hash, $rol, $nombre, $apellido, $rut])) {
                response_json(201, ['success' => true, 'message' => 'Usuario registrado exitosamente.']);
            } else {
                throw new Exception("Fallo en la inserción de datos.");
            }
        } catch(Exception $e) {
            response_json(500, ['success' => false, 'message' => 'Error Interno: ' . $e->getMessage()]);
        }
        break;

    case 'PUT':
        // Operación de Actualización: Modificación de datos de usuario.
        $id = $data['id'] ?? null;
        if (!$id) response_json(400, ['success' => false, 'message' => 'Identificador de usuario requerido.']);

        $updates = [];
        $params = [];

        // Construcción dinámica de la consulta de actualización.
        if(isset($data['nombre'])) { $updates[] = "nombre = ?"; $params[] = $data['nombre']; }
        if(isset($data['apellido'])) { $updates[] = "apellido = ?"; $params[] = $data['apellido']; }
        if(isset($data['telefono'])) { $updates[] = "telefono = ?"; $params[] = $data['telefono']; }
        if(isset($data['rut'])) { $updates[] = "rut = ?"; $params[] = $data['rut']; }
        if(isset($data['rol'])) { $updates[] = "rol = ?"; $params[] = $data['rol']; }
        if(isset($data['email'])) { $updates[] = "correo = ?"; $params[] = $data['email']; }
        
        // Actualización condicional de contraseña (solo si se proporciona una nueva).
        if(!empty($data['password'])) {
            $updates[] = "password = ?";
            $params[] = password_hash($data['password'], PASSWORD_DEFAULT);
        }

        if(empty($updates)) response_json(400, ['success' => false, 'message' => 'No se detectaron cambios para actualizar.']);

        $params[] = $id; // Parámetro para cláusula WHERE
        
        try {
            $sql = "UPDATE usuarios SET " . implode(", ", $updates) . " WHERE IDUsuario = ?";
            $stmt = $conexion->prepare($sql);
            if($stmt->execute($params)) {
                response_json(200, ['success' => true, 'message' => 'Datos de usuario actualizados correctamente.']);
            } else {
                throw new Exception("Fallo al ejecutar actualización.");
            }
        } catch(Exception $e) {
            response_json(500, ['success' => false, 'message' => 'Error Interno: ' . $e->getMessage()]);
        }
        break;

    case 'DELETE':
        // Operación de Eliminación: Baja de usuario.
        $id = $_GET['id'] ?? null;
        if (!$id) response_json(400, ['success' => false, 'message' => 'Identificador de usuario requerido.']);
        
        // Restricción de Seguridad: Prevención de auto-eliminación.
        if ($id == $_SESSION['usuario_id']) response_json(400, ['success' => false, 'message' => 'Operación no permitida: No puede eliminar su propia cuenta activa.']);

        try {
            $stmt = $conexion->prepare("DELETE FROM usuarios WHERE IDUsuario = ?");
            if($stmt->execute([$id])) response_json(200, ['success' => true]);
            else response_json(500, ['success' => false, 'message' => 'Error al eliminar el registro.']);
        } catch(Exception $e) {
            response_json(500, ['success' => false, 'message' => 'Error Interno: ' . $e->getMessage()]);
        }
        break;
}
?>
