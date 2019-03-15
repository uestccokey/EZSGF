package cn.ezandroid.aq.test;

import com.apetresc.sgfstream.IncorrectFormatException;
import com.apetresc.sgfstream.SGF;
import com.toomasr.sgf4j.Sgf;
import com.toomasr.sgf4j.parser.Game;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import cn.ezandroid.lib.sgf.SGFException;
import cn.ezandroid.lib.sgf.SGFGame;
import cn.ezandroid.lib.sgf.SGFLoader;

public class Test {

    public static void main(String[] args) {
        System.out.println("Run");
        loadSGF();
    }

    private static void loadSGF() {
//        testHash();
        SGFLoader loader = new SGFLoader();
        try {
            long time = System.currentTimeMillis();

            // /Users/like/SGF/app/src/main/res/raw/book1486.sgf
            // /Users/like/SGF/app/src/main/res/raw/book123244.sgf
            // /Users/like/SGF/app/src/main/res/raw/alphago_opening_book.sgf
            SGFGame game = loader.load(new FileInputStream(new File("/Users/like/SGF/app/src/main/res/raw/alphago_opening_book.sgf")));
            System.out.println("Load SGF UseTime:" + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();

            Game game1 = Sgf.createFromPath(Paths.get("/Users/like/SGF/app/src/main/res/raw/alphago_opening_book.sgf"));
            System.out.println("v2 Load SGF UseTime:" + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();

            SGF sgf = new SGF();
            try {
                sgf.parseSGF(new FileInputStream(new File("/Users/like/SGF/app/src/main/res/raw/alphago_opening_book.sgf")));
            } catch (IncorrectFormatException e) {
                e.printStackTrace();
            }
            System.out.println("v3 Load SGF UseTime:" + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();

//            GameCollection collection = new SGFParser(GameType.Go).parseSGF(new File("/Users/like/SGF/app/src/main/res/raw/alphago_opening_book" +
//                    ".sgf"));
//            System.out.println("v4 Load SGF UseTime:" + (System.currentTimeMillis() - time));
//            time = System.currentTimeMillis();
//
//            ZobristHash hash = ZobristHashHelper.create(new File("/Users/like/SGF/app/src/main/res/raw/zobrist_hash.zh"));
//            System.out.println("Load Hash UseTime:" + (System.currentTimeMillis() - time));
//            time = System.currentTimeMillis();
//
////            OpeningBook book = OpeningBookHelper.create(new File("/Users/like/SGF/app/src/main/res/raw/opening_book_1487.ob"));
//            OpeningBook book = new OpeningBook((byte) 19);
//            System.out.println("Load Book UseTime:" + (System.currentTimeMillis() - time));
//            time = System.currentTimeMillis();
//
//            SGFGameViewer viewer = new SGFGameViewer(game, hash, book);
//            System.out.println("Init Viewer UseTime:" + (System.currentTimeMillis() - time));
//            time = System.currentTimeMillis();
//
//            viewer.writeOpeningBook(false);
//            System.out.println("Traverse SGF UseTime:" + (System.currentTimeMillis() - time));
//
////            viewer.start();
////            viewer.redo();
////            viewer.undo();
////            viewer.switchNextBranch();
////            viewer.undo();
////            viewer.switchNextBranch();
////            viewer.redo();
//            System.out.println("Start Viewer UseTime:" + (System.currentTimeMillis() - time));
//            testHash();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SGFException e) {
            e.printStackTrace();
        }
    }

    private static void testHash() {
        ZobristHash hash = ZobristHashHelper.create(new File("/Users/like/SGF/app/src/main/res/raw/zobrist_hash.zh"));
        hash.applyMove(16, 3, ZobristHash.STATE_BLACK);
        System.out.println("(16,3):" + hash.getKey());
    }
}
