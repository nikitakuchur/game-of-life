package automata;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

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

        service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        while (!stopButton.isPressed()) {
                            board.nextGeneration();
                            draw(canvas.getGraphicsContext2D());
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

    // TODO: Fix it
    private void draw(GraphicsContext gc) {
        double size = canvas.getWidth() / board.getWidth();

        if (board.getHeight() * size > canvas.getHeight())
            size = canvas.getHeight() / board.getHeight();

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double xPos = canvas.getWidth() / 2 - board.getWidth() * size / 2;
        double yPos = canvas.getHeight() / 2 - board.getHeight() * size  / 2;

        // Draw the grid
        gc.setStroke(Color.LIGHTGRAY);
        for (int i = 0; i <= board.getWidth(); i++)
            gc.strokeLine(xPos + i * size, yPos, xPos + i * size, yPos + board.getHeight() * size);

        for (int i = 0; i <= board.getHeight(); i++)
            gc.strokeLine(xPos, yPos + i * size, xPos + board.getWidth() * size, yPos + i * size);

        // Draw cells
        gc.setFill(Color.ORANGE);
        for (int i = 0; i < board.getWidth(); i++)
            for (int j = 0; j < board.getHeight(); j++)
                if (board.isAlive(i, j))
                    gc.fillRect(xPos + i * size, yPos + j * size, size, size);
    }

    @FXML
    public void handleRandomButtonClick() {
        board.generate();
        draw(canvas.getGraphicsContext2D());
    }

    public void handleClearButtonClick() {
        board.clear();
        draw(canvas.getGraphicsContext2D());
    }

    public void handlePlayButtonClick() {
        service.start();
    }
}
