RequestManager


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
    //注释1处，取消Glide可能为此target(最常见的ImageViewTarget)的所有未完成的加载，并释放可能已为目标加载的任何资源（例如{@link Bitmap}），以便可以重用它们。
    //注意：这里内部会清除Glide为Target设置的Tag。
    requestManager.clear(target);
    //注释2处，重新设置tag
    target.setRequest(request);
    requestManager.track(target, request);

    return target;
  }
```

RequestManager 的 clear 方法。

```java
public void clear(@Nullable final Target<?> target) {
    if (target == null) {
      return;
    }

    untrackOrDelegate(target);
  }
```

RequestManager 的 untrackOrDelegate 方法。

```java
private void untrackOrDelegate(@NonNull Target<?> target) {
    //注释1处，调用 RequestManager 的 untrack 方法。
    boolean isOwnedByUs = untrack(target);
    // We'll end up here if the Target was cleared after the RequestManager that started the request
    // is destroyed. That can happen for at least two reasons:
    // 1. We call clear() on a background thread using something other than Application Context
    // RequestManager.

    //如果Target 在启动请求的RequestManager被销毁后 被清除。我们会在这里结束。这至少有两个原因：

    // 1.我们在后台线程上使用除应用程序上下文RequestManager之外的其他内容调用clear()。



    // 2. The caller retains a reference to the RequestManager after the corresponding Activity or
    // Fragment is destroyed, starts a load with it, and then clears that load with a different
    // RequestManager. Callers seem especially likely to do this in retained Fragments (#2262).

    //
    // 2.在对应的Activity或Fragment被销毁后，调用者保留了对RequestManager的引用，使用它启动了一个加载，然后使用不同的RequestManager清除了该加载。调用者似乎特别喜欢在保留的Fragment(#2262)中这样做。
    //
    // #1 is always an error. At best the caller is leaking memory briefly in something like an
    // AsyncTask. At worst the caller is leaking an Activity or Fragment for a sustained period of
    // time if they do something like reference the Activity RequestManager in a long lived
    // background thread or task.

    // #1总是一个错误。在最好的情况下，调用者在像AsyncTask这样的东西中短暂地泄漏内存。在最坏的情况下，如果调用者在长时间运行的后台线程或任务中引用Activity RequestManager，调用者将在持续的一段时间内泄漏Activity或Fragment。
    //
    // #2 is always an error. Callers shouldn't be starting new loads using RequestManagers after
    // the corresponding Activity or Fragment is destroyed because retaining any reference to the
    // RequestManager leaks memory. It's possible that there's some brief period of time during or
    // immediately after onDestroy where this is reasonable, but I can't think of why.

    // #2总是一个错误。在对应的Activity或Fragment被销毁后，调用者不应该使用RequestManagers启动新的加载，因为保留对RequestManager的任何引用都会泄漏内存。在onDestroy期间或之后的短暂时间内，这是合理的，但我想不出为什么。

    //
    Request request = target.getRequest();
    if (!isOwnedByUs && !glide.removeFromManagers(target) && request != null) {
      target.setRequest(null);
      request.clear();
    }
  }
```

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

ViewTarget 的setRequest方法。

```java
@Override
public void setRequest(@Nullable Request request) {
  setTag(request);
}
```

```java
private static int tagId = R.id.glide_custom_view_target_tag;
private void setTag(@Nullable Object tag) {
  //清除的时候，会将Tag设置为null。
  isTagUsedAtLeastOnce = true;
  view.setTag(tagId, tag);
}
```


### 如果一个请求被清除了，应该会走到 Target 的 onLoadCleared 方法。

我们看看 ImageViewTarget 的 onLoadCleared 方法。

```java
@Override
  public void onLoadCleared(@Nullable Drawable placeholder) {
    super.onLoadCleared(placeholder);
    if (animatable != null) {
      animatable.stop();
    }
    setResourceInternal(null);
    setDrawable(placeholder);
  }
```

### TODO
在RecyclerView中，使用Target来加载图片，看看上下滑动的时候，是否会走到这里。



Glide 加载图片，如果没有指定宽高，则默认取屏幕宽高中大者，作为尺寸。
```
Log.i(
              TAG,
              "Glide treats LayoutParams.WRAP_CONTENT as a request for an image the size of this"
                  + " device's screen dimensions. If you want to load the original image and are"
                  + " ok with the corresponding memory cost and OOMs (depending on the input size),"
                  + " use override(Target.SIZE_ORIGINAL). Otherwise, use LayoutParams.MATCH_PARENT,"
                  + " set layout_width and layout_height to fixed dimension, or use .override()"
                  + " with fixed dimensions.");

```

参考链接
* [Glide源码难看懂？用这个角度让你事半功倍！](https://juejin.cn/post/6994669144490639368)


/**
 * Any calls to {@link View#setTag(Object)}} on a View given to this class will result in
 * excessive allocations and and/or {@link IllegalArgumentException}s. If you must call {@link
 * View#setTag(Object)} on a view, use {@link #setTagId(int)} to specify a custom tag for Glide to
 * use.
 *  
 */

 任何调用

 2023-11-05 14:07:05.515 MyCustomViewTarget      com.hm.bitmaploadexample             I  setRequest: null
                                                                                        java.lang.Throwable
                                                                                        	at com.hm.bitmaploadexample.activity.sourcecode.MyCustomViewTarget.setRequest(MyCustomViewTarget.java:239)
                                                                                        	at com.bumptech.glide.RequestManager.untrack(RequestManager.java:662)
                                                                                        	at com.bumptech.glide.RequestManager.untrackOrDelegate(RequestManager.java:628)
                                                                                        	at com.bumptech.glide.RequestManager.clear(RequestManager.java:624)
                                                                                        	at com.bumptech.glide.RequestBuilder.into(RequestBuilder.java:738)
                                                                                        	at com.bumptech.glide.RequestBuilder.into(RequestBuilder.java:707)
                                                                                        	at com.bumptech.glide.RequestBuilder.into(RequestBuilder.java:698)
                                                                                        	at com.hm.bitmaploadexample.adapter.RvAdapter$ViewHolder.glideIntoTarget(RvAdapter.kt:63)
                                                                                        	at com.hm.bitmaploadexample.adapter.RvAdapter$ViewHolder.bind(RvAdapter.kt:52)
                                                                                        	at com.hm.bitmaploadexample.adapter.RvAdapter.onBindViewHolder(RvAdapter.kt:33)
                                                                                        	at com.hm.bitmaploadexample.adapter.RvAdapter.onBindViewHolder(RvAdapter.kt:22)
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


 