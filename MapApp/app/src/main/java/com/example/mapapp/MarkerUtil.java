package com.example.mapapp;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import  org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;



/**
 *  class MarkerUtil
 */
public class MarkerUtil  {

    // debug
    private final static boolean D = true;
    private final static String TAG = "OSM";

    private final static String TAG_SUB = "MarkerUtil";

    private final static String FILE_NAME = "shelter.csv";

    private final static String COMMA = ",";
    private final static String SPACE = " ";

    private AssetManager mAssetManager;

    /**
     *  constractor
     */
    public MarkerUtil(Context context) {
        mAssetManager = context.getAssets();
    }


    /**
     *  getMarkers
     */
    public List<OverlayItem> getMarkers(){

        /* Create ItemizedOverlay showing a some Markers on some cities. */

        List<OverlayItem> items = new ArrayList<>();

        List<String> lines = readAsset(FILE_NAME);

        String DESC = "";
        Drawable marker = ContextCompat.getDrawable(MapActivity.getInstance(), R.drawable.icon);
        if(marker == null){
            log_d("NULLLLLLLLLLLLLLLLL");
        }
        //overlayItem.setMarker(marker);

        for(String line: lines) {
            String[] cols = line.split(COMMA);

            if (cols.length >= 13 && cols[6].equals("1")) { //6: 地震 7:津波の避難場所のみ
                String title = cols[1];
                double lat = parseDouble(cols[12]);
                double lgn = parseDouble(cols[13]);
                log_d( title + SPACE +  lat + SPACE + lgn );
                OverlayItem item = (new OverlayItem(title, DESC, new GeoPoint(lat, lgn)));
                items.add(item);
                item.setMarker(marker);
            }
        }
        return items;
    }

    public List<OverlayItem> getCurrentPin(){
        final double MAP_LAT = 33.620658697519;
        final double MAP_LON = 133.71962548304;

        List<OverlayItem> items = new ArrayList<>();
        String title = "現在地";
        String DESC = "";
        Drawable marker = ContextCompat.getDrawable(MapActivity.getInstance(), R.drawable.current_pin);
        OverlayItem item = (new OverlayItem(title, DESC, new GeoPoint(MAP_LAT, MAP_LON)));
        item.setMarker(marker);
        items.add(item);
        return items;
    }


    /**
     *  readAsset
     */
    private  List<String> readAsset(String fileName ) {

        List<String> lines = new ArrayList<String>();

        InputStream is = null;
        BufferedReader br = null;

        try {
            try {
                is = mAssetManager.open(fileName);
                br = new BufferedReader(new InputStreamReader(is, "Shift-JIS"));

                String str;
                while ((str = br.readLine()) != null) {
                    lines.add( str);
                }
            } finally {
                if (is != null) is.close();
                if (br != null) br.close();
            }
        } catch (Exception e){
            if (D) e.printStackTrace();
        }

        return lines;
    }


    /**
     * parseDouble
     */
    private  double parseDouble(String str) {
        double d = 0;
        try {
            d = Double.parseDouble(str);
        } catch (Exception e){
            if (D) e.printStackTrace();
        }
        return d;
    }

    /**
     * write into logcat
     */
    private  void log_d( String msg ) {
        if (D) Log.d( TAG, TAG_SUB + " " + msg );
    } // log_d

}