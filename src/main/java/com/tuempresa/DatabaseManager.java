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
    
    // ============= MÉTODOS ORIGINALES =============
    
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
        String sql = "SELECT nombre FROM trabajadores WHERE activo = TRUE ORDER BY nombre ASC";
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
        String sql = "SELECT * FROM vista_inventario_actual ORDER BY nombre";
        try (Connection conn = this.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                productos.add(new Object[]{
                    rs.getInt("id"), 
                    rs.getString("nombre"), 
                    rs.getDouble("precio_compra"), 
                    rs.getDouble("precio_venta"), 
                    rs.getInt("stock_actual")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos con stock: " + e.getMessage());
        }
        return productos;
    }

    public List<String> getNombresProductos() {
        List<String> nombres = new ArrayList<>();
        String sql = "SELECT nombre FROM productos WHERE activo = TRUE ORDER BY nombre ASC";
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

    // ============= NUEVOS MÉTODOS PARA SISTEMA DE RECAUDOS =============
    
    /**
     * Registra el recaudo diario de un trabajador
     */
    public void registrarRecaudoDiario(int idTrabajador, LocalDate fecha, double totalRecaudado, String observaciones) {
        double porcentajeComision = 18.0;
        double comisionTrabajador = totalRecaudado * (porcentajeComision / 100.0);
        
        String sql = "INSERT INTO recaudo_diario (id_trabajador, fecha, total_recaudado, comision_trabajador, porcentaje_comision, observaciones) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id_trabajador, fecha) " +
                     "DO UPDATE SET total_recaudado = ?, comision_trabajador = ?, observaciones = ?";
        
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idTrabajador);
            pstmt.setDate(2, Date.valueOf(fecha));
            pstmt.setDouble(3, totalRecaudado);
            pstmt.setDouble(4, comisionTrabajador);
            pstmt.setDouble(5, porcentajeComision);
            pstmt.setString(6, observaciones);
            // Para el ON CONFLICT UPDATE
            pstmt.setDouble(7, totalRecaudado);
            pstmt.setDouble(8, comisionTrabajador);
            pstmt.setString(9, observaciones);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al registrar recaudo diario: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene los recaudos diarios de un trabajador
     */
    public List<Object[]> getRecaudosDiarios(int idTrabajador, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Object[]> recaudos = new ArrayList<>();
        String sql = "SELECT rd.fecha, rd.total_recaudado, rd.comision_trabajador, rd.observaciones, t.nombre " +
                     "FROM recaudo_diario rd " +
                     "JOIN trabajadores t ON rd.id_trabajador = t.id " +
                     "WHERE rd.id_trabajador = ? AND rd.fecha BETWEEN ? AND ? " +
                     "ORDER BY rd.fecha DESC";
        
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idTrabajador);
            pstmt.setDate(2, Date.valueOf(fechaInicio));
            pstmt.setDate(3, Date.valueOf(fechaFin));
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                recaudos.add(new Object[]{
                    rs.getDate("fecha"),
                    rs.getDouble("total_recaudado"),
                    rs.getDouble("comision_trabajador"),
                    rs.getString("observaciones"),
                    rs.getString("nombre")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener recaudos diarios: " + e.getMessage());
        }
        return recaudos;
    }
    
    /**
     * Obtiene todos los recaudos diarios en un rango de fechas
     */
    public List<Object[]> getTodosLosRecaudosDiarios(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Object[]> recaudos = new ArrayList<>();
        String sql = "SELECT rd.fecha, t.nombre, rd.total_recaudado, rd.comision_trabajador, rd.observaciones " +
                     "FROM recaudo_diario rd " +
                     "JOIN trabajadores t ON rd.id_trabajador = t.id " +
                     "WHERE rd.fecha BETWEEN ? AND ? " +
                     "ORDER BY rd.fecha DESC, t.nombre";
        
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(fechaInicio));
            pstmt.setDate(2, Date.valueOf(fechaFin));
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                recaudos.add(new Object[]{
                    rs.getDate("fecha"),
                    rs.getString("nombre"),
                    rs.getDouble("total_recaudado"),
                    rs.getDouble("comision_trabajador"),
                    rs.getString("observaciones")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los recaudos diarios: " + e.getMessage());
        }
        return recaudos;
    }
    
    /**
     * Obtiene el total recaudado por todos los trabajadores en una fecha
     */
    public double getTotalRecaudadoPorFecha(LocalDate fecha) {
        String sql = "SELECT COALESCE(SUM(total_recaudado), 0) FROM recaudo_diario WHERE fecha = ?";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(fecha));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener total recaudado: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * Obtiene el total de comisiones pagadas en una fecha
     */
    public double getTotalComisionesPorFecha(LocalDate fecha) {
        String sql = "SELECT COALESCE(SUM(comision_trabajador), 0) FROM recaudo_diario WHERE fecha = ?";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(fecha));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener total comisiones: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * Obtiene estadísticas de recaudos por semana
     */
    public Object[] getEstadisticasRecaudosSemana(int anio, int semana) {
        String sql = "SELECT " +
                     "COUNT(*) as dias_trabajados, " +
                     "COALESCE(SUM(total_recaudado), 0) as total_recaudado, " +
                     "COALESCE(SUM(comision_trabajador), 0) as total_comisiones, " +
                     "COALESCE(AVG(total_recaudado), 0) as promedio_diario " +
                     "FROM recaudo_diario " +
                     "WHERE EXTRACT(YEAR FROM fecha) = ? AND EXTRACT(WEEK FROM fecha) = ?";
        
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, anio);
            pstmt.setInt(2, semana);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Object[]{
                    rs.getInt("dias_trabajados"),
                    rs.getDouble("total_recaudado"),
                    rs.getDouble("total_comisiones"),
                    rs.getDouble("promedio_diario")
                };
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas de recaudos: " + e.getMessage());
        }
        return new Object[]{0, 0.0, 0.0, 0.0};
    }

    // ============= MÉTODOS ORIGINALES MANTENIDOS =============
    
    public List<Object[]> getDatosReporte(int anio, int semana, int idTrabajador) {
        List<Object[]> datos = new ArrayList<>();
        String sql = "SELECT p.nombre, p.precio_venta, p.precio_compra, " +
                     "SUM(CASE WHEN r.tipo_movimiento = 'LLEVADA' THEN r.cantidad ELSE 0 END) - " +
                     "SUM(CASE WHEN r.tipo_movimiento = 'DEVUELTA' THEN r.cantidad ELSE 0 END) as cantidad_vendida " +
                     "FROM registros r " +
                     "JOIN productos p ON r.id_producto = p.id " +
                     "WHERE r.id_trabajador = ? AND EXTRACT(YEAR FROM r.fecha) = ? AND EXTRACT(WEEK FROM r.fecha) = ? " +
                     "GROUP BY p.id, p.nombre, p.precio_venta, p.precio_compra " +
                     "HAVING SUM(CASE WHEN r.tipo_movimiento = 'LLEVADA' THEN r.cantidad ELSE 0 END) > 0";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idTrabajador);
            pstmt.setInt(2, anio);
            pstmt.setInt(3, semana);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                datos.add(new Object[]{
                    rs.getString("nombre"), 
                    rs.getInt("cantidad_vendida"), 
                    rs.getDouble("precio_venta"), 
                    rs.getDouble("precio_compra")
                });
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
                datos.add(new Object[]{ 
                    rs.getString("nombre"), 
                    rs.getInt("cantidad_vendida"), 
                    rs.getDouble("precio_venta") 
                });
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
    
    /**
     * Obtiene gastos en un período específico
     */
    public List<Object[]> getGastosPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Object[]> gastos = new ArrayList<>();
        String sql = "SELECT fecha, concepto, valor FROM gastos WHERE fecha BETWEEN ? AND ? ORDER BY fecha DESC";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(fechaInicio));
            pstmt.setDate(2, Date.valueOf(fechaFin));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                gastos.add(new Object[]{rs.getDate("fecha"), rs.getString("concepto"), rs.getDouble("valor")});
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener gastos por período: " + e.getMessage());
        }
        return gastos;
    }
}