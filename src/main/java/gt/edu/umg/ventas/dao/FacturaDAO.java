package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.Factura;
import java.util.List;

/**
 * Interfaz de acceso a datos para la entidad Factura.
 */
public interface FacturaDAO {

    /**
     * Persiste una nueva factura junto con sus detalles y pagos iniciales.
     *
     * @param factura Factura a guardar
     */
    void guardar(Factura factura);

    /**
     * Busca una factura por su número único de identificación.
     *
     * @param numero Número de factura
     * @return Factura encontrada o null si no existe
     */
    Factura buscarPorNumero(String numero);

    /**
     * Lista todas las facturas registradas.
     *
     * @return Lista de facturas
     */
    List<Factura> listar();

    /**
     * Actualiza el estado, observaciones o detalles de una factura existente.
     *
     * @param factura Factura con los datos modificados
     */
    void actualizar(Factura factura);

    /**
     * Marca una factura como ANULADA por su identificador.
     *
     * @param id Identificador de la factura
     */
    void anular(long id);
}
