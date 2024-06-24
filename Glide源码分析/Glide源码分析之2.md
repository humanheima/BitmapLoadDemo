
DataFetcherGenerator: 使用已注册的 ModelLoader 实例和一个模型(model)来生成一系列的 DataFetcher 实例。类型有：




ModelLoader

```
一个用于将任意复杂的数据模型转换为具体数据类型的工厂接口,
这种数据类型可以被 {@link DataFetcher} 用来获取由该模型表示的资源的数据。

这个接口有两个目标:
1. 将特定的模型转换为可以解码成资源的数据类型。

2. 允许将模型与视图的尺寸组合,以获取特定大小的资源。
```

* 类型有

* ByteBufferFileLoader
* FileLoader: StreamFactory 类型
* FileLoader: FileDescriptorFactory 类型
* UnitModelLoader


DataFetcher: 延迟获取可用于加载资源的数据。比如加载 InputStream, byte[], File etc。


开始从 ResourceCacheGenerator 没有 cacheFile


从 DataCacheGenerator 找到了缓存文件

`/data/user/0/com.hm.bitmaploadexample/cache/image_manager_disk_cache/2829dd349db5d3f90cf167007f009b7b7c5e3cba8168ba2cd3d4ad6cb52d65d9.0`

```java
@Override
public boolean startNext() {
    while(modelLoaders == null || !hasNextModelLoader()) {
        sourceIdIndex++;
        if(sourceIdIndex >= cacheKeys.size()) {
            //注释0处，
            return false;
        }

        //sourceId 是 图片url
        Key sourceId = cacheKeys.get(sourceIdIndex);
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        Key originalKey = new DataCacheKey(sourceId, helper.getSignature());
        //获取磁盘文件
        cacheFile = helper.getDiskCache().get(originalKey);
        if(cacheFile != null) {
            this.sourceKey = sourceId;
            //注释1处，
            modelLoaders = helper.getModelLoaders(cacheFile);
            modelLoaderIndex = 0;
        }
    }

    loadData = null;
    boolean started = false;
    while(!started && hasNextModelLoader()) {
        //注释2处，开始循环用ModelLoader加载数据
        ModelLoader < File, ?> modelLoader = modelLoaders.get(modelLoaderIndex++);
        //注释3处，构建LoadData
        loadData =
            modelLoader.buildLoadData(
                cacheFile, helper.getWidth(), helper.getHeight(), helper.getOptions());

        if(loadData != null && helper.hasLoadPath(loadData.fetcher.getDataClass())) {
            started = true;
            // 注释4处，调用 fetcher 的 loadData 方法
            loadData.fetcher.loadData(helper.getPriority(), this);
        }
    }
    return started;
}

```

注释1处，ModelLoader 类型有

* ByteBufferFileLoader
* FileLoader: StreamFactory 类型
* FileLoader: FileDescriptorFactory 类型
* UnitModelLoader


注释2处，开始循环用ModelLoader加载数据。第一个是 ByteBufferFileLoader。
注释3处，构建LoadData。

ByteBufferFileLoader 的 buildLoadData 方法。

```java
@Override
public LoadData<ByteBuffer> buildLoadData(@NonNull File file, int width, int height, @NonNull Options options) {
    //使用的Fetcher 是 ByteBufferFetcher
    return new LoadData<>(new ObjectKey(file), new ByteBufferFetcher(file));
}
```

注释4处，调用 fetcher 的 loadData 方法。先看 ByteBufferFetcher 的 loadData 方法

```
@Override
public void loadData(@NonNull Priority priority, @NonNull DataCallback <?super ByteBuffer> callback) {
    ByteBuffer result;
    try {
        //返回一个 java.nio.DirectByteBuffer[pos=0 lim=320261 cap=320261] 对象。
        result = ByteBufferUtil.fromFile(file);
        //回调 DataCacheGenerator 的 onDataReady 方法
        callback.onDataReady(result);
    } catch (IOException e) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Failed to obtain ByteBuffer for file", e);
        }
        callback.onLoadFailed(e);
    }
}
```

```java
@Override
public void onDataReady(Object data) {
    cb.onDataFetcherReady(sourceKey, data, loadData.fetcher, DataSource.DATA_DISK_CACHE, sourceKey);
}
```

回到 DecodeJob 的 onDataFetcherReady 方法。

```java
@Override
public void onDataFetcherReady(
    Key sourceKey, Object data, DataFetcher <? > fetcher, DataSource dataSource, Key attemptedKey) {
    this.currentSourceKey = sourceKey;
    this.currentData = data;
    this.currentFetcher = fetcher;
    this.currentDataSource = dataSource;
    this.currentAttemptingKey = attemptedKey;
    this.isLoadingFromAlternateCacheKey = sourceKey != decodeHelper.getCacheKeys().get(0);

    if (Thread.currentThread() != currentThread) {
        runReason = RunReason.DECODE_DATA;
        callback.reschedule(this);
    } else {
        GlideTrace.beginSection("DecodeJob.decodeFromRetrievedData");
        try {
            //注释1处，调用 decodeFromRetrievedData 方法
            decodeFromRetrievedData();
        } finally {
            GlideTrace.endSection();
        }
    }
}

```

DecodeJob 的 decodeFromRetrievedData 方法

```java
private void decodeFromRetrievedData() {
    //...
    Resource < R > resource = null;
    try {

        //注释1处，解码
        resource = decodeFromData(currentFetcher, currentData, currentDataSource);
    } catch (GlideException e) {
        e.setLoggingDetails(currentAttemptingKey, currentDataSource);
        throwables.add(e);
    }
    if (resource != null) {
        //注释2处，notifyEncodeAndRelease
        notifyEncodeAndRelease(resource, currentDataSource, isLoadingFromAlternateCacheKey);
    } else {
        runGenerators();
    }
}

```
注释1处，解码。关键的地方。

currentFetcher = ByteBufferFetcher 
currentData = DirectByteBuffer

currentDataSource = DATA_DISK_CACHE

```java
private <Data> Resource <R> decodeFromData(
    DataFetcher <?> fetcher, Data data, DataSource dataSource) throws GlideException {
    try {
        if (data == null) {
            return null;
        }
        long startTime = LogTime.getLogTime();
        Resource <R> result = decodeFromFetcher(data, dataSource);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Decoded result " + result, startTime);
        }
        return result;
    } finally {
        fetcher.cleanup();
    }
}
```


```java
private <Data> Resource <R> decodeFromFetcher(Data data, DataSource dataSource) throws GlideException {
    //注释1处，返回的path   
    LoadPath <Data, ? , R> path = decodeHelper.getLoadPath((Class <Data>) data.getClass());
    //注释2处，
    return runLoadPath(data, dataSource, path);
}
```

注释1处，返回的path 总共有3个


第一个path：包含 ByteBufferGifDecoder

DecodePath{ dataClass=class java.nio.DirectByteBuffer, decoders=[com.bumptech.glide.load.resource.gif.ByteBufferGifDecoder@f2c14b7], transcoder=com.bumptech.glide.load.resource.transcode.UnitTranscoder@9ec424}

第二个path包含：ByteBufferBitmapDecoder，VideoDecoder

DecodePath{ dataClass=class java.nio.DirectByteBuffer, decoders=[com.bumptech.glide.load.resource.bitmap.ByteBufferBitmapDecoder@b34ab8d, com.bumptech.glide.load.resource.bitmap.VideoDecoder@dc03142], transcoder=com.bumptech.glide.load.resource.transcode.BitmapDrawableTranscoder@4e34553}

第3个path包含：BitmapDrawableDecoder

DecodePath{ dataClass=class java.nio.DirectByteBuffer, decoders=[com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder@515fe90, com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder@14d7389], transcoder=com.bumptech.glide.load.resource.transcode.UnitTranscoder@9ec424}


注释2处，DecodeJob 的 runLoadPath 方法

```java
private <Data, ResourceType> Resource <R> runLoadPath(Data data, DataSource dataSource, LoadPath <Data, ResourceType, R> path) throws GlideException {
    Options options = getOptionsWithHardwareConfig(dataSource);
    DataRewinder <Data> rewinder = glideContext.getRegistry().getRewinder(data);
    try {
        // ResourceType in DecodeCallback below is required for compilation to work with gradle.

        //注释1处，LoadPath 的 load 方法
        return path.load(
            rewinder, options, width, height, new DecodeCallback <ResourceType> (dataSource));
    } finally {
        rewinder.cleanup();
    }
}
```



LoadPath 的 load 方法


```java
public Resource <Transcode> load(DataRewinder <Data> rewinder, @NonNull Options options, int width, int height,
    DecodePath.DecodeCallback <ResourceType> decodeCallback)
throws GlideException {
    List <Throwable> throwables = Preconditions.checkNotNull(listPool.acquire());
    try {
        //注释1处，调用 loadWithExceptionList 方法
        return loadWithExceptionList(rewinder, options, width, height, decodeCallback, throwables);
    } finally {
        listPool.release(throwables);
    }
}

```

LoadPath 的 loadWithExceptionList 方法。

```java
private Resource <Transcode> loadWithExceptionList(DataRewinder <Data> rewinder, @NonNull Options options,
    int width, int height, DecodePath.DecodeCallback <ResourceType> decodeCallback, List <Throwable> exceptions) throws GlideException {
    Resource <Transcode> result = null;
    //noinspection ForLoopReplaceableByForEach to improve perf
    //注释1处，总共有3个path
    for(int i = 0, size = decodePaths.size(); i < size; i++) {
        DecodePath <Data, ResourceType, Transcode> path = decodePaths.get(i);
        try {
            //注释1处，调用 DecodePath 的 load 方法
            result = path.decode(rewinder, width, height, options, decodeCallback);
        } catch(GlideException e) {
            exceptions.add(e);
        }
        if(result != null) {
            break;
        }
    }
    if(result == null) {
        throw new GlideException(failureMessage, new ArrayList <> (exceptions));
    }
    return result;
}

```
注释1处，总共有3个 DecodePath 。 

第一个 DecodePath 的 ByteBufferGifDecoder 无法解析我们这个文件，抛出异常：

`Failed DecodePath{DirectByteBuffer->GifDrawable->Drawable}`

第二个path

DecodePath 的 decode 方法

```java
public Resource<Transcode> decode(
      DataRewinder<DataType> rewinder,
      int width,
      int height,
      @NonNull Options options,
      DecodeCallback<ResourceType> callback)
      throws GlideException {
    //注释1处
    Resource<ResourceType> decoded = decodeResource(rewinder, width, height, options);
    //注释2处
    Resource<ResourceType> transformed = callback.onResourceDecoded(decoded);
    //注释3处，
    return transcoder.transcode(transformed, options);
}
```

```java
@NonNull
  private Resource<ResourceType> decodeResource(
      DataRewinder<DataType> rewinder, int width, int height, @NonNull Options options)
      throws GlideException {
    List<Throwable> exceptions = Preconditions.checkNotNull(listPool.acquire());
    try {
      return decodeResourceWithList(rewinder, width, height, options, exceptions);
    } finally {
      listPool.release(exceptions);
    }
}

```

DecodePath 的 decodeResourceWithList 方法


```java
@NonNull
private Resource <ResourceType> decodeResourceWithList(DataRewinder <DataType> rewinder, int width, int height, 
    @NonNull Options options, List <Throwable> exceptions)throws GlideException {
    Resource < ResourceType > result = null;
    //noinspection ForLoopReplaceableByForEach to improve perf
    for(int i = 0, size = decoders.size(); i < size; i++) {
        ResourceDecoder <DataType, ResourceType> decoder = decoders.get(i);
        try {
            DataType data = rewinder.rewindAndGet();
            if(decoder.handles(data, options)) {
                //注释1处，第二个path的ByteBufferBitmapDecoder是可以解析的
                data = rewinder.rewindAndGet();
                //注释2处，
                result = decoder.decode(data, width, height, options);
            }
            // Some decoders throw unexpectedly. If they do, we shouldn't fail the entire load path, but
            // instead log and continue. See #2406 for an example.
        } catch(IOException | RuntimeException | OutOfMemoryError e) {
            if(Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Failed to decode data for " + decoder, e);
            }
            exceptions.add(e);
        }
        if(result != null) {
            break;
        }
    }
    if(result == null) {
        throw new GlideException(failureMessage, new ArrayList <> (exceptions));
    }
    return result;
}
```

ByteBufferBitmapDecoder 的 decode 方法

```java
@Override
public Resource<Bitmap> decode(@NonNull ByteBuffer source, int width, int height, @NonNull Options options)
      throws IOException {
    InputStream is = ByteBufferUtil.toStream(source);
    //调用 Downsampler 的 decode 方法
    return downsampler.decode(is, width, height, options);
}
```

Downsampler 的 decode 方法

```java
public Resource <Bitmap> decode(InputStream is, int requestedWidth, int requestedHeight, Options options, DecodeCallbacks callbacks)
throws IOException {
    //注释1处，构建了一个 ImageReader，然后调用重载的 decode 方法
    return decode(new ImageReader.InputStreamImageReader(is, parsers, byteArrayPool), requestedWidth,
        requestedHeight, options, callbacks);
}
```

方法参数 
is: com.bumptech.glide.util.ByteBufferUtil$ByteBufferStream
requestedWidth: 1440
requestedHeight: 700
options: `Options{values={Option{key='com.bumptech.glide.load.resource.bitmap.Downsampler.DownsampleStrategy'}=com.bumptech.glide.load.resource.bitmap.DownsampleStrategy$FitCenter@35e6e9c}}`

注释1处，构建了一个 ImageReader，然后调用重载的 decode 方法。

```java
private Resource <Bitmap> decode(ImageReader imageReader, int requestedWidth, int requestedHeight, Options options,
    DecodeCallbacks callbacks)
throws IOException {
    byte[] bytesForOptions = byteArrayPool.get(ArrayPool.STANDARD_BUFFER_SIZE_BYTES, byte[].class);
    BitmapFactory.Options bitmapFactoryOptions = getDefaultOptions();
    bitmapFactoryOptions.inTempStorage = bytesForOptions;
    //默认解析格式 PREFER_ARGB_8888
    DecodeFormat decodeFormat = options.get(DECODE_FORMAT);
    PreferredColorSpace preferredColorSpace = options.get(PREFERRED_COLOR_SPACE);
    //默认 DownsampleStrategy 是 DownsampleStrategy.FitCenter
    DownsampleStrategy downsampleStrategy = options.get(DownsampleStrategy.OPTION);
    boolean fixBitmapToRequestedDimensions = options.get(FIX_BITMAP_SIZE_TO_REQUESTED_DIMENSIONS);
    boolean isHardwareConfigAllowed = options.get(ALLOW_HARDWARE_CONFIG) != null && options.get(
        ALLOW_HARDWARE_CONFIG);
    try {
        //注释1处，调用的 decodeFromWrappedStreams 方法，真正解析图片
        Bitmap result = decodeFromWrappedStreams(imageReader, bitmapFactoryOptions, downsampleStrategy,
            decodeFormat, preferredColorSpace, isHardwareConfigAllowed, requestedWidth, requestedHeight,
            fixBitmapToRequestedDimensions, callbacks);
        //注释2处，最终返回一个 BitmapResource 对象。      
        return BitmapResource.obtain(result, bitmapPool);
    } finally {
        releaseOptions(bitmapFactoryOptions);
        byteArrayPool.put(bytesForOptions);
    }
}
```


Downsampler 的 decodeFromWrappedStreams 方法，这里真正解析出图片。

```java
private Bitmap decodeFromWrappedStreams(
      ImageReader imageReader,
      BitmapFactory.Options options,
      DownsampleStrategy downsampleStrategy,
      DecodeFormat decodeFormat,
      PreferredColorSpace preferredColorSpace,
      boolean isHardwareConfigAllowed,
      int requestedWidth,
      int requestedHeight,
      boolean fixBitmapToRequestedDimensions,
      DecodeCallbacks callbacks)
      throws IOException {
    long startTime = LogTime.getLogTime();

    //先解析出图片的宽高，inJustDecodeBounds 为 true，在我们的例子中宽高都是480
    int[] sourceDimensions = getDimensions(imageReader, options, callbacks, bitmapPool);
    int sourceWidth = sourceDimensions[0];
    int sourceHeight = sourceDimensions[1];
    //图片类型 image/png
    String sourceMimeType = options.outMimeType;

    // If we failed to obtain the image dimensions, we may end up with an incorrectly sized Bitmap,
    // so we want to use a mutable Bitmap type. One way this can happen is if the image header is so
    // large (10mb+) that our attempt to use inJustDecodeBounds fails and we're forced to decode the
    // full size image.
    if (sourceWidth == -1 || sourceHeight == -1) {
      isHardwareConfigAllowed = false;
    }

    int orientation = imageReader.getImageOrientation();
    int degreesToRotate = TransformationUtils.getExifOrientationDegrees(orientation);
    boolean isExifOrientationRequired = TransformationUtils.isExifOrientationRequired(orientation);

    //目标宽度 1440 和 高度 700
    int targetWidth =
        requestedWidth == Target.SIZE_ORIGINAL
            ? (isRotationRequired(degreesToRotate) ? sourceHeight : sourceWidth)
            : requestedWidth;
    int targetHeight =
        requestedHeight == Target.SIZE_ORIGINAL
            ? (isRotationRequired(degreesToRotate) ? sourceWidth : sourceHeight)
            : requestedHeight;

    //图片类型 PNG
    ImageType imageType = imageReader.getImageType();

    //计算缩放
    calculateScaling(
        imageType,
        imageReader,
        callbacks,
        bitmapPool,
        downsampleStrategy,
        degreesToRotate,
        sourceWidth,
        sourceHeight,
        targetWidth,
        targetHeight,
        options);

    calculateConfig(
        imageReader,
        decodeFormat,
        isHardwareConfigAllowed,
        isExifOrientationRequired,
        options,
        targetWidth,
        targetHeight);

    boolean isKitKatOrGreater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    // Prior to KitKat, the inBitmap size must exactly match the size of the bitmap we're decoding.
    if ((options.inSampleSize == 1 || isKitKatOrGreater) && shouldUsePool(imageType)) {
      int expectedWidth;
      int expectedHeight;
      if (sourceWidth >= 0
          && sourceHeight >= 0
          && fixBitmapToRequestedDimensions
          && isKitKatOrGreater) {
        expectedWidth = targetWidth;
        expectedHeight = targetHeight;
      } else {
        float densityMultiplier =
            isScaling(options) ? (float) options.inTargetDensity / options.inDensity : 1f;
        int sampleSize = options.inSampleSize;
        int downsampledWidth = (int) Math.ceil(sourceWidth / (float) sampleSize);
        int downsampledHeight = (int) Math.ceil(sourceHeight / (float) sampleSize);
        expectedWidth = Math.round(downsampledWidth * densityMultiplier);
        expectedHeight = Math.round(downsampledHeight * densityMultiplier);

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
          Log.v(
              TAG,
              "Calculated target ["
                  + expectedWidth
                  + "x"
                  + expectedHeight
                  + "] for source"
                  + " ["
                  + sourceWidth
                  + "x"
                  + sourceHeight
                  + "]"
                  + ", sampleSize: "
                  + sampleSize
                  + ", targetDensity: "
                  + options.inTargetDensity
                  + ", density: "
                  + options.inDensity
                  + ", density multiplier: "
                  + densityMultiplier);
        }
      }
      // If this isn't an image, or BitmapFactory was unable to parse the size, width and height
      // will be -1 here.
      if (expectedWidth > 0 && expectedHeight > 0) {
        setInBitmap(options, bitmapPool, expectedWidth, expectedHeight);
      }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      boolean isP3Eligible =
          preferredColorSpace == PreferredColorSpace.DISPLAY_P3
              && options.outColorSpace != null
              && options.outColorSpace.isWideGamut();
      options.inPreferredColorSpace =
          ColorSpace.get(isP3Eligible ? ColorSpace.Named.DISPLAY_P3 : ColorSpace.Named.SRGB);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      options.inPreferredColorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
    }

    //这里最终解析出图片，在我们的例子中是700 * 700 的尺寸
    Bitmap downsampled = decodeStream(imageReader, options, callbacks, bitmapPool);
    callbacks.onDecodeComplete(bitmapPool, downsampled);

    if (Log.isLoggable(TAG, Log.VERBOSE)) {
      logDecode(
          sourceWidth,
          sourceHeight,
          sourceMimeType,
          options,
          downsampled,
          requestedWidth,
          requestedHeight,
          startTime);
    }

    Bitmap rotated = null;
    if (downsampled != null) {
      // If we scaled, the Bitmap density will be our inTargetDensity. Here we correct it back to
      // the expected density dpi.
      //给图片设置密度
      downsampled.setDensity(displayMetrics.densityDpi);

      rotated = TransformationUtils.rotateImageExif(bitmapPool, downsampled, orientation);
      if (!downsampled.equals(rotated)) {
        bitmapPool.put(downsampled);
      }
    }

    return rotated;
}
```

回到 DecodePath 的 decodeResourceWithList 方法注释2处，此时解析出来，result 是一个 BitmapResource 对象。

然后回到DecodePath 的 decode 方法注释2处。会回到 DecodeJob 的onResourceDecoded方法。

DecodeJob 的onResourceDecoded方法。

```java
@NonNull
@Override
public Resource<Z> onResourceDecoded(@NonNull Resource<Z> decoded) {
    return DecodeJob.this.onResourceDecoded(dataSource, decoded);
}
```

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
    // TODO: Make this the responsibility of the Transformation.
    if (!decoded.equals(transformed)) {
        //有变换的话，把decoded 回收
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
    //从缓存取出来的，并且没有被 transform，这里不能缓存了。
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

注释2处，执行 Transformation。BitmapTransformation 的 transform 方法。


```java
@NonNull
@Override
public final Resource<Bitmap> transform(
      @NonNull Context context, @NonNull Resource<Bitmap> resource, int outWidth, int outHeight) {
    if (!Util.isValidDimensions(outWidth, outHeight)) {
      throw new IllegalArgumentException(
          "Cannot apply transformation on width: "
              + outWidth
              + " or height: "
              + outHeight
              + " less than or equal to zero and not Target.SIZE_ORIGINAL");
    }
    BitmapPool bitmapPool = Glide.get(context).getBitmapPool();
    Bitmap toTransform = resource.get();
    //目标宽高是 1440 和 高度 700，图片宽高都是700
    int targetWidth = outWidth == Target.SIZE_ORIGINAL ? toTransform.getWidth() : outWidth;
    int targetHeight = outHeight == Target.SIZE_ORIGINAL ? toTransform.getHeight() : outHeight;
    Bitmap transformed = transform(bitmapPool, toTransform, targetWidth, targetHeight);

    final Resource<Bitmap> result;
    if (toTransform.equals(transformed)) {
      //注释1处，在我们的例子中，使用的是fitCenter，计算出来的toTransform 和 transformed 是同一个没有发生变化。
      result = resource;
    } else {
      result = BitmapResource.obtain(transformed, bitmapPool);
    }
    return result;
}
```
FitCenter 的 transform 方法

```java
@Override
protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
    return TransformationUtils.fitCenter(pool, toTransform, outWidth, outHeight);
}
```

TransformationUtils 的 fitCenter 方法

```java
public static Bitmap fitCenter(
      @NonNull BitmapPool pool, @NonNull Bitmap inBitmap, int width, int height) {
    if (inBitmap.getWidth() == width && inBitmap.getHeight() == height) {
      if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "requested target size matches input, returning input");
      }
      return inBitmap;
    }
    final float widthPercentage = width / (float) inBitmap.getWidth();
    final float heightPercentage = height / (float) inBitmap.getHeight();
    //fitcenter，取最小宽高
    final float minPercentage = Math.min(widthPercentage, heightPercentage);

    // Round here in case we've decoded exactly the image we want, but take the floor below to
    // avoid a line of garbage or blank pixels in images.
    //fitcenter，取最小宽高，计算出来宽高都是700
    int targetWidth = Math.round(minPercentage * inBitmap.getWidth());
    int targetHeight = Math.round(minPercentage * inBitmap.getHeight());

    if (inBitmap.getWidth() == targetWidth && inBitmap.getHeight() == targetHeight) {
      if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "adjusted target size matches input, returning input");
      }
      return inBitmap;
    }

    // Take the floor of the target width/height, not round. If the matrix
    // passed into drawBitmap rounds differently, we want to slightly
    // overdraw, not underdraw, to avoid artifacts from bitmap reuse.
    targetWidth = (int) (minPercentage * inBitmap.getWidth());
    targetHeight = (int) (minPercentage * inBitmap.getHeight());

    Bitmap.Config config = getNonNullConfig(inBitmap);
    Bitmap toReuse = pool.get(targetWidth, targetHeight, config);

    // We don't add or remove alpha, so keep the alpha setting of the Bitmap we were given.
    TransformationUtils.setAlpha(inBitmap, toReuse);

    if (Log.isLoggable(TAG, Log.VERBOSE)) {
      Log.v(TAG, "request: " + width + "x" + height);
      Log.v(TAG, "toFit:   " + inBitmap.getWidth() + "x" + inBitmap.getHeight());
      Log.v(TAG, "toReuse: " + toReuse.getWidth() + "x" + toReuse.getHeight());
      Log.v(TAG, "minPct:   " + minPercentage);
    }

    Matrix matrix = new Matrix();
    matrix.setScale(minPercentage, minPercentage);
    applyMatrix(inBitmap, toReuse, matrix);

    return toReuse;
}
```


回到 DecodePath 的 decode 方法 的注释3处

调用 BitmapDrawableTranscoder 的 transcode 方法 返回一个 LazyBitmapDrawableResource 对象。

```java
@Nullable
@Override
public Resource<BitmapDrawable> transcode(
      @NonNull Resource<Bitmap> toTranscode, @NonNull Options options) {
    return LazyBitmapDrawableResource.obtain(resources, toTranscode);
}
```

回到 LoadPath 的 loadWithExceptionList 方法的注释1处。我们使用第二个path的ByteBufferBitmapDecoder解析出了结果。

然后回到 DecodeJob 的 runLoadPath 方法的注释2处。

然后回到 DecodeJob 的 decodeFromFetcher 方法。

然后回到 DecodeJob 的 decodeFromData 方法。

然后回到 DecodeJob 的 decodeFromRetrievedData 方法。注释2处，notifyEncodeAndRelease。

















