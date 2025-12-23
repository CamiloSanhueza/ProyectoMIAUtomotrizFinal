<?php
/**
 * Controlador de Autenticación.
 *
 * Este script gestiona los procesos de autenticación y autorización de usuarios en el sistema.
 * Incluye funcionalidades para el inicio de sesión, registro de nuevos usuarios y cierre de sesión.
 *
 * Ubicación: controllers/API/auth.php
 */

session_start();
// Importación de configuración de base de datos y biblioteca de funciones auxiliares.
require_once '../conexion.php';
require_once 'functions.php'; // Utilidad para estandarizar respuestas JSON via response_json.

// Determinación de la acción solicitada por el cliente.
$action = $_POST['action'] ?? $_GET['action'] ?? '';

// Proceso de Cierre de Sesión (Logout).
if ($action === 'logout') {
    // Eliminación de cookies de sesión si están habilitadas.
    if (ini_get("session.use_cookies")) {
        $params = session_get_cookie_params();
        setcookie(session_name(), '', time() - 42000,
            $params["path"], $params["domain"],
            $params["secure"], $params["httponly"]
        );
    }

    // Destrucción de la sesión actual.
    session_destroy();
    
    // Redireccionamiento a la vista de inicio de sesión.
    header('Location: ../../views/login.html');
    exit();
}

// Verificación del estado de la sesión activa.
if ($action === 'check_session') {
    if (isset($_SESSION['usuario_id'])) {
        response_json(200, [
            'success' => true,
            'user' => [
                'id' => $_SESSION['usuario_id'],
                'correo' => $_SESSION['usuario_correo'] ?? '',
                'rol' => strtolower($_SESSION['usuario_rol'] ?? '')
            ]
        ]);
    } else {
        response_json(200, ['success' => false, 'message' => 'No session']);
    }
}

// Proceso de Inicio de Sesión (Login).
if ($action === 'login') {
    $email = trim($_POST['email'] ?? '');
    $password = $_POST['password'] ?? '';

    $remember = isset($_POST['remember']);
    // Normalización del rol solicitado a minúsculas para comparaciones consistentes.
    $requested_role = isset($_POST['user_role']) ? strtolower(trim($_POST['user_role'])) : '';

    // Validación de campos obligatorios.
    if (empty($email) || empty($password)) {

        header('Location: ../../views/login.html?error=' . urlencode('Complete todos los campos'));
        exit();
    }

    try {
        // Búsqueda de usuario existente por correo electrónico.
        $sql = "SELECT IDUsuario, correo, password, rol FROM usuarios WHERE correo = ?";
        $stmt = $conexion->prepare($sql);
        $stmt->execute([$email]);
        
        if ($stmt->rowCount() === 0) {
            error_log("Fallo de Autenticación: Usuario no encontrado para el correo $email");

            header('Location: ../../views/login.html?error=' . urlencode('Credenciales incorrectas'));
            exit();
        }

        $usuario = $stmt->fetch(PDO::FETCH_ASSOC);

        // Verificación de la contraseña utilizando comparacion segura de hash.
        if (!password_verify($password, $usuario['password'])) {
            error_log("Fallo de Autenticación: Contraseña inválida para el correo $email");

            header('Location: ../../views/login.html?error=' . urlencode('Credenciales incorrectas'));
            exit();
        }

        // Normalización del rol almacenado en base de datos.
        $db_role = strtolower(trim($usuario['rol'])); // 'administrador' o 'mecanico'

        // Verificación de correspondencia de roles (Seguridad).
        if (!empty($requested_role)) {

            if ($db_role !== $requested_role) {
                error_log("Acceso Denegado: Discrepancia de rol. El usuario es $db_role pero intentó acceder como $requested_role");
                // Mensaje por seguridad
                header('Location: ../../views/login.html?error=' . urlencode('Credenciales incorrectas'));
                exit();
            }
        }

        // Inicialización y almacenamiento de datos en la sesión.
        $_SESSION['usuario_id'] = $usuario['idusuario'];
        $_SESSION['usuario_correo'] = $usuario['correo'];
        $_SESSION['usuario_rol'] = $db_role;

        // Configuración de persistencia de sesión ("Recuérdame").
        if ($remember) {
            setcookie('user_email', $email, time() + (86400 * 30), "/"); // Validez de 30 días.
        }

        // Registro de depuración.
        error_log("Autenticación Exitosa: Usuario $email con rol $db_role. Redirigiendo...");

        // Finalización de escritura de sesión.
        session_write_close();

        // Redireccionamiento según el rol del usuario.
        if ($db_role === 'administrador') {
            header('Location: ../../views/Admin/dashboard-admin.html');
        } else {
            header('Location: ../../views/Trabajador/dashboard-trabajador.html');
        }
        exit();

    } catch (Exception $e) {
        error_log("EXCEPCIÓN DE DEPURACIÓN: " . $e->getMessage());
        header('Location: ../../views/login.html?error=' . urlencode('Ocurrió un problema en el servidor. Intente más tarde.'));
        exit();
    }
}

// Proceso de Registro de Usuario.
if ($action === 'register') {
    $email = trim($_POST['email'] ?? '');
    $password = $_POST['contraseña'] ?? '';
    $confirm_password = $_POST['confirm_contraseña'] ?? '';

    // Datos Adicionales del Usuario.
    $nombre = trim($_POST['nombre'] ?? '');
    $apellido = trim($_POST['apellido'] ?? '');
    $rut = trim($_POST['rut'] ?? '');
    $telefono = trim($_POST['telefono'] ?? '');

    // Validación de integridad del formulario (Nombre, Apellido y RUT obligatorios).
    if (empty($email) || empty($password) || empty($confirm_password) || empty($nombre) || empty($apellido) || empty($rut)) {
        header('Location: ../../views/registro_usuario.html?error=' . urlencode('Complete todos los campos obligatorios') . '&email_input=' . urlencode($email));
        exit();
    }

    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        header('Location: ../../views/registro_usuario.html?error=' . urlencode('Correo electrónico inválido') . '&email_input=' . urlencode($email));
        exit();
    }

    if (strlen($password) < 8) {
        header('Location: ../../views/registro_usuario.html?error=' . urlencode('La contraseña debe tener al menos 8 caracteres') . '&email_input=' . urlencode($email));
        exit();
    }

    if ($password !== $confirm_password) {
        header('Location: ../../views/registro_usuario.html?error=' . urlencode('Las contraseñas no coinciden') . '&email_input=' . urlencode($email));
        exit();
    }

    try {
        // Verificación de existencia previa del usuario.
        $stmt = $conexion->prepare("SELECT IDUsuario FROM usuarios WHERE correo = ?");
        $stmt->execute([$email]);

        if ($stmt->rowCount() > 0) {
            header('Location: ../../views/registro_usuario.html?error=' . urlencode('El correo electrónico ya está registrado') . '&email_input=' . urlencode($email));
            exit();
        }

        // Verificación de existencia del RUT (Validación recomendada).
        $stmt = $conexion->prepare("SELECT IDUsuario FROM usuarios WHERE rut = ?");
        $stmt->execute([$rut]);
        if ($stmt->rowCount() > 0) {
            header('Location: ../../views/registro_usuario.html?error=' . urlencode('El RUT ya está registrado') . '&email_input=' . urlencode($email));
            exit();
        }

        // Encriptación de contraseña mediante algoritmo hash seguro.
        $password_hash = password_hash($password, PASSWORD_DEFAULT);
        
        // Asignación automática de rol basada en el dominio del correo electrónico.
        if (preg_match('/@taller\.cl$/i', $email)) {
            $rol_default = 'Administrador';
        } elseif (preg_match('/@gmail\.cl$/i', $email)) {
            $rol_default = 'Mecanico';
        } else {
            $rol_default = 'Mecanico';
        }

        // Inserción del nuevo usuario en la base de datos.
        $stmt = $conexion->prepare("INSERT INTO usuarios (correo, password, rol, nombre, apellido, rut, telefono) VALUES (?, ?, ?, ?, ?, ?, ?)");
        
        if ($stmt->execute([$email, $password_hash, $rol_default, $nombre, $apellido, $rut, $telefono])) {
            // Registro exitoso.
            header('Location: ../../views/login.html?registered=true');
            exit();
        } else {
            throw new Exception("Error al crear el usuario");
        }

    } catch (PDOException $e) {
        $msg = $e->getMessage();
        error_log("DB Error: " . $msg); // Log interno real
        
        // 1. Manejo de error específico: RUT inválido (Reportado por Trigger de Base de Datos).
        if (strpos($msg, 'RUT inválido') !== false) {
             header('Location: ../../views/registro_usuario.html?error=' . urlencode('El RUT ingresado no es válido. Revise el dígito verificador.') . '&email_input=' . urlencode($email));
             exit();
        }
        
        // 2. Manejo de error específico: Duplicidad de claves (Violación de Constraint).
        if (strpos($msg, 'duplicate key') !== false) {
             header('Location: ../../views/registro_usuario.html?error=' . urlencode('El correo o RUT ya están registrados en el sistema.') . '&email_input=' . urlencode($email));
             exit();
        }

        // Manejo de error genérico de base de datos.
        header('Location: ../../views/registro_usuario.html?error=' . urlencode('Error interno del servidor. Por favor intente nuevamente.') . '&email_input=' . urlencode($email));
        exit();

    } catch (Exception $e) {
        error_log("General Error: " . $e->getMessage());
        // Manejo de excepciones generales del sistema.
        header('Location: ../../views/registro_usuario.html?error=' . urlencode('Error inesperado del sistema.') . '&email_input=' . urlencode($email));
        exit();
    }
}

// Manejo de acción no válida o no reconocida.
header('Location: ../../views/login.html?error=' . urlencode('Acción no válida'));
exit();
