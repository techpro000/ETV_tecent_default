package com.ys.bannerlib.adapter;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

class ImageViewHolder extends RecyclerView.ViewHolder {

    public ImageViewHolder(View itemView) {
        super(itemView);
    }

    public <IV extends ImageView> IV getImageView() {
        return (IV) itemView;
    }
}
