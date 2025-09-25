package com.tuempresa;

// 1. Importamos la nueva librería
import net.miginfocom.swing.MigLayout;

import java.awt.BorderLayout; // Mantenemos este por si acaso, aunque MigLayout lo puede reemplazar
import java.sql.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class GastosPanel extends JPanel {
    private JTextField conceptoField;
    private JSpinner valorSpinner;
    private DefaultTableModel tableModel;

    public GastosPanel() {
        // 2. Establecemos MigLayout como el diseñador principal del panel
        // "fill" = El layout llenará todo el panel.
        // "[grow]" = La única columna que definimos crecerá horizontalmente.
        // "[][grow]" = Definimos dos filas: la primera (formulario) con su altura natural, la segunda (tabla) crecerá verticalmente.
        setLayout(new MigLayout("fill", "[grow]", "[][grow]"));

        // --- PANEL SUPERIOR PARA INGRESAR DATOS ---
        // "wrap 2" = crea una cuadrícula de 2 columnas. Después del 2do componente, salta a la siguiente línea.
        // "[align right]" = la columna 1 (donde van los JLabel) se alinea a la derecha.
        // "[grow, fill]" = la columna 2 (donde van los campos) crece para llenar el espacio.
        JPanel topPanel = new JPanel(new MigLayout("wrap 2", "[align right][grow, fill]"));
        topPanel.setBorder(BorderFactory.createTitledBorder("Nuevo Gasto"));

        // 3. Añadimos componentes de forma mucho más simple
        topPanel.add(new JLabel("Concepto:"));
        conceptoField = new JTextField();
        topPanel.add(conceptoField, "growx"); // "growx" = le dice a este componente que crezca en el eje X.

        topPanel.add(new JLabel("Valor:"));
        valorSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000000.0, 100.0));
        topPanel.add(valorSpinner, "w 150!"); // "w 150!" = le da un ancho preferido y fijo de 150px.

        JButton btnGuardarGasto = new JButton("Guardar Gasto");
        // "skip 1" = salta la primera columna. "align right" = alinea este componente a la derecha de su celda.
        topPanel.add(btnGuardarGasto, "skip 1, align right");

        // 4. Añadimos el formulario al panel principal
        // "dock north" = pégalo en la parte de arriba del panel principal.
        add(topPanel, "dock north");

        // --- TABLA CENTRAL (Sin cambios en su creación) ---
        String[] columnNames = {"Fecha", "Concepto", "Valor"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tablaGastos = new JTable(tableModel);
        
        // 5. Añadimos la tabla al panel principal
        // "grow" = le dice a la tabla que crezca para ocupar todo el espacio restante.
        add(new JScrollPane(tablaGastos), "grow");

        // --- LÓGICA (Exactamente la misma que antes) ---
        btnGuardarGasto.addActionListener(e -> {
            String concepto = conceptoField.getText();
            Double valor = (Double) valorSpinner.getValue();
            if (concepto.trim().isEmpty() || valor <= 0) {
                JOptionPane.showMessageDialog(this, "El concepto no puede estar vacío y el valor debe ser mayor a cero.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DatabaseManager.getInstance().addGasto(new Date(System.currentTimeMillis()), concepto, valor);
            conceptoField.setText("");
            valorSpinner.setValue(0.0);
            refrescarTablaGastos();
            JOptionPane.showMessageDialog(this, "Gasto registrado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        });
        refrescarTablaGastos();
    }

    public void refrescarTablaGastos() {
        tableModel.setRowCount(0);
        List<Object[]> gastos = DatabaseManager.getInstance().getGastosRecientes();
        for (Object[] gasto : gastos) {
            tableModel.addRow(gasto);
        }
    }
}