package cn.ezandroid.sgf.demo;

import java.io.Serializable;
import java.util.Set;

/**
 * 落子模型
 *
 * @author like
 */
public class Move implements Serializable {

    public static final long serialVersionUID = 42L;

    private Stone mStone; // 落子的棋子
    private Set<Chain> mCaptured; // 落子后提走的棋串集合
    private Intersection mKO; // 劫的位置

    public Move(Stone s, Set<Chain> c) {
        mStone = s;
        mCaptured = c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;

        Move move = (Move) o;

        if (mStone != null ? !mStone.equals(move.mStone) : move.mStone != null) return false;
        if (mCaptured != null ? !mCaptured.equals(move.mCaptured) : move.mCaptured != null) return false;
        return mKO != null ? mKO.equals(move.mKO) : move.mKO == null;
    }

    @Override
    public int hashCode() {
        int result = mStone != null ? mStone.hashCode() : 0;
        result = 31 * result + (mCaptured != null ? mCaptured.hashCode() : 0);
        result = 31 * result + (mKO != null ? mKO.hashCode() : 0);
        return result;
    }

    public Stone getStone() {
        return mStone;
    }

    public Set<Chain> getCaptured() {
        return mCaptured;
    }

    public void setKO(Intersection ko) {
        mKO = ko;
    }

    public Intersection getKO() {
        return mKO;
    }

    @Override
    public String toString() {
        return "Move{" +
                "mStone=" + mStone +
                ", mCaptured=" + mCaptured +
                ", mKO=" + mKO +
                '}';
    }
}
