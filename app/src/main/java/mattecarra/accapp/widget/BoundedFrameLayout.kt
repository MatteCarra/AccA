package mattecarra.accapp.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet

@SuppressLint("CustomViewStyleable")
class BoundedFrameLayout(context: Context, attributeSet: AttributeSet?)
    : androidx.cardview.widget.CardView(context, attributeSet)
{
    private var gwidth = 0
    private var gheight = 0

    public override fun onMeasure(i: Int, i2: Int)
    {
        var pwidth = i
        var pheight = i2

        if (gwidth > 0 && gwidth < MeasureSpec.getSize(pwidth)) pwidth = MeasureSpec.makeMeasureSpec(gwidth, MeasureSpec.getMode(pwidth))
        if (gheight > 0 && gheight < MeasureSpec.getSize(pheight)) pheight = MeasureSpec.makeMeasureSpec(gheight, MeasureSpec.getMode(pheight))

        super.onMeasure(pwidth, pheight)
    }

    fun setMaxWidth(i: Int)
    {
        gwidth = i
        invalidate()
    }

    init
    {
        val typedArray: TypedArray = context.obtainStyledAttributes(attributeSet, mattecarra.accapp.R.styleable.BoundedView)

        try
        {
            gwidth = typedArray.getDimensionPixelSize(mattecarra.accapp.R.styleable.BoundedView_bounded_width, 0)
            gheight = typedArray.getDimensionPixelSize(mattecarra.accapp.R.styleable.BoundedView_bounded_height, 0)
            typedArray.recycle()
        }
        catch (th: Throwable)
        {
            typedArray.recycle()
        }
    }
}