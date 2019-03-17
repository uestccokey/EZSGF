package cn.ezandroid.sgf.demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import cn.ezandroid.lib.sgf.board.StoneState;
import cn.ezandroid.lib.sgf.parser.GameNode;

/**
 * TreeStone
 *
 * @author like
 * @date 2019-03-15
 */
public class TreeStone extends View {

    private StoneState mSquareState;

    private GameNode mNode;
    private Paint mPaint;

    private boolean mDrawLeftArrow;
    private boolean mDrawRightArrow;

    public TreeStone(Context context) {
        super(context);
    }

    public TreeStone(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNode(GameNode node) {
        setNode(node, true, true);
    }

    public void setNode(GameNode node, boolean drawLeftArrow, boolean drawRightArrow) {
        mNode = node;
        this.mDrawLeftArrow = drawLeftArrow;
        this.mDrawRightArrow = drawRightArrow;

        if ("W".equals(node.getColor())) {
            this.mSquareState = StoneState.WHITE;
        } else {
            this.mSquareState = StoneState.BLACK;
        }

        this.mNode = node;
    }

    public GameNode getNode() {
        return mNode;
    }

    public static TreeStone create(Context context, GameNode node) {
        boolean drawLeftArrow = true;
        boolean drawRightArrow = true;

        // no left arrow if no move preceding
        if (node.getPrevNode() == null) {
            drawLeftArrow = false;
        }

        // no right arrow if no move following
        if (node.getNextNode() == null || (node.getNextNode() != null && !node.getNextNode().isMove())) {
            drawRightArrow = false;
        }

        TreeStone stone = new TreeStone(context);
        stone.setNode(node, drawLeftArrow, drawRightArrow);
        return stone;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (mPaint == null) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        float circleRadius = width * 2f / 5;
        switch (mSquareState) {
            case WHITE:
                mPaint.setColor(Color.WHITE);
                break;
            case BLACK:
                mPaint.setColor(Color.BLACK);
                break;
            default:
                break;
        }
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(width / 2f, height / 2f, circleRadius, mPaint);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(width / 2f, height / 2f, circleRadius, mPaint);
        if (mDrawLeftArrow) {
            canvas.drawLine(0, height / 2f, (width - circleRadius * 2) / 2f, height / 2f, mPaint);
        }
        if (mDrawRightArrow) {
            canvas.drawLine(width - (width - circleRadius * 2) / 2f, height / 2f, width, height / 2f, mPaint);
        }
        switch (mSquareState) {
            case WHITE:
                mPaint.setColor(Color.BLACK);
                break;
            case BLACK:
                mPaint.setColor(Color.WHITE);
                break;
            default:
                break;
        }
        String no = String.valueOf(mNode.getMoveNo());
        float textSize;
        if (no.length() <= 1) {
            textSize = width / 2f;
        } else if (no.length() <= 2) {
            textSize = width / 2.25f;
        } else {
            textSize = width / 2.5f;
        }
        mPaint.setTextSize(textSize);
        float textWidth = mPaint.measureText(no);
        Rect textRect = new Rect();
        mPaint.getTextBounds(no, 0, no.length(), textRect);
        canvas.drawText(no, (width - textWidth) / 2f, (height + textRect.height()) / 2f, mPaint);
        if (mNode.getProperty("C") != null) {
            mPaint.setColor(Color.BLACK);
            canvas.drawLine(0, height, width, height, mPaint);
        }
    }
}
