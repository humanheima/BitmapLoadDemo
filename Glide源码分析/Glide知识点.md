### 整个加载流程

比如一个数据类型A 是一个网络上的图片地址 ：`https://zmdcharactercdn.zhumengdao.com/0566bcda741e8053f24b3fa3d765beea.png`

数据A -> ModelLoader 把 A 转化后的数据类型B -> DataFetcher 从 数据 B 中读取数据 -> DecodePath 把读取的数据解码成 Bitmap (这个过程涉及 transform，transcoder，downsample 等操作) 。

比如 StringLoader，就是把一个String，比如 `https://zmdcharactercdn.zhumengdao.com/0566bcda741e8053f24b3fa3d765beea.png` ，
转化成一个数据类型，比如 `InputStream`，然后 DataFetcher，比如说 `OkHttpStreamFetcher`，就从这个流里面读取数据。然后 `DecodePath` 把读取的数据解码成 Bitmap。

RegistryFactory 添加了所有的 Loader

### Bitmap 是怎么加入内存缓存的。

1. 在传入的Context 生命周期结束的时候。
2. 内存紧张的时候，会自动清理。这个没验证过。
3. 在 DecodeJob 的 onResourceDecoded 方法。

```java
@Synthetic
@NonNull
<Z> Resource<Z> onResourceDecoded(DataSource dataSource, @NonNull Resource<Z> decoded) {
    @SuppressWarnings("unchecked")
    Class<Z> resourceSubClass = (Class<Z>) decoded.get().getClass();
    Transformation<Z> appliedTransformation = null;
    Resource<Z> transformed = decoded;
    if (dataSource != DataSource.RESOURCE_DISK_CACHE) {
      //应用 Transformation，这里是FitCenter
      appliedTransformation = decodeHelper.getTransformation(resourceSubClass);
      //注释2处，执行 Transformation
      transformed = appliedTransformation.transform(glideContext, decoded, width, height);
    }
    if (!decoded.equals(transformed)) {
        //注释1处，有变换的话，把decoded 回收
        decoded.recycle();
    }

    final EncodeStrategy encodeStrategy;
    final ResourceEncoder<Z> encoder;
    if (decodeHelper.isResourceEncoderAvailable(transformed)) {
      encoder = decodeHelper.getResultEncoder(transformed);
      encodeStrategy = encoder.getEncodeStrategy(options);
    } else {
      encoder = null;
      encodeStrategy = EncodeStrategy.NONE;
    }

    Resource<Z> result = transformed;
    boolean isFromAlternateCacheKey = !decodeHelper.isSourceKey(currentSourceKey);
    //注释1处，貌似isFromAlternateCacheKey这个值一直是false，没法进到这if条件
    if (diskCacheStrategy.isResourceCacheable(
        isFromAlternateCacheKey, dataSource, encodeStrategy)) {
      if (encoder == null) {
        throw new Registry.NoResultEncoderAvailableException(transformed.get().getClass());
      }
      final Key key;
      switch (encodeStrategy) {
        case SOURCE:
          key = new DataCacheKey(currentSourceKey, signature);
          break;
        case TRANSFORMED:
          key =
              new ResourceCacheKey(
                  decodeHelper.getArrayPool(),
                  currentSourceKey,
                  signature,
                  width,
                  height,
                  appliedTransformation,
                  resourceSubClass,
                  options);
          break;
        default:
          throw new IllegalArgumentException("Unknown strategy: " + encodeStrategy);
      }

      LockedResource<Z> lockedResult = LockedResource.obtain(transformed);
      deferredEncodeManager.init(key, encoder, lockedResult);
      result = lockedResult;
    }
    return result;
}
```

注释1处，有变换的话，把decoded 回收，调用 BitmapResource 的 recycle 方法。

```java
@Override
public void recycle() {
    //注释1处，调用  LruBitmapPool 的 put 方法。
    bitmapPool.put(bitmap);
}
```

LruBitmapPool 的 put 方法。

```java
@Override
public synchronized void put(Bitmap bitmap) {
    if (bitmap == null) {
      throw new NullPointerException("Bitmap must not be null");
    }
    if (bitmap.isRecycled()) {
      throw new IllegalStateException("Cannot pool recycled bitmap");
    }
    if (!bitmap.isMutable()
        || strategy.getSize(bitmap) > maxSize
        || !allowedConfigs.contains(bitmap.getConfig())) {
      if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(
            TAG,
            "Reject bitmap from pool"
                + ", bitmap: "
                + strategy.logBitmap(bitmap)
                + ", is mutable: "
                + bitmap.isMutable()
                + ", is allowed config: "
                + allowedConfigs.contains(bitmap.getConfig()));
      }
      bitmap.recycle();
      return;
    }

    final int size = strategy.getSize(bitmap);
    //加入到内存缓存中
    strategy.put(bitmap);
    tracker.add(bitmap);

    puts++;
    currentSize += size;

    if (Log.isLoggable(TAG, Log.VERBOSE)) {
      Log.v(TAG, "Put bitmap in pool=" + strategy.logBitmap(bitmap));
    }
    dump();

    evict();
}
```

### Transform后的Bitmap会加入缓存吗

取决于使用的 DiskCacheStrategy，默认使用的是  DiskCacheStrategy.AUTOMATIC ，不会缓存。如果使用 DiskCacheStrategy.ALL 则缓存。


```java
/**
 * Caches remote data with both {@link #DATA} and {@link #RESOURCE}, and local data with {@link
 * #RESOURCE} only.
 */
public static final DiskCacheStrategy ALL =
   
    new DiskCacheStrategy() {
    
        @Override
        public boolean isDataCacheable(DataSource dataSource) {
            return dataSource == DataSource.REMOTE;
        }


        /**
         * Returns true if this request should cache the final transformed resource.
         *
         * @param isFromAlternateCacheKey {@code true} if the resource we've decoded was loaded using an
         *     alternative, rather than the primary, cache key.
         * @param dataSource Indicates where the data used to decode the resource was originally
         *     retrieved.
         * @param encodeStrategy The {@link EncodeStrategy} the {@link
         *     com.bumptech.glide.load.ResourceEncoder} will use to encode the resource.
         */
        @Override
        public boolean isResourceCacheable(
            boolean isFromAlternateCacheKey, DataSource dataSource, EncodeStrategy encodeStrategy) {
            // 注释1处，会缓存 tranformed 的Bitmap
            return dataSource != DataSource.RESOURCE_DISK_CACHE && dataSource != DataSource.MEMORY_CACHE;
        }

        @Override
        public boolean decodeCachedResource() {
            return true;
        }

        @Override
        public boolean decodeCachedData() {
            return true;
        }
    };

```


### 图片加载流程

1. 先从内存缓存中查找
2. 从 降低采样率/应用了transform 的磁盘缓存文件中查找 ResourceCacheGenerator
3. 从 包含原始资源数据的磁盘缓存文件中查找 DataCacheGenerator
4. 加载网络资源 SourceGenerator





### 生命周期感知相关

Glide.with(context).load(url).into(imageView)

通过传递的 context 获取到 Lifecycle，然后 Lifecycle 添加了一个 LifecycleListener(一个 RequestManager 对象)，在生命周期onStop的时候，停止加载。
在 onStart 的时候，开始加载，或者 resume 加载。

RequestManager 的 onStop 方法
```java
/**
   * Lifecycle callback that unregisters for connectivity events (if the
   * android.permission.ACCESS_NETWORK_STATE permission is present) and pauses in progress loads
   * and clears all resources if {@link #clearOnStop()} is called.
   */
  @Override
  public synchronized void onStop() {
    targetTracker.onStop();
    if (clearOnStop) {
        //清除
      clearRequests();
    } else {
        //或者暂停
      pauseRequests();
    }
  }

  //调用 RequestTracker 的 pauseRequests 方法
  public synchronized void pauseRequests() {
      requestTracker.pauseRequests();
  }

```

//RequestTracker 的 pauseRequests 方法 和 resumeRequests 方法

```java

/** Stops any in progress requests. */
public void pauseRequests() {
    isPaused = true;
    for (Request request : Util.getSnapshot(requests)) {
        if (request.isRunning()) {
            // Avoid clearing parts of requests that may have completed (thumbnails) to avoid blinking
            // in the UI, while still making sure that any in progress parts of requests are immediately
            // stopped.
            //暂停所有的请求
            request.pause();
            pendingRequests.add(request);
        }
    }
}


/** Starts any not yet completed or failed requests. */
public void resumeRequests() {
    isPaused = false;
    for (Request request : Util.getSnapshot(requests)) {
        // We don't need to check for cleared here. Any explicit clear by a user will remove the
        // Request from the tracker, so the only way we'd find a cleared request here is if we cleared
        // it. As a result it should be safe for us to resume cleared requests.
        if (!request.isComplete() && !request.isRunning()) {
            //重启所有的请求
            request.begin();
        }
    }
    pendingRequests.clear();
}
```




### 定制请求

RequestOptions 类来在多个请求之间共享

如果你想让你的应用的不同部分之间共享相同的加载选项，你也可以初始化一个新的 RequestOptions 对象，并在每次加载时通过 apply() 方法传入这个对象：

```java
RequestOptions cropOptions = new RequestOptions().centerCrop(context);

Glide.with(fragment)
.load(url)
.apply(cropOptions)
.into(imageView);

```
apply() 方法可以被调用多次，因此 RequestOption 可以被组合使用。如果 RequestOptions 对象之间存在相互冲突的设置，那么只有最后一个被应用的 RequestOptions 会生效。


### 在失败时开始新的请求

从 Glide 4.3.0 开始，你现在可以使用 error API 来指定一个 RequestBuilder，以在主请求失败时开始一次新的加载。例如，在请求 primaryUrl 失败后加载 fallbackUrl：

```java
Glide.with(fragment)
.load(primaryUrl)
.error(Glide.with(fragment)
.load(fallbackUrl))
.into(imageView);
```

如果主请求成功完成，这个error RequestBuilder 将不会被启动。如果你同时指定了一个 thumbnail() 和一个 error() RequestBuilder，则这个后备的 RequestBuilder 将在主请求失败时启动，即使缩略图请求成功也是如此。

### ImageView的自动变换
在Glide中，当你为一个 ImageView 开始加载时，Glide可能会自动应用 FitCenter 或 CenterCrop ，这取决于view的 ScaleType 。如果 scaleType 是 CENTER_CROP , Glide 将会自动应用 CenterCrop 变换。如果 scaleType 为 FIT_CENTER 或 CENTER_INSIDE ，Glide会自动使用 FitCenter 变换。

当然，你总有权利覆写默认的变换，只需要一个带有 Transformation 集合的 RequestOptions 即可。另外，你也可以通过使用 dontTransform() 确保不会自动应用任何变换。

### Glide4 的官方文档

[资源重用](https://muyangmin.github.io/glide-docs-cn/doc/resourcereuse.html)
