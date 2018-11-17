package com.qy.dealer.esq.esqdealer.data;
import cn.bmob.v3.BmobObject;

public class Dealer extends BmobObject {

    private String Num;
    private  String Name;
    private  String BossNum;
    public Dealer() {
    }

    public String getNum() {
        return Num;
    }

    public void setNum(String num) {
        this.Num = num;
    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getBossnum() {
        return BossNum;
    }

    public void setBossnum(String bossnum) {
        this.BossNum = bossnum;
    }
}
