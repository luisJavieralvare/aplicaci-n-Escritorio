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

public class PdfGenerator {

    public static void generarReciboDiario(File archivo, String nombreTrabajador, LocalDate fecha,
                                           List<Object[]> ventas, double totalIngresos, double totalGastos) {

        // Formateador para moneda (ej. $1,234.56)
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                
                // --- TÍTULO ---
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(150, 750); // Coordenadas (x, y) desde abajo a la izquierda
                contentStream.showText("Recibo de Liquidación Diaria");
                contentStream.endText();

                // --- INFO GENERAL ---
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Trabajador: " + nombreTrabajador);
                contentStream.newLineAtOffset(300, 0); // Mueve el cursor 300 a la derecha
                contentStream.showText("Fecha: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                contentStream.endText();

                // --- TABLA DE VENTAS ---
                int y = 650; // Posición Y inicial para la tabla
                // Cabecera
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Producto");
                contentStream.newLineAtOffset(300, 0);
                contentStream.showText("Cant.");
                contentStream.newLineAtOffset(70, 0);
                contentStream.showText("P. Venta");
                contentStream.newLineAtOffset(70, 0);
                contentStream.showText("Ingresos");
                contentStream.endText();
                y -= 20;

                // Filas de la tabla
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                for (Object[] venta : ventas) {
                    String nombre = (String) venta[0];
                    int cantidad = (int) venta[1];
                    double precioVenta = (double) venta[2];
                    double ingresos = cantidad * precioVenta;

                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText(nombre);
                    contentStream.newLineAtOffset(300, 0);
                    contentStream.showText(String.valueOf(cantidad));
                    contentStream.newLineAtOffset(70, 0);
                    contentStream.showText(currencyFormatter.format(precioVenta));
                    contentStream.newLineAtOffset(70, 0);
                    contentStream.showText(currencyFormatter.format(ingresos));
                    contentStream.endText();
                    y -= 20; // Bajamos a la siguiente línea
                }

                // --- RESUMEN FINAL ---
                y -= 30;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(350, y);
                contentStream.showText("Total Ingresos: " + currencyFormatter.format(totalIngresos));
                contentStream.newLineAtOffset(0, -25); // Bajamos 25 puntos
                contentStream.showText("Total Gastos: " + currencyFormatter.format(totalGastos));
                contentStream.newLineAtOffset(0, -25);
                contentStream.showText("GANANCIA NETA: " + currencyFormatter.format(totalIngresos - totalGastos));
                contentStream.endText();
            }

            document.save(archivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}