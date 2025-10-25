public class ListaDobleCircular<T extends Comparable<T>> {
    private NodoDoble<T> inicio;
    private NodoDoble<T> fin;
    private int tamaño;

    public ListaDobleCircular() {
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
        NodoDoble<T> nuevo = new NodoDoble<>(dato);
        if (estaVacia()) {
            inicio = nuevo;
            fin = nuevo;
            inicio.setSig(inicio);
            inicio.setAnt(inicio);
        } else {
            nuevo.setSig(inicio);
            nuevo.setAnt(fin);
            inicio.setAnt(nuevo);
            fin.setSig(nuevo);
            inicio = nuevo;
        }
        tamaño++;
    }

    public void insertarFin(T dato) {
        NodoDoble<T> nuevo = new NodoDoble<>(dato);
        if (estaVacia()) {
            inicio = nuevo;
            fin = nuevo;
            inicio.setSig(inicio);
            inicio.setAnt(inicio);
        } else {
            nuevo.setSig(inicio);
            nuevo.setAnt(fin);
            fin.setSig(nuevo);
            inicio.setAnt(nuevo);
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
            inicio.setAnt(fin);
            fin.setSig(inicio);
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
            fin = fin.getAnt();
            fin.setSig(inicio);
            inicio.setAnt(fin);
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

    public NodoDoble<T> getInicio() {
        return inicio;
    }

    public NodoDoble<T> getFin() {
        return fin;
    }

    public String mostrarAdelante() {
        if (estaVacia()) return "Lista vacía";
        StringBuilder sb = new StringBuilder();
        NodoDoble<T> actual = inicio;
        int count = 0;
        do {
            sb.append(actual.getDato());
            if (actual.getSig() != inicio) {
                sb.append(" <-> ");
            }
            actual = actual.getSig();
            count++;
            if (count > tamaño) break; // Prevención de bucles infinitos
        } while (actual != inicio);
        return sb.toString();
    }

    public String mostrarAtras() {
        if (estaVacia()) return "Lista vacía";
        StringBuilder sb = new StringBuilder();
        NodoDoble<T> actual = fin;
        int count = 0;
        do {
            sb.append(actual.getDato());
            if (actual.getAnt() != fin) {
                sb.append(" <-> ");
            }
            actual = actual.getAnt();
            count++;
            if (count > tamaño) break;
        } while (actual != fin);
        return sb.toString();
    }

    public boolean contiene(T dato) {
        if (estaVacia()) return false;
        NodoDoble<T> actual = inicio;
        do {
            if (actual.getDato().equals(dato)) {
                return true;
            }
            actual = actual.getSig();
        } while (actual != inicio);
        return false;
    }

    public void limpiar() {
        inicio = null;
        fin = null;
        tamaño = 0;
    }

    // Métodos adicionales requeridos
    public String mostrarRecursivo() {
        if (estaVacia()) return "Lista vacía";
        return mostrarRecursivoAux(inicio, inicio);
    }

    private String mostrarRecursivoAux(NodoDoble<T> nodo, NodoDoble<T> inicioLista) {
        String result = nodo.getDato().toString();
        if (nodo.getSig() == inicioLista) {
            return result + " (circular)";
        } else {
            return result + " <-> " + mostrarRecursivoAux(nodo.getSig(), inicioLista);
        }
    }

    public T eliminaX(T x) {
        if (estaVacia()) return null;

        if (inicio.getDato().equals(x)) {
            return eliminarInicio();
        }

        NodoDoble<T> actual = inicio.getSig();
        while (actual != inicio) {
            if (actual.getDato().equals(x)) {
                if (actual == fin) {
                    return eliminarFin();
                }
                NodoDoble<T> anterior = actual.getAnt();
                NodoDoble<T> siguiente = actual.getSig();
                anterior.setSig(siguiente);
                siguiente.setAnt(anterior);
                tamaño--;
                return actual.getDato();
            }
            actual = actual.getSig();
        }
        return null;
    }

    public int buscar(T x) {
        if (estaVacia()) return -1;
        int pos = 0;
        NodoDoble<T> actual = inicio;
        do {
            if (actual.getDato().equals(x)) {
                return pos;
            }
            pos++;
            actual = actual.getSig();
        } while (actual != inicio);
        return -1;
    }

    public T eliminaPosicion(int posicion) {
        if (estaVacia() || posicion < 0 || posicion >= tamaño) return null;
        if (posicion == 0) {
            return eliminarInicio();
        }
        if (posicion == tamaño - 1) {
            return eliminarFin();
        }

        NodoDoble<T> actual = inicio;
        for (int i = 0; i < posicion; i++) {
            actual = actual.getSig();
        }

        NodoDoble<T> anterior = actual.getAnt();
        NodoDoble<T> siguiente = actual.getSig();
        anterior.setSig(siguiente);
        siguiente.setAnt(anterior);
        tamaño--;
        return actual.getDato();
    }

    public void ordenarLista() {
        if (estaVacia() || tamaño == 1) return;

        // Convertir a array para ordenar
        @SuppressWarnings("unchecked")
        T[] array = (T[]) new Comparable[tamaño];
        NodoDoble<T> actual = inicio;
        for (int i = 0; i < tamaño; i++) {
            array[i] = actual.getDato();
            actual = actual.getSig();
        }

        // Ordenar el array
        for (int i = 0; i < tamaño - 1; i++) {
            for (int j = 0; j < tamaño - i - 1; j++) {
                if (array[j].compareTo(array[j + 1]) > 0) {
                    T temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }

        // Reconstruir la lista
        limpiar();
        for (T elemento : array) {
            insertarFin(elemento);
        }
    }

    public void insertaenPosicion(T dato, int posicion) {
        if (posicion <= 0) {
            insertarInicio(dato);
            return;
        }
        if (posicion >= tamaño) {
            insertarFin(dato);
            return;
        }

        NodoDoble<T> nuevo = new NodoDoble<>(dato);
        NodoDoble<T> actual = inicio;
        for (int i = 0; i < posicion; i++) {
            actual = actual.getSig();
        }

        NodoDoble<T> anterior = actual.getAnt();
        nuevo.setSig(actual);
        nuevo.setAnt(anterior);
        anterior.setSig(nuevo);
        actual.setAnt(nuevo);
        tamaño++;
    }
}