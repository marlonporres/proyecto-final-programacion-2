package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.ClienteDAO;
import gt.edu.umg.ventas.dao.ClienteDAOImpl;
import gt.edu.umg.ventas.modelo.Cliente;
import gt.edu.umg.ventas.modelo.Telefono;
import gt.edu.umg.ventas.util.PaisTelefono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * // [CAMBIO 1:N TELÉFONOS]: Capa de Servicio que administra la lógica de negocio de Cliente y su colección 1:N de Teléfonos.
 * Cumple el flujo oficial: Vista -> Controlador -> Servicio -> DAO -> Base de Datos.
 */
public class ClienteService {

    private final ClienteDAO clienteDAO;

    public ClienteService() {
        this(new ClienteDAOImpl());
    }

    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = Objects.requireNonNull(clienteDAO, "El DAO de cliente es obligatorio.");
    }

    public List<Cliente> listar() {
        return clienteDAO.listar();
    }

    public void guardar(Cliente cliente) {
        validarYNormalizarCliente(cliente, false);
        clienteDAO.guardar(cliente);
    }

    public void actualizar(Cliente cliente) {
        validarYNormalizarCliente(cliente, true);
        clienteDAO.actualizar(cliente);
    }

    public List<Cliente> buscar(String criterio) {
        return clienteDAO.buscarPorTexto(criterio);
    }

    public Cliente buscarPorId(long id) {
        return clienteDAO.buscarPorId(id);
    }

    // [CAMBIO 1:N TELÉFONOS]: Valida los datos del cliente y depura su colección de teléfonos (número y tipo: Personal, Trabajo, Casa, Otro).
    private void validarYNormalizarCliente(Cliente cliente, boolean esEdicion) {
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

        // [CAMBIO 1:N TELÉFONOS]: Depura la colección de objetos Telefono evitando números repetidos y validando número y tipo.
        Map<String, Telefono> telefonosDepurados = new LinkedHashMap<>();
        List<Telefono> origen = cliente.getListaTelefonos();
        if (origen != null) {
            for (Telefono tel : origen) {
                if (tel != null && tel.getNumero() != null && !tel.getNumero().isBlank()) {
                    String numLimpio = tel.getNumero().trim();
                    if (numLimpio.length() > 20) {
                        throw new IllegalArgumentException("El teléfono '" + numLimpio + "' no puede exceder 20 caracteres.");
                    }
                    // [VALIDACIÓN POR PAÍS]: Valida largo y primer dígito según el país (sin prefijo = Guatemala, 8 dígitos)
                    // y guarda Guatemala como "5555-1111" y los demás países con prefijo, por ejemplo "+504 9876-5432".
                    String numNormalizado = PaisTelefono.normalizar(numLimpio, PaisTelefono.GUATEMALA);
                    String tipoNormalizado = Telefono.normalizarTipo(tel.getTipo());
                    telefonosDepurados.put(numNormalizado, new Telefono(tel.getIdTelefono(), cliente.getId(), numNormalizado, tipoNormalizado));
                }
            }
        }

        cliente.setNit(nit);
        cliente.setNombre(nombre);
        cliente.setDireccion(direccion);
        cliente.setCorreo(correo);
        cliente.setListaTelefonos(new ArrayList<>(telefonosDepurados.values()));

        Cliente existente = clienteDAO.buscarPorNit(nit);
        if (existente != null) {
            if (cliente.getId() <= 0 || existente.getId() != cliente.getId()) {
                throw new IllegalArgumentException("Ya existe un cliente registrado con el NIT " + nit + ".");
            }
        }
    }
}
