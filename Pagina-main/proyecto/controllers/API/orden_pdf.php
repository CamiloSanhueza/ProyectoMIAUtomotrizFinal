<?php
/**
 * Controlador de Generación de Informes PDF.
 *
 * Módulo encargado de la generación y exportación de documentos (Órdenes, Facturas, Cotizaciones)
 * en formato PDF utilizando el servicio PDFService.
 *
 * Ubicación: controllers/API/orden_pdf.php
 */

session_start();

// Verificación de sesión activa.
if (!isset($_SESSION['usuario_id'])) {
    die("Acceso Denegado: Se requiere autenticación previa.");
}

// Importación de dependencias del sistema.
require_once '../conexion.php';
require_once '../services/PDFService.php';

// Validación de identificador de orden (Entero obligatorio).
if (!isset($_GET['id']) || !is_numeric($_GET['id'])) {
    die("Error: Identificador de orden inválido o no proporcionado.");
}

$ordenId = (int) $_GET['id'];

// Inicialización del servicio de generación de PDF.
$pdfService = new PDFService($conexion);
$tipo = $_GET['type'] ?? 'orden'; // Tipos soportados: orden, factura, cotizacion.

try {
    // Intento de generación y renderizado del documento PDF.
   echo $pdfService->generarHTMLDocumento($ordenId, $tipo);
    exit;

} catch (Exception $e) {
    if (!$resultado) {
        // Excepción forzada en caso de fallo crítico en biblioteca PDF.
        throw new Exception("Motor PDF no disponible, activando mecanismo de respaldo HTML.");
    }
    exit;
    
} catch (Exception $e) {
    // Mecanismo de respaldo: Generación de vista HTML imprimible.
    echo $pdfService->generarHTMLParaImprimir($ordenId);
    // Nota: El script de impresión automática está incrustado en el HTML generado.
}
?>
