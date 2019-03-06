package cn.ezandroid.lib.sgf.tokens;

import java.util.HashMap;

import cn.ezandroid.lib.sgf.Point;

/**
 * 删除棋子
 *
 * @author like
 * @date 2017-12-30
 */
public class AddEmptyToken extends PlacementListToken implements AddToken {

    private HashMap<Point, MoveToken> mChanges = new HashMap<>();

    public AddEmptyToken() {}

    public void addChange(Point point, MoveToken token) {
        mChanges.put(point, token);
    }

    public MoveToken getChange(Point point) {
        return mChanges.get(point);
    }
}
