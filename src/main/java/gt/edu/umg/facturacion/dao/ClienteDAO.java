package gt.edu.umg.facturacion.dao;

import gt.edu.umg.facturacion.modelo.Cliente;
import java.util.List;

/**
 * Interfaz de acceso a datos para la entidad Cliente.
 */
public interface ClienteDAO {

    void guardar(Cliente cliente);

    Cliente buscarPorId(long idCliente);

    Cliente buscarPorNit(String nit);

    List<Cliente> listar();

    List<Cliente> buscarPorTexto(String criterio);
}
