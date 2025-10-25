public class NodoDoble<T> {
    private T dato;
    private NodoDoble<T> sig;
    private NodoDoble<T> ant;

    public NodoDoble() {
        this.dato = null;
        this.sig = null;
        this.ant = null;
    }

    public NodoDoble(T dato) {
        this.dato = dato;
        this.sig = null;
        this.ant = null;
    }

    public T getDato() {
        return dato;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public NodoDoble<T> getSig() {
        return sig;
    }

    public void setSig(NodoDoble<T> sig) {
        this.sig = sig;
    }

    public NodoDoble<T> getAnt(){
        return ant;
    }

    public void setAnt(NodoDoble<T> ant){
        this.ant = ant;
    }

    @Override
    public String toString() {
        return dato != null ? dato.toString() : "null";
    }
}