package com.tuempresa;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

public class PdfGeneratorMejorado {

    private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    
    // Colores para el PDF
    private static final PDColor COLOR_HEADER = new PDColor(new float[]{0.16f, 0.50f, 0.73f}, PDDeviceRGB.INSTANCE); // Azul
    private static final PDColor COLOR_SUCCESS = new PDColor(new float[]{0.15f, 0.68f, 0.38f}, PDDeviceRGB.INSTANCE); // Verde
    private static final PDColor COLOR_WARNING = new PDColor(new float[]{0.90f, 0.49f, 0.24f}, PDDeviceRGB.INSTANCE); // Naranja
    private static final PDColor COLOR_TEXT = new PDColor(new float[]{0.2f, 0.2f, 0.2f}, PDDeviceRGB.INSTANCE); // Gris oscuro

    public static void generarReciboDiario(File archivo, String nombreTrabajador, LocalDate fecha,
                                           List<Object[]> ventas, double totalIngresosTeoricos, 
                                           double totalRecaudado, double totalGastos) {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                
                // --- ENCABEZADO CON ESTILO ---
                dibujarEncabezado(contentStream, nombreTrabajador, fecha);
                
                // --- SECCIÓN DE VENTAS REALIZADAS ---
                int yPosition = dibujarSeccionVentas(contentStream, ventas, 580);
                
                // --- SECCIÓN DE RESUMEN FINANCIERO ---
                yPosition = dibujarResumenFinanciero(contentStream, totalIngresosTeoricos, 
                                                   totalRecaudado, totalGastos, yPosition - 40);
                
                // --- PIE DE PÁGINA ---
                dibujarPiePagina(contentStream, fecha);
            }

            document.save(archivo);
            
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage());
        }
    }

    private static void dibujarEncabezado(PDPageContentStream contentStream, String nombreTrabajador, LocalDate fecha) 
            throws IOException {
        
        // Fondo del encabezado
        contentStream.setNonStrokingColor(COLOR_HEADER);
        contentStream.addRect(50, 720, 500, 60);
        contentStream.fill();
        
        // Título principal
        contentStream.beginText();
        contentStream.setNonStrokingColor(1f, 1f, 1f); // Blanco
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
        contentStream.newLineAtOffset(60, 750);
        contentStream.showText("RECIBO DE LIQUIDACION DIARIA");
        contentStream.endText();
        
        // Subtítulo
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(60, 730);
        contentStream.showText("Sistema de Gestión de Rutas y Recaudos");
        contentStream.endText();
        
        // Información del trabajador y fecha
        contentStream.setNonStrokingColor(COLOR_TEXT);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.newLineAtOffset(60, 690);
        contentStream.showText("Trabajador: " + nombreTrabajador);
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.newLineAtOffset(350, 690);
        contentStream.showText("Fecha: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        contentStream.endText();
        
        // Línea separadora
        contentStream.setStrokingColor(COLOR_HEADER);
        contentStream.setLineWidth(2);
        contentStream.moveTo(50, 680);
        contentStream.lineTo(550, 680);
        contentStream.stroke();
    }

    private static int dibujarSeccionVentas(PDPageContentStream contentStream, List<Object[]> ventas, int startY) 
            throws IOException {
        
        int yPosition = startY;
        
        // Título de la sección
        contentStream.beginText();
        contentStream.setNonStrokingColor(COLOR_HEADER);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.newLineAtOffset(60, yPosition);
        contentStream.showText("📦 PRODUCTOS VENDIDOS");
        contentStream.endText();
        
        yPosition -= 30;
        
        // Encabezados de la tabla
        contentStream.setNonStrokingColor(COLOR_TEXT);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
        contentStream.newLineAtOffset(60, yPosition);
        contentStream.showText("Producto");
        contentStream.newLineAtOffset(250, 0);
        contentStream.showText("Cantidad");
        contentStream.newLineAtOffset(80, 0);
        contentStream.showText("Precio Unit.");
        contentStream.newLineAtOffset(80, 0);
        contentStream.showText("Subtotal");
        contentStream.endText();
        
        yPosition -= 5;
        
        // Línea bajo encabezados
        contentStream.setStrokingColor(COLOR_TEXT);
        contentStream.setLineWidth(1);
        contentStream.moveTo(60, yPosition);
        contentStream.lineTo(520, yPosition);
        contentStream.stroke();
        
        yPosition -= 20;
        
        // Datos de las ventas
        contentStream.setFont(PDType1Font.HELVETICA, 11);
        double totalVentas = 0;
        
        for (Object[] venta : ventas) {
            String nombre = (String) venta[0];
            int cantidad = (int) venta[1];
            double precioVenta = (double) venta[2];
            double subtotal = cantidad * precioVenta;
            totalVentas += subtotal;
            
            // Truncar nombre si es muy largo
            if (nombre.length() > 28) {
                nombre = nombre.substring(0, 25) + "...";
            }
            
            contentStream.beginText();
            contentStream.newLineAtOffset(60, yPosition);
            contentStream.showText(nombre);
            contentStream.newLineAtOffset(250, 0);
            contentStream.showText(String.valueOf(cantidad));
            contentStream.newLineAtOffset(80, 0);
            contentStream.showText(currencyFormatter.format(precioVenta));
            contentStream.newLineAtOffset(80, 0);
            contentStream.showText(currencyFormatter.format(subtotal));
            contentStream.endText();
            
            yPosition -= 18;
        }
        
        // Línea de separación
        yPosition -= 10;
        contentStream.setStrokingColor(COLOR_TEXT);
        contentStream.moveTo(60, yPosition);
        contentStream.lineTo(520, yPosition);
        contentStream.stroke();
        
        // Total de ventas teóricas
        yPosition -= 25;
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.setNonStrokingColor(COLOR_SUCCESS);
        contentStream.newLineAtOffset(350, yPosition);
        contentStream.showText("TOTAL VENTAS: " + currencyFormatter.format(totalVentas));
        contentStream.endText();
        
        return yPosition;
    }

    private static int dibujarResumenFinanciero(PDPageContentStream contentStream, double totalIngresosTeoricos,
                                              double totalRecaudado, double totalGastos, int startY) throws IOException {
        
        int yPosition = startY;
        
        // Título de la sección
        contentStream.beginText();
        contentStream.setNonStrokingColor(COLOR_HEADER);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.newLineAtOffset(60, yPosition);
        contentStream.showText("💰 RESUMEN FINANCIERO");
        contentStream.endText();
        
        yPosition -= 30;
        
        // Crear caja de resumen con fondo
        contentStream.setNonStrokingColor(0.95f, 0.95f, 0.95f); // Gris muy claro
        contentStream.addRect(60, yPosition - 120, 450, 130);
        contentStream.fill();
        
        // Borde de la caja
        contentStream.setStrokingColor(COLOR_HEADER);
        contentStream.setLineWidth(2);
        contentStream.addRect(60, yPosition - 120, 450, 130);
        contentStream.stroke();
        
        yPosition -= 20;
        
        // Datos del resumen
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.setNonStrokingColor(COLOR_TEXT);
        
        // Ventas teóricas
        contentStream.beginText();
        contentStream.newLineAtOffset(80, yPosition);
        contentStream.showText("💼 Ingresos por ventas (teórico):");
        contentStream.newLineAtOffset(220, 0);
        contentStream.showText(currencyFormatter.format(totalIngresosTeoricos));
        contentStream.endText();
        
        yPosition -= 20;
        
        // Recaudo real
        contentStream.setNonStrokingColor(COLOR_SUCCESS);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(80, yPosition);
        contentStream.showText("💰 Recaudo real del día:");
        contentStream.newLineAtOffset(220, 0);
        contentStream.showText(currencyFormatter.format(totalRecaudado));
        contentStream.endText();
        
        yPosition -= 20;
        
        // Gastos
        contentStream.setNonStrokingColor(COLOR_WARNING);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(80, yPosition);
        contentStream.showText("💳 Gastos operacionales:");
        contentStream.newLineAtOffset(220, 0);
        contentStream.showText(currencyFormatter.format(totalGastos));
        contentStream.endText();
        
        yPosition -= 30;
        
        // Cálculos finales
        double comisionTrabajador = totalRecaudado * 0.18;
        double gananciaNeta = totalRecaudado - totalGastos;
        double paraEmpresa = gananciaNeta - comisionTrabajador;
        
        // Línea separadora
        contentStream.setStrokingColor(COLOR_HEADER);
        contentStream.setLineWidth(1);
        contentStream.moveTo(80, yPosition);
        contentStream.lineTo(480, yPosition);
        contentStream.stroke();
        
        yPosition -= 20;
        
        // Comisión del trabajador
        contentStream.setNonStrokingColor(COLOR_SUCCESS);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 13);
        contentStream.newLineAtOffset(80, yPosition);
        contentStream.showText("👷 COMISIÓN TRABAJADOR (18%):");
        contentStream.newLineAtOffset(220, 0);
        contentStream.showText(currencyFormatter.format(comisionTrabajador));
        contentStream.endText();
        
        yPosition -= 20;
        
        // Para la empresa
        contentStream.setNonStrokingColor(COLOR_HEADER);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 13);
        contentStream.newLineAtOffset(80, yPosition);
        contentStream.showText("🏢 PARA LA EMPRESA:");
        contentStream.newLineAtOffset(220, 0);
        contentStream.showText(currencyFormatter.format(paraEmpresa));
        contentStream.endText();
        
        return yPosition - 40;
    }

    private static void dibujarPiePagina(PDPageContentStream contentStream, LocalDate fecha) throws IOException {
        
        // Línea superior
        contentStream.setStrokingColor(COLOR_HEADER);
        contentStream.setLineWidth(1);
        contentStream.moveTo(50, 80);
        contentStream.lineTo(550, 80);
        contentStream.stroke();
        
        // Información del sistema
        contentStream.beginText();
        contentStream.setNonStrokingColor(0.6f, 0.6f, 0.6f); // Gris
        contentStream.setFont(PDType1Font.HELVETICA, 9);
        contentStream.newLineAtOffset(60, 60);
        contentStream.showText("Generado por: Sistema de Gestión de Microempresa v2.0");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(350, 60);
        contentStream.showText("Fecha de generación: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        contentStream.endText();
        
        // Nota importante
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
        contentStream.newLineAtOffset(60, 40);
        contentStream.showText("* Este documento refleja el recaudo real vs las ventas teóricas del día");
        contentStream.endText();
    }
}