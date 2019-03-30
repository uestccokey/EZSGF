package cn.ezandroid.lib.sgf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class Sgf {

    private SgfParser mSgfParser;
    private SgfGame mGame;

    private Sgf(String sgf) {
        mSgfParser = new SgfParser(sgf);
        mGame = mSgfParser.parse();

        mGame.postProcess();
    }

    public static SgfGame createFromString(String gameAsString) {
        Sgf sgf = new Sgf(gameAsString);
        return sgf.getGame();
    }

    public static SgfGame createFromInputStream(InputStream in) {
        return createFromInputStream(in, "UTF-8");
    }

    public static SgfGame createFromInputStream(InputStream in, String encoding) {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            Sgf sgf = new Sgf(result.toString(encoding));
            return sgf.getGame();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeToFile(SgfGame game, File destination) {
        writeToFile(game, destination, "UTF-8");
    }

    public static void writeToFile(SgfGame game, File destination, String encoding) {
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(destination), Charset.forName(encoding).newEncoder())) {
            osw.write(game.getGeneratedSgf());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SgfGame getGame() {
        return mGame;
    }
}
