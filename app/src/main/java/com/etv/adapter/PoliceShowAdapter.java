package com.etv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.etv.entity.PoliceNumEntity;
import com.etv.util.MyLog;
import com.ys.etv.R;
import com.ys.model.listener.AdapterItemClickListener;

import java.util.List;


public class PoliceShowAdapter extends BaseAdapter {
    List<PoliceNumEntity> appInfos;
    Context context;
    LayoutInflater inflater;
    private int showTag = 0;  //0 policDialog   1:设置界面

    public void setList(List<PoliceNumEntity> paramList) {
        this.appInfos = paramList;
        notifyDataSetChanged();
    }

    public PoliceShowAdapter(Context paramContext, List<PoliceNumEntity> paramList, int showTag) {
        this.context = paramContext;
        this.showTag = showTag;
        this.appInfos = paramList;
        inflater = LayoutInflater.from(context);
    }

    public Context getContext() {
        return this.context;
    }

    public int getCount() {
        return appInfos.size();
    }

    public Object getItem(int paramInt) {
        return this.appInfos.get(paramInt);
    }

    public long getItemId(int paramInt) {
        return paramInt;
    }

    public View getView(int position, View convertView, ViewGroup paramViewGroup) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            if (showTag == 0) {
                convertView = inflater.inflate(R.layout.item_police_show, null);
            } else {
                convertView = inflater.inflate(R.layout.item_police_set, null);
            }
            viewHolder.tv_phone_num = ((TextView) convertView.findViewById(R.id.tv_phone_num));
            viewHolder.iv_show_check = ((ImageView) convertView.findViewById(R.id.iv_show_check));
            viewHolder.lin_item_layout = ((View) convertView.findViewById(R.id.lin_item_layout));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        PoliceNumEntity policeNumEntity = appInfos.get(position);
        boolean isChooice = policeNumEntity.isChooice();
        MyLog.cdl("=========item刷新=========isChooice=====" + isChooice);
        if (isChooice) {
            viewHolder.iv_show_check.setBackgroundResource(R.mipmap.check_yes);
        } else {
            viewHolder.iv_show_check.setBackgroundResource(R.mipmap.check_no);
        }
        viewHolder.tv_phone_num.setText(policeNumEntity.getPhoneNum());
        viewHolder.lin_item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyLog.cdl("=========点击了item按钮==============");
                if (adapterItemClickListener == null) {
                    return;
                }
                adapterItemClickListener.adapterItemClick(policeNumEntity, position);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        ImageView iv_show_check;
        TextView tv_phone_num;
        View lin_item_layout;
    }

    AdapterItemClickListener adapterItemClickListener;

    public void setAdapterItemClick(AdapterItemClickListener adapterItemClickListener) {
        this.adapterItemClickListener = adapterItemClickListener;
    }

}
