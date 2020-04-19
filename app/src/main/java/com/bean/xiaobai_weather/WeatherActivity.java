package com.bean.xiaobai_weather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bean.xiaobai_weather.db.DBManager;
import com.bean.xiaobai_weather.util.IconUtils;
import com.bean.xiaobai_weather.util.NetworkUtil;
import com.bumptech.glide.Glide;
import com.bean.xiaobai_weather.gson.Forecast;
import com.bean.xiaobai_weather.gson.Lifestyle;
import com.bean.xiaobai_weather.gson.Weather;
import com.bean.xiaobai_weather.service.AutoUpdateService;
import com.bean.xiaobai_weather.util.HttpUtil;
import com.bean.xiaobai_weather.util.Utility;
import com.heweather.plugin.view.HeContent;
import com.heweather.plugin.view.HeWeatherConfig;
import com.heweather.plugin.view.LeftLargeView;
import com.bean.xiaobai_weather.city_manager.CityActivity;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public SwipeRefreshLayout swipeRefresh;//下拉刷新
    private ScrollView weatherLayout;//滚动视图对象
    private Button navButton;
    private TextView titleCity;
    private TextView titleUpdateTime;//基本信息--更新时间Location_Activity
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

    //高德
    //private Button locationbt;
    private String Gprovince;
    private String Gcity;
    private String Gdistrict;
    private LeftLargeView llView;

    private ImageView iconnow;
    private ImageView iconloc;

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new MyAMapLocationListener();
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    private LinearLayout forecastLayout;//线性布局对象--预报天气
    private LinearLayout lifestyleLayout;//线性布局对象--建议
    private ImageView bingPicImg;

    private String mWeatherId;
    public static final String TAG = "ContentValues";
    List<String> cityList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }//状态栏透明
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

        //高德
        init();

        //icon
        iconloc = (ImageView)findViewById(R.id.icon_loc);
        iconnow = (ImageView)findViewById(R.id.icon_now);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);//下拉刷新颜色
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);

        cityList = DBManager.queryAllCityName();//获取数据库包含的城市信息列表
        Boolean Flag_intent = false;

        if (!NetworkUtil.isNetworkAvailable(this)){
            navButton.setVisibility(View.GONE);
            iconloc.setVisibility(View.GONE);
            Toast.makeText(WeatherActivity.this, "请检查网络", Toast.LENGTH_LONG).show();
        }
        if (NetworkUtil.isNetworkAvailable(this)){
            navButton.setVisibility(View.VISIBLE);
            iconloc.setVisibility(View.VISIBLE);
        }

        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Flag_intent = false;
            Log.e(TAG,"bean falsedenzhi");
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.cityName;
            //mWeatherId = weather.basic.weatherId;
            if (!cityList.contains(mWeatherId)&&!TextUtils.isEmpty(mWeatherId)) {
                cityList.add(mWeatherId);
                DBManager.addCityInfo(mWeatherId,"");
            }
            showWeatherInfo(weather);
//                try {
//                    String location  =Gdistrict;
//                    HeWeatherConfig.init("ad1f9cb4bc114c719ab5c56a728b4220",location);//定位本地
//                    showWeatherlittle();
//                } catch (Exception e) {
//                    Log.e(TAG,Log.getStackTraceString(e));
//                }
        } else {
            // 无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_name");
            //mWeatherId = getIntent().getStringExtra("weather_id");
            Log.d(TAG, "bean weather_name"+mWeatherId);
            if (!cityList.contains(mWeatherId)&&!TextUtils.isEmpty(mWeatherId)) {
                cityList.add(mWeatherId);
                DBManager.addCityInfo(mWeatherId,"");
            }
            Log.d(TAG, "bean DBManager"+mWeatherId);
            //weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
            Log.d(TAG, "bean requestWeather"+mWeatherId);
//            try {
//                String location  =Gdistrict;
//                HeWeatherConfig.init("ad1f9cb4bc114c719ab5c56a728b4220",location);//定位本地
//                showWeatherlittle();
//            } catch (Exception e) {
//                Log.e(TAG,Log.getStackTraceString(e));
//            }
        }
        //设置下拉刷新监听器
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetworkUtil.isNetworkAvailable(WeatherActivity.this)){
                    navButton.setVisibility(View.VISIBLE);
                    iconloc.setVisibility(View.VISIBLE);
                }else {
                    navButton.setVisibility(View.GONE);
                    iconloc.setVisibility(View.GONE);
                    Toast.makeText(WeatherActivity.this, "请检查网络", Toast.LENGTH_LONG).show();
                }
                requestWeather(mWeatherId);
//                try {
//                    String location  =Gdistrict;
//                    if (Gdistrict==null){
//                        location = mWeatherId;;
//                    }
//                    HeWeatherConfig.init("ad1f9cb4bc114c719ab5c56a728b4220",location);//定位本地
//                    showWeatherlittle();
//                } catch (Exception e) {
//                    Log.e(TAG,Log.getStackTraceString(e));
//                }
            }
        });
        //本地天气
        iconloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                swipeRefresh.setRefreshing(true);
                requestWeather(Gdistrict);
            }
        });
        //请求新选择城市的天气信息
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                drawerLayout.openDrawer(GravityCompat.START);
                Log.d(TAG, "bean3"+cityList);
                Intent intent = new Intent(v.getContext(),CityActivity.class);
                startActivity(intent);
            }
        });
//        String bingPic = prefs.getString("bing_pic", null);
//        if (bingPic != null) {
//            //如果有缓存数据就直接使用Glide来加载这张图片
//            Glide.with(this).load(bingPic).into(bingPicImg);
//        } else {
//            //如果没有缓存数据就调用loadBingPic()方法去请求今日的必应背景图
//            loadBingPic();
//        }
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
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);//下拉刷新恢复
                    }
                });
            }
        });
        //loadBingPic();
    }

    /**
     * 加载必应每日一图
     */
//    private void loadBingPic() {
//        String requestBingPic = "http://guolin.tech/api/bing_pic";
//        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                final String bingPic = response.body().string();
//                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
//                editor.putString("bing_pic", bingPic);
//                editor.apply();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
//                    }
//                });
//            }
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }

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
        DateTime nowTime = DateTime.now();
        int hourOfDay = nowTime.getHourOfDay();
        if (hourOfDay > 6 && hourOfDay < 19) {
            iconnow.setImageResource(IconUtils.getDayIconDark(weather.now.cond_code));
            bingPicImg.setImageResource(IconUtils.getDayBack(weather.now.cond_code));
        } else {
            iconnow.setImageResource(IconUtils.getNightIconDark(weather.now.cond_code));
            bingPicImg.setImageResource(IconUtils.getNightBack(weather.now.cond_code));
        }

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
//            TextView infoTextd = (TextView) view.findViewById(R.id.info_text_d);
//            TextView infoTextn = (TextView) view.findViewById(R.id.info_text_n);
            ImageView icond = (ImageView)view.findViewById(R.id.info_icon_d);
            ImageView iconn = (ImageView)view.findViewById(R.id.info_icon_n);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            TextView dateweekday = (TextView) view.findViewById(R.id.date_weekday);
            String []date = new String[3];
            date = forecast.date.split("-");
            //时间转换星期
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            ParsePosition pos = new ParsePosition(0);
            Date strtodate = formatter.parse(forecast.date, pos);

            String weekday = getWeekOfDate(strtodate);
            dateweekday.setText(weekday);

            dateText.setText(date[1]+'/'+date[2]);
            //获得图
//            infoTextd.setText(forecast.cond_txt_d);
            icond.setImageResource(IconUtils.getDayIconDark(forecast.cond_code_d));
//            infoTextn.setText(forecast.cond_txt_n);
            iconn.setImageResource(IconUtils.getNightIconDark(forecast.cond_code_n));
            maxText.setText(forecast.tmp_max+ "℃");
            minText.setText(forecast.tmp_min+ "℃");
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


        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
//    private void showWeatherlittle() {
//        //左侧大布局右侧双布局控件
//        //        LeftLargeView llView = (LeftLargeView) findViewById(R.id.ll_view);
//        llView = (LeftLargeView) findViewById(R.id.ll_view);
//        llView.setOnClickListener(null);
//        llView.setEnabled(false);//不允许点击，因为我不想跳转
//        //取消默认背景
//        llView.setDefaultBack(false);
//        ////设置布局的背景圆角角度，颜色，边框宽度，边框颜色
//        //        llView.setStroke(5, Color.parseColor("#313a44"), 1, Color.BLACK);
//        //获取左侧大布局
//        LinearLayout leftLayout = llView.getLeftLayout();
//        //获取右上布局
//        leftLayout.removeAllViews();//清理一下
//        LinearLayout rightTopLayout = llView.getRightTopLayout();
//        //获取右下布局
//        rightTopLayout.removeAllViews();//清理一下
//        LinearLayout rightBottomLayout = llView.getRightBottomLayout();
//        rightBottomLayout.removeAllViews();//清理一下
//
//        //设置布局的背景圆角角度（单位：dp），颜色，边框宽度（单位：px），边框颜色
//        llView.setStroke(5, Color.parseColor("#313a44"), 1, Color.BLACK);
//
//        //添加温度描述到左侧大布局
//        //第一个参数为需要加入的布局
//        //第二个参数为文字大小，单位：sp
//        //第三个参数为文字颜色，默认白色
//        llView.addTemp(rightTopLayout, 14, Color.WHITE);//温度描述
//        //llView.addTemp(leftLayout, 40, Color.WHITE);
//        //添加温度图标到右上布局，第二个参数为图标宽高（宽高1：1，单位：dp）
//        llView.addWeatherIcon(leftLayout, 60);//温度描述
//        //llView.addWeatherIcon(rightTopLayout, 14);
//        //添加预警图标到右上布局
//        llView.addAlarmIcon(rightTopLayout, 14);
//        //添加预警描述到右上布局
//        llView.addAlarmTxt(rightTopLayout, 14);
//        //添加文字AQI到右上布局
//        llView.addAqiText(rightTopLayout, 14);
//        //添加空气质量到右上布局
//        llView.addAqiQlty(rightTopLayout, 14);
//        //添加空气质量数值到右上布局
//        llView.addAqiNum(rightTopLayout, 14);
//        //添加地址信息到右上布局
//        llView.addLocation(rightTopLayout, 14, Color.WHITE);
//        //添加天气描述到右下布局
//        llView.addCond(rightBottomLayout, 14, Color.WHITE);
//        //添加风向图标到右下布局
//        llView.addWindIcon(rightBottomLayout, 14);
//        //添加风力描述到右下布局
//        llView.addWind(rightBottomLayout, 14, Color.WHITE);
//        //添加降雨图标到右下布局
//        llView.addRainIcon(rightBottomLayout, 14);
//        //添加降雨描述到右下布局
//        llView.addRainDetail(rightBottomLayout, 14, Color.WHITE);
//        //设置控件的对齐方式，默认居中
//        llView.setViewGravity(HeContent.GRAVITY_LEFT);
//        //显示布局
//        llView.show();
//    }

    private void init() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(false);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);
        //获取一次定位结果：
        //该方法默认为false。
        //mLocationOption.setOnceLocation(true);

        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

    }

    private class MyAMapLocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    Log.e("位置：", aMapLocation.getAddress());
                    Gprovince = aMapLocation.getProvince();
                    Gcity = aMapLocation.getCity();
                    Gdistrict = aMapLocation.getDistrict();
                    //Toast.makeText(WeatherActivity.this,Gprovince+"+"+Gcity+"+"+Gdistrict,Toast.LENGTH_SHORT).show();
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    }
    public static String sHA1(Context context){
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuilder hexString = new StringBuilder();
            for (byte aPublicKey : publicKey) {
                String appendString = Integer.toHexString(0xFF & aPublicKey)
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length()-1);
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getWeekOfDate(Date dt) {
        String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        //String[] weekDays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }


}
