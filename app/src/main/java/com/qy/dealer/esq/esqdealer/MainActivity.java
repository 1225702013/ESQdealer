package com.qy.dealer.esq.esqdealer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qy.dealer.esq.esqdealer.Trade.TableActivity;
import com.qy.dealer.esq.esqdealer.android.CaptureActivity;
import com.qy.dealer.esq.esqdealer.bean.ZxingConfig;
import com.qy.dealer.esq.esqdealer.common.Constant;
import com.qy.dealer.esq.esqdealer.data.Dealer;
import com.qy.dealer.esq.esqdealer.data.Trade;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.util.Date;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.update.BmobUpdateAgent;

import static java.lang.System.currentTimeMillis;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,SetVolumeDialog.MyDialogFragment_Listener {

    private TextView name;
    private TextView num;
    private TextView bossnum;
    private Button start;
    private Button set;
    private Button volumetable;
    private String id;
    private LoadingDialog load;
    private Dealer mainDealer;
    private Trade trade;
    private int flag;
    private SharedPreferences sp;
    private TextView courseText;
    private TextView volumeTypeText;
    private TextView volumeText;
    private TextView flightText;
    public static long startDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        name = findViewById(R.id.dealerName);
        num = findViewById(R.id.dealerNum);
        bossnum = findViewById(R.id.bossNum);
        start = findViewById(R.id.start);
        set = findViewById(R.id.setvolume);
        volumetable = findViewById(R.id.update);
        courseText  = findViewById(R.id.courseText);
        volumeText  = findViewById(R.id.volumeText);
        volumeTypeText  = findViewById(R.id.volumeType);
        courseText  = findViewById(R.id.courseText);
        flightText = findViewById(R.id.flightText);
        sp = this.getSharedPreferences("SetData",MODE_PRIVATE);
        trade = new Trade();
        BmobUpdateAgent.setUpdateOnlyWifi(false);
        BmobUpdateAgent.update(MainActivity.this);
        Intent data = getIntent();
        id = data.getStringExtra("ObjectId");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    1);
        load =setLoad();
        load.show();
        BmobQuery<Dealer> query = new BmobQuery<>();
        query.getObject(id, new QueryListener<Dealer>() {
            @Override
            public void done(Dealer dealer, BmobException e) {

                if(e!=null){
                    load.loadFailed();
                    AlertDialog meserr = new AlertDialog.Builder(getApplicationContext()).setTitle("错误！")
                            .setMessage("请检查您的网络连接："+e.getMessage())
                            .setNegativeButton("知道了", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).create();
                    meserr.show();
                }else {
                    mainDealer = dealer;
                    name.setText(mainDealer.getName());
                    num.setText(mainDealer.getNum());
                    bossnum.setText(mainDealer.getBossnum());
                    if (sp.getBoolean("auto_isCheck", false)) {
                        volumeText.setText("方量：" + sp.getString("volume", ""));
                        volumeText.setVisibility(View.VISIBLE);
                        TextView courseText = findViewById(R.id.courseText);
                        courseText.setText("里程：" + sp.getString("course", ""));
                        courseText.setVisibility(View.VISIBLE);
                        TextView volumeTypeText = findViewById(R.id.volumeType);
                        volumeTypeText.setText("土方类型：" + sp.getString("volumeType", ""));
                        flightText.setText("班次：" + sp.getString("flight", "空"));
                        startDate = sp.getLong("startDate", 0);
                        trade.setCourse(sp.getString("course", ""));
                        trade.setVolume(sp.getString("volume", ""));
                        trade.setVolumeType(sp.getString("volumeType", ""));
                        trade.setFlight_schedules(sp.getString("flight", ""));
                        trade.setCount(sp.getString("count", ""));
                        flag = 1;

                    }
                    load.loadSuccess();
                }
            }
        });

        start.setOnClickListener(this);
        set.setOnClickListener(this);
        volumetable.setOnClickListener(this);



    }

    private LoadingDialog setLoad(){

        LoadingDialog load = new LoadingDialog(this);
        load.setLoadingText("正在登录...");
        load.setSuccessText("登录成功");//显示加载成功时的文字
        load.setFailedText("登录失败");
        load.setLoadSpeed(LoadingDialog.Speed.SPEED_TWO);
        return load;

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.update:
                Intent data = new Intent(MainActivity.this, TableActivity.class);
                data.putExtra("num",mainDealer.getBossnum());
                data.putExtra("name",trade.getFlight_schedules());
                startActivity(data);
                break;

            case R.id.setvolume:
                flag = 0;
                SetVolumeDialog dialog = new SetVolumeDialog();
                dialog.show(getSupportFragmentManager(),"Set");
                break;

            case R.id.start :
                if(flag == 1){
                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                    ZxingConfig config = new ZxingConfig();
                    config.setPlayBeep(false);
                    config.setShake(true);
                    config.setShowAlbum(false);
                    intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                    trade.setDealer(mainDealer.getName());
                    trade.setBoss_num(mainDealer.getBossnum());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("data",trade);
                    bundle.putSerializable("start",startDate);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else {
                    AlertDialog meserr = new AlertDialog.Builder(this).setTitle("错误！")
                            .setMessage("请先设置好方量/里程/土方类型！")
                            .setNegativeButton("知道了", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).create();
                    meserr.show();
                }

                break;
        }

    }

    @Override
    public void getDataFrom_DialogFragment(String volume, String course, String volumeType,String flight) {

        startDate = System.currentTimeMillis();
        volumeText.setText("方量："+volume);
        volumeText.setVisibility(View.VISIBLE);
        courseText.setText("里程："+course);
        courseText.setVisibility(View.VISIBLE);
        volumeTypeText.setText("土方类型："+volumeType);
        flightText.setText("班次："+flight);
        trade.setCourse(course);
        trade.setVolume(volume);
        trade.setVolumeType(volumeType);
        trade.setFlight_schedules(flight);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("volume",volume);
        editor.putString("course",course);
        editor.putString("volumeType",volumeType);
        editor.putBoolean("auto_isCheck",true);
        editor.putString("flight",flight);
        editor.putLong("start",startDate);
        editor.commit();
        flag = 1;
        }

    @Override
    protected void onDestroy() {
        if(false){
            super.onDestroy();
        }
        Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);

        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(mHomeIntent);
    }
    public void finish() {
        //super.finish();
        moveTaskToBack(true);//设置该activity永不过期，即不执行onDestroy()
    }
}

