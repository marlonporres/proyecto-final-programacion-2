package gt.edu.umg.facturacion.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase encargada de centralizar la conexión JDBC con Microsoft SQL Server.
 * Permite configuración mediante variables de entorno o propiedades del sistema,
 * evitando credenciales sensibles en el código fuente.
 */
public class ConexionBD {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_PORT = "1433";
    private static final String DEFAULT_DB = "FacturacionDB";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASS = "12345";

    private final String url;
    private final String user;
    private final String password;

    public ConexionBD() {
        String host = getEnvOrProp("DB_HOST", DEFAULT_HOST);
        String port = getEnvOrProp("DB_PORT", DEFAULT_PORT);
        String dbName = getEnvOrProp("DB_NAME", DEFAULT_DB);

        this.url = "jdbc:sqlserver://" + host + ":" + port + ";"
                + "databaseName=" + dbName + ";"
                + "encrypt=true;"
                + "trustServerCertificate=true;";
        this.user = getEnvOrProp("DB_USER", DEFAULT_USER);
        this.password = getEnvOrProp("DB_PASSWORD", DEFAULT_PASS);
    }

    public ConexionBD(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Obtiene una nueva conexión a la base de datos SQL Server.
     * El consumidor debe cerrar la conexión utilizando try-with-resources.
     *
     * @return Conexión activa a SQL Server
     * @throws SQLException si ocurre un error de conexión o el driver no está disponible
     */
    public Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(this.url, this.user, this.password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver JDBC de SQL Server en el classpath: " + e.getMessage(), e);
        }
    }

    public String getUrl() {
        return url;
    }

    private static String getEnvOrProp(String key, String defaultValue) {
        String val = System.getenv(key);
        if (val == null || val.isBlank()) {
            val = System.getProperty(key);
        }
        return (val != null && !val.isBlank()) ? val.trim() : defaultValue;
    }
}
