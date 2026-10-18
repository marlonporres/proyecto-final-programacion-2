package gt.edu.umg.ventas.util;

import java.util.regex.Pattern;

/**
 * Reglas de numeración telefónica por país (Centroamérica, México y EE. UU.).
 *
 * El número se valida SIN el código de país: por ejemplo, para Guatemala se
 * escriben los 8 dígitos (5555-1111) y el prefijo +502 se agrega al armar el
 * número completo con {@link #formatear(String)}.
 *
 * Las reglas son de longitud y primer dígito (tipo de línea), no garantizan que
 * el número exista. Si algún plan de numeración cambia, solo hay que ajustar la
 * expresión regular del país correspondiente.
 */
public enum PaisTelefono {

    GUATEMALA("Guatemala", "502", "[2-7]\\d{7}", 8,
            "8 dígitos; debe iniciar con 2, 3, 4, 5, 6 o 7"),
    HONDURAS("Honduras", "504", "[23789]\\d{7}", 8,
            "8 dígitos; debe iniciar con 2 (fijo) o 3, 7, 8, 9 (móvil)"),
    BELICE("Belice", "501", "[2-7]\\d{6}", 7,
            "7 dígitos; debe iniciar con 2 a 7"),
    EL_SALVADOR("El Salvador", "503", "[267]\\d{7}", 8,
            "8 dígitos; debe iniciar con 2 (fijo) o 6, 7 (móvil)"),
    NICARAGUA("Nicaragua", "505", "[2578]\\d{7}", 8,
            "8 dígitos; debe iniciar con 2 (fijo) o 5, 7, 8 (móvil)"),
    COSTA_RICA("Costa Rica", "506", "[2-8]\\d{7}", 8,
            "8 dígitos; debe iniciar con 2 a 8"),
    PANAMA("Panamá", "507", "(?:[2-57-9]\\d{6}|6\\d{7})", 0,
            "7 dígitos (fijo) u 8 dígitos si inicia con 6 (móvil)"),
    MEXICO("México", "52", "[2-9]\\d{9}", 10,
            "10 dígitos (clave de zona + número); no inicia con 0 ni 1"),
    ESTADOS_UNIDOS("Estados Unidos", "1", "[2-9]\\d{2}[2-9]\\d{6}", 10,
            "10 dígitos (código de área + número); el código de área y la central inician con 2 a 9");

    private final String nombre;
    private final String codigo;
    private final Pattern patron;
    private final int longitudFija; // 0 = variable (Panamá)
    private final String regla;

    PaisTelefono(String nombre, String codigo, String regex, int longitudFija, String regla) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.patron = Pattern.compile(regex);
        this.longitudFija = longitudFija;
        this.regla = regla;
    }

    public String getNombre() { return nombre; }

    /** Código de país sin el signo +, por ejemplo "502". */
    public String getCodigo() { return codigo; }

    /** Descripción corta de la regla, para mostrar al usuario. */
    public String getRegla() { return regla; }

    /** Texto para listas desplegables: "Guatemala (+502)". */
    @Override
    public String toString() { return nombre + " (+" + codigo + ")"; }

    /** Deja solo los dígitos (quita espacios, guiones, paréntesis y signos). */
    public static String soloDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    /** Verdadero si el número local (sin código de país) cumple la regla del país. */
    public boolean esValido(String numeroLocal) {
        return patron.matcher(soloDigitos(numeroLocal)).matches();
    }

    /**
     * Devuelve null si el número es válido, o un mensaje de error en español
     * listo para mostrar al usuario.
     */
    public String validar(String numeroLocal) {
        String d = soloDigitos(numeroLocal);
        if (d.isEmpty()) {
            return "Ingrese el número de teléfono.";
        }
        if (!patron.matcher(d).matches()) {
            return "Teléfono inválido para " + nombre + " (+" + codigo + "): " + regla + ".";
        }
        return null;
    }

    /** Número con agrupación pero SIN prefijo, por ejemplo "5555-1111". No valida. */
    public String formatearLocal(String numeroLocal) {
        String d = soloDigitos(numeroLocal);
        switch (d.length()) {
            case 7: return d.substring(0, 3) + "-" + d.substring(3);
            case 8: return d.substring(0, 4) + "-" + d.substring(4);
            case 10: return d.substring(0, 3) + "-" + d.substring(3, 6) + "-" + d.substring(6);
            default: return d;
        }
    }

    /** Número completo con prefijo, por ejemplo "+502 5555-1111". No valida. */
    public String formatear(String numeroLocal) {
        return "+" + codigo + " " + formatearLocal(numeroLocal);
    }

    /** Valida y devuelve el número completo; lanza IllegalArgumentException si no es válido. */
    public String validarYFormatear(String numeroLocal) {
        String error = validar(numeroLocal);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        return formatear(numeroLocal);
    }

    /**
     * Intenta reconocer el país de un número escrito con prefijo ("+502 5555-1111",
     * "00502 55551111"). Devuelve null si no empieza con un código conocido.
     */
    public static PaisTelefono detectar(String numeroInternacional) {
        if (numeroInternacional == null) return null;
        String t = numeroInternacional.trim();
        if (t.startsWith("+")) {
            t = t.substring(1);
        } else if (t.startsWith("00")) {
            t = t.substring(2);
        } else {
            return null;
        }
        String d = soloDigitos(t);
        // Primero los códigos de 3 dígitos (50x), luego 52 y 1, para evitar ambigüedades.
        for (PaisTelefono p : values()) {
            if (p.codigo.length() == 3 && d.startsWith(p.codigo)) return p;
        }
        for (PaisTelefono p : values()) {
            if (p.codigo.length() < 3 && d.startsWith(p.codigo)) return p;
        }
        return null;
    }

    /** Quita el código de país de un número escrito con prefijo; si no tiene, devuelve solo dígitos. */
    public String quitarPrefijo(String numero) {
        String t = numero == null ? "" : numero.trim();
        if (t.startsWith("+")) t = t.substring(1);
        else if (t.startsWith("00")) t = t.substring(2);
        else return soloDigitos(t);
        String d = soloDigitos(t);
        return d.startsWith(codigo) ? d.substring(codigo.length()) : d;
    }

    /**
     * Punto único de validación para guardar teléfonos.
     *
     * - Si el texto trae prefijo (+504 ..., 00504 ...), se usa el país del prefijo.
     * - Si no trae prefijo, se usa {@code porDefecto} (Guatemala si es null).
     * - Se valida largo y primer dígito según el país.
     * - Formato guardado: Guatemala sin prefijo ("5555-1111", como ya se manejaba);
     *   los demás países con prefijo ("+504 9876-5432").
     *
     * Es idempotente: normalizar(normalizar(x)) == normalizar(x).
     *
     * @throws IllegalArgumentException con un mensaje en español listo para mostrar.
     */
    public static String normalizar(String texto, PaisTelefono porDefecto) {
        PaisTelefono base = porDefecto != null ? porDefecto : GUATEMALA;
        String t = texto == null ? "" : texto.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Ingrese el número de teléfono.");
        }
        PaisTelefono pais = detectar(t);
        String local;
        if (pais != null) {
            local = pais.quitarPrefijo(t);
        } else if (t.startsWith("+") || t.startsWith("00")) {
            throw new IllegalArgumentException("El código de país de '" + t + "' no está soportado. "
                    + "Países admitidos: " + listaDePaises() + ".");
        } else {
            pais = base;
            local = soloDigitos(t);
        }
        if (!pais.esValido(local)) {
            throw new IllegalArgumentException("El teléfono '" + t + "' no es válido para "
                    + pais + ": " + pais.regla + ".");
        }
        return pais == GUATEMALA ? pais.formatearLocal(local) : pais.formatear(local);
    }

    /** "Guatemala (+502), Honduras (+504), ..." */
    public static String listaDePaises() {
        StringBuilder sb = new StringBuilder();
        for (PaisTelefono p : values()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(p);
        }
        return sb.toString();
    }
}
