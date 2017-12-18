package cn.ezandroid.sgf.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import cn.ezandroid.sgf.SGFTree;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread() {
            public void run() {
                loadRawSGF(R.raw.simple);
                loadRawSGF(R.raw.complex);
                loadRawSGF(R.raw.alphago_opening_book);
            }
        }.start();
    }

    private void loadRawSGF(int id) {
        BufferedReader reader = null;
        try {
            long time = System.currentTimeMillis();
            reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(id)));
            Vector<SGFTree> trees = SGFTree.load(reader);
            Log.e("MainActivity", "UseTime:" + (System.currentTimeMillis() - time) + " Tree size:" + trees.size());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
