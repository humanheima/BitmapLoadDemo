package com.hm.bitmaploadexample.activity;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.hm.bitmaploadexample.R;
import com.hm.bitmaploadexample.transform.CircleTransformation;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide);
        ButterKnife.bind(this);
        glide1();
        glide2();
        //glide2x();
        glide3();
        //glide4();
        glide5();
        glide6();
        glide7();
        glide8();
        glide9();
        glide10();
        glide11();
        glide12();
        glide13();
        glide14();
        glide15();
    }

    /**
     * 从网络，资源文件，或者file 加载
     * ，并使用占位图和加载出错占位图
     */
    private void glide1() {
        //从网络加载
        Glide.with(this).load(Images.imageUrls[1])
                //.asBitmap()
                // .asGif()
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .crossFade(300)//使用cross fade动画
                .into(imageView1);
//从资源文件加载
      /*  Glide.with(this).load(R.drawable.me)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(imageView1);*/
     /*   File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "glidetest.jpg");
        //从文件加载
        Glide.with(this).load(file)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .dontAnimate()//直接显示图片，而不需要crossfade效果
                .into(imageView1);*/
    }

    /**
     * 在将图片显示在ImageView之前调整图片的大小为400*400像素
     */
    private void glide2() {
        Glide.with(this).load(Images.imageUrls[2])
                .override(400, 400)//在将图片显示在ImageView之前调整图片的大小为400*400像素
                .centerCrop()//缩放类型
                .into(imageView2);
    }

    private void glide2x() {
        //自定义加载动画
        Glide.with(this).load("http://img2.3lian.com/2014/f6/173/d/51.jpg")
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .animate(R.anim.my_anim)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)//只缓存最终降低分辨后用到的图片
                .into(imageView1);

        //自定义加载动画
        ViewPropertyAnimation.Animator animator = new ViewPropertyAnimation.Animator() {
            @Override
            public void animate(View view) {
                view.setAlpha(0f);
                ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
                fadeAnim.setDuration(2500);
                fadeAnim.start();
            }
        };


        //自定义圆角图片
        Glide.with(this).load("http://img2.3lian.com/2014/f6/173/d/51.jpg")
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .transform(new GlideRoundTransform(this, 40))
                .into(imageView4);

        //自定义旋转和圆角图片
        Glide.with(this).load("http://img2.3lian.com/2014/f6/173/d/51.jpg")
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .transform(new GlideRoundTransform(this, 40), new GlideRotateTransform(this, 180))
                .into(imageView5);
    }

    /**
     * 加载gif，进行gif检查
     */
    private void glide3() {
        //加载Gif文件
        Glide.with(this)
                .load("http://img1.3lian.com/2015/w4/17/d/64.gif")//gif
                // .load(Images.imageUrls[3])
                .asGif()//强制把加载的图片生成一个gif，如果加载的图片不是gif会显示错误占位图
                //.asBitmap()//如果只想显示加载的gif的第一帧
                .error(R.mipmap.ic_launcher)
                .into(imageView3);
    }

    /**
     * 加载本地视频，感觉没什么用啊
     */
    private void glide4() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "glidevideo.mp4");
        Glide.with(this).load(Uri.fromFile(file))
                //监听是否加载成功
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Log.e("onException", e.toString() + "  model:" + model + " isFirstResource: " + isFirstResource);
                        // important to return false so the error placeholder can be placed
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Log.e("onResourceReady", "isFromMemoryCache:" + isFromMemoryCache + "  model:" + model + " isFirstResource: " + isFirstResource);
                        return false;
                    }
                })
                .into(imageView4);
    }

    /**
     * 磁盘缓存策略
     * DiskCacheStrategy.NONE 啥也不缓存
     * DiskCacheStrategy.SOURCE 只缓存全尺寸图. 上面例子里的1000x1000像素的图片
     * DiskCacheStrategy.RESULT 只缓存最终降低分辨后用到的图片
     * DiskCacheStrategy.ALL 缓存所有类型的图片 (默认行为)
     */
    private void glide5() {
        Glide.with(this).load(Images.imageUrls[5])
                .skipMemoryCache(true)//不缓存在内存里，但是图片仍然会缓存在磁盘缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE)//也不缓存在磁盘上
                // .diskCacheStrategy(DiskCacheStrategy.SOURCE)//如果你有一个图片你需要经常处理它，会生成各种不同的版本的图片,Glide只缓存原始版本：
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
                .priority(Priority.IMMEDIATE)
                .into(imageView6);
      /*  Glide.with(this).load(Images.imageUrls[7])
                .priority(Priority.LOW)
                .into(imageView7);*/
    }

    /**
     * 缩略图
     */
    private void glide7() {
        DrawableRequestBuilder<String> thumbnailRequest =
                Glide.with(this)
                        .load("https://www.baidu.com/img/bd_logo1.png");//这个图片地址应该是大图的缩略图地址

        Glide.with(this).load(Images.imageUrls[10])
                .thumbnail(thumbnailRequest)
                .into(imageView7);

    }

    /**
     * 定制view中使用SimpleTarget和ViewTarget
     */
    private void glide8() {
        SimpleTarget<Bitmap> simpleTarget = new SimpleTarget<Bitmap>(400, 400) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                imageView8.setImageBitmap(resource);
            }
        };
        Glide.with(this)
                .load(Images.imageUrls[11])
                .asBitmap()
                .into(simpleTarget);

    }

    private void glide9() {
        ViewTarget<FutureStudioView, GlideDrawable> viewTarget = new ViewTarget<FutureStudioView, GlideDrawable>(futureTudioVIew) {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                futureTudioVIew.setImage(resource.getCurrent());
            }
        };
        Glide
                .with(this) // safer!
                .load(Images.imageUrls[12])
                .into(viewTarget);
    }

    /**
     * 自定义变换
     * 当你使用变换的时候，你不能使用.centerCrop()或者.fitCenter()
     */
    private void glide10() {
        Glide.with(this)
                .load(Images.imageUrls[12])
                // .transform(new GlideRotateTransform(this, 180))//旋转图片
                .transform(new GlideRoundTransform(this,20))
                //.bitmapTransform(new BlurTransformation(this)) // this would work too!
                .into(imageView10);
    }

    /**
     * 自定义加载动画
     */
    private void glide11() {

        //自定义加载动画
        // if it's a custom view class, cast it here
        // then find subviews and do the animations
        // here, we just use the entire view for the fade animation
        ViewPropertyAnimation.Animator animator = new ViewPropertyAnimation.Animator() {
            @Override
            public void animate(View view) {
                view.setAlpha(0f);
                ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
                fadeAnim.setDuration(5000);
                fadeAnim.start();
            }
        };

        Glide.with(this).load(Images.imageUrls[13])
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                // .animate(R.anim.my_anim)//传入一个id
                .animate(animator)
                .into(imageView11);
    }

    /**
     * 加载原始大小图片
     */
    private void glide12() {
        String url = "https://www.baidu.com/img/bd_logo1.png";
        Glide.with(this).load(url)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(imageView12);
    }

    /**
     * 圆形变换
     */
    private void glide13() {
        Glide.with(this)
                .load(Images.imageUrls[20])
                .transform(new CircleTransformation(this))
                .into(imageView13);

    }

    private void glide14() {

    }

    private void glide15() {

    }

    private void glide16() {

    }


}
