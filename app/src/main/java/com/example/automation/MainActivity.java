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

    String sdcardPath = Environment.getExternalStorageDirectory().getPath() + "/";
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
                .setMessage("default: gesture")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(name)
                .setPositiveButton("confirm", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logpathString = sdcardPath + name.getText().toString() + ".txt";
                    }
                })
                .setNegativeButton("default", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logpathString = sdcardPath + "gesture.txt";
                    }
                })
                .show();

        //Step 1: Create a list for dropdown menu
        list.add("right_thumb");
        list.add("right_index");
        list.add("left_thumb");
        list.add("left_index");

        //Step 2: create a spinner and add an adapter for the dropdown menu
        spinner = (Spinner) findViewById(R.id.spinner);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);

        //Step 3: set up menu style
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Step 4: Add adapter to the dropdown menu
        spinner.setAdapter(adapter);

        //Step 5: set how they respond to audience
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                /* put option into info for log*/
                info = adapter.getItem(arg2);
                /* present what audience choose*/
                arg0.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*下拉菜单弹出的内容选项触屏事件处理*/
        spinner.setOnTouchListener(new Spinner.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        /*下拉菜单弹出的内容选项焦点改变事件处理*/
        spinner.setOnFocusChangeListener(new Spinner.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
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
