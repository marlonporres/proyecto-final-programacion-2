package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.dao.ClienteDAO;
import gt.edu.umg.ventas.dao.ClienteDAOImpl;
import gt.edu.umg.ventas.modelo.Cliente;
//*prueba*/

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ClienteController {

    private final ClienteDAO clienteDAO;

    public ClienteController() {
        this(new ClienteDAOImpl());
    }

    public ClienteController(ClienteDAO clienteDAO) {
        this.clienteDAO = Objects.requireNonNull(clienteDAO, "El DAO de cliente es obligatorio.");
    }

    public List<Cliente> listar() {
        return clienteDAO.listar();
    }

    public void guardar(Cliente cliente) {
        validarCliente(cliente, false);
        clienteDAO.guardar(cliente);
    }

    public void actualizar(Cliente cliente) {
        validarCliente(cliente, true);
        clienteDAO.actualizar(cliente);
    }

    public List<Cliente> buscar(String criterio) {
        return clienteDAO.buscarPorTexto(criterio);
    }

    public Cliente buscarPorId(long id) {
        return clienteDAO.buscarPorId(id);
    }

    private void validarCliente(Cliente cliente, boolean esEdicion) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        if (esEdicion && cliente.getId() <= 0) {
            throw new IllegalArgumentException("Cliente inválido para actualizar.");
        }
        if (cliente.getNit() == null || cliente.getNit().isBlank()) {
            throw new IllegalArgumentException("El NIT del cliente es obligatorio.");
        }
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }

        String nit = cliente.getNit().trim();
        String nombre = cliente.getNombre().trim();
        String direccion = cliente.getDireccion() != null ? cliente.getDireccion().trim() : null;
        String correo = cliente.getCorreo() != null ? cliente.getCorreo().trim() : null;

        if (nit.length() > 20) {
            throw new IllegalArgumentException("El NIT no puede exceder 20 caracteres.");
        }
        if (nombre.length() > 150) {
            throw new IllegalArgumentException("El nombre no puede exceder 150 caracteres.");
        }
        if (direccion != null && direccion.length() > 255) {
            throw new IllegalArgumentException("La dirección no puede exceder 255 caracteres.");
        }
        if (correo != null && correo.length() > 100) {
            throw new IllegalArgumentException("El correo no puede exceder 100 caracteres.");
        }

        // [CAMBIO 1:N TELÉFONOS]: Limpia espacios, descarta vacíos/duplicados con LinkedHashSet y valida longitud <= 20 por cada teléfono.
        Set<String> telefonosUnicos = new LinkedHashSet<>();
        List<String> origenTelefonos = cliente.getTelefonos();
        if (origenTelefonos != null) {
            for (String tel : origenTelefonos) {
                if (tel != null && !tel.isBlank()) {
                    String limpio = tel.trim();
                    if (limpio.length() > 20) {
                        throw new IllegalArgumentException("El teléfono '" + limpio + "' no puede exceder 20 caracteres.");
                    }
                    telefonosUnicos.add(limpio);
                }
            }
        }

        cliente.setNit(nit);
        cliente.setNombre(nombre);
        cliente.setDireccion(direccion);
        cliente.setCorreo(correo);
        // [CAMBIO 1:N TELÉFONOS]: Asigna la lista depurada de teléfonos al modelo antes de enviarlo al DAO.
        cliente.setTelefonos(new ArrayList<>(telefonosUnicos));

        Cliente existente = clienteDAO.buscarPorNit(nit);
        if (existente != null) {
            if (cliente.getId() <= 0 || existente.getId() != cliente.getId()) {
                throw new IllegalArgumentException("Ya existe un cliente registrado con el NIT " + nit + ".");
            }
        }
    }
}
