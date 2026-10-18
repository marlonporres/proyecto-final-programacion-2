package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.dao.ClienteDAO;
import gt.edu.umg.ventas.modelo.Cliente;
//*prueba*/
import gt.edu.umg.ventas.servicio.ClienteService;

import java.util.List;
import java.util.Objects;

/**
 * Controlador de Clientes que comunica la Vista (FrmClientes) con la capa de Servicio (ClienteService).
 */
public class ClienteController {

    // [CAMBIO 1:N TELÉFONOS]: El controlador se comunica con ClienteService siguiendo el flujo Vista -> Controlador -> Servicio -> DAO -> BD.
    private final ClienteService clienteService;

    public ClienteController() {
        this.clienteService = new ClienteService();
    }

    public ClienteController(ClienteDAO clienteDAO) {
        this.clienteService = new ClienteService(Objects.requireNonNull(clienteDAO, "El DAO de cliente es obligatorio."));
    }

    public ClienteController(ClienteService clienteService) {
        this.clienteService = Objects.requireNonNull(clienteService, "El servicio de cliente es obligatorio.");
    }

    public List<Cliente> listar() {
        return clienteService.listar();
    }

    // [CAMBIO 1:N TELÉFONOS]: Recibe el Cliente con su lista de objetos Telefono (número y tipo) desde la Vista y lo envía al Servicio.
    public void guardar(Cliente cliente) {
        clienteService.guardar(cliente);
    }

    // [CAMBIO 1:N TELÉFONOS]: Recibe el Cliente modificado con su lista de objetos Telefono desde la Vista y solicita la actualización al Servicio.
    public void actualizar(Cliente cliente) {
        clienteService.actualizar(cliente);
    }

    public List<Cliente> buscar(String criterio) {
        return clienteService.buscar(criterio);
    }

    public Cliente buscarPorId(long id) {
        return clienteService.buscarPorId(id);
    }
}
