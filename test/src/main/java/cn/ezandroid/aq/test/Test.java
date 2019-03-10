package cn.ezandroid.aq.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import cn.ezandroid.lib.sgf.SGFException;
import cn.ezandroid.lib.sgf.SGFGame;
import cn.ezandroid.lib.sgf.SGFLoader;

public class Test {

    public static void main(String[] args) {
        System.out.println("Run");
        loadSGF();
    }

    private static void loadSGF() {
        SGFLoader loader = new SGFLoader();
        try {
            long time = System.currentTimeMillis();

            SGFGame game = loader.load(new FileInputStream(new File("/Users/like/SGF/app/src/main/res/raw/book1485.sgf")));
            System.out.println("Load SGF UseTime:" + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();

            ZobristHash hash = ZobristHashHelper.create(new File("/Users/like/SGF/app/src/main/res/raw/zobrist_hash.zh"));
            System.out.println("Load Hash UseTime:" + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();

            OpeningBook book = OpeningBookHelper.create(new File("/Users/like/SGF/app/src/main/res/raw/opening_book.ob"));
            System.out.println("Load Book UseTime:" + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();

            SGFGameViewer viewer = new SGFGameViewer(game, hash, book);
            System.out.println("Init Viewer UseTime:" + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();

            viewer.writeOpeningBook(false);
            System.out.println("Traverse SGF UseTime:" + (System.currentTimeMillis() - time));

//            viewer.start();
//            System.out.println("Start Viewer UseTime:" + (System.currentTimeMillis() - time));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SGFException e) {
            e.printStackTrace();
        }
    }
}
