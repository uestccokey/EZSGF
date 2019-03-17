package cn.ezandroid.sgf.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import cn.ezandroid.ezpermission.EZPermission;
import cn.ezandroid.ezpermission.Permission;
import cn.ezandroid.ezpermission.PermissionCallback;
import cn.ezandroid.lib.sgf.Sgf;
import cn.ezandroid.sgf.demo.view.MoveTreeView;

public class MainActivity extends Activity {

    private Button mUndoButton;
    private Button mVarButton;
    private Button mRedoButton;

    private MoveTreeView mMoveTreeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUndoButton = findViewById(R.id.undo);
        mUndoButton.setOnClickListener(v -> {
        });
        mVarButton = findViewById(R.id.var);
        mVarButton.setOnClickListener(v -> {
        });
        mRedoButton = findViewById(R.id.redo);
        mRedoButton.setOnClickListener(v -> {
        });

        mMoveTreeView = findViewById(R.id.tree);

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
        mMoveTreeView.bindSgfGame(Sgf.createFromInputStream(getResources().openRawResource(id)));
    }
}
