
应用一个高斯模糊的 Transform 时候，会调用到 Transform 的 transform 方法。
```java
private void sourceCodeTest() {
        Glide.with(this)
                .load(Images.imageUrls[1])
                .transform(new BlurTransformation(this, 15))
                .into(imageView1);
    }
```



应用 transform 时候的方法调用栈。DecodeJob 的 onResourceDecoded 方法中调用 transform 方法。


```java
 I  transform: 
        java.lang.Throwable
        at com.hm.bitmaploadexample.transform.BlurTransformation.transform(BlurTransformation.java:42)
        at com.bumptech.glide.load.resource.bitmap.BitmapTransformation.transform(BitmapTransformation.java:85)
        at com.bumptech.glide.load.engine.DecodeJob.onResourceDecoded(DecodeJob.java:568)
        at com.bumptech.glide.load.engine.DecodeJob$DecodeCallback.onResourceDecoded(DecodeJob.java:632)
        at com.bumptech.glide.load.engine.DecodePath.decode(DecodePath.java:60)
        at com.bumptech.glide.load.engine.LoadPath.loadWithExceptionList(LoadPath.java:76)
        at com.bumptech.glide.load.engine.LoadPath.load(LoadPath.java:57)
        at com.bumptech.glide.load.engine.DecodeJob.runLoadPath(DecodeJob.java:529)
        at com.bumptech.glide.load.engine.DecodeJob.decodeFromFetcher(DecodeJob.java:493)
        at com.bumptech.glide.load.engine.DecodeJob.decodeFromData(DecodeJob.java:479)
        at com.bumptech.glide.load.engine.DecodeJob.decodeFromRetrievedData(DecodeJob.java:430)
        at com.bumptech.glide.load.engine.DecodeJob.onDataFetcherReady(DecodeJob.java:394)
        at com.bumptech.glide.load.engine.DataCacheGenerator.onDataReady(DataCacheGenerator.java:94)
        at com.bumptech.glide.load.model.ByteBufferFileLoader$ByteBufferFetcher.loadData(ByteBufferFileLoader.java:62)
        at com.bumptech.glide.load.engine.DataCacheGenerator.startNext(DataCacheGenerator.java:74)
        at com.bumptech.glide.load.engine.DecodeJob.runGenerators(DecodeJob.java:311)
        at com.bumptech.glide.load.engine.DecodeJob.runWrapped(DecodeJob.java:277)
        at com.bumptech.glide.load.engine.DecodeJob.run(DecodeJob.java:235)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
        at java.lang.Thread.run(Thread.java:919)
        at com.bumptech.glide.load.engine.executor.GlideExecutor$DefaultThreadFactory$1.run(GlideExecutor.java:393)

```


应用 transform 的地方。

```java
@Synthetic
@NonNull
  <Z> Resource<Z> onResourceDecoded(DataSource dataSource, @NonNull Resource<Z> decoded) {
    @SuppressWarnings("unchecked")
    Class<Z> resourceSubClass = (Class<Z>) decoded.get().getClass();
    Transformation<Z> appliedTransformation = null;
    Resource<Z> transformed = decoded;
    if (dataSource != DataSource.RESOURCE_DISK_CACHE) {
      appliedTransformation = decodeHelper.getTransformation(resourceSubClass);
      //注释1处，应用 transform
      transformed = appliedTransformation.transform(glideContext, decoded, width, height);
    }
    // TODO: Make this the responsibility of the Transformation.
    if (!decoded.equals(transformed)) {
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

    //注释2处，将 transform 后的结果赋值给 result
    Resource<Z> result = transformed;
    boolean isFromAlternateCacheKey = !decodeHelper.isSourceKey(currentSourceKey);
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

      //这里没有做什么修改
      LockedResource<Z> lockedResult = LockedResource.obtain(transformed);
      //注释3处，如果 DiskCacheStrategy 允许缓存transfrom 的 结果，则创建一个DeferredEncodeManager 对象。
      //然后，在 DecodeJob 类的 notifyEncodeAndRelease 方法中，会调用 DeferredEncodeManager 的 encode 方法。缓存到磁盘。
      deferredEncodeManager.init(key, encoder, lockedResult);
      result = lockedResult;
    }
    //注释3处，返回 result
    return result;
  }
```

