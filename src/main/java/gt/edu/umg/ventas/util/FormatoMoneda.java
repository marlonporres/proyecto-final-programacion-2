package gt.edu.umg.ventas.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Clase de utilidades para formateo y parseo de valores monetarios en Quetzales (Q).
 */
public final class FormatoMoneda {

    private static final DecimalFormat FORMATO_QUETZAL;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("es", "GT"));
        symbols.setCurrencySymbol("Q");
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        FORMATO_QUETZAL = new DecimalFormat("Q #,##0.00", symbols);
    }

    private FormatoMoneda() {
    }

    /**
     * Formatea un valor BigDecimal a formato estándar de Quetzales.
     *
     * @param monto Monto a formatear
     * @return Cadena formateada (ej: "Q 1,250.50")
     */
    public static synchronized String formatear(BigDecimal monto) {
        if (monto == null) {
            return "Q 0.00";
        }
        return FORMATO_QUETZAL.format(monto.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Convierte una cadena de texto a BigDecimal de forma segura.
     *
     * @param texto Cadena con el monto numérico o con formato
     * @return BigDecimal correspondiente
     * @throws NumberFormatException si el texto no es un número válido
     */
    public static BigDecimal parsear(String texto) {
        if (texto == null || texto.isBlank()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        String limpio = texto.replace("Q", "")
                             .replace("$", "")
                             .replace(",", "")
                             .trim();
        return new BigDecimal(limpio).setScale(2, RoundingMode.HALF_UP);
    }
}
