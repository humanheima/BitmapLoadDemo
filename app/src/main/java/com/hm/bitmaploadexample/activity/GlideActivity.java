package com.hm.bitmaploadexample.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hm.bitmaploadexample.GlideApp;
import com.hm.bitmaploadexample.R;
import com.hm.bitmaploadexample.transform.BlurTransformation;
import com.hm.bitmaploadexample.transform.GlideRotateTransform;
import com.hm.bitmaploadexample.transform.GlideRoundTransform;
import com.hm.bitmaploadexample.utils.Images;
import com.hm.bitmaploadexample.widget.FutureStudioView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 使用glide
 */
public class GlideActivity extends AppCompatActivity {

    @BindView(R.id.imageView1)
    ImageView imageView1;
    @BindView(R.id.activity_glide)
    ScrollView activityGlide;
    @BindView(R.id.imageView2)
    ImageView imageView2;
    @BindView(R.id.imageView3)
    ImageView imageView3;
    @BindView(R.id.imageView4)
    ImageView imageView4;
    @BindView(R.id.imageView5)
    ImageView imageView5;
    @BindView(R.id.imageView6)
    ImageView imageView6;
    @BindView(R.id.imageView7)
    ImageView imageView7;
    @BindView(R.id.imageView8)
    ImageView imageView8;
    @BindView(R.id.imageView9)
    ImageView imageView9;
    @BindView(R.id.imageView10)
    ImageView imageView10;
    @BindView(R.id.imageView11)
    ImageView imageView11;
    @BindView(R.id.imageView12)
    ImageView imageView12;
    @BindView(R.id.imageView13)
    ImageView imageView13;
    @BindView(R.id.imageView14)
    ImageView imageView14;
    @BindView(R.id.future_tudioV_iew)
    FutureStudioView futureTudioVIew;

    private RequestOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide);
        ButterKnife.bind(this);
        options = new RequestOptions()
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher);

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
        glide15();
    }

    /**
     * 从网络，资源文件，或者file 加载
     * ，并使用占位图和加载出错占位图
     */
    private void glide1() {
        //从网络加载
        Glide.with(this)
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
    }

    /**
     * 定制view中使用SimpleTarget和ViewTarget
     */
    private void glide8() {
        Glide.with(this)
                .asBitmap()
                .load(Images.imageUrls[11])
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        imageView8.setImageBitmap(resource);
                    }
                });
    }

    private void glide9() {
        ViewTarget<FutureStudioView, Drawable> viewTarget = new ViewTarget<FutureStudioView, Drawable>(futureTudioVIew) {
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
        Glide.with(this)
                .load(Images.imageUrls[20])
                .apply(new RequestOptions().transform(new CircleCrop()))
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
        GlideApp.with(this)
                .load(Images.imageUrls[12])
                .override(480, 800)
                .into(imageView13);
    }

    private void glide14() {

    }

    private void glide15() {

    }


}
