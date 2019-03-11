/** Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT */
package cn.ezandroid.sgf.demo;

import java.io.Serializable;

/**
 * 使用异或进行计算棋盘状态的哈希值
 *
 * @author Barry Becker
 */
public class HashKey implements Serializable {

    private static final long serialVersionUID = 42L;

    private volatile Long mKey;

    public HashKey() {
        mKey = 0L;
    }

    public HashKey(Long key) {
        mKey = key;
    }

    public HashKey(HashKey key) {
        this(key.getKey());
    }

    public HashKey copy() {
        return new HashKey(this);
    }

    public void applyMove(long specialNumber) {
        // note ^ is XOR (exclusive OR) in java.
        mKey ^= specialNumber;
    }

    public boolean matches(Long key) {
        return mKey.equals(key);
    }

    public Long getKey() {
        return mKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashKey)) return false;

        HashKey hashKey = (HashKey) o;

        return !(mKey != null ? !mKey.equals(hashKey.mKey) : hashKey.mKey != null);
    }

    @Override
    public int hashCode() {
        return mKey.hashCode();
    }

    public String toString() {
//        return Long.toBinaryString(mKey);
        return String.valueOf(mKey);
    }
}
