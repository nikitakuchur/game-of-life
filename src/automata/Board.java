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

    public int getWidth() {
        return cells[0].length;
    }

    public int getHeight() {
        return cells.length;
    }

    public boolean isAlive(int x, int y) {
        x %= getWidth();
        y %= getHeight();

        if (x < 0)
            x += getWidth();
        if (y < 0)
            y += getHeight();

        return cells[y][x] == 1;
    }

    public void generate() {
        Random rand = new Random();

        for (int i = 0; i < cells.length; i++)
            for (int j = 0; j < cells[0].length; j++)
                cells[i][j] = rand.nextInt(2);
    }

    public void clear() {
        for (int i = 0; i < cells.length; i++)
            for (int j = 0; j < cells[0].length; j++)
                cells[i][j] = 0;
    }

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

    public void nextGeneration() {
        int newCells[][] = new int[getHeight()][getWidth()];

        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
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
