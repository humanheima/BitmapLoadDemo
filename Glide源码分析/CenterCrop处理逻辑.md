
```java
public static Bitmap centerCrop(@NonNull BitmapPool pool, @NonNull Bitmap inBitmap, int width, int height) {

    //注释0处，宽高比一致，直接返回原图    
    if (inBitmap.getWidth() == width && inBitmap.getHeight() == height) {
        return inBitmap;
    }
    // From ImageView/Bitmap.createScaledBitmap.
    final float scale;
    final float dx;
    final float dy;
    Matrix m = new Matrix();
    //注释1处，条件满足，即输入位图“更宽”。
    if (inBitmap.getWidth() * height > width * inBitmap.getHeight()) {
        //注释2处，计算缩放比例
        scale = (float) height / (float) inBitmap.getHeight();
        //注释3处，dx 是小于0的。，会向左移动，dx 是大于0的，则会向右移动。
        //这里向左移动一半
        dx = (width - inBitmap.getWidth() * scale) * 0.5f;
        dy = 0;
    } else {
        //注释4处，输入位图更高，以宽度为基准
        scale = (float) width / (float) inBitmap.getWidth();
        dx = 0;
        //注释5处，dy 是小于0的，则会向上移动，dy 是大于0的，则会向下移动。
        //这里向上移动一半
        dy = (height - inBitmap.getHeight() * scale) * 0.5f;
    }
    //先缩放
    m.setScale(scale, scale);
    //再移动
    m.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));

    Bitmap result = pool.get(width, height, getNonNullConfig(inBitmap));
    // We don't add or remove alpha, so keep the alpha setting of the Bitmap we were given.
    TransformationUtils.setAlpha(inBitmap, result);

    applyMatrix(inBitmap, result, m);
    return result;
}
```

inBitmap.getWidth() * height > width * inBitmap.getHeight()

等价于 inBitmap.getWidth() / inBitmap.getHeight() > width / height

说明输入位图的宽高比大于目标宽高比。即输入位图“更宽”。比如 inBitmap 宽高比是 5:2，
width / height = 3:2。那么应该：

1. 以高为基准，先缩放，让 inBitmap 的高 = height。缩放比例是：scale = (float) height / (float) inBitmap.getHeight(); scale，有可能大于0，有可能小于0。

注释3处，dx 是小于0的。会向左移动。在水平方向上居中剪裁。

注释4处，输入位图更高，以宽度为基准。

注释5处，dy 是小于0的，则会向上移动。在水平方向上居中剪裁。