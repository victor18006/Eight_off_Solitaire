public class ListaSimple<T extends Comparable<T>> {
    private NodoSimple<T> inicio;
    private NodoSimple<T> fin;
    private int tamaño;

    public ListaSimple() {
        inicio = null;
        fin = null;
        tamaño = 0;
    }

    public boolean estaVacia() {
        return inicio == null;
    }

    public int getTamaño() {
        return tamaño;
    }

    public void insertarInicio(T dato) {
        NodoSimple<T> nuevo = new NodoSimple<>(dato);
        if (estaVacia()) {
            inicio = nuevo;
            fin = nuevo;
        } else {
            nuevo.setSig(inicio);
            inicio = nuevo;
        }
        tamaño++;
    }

    public void insertarFin(T dato) {
        NodoSimple<T> nuevo = new NodoSimple<>(dato);
        if (estaVacia()) {
            inicio = nuevo;
            fin = nuevo;
        } else {
            fin.setSig(nuevo);
            fin = nuevo;
        }
        tamaño++;
    }

    public T eliminarInicio() {
        if (estaVacia()) {
            return null;
        }
        T dato = inicio.getDato();
        if (inicio == fin) {
            inicio = null;
            fin = null;
        } else {
            inicio = inicio.getSig();
        }
        tamaño--;
        return dato;
    }

    public T eliminarFin() {
        if (estaVacia()) {
            return null;
        }
        T dato = fin.getDato();
        if (inicio == fin) {
            inicio = null;
            fin = null;
        } else {
            NodoSimple<T> actual = inicio;
            while (actual.getSig() != fin) {
                actual = actual.getSig();
            }
            actual.setSig(null);
            fin = actual;
        }
        tamaño--;
        return dato;
    }

    public T obtenerInicio() {
        return estaVacia() ? null : inicio.getDato();
    }

    public T obtenerFin() {
        return estaVacia() ? null : fin.getDato();
    }

    public NodoSimple<T> getInicio() {
        return inicio;
    }

    public NodoSimple<T> getFin() {
        return fin;
    }

    public String mostrarAdelante() {
        if (estaVacia()) return "Lista vacía";
        StringBuilder sb = new StringBuilder();
        NodoSimple<T> actual = inicio;
        while (actual != null) {
            sb.append(actual.getDato());
            if (actual.getSig() != null) {
                sb.append(" -> ");
            }
            actual = actual.getSig();
        }
        return sb.toString();
    }

    public boolean contiene(T dato) {
        if (estaVacia()) return false;
        NodoSimple<T> actual = inicio;
        while (actual != null) {
            if (actual.getDato().equals(dato)) {
                return true;
            }
            actual = actual.getSig();
        }
        return false;
    }

    public void limpiar() {
        inicio = null;
        fin = null;
        tamaño = 0;
    }

    // Método para eliminar por posición (necesario para el juego)
    public T eliminaPosicion(int posicion) {
        if (estaVacia() || posicion < 0 || posicion >= tamaño) return null;
        if (posicion == 0) {
            return eliminarInicio();
        }
        if (posicion == tamaño - 1) {
            return eliminarFin();
        }

        NodoSimple<T> actual = inicio;
        NodoSimple<T> anterior = null;
        for (int i = 0; i < posicion; i++) {
            anterior = actual;
            actual = actual.getSig();
        }

        anterior.setSig(actual.getSig());
        if (actual == fin) {
            fin = anterior;
        }
        tamaño--;
        return actual.getDato();
    }

    // Método para buscar elemento
    public int buscar(T x) {
        if (estaVacia()) return -1;
        int pos = 0;
        NodoSimple<T> actual = inicio;
        while (actual != null) {
            if (actual.getDato().equals(x)) {
                return pos;
            }
            pos++;
            actual = actual.getSig();
        }
        return -1;
    }
}