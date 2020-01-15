/**
 * Osmdroid Sample
 * Offline Only Tiles 
 * 2019-02-01 K.OHWADA 
 */

package com.example.mapapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.api.IMapController;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.util.BoundingBox;
import java.util.List;

/**
 *  class MapActivity
 *  reference : https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/samplefragments/tileproviders/SampleOfflineOnly.java
 */
public class MapActivity extends Activity {
    private static  MapActivity instance = null;

    public static final String DIR_NAME = "osmdroid";
    private ItemizedOverlay<OverlayItem> mMyLocationOverlay;
    private MapView mMapView = null;
    private BoundingBox boundingBox = new BoundingBox();

    // debug
    private final static boolean D = true;
    private final static String TAG = "OSM";
    private final static String TAG_SUB = "MapActivity";

    // KUT
    private static double MAP_LAT = 33.620658697519;
    private static double MAP_LON = 136.71962548304;

    private static final double DEFAULT_LAT = 33.620658697519;
    private static final double DEFAULT_LON = 133.71962548304;

    private static final double LIMIT_NORTH = 35.869021;
    private static final double LIMIT_EAST = 135.000000;
    private static final double LIMIT_SOUTH = 32.233712;
    private static final double LIMIT_WEST = 132.182993;

    //setting of initial map
    private static final double MAP_ZOOM = 12.0;
    private static final double MAX_ZOOM = 17.0;
    private static final double MIN_ZOOM = 9.0;

    private boolean isCurrent = true;

    /**
     *  onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // should be called before any instances of MapView are created
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_map);
        instance = this;

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.setMultiTouchControls(true);
        mMapView.setMinZoomLevel(MIN_ZOOM);
        mMapView.setMaxZoomLevel(MAX_ZOOM);
        mMapView.setBuiltInZoomControls(false);

        //lon,  lat, lon, lat
        boundingBox.set(LIMIT_NORTH, LIMIT_EAST, LIMIT_SOUTH, LIMIT_WEST);
        mMapView.setScrollableAreaLimitDouble(boundingBox);

        final IMapController mapController =  mMapView.getController();
        mapController.setZoom(MAP_ZOOM);
        MAP_LAT = 32.233712; MAP_LON = 132.182993;
        GeoPoint centerPoint = new GeoPoint(MAP_LAT,MAP_LON);
        mapController.setCenter(centerPoint);

        Intent intent = new Intent(MapActivity.this, LoadActivity.class);
        startActivity(intent);

        setupMarker();

        findViewById(R.id.current_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCurrent){
                    mapController.setCenter(new GeoPoint(MAP_LAT, MAP_LON));
                }
                else{
                    ToastMaster.makeText(MapActivity.this, "現在地機能はご利用いただけません", Toast.LENGTH_LONG).show();
                }

            }
        });

        if(MAP_LON < LIMIT_SOUTH || LIMIT_NORTH < MAP_LON || MAP_LAT < LIMIT_WEST || LIMIT_EAST < MAP_LAT){
            log_d( "表示範囲外です");
            GeoPoint point = new GeoPoint(DEFAULT_LAT,DEFAULT_LON);
            mapController.setCenter(point);
            isCurrent = false;
        }

        if(isCurrent){
            MarkerUtil util = new MarkerUtil(this);
            ItemizedOverlay<OverlayItem> currentPin = new ItemizedIconOverlay<>(util.getCurrentPin(), null, getApplicationContext());
            this.mMapView.getOverlays().add(currentPin);
        }
    } //  onCreate


    /**
     *  onResume
     */
    @Override
    public void onResume(){
        super.onResume();
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
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
                        shelterDisp();
                    }
                });
            }

        })).start();
        return false;
    }

    private void shelterDisp(){
        MarkerUtil util = new MarkerUtil(this);
        if(mMapView.getZoomLevelDouble() > 13.0 && this.flag == 0) {
            flag = 1;
            this.mMapView.getOverlays().add(this.mMyLocationOverlay);
        }
        else if (mMapView.getZoomLevelDouble() <= 13.0 && this.flag == 1){
            flag = 0;
            this.mMapView.getOverlays().clear();
        }
        if(isCurrent){
            ItemizedOverlay<OverlayItem> currentPin = new ItemizedIconOverlay<>(util.getCurrentPin(), null, getApplicationContext());
            this.mMapView.getOverlays().add(currentPin);
        }
    }


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

    public static MapActivity getInstance(){
        if (instance == null) {
            // こんなことは起きないはず
            throw new RuntimeException("MyContext should be initialized!");
        }
        return instance;
    }

    public MapView getmMapView(){
        return this.mMapView;
    }

    public ItemizedOverlay<OverlayItem> getmMyLocationOverlay(){
        return this.mMyLocationOverlay;
    }
} // class MapActivity