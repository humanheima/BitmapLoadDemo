#BitmapLoadDemo
《Android 开发艺术探索》学习

BitmapFactory解析图片的时候，inSampleSize的计算结果是2的次幂
```java
private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
```

Load large image.

[subsampling-scale-image-view](https://github.com/davemorrissey/subsampling-scale-image-view)


### 给一个高度是wrap_content的ImageView设置图片，最终Imageview的高度是图片的原始高度+padding，而不是wrap_content的源码分析

ImageView 的 setImageDrawable 方法
```java
public void setImageDrawable(@Nullable Drawable drawable) {
    if (mDrawable != drawable) {
        mResource = 0;
        mUri = null;

        final int oldWidth = mDrawableWidth;
        final int oldHeight = mDrawableHeight;

        //注释1处，这里会根更新 mDrawableWidth 和 mDrawableHeight 为图片的原始宽高
        updateDrawable(drawable);

        if (oldWidth != mDrawableWidth || oldHeight != mDrawableHeight) {
            //注释2处，重新测量
            requestLayout();
        }
        invalidate();
    }
}
```

Imageview 的 updateDrawable 方法

```java
private void updateDrawable(Drawable d) {
    if (d != mRecycleableBitmapDrawable && mRecycleableBitmapDrawable != null) {
        mRecycleableBitmapDrawable.setBitmap(null);
    }

    boolean sameDrawable = false;

    if (mDrawable != null) {
        sameDrawable = mDrawable == d;
        mDrawable.setCallback(null);
        unscheduleDrawable(mDrawable);
        if (!sCompatDrawableVisibilityDispatch && !sameDrawable && isAttachedToWindow()) {
            mDrawable.setVisible(false, false);
        }
    }

    mDrawable = d;

    if (d != null) {
        d.setCallback(this);
        d.setLayoutDirection(getLayoutDirection());
        if (d.isStateful()) {
            d.setState(getDrawableState());
        }
        if (!sameDrawable || sCompatDrawableVisibilityDispatch) {
            final boolean visible = sCompatDrawableVisibilityDispatch ? getVisibility() == VISIBLE : isAttachedToWindow() && getWindowVisibility() == VISIBLE && isShown();
            d.setVisible(visible, true);
        }
        d.setLevel(mLevel);
        //注释1处，这里会设置 mDrawableWidth 和 mDrawableHeight 为图片的原始宽高
        mDrawableWidth = d.getIntrinsicWidth();
        mDrawableHeight = d.getIntrinsicHeight();
        applyImageTint();
        applyColorMod();

        configureBounds();
    } else {
        mDrawableWidth = mDrawableHeight = -1;
    }
}
```

Imageview 的 onMeasure 方法

```java
@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    resolveUri();
    int w;
    int h;

    // Desired aspect ratio of the view's contents (not including padding)
    float desiredAspect = 0.0 f;

    // We are allowed to change the view's width
    boolean resizeWidth = false;

    // We are allowed to change the view's height
    boolean resizeHeight = false;

    final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

    if (mDrawable == null) {
        // If no drawable, its intrinsic size is 0.
        mDrawableWidth = -1;
        mDrawableHeight = -1;
        w = h = 0;
    } else {
        //注释1处，如果有图片的宽高，则直接使用，最后ImageView的宽高就是图片的宽高+padding
        w = mDrawableWidth;
        h = mDrawableHeight;
        if (w <= 0) w = 1;
        if (h <= 0) h = 1;

        //...
    }

    final int pleft = mPaddingLeft;
    final int pright = mPaddingRight;
    final int ptop = mPaddingTop;
    final int pbottom = mPaddingBottom;

    int widthSize;
    int heightSize;

    if (resizeWidth || resizeHeight) {
        //...
    } else {
        /* We are either don't want to preserve the drawables aspect ratio,
           or we are not allowed to change view dimensions. Just measure in
           the normal way.
        */
        w += pleft + pright;
        h += ptop + pbottom;

        w = Math.max(w, getSuggestedMinimumWidth());
        h = Math.max(h, getSuggestedMinimumHeight());

        widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
        heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);
    }

    setMeasuredDimension(widthSize, heightSize);
}
```
