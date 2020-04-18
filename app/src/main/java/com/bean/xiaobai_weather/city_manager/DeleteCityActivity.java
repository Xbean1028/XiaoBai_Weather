package com.bean.xiaobai_weather.city_manager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.bean.xiaobai_weather.R;
import com.bean.xiaobai_weather.db.DBManager;
import com.bean.xiaobai_weather.gson.Weather;
import com.bean.xiaobai_weather.util.HttpUtil;
import com.bean.xiaobai_weather.util.IconUtils;
import com.bean.xiaobai_weather.util.Utility;
import com.bumptech.glide.Glide;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DeleteCityActivity extends AppCompatActivity implements View.OnClickListener{
    ImageView errorIv,rightIv;
    ListView deleteLv;
    List<String> mDatas;   //listview的数据源
    List<String>deleteCitys;  //表示存储了删除的城市信息
    private DeleteCityAdapter adapter;
    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_city);
        errorIv = (ImageView) findViewById(R.id.delete_iv_error);
        rightIv = (ImageView) findViewById(R.id.delete_iv_right);
        deleteLv = (ListView) findViewById(R.id.delete_lv);
        mDatas = DBManager.queryAllCityName();
        deleteCitys = new ArrayList<>();
//        设置点击监听事件
        errorIv.setOnClickListener(this);
        rightIv.setOnClickListener(this);
//        适配器的设置
        adapter = new DeleteCityAdapter(this, mDatas, deleteCitys);
        deleteLv.setAdapter(adapter);

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img3);
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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_iv_error:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示信息").setMessage("您确定要舍弃更改么？")
                        .setPositiveButton("舍弃更改", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();   //关闭当前的activity
                            }
                        });
                builder.setNegativeButton("取消",null);
                builder.create().show();
                break;
            case R.id.delete_iv_right:
                for (int i = 0; i < deleteCitys.size(); i++) {
                    String city = deleteCitys.get(i);
//                    调用删除城市的函数
                    int i1 = DBManager.deleteInfoByCity(city);
                }
//                删除成功返回上一级页面
                finish();
                break;
        }
    }
}
