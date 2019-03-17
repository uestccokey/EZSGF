package cn.ezandroid.sgf.demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * GlueStone
 *
 * @author like
 * @date 2019-03-16
 */
public class GlueStone extends View {

    private GlueStoneType mType;
    private Paint mPaint;

    public GlueStone(Context context) {
        super(context);
    }

    public GlueStone(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setType(GlueStoneType type) {
        mType = type;
    }

    public GlueStoneType getType() {
        return mType;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (mPaint == null) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(Color.BLACK);
        }
        switch (mType) {
            case DIAGONAL:
                canvas.drawLine(width / 2f, 0, width, height / 2f, mPaint);
                break;
            case MULTIPLE:
                canvas.drawLine(width / 2f, 0, width / 2f, height, mPaint);
                canvas.drawLine(width / 2f, 0, width, height / 2f, mPaint);
                break;
            case VERTICAL:
                canvas.drawLine(width / 2f, 0, width / 2f, height, mPaint);
                break;
        }
    }
}
