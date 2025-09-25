package com.tuempresa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import com.github.lgooddatepicker.components.DatePicker;

import net.miginfocom.swing.MigLayout;

public class ReportePanel extends JPanel {
    // Componentes existentes
    private JSpinner yearSpinner, weekSpinner;
    private JComboBox<String> trabajadorComboBox;
    private DefaultTableModel tableModel;
    private JLabel lblGananciaBruta, lblGastos, lblGananciaNeta, lblComision;
    
    // Nuevos componentes para recaudos
    private JLabel lblTotalRecaudado, lblComisionesReales, lblDiferencia;
    private DefaultTableModel recaudosTableModel;
    private JTable tablaRecaudos;
    
    private NumberFormat currencyFormatter;
    private DatePicker datePicker;
    private JButton btnGenerarPdf;
    private JButton btnGenerarReporte;

    public ReportePanel() {
        setLayout(new BorderLayout(10, 10));
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        // Header mejorado
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Panel central con pestañas para ventas y recaudos
        add(createMainPanel(), BorderLayout.CENTER);
        
        // Panel de resumen en la parte inferior
        add(createResumenPanel(), BorderLayout.SOUTH);

        configurarAcciones();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new MigLayout("insets 10, wrap 8"));
        headerPanel.setBorder(BorderFactory.createTitledBorder("📊 Opciones de Reporte"));
        headerPanel.setBackground(new Color(248, 249, 250));

        // Reporte Semanal
        headerPanel.add(new JLabel("Año:"));
        yearSpinner = new JSpinner(new SpinnerNumberModel(Calendar.getInstance().get(Calendar.YEAR), 2020, 2050, 1));
        headerPanel.add(yearSpinner);

        headerPanel.add(new JLabel("Semana:"));
        weekSpinner = new JSpinner(new SpinnerNumberModel(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR), 1, 53, 1));
        headerPanel.add(weekSpinner);

        headerPanel.add(new JLabel("Trabajador:"));
        trabajadorComboBox = new JComboBox<>();
        headerPanel.add(trabajadorComboBox, "growx");

        btnGenerarReporte = new JButton("📋 Generar Reporte");
        btnGenerarReporte.setBackground(new Color(52, 152, 219));
        btnGenerarReporte.setForeground(Color.WHITE);
        btnGenerarReporte.setFocusPainted(false);
        headerPanel.add(btnGenerarReporte);

        // Separador
        headerPanel.add(new JSeparator(), "span, growx, wrap");

        // Reporte Diario PDF
        headerPanel.add(new JLabel("Fecha PDF:"));
        datePicker = new DatePicker();
        datePicker.setDateToToday();
        headerPanel.add(datePicker, "span 2");

        headerPanel.add(new JLabel(""), "span 3, growx");

        btnGenerarPdf = new JButton("📄 Generar PDF");
        btnGenerarPdf.setBackground(new Color(231, 76, 60));
        btnGenerarPdf.setForeground(Color.WHITE);
        btnGenerarPdf.setFocusPainted(false);
        headerPanel.add(btnGenerarPdf);

        return headerPanel;
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        // Panel izquierdo - Ventas
        JPanel ventasPanel = new JPanel(new BorderLayout());
        ventasPanel.setBorder(BorderFactory.createTitledBorder("💼 Productos Vendidos"));
        
        String[] columnNames = {"Producto", "Cant. Vendida", "Ingresos Brutos", "Costo de Venta", "Ganancia Bruta"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tablaReporte = new JTable(tableModel);
        ventasPanel.add(new JScrollPane(tablaReporte), BorderLayout.CENTER);
        
        // Panel derecho - Recaudos
        JPanel recaudosPanel = new JPanel(new BorderLayout());
        recaudosPanel.setBorder(BorderFactory.createTitledBorder("💰 Recaudos de la Semana"));
        
        String[] recaudosColumns = {"Fecha", "Recaudado", "Comisión"};
        recaudosTableModel = new DefaultTableModel(recaudosColumns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaRecaudos = new JTable(recaudosTableModel);
        recaudosPanel.add(new JScrollPane(tablaRecaudos), BorderLayout.CENTER);
        
        // Dividir el panel principal
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ventasPanel, recaudosPanel);
        splitPane.setResizeWeight(0.6); // 60% para ventas, 40% para recaudos
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        return mainPanel;
    }

    private JPanel createResumenPanel() {
        JPanel summaryPanel = new JPanel(new BorderLayout(10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("💹 Resumen Financiero Semanal"));
        summaryPanel.setBackground(new Color(248, 249, 250));
        
        // Panel de ventas
        JPanel ventasResumen = new JPanel(new GridLayout(4, 2, 10, 5));
        ventasResumen.setBorder(BorderFactory.createTitledBorder("📊 Por Ventas (Teórico)"));
        
        ventasResumen.add(new JLabel("Ganancia Bruta:"));
        lblGananciaBruta = new JLabel(currencyFormatter.format(0));
        lblGananciaBruta.setForeground(new Color(39, 174, 96));
        lblGananciaBruta.setFont(lblGananciaBruta.getFont().deriveFont(Font.BOLD));
        ventasResumen.add(lblGananciaBruta);
        
        ventasResumen.add(new JLabel("Gastos Operacionales:"));
        lblGastos = new JLabel(currencyFormatter.format(0));
        lblGastos.setForeground(new Color(231, 76, 60));
        lblGastos.setFont(lblGastos.getFont().deriveFont(Font.BOLD));
        ventasResumen.add(lblGastos);
        
        ventasResumen.add(new JLabel("<html><b>GANANCIA NETA:</b></html>"));
        lblGananciaNeta = new JLabel("<html><b>" + currencyFormatter.format(0) + "</b></html>");
        lblGananciaNeta.setForeground(new Color(41, 128, 185));
        ventasResumen.add(lblGananciaNeta);
        
        ventasResumen.add(new JLabel("Comisión Teórica (18%):"));
        lblComision = new JLabel(currencyFormatter.format(0));
        lblComision.setForeground(new Color(230, 126, 34));
        lblComision.setFont(lblComision.getFont().deriveFont(Font.BOLD));
        ventasResumen.add(lblComision);
        
        // Panel de recaudos reales
        JPanel recaudosResumen = new JPanel(new GridLayout(4, 2, 10, 5));
        recaudosResumen.setBorder(BorderFactory.createTitledBorder("💰 Por Recaudos (Real)"));
        
        recaudosResumen.add(new JLabel("Total Recaudado:"));
        lblTotalRecaudado = new JLabel(currencyFormatter.format(0));
        lblTotalRecaudado.setForeground(new Color(39, 174, 96));
        lblTotalRecaudado.setFont(lblTotalRecaudado.getFont().deriveFont(Font.BOLD));
        recaudosResumen.add(lblTotalRecaudado);
        
        recaudosResumen.add(new JLabel("Comisiones Pagadas:"));
        lblComisionesReales = new JLabel(currencyFormatter.format(0));
        lblComisionesReales.setForeground(new Color(230, 126, 34));
        lblComisionesReales.setFont(lblComisionesReales.getFont().deriveFont(Font.BOLD));
        recaudosResumen.add(lblComisionesReales);
        
        recaudosResumen.add(new JLabel("<html><b>GANANCIA REAL:</b></html>"));
        JLabel lblGananciaReal = new JLabel("<html><b>" + currencyFormatter.format(0) + "</b></html>");
        lblGananciaReal.setForeground(new Color(41, 128, 185));
        recaudosResumen.add(lblGananciaReal);
        
        recaudosResumen.add(new JLabel("Diferencia vs Teórico:"));
        lblDiferencia = new JLabel(currencyFormatter.format(0));
        lblDiferencia.setFont(lblDiferencia.getFont().deriveFont(Font.BOLD));
        recaudosResumen.add(lblDiferencia);
        
        summaryPanel.add(ventasResumen, BorderLayout.WEST);
        summaryPanel.add(recaudosResumen, BorderLayout.EAST);
        
        return summaryPanel;
    }

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
        
        // Limpiar tablas
        tableModel.setRowCount(0);
        recaudosTableModel.setRowCount(0);
        
        // === REPORTE DE VENTAS (Tabla izquierda) ===
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
            
            tableModel.addRow(new Object[]{
                nombreProducto, 
                cantidadVendida, 
                currencyFormatter.format(ingresosBrutos), 
                currencyFormatter.format(costoVenta), 
                currencyFormatter.format(gananciaBruta)
            });
        }
        
        // === REPORTE DE RECAUDOS (Tabla derecha) ===
        // Obtener recaudos de la semana para este trabajador
        LocalDate inicioSemana = getInicioSemana(anio, semana);
        LocalDate finSemana = inicioSemana.plusDays(6);
        
        List<Object[]> recaudosSemana = DatabaseManager.getInstance()
            .getRecaudosDiarios(idTrabajador, inicioSemana, finSemana);
        
        double totalRecaudado = 0;
        double totalComisionesReales = 0;
        
        for (Object[] recaudo : recaudosSemana) {
            java.sql.Date fecha = (java.sql.Date) recaudo[0];
            double montoRecaudado = (Double) recaudo[1];
            double comision = (Double) recaudo[2];
            
            totalRecaudado += montoRecaudado;
            totalComisionesReales += comision;
            
            recaudosTableModel.addRow(new Object[]{
                fecha,
                currencyFormatter.format(montoRecaudado),
                currencyFormatter.format(comision)
            });
        }
        
        // === CÁLCULOS Y RESUMEN ===
        double totalGastos = DatabaseManager.getInstance().getTotalGastosSemana(anio, semana);
        double gananciaNeta = totalGananciaBruta - totalGastos;
        double comisionTeorica = gananciaNeta * 0.18;
        double gananciaReal = totalRecaudado - totalComisionesReales - totalGastos;
        double diferencia = gananciaReal - gananciaNeta;
        
        // Actualizar labels del resumen
        lblGananciaBruta.setText(currencyFormatter.format(totalGananciaBruta));
        lblGastos.setText(currencyFormatter.format(totalGastos));
        lblGananciaNeta.setText("<html><b>" + currencyFormatter.format(gananciaNeta) + "</b></html>");
        lblComision.setText(currencyFormatter.format(comisionTeorica));
        
        lblTotalRecaudado.setText(currencyFormatter.format(totalRecaudado));
        lblComisionesReales.setText(currencyFormatter.format(totalComisionesReales));
        
        // Actualizar ganancia real
        JLabel lblGananciaReal = (JLabel) ((JPanel) ((JPanel) getComponent(2)).getComponent(1)).getComponent(5);
        lblGananciaReal.setText("<html><b>" + currencyFormatter.format(gananciaReal) + "</b></html>");
        
        lblDiferencia.setText(currencyFormatter.format(diferencia));
        
        // Colorear la diferencia según si es positiva o negativa
        if (diferencia >= 0) {
            lblDiferencia.setForeground(new Color(39, 174, 96)); // Verde para positivo
        } else {
            lblDiferencia.setForeground(new Color(231, 76, 60)); // Rojo para negativo
        }
        
        // Mostrar mensaje informativo
        String mensaje = String.format(
            "Reporte generado para %s - Semana %d/%d:\n\n" +
            "📊 VENTAS TEÓRICAS:\n" +
            "• Ganancia bruta: %s\n" +
            "• Comisión teórica (18%%): %s\n\n" +
            "💰 RECAUDOS REALES:\n" +
            "• Total recaudado: %s\n" +
            "• Comisiones pagadas: %s\n\n" +
            "📈 DIFERENCIA: %s %s",
            nombreTrabajador, semana, anio,
            currencyFormatter.format(totalGananciaBruta),
            currencyFormatter.format(comisionTeorica),
            currencyFormatter.format(totalRecaudado),
            currencyFormatter.format(totalComisionesReales),
            currencyFormatter.format(Math.abs(diferencia)),
            diferencia >= 0 ? "(A favor)" : "(En contra)"
        );
        
        JOptionPane.showMessageDialog(this, mensaje, "Reporte Generado", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Calcula el primer día de una semana específica
     */
    private LocalDate getInicioSemana(int anio, int semana) {
        // Java considera que la semana empieza en lunes
        LocalDate primeroEnero = LocalDate.of(anio, 1, 1);
        LocalDate primerLunes = primeroEnero.with(java.time.temporal.TemporalAdjusters.firstInMonth(java.time.DayOfWeek.MONDAY));
        
        // Si enero 1 no es lunes, ajustamos
        if (primeroEnero.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
            primerLunes = primeroEnero.with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.MONDAY));
        }
        
        return primerLunes.plusWeeks(semana - 1);
    }

    private void generarReportePdf() {
        LocalDate fecha = datePicker.getDate();
        String nombreTrabajador = (String) trabajadorComboBox.getSelectedItem();
        
        if (fecha == null || nombreTrabajador == null) {
            JOptionPane.showMessageDialog(this, 
                "Debe seleccionar un trabajador y una fecha válida.", 
                "Datos incompletos", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idTrabajador = DatabaseManager.getInstance().getTrabajadorIdPorNombre(nombreTrabajador);
        
        // Obtener datos de ventas del día
        List<Object[]> ventas = DatabaseManager.getInstance().getDatosReporteDiario(fecha, idTrabajador);
        
        if (ventas.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No se encontraron ventas para este trabajador en la fecha seleccionada.", 
                "Sin Datos", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Calcular total de ingresos por ventas
        double totalIngresosPorVentas = 0;
        for (Object[] venta : ventas) {
            totalIngresosPorVentas += (int) venta[1] * (double) venta[2];
        }
        
        // Obtener recaudo real del día
        List<Object[]> recaudoDia = DatabaseManager.getInstance()
            .getRecaudosDiarios(idTrabajador, fecha, fecha);
        
        double totalRecaudado = 0;
        if (!recaudoDia.isEmpty()) {
            totalRecaudado = (Double) recaudoDia.get(0)[1]; // total_recaudado
        }
        
        double totalGastos = DatabaseManager.getInstance().getTotalGastosDia(fecha);
        
        // Usar el recaudo real en lugar del teórico
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Recibo PDF");
        fileChooser.setSelectedFile(new File("Recibo_" + nombreTrabajador.replace(" ", "_") + "_" + fecha + ".pdf"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivoParaGuardar = fileChooser.getSelectedFile();
            
            // Generar PDF mejorado con información de recaudos
            PdfGeneratorMejorado.generarReciboDiario(
                archivoParaGuardar, 
                nombreTrabajador, 
                fecha, 
                ventas, 
                totalIngresosPorVentas,
                totalRecaudado,
                totalGastos
            );
            
            JOptionPane.showMessageDialog(this, 
                "PDF generado exitosamente en: " + archivoParaGuardar.getAbsolutePath(), 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}