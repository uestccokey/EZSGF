package cn.ezandroid.aq.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import cn.ezandroid.aq.android.Environment;
import cn.ezandroid.aq.android.Log;
import cn.ezandroid.aq.android.Pair;
import cn.ezandroid.lib.sgf.Point;
import cn.ezandroid.lib.sgf.SGFGame;
import cn.ezandroid.lib.sgf.SGFLeaf;
import cn.ezandroid.lib.sgf.SGFTree;
import cn.ezandroid.lib.sgf.tokens.AddBlackToken;
import cn.ezandroid.lib.sgf.tokens.AddEmptyToken;
import cn.ezandroid.lib.sgf.tokens.AddWhiteToken;
import cn.ezandroid.lib.sgf.tokens.BlackMoveToken;
import cn.ezandroid.lib.sgf.tokens.CommentToken;
import cn.ezandroid.lib.sgf.tokens.MoveToken;
import cn.ezandroid.lib.sgf.tokens.PlacementListToken;
import cn.ezandroid.lib.sgf.tokens.SGFToken;
import cn.ezandroid.lib.sgf.tokens.WhiteMoveToken;

/**
 * SGF对局查看器
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

    private static final int TRANSFORM_NORMAL = 0;
    private static final int TRANSFORM_MIRRORH = TRANSFORM_NORMAL + 1; // 水平镜像
    private static final int TRANSFORM_MIRRORV = TRANSFORM_MIRRORH + 1; // 垂直镜像
    private static final int TRANSFORM_ROTATE90 = TRANSFORM_MIRRORV + 1; // 顺时针旋转90度
    private static final int TRANSFORM_ROTATE180 = TRANSFORM_ROTATE90 + 1; // 顺时针旋转180度
    private static final int TRANSFORM_ROTATE270 = TRANSFORM_ROTATE180 + 1; // 顺时针旋转270度
    private static final int TRANSFORM_MAX = TRANSFORM_ROTATE270 + 1;

    private byte[] mBoard;
    private Game mGame; // 正向棋盘
    private ZobristHash[] mHashes = new ZobristHash[TRANSFORM_ROTATE270 + 1];

    private int mBoardSize = 19;

    private OpeningBook mOpeningBook;

    public SGFGameViewer(SGFGame game, ZobristHash hash, OpeningBook book) {
        mBoard = new byte[mBoardSize * mBoardSize];
        mGame = new Game(mBoardSize);
        mHashes[TRANSFORM_NORMAL] = hash;
        try {
            mHashes[TRANSFORM_MIRRORH] = hash.clone();
            mHashes[TRANSFORM_MIRRORV] = hash.clone();
            mHashes[TRANSFORM_ROTATE90] = hash.clone();
            mHashes[TRANSFORM_ROTATE180] = hash.clone();
            mHashes[TRANSFORM_ROTATE270] = hash.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        mOpeningBook = book;
        mSGFTree = game.getTree();
    }

    /**
     * 开始浏览
     */
    public void start() {
        mTrees = mSGFTree.getListTrees();
        while (mSGFTree.getLeafCount() == 0 && mTrees.hasNext()) {
            mSGFTree = mTrees.next();
        }
        mTrees = mSGFTree.getListTrees();
        mLeaves = mSGFTree.getListLeaves();

        while (mLeaves.hasNext()) {
            ListIterator<SGFToken> tokens = mLeaves.next().getListTokens();
            while (tokens.hasNext()) {
                SGFToken token = tokens.next();
                redoToken(token);
            }

            printBoard(mBoard);
        }
    }

    /**
     * 遍历所有节点，生成开局库文件
     *
     * @param append
     */
    public void writeOpeningBook(boolean append) {
        if (!append) {
            mOpeningBook = new OpeningBook((byte) mBoardSize);
        }

        mTrees = mSGFTree.getListTrees();
        while (mSGFTree.getLeafCount() == 0 && mTrees.hasNext()) {
            mSGFTree = mTrees.next();
        }
        mTrees = mSGFTree.getListTrees();
        mLeaves = mSGFTree.getListLeaves();

        updateOpeningBook();

        redoTraverse();

        Log.e("SGFGameViewer", "总局面数:" + mOpeningBook.size());
        OpeningBookHelper.writeOpeningBook(
                new File(Environment.getExternalStorageDirectory().toString(), "opening_book_" + mOpeningBook.size() + ".ob"), mOpeningBook);
    }

    private void undoTraverse() {
//        Log.e("SGFGameViewer", "undoTraverse");
        ListIterator<SGFToken> previousTokens;
        while (mLeaves.hasPrevious()) {
            SGFLeaf leaf = mLeaves.previous();
            previousTokens = leaf.getListTokens();

            while (previousTokens.hasNext()) {
                SGFToken token = previousTokens.next();
                undoToken(token);
            }
        }
//        printBoard(mBoard);

        SGFTree parent = mSGFTree.getParentTree();
        if (parent != null) {
            mSGFTree = parent;
            mTrees = mSGFTree.getListTrees();
            mLeaves = mSGFTree.getListLeaves();

            if (mTrees.hasNext()) {
                // 有下一个分支切换到下一个分支
                mSGFTree = mTrees.next();
                redoTraverse();
            } else {
                // 没有下一个分支退出该级
                undoTraverse();
            }
        }
    }

    private void redoTraverse() {
//        Log.e("SGFGameViewer", "redoTraverse");
        mTrees = mSGFTree.getListTrees();
        mLeaves = mSGFTree.getListLeaves();

        ListIterator<SGFToken> nextTokens;
        while (mLeaves.hasNext()) {
            SGFLeaf leaf = mLeaves.next();
            nextTokens = leaf.getListTokens();

            while (nextTokens.hasNext()) {
                SGFToken token = nextTokens.next();
                boolean isPlayMove = redoToken(token);
                if (isPlayMove) {
                    updateOpeningBook();
                }
            }
        }
//        printBoard(mBoard);

        if (mTrees.hasNext()) {
            // 有下一级进入下一级
            mSGFTree = mTrees.next();
            redoTraverse();
        } else {
            // 没有下一级退出该级
            undoTraverse();
        }
    }

    private void updateOpeningBook() {
        updateOpeningBook(mSGFTree.getNewListTrees(mTrees.nextIndex()), mSGFTree.getNewListLeaves(mLeaves.nextIndex()));
    }

    private void updateOpeningBook(ListIterator<SGFTree> trees, ListIterator<SGFLeaf> leaves) {
        long hash = mHashes[TRANSFORM_NORMAL].getKey().getKey();
        boolean findMove = false;
        boolean findComment = false;
        MoveToken moveToken = null;
        CommentToken commentToken = null;
        while (leaves.hasNext()) {
            // 使用下一步棋作为对于当前局面的预测记录到开局库中
            SGFLeaf leaf = leaves.next();
            ListIterator<SGFToken> tokens = leaf.getListTokens();
            while (tokens.hasNext()) {
                SGFToken token = tokens.next();
                if (token instanceof MoveToken) {
                    moveToken = (MoveToken) token;
                    findMove = true;
                } else if (token instanceof CommentToken) {
                    commentToken = (CommentToken) token;
                    findComment = true;
                }
                if (findMove && findComment) {
                    break;
                }
            }
            if (findMove) {
                break;
            }
        }
        if (findMove) {
            String comment = commentToken != null ? commentToken.getComment() : "";
            Iterator<Point> points = moveToken.getPoints();
            if (points.hasNext()) {
                Point point = points.next();
                int position = (point.x - 1) + mBoardSize * (point.y - 1);
                OpeningBook.Forecast forecast = new OpeningBook.Forecast((short) position, comment);
                List<OpeningBook.Forecast> forecasts = mOpeningBook.get(hash);
                if (forecasts != null) {
                    if (forecasts.contains(forecast)) {
                        forecasts.get(forecasts.indexOf(forecast)).appendInfo(forecast.getInfo());
                    } else {
                        mOpeningBook.add(hash, forecast);
                    }
                } else {
                    mOpeningBook.add(hash, forecast);
                }
                System.err.println(mOpeningBook.size() + ":" + hash + "->(" + (point.x - 1) + ", " + (point.y - 1) + ")" + moveToken + " " + comment);
            }
        } else {
            while (trees.hasNext()) {
                // 有下一级进入下一级
                SGFTree nextTree = trees.next();
                updateOpeningBook(nextTree.getNewListTrees(), nextTree.getNewListLeaves());
            }
        }
    }

    /**
     * 当前状态是否可用
     *
     * @return
     */
    public boolean isLegal() {
        return mTrees != null && mLeaves != null;
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
        return !mLeaves.hasNext() && mSGFTree.getTreeCount() > 0;
    }

    /**
     * 获取分支首步列表
     *
     * @return
     */
    public List<Point> getBranchesPoints() {
        List<Point> branchesPoints = new ArrayList<>();
        if (!isSwitchableBranch()) {
            return branchesPoints;
        }
        ListIterator<SGFTree> branches = mSGFTree.getNewListTrees();
        while (branches.hasNext()) {
            SGFTree tree = branches.next();
            ListIterator<SGFLeaf> leaves = tree.getNewListLeaves();
            if (leaves.hasNext()) {
                SGFLeaf leaf = leaves.next();
                ListIterator<SGFToken> tokens = leaf.getListTokens();
                while (tokens.hasNext()) {
                    SGFToken token = tokens.next();
                    if (token instanceof MoveToken) {
                        Iterator<Point> points = ((PlacementListToken) token).getPoints();
                        while (points.hasNext()) {
                            Point point = points.next();
                            branchesPoints.add(point);
                        }
                        break;
                    }
                }
            }
        }
        return branchesPoints;
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

//            while (mLeaves.hasPrevious()) {
//                mLeaves.previous();
//            }

            redo();
        }
        return switchable;
    }

    /**
     * 切换到主分支
     *
     * @return
     */
    private boolean switchMainBranch() {
        boolean switchable = isSwitchableBranch();
        if (switchable) {
            // 如果不是主分支则先回到主分支
            while (mTrees.hasPrevious()) {
                mTrees.previous();
            }

            mSGFTree = mTrees.next();
            mTrees = mSGFTree.getListTrees();
            mLeaves = mSGFTree.getListLeaves();

//            while (mLeaves.hasPrevious()) {
//                mLeaves.previous();
//            }

            redo();
        }
        return switchable;
    }

    private void applyPassingMove() {
        for (ZobristHash hash : mHashes) {
            hash.applyPassingMove();
        }
    }

    private void applyMove(int x, int y, int state) {
        mHashes[TRANSFORM_NORMAL].applyMove(x, y, state);
        mHashes[TRANSFORM_MIRRORH].applyMove(mBoardSize - 1 - x, y, state);
        mHashes[TRANSFORM_MIRRORV].applyMove(x, mBoardSize - 1 - y, state);
        mHashes[TRANSFORM_ROTATE90].applyMove(y, x, state);
        mHashes[TRANSFORM_ROTATE180].applyMove(mBoardSize - 1 - x, mBoardSize - 1 - y, state);
        mHashes[TRANSFORM_ROTATE270].applyMove(mBoardSize - 1 - y, mBoardSize - 1 - x, state);
    }

    /**
     * 获取指定位置的状态序号
     *
     * @param index
     * @return
     */
    private int getStateIndex(int index) {
        switch (mBoard[index]) {
            case BLACK:
                return ZobristHash.STATE_BLACK;
            case WHITE:
                return ZobristHash.STATE_WHITE;
        }
        return ZobristHash.STATE_EMPTY;
    }

    private boolean isPlayMoveToken(SGFToken token) {
        return token instanceof MoveToken ||
                token instanceof AddBlackToken ||
                token instanceof AddWhiteToken ||
                token instanceof AddEmptyToken;
    }

    private boolean undoToken(SGFToken token) {
        if (token instanceof PlacementListToken) {
            Iterator<Point> points = ((PlacementListToken) token).getPoints();
            while (points.hasNext()) {
                Point point = points.next();
                int x = point.x - 1;
                int y = point.y - 1;
                int index = x + mBoardSize * y;
                if (token instanceof MoveToken) {
                    Pair<Move, Chain> pair = mGame.undo();
                    if (pair != null) {
                        Move move = pair.first;
                        if (((MoveToken) token).isPass(mBoardSize)) {
//                            mHash.applyPassingMove();
                            applyPassingMove();
                        } else {
//                            mHash.applyMove(x, y, getStateIndex(index));
                            applyMove(x, y, getStateIndex(index));
                            mBoard[index] = EMPTY;
                            Set<Chain> chains = move.getCaptured();
                            for (Chain chain : chains) {
                                for (Stone stone : chain.getStones()) {
                                    int i = stone.intersection.x + mBoardSize * stone.intersection.y;
//                                    mHash.applyMove(stone.intersection.x, stone.intersection.y, getStateIndex(i));
                                    applyMove(stone.intersection.x, stone.intersection.y, getStateIndex(i));
                                    mBoard[i] = (stone.color == StoneColor.BLACK) ? BLACK : WHITE;
                                }
                            }
                        }
                    }
                } else if (token instanceof AddBlackToken) {
                    Stone stone = new Stone();
                    stone.intersection = new Intersection(x, y);
                    stone.color = StoneColor.BLACK;
                    mGame.forceRemoveStone(stone);

//                    mHash.applyMove(x, y, ZobristHash.STATE_BLACK);
                    applyMove(x, y, ZobristHash.STATE_BLACK);
                    mBoard[index] = EMPTY;
                } else if (token instanceof AddWhiteToken) {
                    Stone stone = new Stone();
                    stone.intersection = new Intersection(x, y);
                    stone.color = StoneColor.WHITE;
                    mGame.forceRemoveStone(stone);

//                    mHash.applyMove(x, y, ZobristHash.STATE_WHITE);
                    applyMove(x, y, ZobristHash.STATE_WHITE);
                    mBoard[index] = EMPTY;
                } else if (token instanceof AddEmptyToken) {
                    MoveToken original = ((AddEmptyToken) token).getChange(new Point((byte) (x + 1), (byte) (y + 1)));
                    if (original != null) {
                        if (original.isBlack()) {
                            Stone stone = new Stone();
                            stone.intersection = new Intersection(x, y);
                            stone.color = StoneColor.BLACK;
                            mGame.forceAddStone(stone);

//                            mHash.applyMove(x, y, ZobristHash.STATE_BLACK);
                            applyMove(x, y, ZobristHash.STATE_BLACK);
                            mBoard[index] = BLACK;
                        } else {
                            Stone stone = new Stone();
                            stone.intersection = new Intersection(x, y);
                            stone.color = StoneColor.WHITE;
                            mGame.forceAddStone(stone);

//                            mHash.applyMove(x, y, ZobristHash.STATE_WHITE);
                            applyMove(x, y, ZobristHash.STATE_WHITE);
                            mBoard[index] = WHITE;
                        }
                    }
                }
            }
        }
//        Log.e("SGFGameViewer", "undoToken:" + token + " " + mHash.getKey().getKey());
        return isPlayMoveToken(token);
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
            ListIterator<SGFToken> tokens = mLeaves.previous().getListTokens();
            while (tokens.hasNext()) {
                SGFToken token = tokens.next();
                undoToken(token);
            }
            if (!mLeaves.hasPrevious()) {
                SGFTree parent = mSGFTree.getParentTree();
                if (parent != null) {
                    mSGFTree = parent;
                    mTrees = mSGFTree.getListTrees();
                    mLeaves = mSGFTree.getListLeaves();
                }
            }

            printBoard(mBoard);
            return true;
        } else {
            return false;
        }
    }

    private boolean redoToken(SGFToken token) {
        if (token instanceof PlacementListToken) {
            Iterator<Point> points = ((PlacementListToken) token).getPoints();
            while (points.hasNext()) {
                Point point = points.next();
                int x = point.x - 1;
                int y = point.y - 1;
                int index = x + mBoardSize * y;
                if (token instanceof MoveToken) {
                    Move move = mGame.redo();
                    if (move != null) {
                        if (((MoveToken) token).isPass(mBoardSize)) {
//                            mHash.applyPassingMove();
                            applyPassingMove();
                        } else {
                            mBoard[index] = ((MoveToken) token).isBlack() ? BLACK : WHITE;
//                            mHash.applyMove(x, y, getStateIndex(index));
                            applyMove(x, y, getStateIndex(index));
                            Set<Chain> chains = move.getCaptured();
                            for (Chain chain : chains) {
                                for (Stone stone : chain.getStones()) {
                                    int i = stone.intersection.x + mBoardSize * stone.intersection.y;
//                                    mHash.applyMove(stone.intersection.x, stone.intersection.y, getStateIndex(i));
                                    applyMove(stone.intersection.x, stone.intersection.y, getStateIndex(i));
                                    mBoard[i] = EMPTY;
                                }
                            }
                        }
                    } else {
                        if (((MoveToken) token).isPass(mBoardSize)) {
                            Stone stone = new Stone();
                            stone.color = ((MoveToken) token).isBlack() ? StoneColor.BLACK : StoneColor.WHITE;
                            mGame.addPassStone(stone);

//                            mHash.applyPassingMove();
                            applyPassingMove();
                        } else {
                            HashSet<Chain> chains = new HashSet<>();
                            Stone stone = new Stone();
                            stone.intersection = new Intersection(x, y);
                            stone.color = ((MoveToken) token).isBlack() ? StoneColor.BLACK : StoneColor.WHITE;
                            mGame.addStone(stone, chains);

                            mBoard[index] = ((MoveToken) token).isBlack() ? BLACK : WHITE;
//                            mHash.applyMove(x, y, getStateIndex(index));
                            applyMove(x, y, getStateIndex(index));
                            for (Chain chain : chains) {
                                for (Stone s : chain.getStones()) {
                                    int i = s.intersection.x + mBoardSize * s.intersection.y;
//                                    mHash.applyMove(s.intersection.x, s.intersection.y, getStateIndex(i));
                                    applyMove(s.intersection.x, s.intersection.y, getStateIndex(i));
                                    mBoard[i] = EMPTY;
                                }
                            }
                        }
                    }
                } else if (token instanceof AddBlackToken) {
                    Stone stone = new Stone();
                    stone.intersection = new Intersection(x, y);
                    stone.color = StoneColor.BLACK;
                    mGame.forceAddStone(stone);

                    mBoard[index] = BLACK;
//                    mHash.applyMove(x, y, ZobristHash.STATE_BLACK);
                    applyMove(x, y, ZobristHash.STATE_BLACK);
                } else if (token instanceof AddWhiteToken) {
                    Stone stone = new Stone();
                    stone.intersection = new Intersection(x, y);
                    stone.color = StoneColor.WHITE;
                    mGame.forceAddStone(stone);

                    mBoard[index] = WHITE;
//                    mHash.applyMove(x, y, ZobristHash.STATE_WHITE);
                    applyMove(x, y, ZobristHash.STATE_WHITE);
                } else if (token instanceof AddEmptyToken) {
                    switch (mBoard[index]) {
                        case BLACK: {
                            Stone stone = new Stone();
                            stone.intersection = new Intersection(x, y);
                            stone.color = StoneColor.BLACK;
                            mGame.forceRemoveStone(stone);

//                            mHash.applyMove(x, y, ZobristHash.STATE_BLACK);
                            applyMove(x, y, ZobristHash.STATE_BLACK);
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

//                            mHash.applyMove(x, y, ZobristHash.STATE_WHITE);
                            applyMove(x, y, ZobristHash.STATE_WHITE);
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
//        Log.e("SGFGameViewer", "redoToken:" + token + " " + mHash.getKey().getKey());
        return isPlayMoveToken(token);
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
            ListIterator<SGFToken> tokens = mLeaves.next().getListTokens();
            while (tokens.hasNext()) {
                SGFToken token = tokens.next();
                redoToken(token);
            }

            printBoard(mBoard);
        } else {
            switchMainBranch();
        }
        return true;
    }

    private List<Point> findForecastsPoints(int transform) {
        long hash = mHashes[transform].getKey().getKey();
        List<Point> forecastsPoints = new ArrayList<>();
        List<OpeningBook.Forecast> forecasts = mOpeningBook.get(hash);
        if (forecasts != null) {
            for (OpeningBook.Forecast forecast : forecasts) {
                short position = forecast.getPosition();
                int x = -1;
                int y = -1;
                switch (transform) {
                    case TRANSFORM_NORMAL:
                        x = position % mBoardSize;
                        y = position / mBoardSize;
                        break;
                    case TRANSFORM_MIRRORH:
                        x = mBoardSize - 1 - position % mBoardSize;
                        y = position / mBoardSize;
                        break;
                    case TRANSFORM_MIRRORV:
                        x = position % mBoardSize;
                        y = mBoardSize - 1 - position / mBoardSize;
                        break;
                    case TRANSFORM_ROTATE90:
                        x = position / mBoardSize;
                        y = position % mBoardSize;
                        break;
                    case TRANSFORM_ROTATE180:
                        x = mBoardSize - 1 - position % mBoardSize;
                        y = mBoardSize - 1 - position / mBoardSize;
                        break;
                    case TRANSFORM_ROTATE270:
                        x = mBoardSize - 1 - position / mBoardSize;
                        y = mBoardSize - 1 - position % mBoardSize;
                        break;
                }
                if (x != -1 && y != -1) {
                    System.err.println("变换:" + transform + " 下一手:" + "(" + x + "," + y + ")" + " 信息:" + forecast.getInfo());
                    forecastsPoints.add(new Point((byte) x, (byte) y));
                }
            }
        }
        return forecastsPoints;
    }

    private void printBoard(byte[] board) {
        // 预测点列表
        List<Point> forecastsPoints = new ArrayList<>();
        int transform = TRANSFORM_NORMAL;
        while (forecastsPoints.isEmpty()) {
            forecastsPoints.addAll(findForecastsPoints(transform));
            transform++;
            if (transform >= TRANSFORM_MAX) {
                break;
            }
        }

        // 分支点列表
        List<Point> branchesPoints = getBranchesPoints();

        System.err.println("Hash(" + mHashes[TRANSFORM_NORMAL].getKey()
                + " MH:" + mHashes[TRANSFORM_MIRRORH].getKey()
                + " MV:" + mHashes[TRANSFORM_MIRRORV].getKey()
                + " R90:" + mHashes[TRANSFORM_ROTATE90].getKey()
                + " R180:" + mHashes[TRANSFORM_ROTATE180].getKey()
                + " R270:" + mHashes[TRANSFORM_ROTATE270].getKey() + ")");
        System.err.println("Board(手数:" + mGame.getCurrentMoveNumber() + " 预测:" + forecastsPoints.size() + " 分支:" + branchesPoints.size() + ")");
        System.err.print(" |-");
        for (int i = 0; i < mBoardSize; i++) {
            System.err.print("--");
        }
        System.err.print("|");
        System.err.println();
        for (int i = 0; i < mBoardSize; i++) {
            for (int j = 0; j < mBoardSize + 1 + 1; j++) {
                if (j == 0) {
                    System.err.print(((i + 1) % 10) + "| ");
                } else if (j == mBoardSize + 1) {
                    System.err.print("| ");
                } else {
                    int player = board[i * mBoardSize + (j - 1)];
                    if (player == BLACK) {
                        System.err.print("B");
                    } else if (player == WHITE) {
                        System.err.print("W");
                    } else {
                        if (forecastsPoints.contains(new Point((byte) (j - 1), (byte) (i)))) {
                            System.err.print("#");
                        } else if (branchesPoints.contains(new Point((byte) j, (byte) (i + 1)))) {
                            System.err.print("@");
                        } else {
                            System.err.print("+");
                        }
                    }
                    System.err.print(" ");
                }
            }
            System.err.println();
        }
        System.err.print(" |-");
        for (int i = 0; i < mBoardSize; i++) {
            System.err.print("--");
        }
        System.err.print("|");
        System.err.println();
    }
}
