public class NodoSimple<T> {
    private T dato;
    private NodoSimple<T> sig;

    public NodoSimple() {
        this.dato = null;
        this.sig = null;
    }

    public NodoSimple(T dato) {
        this.dato = dato;
        this.sig = null;
    }

    public T getDato() {
        return dato;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public NodoSimple<T> getSig() {
        return sig;
    }

    public void setSig(NodoSimple<T> sig) {
        this.sig = sig;
    }

    @Override
    public String toString() {
        return dato != null ? dato.toString() : "null";
    }
}