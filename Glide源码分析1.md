
# todo，还没完成，还得继续

```java
/**
 * 最简单的使用方式
 */
private void sourceCodeTest() {
    Glide.with(this)
		.load(Images.imageUrls[1])
		.into(imageView1);
}
```

with 方法返回的是 RequestManager 对象。

RequestManager 的 load 方法。

```java
@Override
public RequestBuilder<Drawable> load(@Nullable String string) {
    return asDrawable().load(string);
}
```

RequestManager 的 asDrawable 方法。

```java
@CheckResult
public RequestBuilder<Drawable> asDrawable() {
    return as(Drawable.class);
}
```

```java
public <ResourceType> RequestBuilder<ResourceType> as(
      @NonNull Class<ResourceType> resourceClass) {
    return new RequestBuilder<>(glide, this, resourceClass, context);
}
```

最终是构建了一个 RequestBuilder 对象。


RequestBuilder 的 into 方法

```java
@NonNull
public ViewTarget <ImageView, TranscodeType> into(@NonNull ImageView view) {
    Util.assertMainThread();
    Preconditions.checkNotNull(view);

    BaseRequestOptions <?> requestOptions = this;
    //注释1处，如果没有设置Transformation && 允许Transformation && view 的 ScaleType 不为null，则获取 requestOptions 对象。
    if(!requestOptions.isTransformationSet() && requestOptions.isTransformationAllowed() &&
        view.getScaleType() != null) {
        // Clone in this method so that if we use this RequestBuilder to load into a View and then
        // into a different target, we don't retain the transformation applied based on the previous
        // View's scale type.
		
		//在此方法中进行克隆，这样，如果我们使用此RequestBuilder加载到一个View中，然后加载到另一个目标中，
		//则不会保留基于上一个视图的缩放类型应用的转换。
        switch(view.getScaleType()) {
            case CENTER_CROP:
                requestOptions = requestOptions.clone().optionalCenterCrop();
                break;
            case CENTER_INSIDE:
                requestOptions = requestOptions.clone().optionalCenterInside();
                break;
            case FIT_CENTER:
            case FIT_START:
            case FIT_END:
                requestOptions = requestOptions.clone().optionalFitCenter();
                break;
            case FIT_XY:
                requestOptions = requestOptions.clone().optionalCenterInside();
                break;
            case CENTER:
            case MATRIX:
            default:
                // Do nothing.
        }
    }

    //注释2处，调用重载的 into 方法
    return into(
        glideContext.buildImageViewTarget(view, transcodeClass),
        /*targetListener=*/
        null,
        requestOptions,
        Executors.mainThreadExecutor());
}
```
注释1处，如果没有设置Transformation && 允许Transformation && view 的 ScaleType 不为null，则获取 requestOptions 对象。

注释2处，调用重载的 into 方法。

RequestBuilder 的 into 方法。

```java
private <Y extends Target<TranscodeType>> Y into(
      @NonNull Y target,
      @Nullable RequestListener<TranscodeType> targetListener,
      BaseRequestOptions<?> options,
      Executor callbackExecutor) {
    //注释1处，在我们的例子中，target 是 DrawableImageViewTarget 对象。
    Preconditions.checkNotNull(target);
    if (!isModelSet) {
      throw new IllegalArgumentException("You must call #load() before calling #into()");
    }

    //An interface that allows a transition to be applied to {@link android.view.View}s in {@link
    //com.bumptech.glide.request.target.Target}s in across resource types.
		

  
    //注释2处，构建request，我们先忽略其中的细节
    Request request = buildRequest(target, targetListener, options, callbackExecutor);

    Request previous = target.getRequest();
    //注释3处，如果有和之前的请求相同的请求，并且不是跳过内存缓存的请求，则使用之前的请求。
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
    //注释4处，取消Glide可能为此target(最常见的ImageViewTarget)的所有未完成的加载，并释放可能已为目标加载的任何资源（例如{@link Bitmap}），以便可以重用它们。
    //注意：这里内部会清除Glide为Target设置的Tag。
    requestManager.clear(target);
    //注释5处，重新设置tag
    target.setRequest(request);
    //注释6处，跟踪请求
    requestManager.track(target, request);

    return target;
  }
```

注释1处，在我们的例子中，target 是 DrawableImageViewTarget 对象。

注释2处，构建request，我们先忽略其中的细节。

注释3处，如果有和之前的请求相同的请求，并且不是跳过内存缓存的请求，则使用之前的请求。我们先忽略这种情况。

注释4处，取消Glide可能为此target(最常见的ImageViewTarget)的所有未完成的加载，并释放可能已为目标加载的任何资源（例如{@link Bitmap}），以便可以重用它们。 注意：这里内部会清除Glide为Target设置的Tag。

RequestManager 的 clear 方法。这块细节先略过。分析在RecyclerView中应用的时候， 在详细分析。

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
    // 2.在对应的Activity或Fragment被销毁后，调用者保留了对RequestManager的引用，使用它启动了一个加载，然后使用不同的RequestManager清除了该加载。
	// 调用者似乎特别喜欢在保留的Fragment(#2262)中这样做。
    //
    // #1 is always an error. At best the caller is leaking memory briefly in something like an
    // AsyncTask. At worst the caller is leaking an Activity or Fragment for a sustained period of
    // time if they do something like reference the Activity RequestManager in a long lived
    // background thread or task.

    // #1总是一个错误。在最好的情况下，调用者在像AsyncTask这样的东西中短暂地泄漏内存。
	// 在最坏的情况下，如果调用者在长时间运行的后台线程或任务中引用Activity RequestManager，
	// 调用者将在持续的一段时间内泄漏Activity或Fragment。
    //
    // #2 is always an error. Callers shouldn't be starting new loads using RequestManagers after
    // the corresponding Activity or Fragment is destroyed because retaining any reference to the
    // RequestManager leaks memory. It's possible that there's some brief period of time during or
    // immediately after onDestroy where this is reasonable, but I can't think of why.

    // #2总是一个错误。在对应的Activity或Fragment被销毁后，调用者不应该使用RequestManagers启动新的加载，
	// 因为保留对RequestManager的任何引用都会泄漏内存。在onDestroy期间或之后的短暂时间内，这是合理的，但我想不出为什么。
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


* 如果一个请求被清除了，应该会走到 Target 的 onLoadCleared 方法。

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

我们回到 RequestBuilder 的 into 方法的注释5处。 调用 ViewTarget 的setRequest方法。内部给View设置了tag。
这个是保证不乱序的原因。

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


RequestBuilder 的 into 方法，注释6处， RequestManager 调用 track 方法。

```java
synchronized void track(@NonNull Target<?> target, @NonNull Request request) {
    targetTracker.track(target);
    requestTracker.runRequest(request);
}
```

RequestTracker 的 runRequest 方法。

```java
/** Starts tracking the given request. */
public void runRequest(@NonNull Request request) {
    requests.add(request);
    if(!isPaused) {
        //注释1处，调用 SingleRequest 的 begin 方法。
        request.begin();
    } else {
        request.clear();
        if(Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Paused, delaying request");
        }
        pendingRequests.add(request);
    }
}
```

注释1处，调用 SingleRequest 的 begin 方法。

```java
@Override
public void begin() {
    synchronized(requestLock) {
        assertNotCallingCallbacks();
        stateVerifier.throwIfRecycled();
        startTime = LogTime.getLogTime();
        if(model == null) {
            if(Util.isValidDimensions(overrideWidth, overrideHeight)) {
                width = overrideWidth;
                height = overrideHeight;
            }
            // Only log at more verbose log levels if the user has set a fallback drawable, because
            // fallback Drawables indicate the user expects null models occasionally.
            int logLevel = getFallbackDrawable() == null ? Log.WARN :
                Log.DEBUG;
            onLoadFailed(new GlideException("Received null model"),
                logLevel);
            return;
        }

        if(status == Status.RUNNING) {
            throw new IllegalArgumentException(
                "Cannot restart a running request");
        }

        // If we're restarted after we're complete (usually via something like a notifyDataSetChanged
        // that starts an identical request into the same Target or View), we can simply use the
        // resource and size we retrieved the last time around and skip obtaining a new size, starting
        // a new load etc. This does mean that users who want to restart a load because they expect
        // that the view size has changed will need to explicitly clear the View or Target before
        // starting the new load.
        if(status == Status.COMPLETE) {
            onResourceReady(
                resource, DataSource.MEMORY_CACHE, /* isLoadedFromAlternateCacheKey= */
                false);
            return;
        }

        // Restarts for requests that are neither complete nor running can be treated as new requests
        // and can run again from the beginning.

		//注释1处，开始状态，等待控件测量过后有尺寸
        status = Status.WAITING_FOR_SIZE;
        if(Util.isValidDimensions(overrideWidth, overrideHeight)) {
            //注释2处，如果指定了宽高，直接调用onSizeReady方法。
            onSizeReady(overrideWidth, overrideHeight);
        } else {
            //注释3处，否则，调用Target的getSize方法。SingleRequest 本身做为回调传入
            target.getSize(this);
        }

        if((status == Status.RUNNING || status == Status.WAITING_FOR_SIZE) &&
            canNotifyStatusChanged()) {
            //先给ImageView 设置placeholderDrawable
            target.onLoadStarted(getPlaceholderDrawable());
        }
        if(IS_VERBOSE_LOGGABLE) {
            logV("finished run method in " + LogTime.getElapsedMillis(
                startTime));
        }
    }
}
```

注释3处，否则，调用 ViewTarget 的getSize方法。内部是调用 SizeDeterminer 的 getSize 方法。

```java
void getSize(@NonNull SizeReadyCallback cb) {
    int currentWidth = getTargetWidth();
    int currentHeight = getTargetHeight();
    if(isViewStateAndSizeValid(currentWidth, currentHeight)) {
        cb.onSizeReady(currentWidth, currentHeight);
        return;
    }

    // We want to notify callbacks in the order they were added and we only expect one or two
    // callbacks to be added a time, so a List is a reasonable choice.
    if(!cbs.contains(cb)) {
        cbs.add(cb);
    }
    if(layoutListener == null) {
        ViewTreeObserver observer = view.getViewTreeObserver();
        layoutListener = new SizeDeterminerLayoutListener(this);
        //注释1处，添加监听，监听尺寸的变化
        observer.addOnPreDrawListener(layoutListener);
    }
}
```

ViewTarget.SizeDeterminer.SizeDeterminerLayoutListener 类

```java
private static final class SizeDeterminerLayoutListener
implements ViewTreeObserver.OnPreDrawListener {
    private final WeakReference < SizeDeterminer > sizeDeterminerRef;

    SizeDeterminerLayoutListener(@NonNull SizeDeterminer sizeDeterminer) {
        sizeDeterminerRef = new WeakReference < > (sizeDeterminer);
    }

    @Override
    public boolean onPreDraw() {
        if(Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG,
                "OnGlobalLayoutListener called attachStateListener=" +
                this);
        }
        SizeDeterminer sizeDeterminer = sizeDeterminerRef.get();
        if(sizeDeterminer != null) {
            //注释1处，调用 SizeDeterminer 的 checkCurrentDimens 方法。
            sizeDeterminer.checkCurrentDimens();
        }
        return true;
    }
}
```

注释1处，调用 SizeDeterminer 的 checkCurrentDimens 方法。

```java
@Synthetic
void checkCurrentDimens() {
    if(cbs.isEmpty()) {
        return;
    }

    //注释1处，注意，getTartgetWidth和getTargetHeight方法
    int currentWidth = getTargetWidth();
    int currentHeight = getTargetHeight();
    if(!isViewStateAndSizeValid(currentWidth, currentHeight)) {
        //注释2处，宽高不合法，说明此时View还没有经过measure，直接返回，SizeDeterminerLayoutListener不会被清除。
		//等待经过measure后，再次调用checkCurrentDimens方法。
        return;
    }

    //注释3处，如果宽高合法通知回调，并清除监听SizeDeterminerLayoutListener。
    notifyCbs(currentWidth, currentHeight);
    clearCallbacksAndListener();
}
```

注释1处，注意，SizeDeterminer 的 getTargetWidth 和 getTargetHeight 方法。

```java
private int getTargetWidth() {
    int horizontalPadding = view.getPaddingLeft() + view.getPaddingRight();
    LayoutParams layoutParams = view.getLayoutParams();
    int layoutParamSize = layoutParams != null ? layoutParams.width :
        PENDING_SIZE;
    return getTargetDimen(view.getWidth(), layoutParamSize,
        horizontalPadding);
}
```

SizeDeterminer 的 getTargetDimen 方法。

```java
    private int getTargetDimen(int viewSize, int paramSize, int paddingSize) {
        // We consider the View state as valid if the View has non-null layout params and a non-zero
        // layout params width and height. This is imperfect. We're making an assumption that View
        // parents will obey their child's layout parameters, which isn't always the case.
        int adjustedParamSize = paramSize - paddingSize;
        if(adjustedParamSize > 0) {
            return adjustedParamSize;
        }

        // Since we always prefer layout parameters with fixed sizes, even if waitForLayout is true,
        // we might as well ignore it and just return the layout parameters above if we have them.
        // Otherwise we should wait for a layout pass before checking the View's dimensions.
        if(waitForLayout && view.isLayoutRequested()) {
            return PENDING_SIZE;
        }

        // We also consider the View state valid if the View has a non-zero width and height. This
        // means that the View has gone through at least one layout pass. It does not mean the Views
        // width and height are from the current layout pass. For example, if a View is re-used in
        // RecyclerView or ListView, this width/height may be from an old position. In some cases
        // the dimensions of the View at the old position may be different than the dimensions of the
        // View in the new position because the LayoutManager/ViewParent can arbitrarily decide to
        // change them. Nevertheless, in most cases this should be a reasonable choice.
        int adjustedViewSize = viewSize - paddingSize;
        if(adjustedViewSize > 0) {
            return adjustedViewSize;
        }

        // Finally we consider the view valid if the layout parameter size is set to wrap_content.
        // It's difficult for Glide to figure out what to do here. Although Target.SIZE_ORIGINAL is a
        // coherent choice, it's extremely dangerous because original images may be much too large to
        // fit in memory or so large that only a couple can fit in memory, causing OOMs. If users want
        // the original image, they can always use .override(Target.SIZE_ORIGINAL). Since wrap_content
        // may never resolve to a real size unless we load something, we aim for a square whose length
        // is the largest screen size. That way we're loading something and that something has some
        // hope of being downsampled to a size that the device can support. We also log a warning that
        // tries to explain what Glide is doing and why some alternatives are preferable.
        // Since WRAP_CONTENT is sometimes used as a default layout parameter, we always wait for
        // layout to complete before using this fallback parameter (ConstraintLayout among others).
        if(!view.isLayoutRequested() && paramSize == LayoutParams.WRAP_CONTENT) {
            if(Log.isLoggable(TAG, Log.INFO)) {
                Log.i(
                    TAG,
                    "Glide treats LayoutParams.WRAP_CONTENT as a request for an image the size of this" +
                    " device's screen dimensions. If you want to load the original image and are" +
                    " ok with the corresponding memory cost and OOMs (depending on the input size)," +
                    " use override(Target.SIZE_ORIGINAL). Otherwise, use LayoutParams.MATCH_PARENT," +
                    " set layout_width and layout_height to fixed dimension, or use .override()" +
                    " with fixed dimensions.");
            }
            //注释1处，获取屏幕的宽高中最大者，作为尺寸
            return getMaxDisplayLength(view.getContext());
        }

        // If the layout parameters are < padding, the view size is < padding, or the layout
        // parameters are set to match_parent or wrap_content and no layout has occurred, we should
        // wait for layout and repeat.
        return PENDING_SIZE;
    }
```

注释1处，注意一下，如果无法正确获取View的宽或者高，则获取屏幕的宽高中最大者，作为尺寸。

回到 SizeDeterminer 的 checkCurrentDimens 方法的注释2处，如果宽高不合法，说明此时View还没有经过measure，直接返回，SizeDeterminerLayoutListener 不会被清除。等待经过 measure 后，再次调用 checkCurrentDimens 方法。

注释3处，如果宽高合法，回调 onSizeReady 方法，并清除监听 SizeDeterminerLayoutListener。

获取到尺寸以后，回到 SingleRequest 的 onSizeReady 方法。

```java
@Override
public void onSizeReady(int width, int height) {
    stateVerifier.throwIfRecycled();
    synchronized(requestLock) {
        if(IS_VERBOSE_LOGGABLE) {
            logV("Got onSizeReady in " + LogTime.getElapsedMillis(
                startTime));
        }
        if(status != Status.WAITING_FOR_SIZE) {
            return;
        }
        status = Status.RUNNING;

        float sizeMultiplier = requestOptions.getSizeMultiplier();
        this.width = maybeApplySizeMultiplier(width, sizeMultiplier);
        this.height = maybeApplySizeMultiplier(height, sizeMultiplier);

        if(IS_VERBOSE_LOGGABLE) {
            logV("finished setup for calling load in " + LogTime.getElapsedMillis(
                startTime));
        }
        //注释1处，调用Engine的load方法，开始加载图片
        loadStatus =
            engine.load(
                glideContext,
                model,
                requestOptions.getSignature(),
                this.width,
                this.height,
                requestOptions.getResourceClass(),
                transcodeClass,
                priority,
                requestOptions.getDiskCacheStrategy(),
                requestOptions.getTransformations(),
                requestOptions.isTransformationRequired(),
                requestOptions.isScaleOnlyOrNoTransform(),
                requestOptions.getOptions(),
                requestOptions.isMemoryCacheable(),
                requestOptions.getUseUnlimitedSourceGeneratorsPool(),
                requestOptions.getUseAnimationPool(),
                requestOptions.getOnlyRetrieveFromCache(),
                this,
                callbackExecutor);

        // This is a hack that's only useful for testing right now where loads complete synchronously
        // even though under any executor running on any thread but the main thread, the load would
        // have completed asynchronously.
        if(status != Status.RUNNING) {
            loadStatus = null;
        }
        if(IS_VERBOSE_LOGGABLE) {
            logV("finished onSizeReady in " + LogTime.getElapsedMillis(
                startTime));
        }
    }
}

```


注释1处，调用 Engine 的 load 方法，开始加载图片。

```java
/**
   * 开始加载给定参数的资源。
   *
   * <p>必须在主线程上调用。
   *
   * <p>任何请求的流程如下：
   *
   * <ul>
   *   <li>检查当前的活动资源集，如果存在则返回活动资源，并将任何新的非活动资源移动到内存缓存中。
   *   <li>检查内存缓存并提供缓存的资源（如果存在）。
   *   <li>检查当前的进行中的加载，并将cb添加到进行中的加载（如果存在）。
   *   <li>开始新的加载。
   * </ul>
   *
   * <p>活动资源是那些已经提供给至少一个请求并且尚未释放的资源。一旦资源的所有消费者都释放了该资源，该资源就会进入缓存。如果资源从缓存中返回给新的消费者，它将被重新添加到活动资源中。如果资源从缓存中被逐出，其资源将被回收并在可能的情况下重用，资源将被丢弃。消费者没有严格的要求释放他们的资源，所以活动资源被弱引用持有。
   *
   * @param width 目标资源的目标宽度（以像素为单位）。
   * @param height 目标资源的目标高度（以像素为单位）。
   * @param cb 当加载完成时将被调用的回调。
   */
public <R> LoadStatus load(
      GlideContext glideContext,
      Object model,
      Key signature,
      int width,
      int height,
      Class<?> resourceClass,
      Class<R> transcodeClass,
      Priority priority,
      DiskCacheStrategy diskCacheStrategy,
      Map<Class<?>, Transformation<?>> transformations,
      boolean isTransformationRequired,
      boolean isScaleOnlyOrNoTransform,
      Options options,
      boolean isMemoryCacheable,
      boolean useUnlimitedSourceExecutorPool,
      boolean useAnimationPool,
      boolean onlyRetrieveFromCache,
      ResourceCallback cb,
      Executor callbackExecutor) {
    long startTime = VERBOSE_IS_LOGGABLE ? LogTime.getLogTime() : 0;

    EngineKey key =
        keyFactory.buildKey(
            model,
            signature,
            width,
            height,
            transformations,
            resourceClass,
            transcodeClass,
            options);

    EngineResource<?> memoryResource;
    synchronized (this) {
      //注释1处，先从内存加载  
      memoryResource = loadFromMemory(key, isMemoryCacheable, startTime);

      if (memoryResource == null) {
        //注释2处，内存为null，检查当前的进行中的加载，并将cb添加到进行中的加载（如果存在）。不存在的话，开始新的加载。
        return waitForExistingOrStartNewJob(
            glideContext,
            model,
            signature,
            width,
            height,
            resourceClass,
            transcodeClass,
            priority,
            diskCacheStrategy,
            transformations,
            isTransformationRequired,
            isScaleOnlyOrNoTransform,
            options,
            isMemoryCacheable,
            useUnlimitedSourceExecutorPool,
            useAnimationPool,
            onlyRetrieveFromCache,
            cb,
            callbackExecutor,
            key,
            startTime);
      }
    }

    // Avoid calling back while holding the engine lock, doing so makes it easier for callers to
    // deadlock.
	//内存存在的话，会直接回调onResourceReady方法
    cb.onResourceReady(
        memoryResource, DataSource.MEMORY_CACHE, /* isLoadedFromAlternateCacheKey= */ false);
    return null;
  }

```

Engine 的 waitForExistingOrStartNewJob 方法

```java
private < R > LoadStatus waitForExistingOrStartNewJob(
    GlideContext glideContext,
    Object model,
    Key signature,
    int width,
    int height,
    Class <? > resourceClass,
    Class < R > transcodeClass,
    Priority priority,
    DiskCacheStrategy diskCacheStrategy,
    Map < Class <? > , Transformation <? >> transformations,
    boolean isTransformationRequired,
    boolean isScaleOnlyOrNoTransform,
    Options options,
    boolean isMemoryCacheable,
    boolean useUnlimitedSourceExecutorPool,
    boolean useAnimationPool,
    boolean onlyRetrieveFromCache,
    ResourceCallback cb,
    Executor callbackExecutor,
    EngineKey key,
    long startTime) {

    //注释1处，存在正在进行的加载，将cb添加到进行中的加载，直接返回。
    EngineJob <?> current = jobs.get(key, onlyRetrieveFromCache);
    if(current != null) {
        current.addCallback(cb, callbackExecutor);
        if(VERBOSE_IS_LOGGABLE) {
            logWithTimeAndKey("Added to existing load", startTime, key);
        }
        return new LoadStatus(cb, current);
    }

    EngineJob <R> engineJob =engineJobFactory.build(
            key,
            isMemoryCacheable,
            useUnlimitedSourceExecutorPool,
            useAnimationPool,
            onlyRetrieveFromCache);

    DecodeJob <R> decodeJob = decodeJobFactory.build(
            glideContext,
            model,
            key,
            signature,
            width,
            height,
            resourceClass,
            transcodeClass,
            priority,
            diskCacheStrategy,
            transformations,
            isTransformationRequired,
            isScaleOnlyOrNoTransform,
            onlyRetrieveFromCache,
            options,
            engineJob);

    jobs.put(key, engineJob);

    engineJob.addCallback(cb, callbackExecutor);
    //注释2处，开始加载的地方
    engineJob.start(decodeJob);

    if(VERBOSE_IS_LOGGABLE) {
        logWithTimeAndKey("Started new load", startTime, key);
    }
    return new LoadStatus(cb, engineJob);
}
```

注释2处，开始加载的地方 EngineJob 的 start 方法

```java
public synchronized void start(DecodeJob<R> decodeJob) {
    this.decodeJob = decodeJob;
    GlideExecutor executor =
        decodeJob.willDecodeFromCache() ? diskCacheExecutor : getActiveSourceExecutor();
    //调用 GlideExecutor 的 execute 方法
    executor.execute(decodeJob);
}
```

GlideExecutor 的 execute 方法

```java
@Override
public void execute(@NonNull Runnable command) {
    delegate.execute(command);
}
```

最终会执行 传入的 DecodeJob 的 run 方法

```java
 @Override
  public void run() {
    // This should be much more fine grained, but since Java's thread pool implementation silently
    // swallows all otherwise fatal exceptions, this will at least make it obvious to developers
    // that something is failing.
    GlideTrace.beginSectionFormat("DecodeJob#run(model=%s)", model);
    // Methods in the try statement can invalidate currentFetcher, so set a local variable here to
    // ensure that the fetcher is cleaned up either way.
    DataFetcher<?> localFetcher = currentFetcher;
    try {
      if (isCancelled) {
        notifyFailed();
        return;
      }
      //注释1处，调用 runWrapped 方法
      runWrapped();
    } catch (CallbackException e) {
      // If a callback not controlled by Glide throws an exception, we should avoid the Glide
      // specific debug logic below.
      throw e;
    } catch (Throwable t) {
      // Catch Throwable and not Exception to handle OOMs. Throwables are swallowed by our
      // usage of .submit() in GlideExecutor so we're not silently hiding crashes by doing this. We
      // are however ensuring that our callbacks are always notified when a load fails. Without this
      // notification, uncaught throwables never notify the corresponding callbacks, which can cause
      // loads to silently hang forever, a case that's especially bad for users using Futures on
      // background threads.
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(
            TAG,
            "DecodeJob threw unexpectedly" + ", isCancelled: " + isCancelled + ", stage: " + stage,
            t);
      }
      // When we're encoding we've already notified our callback and it isn't safe to do so again.
      if (stage != Stage.ENCODE) {
        throwables.add(t);
        notifyFailed();
      }
      if (!isCancelled) {
        throw t;
      }
      throw t;
    } finally {
      // Keeping track of the fetcher here and calling cleanup is excessively paranoid, we call
      // close in all cases anyway.
      if (localFetcher != null) {
        localFetcher.cleanup();
      }
      GlideTrace.endSection();
    }
  }
```

注释1处，调用 DecodeJob 的  runWrapped 方法。

```java
private void runWrapped() {
    switch(runReason) {
        case INITIALIZE:
            //注释1处，INITIALIZE 的下一个状态 RESOURCE_CACHE; 下一个生成器 ResourceCacheGenerator
            stage = getNextStage(Stage.INITIALIZE);
            currentGenerator = getNextGenerator();
            runGenerators();
            break;
        case SWITCH_TO_SOURCE_SERVICE:
            runGenerators();
            break;
        case DECODE_DATA:
            decodeFromRetrievedData();
            break;
        default:
            throw new IllegalStateException("Unrecognized run reason: " + runReason);
    }
}


```

注释1处，INITIALIZE 的下一个状态 RESOURCE_CACHE; 下一个生成器 ResourceCacheGenerator(这个貌似是负责从磁盘获取的操作)。
然后调用 DecodeJob 的 runGenerators 方法

```java
private void runGenerators() {
    currentThread = Thread.currentThread();
    startFetchTime = LogTime.getLogTime();
    boolean isStarted = false;
    //注释1处，循环调用 startNext 方法
    while(!isCancelled && currentGenerator != null && !(isStarted = currentGenerator.startNext())) {
        stage = getNextStage(stage);
        currentGenerator = getNextGenerator();

        if(stage == Stage.SOURCE) {
            reschedule();
            return;
        }
    }
    // We've run out of stages and generators, give up.
    if((stage == Stage.FINISHED || isCancelled) && !isStarted) {
        notifyFailed();
    }

    // Otherwise a generator started a new load and we expect to be called back in
    // onDataFetcherReady.
}
```

注释1处，循环调用 startNext 方法。

第一次进入循环，调用 ResourceCacheGenerator 的 startNext 方法，内部做了从磁盘缓存中获取图片的操作。

```java
@Override
public boolean startNext() {
    List <Key> sourceIds = helper.getCacheKeys();
    if(sourceIds.isEmpty()) {
        return false;
    }
    /**
	 * 注册的资源类有
	 * 1. class com.bumptech.glide.load.resource.gif.GifDrawable
	 * 2. class android.graphics.Bitmap
	 * 3. class android.graphics.drawable.BitmapDrawable
	 */
    List <Class <?>> resourceClasses = helper.getRegisteredResourceClasses();
    //...
    while(modelLoaders == null || !hasNextModelLoader()) {
        resourceClassIndex++;
        if(resourceClassIndex >= resourceClasses.size()) {
            sourceIdIndex++;
            if(sourceIdIndex >= sourceIds.size()) {
                return false;
            }
            resourceClassIndex = 0;
        }

        //sourceId 就是图片地址
        Key sourceId = sourceIds.get(sourceIdIndex);
        Class <?> resourceClass = resourceClasses.get(resourceClassIndex);
        Transformation <? > transformation = helper.getTransformation(resourceClass);
        // PMD.AvoidInstantiatingObjectsInLoops Each iteration is comparatively expensive anyway,
        // we only run until the first one succeeds, the loop runs for only a limited
        // number of iterations on the order of 10-20 in the worst case.
        currentKey =
            new ResourceCacheKey( // NOPMD AvoidInstantiatingObjectsInLoops
                helper.getArrayPool(),
                sourceId,
                helper.getSignature(),
                helper.getWidth(),
                helper.getHeight(),
                transformation,
                resourceClass,
                helper.getOptions());
        //注释2处，获取缓存的磁盘文件
        cacheFile = helper.getDiskCache().get(currentKey);
        if(cacheFile != null) {
            sourceKey = sourceId;
            modelLoaders = helper.getModelLoaders(cacheFile);
            modelLoaderIndex = 0;
        }
    }

    loadData = null;
    boolean started = false;
    while(!started && hasNextModelLoader()) {
        ModelLoader < File, ?> modelLoader = modelLoaders.get(modelLoaderIndex++);
        loadData =
            modelLoader.buildLoadData(
                cacheFile, helper.getWidth(), helper.getHeight(), helper.getOptions());
        if(loadData != null && helper.hasLoadPath(loadData.fetcher.getDataClass())) {
            started = true;
            loadData.fetcher.loadData(helper.getPriority(), this);
        }
    }

    return started;
}
```

经过debug，这个方法最终返回了false。

回到 DecodeJob 的 runGenerators 方法，继续循环调用 startNext 方法。

下一个阶段 DATA_CACHE，调用 DataCacheGenerator 的 startNext 方法。

```java
@Override
public boolean startNext() {
    while(modelLoaders == null || !hasNextModelLoader()) {
        sourceIdIndex++;
        if(sourceIdIndex >= cacheKeys.size()) {
            return false;
        }

        //图片url
        Key sourceId = cacheKeys.get(sourceIdIndex);
        // PMD.AvoidInstantiatingObjectsInLoops The loop iterates a limited number of times
        // and the actions it performs are much more expensive than a single allocation.
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        Key originalKey = new DataCacheKey(sourceId, helper.getSignature());
        cacheFile = helper.getDiskCache().get(originalKey);
        if(cacheFile != null) {
            this.sourceKey = sourceId;
            modelLoaders = helper.getModelLoaders(cacheFile);
            modelLoaderIndex = 0;
        }
    }

    loadData = null;
    boolean started = false;
    while(!started && hasNextModelLoader()) {
        ModelLoader < File, ?> modelLoader = modelLoaders.get(modelLoaderIndex++);
        loadData =
            modelLoader.buildLoadData(
                cacheFile, helper.getWidth(), helper.getHeight(), helper.getOptions());
        if(loadData != null && helper.hasLoadPath(loadData.fetcher.getDataClass())) {
            started = true;
            loadData.fetcher.loadData(helper.getPriority(), this);
        }
    }
    return started;
}
```

经过debug，这个方法最终返回了false。

回到 DecodeJob 的 runGenerators 方法，继续循环调用 startNext 方法。

下一个阶段 SOURCE，如果是 SOURCE 阶段，调用 reschedule 方法。

```java
@Override
public void reschedule() {
    //runReason 变成了 SWITCH_TO_SOURCE_SERVICE;
    runReason = RunReason.SWITCH_TO_SOURCE_SERVICE;
    callback.reschedule(this);
}
```

最终还是会调用 DecodeJob 的 runGenerators 方法

```java
private void runGenerators() {
    currentThread = Thread.currentThread();
    startFetchTime = LogTime.getLogTime();
    boolean isStarted = false;
    //注释1处，循环调用 startNext 方法
    while(!isCancelled && currentGenerator != null && !(isStarted = currentGenerator.startNext())) {
        stage = getNextStage(stage);
        currentGenerator = getNextGenerator();

        if(stage == Stage.SOURCE) {
            reschedule();
            return;
        }
    }
    // We've run out of stages and generators, give up.
    if((stage == Stage.FINISHED || isCancelled) && !isStarted) {
        notifyFailed();
    }

    // Otherwise a generator started a new load and we expect to be called back in
    // onDataFetcherReady.
}
```

注释1处，循环调用 startNext 方法。这时候调用的是 SourceGenerator 的 startNext 方法。

```java
@Override
public boolean startNext() {
    if(dataToCache != null) {
        Object data = dataToCache;
        dataToCache = null;
        cacheData(data);
    }

    if(sourceCacheGenerator != null && sourceCacheGenerator.startNext()) {
        return true;
    }
    sourceCacheGenerator = null;

    loadData = null;
    boolean started = false;
    while(!started && hasNextModelLoader()) {
        loadData = helper.getLoadData().get(loadDataListIndex++);
        if(loadData != null && (helper.getDiskCacheStrategy().isDataCacheable(
                    loadData.fetcher.getDataSource()) ||
                helper.hasLoadPath(loadData.fetcher.getDataClass()))) {
            started = true;
            //注释1处，startNextLoad 方法
            startNextLoad(loadData);
        }
    }
    return started;
}
```

注释1处，调用 SourceGenerator 的 startNextLoad 方法

```java
private void startNextLoad(final LoadData <?> toStart) {
    // 注释1处，调用 MultiModelLoader.MultiFetcher 的 loadData 方法。
    loadData.fetcher.loadData(helper.getPriority(),new DataCallback <Object> () {
            @Override
            public void onDataReady(@Nullable Object data) {
                if(isCurrentRequest(toStart)) {
                    //加载成功
                    onDataReadyInternal(toStart, data);
                }
            }

            @Override
            public void onLoadFailed(@NonNull Exception e) {
                if(isCurrentRequest(toStart)) {
                    //加载失败
                    onLoadFailedInternal(toStart, e);
                }
            }
        });
}
```
	
注释1处，调用 MultiModelLoader.MultiFetcher 的 loadData 方法。

```java
@Override
public void loadData(@NonNull Priority priority, @NonNull DataCallback <?super Data>
    callback) {
    this.priority = priority;
    this.callback = callback;
    exceptions = throwableListPool.acquire();
    //注释1处，调用 OkHttpStreamFetcher 的 loadData 方法。 回调是 MultiModelLoader.MultiFetcher 对象
    fetchers.get(currentIndex).loadData(priority, this);

    // If a race occurred where we cancelled the fetcher in cancel() and then called loadData here
    // immediately after, make sure that we cancel the newly started fetcher. We don't bother
    // checking cancelled before loadData because it's not required for correctness and would
    // require an unlikely race to be useful.
    if(isCancelled) {
        cancel();
    }
}
```

注释1处，调用 OkHttpStreamFetcher 的 loadData 方法。

```java
@Override
public void loadData(@NonNull Priority priority, @NonNull final DataCallback <?super InputStream> callback) {
    Request.Builder requestBuilder = new Request.Builder().url(url.toStringUrl());
    for(Map.Entry < String, String > headerEntry: url.getHeaders().entrySet()) {
        String key = headerEntry.getKey();
        requestBuilder.addHeader(key, headerEntry.getValue());
    }
    Request request = requestBuilder.build();
    this.callback = callback;
	
    //注释1处，构建一个请求，并加入到请求队列中。
    call = client.newCall(request);
    //回调是 OkHttpStreamFetcher 对象
    call.enqueue(this);
}
```

请求成功以后，回调 OkHttpStreamFetcher 的 onResponse 方法。

```java
@Override
public void onResponse(@NonNull Call call, @NonNull Response response) {
    responseBody = response.body();
    if(response.isSuccessful()) {
        long contentLength = Preconditions.checkNotNull(responseBody).contentLength();
        stream = ContentLengthInputStream.obtain(responseBody.byteStream(),
            contentLength);
        //注释1处，回调 MultiModelLoader.MultiFetcher 的 onDataReady 方法。
        callback.onDataReady(stream);
    } else {
        callback.onLoadFailed(new HttpException(response.message(),
            response.code()));
    }
}
```

注释1处，回调 MultiModelLoader.MultiFetcher 的 onDataReady 方法

```java
@Override
public void onDataReady(@Nullable Data data) {
    if(data != null) {
        //注释1处，最终会回调到 SourceGenerator 的 onDataReadyInternal 方法。
        callback.onDataReady(data);
    } else {
        startNextOrFail();
    }
}
```

注释1处，最终会回调到 SourceGenerator 的 onDataReadyInternal 方法。

```java
@Synthetic
void onDataReadyInternal(LoadData <? > loadData, Object data) {
    DiskCacheStrategy diskCacheStrategy = helper.getDiskCacheStrategy();
    if(data != null && diskCacheStrategy.isDataCacheable(loadData.fetcher.getDataSource())) {
        dataToCache = data;
        // We might be being called back on someone else's thread. Before doing anything, we should
        // reschedule to get back onto Glide's thread.
		//注释1处，再次调用 DecodeJob 的 reschedule 方法。
        cb.reschedule();
    } else {
        cb.onDataFetcherReady(
            loadData.sourceKey,
            data,
            loadData.fetcher,
            loadData.fetcher.getDataSource(),
            originalKey);
    }
}
```

注释1处，再次调用 DecodeJob 的 reschedule 方法。

最终还是会调用 DecodeJob 的 runGenerators 方法

```java
private void runGenerators() {
    currentThread = Thread.currentThread();
    startFetchTime = LogTime.getLogTime();
    boolean isStarted = false;
    //注释1处，循环调用 startNext 方法
    while(!isCancelled && currentGenerator != null && !(isStarted = currentGenerator.startNext())) {
        stage = getNextStage(stage);
        currentGenerator = getNextGenerator();

        if(stage == Stage.SOURCE) {
            reschedule();
            return;
        }
    }
    // We've run out of stages and generators, give up.
    if((stage == Stage.FINISHED || isCancelled) && !isStarted) {
        notifyFailed();
    }

    // Otherwise a generator started a new load and we expect to be called back in
    // onDataFetcherReady.
}
```

注释1处，循环调用 startNext 方法。这时候调用的是 SourceGenerator 的 startNext 方法。

```java
@Override
public boolean startNext() {
    if(dataToCache != null) {
        //注释1处，这时候，dataToCache 不为空，调用 cacheData 方法。
        Object data = dataToCache;
        dataToCache = null;
        cacheData(data);
    }

    if(sourceCacheGenerator != null && sourceCacheGenerator.startNext()) {
        return true;
    }
    sourceCacheGenerator = null;

    loadData = null;
    boolean started = false;
    while(!started && hasNextModelLoader()) {
        loadData = helper.getLoadData().get(loadDataListIndex++);
        if(loadData != null && (helper.getDiskCacheStrategy().isDataCacheable(
                loadData.fetcher.getDataSource()) || helper.hasLoadPath(
                loadData.fetcher.getDataClass()))) {
            started = true;
            startNextLoad(loadData);
        }
    }
    return started;
}

```

注释1处，这时候，dataToCache 不为空，调用 cacheData 方法。

```java
private void cacheData(Object dataToCache) {
    long startTime = LogTime.getLogTime();
    try {
        Encoder < Object > encoder = helper.getSourceEncoder(dataToCache);
        DataCacheWriter < Object > writer =
            new DataCacheWriter < > (encoder, dataToCache, helper.getOptions());
        originalKey = new DataCacheKey(loadData.sourceKey, helper.getSignature());
        //注释1处，调用 DiskLruCacheWrapper 的 put 方法。
        helper.getDiskCache().put(originalKey, writer);
        if(Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(
                TAG,
                "Finished encoding source to cache" + ", key: " +
                originalKey + ", data: " + dataToCache + ", encoder: " +
                encoder + ", duration: " + LogTime.getElapsedMillis(
                    startTime));
        }
    } finally {
        loadData.fetcher.cleanup();
    }
    //注释2处，缓存完数据以后，构建了一个 DataCacheGenerator 对象。后面会用这个对象来加载缓存数据。
    sourceCacheGenerator =
        new DataCacheGenerator(Collections.singletonList(loadData.sourceKey),
            helper, this);
}
```
注释1处，调用 DiskLruCacheWrapper 的 put 方法。


```java
@Override
public void put(Key key, Writer writer) {
    // We want to make sure that puts block so that data is available when put completes. We may
    // actually not write any data if we find that data is written by the time we acquire the lock.
    String safeKey = safeKeyGenerator.getSafeKey(key);
    writeLocker.acquire(safeKey);
    try {
        if(Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Put: Obtained: " + safeKey + " for for Key: " + key);
        }
        try {
            // We assume we only need to put once, so if data was written while we were trying to get
            // the lock, we can simply abort.
            DiskLruCache diskCache = getDiskCache();
            Value current = diskCache.get(safeKey);
            if(current != null) {
                return;
            }

            DiskLruCache.Editor editor = diskCache.edit(safeKey);
            if(editor == null) {
                throw new IllegalStateException(
                    "Had two simultaneous puts for: " + safeKey);
            }
            try {
                //注释1处，构建文件，写入文件
                File file = editor.getFile(0);
                if(writer.write(file)) {
                    editor.commit();
                }
            } finally {
                editor.abortUnlessCommitted();
            }
        } catch(IOException e) {
            if(Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unable to put to disk cache", e);
            }
        }
    } finally {
        writeLocker.release(safeKey);
    }
}
```

注释1处，构建文件，写入文件。

cacheData 方法。注释2处，缓存完数据以后，构建了一个 DataCacheGenerator 对象。后面在 SourceGenerator 的 startNext方法中判断 sourceCacheGenerator 不为null，
会用这个对象继续来 startNext ，从而加载缓存数据。

DataCacheGenerator 的 startNext 方法

```java
@Override
public boolean startNext() {
    while(modelLoaders == null || !hasNextModelLoader()) {
        sourceIdIndex++;
        if(sourceIdIndex >= cacheKeys.size()) {
            return false;
        }

        //图片url
        Key sourceId = cacheKeys.get(sourceIdIndex);
        // PMD.AvoidInstantiatingObjectsInLoops The loop iterates a limited number of times
        // and the actions it performs are much more expensive than a single allocation.
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        Key originalKey = new DataCacheKey(sourceId, helper.getSignature());
        //注释1处，这里从磁盘缓存中获取缓存文件就不为null了。
        cacheFile = helper.getDiskCache().get(originalKey);
        if(cacheFile != null) {
            this.sourceKey = sourceId;
            /**
			 * 这里的modelLoaders 是一个 List<ModelLoader<File, ?>> 对象。
			 * 1. ByteBufferFileLoader
			 * 2. FileLoader
			 * 3. FileLoader
			 * 4. UnitModelLoader
			 * 
			 */
            modelLoaders = helper.getModelLoaders(cacheFile);
            modelLoaderIndex = 0;
        }
    }

    loadData = null;
    boolean started = false;
    while(!started && hasNextModelLoader()) {
        ModelLoader < File, ?> modelLoader = modelLoaders.get(
            modelLoaderIndex++);
        loadData =
            modelLoader.buildLoadData(
                cacheFile, helper.getWidth(), helper.getHeight(), helper.getOptions()
            );
        if(loadData != null && helper.hasLoadPath(loadData.fetcher.getDataClass())) {
            started = true;
            //注释1处，调用 ByteBufferFileLoader.ByteBufferFetcher 的 loadData 方法。
			
            loadData.fetcher.loadData(helper.getPriority(), this);
        }
    }
    //注释2处，返回true
    return started;
}
```

注释1处，调用 ByteBufferFileLoader.ByteBufferFetcher 的 loadData 方法。回调是 DataCacheGenerator 对象。

```java
@Override
public void loadData(@NonNull Priority priority, @NonNull DataCallback <? super ByteBuffer> callback) {
    ByteBuffer result;
    try {
        //缓存文件是：
		// /data/user/0/com.hm.bitmaploadexample/cache/image_manager_disk_cache/7d2fca3704dc60f2da82b3484e656e6e35bee407249878dea160c8809af902f1.0
        result = ByteBufferUtil.fromFile(file);
        //回调 DataCacheGenerator 对象的 onDataReady 方法。
        callback.onDataReady(result);
    } catch(IOException e) {
        if(Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Failed to obtain ByteBuffer for file", e);
        }
        callback.onLoadFailed(e);
    }
}
```

DataCacheGenerator 对象的 onDataReady 方法。

```java
@Override
public void onDataReady(Object data) {
    //回调 SourceGenerator 对象的 onDataFetcherReady 方法。
    cb.onDataFetcherReady(sourceKey, data, loadData.fetcher, DataSource.DATA_DISK_CACHE, sourceKey);
}
```

SourceGenerator 对象的 onDataFetcherReady 方法。

```java
@Override
  public void onDataFetcherReady(
      Key sourceKey, Object data, DataFetcher<?> fetcher, DataSource dataSource, Key attemptedKey) {
    // This data fetcher will be loading from a File and provide the wrong data source, so override
    // with the data source of the original fetcher
	//调用 MultiModelLoader.MultiFetcher 的 getDataSources 方法。
	//然后回调到 DecodeJob 的 onDataFetcherReady 方法。	
    cb.onDataFetcherReady(sourceKey, data, fetcher, loadData.fetcher.getDataSource(), sourceKey);
  }
```

DecodeJob 的 onDataFetcherReady 方法。

```java
@Override
public void onDataFetcherReady(
    Key sourceKey, Object data, DataFetcher <? > fetcher, DataSource dataSource,
    Key attemptedKey) {
    this.currentSourceKey = sourceKey;
    //data 是一个 java.nio.DirectByteBuffer[pos=0 lim=437344 cap=437344] 对象
    this.currentData = data;
    this.currentFetcher = fetcher;
    this.currentDataSource = dataSource;
    this.currentAttemptingKey = attemptedKey;
    this.isLoadingFromAlternateCacheKey = sourceKey != decodeHelper.getCacheKeys()
        .get(0);

    if(Thread.currentThread() != currentThread) {
        runReason = RunReason.DECODE_DATA;
        callback.reschedule(this);
    } else {
        GlideTrace.beginSection("DecodeJob.decodeFromRetrievedData");
        try {
		    //注释1处，解析数据
            decodeFromRetrievedData();
        } finally {
            GlideTrace.endSection();
        }
    }
}
```
注释1处，解析数据  DecodeJob 的 decodeFromRetrievedData 方法。

```java
private void decodeFromRetrievedData() {
    if(Log.isLoggable(TAG, Log.VERBOSE)) {
        logWithTimeAndKey(
            "Retrieved data",
            startFetchTime,
            "data: " + currentData + ", cache key: " +
            currentSourceKey + ", fetcher: " + currentFetcher);
    }
    Resource <R> resource = null;
    try {
        //注释1处，调用 DecodeJob 的 decodeFromData 方法。
        resource = decodeFromData(currentFetcher, currentData,
            currentDataSource);
    } catch(GlideException e) {
        e.setLoggingDetails(currentAttemptingKey, currentDataSource);
        throwables.add(e);
    }
    if(resource != null) {
        //注释2处，调用 DecodeJob 的 notifyEncodeAndRelease 方法。
        notifyEncodeAndRelease(resource, currentDataSource,
            isLoadingFromAlternateCacheKey);
    } else {
        runGenerators();
    }
}
```

注释1处，调用 DecodeJob 的 decodeFromData 方法。

```java
private <Data> Resource<R> decodeFromData(
      DataFetcher<?> fetcher, Data data, DataSource dataSource) throws GlideException {
    try {
      if (data == null) {
        return null;
      }
      long startTime = LogTime.getLogTime();
      //注释1处，调用 DecodeJob 的 decodeFromFetcher 方法。
      Resource<R> result = decodeFromFetcher(data, dataSource);
      if (Log.isLoggable(TAG, Log.VERBOSE)) {
        logWithTimeAndKey("Decoded result " + result, startTime);
      }
      //注释2处，最终返回的是 LazyBitmapDrawableResource 对象。
      return result;
    } finally {
      fetcher.cleanup();
    }
  }
```

在我们的例子中，最终会调用 ByteBufferBitmapDecoder 的 decode 方法。

```java
@Override
public Resource < Bitmap > decode(@NonNull ByteBuffer source, int width, int height, @
		NonNull Options options)
		throws IOException {
    InputStream is = ByteBufferUtil.toStream(source);
    return downsampler.decode(is, width, height, options);
}
```

注释2处，最终返回的是 LazyBitmapDrawableResource 对象。

解析完毕后，回到 decodeFromRetrievedData 方法 注释2处，调用 DecodeJob 的 notifyEncodeAndRelease 方法。

```java
private void notifyEncodeAndRelease(
    Resource <R> resource, DataSource dataSource, boolean isLoadedFromAlternateCacheKey
) {
    if(resource instanceof Initializable) {
        ((Initializable) resource).initialize();
    }

    Resource <R> result = resource;
    LockedResource <R> lockedResource = null;
    if(deferredEncodeManager.hasResourceToEncode()) {
        lockedResource = LockedResource.obtain(resource);
        result = lockedResource;
    }

    //注释1处，调用 DecodeJob 的 notifyComplete 方法。通知成功
    notifyComplete(result, dataSource, isLoadedFromAlternateCacheKey);

    stage = Stage.ENCODE;
    try {
        if(deferredEncodeManager.hasResourceToEncode()) {
            deferredEncodeManager.encode(diskCacheProvider, options);
        }
    } finally {
        if(lockedResource != null) {
            lockedResource.unlock();
        }
    }
    // Call onEncodeComplete outside the finally block so that it's not called if the encode process
    // throws.
    onEncodeComplete();
}

```


注释1处，调用 DecodeJob 的 notifyComplete 方法。通知成功

```java
private void notifyComplete(
      Resource<R> resource, DataSource dataSource, boolean isLoadedFromAlternateCacheKey) {
    setNotifiedOrThrow();
    //回调 EngineJob 的 onResourceReady 方法。
    callback.onResourceReady(resource, dataSource, isLoadedFromAlternateCacheKey);
}
```

EngineJob 的 onResourceReady 方法。

```java
@Override
public void onResourceReady(
      Resource<R> resource, DataSource dataSource, boolean isLoadedFromAlternateCacheKey) {
    synchronized (this) {
      this.resource = resource;
      this.dataSource = dataSource;
      this.isLoadedFromAlternateCacheKey = isLoadedFromAlternateCacheKey;
    }
    //注释1处，调用 EngineJob 的 notifyCallbacksOfResult 方法。
    notifyCallbacksOfResult();
}
```

注释1处，调用 EngineJob 的 notifyCallbacksOfResult 方法。

```java
@Synthetic
void notifyCallbacksOfResult() {
    ResourceCallbacksAndExecutors copy;
    Key localKey;
    EngineResource <?> localResource;
    synchronized(this) {
        stateVerifier.throwIfRecycled();
        if(isCancelled) {
            // TODO: Seems like we might as well put this in the memory cache instead of just recycling
            // it since we've gotten this far...
            resource.recycle();
            release();
            return;
        } else if(cbs.isEmpty()) {
            throw new IllegalStateException(
                "Received a resource without any callbacks to notify"
            );
        } else if(hasResource) {
            throw new IllegalStateException("Already have resource");
        }
        engineResource = engineResourceFactory.build(resource,
            isCacheable, key, resourceListener);
        // Hold on to resource for duration of our callbacks below so we don't recycle it in the
        // middle of notifying if it synchronously released by one of the callbacks. Acquire it under
        // a lock here so that any newly added callback that executes before the next locked section
        // below can't recycle the resource before we call the callbacks.
        hasResource = true;
        copy = cbs.copy();
        incrementPendingCallbacks(copy.size() + 1);

        localKey = key;
        localResource = engineResource;
    }

    engineJobListener.onEngineJobComplete(this, localKey, localResource);

    for(final ResourceCallbackAndExecutor entry: copy) {
        //注释1处，这里最终会调用 SingleRequest 的 onResourceReady 方法。
        entry.executor.execute(new CallResourceReady(entry.cb));
    }
    decrementPendingCallbacks();
}
```

注释1处，这里最终会调用 SingleRequest 的 onResourceReady 方法。

```java
@Override
  public void onResourceReady(
      Resource<?> resource, DataSource dataSource, boolean isLoadedFromAlternateCacheKey) {
    stateVerifier.throwIfRecycled();
    Resource<?> toRelease = null;
    try {
      synchronized (requestLock) {
        loadStatus = null;
        if (resource == null) {
          GlideException exception =
              new GlideException(
                  "Expected to receive a Resource<R> with an "
                      + "object of "
                      + transcodeClass
                      + " inside, but instead got null.");
          onLoadFailed(exception);
          return;
        }

        Object received = resource.get();
        if (received == null || !transcodeClass.isAssignableFrom(received.getClass())) {
          toRelease = resource;
          this.resource = null;
          GlideException exception =
              new GlideException(
                  "Expected to receive an object of "
                      + transcodeClass
                      + " but instead"
                      + " got "
                      + (received != null ? received.getClass() : "")
                      + "{"
                      + received
                      + "} inside"
                      + " "
                      + "Resource{"
                      + resource
                      + "}."
                      + (received != null
                          ? ""
                          : " "
                              + "To indicate failure return a null Resource "
                              + "object, rather than a Resource object containing null data."));
          onLoadFailed(exception);
          return;
        }

        if (!canSetResource()) {
          toRelease = resource;
          this.resource = null;
          // We can't put the status to complete before asking canSetResource().
          status = Status.COMPLETE;
          return;
        }

        //注释1处，调用重载的 onResourceReady 方法。
        onResourceReady(
            (Resource<R>) resource, (R) received, dataSource, isLoadedFromAlternateCacheKey);
      }
    } finally {
      if (toRelease != null) {
        engine.release(toRelease);
      }
    }
  }
```

注释1处，调用重载的 onResourceReady 方法。

```java
private void onResourceReady(
    Resource <R> resource, R result, DataSource dataSource, boolean isAlternateCacheKey) {
    // We must call isFirstReadyResource before setting status.
    boolean isFirstResource = isFirstReadyResource();
    status = Status.COMPLETE;
    this.resource = resource;

    if(glideContext.getLogLevel() <= Log.DEBUG) {
        Log.d(
            GLIDE_TAG,
            "Finished loading " + result.getClass().getSimpleName() +
            " from " + dataSource + " for " + model + " with size [" +
            width + "x" + height + "] in " + LogTime.getElapsedMillis(
                startTime) + " ms");
    }

    isCallingCallbacks = true;
    try {
        boolean anyListenerHandledUpdatingTarget = false;
        if(requestListeners != null) {
            for(RequestListener < R > listener: requestListeners) {
                anyListenerHandledUpdatingTarget |=
                    listener.onResourceReady(result, model, target,
                        dataSource, isFirstResource);
            }
        }
        anyListenerHandledUpdatingTarget |=
            targetListener != null && targetListener.onResourceReady(result,
                model, target, dataSource, isFirstResource);

        if(!anyListenerHandledUpdatingTarget) {
            Transition <? super R > animation = animationFactory.build(
                dataSource, isFirstResource);
            //注释1处，回调 DrawableImageViewTarget 的 onResourceReady 方法。
            target.onResourceReady(result, animation);
        }
    } finally {
        isCallingCallbacks = false;
    }

    notifyLoadSuccess();
}
```
注释1处，回调 DrawableImageViewTarget 的 onResourceReady 方法。会走到父类 ImageViewTarget 的 onResourceReady 方法。

ImageViewTarget 的 onResourceReady 方法。

```java
@Override
public void onResourceReady(@NonNull Z resource, @Nullable Transition <? super Z >
    transition) {
    if(transition == null || !transition.transition(resource, this)) {
        //注释1处，调用 setResourceInternal 方法。
        setResourceInternal(resource);
    } else {
        maybeUpdateAnimatable(resource);
    }
}
```

ImageViewTarget 的 setResourceInternal 方法。


```java
private void setResourceInternal(@Nullable Z resource) {
    // Order matters here. Set the resource first to make sure that the Drawable has a valid and
    // non-null Callback before starting it.
	//注释1处，调用 DrawableImageViewTarget 的 setResource 方法。	
    setResource(resource);
    maybeUpdateAnimatable(resource);
  }
```

注释1处，调用 DrawableImageViewTarget 的 setResource 方法。

```java
 @Override
protected void setResource(@Nullable Drawable resource) {
    //这里最终给 ImageView 设置了resource，是一个BitmapDrawable。
    view.setImageDrawable(resource);
}
```

* 告一段落，终于把图片显示到ImageView上了。


        

















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


 