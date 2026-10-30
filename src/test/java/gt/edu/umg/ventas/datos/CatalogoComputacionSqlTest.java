package gt.edu.umg.ventas.datos;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

class CatalogoComputacionSqlTest {
    @Test void instaladorContieneLos24ProductosDeComputacion() throws Exception {
        String instalador = Files.readString(Path.of("script_base_datos_ventas.sql"));
        String catalogo = bloque(instalador);
        assertEquals(24, Pattern.compile("(?m)^\\('(?:TEC|PER|EQP|MON|CMP|RED|ACC)-\\d{3}',").matcher(catalogo).results().count());
        assertFalse(catalogo.contains("DELETE "));
        assertFalse(catalogo.contains("DROP "));
    }

    private String bloque(String sql) {
        String texto = sql.replace("\r\n", "\n");
        String inicio = "-- INICIO CATALOGO COMPUTACION";
        String fin = "-- FIN CATALOGO COMPUTACION";
        int desde = texto.indexOf(inicio), hasta = texto.indexOf(fin);
        assertTrue(desde >= 0 && hasta > desde, "Faltan delimitadores del catálogo.");
        return texto.substring(desde, hasta + fin.length());
    }
}
