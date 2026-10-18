package gt.edu.umg.ventas.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PaisTelefonoTest {

    @Test
    void guatemalaAceptaOchoDigitosQueIniciaEn2a7() {
        assertTrue(PaisTelefono.GUATEMALA.esValido("5555-1111"));
        assertTrue(PaisTelefono.GUATEMALA.esValido("22223333"));
        assertFalse(PaisTelefono.GUATEMALA.esValido("1555-1111"));
        assertFalse(PaisTelefono.GUATEMALA.esValido("8555-1111"));
        assertFalse(PaisTelefono.GUATEMALA.esValido("5555111"));
        assertFalse(PaisTelefono.GUATEMALA.esValido("555511112"));
        assertFalse(PaisTelefono.GUATEMALA.esValido(""));
        assertFalse(PaisTelefono.GUATEMALA.esValido(null));
    }

    @Test
    void reglasPorPais() {
        assertTrue(PaisTelefono.HONDURAS.esValido("9876-5432"));
        assertFalse(PaisTelefono.HONDURAS.esValido("5234-5678"));
        assertTrue(PaisTelefono.BELICE.esValido("223-4567"));
        assertFalse(PaisTelefono.BELICE.esValido("5555-1111"));
        assertTrue(PaisTelefono.EL_SALVADOR.esValido("7123-4567"));
        assertFalse(PaisTelefono.EL_SALVADOR.esValido("9123-4567"));
        assertTrue(PaisTelefono.NICARAGUA.esValido("8888-7766"));
        assertFalse(PaisTelefono.NICARAGUA.esValido("6888-7766"));
        assertTrue(PaisTelefono.COSTA_RICA.esValido("8888-1234"));
        assertFalse(PaisTelefono.COSTA_RICA.esValido("1888-1234"));
        assertTrue(PaisTelefono.PANAMA.esValido("212-3456"));
        assertTrue(PaisTelefono.PANAMA.esValido("6123-4567"));
        assertFalse(PaisTelefono.PANAMA.esValido("2123-4567"));
        assertTrue(PaisTelefono.MEXICO.esValido("55 1234 5678"));
        assertFalse(PaisTelefono.MEXICO.esValido("0155 1234 56"));
        assertTrue(PaisTelefono.ESTADOS_UNIDOS.esValido("(212) 555-1234"));
        assertFalse(PaisTelefono.ESTADOS_UNIDOS.esValido("(112) 555-1234"));
    }

    @Test
    void formateaConPrefijoYLoReconoce() {
        assertEquals("+502 5555-1111", PaisTelefono.GUATEMALA.formatear("55551111"));
        assertEquals("+52 551-234-5678", PaisTelefono.MEXICO.formatear("5512345678"));
        assertEquals(PaisTelefono.GUATEMALA, PaisTelefono.detectar("+502 5555-1111"));
        assertEquals(PaisTelefono.ESTADOS_UNIDOS, PaisTelefono.detectar("+1 212-555-1234"));
        assertNull(PaisTelefono.detectar("5555-1111"));
        assertEquals("55551111", PaisTelefono.GUATEMALA.quitarPrefijo("+502 5555-1111"));
    }

    @Test
    void mensajeDeErrorEnEspanol() {
        String error = PaisTelefono.GUATEMALA.validar("123");
        assertNotNull(error);
        assertTrue(error.contains("Guatemala"));
        assertNull(PaisTelefono.GUATEMALA.validar("5555-1111"));
        assertThrows(IllegalArgumentException.class, () -> PaisTelefono.GUATEMALA.validarYFormatear("1234"));
    }

    @Test
    void normalizarUsaPrefijoODefecto() {
        assertEquals("5555-1111", PaisTelefono.normalizar("55551111", null));
        assertEquals("5555-1111", PaisTelefono.normalizar(" +502 5555-1111 ", null));
        assertEquals("+504 9876-5432", PaisTelefono.normalizar("98765432", PaisTelefono.HONDURAS));
        assertEquals("+504 9876-5432", PaisTelefono.normalizar("+504 9876-5432", PaisTelefono.GUATEMALA));
        assertEquals("+52 551-234-5678", PaisTelefono.normalizar("5512345678", PaisTelefono.MEXICO));
        // Idempotente: normalizar dos veces da el mismo resultado
        String una = PaisTelefono.normalizar("98765432", PaisTelefono.HONDURAS);
        assertEquals(una, PaisTelefono.normalizar(una, PaisTelefono.GUATEMALA));
    }

    @Test
    void normalizarRechazaNumerosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> PaisTelefono.normalizar("", null));
        assertThrows(IllegalArgumentException.class, () -> PaisTelefono.normalizar("1234", null));
        assertThrows(IllegalArgumentException.class, () -> PaisTelefono.normalizar("1555-1111", null));
        assertThrows(IllegalArgumentException.class, () -> PaisTelefono.normalizar("98765432", null));
        assertThrows(IllegalArgumentException.class, () -> PaisTelefono.normalizar("+34 612345678", null));
    }
}
