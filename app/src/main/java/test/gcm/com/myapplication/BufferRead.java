package test.gcm.com.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;
import java.lang.Object;
/**
 * Created by JungYoungHoon on 2017-04-20.
 */

public class BufferRead  extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Context context;
    FileInputStream fis;
    private final int SOCKET_FILENAMESIZE = 32;
    private final int SOCKET_TOTAL_SIZE = 32;
    private final int SOCKET_FILESIZE = 32;
    private final int REQ_CODE_GALLERY = 100;
    private ArrayAdapter<String> ImageAdapter;
    private String storage = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp/";
    TextView text_info;
    LinearLayout buf_layout;
    ListView ImageListView;
    ArrayList<ImageData> datas = new ArrayList<ImageData>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buffer_reader);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        buf_layout = (LinearLayout) findViewById(R.id.buffer_layout);
        context = this;

        String Files = storage + "bufferinfo.txt";
        File file = new File(storage);

        text_info = (TextView) findViewById(R.id.View_Buffer1);
        ImageListView = (ListView) findViewById(R.id.Listview_buffer);

        if (!file.exists())  // 원하는 경로에 폴더가 있는지 확인
            file.mkdirs();

        if (Files != null) {
            try {
                fis = new FileInputStream(new File(Files));
                Log.e("FILE", "Total file size to read (in bytes) : " + fis.available());

                byte[] total_size_temp = new byte[SOCKET_FILENAMESIZE];
                int i_total_size = 0;
                byte[] nameSizetemp = new byte[SOCKET_FILENAMESIZE];
                int file_namesize = 0;
                byte[] filename;
                String file_name;
                byte[] fileSizetemp = new byte[SOCKET_FILESIZE];
                int file_size = 0;
                byte[] imgfile;
                byte[] totalbuffer;

                String temp;
                ByteBuffer nameSizebuf = null, filenamebuf = null, filesizebuf = null, filebuf = null, total_size = null;

                ByteBuffer hash_md5_buf = null;
                byte[] hash_md5_byte;
                String hash_md5_string;

                FileChannel cin = fis.getChannel();
                total_size = ByteBuffer.allocate((SOCKET_TOTAL_SIZE));
                int check = 0;
                check = cin.read(total_size);
                if (check == -1) {
                } else {

                    total_size_temp = total_size.array();
                    temp = bytesToString(total_size_temp);
                    i_total_size = parseInt(temp);

                    do {
                        Log.e("FILE", "FILE CREATE START REMAIN CHANNEL : " + cin.size());

                        nameSizebuf = ByteBuffer.allocate(SOCKET_FILENAMESIZE);
                        check = cin.read(nameSizebuf);
                        if (check == -1) {
                            break;
                        }
                        nameSizetemp = nameSizebuf.array();
                        temp = bytesToString(nameSizetemp);
                        file_namesize = parseInt(temp);
                        //Log.e("FILE", "(1)FILE NAME SIZE : " + file_namesize);

                        filename = new byte[file_namesize];
                        filenamebuf = ByteBuffer.allocate(file_namesize);
                        cin.read(filenamebuf);
                        filename = filenamebuf.array();
                        file_name = new String(filename, "UTF-8");
                        //Log.e("FILE", "(2)FILE NAME SIZE : " + file_namesize + ", FILE NAME : " + file_name);

                        filesizebuf = ByteBuffer.allocate(SOCKET_FILESIZE);
                        cin.read(filesizebuf);
                        fileSizetemp = filesizebuf.array();
                        temp = bytesToString(fileSizetemp);
                        file_size = parseInt(temp);
                        //Log.e("FILE", "(3)FILE NAME SIZE : " + file_namesize + ", FILE NAME : " + file_name + ", FILE SIZE : " + file_size);

                        hash_md5_byte = new byte[32];
                        hash_md5_buf = ByteBuffer.allocate(32);
                        cin.read(hash_md5_buf);
                        hash_md5_byte = hash_md5_buf.array();
                        hash_md5_string = new String(hash_md5_byte, "UTF-8");

                        imgfile = new byte[file_size];
                        filebuf = ByteBuffer.allocate(file_size);
                        cin.read(filebuf);
                        filebuf.flip();
                        FileOutputStream fos = new FileOutputStream(new File(storage + file_name));
                        FileChannel cout = fos.getChannel();
                        cout.write(filebuf);

                        String make_hash = check_md5(file_name, hash_md5_string);
                        if (make_hash.equals(hash_md5_string)) {
                            text_info.setText("The hash value is correct.");
                            text_info.setTypeface(text_info.getTypeface(), Typeface.BOLD);
                            Bitmap bm = BitmapFactory.decodeFile(storage + file_name);
                            datas.add(new ImageData(file_name, bm,make_hash,hash_md5_string));
                        } else {
                            text_info.setText("The hash value is not correct.");
                            text_info.setTypeface(text_info.getTypeface(), Typeface.BOLD);
                            Bitmap bm = BitmapFactory.decodeFile(storage + file_name);
                            datas.add(new ImageData(file_name,bm,make_hash,hash_md5_string));
                        }
                    }

                    while (cin.size() > 0);
                    cin.close();
                    nameSizebuf.flip();
                    filenamebuf.flip();
                    filesizebuf.flip();
                    filebuf.flip();
                }
            } catch (FileNotFoundException e) {
                Log.e("420", "ERROR FILE NOT FOUND1");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("420", "ERROR FILE NOT FOUND2");
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Log.e("420", "FILE NULL");
        }

        String[] filelist = getTitleList();

        if (filelist != null) {
            for (int i = 0; i < filelist.length; i++) {
                Bitmap bm = BitmapFactory.decodeFile(storage + filelist[i]);
                //datas.add(new ImageData(filelist[i], bm));
            }
            CustomAdapter adapter = new CustomAdapter(getLayoutInflater(), datas);
            ImageListView.setAdapter(adapter);

            if (adapter != null) {

            }
        } else {
            text_info.setText("Reading Fail.");
        }


        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab3);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout3);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view3);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private String check_md5(String filename, String Recv_md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String Files = storage + filename;
            String new_md5 ="";
            int numRead = 0;
            byte[] buffer = new byte[1024];
            try {
                FileInputStream fis = new FileInputStream(new File(Files));
                if (fis != null) {
                    do {
                        numRead = fis.read(buffer);
                        if (numRead > 0) {
                            md.update(buffer, 0, numRead);
                        }
                    } while (numRead != -1);
                    fis.close();

                    byte[] mdbytes = md.digest();
                    StringBuffer sb = Convert_2_String(mdbytes);
                    new_md5 = sb.toString();
                    return  new_md5;
                } else {
                    return "Fail : File is Null";
                }
            } catch (FileNotFoundException fe) {
                fe.printStackTrace();
                return "Fail : "+fe;
            } catch (IOException e) {
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "Fail : "+e;
        }
    return "Fail";
    }

    public StringBuffer Convert_2_String(byte[] byte_md){
        StringBuffer complete = new StringBuffer();
        for (int i = 0; i < byte_md.length; i++) {
            complete.append(Integer.toString((byte_md[i] & 0xff) + 0x100, 16).substring(1));
        }
        return complete;
    }

    private String[] getTitleList()
    {
        try
        {
            FilenameFilter fileFilter = new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.endsWith("jpg");
                } //end accept
            };
            File file = new File(storage); //경로를 SD카드로 잡은거고 그 안에 있는 A폴더 입니다. 입맛에 따라 바꾸세요.
            File[] files = file.listFiles(fileFilter);//위에 만들어 두신 필터를 넣으세요. 만약 필요치 않으시면 fileFilter를 지우세요.
            String [] titleList = new String [files.length]; //파일이 있는 만큼 어레이 생성했구요
            for(int i = 0;i < files.length;i++)
            {
                titleList[i] = files[i].getName();	//루프로 돌면서 어레이에 하나씩 집어 넣습니다.
            }//end for
            return titleList;
        } catch( Exception e )
        {
            return null;
        }//end catch()
    }//end getTitleList

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout3);
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

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_bluetooth){
            Intent i = new Intent(getApplicationContext(),PairedDeviceList.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout3);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static int bytesToInt(byte[] x) {
        int value = 0;
        for(int i = 0; i < x.length; i++)
            value += ((long) x[i] & 0xffL) << (8 * i);
        return value;
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
}

