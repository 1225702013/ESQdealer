package com.qy.dealer.esq.esqdealer.data;

import java.io.Serializable;
import java.util.Date;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobDate;

public class Trade extends BmobObject implements Serializable {

    private String volume;
    private String num;
    private String dealer;
    private String bossnum;
    private String course;
    private String driver;
    private String volumeType;
    private BmobDate date;
    private String count;
    private String flight_schedules;


    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getDealer() {
        return dealer;
    }

    public void setDealer(String dealer) {
        this.dealer = dealer;
    }

    public String getBoss_num() {
        return bossnum;
    }

    public void setBoss_num(String bossnum) {
        this.bossnum = bossnum;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(String volumeType) {
        this.volumeType = volumeType;
    }

    public BmobDate getDate() {
        return date;
    }

    public void setDate(BmobDate date) {
        this.date = date;
    }

    public String getFlight_schedules() {
        return flight_schedules;
    }

    public void setFlight_schedules(String flight_schedules) {
        this.flight_schedules = flight_schedules;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
