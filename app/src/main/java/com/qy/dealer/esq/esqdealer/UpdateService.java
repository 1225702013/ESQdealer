package com.qy.dealer.esq.esqdealer;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.qy.dealer.esq.esqdealer.data.Trade;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import static cn.bmob.v3.b.From.e;

public class UpdateService extends Service {

    public static ArrayList<Trade> trades = new ArrayList<>();
    private int flag = 1;

    public UpdateService() {
    }




    public class Binder extends android.os.Binder {

        public void addTrades(Trade trade){
            trade.save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {
                    Log.d("Update", "done: 提示！" + e);
                    if (e == null) {
                        Toast.makeText(getApplicationContext(), trades.get(0).getDriver() + " " + trades.get(0).getNum() + "上传成功！", Toast.LENGTH_LONG).show();
                    } else {
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData mClipData = ClipData.newPlainText("Label", "错误" + e.getErrorCode() + "：" + e.getMessage());
                        cm.setPrimaryClip(mClipData);
                        Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
                        Log.d("Update", "done: 错误！" + e.getMessage());
                    }
                }
            });
            //trades.add(trade);
            //Toast.makeText(getApplicationContext(),trade.getDriver()+"成功添加到上传队列",Toast.LENGTH_LONG).show();
        }

    }

    /*class Update_Trade extends Thread {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        @Override
        public void run() {

            while (true) {
                if (trades.size() > 0) {
                    Log.d("Update", "run: " + trades.get(0).getNum());
                    trades.get(0).save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            Log.d("Update", "done: 提示！" + e);
                            if (e == null) {
                                Toast.makeText(getApplicationContext(), trades.get(0).getDriver() + " " + trades.get(0).getNum() + "上传成功！", Toast.LENGTH_LONG).show();
                                trades.remove(0);
                            } else {
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData mClipData = ClipData.newPlainText("Label", "错误" + e.getErrorCode() + "：" + e.getMessage());
                                cm.setPrimaryClip(mClipData);
                                Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
                                Log.d("Update", "done: 错误！" + e.getMessage());
                            }
                            countDownLatch.countDown();
                        }
                    });
                }
                try {
                    countDownLatch.await();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }*/


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //new Update_Trade().start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
