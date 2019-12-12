package com.example.automation;

import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public String logpathString = "";
    private List<String> list = new ArrayList<String>();
    private Spinner spinner;
    private ArrayAdapter<String> adapter;

    String info;
    int id = 1;
    int point_count = 1;

    Point point_start = new Point();
    Point point_move = new Point();
    ArrayList point_list = new ArrayList();

    Date curDate;
    Date endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get file path
        File[] files;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            files = getExternalFilesDirs(Environment.MEDIA_MOUNTED);
            for (File file : files) {
                Log.e("main", String.valueOf(file));
            }
        }

        final EditText name = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("please input file name")
                .setMessage("default: gest+number")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(name)
                .setPositiveButton("confirm", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logpathString = "/storage/emulated/0/" + name.getText().toString() + ".txt";
                    }
                })
                .setNegativeButton("default", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logpathString = "/storage/emulated/0/" + name.getText().toString() + ".txt";
                    }
                })
                .show();

        //第一步：添加一个下拉列表项的list，这里添加的项就是下拉列表的菜单项
        list.add("right_thumb");
        list.add("right_index");
        list.add("left_thumb");
        list.add("left_index");
        spinner = (Spinner) findViewById(R.id.spinner);
        //第二步：为下拉列表定义一个适配器，这里就用到里前面定义的list。
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        //第三步：为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //第四步：将适配器添加到下拉列表上
        spinner.setAdapter(adapter);
        //第五步：为下拉列表设置各种事件的响应，这个事响应菜单被选中
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                /* 将所选mySpinner 的值带入info中*/
                info = adapter.getItem(arg2);
                /* 将mySpinner 显示*/
                arg0.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*下拉菜单弹出的内容选项触屏事件处理*/
        spinner.setOnTouchListener(new Spinner.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return false;
            }
        });
        /*下拉菜单弹出的内容选项焦点改变事件处理*/
        spinner.setOnFocusChangeListener(new Spinner.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //继承了Activity的onTouchEvent方法，直接监听点击事件
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候
            curDate = new Date(System.currentTimeMillis());
            point_start.x = (int) event.getX();
            point_start.y = (int) event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //当手指移动的时候
            point_move.x = (int) event.getX();
            point_move.y = (int) event.getY();
            point_count = point_count + 1;
            point_list.add(point_move.x);
            point_list.add(point_move.y);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //当手指离开的时候
            endDate = new Date(System.currentTimeMillis());
            long diff = endDate.getTime() - curDate.getTime();
            filelog(id, info, point_count, diff, point_start, point_list);
            point_list.clear();
            id = id + 1;
            point_count = 1;
        }
        return super.onTouchEvent(event);
    }


    private void filelog(int id, String info, int point_count, long diff, Point point_start, ArrayList point_list) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(logpathString, true);
            fw.write(id + "," + info + "," + point_count + "," + diff + "," + point_start.x + "," + point_start.y);
            for (int i = 0; i < point_list.size(); i++) {
                fw.write("," + point_list.get(i));
            }
            fw.write("\r\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println(e);
        }
        System.out.println("Success");
    }
}
