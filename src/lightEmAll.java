import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

class LightEmAll extends World {
  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all nodes
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree
  ArrayList<Edge> mst;
  // the width and height of the board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;

  // constructor for generating the game.
  LightEmAll(int width, int height, Random rand) {
    //this.board = new Utils().createBoard(height, width);
    this.board = new Utils().emptyBoard(width, height);
    new Utils().setRowsAndCols(this.board);
    ArrayList<Edge> allEdges = new Utils().generateEdges(this.board, rand); 
    this.mst = new Utils().kruskals(allEdges, this.board);
    new Utils().createWires(this.mst, this.board);
    new Utils().scrambleBoard(this.board, rand);
    this.nodes = new Utils().getAllNodes(this.board);
    this.width = width;
    this.height = height;
    this.powerRow = width / 2;
    this.powerCol = height / 2;
    this.board.get(powerRow).get(powerCol).negatePowerStation();
    // this.radius = radius;
    new Utils().updatePower(this.board, this.powerRow, this.powerCol, new ArrayList<GamePiece>(),
        new ArrayList<GamePiece>());
  }

  // constructor for testing
  LightEmAll(ArrayList<ArrayList<GamePiece>> board) {
    this.board = board;
    new Utils().setRowsAndCols(this.board);
    this.width = board.get(0).size();
    this.height = board.size();

  }

  // makes the worldscene for the game
  public WorldScene makeScene() {
    return new Utils().drawBoard(this.board, getEmptyScene());
  }

  // controlls onMouseClicked inputs
  public void onMouseClicked(Posn pos) {
    if ((pos.x > 80 && pos.x < 80 + 40 * this.board.get(0).size())
        && (pos.y > 80 && pos.y < 80 + 40 * this.board.size())) {
      int row = (int) (pos.y - 80) / 40;
      int col = (int) (pos.x - 80) / 40;
      if (!new Utils().allPowered(this.board)) {
        this.board.get(row).get(col).rotatePiece();
        // this.board.get(row).get(col).checkPower(this.board);
      }
      for (GamePiece gp : this.nodes) {
        if (!gp.checkPowerStation()) {
          gp.unPower();
        }
      }
      new Utils().updatePower(this.board, this.powerRow, this.powerCol, new ArrayList<GamePiece>(),
          new ArrayList<GamePiece>());
    }
  }

  // controlls onKey inputs
  public void onKeyEvent(String key) {
    for (GamePiece gp : this.nodes) {
      if (!gp.checkPowerStation()) {
        gp.unPower();
      }
    }
    if (key.equals("up")) {
      if (this.powerRow != 0 && this.board.get(this.powerRow - 1).get(this.powerCol).canGo(key)
          && this.board.get(this.powerRow).get(this.powerCol).canGo("down")) {
        this.board.get(this.powerRow).get(this.powerCol).negatePowerStation();
        this.board.get(this.powerRow - 1).get(this.powerCol).negatePowerStation();
        this.powerRow -= 1;
      }
    }
    else if (key.equals("down")) {
      if (this.powerRow != this.height - 1
          && this.board.get(this.powerRow + 1).get(this.powerCol).canGo(key)
          && this.board.get(this.powerRow).get(this.powerCol).canGo("up")) {
        this.board.get(this.powerRow).get(this.powerCol).negatePowerStation();
        this.board.get(this.powerRow + 1).get(this.powerCol).negatePowerStation();
        this.powerRow += 1;
      }
    }
    else if (key.equals("left")) {
      if (this.powerCol != 0 && this.board.get(this.powerRow).get(this.powerCol - 1).canGo(key)
          && this.board.get(this.powerRow).get(this.powerCol).canGo("right")) {
        this.board.get(this.powerRow).get(this.powerCol).negatePowerStation();
        this.board.get(this.powerRow).get(this.powerCol - 1).negatePowerStation();
        this.powerCol -= 1;
      }
    }
    else if (key.equals("right")) {
      if (this.powerCol != this.width - 1
          && this.board.get(this.powerRow).get(this.powerCol + 1).canGo(key)
          && this.board.get(this.powerRow).get(this.powerCol).canGo("left")) {
        this.board.get(this.powerRow).get(this.powerCol).negatePowerStation();
        this.board.get(this.powerRow).get(this.powerCol + 1).negatePowerStation();
        this.powerCol += 1;
      }
    }
    new Utils().updatePower(this.board, this.powerRow, this.powerCol, new ArrayList<GamePiece>(),
        new ArrayList<GamePiece>());
  }
}

// Utils class
class Utils {

  // draws the board
  public WorldScene drawBoard(ArrayList<ArrayList<GamePiece>> board, WorldScene world) {
    if (new Utils().allPowered(board)) {
      world.placeImageXY(
          new OverlayImage(new TextImage("You Win!", 8 * board.size(), Color.YELLOW),
              new RectangleImage(40 * board.get(0).size(), 40 * board.size(), OutlineMode.SOLID,
                  Color.CYAN)),
          100 + ((board.get(0).size() - 1) * 20), 100 + ((board.size() - 1) * 20));
    }
    else {
      for (int i = 0; i < board.size(); i++) {
        for (int j = 0; j < board.get(0).size(); j++) {
          world.placeImageXY(board.get(i).get(j).drawGamePiece(), 100 + j * 40, 100 + i * 40);
        }
      }
    }
    return world;
  }


  // initialize an empty board with the rows and cols 
  public ArrayList<ArrayList<GamePiece>> emptyBoard(int rows, int cols) {
    ArrayList<ArrayList<GamePiece>> board = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      ArrayList<GamePiece> row = new ArrayList<GamePiece>();
      for (int j = 0; j < cols; j++) {
        row.add(new GamePiece(false, false, false, false, false, false));
      }
      board.add(row);
    }
    return board;
  }

  // given an empty board, make all of the gamePieces have connecting edges with random weights
  // returns a sorted list of edges
  public ArrayList<Edge> generateEdges(ArrayList<ArrayList<GamePiece>> board, Random rand) {
    ArrayList<Edge> result = new ArrayList<Edge>();
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.get(0).size(); j++) {

        // adds horizontal edges
        if (j < board.get(0).size() - 1) {
          result.add(new Edge(board.get(i).get(j), board.get(i).get(j + 1), rand.nextInt(100)));
        }

        // adds vertical edges
        if (i < board.size() - 1) {
          result.add(new Edge(board.get(i).get(j), board.get(i + 1).get(j), rand.nextInt(100)));
        }
      }
    }
    result.sort(new CompareEdgeWeight());
    return result;
  }

  // finds the top-end representitive for a given gamepiece
  public GamePiece find(HashMap<GamePiece, GamePiece> reps, GamePiece gp) {
    //ok to check equality using == as we are trying to see if they are
    // the same object in memory
    if (reps.get(gp) == gp) {
      return gp;
    }
    else {
      return this.find(reps, reps.get(gp));
    }
  }

  // unions the two representitives within the hashmap of all reps
  public void union(HashMap<GamePiece, GamePiece> reps, GamePiece rep1, GamePiece rep2) {
    reps.put(rep2, rep1);
  }

  // creates an MST from a sorted list of all possible edges
  public ArrayList<Edge> kruskals(ArrayList<Edge> allEdges, ArrayList<ArrayList<GamePiece>> board) {
    // sets up the hashmap for every gamepiece in the board that maps to itself
    HashMap<GamePiece, GamePiece> representatives = new HashMap<GamePiece, GamePiece>();
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.get(0).size(); j++) {
        representatives.put(board.get(i).get(j), board.get(i).get(j));
      }
    }

    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    while (allEdges.size() > 1) {
      Edge temp = allEdges.get(0);
      if (this.find(representatives, temp.fromNode) == this.find(representatives, temp.toNode)) {
        allEdges.remove(0);
      } else {
        edgesInTree.add(temp);
        this.union(representatives, 
            this.find(representatives, temp.fromNode),
            this.find(representatives, temp.toNode));
      }
    }
    return edgesInTree;
  }

  //draws all the wires from an MST onto the board
  public void createWires(ArrayList<Edge> edges, ArrayList<ArrayList<GamePiece>> board) {
    for (Edge e: edges) {
      e.changeConnections(board);
    }
  }

  // checks if all the gamePieces on the board are powered
  public boolean allPowered(ArrayList<ArrayList<GamePiece>> board) {
    boolean allPowered = true;
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.get(0).size(); j++) {
        if (!board.get(i).get(j).powered) {
          allPowered = false;
        }
      }

    }
    return allPowered;
  }

  // updates the power on the board
  public void updatePower(ArrayList<ArrayList<GamePiece>> board, int powerRow, int powerCol,
      ArrayList<GamePiece> found, ArrayList<GamePiece> workList) {
    GamePiece gp = board.get(powerRow).get(powerCol);
    workList.add(gp);
    // gp.updatePowerHelp(workList, found, board);
    while (workList.size() > 0) {
      workList.remove(0).updatePowerHelp(workList, found, board);
    }
  }

  // creates the board for part 1 of the assignment depending on the rows/cols
  public ArrayList<ArrayList<GamePiece>> createBoard(int rows, int cols) {
    ArrayList<ArrayList<GamePiece>> board = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      ArrayList<GamePiece> row = new ArrayList<GamePiece>();
      for (int j = 0; j < cols; j++) {
        if (i != cols / 2) { // straight lines
          row.add(new GamePiece(false, false, true, true, false, false));
        }
        else {
          if (j == rows / 2) { // adds the power source
            row.add(new GamePiece(true, true, true, true, true, true));
          }
          else if (j == cols - 1) { // end pieces on the middle row
            row.add(new GamePiece(true, false, true, true, false, false));
          }
          else if (j == 0) { // end pieces on the middle row
            row.add(new GamePiece(false, true, true, true, false, false));
          }
          else { // middle row non end or power source pieces
            row.add(new GamePiece(true, true, true, true, false, false));
          }
        }
      }
      board.add(row);
    }
    return board;
  }

  // sets the rows and cols of each gamepiece in the given board
  public void setRowsAndCols(ArrayList<ArrayList<GamePiece>> board) {
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.get(0).size(); j++) {
        board.get(i).get(j).changeRowsAndCols(i, j);
      }
    }
  }

  // gets all nodes from a 2d array and converts it to a single arraylist
  public ArrayList<GamePiece> getAllNodes(ArrayList<ArrayList<GamePiece>> board) {
    ArrayList<GamePiece> result = new ArrayList<GamePiece>();
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.get(0).size(); j++) {
        result.add(board.get(i).get(j));
      }
    }
    return result;
  }

  // rotates each gamepiece on the board a random amount of times
  public void scrambleBoard(ArrayList<ArrayList<GamePiece>> board, Random rand) {
    for (ArrayList<GamePiece> arr : board) {
      for (GamePiece piece : arr) {
        piece.scramble(rand);
      }
    }
  }

}

// represents each gamepiece in the game
class GamePiece {
  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  boolean powerStation;
  boolean powered;

  // constructor for the gamepiece with rows/cols
  GamePiece(int row, int col, boolean left, boolean right, boolean top, boolean bottom,
      boolean powerStation, boolean powered) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = powerStation;
    this.powered = powered;
  }

  // sets row and col to 0 as the assignments are done in
  // the constructor of the game
  GamePiece(boolean left, boolean right, boolean top, boolean bottom, boolean powerStation,
      boolean powered) {
    this.row = 0;
    this.col = 0;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = powerStation;
    this.powered = powered;
  }

  // draws this gamepiece
  public WorldImage drawGamePiece() {
    WorldImage base = new RectangleImage(40, 40, OutlineMode.SOLID, Color.DARK_GRAY);
    base = new OverlayImage(new RectangleImage(40, 40, OutlineMode.OUTLINE, Color.black), base);
    Color powerColor = Color.gray;
    if (this.powered) {
      powerColor = Color.YELLOW;
    }

    if (this.left) {
      base = new OverlayOffsetImage(new RectangleImage(20, 5, OutlineMode.SOLID, powerColor), 10, 0,
          base);
    }

    if (this.right) {
      base = new OverlayOffsetImage(new RectangleImage(20, 5, OutlineMode.SOLID, powerColor), -10,
          0, base);
    }

    if (this.top) {
      base = new OverlayOffsetImage(new RectangleImage(5, 20, OutlineMode.SOLID, powerColor), 0, 10,
          base);
    }

    if (this.bottom) {
      base = new OverlayOffsetImage(new RectangleImage(5, 20, OutlineMode.SOLID, powerColor), 0,
          -10, base);
    }
    if (this.powerStation) {
      StarImage star = new StarImage(15, OutlineMode.SOLID, Color.green);
      OverlayImage starWithOutline = new OverlayImage(
          new StarImage(15, OutlineMode.OUTLINE, Color.blue), star);
      base = new OverlayOffsetImage(starWithOutline, 1, 0, base);
    }
    return base;
  }

  // changes the rows and cols of this gamepiece to the given rows/cols
  public void changeRowsAndCols(int row, int col) {
    this.row = row;
    this.col = col;
  }

  // rotates this piece
  public void rotatePiece() {
    boolean originalRight = this.right;
    this.right = this.bottom;
    this.bottom = this.left;
    this.left = this.top;
    this.top = originalRight;
  }

  // scrambles this piece by rotating it a random amount of times
  public void scramble(Random rand) {
    int numRotations = rand.nextInt(3);
    while (numRotations > 0) {
      this.rotatePiece();
      numRotations -= 1;
    }
  }

  // negates this gamePiece's powerStation field
  public void negatePowerStation() {
    this.powerStation = !this.powerStation;
  }

  // determines if this tile can go to the given direction
  public boolean canGo(String directionFrom) {
    if (directionFrom.equals("up") && this.bottom) {
      // this.powered = true;
      return this.bottom;
    }
    if (directionFrom.equals("down") && this.top) {
      // this.powered = true;
      return this.top;
    }
    if (directionFrom.equals("left") && this.right) {
      // this.powered = true;
      return this.right;
    }
    if (directionFrom.equals("right") && this.left) {
      // this.powered = true;
      return this.left;
    }
    else {
      return false;
    }
  }

  // determines if this gamePiece is a power station
  public boolean checkPowerStation() {
    return this.powerStation;
  }

  // unpowers this gamePiece
  public void unPower() {
    this.powered = false;
  }

  // checks each direction and adds neighbor to worklist
  public void updatePowerHelp(ArrayList<GamePiece> workList, ArrayList<GamePiece> found,
      ArrayList<ArrayList<GamePiece>> board) {

    if (!found.contains(this)) {
      this.powered = true;
      found.add(this);
      if (this.top && this.row != 0) {
        if (board.get(this.row - 1).get(this.col).canGo("up")) {
          workList.add(board.get(this.row - 1).get(this.col));

        }
      }
      if (this.left && this.col != 0) {
        if (board.get(this.row).get(this.col - 1).canGo("left")) {
          workList.add(board.get(this.row).get(this.col - 1));

        }
      }
      if (this.bottom && this.row != board.size() - 1) {
        if (board.get(this.row + 1).get(this.col).canGo("down")) {
          workList.add(board.get(this.row + 1).get(this.col));

        }
      }
      if (this.right && this.col != board.get(0).size() - 1) {
        if (board.get(this.row).get(this.col + 1).canGo("right")) {
          workList.add(board.get(this.row).get(this.col + 1));
        }
      }
    }
  }

  // sets the connections given this gamepiece and that gamepiece
  public void changeConnections(GamePiece that, ArrayList<ArrayList<GamePiece>> board) {
    if (this.row - that.row == 1) {
      board.get(this.row).get(this.col).top = true;
      if (that.row != board.size()) {
        board.get(that.row).get(that.col).bottom = true;
      }
    }
    if (this.row - that.row == -1) {
      board.get(this.row).get(this.col).bottom = true;
      if (that.row != 0) {
        board.get(that.row).get(that.col).top = true;
      }
    }
    if (this.col - that.col == 1) {
      board.get(this.row).get(this.col).left = true;
      if (that.col != board.get(0).size()) {
        board.get(that.row).get(that.col).right = true;
      }

    }
    if (this.col - that.col == -1) {
      board.get(this.row).get(this.col).right = true;
      if (that.col != 0) {
        board.get(that.row).get(that.col).left = true;
      }
    }
  }
}

// represents an Edge (used in part 2)
class Edge {
  GamePiece fromNode;
  GamePiece toNode;
  int weight;

  // constructor for the edge class
  Edge(GamePiece fromNode, GamePiece toNode, int weight) {
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.weight = weight;
  }

  // changes the connections of the from and to nodes
  public void changeConnections(ArrayList<ArrayList<GamePiece>> board) {
    this.fromNode.changeConnections(this.toNode, board);
  }
}

//compares edges by weights
class CompareEdgeWeight implements Comparator<Edge> {

  // compares the given edges by weights
  public int compare(Edge e1, Edge e2) {
    return e1.weight - e2.weight;
  }

}

// examples class
class ExamplesLight {

  // GamePiece(boolean left, boolean right, boolean top,
  // boolean bottom, boolean powerStation, boolean powered) {

  // * unpowered connectors

  // unpowered single connections
  GamePiece topUnpowered;
  GamePiece bottomUnpowered;
  GamePiece leftUnpowered;
  GamePiece rightUnpowered;

  // unpowered double conections
  GamePiece leftRightUnpowered;
  GamePiece topBottomUnpowered;

  // unpowered triple connections
  GamePiece noTopUnpowered;
  GamePiece noBottomUnpowered;
  GamePiece noLeftUnpowered;
  GamePiece noRightUnpowered;

  // unpowered quad connection
  GamePiece allDirectionsUnpowered;

  // * powered connectors

  // unpowered single connections
  GamePiece topPowered;
  GamePiece bottomPowered;
  GamePiece leftPowered;
  GamePiece rightPowered;

  // unpowered double conections
  GamePiece leftRightPowered;
  GamePiece topBottomPowered;

  // unpowered triple connections
  GamePiece noTopPowered;
  GamePiece noBottomPowered;
  GamePiece noLeftPowered;
  GamePiece noRightPowered;

  // unpowered quad connection
  GamePiece allDirectionsPowered;

  GamePiece allDirectionsPowerStation;

  WorldImage baseNoBorder;
  WorldImage baseBorder;
  WorldImage blankCell;
  WorldImage TBLRCell;
  WorldImage TBLRPowerStation;
  WorldImage LRCell;
  WorldImage LCell;
  WorldImage LRTCell;

  void initialConditions() {
    // * unpowered connectors

    // unpowered single connections
    topUnpowered = new GamePiece(false, false, true, false, false, false);
    bottomUnpowered = new GamePiece(false, false, false, true, false, false);
    leftUnpowered = new GamePiece(true, false, false, false, false, false);
    rightUnpowered = new GamePiece(false, true, false, false, false, false);

    // unpowered double conections
    leftRightUnpowered = new GamePiece(true, true, false, false, false, false);
    topBottomUnpowered = new GamePiece(false, false, true, true, false, false);

    // unpowered triple connections
    noTopUnpowered = new GamePiece(true, true, false, true, false, false);
    noBottomUnpowered = new GamePiece(true, true, true, false, false, false);
    noLeftUnpowered = new GamePiece(false, true, true, true, false, false);
    noRightUnpowered = new GamePiece(true, false, true, true, false, false);

    // unpowered quad connection
    allDirectionsUnpowered = new GamePiece(true, true, true, true, false, false);

    // * powered connectors

    // unpowered single connections
    topPowered = new GamePiece(false, false, true, false, false, true);
    bottomPowered = new GamePiece(false, false, true, false, false, true);
    leftPowered = new GamePiece(false, false, true, false, false, true);
    rightPowered = new GamePiece(false, false, true, false, false, true);

    // unpowered double conections
    leftRightPowered = new GamePiece(true, true, false, false, false, true);
    topBottomPowered = new GamePiece(false, false, true, true, false, true);

    // unpowered triple connections
    noTopPowered = new GamePiece(true, true, false, true, false, true);
    noBottomPowered = new GamePiece(true, true, true, false, false, true);
    noLeftPowered = new GamePiece(false, true, true, true, false, true);
    noRightPowered = new GamePiece(true, false, true, true, false, true);

    // unpowered quad connection
    allDirectionsPowered = new GamePiece(true, true, true, true, false, true);

    allDirectionsPowerStation = new GamePiece(true, true, true, true, true, true);

    l2 = new LightEmAll(3, 3, new Random(2));

    workList = new ArrayList<GamePiece>();
    found = new ArrayList<GamePiece>();

    singleTile = new LightEmAll(new ArrayList<ArrayList<GamePiece>>(
        Arrays.asList(new ArrayList<GamePiece>(Arrays.asList(this.allDirectionsUnpowered)))));

    goingUp = new LightEmAll(new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
        new ArrayList<GamePiece>(
            Arrays.asList(new GamePiece(false, false, false, true, false, false))),
        new ArrayList<GamePiece>(
            Arrays.asList(new GamePiece(true, true, true, true, false, false))))));

    goingDown = new LightEmAll(new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
        new ArrayList<GamePiece>(
            Arrays.asList(new GamePiece(true, true, true, true, false, false))),
        new ArrayList<GamePiece>(
            Arrays.asList(new GamePiece(false, false, true, false, false, false))))));

    goingRight = new LightEmAll(new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
        new ArrayList<GamePiece>(Arrays.asList(new GamePiece(true, true, true, true, false, false),
            new GamePiece(true, false, false, false, false, false))))));

    goingLeft = new LightEmAll(
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(new ArrayList<GamePiece>(
            Arrays.asList(new GamePiece(false, true, false, false, false, false),
                new GamePiece(true, true, true, true, false, false))))));
  }


  void drawConditions() {
    baseNoBorder = new RectangleImage(40, 40, OutlineMode.SOLID, Color.DARK_GRAY);
    baseBorder = new OverlayImage(new RectangleImage(40, 40, OutlineMode.OUTLINE, Color.black),
        this.baseNoBorder);
    LCell = new OverlayOffsetImage(new RectangleImage(20, 5, OutlineMode.SOLID, Color.YELLOW), 10,
        0, this.baseBorder);
    LRCell = new OverlayOffsetImage(new RectangleImage(20, 5, OutlineMode.SOLID, Color.YELLOW), -10,
        0, this.baseBorder);
    LRTCell = new OverlayOffsetImage(new RectangleImage(5, 20, OutlineMode.SOLID, Color.YELLOW), 0,
        10, this.baseBorder);
    TBLRCell = new OverlayOffsetImage(new RectangleImage(5, 20, OutlineMode.SOLID, Color.YELLOW), 0,
        -10, this.baseBorder);
    StarImage star = new StarImage(15, OutlineMode.SOLID, Color.green);
    OverlayImage starWithOutline = new OverlayImage(
        new StarImage(15, OutlineMode.OUTLINE, Color.blue), star);
    TBLRPowerStation = new OverlayOffsetImage(starWithOutline, 1, 0, this.baseBorder);
  }

  LightEmAll singleTile;
  LightEmAll goingUp;
  LightEmAll goingLeft;
  LightEmAll goingRight;
  LightEmAll goingDown;

  GamePiece TBpiece = new GamePiece(false, false, true, true, false, false);
  GamePiece TBLpiece = new GamePiece(true, false, true, true, false, false);
  GamePiece TBRpiece = new GamePiece(false, true, true, true, false, false);
  GamePiece TBLRpiece = new GamePiece(true, true, true, true, false, false);
  GamePiece TBLRpowerStationpiece = new GamePiece(true, true, true, true, true, true);
  GamePiece Tpiece = new GamePiece(false, false, true, false, false, false);

  ArrayList<GamePiece> TBrow = new ArrayList<GamePiece>(Arrays.asList(this.TBpiece, this.TBpiece,
      this.TBpiece, this.TBpiece, this.TBpiece, this.TBpiece, this.TBpiece, this.TBpiece));
  ArrayList<GamePiece> middleRow = new ArrayList<GamePiece>(
      Arrays.asList(this.TBRpiece, this.TBLRpiece, this.TBLRpiece, this.TBLRpiece,
          this.TBLRpowerStationpiece, this.TBLRpiece, this.TBLRpiece, this.TBLpiece));
  ArrayList<GamePiece> middleRowPrac = new ArrayList<GamePiece>(
      Arrays.asList(this.Tpiece, this.Tpiece, this.Tpiece, this.TBLRpiece,
          this.TBLRpowerStationpiece, this.TBLpiece, this.TBLpiece, this.TBLpiece));

  ArrayList<ArrayList<GamePiece>> board1 = new ArrayList<ArrayList<GamePiece>>(
      Arrays.asList(this.TBrow, this.TBrow, this.TBrow, this.TBrow, this.middleRowPrac, this.TBrow,
          this.TBrow, this.TBrow, this.TBrow));

  LightEmAll l1 = new LightEmAll(8, 9, new Random());
  LightEmAll l2;
  LightEmAll l3 = new LightEmAll(1, 1, new Random());
  LightEmAll l4 = new LightEmAll(6,6, new Random());

  ArrayList<GamePiece> l2row1 = new ArrayList<GamePiece>(
      Arrays.asList(new GamePiece(0, 0, false, false, true, true, false, false),
          new GamePiece(0, 1, false, false, true, true, false, false),
          new GamePiece(0, 2, false, false, true, true, false, false)));
  ArrayList<GamePiece> l2row2 = new ArrayList<GamePiece>(
      Arrays.asList(new GamePiece(1, 0, false, true, true, true, false, false),
          new GamePiece(1, 1, true, true, true, true, true, true),
          new GamePiece(1, 2, true, false, true, true, false, false)));
  ArrayList<GamePiece> l2row3 = new ArrayList<GamePiece>(
      Arrays.asList(new GamePiece(2, 0, false, false, true, true, false, false),
          new GamePiece(2, 1, false, false, true, true, false, false),
          new GamePiece(2, 2, false, false, true, true, false, false)));
  ArrayList<GamePiece> l2row1NoCord = new ArrayList<GamePiece>(
      Arrays.asList(new GamePiece(false, false, true, true, false, false),
          new GamePiece(false, false, true, true, false, false),
          new GamePiece(false, false, true, true, false, false)));
  ArrayList<GamePiece> l2row2NoCord = new ArrayList<GamePiece>(
      Arrays.asList(new GamePiece(false, true, true, true, false, false),
          new GamePiece(true, true, true, true, true, true),
          new GamePiece(true, false, true, true, false, false)));
  ArrayList<GamePiece> l2row3NoCord = new ArrayList<GamePiece>(
      Arrays.asList(new GamePiece(false, false, true, true, false, false),
          new GamePiece(false, false, true, true, false, false),
          new GamePiece(false, false, true, true, false, false)));
  ArrayList<ArrayList<GamePiece>> l2rows123 = new ArrayList<ArrayList<GamePiece>>(
      Arrays.asList(this.l2row1, this.l2row2, this.l2row3));
  ArrayList<ArrayList<GamePiece>> l2rows123NoCord = new ArrayList<ArrayList<GamePiece>>(
      Arrays.asList(this.l2row1NoCord, this.l2row2NoCord, this.l2row3NoCord));
  ArrayList<ArrayList<GamePiece>> onlyOne = new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
      new ArrayList<GamePiece>(Arrays.asList(new GamePiece(true, true, true, true, true, true)))));
  LightEmAll onlyOneL = new LightEmAll(this.onlyOne);

  ArrayList<GamePiece> workList;
  ArrayList<GamePiece> found;


  // testing changeRowsAndCols
  void testChangeRowsAndCols(Tester t) {
    this.initialConditions();

    // testing on tile multiple times
    t.checkExpect(this.topUnpowered.row, 0);
    t.checkExpect(this.topUnpowered.col, 0);

    this.topUnpowered.changeRowsAndCols(50, 75);

    t.checkExpect(this.topUnpowered.row, 50);
    t.checkExpect(this.topUnpowered.col, 75);

    this.topUnpowered.changeRowsAndCols(20, 5);

    t.checkExpect(this.topUnpowered.row, 20);
    t.checkExpect(this.topUnpowered.col, 5);

  }

  // testing rotatePiece
  void testRotatePiece(Tester t) {
    this.initialConditions();

    // ***********************
    // TESTING ON SINGLE PIECE
    // ***********************

    // testing the initial conditions of the piece
    t.checkExpect(this.topUnpowered.top, true);
    t.checkExpect(this.topUnpowered.bottom, false);
    t.checkExpect(this.topUnpowered.left, false);
    t.checkExpect(this.topUnpowered.right, false);

    // rotating and checking new result
    this.topUnpowered.rotatePiece();
    t.checkExpect(this.topUnpowered.top, false);
    t.checkExpect(this.topUnpowered.bottom, false);
    t.checkExpect(this.topUnpowered.left, true);
    t.checkExpect(this.topUnpowered.right, false);

    // rotating and checking new result
    this.topUnpowered.rotatePiece();
    t.checkExpect(this.topUnpowered.top, false);
    t.checkExpect(this.topUnpowered.bottom, true);
    t.checkExpect(this.topUnpowered.left, false);
    t.checkExpect(this.topUnpowered.right, false);

    // rotating and checking new result
    this.topUnpowered.rotatePiece();
    t.checkExpect(this.topUnpowered.top, false);
    t.checkExpect(this.topUnpowered.bottom, false);
    t.checkExpect(this.topUnpowered.left, false);
    t.checkExpect(this.topUnpowered.right, true);

    // ***********************
    // TESTING ON DOUBLE PIECE
    // ***********************

    t.checkExpect(this.leftRightUnpowered.top, false);
    t.checkExpect(this.leftRightUnpowered.bottom, false);
    t.checkExpect(this.leftRightUnpowered.left, true);
    t.checkExpect(this.leftRightUnpowered.right, true);

    // rotating and checking new result
    this.leftRightUnpowered.rotatePiece();
    t.checkExpect(this.leftRightUnpowered.top, true);
    t.checkExpect(this.leftRightUnpowered.bottom, true);
    t.checkExpect(this.leftRightUnpowered.left, false);
    t.checkExpect(this.leftRightUnpowered.right, false);

    // rotating and checking new result
    this.leftRightUnpowered.rotatePiece();
    t.checkExpect(this.leftRightUnpowered.top, false);
    t.checkExpect(this.leftRightUnpowered.bottom, false);
    t.checkExpect(this.leftRightUnpowered.left, true);
    t.checkExpect(this.leftRightUnpowered.right, true);

    // rotating and checking new result
    this.leftRightUnpowered.rotatePiece();
    t.checkExpect(this.leftRightUnpowered.top, true);
    t.checkExpect(this.leftRightUnpowered.bottom, true);
    t.checkExpect(this.leftRightUnpowered.left, false);
    t.checkExpect(this.leftRightUnpowered.right, false);

    // ***********************
    // TESTING ON TRIPLE PIECE
    // ***********************

    // testting initial
    t.checkExpect(this.noTopUnpowered.top, false);
    t.checkExpect(this.noTopUnpowered.bottom, true);
    t.checkExpect(this.noTopUnpowered.left, true);
    t.checkExpect(this.noTopUnpowered.right, true);

    // rotating and checking new result
    this.noTopUnpowered.rotatePiece();
    t.checkExpect(this.noTopUnpowered.top, true);
    t.checkExpect(this.noTopUnpowered.bottom, true);
    t.checkExpect(this.noTopUnpowered.left, false);
    t.checkExpect(this.noTopUnpowered.right, true);

    // rotating and checking new result
    this.noTopUnpowered.rotatePiece();
    t.checkExpect(this.noTopUnpowered.top, true);
    t.checkExpect(this.noTopUnpowered.bottom, false);
    t.checkExpect(this.noTopUnpowered.left, true);
    t.checkExpect(this.noTopUnpowered.right, true);

    // rotating and checking new result
    this.noTopUnpowered.rotatePiece();
    t.checkExpect(this.noTopUnpowered.top, true);
    t.checkExpect(this.noTopUnpowered.bottom, true);
    t.checkExpect(this.noTopUnpowered.left, true);
    t.checkExpect(this.noTopUnpowered.right, false);

    // ***********************
    // TESTING ON Quad PIECE
    // ***********************

    t.checkExpect(this.allDirectionsUnpowered.top, true);
    t.checkExpect(this.allDirectionsUnpowered.bottom, true);
    t.checkExpect(this.allDirectionsUnpowered.left, true);
    t.checkExpect(this.allDirectionsUnpowered.right, true);

    // rotating and checking new result
    this.allDirectionsUnpowered.rotatePiece();
    t.checkExpect(this.allDirectionsUnpowered.top, true);
    t.checkExpect(this.allDirectionsUnpowered.bottom, true);
    t.checkExpect(this.allDirectionsUnpowered.left, true);
    t.checkExpect(this.allDirectionsUnpowered.right, true);

  }

  void testScramble(Tester t) {
    this.initialConditions();

    // should rotate the piece twice
    this.topUnpowered.scramble(new Random(25));
    t.checkExpect(this.topUnpowered.top, false);
    t.checkExpect(this.topUnpowered.bottom, true);
    t.checkExpect(this.topUnpowered.left, false);
    t.checkExpect(this.topUnpowered.right, false);

    // rotates it twice again
    this.topUnpowered.scramble(new Random(25));
    t.checkExpect(this.topUnpowered.top, true);
    t.checkExpect(this.topUnpowered.bottom, false);
    t.checkExpect(this.topUnpowered.left, false);
    t.checkExpect(this.topUnpowered.right, false);

  }

  void testScrambleBoard(Tester t) {
    this.initialConditions();
    t.checkExpect(this.l3.board.get(0).get(0).top, false);
    t.checkExpect(this.l3.board.get(0).get(0).bottom, false);
    t.checkExpect(this.l3.board.get(0).get(0).left, false);
    t.checkExpect(this.l3.board.get(0).get(0).right, false);
    new Utils().scrambleBoard(this.l3.board, new Random(25));
    t.checkExpect(this.l3.board.get(0).get(0).top, false);
    t.checkExpect(this.l3.board.get(0).get(0).bottom, false);
    t.checkExpect(this.l3.board.get(0).get(0).left, false);
    t.checkExpect(this.l3.board.get(0).get(0).right, false);

    t.checkExpect(this.goingDown.board.get(0).get(0).top, true);
    t.checkExpect(this.goingDown.board.get(0).get(0).bottom, true);
    t.checkExpect(this.goingDown.board.get(0).get(0).left, true);
    t.checkExpect(this.goingDown.board.get(0).get(0).right, true);
    t.checkExpect(this.goingDown.board.get(1).get(0).top, true);
    t.checkExpect(this.goingDown.board.get(1).get(0).bottom, false);
    t.checkExpect(this.goingDown.board.get(1).get(0).left, false);
    t.checkExpect(this.goingDown.board.get(1).get(0).right, false);
    new Utils().scrambleBoard(this.goingDown.board, new Random(25));
    t.checkExpect(this.goingDown.board.get(1).get(0).top, true);
    t.checkExpect(this.goingDown.board.get(1).get(0).bottom, false);
    t.checkExpect(this.goingDown.board.get(1).get(0).left, false);
    t.checkExpect(this.goingDown.board.get(1).get(0).right, false);
  }

  void testNegatePowerStation(Tester t) {
    this.initialConditions();

    t.checkExpect(this.topPowered.powerStation, false);
    this.topPowered.negatePowerStation();
    t.checkExpect(this.topPowered.powerStation, true);
    this.topPowered.negatePowerStation();
    t.checkExpect(this.topPowered.powerStation, false);
  }

  // testing canGo
  void testCanGo(Tester t) {
    this.initialConditions();

    // power coming down into cell
    t.checkExpect(this.topUnpowered.canGo("down"), true);
    // power coming up into cell
    t.checkExpect(this.bottomUnpowered.canGo("up"), true);
    // power coming into cell from right
    t.checkExpect(this.rightUnpowered.canGo("left"), true);
    // power coming into cell from left
    t.checkExpect(this.leftRightUnpowered.canGo("right"), true);

    // power coming into cell with no pipe in that end
    t.checkExpect(this.topUnpowered.canGo("left"), false);
    t.checkExpect(this.noBottomUnpowered.canGo("top"), false);
  }

  // testing checkPowerStation
  void testCheckPowerStation(Tester t) {
    this.initialConditions();

    // testing on a powerstation
    t.checkExpect(this.allDirectionsPowerStation.checkPowerStation(), true);

    // testing on a non powerstation
    t.checkExpect(this.noBottomPowered.checkPowerStation(), false);
    t.checkExpect(this.topBottomUnpowered.checkPowerStation(), false);
  }

  // testing unPower
  void testUnPower(Tester t) {
    this.initialConditions();

    // testing initial cell
    t.checkExpect(this.topPowered.powered, true);
    this.topPowered.unPower();
    t.checkExpect(this.topPowered.powered, false);

    // testing on one more cell
    t.checkExpect(this.allDirectionsPowered.powered, true);
    this.allDirectionsPowered.unPower();
    t.checkExpect(this.allDirectionsPowered.powered, false);
  }

  void testUpdatePowerHelp(Tester t) {
    this.initialConditions();

    // testing to make sure an already found piece doesn't have anything happen to
    // it
    this.found.add(this.topUnpowered);
    this.topUnpowered.updatePowerHelp(workList, found, singleTile.board);
    t.checkExpect(this.found, new ArrayList<GamePiece>(Arrays.asList(this.topUnpowered)));

    // testing adding to the worklist in all directions

    // testing going up
    this.initialConditions();
    t.checkExpect(this.found, new ArrayList<GamePiece>());
    this.goingUp.board.get(1).get(0).updatePowerHelp(workList, found, goingUp.board);
    t.checkExpect(this.goingUp.board.get(1).get(0).powered, true);
    t.checkExpect(this.found,
        new ArrayList<GamePiece>(Arrays.asList(this.goingUp.board.get(1).get(0))));
    t.checkExpect(this.workList,
        new ArrayList<GamePiece>(Arrays.asList(this.goingUp.board.get(0).get(0))));

    // testing going down
    this.initialConditions();
    t.checkExpect(this.found, new ArrayList<GamePiece>());
    this.goingDown.board.get(0).get(0).updatePowerHelp(workList, found, goingDown.board);
    t.checkExpect(this.goingDown.board.get(0).get(0).powered, true);
    t.checkExpect(this.found,
        new ArrayList<GamePiece>(Arrays.asList(this.goingDown.board.get(0).get(0))));
    t.checkExpect(this.workList,
        new ArrayList<GamePiece>(Arrays.asList(this.goingDown.board.get(1).get(0))));

    // testing going down
    this.initialConditions();
    t.checkExpect(this.found, new ArrayList<GamePiece>());
    this.goingRight.board.get(0).get(0).updatePowerHelp(workList, found, goingRight.board);
    t.checkExpect(this.goingRight.board.get(0).get(0).powered, true);
    t.checkExpect(this.found,
        new ArrayList<GamePiece>(Arrays.asList(this.goingRight.board.get(0).get(0))));
    t.checkExpect(this.workList,
        new ArrayList<GamePiece>(Arrays.asList(this.goingRight.board.get(0).get(1))));

    // testing going down
    this.initialConditions();
    t.checkExpect(this.found, new ArrayList<GamePiece>());
    this.goingLeft.board.get(0).get(1).updatePowerHelp(workList, found, goingLeft.board);
    t.checkExpect(this.goingLeft.board.get(0).get(1).powered, true);
    t.checkExpect(this.found,
        new ArrayList<GamePiece>(Arrays.asList(this.goingLeft.board.get(0).get(1))));
    t.checkExpect(this.workList,
        new ArrayList<GamePiece>(Arrays.asList(this.goingLeft.board.get(0).get(0))));

  }

  void testAllPowered(Tester t) {
    t.checkExpect(new Utils().allPowered(this.board1), false);
    t.checkExpect(new Utils().allPowered(this.onlyOne), true);
    t.checkExpect(new Utils().allPowered(this.l2rows123), false);
    // t.checkExpect(new Utils().allPowered(this.l2.board), false);
    t.checkExpect(new Utils().allPowered(this.l3.board), true);
  }

  void testCreateBoard(Tester t) {
    // t.checkExpect(new Utils().createBoard(3, 3), this.l2rows123NoCord);
    // t.checkExpect(this.l3.board, this.onlyOne);
    //t.checkExpect(new Utils().createBoard(1, 1), this.onlyOne);
  }

  void testSetRowsAndCols(Tester t) {
    this.initialConditions();
    new Utils().setRowsAndCols(this.l2rows123NoCord);
    t.checkExpect(this.l2rows123NoCord, this.l2rows123);
    new Utils().setRowsAndCols(this.onlyOne);
    t.checkExpect(this.onlyOne,
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(new ArrayList<GamePiece>(
            Arrays.asList(new GamePiece(0, 0, true, true, true, true, true, true))))));
  }

  void testGetAllNodes(Tester t) {
    this.initialConditions();
    t.checkExpect(new Utils().getAllNodes(this.l2.board), this.l2.nodes);
    t.checkExpect(new Utils().getAllNodes(this.l3.board), this.l3.nodes);
    t.checkExpect(new Utils().getAllNodes(this.l1.board), this.l1.nodes);
  }

  void testMakeScene(Tester t) {
    this.initialConditions();
    WorldScene testBoard = new WorldScene(1000, 1000);
    WorldScene testBoard2 = new WorldScene(0, 0);
    testBoard.placeImageXY(this.TBLRPowerStation, 100, 100);
    testBoard.placeImageXY(
        new OverlayImage(new TextImage("You Win!", 8.0, Color.YELLOW),
            new RectangleImage(40, 40, OutlineMode.SOLID, Color.CYAN)),
        100 + (39) * 20, 100 + (39) * 20);
    testBoard2.placeImageXY(new OverlayImage(new TextImage("You Win!", 8, Color.YELLOW),
        new RectangleImage(40, 40, OutlineMode.SOLID, Color.CYAN)), 100, 100);
    t.checkExpect(this.l3.makeScene(), testBoard2);
  }

  void testOnMouseClick(Tester t) {
    this.initialConditions();
    this.l3.onMouseClicked(new Posn(105, 105));
    ArrayList<ArrayList<GamePiece>> temp = new ArrayList<ArrayList<GamePiece>>(
        Arrays.asList(new ArrayList<GamePiece>(
            Arrays.asList(new GamePiece(true, true, true, true, true, true)))));
    //t.checkExpect(this.l3.board, temp);
    this.goingDown.onMouseClicked(new Posn(0, 0));
    ArrayList<ArrayList<GamePiece>> temp2 = new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
        new ArrayList<GamePiece>(
            Arrays.asList(new GamePiece(true, true, true, true, false, false))),
        new ArrayList<GamePiece>(
            Arrays.asList(new GamePiece(1, 0, false, false, true, false, false, false)))));
    t.checkExpect(this.goingDown.board, temp2);
    ArrayList<ArrayList<GamePiece>> temp3 = new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
        new ArrayList<GamePiece>(
            Arrays.asList(new GamePiece(true, true, true, true, false, false))),
        new ArrayList<GamePiece>(
            Arrays.asList(new GamePiece(1, 0, false, false, true, false, false, false)))));
    this.goingDown.onMouseClicked(new Posn(50, 50));
    t.checkExpect(this.goingDown.board, temp3);
  }

  void testKruskals(Tester t) {
    this.initialConditions();
    GamePiece emptyPiece = 
        new GamePiece(false, false, false, false, false, false);
    ArrayList<ArrayList<GamePiece>> test = new Utils().emptyBoard(2, 2);
    ArrayList<Edge> test2 = new Utils().generateEdges(test, new Random());
    ArrayList<Edge> test3 = new Utils().kruskals(test2, test);
    t.checkExpect(new Utils().emptyBoard(2,2), 
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
            new ArrayList<GamePiece>(Arrays.asList(
                new GamePiece(false, false, false, false, false, false),
                new GamePiece(false, false, false, false, false, false))), 
            new ArrayList<GamePiece>(Arrays.asList(
                new GamePiece(false, false, false, false, false, false), 
                new GamePiece(false, false, false, false, false, false))))));
    t.checkExpect(new Utils().generateEdges(test, new Random(25)), 
        new ArrayList<Edge>(Arrays.asList(
            new Edge(emptyPiece, emptyPiece, 28), 
            new Edge(emptyPiece, emptyPiece, 38), 
            new Edge(emptyPiece, emptyPiece, 47), 
            new Edge(emptyPiece, emptyPiece, 81))));
    t.checkExpect(new Utils().kruskals(test2, test),
        new ArrayList<Edge>());
  }
  
  void testOnKeyEvent(Tester t) {
    this.initialConditions();
    this.l3.onKeyEvent("left");
    t.checkExpect(this.l3.board, 
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
            new ArrayList<GamePiece>(Arrays.asList(
                new GamePiece(false, false, false, false, true, true))))));
    this.l3.onKeyEvent("right");
    t.checkExpect(this.l3.board, 
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
            new ArrayList<GamePiece>(Arrays.asList(
                new GamePiece(false, false, false, false, true, true))))));
    this.l3.onKeyEvent("up");
    t.checkExpect(this.l3.board, 
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
            new ArrayList<GamePiece>(Arrays.asList(
                new GamePiece(false, false, false, false, true, true))))));
  }

  void testCompareEdgeWeight(Tester t) {
    Edge temp = new Edge(this.allDirectionsPowered, this.allDirectionsPowerStation, 50);
    Edge temp1 = new Edge(this.allDirectionsPowered, this.allDirectionsPowerStation, 49);
    Edge temp2 = new Edge(this.allDirectionsPowered, this.allDirectionsPowerStation, 51);
    Edge temp3 = new Edge(this.allDirectionsPowered, this.allDirectionsPowerStation, 50);
    t.checkExpect(new CompareEdgeWeight().compare(temp, temp1), 1);
    t.checkExpect(new CompareEdgeWeight().compare(temp1, temp), -1);
    t.checkExpect(new CompareEdgeWeight().compare(temp, temp3), 0);
    t.checkExpect(new CompareEdgeWeight().compare(temp2, temp3), 1);

  }

  void testBigBang(Tester t) {
    this.l4.bigBang(1000, 1000);
  }
}
