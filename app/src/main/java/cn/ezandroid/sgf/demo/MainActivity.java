package cn.ezandroid.sgf.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;

import cn.ezandroid.lib.sgf.SGFException;
import cn.ezandroid.lib.sgf.SGFGame;
import cn.ezandroid.lib.sgf.SGFLoader;
import cn.ezandroid.lib.sgf.demo.R;

public class MainActivity extends AppCompatActivity {

    private long mSGFLoadTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread() {
            public void run() {
                int n = 1;
                for (int i = 0; i < n; i++) {
//                    loadRawSGF(R.raw.simple);
                    loadRawSGF(R.raw.sina);
//                    loadRawSGF(R.raw.normal);
//                    loadRawSGF(R.raw.normal2);
//                    loadRawSGF(R.raw.complex);
//                    loadRawSGF(R.raw.alphago_opening_book);
                }
            }
        }.start();
    }

    private void loadRawSGF(int id) {
        SGFLoader loader = new SGFLoader();
        try {
            long time = System.currentTimeMillis();
            SGFGame game = loader.load(getResources().openRawResource(id));
            Log.e("MainActivity", "SGFLoader UseTime:" + (System.currentTimeMillis() - time) + " Tree:" + game.getTree());
            mSGFLoadTime += (System.currentTimeMillis() - time);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SGFException e) {
            e.printStackTrace();
        }
    }
}
