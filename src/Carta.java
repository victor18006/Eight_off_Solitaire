public class Carta implements Comparable<Carta> {
    public enum Palo {
        CORAZONES, DIAMANTES, TREBOLES, PICAS
    }

    private int valor;
    private Palo palo;

    public Carta(int valor, Palo palo) {
        if (valor < 1 || valor > 13) {
            throw new IllegalArgumentException("Valor de carta inválido: " + valor);
        }
        this.valor = valor;
        this.palo = palo;
    }

    public int getValor() {
        return valor;
    }

    public Palo getPalo() {
        return palo;
    }

    public boolean isRoja() {
        return palo == Palo.CORAZONES || palo == Palo.DIAMANTES;
    }

    public boolean esMismoColor(Carta otra) {
        if (this.palo == Palo.CORAZONES || this.palo == Palo.DIAMANTES) {
            return otra.palo == Palo.CORAZONES || otra.palo == Palo.DIAMANTES;
        } else {
            return otra.palo == Palo.TREBOLES || otra.palo == Palo.PICAS;
        }
    }

    @Override
    public int compareTo(Carta otra) {
        if (this.palo == otra.palo) {
            return Integer.compare(this.valor, otra.valor);
        }
        return this.palo.compareTo(otra.palo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Carta carta = (Carta) obj;
        return valor == carta.valor && palo == carta.palo;
    }

    @Override
    public int hashCode() {
        return 31 * valor + palo.hashCode();
    }

    @Override
    public String toString() {
        String valorStr;
        switch (valor) {
            case 1: valorStr = "A"; break;
            case 11: valorStr = "J"; break;
            case 12: valorStr = "Q"; break;
            case 13: valorStr = "K"; break;
            default: valorStr = String.valueOf(valor);
        }
        return valorStr + " de " + palo.toString();
    }
}