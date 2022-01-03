package mattecarra.accapp.models.scolorview;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import mattecarra.accapp.R;

/**
 * This is a color preview View, that is used by ColorPickerView.
 * This class may be used as a standalone color preview also.
 */
public class ColorView extends View {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private BitmapShader mShader;
    private Path mClipPath = new Path();
    private int mColor;

    {
        mShader = new BitmapShader(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.checker), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
    }

    public ColorView(Context context) {
        super(context);
    }

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        int width = getWidth();
        int height = getHeight();

        int radius = Math.min(width, height) / 2;
        int borderWidth = radius / 5;
        mClipPath.reset();
        mClipPath.addRect(borderWidth-1, borderWidth-1, width - borderWidth + 1, height - borderWidth + 1, Direction.CW);
        //api21//mClipPath.addRoundRect(borderWidth - 1, borderWidth - 1, width - borderWidth + 1, height - borderWidth + 1, radius, radius, Direction.CW);

        canvas.save();
        canvas.clipPath(mClipPath);

        mPaint.setStyle(Style.FILL);
        mPaint.setShader(mShader);
        mPaint.setColor(0xffffffff);
        canvas.drawRect(0, 0, width, height, mPaint);

        canvas.restore();
        mPaint.setShader(null);
        mPaint.setColor(mColor);
        canvas.drawRect(borderWidth - 2, borderWidth - 2, width - borderWidth + 2, height - borderWidth + 2, mPaint);
        //api21//canvas.drawRoundRect(borderWidth - 2, borderWidth - 2, width - borderWidth + 2, height - borderWidth + 2, radius, radius, mPaint);

        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(borderWidth / 3);
        mPaint.setColor(0xff000000);
        canvas.drawRect(borderWidth / 4, borderWidth / 4, width - borderWidth / 4, height - borderWidth / 4, mPaint);
        //api21//canvas.drawRoundRect(borderWidth / 4, borderWidth / 4, width - borderWidth / 4, height - borderWidth / 4, radius, radius, mPaint);
        mPaint.setColor(0xffffffff);
        canvas.drawRect(borderWidth * 3 / 4 + 1, borderWidth * 3 / 4 + 1, width - borderWidth * 3 / 4 - 1, height - borderWidth * 3 / 4 - 1, mPaint);
        //api21//canvas.drawRoundRect(borderWidth * 3 / 4 + 1, borderWidth * 3 / 4 + 1, width - borderWidth * 3 / 4 - 1, height - borderWidth * 3 / 4 - 1, radius, radius, mPaint);
    }

    public void setColor(int color)
    {
        mColor = color;
        invalidate();
    }

    public int getColor()
    {
        return mColor;
    }
}
