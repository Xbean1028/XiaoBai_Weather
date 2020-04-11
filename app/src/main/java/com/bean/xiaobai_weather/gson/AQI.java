package com.bean.xiaobai_weather.gson;

public class AQI {
//空气质量情况
    public AQICity city;

    public class AQICity {

        public String aqi;//空气质量指数

        public String co;//一氧化碳指数

        public String no2;//二氧化氮指数

        public String o3;//臭氧指数

        public String pm10;//PM10指数

        public String pm25;//PM2.5指数

        public String qlty;//空气质量（优/良/轻度污染/中度污染/重度污染/严重污染）

        public String so2;//二氧化硫指数


    }

}
