package cn.ezandroid.aq.test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import cn.ezandroid.aq.android.Environment;
import cn.ezandroid.aq.android.Log;
import cn.ezandroid.aq.android.TextUtils;
import cn.ezandroid.lib.sgf.SgfGame;
import cn.ezandroid.lib.sgf.SgfNode;

public class OpeningBookWriter {

    private int mBoardSize = 19;
    private OpeningBook mOpeningBook;

    public OpeningBookWriter(SgfGame game, ZobristHash hash) {
        mOpeningBook = new OpeningBook((byte) mBoardSize);

        SgfNode rootNode = game.getRootNode();
        try {
            addOpenBook(rootNode, new Game(), hash);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void writeOpeningBook() {
        OpeningBookHelper.writeOpeningBook(
                new File(Environment.getExternalStorageDirectory().toString(), "opening_book_" + mOpeningBook.size() + ".ob"), mOpeningBook);
    }

    private void applyPassingMove(ZobristHash hash) {
        hash.applyPassingMove();
    }

    private void applyMove(ZobristHash hash, int x, int y, int state) {
        Log.e("OpeningBookWriter", hash.getKey() + " applyMove (" + x + "," + y + ") " + state);
        hash.applyMove(x, y, state);
        Log.e("OpeningBookWriter", hash.getKey() + "");
    }

    public static boolean isDoubleOrFloat(String str) {
        Pattern pattern = Pattern.compile("^[-+]?[.\\d]*$");
        return pattern.matcher(str).matches();
    }

    private void addOpenBook(SgfNode node, Game game, ZobristHash zobristHash) throws CloneNotSupportedException {
        long hash = zobristHash.getKey().getKey();
        if (node.hasChildren()) {
            for (SgfNode cNode : node.getChildren()) {
                Game game1 = game.clone();
                ZobristHash zobristHash1 = zobristHash.clone();

                if (!cNode.isPass()) {
                    int x = cNode.getCoords()[0];
                    int y = cNode.getCoords()[1];

                    Intersection intersection = new Intersection(x, y);
                    HashSet<Chain> chains = new HashSet<>();
                    Stone stone = new Stone();
                    stone.intersection = intersection;
                    stone.color = cNode.isBlack() ? StoneColor.BLACK : StoneColor.WHITE;
                    game1.addStone(stone, chains);

                    applyMove(zobristHash1, x, y, cNode.isBlack() ? ZobristHash.STATE_BLACK : ZobristHash.STATE_WHITE);
                    for (Chain chain : chains) {
                        for (Stone s : chain.getStones()) {
                            applyMove(zobristHash1, s.intersection.x, s.intersection.y,
                                    s.color == StoneColor.BLACK ? ZobristHash.STATE_BLACK : ZobristHash.STATE_WHITE);
                        }
                    }

                    int position = cNode.getCoords()[0] + mBoardSize * cNode.getCoords()[1];
                    String comment = cNode.getSgfComment();
                    if (!TextUtils.isEmpty(comment)) {
                        String[] infoSplits = comment.split("\n");
                        if (infoSplits.length > 0) {
                            String blackWinStr = infoSplits[0];
                            if (isDoubleOrFloat(blackWinStr)) {
                                float blackWin = Float.parseFloat(blackWinStr);
                                OpeningBook.Forecast forecast = new OpeningBook.Forecast((short) position, (short) Math.round(blackWin * 100));
                                List<OpeningBook.Forecast> forecasts = mOpeningBook.get(hash);
                                if (forecasts != null) {
                                    if (!forecasts.contains(forecast)) {
                                        mOpeningBook.add(hash, forecast);
                                    } else {
                                        // TODO
                                    }
                                } else {
                                    mOpeningBook.add(hash, forecast);
                                }
                                Log.e("OpeningBookWriter",
                                        hash + " 开局库:" + mOpeningBook.size() + " " + cNode.getCoords()[0] + "x" + cNode.getCoords()[1]);
                            }
                        }
                    }
                } else {
                    applyPassingMove(zobristHash1);
                }

                addOpenBook(cNode, game1, zobristHash1);
            }
        }

        SgfNode nextNode = node.getNextNode();
        if (nextNode != null) {
            if (!nextNode.isPass()) {
                int x = nextNode.getCoords()[0];
                int y = nextNode.getCoords()[1];

                Intersection intersection = new Intersection(x, y);
                HashSet<Chain> chains = new HashSet<>();
                Stone stone = new Stone();
                stone.intersection = intersection;
                stone.color = nextNode.isBlack() ? StoneColor.BLACK : StoneColor.WHITE;
                game.addStone(stone, chains);

                applyMove(zobristHash, x, y, nextNode.isBlack() ? ZobristHash.STATE_BLACK : ZobristHash.STATE_WHITE);
                for (Chain chain : chains) {
                    for (Stone s : chain.getStones()) {
                        applyMove(zobristHash, s.intersection.x, s.intersection.y,
                                s.color == StoneColor.BLACK ? ZobristHash.STATE_BLACK : ZobristHash.STATE_WHITE);
                    }
                }

                int position = nextNode.getCoords()[0] + mBoardSize * nextNode.getCoords()[1];
                String comment = nextNode.getSgfComment();
                if (!TextUtils.isEmpty(comment)) {
                    String[] infoSplits = comment.split("\n");
                    if (infoSplits.length > 0) {
                        String blackWinStr = infoSplits[0];
                        if (isDoubleOrFloat(blackWinStr)) {
                            float blackWin = Float.parseFloat(blackWinStr);
                            OpeningBook.Forecast forecast = new OpeningBook.Forecast((short) position, (short) Math.round(blackWin * 100));
                            List<OpeningBook.Forecast> forecasts = mOpeningBook.get(hash);
                            if (forecasts != null) {
                                if (!forecasts.contains(forecast)) {
                                    mOpeningBook.add(hash, forecast);
                                } else {
                                    // TODO
                                }
                            } else {
                                mOpeningBook.add(hash, forecast);
                            }
                            Log.e("OpeningBookWriter",
                                    hash + " 开局库:" + mOpeningBook.size() + " " + nextNode.getCoords()[0] + "x" + nextNode.getCoords()[1]);
                        }
                    }
                }
            } else {
                applyPassingMove(zobristHash);
            }

            addOpenBook(nextNode, game, zobristHash);
        }
    }
}
