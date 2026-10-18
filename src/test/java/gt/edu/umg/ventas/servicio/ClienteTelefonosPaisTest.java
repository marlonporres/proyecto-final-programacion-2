package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.ClienteDAO;
import gt.edu.umg.ventas.modelo.Cliente;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ClienteTelefonosPaisTest {

    private final AtomicReference<Cliente> guardado = new AtomicReference<>();

    private ClienteService servicio() {
        ClienteDAO dao = (ClienteDAO) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ClienteDAO.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "buscarPorNit" -> null;
                    case "guardar", "actualizar" -> {
                        guardado.set((Cliente) args[0]);
                        yield null;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        return new ClienteService(dao);
    }

    private Cliente cliente() {
        return new Cliente(0L, "123456-7", "Empresa S.A.", "Zona 1", "", "info@empresa.com");
    }

    @Test
    void guatemalaSinPrefijoSeGuardaConOchoDigitos() {
        Cliente c = cliente();
        c.agregarTelefono("55551111", "Personal");
        servicio().guardar(c);
        assertEquals(List.of("5555-1111"), guardado.get().getTelefonos());
    }

    @Test
    void otrosPaisesSeGuardanConPrefijo() {
        Cliente c = cliente();
        c.agregarTelefono("+504 98765432", "Trabajo");
        c.agregarTelefono("+52 5512345678", "Otro");
        c.agregarTelefono("+1 (212) 555-1234", "Casa");
        servicio().guardar(c);
        assertEquals(List.of("+504 9876-5432", "+52 551-234-5678", "+1 212-555-1234"),
                guardado.get().getTelefonos());
    }

    @Test
    void numerosEquivalentesNoSeDuplican() {
        Cliente c = cliente();
        c.agregarTelefono("55551111", "Personal");
        c.agregarTelefono("+502 5555-1111", "Trabajo");
        servicio().guardar(c);
        assertEquals(1, guardado.get().getTelefonos().size());
    }

    @Test
    void rechazaTelefonosInvalidosPorPais() {
        ClienteService s = servicio();
        Cliente corto = cliente();
        corto.agregarTelefono("5555111", "Personal");
        assertThrows(IllegalArgumentException.class, () -> s.guardar(corto));

        Cliente inicioInvalido = cliente();
        inicioInvalido.agregarTelefono("1555-1111", "Personal");
        assertThrows(IllegalArgumentException.class, () -> s.guardar(inicioInvalido));

        Cliente honduras = cliente();
        honduras.agregarTelefono("+504 5234-5678", "Personal");
        assertThrows(IllegalArgumentException.class, () -> s.guardar(honduras));

        Cliente paisNoSoportado = cliente();
        paisNoSoportado.agregarTelefono("+34 612345678", "Personal");
        assertThrows(IllegalArgumentException.class, () -> s.guardar(paisNoSoportado));
    }
}
