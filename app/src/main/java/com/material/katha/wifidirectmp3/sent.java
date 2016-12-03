package com.material.katha.wifidirectmp3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class sent extends Activity implements View.OnClickListener {

    public static List<Map<String,String>> files = new ArrayList<Map<String,String>>();
    public ListView lv1;
    ListAdapter la;
    //int pos,selectedItemPosition;
    dbadapter mdb = new dbadapter(this);
    //String id;
    //private Button lob;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        lv1 = (ListView)findViewById(R.id.listView);
        la = getallfiles();
        lv1.setAdapter(la);
        //registerForContextMenu(lv1);
    }

    public SimpleAdapter getallfiles()
    {

        mdb.open();
        Cursor c = mdb.getAllEntries();
        files.clear();


        if(c.getCount()>0) {
            while (c.moveToNext()) {
                Map<String, String> datum = new HashMap<String, String>(2);

                if(c.getString(2).contentEquals("Sent")) {
                    datum.put("files", c.getString(0));
                    datum.put("info", "To " + c.getString(1) + " on " + c.getString(3));
                    files.add(datum);
                }


            }
            SimpleAdapter la = new SimpleAdapter(this,files,android.R.layout.simple_list_item_2,new String[] {"files", "info"},
                    new int[] {android.R.id.text1,android.R.id.text2}){
                @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                    text.setTextColor(Color.BLACK);
                    text2.setTextColor(Color.BLACK);
                return view;
            }};
            return la;
        }
        else
            return null;
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //noinspection SimplifiableIfStatement
        if (id == R.id.delete) {
            dbadapter m = new dbadapter(getApplicationContext());
            m.open();
            m.delete();
            m.close();
            Toast.makeText(getApplicationContext(), "File history Deleted !!", Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

    }
}
