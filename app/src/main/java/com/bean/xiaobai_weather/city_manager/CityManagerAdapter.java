package com.bean.xiaobai_weather.city_manager;

/**
 * Created by Bean on 2020/4/16.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bean.xiaobai_weather.R;
import com.bean.xiaobai_weather.gson.Weather;
import com.bean.xiaobai_weather.db.DatabaseBean;
import com.google.gson.Gson;

import java.util.List;

public class CityManagerAdapter extends BaseAdapter{
    Context context;
    List<DatabaseBean>mDatas;

    public CityManagerAdapter(Context context, List<DatabaseBean> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_city_manager,null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        DatabaseBean bean = mDatas.get(position);
        holder.cityTv.setText(bean.getCity());
        Weather weatherBean = new Gson().fromJson(bean.getContent(), Weather.class);
//        获取今日天气情况
        String todayTemp = weatherBean.now.temperature;
        String city_tv_condition = weatherBean.now.info;
        holder.currentTempTv.setText(todayTemp + "℃");
        holder.city_tv_condition.setText(city_tv_condition);
        return convertView;
    }

    class ViewHolder{
        TextView cityTv,currentTempTv,city_tv_condition;
        public ViewHolder(View itemView){
            cityTv = itemView.findViewById(R.id.item_city_tv_city);
            currentTempTv = itemView.findViewById(R.id.item_city_tv_temp);
            city_tv_condition = itemView.findViewById(R.id.item_city_tv_condition);
        }
    }
}