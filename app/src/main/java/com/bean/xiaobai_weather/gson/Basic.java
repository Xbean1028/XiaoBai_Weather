package com.bean.xiaobai_weather.gson;

import com.google.gson.annotations.SerializedName;
//城市的基本信息
public class Basic {

    @SerializedName("location")
    public String cityName;

    @SerializedName("cid")
    public String weatherId;

}
