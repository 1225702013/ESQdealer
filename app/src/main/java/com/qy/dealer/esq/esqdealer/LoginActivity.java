package com.qy.dealer.esq.esqdealer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.qy.dealer.esq.esqdealer.data.Dealer;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.b.V;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.update.BmobUpdateAgent;

public class LoginActivity extends AppCompatActivity {

    private EditText phone;
    private EditText code;
    private Button get_code;
    private Button login;
    private int flag =0;
    private Dealer dealer;
    private LoadingDialog load;
    private SharedPreferences sp;
    private CheckBox auto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Bmob.initialize(this, "848cad5ba37c4fd27d8e0fcbee8e6ac9");
        sp = this.getSharedPreferences("dealerInfo",MODE_PRIVATE);
        phone = findViewById(R.id.login_phone);
        code = findViewById(R.id.login_pwd);
        get_code = findViewById(R.id.get_code);
        login = findViewById(R.id.login);
        auto = findViewById(R.id.auto_login);
        load =setLoad();
        String[] PERMISSIONS = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE" };
//检测是否有写的权限
        int permission = ContextCompat.checkSelfPermission(LoginActivity.this,
                "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 没有写的权限，去申请写的权限，会弹出对话框
            ActivityCompat.requestPermissions(LoginActivity.this, PERMISSIONS,1);
        }
        if(sp.getBoolean("auto_isCheck", false)){

            auto.setChecked(true);
            Intent login_data = new Intent(LoginActivity.this,MainActivity.class);
            login_data.putExtra("ObjectId",sp.getString("ObjectId",""));
            load.loadSuccess();
            startActivity(login_data);
            finish();
        }else {
            BmobUpdateAgent.setUpdateOnlyWifi(false);
            BmobUpdateAgent.update(LoginActivity.this);
        }
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.length()<11){
                    code.setVisibility(View.GONE);
                    get_code.setVisibility(View.GONE);
                }else{
                    get_code.setVisibility(View.VISIBLE);
                    flag = 1;
                }

            }
        });

        get_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BmobQuery<Dealer> query = new BmobQuery<>();
                query.addWhereEqualTo("Num",phone.getText().toString());
                query.findObjects(new FindListener<Dealer>() {
                    @Override
                    public void done(List<Dealer> list, BmobException e) {
                        if(list.isEmpty()){
                            Toast.makeText(LoginActivity.this,"没有找到该账号，请联系负责人添加！",Toast.LENGTH_LONG).show();
                        }else {
                            dealer = list.get(0);
                            new MyThread().start();
                            code.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                load.show();
                BmobSMS.verifySmsCode(phone.getText().toString(), code.getText().toString(), new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if(e==null){
                            if(auto.isChecked()){
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("ObjectId",dealer.getObjectId());
                                editor.putBoolean("auto_isCheck",true);
                                editor.commit();
                            }
                            /*
                             * 进入下一个页面*/
                            Intent data = new Intent(LoginActivity.this,MainActivity.class);
                            data.putExtra("ObjectId",dealer.getObjectId());
                            load.loadSuccess();
                            startActivity(data);
                            finish();


                        }else{
                            Toast.makeText(LoginActivity.this,"验证码错误！",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }

    private class  MyThread extends Thread {

        public void run() {
            BmobSMS.requestSMSCode(phone.getText().toString(), "易记方登录", new QueryListener<Integer>() {
                @Override
                public void done(Integer integer, BmobException e) {

                    if(e!=null){
                        Toast.makeText(LoginActivity.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
                    }else {
                        new CountDownTimer(60000,1000){
                            @Override
                            public void onTick(long millisUntilFinished) {
                                get_code.setClickable(false);
                                get_code.setText(millisUntilFinished / 1000 + "秒");
                                get_code.setBackground(getDrawable(R.drawable.shape_onpress));
                            }
                            @Override
                            public void onFinish() {
                                get_code.setClickable(true);
                                get_code.setText("重新发送");
                            }
                        }.start();
                    }
                }
            });
        }
    }

    private LoadingDialog setLoad(){

        LoadingDialog load = new LoadingDialog(this);
        load.setLoadingText("正在登录...");
        load.setSuccessText("登录成功");//显示加载成功时的文字
        load.setFailedText("登录失败");
        load.setLoadSpeed(LoadingDialog.Speed.SPEED_TWO);
        return load;

    }
}
