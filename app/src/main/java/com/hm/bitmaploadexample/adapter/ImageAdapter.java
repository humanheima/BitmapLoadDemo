package com.hm.bitmaploadexample.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.hm.bitmaploadexample.R;
import com.hm.bitmaploadexample.imageloader.ImageLoader;
import com.hm.bitmaploadexample.utils.MyUtils;

import java.util.List;

import static android.R.attr.scaleWidth;

/**
 * Created by Administrator on 2017/1/5.
 */
public class ImageAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

    private List<String> mUrList;
    private Context context;
    private Drawable mDefaultBitmapDrawable;
    private ImageLoader imageLoader;
    private boolean mIsGridViewIdle = true;
    int screenWidth;
    int space;
    int mImageWidth;

    public ImageAdapter(AbsListView absListView, List<String> mUrList, Context context) {
        absListView.setOnScrollListener(this);
        this.mUrList = mUrList;
        this.context = context;
        mDefaultBitmapDrawable = context.getResources().getDrawable(R.mipmap.ic_launcher);
        imageLoader = ImageLoader.getInstance();
        screenWidth = MyUtils.getScreenMetrics(context).widthPixels;
        space = (int) MyUtils.dp2px(context, 8f);
        mImageWidth = (scaleWidth - space) / 3;
    }

    @Override
    public int getCount() {
        return mUrList.size();
    }

    @Override
    public String getItem(int position) {
        return mUrList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.square_imageview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ImageView imageView = holder.imageView;
        final String tag = (String) imageView.getTag();
        final String url = getItem(position);
        if (!url.equals(tag)) {
            imageView.setImageDrawable(mDefaultBitmapDrawable);
        }
        if (mIsGridViewIdle) {
            imageView.setTag(url);
            imageLoader.bindBitmap(url, imageView, mImageWidth,mImageWidth);
        }
        return convertView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.e("tag", "onScrollStateChanged ");
        if (scrollState == SCROLL_STATE_IDLE) {
            Log.e("tag", "onScrollStateChanged SCROLL_STATE_IDLE");
            mIsGridViewIdle = true;
            notifyDataSetChanged();
        } else {
            mIsGridViewIdle = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    private static class ViewHolder {
        ImageView imageView;
    }
}
