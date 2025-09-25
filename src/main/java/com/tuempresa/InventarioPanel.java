package com.tuempresa;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class InventarioPanel extends JPanel {
    private JTable tablaProductos;
    private DefaultTableModel tableModel;

    public InventarioPanel() {
        setLayout(new BorderLayout(10, 10));
        add(new JLabel("Gestión de Inventario y Catálogos", SwingConstants.CENTER), BorderLayout.NORTH);

        String[] columnNames = {"ID", "Nombre", "Precio Compra", "Precio Venta", "Stock Actual"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaProductos = new JTable(tableModel);
        add(new JScrollPane(tablaProductos), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton btnAnadirProducto = new JButton("Añadir Nuevo Producto");
        JButton btnSurtirStock = new JButton("Surtir Stock");
        JButton btnGestionarTrabajadores = new JButton("Gestionar Trabajadores");
        bottomPanel.add(btnAnadirProducto);
        bottomPanel.add(btnSurtirStock);
        bottomPanel.add(btnGestionarTrabajadores);
        add(bottomPanel, BorderLayout.SOUTH);

        configurarAcciones(btnAnadirProducto, btnSurtirStock, btnGestionarTrabajadores);
        refrescarTablaProductos();
    }

    private void configurarAcciones(JButton btnAnadirProducto, JButton btnSurtirStock, JButton btnGestionarTrabajadores) {
        btnAnadirProducto.addActionListener(e -> {
            JTextField nombreField = new JTextField();
            JTextField precioCompraField = new JTextField();
            JTextField precioVentaField = new JTextField();
            JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
            panel.add(new JLabel("Nombre:")); panel.add(nombreField);
            panel.add(new JLabel("Precio Compra:")); panel.add(precioCompraField);
            panel.add(new JLabel("Precio Venta:")); panel.add(precioVentaField);
            int result = JOptionPane.showConfirmDialog(this, panel, "Añadir Nuevo Producto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String nombre = nombreField.getText();
                    double pCompra = Double.parseDouble(precioCompraField.getText());
                    double pVenta = Double.parseDouble(precioVentaField.getText());
                    if (!nombre.trim().isEmpty()) {
                        DatabaseManager.getInstance().addProducto(nombre, pCompra, pVenta);
                        refrescarTablaProductos();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Por favor, ingrese números válidos para los precios.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnSurtirStock.addActionListener(e -> {
            Vector<String> productosNombres = new Vector<>(DatabaseManager.getInstance().getNombresProductos());
            if (productosNombres.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Primero debe añadir productos.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JComboBox<String> productoComboBox = new JComboBox<>(productosNombres);
            JSpinner cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
            JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
            panel.add(new JLabel("Producto:")); panel.add(productoComboBox);
            panel.add(new JLabel("Cantidad a Surtir:")); panel.add(cantidadSpinner);
            int result = JOptionPane.showConfirmDialog(this, panel, "Surtir Stock", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String nombreProducto = (String) productoComboBox.getSelectedItem();
                int cantidad = (int) cantidadSpinner.getValue();
                if (nombreProducto != null) {
                    DatabaseManager.getInstance().addRegistroSurtido(nombreProducto, cantidad);
                    refrescarTablaProductos();
                }
            }
        });

        btnGestionarTrabajadores.addActionListener(e -> {
            JTextField nombreField = new JTextField();
            JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
            panel.add(new JLabel("Nombre del nuevo trabajador:"));
            panel.add(nombreField);
            int result = JOptionPane.showConfirmDialog(this, panel, "Añadir Trabajador", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String nombre = nombreField.getText();
                if (!nombre.trim().isEmpty()) {
                    DatabaseManager.getInstance().addTrabajador(nombre);
                    JOptionPane.showMessageDialog(this, "Trabajador añadido.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }

    public void refrescarTablaProductos() {
        tableModel.setRowCount(0);
        List<Object[]> productos = DatabaseManager.getInstance().getProductosConStock();
        for (Object[] producto : productos) {
            tableModel.addRow(producto);
        }
    }
}