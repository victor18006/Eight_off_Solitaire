import java.util.Random;

public class Baraja {
    private ListaDobleCircular<Carta> cartas;

    public Baraja() {
        cartas = new ListaDobleCircular<>();
        inicializarBaraja();
        barajar();
    }

    private void inicializarBaraja() {
        for (Carta.Palo palo : Carta.Palo.values()) {
            for (int valor = 1; valor <= 13; valor++) {
                cartas.insertarFin(new Carta(valor, palo));
            }
        }
    }

    public void barajar() {
        Random rand = new Random();
        int total = cartas.getTamaño();

        // Convertir a array para barajar
        Carta[] arrayCartas = new Carta[total];
        NodoDoble<Carta> actual = cartas.getInicio();
        for (int i = 0; i < total; i++) {
            arrayCartas[i] = actual.getDato();
            actual = actual.getSig();
        }

        // Barajar el array
        for (int i = total - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            Carta temp = arrayCartas[i];
            arrayCartas[i] = arrayCartas[j];
            arrayCartas[j] = temp;
        }

        // Reconstruir la baraja
        cartas.limpiar();
        for (Carta carta : arrayCartas) {
            cartas.insertarFin(carta);
        }
    }

    public Carta robarCarta() {
        return cartas.eliminarInicio();
    }

    public boolean estaVacia() {
        return cartas.estaVacia();
    }

    public int cartasRestantes() {
        return cartas.getTamaño();
    }

    public void mostrarBaraja() {
        System.out.println(cartas.mostrarAdelante());
    }

    public void ordenar() {
        cartas.ordenarLista();
    }
}