package cn.ezandroid.lib.sgf.board;

import java.util.HashSet;
import java.util.Set;

public class Group {

    public Set<Square> mStones = new HashSet<Square>();

    public void addStone(Square sq) {
        mStones.add(sq);
    }

    public boolean isEmpty() {
        return mStones.size() == 0;
    }

    public boolean contains(Square square) {
        return mStones.contains(square);
    }

    public boolean isDead(Square[][] board) {
        for (Square square : mStones) {
            if (square.x - 1 > -1 && board[square.x - 1][square.y].isEmpty())
                return false;
            if (square.x + 1 < board.length && board[square.x + 1][square.y].isEmpty())
                return false;
            if (square.y + 1 < board[square.x].length && board[square.x][square.y + 1].isEmpty())
                return false;
            if (square.y - 1 > -1 && board[square.x][square.y - 1].isEmpty())
                return false;
        }
        return true;
    }

    public void printGroup() {
        System.out.println("Print group of size " + mStones.size());
        Square[][] board = new Square[19][19];

        for (Square square : mStones) {
            board[square.x][square.y] = square;
        }

        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                if (board[i][j] == null) {
                    board[i][j] = new Square(StoneState.EMPTY, i, j);
                }
                System.out.print(board[i][j]);
            }
            System.out.println();
        }
    }

    public boolean isDead(VirtualBoard brd) {
        return isDead(brd.getBoard());
    }

    @Override
    public String toString() {
        return "Size=" + mStones.size();
    }
}
