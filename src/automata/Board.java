package automata;

import java.util.Random;

public class Board {

    private int[][] cells;

    public Board() {
        cells = new int[60][80];
    }

    public Board(int w, int h) {
        cells = new int[h][w];
    }

    /**
     * @return the width of the board
     */
    public int getWidth() {
        return cells[0].length;
    }

    /**
     * @return the height of the board
     */
    public int getHeight() {
        return cells.length;
    }

    /**
     * @param x the x-component
     * @param y the y-component
     * @return true if the cell is alive in the given position and false otherwise
     */
    public boolean isAlive(int x, int y) {
        x %= getWidth();
        y %= getHeight();

        if (x < 0)
            x += getWidth();
        if (y < 0)
            y += getHeight();

        return cells[y][x] == 1;
    }

    /**
     * Kills the cell in the given position.
     * @param x the x-component
     * @param y the y-component
     */
    public void kill(int x, int y) {
        cells[y][x] = 0;
    }

    /**
     * Revives the cell in the given position.
     * @param x the x-component
     * @param y the y-component
     */
    public void revive(int x, int y) {
        cells[y][x] = 1;
    }

    /**
     * Generates a random board.
     */
    public void generate() {
        Random rand = new Random();

        for (int i = 0; i < cells.length; i++)
            for (int j = 0; j < cells[0].length; j++)
                cells[i][j] = rand.nextInt(2);
    }

    /**
     * Clears the board.
     */
    public void clear() {
        for (int i = 0; i < cells.length; i++)
            for (int j = 0; j < cells[0].length; j++)
                cells[i][j] = 0;
    }

    /**
     * Returns the number of neighbours in the given position.
     *
     * @param x the x-component
     * @param y the y-component
     * @return the number of neighbours
     */
    public int neighboursCountAt(int x, int y) {
        int count = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 1 && j == 1)
                    continue;
                if (isAlive(x - 1 + i, y - 1 + j))
                    count++;
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

        int newCells[][] = new int[h][w];

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (!isAlive(i, j) && neighboursCountAt(i, j) == 3)
                    newCells[j][i] = 1;

                if (isAlive(i, j)) {
                    if ((neighboursCountAt(i, j) == 2 || neighboursCountAt(i, j) == 3))
                        newCells[j][i] = 1;
                    else
                        newCells[j][i] = 0;
                }
            }
        }

        cells = newCells;
    }
}
