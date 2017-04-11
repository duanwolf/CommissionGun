package com.commissiongun;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.commissiongun.yore.commission.Admin;

import java.util.ArrayList;

public class PersionInfoActivity extends AppCompatActivity {
    Admin admin;
    ListView listView;
    Handler handler = new Handler(Looper.getMainLooper());
    ArrayList<String[]> result;
    MyAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persion_info);
        listView = (ListView) findViewById(R.id.list);
        admin = (Admin) getIntent().getSerializableExtra("Admin");
        new Thread(new Runnable() {
            @Override
            public void run() {
                result = admin.getUnCheckedData();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (result.size() == 0) {
                            new AlertDialog.Builder(PersionInfoActivity.this).setMessage("没有未处理信息").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            PersionInfoActivity.this.finish();
                                        }
                                    }).create().show();
                        } else {
                            adapter = new MyAdapter(result);
                            listView.setAdapter(adapter);
                        }
                    }
                });
            }
        }).start();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(PersionInfoActivity.this).setMessage("是否确认提交信息?").
                        setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String[] info = result.get(position);
                                        final boolean is = admin.doCheck(Integer.valueOf(info[0]));
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                new AlertDialog.Builder(PersionInfoActivity.this).
                                                        setMessage(is?"提交成功":"提交失败").setPositiveButton("OK",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        }).create().show();
                                                if (is) {
                                                    adapter.removeData(position);
                                                }
                                            };
                                        });
                                    }
                                }).start();
                            }
                        }).create().show();
            }
        });
    }

    class MyAdapter extends BaseAdapter {
        private ArrayList<String[]> lists;

        public MyAdapter(ArrayList<String[]> lists) {
            super();
            this.lists = lists;
        }

        public void changeData(ArrayList<String[]> list) {
            lists = list;
        }

        public void removeData(int position) {
            lists.remove(position);
            MyAdapter.this.notifyDataSetChanged();
        }

        @Override
        public Object getItem(int position) {
            return lists.get(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String[] list = lists.get(position);
            holder.city_time.setText(list[1] + "   " + list[2]);
            holder.locks.setText("销售额为:$" + list[3]);
            holder.stocks.setText("销售额为:$" + list[4]);
            holder.barrels.setText("销售额为:$" + list[5]);
            return convertView;
        }


        @Override
        public int getCount() {
            return lists.size();
        }

        public class ViewHolder {
            public View rootView;
            public TextView city_time;
            public TextView locks;
            public TextView stocks;
            public TextView barrels;

            public ViewHolder(View rootView) {
                this.rootView = rootView;
                this.city_time = (TextView) rootView.findViewById(R.id.city_time);
                this.locks = (TextView) rootView.findViewById(R.id.locks);
                this.stocks = (TextView) rootView.findViewById(R.id.stocks);
                this.barrels = (TextView) rootView.findViewById(R.id.barrels);
            }

        }
    }

}
