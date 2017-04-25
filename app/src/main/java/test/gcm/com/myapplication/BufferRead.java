package test.gcm.com.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

/**
 * Created by JungYoungHoon on 2017-04-20.
 */

public class BufferRead  extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {

    Context context;
    FileInputStream fis;
    private final int SOCKET_FILENAMESIZE = 32;
    private final int SOCKET_FILESIZE = 32;
    private final int REQ_CODE_GALLERY =100;
    private ArrayAdapter<String> ImageAdapter;
    private String storage = Environment.getExternalStorageDirectory().getAbsolutePath()+"/temp/";
    TextView text_info;
    LinearLayout buf_layout;
    ListView ImageListView;
    ArrayList<ImageData> datas= new ArrayList<ImageData>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buffer_reader);
        Intent intent = getIntent();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        buf_layout = (LinearLayout) findViewById(R.id.buffer_layout);
        context = this;

        String Files = storage+"bufferinfo.txt";
        File file = new File(storage);

        text_info = (TextView) findViewById(R.id.View_Buffer1);
        ImageListView = (ListView)findViewById(R.id.Listview_buffer);

        if( !file.exists() )  // 원하는 경로에 폴더가 있는지 확인
            file.mkdirs();

        if(file!=null) {
            try {
                byte[] nameSizetemp = new byte[SOCKET_FILENAMESIZE];
                int file_namesize=0;
                byte[] filename;
                String file_name;
                byte[] fileSizetemp = new byte[SOCKET_FILESIZE];
                int file_size = 0;
                byte[] imgfile;
                byte[] totalbuffer;
                String temp;
                ByteBuffer nameSizebuf=null, filenamebuf = null, filesizebuf=null, filebuf=null;

                fis =  new FileInputStream(new File(Files));
               // Log.e("FILE", "Total file size to read (in bytes) : " + fis.available());
                FileChannel cin = fis.getChannel();
                int check = 0 ;
                do{
                  // Log.e("FILE","FILE CREATE START REMAIN CHANNEL : "+cin.size());
                   nameSizebuf = ByteBuffer.allocate(SOCKET_FILENAMESIZE);
                   check = cin.read(nameSizebuf);
                    if(check == -1){
                        break;
                    }
                    nameSizetemp=nameSizebuf.array();
                   temp = bytesToString(nameSizetemp);

                   file_namesize = parseInt(temp);
                   //Log.e("FILE","(1)FILE NAME SIZE : "+file_namesize);

                   filename = new byte[file_namesize];
                   filenamebuf = ByteBuffer.allocate(file_namesize);
                   cin.read(filenamebuf);
                   filename = filenamebuf.array();
                   file_name = new String(filename,"UTF-8");
                  // Log.e("FILE","(2)FILE NAME SIZE : "+file_namesize+", FILE NAME : "+file_name);

                   filesizebuf = ByteBuffer.allocate(SOCKET_FILESIZE);
                   cin.read(filesizebuf);
                   fileSizetemp=filesizebuf.array();
                   temp = bytesToString(fileSizetemp);
                   file_size = parseInt(temp);
                   //Log.e("FILE","(3)FILE NAME SIZE : "+file_namesize+", FILE NAME : "+file_name+", FILE SIZE : "+file_size);

                   imgfile = new byte[file_size];
                   filebuf = ByteBuffer.allocate(file_size);
                   cin.read(filebuf);
                   filebuf.flip();
                   FileOutputStream fos = new FileOutputStream(new File(storage + file_name));
                   FileChannel cout = fos.getChannel();
                   cout.write(filebuf);
                   //Log.e("FILE","(4)FILE NAME SIZE : "+file_namesize+", FILE NAME : "+file_name+", FILE SIZE : "+file_size+",  FILE CREATE");
               }
               while(cin.size()>0);
                cin.close();
                nameSizebuf.flip();
                filenamebuf.flip();
                filesizebuf.flip();
                filebuf.flip();;

            } catch (FileNotFoundException e) {
                Log.e("420","ERROR FILE NOT FOUND1");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("420","ERROR FILE NOT FOUND2");
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else {
            Log.e("420","FILE NULL");
        }


        String[] filelist = getTitleList();
        for(int i=0; i<filelist.length; i++){
            Bitmap bm = BitmapFactory.decodeFile(storage+filelist[i]);
            datas.add(new ImageData(filelist[i],bm));
        }
        CustomAdapter adapter = new CustomAdapter(getLayoutInflater(), datas);
        ImageListView.setAdapter(adapter);

        if(adapter!= null){
            text_info.setText("Reading success.");
            text_info.setTypeface(text_info.getTypeface(), Typeface.BOLD);
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

    private String[] getTitleList() //알아 보기 쉽게 메소드 부터 시작합니다.
    {
        try
        {
            FilenameFilter fileFilter = new FilenameFilter()  //이부분은 특정 확장자만 가지고 오고 싶을 경우 사용하시면 됩니다.
            {
                public boolean accept(File dir, String name)
                {
                    return name.endsWith("jpg"); //이 부분에 사용하고 싶은 확장자를 넣으시면 됩니다.
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

