package com.hm.bitmaploadexample.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hm.bitmaploadexample.R;

/**
 * Created by Administrator on 2017/1/8.
 */
public class FutureStudioView extends FrameLayout {

    private ImageView iv;
    private TextView tv;

    public FutureStudioView(Context context) {
        this(context, null);
    }

    public FutureStudioView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FutureStudioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);

    }

    private void initialize(Context context) {
        inflate(context, R.layout.custom_view_futurestudio, this);

        iv = findViewById(R.id.custom_view_image);
        tv = findViewById(R.id.custom_text_view);
    }

    public void setImage(Drawable drawable) {
        iv = findViewById(R.id.custom_view_image);
        iv.setImageDrawable(drawable);
    }

}
