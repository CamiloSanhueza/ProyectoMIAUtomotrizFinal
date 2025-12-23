<?php
ob_start();
/**
 * Controlador de Gestión de Clientes.
 *
 * Este módulo implementa la lógica para la administración de la entidad Cliente.
 * Incluye operaciones CRUD (Crear, Leer, Actualizar, Eliminar) y capacidades de búsqueda avanzada.
 *
 * Ubicación: controllers/API/clientes.php
 */

session_start();
// Importación de funciones auxiliares y configuración de conexión a la base de datos.
require_once 'functions.php';
require_once '../conexion.php';

// Verificación de autenticación y autorización del usuario.
require_auth();

// Configuración de parámetros de paginación y criterios de búsqueda.
$limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 1000;
$q = isset($_GET['q']) ? trim($_GET['q']) : '';

$method = $_SERVER['REQUEST_METHOD'];
$data = [];

// Procesamiento de datos de entrada en formato JSON para solicitudes no GET.
if ($method !== 'GET') {
    $input = file_get_contents('php://input');
    $data = json_decode($input, true) ?? [];
    if (empty($data) && !empty($_POST)) {
        $data = $_POST;
    }
}

switch ($method) {
    case 'GET':
        // Operación de Lectura: Recuperación de datos de clientes.
        try {
            // Construcción de consulta SQL con unión a tabla de Direcciones.
            $sql = "SELECT 
                        t1.RUNCliente as id,
                        t1.nombreCliente as nombre,
                        t1.apellidoCliente as apellido,
                        t1.correo,
                        t1.telefonoCliente as telefono,
                        t1.giro,
                        t1.fechaNacimientoCliente as fecha_nacimiento,
                        t2.region,
                        t2.ciudad,
                        t2.calle,
                        t2.numero,
                        t2.poblacion,
                        t2.pasaje,
                        t2.codigoPostal as codigo_postal
                    FROM cliente t1
                    LEFT JOIN Direccion t2 ON t1.codigoPostal = t2.codigoPostal";
        
            // Inicialización de filtros y parámetros.
            $where = [];
            $params = []; 

            // Filtrado por Identificador Único (RUN).
            if (isset($_GET['id'])) {
                $id = $_GET['id'];
                $where[] = "t1.RUNCliente = ?";
                $params[] = $id;
            }
            
            // Lógica de Búsqueda Multicampo.
            if (!empty($q)) {
                $params[] = "%$q%";
                $params[] = "%$q%";
                $params[] = "%$q%";
                $params[] = "%$q%";
                $where[] = "(t1.RUNCliente LIKE ? OR
                            t1.nombreCliente LIKE ? OR
                            t1.apellidoCliente LIKE ? OR
                            t1.correo LIKE ?)";
            }
        
            if (!empty($where)) {
                $sql .= " WHERE " . implode(' AND ', $where);
            }
        
            // Aplicación de ordenamiento y límites.
            $sql .= " ORDER BY t1.RUNCliente DESC LIMIT $limit";
        
            // Ejecución de la consulta preparada.
            $stmt = $conexion->prepare($sql);
            $stmt->execute($params);
        
            $data_res = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
            // Retorno de recurso individual si se especificó ID.
            if (isset($_GET['id'])) {
                if (count($data_res) > 0) {
                    response_json(200, [
                        'success' => true,
                        'data' => $data_res[0]
                    ]);
                } else {
                    response_json(404, [
                        'success' => false,
                        'message' => 'Cliente no encontrado en los registros.'
                    ]);
                }
                exit;
            }
        
            // Retorno de colección de clientes.
            response_json(200, [
                'success' => true,
                'message' => count($data_res) > 0 ? 'Clientes encontrados' : 'No se encontraron clientes',
                'total' => count($data_res),
                'data' => $data_res
            ]);
        
        } catch (Exception $e) {
            error_log("Error Clientes GET: " . $e->getMessage());
            response_json(500, [
                'success' => false,
                'message' => 'Error Interno al obtener clientes.'
            ]);
        }
        break;

    case 'POST':
        require_role('administrador');
        // Operación de Creación: Registro de nuevo cliente.
        try {
            $nombre_completo = $data['nombre'] ?? '';
            $rut = $data['rut'] ?? '';
            $correo = $data['email'] ?? $data['correo'] ?? '';
            $telefono = $data['telefono'] ?? '';
            $giro = $data['giro'] ?? '';
            $fecha_nacimiento = !empty($data['fecha_nacimiento']) ? $data['fecha_nacimiento'] : NULL;
            
            // Datos de Dirección.
            $region = $data['region'] ?? '';
            $ciudad = $data['ciudad'] ?? '';
            $poblacion = $data['poblacion'] ?? '';
            $calle = $data['calle'] ?? '';
            $numero = $data['numero'] ?? '';
            $codigo_postal = $data['codigo_postal'] ?? '';
            $pasaje = $data['pasaje'] ?? '';
            
            // Inicio de transacción para asegurar integridad referencial.
            $conexion->beginTransaction();
            
            // Verificación y/o Creación de Dirección.
            $checkDir = $conexion->prepare("SELECT codigopostal FROM Direccion WHERE codigopostal = ?");
            $checkDir->execute([$codigo_postal]);
            
            if ($checkDir->rowCount() === 0) {
                // Inserción de nueva dirección si no existe.
                $stmtDir = $conexion->prepare("INSERT INTO Direccion (codigopostal, calle, numero, pasaje, poblacion, ciudad, region) VALUES (?, ?, ?, ?, ?, ?, ?)");
                $stmtDir->execute([
                    $codigo_postal,
                    $data['calle'],
                    $data['numero'],
                    $data['pasaje'] ?? '',
                    $data['poblacion'] ?? '',
                    $data['ciudad'],
                    $data['region']
                ]);
            }
            
            // Separación de nombre completo en Nombre y Apellido.
            $parts = explode(' ', trim($nombre_completo));
            $nom = $parts[0] ?? $nombre_completo;
            $ape = count($parts) > 1 ? end($parts) : '';
                
                // Inserción de registro en tabla Cliente.
                $stmtInsert = $conexion->prepare("INSERT INTO cliente (runcliente, nombrecliente, apellidocliente, fechanacimientocliente, telefonocliente, giro, correo, codigopostal) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                
                // Manejo de valores nulos para fecha.
                $fechaNac = !empty($data['fechaNacimiento']) ? $data['fechaNacimiento'] : null;

                if ($stmtInsert->execute([
                    $rut,
                    $nom, 
                    $ape, 
                    $fechaNac, // Admite valor NULL
                    $telefono,
                    $giro,
                    $correo,
                    $codigo_postal // Llave foránea a Dirección
                ])) {
                    $conexion->commit();
                    response_json(201, ['success' => true, 'message' => 'Cliente registrado exitosamente en el sistema.']);
                } else {
                    throw new Exception("Error al persistir el cliente.");
                }
        } catch (Exception $e) {
            // Reversión de cambios en caso de error.
            if ($conexion->inTransaction()) {
                $conexion->rollBack();
            }
            $msg = $e->getMessage();
            error_log("Error Clientes POST: " . $msg);

            // Mensajes seguros para el usuario
            $userMsg = 'Ocurrió un error interno al registrar el cliente.';

            if (strpos($msg, 'Duplicate') !== false) {
                 $userMsg = 'El cliente ya se encuentra registrado (Conflicto de RUN o Correo).';
            } elseif (strpos($msg, 'RUT inválido') !== false) {
                 $userMsg = 'El RUT ingresado no es válido. Revise el dígito verificador.';
            }
            response_json(500, ['success' => false, 'message' => $userMsg]);
        }
        break;

    case 'PUT':
        require_role('administrador');
        // Operación de Actualización: Modificación de cliente existente.
        try {
            $rut = $data['run_original'] ?? $data['rut'] ?? ''; // Identificador inmutable original.
            if (empty($rut)) throw new Exception("Identificador de cliente es requerido para actualización.");
            
            $nombre_completo = $data['nombre'] ?? '';
            $correo = $data['email'] ?? $data['correo'] ?? '';
            $telefono = $data['telefono'] ?? '';
            $giro = $data['giro'] ?? '';
            $fecha_nacimiento = !empty($data['fecha_nacimiento']) ? $data['fecha_nacimiento'] : NULL;
            
            // Datos de Dirección.
            $region = $data['region'] ?? '';
            $ciudad = $data['ciudad'] ?? '';
            $poblacion = $data['poblacion'] ?? '';
            $calle = $data['calle'] ?? '';
            $numero = $data['numero'] ?? '';
            $codigo_postal = $data['codigo_postal'] ?? '';
            $pasaje = $data['pasaje'] ?? '';
            
            $conexion->beginTransaction();
            
            // Gestión de Dirección: Actualización o Inserción según existencia.
            $sql_cp = "SELECT codigoPostal FROM Direccion WHERE codigoPostal = ?";
            $stmt_cp = $conexion->prepare($sql_cp);
            $stmt_cp->execute([$codigo_postal]);
            $num_int = intval($numero);
            
            if ($stmt_cp->rowCount() === 0) {
                    $stmt_ins_dir = $conexion->prepare("INSERT INTO Direccion (codigoPostal, calle, numero, pasaje, poblacion, ciudad, region) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    $stmt_ins_dir->execute([$codigo_postal, $calle, $num_int, $pasaje, $poblacion, $ciudad, $region]);
            } else {
                    $stmt_upd_dir = $conexion->prepare("UPDATE Direccion SET calle=?, numero=?, pasaje=?, poblacion=?, ciudad=?, region=? WHERE codigoPostal=?");
                    $stmt_upd_dir->execute([$calle, $num_int, $pasaje, $poblacion, $ciudad, $region, $codigo_postal]);
            }
            
            // Actualización de registro en tabla Cliente.
            $parts = explode(' ', trim($nombre_completo));
            $nom = $parts[0] ?? $nombre_completo;
            $ape = count($parts) > 1 ? end($parts) : '';
            
            $sql_cli = "UPDATE cliente SET nombreCliente=?, apellidoCliente=?, fechaNacimientoCliente=?, telefonoCliente=?, giro=?, correo=?, codigoPostal=? WHERE RUNCliente=?";
            $stmt_cli = $conexion->prepare($sql_cli);
            $stmt_cli->execute([$nom, $ape, $fecha_nacimiento, $telefono, $giro, $correo, $codigo_postal, $rut]);
            
            // Confirmación de transacción.
            $conexion->commit();
            response_json(200, ['success' => true, 'message' => 'Datos del cliente actualizados correctamente.']);

        } catch (Exception $e) {
            if ($conexion->inTransaction()) {
                $conexion->rollBack();
            }
            error_log("Error Clientes PUT: " . $e->getMessage());
            response_json(500, ['success' => false, 'message' => 'Fallo al actualizar los datos del cliente.']);
        }
        break;

    case 'DELETE':
        require_role('administrador');
        // Operación de Eliminación: Borrado de cliente.
        try {
            $id = $_GET['id'] ?? $data['id'] ?? '';
            if (empty($id)) throw new Exception("Identificador de cliente es requerido.");
            
            $stmt = $conexion->prepare("DELETE FROM cliente WHERE RUNCliente = ?");
            $stmt->execute([$id]);
            
            if ($stmt->rowCount() > 0) {
                response_json(200, ['success' => true, 'message' => 'Cliente eliminado del sistema.']);
            } else {
                response_json(404, ['success' => false, 'message' => 'Cliente no encontrado.']);
            }
        } catch (Exception $e) {
            error_log("Error Clientes DELETE: " . $e->getMessage());
            response_json(500, ['success' => false, 'message' => 'Error al procesar la eliminación.']);
        }
        break;
        
    default:
        response_json(405, ['success' => false, 'message' => 'Método HTTP no permitido.']);
        break;
}
?>