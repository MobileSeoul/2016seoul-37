package com.example.jjw.mydemo.lib.common;

import android.app.Service;
import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import com.example.jjw.mydemo.lib.inf.GPSFinderListener;
import com.example.jjw.mydemo.logger.Log;
import com.google.android.gms.identity.intents.Address;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * Created by JJW on 2016-10-05.
 */
public class GpsInfo extends Service implements LocationListener {
    private Context mContext;

    // 현재 GPS 사용유무
    boolean isGPSEnabled = false;

    // 네트워크 사용유무
    boolean isNetworkEnabled = false;

    // GPS 상태값
    boolean isGetLocation = false;

    Location location;
    double lat; // 위도
    double lon; // 경도

    // 최소 GPS 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    protected LocationManager locationManager;
    private boolean currentLocationAddress;

    //GPSFinderListener
    private GPSFinderListener gpsFinderListener;

    public GpsInfo(Context context) {
        this.mContext = context;
        getLocation();
    }

    public GpsInfo()
    {

    }

    public void setContext(Context ctx)
    {
        this.mContext = ctx;
        this.gpsFinderListener = (GPSFinderListener)ctx;

    }

    public void getGPSEnabled()
    {
        // GPS 정보 가져오기
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        if(isGPSEnabled){
            gpsFinderListener.onGPSFinderSuccess();
        }else{
            gpsFinderListener.onGPSFinderFailed();
        }
        //return isGPSEnabled;
    }


    public Location getLocation() {
        try {

            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // GPS 정보 가져오기
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // 현재 네트워크 상태 값 알아오기
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled || !isNetworkEnabled) {
                // GPS 와 네트워크사용이 가능하지 않을때 소스 구현
                //showSettingsAlert();
            } else {
                this.isGetLocation = true;
                // 네트워크 정보로 부터 위치값 가져오기
                if (isNetworkEnabled) {

                    try {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    } catch (SecurityException e) {
                        Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
                    }

                    if (locationManager != null) {

                        try {
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                // 위도 경도 저장
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                            }
                        } catch (SecurityException e) {
                            Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
                        }
                    }
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        try {

                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    lat = location.getLatitude();
                                    lon = location.getLongitude();
                                }
                            }
                        }catch(SecurityException e)
                        {
                            Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     * GPS 종료
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            try {
                locationManager.removeUpdates(GpsInfo.this);
            }catch(SecurityException e)
            {
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }
        }
    }

    /**
     * 위도값을 가져옵니다.
     * */
    public double getLatitude(){
        if(location != null){
            lat = location.getLatitude();
        }
        return lat;
    }

    /**
     * 경도값을 가져옵니다.
     * */
    public double getLongitude(){
        if(location != null){
            lon = location.getLongitude();
        }
        return lon;
    }

    /**
     * GPS 나 wife 정보가 켜져있는지 확인합니다.
     * */
    public boolean isGetLocation() {
        return this.isGetLocation;
    }

    /**
     * GPS 정보를 가져오지 못했을때
     * 설정값으로 갈지 물어보는 alert 창
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("GPS 사용유무셋팅");
        alertDialog.setMessage("GPS 셋팅이 되지 않았을수도 있습니다. 설정창으로 가시겠습니까?");

                // OK 를 누르게 되면 설정창으로 이동합니다.
                alertDialog.setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                mContext.startActivity(intent);
                            }
                        });
        // Cancle 하면 종료 합니다.
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    public String getGeoLocation(Context ctx, double lat, double lng){
        String str = null;
        Geocoder geocoder = new Geocoder(ctx, Locale.KOREA);

        List<android.location.Address> address;
        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(lat, lng, 1);
                if (address != null && address.size() > 0) {
                    str = address.get(0).getAddressLine(0).toString();
                }
            }
        } catch (IOException e) {
            Log.e("MainActivity", "주소를 찾지 못하였습니다.");
            e.printStackTrace();
        }


        return str;

    }


    /**
     * 주소로부터 위치정보 취득
     * @param address 주소
     */
    public Location findGeoPoint(Context ctx, String address) {
        Geocoder geocoder = new Geocoder(ctx);
        Location location = new Location("");
        List<android.location.Address> addr = null;
        try {
            addr = geocoder.getFromLocationName(address, 5);
        } catch (IOException e) {
            Log.e("GPS Info", "좌표를 찾지 못하였습니다.");
            System.out.println("--------좌표를 찾지 못함");
            e.printStackTrace();
        }

        if(addr != null){
            for(int i = 0 ; i < addr.size() ; i++){
                android.location.Address lating = addr.get(i);
                double lat = lating.getLatitude();
                double lon = lating.getLongitude();

                location.setLatitude(lat);
                location.setLongitude(lon);
            }
        }
        System.out.println("--------location : " + location);
        return location;
    }
/*
    public String findAddress(double lat, double lon)
    {
        StringBuffer bf = new StringBuffer();
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                // 세번째 인수는 최대결과값인데 하나만 리턴받도록 설정했다
                address = geocoder.getFromLocation(lat, lon, 1);
                // 설정한 데이터로 주소가 리턴된 데이터가 있으면
                if (address != null && address.size() > 0) {
                    // 주소
                    currentLocationAddress = address.get(0).getAddressLine(0).toString();

                    // 전송할 주소 데이터 (위도/경도 포함 편집)
                    bf.append(currentLocationAddress).append("#");
                    bf.append(lat).append("#");
                    bf.append(lng);
                }
            }

        } catch (IOException e) {
            Toast.makeText(this, "주소취득 실패"
                    , Toast.LENGTH_LONG).show();

            e.printStackTrace();
        }
        return bf.toString();
    }*/


    //GPS 설정 체크
    private boolean chkGpsService() {

        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        Log.d(gps, "aaaa");

        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {

            // GPS OFF 일때 Dialog 표시
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
            gsDialog.setTitle("위치 서비스 설정");
            gsDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n위치 서비스 기능을 설정하시겠습니까?");
            gsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // GPS설정 화면으로 이동
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).create().show();
            return false;

        } else {
            return true;
        }
    }
}
