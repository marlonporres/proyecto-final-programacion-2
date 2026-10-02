package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.dao.ClienteDAO;
import gt.edu.umg.ventas.dao.ClienteDAOImpl;
import gt.edu.umg.ventas.modelo.Cliente;

import java.util.List;

public class ClienteController {

    private final ClienteDAO clienteDAO;

    public ClienteController() {
        this.clienteDAO = new ClienteDAOImpl();
    }

    public List<Cliente> listar() {
        return clienteDAO.listar();
    }

    public void guardar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        if (cliente.getNit() == null || cliente.getNit().isBlank()) {
            throw new IllegalArgumentException("El NIT del cliente es obligatorio.");
        }
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
        clienteDAO.guardar(cliente);
    }

    public void actualizar(Cliente cliente) {
        if (cliente == null || cliente.getIdCliente() <= 0) {
            throw new IllegalArgumentException("Cliente inválido para actualizar.");
        }
        clienteDAO.actualizar(cliente);
    }

    public List<Cliente> buscar(String criterio) {
        return clienteDAO.buscarPorTexto(criterio);
    }

    public Cliente buscarPorId(long id) {
        return clienteDAO.buscarPorId(id);
    }
}
