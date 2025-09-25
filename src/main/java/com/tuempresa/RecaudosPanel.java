package com.tuempresa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.github.lgooddatepicker.components.DatePicker;

import net.miginfocom.swing.MigLayout;

public class RecaudosPanel extends JPanel {
    private DatePicker datePicker;
    private JComboBox<String> trabajadorComboBox;
    private JSpinner montoRecaudadoSpinner;
    private JTextArea observacionesArea;
    private DefaultTableModel tableModel;
    private JTable tablaRecaudos;
    private JLabel lblTotalRecaudado, lblTotalComisiones, lblGananciaNeta;
    private NumberFormat currencyFormatter;
    private DatePicker fechaInicioFiltro, fechaFinFiltro;

    public RecaudosPanel() {
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        setLayout(new BorderLayout(10, 10));
        
        initializeComponents();
        setupLayout();
        configurarAcciones();
        configurarTabla();
        refrescarTabla();
    }

    private void initializeComponents() {
        // Componentes del formulario
        datePicker = new DatePicker();
        datePicker.setDateToToday();
        
        trabajadorComboBox = new JComboBox<>();
        
        montoRecaudadoSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10000000.0, 1000.0));
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) montoRecaudadoSpinner.getEditor();
        editor.getFormat().setMinimumFractionDigits(0);
        editor.getFormat().setMaximumFractionDigits(0);
        
        observacionesArea = new JTextArea(3, 20);
        observacionesArea.setLineWrap(true);
        observacionesArea.setWrapStyleWord(true);
        
        // Componentes de filtros
        fechaInicioFiltro = new DatePicker();
        fechaFinFiltro = new DatePicker();
        fechaInicioFiltro.setDate(LocalDate.now().minusDays(7));
        fechaFinFiltro.setDate(LocalDate.now());
        
        // Labels de resumen
        lblTotalRecaudado = new JLabel(currencyFormatter.format(0));
        lblTotalComisiones = new JLabel(currencyFormatter.format(0));
        lblGananciaNeta = new JLabel(currencyFormatter.format(0));
    }

    private void setupLayout() {
        // Panel principal con título
        add(new JLabel("💰 Gestión de Recaudos Diarios", SwingConstants.CENTER) {{
            setFont(getFont().deriveFont(Font.BOLD, 18f));
            setForeground(new Color(41, 128, 185));
        }}, BorderLayout.NORTH);

        // Panel central dividido en tres secciones
        JPanel centralPanel = new JPanel(new BorderLayout(10, 10));
        
        // 1. Formulario de registro (Izquierda)
        JPanel formularioPanel = createFormularioPanel();
        
        // 2. Tabla con filtros (Centro)
        JPanel tablaPanel = createTablaPanel();
        
        // 3. Panel de resumen (Abajo)
        JPanel resumenPanel = createResumenPanel();
        
        centralPanel.add(formularioPanel, BorderLayout.WEST);
        centralPanel.add(tablaPanel, BorderLayout.CENTER);
        centralPanel.add(resumenPanel, BorderLayout.SOUTH);
        
        add(centralPanel, BorderLayout.CENTER);
    }

    private JPanel createFormularioPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx, wrap 2", "[align right][grow, fill]"));
        panel.setBorder(BorderFactory.createTitledBorder("📝 Registrar Recaudo"));
        panel.setPreferredSize(new Dimension(350, 0));
        
        // Campos del formulario
        panel.add(new JLabel("Fecha:"));
        panel.add(datePicker);
        
        panel.add(new JLabel("Trabajador:"));
        panel.add(trabajadorComboBox);
        
        panel.add(new JLabel("Monto Recaudado:"));
        panel.add(montoRecaudadoSpinner);
        
        panel.add(new JLabel("Observaciones:"), "top");
        panel.add(new JScrollPane(observacionesArea), "height 80!");
        
        // Botones
        JButton btnGuardar = new JButton("💾 Guardar Recaudo");
        btnGuardar.setBackground(new Color(39, 174, 96));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        
        JButton btnLimpiar = new JButton("🗑️ Limpiar");
        btnLimpiar.setBackground(new Color(231, 76, 60));
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setFocusPainted(false);
        
        panel.add(btnGuardar, "span 2, split 2, growx");
        panel.add(btnLimpiar, "growx");
        
        // Configurar acciones de los botones
        btnGuardar.addActionListener(e -> guardarRecaudo());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        
        return panel;
    }

    private JPanel createTablaPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("📊 Historial de Recaudos"));
        
        // Panel de filtros
        JPanel filtrosPanel = new JPanel(new MigLayout("insets 5, wrap 6"));
        filtrosPanel.add(new JLabel("Desde:"));
        filtrosPanel.add(fechaInicioFiltro);
        filtrosPanel.add(new JLabel("Hasta:"));
        filtrosPanel.add(fechaFinFiltro);
        
        JButton btnFiltrar = new JButton("🔍 Filtrar");
        btnFiltrar.setBackground(new Color(52, 152, 219));
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setFocusPainted(false);
        btnFiltrar.addActionListener(e -> refrescarTabla());
        
        JButton btnHoy = new JButton("📅 Hoy");
        btnHoy.addActionListener(e -> {
            LocalDate hoy = LocalDate.now();
            fechaInicioFiltro.setDate(hoy);
            fechaFinFiltro.setDate(hoy);
            refrescarTabla();
        });
        
        filtrosPanel.add(btnFiltrar);
        filtrosPanel.add(btnHoy);
        
        panel.add(filtrosPanel, BorderLayout.NORTH);
        
        // Tabla
        String[] columnNames = {"Fecha", "Trabajador", "Recaudado", "Comisión (18%)", "Observaciones"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: return java.util.Date.class;
                    case 2: case 3: return Double.class;
                    default: return String.class;
                }
            }
        };
        
        tablaRecaudos = new JTable(tableModel);
        panel.add(new JScrollPane(tablaRecaudos), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createResumenPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx, wrap 6", "[align right][grow][align right][grow][align right][grow]"));
        panel.setBorder(BorderFactory.createTitledBorder("💹 Resumen Financiero"));
        panel.setBackground(new Color(248, 249, 250));
        
        // Configurar labels de resumen con colores
        lblTotalRecaudado.setForeground(new Color(39, 174, 96));
        lblTotalRecaudado.setFont(lblTotalRecaudado.getFont().deriveFont(Font.BOLD));
        
        lblTotalComisiones.setForeground(new Color(230, 126, 34));
        lblTotalComisiones.setFont(lblTotalComisiones.getFont().deriveFont(Font.BOLD));
        
        lblGananciaNeta.setForeground(new Color(41, 128, 185));
        lblGananciaNeta.setFont(lblGananciaNeta.getFont().deriveFont(Font.BOLD, 14f));
        
        panel.add(new JLabel("💰 Total Recaudado:"));
        panel.add(lblTotalRecaudado);
        panel.add(new JLabel("👷 Total Comisiones:"));
        panel.add(lblTotalComisiones);
        panel.add(new JLabel("📈 Ganancia Neta:"));
        panel.add(lblGananciaNeta);
        
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
        
        tablaRecaudos.getColumnModel().getColumn(2).setCellRenderer(currencyRenderer);
        tablaRecaudos.getColumnModel().getColumn(3).setCellRenderer(currencyRenderer);
        
        // Configurar anchos de columnas
        tablaRecaudos.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablaRecaudos.getColumnModel().getColumn(1).setPreferredWidth(150);
        tablaRecaudos.getColumnModel().getColumn(2).setPreferredWidth(120);
        tablaRecaudos.getColumnModel().getColumn(3).setPreferredWidth(120);
        tablaRecaudos.getColumnModel().getColumn(4).setPreferredWidth(200);
        
        // Alternar colores de filas
        tablaRecaudos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
                if ((column == 2 || column == 3) && value instanceof Double) {
                    setText(currencyFormatter.format(value));
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }
                
                return c;
            }
        });
    }

    private void configurarAcciones() {
        // Cargar trabajadores cuando el panel se hace visible
        SwingUtilities.invokeLater(this::cargarTrabajadores);
    }

    private void cargarTrabajadores() {
        Object selected = trabajadorComboBox.getSelectedItem();
        trabajadorComboBox.removeAllItems();
        trabajadorComboBox.addItem("Seleccione un trabajador");
        
        List<String> trabajadores = DatabaseManager.getInstance().getTrabajadores();
        for (String trabajador : trabajadores) {
            trabajadorComboBox.addItem(trabajador);
        }
        
        if (selected != null) {
            trabajadorComboBox.setSelectedItem(selected);
        }
    }

    private void guardarRecaudo() {
        // Validaciones
        if (trabajadorComboBox.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, seleccione un trabajador.", 
                "Campo requerido", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        LocalDate fecha = datePicker.getDate();
        if (fecha == null) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, seleccione una fecha.", 
                "Campo requerido", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Double montoRecaudado = (Double) montoRecaudadoSpinner.getValue();
        if (montoRecaudado <= 0) {
            JOptionPane.showMessageDialog(this, 
                "El monto recaudado debe ser mayor a cero.", 
                "Monto inválido", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String nombreTrabajador = (String) trabajadorComboBox.getSelectedItem();
        int idTrabajador = DatabaseManager.getInstance().getTrabajadorIdPorNombre(nombreTrabajador);
        
        if (idTrabajador == -1) {
            JOptionPane.showMessageDialog(this, 
                "Error al obtener el trabajador seleccionado.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String observaciones = observacionesArea.getText().trim();
        if (observaciones.isEmpty()) {
            observaciones = null;
        }
        
        // Guardar el recaudo
        DatabaseManager.getInstance().registrarRecaudoDiario(
            idTrabajador, fecha, montoRecaudado, observaciones);
        
        // Mostrar mensaje de éxito
        double comision = montoRecaudado * 0.18;
        String mensaje = String.format(
            "Recaudo registrado exitosamente:\n\n" +
            "💰 Recaudado: %s\n" +
            "👷 Comisión (18%%): %s\n" +
            "🏢 Para la empresa: %s",
            currencyFormatter.format(montoRecaudado),
            currencyFormatter.format(comision),
            currencyFormatter.format(montoRecaudado - comision)
        );
        
        JOptionPane.showMessageDialog(this, 
            mensaje, 
            "Recaudo Guardado", 
            JOptionPane.INFORMATION_MESSAGE);
        
        limpiarFormulario();
        refrescarTabla();
    }

    private void limpiarFormulario() {
        datePicker.setDateToToday();
        trabajadorComboBox.setSelectedIndex(0);
        montoRecaudadoSpinner.setValue(0.0);
        observacionesArea.setText("");
    }

    private void refrescarTabla() {
        tableModel.setRowCount(0);
        
        LocalDate fechaInicio = fechaInicioFiltro.getDate();
        LocalDate fechaFin = fechaFinFiltro.getDate();
        
        if (fechaInicio == null || fechaFin == null) {
            return;
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            JOptionPane.showMessageDialog(this, 
                "La fecha de inicio no puede ser posterior a la fecha de fin.", 
                "Fechas inválidas", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        List<Object[]> recaudos = DatabaseManager.getInstance()
            .getTodosLosRecaudosDiarios(fechaInicio, fechaFin);
        
        double totalRecaudado = 0;
        double totalComisiones = 0;
        
        for (Object[] recaudo : recaudos) {
            tableModel.addRow(recaudo);
            totalRecaudado += (Double) recaudo[2];
            totalComisiones += (Double) recaudo[3];
        }
        
        // Actualizar labels de resumen
        double gananciaNeta = totalRecaudado - totalComisiones;
        lblTotalRecaudado.setText(currencyFormatter.format(totalRecaudado));
        lblTotalComisiones.setText(currencyFormatter.format(totalComisiones));
        lblGananciaNeta.setText(currencyFormatter.format(gananciaNeta));
    }

    @Override
    public void addNotify() {
        super.addNotify();
        cargarTrabajadores();
        refrescarTabla();
    }
}