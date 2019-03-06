package cn.ezandroid.sgf.demo;

import android.util.Pair;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 棋局模型
 *
 * @author like
 */
public class Game implements Cloneable, Serializable {

    private static final long serialVersionUID = 42L;

    public static final int MAX_LADDER_ATTEMPT = 40;

    /**
     * 棋盘上的所有棋串
     */
    private Set<Chain> mChains;

    /**
     * 坐标与棋串的映射图
     */
    private Map<Intersection, Chain> mFilled;

    /**
     * 落子历史
     */
    private History<Move> mHistory;

    /**
     * 棋盘大小
     */
    private int mBoardSize;

    /**
     * 贴目
     */
    private float mKomi;

    /**
     * 白棋提子数
     */
    private int mWhitesCaptures;

    /**
     * 黑棋提子数
     */
    private int mBlacksCaptures;

    private GameResult mGameResult;

    public Game() {
        this(19);
    }

    public Game(int size) {
        this(size, 7.5f);
    }

    public Game(int size, float komi) {
        mChains = new HashSet<>();
        mFilled = new HashMap<>();
        mHistory = new History<>();
        mBoardSize = size;
        mKomi = komi;
        mWhitesCaptures = 0;
        mBlacksCaptures = 0;
    }

    /**
     * 判断最近两手棋是否是Pass
     *
     * @return
     */
    public boolean hasTwoPass() {
        Move currentMove = mHistory.readLatest();
        if (currentMove != null && mHistory.getHead() >= 1) {
            Move lastMove = mHistory.get(mHistory.getHead() - 1);
            if (lastMove != null) {
                return currentMove.getStone().isPassStone() && lastMove.getStone().isPassStone();
            }
        }
        return false;
    }

    public boolean isFinished() {
        return mGameResult != null;
    }

    public void resign(StoneColor color) {
        mGameResult = new GameResult(color == StoneColor.BLACK ? GameResult.WHITE : GameResult.BLACK, GameResult.RESIGN);
    }

    public void setGameResult(GameResult gameResult) {
        mGameResult = gameResult;
    }

    public GameResult getGameResult() {
        return mGameResult;
    }

    public int getBoardSize() {
        return mBoardSize;
    }

    public float getKomi() {
        return mKomi;
    }

    public int getBlacksCaptures() {
        return mBlacksCaptures;
    }

    public int getWhitesCaptures() {
        return mWhitesCaptures;
    }

    @Override
    public Game clone() throws CloneNotSupportedException {
        Game game = (Game) super.clone();
        game.mChains = new HashSet<>();
        game.mFilled = new HashMap<>();
        Map<Chain, Chain> chainMap = new HashMap<>();
        for (Chain chain : mChains) {
            Chain chainClone = chain.clone();
            chainMap.put(chain, chainClone);
            game.mChains.add(chainClone);
        }
        for (Map.Entry<Intersection, Chain> entry : mFilled.entrySet()) {
            Chain chain = entry.getValue();
            Chain chainClone = chainMap.get(chain);
            if (chainClone == null) {
                chainClone = chain.clone();
            }
            game.mFilled.put(entry.getKey().clone(), chainClone);
        }
        game.mHistory = new History<>();
        for (Move move : mHistory) {
            Set<Chain> capturedClone = new HashSet<>();
            Set<Chain> captured = move.getCaptured();
            for (Chain chain : captured) {
                Chain chainClone = chainMap.get(chain);
                if (chainClone == null) {
                    chainClone = chain.clone();
                }
                capturedClone.add(chainClone);
            }
            Move moveClone = new Move(move.getStone().clone(), capturedClone);
            Intersection ko = move.getKO();
            if (ko != null) {
                moveClone.setKO(ko.clone());
            }
            game.mHistory.add(moveClone);
        }
        game.mHistory.setHead(mHistory.getHead());
        if (mGameResult != null) {
            game.mGameResult = new GameResult(mGameResult.getWinner(), mGameResult.getScore());
        }
        return game;
    }

    @Override
    public String toString() {
        return "Game{" +
                "mChains=" + mChains +
                ", mFilled=" + mFilled +
                ", mHistory=" + mHistory +
                ", mBoardSize=" + mBoardSize +
                ", mKomi=" + mKomi +
                ", mWhitesCaptures=" + mWhitesCaptures +
                ", mBlacksCaptures=" + mBlacksCaptures +
                ", mGameResult=" + mGameResult +
                '}';
    }

    public Set<Chain> getChains() {
        return mChains;
    }

    public Chain getChain(Intersection intersection) {
        return mFilled.get(intersection);
    }

    public History<Move> getHistory() {
        return mHistory;
    }

    public Move getCurrentMove() {
        return mHistory.readLatest();
    }

    public int getCurrentMoveNumber() {
        return mHistory.getHead() + 1;
    }

    /**
     * 检查该点是否已有棋子
     *
     * @param intersection
     * @return
     */
    public boolean occupied(Intersection intersection) {
        return mFilled.containsKey(intersection);
    }

    /**
     * 是否能撤销
     */
    public boolean hasPast() {
        return mHistory.hasPast();
    }

    /**
     * 是否能重做
     */
    public boolean hasFuture() {
        return mHistory.hasFuture();
    }

    /**
     * 撤销
     *
     * @return
     */
    public Pair<Move, Chain> undo() {
        Move move = mHistory.stepBack();
        if (move == null) {
            return null;
        }

        Stone stone = move.getStone();
        if (stone.isPassStone()) {
            return new Pair<>(move, null);
        } else {
            Set<Chain> captured = move.getCaptured();
            Chain chain = mFilled.get(stone.intersection);
            if (chain == null) {
                throw new IllegalStateException("Popped stone keyed to null chain");
            }

            // 邻接敌串气数增加
            addToOpposingLiberties(stone);

            // 删除或重建当前链，因为它可能已经分裂
            mChains.remove(chain);
            Set<Stone> stones = chain.getStones();
            for (Stone s : stones) {
                mFilled.remove(s.intersection);
            }
            for (Stone s : stones) {
                if (!s.equals(stone)) {
                    incorporateIntoChains(s);
                }
            }

            // 还原提走的棋子
            int capturedStoneCount = 0;
            for (Chain c : captured) {
                capturedStoneCount += c.size();
                for (Stone s : c.getStones()) {
                    incorporateIntoChains(s);
                }
            }

            // 更新提子数
            updateCaptureCount(stone.color, capturedStoneCount, false);

            return new Pair<>(move, chain);
        }
    }

    /**
     * 重做
     *
     * @return
     */
    public Move redo() {
        Move move = mHistory.stepForward();
        if (move == null) {
            return null;
        }

        Stone stone = move.getStone();
        if (!stone.isPassStone()) {
            // 不传入move.getCaptured()，以免再次进行capture
            addStoneWithHistory(stone, new HashSet<>(), false);
        }
        return move;
    }

    /**
     * 添加Pass
     *
     * @param stone
     */
    public void addPassStone(Stone stone) {
        Move move = new Move(stone, new HashSet<>());
        mHistory.add(move);
    }

    /**
     * 添加棋子到棋盘
     *
     * @param stone
     * @param captured
     * @return 无效落子（如自杀）返回false
     */
    public boolean addStone(Stone stone, Set<Chain> captured) {
        return addStoneWithHistory(stone, captured, true);
    }

    /**
     * 在棋盘上增加黑\白子
     * <p>
     * 增加黑\白子就是在指定的点用黑\白子“覆盖”，无论该位置之前是什么情况。增加棋子不导致任何提子，也不导致任何其它吃子（例如自杀）。
     *
     * @param stone
     */
    public void forceAddStone(Stone stone) {
        incorporateIntoChains(stone);
    }

    /**
     * 清除棋盘上指定位置的棋子
     * <p>
     * 清除棋子就是在指定的点用无子“覆盖”，无论该位置之前是什么情况。清除棋子不计入被提子数。
     *
     * @param stone
     */
    public void forceRemoveStone(Stone stone) {
        Chain chain = mFilled.get(stone.intersection);
        if (chain == null) {
            return;
        }

        // 邻接敌串气数增加
        addToOpposingLiberties(stone);

        // 删除或重建当前链，因为它可能已经分裂
        mChains.remove(chain);
        Set<Stone> stones = chain.getStones();
        for (Stone s : stones) {
            mFilled.remove(s.intersection);
        }
        for (Stone s : stones) {
            if (!s.equals(stone)) {
                incorporateIntoChains(s);
            }
        }
    }

    /**
     * 判断该点是否是劫点位置
     *
     * @param intersection
     * @return
     */
    private boolean isKo(Intersection intersection) {
        // 劫位置检查
        Move lastMove = mHistory.readLatest();
        if (lastMove != null) {
            Intersection ko = lastMove.getKO();
            return ko != null && ko.equals(intersection);
        }
        return false;
    }

    private boolean addStoneWithHistory(Stone stone, Set<Chain> captured, boolean modifyHistory) {
        if (isKo(stone.intersection)) {
            return false;
        }

        int koPos = capture(stone, captured);
        if (!isLikeSuicide(stone)) {
            incorporateIntoChains(stone);
            if (modifyHistory) {
                Move move = new Move(stone, captured);
                if (koPos != -1) {
                    move.setKO(new Intersection(koPos % 19, koPos / 19));
                }
                mHistory.add(move);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 返回与指定坐标相邻的指定颜色的棋串集合
     *
     * @param intersection
     * @param color
     * @return
     */
    public Set<Chain> getNeighborChains(Intersection intersection, StoneColor color) {
        return new NeighborChecker<Chain>().getMatchingNeighbors((found, intersection1, criterion) -> {
            if (occupied(intersection1) && mFilled.get(intersection1).getStoneColor() == criterion) {
                found.add(mFilled.get(intersection1));
            }
        }, intersection, color);
    }

    /**
     * 返回与指定坐标相邻的指定颜色和指定气数的棋串集合
     *
     * @param intersection
     * @param color
     * @param libertyCount
     * @return
     */
    public Set<Chain> getNeighborChains(Intersection intersection, StoneColor color, int libertyCount) {
        return new NeighborChecker<Chain>().getMatchingNeighbors((found, intersection1, criterion) -> {
            if (occupied(intersection1) && mFilled.get(intersection1).getStoneColor() == criterion
                    && mFilled.get(intersection1).getLiberties().size() == libertyCount) {
                found.add(mFilled.get(intersection1));
            }
        }, intersection, color);
    }

    /**
     * 返回与指定棋串相邻的的指定颜色和指定气数的棋串集合
     *
     * @param chain
     * @param color
     * @param libertyCount
     * @return
     */
    public Set<Chain> getNeighborChains(Chain chain, StoneColor color, int libertyCount) {
        Set<Chain> neighborChains = new HashSet<>();
        for (Stone stone : chain.getStones()) {
            neighborChains.addAll(getNeighborChains(stone.intersection, color, libertyCount));
        }
        return neighborChains;
    }

    public boolean isLadderCaptureSuccess(Stone stone, Chain preyChain, int remainingAttempts) {
        if (remainingAttempts <= 0) {
            return true;
        }

        Set<Chain> potentialPrey;
        if (preyChain == null) {
            // 默认情况是检查所有只有2气的相邻敌方玩家棋串作为可能的被征吃棋串
            potentialPrey = getNeighborChains(stone.intersection, stone.color.getOther(), 2);
        } else {
            potentialPrey = new HashSet<>();
            potentialPrey.add(preyChain);
        }

        if (!potentialPrey.isEmpty()) {
            try {
                // 克隆棋盘，模拟征吃
                Game copyGame = clone();
                for (Chain prey : potentialPrey) {
                    boolean result = copyGame.addStone(stone, new HashSet<>());
                    if (result) {
                        // 如果落子后是被打吃状态，则返回false，征吃失败
                        Chain hunterChain = copyGame.getChain(stone.intersection);
                        if (hunterChain.isAtari()) {
                            return false;
                        }

                        boolean escape = false;
                        // 如果被征吃的棋串周围有正在被打吃的敌方棋串，则将敌方棋串的被打吃位置也加入搜索
                        Set<Chain> atariChains = getNeighborChains(prey, stone.color, 1);
                        Set<Intersection> escapeIntersections = new LinkedHashSet<>(prey.getLiberties());
                        for (Chain chain : atariChains) {
                            escapeIntersections.add(chain.getLiberties().iterator().next());
                        }
                        for (Intersection intersection : escapeIntersections) {
                            if (intersection.equals(stone.intersection)) {
                                continue;
                            }
                            if (copyGame.isKo(intersection)) {
                                continue;
                            }
                            // 检查落子后敌方棋串从其唯一的气点出逃是否能成功
                            Stone stone1 = new Stone();
                            stone1.intersection = intersection;
                            stone1.color = stone.color.getOther();
                            if (copyGame.isLadderEscapeSuccess(stone1, prey, remainingAttempts - 1)) {
                                escape = true;
                                break; // 只要有一条路能跑掉，就算出逃成功，直接break
                            }
                        }
                        return !escape;
                    }
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean isLadderCaptureFail(Stone stone) {
        // 检查所有只有2气的相邻敌方玩家棋串作为可能的被征吃棋串
        Set<Chain> potentialPrey = getNeighborChains(stone.intersection, stone.color.getOther(), 2);

        if (!potentialPrey.isEmpty()) {
            try {
                // 克隆棋盘，模拟征吃
                Game copyGame = clone();
                for (Chain prey : potentialPrey) {
                    if (prey.size() <= 1) {
                        return false;
                    }
                    boolean result = copyGame.addStone(stone, new HashSet<>());
                    if (result) {
                        // 如果落子后是被打吃状态，则返回false，不属于征吃点
                        Chain hunterChain = copyGame.getChain(stone.intersection);
                        if (hunterChain.isAtari()) {
                            return false;
                        }

                        boolean escape = false;
                        // 如果被征吃的棋串周围有正在被打吃的敌方棋串，则将敌方棋串的被打吃位置也加入搜索
                        Set<Chain> atariChains = getNeighborChains(prey, stone.color, 1);
                        Set<Intersection> escapeIntersections = new LinkedHashSet<>(prey.getLiberties());
                        for (Chain chain : atariChains) {
                            escapeIntersections.add(chain.getLiberties().iterator().next());
                        }
                        for (Intersection intersection : escapeIntersections) {
                            if (intersection.equals(stone.intersection)) {
                                continue;
                            }
                            if (copyGame.isKo(intersection)) {
                                continue;
                            }
                            // 检查落子后敌方棋串从其唯一的气点出逃是否能成功
                            Stone stone1 = new Stone();
                            stone1.intersection = intersection;
                            stone1.color = stone.color.getOther();
                            // 排除一些明显不属于征子的棋，普通的打吃不属于征吃点
                            if (copyGame.isLadderEscapeSuccess(stone1, prey, 1)) {
                                return false;
                            }
                            if (copyGame.isLadderEscapeSuccess(stone1, prey, MAX_LADDER_ATTEMPT)) {
                                escape = true;
                                break; // 只要有一条路能跑掉，就算出逃成功，直接break
                            }
                        }
                        return escape;
                    }
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean isLadderEscapeSuccess(Stone stone, Chain preyChain, int remainingAttempts) {
        if (remainingAttempts <= 0) {
            return false;
        }

        Set<Chain> potentialPrey;
        if (preyChain == null) {
            // 默认情况是检查所有只有1气的相邻己方玩家棋串作为可能的被征吃棋串
            potentialPrey = getNeighborChains(stone.intersection, stone.color, 1);
        } else {
            potentialPrey = new HashSet<>();
            potentialPrey.add(preyChain);
        }

        if (!potentialPrey.isEmpty()) {
            try {
                // 克隆棋盘，模拟逃征
                Game copyGame = clone();
                for (Chain prey : potentialPrey) {
                    boolean result = copyGame.addStone(stone, new HashSet<>());
                    if (result) {
                        prey = copyGame.getChain(prey.getStones().iterator().next().intersection);
                        int libertiesCount = prey.getLiberties().size();

                        // 有3气及以上则已经逃脱
                        if (libertiesCount > 2) {
                            return true;
                        }

                        // 只有1气则逃脱失败
                        if (libertiesCount < 2) {
                            return false;
                        }

                        // 只有2气则继续在被征吃状态
                        boolean capture = false;
                        for (Intersection intersection : prey.getLiberties()) {
                            if (copyGame.isKo(intersection)) {
                                continue;
                            }
                            // 分别检查从棋串剩下的两个气点打吃，是否能继续征吃棋串
                            Stone stone1 = new Stone();
                            stone1.intersection = intersection;
                            stone1.color = stone.color.getOther();
                            if (copyGame.isLadderCaptureSuccess(stone1, prey, remainingAttempts - 1)) {
                                capture = true;
                                break; // 只要有一条路能征吃，就算征吃成功，直接break
                            }
                        }
                        return !capture;
                    }
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean isLadderEscapeFail(Stone stone) {
        // 检查所有只有1气的相邻己方玩家棋串作为可能的被征吃棋串
        Set<Chain> potentialPrey = getNeighborChains(stone.intersection, stone.color, 1);

        if (!potentialPrey.isEmpty()) {
            try {
                // 克隆棋盘，模拟逃征
                Game copyGame = clone();
                for (Chain prey : potentialPrey) {
                    boolean result = copyGame.addStone(stone, new HashSet<>());
                    if (result) {
                        prey = copyGame.getChain(stone.intersection);
                        int libertiesCount = prey.getLiberties().size();
                        if (libertiesCount == 2) {
                            // 只有2气则继续在被征吃状态
                            boolean capture = false;
                            for (Intersection intersection : prey.getLiberties()) {
                                if (copyGame.isKo(intersection)) {
                                    continue;
                                }
                                // 分别检查从棋串剩下的两个气点打吃，是否能继续征吃棋串
                                Stone stone1 = new Stone();
                                stone1.intersection = intersection;
                                stone1.color = stone.color.getOther();
                                if (copyGame.isLadderCaptureSuccess(stone1, prey, MAX_LADDER_ATTEMPT)) {
                                    capture = true;
                                    break; // 只要有一条路能征吃，就算征吃成功，直接break
                                }
                            }
                            return capture;
                        }
                        copyGame.undo();
                    }
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 当落子是其周围敌串的最后气点时，移除这些敌串
     *
     * @param stone
     * @param captured
     * @return 如果出现劫，则返回劫的位置，否则返回-1
     */
    private int capture(Stone stone, Set<Chain> captured) {
        Set<Chain> opposingChains = getNeighborChains(stone.intersection, stone.color.getOther());
        for (Chain chain : opposingChains) {
            if (chain.isLastLiberty(stone.intersection)) {
                captured.add(chain);
                captureChain(chain);
            }
        }

        // 当3面都是敌串，且只提了1子时为劫
        Set<Chain> friendChains = getNeighborChains(stone.intersection, stone.color);
        Set<Intersection> liberties = getNeighborLiberties(stone.intersection);
        if (friendChains.isEmpty() && liberties.size() == 1 && captured.size() == 1) {
            Chain chain = captured.iterator().next();
            if (chain.size() == 1) {
                Stone sto = chain.getStones().iterator().next();
                return sto.intersection.x + sto.intersection.y * 19;
            }
        }
        return -1;
    }

    /**
     * 移除指定棋串
     *
     * @param chain
     */
    private void captureChain(Chain chain) {
        Set<Stone> stones = chain.getStones();
        updateCaptureCount(chain.getStoneColor(), chain.size(), true);
        for (Stone stone : stones) {
            mFilled.remove(stone.intersection);
            addToOpposingLiberties(stone);
        }
        mChains.remove(chain);
    }

    /**
     * 更新提子数
     *
     * @param color
     * @param count
     * @param increment
     */
    private void updateCaptureCount(StoneColor color, int count, boolean increment) {
        if (increment) {
            if (color == StoneColor.BLACK) {
                mWhitesCaptures += count;
            } else {
                mBlacksCaptures += count;
            }
        } else {
            if (color == StoneColor.BLACK) {
                mBlacksCaptures -= count;
            } else {
                mWhitesCaptures -= count;
            }
        }
    }

    /**
     * 是否自杀着
     *
     * @param stone
     * @return
     */
    public boolean isSuicide(Stone stone) {
        if (!isLikeSuicide(stone)) {
            return false;
        }
        // 检查是否敌方棋串的最后一口气，是则返回false
        for (Chain chain : getNeighborChains(stone.intersection, stone.color.getOther())) {
            if (chain.isLastLiberty(stone.intersection)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否像自杀着
     *
     * @param stone
     * @return
     */
    private boolean isLikeSuicide(Stone stone) {
        // 检查是否己方棋串的最后一口气，不是则返回false
        for (Chain chain : getNeighborChains(stone.intersection, stone.color)) {
            if (!chain.isLastLiberty(stone.intersection)) {
                return false;
            }
        }
        return getNeighborLiberties(stone.intersection).size() == 0;
    }

    /**
     * 返回与指定坐标的相邻的气点集合
     *
     * @param intersection
     * @return
     */
    private Set<Intersection> getNeighborLiberties(Intersection intersection) {
        return new HashSet<>(new NeighborChecker<Intersection>().getMatchingNeighbors((neighbors, intersection1, dummy) -> {
            if (!occupied(intersection1)) {
                neighbors.add(intersection1);
            }
        }, intersection, null));
    }

    /**
     * 归并棋串
     * <p>
     * 1. 当落子周围有一个同色棋串时，添加到该棋串中
     * 2. 当落子周围有多个同色棋串时，合并同色棋串，并添加到该棋串中
     * 3. 当落子周围没有同色棋串时，创建一个仅包含该落子的棋串
     *
     * @param stone
     */
    private void incorporateIntoChains(Stone stone) {
        Set<Chain> friends = getNeighborChains(stone.intersection, stone.color);
        Chain merged;
        Iterator<Chain> iterator = friends.iterator();
        if (!iterator.hasNext()) {
            merged = new Chain(stone.color);
            mChains.add(merged);
        } else {
            Chain friend;
            merged = iterator.next();
            while (iterator.hasNext()) {
                friend = iterator.next();
                merged.merge(friend);
                updateFilled(merged, friend.getStones());
                mChains.remove(friend);
            }
        }
        addToChain(merged, stone);
    }

    /**
     * 添加棋子到棋串
     *
     * @param chain
     * @param stone
     */
    private void addToChain(Chain chain, Stone stone) {
        chain.add(stone, getNeighborLiberties(stone.intersection));
        // 邻接敌串气数减少
        removeFromOpposingLiberties(stone);
        mFilled.put(stone.intersection, chain);
    }

    /**
     * 更新坐标与棋串映射图
     *
     * @param chain
     * @param stones
     */
    private void updateFilled(Chain chain, Set<Stone> stones) {
        for (Stone stone : stones) {
            mFilled.put(stone.intersection, chain);
        }
    }

    /**
     * 邻接敌串气数增加
     *
     * @param stone
     */
    private void addToOpposingLiberties(Stone stone) {
        for (Chain chain : getNeighborChains(stone.intersection, stone.color.getOther())) {
            chain.addLiberty(stone.intersection);
        }
    }

    /**
     * 邻接敌串气数减少
     *
     * @param stone
     */
    private void removeFromOpposingLiberties(Stone stone) {
        for (Chain chain : getNeighborChains(stone.intersection, stone.color.getOther())) {
            chain.removeLiberty(stone.intersection);
        }
    }

    /**
     * 重置棋盘
     */
    public void reset() {
        mChains.clear();
        mHistory.clear();
        mFilled.clear();
        mWhitesCaptures = 0;
        mBlacksCaptures = 0;
    }

    /**
     * 基于某些规则的相邻坐标检查
     */
    private class NeighborChecker<T> {

        /**
         * 返回符合标准的相邻坐标集合
         *
         * @param checker
         * @param intersection
         * @param criterion
         * @return
         */
        private Set<T> getMatchingNeighbors(CheckIntersection<T> checker, Intersection intersection,
                                            Object criterion) {
            Set<T> neighbors = new HashSet<>();
            if (intersection.x - 1 > -1) {
                checker.check(neighbors, new Intersection(intersection.x - 1, intersection.y), criterion);
            }
            if (intersection.x + 1 < mBoardSize) {
                checker.check(neighbors, new Intersection(intersection.x + 1, intersection.y), criterion);
            }
            if (intersection.y - 1 > -1) {
                checker.check(neighbors, new Intersection(intersection.x, intersection.y - 1), criterion);
            }
            if (intersection.y + 1 < mBoardSize) {
                checker.check(neighbors, new Intersection(intersection.x, intersection.y + 1), criterion);
            }
            return neighbors;
        }
    }

    private interface CheckIntersection<T> {

        void check(Set<T> found, Intersection intersection, Object criterion);
    }
}

