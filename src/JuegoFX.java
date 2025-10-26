import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class JuegoFX extends Application {
    private JuegoLogica juego;
    private VBox[] panelesColumnas;
    private StackPane[] contenedoresReservas;
    private StackPane[] contenedoresFundaciones;
    private Button btnDeshacer;
    private Button btnPista;

    private enum OrigenTipo { COLUMNA, RESERVA }
    private OrigenTipo tipoSeleccionado = null;
    private int indiceSeleccionado = -1;

    @Override
    public void start(Stage primaryStage) {
        juego = new JuegoLogica();
        primaryStage.setTitle("Eight Off Solitaire");

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.DARKGREEN,
                CornerRadii.EMPTY, Insets.EMPTY)));
        root.setPadding(new Insets(10));

        // Panel superior con controles
        VBox panelSuperior = new VBox(10);
        panelSuperior.setPadding(new Insets(10));
        panelSuperior.setBackground(new Background(new BackgroundFill(Color.DARKGREEN,
                CornerRadii.EMPTY, Insets.EMPTY)));

        HBox panelControles = new HBox(10);
        panelControles.setAlignment(Pos.CENTER_LEFT);

        btnDeshacer = new Button("Deshacer");
        btnPista = new Button("Pista");
        btnDeshacer.setTooltip(new Tooltip("Deshacer último movimiento"));
        btnPista.setTooltip(new Tooltip("Mostrar un movimiento sugerido"));

        panelControles.getChildren().addAll(btnDeshacer, btnPista);

        // Panel de reservas (8 reservas en Eight Off)
        HBox panelReservas = new HBox(5);
        panelReservas.setAlignment(Pos.CENTER);
        contenedoresReservas = new StackPane[8];
        for (int i = 0; i < 8; i++) {
            final int idx = i;
            StackPane contenedor = crearContenedorCarta();
            contenedor.setOnMouseClicked(e -> manejarClicReserva(idx));
            contenedoresReservas[idx] = contenedor;
            panelReservas.getChildren().add(contenedor);
        }

        // Panel de fundaciones
        HBox panelFundaciones = new HBox(10);
        panelFundaciones.setAlignment(Pos.CENTER);
        contenedoresFundaciones = new StackPane[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            StackPane contenedor = crearContenedorCarta();
            contenedor.setOnMouseClicked(e -> manejarClicFundacion(idx));
            contenedoresFundaciones[idx] = contenedor;
            panelFundaciones.getChildren().add(contenedor);
        }

        VBox panelCeldasFund = new VBox(5);
        panelCeldasFund.getChildren().addAll(panelReservas, panelFundaciones);

        panelSuperior.getChildren().addAll(panelControles, panelCeldasFund);
        root.setTop(panelSuperior);

        // Panel de columnas
        HBox tablero = new HBox(5);
        tablero.setAlignment(Pos.CENTER);
        tablero.setPadding(new Insets(10));
        tablero.setBackground(new Background(new BackgroundFill(Color.DARKGREEN,
                CornerRadii.EMPTY, Insets.EMPTY)));

        panelesColumnas = new VBox[8];
        for (int i = 0; i < 8; i++) {
            panelesColumnas[i] = crearPanelColumna(i);
            tablero.getChildren().add(panelesColumnas[i]);
        }
        root.setCenter(tablero);

        // Listeners
        btnDeshacer.setOnAction(e -> {
            if (juego.puedeDeshacer()) {
                juego.deshacer();
                refrescarUI();
            }
        });

        btnPista.setOnAction(e -> {
            JuegoLogica.Move pista = juego.sugerirMovimiento();
            if (pista != null) {
                mostrarPista(pista);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sin movimientos");
                alert.setHeaderText(null);
                alert.setContentText("No hay movimientos posibles.\nEl juego ha terminado.");
                alert.showAndWait();
            }
        });

        refrescarUI();

        Scene scene = new Scene(root, 1200, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private StackPane crearContenedorCarta() {
        StackPane contenedor = new StackPane();
        contenedor.setPrefSize(80, 120);
        contenedor.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY,
                new CornerRadii(5), Insets.EMPTY)));
        contenedor.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));
        return contenedor;
    }

    private VBox crearPanelColumna(int idx) {
        VBox col = new VBox();
        col.setAlignment(Pos.TOP_CENTER);
        col.setSpacing(-90); // ESPACIO NEGATIVO PARA APILAR CARTAS
        col.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY,
                CornerRadii.EMPTY, Insets.EMPTY)));
        col.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        col.setPrefSize(100, 500);

        // Permitir clic en la columna completa
        col.setOnMouseClicked(e -> manejarClicColumna(idx));
        return col;
    }

    private void manejarClicColumna(int col) {
        if (juego.isJuegoTerminado()) return;

        // Quitar resaltado de pista si existe
        quitarResaltadoPista();

        if (tipoSeleccionado == null) {
            // Seleccionar carta de columna
            if (juego.getColumnas()[col].obtenerFin() != null) {
                tipoSeleccionado = OrigenTipo.COLUMNA;
                indiceSeleccionado = col;
                resaltarSeleccion();
                System.out.println("Columna " + col + " seleccionada como origen");
            }
        } else {
            // Realizar movimiento
            boolean exito = false;
            if (tipoSeleccionado == OrigenTipo.COLUMNA) {
                exito = juego.moverEntreColumnas(indiceSeleccionado, col);
                System.out.println("Intentando mover de columna " + indiceSeleccionado + " a columna " + col + ": " + (exito ? "ÉXITO" : "FALLÓ"));
            } else {
                exito = juego.moverDesdeReservaAColumna(indiceSeleccionado, col);
                System.out.println("Intentando mover de reserva " + indiceSeleccionado + " a columna " + col + ": " + (exito ? "ÉXITO" : "FALLÓ"));
            }

            if (!exito) {
                mostrarAlerta("Error", "Movimiento inválido", Alert.AlertType.ERROR);
            }
            resetSeleccion();
            refrescarUI();
        }
    }

    private void manejarClicReserva(int reserva) {
        if (juego.isJuegoTerminado()) return;

        // Quitar resaltado de pista si existe
        quitarResaltadoPista();

        if (tipoSeleccionado == null) {
            // Seleccionar carta de reserva
            if (juego.getReservas()[reserva] != null) {
                tipoSeleccionado = OrigenTipo.RESERVA;
                indiceSeleccionado = reserva;
                resaltarSeleccion();
                System.out.println("Reserva " + reserva + " seleccionada como origen");
            }
        } else {
            // Realizar movimiento
            boolean exito = false;
            if (tipoSeleccionado == OrigenTipo.COLUMNA) {
                // Mover de columna a reserva
                exito = juego.moverAReserva(indiceSeleccionado);
                System.out.println("Intentando mover de columna " + indiceSeleccionado + " a reserva libre: " + (exito ? "ÉXITO" : "FALLÓ"));
                if (!exito) {
                    mostrarAlerta("Error", "No hay reservas libres", Alert.AlertType.ERROR);
                }
            } else if (tipoSeleccionado == OrigenTipo.RESERVA) {
                // MOVER ENTRE RESERVAS - USANDO EL MÉTODO DE JUEGOLOGICA
                exito = juego.moverEntreReservas(indiceSeleccionado, reserva);
                System.out.println("Intentando mover de reserva " + indiceSeleccionado + " a reserva " + reserva + ": " + (exito ? "ÉXITO" : "FALLÓ"));
            }

            if (!exito && tipoSeleccionado == OrigenTipo.RESERVA) {
                mostrarAlerta("Error", "No se puede mover entre reservas", Alert.AlertType.ERROR);
            }
            resetSeleccion();
            refrescarUI();
        }
    }

    private void manejarClicFundacion(int fundacion) {
        if (juego.isJuegoTerminado()) return;

        // Quitar resaltado de pista si existe
        quitarResaltadoPista();

        if (tipoSeleccionado == null) return;

        boolean exito = false;
        if (tipoSeleccionado == OrigenTipo.COLUMNA) {
            exito = juego.moverAFundacionDesdeColumna(indiceSeleccionado, fundacion);
            System.out.println("Intentando mover de columna " + indiceSeleccionado + " a fundación " + fundacion + ": " + (exito ? "ÉXITO" : "FALLÓ"));
        } else {
            exito = juego.moverAFundacionDesdeReserva(indiceSeleccionado, fundacion);
            System.out.println("Intentando mover de reserva " + indiceSeleccionado + " a fundación " + fundacion + ": " + (exito ? "ÉXITO" : "FALLÓ"));
        }

        if (!exito) {
            mostrarAlerta("Error", "No se puede mover a esta fundación", Alert.AlertType.ERROR);
        }
        resetSeleccion();
        refrescarUI();
    }

    private void resaltarSeleccion() {
        // Resaltar visualmente la selección
        if (tipoSeleccionado == OrigenTipo.COLUMNA) {
            panelesColumnas[indiceSeleccionado].setBorder(new Border(new BorderStroke(Color.GOLD,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        } else if (tipoSeleccionado == OrigenTipo.RESERVA) {
            contenedoresReservas[indiceSeleccionado].setBorder(new Border(new BorderStroke(Color.GOLD,
                    BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(3))));
        }
    }

    private void mostrarPista(JuegoLogica.Move pista) {
        String mensaje = "";

        switch (pista.type) {
            case COL_COL:
                //mensaje = "Mover de columna " + (pista.origen + 1) + " a columna " + (pista.destino + 1);
                resaltarPistaColumna(pista.origen);
                resaltarPistaColumna(pista.destino);
                break;
            case COL_CEL:
                //mensaje = "Mover de columna " + (pista.origen + 1) + " a reserva " + (pista.destino + 1);
                resaltarPistaColumna(pista.origen);
                resaltarPistaReserva(pista.destino);
                break;
            case CEL_COL:
                //mensaje = "Mover de reserva " + (pista.origen + 1) + " a columna " + (pista.destino + 1);
                resaltarPistaReserva(pista.origen);
                resaltarPistaColumna(pista.destino);
                break;
            case COL_FND:
                //mensaje = "Mover de columna " + (pista.origen + 1) + " a fundación " + (pista.destino + 1);
                resaltarPistaColumna(pista.origen);
                resaltarPistaFundacion(pista.destino);
                break;
            case CEL_FND:
                //mensaje = "Mover de reserva " + (pista.origen + 1) + " a fundación " + (pista.destino + 1);
                resaltarPistaReserva(pista.origen);
                resaltarPistaFundacion(pista.destino);
                break;
        }

        /*Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Pista");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);*/

        // Cuando se cierra la alerta, quitar el resaltado de pista
        //alert.setOnHidden(e -> quitarResaltadoPista());

        //alert.showAndWait();
    }

    private void resaltarPistaColumna(int columna) {
        if (columna >= 0 && columna < 8) {
            panelesColumnas[columna].setBorder(new Border(new BorderStroke(Color.YELLOW,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        }
    }

    private void resaltarPistaReserva(int reserva) {
        if (reserva >= 0 && reserva < 8) {
            contenedoresReservas[reserva].setBorder(new Border(new BorderStroke(Color.YELLOW,
                    BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(3))));
        }
    }

    private void resaltarPistaFundacion(int fundacion) {
        if (fundacion >= 0 && fundacion < 4) {
            contenedoresFundaciones[fundacion].setBorder(new Border(new BorderStroke(Color.YELLOW,
                    BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(3))));
        }
    }

    private void quitarResaltadoPista() {
        // Quitar resaltado amarillo de todas las columnas
        for (int i = 0; i < 8; i++) {
            panelesColumnas[i].setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        }

        // Quitar resaltado amarillo de todas las reservas
        for (int i = 0; i < 8; i++) {
            contenedoresReservas[i].setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));
        }

        // Quitar resaltado amarillo de todas las fundaciones
        for (int i = 0; i < 4; i++) {
            contenedoresFundaciones[i].setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));
        }
    }

    private void resetSeleccion() {
        // Quitar resaltado visual
        if (tipoSeleccionado == OrigenTipo.COLUMNA && indiceSeleccionado >= 0) {
            panelesColumnas[indiceSeleccionado].setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        } else if (tipoSeleccionado == OrigenTipo.RESERVA && indiceSeleccionado >= 0) {
            contenedoresReservas[indiceSeleccionado].setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));
        }

        tipoSeleccionado = null;
        indiceSeleccionado = -1;
    }

    private void refrescarUI() {
        // Actualizar reservas
        Carta[] reservas = juego.getReservas();
        for (int i = 0; i < 8; i++) {
            StackPane contenedor = contenedoresReservas[i];
            contenedor.getChildren().clear();

            if (reservas[i] != null) {
                // Crear CardView personalizada para la carta
                CardView cardView = crearCardView(reservas[i]);
                contenedor.getChildren().add(cardView);
                contenedor.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,
                        CornerRadii.EMPTY, Insets.EMPTY)));
            } else {
                // Mostrar contenedor vacío
                contenedor.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY,
                        new CornerRadii(5), Insets.EMPTY)));
            }
        }

        // Actualizar fundaciones
        ListaDobleCircular<Carta>[] fundaciones = juego.getFundaciones();
        for (int i = 0; i < 4; i++) {
            StackPane contenedor = contenedoresFundaciones[i];
            contenedor.getChildren().clear();

            Carta top = fundaciones[i].obtenerFin();
            if (top != null) {
                // Crear CardView personalizada para la carta
                CardView cardView = crearCardView(top);
                contenedor.getChildren().add(cardView);
                contenedor.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,
                        CornerRadii.EMPTY, Insets.EMPTY)));
            } else {
                // Mostrar contenedor vacío con texto
                Label label = new Label("F" + (i + 1));
                label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                label.setTextFill(Color.DARKGREEN);
                contenedor.getChildren().add(label);
                contenedor.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY,
                        new CornerRadii(5), Insets.EMPTY)));
            }
        }

        // Actualizar columnas - CON CARTAS APILADAS
        ListaDobleCircular<Carta>[] columnas = juego.getColumnas();
        for (int i = 0; i < 8; i++) {
            panelesColumnas[i].getChildren().clear();

            NodoDoble<Carta> nodo = columnas[i].getInicio();
            int cardIndex = 0;
            if (nodo != null) {
                do {
                    // Crear CardView personalizada para cada carta
                    CardView cardView = crearCardView(nodo.getDato());
                    cardView.setMaxWidth(Double.MAX_VALUE);
                    cardView.setAlignment(Pos.CENTER);

                    // Aplicar desplazamiento vertical para apilar
                    VBox.setMargin(cardView, new Insets(cardIndex * 1, 0, 0, 0));

                    panelesColumnas[i].getChildren().add(cardView);
                    nodo = nodo.getSig();
                    cardIndex++;
                } while (nodo != columnas[i].getInicio());
            }
        }

        // Verificar estado del juego
        if (juego.esVictoria()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Victoria");
            alert.setHeaderText(null);
            alert.setContentText("¡Felicidades! Has ganado el juego.");
            alert.showAndWait();
        } else if (!juego.hayMovimientosPosibles()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fin del juego");
            alert.setHeaderText(null);
            alert.setContentText("No hay más movimientos posibles. Juego terminado.");
            alert.showAndWait();
        }

        btnDeshacer.setDisable(!juego.puedeDeshacer());
    }

    /**
     * Método para crear una CardView visualmente atractiva
     */
    private CardView crearCardView(Carta carta) {
        CardView cardView = new CardView(carta);
        return cardView;
    }

    // Clase interna CardView adaptada para tu clase Carta - CON DISEÑO MEJORADO
    public class CardView extends StackPane {
        private Carta carta;
        private static final double CARD_WIDTH = 80;
        private static final double CARD_HEIGHT = 120;

        public CardView(Carta carta) {
            this.carta = carta;
            initializeCard();
        }

        private void initializeCard() {
            setPrefSize(CARD_WIDTH, CARD_HEIGHT);
            setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0.75, 0.75);");

            // Fondo de la carta con bordes redondeados mejorados
            Rectangle cardRect = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
            cardRect.setFill(Color.WHITE);
            cardRect.setArcWidth(CARD_WIDTH * 0.125);  // 12.5% del ancho
            cardRect.setArcHeight(CARD_HEIGHT * 0.083); // 8.3% del alto
            cardRect.setStroke(Color.BLACK);
            cardRect.setStrokeWidth(1.5);

            // Texto principal centrado (más grande y proporcional)
            Text mainText = new Text(getCardText());
            double baseFontSize = CARD_HEIGHT * 0.1167; // Tamaño base proporcional
            double enlargedFontSize = baseFontSize * 1.5; // 50% más grande
            mainText.setFont(Font.font("Arial", FontWeight.BOLD, enlargedFontSize));
            mainText.setFill(getTextColor());

            // Texto pequeño en esquina superior izquierda
            Text cornerText = new Text(getCardText());
            double cornerFontSize = baseFontSize * 0.8; // 70% del tamaño base
            cornerText.setFont(Font.font("Arial", FontWeight.BOLD, cornerFontSize));
            cornerText.setFill(getTextColor());
            cornerText.setTranslateX(-CARD_WIDTH * 0.35); // Mover a la izquierda
            cornerText.setTranslateY(-CARD_HEIGHT * 0.35); // Mover hacia arriba

            getChildren().addAll(cardRect, mainText, cornerText);
        }

        private String getCardText() {
            String valorStr;
            switch (carta.getValor()) {
                case 1: valorStr = "A"; break;
                case 11: valorStr = "J"; break;
                case 12: valorStr = "Q"; break;
                case 13: valorStr = "K"; break;
                default: valorStr = String.valueOf(carta.getValor());
            }

            String suitStr;
            switch (carta.getPalo()) {
                case CORAZONES: suitStr = "♥"; break;
                case DIAMANTES: suitStr = "♦"; break;
                case TREBOLES: suitStr = "♣"; break;
                case PICAS: suitStr = "♠"; break;
                default: suitStr = "";
            }

            return valorStr + suitStr;
        }

        private Color getTextColor() {
            return carta.isRoja() ? Color.RED : Color.BLACK;
        }

        public Carta getCarta() {
            return carta;
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}