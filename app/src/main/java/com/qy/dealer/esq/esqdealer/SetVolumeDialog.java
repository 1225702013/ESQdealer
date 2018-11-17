package com.qy.dealer.esq.esqdealer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class SetVolumeDialog extends DialogFragment {

    private View view;
    private MyDialogFragment_Listener myDialogFragment_Listener;
    private EditText volumeEdit;
    private EditText courseEdit;
    private EditText volumeTypeEdit;
    private EditText flightSchedulesEdit;
    private Button save;
    private Dialog meserr;
    private int flag = 0;

    // 回调接口，用于传递数据给Activity -------
    public interface MyDialogFragment_Listener {
        void getDataFrom_DialogFragment(String volume, String course, String volumeType,String flight);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            myDialogFragment_Listener = (MyDialogFragment_Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implementon MyDialogFragment_Listener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // 接收关联Activity传来的数据 -----
        view = inflater.inflate(R.layout.activity_set_volume_dialog, container);
            volumeEdit = view.findViewById(R.id.dialog_volume_edit);
            volumeTypeEdit = view.findViewById(R.id.dialog_volumeTypeEdit);
            courseEdit = view.findViewById(R.id.dialog_course_edit);
            flightSchedulesEdit = view.findViewById(R.id.flightSchedulesEdit);
            save = view.findViewById(R.id.dialog_save);
            meserr = new AlertDialog.Builder(getContext()).setTitle("错误！")
                    .setMessage("请先将信息填写完整！")
                    .setNegativeButton("知道了", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create();
            getDialog().setTitle("设置");
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (volumeEdit.length() > 0 && courseEdit.length() > 0 && volumeTypeEdit.length() > 0 && flightSchedulesEdit.length()>0) {
                        flag = 1;
                        onDestroy();
                    } else {
                        meserr.show();
                    }
                }
            });
        return view;
    }

        // DialogFragment关闭时回传数据给Activity
        @Override
        public void onDestroy () {
            // 通过接口回传数据给activity
            if (myDialogFragment_Listener != null) {
                if(flag==1){
                    Log.d("return", "onDestroy: "+courseEdit.getText().toString());
                    myDialogFragment_Listener.getDataFrom_DialogFragment( volumeEdit.getText().toString(), courseEdit.getText().toString(),volumeTypeEdit.getText().toString(),flightSchedulesEdit.getText().toString());
                }

            }
            super.onDestroy();
            dismiss();
        }
    }