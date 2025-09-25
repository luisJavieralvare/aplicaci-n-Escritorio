package com.tuempresa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class MainFrame extends JFrame {
    
    public MainFrame() {
        setTitle("🚚 Gestión de Microempresa - Sistema de Rutas y Recaudos");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Configurar el ícono de la aplicación (opcional)
        setIconImage(createIcon());
        
        // Configurar Look and Feel mejorado
        setupLookAndFeel();
        
        // Crear el panel de pestañas con diseño mejorado
        JTabbedPane tabbedPane = createStyledTabbedPane();
        
        // Crear los paneles
        InventarioPanel inventarioPanel = new InventarioPanel();
        JornadaPanel jornadaPanel = new JornadaPanel(inventarioPanel);
        RecaudosPanel recaudosPanel = new RecaudosPanel();
        GastosPanel gastosPanel = new GastosPanel();
        ReportePanel reportePanel = new ReportePanel();
        
        // Añadir las pestañas con iconos y tooltips
        tabbedPane.addTab("Jornada", createTabIcon("📋"), jornadaPanel);
        tabbedPane.setToolTipTextAt(0, "Gestión diaria de salidas y devoluciones");
        
        tabbedPane.addTab("Recaudos", createTabIcon("💰"), recaudosPanel);
        tabbedPane.setToolTipTextAt(1, "Registro de recaudos diarios y comisiones");
        
        tabbedPane.addTab("Inventario", createTabIcon("📦"), inventarioPanel);
        tabbedPane.setToolTipTextAt(2, "Gestión de productos, stock y trabajadores");
        
        tabbedPane.addTab("Gastos", createTabIcon("💳"), gastosPanel);
        tabbedPane.setToolTipTextAt(3, "Registro y control de gastos operacionales");
        
        tabbedPane.addTab("Reportes", createTabIcon("📊"), reportePanel);
        tabbedPane.setToolTipTextAt(4, "Reportes semanales y generación de PDFs");
        
        // Configurar el panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(createStatusPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void setupLookAndFeel() {
        try {
            // Configurar colores personalizados para FlatLaf
            UIManager.put("TabbedPane.selectedBackground", new Color(41, 128, 185));
            UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
            UIManager.put("TabbedPane.hoverColor", new Color(52, 152, 219));
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
        } catch (Exception e) {
            System.err.println("Error configurando el tema: " + e.getMessage());
        }
    }
    
    private JTabbedPane createStyledTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                // Añadir efectos visuales adicionales si es necesario
            }
        };
        
        // Configurar el estilo del tabbedPane
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        tabbedPane.setFont(tabbedPane.getFont().deriveFont(Font.BOLD, 12f));
        
        return tabbedPane;
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(44, 62, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Título principal
        JLabel titleLabel = new JLabel("Sistema de Gestión de Microempresa");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
        // Subtítulo
        JLabel subtitleLabel = new JLabel("Gestión de Rutas, Recaudos y Comisiones");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        
        // Panel de información
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        infoPanel.setOpaque(false);
        infoPanel.add(titleLabel);
        infoPanel.add(subtitleLabel);
        
        // Información de la empresa (lado derecho)
        JLabel empresaLabel = new JLabel("🏢 Tu Microempresa");
        empresaLabel.setFont(new Font("Arial", Font.BOLD, 16));
        empresaLabel.setForeground(new Color(52, 152, 219));
        empresaLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        headerPanel.add(infoPanel, BorderLayout.WEST);
        headerPanel.add(empresaLabel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusPanel.setBackground(new Color(248, 249, 250));
        
        // Información de estado
        JLabel statusLabel = new JLabel("✅ Sistema operativo - Base de datos conectada");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(39, 174, 96));
        
        // Información de versión
        JLabel versionLabel = new JLabel("v2.0 - Sistema de Recaudos");
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        versionLabel.setForeground(new Color(127, 140, 141));
        versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(versionLabel, BorderLayout.EAST);
        
        return statusPanel;
    }
    
    private ImageIcon createTabIcon(String emoji) {
        // Crear un ícono simple con emoji para las pestañas
        JLabel label = new JLabel(emoji);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        label.setSize(20, 20);
        
        // Crear imagen desde el componente
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(20, 20, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        label.paint(g2d);
        g2d.dispose();
        
        return new ImageIcon(img);
    }
    
    private Image createIcon() {
        // Crear un ícono simple para la aplicación
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dibujar un ícono simple
        g2d.setColor(new Color(41, 128, 185));
        g2d.fillRoundRect(2, 2, 28, 28, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("$", 12, 22);
        
        g2d.dispose();
        return img;
    }
}