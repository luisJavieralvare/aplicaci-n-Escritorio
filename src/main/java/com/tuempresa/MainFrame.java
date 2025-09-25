package com.tuempresa;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Gestión de Microempresa - Rutas de Venta");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        InventarioPanel inventarioPanel = new InventarioPanel();
        JornadaPanel jornadaPanel = new JornadaPanel(inventarioPanel);
        GastosPanel gastosPanel = new GastosPanel();
        ReportePanel reportePanel = new ReportePanel();

        tabbedPane.addTab("Jornada Diaria", jornadaPanel);
        tabbedPane.addTab("Inventario y Compras", inventarioPanel);
        tabbedPane.addTab("Registro de Gastos", gastosPanel);
        tabbedPane.addTab("Reporte Semanal", reportePanel);

        add(tabbedPane);
    }
}