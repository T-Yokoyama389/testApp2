package com.example.mapapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.PrecomputedText;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.Set;

public class SetupOfflineMap extends Thread {
    MapActivity mapActivity = null;
    LoadActivity loadActivity = null;
    // debug
    private final static boolean D = true;
    private final static String TAG = "OSM";
    private final static String TAG_SUB = "MapActivity";
    public static final String DIR_NAME = "osmdroid";

    private MapView mMapView = null;

    private OfflineTileProvider provider;
    private ITileSource source;

    public static boolean isDisp = true;

    public SetupOfflineMap(MapView mapView, MapActivity mapActivity, LoadActivity loadActivity){
        super();
        this.mMapView = mapView;
        this.mapActivity = mapActivity;
        this.loadActivity = loadActivity;
    }

    @Override
    public void run(){
        setupOfflineMap();
        if(!isDisp){
            loadActivity.setText();
        }
        else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setInfo();
                }
            });
            loadActivity.finish();
        }
    }

    /**
     *  setupOfflineMap
     */
    private void setupOfflineMap() {

        //not even needed since we are using the offline tile provider only
        mMapView.setUseDataConnection(false);

        //mMapView.getTileProvider().setTileLoadFailureImage(getResources().getDrawable(R.drawable.notfound));

        File file = getOfflineTileFile();
        if ( file == null ){
            isDisp = false;
            return;
        }

        provider = createOfflineTileProvider(file);
        if ( provider == null ){
            isDisp = false;
            return;
        }

        source = getTilesource(provider);
        if (source == null ){
            isDisp = false;
            return;
        }
    } // setupOfflineMap

    private void setInfo(){
        mMapView.setTileProvider(provider);
        mMapView.setTileSource(source);
        mMapView.invalidate();
    }

    /**
     * getOfflineTileFile
     */
    private File  getOfflineTileFile() {

        File tileFile = null;

        String msg;

        //first we'll look at the default location for tiles that we support
        File main_dir = new File(String.valueOf(mapActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
        File sub_dir = new File(main_dir, DIR_NAME);
        String path = sub_dir.getAbsolutePath();
        if ( !sub_dir.exists()) {
            msg = path + " dir not found";
           // toast_long( msg );
            log_d( msg );
            return null;
        }
        File[] list = sub_dir.listFiles();
        if (list == null) {
            msg = path + " have no files";
            //toast_long( msg );
            log_d( msg );
            return null;
        }
        for(int i = 0; i < list.length; i++){
            log_d(i + ": " + list[i].getName());
        }
        // select first one
        for (int i = 0; i < list.length; i++) {

            String name = list[i].getName().toLowerCase();
            log_d( i + " : "+ name );

            if (list[i].isDirectory()) {
                continue;
            }
            if (!name.contains(".")) {
                continue; //skip files without an extension
            }
            name = name.substring(name.lastIndexOf(".") + 1);
            if (name.length() == 0) {
                continue;
            }
            if ( !ArchiveFileFactory.isFileExtensionRegistered(name)) {
                // not match extension : "zip" "sqlite" "mbtiles" "gemf"
                continue;
            }
            //ok found a file we support and have a driver for the format, for this demo, we'll just use the first one

            tileFile = list[i];
            break;

        } // for

        if ( tileFile == null ) {
            msg = path + " not find  Tile files in " + path;
            //toast_long( msg );
            log_d( msg );
        }

        //ToastMaster.makeText(this, tileFile.toString(), Toast.LENGTH_LONG).show();

        return tileFile;
    } // getOfflineTileFile


    /**
     * createOfflineTileProvider
     */
    private OfflineTileProvider createOfflineTileProvider(File file) {

        //create the offline tile provider, it will only do offline file archives
        OfflineTileProvider tileProvider = null;
        try {
            tileProvider = new OfflineTileProvider(new SimpleRegisterReceiver(mapActivity),
                    new File[]{file});
        } catch (Exception ex) {
            if (D) ex.printStackTrace();
        }
        return tileProvider;
    } // createOfflineTileProvider


    /**
     * getTilesource
     */
    private  ITileSource getTilesource(OfflineTileProvider tileProvider) {

        ITileSource  tileSource = null;
        IArchiveFile[] archives = tileProvider.getArchives();
        if (archives.length == 0 ) {
            log_d( "archives.length == 0" );
            return null;
        }

        //cheating a bit here, get the first archive file and ask for the tile sources names it contains
        Set<String> sourceNames = archives[0].getTileSources();
        //presumably, this would be a great place to tell your users which tiles sources are available

        if (!sourceNames.isEmpty()) {
            //ok good, we found at least one tile source, create a basic file based tile source using that name
            //and set it. If we don't set it, osmdroid will attempt to use the default source, which is "MAPNIK",
            //which probably won't match your offline tile source, unless it's MAPNIK
            String source = sourceNames.iterator().next();
            tileSource = FileBasedTileSource.getSource(source);

            String msg = " source : " + source;
            //toast_long( msg);
            log_d( msg );

        } else {
            // sourceNamess isEmpty
            // set Mapnik to tile source
            tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
            log_d( "DEFAULT_TILE_SOURCE" );
        }  // if (!sourceNamess.isEmpty()) {

        return  tileSource;
    } // getTileSource

    /**
     * write into logcat
     */
    private void log_d( String msg ) {
        if (D) Log.d( TAG, TAG_SUB + " " + msg );
    } // log_d

}
