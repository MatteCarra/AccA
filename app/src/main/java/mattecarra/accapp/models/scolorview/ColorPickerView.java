package mattecarra.accapp.models.scolorview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import mattecarra.accapp.R;

/**
 * This class defines the color picker view. You can embed it in your own dialog on anywhere else
 * where a regular Android View may fit. Please see the readme for the feature list.
 */

public class ColorPickerView extends FrameLayout
{
    private Bitmap mWhiteBitmap;
    private Bitmap mRingBitmap;
    private Bitmap mLineBitmap;

    private Point mTempPoint = new Point();
    private TextWatcher mTextWatcher;
    public ColorObserver observer;
    private boolean hasAlpha = false;

    private int mARGB;
    private int mHueColor;
    private int mSaturationColor;

    private float mYCoordHue;
    private float mXCoordSaturation, mYCoordSaturation;
    private float mYCoordAlpha;

    private TextView prefixView;
    private TextView hexView;

    public ColorPickerView(Context context)
    {
        this(context, null, 0);
    }

    public ColorPickerView(Context context,  @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerView);
        int startColor = typedArray.getInt(R.styleable.ColorPickerView_startColor, Color.BLACK);
        hasAlpha = typedArray.getBoolean(R.styleable.ColorPickerView_enableAlpha, false);
        boolean enableBrightness = typedArray.getBoolean(R.styleable.ColorPickerView_enableBrightness, false);
        boolean onlyUpdateOnTouchEventUp = typedArray.getBoolean(R.styleable.ColorPickerView_onlyUpdateOnTouchEventUp, false);
        typedArray.recycle();

        mWhiteBitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mWhiteBitmap);
        canvas.drawColor(0xffffffff);

        mRingBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.colorpicker_ring);
        mLineBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.colorpicker_line);

        LayoutInflater.from(context).inflate(R.layout.colorpicker, this);
        View hue = findViewById(R.id.id_settings_dialog_color_hue);
        View saturation = findViewById(R.id.id_settings_dialog_color_saturation);
        View alpha = findViewById(R.id.id_settings_dialog_color_alpha);
        ColorView preview = findViewById(R.id.id_settings_dialog_color_preview);
        prefixView = findViewById(R.id.id_settings_dialog_color_hex_prefix);
        hexView = findViewById(R.id.id_settings_dialog_color_hex);

        if (!hasAlpha) alpha.setVisibility(View.GONE);

        setColorVariables(startColor);
        preview.setColor(startColor);
        prefixView.setText(hasAlpha ? "#" : "#FF");
        hexView.setText(String.format("%08X", startColor).substring(hasAlpha ? 0 : 2));

        //--------------------------------------------------------------

        hue.setOnTouchListener((v, event) ->
        {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN
             || event.getActionMasked() == MotionEvent.ACTION_UP
             || event.getActionMasked() == MotionEvent.ACTION_CANCEL)
            {
                hue.getParent().requestDisallowInterceptTouchEvent(event.getActionMasked() == MotionEvent.ACTION_DOWN);
            }

            mYCoordHue = Math.max(0, Math.min(hue.getHeight(), event.getY()));
            mHueColor = getHueColor((int) (mYCoordHue + 0.5f), hue.getHeight());
            hue.invalidate();

            mSaturationColor = getSaturationColor(mHueColor, (int) (mXCoordSaturation + 0.5f), (int) (mYCoordSaturation + 0.5f), saturation.getWidth(), saturation.getHeight());
            saturation.invalidate();
            alpha.invalidate();

            mARGB = getAlphaColor(mSaturationColor, (int) (mYCoordAlpha + 0.5f), (hasAlpha ? alpha.getHeight() : 0));
            preview.setColor(mARGB);
            if (observer != null) observer.onColor(mARGB, true);

            hexView.removeTextChangedListener(mTextWatcher);
            hexView.setText(String.format("%08X", mARGB).substring(hasAlpha ? 0 : 2));
            hexView.addTextChangedListener(mTextWatcher);
            return true;
        });

        hue.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
        {
            int height = hue.getHeight();
            mYCoordHue = getHuePosition(mHueColor, height);
            hue.invalidate();
        });

        hue.setBackground(new Drawable()
        {
            private Paint mPaint = new Paint();
            private Rect mDstRect = new Rect();
            private int[] mColors = new int[]{0xffff0000, 0xffff00ff, 0xff0000ff, 0xff00ffff, 0xff00ff00, 0xffffff00, 0xffff0000};

            @Override
            public void draw(Canvas canvas)
            {
                int width = getBounds().width();
                int height = getBounds().height();

                mPaint.setShader(new LinearGradient(0, 0, 0, height, mColors, null, Shader.TileMode.CLAMP));
                canvas.drawRect(0, 0, width, height, mPaint);

                mDstRect.set(0, (int) (mYCoordHue - mLineBitmap.getHeight() / 2), width / 2 - mRingBitmap.getWidth() / 2, (int) (mYCoordHue + mLineBitmap.getHeight() / 2));
                canvas.drawBitmap(mLineBitmap, null, mDstRect, null);
                mDstRect.set(width / 2 + mRingBitmap.getWidth() / 2, (int) (mYCoordHue - mLineBitmap.getHeight() / 2), width, (int) (mYCoordHue + mLineBitmap.getHeight() / 2));
                canvas.drawBitmap(mLineBitmap, null, mDstRect, null);
                canvas.drawBitmap(mRingBitmap, width / 2 - mRingBitmap.getWidth() / 2, mYCoordHue - mRingBitmap.getHeight() / 2, null);
            }

            @Override
            public void setAlpha(int alpha) { }

            @Override
            public void setColorFilter(ColorFilter colorFilter) { }

            @Override
            public int getOpacity() { return PixelFormat.OPAQUE; }
        });

        saturation.setOnTouchListener((v, event) ->
        {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN
             || event.getActionMasked() == MotionEvent.ACTION_UP
             || event.getActionMasked() == MotionEvent.ACTION_CANCEL)
            {
                saturation.getParent().requestDisallowInterceptTouchEvent(event.getActionMasked() == MotionEvent.ACTION_DOWN);
            }

            mXCoordSaturation = Math.max(0, Math.min(saturation.getWidth(), event.getX()));
            mYCoordSaturation = Math.max(0, Math.min(saturation.getHeight(), event.getY()));
            mSaturationColor = getSaturationColor(mHueColor, (int) (mXCoordSaturation + 0.5f), (int) (mYCoordSaturation + 0.5f), saturation.getWidth(), saturation.getHeight());
            saturation.invalidate();
            alpha.invalidate();

            mARGB = getAlphaColor(mSaturationColor, (int) (mYCoordAlpha + 0.5f), (hasAlpha ? alpha.getHeight() : 0));
            preview.setColor(mARGB);
            if (observer != null) observer.onColor(mARGB, true);

            hexView.removeTextChangedListener(mTextWatcher);
            hexView.setText(String.format("%08X", mARGB).substring(hasAlpha ? 0 : 2));
            hexView.addTextChangedListener(mTextWatcher);
            return true;
        });

        saturation.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
        {
            int width = saturation.getWidth();
            int height = saturation.getHeight();
            getSaturationPosition(mSaturationColor, mHueColor, width, height, mTempPoint);
            mXCoordSaturation = mTempPoint.x;
            mYCoordSaturation = mTempPoint.y;
            saturation.invalidate();
        });

        saturation.setBackground(new Drawable()
        {
            private final int RESOLUTION = 8;
            private float[] verts = new float[((RESOLUTION + 1) * (RESOLUTION + 1)) * 2];
            private int[] colors = new int[((RESOLUTION + 1) * (RESOLUTION + 1))];
            private Paint mPaint;

            {
                mPaint = new Paint();
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
                mPaint.setColor(0xffffffff);
            }

            @Override
            public void draw(Canvas canvas)
            {
                int width = saturation.getWidth();
                int height = saturation.getHeight();

                int index = 0;

                for (int y = 0; y <= RESOLUTION; y++)
                {
                    for (int x = 0; x <= RESOLUTION; x++)
                    {
                        verts[index] = x * width / RESOLUTION;
                        verts[index + 1] = y * height / RESOLUTION;
                        colors[index / 2] = mix(0xff000000, mix(mHueColor, 0xffffffff, (float) verts[index] / width), (float) verts[index + 1] / height);
                        index += 2;
                    }
                }

                canvas.drawBitmapMesh(mWhiteBitmap, RESOLUTION, RESOLUTION, verts, 0, colors, 0, null);
                canvas.drawBitmap(mRingBitmap, mXCoordSaturation - mRingBitmap.getWidth() / 2, mYCoordSaturation - mRingBitmap.getHeight() / 2, null);
            }

            @Override
            public void setAlpha(int alpha) { }

            @Override
            public void setColorFilter(ColorFilter colorFilter) { }

            @Override
            public int getOpacity() { return PixelFormat.OPAQUE; }
        });

        alpha.setOnTouchListener((v, event) ->
        {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN
             || event.getActionMasked() == MotionEvent.ACTION_UP
             || event.getActionMasked() == MotionEvent.ACTION_CANCEL)
            {
                alpha.getParent().requestDisallowInterceptTouchEvent(event.getActionMasked() == MotionEvent.ACTION_DOWN);
            }

            mYCoordAlpha = Math.max(0, Math.min(alpha.getHeight(), event.getY()));
            alpha.invalidate();

            mARGB = getAlphaColor(mSaturationColor, (int) (mYCoordAlpha + 0.5f), (hasAlpha ? alpha.getHeight() : 0));
            preview.setColor(mARGB);
            if (observer != null) observer.onColor(mARGB, true);

            hexView.removeTextChangedListener(mTextWatcher);
            hexView.setText(String.format("%08X", mARGB).substring(hasAlpha ? 0 : 2));
            hexView.addTextChangedListener(mTextWatcher);
            return true;
        });

        hue.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
        {
            int height = hue.getHeight();
            mYCoordAlpha = getAlphaPosition(mARGB, height);
            hue.invalidate();
        });

        alpha.setBackground(new Drawable()
        {
            private Paint mPaint = new Paint();
            private Rect mDstRect = new Rect();

            private BitmapShader mShader;
            {
                mShader = new BitmapShader(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.checker), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                mPaint.setColor(0xffffffff);
            }

            @Override
            public void draw(Canvas canvas)
            {
                int width = alpha.getWidth();
                int height = alpha.getHeight();

                mPaint.setShader(mShader);
                canvas.drawRect(0, 0, width, height, mPaint);

                int[] colors = new int[]{mSaturationColor, getColorWithAlpha(mSaturationColor, 0)};
                Shader shader = new LinearGradient(0, 0, 0, height, colors, null, Shader.TileMode.CLAMP);
                mPaint.setShader(shader);
                canvas.drawRect(0, 0, width, height, mPaint);

                mDstRect.set(0, (int) (mYCoordAlpha - mLineBitmap.getHeight() / 2), width / 2 - mRingBitmap.getWidth() / 2, (int) (mYCoordAlpha + mLineBitmap.getHeight() / 2));
                canvas.drawBitmap(mLineBitmap, null, mDstRect, null);
                mDstRect.set(width / 2 + mRingBitmap.getWidth() / 2, (int) (mYCoordAlpha - mLineBitmap.getHeight() / 2), width, (int) (mYCoordAlpha + mLineBitmap.getHeight() / 2));
                canvas.drawBitmap(mLineBitmap, null, mDstRect, null);
                canvas.drawBitmap(mRingBitmap, width / 2 - mRingBitmap.getWidth() / 2, mYCoordAlpha - mRingBitmap.getHeight() / 2, null);
            }

            @Override
            public void setAlpha(int alpha) { }

            @Override
            public void setColorFilter(ColorFilter colorFilter) { }

            @Override
            public int getOpacity()
            {
                return PixelFormat.OPAQUE;
            }
        });

        InputFilter inputFilter = (source, start, end, dest, dstart, dend) ->
        {
            for (int i = start; i < end; i++) if ("1234567890ABCDEFabcdef".indexOf(source.charAt(i)) == -1) return "";
            if ((source.length()+dest.length()) > (hasAlpha ? 8 : 6)) return "";
            return null;
        };

        hexView.setFilters(new InputFilter[]{inputFilter, new InputFilter.AllCaps()});

        mTextWatcher = new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                String string = hexView.getText().toString();

                if (string.length() > 0)
                {
                    setColorVariables((int) Long.parseLong(string, 16) | (string.length() <= 6 ? 0xFF000000 : 0));

                    mYCoordHue = getHuePosition(mHueColor, hue.getHeight());
                    hue.invalidate();

                    getSaturationPosition(mSaturationColor, mHueColor, saturation.getWidth(), saturation.getHeight(), mTempPoint);
                    mXCoordSaturation = mTempPoint.x;
                    mYCoordSaturation = mTempPoint.y;

                    mYCoordAlpha = getAlphaPosition(mARGB, alpha.getHeight());

                    hue.invalidate();
                    saturation.invalidate();
                    alpha.invalidate();
                    preview.setColor(mARGB);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        };

        hexView.addTextChangedListener(mTextWatcher);
    }

    private int mix( int color1, int color2, float fraction )
    {
        final float oneMinusFraction = 1.0f - fraction;
        int a = ((int)(((float)(color1 >> 24 & 0xff )*fraction) + ((float)(color2 >> 24 & 0xff )*oneMinusFraction))) & 0xff;
        int r = ((int)(((float)(color1 >> 16 & 0xff )*fraction) + ((float)(color2 >> 16 & 0xff )*oneMinusFraction))) & 0xff;
        int g = ((int)(((float)(color1 >> 8 & 0xff )*fraction) + ((float)(color2 >> 8 & 0xff )*oneMinusFraction))) & 0xff;
        int b = ((int)(((float)(color1 & 0xff )*fraction) + ((float)(color2 & 0xff )*oneMinusFraction))) & 0xff;
        return a << 24 | r << 16 | g << 8 | b;
    }

    private int getColorWithAlpha(int color, int alpha)
    {
        return color & 0x00ffffff | alpha<<24;
    }

    private static int getHueColor(int position, int length)
    {
        float unitLength = length/6.0f;

        if(position < unitLength)
        {
            float normalizedPosition = position/unitLength;
            return android.graphics.Color.rgb(255, 0, (int)(255*normalizedPosition));
        }
        else if(position < 2*unitLength)
        {
            float normalizedPosition = (position - unitLength)/unitLength;
            return android.graphics.Color.rgb((int)(255*(1-normalizedPosition)), 0, 255);
        }
        else if(position < 3*unitLength)
        {
            float normalizedPosition = (position - 2*unitLength)/unitLength;
            return android.graphics.Color.rgb(0, (int)(255*normalizedPosition), 255);
        }
        else if(position < 4*unitLength)
        {
            float normalizedPosition = (position - 3*unitLength)/unitLength;
            return android.graphics.Color.rgb(0, 255, (int)(255*(1-normalizedPosition)));
        }
        else if(position < 5*unitLength)
        {
            float normalizedPosition = (position - 4*unitLength)/unitLength;
            return android.graphics.Color.rgb((int)(255*(normalizedPosition)), 255, 0);
        }
        else
        {
            float normalizedPosition = (position - 5*unitLength)/unitLength;
            return android.graphics.Color.rgb(255, (int)(255*(1-normalizedPosition)), 0);
        }
    }

    private static int getHuePosition(int hueColor, int length)
    {
        float red = android.graphics.Color.red(hueColor);
        float green = android.graphics.Color.green(hueColor);
        float blue = android.graphics.Color.blue(hueColor);

        if(red == 255)
        {
            if(blue > 0){ return (int)(length*(blue/255/6) + 0.5f); }
            else{/*green > 0*/return (int)(length*(5f/6 + (1-green/255)/6) + 0.5f); }
        }
        else if(blue == 255)
        {
            if(red > 0){ return (int)(length*(1f/6 + (1-red/255)/6) + 0.5f); }
            else{/*green > 0*/return (int)(length*(2f/6 + green/255/6) + 0.5f); }
        }
        else {//green==255
            if(blue > 0){ return (int)(length*(3f/6 + (1-blue/255)/6) + 0.5f); }
            else{/*red > 0*/return (int)(length*(4f/6 + red/255/6) + 0.5f); }
        }
    }

    private int getSaturationColor(int hueColor, int x, int y, int width, int height)
    {
        return mix(0xff000000, mix(mHueColor, 0xffffffff, (float)x/width), (float)y/height);
    }

    private static void getSaturationPosition(int saturationColor, int hueColor, int width, int height, Point position)
    {
        int redSaturation = android.graphics.Color.red(saturationColor);
        int greenSaturation = android.graphics.Color.green(saturationColor);
        int blueSaturation = android.graphics.Color.blue(saturationColor);
        int redHue = android.graphics.Color.red(hueColor);
        int greenHue = android.graphics.Color.green(hueColor);

        if(redHue == 0){ position.x = width * (255-redSaturation) / 255; }
        else if(greenHue == 0){ position.x = width * (255-greenSaturation) / 255; }
        else{/*blueHue == 0*/ position.x = width * (255-blueSaturation) / 255; }

        if(redHue == 255){ position.y = height * (255-redSaturation) / 255; }
        else if(greenHue == 255){ position.y = height * (255-greenSaturation) / 255; }
        else{/*/blueHue == 255*/position.y = height * (255-blueSaturation) / 255; }
    }

    private int getAlphaColor(int saturationColor, int y, int height)
    {
        int alpha = (height==0)? 255: (255*(height-y)/height);
        int red = android.graphics.Color.red(saturationColor);
        int green = android.graphics.Color.green(saturationColor);
        int blue = android.graphics.Color.blue(saturationColor);
        return android.graphics.Color.argb(alpha, red, green, blue);
    }

    private int getAlphaPosition(int finalColor, int height)
    {
        return (255-android.graphics.Color.alpha(finalColor))*height/255;
    }

    public void setColor(int color)
    {
        if (prefixView != null) prefixView.setText(hasAlpha ? "#" : "#FF");
        if (hexView != null) hexView.setText(String.format("%08X", color).substring(hasAlpha ? 0 : 2));
        if (observer != null) observer.onColor(color, false);
    }

    private void setColorVariables(int color)
    {
        int red = android.graphics.Color.red(color);
        int green = android.graphics.Color.green(color);
        int blue = android.graphics.Color.blue(color);
        int minVal = Math.min(red, Math.min(green, blue));

        int redHue = red - minVal;
        int greenHue = green - minVal;
        int blueHue = blue - minVal;
        int maxVal = Math.max(redHue, Math.max(greenHue, blueHue));

        if (maxVal > 0)
        {
            redHue = 255 * redHue / maxVal;
            greenHue = 255 * greenHue / maxVal;
            blueHue = 255 * blueHue / maxVal;
            mHueColor = android.graphics.Color.argb(255, redHue, greenHue, blueHue);
        }
        else mHueColor = android.graphics.Color.argb(255, 255, 0, 0);

        mSaturationColor = android.graphics.Color.argb(255, red, green, blue);
        mARGB = color;
    }

    public int getColor()
    {
        return mARGB;
    }

    public interface ColorObserver
    {
        /**
         * Color has changed.
         * @param color the new color
         * @param fromUser if this color is changed by user (programmatically)
         */

        void onColor(int color, boolean fromUser);
    }
}
