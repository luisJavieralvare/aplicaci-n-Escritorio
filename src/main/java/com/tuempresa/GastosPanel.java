package com.tuempresa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.github.lgooddatepicker.components.DatePicker;

import net.miginfocom.swing.MigLayout;

public class GastosPanel extends JPanel {
    private JTextField conceptoField;
    private JSpinner valorSpinner;
    private DatePicker datePicker;
    private DefaultTableModel tableModel;
    private JTable tablaGastos;
    private DatePicker fechaInicioFiltro, fechaFinFiltro;
    private JLabel lblTotalGastos;
    private NumberFormat currencyFormatter;

    public GastosPanel() {
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        setLayout(new BorderLayout(10, 10));
        
        initializeComponents();
        setupLayout();
        configurarAcciones();
        configurarTabla();
        refrescarTablaGastos();
    }

    private void initializeComponents() {
        // Componentes del formulario
        conceptoField = new JTextField();
        valorSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000000.0, 1000.0));
        datePicker = new DatePicker();
        datePicker.setDateToToday();
        
        // Componentes de filtros
        fechaInicioFiltro = new DatePicker();
        fechaFinFiltro = new DatePicker();
        fechaInicioFiltro.setDate(LocalDate.now().minusDays(7));
        fechaFinFiltro.setDate(LocalDate.now());
        
        // Label de resumen
        lblTotalGastos = new JLabel(currencyFormatter.format(0));
        
        // Configurar spinner con formato de moneda
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) valorSpinner.getEditor();
        editor.getFormat().setMinimumFractionDigits(0);
        editor.getFormat().setMaximumFractionDigits(0);
    }

    private void setupLayout() {
        // Header con título
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("💳 Registro de Gastos Operacionales", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(new Color(231, 76, 60));
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);

        // Panel central
        JPanel centralPanel = new JPanel(new BorderLayout(10, 10));
        
        // Panel de formulario (arriba)
        JPanel formularioPanel = createFormularioPanel();
        centralPanel.add(formularioPanel, BorderLayout.NORTH);
        
        // Panel de tabla (centro)
        JPanel tablaPanel = createTablaPanel();
        centralPanel.add(tablaPanel, BorderLayout.CENTER);
        
        // Panel de resumen (abajo)
        JPanel resumenPanel = createResumenPanel();
        centralPanel.add(resumenPanel, BorderLayout.SOUTH);
        
        add(centralPanel, BorderLayout.CENTER);
    }

    private JPanel createFormularioPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx, wrap 4", "[align right][grow, fill][align right][grow, fill]"));
        panel.setBorder(BorderFactory.createTitledBorder("📝 Registrar Nuevo Gasto"));
        panel.setBackground(new Color(248, 249, 250));
        
        // Primera fila
        panel.add(new JLabel("Fecha:"));
        panel.add(datePicker);
        
        panel.add(new JLabel("Valor:"));
        panel.add(valorSpinner);
        
        // Segunda fila
        panel.add(new JLabel("Concepto:"));
        panel.add(conceptoField, "span 3, growx"); // El concepto ocupa las 3 columnas restantes
        
        // Tercera fila - Botones
        JButton btnGuardar = new JButton("💾 Guardar Gasto");
        btnGuardar.setBackground(new Color(231, 76, 60));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        
        JButton btnLimpiar = new JButton("🗑️ Limpiar");
        btnLimpiar.setBackground(new Color(149, 165, 166));
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setFocusPainted(false);
        
        panel.add(new JLabel(""), "skip 1"); // Salta una celda
        panel.add(btnGuardar, "split 2, growx");
        panel.add(btnLimpiar, "growx");
        
        // Configurar acciones
        btnGuardar.addActionListener(e -> guardarGasto());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        
        return panel;
    }

    private JPanel createTablaPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("📊 Historial de Gastos"));
        
        // Panel de filtros
        JPanel filtrosPanel = new JPanel(new MigLayout("insets 5, wrap 6"));
        
        filtrosPanel.add(new JLabel("Filtrar desde:"));
        filtrosPanel.add(fechaInicioFiltro);
        filtrosPanel.add(new JLabel("hasta:"));
        filtrosPanel.add(fechaFinFiltro);
        
        JButton btnFiltrar = new JButton("🔍 Aplicar Filtro");
        btnFiltrar.setBackground(new Color(52, 152, 219));
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setFocusPainted(false);
        btnFiltrar.addActionListener(e -> refrescarTablaGastos());
        
        JButton btnSemana = new JButton("📅 Esta Semana");
        btnSemana.addActionListener(e -> {
            LocalDate hoy = LocalDate.now();
            fechaInicioFiltro.setDate(hoy.minusDays(7));
            fechaFinFiltro.setDate(hoy);
            refrescarTablaGastos();
        });
        
        filtrosPanel.add(btnFiltrar);
        filtrosPanel.add(btnSemana);
        
        panel.add(filtrosPanel, BorderLayout.NORTH);
        
        // Tabla
        String[] columnNames = {"Fecha", "Concepto", "Valor"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return java.util.Date.class;
                if (column == 2) return Double.class;
                return String.class;
            }
        };
        
        tablaGastos = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tablaGastos);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createResumenPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx, wrap 2", "[align right][grow]"));
        panel.setBorder(BorderFactory.createTitledBorder("💹 Resumen de Gastos"));
        panel.setBackground(new Color(248, 249, 250));
        
        // Configurar label de total con estilo
        lblTotalGastos.setForeground(new Color(231, 76, 60));
        lblTotalGastos.setFont(lblTotalGastos.getFont().deriveFont(Font.BOLD, 16f));
        
        JLabel labelTotal = new JLabel("💰 Total de Gastos (período seleccionado):");
        labelTotal.setFont(labelTotal.getFont().deriveFont(Font.BOLD, 14f));
        
        panel.add(labelTotal);
        panel.add(lblTotalGastos);
        
        return panel;
    }

    private void configurarTabla() {
        // Configurar renderizador para moneda
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                if (value instanceof Double) {
                    setText(currencyFormatter.format(value));
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    super.setValue(value);
                }
            }
        };
        
        tablaGastos.getColumnModel().getColumn(2).setCellRenderer(currencyRenderer);
        
        // Configurar anchos de columnas
        tablaGastos.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablaGastos.getColumnModel().getColumn(1).setPreferredWidth(300);
        tablaGastos.getColumnModel().getColumn(2).setPreferredWidth(120);
        
        // Alternar colores de filas
        tablaGastos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(248, 249, 250));
                    }
                }
                
                // Aplicar formato de moneda si es necesario
                if (column == 2 && value instanceof Double) {
                    setText(currencyFormatter.format(value));
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }
                
                return c;
            }
        });
        
        // Configurar altura de filas
        tablaGastos.setRowHeight(25);
    }

    private void configurarAcciones() {
        // Permitir guardar con Enter en el campo concepto
        conceptoField.addActionListener(e -> guardarGasto());
        
        // Permitir guardar con Enter en el spinner
        valorSpinner.addChangeListener(e -> {
            // Opcional: validar valores mientras se cambian
        });
    }

    private void guardarGasto() {
        String concepto = conceptoField.getText().trim();
        Double valor = (Double) valorSpinner.getValue();
        LocalDate fecha = datePicker.getDate();
        
        // Validaciones
        if (concepto.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "El concepto del gasto no puede estar vacío.", 
                "Campo requerido", 
                JOptionPane.WARNING_MESSAGE);
            conceptoField.requestFocus();
            return;
        }
        
        if (valor <= 0) {
            JOptionPane.showMessageDialog(this, 
                "El valor debe ser mayor a cero.", 
                "Valor inválido", 
                JOptionPane.WARNING_MESSAGE);
            valorSpinner.requestFocus();
            return;
        }
        
        if (fecha == null) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, seleccione una fecha.", 
                "Campo requerido", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Guardar el gasto
        DatabaseManager.getInstance().addGasto(Date.valueOf(fecha), concepto, valor);
        
        // Mostrar mensaje de confirmación
        String mensaje = String.format(
            "Gasto registrado exitosamente:\n\n" +
            "📅 Fecha: %s\n" +
            "📝 Concepto: %s\n" +
            "💰 Valor: %s",
            fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            concepto,
            currencyFormatter.format(valor)
        );
        
        JOptionPane.showMessageDialog(this, 
            mensaje, 
            "Gasto Guardado", 
            JOptionPane.INFORMATION_MESSAGE);
        
        limpiarFormulario();
        refrescarTablaGastos();
    }

    private void limpiarFormulario() {
        conceptoField.setText("");
        valorSpinner.setValue(0.0);
        datePicker.setDateToToday();
        conceptoField.requestFocus();
    }

    public void refrescarTablaGastos() {
        tableModel.setRowCount(0);
        
        LocalDate fechaInicio = fechaInicioFiltro.getDate();
        LocalDate fechaFin = fechaFinFiltro.getDate();
        
        if (fechaInicio == null || fechaFin == null) {
            // Si no hay filtros, mostrar los últimos 7 días
            fechaInicio = LocalDate.now().minusDays(7);
            fechaFin = LocalDate.now();
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            JOptionPane.showMessageDialog(this, 
                "La fecha de inicio no puede ser posterior a la fecha de fin.", 
                "Fechas inválidas", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Obtener gastos filtrados por fecha
        List<Object[]> gastos = getGastosPorPeriodo(fechaInicio, fechaFin);
        
        double totalGastos = 0;
        for (Object[] gasto : gastos) {
            tableModel.addRow(gasto);
            totalGastos += (Double) gasto[2];
        }
        
        // Actualizar label de total
        lblTotalGastos.setText(currencyFormatter.format(totalGastos));
    }
    
    private List<Object[]> getGastosPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        return DatabaseManager.getInstance().getGastosPorPeriodo(fechaInicio, fechaFin);
    }
}