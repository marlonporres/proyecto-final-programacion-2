package gt.edu.umg.ventas.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {
    private static final Properties properties = new Properties();
    
    static {
        try (InputStream input = ConexionBD.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                System.err.println("No se pudo encontrar database.properties. Usando valores por defecto o fallara la conexion.");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Connection obtenerConexion() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");
        
        if (url == null || user == null || password == null) {
            throw new SQLException("Falta configuracion en database.properties. Revise db.url, db.user, db.password");
        }
        return DriverManager.getConnection(url, user, password);
    }
}

