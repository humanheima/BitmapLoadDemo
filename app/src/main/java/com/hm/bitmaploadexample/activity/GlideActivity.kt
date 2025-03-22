package com.hm.bitmaploadexample.activity

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.hm.bitmaploadexample.R
import com.hm.bitmaploadexample.activity.sourcecode.MyCustomViewTarget
import com.hm.bitmaploadexample.databinding.ActivityGlideBinding
import com.hm.bitmaploadexample.transform.BlurTransformation
import com.hm.bitmaploadexample.transform.GlideRotateTransform
import com.hm.bitmaploadexample.transform.GlideRoundTransform
import com.hm.bitmaploadexample.utils.Images
import com.hm.bitmaploadexample.widget.FutureStudioView

/**
 * 使用glide
 */
class GlideActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "GlideActivity"
    }

    private var futureTudioVIew: FutureStudioView? = null
    private lateinit var options: RequestOptions

    private lateinit var binding: ActivityGlideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGlideBinding.inflate(layoutInflater)
        setContentView(binding.root)
        findViews()
        options = RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher)
        glide1()
        glide2()
        glide3()
        glide4()
        glide5()
        glide6()
        glide7()
        glide8()
        glide9()
        glide10()
        glide11()
        glide12()
        glide13()
        glideIntoTarget()
        useGlideApp()
    }

    private fun findViews() {
        futureTudioVIew = findViewById(R.id.future_tudio_view)
    }

    /**
     * 从网络，资源文件，或者file 加载
     * ，并使用占位图和加载出错占位图
     */
    private fun glide1() {
        //从网络加载
        Glide.with(this) //.asGif()
            .load(Images.imageUrls[1])
            // .asGif()
            .apply(options)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.imageView1)
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
    private fun glide2() {
        Glide.with(this).load(Images.imageUrls[2])
            .apply(RequestOptions.overrideOf(400, 400)) //在将图片显示在ImageView之前调整图片的大小为400*400像素
            .apply(RequestOptions.centerCropTransform()) //缩放类型
            .into(binding.imageView2)
    }

    /**
     * 应用多个变换
     */
    private fun glide3() {
        //自定义圆角、旋转、模糊图片,应用变换的顺序不一样，结果会不一样
        Glide.with(this).load(Images.imageUrls[5])
            .apply(RequestOptions.overrideOf(400, 400))
            .apply(
                RequestOptions().transform(
                    MultiTransformation(
                        BlurTransformation(this, 15),
                        GlideRotateTransform(180),
                        GlideRoundTransform(40)
                    )
                )
            )
            .into(binding.imageView3)

        //另外一种方法。
//        Glide.with(this).load(Images.imageUrls[5])
//                .transform(new FitCenter(), new BlurTransformation(this, 15), new GlideRotateTransform(180))
//                .into(imageView3);
    }

    /**
     * 加载gif，进行gif检查
     */
    private fun glide4() {
        //加载Gif文件
        Glide.with(this)
            .asGif() //强制把加载的图片生成一个gif，如果加载的图片不是gif会显示错误占位图
            .load("http://img1.3lian.com/2015/w4/17/d/64.gif") //gif
            .apply(options) //.asBitmap()//如果只想显示加载的gif的第一帧
            .into(binding.imageView4)
    }

    /**
     * 磁盘缓存策略
     */
    private fun glide5() {
        Glide.with(this).load(Images.imageUrls[5])
            .apply(RequestOptions.skipMemoryCacheOf(true))
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
            .into(binding.imageView5)
    }

    /**
     * 图片请求优先级
     * Priority.LOW
     * Priority.NORMAL
     * Priority.HIGH
     * Priority.IMMEDIATE//优先级最高
     */
    private fun glide6() {
        Glide.with(this).load(Images.imageUrls[6])
            .apply(RequestOptions.priorityOf(Priority.IMMEDIATE))
            .into(binding.imageView6)
    }

    /**
     * 加载缩略图
     */
    private fun glide7() {
        //缩略图路径
        val thumbnailUrl = ""
        Glide.with(this).load(Images.imageUrls[10])
            .thumbnail(Glide.with(this).load(thumbnailUrl))
            .into(binding.imageView7)

        //第二种方法
        Glide.with(this).load(Images.imageUrls[10])
            .thumbnail(0.25f)
            .into(binding.imageView7)
    }

    /**
     * 定制view中使用SimpleTarget和ViewTarget
     */
    private fun glide8() {
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
            .into(object : CustomViewTarget<ImageView?, Bitmap?>(binding.imageView8) {
                override fun onLoadFailed(errorDrawable: Drawable?) {}
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    Log.i(TAG, "glide8 CustomViewTarget onResourceReady: ")
                    binding.imageView8.setImageBitmap(resource)
                }

                override fun onResourceCleared(placeholder: Drawable?) {}
            })
    }

    private fun glide9() {
        val viewTarget: CustomViewTarget<FutureStudioView, Drawable?> =
            object : CustomViewTarget<FutureStudioView, Drawable?>(
                binding.futureTudioView
            ) {
                override fun onResourceCleared(placeholder: Drawable?) {}
                override fun onLoadFailed(errorDrawable: Drawable?) {}
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    binding.futureTudioView.setImage(resource)
                }
            }
        Glide.with(this)
            .load(Images.imageUrls[12])
            .into(viewTarget)
    }

    /**
     * 自定义变换
     */
    private fun glide10() {
        Glide.with(this)
            .load(Images.imageUrls[12]) //.fitCenter()
            .apply(RequestOptions().transform(GlideRoundTransform(20))) /*.apply(new RequestOptions().transform(new MultiTransformation<>(
                        new RoundedCorners(20),
                        new FitCenter())))//应用多个变换*/
            .into(binding.imageView10)
    }

    /**
     * 圆形变换
     */
    private fun glide11() {
        val requestOptions = RequestOptions().transform(CircleCrop())
        Glide.with(this)
            .load(Images.imageUrls[20])
            .apply(requestOptions)
            .into(binding.imageView11)
    }

    /**
     * 在失败时开始新的请求
     */
    private fun glide12() {
        val primaryUrl = ""
        //加载失败时的后备图片地址
        val backUrl = Images.imageUrls[12]
        Glide.with(this)
            .load(primaryUrl)
            .error(Glide.with(this).load(backUrl))
            .into(binding.imageView12)
    }

    /**
     * 使用 Generated API
     */
    private fun glide13() {
        Glide.with(this)
            .load(Images.imageUrls[12])
            .override(480, 800)
            .into(binding.imageView13)
    }

    private fun glideIntoTarget() {
        Glide.with(this)
            .load(Images.imageUrls[12])
            .into(object : MyCustomViewTarget<ImageView?, Drawable>(binding.imageView14) {
                override fun onResourceCleared(placeholder: Drawable?) {}
                override fun onLoadFailed(errorDrawable: Drawable?) {}
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                }
            })
    }

    private fun useGlideApp() {
//        GlideApp.with(this)
//                //.mAsGif()
//                .load(Images.imageUrls[15])
//                .miniThumb(50)
//                .into(imageView15);
    }


}
