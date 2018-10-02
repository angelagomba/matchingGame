import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;

// Represents the Grid of the Board
class Grid extends World {
  
  // the dimensions of the Board
  int width;
  int height;
  
  // a list of the Grid's Cells
  ArrayList<Cell> grid;
  
  // list of revealed Tiles
  ArrayList<Cell> revealed;
  
  // list of Tiles left
  ArrayList<Cell> cellsLeft;
  
  // to check if the game is over
  boolean worldState;
  
  // sets a tile size
  public static int TILESIZE = 30;
  
  // Initial Constructor
  Grid(int width, int height) {
    if ((this.width * this.height) % 2 == 1) {
      throw new IllegalArgumentException("Grid must have an even amount of Cells");
    } else {
      this.width = width;
      this.height = height;
      this.worldState = false;
      this.grid = new ArrayList<Cell>();
      this.revealed = new ArrayList<Cell>();
      this.makeGrid();
      this.assignNum();
      this.cellsLeft = new ArrayList<Cell>(this.grid);
    }
  }
  
  // creates the grid
  public void makeGrid() {

    while (this.grid.size() < (this.width * this.height)) {
      this.grid.add(new Cell());
    }
  }
  
  // assigns a number to each Cell in the Grid
  public void assignNum() {
    
    int numRange = (this.width * this.height) / 2;
    ArrayList<Integer> nums = new ArrayList<Integer>();
    ArrayList<Cell> cells = new ArrayList<Cell>(this.grid);
    
    for (int i = 1; i <= numRange; i++) {
      nums.add(i);
      nums.add(i);
    }
    
    while (cells.size() > 0) {
      int currIndex = new Random().nextInt(cells.size());
      // picks a cell
      Cell chosenCell = cells.get(currIndex);
      // gets the index of the same Cell from the grid
      int newIndex = this.grid.indexOf(chosenCell);
      // assigns the number to the cell
      this.grid.get(newIndex).assignNumHelp(nums.get(0));
      // deletes the cell from the grid copy
      cells.remove(chosenCell);
      // deletes the int from the list of integers
      nums.remove(0);
    }
  } 
  
  // determines how many tiles are revealed
  int howManyRevealed() {
    int count = 0;
    for (Cell c : grid) {
      if (c.isFlipped) {
        count = count + 1;
      }
    } return count;
  }
  
  // handles the user's mouse clicks 
  public void onMouseClicked(Posn pos) {
    Cell currentCell = this.clickedCell(pos.x, pos.y);
    
    if (this.howManyRevealed() % 2 == 0 && !this.isSolved(currentCell)) {
      currentCell.flipCell();
      this.revealed.add(currentCell);
    } else if (!this.isSolved(currentCell)){
      Cell first = this.revealed.get(this.revealed.size() - 1);
      if (currentCell.tileNum == first.tileNum) {
        currentCell.flipCell();
        this.revealed.add(currentCell);
        this.cellsLeft.remove(first);
        this.cellsLeft.remove(currentCell);
      } else {
        currentCell.flipCell();
        int index = this.grid.indexOf(first);
        this.revealed.remove(first);
        currentCell.flipCell();
        this.grid.get(index).flipCell();
      }
    }
  }
  
  // helper function that links the posn to the cell
  public Cell clickedCell(int x, int y) {
    int column = this.howManyTimes(x, 0);
    int row = this.howManyTimes(y, 0);

    return this.grid.get((this.width * row) + column);
  }
  
  // helper function that determines the row or column based on the posn
  public int howManyTimes(int pos, int count) {
    if (pos - 30 < 1) {
      return count;
    } else {
      return this.howManyTimes((pos - 30), count + 1);
    }
  }
  
  // helper function that determines whether or not the cell has been solved
  public boolean isSolved(Cell current) {
    int count = 0;
    for (Cell c : this.cellsLeft) {
      if (c.equals(current)) {
        count = count + 1;
      }
    } return count == 0;
  }
  
  // DRAWING //
  
  // draws a row from the grid
  WorldImage drawRows(int min) {

    // sets the limit to know when the row is completed
    int limit = this.width - 1;
    // set the starting index of the row (to make every row)
    int currIndex = min;
    // the starting tile (first tile of the row)
    WorldImage currRow = this.grid.get(currIndex).drawCell();

    while (limit > 0) {
      // increments the index to draw the remaining tiles
      currIndex = currIndex + 1;
      // adds the second tile to the first tile
      currRow = new BesideImage(currRow, this.grid.get(currIndex).drawCell());
      // changes the limit
      limit = limit - 1;
    }
    return currRow;
  }
  
  // draws the complete grid
  WorldImage drawGrid() {

    // sets the limit to know when the column is completed
    int limit = this.height - 1;
    // the starting column set to 0 (to start with the first row)
    int  currIndex = 0;

    // draws the row between the range  (currIndex - columns - 1)
    WorldImage currGrid = this.drawRows(currIndex);

    while (limit > 0) {
      // increments the index to draw the remaining rows
      currIndex = currIndex + this.width;
      // adds the new row below the top row
      currGrid = new AboveImage(currGrid, this.drawRows(currIndex));
      // changes the limit
      limit = limit - 1;
    }

    return currGrid;
  }
  
  // draws the game scene
  public WorldScene makeScene() {

    WorldScene scene = this.getEmptyScene();  

    WorldImage score = new TextImage("REVEALED = " 
        + Integer.toString(this.revealed.size()), Color.black);

    WorldImage gridWithScore = new AboveImage(this.drawGrid(), score);
    scene.placeImageXY(gridWithScore, this.width * TILESIZE / 2,
        this.height * TILESIZE / 2 + TILESIZE / 5);
    return scene;
  }
  
  //draws the win scene
  public WorldScene winScene() {
    WorldScene scene = this.getEmptyScene();

    // win message
    WorldImage message = new OverlayImage(new TextImage("YOU WIN", Color.black), 
        new RectangleImage(this.width * TILESIZE, TILESIZE / 2 + TILESIZE / 5, 
            OutlineMode.SOLID, Color.GREEN));  

    // reveals the entire grid
    this.revealGrid();
    
    WorldImage gridWithMessage = new AboveImage(this.drawGrid(), message);
    scene.placeImageXY(gridWithMessage, this.width * TILESIZE / 2,
        this.height * TILESIZE / 2 + TILESIZE / 5);


    return scene;
  }
  
  // reveals the entire grid - to draw the loss and win scene
  void revealGrid() {
    for (Cell c : this.grid) {
      if (!c.isFlipped) {
        c.flipCell();
      }
    }
  }
  
  // to return the scene corresponding to whether the player
  // won or is still playing the game
  public WorldEnd worldEnds() {
    if (this.revealed.size() == (this.width * this.height)) {
      return new WorldEnd(true, this.winScene());
    } else {
      return new WorldEnd(false, this.makeScene());
    }
  } 
}

// Represents a Cell of the Grid
class Cell {

  // the displayed number on the tile
  int tileNum;
  boolean isFlipped;
 
  // set a tile size
  public static int TILESIZE = 30;
  
  // Initial Constructor
  Cell() {
    this.isFlipped = false;
  }
  
  // assigns the random number to the cell
  public void assignNumHelp(int num) {
    this.tileNum = num;
  }
  
  // determines whether the tileNums are the same
  boolean isPartner(int num) {
    return this.tileNum == num;
  }
  
  // flips the Cell
  public void flipCell() {
    this.isFlipped = !this.isFlipped;
  }
  
  //draws the cell according to its properties
  WorldImage drawCell() {
    
    RectangleImage blank = new RectangleImage(TILESIZE,
        TILESIZE, OutlineMode.OUTLINE, Color.black);
    RectangleImage tileFill = new RectangleImage(TILESIZE,
        TILESIZE, OutlineMode.SOLID, Color.gray);
    RectangleImage tileOutline = new RectangleImage(TILESIZE,
        TILESIZE, OutlineMode.OUTLINE, Color.BLACK);
    OverlayImage tile = new OverlayImage(tileOutline, tileFill);
    OverlayImage tileNum = new OverlayImage(
        new TextImage(Integer.toString(this.tileNum), Color.black), blank);
    
    if (this.isFlipped) {
      return tileNum;
    } else {
      return tile;
    }
  }
}

//Test the game
class ExamplesGrid {

  public static int TILESIZE = 30;
  Grid game = new Grid(5, 6);


  void testGrid(Tester t) {

    game.bigBang(game.width * TILESIZE, (game.height * TILESIZE) + TILESIZE / 3 + 5, 0.5);
  }
}


