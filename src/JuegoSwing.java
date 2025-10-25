import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JuegoSwing extends JFrame {
    private JuegoLogica juego;
    private JPanel[] panelesColumnas;
    private JButton[] botonesReservas;
    private JButton[] botonesFundaciones;
    private JButton btnDeshacer;
    private JButton btnPista;

    private enum OrigenTipo { COLUMNA, RESERVA }
    private OrigenTipo tipoSeleccionado = null;
    private int indiceSeleccionado = -1;

    public JuegoSwing() {
        juego = new JuegoLogica();
        setTitle("Eight Off Solitaire");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLayout(new BorderLayout(10, 10));

        // Panel superior con controles
        JPanel panelSuperior = new JPanel(new BorderLayout(5, 5));
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        btnDeshacer = new JButton("↩️ Deshacer");
        btnPista = new JButton("💡 Pista");
        btnDeshacer.setToolTipText("Deshacer último movimiento");
        btnPista.setToolTipText("Mostrar un movimiento sugerido");

        panelControles.add(btnDeshacer);
        panelControles.add(btnPista);

        // Panel de reservas (8 reservas en Eight Off)
        JPanel panelReservas = new JPanel(new GridLayout(1, 8, 5, 5));
        botonesReservas = new JButton[8];
        for (int i = 0; i < 8; i++) {
            final int idx = i;
            botonesReservas[idx] = crearBotonCarta("", e -> manejarClicReserva(idx));
            botonesReservas[idx].setToolTipText("Reserva " + (idx + 1));
            panelReservas.add(botonesReservas[idx]);
        }

        // Panel de fundaciones
        JPanel panelFundaciones = new JPanel(new GridLayout(1, 4, 10, 5));
        botonesFundaciones = new JButton[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            botonesFundaciones[idx] = crearBotonCarta("", e -> manejarClicFundacion(idx));
            botonesFundaciones[idx].setToolTipText("Fundación " + (idx + 1));
            panelFundaciones.add(botonesFundaciones[idx]);
        }

        JPanel panelCeldasFund = new JPanel(new BorderLayout(5, 5));
        panelCeldasFund.add(panelReservas, BorderLayout.NORTH);
        panelCeldasFund.add(panelFundaciones, BorderLayout.SOUTH);

        panelSuperior.add(panelControles, BorderLayout.NORTH);
        panelSuperior.add(panelCeldasFund, BorderLayout.SOUTH);
        add(panelSuperior, BorderLayout.NORTH);

        // Panel de columnas
        JPanel tablero = new JPanel(new GridLayout(1, 8, 5, 5));
        panelesColumnas = new JPanel[8];
        for (int i = 0; i < 8; i++) {
            panelesColumnas[i] = crearPanelColumna(i);
            tablero.add(panelesColumnas[i]);
        }
        add(tablero, BorderLayout.CENTER);

        // Listeners
        btnDeshacer.addActionListener(e -> {
            if (juego.puedeDeshacer()) {
                juego.deshacer();
                refrescarUI();
            }
        });

        btnPista.addActionListener(e -> {
            JuegoLogica.Move pista = juego.sugerirMovimiento();
            if (pista != null) {
                mostrarPista(pista);
            } else {
                JOptionPane.showMessageDialog(this,
                        "No hay movimientos posibles.\nEl juego ha terminado.",
                        "Sin movimientos",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        refrescarUI();
    }

    private JButton crearBotonCarta(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 16));
        btn.setPreferredSize(new Dimension(80, 120));
        btn.setBackground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        btn.addActionListener(listener);
        return btn;
    }

    private JPanel crearPanelColumna(int idx) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        col.setBackground(new Color(240, 240, 240));
        col.setPreferredSize(new Dimension(100, 500));

        // Permitir clic en la columna completa
        col.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                manejarClicColumna(idx);
            }
        });
        return col;
    }

    private void manejarClicColumna(int col) {
        if (juego.isJuegoTerminado()) return;

        if (tipoSeleccionado == null) {
            // Seleccionar carta de columna
            if (juego.getColumnas()[col].obtenerFin() != null) {
                tipoSeleccionado = OrigenTipo.COLUMNA;
                indiceSeleccionado = col;
                resaltarSeleccion();
            }
        } else {
            // Realizar movimiento
            boolean exito = false;
            if (tipoSeleccionado == OrigenTipo.COLUMNA) {
                exito = juego.moverEntreColumnas(indiceSeleccionado, col);
            } else {
                exito = juego.moverDesdeReservaAColumna(indiceSeleccionado, col);
            }

            if (!exito) {
                JOptionPane.showMessageDialog(this, "Movimiento inválido", "Error", JOptionPane.ERROR_MESSAGE);
            }
            resetSeleccion();
            refrescarUI();
        }
    }

    private void manejarClicReserva(int reserva) {
        if (juego.isJuegoTerminado()) return;

        if (tipoSeleccionado == null) {
            // Seleccionar carta de reserva
            if (juego.getReservas()[reserva] != null) {
                tipoSeleccionado = OrigenTipo.RESERVA;
                indiceSeleccionado = reserva;
                resaltarSeleccion();
            }
        } else {
            // Mover a reserva libre
            if (tipoSeleccionado == OrigenTipo.COLUMNA) {
                boolean exito = juego.moverAReserva(indiceSeleccionado);
                if (!exito) {
                    JOptionPane.showMessageDialog(this, "No hay reservas libres", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            resetSeleccion();
            refrescarUI();
        }
    }

    private void manejarClicFundacion(int fundacion) {
        if (juego.isJuegoTerminado() || tipoSeleccionado == null) return;

        boolean exito = false;
        if (tipoSeleccionado == OrigenTipo.COLUMNA) {
            exito = juego.moverAFundacionDesdeColumna(indiceSeleccionado, fundacion);
        } else {
            exito = juego.moverAFundacionDesdeReserva(indiceSeleccionado, fundacion);
        }

        if (!exito) {
            JOptionPane.showMessageDialog(this, "No se puede mover a esta fundación", "Error", JOptionPane.ERROR_MESSAGE);
        }
        resetSeleccion();
        refrescarUI();
    }

    private void resaltarSeleccion() {
        // Implementar resaltado visual si es necesario
    }

    private void mostrarPista(JuegoLogica.Move pista) {
        Component origenComp = null, destComp = null;
        String mensaje = "";

        switch (pista.type) {
            case COL_COL:
                origenComp = panelesColumnas[pista.origen];
                destComp = panelesColumnas[pista.destino];
                mensaje = "Mover de columna " + (pista.origen + 1) + " a columna " + (pista.destino + 1);
                break;
            case COL_CEL:
                origenComp = panelesColumnas[pista.origen];
                destComp = botonesReservas[pista.destino];
                mensaje = "Mover de columna " + (pista.origen + 1) + " a reserva " + (pista.destino + 1);
                break;
            case CEL_COL:
                origenComp = botonesReservas[pista.origen];
                destComp = panelesColumnas[pista.destino];
                mensaje = "Mover de reserva " + (pista.origen + 1) + " a columna " + (pista.destino + 1);
                break;
            case COL_FND:
                origenComp = panelesColumnas[pista.origen];
                destComp = botonesFundaciones[pista.destino];
                mensaje = "Mover de columna " + (pista.origen + 1) + " a fundación " + (pista.destino + 1);
                break;
            case CEL_FND:
                origenComp = botonesReservas[pista.origen];
                destComp = botonesFundaciones[pista.destino];
                mensaje = "Mover de reserva " + (pista.origen + 1) + " a fundación " + (pista.destino + 1);
                break;
        }

        JOptionPane.showMessageDialog(this, mensaje, "Pista", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetSeleccion() {
        tipoSeleccionado = null;
        indiceSeleccionado = -1;
    }

    private void refrescarUI() {
        // Actualizar reservas
        Carta[] reservas = juego.getReservas();
        for (int i = 0; i < 8; i++) {
            if (reservas[i] != null) {
                botonesReservas[i].setText(reservas[i].toString());
                botonesReservas[i].setBackground(Color.WHITE);
            } else {
                botonesReservas[i].setText("");
                botonesReservas[i].setBackground(Color.LIGHT_GRAY);
            }
        }

        // Actualizar fundaciones
        ListaDobleCircular<Carta>[] fundaciones = juego.getFundaciones();
        for (int i = 0; i < 4; i++) {
            Carta top = fundaciones[i].obtenerFin();
            if (top != null) {
                botonesFundaciones[i].setText(top.toString());
                botonesFundaciones[i].setBackground(Color.WHITE);
            } else {
                botonesFundaciones[i].setText("Fundación " + (i + 1));
                botonesFundaciones[i].setBackground(Color.LIGHT_GRAY);
            }
        }

        // Actualizar columnas
        ListaDobleCircular<Carta>[] columnas = juego.getColumnas();
        for (int i = 0; i < 8; i++) {
            panelesColumnas[i].removeAll();
            panelesColumnas[i].setLayout(new BoxLayout(panelesColumnas[i], BoxLayout.Y_AXIS));

            NodoDoble<Carta> nodo = columnas[i].getInicio();
            if (nodo != null) {
                do {
                    JLabel lblCarta = new JLabel(nodo.getDato().toString());
                    lblCarta.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    lblCarta.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    lblCarta.setOpaque(true);
                    lblCarta.setBackground(Color.WHITE);
                    lblCarta.setAlignmentX(Component.CENTER_ALIGNMENT);
                    panelesColumnas[i].add(lblCarta);
                    nodo = nodo.getSig();
                } while (nodo != columnas[i].getInicio());
            }

            panelesColumnas[i].revalidate();
            panelesColumnas[i].repaint();
        }

        // Verificar estado del juego
        if (juego.esVictoria()) {
            JOptionPane.showMessageDialog(this, "¡Felicidades! Has ganado el juego.", "Victoria", JOptionPane.INFORMATION_MESSAGE);
        } else if (!juego.hayMovimientosPosibles()) {
            JOptionPane.showMessageDialog(this, "No hay más movimientos posibles. Juego terminado.", "Fin del juego", JOptionPane.INFORMATION_MESSAGE);
        }

        btnDeshacer.setEnabled(juego.puedeDeshacer());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JuegoSwing().setVisible(true);
        });
    }
}