package test.gcm.com.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

/**
 * Created by JungYoungHoon on 2017-04-20.
 */

public class PairedDeviceList  extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    Context context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paired_device_list);
        Intent intent = getIntent();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        context = this;
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView mPairedListView = (ListView) findViewById(R.id.paired_devices);
        mPairedListView.setAdapter(mPairedDevicesArrayAdapter);
        //mPairedListView.setOnItemClickListener();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();

        if (mPairedDevices.size() > 0) {
            /*findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);*/


            for (BluetoothDevice mDevice : mPairedDevices) {
                int i =1;
                mPairedDevicesArrayAdapter.add(i+".  "+mDevice.getName() + "  " + mDevice.getAddress());
                i++;
            }
        } else {
            //mNoDevices = getResources().getText(R.string.none_paired).toString();
            String mNoDevices = "None";
            mPairedDevicesArrayAdapter.add(mNoDevices);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view2);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            Intent i2 = new Intent(this.context,BufferRead.class);
            startActivity(i2);
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_bluetooth){

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mBluetoothAdapter != null)
        {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> mAdapterView, View mView, int mPosition, long mLong)
        {
            mBluetoothAdapter.cancelDiscovery();
            String mDeviceInfo = ((TextView) mView).getText().toString();
            String mDeviceAddress = mDeviceInfo.substring(mDeviceInfo.length() - 17);
            Log.e("420", "Device_Address " + mDeviceAddress);

            Bundle mBundle = new Bundle();
            mBundle.putString("DeviceAddress", mDeviceAddress);
            Intent mBackIntent = new Intent();
            mBackIntent.putExtras(mBundle);
            setResult(Activity.RESULT_OK, mBackIntent);
            finish();
        }
    };
}

