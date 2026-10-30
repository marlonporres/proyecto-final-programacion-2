package gt.edu.umg.ventas.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;

public class ConexionBD {
    private static volatile DataSource dataSource;
    private static volatile Properties properties;

    /** La API aporta su pool; el escritorio conserva database.properties. */
    public static void configurarDataSource(DataSource origen) {
        dataSource = origen;
    }

    public static Connection obtenerConexion() throws SQLException {
        DataSource origen = dataSource;
        if (origen != null) {
            return origen.getConnection();
        }

        Properties configuracion = configuracionEscritorio();
        String url = configuracion.getProperty("db.url");
        String user = configuracion.getProperty("db.user");
        String password = configuracion.getProperty("db.password");
        
        if (url == null || user == null || password == null) {
            throw new SQLException("Falta configuracion en database.properties. Revise db.url, db.user, db.password");
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static Properties configuracionEscritorio() throws SQLException {
        Properties configuracion = properties;
        if (configuracion != null) {
            return configuracion;
        }
        synchronized (ConexionBD.class) {
            if (properties == null) {
                Properties cargadas = new Properties();
                try (InputStream input = ConexionBD.class.getClassLoader().getResourceAsStream("database.properties")) {
                    if (input == null) {
                        throw new SQLException("No se encontró database.properties en el classpath del escritorio.");
                    }
                    cargadas.load(input);
                } catch (IOException ex) {
                    throw new SQLException("No se pudo leer la configuración de conexión del escritorio.", ex);
                }
                properties = cargadas;
            }
            return properties;
        }
    }
}

