package com.tuempresa;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String HOST = "localhost";
    private static final String PORT = "5432";
    private static final String DB_NAME = "gestion_microempresa";
    private static final String USER = "postgres";
    private static final String PASSWORD = "luisjavier";
    private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB_NAME;
    private static DatabaseManager instance;

    private DatabaseManager() {
        try (Connection conn = getConnection()) {
            System.out.println("Conexión con PostgreSQL establecida exitosamente.");
        } catch (SQLException e) {
            System.err.println("ERROR FATAL: No se pudo conectar a la base de datos PostgreSQL.");
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    public void addGasto(java.sql.Date fecha, String concepto, double valor) {
        String sql = "INSERT INTO gastos(fecha, concepto, valor) VALUES(?, ?, ?)";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, fecha);
            pstmt.setString(2, concepto);
            pstmt.setDouble(3, valor);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al añadir gasto: " + e.getMessage());
        }
    }
    
    public void addTrabajador(String nombre) {
        String sql = "INSERT INTO trabajadores(nombre) VALUES(?)";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al añadir trabajador: " + e.getMessage());
        }
    }

    public void addProducto(String nombre, double precioCompra, double precioVenta) {
        String sql = "INSERT INTO productos(nombre, precio_compra, precio_venta) VALUES(?,?,?)";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setDouble(2, precioCompra);
            pstmt.setDouble(3, precioVenta);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al añadir producto: " + e.getMessage());
        }
    }

    public void addRegistroSurtido(String nombreProducto, int cantidad) {
        String sql = "INSERT INTO registros(id_producto, tipo_movimiento, cantidad) VALUES ((SELECT id FROM productos WHERE nombre = ?), 'SURTIDO', ?)";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombreProducto);
            pstmt.setInt(2, cantidad);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al surtir stock: " + e.getMessage());
        }
    }

    public void addRegistroJornada(int idTrabajador, int idProducto, String tipoMovimiento, int cantidad) {
        String sql = "INSERT INTO registros(id_trabajador, id_producto, tipo_movimiento, cantidad) VALUES(?, ?, ?, ?)";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idTrabajador);
            pstmt.setInt(2, idProducto);
            pstmt.setString(3, tipoMovimiento);
            pstmt.setInt(4, cantidad);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al añadir registro de jornada: " + e.getMessage());
        }
    }

    public int getTrabajadorIdPorNombre(String nombre) {
        String sql = "SELECT id FROM trabajadores WHERE nombre = ?";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ID de trabajador: " + e.getMessage());
        }
        return -1;
    }

    public List<String> getTrabajadores() {
        List<String> trabajadores = new ArrayList<>();
        String sql = "SELECT nombre FROM trabajadores ORDER BY nombre ASC";
        try (Connection conn = this.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                trabajadores.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener trabajadores: " + e.getMessage());
        }
        return trabajadores;
    }

    public List<Object[]> getProductosConStock() {
        List<Object[]> productos = new ArrayList<>();
        String sql = "SELECT p.id, p.nombre, p.precio_compra, p.precio_venta, COALESCE((SELECT SUM(cantidad) FROM registros WHERE id_producto = p.id AND tipo_movimiento = 'SURTIDO'), 0) + COALESCE((SELECT SUM(cantidad) FROM registros WHERE id_producto = p.id AND tipo_movimiento = 'DEVUELTA'), 0) - COALESCE((SELECT SUM(cantidad) FROM registros WHERE id_producto = p.id AND tipo_movimiento = 'LLEVADA'), 0) AS stock_actual FROM productos p ORDER BY p.nombre ASC";
        try (Connection conn = this.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                productos.add(new Object[]{rs.getInt("id"), rs.getString("nombre"), rs.getDouble("precio_compra"), rs.getDouble("precio_venta"), rs.getInt("stock_actual")});
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos con stock: " + e.getMessage());
        }
        return productos;
    }

    public List<String> getNombresProductos() {
        List<String> nombres = new ArrayList<>();
        String sql = "SELECT nombre FROM productos ORDER BY nombre ASC";
        try (Connection conn = this.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                nombres.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener nombres de productos: " + e.getMessage());
        }
        return nombres;
    }

    public List<Object[]> getGastosRecientes() {
        List<Object[]> gastos = new ArrayList<>();
        String sql = "SELECT fecha, concepto, valor FROM gastos WHERE fecha >= CURRENT_DATE - INTERVAL '7 days' ORDER BY fecha DESC";
        try (Connection conn = this.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                gastos.add(new Object[]{rs.getDate("fecha"), rs.getString("concepto"), rs.getDouble("valor")});
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener gastos recientes: " + e.getMessage());
        }
        return gastos;
    }

    public List<Object[]> getDatosReporte(int anio, int semana, int idTrabajador) {
        List<Object[]> datos = new ArrayList<>();
        String sql = "SELECT p.nombre, p.precio_venta, p.precio_compra, SUM(CASE WHEN r.tipo_movimiento = 'LLEVADA' THEN r.cantidad ELSE 0 END) - SUM(CASE WHEN r.tipo_movimiento = 'DEVUELTA' THEN r.cantidad ELSE 0 END) as cantidad_vendida FROM registros r JOIN productos p ON r.id_producto = p.id WHERE r.id_trabajador = ? AND EXTRACT(YEAR FROM r.fecha) = ? AND EXTRACT(WEEK FROM r.fecha) = ? GROUP BY p.id, p.nombre, p.precio_venta, p.precio_compra HAVING SUM(CASE WHEN r.tipo_movimiento = 'LLEVADA' THEN r.cantidad ELSE 0 END) > 0";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idTrabajador);
            pstmt.setInt(2, anio);
            pstmt.setInt(3, semana);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                datos.add(new Object[]{rs.getString("nombre"), rs.getInt("cantidad_vendida"), rs.getDouble("precio_venta"), rs.getDouble("precio_compra")});
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener datos del reporte: " + e.getMessage());
        }
        return datos;
    }

    public double getTotalGastosSemana(int anio, int semana) {
        String sql = "SELECT COALESCE(SUM(valor), 0) as total_gastos FROM gastos WHERE EXTRACT(YEAR FROM fecha) = ? AND EXTRACT(WEEK FROM fecha) = ?";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, anio);
            pstmt.setInt(2, semana);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_gastos");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener total de gastos: " + e.getMessage());
        }
        return 0.0;
    }
    
    // --- MÉTODOS AÑADIDOS PARA EL PDF ---
    
    public List<Object[]> getDatosReporteDiario(LocalDate fecha, int idTrabajador) {
        List<Object[]> datos = new ArrayList<>();
        String sql = "SELECT p.nombre, p.precio_venta, " +
                     "SUM(CASE WHEN r.tipo_movimiento = 'LLEVADA' THEN r.cantidad ELSE 0 END) - " +
                     "SUM(CASE WHEN r.tipo_movimiento = 'DEVUELTA' THEN r.cantidad ELSE 0 END) as cantidad_vendida " +
                     "FROM registros r " +
                     "JOIN productos p ON r.id_producto = p.id " +
                     "WHERE r.id_trabajador = ? AND DATE(r.fecha) = ? " +
                     "GROUP BY p.id, p.nombre, p.precio_venta " +
                     "HAVING SUM(CASE WHEN r.tipo_movimiento = 'LLEVADA' THEN r.cantidad ELSE 0 END) - SUM(CASE WHEN r.tipo_movimiento = 'DEVUELTA' THEN r.cantidad ELSE 0 END) > 0";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idTrabajador);
            pstmt.setDate(2, Date.valueOf(fecha));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                datos.add(new Object[]{ rs.getString("nombre"), rs.getInt("cantidad_vendida"), rs.getDouble("precio_venta") });
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener datos del reporte diario: " + e.getMessage());
        }
        return datos;
    }

    public double getTotalGastosDia(LocalDate fecha) {
        String sql = "SELECT COALESCE(SUM(valor), 0) as total_gastos FROM gastos WHERE fecha = ?";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(fecha));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_gastos");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener total de gastos del día: " + e.getMessage());
        }
        return 0.0;
    }
}