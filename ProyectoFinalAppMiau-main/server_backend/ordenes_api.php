<?php
// ordenes_api.php
header('Content-Type: application/json');
require_once 'db_connect.php';

$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        // Obtener órdenes
        $patente = $_GET['patente'] ?? null;
        
        if ($patente) {
            $query = "SELECT * FROM OrdenDeTrabajo WHERE patente = $1 ORDER BY fechaOrdenDeTrabajo DESC";
            $result = pg_query_params($dbconn, $query, array($patente));
        } else {
            $query = "SELECT * FROM OrdenDeTrabajo ORDER BY fechaOrdenDeTrabajo DESC";
            $result = pg_query($dbconn, $query);
        }
        
        $ordenes = [];
        while ($row = pg_fetch_assoc($result)) {
            $ordenes[] = $row;
        }
        echo json_encode($ordenes);
        break;

    case 'POST':
        // Crear nueva orden
        $patente = $_POST['patente'] ?? '';
        $observaciones = $_POST['observaciones'] ?? '';
        $idCliente = $_POST['idCliente'] ?? null;
        
        if (empty($patente)) {
            echo json_encode(["status" => "error", "message" => "Patente requerida"]);
            exit;
        }

        $query = "INSERT INTO OrdenDeTrabajo (patente, ObservacionesOrdenDeTrabajo, IDCliente, estado) VALUES ($1, $2, $3, 'Pendiente') RETURNING numeroOrdenDeTrabajo";
        $result = pg_query_params($dbconn, $query, array($patente, $observaciones, $idCliente));
        
        if ($result) {
            $row = pg_fetch_assoc($result);
            echo json_encode(["status" => "success", "id" => $row['numeroordendetrabajo']]);
        } else {
            echo json_encode(["status" => "error", "message" => pg_last_error($dbconn)]);
        }
        break;
        
    default:
        echo json_encode(["status" => "error", "message" => "Método no soportado"]);
        break;
}

pg_close($dbconn);
?>
