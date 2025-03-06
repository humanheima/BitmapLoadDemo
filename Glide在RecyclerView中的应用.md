# Glide 在 RecyclerView 中的应用


### 第一个问题，RecyclerView 中 使用 Glide 加载图片是如何避免错乱的？

简单来说，就是通过View的 setTag 方法！

我们在 onBindViewHolder 方法中，调用 Glide 加载图片到一个 ImageView 的时候，
1. 会先清除 Glide 为 ImageView 设置的老的tag并取消加载任务。
1.1 取消加载任务，内部会移除，ResourceCallback，那我们就收不到 ResourceCallback#onResourceReady方法回调了，所以图片不会显示出来。
1.2 清除老的的tag，会回调 Target#onLoadCleared 方法，我们可以在这个方法中，设置一个默认的图片(如果设置了占位图的话)。

2. 然后再设置新的tag，设置新的ResourceCallback回调等等。


我们来测试一下。

拷贝一份 Glide 自带的 CustomViewTarget 类，定义为 MyCustomViewTarget，然后添加一些注释。


MyCustomViewTarget的setRequest方法。
```java
/**
 * Stores the request using {@link View#setTag(Object)}.
 *
 * @param request {@inheritDoc}
 */
@Override
public final void setRequest(@Nullable Request request) {
    setTag(request);
}

```
MyCustomViewTarget 的 setTag 方法。

```java
private static final int VIEW_TAG_ID = R.id.glide_custom_view_target_tag;

private void setTag(@Nullable Object tag) {
    Log.i(TAG, "setTag: " + tag + "\n" + Log.getStackTraceString(new Throwable()));
    view.setTag(VIEW_TAG_ID, tag);
}
```


然后我们在RecyclerView 的适配器 onBindViewHolder 方法中，加载图片。 

Glide#into传入的是一个 MyCustomViewTarget 对象。

```kotlin
class RvAdapter(private val mData: List<String>) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_rv_load_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView

        init {
            imageView = itemView.findViewById(R.id.iv_load_image)
        }

        fun bind(item: String?) {
            item?.let {
                glideIntoTarget(imageView.context, item, imageView)
            }

        }
        //加载图片
        private fun glideIntoTarget(context: Context, url: String, imageView: ImageView) {
            Glide.with(context)
                .load(url)
                .error(R.mipmap.ic_launcher)
                .into(object : MyCustomViewTarget<ImageView, Drawable?>(imageView) {

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        imageView.setImageDrawable(errorDrawable)
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable?>?
                    ) {
                        imageView.setImageDrawable(resource)
                    }

                    override fun onResourceCleared(placeholder: Drawable?) {
                    }
                })
        }
    }
}
```

然后我们打开Logcat，查看日志，看看都在哪里

```java
setTag: com.bumptech.glide.request.SingleRequest@a99ffc7
    java.lang.Throwable
    	at com.hm.bitmaploadexample.activity.sourcecode.MyCustomViewTarget.setTag(MyCustomViewTarget.java:287)
    	at com.hm.bitmaploadexample.activity.sourcecode.MyCustomViewTarget.setRequest(MyCustomViewTarget.java:239)
    	at com.bumptech.glide.RequestBuilder.into(RequestBuilder.java:739)
    	at com.bumptech.glide.RequestBuilder.into(RequestBuilder.java:707)
    	at com.bumptech.glide.RequestBuilder.into(RequestBuilder.java:698)
    	at com.hm.bitmaploadexample.adapter.RvAdapter$ViewHolder.glideIntoTarget(RvAdapter.kt:57)
    	at com.hm.bitmaploadexample.adapter.RvAdapter$ViewHolder.bind(RvAdapter.kt:49)
    	at com.hm.bitmaploadexample.adapter.RvAdapter.onBindViewHolder(RvAdapter.kt:32)
    	at com.hm.bitmaploadexample.adapter.RvAdapter.onBindViewHolder(RvAdapter.kt:21)
    	at androidx.recyclerview.widget.RecyclerView$Adapter.onBindViewHolder(RecyclerView.java:6781)
    	at androidx.recyclerview.widget.RecyclerView$Adapter.bindViewHolder(RecyclerView.java:6823)
    	at androidx.recyclerview.widget.RecyclerView$Recycler.tryBindViewHolderByDeadline(RecyclerView.java:5752)
    	at androidx.recyclerview.widget.RecyclerView$Recycler.tryGetViewHolderForPositionByDeadline(RecyclerView.java:6019)
    	at androidx.recyclerview.widget.RecyclerView$Recycler.getViewForPosition(RecyclerView.java:5858)
    	at androidx.recyclerview.widget.RecyclerView$Recycler.getViewForPosition(RecyclerView.java:5854)
    	at androidx.recyclerview.widget.LinearLayoutManager$LayoutState.next(LinearLayoutManager.java:2230)
    	at androidx.recyclerview.widget.LinearLayoutManager.layoutChunk(LinearLayoutManager.java:1557)
    	at androidx.recyclerview.widget.LinearLayoutManager.fill(LinearLayoutManager.java:1517)
    	at androidx.recyclerview.widget.LinearLayoutManager.onLayoutChildren(LinearLayoutManager.java:612)
    	at androidx.recyclerview.widget.RecyclerView.dispatchLayoutStep2(RecyclerView.java:3924)
    	at androidx.recyclerview.widget.RecyclerView.dispatchLayout(RecyclerView.java:3641)
    	at androidx.recyclerview.widget.RecyclerView.onLayout(RecyclerView.java:4194)
    	at android.view.View.layout(View.java:22501)
    	at android.view.ViewGroup.layout(ViewGroup.java:6528)
    	at android.widget.RelativeLayout.onLayout(RelativeLayout.java:1103)
    	at android.view.View.layout(View.java:22501)
    	at android.view.ViewGroup.layout(ViewGroup.java:6528)
    	at android.widget.FrameLayout.layoutChildren(FrameLayout.java:334)
    	at android.widget.FrameLayout.onLayout(FrameLayout.java:270)
    	at android.view.View.layout(View.java:22501)
    	at android.view.ViewGroup.layout(ViewGroup.java:6528)
    	at android.widget.LinearLayout.setChildFrame(LinearLayout.java:1857)
    	at android.widget.LinearLayout.layoutVertical(LinearLayout.java:1701)
    	at android.widget.LinearLayout.onLayout(LinearLayout.java:1610)
    	at android.view.View.layout(View.java:22501)
    	at android.view.ViewGroup.layout(ViewGroup.java:6528)
    	at android.widget.FrameLayout.layoutChildren(FrameLayout.java:334)
    	at android.widget.FrameLayout.onLayout(FrameLayout.java:270)
    	at android.view.View.layout(View.java:22501)
    	at android.view.ViewGroup.layout(ViewGroup.java:6528)
    	at android.widget.LinearLayout.setChildFrame(LinearLayout.java:1857)
    	at android.widget.LinearLayout.layoutVertical(LinearLayout.java:1701)
    	at android.widget.LinearLayout.onLayout(LinearLayout.java:1610)
    	at android.view.View.layout(View.java:22501)
    	at android.view.ViewGroup.layout(ViewGroup.java:6528)
    	at android.widget.FrameLayout.layoutChildren(FrameLayout.java:334)
    	at android.widget.FrameLayout.onLayout(FrameLayout.java:270)
    	at com.android.internal.policy.DecorView.onLayout(DecorView.java:1180)
    	at android.view.View.layout(View.java:22501)
    	at android.view.ViewGroup.layout(ViewGroup.java:6528)
    	at android.view.ViewRootImpl.performLayout(ViewRootImpl.java:3812)
    	at android.view.ViewRootImpl.performTraversals(ViewRootImpl.java:3276)
    	at android.view.ViewRootImpl.doTraversal(ViewRootImpl.java:2235)
    	at android.view.ViewRootImpl$TraversalRunnable.run(ViewRootImpl.java:9043)
    	at android.view.Choreographer$CallbackRe
```
看到上面的log，我们看出来，在 onBindViewHolder 开始，最终是在 RequestBuilder 的 into 方法中注释2处，给Target设置了Tag。

RequestBuilder 的 into 方法

```java
private <Y extends Target<TranscodeType>> Y into(
      @NonNull Y target,
      @Nullable RequestListener<TranscodeType> targetListener,
      BaseRequestOptions<?> options,
      Executor callbackExecutor) {
    Preconditions.checkNotNull(target);
    if (!isModelSet) {
      throw new IllegalArgumentException("You must call #load() before calling #into()");
    }

    //An interface that allows a transition to be applied to {@link android.view.View}s in {@link
    //com.bumptech.glide.request.target.Target}s in across resource types.

  

    Request request = buildRequest(target, targetListener, options, callbackExecutor);

    Request previous = target.getRequest();
    if (request.isEquivalentTo(previous)
        && !isSkipMemoryCacheWithCompletePreviousRequest(options, previous)) {
      // 如果请求已经完成，重新开始将确保结果被重新传递，从而触发RequestListeners和Targets。
      //如果请求失败，重新开始将重新启动请求，使其有机会完成。如果请求已经运行，我们可以让它继续运行而不中断。
      // If the request is completed, beginning again will ensure the result is re-delivered,
      // triggering RequestListeners and Targets. If the request is failed, beginning again will
      // restart the request, giving it another chance to complete. If the request is already
      // running, we can let it continue running without interruption.
     
    
      if (!Preconditions.checkNotNull(previous).isRunning()) {
        //使用之前的请求，而不是新的请求，以便进行优化，例如跳过设置占位符、跟踪和取消跟踪目标，以及获取在之前请求中获取的的View控件的大小。
        // Use the previous request rather than the new one to allow for optimizations like skipping
        // setting placeholders, tracking and un-tracking Targets, and obtaining View dimensions
        // that are done in the individual Request.
        previous.begin();
      }
      return target;
    }
    //注释1处，取消Glide可能为此target(最常见的 ImageViewTarget )的所有未完成的加载，并释放可能已为目标加载的任何资源（例如{@link Bitmap}），以便可以重用它们。
    //注意：这里内部会清除Glide为Target设置的Tag。
    requestManager.clear(target);
    //注释2处，给Target设置了Tag
    target.setRequest(request);
    requestManager.track(target, request);

    return target;
  }
```

那么在快速滚动时候，RecyclerView 会复用，是怎么清除Tag(也就是把tag设置为null)的呢？我们来看看。我们过滤Logcat中tag为null的日志。

```java
 I  setTag: null
        java.lang.Throwable
        at com.hm.bitmaploadexample.activity.sourcecode.MyCustomViewTarget.setTag(MyCustomViewTarget.java:287)
        at com.hm.bitmaploadexample.activity.sourcecode.MyCustomViewTarget.setRequest(MyCustomViewTarget.java:239)
        at com.bumptech.glide.RequestManager.untrack(RequestManager.java:662)
        at com.bumptech.glide.RequestManager.untrackOrDelegate(RequestManager.java:628)
        at com.bumptech.glide.RequestManager.clear(RequestManager.java:624)
        at com.bumptech.glide.RequestBuilder.into(RequestBuilder.java:738)
        at com.bumptech.glide.RequestBuilder.into(RequestBuilder.java:707)
        at com.bumptech.glide.RequestBuilder.into(RequestBuilder.java:698)
        at com.hm.bitmaploadexample.adapter.RvAdapter$ViewHolder.glideIntoTarget(RvAdapter.kt:57)
        at com.hm.bitmaploadexample.adapter.RvAdapter$ViewHolder.bind(RvAdapter.kt:49)
        at com.hm.bitmaploadexample.adapter.RvAdapter.onBindViewHolder(RvAdapter.kt:32)
        at com.hm.bitmaploadexample.adapter.RvAdapter.onBindViewHolder(RvAdapter.kt:21)
        at androidx.recyclerview.widget.RecyclerView$Adapter.onBindViewHolder(RecyclerView.java:6781)
        at androidx.recyclerview.widget.RecyclerView$Adapter.bindViewHolder(RecyclerView.java:6823)
        at androidx.recyclerview.widget.RecyclerView$Recycler.tryBindViewHolderByDeadline(RecyclerView.java:5752)
        at androidx.recyclerview.widget.RecyclerView$Recycler.tryGetViewHolderForPositionByDeadline(RecyclerView.java:6019)
        at androidx.recyclerview.widget.GapWorker.prefetchPositionWithDeadline(GapWorker.java:286)
        at androidx.recyclerview.widget.GapWorker.flushTaskWithDeadline(GapWorker.java:343)
        at androidx.recyclerview.widget.GapWorker.flushTasksWithDeadline(GapWorker.java:359)
        at androidx.recyclerview.widget.GapWorker.prefetch(GapWorker.java:366)
        at androidx.recyclerview.widget.GapWorker.run(GapWorker.java:397)
        at android.os.Handler.handleCallback(Handler.java:900)
        at android.os.Handler.dispatchMessage(Handler.java:103)
        at android.os.Looper.loop(Looper.java:219)
        at android.app.ActivityThread.main(ActivityThread.java:8673)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:513)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1109)
```
我们看到还是在 onBindViewHolder 中开始，接着是 RequestBuilder的 into 方法中调用 RequestManager的 untrack 方法中 注释1处，将Target的请求设置为null的。

注意一下，RecyclerView复用的时候，在onBindViewHolder中，我们调用Glide加载图片到一个ImageView的时候，会先清除 Glide 为 ImageView 设置的老的tag，然后再设置新的tag。

RequestManager 的 untrack 方法。

```java
synchronized boolean untrack(@NonNull Target<?> target) {
    Request request = target.getRequest();
    // If the Target doesn't have a request, it's already been cleared.
    //如果Target的请求为null，说明Targer它已经被清除了。
    if (request == null) {
      return true;
    }

    //停止跟踪请求，清除请求，recycle请求，如果请求被移除了，返回true。
    if (requestTracker.clearAndRemove(request)) {
      targetTracker.untrack(target);
      //注释1处，将Target的请求设置为null。我们看一看Target的子类 ViewTarget 的setRequest方法。
      target.setRequest(null);
      return true;
    } else {
      return false;
    }
  }
```
