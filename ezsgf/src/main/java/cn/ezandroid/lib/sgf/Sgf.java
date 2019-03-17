package cn.ezandroid.lib.sgf;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import cn.ezandroid.lib.sgf.parser.SgfGame;

public class Sgf {

    private Parser mParser;
    private SgfGame mGame;

    private Sgf(String sgf) {
        mParser = new Parser(sgf);
        mGame = mParser.parse();

        mGame.postProcess();
    }

    public static SgfGame createFromString(String gameAsString) {
        Sgf rtrn = new Sgf(gameAsString);
        return rtrn.getGame();
    }

    public static SgfGame createFromInputStream(InputStream in) {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            Sgf rtrn = new Sgf(result.toString("UTF-8"));
            return rtrn.getGame();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeToFile(SgfGame game, File destination) {
        writeToFile(game, destination, "UTF-8");
    }

    public static void writeToFile(SgfGame game, File destination, String encoding) {
        try (
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(destination), Charset.forName(encoding).newEncoder())) {
            osw.write(game.getGeneratedSgf());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File writeToFile(String sgf) {
        BufferedOutputStream bos = null;
        try {
            File tmpFile = File.createTempFile("sgf4j-test-", ".sgf");
            bos = new BufferedOutputStream(new FileOutputStream(tmpFile));
            bos.write(sgf.getBytes());
            return tmpFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private SgfGame getGame() {
        return mGame;
    }
}
