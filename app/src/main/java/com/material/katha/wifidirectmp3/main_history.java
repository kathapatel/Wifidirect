package com.material.katha.wifidirectmp3;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

public class main_history extends ActivityGroup {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_history);

        TabHost tabHost = (TabHost)findViewById(R.id.tabHost);

        tabHost.setup(this.getLocalActivityManager());

        TabHost.TabSpec tab1 = tabHost.newTabSpec("Received");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Sent");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("All");

        // Set the Tab name and Activity
        // that will be opened when particular Tab will be selected
        tab1.setIndicator("Received");
        tab1.setContent(new Intent(this,received.class));

        tab2.setIndicator("Sent");
        tab2.setContent(new Intent(this,sent.class));

        tab3.setIndicator("All");
        tab3.setContent(new Intent(this,history.class));

        /** Add the tabs  to the TabHost to display. */
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);

        tabHost.setCurrentTab(0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete) {
            dbadapter m = new dbadapter(getApplicationContext());
            m.open();
            m.delete();
            m.close();
            Toast.makeText(getCurrentActivity(), "File history Deleted !!", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
