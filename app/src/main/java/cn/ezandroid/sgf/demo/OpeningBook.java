package cn.ezandroid.sgf.demo;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 开局库
 *
 * @author like
 * @date 2019-03-08
 */
public class OpeningBook implements Serializable {

    private static final long serialVersionUID = 42L;

    @SuppressLint("UseSparseArrays")
    private Map<Long, List<Forecast>> mBookTable = new HashMap<>();

    /**
     * 对于某个局面的落子预测
     */
    public static class Forecast implements Serializable {

        private static final long serialVersionUID = 42L;

        private short mPosition;
        private String mInfo;

        public Forecast(short position, String info) {
            mPosition = position;
            mInfo = info;
        }

        public short getPosition() {
            return mPosition;
        }

        public String getInfo() {
            return mInfo;
        }
    }

    /**
     * 将对于某个局面的落子预测列表添加到开局库中
     *
     * @param hash
     * @param forecasts
     */
    public void add(long hash, List<Forecast> forecasts) {
        List<Forecast> cache = mBookTable.get(hash);
        if (cache != null) {
            cache.addAll(forecasts);
        } else {
            mBookTable.put(hash, forecasts);
        }
    }

    /**
     * 将对于某个局面的落子预测添加到开局库中
     *
     * @param hash
     * @param forecast
     */
    public void add(long hash, Forecast forecast) {
        List<Forecast> cache = mBookTable.get(hash);
        if (cache != null) {
            cache.add(forecast);
        } else {
            cache = new ArrayList<>();
            cache.add(forecast);
            mBookTable.put(hash, cache);
        }
    }

    /**
     * 获取对于某个局面的落子预测列表
     *
     * @param hash
     * @return
     */
    public List<Forecast> get(long hash) {
        return mBookTable.get(hash);
    }

    /**
     * 获取开局库局面数
     *
     * @return
     */
    public int size() {
        return mBookTable.size();
    }
}
