package cn.ezandroid.aq.test;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 棋串模型
 *
 * @author like
 */
public class Chain implements Cloneable, Serializable {

    public static final long serialVersionUID = 42L;

    private StoneColor mStoneColor; // 棋串颜色
    private Set<Stone> mStones; // 棋串棋子集合
    private Set<Intersection> mLiberties; // 棋串气点集合

    public Chain(StoneColor c) {
        mStoneColor = c;
        mStones = new HashSet<>();
        mLiberties = new HashSet<>();
    }

    @Override
    public Chain clone() throws CloneNotSupportedException {
        Chain chain = (Chain) super.clone();
//        chain.mStoneColor = mStoneColor;
        chain.mStones = new HashSet<>();
        for (Stone stone : mStones) {
            chain.mStones.add(stone.clone());
        }
        chain.mLiberties = new HashSet<>();
        for (Intersection intersection : mLiberties) {
            chain.mLiberties.add(intersection.clone());
        }
        return chain;
    }

// TODO 覆盖equals方法后，棋串气数有问题，需要检查
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof Chain)) return false;
//
//        Chain chain = (Chain) o;
//
//        if (mStoneColor != chain.mStoneColor) return false;
//        if (mStones != null ? !mStones.equals(chain.mStones) : chain.mStones != null) return false;
//        return mLiberties != null ? mLiberties.equals(chain.mLiberties) : chain.mLiberties == null;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = mStoneColor.hashCode();
//        result = 31 * result + (mStones != null ? mStones.hashCode() : 0);
//        result = 31 * result + (mLiberties != null ? mLiberties.hashCode() : 0);
//        return result;
//    }

    @Override
    public String toString() {
        return "Chain{" + hashCode() + " " +
                "mStoneColor=" + mStoneColor +
                ", mStones=" + mStones +
                ", mLiberties=" + mLiberties +
                '}';
    }

    /**
     * 获取棋串颜色
     *
     * @return
     */
    public StoneColor getStoneColor() {
        return mStoneColor;
    }

    public Set<Stone> getStones() {
        return mStones;
    }

    public Set<Intersection> getLiberties() {
        return mLiberties;
    }

    /**
     * 获取棋串棋子总数
     *
     * @return
     */
    public int size() {
        return mStones.size();
    }

    /**
     * 添加同色棋子到串中，更新气点集合
     *
     * @param stone
     * @param newLiberties
     */
    public void add(Stone stone, Set<Intersection> newLiberties) {
        if (mStoneColor != stone.color) {
            throw new IllegalArgumentException("Can not add stone of color " + stone.color +
                    " to chain of color " + mStoneColor);
        }
        mLiberties.remove(stone.intersection);
        mLiberties.addAll(newLiberties);
        mStones.add(stone);
    }

    /**
     * 合并同色棋串，更新气点集合
     */
    public void merge(Chain chain) {
        if (mStoneColor != chain.mStoneColor) {
            throw new IllegalArgumentException("Can not merge chain of color " + chain
                    .mStoneColor +
                    " to chain of color " + mStoneColor);
        }
        mStones.addAll(chain.mStones);
        mLiberties.addAll(chain.mLiberties);
    }

    /**
     * 棋串气增加
     *
     * @param intersection
     */
    public void addLiberty(Intersection intersection) {
        mLiberties.add(intersection);
    }

    /**
     * 棋串气减少
     *
     * @param intersection
     */
    public void removeLiberty(Intersection intersection) {
        mLiberties.remove(intersection);
    }

    /**
     * 检查该点是否为棋串最后一口气
     *
     * @param intersection
     * @return
     */
    public boolean isLastLiberty(Intersection intersection) {
        return mLiberties.size() == 1 && mLiberties.contains(intersection);
    }

    /**
     * 是否正处于被打吃状态
     *
     * @return
     */
    public boolean isAtari() {
        return mLiberties.size() == 1;
    }
}
