package com.github.nikitakuchur.automata;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final String FILE_EXTENSION = ".gol";

    @FXML
    public Pane pane;

    @FXML
    public Canvas canvas;

    @FXML
    public Button randomButton;

    @FXML
    public Button clearButton;

    @FXML
    public Slider speedSlider;

    @FXML
    public Button stepButton;

    @FXML
    public Button playButton;

    @FXML
    public Button stopButton;

    private Board board;
    private Service<Void> service;
    private boolean running;

    private FileChooser fileChooser;
    private File file;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        board = new Board();

        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Game of Life files", "*" + FILE_EXTENSION));

        pane.prefWidthProperty().bind(pane.widthProperty());
        pane.prefHeightProperty().bind(pane.heightProperty());

        pane.prefWidthProperty().addListener((ov, oldValue, newValue) -> {
            canvas.setWidth(newValue.doubleValue());
            draw();
        });

        pane.prefHeightProperty().addListener((ov, oldValue, newValue) -> {
            canvas.setHeight(newValue.doubleValue());
            draw();
        });

        EventHandler<MouseEvent> handler = event -> {
            Point2D boardPosition = getBoardPosition();

            int x = (int) ((event.getX() - boardPosition.getX()) /
                    ((canvas.getWidth() - 2 * boardPosition.getX()) / board.getWidth()));

            int y = (int) ((event.getY() - boardPosition.getY()) /
                    ((canvas.getHeight() - 2 * boardPosition.getY()) / board.getHeight()));

            if (board.isOutside(x, y)) {
                return;
            }

            if (event.getButton() == MouseButton.PRIMARY) {
                board.revive(x, y);
            } else if (event.getButton() == MouseButton.SECONDARY) {
                board.kill(x, y);
            }

            draw();
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
                            Platform.runLater(MainController.this::draw);
                            Thread.sleep((long) (200 - speedSlider.getValue()));
                        }
                        return null;
                    }
                };
            }
        };

        service.setOnRunning(v -> {
            randomButton.setDisable(true);
            clearButton.setDisable(true);
            stepButton.setDisable(true);
            playButton.setDisable(true);
            stopButton.setDisable(false);
        });

        service.setOnSucceeded(v -> {
            randomButton.setDisable(false);
            clearButton.setDisable(false);
            stepButton.setDisable(false);
            playButton.setDisable(false);
            stopButton.setDisable(true);
            service.reset();
        });
    }

    /**
     * Returns the cell size
     */
    private double getCellSize() {
        double wr = canvas.getWidth() / board.getWidth();
        double hr = canvas.getHeight() / board.getHeight();

        return Math.min(wr, hr);
    }

    /**
     * Returns the board position
     */
    private Point2D getBoardPosition() {
        double size = getCellSize();
        double x = canvas.getWidth() / 2 - board.getWidth() * size / 2;
        double y = canvas.getHeight() / 2 - board.getHeight() * size / 2;

        return new Point2D(x, y);
    }

    /**
     * Draws the grid and cells.
     */
    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double size = getCellSize();
        Point2D boardPosition = getBoardPosition();

        // Clear the canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw the grid
        gc.setStroke(Color.LIGHTGRAY);
        for (int i = 0; i <= board.getWidth(); i++) {
            gc.strokeLine(boardPosition.getX() + i * size, boardPosition.getY(),
                    boardPosition.getX() + i * size, boardPosition.getY() + board.getHeight() * size);
        }

        for (int i = 0; i <= board.getHeight(); i++) {
            gc.strokeLine(boardPosition.getX(), boardPosition.getY() + i * size,
                    boardPosition.getX() + board.getWidth() * size, boardPosition.getY() + i * size);
        }

        // Draw cells
        gc.setFill(Color.ORANGE);
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                if (board.isAlive(i, j)) {
                    gc.fillRect(boardPosition.getX() + i * size, boardPosition.getY() + j * size, size, size);
                }
            }
        }
    }

    @FXML
    public void handleNewButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/dialog.fxml"));

        Parent parent = fxmlLoader.load();
        DialogController dc = fxmlLoader.getController();

        Scene scene = new Scene(parent, 250, 150);
        Stage stage = new Stage();
        stage.setTitle("New Board");
        stage.setResizable(false);
        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();

        board = new Board(dc.widthSpinner.getValue(), dc.heightSpinner.getValue(), dc.toroidal.isSelected());
        draw();

        file = null;
        Main.getPrimaryStage().setTitle(Main.TITLE);
    }

    @FXML
    public void handleOpenButtonClick() throws IOException, ClassNotFoundException {
        fileChooser.setTitle("Open File");
        file = fileChooser.showOpenDialog(Main.getPrimaryStage());

        if (file == null) return;

        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file))) {
            board = (Board) objectInputStream.readObject();
            draw();
        }

        Main.getPrimaryStage().setTitle(file + " - " + Main.TITLE);
    }

    @FXML
    public void handleSaveButtonClick() throws IOException {
        if (file == null) {
            handleSaveAsButtonClick();
            return;
        }

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file))) {
            objectOutputStream.writeObject(board);
        }
    }

    @FXML
    public void handleSaveAsButtonClick() throws IOException {
        fileChooser.setTitle("Save File");
        file = fileChooser.showSaveDialog(Main.getPrimaryStage());

        if (file == null) return;
        if (!file.getName().endsWith(FILE_EXTENSION)) {
            String path = file.getAbsolutePath() + FILE_EXTENSION;
            file = new File(path);
        }

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file))) {
            objectOutputStream.writeObject(board);
        }

        Main.getPrimaryStage().setTitle(file + " - " + Main.TITLE);
    }

    @FXML
    public void handleExitButtonClick() {
        Platform.exit();
    }

    @FXML
    public void handleAboutButtonClick() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("About");
        alert.setHeaderText(null);
        alert.setContentText("Conway's Game of Life\nhttps://github.com/nikitakuchur/game-of-life\n" +
                "\nNikita Kuchur\nnikitakuchur@gmail.com");
        alert.getDialogPane().getButtonTypes().add(ButtonType.OK);

        alert.showAndWait();
    }

    @FXML
    public void handleRandomButtonClick() {
        board.generate();
        draw();
    }

    @FXML
    public void handleClearButtonClick() {
        board.clear();
        draw();
    }

    @FXML
    public void handleStepButtonClick() {
        board.nextGeneration();
        draw();
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
