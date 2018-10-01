package cn.ezandroid.sgf.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.util.Iterator;

import cn.ezandroid.lib.sgf.SGFException;
import cn.ezandroid.lib.sgf.SGFGame;
import cn.ezandroid.lib.sgf.SGFLeaf;
import cn.ezandroid.lib.sgf.SGFLoader;
import cn.ezandroid.lib.sgf.SGFTree;
import cn.ezandroid.lib.sgf.tokens.SGFToken;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread() {
            public void run() {
                int n = 1;
                for (int i = 0; i < n; i++) {
//                    loadRawSGF(R.raw.simple);
//                    loadRawSGF(R.raw.complex);
//                    loadRawSGF(R.raw.sina);
//                    loadRawSGF(R.raw.normal);
//                    loadRawSGF(R.raw.normal2);
                    loadRawSGF(R.raw.normal3);
//                    loadRawSGF(R.raw.alphago_opening_book);
                }
            }
        }.start();
    }

    private static void extractMoveList(SGFTree tree) {
        Log.e("Main", "Tree:" + tree.getVariationCount() + " " + tree.getLeafCount());
        Iterator<SGFTree> trees = tree.getTrees();
        Iterator<SGFLeaf> leaves = tree.getLeaves();
        while (leaves.hasNext()) {
            Iterator<SGFToken> tokens = leaves.next().getTokens();

            // While a move token hasn't been found, and there are more tokens to
            // examine ... try and find a move token in this tree's leaves to add
            // to the collection of moves (moveList).
            while (tokens.hasNext()) {
                SGFToken token = tokens.next();
                Log.e("Main", "Token:" + token);
            }
        }

        // If there are variations, use the first variation, which is
        // the entire game, without extraneous variations.
        if (trees.hasNext()) {
            extractMoveList(trees.next());
        }
    }

    private void loadRawSGF(int id) {
        SGFLoader loader = new SGFLoader();
        try {
            long time = System.currentTimeMillis();
            SGFGame game = loader.load(getResources().openRawResource(id));
            SGFTree tree = game.getTree();
            Log.e("MainActivity", "SGFLoader UseTime:" + (System.currentTimeMillis() - time)
                    + " Tree:" + tree.getLeafCount() + " " + tree.getVariationCount());
            extractMoveList(tree);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SGFException e) {
            e.printStackTrace();
        }
    }
}
