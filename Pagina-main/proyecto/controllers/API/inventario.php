<?php
/**
 * Controlador de Gestión de Inventario y Adquisiciones.
 *
 * Módulo encargado del control de stock, gestión de piezas/repuestos
 * y administración de órdenes de compra con proveedores.
 *
 * Ubicación: controllers/API/inventario.php
 */

session_start();
require_once 'functions.php';
require_once '../conexion.php';

require_auth();
require_role('administrador');

$method = $_SERVER['REQUEST_METHOD'];
$action = $_GET['action'] ?? 'stock'; // Parámetros válidos: stock, ordenes, alertas, parts_list.

$data = [];
if ($method !== 'GET') {
    $data = json_decode(file_get_contents('php://input'), true);
}

// Gestión de Existencias y Alertas de Stock Crítico.
if ($action === 'stock') {
    if ($method === 'GET') {
        // Consulta del estado actual del inventario.
        // Generación de indicador de alerta (1/0) cuando el stock actual es menor o igual al mínimo definido.
        $sql = "SELECT IDPiezaExterior as id, NombrePiezaExterior as nombre, stock_actual, stock_minimo, precio_compra, precio_venta,
                CASE WHEN stock_actual <= stock_minimo THEN 1 ELSE 0 END as alerta
                FROM PiezaExterior ORDER BY alerta DESC, stock_actual ASC";
        $stmt = $conexion->query($sql);
        $items = $stmt->fetchAll(PDO::FETCH_ASSOC);
        response_json(200, ['success' => true, 'data' => $items]);
    } 
    elseif ($method === 'PUT') {
        // Ajuste manual de niveles de stock y precio.
        $id = $data['id'] ?? null;
        $cantidad = $data['cantidad'] ?? null;
        $precio = $data['precio'] ?? null;
        
        if (!$id) {
            response_json(400, ['success' => false, 'message' => 'ID de producto es requerido.']);
        }

        try {
            $conexion->beginTransaction();
            
            if (is_numeric($cantidad)) {
                 $stmt = $conexion->prepare("UPDATE PiezaExterior SET stock_actual = ? WHERE IDPiezaExterior = ?");
                 $stmt->execute([$cantidad, $id]);
            }
            
            if (is_numeric($precio)) {
                 $stmt2 = $conexion->prepare("UPDATE PiezaExterior SET precio_venta = ? WHERE IDPiezaExterior = ?");
                 $stmt2->execute([$precio, $id]);
            }
            
            $conexion->commit();
            response_json(200, ['success' => true, 'message' => 'Datos de inventario actualizados correctamente.']);
            
        } catch(Exception $e) {
            $conexion->rollBack();
            error_log("Error Inventario PUT: " . $e->getMessage());
            response_json(500, ['success' => false, 'message' => 'Error interno al actualizar inventario.']);
        }
    }
}

// Listado simplificado de repuestos para componentes de interfaz de usuario (Selects).
elseif ($action === 'parts_list') {
    if ($method === 'GET') {
        try {
            $stmt = $conexion->query('SELECT IDPiezaExterior as "IDPiezaExterior", NombrePiezaExterior as "NombrePiezaExterior", stock_actual, precio_venta FROM PiezaExterior WHERE stock_actual > 0 ORDER BY NombrePiezaExterior ASC');
            $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
            response_json(200, ['success' => true, 'data' => $data]);
        } catch (Exception $e) {
            response_json(500, ['success' => false, 'message' => 'Error al cargar lista de repuestos.']);
        }
    }
}

// Gestión de Órdenes de Compra a Proveedores.
elseif ($action === 'ordenes') {
    if ($method === 'GET') {
        // Consulta de historial de órdenes de compra con detalles de proveedor y resumen de ítems.
        $sql = "SELECT oc.id_orden_compra, oc.fecha_emision, oc.estado, oc.total, p.nombre as proveedor,
                STRING_AGG(pe.NombrePiezaExterior, ', ') as items_desc,
                COUNT(doc.id_repuesto) as items_count
                FROM OrdenesCompra oc 
                JOIN Proveedores p ON oc.id_proveedor = p.id_proveedor 
                LEFT JOIN DetalleOrdenCompra doc ON oc.id_orden_compra = doc.id_orden_compra
                LEFT JOIN PiezaExterior pe ON doc.id_repuesto = pe.IDPiezaExterior
                GROUP BY oc.id_orden_compra, oc.fecha_emision, oc.estado, oc.total, p.nombre
                ORDER BY oc.id_orden_compra DESC";
        $stmt = $conexion->query($sql);
        $ordenes = $stmt->fetchAll(PDO::FETCH_ASSOC);
        response_json(200, ['success' => true, 'data' => $ordenes]);
    }
    
    // Generación de nueva Orden de Compra.
    elseif ($method === 'POST') {
        $id_prov = $data['id_proveedor'] ?? null;
        $items = $data['items'] ?? [];
        
        if (!$id_prov || empty($items)) response_json(400, ['success' => false, 'message' => 'Datos de orden incompletos.']);
        
        try {
            $conexion->beginTransaction();
            
            // Creación de cabecera de orden.
            $stmt = $conexion->prepare("INSERT INTO OrdenesCompra (id_proveedor, estado, total) VALUES (?, 'Pendiente', 0) RETURNING id_orden_compra");
            $stmt->execute([$id_prov]);
            $id_orden = $stmt->fetchColumn();
            
            $total_orden = 0;
            
            // Creación de líneas de detalle.
            $sql_det = "INSERT INTO DetalleOrdenCompra (id_orden_compra, id_repuesto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
            $stmt_det = $conexion->prepare($sql_det);
            
            foreach($items as $item) {
                $subtotal = $item['cantidad'] * $item['precio'];
                $total_orden += $subtotal;
                $stmt_det->execute([$id_orden, $item['id_repuesto'], $item['cantidad'], $item['precio']]);
            }
            
            // Actualización del monto total de la orden.
            $stmt_update = $conexion->prepare("UPDATE OrdenesCompra SET total = ? WHERE id_orden_compra = ?");
            $stmt_update->execute([$total_orden, $id_orden]);
            
            $conexion->commit();
            response_json(201, ['success' => true, 'message' => 'Orden de compra generada exitosamente.']);
            
        } catch(Exception $e) {
            $conexion->rollBack();
            error_log("Error Inventario Ordenes POST: " . $e->getMessage());
            response_json(500, ['success' => false, 'message' => 'Error interno al generar orden de compra.']);
        }
    }
}

// Recepción de Mercancía y Actualización de Inventario.
elseif ($action === 'recibir') {
    if ($method === 'POST') {
        $id_orden = $data['id_orden'] ?? null;
        
        try {
            $conexion->beginTransaction();
            
            // Validación del estado previo (Debe ser 'Pendiente').
            $check = $conexion->prepare("SELECT estado FROM OrdenesCompra WHERE id_orden_compra = ?");
            $check->execute([$id_orden]);
            $estado = $check->fetchColumn();
            
            if ($estado !== 'Pendiente') throw new Exception("La orden no se encuentra en estado Pendiente.");
            
            // Recuperación de items de la orden.
            $stmt = $conexion->prepare("SELECT id_repuesto, cantidad, precio_unitario FROM DetalleOrdenCompra WHERE id_orden_compra = ?");
            $stmt->execute([$id_orden]);
            $items = $stmt->fetchAll(PDO::FETCH_ASSOC);
            
            // Incremento de stock y actualización de costo de compra (Precio Unitario).
            $update_stock = $conexion->prepare("UPDATE PiezaExterior SET stock_actual = stock_actual + ?, precio_compra = ? WHERE IDPiezaExterior = ?");
            
            foreach($items as $item) {
                $update_stock->execute([$item['cantidad'], $item['precio_unitario'], $item['id_repuesto']]);
            }
            
            // Cambio de estado de la orden a 'Recibida'.
            $stmt_upd = $conexion->prepare("UPDATE OrdenesCompra SET estado = 'Recibida' WHERE id_orden_compra = ?");
            $stmt_upd->execute([$id_orden]);
            
            $conexion->commit();
            response_json(200, ['success' => true, 'message' => 'Mercadería recepcionada y stock actualizado correctamente.']);
            
        } catch(Exception $e) {
            $conexion->rollBack();
            error_log("Error Inventario Recepcion: " . $e->getMessage());
            response_json(500, ['success' => false, 'message' => 'Error interno al recepcionar mercadería.']);
        }
    }
}
?>
