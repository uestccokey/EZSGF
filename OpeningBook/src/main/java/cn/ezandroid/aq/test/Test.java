package cn.ezandroid.aq.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import cn.ezandroid.lib.sgf.Sgf;
import cn.ezandroid.lib.sgf.SgfGame;

public class Test {

    public static void main(String[] args) {
        System.out.println("Run");
        loadSGF();
    }

    private static void loadSGF() {
        try {
            ZobristHash hash = ZobristHashHelper.create(new File("/Users/like/SGF/app/src/main/res/raw/zobrist_hash.zh"));

            long time = System.currentTimeMillis();
            SgfGame game1 = Sgf.createFromInputStream(new FileInputStream(new File("/Users/like/Desktop/alphago_opening_book.sgf")));
            System.out.println("Load SGF UseTime:" + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();

            OpeningBookWriter writer = new OpeningBookWriter(game1, hash);
            System.out.println("Traverse SGF UseTime:" + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();

            writer.writeOpeningBook();
            System.out.println("Write OpeningBook UseTime:" + (System.currentTimeMillis() - time));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
