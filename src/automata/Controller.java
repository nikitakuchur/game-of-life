package automata;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public Button randomButton;

    @FXML
    public Button clearButton;

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

        canvas.setOnMouseDragged(event -> {
            Point2D boardPosition = getBoardPosition();

            int x = (int) ((event.getX() - boardPosition.getX()) /
                          ((canvas.getWidth() - 2 * boardPosition.getX()) / board.getWidth()));

            int y = (int) ((event.getY() - boardPosition.getY()) /
                          ((canvas.getHeight() - 2 * boardPosition.getY()) / board.getHeight()));

            if (event.getButton() == MouseButton.PRIMARY)
                board.revive(x, y);
            else if (event.getButton() == MouseButton.SECONDARY)
                board.kill(x, y);

            draw(canvas.getGraphicsContext2D());
        });

        service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        while (!stopButton.isPressed()) {
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

    // TODO: Fix it
    private void draw(GraphicsContext gc) {
        double size = getCellSize();
        Point2D boardPosition = getBoardPosition();

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
        service.start();
    }
}
