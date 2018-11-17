package com.qy.dealer.esq.esqdealer.data;
import cn.bmob.v3.BmobObject;

public class Car extends BmobObject {

    private String num;
    private String volume;
    private  String driver;
    private  String bossnum;
    private  String CarType;
    public Car() {
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }
    public String getBossnum() {
        return bossnum;
    }

    public void setBossnum(String bossnum) {
        this.bossnum = bossnum;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getCarType() {
        return CarType;
    }

    public void setCarType(String carType) {
        CarType = carType;
    }
}
