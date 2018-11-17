package com.qy.dealer.esq.esqdealer.Trade;
import com.bin.david.form.annotation.SmartColumn;
import com.bin.david.form.annotation.SmartTable;

@SmartTable(name = "方量明细")
public class TableInfo {
    public TableInfo(String num, String volume, String volumeType, String date,String course,String driver,String flight,String count) {
        this.num = num;
        this.volume = volume;
        this.volumeType = volumeType;
        this.date = date;
        this.course = course;
        this.driver = driver;
        this.flight = flight;
        this.count =count;
    }

    @SmartColumn(id = 1, name = "司机")
    private String driver;
    @SmartColumn(id = 4, name = "车号")
    private String num;
    @SmartColumn(id = 5, name = "方量")
    private String volume;
    @SmartColumn(id = 6, name = "方量类型")
    private String volumeType;
    @SmartColumn(id = 3, name = "时间")
    private String date;
    @SmartColumn(id = 7, name = "里程")
    private String course;
    @SmartColumn(id = 0, name = "班次",autoMerge = true)
    private String flight;
    @SmartColumn(id = 2, name = "车次")
    private String count;
}