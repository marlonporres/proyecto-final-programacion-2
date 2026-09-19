package gt.edu.umg.facturacion.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase encargada de centralizar la conexión JDBC con Microsoft SQL Server.
 * Basada en la implementación probada del repositorio origen (localhost / 127.0.0.1:1433,
 * base de datos 'Estudiantes', usuario 'sa', contraseña '12345').
 * 
 * Permite además sobreescritura dinámica mediante variables de entorno o propiedades
 * del sistema sin modificar el código fuente.
 */
public class ConexionBD {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "1433";
    private static final String DEFAULT_DB = "Estudiantes"; // Base de datos del repositorio origen
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
     * Obtiene una conexión activa a la base de datos SQL Server.
     * Implementación de instancia según el diagrama UML.
     *
     * @return Conexión activa a SQL Server
     * @throws SQLException si ocurre un error al conectar o el driver no está disponible
     */
    public Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(this.url, this.user, this.password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Error: No se encontró el driver JDBC de SQL Server: " + e.getMessage(), e);
        }
    }

    /**
     * Método estático auxiliar compatible con la implementación del repositorio origen.
     *
     * @return Conexión activa a SQL Server
     * @throws SQLException si ocurre un error de conexión
     */
    public static Connection getConexion() throws SQLException {
        return new ConexionBD().obtenerConexion();
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    private static String getEnvOrProp(String key, String defaultValue) {
        String val = System.getenv(key);
        if (val == null || val.isBlank()) {
            val = System.getProperty(key);
        }
        return (val != null && !val.isBlank()) ? val.trim() : defaultValue;
    }

    /**
     * Método de prueba rápida de conexión ejecutable directamente desde NetBeans.
     */
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE CONEXIÓN A SQL SERVER ===");
        ConexionBD conexion = new ConexionBD();
        System.out.println("URL: " + conexion.getUrl());
        System.out.println("Usuario: " + conexion.getUser());
        
        try (Connection con = conexion.obtenerConexion()) {
            if (con != null && !con.isClosed()) {
                System.out.println("✅ ¡CONEXIÓN EXITOSA A SQL SERVER!");
                System.out.println("Catálogo / Base de datos: " + con.getCatalog());
            }
        } catch (SQLException e) {
            System.err.println("❌ Error de conexión con SQL Server:");
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("\nNOTA IMPORTANTE: Asegúrate de que el servicio 'SQL Server (MSSQLSERVER)'");
            System.err.println("esté INICIADO en Windows Services y que el protocolo TCP/IP esté habilitado en el puerto 1433.");
        }
        System.out.println("=== FIN DE LA PRUEBA ===");
    }
}
