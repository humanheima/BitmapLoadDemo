# BitmapLoadDemo
《Android 开发艺术探索》学习

### Glide 使用 Glide执行流程 
[郭霖- Android图片加载框架最全解析（二），从源码的角度理解Glide的执行流程](http://blog.csdn.net/guolin_blog/article/details/53939176)

`Glide.with(this).load(string).into(imageView);`

* with()方法：获取RequestManager对象，然后Glide会根据我们传入with()方法的参数来确定图片加载的生命周期。

如果传入的Context是Application的话，那么Glide的加载过程是和应用程序的生命周期是同步的，如果应用程序关闭的话，Glide的加载也会同时终止。如果传入的
Context是非Application参数的情况，最终的流程都是一样的，那就是会向当前的Activity当中添加一个隐藏的Fragment。那么这里为什么要添加一个隐藏的
Fragment呢？因为Glide需要知道加载的生命周期。很简单的一个道理，如果你在某个Activity上正在加载着一张图片，结果图片还没加载出来，Activity就被用户
关掉了，那么图片还应该继续加载吗？当然不应该。可是Glide并没有办法知道Activity的生命周期，于是Glide就使用了添加隐藏Fragment的这种小技巧，因为
Fragment的生命周期和Activity是同步的，如果Activity被销毁了，Fragment是可以监听到的，这样Glide就可以捕获这个事件并停止图片加载了。另外如果我们是
在非主线程当中使用的Glide，那么不管你是传入的Activity还是Fragment，都会被强制当成Application来处理。

* load()方法：返回一个DrawableTypeRequest对象。
 1. 实例化一个StreamStringLoader对象，然后返回一个DrawableTypeRequest对象。
 2. DrawableTypeRequest最主要的就是它提供了asBitmap()和asGif()这两个方法。分别是用于强制指定加载静态图片和动态图片。它们分别又创建了一个
    BitmapTypeRequest和GifTypeRequest，如果没有进行强制指定的话，那默认就是使用DrawableTypeRequest。
 3. 调用DrawableTypeRequest的load()方法。这个方法在DrawableTypeRequest的父类DrawableRequestBuilder中
 4. 最终，最后返回一个DrawableTypeRequest对象。
 
* into()方法：加载图像显示到ImageView上。
 1. into()方法在DrawableRequestBuilder的父类GenericRequestBuilder中
 ```java
public Target<TranscodeType> into(ImageView view) {
        Util.assertMainThread();
        if (view == null) {
            throw new IllegalArgumentException("You must pass in a non null View");
        }

        if (!isTransformationSet && view.getScaleType() != null) {
            switch (view.getScaleType()) {
                case CENTER_CROP:
                    applyCenterCrop();
                    break;
                case FIT_CENTER:
                case FIT_START:
                case FIT_END:
                    applyFitCenter();
                    break;
                //$CASES-OMITTED$
                default:
                    // Do nothing.
            }
        }

        return into(glide.buildImageViewTarget(view, transcodeClass));
```
先看最后一行，先是调用了glide.buildImageViewTarget()方法，这个方法会构建出一个Target对象，Target对象则是用来最终展示图片用的
```java
 <R> Target<R> buildImageViewTarget(ImageView imageView, Class<R> transcodedClass) {
        return imageViewTargetFactory.buildTarget(imageView, transcodedClass);
    }
```
这里其实又是调用了ImageViewTargetFactory的buildTarget()方法
```java
public class ImageViewTargetFactory {

    @SuppressWarnings("unchecked")
    public <Z> Target<Z> buildTarget(ImageView view, Class<Z> clazz) {
        if (GlideDrawable.class.isAssignableFrom(clazz)) {
            return (Target<Z>) new GlideDrawableImageViewTarget(view);
        } else if (Bitmap.class.equals(clazz)) {
            return (Target<Z>) new BitmapImageViewTarget(view);
        } else if (Drawable.class.isAssignableFrom(clazz)) {
            return (Target<Z>) new DrawableImageViewTarget(view);
        } else {
            throw new IllegalArgumentException("Unhandled class: " + clazz
                    + ", try .as*(Class).transcode(ResourceTranscoder)");
        }
    }
}
```
可以看到，在buildTarget()方法中会根据传入的class参数来构建不同的Target对象。暂时先记住结论：这个class参数其实基本上只有两种情况，如果你在使用
Glide加载图片的时候调用了asBitmap()方法，那么这里就会构建出BitmapImageViewTarget对象，否则的话构建的都是GlideDrawableImageViewTarget对象。

也就是说，通过glide.buildImageViewTarget()方法，我们构建出了一个GlideDrawableImageViewTarget对象。那现在回到刚才into()方法的最后一行，可以看到，
这里又将这个参数传入到了GenericRequestBuilder另一个接收Target对象的into()方法当中了。我们来看一下这个into()方法的源码
```java
public <Y extends Target<TranscodeType>> Y into(Y target) {
        Util.assertMainThread();
        if (target == null) {
            throw new IllegalArgumentException("You must pass in a non null Target");
        }
        if (!isModelSet) {
            throw new IllegalArgumentException("You must first set a model (try #load())");
        }

        Request previous = target.getRequest();

        if (previous != null) {
            previous.clear();
            requestTracker.removeRequest(previous);
            previous.recycle();
        }
        //构建一个Request对象
        Request request = buildRequest(target);
        target.setRequest(request);
        lifecycle.addListener(target);
        //执行Request
        requestTracker.runRequest(request);

        return target;
    }
```
首先调用`buildRequest(target);`，构建一个Request对象（GenericRequest类型），然后执行Request。执行Request最终会调用GenericRequest的begin()方法
```java
 @Override
    public void begin() {
        startTime = LogTime.getLogTime();
        //如果我们传入的图片的地址string为null的话
        if (model == null) {
            //这个方法最终会设置error占位图
            onException(null);
            return;
        }

        status = Status.WAITING_FOR_SIZE;
        if (Util.isValidDimensions(overrideWidth, overrideHeight)) {
            onSizeReady(overrideWidth, overrideHeight);
        } else {
            target.getSize(this);
        }

        if (!isComplete() && !isFailed() && canNotifyStatusChanged()) {
            //
            target.onLoadStarted(getPlaceholderDrawable());
        }
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logV("finished run method in " + LogTime.getElapsedMillis(startTime));
        }
    }
   
```
具体的图片加载又是从哪里开始的呢？是在begin()方法中的`onSizeReady(overrideWidth, overrideHeight);`和`target.getSize(this);`。这里要分两种情况，
一种是你使用了override() API为图片指定了一个固定的宽高，一种是没有指定。如果指定了的话，就会调用onSizeReady()方法。如果没指定的话，就会执行
target.getSize()方法。这个target.getSize()方法的内部会根据ImageView的layout_width和layout_height值做一系列的计算，来算出图片应该的宽高。
在计算完之后，它也会调用onSizeReady()方法。也就是说，不管是哪种情况，最终都会调用到onSizeReady()方法，在这里进行下一步操作。
```java
 @Override
    public void onSizeReady(int width, int height) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logV("Got onSizeReady in " + LogTime.getElapsedMillis(startTime));
        }
        if (status != Status.WAITING_FOR_SIZE) {
            return;
        }
        status = Status.RUNNING;

        width = Math.round(sizeMultiplier * width);
        height = Math.round(sizeMultiplier * height);
        // first
        ModelLoader<A, T> modelLoader = loadProvider.getModelLoader();
        //third
        final DataFetcher<T> dataFetcher = modelLoader.getResourceFetcher(model, width, height);

        if (dataFetcher == null) {
            onException(new Exception("Failed to load model: \'" + model + "\'"));
            return;
        }
        // second
        ResourceTranscoder<Z, R> transcoder = loadProvider.getTranscoder();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logV("finished setup for calling load in " + LogTime.getElapsedMillis(startTime));
        }
        loadedFromMemoryCache = true;
        //forth
        loadStatus = engine.load(signature, width, height, dataFetcher, loadProvider, transformation, transcoder,
                priority, isMemoryCacheable, diskCacheStrategy, this);
        loadedFromMemoryCache = resource != null;
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logV("finished onSizeReady in " + LogTime.getElapsedMillis(startTime));
        }
    }

```
* 注释为first的地方：loadProvider是一个ImageVideoGifDrawableLoadProvider对象，获取的modelLoader为一个ImageVideoModelLoader对象。
* 注释为second的地方：transcoder是一个GifBitmapWrapperDrawableTranscoder对象。
* 注释为forth的地方：dataFetcher是一个ImageVideoFetcher对象。这里将刚才获得的ImageVideoFetcher、GifBitmapWrapperDrawableTranscoder等等一系列的
值一起传入到了Engine的load()方法当中。
Engine的load()方法 部分逻辑
```
...
 EngineJob engineJob = engineJobFactory.build(key, isMemoryCacheable);
        DecodeJob<T, Z, R> decodeJob = new DecodeJob<T, Z, R>(key, width, height, fetcher, loadProvider, transformation,
                transcoder, diskCacheProvider, diskCacheStrategy, priority);
        EngineRunnable runnable = new EngineRunnable(engineJob, decodeJob, priority);
        jobs.put(key, engineJob);
        engineJob.addCallback(cb);
        engineJob.start(runnable);  
```
这里构建了一个EngineJob，它的主要作用就是用来开启线程的，为后面的异步加载图片做准备。接下来第46行创建了一个DecodeJob对象，从名字上来看，
它好像是用来对图片进行解码的，但实际上它的任务十分繁重，待会我们就知道了。继续往下看，第48行创建了一个EngineRunnable对象，并且在51行调用了
EngineJob的start()方法来运行EngineRunnable对象，这实际上就是让EngineRunnable的run()方法在子线程当中执行了。那么我们现在就可以去看看
EngineRunnable的run()方法里做了些什么，如下所示：
```
@Override
    public void run() {
        if (isCancelled) {
            return;
        }

        Exception exception = null;
        Resource<?> resource = null;
        try {
            resource = decode();
        } catch (Exception e) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Exception decoding", e);
            }
            exception = e;
        }

        if (isCancelled) {
            if (resource != null) {
                resource.recycle();
            }
            return;
        }

        if (resource == null) {
            onLoadFailed(exception);
        } else {
            onLoadComplete(resource);
        }
    }

```
看一下 resource = decode();方法
```java
private Resource<?> decode() throws Exception {
        if (isDecodingFromCache()) {
            return decodeFromCache();
        } else {
            return decodeFromSource();
        }
    }
```
decode()方法中又分了两种情况，从缓存当中去decode图片的话就会执行decodeFromCache()，否则的话就执行decodeFromSource()。本篇文章中我们不讨论缓存
的情况，那么就直接来看decodeFromSource()方法的代码吧，如下所示：
```java
private Resource<?> decodeFromSource() throws Exception {
        return decodeJob.decodeFromSource();
    }
```
这里又调用了DecodeJob的decodeFromSource()方法。刚才已经说了，DecodeJob的任务十分繁重，我们继续跟进看一看吧,decodeFromSource()方法，其实它的工作
分为两部，第一步是调用decodeSource()方法来获得一个Resource对象，第二步是调用transformEncodeAndTranscode()方法来处理这个Resource对象。
```
private Resource<T> decodeSource() throws Exception {
        Resource<T> decoded = null;
        try {
            long startTime = LogTime.getLogTime();
            final A data = fetcher.loadData(priority);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("Fetched data", startTime);
            }
            if (isCancelled) {
                return null;
            }
            decoded = decodeFromSourceData(data);
        } finally {
            fetcher.cleanup();
        }
        return decoded;
    }
```
decodeSource()方法中的逻辑也并不复杂，首先调用了fetcher.loadData()方法。那么这个fetcher是什么呢？其实就是刚才在onSizeReady()方法中得到的
ImageVideoFetcher对象，这里调用它的loadData()方法
```java
        @Override
        public ImageVideoWrapper loadData(Priority priority) throws Exception {
            InputStream is = null;
            if (streamFetcher != null) {
                try {
                    is = streamFetcher.loadData(priority);
                } catch (Exception e) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "Exception fetching input stream, trying ParcelFileDescriptor", e);
                    }
                    if (fileDescriptorFetcher == null) {
                        throw e;
                    }
                }
            }
            ParcelFileDescriptor fileDescriptor = null;
            if (fileDescriptorFetcher != null) {
                try {
                    fileDescriptor = fileDescriptorFetcher.loadData(priority);
                } catch (Exception e) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "Exception fetching ParcelFileDescriptor", e);
                    }
                    if (is == null) {
                        throw e;
                    }
                }
            }
            return new ImageVideoWrapper(is, fileDescriptor);
        }
```
可以看到，在ImageVideoFetcher的loadData()方法的第6行，这里又去调用了streamFetcher.loadData()方法，那么这个streamFetcher是什么呢？自然就是刚才
在组装ImageVideoFetcher对象时传进来的HttpUrlFetcher了。因此这里又会去调用HttpUrlFetcher的loadData()方法，那么我们继续跟进去瞧一瞧。里面才是发送
网络请求的地方。
```java
@Override
    public InputStream loadData(Priority priority) throws Exception {
        return loadDataWithRedirects(glideUrl.toURL(), 0 /*redirects*/, null /*lastUrl*/, glideUrl.getHeaders());
    }

    private InputStream loadDataWithRedirects(URL url, int redirects, URL lastUrl, Map<String, String> headers)
            throws IOException {
        if (redirects >= MAXIMUM_REDIRECTS) {
            throw new IOException("Too many (> " + MAXIMUM_REDIRECTS + ") redirects!");
        } else {
            // Comparing the URLs using .equals performs additional network I/O and is generally broken.
            // See http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html.
            try {
                if (lastUrl != null && url.toURI().equals(lastUrl.toURI())) {
                    throw new IOException("In re-direct loop");
                }
            } catch (URISyntaxException e) {
                // Do nothing, this is best effort.
            }
        }
        urlConnection = connectionFactory.build(url);
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
          urlConnection.addRequestProperty(headerEntry.getKey(), headerEntry.getValue());
        }
        urlConnection.setConnectTimeout(2500);
        urlConnection.setReadTimeout(2500);
        urlConnection.setUseCaches(false);
        urlConnection.setDoInput(true);

        // Connect explicitly to avoid errors in decoders if connection fails.
        urlConnection.connect();
        if (isCancelled) {
            return null;
        }
        final int statusCode = urlConnection.getResponseCode();
        if (statusCode / 100 == 2) {
            return getStreamForSuccessfulRequest(urlConnection);
        } else if (statusCode / 100 == 3) {
            String redirectUrlString = urlConnection.getHeaderField("Location");
            if (TextUtils.isEmpty(redirectUrlString)) {
                throw new IOException("Received empty or null redirect url");
            }
            URL redirectUrl = new URL(url, redirectUrlString);
            return loadDataWithRedirects(redirectUrl, redirects + 1, url, headers);
        } else {
            if (statusCode == -1) {
                throw new IOException("Unable to retrieve response code from HttpUrlConnection.");
            }
            throw new IOException("Request failed " + statusCode + ": " + urlConnection.getResponseMessage());
        }
    }

    private InputStream getStreamForSuccessfulRequest(HttpURLConnection urlConnection)
            throws IOException {
        if (TextUtils.isEmpty(urlConnection.getContentEncoding())) {
            int contentLength = urlConnection.getContentLength();
            stream = ContentLengthInputStream.obtain(urlConnection.getInputStream(), contentLength);
        } else {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Got non empty content encoding: " + urlConnection.getContentEncoding());
            }
            stream = urlConnection.getInputStream();
        }
        return stream;
    }

```
HttpUrlFetcher.loadData()方法只是返回了一个InputStream，服务器返回的数据连读都还没开始读呢。所以我们还是要静下心来继续分析，回到刚才
ImageVideoFetcher的loadData()方法中，在这个方法的最后一行，创建了一个ImageVideoWrapper对象，并把刚才得到的InputStream作为参数传了进去。

回到DecodeJob的decodeSource()方法当中，在得到了这个ImageVideoWrapper对象之后，紧接着又将这个对象传入到了decodeFromSourceData()当中，来去解码
这个对象。decodeFromSourceData()方法的代码如下所示：
```
private Resource<T> decodeFromSourceData(A data) throws IOException {
        final Resource<T> decoded;
        if (diskCacheStrategy.cacheSource()) {
            decoded = cacheAndDecodeSourceData(data);
        } else {
            long startTime = LogTime.getLogTime();
            //
            decoded = loadProvider.getSourceDecoder().decode(data, width, height);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("Decoded from source", startTime);
            }
        }
        return decoded;
    }
```
这里调用了loadProvider.getSourceDecoder().decode()方法来进行解码。loadProvider就是刚才在onSizeReady()方法中得到的FixedLoadProvider，而
getSourceDecoder()得到的则是一个GifBitmapWrapperResourceDecoder对象，也就是要调用这个对象的decode()方法来对图片进行解码。那么我们来看下
GifBitmapWrapperResourceDecoder的代码
```java
  @Override
    public Resource<GifBitmapWrapper> decode(ImageVideoWrapper source, int width, int height) throws IOException {
        ByteArrayPool pool = ByteArrayPool.get();
        byte[] tempBytes = pool.getBytes();

        GifBitmapWrapper wrapper = null;
        try {
            wrapper = decode(source, width, height, tempBytes);
        } finally {
            pool.releaseBytes(tempBytes);
        }
        return wrapper != null ? new GifBitmapWrapperResource(wrapper) : null;
    }

    private GifBitmapWrapper decode(ImageVideoWrapper source, int width, int height, byte[] bytes) throws IOException {
        final GifBitmapWrapper result;
        if (source.getStream() != null) {
            result = decodeStream(source, width, height, bytes);
        } else {
            result = decodeBitmapWrapper(source, width, height);
        }
        return result;
    }

    private GifBitmapWrapper decodeStream(ImageVideoWrapper source, int width, int height, byte[] bytes)
            throws IOException {
        InputStream bis = streamFactory.build(source.getStream(), bytes);
        bis.mark(MARK_LIMIT_BYTES);
        ImageHeaderParser.ImageType type = parser.parse(bis);
        bis.reset();

        GifBitmapWrapper result = null;
        if (type == ImageHeaderParser.ImageType.GIF) {
            result = decodeGifWrapper(bis, width, height);
        }
        // Decoding the gif may fail even if the type matches.
        if (result == null) {
            // We can only reset the buffered InputStream, so to start from the beginning of the stream, we need to
            // pass in a new source containing the buffered stream rather than the original stream.
            ImageVideoWrapper forBitmapDecoder = new ImageVideoWrapper(bis, source.getFileDescriptor());
            result = decodeBitmapWrapper(forBitmapDecoder, width, height);
        }
        return result;
    }

    private GifBitmapWrapper decodeBitmapWrapper(ImageVideoWrapper toDecode, int width, int height) throws IOException {
        GifBitmapWrapper result = null;

        Resource<Bitmap> bitmapResource = bitmapDecoder.decode(toDecode, width, height);
        if (bitmapResource != null) {
            result = new GifBitmapWrapper(bitmapResource, null);
        }

        return result;
    }

```
首先，在decode()方法中，又去调用了另外一个decode()方法的重载。然后在第23行调用了decodeStream()方法，准备从服务器返回的流当中读取数据。
decodeStream()方法中会先从流中读取2个字节的数据，来判断这张图是GIF图还是普通的静图，如果是GIF图就调用decodeGifWrapper()方法来进行解码，
如果是普通的静图就用调用decodeBitmapWrapper()方法来进行解码。这里我们只分析普通静图的实现流程。

然后我们来看一下decodeBitmapWrapper()方法，这里在第52行调用了bitmapDecoder.decode()方法。这个bitmapDecoder是一个`ImageVideoBitmapDecoder`对象，
那么我们来看一下它的代码，如下所示

```
public class ImageVideoBitmapDecoder implements ResourceDecoder<ImageVideoWrapper, Bitmap> {
    private static final String TAG = "ImageVideoDecoder";
    private final ResourceDecoder<InputStream, Bitmap> streamDecoder;
    private final ResourceDecoder<ParcelFileDescriptor, Bitmap> fileDescriptorDecoder;

    public ImageVideoBitmapDecoder(ResourceDecoder<InputStream, Bitmap> streamDecoder,
            ResourceDecoder<ParcelFileDescriptor, Bitmap> fileDescriptorDecoder) {
        this.streamDecoder = streamDecoder;
        this.fileDescriptorDecoder = fileDescriptorDecoder;
    }

    @SuppressWarnings("resource")
    // @see ResourceDecoder.decode
    @Override
    public Resource<Bitmap> decode(ImageVideoWrapper source, int width, int height) throws IOException {
        Resource<Bitmap> result = null;
        InputStream is = source.getStream();
        if (is != null) {
            try {
                result = streamDecoder.decode(is, width, height);
            } catch (IOException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Failed to load image from stream, trying FileDescriptor", e);
                }
            }
        }

        if (result == null) {
            ParcelFileDescriptor fileDescriptor = source.getFileDescriptor();
            if (fileDescriptor != null) {
                result = fileDescriptorDecoder.decode(fileDescriptor, width, height);
            }
        }
        return result;
    }
}
```
在第14行先调用了source.getStream()来获取到服务器返回的InputStream，然后在第17行调用streamDecoder.decode()方法进行解码。streamDecode是一个
`StreamBitmapDecoder`对象，那么我们再来看这个类的源码，如下所示：
```java
public class StreamBitmapDecoder implements ResourceDecoder<InputStream, Bitmap> {
    private static final String ID = "StreamBitmapDecoder.com.bumptech.glide.load.resource.bitmap";
    private final Downsampler downsampler;
    private BitmapPool bitmapPool;
    private DecodeFormat decodeFormat;
    private String id;

    public StreamBitmapDecoder(Context context) {
        this(Glide.get(context).getBitmapPool());
    }

    public StreamBitmapDecoder(BitmapPool bitmapPool) {
        this(bitmapPool, DecodeFormat.DEFAULT);
    }

    public StreamBitmapDecoder(Context context, DecodeFormat decodeFormat) {
        this(Glide.get(context).getBitmapPool(), decodeFormat);
    }

    public StreamBitmapDecoder(BitmapPool bitmapPool, DecodeFormat decodeFormat) {
        this(Downsampler.AT_LEAST, bitmapPool, decodeFormat);
    }

    public StreamBitmapDecoder(Downsampler downsampler, BitmapPool bitmapPool, DecodeFormat decodeFormat) {
        this.downsampler = downsampler;
        this.bitmapPool = bitmapPool;
        this.decodeFormat = decodeFormat;
    }

    @Override
    public Resource<Bitmap> decode(InputStream source, int width, int height) {
        Bitmap bitmap = downsampler.decode(source, bitmapPool, width, height, decodeFormat);
        return BitmapResource.obtain(bitmap, bitmapPool);
    }

}

```
可以看到，它的decode()方法又去调用了Downsampler的decode()方法。接下来又到了激动人心的时刻了，`Downsampler`的代码就不看了，总之Downsampler调用
decode()方法会返回一个Bitmap对象，，中间会设计对图片的压缩，旋转，圆角等逻辑处理。那么图片在这里其实也就已经被加载出来了，剩下的工作就是如果让
这个Bitmap显示到界面上，我们继续往下分析。
回到刚才的StreamBitmapDecoder当中，你会发现，它的decode()方法返回的是一个Resource<Bitmap>对象。而我们从Downsampler中得到的是一个Bitmap对象，
因此这里在第18行又调用了`BitmapResource.obtain()`方法，将Bitmap对象包装成了Resource<Bitmap>对象。代码如下所示：
```java
 public static BitmapResource obtain(Bitmap bitmap, BitmapPool bitmapPool) {
        if (bitmap == null) {
            return null;
        } else {
            return new BitmapResource(bitmap, bitmapPool);
        }
    }

    public BitmapResource(Bitmap bitmap, BitmapPool bitmapPool) {
        if (bitmap == null) {
            throw new NullPointerException("Bitmap must not be null");
        }
        if (bitmapPool == null) {
            throw new NullPointerException("BitmapPool must not be null");
        }
        this.bitmap = bitmap;
        this.bitmapPool = bitmapPool;
    }

    @Override
    public Bitmap get() {
        return bitmap;
    }
```
BitmapResource的源码也非常简单，经过这样一层包装之后，如果我还需要获取Bitmap，只需要调用Resource<Bitmap>的get()方法就可以了。

然后我们需要一层层继续向上返回，StreamBitmapDecoder会将值返回到ImageVideoBitmapDecoder当中，而ImageVideoBitmapDecoder又会将值返回到
GifBitmapWrapperResourceDecoder的decodeBitmapWrapper()方法当中。由于代码隔得有点太远了，我重新把decodeBitmapWrapper()方法的代码贴一下
```java
private GifBitmapWrapper decodeBitmapWrapper(ImageVideoWrapper toDecode, int width, int height) throws IOException {
        GifBitmapWrapper result = null;

        Resource<Bitmap> bitmapResource = bitmapDecoder.decode(toDecode, width, height);
        if (bitmapResource != null) {
            result = new GifBitmapWrapper(bitmapResource, null);
        }

        return result;
    }
```
可以看到，decodeBitmapWrapper()方法返回的是一个GifBitmapWrapper对象。因此，这里在第5行，又将Resource<Bitmap>封装到了一个GifBitmapWrapper对象当中
。这个GifBitmapWrapper顾名思义，就是既能封装GIF，又能封装Bitmap，从而保证了不管是什么类型的图片Glide都能从容应对。我们顺便来看下
GifBitmapWrapper的源码吧，如下所示：
```java
public class GifBitmapWrapper {
    private final Resource<GifDrawable> gifResource;
    private final Resource<Bitmap> bitmapResource;

    public GifBitmapWrapper(Resource<Bitmap> bitmapResource, Resource<GifDrawable> gifResource) {
        if (bitmapResource != null && gifResource != null) {
            throw new IllegalArgumentException("Can only contain either a bitmap resource or a gif resource, not both");
        }
        if (bitmapResource == null && gifResource == null) {
            throw new IllegalArgumentException("Must contain either a bitmap resource or a gif resource");
        }
        this.bitmapResource = bitmapResource;
        this.gifResource = gifResource;
    }

    /**
     * Returns the size of the wrapped resource.
     */
    public int getSize() {
        if (bitmapResource != null) {
            return bitmapResource.getSize();
        } else {
            return gifResource.getSize();
        }
    }

    /**
     * Returns the wrapped {@link android.graphics.Bitmap} resource if it exists, or null.
     */
    public Resource<Bitmap> getBitmapResource() {
        return bitmapResource;
    }

    /**
     * Returns the wrapped {@link com.bumptech.glide.load.resource.gif.GifDrawable} resource if it exists, or null.
     */
    public Resource<GifDrawable> getGifResource() {
        return gifResource;
    }
}
```
然后这个GifBitmapWrapper对象会一直向上返回，返回到GifBitmapWrapperResourceDecoder最外层的decode()方法的时候，会对它再做一次封装，如下所示：
```java
 @Override
    public Resource<GifBitmapWrapper> decode(ImageVideoWrapper source, int width, int height) throws IOException {
        ByteArrayPool pool = ByteArrayPool.get();
        byte[] tempBytes = pool.getBytes();

        GifBitmapWrapper wrapper = null;
        try {
            wrapper = decode(source, width, height, tempBytes);
        } finally {
            pool.releaseBytes(tempBytes);
        }
        return wrapper != null ? new GifBitmapWrapperResource(wrapper) : null;
    }
```
可以看到，这里在第11行，又将GifBitmapWrapper封装到了一个GifBitmapWrapperResource对象当中，最终返回的是一个Resource<GifBitmapWrapper>对象。
这个GifBitmapWrapperResource和刚才的BitmapResource是相似的，它们都实现的Resource接口，都可以通过get()方法来获取封装起来的具体内容。
GifBitmapWrapperResource的源码如下所示
```java
public class GifBitmapWrapperResource implements Resource<GifBitmapWrapper> {
    private final GifBitmapWrapper data;

    public GifBitmapWrapperResource(GifBitmapWrapper data) {
        if (data == null) {
            throw new NullPointerException("Data must not be null");
        }
        this.data = data;
    }

    @Override
    public GifBitmapWrapper get() {
        return data;
    }

    @Override
    public int getSize() {
        return data.getSize();
    }

    @Override
    public void recycle() {
        Resource<Bitmap> bitmapResource = data.getBitmapResource();
        if (bitmapResource != null) {
            bitmapResource.recycle();
        }
        Resource<GifDrawable> gifDataResource = data.getGifResource();
        if (gifDataResource != null) {
            gifDataResource.recycle();
        }
    }
}

```
经过这一层的封装之后，我们从网络上得到的图片就能够以Resource接口的形式返回，并且还能同时处理Bitmap图片和GIF图片这两种情况。那么现在我们可以
回到DecodeJob当中了，它的decodeFromSourceData()方法返回的是一个Resource<T>对象，其实也就是Resource<GifBitmapWrapper>对象了。然后继续向上返回，
最终返回到decodeFromSource()方法当中，如下所示
```java
 public Resource<Z> decodeFromSource() throws Exception {
        Resource<T> decoded = decodeSource();
        return transformEncodeAndTranscode(decoded);
    }
```
刚才我们就是从这里跟进到decodeSource()方法当中，然后执行了一大堆一大堆的逻辑，最终得到了这个Resource<T>对象。然而你会发现，decodeFromSource()
方法最终返回的却是一个Resource<Z>对象，那么这到底是怎么回事呢？我们就需要跟进到transformEncodeAndTranscode()方法来瞧一瞧了，代码如下所示
```java
private Resource<Z> transformEncodeAndTranscode(Resource<T> decoded) {
        long startTime = LogTime.getLogTime();
        Resource<T> transformed = transform(decoded);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Transformed resource from source", startTime);
        }

        writeTransformedToCache(transformed);

        startTime = LogTime.getLogTime();
        Resource<Z> result = transcode(transformed);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Transcoded transformed from source", startTime);
        }
        return result;
    }
    
    private Resource<Z> transcode(Resource<T> transformed) {
            if (transformed == null) {
                return null;
            }
            return transcoder.transcode(transformed);
        }

```
这个方法需要注意的是这里调用了一个transcode()方法，就把Resource<T>对象转换成Resource<Z>对象了。这里的transcoder其实就是这个
GifBitmapWrapperDrawableTranscoder对象.那么我们来看一下它的源码
```java
public class GifBitmapWrapperDrawableTranscoder implements ResourceTranscoder<GifBitmapWrapper, GlideDrawable> {
    private final ResourceTranscoder<Bitmap, GlideBitmapDrawable> bitmapDrawableResourceTranscoder;

    public GifBitmapWrapperDrawableTranscoder(
            ResourceTranscoder<Bitmap, GlideBitmapDrawable> bitmapDrawableResourceTranscoder) {
        this.bitmapDrawableResourceTranscoder = bitmapDrawableResourceTranscoder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Resource<GlideDrawable> transcode(Resource<GifBitmapWrapper> toTranscode) {
        GifBitmapWrapper gifBitmap = toTranscode.get();
        Resource<Bitmap> bitmapResource = gifBitmap.getBitmapResource();

        final Resource<? extends GlideDrawable> result;
        if (bitmapResource != null) {
            //first
            result = bitmapDrawableResourceTranscoder.transcode(bitmapResource);
        } else {
            result = gifBitmap.getGifResource();
        }
        // This is unchecked but always safe, anything that extends a Drawable can be safely cast to a Drawable.
        return (Resource<GlideDrawable>) result;
    }

    @Override
    public String getId() {
        return "GifBitmapWrapperDrawableTranscoder.com.bumptech.glide.load.resource.transcode";
    }
}
```
这里我来简单解释一下，GifBitmapWrapperDrawableTranscoder的核心作用就是用来转码的。因为GifBitmapWrapper是无法直接显示到ImageView上面的，
只有Bitmap或者Drawable才能显示到ImageView上。因此，这里的transcode()方法先从Resource<GifBitmapWrapper>中取出GifBitmapWrapper对象，然后再从
GifBitmapWrapper中取出Resource<Bitmap>对象。接下来做了一个判断，如果Resource<Bitmap>为空，那么说明此时加载的是GIF图，直接调用getGifResource()方
法将图片取出即可，因为Glide用于加载GIF图片是使用的GifDrawable这个类，它本身就是一个Drawable对象了。而如果Resource<Bitmap>不为空，那么就需要再做
一次转码，将Bitmap转换成Drawable对象才行，因为要保证静图和动图的类型一致性，不然逻辑上是不好处理的。

注释为`first`的地方，这里又进行了一次转码，是调用的GlideBitmapDrawableTranscoder对象的transcode()方法，代码如下所示：
```java
public class GlideBitmapDrawableTranscoder implements ResourceTranscoder<Bitmap, GlideBitmapDrawable> {
    private final Resources resources;
    private final BitmapPool bitmapPool;

    public GlideBitmapDrawableTranscoder(Context context) {
        this(context.getResources(), Glide.get(context).getBitmapPool());
    }

    public GlideBitmapDrawableTranscoder(Resources resources, BitmapPool bitmapPool) {
        this.resources = resources;
        this.bitmapPool = bitmapPool;
    }

    @Override
    public Resource<GlideBitmapDrawable> transcode(Resource<Bitmap> toTranscode) {
        GlideBitmapDrawable drawable = new GlideBitmapDrawable(resources, toTranscode.get());
        return new GlideBitmapDrawableResource(drawable, bitmapPool);
    }

    @Override
    public String getId() {
        return "GlideBitmapDrawableTranscoder.com.bumptech.glide.load.resource.transcode";
    }
}
```
可以看到，这里new出了一个GlideBitmapDrawable对象，并把Bitmap封装到里面。然后对GlideBitmapDrawable再进行一次封装，返回一个
Resource<GlideBitmapDrawable>对象。

那么我们继续回到DecodeJob当中，它的decodeFromSource()方法得到了Resource<Z>对象，当然也就是Resource<GlideDrawable>对象。然后继续向上返回会回到
EngineRunnable的decodeFromSource()方法，再回到decode()方法，再回到run()方法当中。那么我们重新再贴一下EngineRunnable run()方法的源码：
```java
@Override
    public void run() {
        if (isCancelled) {
            return;
        }

        Exception exception = null;
        Resource<?> resource = null;
        try {
            
            resource = decode();
        } catch (Exception e) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Exception decoding", e);
            }
            exception = e;
        }

        if (isCancelled) {
            if (resource != null) {
                resource.recycle();
            }
            return;
        }

        if (resource == null) {
            onLoadFailed(exception);
        } else {
            onLoadComplete(resource);
        }
    }

```
也就是说，经过第9行decode()方法的执行，我们最终得到了这个Resource<GlideDrawable>对象，那么接下来就是如何将它显示出来了。可以看到，这里在第25行
调用了onLoadComplete()方法，表示图片加载已经完成了，代码如下所示：
```java
private void onLoadComplete(Resource resource) {
        manager.onResourceReady(resource);
    }
```
这个manager就是EngineJob对象，因此这里实际上调用的是EngineJob的onResourceReady()方法，代码如下所示：
```java
private static final Handler MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper(), new MainThreadCallback());
private final List<ResourceCallback> cbs = new ArrayList<ResourceCallback>();

public void addCallback(ResourceCallback cb) {
        Util.assertMainThread();
        if (hasResource) {
            cb.onResourceReady(engineResource);
        } else if (hasException) {
            cb.onException(exception);
        } else {
            cbs.add(cb);
        }
    }
    
@Override
    public void onResourceReady(final Resource<?> resource) {
        this.resource = resource;
        MAIN_THREAD_HANDLER.obtainMessage(MSG_COMPLETE, this).sendToTarget();
    }
    
     private void handleResultOnMainThread() {
            if (isCancelled) {
                resource.recycle();
                return;
            } else if (cbs.isEmpty()) {
                throw new IllegalStateException("Received a resource without any callbacks to notify");
            }
            engineResource = engineResourceFactory.build(resource, isCacheable);
            hasResource = true;
    
            // Hold on to resource for duration of request so we don't recycle it in the middle of notifying if it
            // synchronously released by one of the callbacks.
            engineResource.acquire();
            listener.onEngineJobComplete(key, engineResource);
    
            for (ResourceCallback cb : cbs) {
                if (!isInIgnoredCallbacks(cb)) {
                    engineResource.acquire();
                    //回调cb的onResourceReady方法
                    cb.onResourceReady(engineResource);
                }
            }
            // Our request is complete, so we can release the resource.
            engineResource.release();
        }
        
        private static class MainThreadCallback implements Handler.Callback {
        
                @Override
                public boolean handleMessage(Message message) {
                    if (MSG_COMPLETE == message.what || MSG_EXCEPTION == message.what) {
                        EngineJob job = (EngineJob) message.obj;
                        if (MSG_COMPLETE == message.what) {
                            job.handleResultOnMainThread();
                        } else {
                            job.handleExceptionOnMainThread();
                        }
                        return true;
                    }
        
                    return false;
                }
            }
```
可以看到，这里在onResourceReady()方法使用Handler发出了一条MSG_COMPLETE消息，那么在MainThreadCallback的handleMessage()方法中就会收到这条消息。
从这里开始，所有的逻辑又回到主线程当中进行了，因为很快就需要更新UI了。然后在第72行调用了handleResultOnMainThread()方法，这个方法中又通过一个
循环，调用了所有ResourceCallback的onResourceReady()方法。那么这个ResourceCallback是什么呢？答案在addCallback()方法当中，它会向cbs集合中去添加
ResourceCallback。那么这个addCallback()方法又是哪里调用的呢？其实调用的地方我们早就已经看过了，只不过之前没有注意，现在重新来看一下Engine的
load()方法，如下所示：
```java
  public <T, Z, R> LoadStatus load(Key signature, int width, int height, DataFetcher<T> fetcher,
            DataLoadProvider<T, Z> loadProvider, Transformation<Z> transformation, ResourceTranscoder<Z, R> transcoder,
            Priority priority, boolean isMemoryCacheable, DiskCacheStrategy diskCacheStrategy, ResourceCallback cb) {
       ...
       
        EngineJob engineJob = engineJobFactory.build(key, isMemoryCacheable);
        DecodeJob<T, Z, R> decodeJob = new DecodeJob<T, Z, R>(key, width, height, fetcher, loadProvider, transformation,
                transcoder, diskCacheProvider, diskCacheStrategy, priority);
        EngineRunnable runnable = new EngineRunnable(engineJob, decodeJob, priority);
        jobs.put(key, engineJob);
        //添加回调
        engineJob.addCallback(cb);
        engineJob.start(runnable);

        return new LoadStatus(cb, engineJob);
    }
```
Engine.load()方法的ResourceCallback参数又是谁传过来的呢？这就需要回到GenericRequest的onSizeReady()方法当中了，我们看到ResourceCallback是load()
方法的最后一个参数，那么在onSizeReady()方法中调用load()方法时传入的最后一个参数是什么？代码如下所示：
```java
public final class GenericRequest<A, T, Z, R> implements Request, SizeReadyCallback,
        ResourceCallback {
engine.load(signature, width, height, dataFetcher, loadProvider, transformation, transcoder,
                priority, isMemoryCacheable, diskCacheStrategy, this);
}
```
GenericRequest本身就实现了ResourceCallback的接口，因此EngineJob的回调最终其实就是回调到了GenericRequest的onResourceReady()方法当中了，代码如
下所示：
```java
 public void onResourceReady(Resource<?> resource) {
        if (resource == null) {
            onException(new Exception("Expected to receive a Resource<R> with an object of " + transcodeClass
                    + " inside, but instead got null."));
            return;
        }
        //获取到了封装的图片对象，也就是GlideBitmapDrawable对象，或者是GifDrawable对象
        Object received = resource.get();
        if (received == null || !transcodeClass.isAssignableFrom(received.getClass())) {
            releaseResource(resource);
            onException(new Exception("Expected to receive an object of " + transcodeClass
                    + " but instead got " + (received != null ? received.getClass() : "") + "{" + received + "}"
                    + " inside Resource{" + resource + "}."
                    + (received != null ? "" : " "
                        + "To indicate failure return a null Resource object, "
                        + "rather than a Resource object containing null data.")
            ));
            return;
        }

        if (!canSetResource()) {
            releaseResource(resource);
            // We can't set the status to complete before asking canSetResource().
            status = Status.COMPLETE;
            return;
        }

        onResourceReady(resource, (R) received);
    }

private void onResourceReady(Resource<?> resource, R result) {
        // We must call isFirstReadyResource before setting status.
        boolean isFirstResource = isFirstReadyResource();
        status = Status.COMPLETE;
        this.resource = resource;

        if (requestListener == null || !requestListener.onResourceReady(result, model, target, loadedFromMemoryCache,
                isFirstResource)) {
            GlideAnimation<R> animation = animationFactory.build(loadedFromMemoryCache, isFirstResource);
            //这是重点
            target.onResourceReady(result, animation);
        }

        notifyLoadSuccess();

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logV("Resource ready in " + LogTime.getElapsedMillis(startTime) + " size: "
                    + (resource.getSize() * TO_MEGABYTE) + " fromCache: " + loadedFromMemoryCache);
        }
    }
```
这里有两个onResourceReady()方法，首先在第一个onResourceReady()方法当中，调用resource.get()方法获取到了封装的图片对象，也就是
GlideBitmapDrawable对象，或者是GifDrawable对象。然后将这个值传入到了第二个onResourceReady()方法当中，并在第36行调用了target.onResourceReady()
方法。那么这个target又是什么呢？这个又需要向上翻很久了，在第三步into()方法的一开始，我们就分析了在into()方法的最后一行，调用了
glide.buildImageViewTarget()方法来构建出一个Target，而这个Target就是一个GlideDrawableImageViewTarget对象。

```java
  @Override
    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
        if (!resource.isAnimated()) {
            //TODO: Try to generalize this to other sizes/shapes.
            // This is a dirty hack that tries to make loading square thumbnails and then square full images less costly
            // by forcing both the smaller thumb and the larger version to have exactly the same intrinsic dimensions.
            // If a drawable is replaced in an ImageView by another drawable with different intrinsic dimensions,
            // the ImageView requests a layout. Scrolling rapidly while replacing thumbs with larger images triggers
            // lots of these calls and causes significant amounts of jank.
            float viewRatio = view.getWidth() / (float) view.getHeight();
            float drawableRatio = resource.getIntrinsicWidth() / (float) resource.getIntrinsicHeight();
            if (Math.abs(viewRatio - 1f) <= SQUARE_RATIO_MARGIN
                    && Math.abs(drawableRatio - 1f) <= SQUARE_RATIO_MARGIN) {
                resource = new SquaringDrawable(resource, view.getWidth());
            }
        }
        super.onResourceReady(resource, animation);
        this.resource = resource;
        resource.setLoopCount(maxLoopCount);
        resource.start();
    }
    
     @Override
        protected void setResource(GlideDrawable resource) {
            view.setImageDrawable(resource);
        }
        
```
在GlideDrawableImageViewTarget的onResourceReady()方法中做了一些逻辑处理，包括如果是GIF图片的话，就调用resource.start()方法开始播放图片，
但是好像并没有看到哪里有将GlideDrawable显示到ImageView上的逻辑。确实没有，不过父类里面有，这里在第25行调用了super.onResourceReady()方法，
GlideDrawableImageViewTarget的父类是ImageViewTarget，我们来看下它的代码吧：

```java
 @Override
    public void onResourceReady(Z resource, GlideAnimation<? super Z> glideAnimation) {
        if (glideAnimation == null || !glideAnimation.animate(resource, this)) {
            setResource(resource);
        }
    }
```
可以看到，在ImageViewTarget的onResourceReady()方法当中调用了setResource()方法，而ImageViewTarget的setResource()方法是一个抽象方法，
具体的实现还是在子类那边实现的。那子类的setResource()方法是怎么实现的呢？回头再来看一下GlideDrawableImageViewTarget的setResource()方法，
没错，调用的view.setImageDrawable()方法，而这个view就是ImageView。代码执行到这里，图片终于也就显示出来了。
```java
 @Override
    protected void setResource(GlideDrawable resource) {
        view.setImageDrawable(resource);
    }
```



