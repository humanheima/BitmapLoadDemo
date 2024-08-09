### 布局文件根布局是 RelativeLayout

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/root_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".activity.BitmapHeightProblemTestActivity">

    <com.hm.bitmaploadexample.widget.TestImageView
        android:id="@+id/iv_big_image"
        android:layout_width="match_parent"
        android:layout_height="900dp"
        android:scaleType="centerCrop" />

    <Button
        android:id="@+id/btn_height"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:layout_above="@+id/et_input"
        android:text="点击获取高度信息"
        android:textAllCaps="false" />

    <EditText
        android:id="@+id/et_input"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:layout_alignParentBottom="true"
        android:background="@null"
        android:hint="测试键盘弹起的时候的ImageView高度的问题" />

</RelativeLayout>
```

### 键盘模式 SOFT_INPUT_ADJUST_RESIZE 

键盘没有弹起

```xml
10:51:39.353 BitmapHe...blemTest com.hm.bitmaploadexample           I  setOnKeyboardListener: decorView height = 3120 top = 0 bottom = 3120
10:51:39.353 BitmapHe...blemTest com.hm.bitmaploadexample           I  setOnKeyboardListener: 根布局 height = 2891  top = 0 bottom = 2891
10:51:39.353 BitmapHe...blemTest com.hm.bitmaploadexample           I  setOnKeyboardListener: 大ImageView height = 2891  top = 0 bottom = 2891
```

键盘弹起的时候，decorView 高度不变，根布局RelativeLayout高度变小，ImageView的高度变小。

```xml
10:47:09.592 BitmapHe...blemTest com.hm.bitmaploadexample           I  setOnKeyboardListener: decorView height = 3120 top = 0 bottom = 3120
10:47:09.592 BitmapHe...blemTest com.hm.bitmaploadexample           I  setOnKeyboardListener: 根布局 height = 1799  top = 0 bottom = 1799
10:47:09.592 BitmapHe...blemTest com.hm.bitmaploadexample           I  setOnKeyboardListener: 大ImageView height = 1799  top = 0 bottom = 1799
```


### 键盘模式 SOFT_INPUT_ADJUST_PAN 


键盘没有弹起

```xml
10:53:01.829 BitmapHe...blemTest com.hm.bitmaploadexample           I  setOnKeyboardListener: decorView height = 3120 top = 0 bottom = 3120
10:53:01.829 BitmapHe...blemTest com.hm.bitmaploadexample           I  setOnKeyboardListener: 根布局 height = 2891  top = 0 bottom = 2891
10:53:01.829 BitmapHe...blemTest com.hm.bitmaploadexample           I  setOnKeyboardListener: 大ImageView height = 2891  top = 0 bottom = 2891

```


键盘弹起的时候，decorView 高度不变，根布局RelativeLayout高度不变，ImageView的高度不变。

```xml
10:53:56.230 BitmapHe...blemTest com.hm.bitmaploadexample           I  setOnKeyboardListener: decorView height = 3120 top = 0 bottom = 3120
10:53:56.230 BitmapHe...blemTest com.hm.bitmaploadexample           I  setOnKeyboardListener: 根布局 height = 2891  top = 0 bottom = 2891
10:53:56.230 BitmapHe...blemTest com.hm.bitmaploadexample           I  setOnKeyboardListener: 大ImageView height = 2891  top = 0 bottom = 2891

```

### 问题来了，这两者是怎么起作用的呢？

