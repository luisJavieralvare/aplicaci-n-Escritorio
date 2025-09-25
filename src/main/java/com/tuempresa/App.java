package com.tuempresa;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // Establece el tema moderno ANTES de crear cualquier componente gráfico.
        FlatLightLaf.setup();
        // Inicializa la conexión con la base de datos
        DatabaseManager.getInstance();
        // Le dice a Swing que cree la interfaz gráfica de forma segura
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            }
        });
    }
}