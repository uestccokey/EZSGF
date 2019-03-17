package cn.ezandroid.sgf.demo.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.ezandroid.lib.sgf.parser.GameNode;
import cn.ezandroid.lib.sgf.parser.SgfGame;
import cn.ezandroid.sgf.demo.R;

/**
 * 落子树View
 *
 * @author like
 * @date 2019-03-06
 */
public class MoveTreeView extends RecyclerView {

    private List<SparseArray<View>> mElements = new ArrayList<>();

    private int mColSize;
    private int mRowSize;
    private int mCount;

    public MoveTreeView(Context context) {
        super(context);
    }

    public MoveTreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private class MoveTreeHolder extends ViewHolder {

        LinearLayout mContainer;

        public MoveTreeHolder(@NonNull View itemView) {
            super(itemView);
            mContainer = itemView.findViewById(R.id.container);
        }
    }

    private class MoveTreeAdapter extends Adapter<MoveTreeHolder> {

        @NonNull
        @Override
        public MoveTreeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.vw_move_tree_element, viewGroup, false);
            return new MoveTreeHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MoveTreeHolder viewHolder, int i) {
            int row = i % mRowSize;
            int col = i / mRowSize;
            View stone = mElements.get(row).get(col);
            viewHolder.mContainer.removeAllViews();
            if (stone != null) {
                ViewParent parent = stone.getParent();
                if (parent != null) {
                    ((ViewGroup) parent).removeView(stone);
                }
                stone.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                viewHolder.mContainer.addView(stone);
            }
        }

        @Override
        public int getItemCount() {
            return mCount;
        }
    }

    public void bindSgfGame(SgfGame game) {
        StartStone rootStone = new StartStone(getContext());
        rootStone.setNode(game.getRootNode());
        addElement(rootStone, 0, 0);
        buildMoveTree(game.getRootNode(), 0);

        for (SparseArray<View> array : mElements) {
            int size = array.size();
            int col = size;
            for (int i = 0; i < size; i++) {
                View view = array.valueAt(i);
                if (view instanceof TreeStone) {
                    int no = ((TreeStone) view).getNode().getMoveNo();
                    if (col < no) {
                        col = no;
                    }
                }
            }
            if (mColSize < col) {
                mColSize = col;
            }
        }
        mRowSize = mElements.size();
        mCount = mRowSize * mColSize;

        Log.e("MoveTreeView", "bindSgfGame:" + mRowSize + "x" + mColSize);

        post(() -> {
            setLayoutManager(new GridLayoutManager(getContext(), mRowSize, GridLayoutManager.HORIZONTAL, false));
            setAdapter(new MoveTreeAdapter());
        });
    }

    private void buildMoveTree(GameNode node, int depth) {
        // we draw out only actual moves
        if (node.isMove() || (node.getMoveNo() == -1 && node.getVisualDepth() > -1)) {
            View treeStone = TreeStone.create(getContext(), node);
            if (node.getMoveNo() == -1) {
                treeStone = new StartStone(getContext());
                ((StartStone) treeStone).setNode(node);
                Log.e("MoveTreeView", "Add StartStone:" + node.getNodeNo() + " " + node.getVisualDepth());
            } else {
                Log.e("MoveTreeView", "Add TreeStone:" + node.getNodeNo() + " " + node.getVisualDepth());
            }
            treeStone.setOnClickListener(v -> {
                Log.e("MoveTreeView", "Click:" + node);
            });
            addElement(treeStone, node.getNodeNo(), node.getVisualDepth());
        }

        // and recursively draw the next node on this line of play
        if (node.getNextNode() != null) {
            buildMoveTree(node.getNextNode(), depth + node.getVisualDepth());
        }

        // populate the children also
        if (node.hasChildren()) {
            Set<GameNode> children = node.getChildren();

            // will determine whether the glue stone should be a single
            // diagonal or a multiple (diagonal and vertical)
            GlueStoneType gStoneType = children.size() > 1 ? GlueStoneType.MULTIPLE : GlueStoneType.DIAGONAL;

            for (Iterator<GameNode> ite = children.iterator(); ite.hasNext(); ) {
                GameNode childNode = ite.next();

                // the last glue shouldn't be a MULTIPLE
                if (GlueStoneType.MULTIPLE.equals(gStoneType) && !ite.hasNext()) {
                    gStoneType = GlueStoneType.DIAGONAL;
                }

                // the visual lines can also be under a the first triangle
                int nodeVisualDepth = node.getVisualDepth();
                int moveNo = node.getNodeNo();

                if (moveNo == -1) {
                    moveNo = 0;
                    nodeVisualDepth = 0;
                } else if (nodeVisualDepth == -1) {
                    nodeVisualDepth = 0;
                }

                // also draw all the "missing" glue stones
                for (int i = nodeVisualDepth + 1; i < childNode.getVisualDepth(); i++) {
                    Log.e("MoveTreeView", "Add GlueStone:" + GlueStoneType.VERTICAL + " " + moveNo + " " + i);
                    GlueStone stone = new GlueStone(getContext());
                    stone.setType(GlueStoneType.VERTICAL);
                    addElement(stone, moveNo, i);
                }

                // glue stone for the node
                Log.e("MoveTreeView", "Add GlueStone:" + gStoneType + " " + moveNo + " " + childNode.getVisualDepth());
                GlueStone stone = new GlueStone(getContext());
                stone.setType(gStoneType);
                addElement(stone, moveNo, childNode.getVisualDepth());

                // and recursively draw the actual node
                buildMoveTree(childNode, depth + childNode.getVisualDepth());
            }
        }
    }

    private void addElement(View element, int moveNo, int depth) {
        SparseArray<View> array = null;
        if (depth >= 0 && depth < mElements.size()) {
            array = mElements.get(depth);
        }
        if (array == null) {
            array = new SparseArray<>();
            mElements.add(depth, array);
        }
        View view = array.get(moveNo);
        if (view instanceof GlueStone) {
            if (((GlueStone) view).getType() == GlueStoneType.MULTIPLE) {
                return;
            }
        }
        array.put(moveNo, element);
    }
}
