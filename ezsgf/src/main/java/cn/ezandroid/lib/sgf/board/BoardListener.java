package cn.ezandroid.lib.sgf.board;

import cn.ezandroid.lib.sgf.parser.GameNode;

public interface BoardListener {

    void placeStone(int x, int y, StoneState color);

    void removeStone(int x, int y);

    void playMove(GameNode node, GameNode prevMove);

    void undoMove(GameNode currentMove, GameNode prevMove);

    void initInitialPosition();
}
