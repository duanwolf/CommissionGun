package com.commissiongun;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.commissiongun.yore.commission.Admin;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    private Admin admin;
    private Handler handler = new Handler(Looper.getMainLooper());
    ArrayList<String[]> resultlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        admin = (Admin)getIntent().getSerializableExtra("Admin");
        final ListView listView = (ListView) findViewById(R.id.list);
        new Thread(new Runnable() {
            @Override
            public void run() {
                resultlist = admin.getAllSalesManInfo(0,0,"");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        initList(listView, resultlist);
                    }
                });
            }
        }).start();
    }

    private void initList(ListView listView, final ArrayList<String[]> list) {
        listView.setAdapter(new MyListAdapter(list));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ListActivity.this, PictureActivity.class);
                i.putExtra("info",list.get(position));
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
                text.setText(" id:" + info[0] + "  " + info[1] + "  销售额: " + info[2]);
            }
            return convertView;
        }
    }
}
