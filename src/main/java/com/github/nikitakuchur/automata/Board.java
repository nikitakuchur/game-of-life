package com.github.nikitakuchur.automata;

import java.io.Serializable;
import java.util.Random;

public class Board implements Serializable {

    private static final Random RANDOM = new Random();

    private boolean[][] cells;
    private final boolean toroidal;

    /**
     * Creates a board.
     */
    public Board() {
        cells = new boolean[60][80];
        toroidal = false;
    }

    /**
     * Creates a board.
     *
     * @param w        width
     * @param h        height
     * @param toroidal toroidal or not
     */
    public Board(int w, int h, boolean toroidal) {
        cells = new boolean[h][w];
        this.toroidal = toroidal;
    }

    /**
     * Returns the width of the board.
     */
    public int getWidth() {
        return cells[0].length;
    }

    /**
     * Returns the height of the board.
     */
    public int getHeight() {
        return cells.length;
    }

    private int convertX(int x) {
        x %= getWidth();
        if (x < 0) {
            x += getWidth();
        }
        return x;
    }

    private int convertY(int y) {
        y %= getHeight();
        if (y < 0) {
            y += getHeight();
        }
        return y;
    }

    /**
     * Checks if the cell is alive or not.
     *
     * @param x the x-component
     * @param y the y-component
     * @return true if the cell is alive in the given position and false otherwise
     */
    public boolean isAlive(int x, int y) {
        if (toroidal) {
            x = convertX(x);
            y = convertY(y);

            return cells[y][x];
        }

        if (isOutside(x, y)) {
            return false;
        }

        return cells[y][x];
    }

    /**
     * Kills the cell in the given position.
     *
     * @param x the x-component
     * @param y the y-component
     */
    public void kill(int x, int y) {
        x = convertX(x);
        y = convertY(y);
        cells[y][x] = false;
    }

    /**
     * Revives the cell in the given position.
     *
     * @param x the x-component
     * @param y the y-component
     */
    public void revive(int x, int y) {
        x = convertX(x);
        y = convertY(y);
        cells[y][x] = true;
    }

    /**
     * Generates a random board.
     */
    public void generate() {
        clear();

        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (RANDOM.nextInt(2) == 1) {
                    cells[i][j] = true;
                }
            }
        }
    }

    /**
     * Clears the board.
     */
    public void clear() {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                cells[i][j] = false;
            }
        }
    }

    /**
     * Returns the number of neighbours in the given position.
     *
     * @param x the x-component
     * @param y the y-component
     * @return the number of neighbours
     */
    public int countNeighbours(int x, int y) {
        int count = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 1 && j == 1) continue;
                if (isAlive(x - 1 + i, y - 1 + j)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Updates the board.
     */
    public void nextGeneration() {
        int w = getWidth();
        int h = getHeight();

        boolean[][] newCells = new boolean[h][w];

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (!isAlive(i, j) && countNeighbours(i, j) == 3) {
                    newCells[j][i] = true;
                }

                if (isAlive(i, j)) {
                    newCells[j][i] = countNeighbours(i, j) == 2 || countNeighbours(i, j) == 3;
                }
            }
        }
        cells = newCells;
    }

    /**
     * Returns true if the position is outside the board and false otherwise.
     */
    public boolean isOutside(int x, int y) {
        return x >= getWidth() || x < 0 || y >= getHeight() || y < 0;
    }
}
