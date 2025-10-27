import java.util.Stack;

public class JuegoLogica {
    private Baraja baraja;
    private ListaDobleCircular<Carta>[] columnas;
    private ListaDobleCircular<Carta>[] fundaciones;
    private Carta[] reservas;
    private Stack<State> undoStack;
    private boolean juegoTerminado;

    @SuppressWarnings("unchecked")
    public JuegoLogica() {
        baraja = new Baraja();
        columnas = new ListaDobleCircular[8];
        fundaciones = new ListaDobleCircular[4];
        reservas = new Carta[8];
        undoStack = new Stack<>();
        juegoTerminado = false;

        for (int i = 0; i < 8; i++) {
            columnas[i] = new ListaDobleCircular<>();
            reservas[i] = null;
        }
        for (int i = 0; i < 4; i++) {
            fundaciones[i] = new ListaDobleCircular<>();
        }

        repartirCartas();
    }

    private void repartirCartas() {
        // Eight Off: 8 columnas con 6 cartas cada una
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 6; j++) {
                Carta carta = baraja.robarCarta();
                columnas[i].insertarFin(carta);
            }
        }

        // Las 4 cartas restantes van a las primeras 4 reservas
        for (int i = 0; i < 4; i++) {
            if (!baraja.estaVacia()) {
                reservas[i] = baraja.robarCarta();
            }
        }
    }

    private void guardarEstado() {
        State s = new State();
        s.columnas = deepCopyLists(columnas);
        s.fundaciones = deepCopyLists(fundaciones);
        s.reservas = reservas.clone();
        undoStack.push(s);
    }

    @SuppressWarnings("unchecked")
    private ListaDobleCircular<Carta>[] deepCopyLists(ListaDobleCircular<Carta>[] src) {
        ListaDobleCircular<Carta>[] copy = new ListaDobleCircular[src.length];
        for (int i = 0; i < src.length; i++) {
            ListaDobleCircular<Carta> newList = new ListaDobleCircular<>();
            NodoDoble<Carta> nodo = src[i].getInicio();
            if (nodo != null) {
                do {
                    newList.insertarFin(nodo.getDato());
                    nodo = nodo.getSig();
                } while (nodo != src[i].getInicio());
            }
            copy[i] = newList;
        }
        return copy;
    }

    // REGLAS EIGHT OFF MODIFICADAS
    public boolean puedeMoverEntreColumnas(Carta origen, Carta destino) {
        if (destino == null) {
            // Columna vacía - solo se puede mover Rey (K)
            return true;
        }
        return origen.getPalo() == destino.getPalo() && origen.getValor() == destino.getValor() - 1;
    }

    public boolean puedeMoverAFundacion(Carta carta, int fundacion) {
        Carta topFundacion = fundaciones[fundacion].obtenerFin();
        if (topFundacion == null) {
            return carta.getValor() == 1; // Solo Ases a fundación vacía
        }
        return carta.getPalo() == topFundacion.getPalo() && carta.getValor() == topFundacion.getValor() + 1;
    }

    public boolean puedeMoverAReserva(int reserva) {
        return reservas[reserva] == null;
    }

    // MÉTODOS DE VERIFICACIÓN
    public boolean canMoveEntreColumnas(int origenCol, int destinoCol) {
        if (origenCol < 0 || origenCol >= 8 || destinoCol < 0 || destinoCol >= 8) return false;
        Carta cartaOrigen = columnas[origenCol].obtenerFin();
        if (cartaOrigen == null) return false;
        Carta cartaDestino = columnas[destinoCol].obtenerFin();
        return puedeMoverEntreColumnas(cartaOrigen, cartaDestino);
    }

    public boolean canMoveACeldaLibre(int columna) {
        if (columna < 0 || columna >= 8) return false;
        if (columnas[columna].obtenerFin() == null) return false;
        for (Carta c : reservas) {
            if (c == null) return true;
        }
        return false;
    }

    public boolean canMoveDesdeCeldaALaColumna(int reserva, int destinoCol) {
        if (reserva < 0 || reserva >= 8 || destinoCol < 0 || destinoCol >= 8) return false;
        Carta c = reservas[reserva];
        if (c == null) return false;
        Carta cartaDestino = columnas[destinoCol].obtenerFin();
        return puedeMoverEntreColumnas(c, cartaDestino);
    }

    public boolean canMoveAFundacionDesdeColumna(int columna, int fundacion) {
        if (columna < 0 || columna >= 8 || fundacion < 0 || fundacion >= 4) return false;
        Carta carta = columnas[columna].obtenerFin();
        if (carta == null) return false;
        return puedeMoverAFundacion(carta, fundacion);
    }

    public boolean canMoveAFundacionDesdeCelda(int reserva, int fundacion) {
        if (reserva < 0 || reserva >= 8 || fundacion < 0 || fundacion >= 4) return false;
        Carta c = reservas[reserva];
        if (c == null) return false;
        return puedeMoverAFundacion(c, fundacion);
    }

    // MÉTODOS PARA OBTENER MOVIMIENTOS VÁLIDOS (para resaltar)
    public java.util.List<Integer> getColumnasValidasParaCarta(Carta carta) {
        java.util.List<Integer> columnasValidas = new java.util.ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Carta topColumna = columnas[i].obtenerFin();
            if (puedeMoverEntreColumnas(carta, topColumna)) {
                columnasValidas.add(i);
            }
        }
        return columnasValidas;
    }

    public java.util.List<Integer> getFundacionesValidasParaCarta(Carta carta) {
        java.util.List<Integer> fundacionesValidas = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (puedeMoverAFundacion(carta, i)) {
                fundacionesValidas.add(i);
            }
        }
        return fundacionesValidas;
    }

    public java.util.List<Integer> getReservasLibres() {
        java.util.List<Integer> reservasLibres = new java.util.ArrayList<>();
        for (int i = 0; i < 8; i++) {
            if (reservas[i] == null) {
                reservasLibres.add(i);
            }
        }
        return reservasLibres;
    }

    // MÉTODOS DE MOVIMIENTO
    public boolean moverEntreColumnas(int origenCol, int destinoCol) {
        if (!canMoveEntreColumnas(origenCol, destinoCol)) return false;
        guardarEstado();
        Carta carta = columnas[origenCol].eliminarFin();
        columnas[destinoCol].insertarFin(carta);
        verificarVictoria();
        return true;
    }

    public boolean moverAReserva(int columna) {
        if (!canMoveACeldaLibre(columna)) return false;
        guardarEstado();
        Carta carta = columnas[columna].eliminarFin();
        for (int i = 0; i < 8; i++) {
            if (reservas[i] == null) {
                reservas[i] = carta;
                break;
            }
        }
        return true;
    }

    public boolean moverDesdeReservaAColumna(int reserva, int destinoCol) {
        if (!canMoveDesdeCeldaALaColumna(reserva, destinoCol)) return false;
        guardarEstado();
        Carta carta = reservas[reserva];
        reservas[reserva] = null;
        columnas[destinoCol].insertarFin(carta);
        verificarVictoria();
        return true;
    }

    public boolean moverAFundacionDesdeColumna(int columna, int fundacion) {
        if (!canMoveAFundacionDesdeColumna(columna, fundacion)) return false;
        guardarEstado();
        Carta carta = columnas[columna].eliminarFin();
        fundaciones[fundacion].insertarFin(carta);
        verificarVictoria();
        return true;
    }

    public boolean moverAFundacionDesdeReserva(int reserva, int fundacion) {
        if (!canMoveAFundacionDesdeCelda(reserva, fundacion)) return false;
        guardarEstado();
        Carta carta = reservas[reserva];
        reservas[reserva] = null;
        fundaciones[fundacion].insertarFin(carta);
        verificarVictoria();
        return true;
    }

    // MOVIMIENTO AUTOMÁTICO CON DOBLE CLIC
    public Move moverAutomaticamenteDesdeColumna(int columna) {
        Carta carta = columnas[columna].obtenerFin();
        if (carta == null) return null;

        // 1. Intentar mover a fundación
        for (int f = 0; f < 4; f++) {
            if (canMoveAFundacionDesdeColumna(columna, f)) {
                return new Move(MoveType.COL_FND, columna, f);
            }
        }

        // 2. Intentar mover a columna
        for (int c = 0; c < 8; c++) {
            if (c != columna && canMoveEntreColumnas(columna, c)) {
                return new Move(MoveType.COL_COL, columna, c);
            }
        }

        return null;
    }

    public Move moverAutomaticamenteDesdeReserva(int reserva) {
        Carta carta = reservas[reserva];
        if (carta == null) return null;

        // 1. Intentar mover a fundación
        for (int f = 0; f < 4; f++) {
            if (canMoveAFundacionDesdeCelda(reserva, f)) {
                return new Move(MoveType.CEL_FND, reserva, f);
            }
        }

        // 2. Intentar mover a columna
        for (int c = 0; c < 8; c++) {
            if (canMoveDesdeCeldaALaColumna(reserva, c)) {
                return new Move(MoveType.CEL_COL, reserva, c);
            }
        }

        return null;
    }

    // SISTEMA DE PISTAS
    public Move sugerirMovimiento() {
        // 1. Intentar mover a fundación desde columna
        for (int i = 0; i < 8; i++) {
            for (int f = 0; f < 4; f++) {
                if (canMoveAFundacionDesdeColumna(i, f)) {
                    return new Move(MoveType.COL_FND, i, f);
                }
            }
        }

        // 2. Intentar mover a fundación desde reserva
        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 4; f++) {
                if (canMoveAFundacionDesdeCelda(r, f)) {
                    return new Move(MoveType.CEL_FND, r, f);
                }
            }
        }

        // 3. Intentar mover entre columnas
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i != j && canMoveEntreColumnas(i, j)) {
                    return new Move(MoveType.COL_COL, i, j);
                }
            }
        }

        // 4. Intentar mover a reserva libre
        for (int i = 0; i < 8; i++) {
            if (canMoveACeldaLibre(i)) {
                for (int r = 0; r < 8; r++) {
                    if (reservas[r] == null) {
                        return new Move(MoveType.COL_CEL, i, r);
                    }
                }
            }
        }

        // 5. Intentar mover desde reserva a columna
        for (int r = 0; r < 8; r++) {
            for (int j = 0; j < 8; j++) {
                if (canMoveDesdeCeldaALaColumna(r, j)) {
                    return new Move(MoveType.CEL_COL, r, j);
                }
            }
        }

        return null;
    }

    // VERIFICACIÓN DE VICTORIA Y FIN DEL JUEGO
    private void verificarVictoria() {
        for (int i = 0; i < 4; i++) {
            if (fundaciones[i].getTamaño() != 13) {
                return;
            }
        }
        juegoTerminado = true;
    }

    public boolean esVictoria() {
        return juegoTerminado;
    }

    public boolean hayMovimientosPosibles() {
        return sugerirMovimiento() != null;
    }

    // SISTEMA DE DESHACER
    public boolean puedeDeshacer() {
        return !undoStack.isEmpty();
    }

    public void deshacer() {
        if (puedeDeshacer()) {
            State s = undoStack.pop();
            columnas = deepCopyLists(s.columnas);
            fundaciones = deepCopyLists(s.fundaciones);
            reservas = s.reservas.clone();
            juegoTerminado = false;
        }
    }

    // GETTERS
    public ListaDobleCircular<Carta>[] getColumnas() { return columnas; }
    public ListaDobleCircular<Carta>[] getFundaciones() { return fundaciones; }
    public Carta[] getReservas() { return reservas; }
    public boolean isJuegoTerminado() { return juegoTerminado; }

    // CLASES INTERNAS
    public static class Move {
        public MoveType type;
        public int origen, destino;
        public Move(MoveType t, int o, int d) {
            type = t; origen = o; destino = d;
        }

        public boolean ejecutar(JuegoLogica juego) {
            switch (type) {
                case COL_COL: return juego.moverEntreColumnas(origen, destino);
                case COL_CEL: return juego.moverAReserva(origen);
                case CEL_COL: return juego.moverDesdeReservaAColumna(origen, destino);
                case COL_FND: return juego.moverAFundacionDesdeColumna(origen, destino);
                case CEL_FND: return juego.moverAFundacionDesdeReserva(origen, destino);
                default: return false;
            }
        }

        @Override
        public String toString() {
            return type + " from " + origen + " to " + destino;
        }
    }

    public enum MoveType { COL_COL, COL_CEL, CEL_COL, COL_FND, CEL_FND }

    private static class State {
        ListaDobleCircular<Carta>[] columnas;
        ListaDobleCircular<Carta>[] fundaciones;
        Carta[] reservas;
    }

    public boolean moverEntreReservas(int reservaOrigen, int reservaDestino) {
        // Verificar que los índices sean válidos
        if (reservaOrigen < 0 || reservaOrigen >= 8 || reservaDestino < 0 || reservaDestino >= 8) {
            return false;
        }

        // Verificar que la reserva origen tenga carta y la reserva destino esté vacía
        if (reservas[reservaOrigen] == null || reservas[reservaDestino] != null) {
            return false;
        }

        // Guardar estado antes del movimiento
        guardarEstado();

        // Mover la carta entre reservas
        reservas[reservaDestino] = reservas[reservaOrigen];
        reservas[reservaOrigen] = null;

        return true;
    }

    public boolean canMoveEntreReservas(int reservaOrigen, int reservaDestino) {
        if (reservaOrigen < 0 || reservaOrigen >= 8 || reservaDestino < 0 || reservaDestino >= 8) {
            return false;
        }
        return reservas[reservaOrigen] != null && reservas[reservaDestino] == null;
    }
}