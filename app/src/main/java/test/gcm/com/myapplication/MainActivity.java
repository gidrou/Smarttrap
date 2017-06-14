package test.gcm.com.myapplication;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    int suc = 0;
    private final int REQUEST_BLUETOOTH_ENABLE = 100;
    private final int ERROR_MEASSAGE = 1;
    private final int ERROR_MESSAGE_DATAINPUT = 10;
    private final int ERROR_MESSAGE_FILEINPUT = 11;
    private final int RESEARCH_MESSAGE = 2;
    private final int QUIT_MEASSAGE = 3;

    private Boolean Search = false;
    private  Boolean DataSuccess  = false;
    public static Context mContext;
    public static AppCompatActivity activity;
    static BluetoothDevice[] devices;
    Set<BluetoothDevice> pairedDevices;
    TextView mV_info,mV_info2;
    Button btn;
    private ProgressBar progress;
    private int DEVICE_SIZE = 0;
    static BluetoothAdapter mBluetoothAdapter;
    BluetoothSocketWrapper  mmSocket;
    BluetoothDevice mmDevice;

    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread = null;
    volatile boolean stopWorker;
    int Connect_try = 0;
    final Handler handler = new Handler(Looper.getMainLooper());
    Boolean isButton=true;
    View v;
    int read = 0;
    int count = 0;
    int c_recv_size = 0;
    int research =0;
    byte[] total_size_temp = new byte[32];
    boolean[] is_connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.content_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;
        activity=this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mV_info = (TextView) findViewById(R.id.View_info1);
        mV_info2 = (TextView) findViewById(R.id.View_info2);
        progress = (ProgressBar) findViewById(R.id.progress);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            msgDialog(1,"This device is not implement Bluetooth.");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
            return;
        }else {
            //2. 페어링 되어 있는 블루투스 장치들의 목록을 보여줍니다.
            //3. 목록에서 블루투스 장치를 선택하면 선택한 디바이스를 인자로 하여 doConnect 함수가 호출됩니다.
            pairedDevices = MainActivity.getPairedDevices();
            DEVICE_SIZE = pairedDevices.size();
            if(DEVICE_SIZE > 0){
                is_connect = new boolean[DEVICE_SIZE];
                Arrays.fill(is_connect, false);
            }

            ListPairedDevices();
            for(int i=0; i<DEVICE_SIZE; i++){
                devices = pairedDevices.toArray(new BluetoothDevice[i]);
                //Log.e("0524",""+devices[i]);
            }
            if (devices.length != 0) {
                mV_info.setText(devices[0] + "\n");
            } else {
                //디바이스가 없는 경우
                mV_info.setText("페어링 디바이스 정보가 없습니다. " + devices.length);
            }
        }




        출처: http://mainia.tistory.com/555 [녹두장군 - 상상을 현실로]
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isButton) {
                    v = view;
                    Search = true;
                    Handler h = new Handler();
                    /*Snackbar.make(view, "Connect Bluetooth...... Success!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();*/
                    h.post(new Runnable() {
                        public void run() {
                            CheckTypesTask task = new CheckTypesTask();
                            task.execute();
                        }
                    });
                    isButton = false;
                    //onloading();
                    FindBLUE();
                    btn.setText("STOP");
                } else {
                    Snackbar.make(view, "Stop", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    btn.setText("START");
                    mV_info2.setText("Stop");
                    isButton = true;
                    //onstop();
                    new CloseTask().execute();
                    insertToDatabase("1","0");
                    Log.e("Insert"," 1, 3");
                    Search=false;
                }
            }
        });

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/
        //"id", "status" id = device[list].
        insertToDatabase("1","1");
        Log.e("Insert"," 1, 1");
    }
    private void onloading() {
        progress.setVisibility(View.VISIBLE);
    }
    private void onstop(){
        progress.setVisibility(View.GONE);
    }
    private void ListPairedDevices() {
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if (mPairedDevices.size() > 0) {
        }
    }
    private class CheckTypesTask extends AsyncTask<String, String, String> {

        ProgressDialog dlg = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            dlg.setMessage("블루투스를 연결중...");
            dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {

                            Toast.makeText(getBaseContext(),
                                    "취소",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Hide",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {

                            Toast.makeText(getBaseContext(),
                                    "Hide",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            dlg.show();// show dialog
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                for (int i = 0; i < 50; i++) {
                    publishProgress("" + (int) (i * 30));
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            AlertDialog.Builder d = new AlertDialog.Builder(MainActivity.this);
            d.setTitle("종료");
            d.setMessage("종료 하시겠습니꺄?");
            d.setIcon(R.drawable.ic_menu_send);
            d.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    insertToDatabase("1","0");
                    MainActivity.this.finish();
                }
            });
            d.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    dialog.cancel();
                }
            });
            d.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            Intent i2 = new Intent(getApplicationContext(),BufferRead.class);
            startActivity(i2);
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_bluetooth){
            Intent i = new Intent(getApplicationContext(),PairedDeviceList.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onDestroy(){
        insertToDatabase("1","0");
        super.onDestroy();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_BLUETOOTH_ENABLE){
            if (resultCode == RESULT_OK){
                //BlueTooth is now Enabled
                //DeviceDialog();
            }
            if(resultCode == RESULT_CANCELED){
                //QuitDialog( "You need to enable bluetooth");
            }
        }
    }
    public static interface BluetoothSocketWrapper {
        InputStream getInputStream() throws IOException;
        OutputStream getOutputStream() throws IOException;
        String getRemoteDeviceName();
        void connect() throws IOException;
        String getRemoteDeviceAddress();
        void close() throws IOException;
        BluetoothSocket getUnderlyingSocket();
    }
    static public Set<BluetoothDevice> getPairedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }
    public static class NativeBluetoothSocket implements BluetoothSocketWrapper {
        private BluetoothSocket socket;

        public NativeBluetoothSocket(BluetoothSocket tmp) {
            this.socket = tmp;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return socket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return socket.getOutputStream();
        }

        @Override
        public String getRemoteDeviceName() {
            return socket.getRemoteDevice().getName();
        }

        @Override
        public void connect() throws IOException {
            socket.connect();
        }

        @Override
        public String getRemoteDeviceAddress() {
            return socket.getRemoteDevice().getAddress();
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }

        @Override
        public BluetoothSocket getUnderlyingSocket() {
            return socket;
        }
    }
    public void doConnect(BluetoothDevice device) {
        mmDevice = device;
        //Standard SerialPortService ID
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        try {
            // 4. 지정한 블루투스 장치에 대한 특정 UUID 서비스를 하기 위한 소켓을 생성합니다.
            // 여기선 시리얼 통신을 위한 UUID를 지정하고 있습니다.
            BluetoothSocket tmp;
            tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);  //secure 모드 경우
            mmSocket = new NativeBluetoothSocket(tmp);
            // 5. 블루투스 장치 검색을 중단합니다.
            mBluetoothAdapter.cancelDiscovery();
            // 6. ConnectTask를 시작합니다.
            new ConnectTask().execute();
        } catch (IOException e) {
            msgDialog(1,e.getMessage());
        }
    }
    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                final Handler handler = new Handler(Looper.getMainLooper());
                //7. 블루투스 장치로 연결을 시도합니다.
                mmSocket.connect();
                //8. 소켓에 대한 입출력 스트림을 가져옵니다.
                mmOutputStream = mmSocket.getOutputStream();
                mmInputStream = mmSocket.getInputStream();
                if (mmSocket != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            mV_info2.setText("SUCCESS SOCKET OPEN :: try (" + Connect_try + ")");
                            insertToDatabase("1", "2");
                            Log.e("Insert", " 1, 2");
                        }
                    });
                    Thread.sleep(3000);
                    beginBuffer();
                } else {
                    Connect_try++;
                    handler.post(new Runnable() {
                        public void run() {
                            mV_info2.setText("SOCKET might closed :: try (" + Connect_try + ")");
                            insertToDatabase("1", "5");
                            Log.e("Insert", " 1, 5");
                        }
                    });
                }
                //9. 데이터 수신을 대기하기 위한 스레드를 생성하여 입력스트림로부터의 데이터를 대기하다가
                //   들어오기 시작하면 버퍼에 저장합니다.
            } catch (Throwable t) {
                //try the fallback
                try {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    mmSocket = new FallbackBluetoothSocket(mmSocket.getUnderlyingSocket());
                    Thread.sleep(100);
                    insertToDatabase("1", "6");
                    //재접속을 시도합니다.
                    mmSocket.connect();

                    //소켓에 대한 입출력 스트림을 가져옵니다.
                    mmOutputStream = mmSocket.getOutputStream();
                    //mmInputStream = mmSocket.getInputStream();
                    if (mmSocket != null) {
                        handler.post(new Runnable() {
                            public void run() {
                                mV_info2.setText("SUCCESS SOCKET OPEN :: try (" + Connect_try + ")");
                                insertToDatabase("1", "2");
                            }
                        });
                        Thread.sleep(3000);
                        beginBuffer();
                    } else {
                        msgDialog(1, "SOECKT might be closed");
                        handler.post(new Runnable() {
                            public void run() {
                                mV_info2.setText("SOCKET might closed :: try (" + Connect_try + ")");
                                insertToDatabase("1", "5");
                            }
                        });
                    }
                    //데이터 수신을 대기하기 위한 스레드를 생성하여 입력스트림로부터의 데이터를 대기하다가
                    //들어오기 시작하면 버퍼에 저장합니다.
                    return null;
                } catch (FallbackException e1) {
                    //msgDialog(1,"트랩을 찾고있습니다.");
                    return false;
                } catch (InterruptedException e1) {
                    return false;
                } catch (IOException e1) {
                    //재접속 실패한 경우...
                    FindBLUE();
                    //msgDialog(1,"트랩을 찾고있습니다.");
                    return false;
                } finally {
                    if (mmSocket == null) {
                        FindBLUE();
                    }
                }
            } finally {
                if (mmSocket == null) {
                    FindBLUE();
                }
            }
            return true;
        }
    }
    private class CloseTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            try {
                try{mmOutputStream.close();}catch(Throwable t){/*ignore*/}
                try{mmInputStream.close();}catch(Throwable t){/*ignore*/}
                mmSocket.close();
            } catch (Throwable t) {
                insertToDatabase("1","6");
                msgDialog(1,t.getMessage());
                return t;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Throwable) {
                msgDialog(1,result.toString());
            }
        }
    }
    protected void FindBLUE() {
        for(Boolean ck : is_connect){
            int chk=0;
            if (ck == true){
                chk++;
            }
            if(chk==DEVICE_SIZE){
                Search = false;
            }
        }
        if(Search){
            ((MainActivity) MainActivity.mContext).doConnect(devices[0]);
            String s_devices = devices[0].getAddress();
            msgDialog(RESEARCH_MESSAGE, s_devices);
        }
        else{msgDialog(1,"Success");
        new CloseTask().execute();
        }
    }

    public class FallbackBluetoothSocket extends NativeBluetoothSocket {
        private BluetoothSocket fallbackSocket;

        public FallbackBluetoothSocket(BluetoothSocket tmp) throws FallbackException {
            super(tmp);
            try
            {
                Class<?> clazz = tmp.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[] {Integer.valueOf(1)};
                fallbackSocket = (BluetoothSocket) m.invoke(tmp.getRemoteDevice(), params);
            }
            catch (Exception e)
            {
                insertToDatabase("1","6");
                throw new FallbackException(e);
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return fallbackSocket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return fallbackSocket.getOutputStream();
        }

        @Override
        public void connect() throws IOException {
            fallbackSocket.connect();
        }

        @Override
        public void close() throws IOException {
            fallbackSocket.close();
        }
    }
    public static class FallbackException extends Exception {
        private static final long serialVersionUID = 1L;
        public FallbackException(Exception e) {
            super(e);
        }
    }
    void beginBuffer() {
        stopWorker = false;
        //문자열 수신 쓰레드.
        workerThread = new Thread(new Runnable() {
            List<Byte> buffer = new ArrayList<>();
            List<Byte> Filedata = new ArrayList<>();
            int TOTAL_SIZE = 9999;

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    read++;
                    try {
                        int bytesAvailable = 0;
                        mmInputStream = mmSocket.getInputStream();
                        if(mmInputStream != null){
                            bytesAvailable = mmInputStream.available();
                        }
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            c_recv_size += bytesAvailable;
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                buffer.add(packetBytes[i]);
                                count = 0;
                            }
                            TOTAL_SIZE = RECV_TOTAL_SIZE(buffer);
                            Log.e("0601","RECV_TOTAL_SIZE : " +TOTAL_SIZE);
                        }else{
                            count++;
                            if (count > 100){
                                if (c_recv_size < TOTAL_SIZE) {
                                   // Log.e("0601","File created || " +TOTAL_SIZE);
                                    new CloseTask().execute();
                                    c_recv_size = 0;
                                    count = 0;
                                    insertToDatabase("1", "7");
                                }else{
                                    if(file_detection(buffer) == 0) {
                                        new CloseTask().execute();
                                        c_recv_size = 0;
                                        count = 0;
                                        insertToDatabase("1", "7");
                                        msgDialog(ERROR_MESSAGE_FILEINPUT, "");
                                    }else{
                                        byte c;
                                        stopWorker = true;
                                        for (int i = 0; i < buffer.size(); i++) {
                                            c = buffer.get(i);
                                            Filedata.add(c);
                                        }
                                        //Log.e("0601","File created || " +TOTAL_SIZE);
                                        String storage = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp/";
                                        String Files = storage + "bufferinfo.txt";
                                        File file = new File(storage);
                                        if (!file.exists())  // 원하는 경로에 폴더가 있는지 확인
                                            file.mkdirs();
                                        FileOutputStream ost = new FileOutputStream(new File(Files));
                                        if (ost != null) {
                                            ost.write(toPrimitives(Filedata));
                                            ost.flush();
                                            ost.close();
                                        } else {
                                            new CloseTask().execute();
                                            c_recv_size = 0;
                                            count = 0;
                                        }
                                        handler.post(new Runnable() {
                                            public void run() {
                                                mV_info2.setText("Success data received :: try (" + TOTAL_SIZE + ")");
                                                c_recv_size = 0;
                                            }
                                        });
                                        DataSuccess = true;
                                        TOTAL_SIZE = 0;
                                        //is_connect[] = true;
                                        insertToDatabase("1", "3");
                                        new CloseTask().execute();
                                    }
                                }
                            }
                        }
                    } catch (IOException ex) {
                        insertToDatabase("1", "6");
                        //Log.e("Insert"," 1, 6");
                        msgDialog(ERROR_MESSAGE_FILEINPUT, ex.getMessage());
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }
    byte[] toPrimitives(List<Byte> oBytes){
        byte[] bytes = new byte[oBytes.size()];

        for(int i = 0; i < oBytes.size(); i++) {
            bytes[i] = oBytes.get(i);
        }
        return bytes;
    }
    public static int parseInt(String binary) {
        if (binary.length() < Integer.SIZE) return Integer.parseInt(binary, 2);
        int result = 0;
        byte[] bytes = binary.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 49) {
                result = result | (1 << (bytes.length - 1 - i));
            }
        }
        return result;
    }
    public int RECV_TOTAL_SIZE(List<Byte> bf) {
        int Totalsize = 9999;
        ByteBuffer by = null;
        if(bf.size()>32){
            by = ByteBuffer.allocate(32);
            for (int j = 0; j < 32; j++) {
                byte c = bf.get(j);
                by.put(c);
            }

            total_size_temp = by.array();
            String temp = bytesToString(total_size_temp);
            Totalsize = parseInt(temp);
            return Totalsize;
        }else{
            return Totalsize;
        }
    }
    public int file_detection(List<Byte> bf){
        if(bf.size()<32){
         return 0;
        }else {
            return 1;
        }
    }
    public static String bytesToString(byte[] b) {
        try {
            String s1 = new String (b,"UTF-8");
            return s1;
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public void msgDialog(int i, String e){
        final String msg = e;
        if(i==ERROR_MEASSAGE) {
            handler.post(new Runnable()
            {
                public void run()
                {
                    mV_info2.setText(msg);
                }
            });
        }
        else if(i==ERROR_MESSAGE_DATAINPUT){
            handler.post(new Runnable()
            {
                public void run()
                {
                    mV_info2.setText("DATABASE 접근 불가. " + msg);
                }
            });
        }
        else if(i==ERROR_MESSAGE_FILEINPUT){
            handler.post(new Runnable()
            {
                public void run()
                {
                    mV_info2.setText("FILEDATA 접근 불가. " + msg);
                }
            });
        }
        else if(i==RESEARCH_MESSAGE){/*
            handler.post(new Runnable()
            {
                public void run() {
                    if(research==0){
                        Snackbar.make(v, "Researching Bluetooth " + "Device : " + msg, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        research++;
                    }
                    else if (research == 1){
                        Snackbar.make(v, "Researching Bluetooth " + "Device : " + msg, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        research++;
                    }
                    else if (research == 2){
                        Snackbar.make(v, "Researching Bluetooth " + "Device : " + msg, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        research=0;
                    }
                }
            });*/
        }
        else if(i==QUIT_MEASSAGE) {
            handler.post(new Runnable()
            {
                public void run()
                {
                    mV_info2.setText(msg);
                }
            });
        }else {
            handler.post(new Runnable()
            {
                public void run()
                {
                    mV_info2.setText(msg);
                }
            });
        }
    }
    private void insertToDatabase(String id, String status){
        class InsertData extends AsyncTask<String, Void, String> {
            // ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //loading = ProgressDialog.show(MainActivity.this, "Please Wait", null, true, true);
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //loading.dismiss();
                //Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
            }
            @Override
            protected String doInBackground(String... params) {
                try {
                    String id = (String) params[0];
                    String status = (String) params[1];

                    String link = "http://220.149.235.139/test.php";
                    String data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");
                    data += "&" + URLEncoder.encode("status", "UTF-8") + "=" + URLEncoder.encode(status, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                } catch (Exception e) {
                    insertToDatabase("1","6");
                    msgDialog(ERROR_MESSAGE_DATAINPUT, "");
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        InsertData task = new InsertData();
        task.execute(id,status);
    }
}
