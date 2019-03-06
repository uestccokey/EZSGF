package cn.ezandroid.sgf.demo;

import android.util.Pair;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;

import cn.ezandroid.lib.sgf.Point;
import cn.ezandroid.lib.sgf.SGFGame;
import cn.ezandroid.lib.sgf.SGFLeaf;
import cn.ezandroid.lib.sgf.SGFTree;
import cn.ezandroid.lib.sgf.tokens.AddBlackToken;
import cn.ezandroid.lib.sgf.tokens.AddEmptyToken;
import cn.ezandroid.lib.sgf.tokens.AddWhiteToken;
import cn.ezandroid.lib.sgf.tokens.BlackMoveToken;
import cn.ezandroid.lib.sgf.tokens.MoveToken;
import cn.ezandroid.lib.sgf.tokens.PlacementListToken;
import cn.ezandroid.lib.sgf.tokens.SGFToken;
import cn.ezandroid.lib.sgf.tokens.WhiteMoveToken;

/**
 * SGFGameViewer
 *
 * @author like
 * @date 2019-03-06
 */
public class SGFGameViewer {

    public static final byte BLACK = 1;
    public static final byte WHITE = -1;
    public static final byte EMPTY = 0;

    private SGFTree mSGFTree;
    private ListIterator<SGFTree> mTrees;
    private ListIterator<SGFLeaf> mLeaves;

    private byte[] mBoard;
    private Game mGame;

    public SGFGameViewer(SGFGame game) {
        mBoard = new byte[19 * 19];
        mGame = new Game(19);
        mSGFTree = game.getTree();
        mTrees = mSGFTree.getListTrees();
        while (mSGFTree.getLeafCount() == 0) {
            mSGFTree = mTrees.next();
            mTrees = mSGFTree.getListTrees();
        }
        mLeaves = mSGFTree.getListLeaves();

        init();
    }

    private void init() {
        while (mLeaves.hasNext()) {
            ListIterator<SGFToken> mTokens = mLeaves.next().getListTokens();
            while (mTokens.hasNext()) {
                SGFToken token = mTokens.next();
                redoToken(token);
            }
        }
        printBoard(mBoard);
    }

    /**
     * 当前状态是否可用
     *
     * @return
     */
    public boolean isLegal() {
        if (mTrees == null) {
            return false;
        }
        if (mSGFTree == null) {
            return false;
        }
        return mLeaves != null;
    }

    /**
     * 当前状态是否可切换分支
     *
     * @return
     */
    public boolean isSwitchableBranch() {
        if (!isLegal()) {
            return false;
        }
        return !mLeaves.hasNext() && mSGFTree.getVariationCount() > 0;
    }

    /**
     * 切换到下一分支
     *
     * @return
     */
    public boolean switchNextBranch() {
        boolean switchable = isSwitchableBranch();
        if (switchable) {
            if (!mTrees.hasNext()) {
                // 已经是最后一个分支了，再调用该方法会回到主分支
                while (mTrees.hasPrevious()) {
                    mTrees.previous();
                }
            }
            mSGFTree = mTrees.next();
            mTrees = mSGFTree.getListTrees();
            mLeaves = mSGFTree.getListLeaves();

            redo();
        }
        return switchable;
    }

    /**
     * 切换到主分支
     *
     * @return
     */
    public boolean switchMainBranch() {
        boolean switchable = isSwitchableBranch();
        if (switchable) {
            // 如果不是主分支则先回到主分支
            while (mTrees.hasPrevious()) {
                mTrees.previous();
            }

            mSGFTree = mTrees.next();
            mTrees = mSGFTree.getListTrees();
            mLeaves = mSGFTree.getListLeaves();

            redo();
        }
        return switchable;
    }

    private void undoToken(SGFToken token) {
        if (token instanceof PlacementListToken) {
            Iterator<Point> points = ((PlacementListToken) token).getPoints();
            while (points.hasNext()) {
                Point point = points.next();
                int x = point.x - 1;
                int y = point.y - 1;
                int index = x + 19 * y;
                if (token instanceof MoveToken) {
                    Pair<Move, Chain> pair = mGame.undo();
                    if (pair != null) {
                        Move move = pair.first;
                        Set<Chain> chains = move.getCaptured();
                        for (Chain chain : chains) {
                            for (Stone stone : chain.getStones()) {
                                mBoard[stone.intersection.x + 19 * stone.intersection.y]
                                        = (stone.color == StoneColor.BLACK) ? BLACK : WHITE;
                            }
                        }

                        mBoard[index] = EMPTY;
                    }
                } else if (token instanceof AddBlackToken) {
                    Stone stone = new Stone();
                    stone.intersection = new Intersection(x, y);
                    stone.color = StoneColor.BLACK;
                    mGame.forceRemoveStone(stone);

                    mBoard[index] = EMPTY;
                } else if (token instanceof AddWhiteToken) {
                    Stone stone = new Stone();
                    stone.intersection = new Intersection(x, y);
                    stone.color = StoneColor.WHITE;
                    mGame.forceRemoveStone(stone);

                    mBoard[index] = EMPTY;
                } else if (token instanceof AddEmptyToken) {
                    MoveToken original = ((AddEmptyToken) token).getChange(new Point((byte) (x + 1), (byte) (y + 1)));
                    if (original != null) {
                        if (original.isBlack()) {
                            Stone stone = new Stone();
                            stone.intersection = new Intersection(x, y);
                            stone.color = StoneColor.BLACK;
                            mGame.forceAddStone(stone);

                            mBoard[index] = BLACK;
                        } else {
                            Stone stone = new Stone();
                            stone.intersection = new Intersection(x, y);
                            stone.color = StoneColor.WHITE;
                            mGame.forceAddStone(stone);

                            mBoard[index] = WHITE;
                        }
                    }
                }
            }
        }
    }

    /**
     * 悔棋
     *
     * @return
     */
    public boolean undo() {
        if (!isLegal()) {
            return false;
        }
        if (mLeaves.hasPrevious()) {
            ListIterator<SGFToken> mTokens = mLeaves.previous().getListTokens();
            while (mTokens.hasNext()) {
                SGFToken token = mTokens.next();
                undoToken(token);
            }
            printBoard(mBoard);

            if (!mLeaves.hasPrevious()) {
                SGFTree parent = mSGFTree.getParentTree();
                if (parent != null) {
                    mSGFTree = parent;
                    mTrees = mSGFTree.getListTrees();
                    mLeaves = mSGFTree.getListLeaves();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void redoToken(SGFToken token) {
        if (token instanceof PlacementListToken) {
            Iterator<Point> points = ((PlacementListToken) token).getPoints();
            while (points.hasNext()) {
                Point point = points.next();
                int x = point.x - 1;
                int y = point.y - 1;
                int index = x + 19 * y;
                if (token instanceof MoveToken) {
                    Move move = mGame.redo();
                    if (move != null) {
                        mBoard[index] = ((MoveToken) token).isBlack() ? BLACK : WHITE;
                        Set<Chain> chains = move.getCaptured();
                        for (Chain chain : chains) {
                            for (Stone stone : chain.getStones()) {
                                mBoard[stone.intersection.x + 19 * stone.intersection.y] = EMPTY;
                            }
                        }
                    } else {
                        HashSet<Chain> chains = new HashSet<>();
                        Stone stone = new Stone();
                        stone.intersection = new Intersection(x, y);
                        stone.color = ((MoveToken) token).isBlack() ? StoneColor.BLACK : StoneColor.WHITE;
                        mGame.addStone(stone, chains);

                        mBoard[index] = ((MoveToken) token).isBlack() ? BLACK : WHITE;
                        for (Chain chain : chains) {
                            for (Stone s : chain.getStones()) {
                                mBoard[s.intersection.x + 19 * s.intersection.y] = EMPTY;
                            }
                        }
                    }
                } else if (token instanceof AddBlackToken) {
                    Stone stone = new Stone();
                    stone.intersection = new Intersection(x, y);
                    stone.color = StoneColor.BLACK;
                    mGame.forceAddStone(stone);

                    mBoard[index] = BLACK;
                } else if (token instanceof AddWhiteToken) {
                    Stone stone = new Stone();
                    stone.intersection = new Intersection(x, y);
                    stone.color = StoneColor.WHITE;
                    mGame.forceAddStone(stone);

                    mBoard[index] = WHITE;
                } else if (token instanceof AddEmptyToken) {
                    switch (mBoard[index]) {
                        case BLACK: {
                            Stone stone = new Stone();
                            stone.intersection = new Intersection(x, y);
                            stone.color = StoneColor.BLACK;
                            mGame.forceRemoveStone(stone);

                            mBoard[index] = EMPTY;

                            MoveToken moveToken = new BlackMoveToken();
                            moveToken.setX((byte) (x + 1));
                            moveToken.setY((byte) (y + 1));
                            ((AddEmptyToken) token).addChange(new Point((byte) (x + 1), (byte) (y + 1)), moveToken);
                        }
                        break;
                        case WHITE:
                            Stone stone = new Stone();
                            stone.intersection = new Intersection(x, y);
                            stone.color = StoneColor.WHITE;
                            mGame.forceRemoveStone(stone);

                            mBoard[index] = EMPTY;

                            MoveToken moveToken = new WhiteMoveToken();
                            moveToken.setX((byte) (x + 1));
                            moveToken.setY((byte) (y + 1));
                            ((AddEmptyToken) token).addChange(new Point((byte) (x + 1), (byte) (y + 1)), moveToken);
                            break;
                    }
                }
            }
        }
    }

    /**
     * 重做
     *
     * @return
     */
    public boolean redo() {
        if (!isLegal()) {
            return false;
        }
        if (mLeaves.hasNext()) {
            ListIterator<SGFToken> mTokens = mLeaves.next().getListTokens();
            while (mTokens.hasNext()) {
                SGFToken token = mTokens.next();
                redoToken(token);
            }
            printBoard(mBoard);
        } else {
            switchMainBranch();
        }
        return true;
    }

    private void printBoard(byte[] board) {
        System.err.println("Board:");
        System.err.print(" |-");
        for (int i = 0; i < 19; i++) {
            System.err.print("--");
        }
        System.err.print("|");
        System.err.println();
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 21; j++) {
                if (j == 0) {
                    System.err.print(((i + 1) % 10) + "| ");
                } else if (j == 20) {
                    System.err.print("| ");
                } else {
                    int player = board[i * 19 + (j - 1)];
                    if (player == BLACK) {
                        System.err.print("B");
                    } else if (player == WHITE) {
                        System.err.print("W");
                    } else {
                        System.err.print("+");
                    }
                    System.err.print(" ");
                }
            }
            System.err.println();
        }
        System.err.print(" |-");
        for (int i = 0; i < 19; i++) {
            System.err.print("--");
        }
        System.err.print("|");
        System.err.println();
    }
}
