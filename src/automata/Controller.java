package automata;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public Pane pane;

    @FXML
    public Canvas canvas;

    @FXML
    public Button playButton;

    @FXML
    public Button stopButton;

    private Board board;
    private Service<Void> service;
    private boolean running;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        board = new Board();

        pane.prefWidthProperty().bind(pane.widthProperty());
        pane.prefHeightProperty().bind(pane.heightProperty());

        pane.prefWidthProperty().addListener((ov, oldValue, newValue) -> {
            canvas.setWidth(newValue.doubleValue());
            draw(canvas.getGraphicsContext2D());
        });

        pane.prefHeightProperty().addListener((ov, oldValue, newValue) -> {
            canvas.setHeight(newValue.doubleValue());
            draw(canvas.getGraphicsContext2D());
        });

        EventHandler<MouseEvent> handler = event -> {
            Point2D boardPosition = getBoardPosition();

            int x = (int) ((event.getX() - boardPosition.getX()) /
                    ((canvas.getWidth() - 2 * boardPosition.getX()) / board.getWidth()));

            int y = (int) ((event.getY() - boardPosition.getY()) /
                    ((canvas.getHeight() - 2 * boardPosition.getY()) / board.getHeight()));

            if (x < 0 || x >= board.getWidth() || y < 0 || y >= board.getHeight())
                return;

            if (event.getButton() == MouseButton.PRIMARY)
                board.revive(x, y);
            else if (event.getButton() == MouseButton.SECONDARY)
                board.kill(x, y);

            draw(canvas.getGraphicsContext2D());
        };

        canvas.setOnMousePressed(handler);
        canvas.setOnMouseDragged(handler);

        service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        while (running) {
                            board.nextGeneration();
                            Platform.runLater(() -> draw(canvas.getGraphicsContext2D()));
                            Thread.sleep(100);
                        }
                        return null;
                    }
                };
            }
        };

        service.setOnRunning(v -> {
            playButton.setDisable(true);
            stopButton.setDisable(false);
        });

        service.setOnSucceeded(v -> {
            playButton.setDisable(false);
            stopButton.setDisable(true);
            service.reset();
        });
    }

    /**
     * @return the cell size
     */
    private double getCellSize() {
        double wr = canvas.getWidth() / board.getWidth();
        double hr = canvas.getHeight() / board.getHeight();

        return wr > hr ? hr : wr;
    }

    /**
     * @return the board position
     */
    private Point2D getBoardPosition() {
        double size = getCellSize();
        double x = canvas.getWidth() / 2 - board.getWidth() * size / 2;
        double y = canvas.getHeight() / 2 - board.getHeight() * size  / 2;

        return new Point2D(x, y);
    }

    /**
     * Draws the grid and cells.
     *
     * @param gc the graphicsContext
     */
    private void draw(GraphicsContext gc) {
        double size = getCellSize();
        Point2D boardPosition = getBoardPosition();

        // Clear the canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw the grid
        gc.setStroke(Color.LIGHTGRAY);
        for (int i = 0; i <= board.getWidth(); i++)
            gc.strokeLine(boardPosition.getX() + i * size, boardPosition.getY(),
                          boardPosition.getX() + i * size, boardPosition.getY() + board.getHeight() * size);

        for (int i = 0; i <= board.getHeight(); i++)
            gc.strokeLine(boardPosition.getX(), boardPosition.getY() + i * size,
                      boardPosition.getX() + board.getWidth() * size, boardPosition.getY() + i * size);

        // Draw cells
        gc.setFill(Color.ORANGE);
        for (int i = 0; i < board.getWidth(); i++)
            for (int j = 0; j < board.getHeight(); j++)
                if (board.isAlive(i, j))
                    gc.fillRect(boardPosition.getX() + i * size, boardPosition.getY() + j * size, size, size);
    }

    @FXML
    public void handleNewButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Dialog.fxml"));
        Parent parent = fxmlLoader.load();
        DialogController dc = fxmlLoader.getController();

        Scene scene = new Scene(parent, 250, 150);
        Stage stage = new Stage();
        stage.setTitle("New Board");
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();

        board = new Board(dc.widthSpinner.getValue(), dc.heightSpinner.getValue(), dc.toroidal.isSelected());
        draw(canvas.getGraphicsContext2D());
    }

    @FXML
    public void handleExitButtonClick() {
        Platform.exit();
    }

    @FXML
    public void handleRandomButtonClick() {
        board.generate();
        draw(canvas.getGraphicsContext2D());
    }

    @FXML
    public void handleClearButtonClick() {
        board.clear();
        draw(canvas.getGraphicsContext2D());
    }

    @FXML
    public void handlePlayButtonClick() {
        running = true;
        service.start();
    }

    @FXML
    public void handleStopButtonClick() {
        running = false;
    }
}
