package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.*;
import gt.edu.umg.ventas.modelo.*;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;

final class DatosPrueba {
    OrdenVenta orden = new OrdenVenta();
    Factura guardada;
    boolean confirmado = true, falloGuardar, falloPago, falloAnular;
    Usuario usuario = new Usuario(1, "Admin", "admin", "ADMIN", true);

    DatosPrueba() {
        orden.setId(1); orden.setNumeroOrden("OV-1"); orden.setUsuario(usuario);
        orden.setCliente(new Cliente(1, "CF", "Cliente", "", "", "")); orden.setEstado(EstadoOrdenVenta.COMPLETADA);
        Producto p = new Producto(1, "P1", "Producto", "", new BigDecimal("99.00"), true, new Categoria());
        DetalleOrdenVenta d = new DetalleOrdenVenta(); d.setProducto(p); d.setCantidad(5);
        d.setPrecioUnitario(new BigDecimal("10.00")); d.setSubtotal(new BigDecimal("50.00")); orden.agregarDetalle(d);
    }

    OrdenVentaDAO ordenDAO() {
        return (OrdenVentaDAO) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{OrdenVentaDAO.class}, (p, m, args) -> switch (m.getName()) {
            case "obtener" -> orden;
            case "obtenerTodos" -> List.of(orden);
            case "tieneDespachoConfirmado" -> confirmado;
            case "crear" -> { orden = (OrdenVenta) args[0]; yield null; }
            default -> throw new UnsupportedOperationException(m.getName());
        });
    }

    FacturaDAO facturaDAO() {
        return (FacturaDAO) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{FacturaDAO.class}, (p, m, args) -> switch (m.getName()) {
            case "buscarPorOrden", "buscarPorNumero" -> guardada;
            case "guardar" -> {
                if (falloGuardar) throw new IllegalStateException("BD no disponible");
                guardada = (Factura) args[0]; guardada.setIdFactura(42); yield null;
            }
            case "actualizar" -> { if (falloPago) throw new IllegalStateException("Pago no guardado"); guardada = (Factura) args[0]; yield null; }
            case "anular" -> { if (falloAnular) throw new IllegalStateException("Anulación no guardada"); yield null; }
            default -> throw new UnsupportedOperationException(m.getName());
        });
    }
    FacturaService servicio() { return new FacturaService(facturaDAO(), ordenDAO()); }
}
