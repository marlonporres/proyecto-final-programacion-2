package gt.edu.umg.facturacion.dao;

import gt.edu.umg.facturacion.modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC para ClienteDAO.
 */
public class ClienteDAOImpl implements ClienteDAO {

    private final ConexionBD conexion;

    public ClienteDAOImpl() {
        this.conexion = new ConexionBD();
    }

    public ClienteDAOImpl(ConexionBD conexion) {
        this.conexion = conexion != null ? conexion : new ConexionBD();
    }

    @Override
    public void guardar(Cliente cliente) {
        String sql = "INSERT INTO dbo.cliente (nit, nombre, direccion, telefono, correo) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNit());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getDireccion());
            ps.setString(4, cliente.getTelefono());
            ps.setString(5, cliente.getCorreo());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setIdCliente(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public Cliente buscarPorId(long idCliente) {
        String sql = "SELECT id_cliente, nit, nombre, direccion, telefono, correo FROM dbo.cliente WHERE id_cliente = ?";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente por ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Cliente buscarPorNit(String nit) {
        String sql = "SELECT id_cliente, nit, nombre, direccion, telefono, correo FROM dbo.cliente WHERE nit = ?";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nit != null ? nit.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente por NIT: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id_cliente, nit, nombre, direccion, telefono, correo FROM dbo.cliente ORDER BY nombre ASC";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearCliente(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar clientes: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public List<Cliente> buscarPorTexto(String criterio) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id_cliente, nit, nombre, direccion, telefono, correo FROM dbo.cliente "
                + "WHERE nit LIKE ? OR nombre LIKE ? ORDER BY nombre ASC";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + (criterio != null ? criterio.trim() : "") + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCliente(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar clientes por texto: " + e.getMessage(), e);
        }
        return lista;
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getLong("id_cliente"),
                rs.getString("nit"),
                rs.getString("nombre"),
                rs.getString("direccion"),
                rs.getString("telefono"),
                rs.getString("correo")
        );
    }
}
