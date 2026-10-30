package gt.edu.umg.ventas.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConexionBDTest {
    @AfterEach
    void restaurarOrigenDelEscritorio() {
        ConexionBD.configurarDataSource(null);
    }

    @Test
    void usaElDataSourceConfiguradoSinAbrirOtraConexion() throws Exception {
        AtomicInteger solicitudes = new AtomicInteger();
        Connection conexion = (Connection) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{Connection.class},
                (proxy, metodo, args) -> { throw new UnsupportedOperationException(metodo.getName()); });
        DataSource origen = (DataSource) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{DataSource.class},
                (proxy, metodo, args) -> {
                    if (metodo.getName().equals("getConnection")) {
                        solicitudes.incrementAndGet();
                        return conexion;
                    }
                    throw new UnsupportedOperationException(metodo.getName());
                });
        ConexionBD.configurarDataSource(origen);

        assertSame(conexion, ConexionBD.obtenerConexion());
        assertEquals(1, solicitudes.get());
    }

    @Test
    void propagaErrorDelPoolSinUsarCredencialesDelEscritorio() {
        SQLException error = new SQLException("Pool no disponible", "08001");
        DataSource origen = (DataSource) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{DataSource.class},
                (proxy, metodo, args) -> { throw error; });
        ConexionBD.configurarDataSource(origen);

        assertSame(error, assertThrows(SQLException.class, ConexionBD::obtenerConexion));
    }
}
