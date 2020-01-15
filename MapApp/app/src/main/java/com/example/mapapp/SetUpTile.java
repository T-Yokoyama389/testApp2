package com.example.mapapp;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.example.mapapp.MapActivity.DIR_NAME;

public class SetUpTile extends Thread {
    private Context context;
    private final static String TILE_FILE_NAME  = "Tyu_Sikoku2.sqlite";
    private FileOutputStream fos;
    private InputStream is;
    private int BUFFER_SIZE = 1024 * 4;

    public SetUpTile(Context context){
        this.context = context;
    }
    @Override
    public void run(){
        File main_dir = new File(String.valueOf(this.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
        File sub_dir = new File(main_dir, DIR_NAME);
        if ( !sub_dir.exists() ) {
            sub_dir.mkdir();
        }
        File file = new File(sub_dir, TILE_FILE_NAME );
        File[] list = sub_dir.listFiles();
        for(int i = 0; i < list.length; i++){
            Log.d("DEBUG", list[i].getName() + "aaaaaaaaaaaaaaa");

        }
        fos  = getFileOutput( file ) ;
        is = getAssetInputStream( TILE_FILE_NAME );

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                boolean ret = copyStream(  is,  fos );
                if (ret) {
                    toast_long("Successful");
                } else {
                    toast_long("Failed");
                }
            }
        });
    }
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
            is = this.context.getAssets().open( fileName );
        } catch (IOException e) {
            if (true) e.printStackTrace();
        }

        return is;
    } //getAssetInputStream


    /**
     * copyStream
     */
    private boolean copyStream( InputStream is, OutputStream os ) {

        byte[] buffer = new byte[BUFFER_SIZE];
        int n = 0;
        boolean is_error = false;

        try {
            // copy input to output
            while (-1 != (n = is.read(buffer))) {
                os.write(buffer, 0, n);
            } // while
        } catch (IOException e) {
            is_error = true;
            if (true) e.printStackTrace();
        }

        return ! is_error;
    }	// copyStream
    private void toast_long( String msg ) {
        ToastMaster.makeText( this.context, msg, Toast.LENGTH_LONG ).show();
    } // toast_long
}
