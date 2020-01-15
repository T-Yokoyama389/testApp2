package com.example.testapp2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {

    private static final String DIR_NAME = MapActivity.DIR_NAME;
    protected final static int EOF = -1;
    private final static int BUFFER_SIZE = 1024 * 4;

    private final static String TILE_FILE_NAME  = "shikoku-latest.sqlite";
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapCall(v);
            }
        });

    }

    private void mapCall(View v){
        (new Thread(new Runnable() {
            @Override
            public void run() {
                // Toast表示用にLooper を回す
                Looper.myLooper().prepare();
                // なんかのスレッド処理
                //ここで処理時間の長い処理を実行する
                setupTile();
                // Looper 回す
                Looper.myLooper().loop();
                // Looper終了
                Looper.myLooper().quit();
            }
        })).start();
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(intent);
    }

    /**
     * setupTile
     */
    private void setupTile() {

        File main_dir = new File(String.valueOf(getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
        File sub_dir = new File(main_dir, DIR_NAME);
        if ( !sub_dir.exists() ) {
            sub_dir.mkdir();
        }
        File file = new File(sub_dir, TILE_FILE_NAME );
        FileOutputStream fos  = getFileOutput( file ) ;
        InputStream is = getAssetInputStream( TILE_FILE_NAME );
        boolean ret = copyStream(  is,  fos );
        if (ret) {
            toast_long("Successful");
        } else {
            toast_long("Failed");
        }
    } // setupTile


    /**
     * getFileOutput
     */
    private FileOutputStream getFileOutput( File file ) {

        FileOutputStream fos = null;

        try{
            fos = new FileOutputStream(file, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fos;
    } // getFileOutput


    /**
     * getAssetInputStream
     */
    private InputStream getAssetInputStream( String fileName ) {

        InputStream is = null;
        try {
            is = getAssets().open( fileName );
        } catch (IOException e) {
            if (true) e.printStackTrace();
        }

        return is;
    } //getAssetInputStream


    /**
     * copyStream
     */
    private boolean copyStream(InputStream is, OutputStream os ) {

        byte[] buffer = new byte[BUFFER_SIZE];
        int n = 0;
        boolean is_error = false;

        try {
            // copy input to output
            while (EOF != (n = is.read(buffer))) {
                os.write(buffer, 0, n);
            } // while
            os.close();
        } catch (IOException e) {
            is_error = true;
            if (true) e.printStackTrace();
        }

        return ! is_error;
    }	// copyStream

    //ディレクトリの中身全部消す
    public static void delete(String path) {
        File filePath = new File(path);
        String[] list = filePath.list();
        for(String file : list) {
            File f = new File(path + File.separator + file);
            if(f.isDirectory()) {
                delete(path + File.separator + file);
            }else {
                f.delete();
            }
        }
        filePath.delete();
    }

    private void toast_long( String msg ) {
        ToastMaster.makeText( this, msg, Toast.LENGTH_LONG ).show();
    } // toast_long

}
