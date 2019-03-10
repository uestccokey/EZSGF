package cn.ezandroid.sgf.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.io.IOException;

import cn.ezandroid.ezpermission.EZPermission;
import cn.ezandroid.ezpermission.Permission;
import cn.ezandroid.ezpermission.PermissionCallback;
import cn.ezandroid.lib.sgf.SGFException;
import cn.ezandroid.lib.sgf.SGFGame;
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

        EZPermission.permissions(new Permission(Permission.STORAGE)).apply(this, new PermissionCallback() {

            @Override
            public void onAllPermissionsGranted() {
                new Thread() {
                    public void run() {
                        loadRawSGF(R.raw.sina);
                    }
                }.start();
            }
        });
    }

    private void loadRawSGF(int id) {
        SGFLoader loader = new SGFLoader();
        try {
            long time = System.currentTimeMillis();

            SGFGame game = loader.load(getResources().openRawResource(id));
            Log.e("MainActivity", "Load SGF UseTime:" + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();

            ZobristHash hash = ZobristHashHelper.common(this);
            Log.e("MainActivity", "Load Hash UseTime:" + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();

            OpeningBook book = OpeningBookHelper.common(this);
            Log.e("MainActivity", "Load Book UseTime:" + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();

            mViewer = new SGFGameViewer(game, hash, book);
            Log.e("MainActivity", "Init Viewer UseTime:" + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();

//            mViewer.writeOpeningBook(false);
//            Log.e("MainActivity", "Traverse SGF UseTime:" + (System.currentTimeMillis() - time));

            mViewer.start();
            Log.e("MainActivity", "Start Viewer UseTime:" + (System.currentTimeMillis() - time));

            runOnUiThread(this::updateVarButton);
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
