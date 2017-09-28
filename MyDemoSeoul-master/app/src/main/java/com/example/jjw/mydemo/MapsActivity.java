package com.example.jjw.mydemo;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.jjw.mydemo.lib.common.AppPlaceInfo;
import com.example.jjw.mydemo.lib.logon.SignInActivity;
import com.example.jjw.mydemo.lib.place.PlaceInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PlaceInfo placeInfo;
    private MenuItem mSignoutMenuItem;
    private MenuItem mSigninMenuItem;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        Intent intent = getIntent();
        placeInfo = (PlaceInfo) intent.getSerializableExtra("clickedPlace");


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initAuthentication();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        //위경도 좌표를 가져온다..
        LatLng placeLatLng = new LatLng(Double.parseDouble(placeInfo.getX_COORD()), Double.parseDouble(placeInfo.getY_COORD()));
        mMap.addMarker(new MarkerOptions().position(placeLatLng).title(placeInfo.getFAC_NAME()));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng, 18));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 18));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        mSignoutMenuItem = menu.findItem(R.id.action_signout);
        mSigninMenuItem = menu.findItem(R.id.action_signin);

        if (mAuth.getCurrentUser() != null) {

            mSigninMenuItem.setVisible(false);
            mSignoutMenuItem.setVisible(true);
        }else
        {


            mSigninMenuItem.setVisible(true);
            mSignoutMenuItem.setVisible(false);
        }


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_home:
                Intent intent = new Intent(MapsActivity.this, Main3Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

                return true;
            case R.id.action_signin:
                startActivity(new Intent(this, SignInActivity.class));

                return true;
            case R.id.action_signout:
                FirebaseAuth.getInstance().signOut();
                //UserInfo 제거
                //mAppPlaceInfo.uniqueUserInfo.signOut();
                startActivity(new Intent(this, SignInActivity.class));

                return true;
            case R.id.action_myplan:
                // startActivity(new Intent(this, PlanStsActivity.class));
                performMyPlan();
                return true;

            case R.id.action_about:
                // startActivity(new Intent(this, PlanStsActivity.class));
                ((AppPlaceInfo)getApplication()).createOkDialog(MapsActivity.this, getResources().getString(R.string.common_information),
                        getResources().getString(R.string.intro_message));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void performMyPlan() {
        //액티비티 이동..
        String content = getResources().getString(R.string.search_rst_planed_place_not_found);
        if (((AppPlaceInfo)getApplication()).uniqueUserInfo.uniquePlanedPlaceInfoList.isEmpty() && ((AppPlaceInfo)getApplication()).uniqueUserInfo.uniquePlanedSharedPlaceInfoList.isEmpty()) {
            Toast.makeText(this, content,
                    Toast.LENGTH_SHORT).show();

        } else {
            Intent intent2 = new Intent(getApplicationContext(), PlanStsActivity.class);
            startActivity(intent2);
        }
    }


    /**
     *  로그인 및 인증 절차 셋팅
     */
    public void initAuthentication()
    {
        //로그온 체크하기
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {

            if(mSigninMenuItem != null && mSignoutMenuItem != null) {
                mSigninMenuItem.setVisible(false);
                mSignoutMenuItem.setVisible(true);
            }
        }else
        {
            if(mSigninMenuItem != null && mSignoutMenuItem != null) {
                mSigninMenuItem.setVisible(true);
                mSignoutMenuItem.setVisible(false);
            }
        }
    }
}
