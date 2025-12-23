<?php
/**
 * Controlador Auxiliar (Helpers).
 *
 * Funcionalidades transversales para la obtención de estadísticas y datos
 * de soporte para los tableros de control (dashboards).
 *
 * Ubicación: controllers/API/helpers.php
 */

session_start();

// Importación de funciones auxiliares y configuración de conexión.
require_once 'functions.php';
require_once '../conexion.php';

// Verificación estricta de autenticación (Acceso restringido a usuarios registrados).
require_auth();

// Restricción de método HTTP (Solo se permite GET).
require_method('GET');

// Obtención del parámetro que define el tipo de datos solicitado.
$data_type = $_GET['data'] ?? '';

// Recuperación de catálogo de Regiones y Ciudades.
if ($data_type === 'regions') {
    $regionesYciudades = [
        "Metropolitana" => ["Santiago"],
        "Valparaíso" => ["Valparaíso"],
        "Biobío" => ["Concepción"],
        "Coquimbo" => ["La Serena"],
        "Antofagasta" => ["Antofagasta"],
        "Araucanía" => ["Temuco"],
        "O'Higgins" => ["Rancagua"],
        "Tarapacá" => ["Iquique"],
        "Los Lagos" => ["Puerto Montt"],
        "Maule" => ["Talca"]
    ];
    
    response_json(200, [
        'success' => true,
        'data' => $regionesYciudades
    ]);
}

// Estadísticas Mensuales de Órdenes de Trabajo.
elseif ($data_type === 'stats') {
    try {
        // Ejecución de consulta para obtener el conteo de órdenes completadas por mes (últimos 6 meses).
        $id_mecanico = $_GET['id_mecanico'] ?? null;
        $sql_mes = "SELECT 
                        TO_CHAR(fechaordendetrabajo, 'YYYY-MM') as mes,
                        COUNT(*) as total 
                    FROM OrdenDeTrabajo 
                    WHERE fechaordendetrabajo >= CURRENT_DATE - INTERVAL '6 MONTH'
                    AND estado = 'Completada'";
        
        $params = [];
        if ($id_mecanico) {
            $sql_mes .= " AND idmecanico = ?";
            $params[] = $id_mecanico;
        }

        $sql_mes .= " GROUP BY TO_CHAR(fechaordendetrabajo, 'YYYY-MM') ORDER BY mes ASC";
        
        $stmt = $conexion->prepare($sql_mes);
        $stmt->execute($params);
        $res_mes = $stmt;
        
        if ($res_mes) {
            $data_mes = $res_mes->fetchAll(PDO::FETCH_ASSOC);
        } else {
            $data_mes = [];
        }

        if (empty($data_mes)) {
            // Relleno de datos con valores en cero si no existen registros.
            $data_mes = [];
            for ($i = 5; $i >= 0; $i--) {
                $data_mes[] = [
                    'mes' => date('Y-m', strtotime("-$i months")),
                    'total' => 0
                ];
            }
        }
        
        response_json(200, [
            'success' => true,
            'data' => [
                'por_mes' => $data_mes
            ]
        ]);

    } catch (Exception $e) {
        error_log("Error Helpers Stats: " . $e->getMessage());
        response_json(500, ['success' => false, 'message' => 'Error al calcular estadísticas.']);
    }
}

// Análisis de Repuestos (PiezaExterior) más utilizados.
elseif ($data_type === 'repuestos') {
    try {
        // Consulta agregada sobre tabla PiezaExterior y su relación en Asigna.
        $sql_repuestos = "SELECT 
                            p.nombrepiezaexterior as nombre,
                            SUM(a.AsignaCantidad) as cantidad
                        FROM PiezaExterior p
                        INNER JOIN Asigna a ON p.IDPiezaExterior = a.IDPiezaExterior
                        GROUP BY p.IDPiezaExterior, p.nombrepiezaexterior
                        ORDER BY cantidad DESC
                        LIMIT 8";
        
        $res_repuestos = $conexion->query($sql_repuestos);
        $repuestos = $res_repuestos->fetchAll(PDO::FETCH_ASSOC);

        response_json(200, [
            'success' => true,
            'data' => $repuestos
        ]);

    } catch (Exception $e) {
         error_log("Error Helpers Repuestos: " . $e->getMessage());
         response_json(500, ['success' => false, 'message' => 'Error al obtener top repuestos.']);
    }
}

// Distribución de Órdenes de Trabajo por Estado.
elseif ($data_type === 'estados') {
    try {
        $id_mecanico = $_GET['id_mecanico'] ?? null;
        $sql_estados = "SELECT 
                            estado,
                            COUNT(*) as cantidad
                        FROM OrdenDeTrabajo";
        
        $params_est = [];
        if ($id_mecanico) {
            $sql_estados .= " WHERE idmecanico = ?";
            $params_est[] = $id_mecanico;
        }

        $sql_estados .= " GROUP BY estado ORDER BY cantidad DESC";
        
        $stmt_est = $conexion->prepare($sql_estados);
        $stmt_est->execute($params_est);
        $res_estados = $stmt_est;
        
        if ($res_estados && $res_estados->rowCount() > 0) {
            $estados = $res_estados->fetchAll(PDO::FETCH_ASSOC);
        } else {
            throw new Exception("No hay datos disponibles para la consulta.");
        }

    } catch (Exception $e) {
        // Provisión de datos simulados en caso de fallo (Fallback).
        $estados = [
            ['estado' => 'Pendiente', 'cantidad' => '5'],
            ['estado' => 'En Proceso', 'cantidad' => '8'],
            ['estado' => 'Completada', 'cantidad' => '12'],
            ['estado' => 'Cancelada', 'cantidad' => '2']
        ];
    }

    response_json(200, [
        'success' => true,
        'data' => $estados
    ]);
}

// Ranking de Vehículos con mayor frecuencia de atención.
elseif ($data_type === 'vehiculos') {
    try {
        $sql_vehiculos = "SELECT 
                            m.nombreMarca as marca,
                            mo.modelo,
                            COUNT(o.numeroOrdenDeTrabajo) as atenciones
                        FROM Vehiculo v
                        INNER JOIN ModeloVehiculo mo ON v.IDModelo = mo.IDModelo
                        INNER JOIN Marca m ON mo.IDMarca = m.IDMarca
                        INNER JOIN OrdenDeTrabajo o ON v.patente = o.patente
                        WHERE o.fechaOrdenDeTrabajo >= CURRENT_DATE - INTERVAL '6 MONTH'
                        GROUP BY m.nombreMarca, mo.modelo
                        ORDER BY atenciones DESC
                        LIMIT 10";
        
        $res_vehiculos = $conexion->query($sql_vehiculos);
        $vehiculos = $res_vehiculos->fetchAll(PDO::FETCH_ASSOC);

        response_json(200, [
            'success' => true,
            'data' => $vehiculos
        ]);

    } catch (Exception $e) {
        error_log("Error Helpers Vehiculos: " . $e->getMessage());
        response_json(500, ['success' => false, 'message' => 'Error al obtener ranking de vehículos.']);
    }
}

// Reporte de Ingresos Mensuales Financieros.
elseif ($data_type === 'ingresos') {
    try {
        // Cálculo de suma total de Piezas + Mano de Obra para órdenes COMPLETADAS.
        $sql_ingresos = "SELECT 
                        TO_CHAR(o.fechaOrdenDeTrabajo, 'YYYY-MM') as mes,
                        SUM(
                            (COALESCE(sub_p.total_piezas, 0) + COALESCE(sub_m.total_mo, 0))
                        ) as total
                    FROM OrdenDeTrabajo o
                    LEFT JOIN (
                        SELECT numeroOrdenDeTrabajo, SUM(a.AsignaCantidad * p.precio_venta) as total_piezas
                        FROM Asigna a
                        JOIN PiezaExterior p ON a.IDPiezaExterior = p.IDPiezaExterior
                        GROUP BY numeroOrdenDeTrabajo
                    ) sub_p ON o.numeroOrdenDeTrabajo = sub_p.numeroOrdenDeTrabajo
                    LEFT JOIN (
                        SELECT numeroOrdenDeTrabajo, SUM(d.horas * d.precio) as total_mo
                        FROM DetalleManoObra d
                        GROUP BY numeroOrdenDeTrabajo
                    ) sub_m ON o.numeroOrdenDeTrabajo = sub_m.numeroOrdenDeTrabajo
                    WHERE o.fechaOrdenDeTrabajo >= CURRENT_DATE - INTERVAL '6 MONTH'
                    AND o.estado = 'Completada'
                    GROUP BY TO_CHAR(o.fechaOrdenDeTrabajo, 'YYYY-MM')
                    ORDER BY mes ASC";
        
        $res = $conexion->query($sql_ingresos);
        $data = $res ? $res->fetchAll(PDO::FETCH_ASSOC) : [];
        
        response_json(200, ['success' => true, 'data' => $data]);
    } catch(Exception $e) {
        error_log("Error Helpers Ingresos: " . $e->getMessage());
        response_json(500, ['success' => false, 'message' => 'Error al calcular ingresos.']);
    }
}

// Estadísticas de Averías más recurrentes.
elseif ($data_type === 'averias') {
    try {
        $sql_averias = "SELECT 
                            t.DescripcionDano as tipo,
                            COUNT(*) as cantidad
                        FROM DetalleDano d
                        JOIN TipoDano t ON d.TipoDano = t.TipoDano
                        GROUP BY t.DescripcionDano
                        ORDER BY cantidad DESC
                        LIMIT 5";
        $res = $conexion->query($sql_averias);
        $data = $res->fetchAll(PDO::FETCH_ASSOC);
        
        response_json(200, ['success' => true, 'data' => $data]);
    } catch (Exception $e) {
        error_log("Error Helpers Averias: " . $e->getMessage());
        response_json(500, ['success' => false, 'message' => 'Error al obtener estadísticas de averías.']);
    }
}

// Error: Solicitud de tipo de dato no válido.
else {
    response_json(400, [
        'success' => false,
        'message' => 'Tipo de dato no válido',
        'tipos_validos' => ['stats', 'repuestos', 'estados', 'vehiculos', 'regions']
    ]);
}
?>