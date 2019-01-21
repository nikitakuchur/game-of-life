# Conway's Game of Life
This is an implementation of Conway's Game Of Life. This is a zero-player game, meaning that its evolution is determined by its initial state, 
requiring no further input. One interacts with the Game of Life by creating an initial configuration and observing how it evolves, or, 
for advanced players, by creating patterns with particular properties.

![Imgur](https://i.imgur.com/FduDJQ6.gif)

### Rules
- Any live cell with fewer than two live neighbours dies, as if caused by underpopulation.
- Any live cell with two or three live neighbours lives on to the next generation.
- Any live cell with more than three live neighbours dies, as if by overpopulation.
- Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
