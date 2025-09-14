package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author TODO: Cc
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }
    /**
     * 将列中空格往上填充
     */
    public void remove_null() {
        boolean changed = false;
        for (int i = 0; i < board.size(); i++) {       // i 是列
            for (int j = 0; j < board.size() - 1; j++) { // j 是行
                if (board.tile(j, i) == null) {
                    for (int r2 = j + 1; r2 < board.size(); r2++) {
                        if (board.tile(r2, i) != null) {
                            // 修正参数顺序：move(col, row, tile)
                            board.move(i, j, board.tile(r2, i));
                            break;
                        }
                    }
                }
            }
        }
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board. */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;
        board.setViewingPerspective(side);
        boolean[][] is_merge = new boolean[board.size()][board.size()];
        for (int i = board.size() - 2; i >= 0; i -= 1) {
            for (int j = 0; j < board.size(); j += 1) {
                Tile t = board.tile(j, i);
                if (t == null) {
                    continue;
                }
                for (int k = i + 1; k < board.size(); k += 1) {
                    Tile next = board.tile(j, k);

                    if (next == null && k < board.size() - 1) {
                        continue;
                    }
                    if (next != null && (next.value() != t.value() ||
                            (next.value() == t.value() && is_merge[k][j]))) {
                        if (k - 1 != i) {
                            board.move(j, k - 1, t);
                            changed = true;
                        }
                        break;
                    } else {
                        boolean moved = board.move(j, k, t);
                        changed = true;
                        if (moved) {
                            score += board.tile(j, k).value();
                            is_merge[k][j] = true;
                            break;
                        }
                    }
                }
            }
        }
        board.setViewingPerspective(Side.NORTH);
        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }





    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        for (int i = 0; 0 <= i && i < b.size();i++){
            for(int j = 0; 0 <= j && j < b.size();j++){
                if (b.tile(i,j)==null){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        for (int i = 0; 0 <= i && i < b.size();i++){
            for(int j = 0; 0 <= j && j < b.size();j++){
                if (b.tile(i,j) == null){
                    continue;
                }
                if (b.tile(i,j).value() == MAX_PIECE){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        for (int i = 0; 0 <= i && i < b.size();i++){
            for(int j = 0; 0 <= j && j < b.size();j++){
                if (b.tile(i,j)==null){
                    return true;
                }
            }
        }
        for (int i = 0; 0 <= i && i < b.size();i++){
            for(int j = 0; 0 <= j && j < b.size()-1;j++){
                if (b.tile(i,j).value()==b.tile(i,j+1).value()){
                    return true;
                }
            }
        }
        for (int i = 0; 0 <= i && i < b.size();i++){
            for(int j = 0; 0 <= j && j < b.size()-1;j++){
                if (b.tile(j+1,i).value()==b.tile(j,i).value()){
                    return true;
                }
            }
        }
        return false;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
