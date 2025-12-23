<?php
ob_start();
/**
 * reparaciones.php
 * Controlador API para la gestión de Órdenes de Trabajo (Reparaciones).
 * UBICACIÓN: controllers/API/reparaciones.php
 */

session_start();
// Incluir funciones auxiliares y conexión
require_once 'functions.php';
require_once '../conexion.php';

// Ver quien es
require_auth();

$method = $_SERVER['REQUEST_METHOD'];
$data = [];

// Procesar payload JSON
if ($method !== 'GET') {
    $raw_input = file_get_contents('php://input');
    file_put_contents('debug_reparaciones.log', date('Y-m-d H:i:s') . " Method: $method Payload: " . $raw_input . "\n", FILE_APPEND);
    $data = json_decode($raw_input, true);
    
    if (json_last_error() !== JSON_ERROR_NONE && !in_array($method, ['DELETE'])) {
        response_json(400, [
            'success' => false,
            'message' => 'Cuerpo JSON inválido.'
        ]);
    }
}

// Resumen del dia
if ($method === 'GET' && isset($_GET['resumen']) && $_GET['resumen'] === 'hoy') {
    try {
        $id_mecanico = $_GET['id_mecanico'] ?? null;
        
        $sql_pendientes = "SELECT COUNT(*) as total FROM OrdenDeTrabajo WHERE estado = 'Pendiente'";
        $sql_proceso = "SELECT COUNT(*) as total FROM OrdenDeTrabajo WHERE estado = 'En Proceso'";
        $sql_completadas = "SELECT COUNT(*) as total FROM OrdenDeTrabajo WHERE estado = 'Completada' AND fechaordendetrabajo = CURRENT_DATE";
        $sql_canceladas = "SELECT COUNT(*) as total FROM OrdenDeTrabajo WHERE estado = 'Cancelada'";

        $params = [];
        if ($id_mecanico) {
            $sql_pendientes .= " AND idmecanico = ?";
            $sql_proceso .= " AND idmecanico = ?";
            $sql_completadas .= " AND idmecanico = ?";
            $sql_canceladas .= " AND idmecanico = ?";
            $params[] = $id_mecanico;
        }
        
        $stmt_p = $conexion->prepare($sql_pendientes); $stmt_p->execute($params);
        $stmt_e = $conexion->prepare($sql_proceso); $stmt_e->execute($params);
        $stmt_c = $conexion->prepare($sql_completadas); $stmt_c->execute($params);
        $stmt_x = $conexion->prepare($sql_canceladas); $stmt_x->execute($params);

        $pendientes = $stmt_p->fetch(PDO::FETCH_ASSOC)['total'] ?? 0;
        $en_proceso = $stmt_e->fetch(PDO::FETCH_ASSOC)['total'] ?? 0;
        $completadas = $stmt_c->fetch(PDO::FETCH_ASSOC)['total'] ?? 0;
        $canceladas = $stmt_x->fetch(PDO::FETCH_ASSOC)['total'] ?? 0;
        
        response_json(200, [
            'success' => true,
            'data' => [
                'pendientes' => $pendientes,
                'en_proceso' => $en_proceso,
                'completadas' => $completadas,
                'canceladas' => $canceladas
            ]
        ]);
    } catch (Exception $e) {
        error_log("Error Resumen: " . $e->getMessage());
        response_json(500, [
            'success' => false,
            'message' => 'Error al obtener resumen de órdenes.'
        ]);
    }
}

// CRUD
switch ($method) {
    
    // Buscar ordenes o una especifica
    case 'GET':
        $id_orden = $_GET['id'] ?? null;
        
        $where_clause = '';
        $params = [];
        $types = '';

        if ($id_orden) {
            $where_clause = "WHERE t1.numeroOrdenDeTrabajo = ?";
            $params = [$id_orden];
        }
        
        // Filtro por mecanico
        $id_mecanico_filter = $_GET['id_mecanico'] ?? null;
        if ($id_mecanico_filter) {
            if ($where_clause) {
                $where_clause .= " AND t1.IDMecanico = ?";
            } else {
                $where_clause = "WHERE t1.IDMecanico = ?";
            }
            $params[] = $id_mecanico_filter;
        }

        try {
            // Query
            $sql = "SELECT 
                        t1.numeroOrdenDeTrabajo AS id,
                        t1.fechaOrdenDeTrabajo AS fecha_ingreso,
                        t1.estado,
                        t1.ObservacionesOrdenDeTrabajo AS descripcion,
                        t1.patente,
                        t1.IDSeguro as id_seguro,
                        t1.IDMecanico as id_mecanico,
                        CONCAT(u.nombre, ' ', u.apellido) as nombre_mecanico,
                        t2.anioVehiculo AS year,
                        t3.nombreMarca AS marca,
                        t4.modelo,
                        t5.RUNCliente AS run_cliente,
                        CONCAT(t5.nombreCliente, ' ', t5.apellidoCliente) AS nombre_cliente,
                        t5.correo AS correo_cliente,
                        t5.telefonoCliente AS telefono_cliente
                    FROM OrdenDeTrabajo t1
                    LEFT JOIN Vehiculo t2 ON t1.patente = t2.patente
                    LEFT JOIN ModeloVehiculo t4 ON t2.IDModelo = t4.IDModelo
                    LEFT JOIN Marca t3 ON t4.IDMarca = t3.IDMarca
                    LEFT JOIN cliente t5 ON t2.RUNCliente = t5.RUNCliente
                    LEFT JOIN usuarios u ON t1.IDMecanico = u.IDUsuario
                    $where_clause
                    ORDER BY t1.fechaOrdenDeTrabajo DESC";

            $stmt = $conexion->prepare($sql);

            if (!empty($params)) {
               $stmt->execute($params);
            } else {
               $stmt->execute();
            }

            $ordenes = $stmt->fetchAll(PDO::FETCH_ASSOC);

            if ($id_orden && count($ordenes) === 0) {
                response_json(404, [
                    'success' => false,
                    'message' => 'Orden de trabajo no encontrada.'
                ]);
            }
            
            $response_data = $id_orden ? $ordenes[0] : $ordenes;
            
            // Si es una orden especifica, cargamos sus detalles
            if ($id_orden && $response_data) {
                // Labor
                $stmt_l = $conexion->prepare("SELECT d.IDManoDeObra as id, m.nombreTarea as nombre, d.horas, d.precio FROM DetalleManoObra d JOIN ManoDeObra m ON d.IDManoDeObra = m.IDManoDeObra WHERE numeroOrdenDeTrabajo = ?");
                $stmt_l->execute([$id_orden]);
                $response_data['labor'] = $stmt_l->fetchAll(PDO::FETCH_ASSOC);

                // Parts
                $stmt_p = $conexion->prepare("SELECT a.IDPiezaExterior as id_pieza, p.NombrePiezaExterior as nombre, a.AsignaCantidad as cantidad, p.precio_venta as precio FROM Asigna a JOIN PiezaExterior p ON a.IDPiezaExterior = p.IDPiezaExterior WHERE numeroOrdenDeTrabajo = ?");
                $stmt_p->execute([$id_orden]);
                $response_data['parts'] = $stmt_p->fetchAll(PDO::FETCH_ASSOC);

                // Damages
                $stmt_d = $conexion->prepare("SELECT d.TipoDano as id_tipo, d.IDUbicacionDano as id_ubicacion, d.observacion, t.DescripcionDano as \"DescripcionDano\", u.DescripcionUbicacion as \"DescripcionUbicacion\" FROM DetalleDano d JOIN TipoDano t ON d.TipoDano = t.TipoDano JOIN UbicacionDano u ON d.IDUbicacionDano = u.IDUbicacionDano WHERE numeroOrdenDeTrabajo = ?");
                $stmt_d->execute([$id_orden]);
                $response_data['damages'] = $stmt_d->fetchAll(PDO::FETCH_ASSOC);
            }

            $message = $id_orden ? 'Detalle recuperado.' : 'Lista recuperada.';
            
            response_json(200, [
                'success' => true,
                'message' => $message,
                'data' => $response_data
            ]);

        } catch (Exception $e) {
            error_log("Error Reparaciones GET: " . $e->getMessage());
            response_json(500, [
                'success' => false,
                'message' => 'Error al recuperar los datos de la orden.'
            ]);
        }
        break;

    // Crear orden nueva
    case 'POST':
        $fecha_ingreso = $data['fecha_ingreso'] ?? date('Y-m-d');
        $patente = $data['patente'] ?? '';
        $id_seguro = !empty($data['id_seguro']) ? $data['id_seguro'] : null; // Permitir nulo
        $id_mecanico = !empty($data['id_mecanico']) ? $data['id_mecanico'] : null;
        $observaciones = $data['observaciones'] ?? '';
        $estado = $data['estado'] ?? 'Pendiente';

        // Validar datos minimos
        if (empty($patente) || empty($estado)) {
            response_json(400, ['success' => false, 'message' => 'Faltan datos obligatorios (Patente, Estado).']);
        }
        
        // Validar que vengan repuestos (Solicitud explícita del usuario)
        if (!isset($data['parts']) || !is_array($data['parts']) || count($data['parts']) === 0) {
            response_json(400, ['success' => false, 'message' => 'Es obligatorio agregar al menos un repuesto a la orden.']);
        }

        $conexion->beginTransaction();
        try {
            $sql_orden = "INSERT INTO OrdenDeTrabajo (fechaOrdenDeTrabajo, estado, ObservacionesOrdenDeTrabajo, patente, IDSeguro, IDMecanico)
            VALUES (?, ?, ?, ?, ?, ?)";
            
            $stmt_orden = $conexion->prepare($sql_orden);
            
            if (!$stmt_orden->execute([$fecha_ingreso, $estado, $observaciones, $patente, $id_seguro, $id_mecanico])) {
                throw new Exception("Error al crear la orden. Verifique patente y seguro.");
            }
            
            $new_id = $conexion->lastInsertId();

            // Guardar Mano de Obra (Si viene)
            // Guardar Mano de Obra (Si viene)
            if (isset($data['labor']) && is_array($data['labor'])) {
                $sql_labor = "INSERT INTO DetalleManoObra (numeroOrdenDeTrabajo, IDManoDeObra, horas, precio) VALUES (?, ?, ?, ?)";
                $stmt_labor = $conexion->prepare($sql_labor);

                foreach ($data['labor'] as $item) {
                    $id_mo = $item['id'] ?? null;
                    $horas = $item['horas'] ?? 1;
                    $precio = $item['precio'] ?? 0;

                    if ($id_mo) {
                        $stmt_labor->execute([$new_id, $id_mo, $horas, $precio]);
                    }
                }
            }

            // Guardar Daños (Si viene)
            if (isset($data['damages']) && is_array($data['damages'])) {
                $sql_dano = "INSERT INTO DetalleDano (numeroOrdenDeTrabajo, TipoDano, IDUbicacionDano, observacion) VALUES (?, ?, ?, ?)";
                $stmt_dano = $conexion->prepare($sql_dano);

                foreach ($data['damages'] as $item) {
                    $id_tipo = $item['id_tipo'] ?? null;
                    $id_ubicacion = $item['id_ubicacion'] ?? null;
                    $obs = $item['observacion'] ?? '';

                    if ($id_tipo && $id_ubicacion) {
                        $stmt_dano->execute([$new_id, $id_tipo, $id_ubicacion, $obs]);
                    }
                }
            }

             // Guardar Repuestos (Asigna)
             if (isset($data['parts']) && is_array($data['parts'])) {
                $sql_asigna = "INSERT INTO Asigna (numeroOrdenDeTrabajo, IDPiezaExterior, AsignaCantidad) VALUES (?, ?, ?)";
                $stmt_asigna = $conexion->prepare($sql_asigna);

                foreach ($data['parts'] as $item) {
                    $id_pieza = $item['id_pieza'] ?? null;
                    $cant = $item['cantidad'] ?? 1;

                    if ($id_pieza) {
                        try {
                            $stmt_asigna->execute([$new_id, $id_pieza, $cant]);
                        } catch (Exception $ex) {
                            // Ignorar duplicados
                        }
                    }
                }
            }
            
            $conexion->commit();
            
            response_json(201, [
                'success' => true,
                'message' => 'Orden de trabajo creada exitosamente.',
                'id_orden' => $new_id
            ]);
            
        } catch (Exception $e) {
            if ($conexion->inTransaction()) {
                $conexion->rollBack();
            }
            $msg = $e->getMessage();
            
            // Check for Insurance FK violation
            if (strpos($msg, 'ordendetrabajo_idseguro_fkey') !== false || strpos($msg, 'tabla «seguro»') !== false) {
                $msg = 'El ID de seguro ingresado no es válido.';
            } else {
                $msg = 'Error interno al procesar la orden.';
            }

            response_json(500, [
                'success' => false,
                'message' => $msg
            ]);
        }
        break;

    // Actualizar orden
    case 'PUT':
        $id_orden = $data['id'] ?? null;
        $estado = $data['estado'] ?? null;
        $observaciones = $data['observaciones'] ?? null;
        
        if (empty($id_orden)) {
            response_json(400, [
                'success' => false,
                'message' => 'ID de orden es obligatorio.'
            ]);
        }

        $sql_update = "UPDATE OrdenDeTrabajo SET ";
        $params = [];
        $types = '';
        
        if ($estado !== null) {
            $sql_update .= "estado = ?, ";
            $params[] = $estado;
        }
        if ($observaciones !== null) {
            $sql_update .= "ObservacionesOrdenDeTrabajo = ?, ";
            $params[] = $observaciones;
        }
        
        if (isset($data['patente'])) {
            $sql_update .= "patente = ?, ";
            $params[] = $data['patente'];
        }
        if (isset($data['id_seguro'])) {
            $sql_update .= "IDSeguro = ?, ";
            $params[] = !empty($data['id_seguro']) ? $data['id_seguro'] : null;
        }
        if (isset($data['id_mecanico'])) {
            $sql_update .= "IDMecanico = ?, ";
            $params[] = !empty($data['id_mecanico']) ? $data['id_mecanico'] : null;
        }
        if (isset($data['fecha_ingreso'])) {
             $sql_update .= "fechaOrdenDeTrabajo = ?, ";
             $params[] = $data['fecha_ingreso'];
        }

        $sql_update = rtrim($sql_update, ', ');
        $sql_update .= " WHERE numeroOrdenDeTrabajo = ?";
        
        $params[] = $id_orden;


        if (count($params) <= 1 && empty($data['labor']) && empty($data['parts']) && empty($data['damages'])) {
            response_json(400, [
                'success' => false,
                'message' => 'No se proporcionaron datos para actualizar.'
            ]);
        }
        
        // Si se envia el array de parts, validar que no venga vacio (si la intencion es actualizar la lista)
        // Nota: El frontend siempre manda el array. Si el usuario borra todos los repuestos, esto llegaria vacio.
        // Validamos que NO este vacio.
        if (isset($data['parts']) && (!is_array($data['parts']) || count($data['parts']) === 0)) {
             response_json(400, ['success' => false, 'message' => 'La orden debe tener al menos un repuesto asignado.']);
        }

        try {
            // Verificar estado actual
            $stmt_check = $conexion->prepare("SELECT estado FROM OrdenDeTrabajo WHERE numeroOrdenDeTrabajo = ?");
            $stmt_check->execute([$id_orden]);
            $current_order = $stmt_check->fetch(PDO::FETCH_ASSOC);

            if ($current_order && $current_order['estado'] === 'Completada') {
                response_json(400, [
                    'success' => false,
                    'message' => 'No se puede editar una orden que ya está Completada.'
                ]);
            }

            // Actualizar datos principales
            if (count($params) > 1) {
                $stmt = $conexion->prepare($sql_update);
                $stmt->execute($params);
            }

            // Actualizar Detalles (Si se enviaron)
            // Borrar y Re-insertar es la estrategia mas limpia para edicion completa
            
            // Labor
            if (isset($data['labor'])) {
                $conexion->prepare("DELETE FROM DetalleManoObra WHERE numeroOrdenDeTrabajo = ?")->execute([$id_orden]);
                if (!empty($data['labor'])) {
                    $stmt_ins = $conexion->prepare("INSERT INTO DetalleManoObra (numeroOrdenDeTrabajo, IDManoDeObra, horas, precio) VALUES (?, ?, ?, ?)");
                    foreach ($data['labor'] as $item) {
                        $stmt_ins->execute([$id_orden, $item['id'], $item['horas'] ?? 1, $item['precio'] ?? 0]);
                    }
                }
            }

            // Parts
            if (isset($data['parts'])) {
                $conexion->prepare("DELETE FROM Asigna WHERE numeroOrdenDeTrabajo = ?")->execute([$id_orden]);
                 if (!empty($data['parts'])) {
                    $stmt_ins = $conexion->prepare("INSERT INTO Asigna (numeroOrdenDeTrabajo, IDPiezaExterior, AsignaCantidad) VALUES (?, ?, ?)");
                    foreach ($data['parts'] as $item) {
                        $stmt_ins->execute([$id_orden, $item['id_pieza'], $item['cantidad'] ?? 1]);
                    }
                }
            }

            // Damages
            if (isset($data['damages'])) {
                $conexion->prepare("DELETE FROM DetalleDano WHERE numeroOrdenDeTrabajo = ?")->execute([$id_orden]);
                 if (!empty($data['damages'])) {
                    $stmt_ins = $conexion->prepare("INSERT INTO DetalleDano (numeroOrdenDeTrabajo, TipoDano, IDUbicacionDano, observacion) VALUES (?, ?, ?, ?)");
                    foreach ($data['damages'] as $item) {
                        try {
                            $stmt_ins->execute([$id_orden, $item['id_tipo'], $item['id_ubicacion'], $item['observacion'] ?? '']);
                        } catch (Exception $d_ex) {
                            file_put_contents('debug_damages.log', "Error adding damage: " . $d_ex->getMessage() . " Item: " . json_encode($item) . "\n", FILE_APPEND);
                        }
                    }
                }
            }
            
            // Si llego aca es exito (o al menos parcial)
            response_json(200, [
                    'success' => true,
                    'message' => 'Orden actualizada exitosamente.'
            ]);

        } catch (Exception $e) {
            error_log("Error Reparaciones PUT: " . $e->getMessage());
            response_json(500, [
                'success' => false,
                'message' => 'Error al actualizar la orden.'
            ]);
        }
        break;

    // Borrar
    case 'DELETE':
        $id_orden = $_GET['id'] ?? ($data['id'] ?? null);

        if (empty($id_orden)) {
            response_json(400, [
                'success' => false,
                'message' => 'ID de orden es obligatorio.'
            ]);
        }
        
        // Security check: Only admins can delete orders
        require_role('administrador');

        try {
            // Preparar y ejecutar la consulta
            $sql = "DELETE FROM OrdenDeTrabajo WHERE numeroOrdenDeTrabajo = ?";
            $stmt = $conexion->prepare($sql);
            if ($stmt->execute([$id_orden]) && $stmt->rowCount() > 0) {
                response_json(200, [
                    'success' => true,
                    'message' => 'Orden eliminada exitosamente.'
                ]);
            } else {
                response_json(404, [
                    'success' => false,
                    'message' => 'Orden no encontrada.'
                ]);
            }
        } catch (Exception $e) {
            error_log("Error Reparaciones DELETE: " . $e->getMessage());
            response_json(500, [
                'success' => false,
                'message' => 'Error interno al intentar eliminar la orden.'
            ]);
        }
        break;

    // Metodo raro (no permitido)
    default:
        response_json(405, [
            'success' => false,
            'message' => 'Método HTTP no permitido. Use GET, POST, PUT o DELETE.'
        ]);
        break;
}
// --- CATALOGOS PARA FORMULARIO (Mano de Obra, Daños) ---
if ($method === 'GET') {
    $action = $_GET['action'] ?? '';

    // Manejo de catalogos
    if ($action === 'labor') {
        try {
            $stmt = $conexion->query('SELECT IDManoDeObra as "IDManoDeObra", nombreTarea as "nombreTarea", precioManoDeObra as "precioManoDeObra" FROM ManoDeObra ORDER BY nombreTarea ASC');
            $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
            response_json(200, ['success' => true, 'data' => $data]);
        } catch (Exception $e) {
            response_json(500, ['success' => false, 'message' => 'Error al cargar catálogo de mano de obra.']);
        }
    }
    elseif ($action === 'damage_types') {
        try {
            $stmt = $conexion->query('SELECT TipoDano as "TipoDano", DescripcionDano as "DescripcionDano" FROM TipoDano ORDER BY DescripcionDano ASC');
            $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
            response_json(200, ['success' => true, 'data' => $data]);
        } catch (Exception $e) {
            response_json(500, ['success' => false, 'message' => 'Error al cargar tipos de daño.']);
        }
    }
    elseif ($action === 'damage_locations') {
        try {
            $stmt = $conexion->query('SELECT IDUbicacionDano as "IDUbicacionDano", DescripcionUbicacion as "DescripcionUbicacion" FROM UbicacionDano ORDER BY DescripcionUbicacion ASC');
            $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
            response_json(200, ['success' => true, 'data' => $data]);
        } catch (Exception $e) {
            response_json(500, ['success' => false, 'message' => 'Error al cargar ubicaciones.']);
        }
    }
}
?>