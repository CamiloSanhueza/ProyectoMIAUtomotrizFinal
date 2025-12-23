<?php
/**
 * Archivo de Conexión a Base de Datos.
 *
 * Este script establece la comunicación con el sistema de gestión de base de datos PostgreSQL.
 * Se implementa la interfaz PHP Data Objects (PDO) para garantizar la seguridad mediante
 * sentencias preparadas y la portabilidad del código.
 *
 * Ubicación: controllers/conexion.php
 */

$host = '10.24.48.235';    // Dirección IP del servidor de base de datos (Raspberry Pi).
$db   = 'taller_db';       // Identificador de la base de datos.
$user = 'icinf';           // Credenciales de autenticación: Usuario.
$pass = 'ICINF';           // Credenciales de autenticación: Contraseña.
$port = "5432";            // Puerto de escucha del servicio PostgreSQL.

try {
    // Verificación de la disponibilidad del controlador PDO para PostgreSQL.
    if (!extension_loaded('pdo_pgsql')) {
        die("ERROR CRÍTICO: El controlador PDO PostgreSQL no se encuentra habilitado en la configuración de PHP.");
    }

    // Definición de la cadena de conexión (Data Source Name - DSN).
    $dsn = "pgsql:host=$host;port=$port;dbname=$db;";
    
    // Instanciación del objeto PDO para iniciar la conexión.
    $conexion = new PDO($dsn, $user, $pass, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,      // Configuración de manejo de errores mediante excepciones.
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC  // Configuración del modo de recuperación de datos como arreglos asociativos.
    ]);
    
} catch (PDOException $e) {
    // Registro de errores de conexión en el log del sistema y terminación segura.
    error_log("Error de Conexión a BD: " . $e->getMessage());
    echo "Error al establecer conexión con el sistema de base de datos. Consulte los registros del servidor.";
    exit;
}