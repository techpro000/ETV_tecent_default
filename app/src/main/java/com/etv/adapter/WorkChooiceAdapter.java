package com.etv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.etv.entity.WorkChooiceEntity;
import com.ys.etv.R;

import java.util.List;

public class WorkChooiceAdapter extends BaseAdapter {

    List<WorkChooiceEntity> list;
    Context context;
    LayoutInflater inflater;

    public void setWorkList(List<WorkChooiceEntity> workChooiceEntityList) {
        this.list = workChooiceEntityList;
        notifyDataSetChanged();
    }

    public WorkChooiceAdapter(Context context, List<WorkChooiceEntity> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (list == null || list.size() < 1) {
            return 0;
        }
        return list.size();
    }

    @Override
    public WorkChooiceEntity getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_work_chooice, null);
            holder.iv_chooice_statues = (ImageView) convertView.findViewById(R.id.iv_chooice_statues);
            holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        WorkChooiceEntity workChooiceEntity = list.get(position);
        holder.iv_icon.setBackgroundResource(workChooiceEntity.getImageId());
        holder.tv_desc.setText(workChooiceEntity.getDesc() + "");
        boolean isChoice = workChooiceEntity.isChooice();
        holder.iv_chooice_statues.setBackgroundResource(isChoice ? R.mipmap.check_yes : R.mipmap.check_no);
        return convertView;
    }


    class ViewHolder {
        ImageView iv_icon;
        TextView tv_desc;
        ImageView iv_chooice_statues;
    }

}
