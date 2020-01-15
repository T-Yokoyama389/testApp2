package com.example.testapp2;

/**
 * Osmdroid Sample
 * Offline Only Tiles
 * 2019-02-01 K.OHWADA
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;

import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.os.Environment;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.List;
import java.util.Set;

import static java.lang.Math.abs;

/**
 *  class MapActivity
 *  reference : https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/samplefragments/tileproviders/SampleOfflineOnly.java
 */
public class MapActivity extends AppCompatActivity{

    public static final String DIR_NAME = "osmdroid";
    private ItemizedOverlay<OverlayItem> mMyLocationOverlay;
    private LocationManager mMyLocationManager;
    private MapView mMapView = null;
    private String bestProvider;

    // debug
    private final static boolean D = true;
    private final static String TAG = "OSM";
    private final static String TAG_SUB = "MapActivity";
    private static final int REQUEST_CODE_GPS = -1;

    // KUT
    private static final double MAP_LAT = 33.620658697519;
    private static final double MAP_LON = 133.71962548304;

    //setting of initial map
    private static final double MAP_ZOOM = 12.0;
    private static final double MAX_ZOOM = 16.0;
    private static final double MIN_ZOOM = 5.0;


    /**
     *  onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Handler handler = new Handler();
        super.onCreate(savedInstanceState);

        // should be called before any instances of MapView are created
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_map);

        mMapView = (MapView) findViewById(R.id.map);

        //mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);


        IMapController mapController = mMapView.getController();
        mapController.setZoom(MAP_ZOOM);

        GeoPoint centerPoint = new GeoPoint(MAP_LAT, MAP_LON);
        mapController.setCenter(centerPoint);


        mMapView.setMaxZoomLevel(MAX_ZOOM);
        mMapView.setMinZoomLevel(MIN_ZOOM);

        (new Thread(new Runnable() {
            @Override
            public void run() {
                // Toast表示用にLooper を回す

                // なんかのスレッド処理
                Looper.myLooper().prepare();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //ここで処理時間の長い処理を実行する
                        setupOfflineMap();
                    }
                });

                // Looper 回す
                Looper.myLooper().loop();
                // Looper終了
                Looper.myLooper().quit();
            }
        })).start();

        setupMarker();
        initLocationManager();

    } //  onCreate



    @Override
    public void onStart() {
        super.onStart();
        mMyLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        locationStop();
    }

    private void initLocationManager() {
        // インスタンス生成
        mMyLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // 詳細設定
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setSpeedRequired(false);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        bestProvider = mMyLocationManager.getBestProvider(criteria, true);
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // パーミッションの許可を取得する
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        }
    }

    private void locationStart() {
        checkPermission();
        mMyLocationManager.requestLocationUpdates(bestProvider, 60000, 3, (LocationListener) this);
    }

    private void locationStop() {
        //mMyLocationManager.removeUpdates((LocationListener) this);
    }

    public void onLocationChanged(Location location) {
        Log.d("DEBUG", "called onLocationChanged");
        Log.d("DEBUG", "lat : " + location.getLatitude());
        Log.d("DEBUG", "lon : " + location.getLongitude());
    }


    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("DEBUG", "called onStatusChanged");
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.d("DEBUG", "AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d("DEBUG", "OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d("DEBUG", "TEMPORARILY_UNAVAILABLE");
                break;
            default:
                Log.d("DEBUG", "DEFAULT");
                break;
        }
    }

    public void onProviderDisabled(String provider) {
        Log.d("DEBUG", "called onProviderDisabled");
    }

    public void onProviderEnabled(String provider) {
        Log.d("DEBUG", "called onProviderEnabled");
    }

private int flag = 0;
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        final Handler handler = new Handler();
        (new Thread(new Runnable(){
            @Override
            public void run(){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //ここで処理時間の長い処理を実行する
                         shelter_disp();
                    }
                });
            }

        })).start();
        return false;
    }

    private void shelter_disp(){
        if(mMapView.getZoomLevelDouble() > 13.0 && this.flag == 0) {
            flag = 1;
            for (int i = 0; i < this.mMyLocationOverlay.size(); i++) {
                if (abs(this.mMyLocationOverlay.getItem(i).getPoint().getLatitude() - mMapView.getMapCenter().getLatitude()) > 0.1
                        && abs(this.mMyLocationOverlay.getItem(i).getPoint().getLongitude() - mMapView.getMapCenter().getLongitude()) > 0.1) {
                    this.mMapView.getOverlays().remove(this.mMyLocationOverlay.getItem(i));
                }
            }
            this.mMapView.getOverlays().add(this.mMyLocationOverlay);
        }
        else if (mMapView.getZoomLevelDouble() <= 13.0 && this.flag >= 1){
            flag = 0;
            this.mMapView.getOverlays().clear();
        }
    }

    /**
     *  onResume
     */
    @Override
    public void onResume(){
        super.onResume();
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        //mMyLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0l, 0f, this);

        if (mMapView!=null) {
            mMapView.onResume();
        }
    } // onResume


    /**
     *  onPause
     */
    @Override
    public void onPause(){
        super.onPause();
        Configuration.getInstance().save(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (mMapView!=null) {
            mMapView.onPause();
        }
    } // onPause


    /**
     *  setupOfflineMap
     */
    private void setupOfflineMap() {

        //not even needed since we are using the offline tile provider only
        mMapView.setUseDataConnection(false);

        //mMapView.getTileProvider().setTileLoadFailureImage(getResources().getDrawable(R.drawable.notfound));

        File  file = getOfflineTileFile();
        if ( file == null ) return;
        log_d(file.getName());

        OfflineTileProvider  provider = createOfflineTileProvider(file);
        if ( provider == null ) return;

        ITileSource source = getTilesource(provider);
        if (source == null ) return;

        mMapView.setTileProvider( provider );
        mMapView.setTileSource( source );
        mMapView.invalidate();

    } // setupOfflineMap



    /**
     * getOfflineTileFile
     */
    private File  getOfflineTileFile() {

        File tileFile = null;

        String msg;

        //first we'll look at the default location for tiles that we support
        File main_dir = new File(String.valueOf(getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
        File sub_dir = new File(main_dir, DIR_NAME);
        String path = sub_dir.getAbsolutePath();
        if ( !sub_dir.exists()) {
            msg = path + " dir not found";
            toast_long( msg );
            log_d( msg );
            return null;
        }
        File[] list = sub_dir.listFiles();
        if (list == null) {
            msg = path + " have no files";
            toast_long( msg );
            log_d( msg );
            return null;
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
            toast_long( msg );
            log_d( msg );
        }

        return tileFile;
    } // getOfflineTileFile


    /**
     * createOfflineTileProvider
     */
    private OfflineTileProvider createOfflineTileProvider(File file) {

        //create the offline tile provider, it will only do offline file archives
        OfflineTileProvider tileProvider = null;
        try {
            tileProvider = new OfflineTileProvider(new SimpleRegisterReceiver( this),
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
            toast_long( msg);
            log_d( msg );

        } else {
            // sourceNamess isEmpty
            // set Mapnik to tile source
            tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
            log_d( "DEFAULT_TILE_SOURCE" );
        }  // if (!sourceNamess.isEmpty()) {

        return  tileSource;
    } // getTileSource

    private void setupMarker(){


        /* Itemized Overlay */

        MarkerUtil  util = new MarkerUtil(this);
        final List<OverlayItem> items = util.getMarkers();

        /* OnTapListener for the Markers, shows a simple Toast. */

        {
            mMyLocationOverlay = new ItemizedIconOverlay<>(items,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                            toast_long(item.getTitle());

                            return true; // We 'handled' this event.
                        }

                        @Override
                        public boolean onItemLongPress(final int index, final OverlayItem item) {
                            toast_long(item.getTitle());
                            return false;
                        }
                    }, getApplicationContext());
            //this.mMapView.getOverlays().add(this.mMyLocationOverlay);
        }

        /* MiniMap */
        /*
        final MinimapOverlay miniMapOverlay = new MinimapOverlay(this,
                map.getTileRequestCompleteHandler());
        map.getOverlays().add(miniMapOverlay);

         */

    } //  setupMarker


    /**
     * toast_long
     */
    private void toast_long( String msg ) {
        ToastMaster.makeText( this, msg, Toast.LENGTH_LONG ).show();
    } // toast_long

    /**
     * write into logcat
     */
    private void log_d( String msg ) {
        if (D) Log.d( TAG, TAG_SUB + " " + msg );
    } // log_d

} // class MapActivity