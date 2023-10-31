RequestManager

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