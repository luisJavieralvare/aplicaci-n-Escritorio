package com.tuempresa;

import java.awt.BorderLayout;
import java.awt.GridLayout; // Importamos MigLayout
import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import com.github.lgooddatepicker.components.DatePicker;

import net.miginfocom.swing.MigLayout;

public class ReportePanel extends JPanel {
    // Componentes (sin cambios)
    private JSpinner yearSpinner, weekSpinner;
    private JComboBox<String> trabajadorComboBox;
    private DefaultTableModel tableModel;
    private JLabel lblGananciaBruta, lblGastos, lblGananciaNeta, lblComision;
    private NumberFormat currencyFormatter;
    private DatePicker datePicker;
    private JButton btnGenerarPdf;
    private JButton btnGenerarReporte;

    public ReportePanel() {
        // Usamos BorderLayout para la estructura general: Controles arriba, tabla en medio, resumen abajo.
        setLayout(new BorderLayout(10, 10));
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        // --- PANEL DE CONTROLES (NORTE) REDISEÑADO CON MIGLAYOUT ---
        // "wrap 7" = crea una cuadrícula de 7 columnas, y salta de línea automáticamente.
        JPanel controlsPanel = new JPanel(new MigLayout("insets 5, wrap 7"));
        controlsPanel.setBorder(BorderFactory.createTitledBorder("Opciones de Reporte"));

        // -- Fila 1: Reporte Semanal --
        controlsPanel.add(new JLabel("Año:"));
        yearSpinner = new JSpinner(new SpinnerNumberModel(Calendar.getInstance().get(Calendar.YEAR), 2020, 2050, 1));
        controlsPanel.add(yearSpinner);

        controlsPanel.add(new JLabel("Semana:"));
        weekSpinner = new JSpinner(new SpinnerNumberModel(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR), 1, 53, 1));
        controlsPanel.add(weekSpinner);

        controlsPanel.add(new JLabel("Trabajador:"));
        trabajadorComboBox = new JComboBox<>();
        controlsPanel.add(trabajadorComboBox, "growx"); // El ComboBox crece horizontalmente

        btnGenerarReporte = new JButton("Generar Reporte Semanal");
        controlsPanel.add(btnGenerarReporte);

        // -- Fila 2: Separador --
        controlsPanel.add(new JSeparator(), "span, growx, wrap"); // El separador ocupa toda la fila y salta de línea

        // -- Fila 3: Reporte Diario PDF --
        controlsPanel.add(new JLabel("Fecha para PDF:"));
        datePicker = new DatePicker();
        datePicker.setDateToToday();
        controlsPanel.add(datePicker, "span 2"); // Ocupa 2 celdas

        // Dejamos celdas vacías para alinear el botón a la derecha
        controlsPanel.add(new JLabel(""), "span 3, growx");

        btnGenerarPdf = new JButton("Generar Recibo PDF");
        controlsPanel.add(btnGenerarPdf, "wrap"); // Añadimos el botón y saltamos de línea

        add(controlsPanel, BorderLayout.NORTH);

        // --- TABLA Y RESUMEN (Sin cambios en su lógica o creación) ---
        String[] columnNames = {"Producto", "Cant. Vendida", "Ingresos Brutos", "Costo de Venta", "Ganancia Bruta"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tablaReporte = new JTable(tableModel);
        add(new JScrollPane(tablaReporte), BorderLayout.CENTER);
        JPanel summaryPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Resumen Financiero Semanal"));
        summaryPanel.add(new JLabel("Total Ganancia Bruta:")); lblGananciaBruta = new JLabel(currencyFormatter.format(0)); summaryPanel.add(lblGananciaBruta);
        summaryPanel.add(new JLabel("Total Gastos de la Semana:")); lblGastos = new JLabel(currencyFormatter.format(0)); summaryPanel.add(lblGastos);
        summaryPanel.add(new JLabel("<html><b>GANANCIA NETA:</b></html>")); lblGananciaNeta = new JLabel("<html><b>" + currencyFormatter.format(0) + "</b></html>"); summaryPanel.add(lblGananciaNeta);
        summaryPanel.add(new JLabel("Comisión del Trabajador (18%):")); lblComision = new JLabel(currencyFormatter.format(0)); summaryPanel.add(lblComision);
        add(summaryPanel, BorderLayout.SOUTH);

        configurarAcciones();
    }
    
    // --- LÓGICA DE LOS MÉTODOS (Toda la funcionalidad se mantiene intacta) ---

    @Override
    public void addNotify() {
        super.addNotify();
        cargarTrabajadores();
    }

    private void configurarAcciones() {
        btnGenerarReporte.addActionListener(e -> generarReporteSemanal());
        btnGenerarPdf.addActionListener(e -> generarReportePdf());
    }

    public void cargarTrabajadores() {
        Object selected = trabajadorComboBox.getSelectedItem();
        trabajadorComboBox.removeAllItems();
        List<String> trabajadores = DatabaseManager.getInstance().getTrabajadores();
        for (String trabajador : trabajadores) {
            trabajadorComboBox.addItem(trabajador);
        }
        if (selected != null) {
            trabajadorComboBox.setSelectedItem(selected);
        }
    }

    private void generarReporteSemanal() {
        int anio = (int) yearSpinner.getValue();
        int semana = (int) weekSpinner.getValue();
        String nombreTrabajador = (String) trabajadorComboBox.getSelectedItem();
        if (nombreTrabajador == null) {
            JOptionPane.showMessageDialog(this, "No hay trabajadores seleccionados.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int idTrabajador = DatabaseManager.getInstance().getTrabajadorIdPorNombre(nombreTrabajador);
        tableModel.setRowCount(0);
        List<Object[]> datosVenta = DatabaseManager.getInstance().getDatosReporte(anio, semana, idTrabajador);
        double totalGananciaBruta = 0;
        for (Object[] fila : datosVenta) {
            String nombreProducto = (String) fila[0];
            int cantidadVendida = (int) fila[1];
            double precioVenta = (double) fila[2];
            double precioCompra = (double) fila[3];
            double ingresosBrutos = cantidadVendida * precioVenta;
            double costoVenta = cantidadVendida * precioCompra;
            double gananciaBruta = ingresosBrutos - costoVenta;
            totalGananciaBruta += gananciaBruta;
            tableModel.addRow(new Object[]{nombreProducto, cantidadVendida, currencyFormatter.format(ingresosBrutos), currencyFormatter.format(costoVenta), currencyFormatter.format(gananciaBruta)});
        }
        double totalGastos = DatabaseManager.getInstance().getTotalGastosSemana(anio, semana);
        double gananciaNeta = totalGananciaBruta - totalGastos;
        double comision = gananciaNeta * 0.18;
        lblGananciaBruta.setText(currencyFormatter.format(totalGananciaBruta));
        lblGastos.setText(currencyFormatter.format(totalGastos));
        lblGananciaNeta.setText("<html><b>" + currencyFormatter.format(gananciaNeta) + "</b></html>");
        lblComision.setText(currencyFormatter.format(comision));
    }

    private void generarReportePdf() {
        LocalDate fecha = datePicker.getDate();
        String nombreTrabajador = (String) trabajadorComboBox.getSelectedItem();
        if (fecha == null || nombreTrabajador == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un trabajador y una fecha válida.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idTrabajador = DatabaseManager.getInstance().getTrabajadorIdPorNombre(nombreTrabajador);
        List<Object[]> ventas = DatabaseManager.getInstance().getDatosReporteDiario(fecha, idTrabajador);
        if (ventas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontraron ventas para este trabajador en la fecha seleccionada.", "Sin Datos", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        double totalIngresos = 0;
        for (Object[] venta : ventas) {
            totalIngresos += (int) venta[1] * (double) venta[2];
        }
        double totalGastos = DatabaseManager.getInstance().getTotalGastosDia(fecha);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Recibo PDF");
        fileChooser.setSelectedFile(new File("Recibo_" + nombreTrabajador.replace(" ", "_") + "_" + fecha + ".pdf"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivoParaGuardar = fileChooser.getSelectedFile();
            PdfGenerator.generarReciboDiario(archivoParaGuardar, nombreTrabajador, fecha, ventas, totalIngresos, totalGastos);
            JOptionPane.showMessageDialog(this, "PDF generado exitosamente en: " + archivoParaGuardar.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}