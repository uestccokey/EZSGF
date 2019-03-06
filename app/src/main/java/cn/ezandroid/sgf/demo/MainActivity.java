package cn.ezandroid.sgf.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.io.IOException;

import cn.ezandroid.lib.sgf.SGFException;
import cn.ezandroid.lib.sgf.SGFLoader;

public class MainActivity extends Activity {

    private Button mUndoButton;
    private Button mVarButton;
    private Button mRedoButton;

    private SGFGameViewer mViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUndoButton = findViewById(R.id.undo);
        mUndoButton.setOnClickListener(v -> {
            if (mViewer != null) {
                mViewer.undo();
                updateVarButton();
            }
        });
        mVarButton = findViewById(R.id.var);
        mVarButton.setOnClickListener(v -> {
            if (mViewer != null) {
                mViewer.switchNextBranch();
                updateVarButton();
            }
        });
        mRedoButton = findViewById(R.id.redo);
        mRedoButton.setOnClickListener(v -> {
            if (mViewer != null) {
                mViewer.redo();
                updateVarButton();
            }
        });

        new Thread() {
            public void run() {
//                    loadRawSGF(R.raw.simple);
//                    loadRawSGF(R.raw.complex);
//                    loadRawSGF(R.raw.sina);
//                    loadRawSGF(R.raw.normal);
//                    loadRawSGF(R.raw.normal2);
                loadRawSGF(R.raw.normal3);
//                    loadRawSGF(R.raw.alphago_opening_book);
            }
        }.start();
    }

    private void loadRawSGF(int id) {
        SGFLoader loader = new SGFLoader();
        try {
            long time = System.currentTimeMillis();
            mViewer = new SGFGameViewer(loader.load(getResources().openRawResource(id)));
            runOnUiThread(this::updateVarButton);
            Log.e("Main", "SGFLoader UseTime:" + (System.currentTimeMillis() - time));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SGFException e) {
            e.printStackTrace();
        }
    }

    private void updateVarButton() {
        mVarButton.setEnabled(mViewer != null && mViewer.isSwitchableBranch());
    }
}
