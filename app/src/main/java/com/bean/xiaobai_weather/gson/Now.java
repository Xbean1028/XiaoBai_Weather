package com.bean.xiaobai_weather.gson;

import com.google.gson.annotations.SerializedName;
//
public class Now {

    @SerializedName("tmp")
    public String temperature;//温度

    @SerializedName("cond_txt")
    public String info;  //天气
    public String cond_code;  //天气

    public String fl; //体感温度
    public String wind_dir;//风向
    public String wind_sc;//风力
    public String wind_spd;//风速，公里/小时
    public String hum;//相对湿度
    public String pcpn;//降水量
    public String vis;//能见度，单位公里
    public String pres;//大气压


}
