package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC para ClienteDAO con soporte 1:N de teléfonos en dbo.cliente_telefono.
 */
public class ClienteDAOImpl implements ClienteDAO {

    // [CAMBIO 1:N TELÉFONOS]: Bandera estática para verificar/migrar la tabla dbo.cliente_telefono una sola vez al iniciar.
    private static volatile boolean tablaVerificada = false;
    private final ConexionBD conexion;

    public ClienteDAOImpl() {
        this.conexion = new ConexionBD();
    }

    public ClienteDAOImpl(ConexionBD conexion) {
        this.conexion = conexion != null ? conexion : new ConexionBD();
    }

    // [CAMBIO 1:N TELÉFONOS]: Auto-migración en caliente que crea dbo.cliente_telefono si no existe y migra los teléfonos actuales.
    private void asegurarTablaTelefonos(Connection con) throws SQLException {
        if (tablaVerificada) {
            return;
        }
        synchronized (ClienteDAOImpl.class) {
            if (tablaVerificada) {
                return;
            }
            String ddl = "IF OBJECT_ID('dbo.cliente_telefono', 'U') IS NULL "
                    + "BEGIN "
                    + "    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='dbo' AND TABLE_NAME='cliente' AND COLUMN_NAME='id_cliente' AND DATA_TYPE='int') "
                    + "        EXEC('CREATE TABLE dbo.cliente_telefono (id_telefono INT IDENTITY(1,1) PRIMARY KEY, id_cliente INT NOT NULL, telefono NVARCHAR(20) NOT NULL, CONSTRAINT FK_cliente_telefono_cliente FOREIGN KEY (id_cliente) REFERENCES dbo.cliente(id_cliente) ON DELETE CASCADE)'); "
                    + "    ELSE "
                    + "        CREATE TABLE dbo.cliente_telefono ("
                    + "            id_telefono INT IDENTITY(1,1) PRIMARY KEY, "
                    + "            id_cliente BIGINT NOT NULL, "
                    + "            telefono NVARCHAR(20) NOT NULL, "
                    + "            CONSTRAINT FK_cliente_telefono_cliente FOREIGN KEY (id_cliente) "
                    + "                REFERENCES dbo.cliente(id_cliente) ON DELETE CASCADE"
                    + "        ); "
                    + "    INSERT INTO dbo.cliente_telefono (id_cliente, telefono) "
                    + "    SELECT id_cliente, LTRIM(RTRIM(telefono)) FROM dbo.cliente "
                    + "    WHERE telefono IS NOT NULL AND LTRIM(RTRIM(telefono)) <> ''; "
                    + "END";
            try (Statement st = con.createStatement()) {
                st.execute(ddl);
                tablaVerificada = true;
            }
        }
    }

    @Override
    public void guardar(Cliente cliente) {
        String sql = "INSERT INTO dbo.cliente (nit, nombre, direccion, telefono, correo) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = conexion.obtenerConexion()) {
            asegurarTablaTelefonos(con);
            // [CAMBIO 1:N TELÉFONOS]: Se abre transacción para guardar la cabecera del cliente y su lista de teléfonos de forma atómica.
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, cliente.getNit());
                    ps.setString(2, cliente.getNombre());
                    ps.setString(3, cliente.getDireccion());
                    ps.setString(4, cliente.getTelefono());
                    ps.setString(5, cliente.getCorreo());
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            cliente.setId(rs.getLong(1));
                        }
                    }
                }
                // [CAMBIO 1:N TELÉFONOS]: Inserta en lote todos los números de teléfono en dbo.cliente_telefono.
                insertarTelefonos(con, cliente.getId(), cliente.getTelefonos());
                con.commit();
            } catch (Exception e) {
                try { con.rollback(); } catch (SQLException ex) { e.addSuppressed(ex); }
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException("Error al guardar cliente: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Cliente cliente) {
        String sql = "UPDATE dbo.cliente SET nit = ?, nombre = ?, direccion = ?, telefono = ?, correo = ? WHERE id_cliente = ?";
        String sqlDeleteTel = "DELETE FROM dbo.cliente_telefono WHERE id_cliente = ?";
        try (Connection con = conexion.obtenerConexion()) {
            asegurarTablaTelefonos(con);
            // [CAMBIO 1:N TELÉFONOS]: Transacción para actualizar cliente, limpiar sus teléfonos anteriores y reinsertar los nuevos.
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, cliente.getNit());
                    ps.setString(2, cliente.getNombre());
                    ps.setString(3, cliente.getDireccion());
                    ps.setString(4, cliente.getTelefono());
                    ps.setString(5, cliente.getCorreo());
                    ps.setLong(6, cliente.getId());
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalStateException("No existe el cliente con ID " + cliente.getId() + " para actualizar.");
                    }
                }
                // [CAMBIO 1:N TELÉFONOS]: Elimina los teléfonos previos del cliente y registra la nueva lista en lote.
                try (PreparedStatement psDel = con.prepareStatement(sqlDeleteTel)) {
                    psDel.setLong(1, cliente.getId());
                    psDel.executeUpdate();
                }
                insertarTelefonos(con, cliente.getId(), cliente.getTelefonos());
                con.commit();
            } catch (Exception e) {
                try { con.rollback(); } catch (SQLException ex) { e.addSuppressed(ex); }
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException("Error al actualizar cliente: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar cliente: " + e.getMessage(), e);
        }
    }

    // [CAMBIO 1:N TELÉFONOS]: Método auxiliar que inserta en lote (addBatch/executeBatch) los teléfonos en dbo.cliente_telefono.
    private void insertarTelefonos(Connection con, long idCliente, List<String> telefonos) throws SQLException {
        if (telefonos == null || telefonos.isEmpty()) {
            return;
        }
        String sqlTel = "INSERT INTO dbo.cliente_telefono (id_cliente, telefono) VALUES (?, ?)";
        try (PreparedStatement psTel = con.prepareStatement(sqlTel)) {
            boolean tieneAlguno = false;
            for (String tel : telefonos) {
                if (tel != null && !tel.isBlank()) {
                    psTel.setLong(1, idCliente);
                    psTel.setString(2, tel.trim());
                    psTel.addBatch();
                    tieneAlguno = true;
                }
            }
            if (tieneAlguno) {
                psTel.executeBatch();
            }
        }
    }

    @Override
    public Cliente buscarPorId(long idCliente) {
        String sql = "SELECT id_cliente, nit, nombre, direccion, telefono, correo FROM dbo.cliente WHERE id_cliente = ?";
        try (Connection con = conexion.obtenerConexion()) {
            asegurarTablaTelefonos(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapear(con, rs);
                    }
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
        try (Connection con = conexion.obtenerConexion()) {
            asegurarTablaTelefonos(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nit != null ? nit.trim() : "");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapear(con, rs);
                    }
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
        try (Connection con = conexion.obtenerConexion()) {
            asegurarTablaTelefonos(con);
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(con, rs));
                }
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
        try (Connection con = conexion.obtenerConexion()) {
            asegurarTablaTelefonos(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                String patron = "%" + (criterio != null ? criterio.trim() : "") + "%";
                ps.setString(1, patron);
                ps.setString(2, patron);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapear(con, rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar clientes por texto: " + e.getMessage(), e);
        }
        return lista;
    }

    // [CAMBIO 1:N TELÉFONOS]: Mapea los datos del cliente y consulta todos sus teléfonos en dbo.cliente_telefono.
    private Cliente mapear(Connection con, ResultSet rs) throws SQLException {
        long idCliente = rs.getLong("id_cliente");
        String telefonoRespaldo = rs.getString("telefono");
        Cliente cliente = new Cliente(
                idCliente,
                rs.getString("nit"),
                rs.getString("nombre"),
                rs.getString("direccion"),
                telefonoRespaldo,
                rs.getString("correo")
        );

        List<String> telefonos = new ArrayList<>();
        String sqlTel = "SELECT telefono FROM dbo.cliente_telefono WHERE id_cliente = ? ORDER BY id_telefono";
        try (PreparedStatement psTel = con.prepareStatement(sqlTel)) {
            psTel.setLong(1, idCliente);
            try (ResultSet rsTel = psTel.executeQuery()) {
                while (rsTel.next()) {
                    String t = rsTel.getString("telefono");
                    if (t != null && !t.isBlank()) {
                        telefonos.add(t.trim());
                    }
                }
            }
        }
        // Si existen teléfonos en la tabla hija se asignan; de lo contrario se usa la columna telefono como respaldo.
        if (!telefonos.isEmpty()) {
            cliente.setTelefonos(telefonos);
        } else if (telefonoRespaldo != null && !telefonoRespaldo.isBlank()) {
            cliente.setTelefonos(List.of(telefonoRespaldo.trim()));
        } else {
            cliente.setTelefonos(new ArrayList<>());
        }
        return cliente;
    }
}
