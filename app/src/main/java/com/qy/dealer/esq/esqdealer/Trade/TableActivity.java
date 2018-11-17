package com.qy.dealer.esq.esqdealer.Trade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bin.david.form.core.SmartTable;
import com.qy.dealer.esq.esqdealer.R;
import com.qy.dealer.esq.esqdealer.data.Trade;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class TableActivity extends AppCompatActivity {

    private SmartTable table;
    private String num;
    private static String[] title = { "日期","司机","车牌号","方量","里程"};
    private ArrayList<ArrayList<String>> recordList;
    private List<TableInfo> tableInfos;
    private Trade trade;
    private List<Trade> trades;
    private TextView volumetext;
    private LoadingDialog load;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        table = findViewById(R.id.table);
        volumetext = findViewById(R.id.volumetext);
        num = getIntent().getStringExtra("num");
        load = setLoad();
        load.show();
        new MyThread().start();

    }

    private LoadingDialog setLoad() {

        LoadingDialog load = new LoadingDialog(TableActivity.this);
        load.setLoadingText("正在获取数据...");
        load.setSuccessText("登录成功");//显示加载成功时的文字
        load.setFailedText("登录失败");
        load.setLoadSpeed(LoadingDialog.Speed.SPEED_TWO);
        return load;
    }


    private class  MyThread extends Thread {

        public void run() {



            BmobQuery<Trade> query = new BmobQuery<>();
            query.addWhereEqualTo("bossnum",num);
            query.addWhereEqualTo("flight_schedules",getIntent().getStringExtra("name"));
            query.order("dealer");
            query.findObjects(new FindListener<Trade>() {
                @Override
                public void done(List<Trade> list, BmobException e) {

                    if(e!=null){
                        if(e.getErrorCode()==9016)
                        {
                            Toast.makeText(TableActivity.this,"出现错误！请检查网络连接",Toast.LENGTH_LONG).show();
                            volumetext.setText("请检查网络连接");

                        }else {
                            Toast.makeText(TableActivity.this,"出现错误！"+e.getMessage(),Toast.LENGTH_LONG).show();
                            volumetext.setText(""+e.getMessage());
                        }
                        Log.d("List", "done: "+e.getMessage().toString());
                        volumetext.setVisibility(View.VISIBLE);
                        load.close();
                    }else {

                        if(list.size()<=0){
                            volumetext.setVisibility(View.VISIBLE);
                            load.close();
                        }else{
                            trades = list;
                            tableInfos = new ArrayList<>();
                            for(int i = 0;i < list.size();i++){

                                trade = list.get(i);
                                tableInfos.add(new TableInfo(trade.getNum(),trade.getVolume(),trade.getVolumeType(),trade.getDate().getDate(),trade.getCourse(),trade.getDriver(),trade.getFlight_schedules(),trade.getCount()));
                            }
                            table.setData(tableInfos);
                            load.close();


                        }
                    }
                }
            });
        }
    }


}
