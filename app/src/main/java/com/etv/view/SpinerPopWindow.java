package com.etv.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ys.etv.R;

import java.util.List;

public class SpinerPopWindow<T> extends PopupWindow {
    private LayoutInflater inflater;
    private ListView mListView;
    private List<String> list;
    private Context context;

    public SpinerPopWindow(Context context, List<String> list, AdapterView.OnItemClickListener itemClickListener) {
        super(context);
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.list = list;
        init(itemClickListener);
    }

    @SuppressLint("NewApi")
    private void init(AdapterView.OnItemClickListener clickListener) {
        View view = inflater.inflate(R.layout.simple_spinner_item, null);
        this.setContentView(view);
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
//        setBackgroundDrawable(context.getDrawable(R.drawable.spinner_yj));
        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setAdapter(new MyAdapter());
        mListView.setOnItemClickListener(clickListener);
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                holder = new ViewHolder();
                view = inflater.inflate(R.layout.spiner_item_layout, null);
                holder.textView = (TextView) view.findViewById(R.id.tv_name);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.textView.setText(getItem(i).toString());
            return view;
        }
    }

    private class ViewHolder {
        private TextView textView;
    }
}
