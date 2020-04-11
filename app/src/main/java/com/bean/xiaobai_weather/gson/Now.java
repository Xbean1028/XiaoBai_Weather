package com.bean.xiaobai_weather.gson;

import com.google.gson.annotations.SerializedName;
//
public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond_txt")
    public String info;

//    public class More {
//
//        @SerializedName("txt")
//        public String info;
//
//    }

}
