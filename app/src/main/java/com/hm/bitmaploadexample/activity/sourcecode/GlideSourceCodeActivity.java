package com.hm.bitmaploadexample.activity.sourcecode;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.hm.bitmaploadexample.R;
import com.hm.bitmaploadexample.databinding.ActivityGlideSourceCodeBinding;
import com.hm.bitmaploadexample.transform.BlurTransformation;
import com.hm.bitmaploadexample.transform.GlideRotateTransform;
import com.hm.bitmaploadexample.transform.GlideRoundTransform;
import com.hm.bitmaploadexample.utils.Images;
import com.hm.bitmaploadexample.widget.FutureStudioView;

/**
 * Glide源码分析
 */
public class GlideSourceCodeActivity extends AppCompatActivity {

    private static final String TAG = "GlideSourceCodeActivity";

    ImageView imageView1;
    ScrollView activityGlide;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;
    ImageView imageView5;
    ImageView imageView6;
    ImageView imageView7;
    ImageView imageView8;
    ImageView imageView9;
    ImageView imageView10;
    ImageView imageView11;
    ImageView imageView12;
    ImageView imageView13;
    ImageView imageView14;
    ImageView imageView15;

    FutureStudioView futureTudioVIew;

    private RequestOptions options;

    private ActivityGlideSourceCodeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGlideSourceCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        findViews();
//        options = new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher)
//        .diskCacheStrategy(DiskCacheStrategy.ALL);

        //sourceCodeTest();
        //glide1();
//        glide2();
//        glide3();
//        glide4();
//        glide5();
//        glide6();
//        glide7();
//        glide8();
//        glide9();
//        glide10();
//        glide11();
//        glide12();
//        glide13();
//
//        glideIntoTarget();
//
//        useGlideApp();

        loadSameUrl();

        binding.btnTestHeightProblem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //sourceCodeTest();
                loadSameUrl();
            }
        });

        binding.btnSourceCode.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                glide1();
            }

        });

        //useInBackgroundThread();

    }

    private void findViews() {
        imageView1 = findViewById(R.id.imageView1);
        activityGlide = findViewById(R.id.activity_glide);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);
        imageView4 = findViewById(R.id.imageView4);
        imageView5 = findViewById(R.id.imageView5);
        imageView6 = findViewById(R.id.imageView6);
        imageView7 = findViewById(R.id.imageView7);
        imageView8 = findViewById(R.id.imageView8);
        imageView9 = findViewById(R.id.imageView9);
        imageView10 = findViewById(R.id.imageView10);
        imageView11 = findViewById(R.id.imageView11);
        imageView12 = findViewById(R.id.imageView12);
        imageView13 = findViewById(R.id.imageView13);
        imageView14 = findViewById(R.id.imageView14);
        futureTudioVIew = findViewById(R.id.future_tudio_view);
        imageView15 = findViewById(R.id.imageView15);
    }

    /**
     * 最简单的使用方式
     */
    private void sourceCodeTest() {
        String imageUrl = Images.imageUrls[1];
        Glide.with(this)
                .load(imageUrl)
                //.diskCacheStrategy(DiskCacheStrategy.ALL)
                //.transform(new BlurTransformation(this, 15))
                .into(binding.ivHeightProblem);

//        binding.ivHeightProblem.post(new Runnable() {
//
//            @Override
//            public void run() {
//                Log.e(TAG, "run: " + binding.ivHeightProblem.getWidth() + " , " + binding.ivHeightProblem.getHeight());
//            }
//        });

    }

    /**
     * 测试1. 加载相同的url，使用缓存
     * 2. 服务端的内容发生了变化，怎么跳过缓存，加载到最新的url？
     * <p>
     * 方法1. 加 signature
     * <p>
     * 方法2. 加一个随机参数，比如时间戳参数
     */
    private void loadSameUrl() {
        String imageUrl = Images.imageUrls[1];
        //imageUrl += "?t=" + System.currentTimeMillis();
        Log.d(TAG, "loadSameUrl: imageUrl = " + imageUrl);
        Glide.with(this)
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        Log.d("Glide", "加载失败");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        switch (dataSource) {
                            case MEMORY_CACHE:
                                Log.d(TAG, "图片从内存缓存加载");
                                break;
                            case DATA_DISK_CACHE:
                                Log.d(TAG, "图片从原始的磁盘缓存加载");
                                break;
                            case RESOURCE_DISK_CACHE:
                                Log.d(TAG, "图片从修改过的磁盘缓存加载");
                                break;
                            case REMOTE:
                                Log.d(TAG, "图片从网络加载");
                                break;
                            case LOCAL:
                                Log.d(TAG, "图片从本地加载");
                                break;
                            default:
                                Log.d(TAG, "其他来源");
                                break;
                        }
                        return false; // 返回 false 表示不拦截，继续正常加载
                    }
                })
                .signature(new ObjectKey(System.currentTimeMillis()))
                .into(binding.ivHeightProblem);
    }

    /**
     * 在后台线程使用
     */
    private void useInBackgroundThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                FutureTarget<Bitmap> futureTarget =
                        Glide.with(GlideSourceCodeActivity.this)
                                .asBitmap()
                                .load(Images.imageUrls[1])
                                //.submit(width, height);//可以指定宽高
                                .submit();
                //get 方法必须在子线程调用
                try {
                    Bitmap bitmap = futureTarget.get();
                    Log.d(TAG, "run: bitmap = " + bitmap);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.ivHeightProblem.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "run: error " + e.getMessage());
                }
            }
        }).start();

        // Do something with the Bitmap and then when you're done with it:
        //Glide.with(this).clear(futureTarget);

    }

    /**
     * 从网络，资源文件，或者file 加载
     * ，并使用占位图和加载出错占位图
     */
    private void glide1() {
        //从网络加载
        Glide.with(this)
                //.asGif()
                .load(Images.imageUrls[1])
                // .asGif()
                //.apply(options)
                //.transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imageView1);
        //从资源文件加载
      /*  Glide.with(this).load(R.drawable.me)
                .into(imageView1);*/
        //从文件加载
       /* File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "glidetest.jpg");
        Glide.with(this).load(file)
                .into(imageView1);*/
    }

    /**
     * 在将图片显示在ImageView之前调整图片的大小为400*400像素
     */
    private void glide2() {
        Glide.with(this).load(Images.imageUrls[2])
                .apply(RequestOptions.overrideOf(400, 400))//在将图片显示在ImageView之前调整图片的大小为400*400像素
                .apply(RequestOptions.centerCropTransform())//缩放类型
                .into(imageView2);
    }

    /**
     * 应用多个变换
     */
    private void glide3() {
        //自定义圆角、旋转、模糊图片,应用变换的顺序不一样，结果会不一样
        Glide.with(this).load(Images.imageUrls[5])
                .apply(RequestOptions.overrideOf(400, 400))
                .apply(new RequestOptions().transform(new MultiTransformation<>(
                                new BlurTransformation(this, 15)
                                , new GlideRotateTransform(180)
                                , new GlideRoundTransform(40)
                        )
                ))
                .into(imageView3);

        //另外一种方法。
//        Glide.with(this).load(Images.imageUrls[5])
//                .transform(new FitCenter(), new BlurTransformation(this, 15), new GlideRotateTransform(180))
//                .into(imageView3);
    }

    /**
     * 加载gif，进行gif检查
     */
    private void glide4() {
        //加载Gif文件
        Glide.with(this)
                .asGif()//强制把加载的图片生成一个gif，如果加载的图片不是gif会显示错误占位图
                .load("http://img1.3lian.com/2015/w4/17/d/64.gif")//gif
                .apply(options)
                //.asBitmap()//如果只想显示加载的gif的第一帧
                .into(imageView4);
    }

    /**
     * 磁盘缓存策略
     */
    private void glide5() {
        Glide.with(this).load(Images.imageUrls[5])
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
                .into(imageView5);
    }

    /**
     * 图片请求优先级
     * Priority.LOW
     * Priority.NORMAL
     * Priority.HIGH
     * Priority.IMMEDIATE//优先级最高
     */
    private void glide6() {
        Glide.with(this).load(Images.imageUrls[6])
                .apply(RequestOptions.priorityOf(Priority.IMMEDIATE))
                .into(imageView6);
    }

    /**
     * 加载缩略图
     */
    private void glide7() {
        //缩略图路径
        String thumbnailUrl = "";
        Glide.with(this).load(Images.imageUrls[10])
                .thumbnail(Glide.with(this).load(thumbnailUrl))
                .into(imageView7);

        //第二种方法
        Glide.with(this).load(Images.imageUrls[10])
                .thumbnail(0.25f)
                .into(imageView7);
    }

    /**
     * 定制view中使用SimpleTarget和ViewTarget
     */
    private void glide8() {
//        Glide.with(this)
//                .asBitmap()
//                .load(Images.imageUrls[11])
//                .into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(@NonNull Bitmap resource,
//                            @Nullable Transition<? super Bitmap> transition) {
//                        imageView8.setImageBitmap(resource);
//                    }
//                });

        Glide.with(this)
                .asBitmap()
                .load(Images.imageUrls[11])
                .into(new CustomViewTarget<ImageView, Bitmap>(imageView8) {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource,
                                                @Nullable Transition<? super Bitmap> transition) {
                        Log.i(TAG, "glide8 CustomViewTarget onResourceReady: ");
                        imageView8.setImageBitmap(resource);
                    }

                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    /**
     * 自定义变换
     */
    private void glide10() {
        Glide.with(this)
                .load(Images.imageUrls[12])
                //.fitCenter()
                .apply(new RequestOptions().transform(new GlideRoundTransform(20)))
                /*.apply(new RequestOptions().transform(new MultiTransformation<>(
                        new RoundedCorners(20),
                        new FitCenter())))//应用多个变换*/
                .into(imageView10);
    }

    /**
     * 圆形变换
     */
    private void glide11() {
        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
        Glide.with(this)
                .load(Images.imageUrls[20])
                .apply(requestOptions)
                .into(imageView11);
    }

    /**
     * 在失败时开始新的请求
     */
    private void glide12() {
        String primaryUrl = "";
        //加载失败时的后备图片地址
        String backUrl = Images.imageUrls[12];
        Glide.with(this)
                .load(primaryUrl)
                .error(Glide.with(this).load(backUrl))
                .into(imageView12);
    }

    /**
     * 使用 Generated API
     */
    private void glide13() {
        Glide.with(this)
                .load(Images.imageUrls[12])
                .override(480, 800)
                .into(imageView13);
    }

    private void glideIntoTarget() {
        Glide.with(this)
                .load(Images.imageUrls[12])
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                                                @Nullable Transition<? super Drawable> transition) {
                        Log.i(TAG, "glideIntoTarget onResourceReady: ");
                        imageView14.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

    }

    private void useGlideApp() {
//        GlideApp.with(this)
//                //.mAsGif()
//                .load(Images.imageUrls[15])
//                .miniThumb(50)
//                .into(imageView15);

    }

}
