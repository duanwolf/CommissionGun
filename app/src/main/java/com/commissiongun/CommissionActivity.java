package com.commissiongun;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.commissiongun.yore.commission.Admin;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

public class CommissionActivity extends AppCompatActivity {
    Admin admin;
    private ListView list;
    private LinearLayout container;
    private TextView barrel;
    private TextView stock;
    private TextView lock;
    private TextView sum_money;
    private Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String[] result = bundle.getStringArray("result");
            switch (msg.what) {
                case 0:
                    if (result[0].equals("0")) {
                        new AlertDialog.Builder(CommissionActivity.this).setTitle("登录失败").
                                setMessage(result[1]).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    } else {
                        Toast.makeText(CommissionActivity.this, "登录成功!", Toast.LENGTH_SHORT).show();
                        setContentView(R.layout.activity_commission);
                        initView();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final int[] result = admin.getSumOfAll();
                                final ArrayList<String[]> list = admin.getTopFiveSalesManInfo();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d("money", result[0] +"." +result[1] +"."+ result[2] + "");
                                        sum_money.setText("$"+result[0] +"");
                                        lock.setText("$"+result[1] + "");
                                        stock.setText("$"+result[2] + "");
                                        barrel.setText("$"+result[3] +"");
                                        initPie(result);
                                        initList(list);
                                    }
                                });
                            }
                        }).start();


                    }
                    break;
                case 1:
                    if (result[0].equals("0")) {
                        new AlertDialog.Builder(CommissionActivity.this).setTitle("上报失败").
                                setMessage(result[1]).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
//                        enableButton();
                    } else {
                        new AlertDialog.Builder(CommissionActivity.this).setTitle("上报成功").
                                setMessage("上报成功").setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
//                        clear();
                    }
                    break;
            }


        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commission);
        String name = getIntent().getStringExtra("username");
        String psw = getIntent().getStringExtra("password");
        admin = new Admin(name, psw);
        Log.d("Commission", "login......" + name + "," + psw);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] result = admin.login();
                Message msg = new Message();
                msg.what = 0;
                Bundle bundle = new Bundle();
                bundle.putStringArray("result", result);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }).start();
    }

    private void initView() {
        sum_money = (TextView) findViewById(R.id.sum_money);
        lock = (TextView) findViewById(R.id.lock);
        stock = (TextView) findViewById(R.id.stock);
        barrel = (TextView) findViewById(R.id.barrel);
        list = (ListView) findViewById(R.id.list);
        container = (LinearLayout) findViewById(R.id.container);
    }

    private void initPie(int[] details) {
        PieChart chart = new PieChart(CommissionActivity.this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels / 3 * 2;
        PieChart.LayoutParams params = new PieChart.LayoutParams(width, width);
        chart.setLayoutParams(params);
        chart.setHoleRadius(0);
        chart.setTransparentCircleRadius(64f);
        chart.setDrawCenterText(true);
        chart.setCenterTextSize(13f);
        Description d = new Description();
        d.setText("销量饼图");
        chart.setDescription(d);
        chart.setEntryLabelTextSize(13f);
        chart.setDrawHoleEnabled(true);
        chart.setUsePercentValues(true);
        chart.setData(getPieData(details));
        Legend legend = chart.getLegend();
        legend.setPosition(Legend.LegendPosition.ABOVE_CHART_RIGHT);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(5f);

        chart.animateXY(1000, 1000);
        container.addView(chart);
    }

    private PieData getPieData(int[] details) {
        PieData data = new PieData();
        List<PieEntry> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(new PieEntry((float)details[i+1] / details[0] * 100, i));
        }
        PieDataSet dataSet = new PieDataSet(list, "lock, stock, barrel");
        ArrayList<Integer> colors = new ArrayList<Integer>();

        // 饼图颜色
        colors.add(Color.rgb(205, 205, 205));
        colors.add(Color.rgb(114, 188, 223));
        colors.add(Color.rgb(255, 123, 124));
//        colors.add(Color.rgb(57, 135, 200));
        dataSet.setColors(colors);
        data.setDataSet(dataSet);
        return data;
    }

    private void initList(ArrayList<String[]> listStr) {
        list.setAdapter(new MyListAdapter(listStr));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(CommissionActivity.this, ListActivity.class);
                i.putExtra("Admin", admin);
                startActivity(i);
            }
        });
    }

    class MyListAdapter extends BaseAdapter {
        ArrayList<String[]> list;

        public MyListAdapter(ArrayList<String[]> list) {
            this.list = list;
        }
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item, null);
                TextView text = (TextView) convertView.findViewById(R.id.text);
                String[] info = list.get(position);
                text.setText("第"+(position + 1) + "名.  id:" + info[0] + "  " + info[1] + "  销售额: $" + info[2]);
            }
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.tosee) {
            Intent i = new Intent(CommissionActivity.this, PersionInfoActivity.class);
            i.putExtra("Admin", admin);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}
