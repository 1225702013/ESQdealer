package com.qy.dealer.esq.esqdealer.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.qy.dealer.esq.esqdealer.R;
import com.qy.dealer.esq.esqdealer.UpdateService;
import com.qy.dealer.esq.esqdealer.android.BeepManager;
import com.qy.dealer.esq.esqdealer.bean.ZxingConfig;
import com.qy.dealer.esq.esqdealer.camera.CameraManager;
import com.qy.dealer.esq.esqdealer.common.Constant;
import com.qy.dealer.esq.esqdealer.data.Car;
import com.qy.dealer.esq.esqdealer.data.Trade;
import com.qy.dealer.esq.esqdealer.decode.DecodeImgCallback;
import com.qy.dealer.esq.esqdealer.decode.DecodeImgThread;
import com.qy.dealer.esq.esqdealer.decode.ImageUtil;
import com.qy.dealer.esq.esqdealer.view.ViewfinderView;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;


public class CaptureActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener, ServiceConnection {

    private static final String TAG = CaptureActivity.class.getSimpleName();
    private ZxingConfig config;
    private SurfaceView preview_view;
    //private Toolbar toolbar;
    private ViewfinderView viewfinder_view;
    private AppCompatImageView flashLightIv;
    private TextView flashLightTv;
    private LinearLayout flashLightLayout;
    private LinearLayout albumLayout;
    private LinearLayoutCompat bottomLayout;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private SurfaceHolder surfaceHolder;
    private Intent data;
    private Trade trade;
    private UpdateService.Binder binder = null;
    private LoadingDialog load;
    Handler mHandler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // handler自带方法实现定时器
            try {

                /*if (handler != null)
                    mHandler.postDelayed(runnable, 6000);*/
                if (handler != null){
                    handler.restartPreviewAndDecode(); // 实现多次扫描
                }


                Log.e(TAG, "done: 再次扫描....");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("exception...");
            }
        }
    };

    public ViewfinderView getViewfinderView() {
        return viewfinder_view;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void drawViewfinder() {
        viewfinder_view.drawViewfinder();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 保持Activity处于唤醒状态
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_capture);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        /*先获取配置信息*/
        try {
            config = (ZxingConfig) getIntent().getExtras().get(Constant.INTENT_ZXING_CONFIG);
        } catch (Exception e) {
            config = new ZxingConfig();
        }
        load = setLoad();
        data = getIntent();
        //bindService(new Intent(CaptureActivity.this, UpdateService.class),this, Context.BIND_AUTO_CREATE);

        initView();


        hasSurface = false;

        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        beepManager.setPlayBeep(config.isPlayBeep());
        beepManager.setVibrate(config.isShake());


    }


    private void initView() {
        preview_view = (SurfaceView) findViewById(R.id.preview_view);
        preview_view.setOnClickListener(this);
        //toolbar = (Toolbar) findViewById(R.id.toolbar);

        viewfinder_view = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinder_view.setOnClickListener(this);
        flashLightIv = (AppCompatImageView) findViewById(R.id.flashLightIv);
        flashLightTv = (TextView) findViewById(R.id.flashLightTv);
        flashLightLayout = (LinearLayout) findViewById(R.id.flashLightLayout);
        flashLightLayout.setOnClickListener(this);
        albumLayout = (LinearLayout) findViewById(R.id.albumLayout);
        albumLayout.setOnClickListener(this);
        bottomLayout = (LinearLayoutCompat) findViewById(R.id.bottomLayout);



        switchVisibility(bottomLayout, config.isShowbottomLayout());
        switchVisibility(flashLightLayout, config.isShowFlashLight());
        switchVisibility(albumLayout, config.isShowAlbum());
//
//        toolbar.setTitle("扫一扫");
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


            /*有闪光灯就显示手电筒按钮  否则不显示*/
        if (isSupportCameraLedFlash(getPackageManager())) {
            flashLightLayout.setVisibility(View.VISIBLE);
        } else {
            flashLightLayout.setVisibility(View.GONE);
        }

    }

    /*判断设备是否支持闪光灯*/
    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                for (FeatureInfo f : features) {
                    if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name))
                        return true;
                }
            }
        }
        return false;
    }

        /*切换手电筒图片*/

    public void switchFlashImg(int flashState) {

        if (flashState == Constant.FLASH_OPEN) {
            flashLightIv.setImageResource(R.drawable.ic_open);
            flashLightTv.setText("关闭闪光灯");
        } else {
            flashLightIv.setImageResource(R.drawable.ic_close);
            flashLightTv.setText("打开闪光灯");
        }

    }

    public long fromDateStringToLong(String inVal) {
        Date date = null;
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            date = inputFormat.parse(inVal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    /**
     * 扫描成功，处理反馈信息
     *
     * @param rawResult
     */
    public void handleDecode(Result rawResult) {
        load = setLoad();
        load.show();
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();
        trade = (Trade) data.getSerializableExtra("data");
        String id = rawResult.getText();
        BmobQuery<Car> getcar = new BmobQuery<>();
        getcar.getObject(id, new QueryListener<Car>() {
            @Override
            public void done(Car car, BmobException e) {

                if(e!=null){
                    Log.d(TAG, "done: 获取车辆错误"+e.getMessage());
                    Toast.makeText(CaptureActivity.this, "获取车辆错误："+e.getMessage(),Toast.LENGTH_LONG).show();

                }else {
                    trade.setNum(car.getNum());
                    trade.setVolume(car.getVolume());
                    trade.setDate(new BmobDate(new Date(System.currentTimeMillis())));
                    trade.setDriver(car.getDriver());
                    Log.d(TAG, "done: 完成，赋值执行完毕");
                    /**
                     * 设置车次*/
                    BmobQuery<Trade> query;
                    query = new BmobQuery<>();
                    query.order("-date");
                    query.addWhereEqualTo("num",car.getNum());
                    query.addWhereEqualTo("bossnum",car.getBossnum());
                    query.findObjects(new FindListener<Trade>() {
                        @Override
                        public void done(List<Trade> list, BmobException e) {
                            if(e!=null){
                                if("java.lang.IndexOutOfBoundsException: Index: 0, Size: 0".equals(e.getMessage())){
                                    /*如果没有取到值*/
                                    Log.d(TAG, "done: 获取到时间的值为空");
                                    Toast.makeText(CaptureActivity.this, "获取到时间的值为空",Toast.LENGTH_LONG).show();
                                }else {
                                    /*输出其他错误*/
                                    Log.d(TAG, "done: 获取时间错误"+e.getMessage());
                                    Toast.makeText(CaptureActivity.this, "获取时间错误："+e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                                load.close();

                            }else {
                                /*判断是不是第一次添加*/
                                int flag =0;
                                if(list.size()==0){
                                    flag =1;
                                }else {
                                    /*
                                    * 进行时间判断*/

                                    long time = new Date(System.currentTimeMillis()).getTime()-fromDateStringToLong(list.get(0).getDate().getDate());
                                    time/=1000;
                                    Log.d(TAG, "done: Time:"+time);
                                    Toast.makeText(CaptureActivity.this, "间隔时间为"+time+"秒",Toast.LENGTH_LONG).show();
                                    if(time<300){
                                        AlertDialog meserr = new AlertDialog.Builder(CaptureActivity.this).setTitle("频繁扫描")
                                                .setMessage("该车辆距离上次扫描时间不足五分钟，无法再次扫描！\n如果频繁多次扫描，系统将上报负责人！\n还剩"+(300-time)+"秒")
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .create();
                                        meserr.show();
                                        load.close();
                                    }else {
                                        flag = 1;
                                    }
                                        
                                }
                                
                                if(flag==1){
                                    Log.d(TAG, "done: 完成，时间判断完毕");
                                    /*
                                    * 获取车次*/
                                    BmobQuery<Trade> count = new BmobQuery<>();
                                    count.addWhereEqualTo("flight_schedules",trade.getFlight_schedules());
                                    count.addWhereEqualTo("num",trade.getNum());
                                    count.count(Trade.class, new CountListener() {
                                        @Override
                                        public void done(Integer integer, BmobException e) {
                                            if(e!=null){
                                                if("java.lang.IndexOutOfBoundsException: Index: 0, Size: 0".equals(e.getMessage().toString())){
                                                    Toast.makeText(CaptureActivity.this, "获取到车次的值为空",Toast.LENGTH_LONG).show();
                                                }else{
                                                    Toast.makeText(CaptureActivity.this, "获取车次错误："+e.getMessage(),Toast.LENGTH_LONG).show();
                                                    Log.d(TAG, "done: "+e.getErrorCode()+"获取车次错误："+e.getMessage());
                                                    load.close();
                                                    Log.d(TAG, "done: 重启扫描");
                                                    mHandler.postDelayed(runnable, 4000);
                                                }
                                            }else {
                                                integer+=1;
                                                trade.setCount("第"+trade.getFlight_schedules()+"班第"+integer+"车");
                                                Log.d(TAG, "done: 完成，获取车次完毕");

                                                trade.save(new SaveListener<String>() {
                                                    @Override
                                                    public void done(String s, BmobException e) {
                                                        Log.d("Update", "done: 提示！" + e);
                                                        if(e!=null){
                                                            Log.d("Update", "done: 错误！" + e.getMessage());
                                                            Toast.makeText(CaptureActivity.this, "错误："+e.getMessage(),Toast.LENGTH_LONG).show();

                                                        }else {
                                                            Log.d("Update", "done: 上传成功");
                                                            Toast.makeText(CaptureActivity.this, trade.getDriver()+" 数据上传成功",Toast.LENGTH_LONG).show();
                                                        }
                                                        load.close();
                                                        Log.d(TAG, "done: 重启扫描");
                                                        mHandler.postDelayed(runnable, 4000);
                                                    }
                                                });

                                            }
                                        }
                                    });
                                }else {
                                    Log.d(TAG, "done: 重启扫描");
                                    load.close();
                                    mHandler.postDelayed(runnable, 4000);
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private LoadingDialog setLoad(){

        LoadingDialog load = new LoadingDialog(this);
        load.setLoadingText("正在上传...");
        load.setSuccessText("登录成功");//显示加载成功时的文字
        load.setFailedText("上传失败");
        load.setLoadSpeed(LoadingDialog.Speed.SPEED_TWO);
        return load;

    }

    /*切换view的显示*/
    private void switchVisibility(View view, boolean b) {
        if (b) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        cameraManager = new CameraManager(getApplication());

        viewfinder_view.setCameraManager(cameraManager);
        handler = null;

        surfaceHolder = preview_view.getHolder();
        if (hasSurface) {

            initCamera(surfaceHolder);
        } else {
            // 重置callback，等待surfaceCreated()来初始化camera
            surfaceHolder.addCallback(this);
        }

        beepManager.updatePrefs();
        inactivityTimer.onResume();

    }

    /**
     * 初始化Camera
     *
     * @param surfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder);
            // 创建一个handler来打开预览，并抛出一个运行时异常
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    /**
     * 显示错误信息
     */
    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("扫一扫");
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();

        if (!hasSurface) {

            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    /*@Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }*/

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    /*点击事件*/
    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.flashLightLayout) {
            /*切换闪光灯*/
            cameraManager.switchFlashLight(handler);
        } else if (id == R.id.albumLayout) {
            /*打开相册*/
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, Constant.REQUEST_IMAGE);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.REQUEST_IMAGE && resultCode == RESULT_OK) {
            String path = ImageUtil.getImageAbsolutePath(this, data.getData());

            new DecodeImgThread(path, new DecodeImgCallback() {
                @Override
                public void onImageDecodeSuccess(Result result) {
                    Log.d(TAG, "onImageDecodeSuccess: "+result.getText());
                    handleDecode(result);
                }

                @Override
                public void onImageDecodeFailed() {
                    Toast.makeText(CaptureActivity.this, "抱歉，解析失败,换个图片试试.", Toast.LENGTH_SHORT).show();
                }
            }).run();


        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        binder = (UpdateService.Binder) iBinder;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }



}
