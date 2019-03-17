package cn.ezandroid.sgf.demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import cn.ezandroid.lib.sgf.parser.GameNode;

/**
 * StartStone
 *
 * @author like
 * @date 2019-03-15
 */
public class StartStone extends View {

    private GameNode mNode;
    private Paint mPaint;

    public StartStone(Context context) {
        super(context);
    }

    public StartStone(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNode(GameNode node) {
        mNode = node;
    }

    public GameNode getNode() {
        return mNode;
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
        mPaint.setColor(Color.YELLOW);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(width / 2f, height / 2f, circleRadius, mPaint);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(width / 2f, height / 2f, circleRadius, mPaint);
        canvas.drawLine(width - (width - circleRadius * 2) / 2f, height / 2f, width, height / 2f, mPaint);
        mPaint.setColor(Color.WHITE);
        String s = "S";
        float textSize = width / 2f;
        mPaint.setTextSize(textSize);
        float textWidth = mPaint.measureText(s);
        Rect textRect = new Rect();
        mPaint.getTextBounds(s, 0, s.length(), textRect);
        canvas.drawText(s, (width - textWidth) / 2f, (height + textRect.height()) / 2f, mPaint);
    }
}
