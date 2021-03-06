package com.longc.mobileplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.longc.mobileplayer.R;
import com.longc.mobileplayer.domain.SearchBean;

import java.util.List;


/**
 * Created by longc on 2016/12/9.
 */
public class SearchAdapter extends BaseAdapter {

    private final List<SearchBean.ItemData> mediaItems;


    private Context context;

    public SearchAdapter(Context context, List<SearchBean.ItemData> mediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;
    }


    @Override

    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHoder viewHoder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_search, null);
            viewHoder = new ViewHoder();
            viewHoder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHoder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
            convertView.setTag(viewHoder);
        } else {
            viewHoder = (ViewHoder) convertView.getTag();
        }
        //根据position得到列表中对应位置的数据
        SearchBean.ItemData mediaItem = mediaItems.get(position);
        viewHoder.tv_name.setText(mediaItem.getItemTitle());
        viewHoder.tv_desc.setText(mediaItem.getKeywords());
        //1、使用xutils加载图片
//        x.image().bind( viewHolder.iv_icon,mediaItem.getImageUrl());
        //2、使用Glide请求加载图片
        Glide.with(context).load(mediaItem.getItemImage().getImgUrl1())
                //.diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.video_default)
                .error(R.drawable.video_default)
                .into(viewHoder.iv_icon);
        return convertView;
    }

    static class ViewHoder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_desc;
    }
}
