package com.example.Drone_Project;

import java.util.Random;

public class Grid {
    private final int size;
    private final Node[][] grid;
    private final Random rand = new Random();

    public Grid(int size) {
        this.size = size;
        grid = new Node[size][size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                grid[x][y] = new Node(x, y);
            }
        }

       
        int[][] directions = { {0, 1}, {1, 0}, {0, -1}, {-1, 0},
                            {1, 1}, {1, -1}, {-1, 1}, {-1, -1} };
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Node node = grid[x][y];
                for (int[] dir : directions) {
                    int nx = x + dir[0], ny = y + dir[1];
                    if (isInBounds(nx, ny)) {
                        node.neighbors.add(grid[nx][ny]);
                    }
                }
            }
        }
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < size && y < size;
    }

    public Node getNode(int x, int y) {
        return grid[x][y];
    }

    public Node getRandomNode() {
        int x = rand.nextInt(size);
        int y = rand.nextInt(size);
        return grid[x][y];
    }

    public int getSize() {
        return size;
    }
}