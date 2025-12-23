<?php
ob_start(); // Control de búfer de salida para mitigar warnings previos al envío de cabeceras JSON.
/**
 * Controlador de Gestión de Vehículos.
 *
 * Administra el parque automotriz registrado en el sistema.
 * Gestiona Marcas, Modelos y la asociación de Vehículos a Clientes.
 *
 * Ubicación: controllers/API/vehiculos.php
 */

session_start();
// Importación de utilidades y conexión.
require_once 'functions.php';
require_once '../conexion.php';

// Verificación de credenciales de sesión.
require_auth();

$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        $action = $_GET['action'] ?? null;

        if ($action === 'get_marcas') {
            try {
                // Recuperación de listado de Marcas.
                $sql_marcas = "SELECT IDMarca as id, nombreMarca as nombre FROM Marca ORDER BY nombreMarca";
                $stmt_marcas = $conexion->prepare($sql_marcas);
                $stmt_marcas->execute();
                $marcas = $stmt_marcas->fetchAll(PDO::FETCH_ASSOC);

                // Recuperación de listado de Modelos.
                $sql_modelos = "SELECT IDModelo as id, modelo as nombre, IDMarca as id_marca FROM ModeloVehiculo ORDER BY modelo";
                $stmt_modelos = $conexion->prepare($sql_modelos);
                $stmt_modelos->execute();
                $modelos = $stmt_modelos->fetchAll(PDO::FETCH_ASSOC);

                response_json(200, [
                    'success' => true,
                    'data' => [
                        'marcas' => $marcas,
                        'modelos' => $modelos
                    ]
                ]);
            } catch (Exception $e) {
                error_log("Error Vehiculos Catalogo: " . $e->getMessage());
                response_json(500, ['success' => false, 'message' => 'Error interno al cargar catálogos.']);
            }
            exit;
        }

        // Inicialización ("Seeding") del catálogo de marcas.
        if ($action === 'setup') {
            $marcas = [
                'Toyota', 'Hyundai', 'Chevrolet', 'Nissan', 'Kia', 'Ford', 'Volkswagen', 'Honda', 'Mazda', 'Peugeot',
                'Renault', 'Mitsubishi', 'Subaru', 'Suzuki', 'Fiat', 'Jeep', 'BMW', 'Mercedes-Benz', 'Audi', 'Volvo',
                'Land Rover', 'Mini', 'Lexus', 'Infiniti', 'Cadillac', 'Chrysler', 'Dodge', 'Jaguar', 'Porsche', 'Tesla'
            ];
            
            $inserted = 0;
            try {
                $conexion->beginTransaction();
                
                $stmtCheck = $conexion->prepare("SELECT IDMarca FROM Marca WHERE nombreMarca = ?");
                $stmtInsertMarca = $conexion->prepare("INSERT INTO Marca (nombreMarca) VALUES (?) RETURNING IDMarca");
                $stmtInsertModelo = $conexion->prepare("INSERT INTO ModeloVehiculo (modelo, tipo, transmision, IDMarca) VALUES ('General', 'Indefinido', 'Manual', ?)");
                
                foreach ($marcas as $nombre) {
                    $stmtCheck->execute([$nombre]);
                    $exists = $stmtCheck->fetch(PDO::FETCH_ASSOC);
                    
                    if (!$exists) {
                        $stmtInsertMarca->execute([$nombre]);
                        $idMarca = $stmtInsertMarca->fetchColumn();
                        
                        // Creación de modelo genérico por defecto para mantener consistencia referencial.
                        $stmtInsertModelo->execute([$idMarca]);
                        $inserted++;
                    } else {
                        // Verificación de existencia de modelo por defecto en marcas preexistentes.
                         $idMarca = $exists['IDMarca'];
                         $checkModel = $conexion->prepare("SELECT COUNT(*) FROM ModeloVehiculo WHERE IDMarca = ?");
                         $checkModel->execute([$idMarca]);
                         if ($checkModel->fetchColumn() == 0) {
                             $stmtInsertModelo->execute([$idMarca]);
                         }
                    }
                }
                
                $conexion->commit();
                response_json(200, ['success' => true, 'message' => "Se han sincronizado $inserted marcas nuevas."]);
            } catch (Exception $e) {
                if ($conexion->inTransaction()) $conexion->rollBack();
                response_json(500, ['success' => false, 'message' => 'Error de sincronización: ' . $e->getMessage()]);
            }
            exit;
        }

        $runCliente = $_GET['runCliente'] ?? null;
        
        try {
            $sql = "SELECT 
                        v.patente, 
                        v.anioVehiculo as anio, 
                        v.color, 
                        m.nombreMarca as marca, 
                        mo.modelo 
                    FROM Vehiculo v
                    JOIN ModeloVehiculo mo ON v.IDModelo = mo.IDModelo
                    JOIN Marca m ON mo.IDMarca = m.IDMarca";
            
            $params = [];
            
            if ($runCliente) {
                $sql .= " WHERE v.RUNCliente = ?";
                $params[] = $runCliente;
            }
            
            $sql .= " ORDER BY m.nombreMarca, mo.modelo";

            $stmt = $conexion->prepare($sql);
            $stmt->execute($params);
            $vehiculos = $stmt->fetchAll(PDO::FETCH_ASSOC);

            response_json(200, [
                'success' => true,
                'data' => $vehiculos
            ]);

        } catch (Exception $e) {
            response_json(500, [
                'success' => false,
                'message' => 'Error interno al obtener el listado de vehículos.'
            ]);
        }
        break;

    case 'POST':
        // Registro de nueva unidad vehicular en el sistema.
        $input = file_get_contents('php://input');
        $data = json_decode($input, true);

        $patente = strtoupper(trim($data['patente'] ?? ''));

        // Validar formato de Patente Chilena (AABB12 o AB1234)
        if (!preg_match('/^([A-Z]{4}\d{2}|[A-Z]{2}\d{4})$/', $patente)) {
            response_json(400, ['success' => false, 'message' => 'Formato de patente inválido. Use AABB12 o AB1234.']);
        }
        $anio = $data['anio'] ?? null;
        $color = $data['color'] ?? '';
        $run_cliente = $data['run_cliente'] ?? null;
        
        // Validación de campos obligatorios.
        $modelo_nombre = trim($data['modelo_nombre'] ?? '');
        $marca_nombre = strtoupper(trim($data['marca_nombre'] ?? '')); // Estandarización a mayúsculas.
        $run_cliente = $data['run_cliente'] ?? null;

        if (empty($patente) || empty($anio) || empty($modelo_nombre) || empty($marca_nombre) || empty($run_cliente)) {
            response_json(400, ['success' => false, 'message' => 'Datos insuficientes. Se requiere Patente, Año, Marca, Modelo y Cliente.']);
        }

        try {
            $conexion->beginTransaction();

            // Comprobación de unicidad de la patente.
            $stmt_check = $conexion->prepare("SELECT patente FROM Vehiculo WHERE patente = ?");
            $stmt_check->execute([$patente]);
            if ($stmt_check->rowCount() > 0) {
                $conexion->rollBack();
                response_json(409, ['success' => false, 'message' => 'La patente ya se encuentra registrada en el sistema.']);
            }

            // Lógica "Upsert" para la Marca: Buscar existente o crear nueva.
            $sql_find_marca = "SELECT IDMarca FROM Marca WHERE nombreMarca = ?";
            $stmt_marca = $conexion->prepare($sql_find_marca);
            $stmt_marca->execute([$marca_nombre]);
            $exists_marca = $stmt_marca->fetch(PDO::FETCH_ASSOC);

            if ($exists_marca) {
                $id_marca = $exists_marca['IDMarca'];
            } else {
                $stmt_new_marca = $conexion->prepare("INSERT INTO Marca (nombreMarca) VALUES (?) RETURNING IDMarca");
                $stmt_new_marca->execute([$marca_nombre]);
                $id_marca = $stmt_new_marca->fetchColumn();
            }

            // Lógica "Upsert" para el Modelo.
            $modelo_nombre_upper = strtoupper($modelo_nombre);

            $sql_find_model = "SELECT IDModelo FROM ModeloVehiculo WHERE IDMarca = ? AND UPPER(modelo) = ?";
            $stmt_find = $conexion->prepare($sql_find_model);
            $stmt_find->execute([$id_marca, $modelo_nombre_upper]);
            $exists_model = $stmt_find->fetch(PDO::FETCH_ASSOC);

            if ($exists_model) {
                $id_modelo = $exists_model['IDModelo'];
            } else {
                // Inserción de nuevo modelo asociado a la marca.
                $sql_new_model = "INSERT INTO ModeloVehiculo (modelo, tipo, transmision, IDMarca) VALUES (?, 'Indefinido', 'Manual', ?) RETURNING IDModelo";
                $stmt_new_model = $conexion->prepare($sql_new_model);
                $stmt_new_model->execute([$modelo_nombre_upper, $id_marca]);
                $id_modelo = $stmt_new_model->fetchColumn();
            }

            // Inserción de Vehículo.
            $sql_insert = "INSERT INTO Vehiculo (patente, anioVehiculo, RUNCliente, IDModelo, color) VALUES (?, ?, ?, ?, ?)";
            $stmt = $conexion->prepare($sql_insert);
            
            if ($stmt->execute([$patente, $anio, $run_cliente, $id_modelo, $color])) {
                $conexion->commit();
                response_json(201, [
                    'success' => true, 
                    'message' => 'Vehículo registrado exitosamente.',
                    'data' => [
                        'patente' => $patente,
                        'modelo' => $modelo_nombre
                    ]
                ]);
            } else {
                throw new Exception("Fallo en la inserción del vehículo.");
            }

        } catch (Exception $e) {
            if ($conexion->inTransaction()) {
                $conexion->rollBack();
            }
            error_log("Error Vehiculos POST: " . $e->getMessage());
            response_json(500, ['success' => false, 'message' => 'Ocurrió un error interno al registrar el vehículo.']);
        }
        break;

    default:
        response_json(405, ['success' => false, 'message' => 'Método HTTP no permitido.']);
        break;
}
?>
