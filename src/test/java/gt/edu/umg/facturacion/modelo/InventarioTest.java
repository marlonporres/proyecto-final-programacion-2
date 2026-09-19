package gt.edu.umg.facturacion.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Inventario")
public class InventarioTest {

    private Inventario inventario;

    @BeforeEach
    public void setUp() {
        inventario = new Inventario(1L, new BigDecimal("50.00"), new BigDecimal("5.00"), LocalDateTime.now());
    }

    @Test
    @DisplayName("Debe validar disponibilidad de existencias correctamente")
    public void testHayDisponibilidad() {
        assertTrue(inventario.hayDisponibilidad(new BigDecimal("10.00")));
        assertTrue(inventario.hayDisponibilidad(new BigDecimal("50.00")));
        assertFalse(inventario.hayDisponibilidad(new BigDecimal("50.01")));
        assertFalse(inventario.hayDisponibilidad(new BigDecimal("0.00")));
        assertFalse(inventario.hayDisponibilidad(new BigDecimal("-5.00")));
    }

    @Test
    @DisplayName("Debe descontar existencias sin permitir valores negativos")
    public void testDescontarInventario() {
        inventario.descontar(new BigDecimal("20.00"));
        assertEquals(new BigDecimal("30.00"), inventario.getExistencia());

        // Descontar exactamente el remanente
        inventario.descontar(new BigDecimal("30.00"));
        assertEquals(new BigDecimal("0.00"), inventario.getExistencia());

        // Intentar descontar más de lo disponible debe lanzar IllegalStateException
        assertThrows(IllegalStateException.class, () -> inventario.descontar(new BigDecimal("1.00")));
    }

    @Test
    @DisplayName("Debe reponer existencias correctamente")
    public void testReponerInventario() {
        inventario.reponer(new BigDecimal("25.00"));
        assertEquals(new BigDecimal("75.00"), inventario.getExistencia());
    }

    @Test
    @DisplayName("No debe permitir descontar ni reponer cantidades menores o iguales a cero")
    public void testCantidadesInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> inventario.descontar(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> inventario.descontar(new BigDecimal("-10.00")));
        assertThrows(IllegalArgumentException.class, () -> inventario.reponer(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> inventario.reponer(new BigDecimal("-10.00")));
    }
}
