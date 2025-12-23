<?php
class PDFService {
    private $conn;

    public function __construct($dbConnection) {
        $this->conn = $dbConnection;
    }

    /**
     * Obtener datos completos de la orden
     */
    public function getOrdenData($id) {
        try {
            $sql = "SELECT 
                        t1.numeroOrdenDeTrabajo AS id,
                        t1.fechaOrdenDeTrabajo AS fecha_ingreso,
                        t1.estado,
                        t1.ObservacionesOrdenDeTrabajo AS descripcion,
                        t1.patente,
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
                    WHERE t1.numeroOrdenDeTrabajo = ?";

            $stmt = $this->conn->prepare($sql);
            $stmt->execute([$id]);
            $orden = $stmt->fetch(PDO::FETCH_ASSOC);

            if (!$orden) {
                return null;
            }
            
            return $orden;

        } catch (Exception $e) {
            error_log("Error en getOrdenData: " . $e->getMessage());
            return null;
        }
    }

    /**
     * Generar PDF usando Dompdf
     */
    public function generarPDFOrdenTrabajo($id, $stream = true) {
        // Verificar si existe la clase Dompdf
        if (!class_exists('Dompdf\Dompdf')) {
            throw new Exception("Librería Dompdf no instalada.");
        }

        $orden = $this->getOrdenData($id);
        if (!$orden) {
            throw new Exception("Orden no encontrada.");
        }

        $dompdf = new \Dompdf\Dompdf();
        
        return true; 
    }


     // Generar HTML limpio para impresión de varios tipos de documentos
     
    /**
     * Calcular costos reales de la orden
     */
    public function calculateCosts($id) {
        // Mano de Obra
        $sqlMO = "SELECT SUM(horas * precio) as total_mo 
                  FROM DetalleManoObra 
                  WHERE numeroOrdenDeTrabajo = ?";
        $stmtMO = $this->conn->prepare($sqlMO);
        $stmtMO->execute([$id]);
        $mo = $stmtMO->fetch(PDO::FETCH_ASSOC)['total_mo'] ?? 0;

        // Repuestos (Usando precio_venta de la pieza)
        $sqlRep = "SELECT SUM(a.AsignaCantidad * p.precio_venta) as total_rep
                   FROM Asigna a
                   JOIN PiezaExterior p ON a.IDPiezaExterior = p.IDPiezaExterior
                   WHERE a.numeroOrdenDeTrabajo = ?";
        $stmtRep = $this->conn->prepare($sqlRep);
        $stmtRep->execute([$id]);
        $rep = $stmtRep->fetch(PDO::FETCH_ASSOC)['total_rep'] ?? 0;

        return [
            'mo' => (int)$mo,
            'rep' => (int)$rep
        ];
    }

    public function generarHTMLDocumento($id, $tipo = 'orden') {
        $orden = $this->getOrdenData($id);
        
        if (!$orden) {
            return "<h1>Error: Orden #$id no encontrada.</h1>";
        }

        // Títulos y Configuraciones según tipo
        $tituloDoc = "ORDEN DE TRABAJO";
        $color = "#007bff";
        $extraContent = "";
        
        // Costos Reales
        $costos = $this->calculateCosts($id);
        $manoDeObra = $costos['mo'];
        $repuestosCost = $costos['rep'];
        
        $neto = $manoDeObra + $repuestosCost;
        $iva = round($neto * 0.19);
        $total = $neto + $iva;

        if ($tipo === 'factura') {
            $tituloDoc = "FACTURA ELECTRÓNICA";
            $color = "#28a745";
            $extraContent = '
            <div class="section">
                <h3>Detalle de Cobros</h3>
                <table style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="background: #f0f0f0;">
                            <th style="padding: 8px; text-align: left;">Item</th>
                            <th style="padding: 8px; text-align: right;">Monto (CLP)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr><td style="padding: 8px; border-bottom: 1px solid #eee;">Servicio de Diagnóstico y Reparación</td><td style="padding: 8px; text-align: right;">$'.number_format($manoDeObra,0,',','.').'</td></tr>
                        <tr><td style="padding: 8px; border-bottom: 1px solid #eee;">Repuestos e Insumos Varios</td><td style="padding: 8px; text-align: right;">$'.number_format($repuestosCost,0,',','.').'</td></tr>
                    </tbody>
                    <tfoot>
                        <tr><td style="padding: 8px; text-align: right;"><strong>Neto:</strong></td><td style="padding: 8px; text-align: right;">$'.number_format($neto,0,',','.').'</td></tr>
                        <tr><td style="padding: 8px; text-align: right;"><strong>IVA (19%):</strong></td><td style="padding: 8px; text-align: right;">$'.number_format($iva,0,',','.').'</td></tr>
                        <tr style="background: #e9ecef;"><td style="padding: 10px; text-align: right; font-size: 1.2em;"><strong>TOTAL:</strong></td><td style="padding: 10px; text-align: right; font-size: 1.2em;"><strong>$'.number_format($total,0,',','.').'</strong></td></tr>
                    </tfoot>
                </table>
            </div>';
        } elseif ($tipo === 'cotizacion') {
            $tituloDoc = "COTIZACIÓN";
            $color = "#17a2b8";
            $extraContent = '
            <div class="section">
                <h3>Estimación de Costos</h3>
                <p>Esta cotización tiene una validez de <strong>15 días</strong>.</p>
                <table style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="background: #f0f0f0;">
                            <th style="padding: 8px; text-align: left;">Item</th>
                            <th style="padding: 8px; text-align: right;">Valor Neto</th>
                        </tr>
                    </thead>
                    <tbody>
                         <tr><td style="padding: 8px; border-bottom: 1px solid #eee;">Mano de Obra Estimada</td><td style="padding: 8px; text-align: right;">$'.number_format($manoDeObra,0,',','.').'</td></tr>
                         <tr><td style="padding: 8px; border-bottom: 1px solid #eee;">Repuestos Sugeridos</td><td style="padding: 8px; text-align: right;">$'.number_format($repuestosCost,0,',','.').'</td></tr>
                    </tbody>
                     <tfoot>
                        <tr><td style="padding: 8px; text-align: right;"><strong>Subtotal Neto:</strong></td><td style="padding: 8px; text-align: right;">$'.number_format($neto,0,',','.').'</td></tr>
                        <tr><td style="padding: 8px; text-align: right;"><strong>IVA (19%):</strong></td><td style="padding: 8px; text-align: right;">$'.number_format($iva,0,',','.').'</td></tr>
                        <tr style="background: #e9ecef;"><td style="padding: 10px; text-align: right;"><strong>Total Estimado:</strong></td><td style="padding: 10px; text-align: right;"><strong>$'.number_format($total,0,',','.').'</strong></td></tr>
                    </tfoot>
                </table>
            </div>';
        }

        // Estilos para impresión
        $html = '
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <title>' . $tituloDoc . ' #' . $orden['id'] . '</title>
            <style>
                body { font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; max-width: 800px; margin: 0 auto; padding: 20px; }
                .header { text-align: center; margin-bottom: 30px; border-bottom: 4px solid '.$color.'; padding-bottom: 10px; }
                .meta { display: flex; justify-content: space-between; margin-bottom: 20px; background: #f8f9fa; padding: 15px; border-radius: 5px; }
                .section { margin-bottom: 20px; border: 1px solid #ddd; padding: 15px; border-radius: 5px; }
                .section h3 { margin-top: 0; color: '.$color.'; border-bottom: 1px solid #eee; padding-bottom: 5px; }
                .row { display: flex; margin-bottom: 8px; }
                .label { font-weight: bold; width: 150px; color: #555; }
                .value { flex: 1; }
                .footer { margin-top: 40px; font-size: 0.8em; text-align: center; color: #666; border-top: 1px solid #eee; padding-top: 10px; }
                @media print {
                    body { max-width: 100%; padding: 0; }
                    .no-print { display: none; }
                    .section { page-break-inside: avoid; }
                }
            </style>
        </head>
        <body>
            <div class="no-print" style="margin-bottom: 20px; text-align: right;">
                <button onclick="window.print()" style="padding: 10px 20px; background: '.$color.'; color: white; border: none; border-radius: 5px; cursor: pointer; font-weight: bold;">Imprimir / Guardar como PDF</button>
                <button onclick="window.close()" style="padding: 10px 20px; background: #6c757d; color: white; border: none; border-radius: 5px; cursor: pointer; margin-left: 10px;">Cerrar</button>
            </div>

            <div class="header">
                 <div style="display: flex; align-items: center; justify-content: center; gap: 15px; margin-bottom: 10px;">
                    <h1 style="margin: 0;">MIAUtomotriz</h1>
                </div>
                <h2>' . $tituloDoc . '</h2>
                <h3 style="color: #666;">#' . str_pad($orden['id'], 6, "0", STR_PAD_LEFT) . '</h3>
            </div>

            <div class="meta">
                <div><strong>Fecha:</strong> ' . date("d/m/Y", strtotime($orden['fecha_ingreso'])) . '</div>
                <div><strong>Estado:</strong> ' . strtoupper($orden['estado']) . '</div>
                <div><strong>Emisor:</strong> Taller MIAUtomotriz</div>
            </div>

            <div class="section">
                <h3>Información del Cliente</h3>
                <div class="row"><span class="label">Nombre:</span> <span class="value">' . htmlspecialchars($orden['nombre_cliente']) . '</span></div>
                <div class="row"><span class="label">RUN:</span> <span class="value">' . htmlspecialchars($orden['run_cliente']) . '</span></div>
                <div class="row"><span class="label">Teléfono:</span> <span class="value">' . htmlspecialchars($orden['telefono_cliente']) . '</span></div>
                <div class="row"><span class="label">Correo:</span> <span class="value">' . htmlspecialchars($orden['correo_cliente']) . '</span></div>
            </div>

            <div class="section">
                <h3>Información del Vehículo</h3>
                <div class="row"><span class="label">Vehículo:</span> <span class="value">' . htmlspecialchars($orden['marca']) . ' ' . htmlspecialchars($orden['modelo']) . '</span></div>
                <div class="row"><span class="label">Año:</span> <span class="value">' . htmlspecialchars($orden['year']) . '</span></div>
                <div class="row"><span class="label">Patente:</span> <span class="value">' . htmlspecialchars($orden['patente']) . '</span></div>
            </div>

            <div class="section">
                <h3>Detalle de la Orden</h3>
                <div class="row" style="flex-direction: column;">
                    <span class="label" style="margin-bottom: 5px;">Descripción / Problema:</span>
                    <div class="value" style="background: #fdfdfd; padding: 10px; border: 1px solid #eee; border-radius: 4px; white-space: pre-wrap;">' . htmlspecialchars($orden['descripcion']) . '</div>
                </div>
            </div>

            ' . $extraContent . '

            <div class="footer">
                <p>Documento generado electrónicamente por <strong>MIAUtomotriz</strong></p>
                <p>Dirección: Av. Principal 1234, Santiago, Chile | Tel: +56 9 1234 5678</p>
                <p>' . date('d/m/Y H:i:s') . '</p>
            </div>
        </body>
        </html>
        ';

        return $html;
    }
}
?>