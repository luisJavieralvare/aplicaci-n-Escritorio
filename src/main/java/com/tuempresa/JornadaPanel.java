package com.tuempresa;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class JornadaPanel extends JPanel {
    private JComboBox<String> trabajadorComboBox;
    private JButton btnIniciarJornada, btnGuardarSalida, btnFinalizarJornada, btnGuardarDevolucion;
    private JTable tablaMercancia;
    private DefaultTableModel tableModel;
    private InventarioPanel inventarioPanel;
    private List<Object[]> mercanciaLlevada;

    public JornadaPanel(InventarioPanel inventarioPanel) {
        this.inventarioPanel = inventarioPanel;
        this.mercanciaLlevada = new ArrayList<>();
        setLayout(new BorderLayout(10, 10));
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.add(new JLabel("Seleccionar Trabajador:"));
        trabajadorComboBox = new JComboBox<>();
        trabajadorComboBox.setPreferredSize(new Dimension(200, 25));
        topPanel.add(trabajadorComboBox);
        btnIniciarJornada = new JButton("Iniciar Jornada");
        topPanel.add(btnIniciarJornada);
        add(topPanel, BorderLayout.NORTH);
        tableModel = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (getColumnCount() == 4 && columnIndex == 3) return Integer.class;
                if (getColumnCount() == 3 && columnIndex == 2) return Integer.class;
                return Object.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return (getColumnCount() == 4 && column == 3) || (getColumnCount() == 3 && column == 2);
            }
        };
        tablaMercancia = new JTable(tableModel);
        add(new JScrollPane(tablaMercancia), BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardarSalida = new JButton("Guardar Salida");
        btnFinalizarJornada = new JButton("Finalizar Jornada");
        btnGuardarDevolucion = new JButton("Guardar Devolución");
        bottomPanel.add(btnGuardarSalida);
        bottomPanel.add(btnFinalizarJornada);
        bottomPanel.add(btnGuardarDevolucion);
        add(bottomPanel, BorderLayout.SOUTH);
        configurarAcciones();
        setEstadoInicial();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        cargarTrabajadores();
    }

    private void configurarAcciones() {
        btnIniciarJornada.addActionListener(e -> {
            if (trabajadorComboBox.getSelectedIndex() <= 0) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un trabajador.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            setEstadoJornadaIniciada();
            cargarTablaSalida();
        });
        btnGuardarSalida.addActionListener(e -> guardarSalida());
        btnFinalizarJornada.addActionListener(e -> {
            if (mercanciaLlevada.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Primero debe guardar la salida de mercancía.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            setEstadoDevolucion();
            cargarTablaDevolucion();
        });
        btnGuardarDevolucion.addActionListener(e -> guardarDevolucion());
    }

    private void guardarSalida() {
        if (tablaMercancia.isEditing()) {
            tablaMercancia.getCellEditor().stopCellEditing();
        }
        String nombreTrabajador = (String) trabajadorComboBox.getSelectedItem();
        int idTrabajador = DatabaseManager.getInstance().getTrabajadorIdPorNombre(nombreTrabajador);
        if (idTrabajador == -1) return;
        mercanciaLlevada.clear();
        int registrosGuardados = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Integer cantidad = (Integer) tableModel.getValueAt(i, 3);
            if (cantidad != null && cantidad > 0) {
                Integer stockDisponible = (Integer) tableModel.getValueAt(i, 2);
                if (cantidad > stockDisponible) {
                    JOptionPane.showMessageDialog(this, "No puedes llevar más del stock disponible.", "Error de Stock", JOptionPane.ERROR_MESSAGE);
                    mercanciaLlevada.clear();
                    return;
                }
                Integer idProducto = (Integer) tableModel.getValueAt(i, 0);
                String nombreProducto = (String) tableModel.getValueAt(i, 1);
                DatabaseManager.getInstance().addRegistroJornada(idTrabajador, idProducto, "LLEVADA", cantidad);
                mercanciaLlevada.add(new Object[]{nombreProducto, cantidad, idProducto});
                registrosGuardados++;
            }
        }
        if (registrosGuardados > 0) {
            JOptionPane.showMessageDialog(this, "Salida registrada.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            btnGuardarSalida.setEnabled(false);
            if (inventarioPanel != null) inventarioPanel.refrescarTablaProductos();
        } else {
            JOptionPane.showMessageDialog(this, "No se ha especificado cantidad.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void guardarDevolucion() {
        if (tablaMercancia.isEditing()) {
            tablaMercancia.getCellEditor().stopCellEditing();
        }
        String nombreTrabajador = (String) trabajadorComboBox.getSelectedItem();
        int idTrabajador = DatabaseManager.getInstance().getTrabajadorIdPorNombre(nombreTrabajador);
        if (idTrabajador == -1) return;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Integer cantidadDevuelta = (Integer) tableModel.getValueAt(i, 2);
            if (cantidadDevuelta != null && cantidadDevuelta > 0) {
                Integer cantidadLlevada = (Integer) tableModel.getValueAt(i, 1);
                if (cantidadDevuelta > cantidadLlevada) {
                    JOptionPane.showMessageDialog(this, "No puedes devolver más de lo que te llevaste.", "Error de Cantidad", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Integer idProducto = (Integer) mercanciaLlevada.get(i)[2];
                DatabaseManager.getInstance().addRegistroJornada(idTrabajador, idProducto, "DEVUELTA", cantidadDevuelta);
            }
        }
        JOptionPane.showMessageDialog(this, "Devolución registrada. Jornada finalizada.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        if (inventarioPanel != null) inventarioPanel.refrescarTablaProductos();
        setEstadoInicial();
    }

    public void cargarTrabajadores() {
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

    private void cargarTablaSalida() {
        String[] columnNames = {"ID_Producto", "Nombre", "Stock Disponible", "Cantidad a Llevar"};
        tableModel.setColumnIdentifiers(columnNames);
        tableModel.setRowCount(0);
        List<Object[]> productos = DatabaseManager.getInstance().getProductosConStock();
        for (Object[] producto : productos) {
            tableModel.addRow(new Object[]{producto[0], producto[1], producto[4], 0});
        }
    }

    private void cargarTablaDevolucion() {
        String[] columnNames = {"Producto", "Cantidad Llevada", "Cantidad Devuelta"};
        tableModel.setColumnIdentifiers(columnNames);
        tableModel.setRowCount(0);
        for (Object[] productoLlevado : mercanciaLlevada) {
            String nombre = (String) productoLlevado[0];
            Integer cantidad = (Integer) productoLlevado[1];
            tableModel.addRow(new Object[]{nombre, cantidad, 0});
        }
    }

    private void setEstadoInicial() {
        mercanciaLlevada.clear();
        cargarTrabajadores();
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{});
        trabajadorComboBox.setEnabled(true);
        btnIniciarJornada.setEnabled(true);
        tablaMercancia.setEnabled(false);
        btnGuardarSalida.setEnabled(false);
        btnFinalizarJornada.setEnabled(false);
        btnGuardarDevolucion.setEnabled(false);
    }

    private void setEstadoJornadaIniciada() {
        trabajadorComboBox.setEnabled(false);
        btnIniciarJornada.setEnabled(false);
        tablaMercancia.setEnabled(true);
        btnGuardarSalida.setEnabled(true);
        btnFinalizarJornada.setEnabled(true);
        btnGuardarDevolucion.setEnabled(false);
    }

    private void setEstadoDevolucion() {
        btnIniciarJornada.setEnabled(false);
        btnGuardarSalida.setEnabled(false);
        btnFinalizarJornada.setEnabled(false);
        btnGuardarDevolucion.setEnabled(true);
    }
}