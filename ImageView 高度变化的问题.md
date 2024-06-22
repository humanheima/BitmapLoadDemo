### 使用约束布局

```kotlin
//和在xml中设置  android:windowSoftInputMode="adjustResize"
window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

```

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".activity.BitmapHeightProblemTestActivity">


    <com.hm.bitmaploadexample.widget.TestImageView
            android:id="@+id/iv_big_image"
            android:layout_width="0dp"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            android:scaleType="centerCrop"
            android:layout_height="800dp" />

    <Button
            android:layout_width="0dp"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            android:layout_height="48dp"
            app:layout_constraintBottom_toTopOf="@+id/et_input" />

    <EditText
            android:id="@+id/et_input"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintBottom_toBottomOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

先说下结论

1. 使用 `android:windowSoftInputMode="adjustResize"` 的时候，在 ConstraintLayout layout的时候，会有一个标记位 `PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT`。
然后会先 `onMeasure` 一次。

View 的 layout 方法。

```java
public void layout(int l, int t, int r, int b){
    if((mPrivateFlags3&PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT)!=0){
        //在layout的时候，如果有标记位 PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT ，会重新onMeasure。
        //这个标记位受 windowSoftInputMode 影响。
        onMeasure(mOldWidthMeasureSpec,mOldHeightMeasureSpec);
        mPrivateFlags3&=~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
    }
    //...
}
```

重新measure的时候，TestImageView 的 约束没有发生改变。 `app:layout_constraintTop_toTopOf="parent"` 。 其余的button 和 EditText 的 约束发生了改变。


注意，在这种场景下：键盘弹起的时候，DecorView 的top坐标，bottom坐标和 height并没有改变。


2. 键盘弹起的时候，最外层的约束布局的top坐标没有发生变化，还是0， bottom坐标 已经发生变化了，变成了 1799。但是大ImageView的bottom = 2800，没有发生变化。大 ImageView的约束是 `app:layout_constraintTop_toTopOf="parent"` 。

```java
17:08:10.705 BitmapHe...blemTest  I  setOnKeyboardListener: decorView height = 3120 top = 0 bottom = 3120
17:08:10.705 BitmapHe...blemTest  I  setOnKeyboardListener: 根布局约束布局 height = 1799  top = 0 bottom = 1799
17:08:10.705 BitmapHe...blemTest  I  setOnKeyboardListener: 大ImageView height = 2800  top = 0 bottom = 2800
```

3. 最外层的约束布局的 top没有变化，不会改变ImageView约束。最外层的约束布局的 bottom 已经发生变化，导致底部的EditText 和 Button的约束发生改变。

4. 现象就是，底部的 EditText 和 Button 会向上移动。但是 大ImageView 不会移动。

5. 这算不算约束布局的一个bug呢？毕竟键盘弹起的时候， 大ImageView的高度已经超过父级空间外层约束布局的高度了。

ConstraintLayout 的  layout 方法。

```java
@Override
protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    
    if (DEBUG) {
        System.out.println(mLayoutWidget.getDebugName() + " onLayout changed: " + changed + " left: " + left + " top: " + top + " right: " + right + " bottom: " + bottom + " (" + (right - left) + " x " + (bottom - top) + ")");
    }
    final int widgetsCount = getChildCount();
    final boolean isInEditMode = isInEditMode();
    for (int i = 0; i < widgetsCount; i++) {
        final View child = getChildAt(i);
        LayoutParams params = (LayoutParams) child.getLayoutParams();
        
        //注释1处，从 params 里获取 ConstraintWidget
        ConstraintWidget widget = params.widget;

        if (child.getVisibility() == GONE && !params.isGuideline && !params.isHelper && !params.isVirtualGroup && !isInEditMode) {
            // If we are in edit mode, let's layout the widget so that they are at "the right place"
            // visually in the editor (as we get our positions from layoutlib)
            continue;
        }
        if (params.isInPlaceholder) {
            continue;
        }
        //注释2处，获取 l,t,r,b 坐标，top坐标发生改变的时候，才会在竖直方向上发生变化。
        int l = widget.getX();
        
        int t = widget.getY();
        int r = l + widget.getWidth();
        int b = t + widget.getHeight();

        if (DEBUG) {
            if (child.getVisibility() != View.GONE && (child.getMeasuredWidth() != widget.getWidth() || child.getMeasuredHeight() != widget.getHeight())) {
                int deltaX = Math.abs(child.getMeasuredWidth() - widget.getWidth());
                int deltaY = Math.abs(child.getMeasuredHeight() - widget.getHeight());
                if (deltaX > 1 || deltaY > 1) {
                    System.out.println("child " + child + " measuredWidth " + child.getMeasuredWidth() + " vs " + widget.getWidth() + " x measureHeight " + child.getMeasuredHeight() + " vs " + widget.getHeight());
                }
            }
        }

        //注释3处，child 调用 layout 布局
        child.layout(l, t, r, b);
        if (child instanceof Placeholder) {
            Placeholder holder = (Placeholder) child;
            View content = holder.getContent();
            if (content != null) {
                content.setVisibility(VISIBLE);
                content.layout(l, t, r, b);
            }
        }
    }
    final int helperCount = mConstraintHelpers.size();
    if (helperCount > 0) {
        for (int i = 0; i < helperCount; i++) {
            ConstraintHelper helper = mConstraintHelpers.get(i);
            helper.updatePostLayout(this);
        }
    }
}
```


注释1处，从 params 里获取 ConstraintWidget。

注释2处，获取 l,t,r,b 坐标。

注释3处，child 调用 layout 布局。

注意，键盘弹起收起的时候，3个子View，EditText top 坐标发生了改变。但是 TestImageView 的top 坐标没有变。

主要是在layout的时候，top坐标发生了变化。哪里改变了top坐标的值呢，就是有 `PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT` 标记位的时候，真正layout之前调用 onMeasure，改变了 l,t,r,b 坐标 。

```java

int t = widget.getY();
```


### 其他

1. 如果ImageView 的 高度依赖 外层约束布局到的bottom坐标，那么 ImageView也会跟着向上移动。

```
 app:layout_constraintBottom_toBottomOf="parent"
android:layout_height="0dp"
```

2. 如果最外层的约束布局改为RelativeLayout，ImageView也会向上移动。
3. 



