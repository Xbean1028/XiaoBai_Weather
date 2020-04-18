package com.bean.xiaobai_weather.city_manager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bean.xiaobai_weather.R;
import com.bean.xiaobai_weather.WeatherActivity;
import com.bean.xiaobai_weather.db.DBManager;
import com.bean.xiaobai_weather.db.DatabaseBean;
import com.bean.xiaobai_weather.util.HttpUtil;
import com.bean.xiaobai_weather.util.IconUtils;
import com.bumptech.glide.Glide;

import org.joda.time.DateTime;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CityActivity extends AppCompatActivity implements View.OnClickListener{
    ImageView addIv,backIv,deleteIv;
    ListView cityLv;
    public DrawerLayout drawerLayout;
    List<DatabaseBean> mDatas;  //显示列表数据源
    private CityManagerAdapter adapter;
    public SwipeRefreshLayout swipeRefresh;//下拉刷新
    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        addIv = (ImageView) findViewById(R.id.city_iv_add);
        backIv = (ImageButton) findViewById(R.id.city_iv_back);
        deleteIv = (ImageButton) findViewById(R.id.city_iv_delete);
        cityLv = (ListView) findViewById(R.id.city_lv);
        mDatas = new ArrayList<>();
//        添加点击监听事件
        addIv.setOnClickListener(this);
        deleteIv.setOnClickListener(this);
        backIv.setOnClickListener(this);
//        设置适配器
        adapter = new CityManagerAdapter(this, mDatas);
        cityLv.setAdapter(adapter);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img2);
        DateTime nowTime = DateTime.now();
        int hourOfDay = nowTime.getHourOfDay();
        if (hourOfDay > 6 && hourOfDay < 19) {
            bingPicImg.setImageResource(R.mipmap.back_100d);
        } else {
            bingPicImg.setImageResource(R.mipmap.back_100n);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.draweradd_layout);
        //下拉刷新
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_city);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);//下拉刷新颜色
        //设置下拉刷新监听器
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //requestWeather(mWeatherId);
                Toast.makeText(CityActivity.this, "I am try", Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);//下拉刷新恢复
            }
        });
        cityLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CityActivity.this, WeatherActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                DatabaseBean bean = mDatas.get(position);
                String weathername = bean.getCity();
                intent.putExtra("weather_name", weathername);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CityActivity.this).edit();
                editor.putString("weather", null);
                editor.apply();
                startActivity(intent);
                finish();
            }
        });
    }
    /*  获取数据库当中真实数据源，添加到原有数据源当中，提示适配器更新*/
    @Override
    protected void onResume() {
        super.onResume();
        List<DatabaseBean> list = DBManager.queryAllInfo();
        mDatas.clear();
        mDatas.addAll(list);
        adapter.notifyDataSetChanged();
    }

    protected void onStart(){
        super.onStart();
        List<DatabaseBean> list = DBManager.queryAllInfo();
        mDatas.clear();
        mDatas.addAll(list);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.city_iv_add:
//                    Intent intent = new Intent(this, SearchCityActivity.class);
//                    startActivity(intent);
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.city_iv_back:
                finish();
                break;
            case R.id.city_iv_delete:
                Intent intent1 = new Intent(this, DeleteCityActivity.class);
                startActivity(intent1);
                break;
        }
    }

}
