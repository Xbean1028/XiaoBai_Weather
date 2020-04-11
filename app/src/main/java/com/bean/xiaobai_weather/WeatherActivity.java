package com.bean.xiaobai_weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bean.xiaobai_weather.gson.Forecast;
import com.bean.xiaobai_weather.gson.Lifestyle;
import com.bean.xiaobai_weather.gson.Weather;
import com.bean.xiaobai_weather.service.AutoUpdateService;
import com.bean.xiaobai_weather.util.HttpUtil;
import com.bean.xiaobai_weather.util.Utility;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;

    public SwipeRefreshLayout swipeRefresh;

    private ScrollView weatherLayout;//滚动视图对象

    private Button navButton;

    private TextView titleCity;

    private TextView titleUpdateTime;//基本信息--更新时间

    private TextView degreeText;

    private TextView weatherInfoText;
    private TextView weatherlat;//基本信息--纬度
    private TextView weatherlon;//基本信息--经度
    //实时天气
    private TextView degreetext;//实时天气信息--温度
    private TextView fltext;//实时天气信息--体感温度
    private TextView weatherinfo_text;//实时天气信息--天气信息
    private TextView humtext;//实时天气信息--相对湿度
    private TextView dirtext;//实时天气信息--风向
    private TextView sctext;//实时天气信息--风力
    private TextView spdtext;//实时天气信息--风速
    private TextView pcpntext;//实时天气信息--降水量

    private TextView aqiText;//空气质量--空气质量指数
    private TextView coText;//空气质量--一氧化碳指数
    private TextView no2Text;//空气质量--二氧化氮指数
    private TextView o3Text;//空气质量--臭氧指数
    private TextView pm10Text;//空气质量--PM10指数
    private TextView pm25Text;//空气质量--PM2.5指数
    private TextView qltyText;//空气质量--空气质量水平
    private TextView so2Text;//空气质量--二氧化硫指数

    private LinearLayout forecastLayout;//线性布局对象--预报天气
    private LinearLayout lifestyleLayout;//线性布局对象--建议
    private ImageView bingPicImg;

    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        // 初始化各控件
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        //degreeText = (TextView) findViewById(R.id.degree_text);
        //weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        lifestyleLayout = (LinearLayout)findViewById(R.id.lifestytle_layout);
        weatherlat = (TextView)findViewById(R.id.weather_lat);
        weatherlon = (TextView)findViewById(R.id.weather_lon);
        //实时天气
        degreetext = (TextView)findViewById(R.id.degree_text);
        fltext = (TextView)findViewById(R.id.fl_text);
        weatherinfo_text = (TextView)findViewById(R.id.weather_info_text);
        humtext = (TextView)findViewById(R.id.hum_text);
        dirtext = (TextView)findViewById(R.id.dir_text);
        sctext = (TextView)findViewById(R.id.sc_text);
        spdtext = (TextView)findViewById(R.id.spd_text);
        pcpntext = (TextView)findViewById(R.id.pcpn_text);
        //空气质量
        aqiText = (TextView) findViewById(R.id.aqi_text);//空气质量--空气质量指数
        coText = (TextView) findViewById(R.id.co_text);//空气质量--一氧化碳指数
        no2Text = (TextView) findViewById(R.id.no2_text);//空气质量--二氧化氮指数
        o3Text = (TextView) findViewById(R.id.o3_text);//空气质量--臭氧指数
        pm10Text = (TextView) findViewById(R.id.pm10_text); //空气质量--PM10指数
        pm25Text = (TextView) findViewById(R.id.pm25_text);//空气质量--PM2.5指数
        qltyText = (TextView) findViewById(R.id.qlty_text);//空气质量--空气质量水平
        so2Text = (TextView) findViewById(R.id.so2_text); //空气质量--二氧化硫指数

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            // 无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        //设置下拉刷新监听器
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        //请求新选择城市的天气信息
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            //如果有缓存数据就直接使用Glide来加载这张图片
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            //如果没有缓存数据就调用loadBingPic()方法去请求今日的必应背景图
            loadBingPic();
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
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            if (weather == null){
                                Toast.makeText(WeatherActivity.this, responseText, Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.info;
        String lat = weather.basic.lat;
        String lon = weather.basic.lon;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        //degreeText.setText(degree);
        //weatherInfoText.setText(weatherInfo);
        weatherlat.setText("纬度:"+lat);
        weatherlon.setText("经度:"+lon);
        //实时
        degreetext.setText(degree);
        fltext.setText(weather.now.fl+ "℃");
        weatherinfo_text.setText(weatherInfo);
        humtext.setText(weather.now.hum+ "%");
        dirtext.setText(weather.now.wind_dir);
        sctext.setText(weather.now.wind_sc+ "级");
        spdtext.setText(weather.now.wind_spd+ "m/s");
        pcpntext.setText(weather.now.pcpn+ "mm");

        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.cond_txt_d+"/"+forecast.cond_txt_n);
            maxText.setText(forecast.tmp_max);
            minText.setText(forecast.tmp_min);
            forecastLayout.addView(view);
        }
        lifestyleLayout.removeAllViews();
        for (Lifestyle life : weather.lifestyleList) {
            View view = LayoutInflater.from(this).inflate(R.layout.lifestyle_item, lifestyleLayout, false);
            TextView lifename = (TextView)view.findViewById(R.id.life_name);
            TextView lifebrf = (TextView)view.findViewById(R.id.life_brf);
            TextView lifetxt = (TextView)view.findViewById(R.id.life_txt);
            String temp = null;
            switch (life.type){
                case "comf":
                    temp = "舒适度：";
                    break;
                case "drsg":
                    temp = "穿衣指数：";
                    break;
                case "flu":
                    temp = "感冒指数：";
                    break;
                case "sport":
                    temp = "运动建议：";
                    break;
                case "trav":
                    temp = "旅行指数：";
                    break;
                case "uv":
                    temp = "紫外线指数：";
                    break;
                case "cw":
                    temp = "洗车指数：";
                    break;
                case "air":
                    temp ="空气质量指数：";
                    break;
            }
            lifename.setText(temp);
            lifebrf.setText(life.brf);
            lifetxt.setText(life.txt);
            lifestyleLayout.addView(view);
        }

//        if(weather.aqi != null){
//            aqiText.setText(weather.aqi.city.aqi);
//            coText.setText(weather.aqi.city.co);
//            no2Text.setText(weather.aqi.city.no2);
//            o3Text.setText(weather.aqi.city.o3);
//            pm10Text.setText(weather.aqi.city.pm10);
//            pm25Text.setText(weather.aqi.city.pm25);
//            qltyText.setText(weather.aqi.city.qlty);
//            so2Text.setText(weather.aqi.city.so2);
//        }

//        if (weather.aqi != null) {
//            aqiText.setText(weather.aqi.city.aqi);
//            pm25Text.setText(weather.aqi.city.pm25);
//        }
//        String comfort = "舒适度：" + weather.lifestyle.comfort.info;
//        String carWash = "洗车指数：" + weather.lifestyle.carWash.info;
//        String sport = "运行建议：" + weather.lifestyle.sport.info;
//        comfortText.setText(comfort);
//        carWashText.setText(carWash);
//        sportText.setText(sport);
//        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

}
