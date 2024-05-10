package com.hm.bitmaploadexample.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hm.bitmaploadexample.R;
import com.hm.bitmaploadexample.activity.sourcecode.MyCustomViewTarget;
import com.hm.bitmaploadexample.transform.BlurTransformation;
import com.hm.bitmaploadexample.transform.GlideRotateTransform;
import com.hm.bitmaploadexample.transform.GlideRoundTransform;
import com.hm.bitmaploadexample.utils.Images;
import com.hm.bitmaploadexample.widget.FutureStudioView;

/**
 * 使用glide
 */
public class GlideActivity extends AppCompatActivity {

    private static final String TAG = "GlideActivity";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide);

        findViews();
        options = new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher);

        glide1();
        glide2();
        glide3();
        glide4();
        glide5();
        glide6();
        glide7();
        glide8();
        glide9();
        glide10();
        glide11();
        glide12();
        glide13();

        glideIntoTarget();

        useGlideApp();
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
     * 从网络，资源文件，或者file 加载
     * ，并使用占位图和加载出错占位图
     */
    private void glide1() {
        //从网络加载
        Glide.with(this)
                //.asGif()
                .load(Images.imageUrls[1])
                // .asGif()
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView1);
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

    private void glide9() {
        ViewTarget<FutureStudioView, Drawable> viewTarget = new ViewTarget<FutureStudioView, Drawable>(
                futureTudioVIew) {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                futureTudioVIew.setImage(resource);
            }
        };
        Glide.with(this)
                .load(Images.imageUrls[12])
                .into(viewTarget);
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
                .into(new MyCustomViewTarget<ImageView, Drawable>(imageView14) {


                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {

                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                            @Nullable Transition<? super Drawable> transition) {

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
