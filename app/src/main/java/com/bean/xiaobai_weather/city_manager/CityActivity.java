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
import com.bean.xiaobai_weather.gson.Weather;
import com.bean.xiaobai_weather.util.HttpUtil;
import com.bean.xiaobai_weather.util.IconUtils;
import com.bean.xiaobai_weather.util.Utility;
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
    private List<String> cityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        addIv = (ImageView) findViewById(R.id.city_iv_add);
        backIv = (ImageButton) findViewById(R.id.city_iv_back);
        deleteIv = (ImageButton) findViewById(R.id.city_iv_delete);
        cityLv = (ListView) findViewById(R.id.city_lv);
        mDatas = new ArrayList<>();
        cityList = DBManager.queryAllCityName();//获取数据库包含的城市信息列表
//        添加点击监听事件
        addIv.setOnClickListener(this);
        deleteIv.setOnClickListener(this);
        backIv.setOnClickListener(this);
//        设置适配器
        adapter = new CityManagerAdapter(this, mDatas);
        cityLv.setAdapter(adapter);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img2);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        Weather weather = Utility.handleWeatherResponse(weatherString);
        DateTime nowTime = DateTime.now();
        int hourOfDay = nowTime.getHourOfDay();
        if (hourOfDay > 6 && hourOfDay < 19) {
            bingPicImg.setImageResource(IconUtils.getDayBack(weather.now.cond_code));
        } else {
            bingPicImg.setImageResource(IconUtils.getNightBack(weather.now.cond_code));
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.draweradd_layout);
        //下拉刷新
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_city);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);//下拉刷新颜色
        //设置下拉刷新监听器
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                for(String city: cityList){
                    requestWeather(city);
                }
                //requestWeather(mWeatherId);mDatas.clear();
                List<DatabaseBean> list = DBManager.queryAllInfo();
                mDatas.clear();
                mDatas.addAll(list);
                adapter.notifyDataSetChanged();
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
    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "https://free-api.heweather.net/s6/weather?location=" + weatherId + "&key=e59171beb7a84b0c9483bc75910f4b68";
        //String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        //String airUrl = "https://free-api.heweather.net/s6/air/now?location=" + weatherId + "&key=e59171beb7a84b0c9483bc75910f4b68";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CityActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                        } else {
                            if (weather == null){
                                Toast.makeText(CityActivity.this, responseText, Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(CityActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);//下拉刷新恢复
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CityActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);//下拉刷新恢复
                    }
                });
            }
        });
        //loadBingPic();
    }
}
