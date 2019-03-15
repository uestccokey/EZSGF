package cn.ezandroid.lib.sgf.v2.board;

import cn.ezandroid.lib.sgf.v2.parser.GameNode;

public interface BoardListener {

    void placeStone(int x, int y, StoneState color);

    void removeStone(int x, int y);

    void playMove(GameNode node, GameNode prevMove);

    void undoMove(GameNode currentMove, GameNode prevMove);

    void initInitialPosition();
}
