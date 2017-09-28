package com.example.jjw.mydemo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jjw.mydemo.lib.direction.DirectionFinder;
import com.example.jjw.mydemo.lib.direction.DirectionFinderListener;
import com.example.jjw.mydemo.lib.direction.Distance;
import com.example.jjw.mydemo.lib.direction.Route;
import com.example.jjw.mydemo.lib.common.AppPlaceInfo;
import com.example.jjw.mydemo.lib.inf.DialogCommonListener;
import com.example.jjw.mydemo.lib.inf.PlaceFinderListener;
import com.example.jjw.mydemo.lib.place.PostSharedPlace;
import com.example.jjw.mydemo.lib.place.PlaceChecker;
import com.example.jjw.mydemo.lib.place.PlaceInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *  장소 종류에 따라 다르게 동작하도록 변경함..
 */
public class SearchRst1Activity extends AppCompatActivity implements OnMapReadyCallback, DirectionFinderListener, PlaceFinderListener, ViewPager.OnPageChangeListener, View.OnClickListener, DialogCommonListener {

    //어떤 정보를 클릭했는지 확인한다.
    public static final int BASIC_PLACE = 0;
    public static final int SHARED_PLACE = 1;

    public static final int SEND_PLAN_REGISTER_REQUEST = 1;
    public static final int SEND_PLAN_SHOW_REQUEST = 2;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    final Context context = this;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private GoogleMap mMap;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    //Place and SharedPlace Marker Setting
    private List<Marker> mPlaceMarkers = new ArrayList<>();
    private List<Marker> mSharedPlaceMarkers = new ArrayList<>();

    /* Src and Dest */
    private String start;
    private String dest;
    private String mStartMarkerName;
    private String mDestMarkerName;

    private PlaceChecker mPlaceChecker;
    private AppPlaceInfo mAppPlaceInfo;

    //basic information
    private ArrayList<PlaceInfo> items;                 //전체 아이템
    public ArrayList<PlaceInfo> mShowItems;            //전체 보여지는 아이템
    //private Vector<PlaceInfo> mListShowItems;        //화면에 보여질 아이템
    private ArrayList<PlaceInfo> mVectorListShowItems;         //화면에 보여질 아이템 - 이거 사용

    //Shared Information
    private ArrayList<PlaceInfo> mSharedItems;                 //전체 아이템
    public ArrayList<PlaceInfo> mSharedShowItems;            //전체 보여지는 아이템
    //private Vector<PostSharedPlace> mSharedListShowItems;        //화면에 보여질 아이템
    private ArrayList<PlaceInfo> mVectorListSharedShowItems;         //화면에 보여질 아이템 - 이거사용

    //Tab Information
    private Tab1 tab1;
    private Tab1 tab2;

    private TextView mCount;
    private Spinner mSpinner;

    private double sAroundDistance = 0.3;

    //onCreate Check..
    private boolean mIsCreated;
    List<Route> mRoutes;

    private FloatingActionButton fab;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private boolean isFabOpen = false;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_rst1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(this);



        mRoutes = null;
        mIsCreated = true;
       init();

    }

    public void init()
    {
        // Initializing
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapShareReg);
        mapFragment.getMapAsync(this);

        //AppPlaceInfo 클래스로부터 가져오기
        mAppPlaceInfo = (AppPlaceInfo)getApplication();
        //Burndle

        Bundle initBurndle = getIntent().getExtras();
        //아래 부분 체크 필요 getExtras
        if(initBurndle != null)
        {
            this.start = initBurndle.getString("origin");
            this.dest = initBurndle.getString("destination");

            //사용자 정보에 추가하기..
            this.mStartMarkerName = initBurndle.getString("originName");
            this.mDestMarkerName =  initBurndle.getString("destinationName");
            mAppPlaceInfo.uniqueUserInfo.setRecentDestination(this.mDestMarkerName);
            mAppPlaceInfo.uniqueUserInfo.setRecentStart(this.mStartMarkerName);
        }
        System.out.println("---------origin : " + this.start + " , destination : " + this.dest);
        mPlaceChecker = new PlaceChecker();
       // mPlaceChecker.latlnToUtm(37.577895,126.978430);
        //listview footer

       // mListShowItems = new Vector<PlaceInfo>();
        mVectorListShowItems = new ArrayList<PlaceInfo>();
        FragmentManager fm = getSupportFragmentManager();

        //Count
        mCount = (TextView) this.findViewById(R.id.txtSearchRstCount);

        initTab1();
        initTab2();
        initSpinner();
        initFAB();
        clearCheckedPlace();
    }

    public void initTab1()
    {
        items = mAppPlaceInfo.uniquePlaceInfoList;      //파싱한 서울 시 정보를 가져온다.
        mShowItems = new ArrayList<PlaceInfo>();
        tab1 = new Tab1(this, this, mVectorListShowItems, 0);          //탭1 생성
    }

    public void initTab2()
    {
        mSharedItems = mAppPlaceInfo.uniqueSharedPlaceInfoList;
        mSharedShowItems = new ArrayList<PlaceInfo>();
        mVectorListSharedShowItems = new ArrayList<PlaceInfo>();

        tab2 = new Tab1(this, this, mVectorListSharedShowItems,1);      //탭2 생성
    }

    //스피너 설정 (반경 설정 도구)
    public void initSpinner()
    {
        //Spinner
        mSpinner = (Spinner)findViewById(R.id.spinnerSearchRstDistance);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.mapdistance, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        //스피너 설정하기..
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            // 스피너 즉 경로 거리를 변경 했을 때, 호출된다.
            // 1. 맵은 그대로 둔다.
            // 2.리스트 뷰만 지우고, 다시 그린다.
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String strDist = mSpinner.getItemAtPosition(position).toString();
                strDist = strDist.replace("m", "");
                double distValue  = (Double.parseDouble(strDist)) / 1000;

                //System.out.println("--------onItemSelected... str : " + (Double.parseDouble(strDist)) + ", double value : " + distValue);
                sAroundDistance = distValue;
                //mRoute가 null 인경우는 처음 액티비티가 호출된 경우이다.
                //따라서, mRoute가 null이 아닌경우, 즉 주변 경로가 검색되었던 적이 있는 경우, 리스트 뷰를 업데이트 한다.
                if (mRoutes != null) {
                    clearListShowItems();
                    processListViews(mRoutes);
                }

                if(position == BASIC_PLACE)
                {
                    //탭이 Place Info에서 거리를 변화 주면, SharedPlaceInfo 쪽에 변화를 알려준다.
                    //따라서, 탭을 옮겼을 때 새로 리프레쉬 할 수 있도록 한다.
                    tab2.isPlaceAroundDistanceChanged = true;
                }else
                {
                    // 그 반대 인 경우
                    tab1.isPlaceAroundDistanceChanged = true;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    //Floating Action Button 설정
    public void initFAB()
    {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);

        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
    }

    // 출발지와 목적지 정보를 전송한다.
    private void sendRequest() {
        String origin = this.start;
        String destination = this.dest;
       // System.out.println("----------------SendRequest-------------origin : " + origin + ", dest : " + destination);
        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectionFinderStart() {
        String title = getResources().getString(R.string.search_rst_progress_title);
        String content = getResources().getString(R.string.search_rst_progress_content_map);
        progressDialog = ProgressDialog.show(this, title,
                content, true);

        try {
            if (originMarkers != null) {
                for (Marker marker : originMarkers) {
                    marker.remove();
                }
            }

            if (destinationMarkers != null) {
                for (Marker marker : destinationMarkers) {
                    marker.remove();
                }
            }

            if (polylinePaths != null) {
                for (Polyline polyline : polylinePaths) {
                    polyline.remove();
                }
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 구글로부터 받아온 루트 정보를 처리한다..
     * @param routes
     */
    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        //구글로부터 경로를 못가지고 온경우..
        if (routes.size() == 0)
        {
            String content = getResources().getString(R.string.search_rst_direction_not_found);

            Toast.makeText(this, content,
                    Toast.LENGTH_SHORT).show();

            return;
        }

        for (Route route : routes) {
            //Check ZoomValue according to distance
            Distance distance = route.distance;
          //  System.out.println("Distance : " + distance.value);     //m 단위
            int zoomLevel = calculateZoomLevel(distance.value);

            //출발지와 목적지의 중간지점을 계산한다..
            LatLng mid = mPlaceChecker.midPoint(route.startLocation, route.endLocation);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mid, zoomLevel));
            ((TextView) findViewById(R.id.txtSearchRstDistance)).setText(route.distance.text);
            String duration = route.duration.text;
            duration = duration.replace("hour", "시간");
            duration = duration.replace("mins", "분");
            ((TextView) findViewById(R.id.txtSearchRstDuration)).setText(duration);

            //((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            // ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(mStartMarkerName)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(mDestMarkerName)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(6);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 18));
        mRoutes = routes;
        processListViews(routes);
    }

    public int calculateZoomLevel(double distance)
    {
        //1km 범위 ..
        int zoomLevel;
        switch ((int)(distance/1000)) {
            //1km 미만
            case 0 :
                zoomLevel = 15;
                break;
            //1km ~ 2Km
            case 1 :
            case 2 :
            case 3:
                zoomLevel = 14;
                break;
            case 4 :
            case 5:
            case 6:
                zoomLevel = 13;
                break;
            case 7 :
            case 8 :
            case 9:
            case 10:
                zoomLevel = 12;
                break;
            default:
                zoomLevel = 11;
                break;
        }

        return zoomLevel;
    }


    /**
     * 리스트 뷰를 업데이트 한다..
     * @param routes
     */
    public void processListViews(List<Route> routes)
    {
        //20160922 - JJW 해당 경로 지점을 체크한다.

        //Distance설정
        mPlaceChecker.setmAroundDistance(sAroundDistance);
        mPlaceChecker.setRoutes(routes);

        calculateAroundPlace();
        //나중에 이벤트 핸들러 사용..
//        tab1.refreshShowItems(mVectorShowItems);
//        tab2.refreshShowItems(mVectorSharedShowItems);
        //RefreshEventHandler.callEvent(SearchRst1Activity.class, "REFRESH_TAB1");

        try {
            int tabPosition = mViewPager.getCurrentItem();
            if(tabPosition == BASIC_PLACE) {
                //최초 무조건 기본정보 부터 표시된다..
                tab1.refreshShowItems(mShowItems);
                tab1.isListRefresh = true;
            }else {
                tab2.refreshShowItems(mSharedShowItems);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     *  주변에 보여질 장소를 계산한다.
     *
     */
    public void calculateAroundPlace()
    {
        //여기서 UI업데이트 한다..
        try {
           // System.out.println("--showAllItems Size :" + items.size());
            mPlaceChecker.setmAllItems(items);
            //mPlaceChecker.setmShowItems(mShowItems);

            //추후 스레드로 빼던가..
            //주변의 장소들을 검색하여 가져온다.. 동기화가 필요없지?? 동기화가 필요한건.. 실제로 보여질 아이템들..
            mShowItems = mPlaceChecker.calculateAroundPlace();

            //공유 장소업데이트
            mPlaceChecker.setmSharedAllItems(mSharedItems);
            // mPlaceChecker.setmSharedShowItems(mSharedShowItems);
            mSharedShowItems = mPlaceChecker.calculateAroundSharedPlace();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *  화면에 보여지는 리스트들을 모두 제거한다.
     */
    public void clearListShowItems()
    {
        //PlaceChecker 를초기화한다..
        mPlaceChecker.init();           //아마 mShowItems들도 다 클리어될듯..
        int currentPosition = mViewPager.getCurrentItem();

        clearCheckedPlace();

        if(currentPosition == BASIC_PLACE) {
            //전체 주변 경로를 초기화 한다.

            mShowItems.clear();
            mVectorListShowItems.clear();

            tab1.adapterRefresh();
        }else
        {



            mSharedShowItems.clear();
            mVectorListSharedShowItems.clear();

            tab2.adapterRefresh();
        }
    }

    public void clearCheckedPlace()
    {
        //체크 정보는 모두 없애자..
        for(int i = 0 ; i < items.size() ; i ++)
        {
            PlaceInfo info = items.get(i);
            if(info.isChecked() == true)
            {
               // System.out.println("------------------isChecked..." + info.toString());
                //이부분을 나중에 수정해야 할듯..
                info.setChecked(false);
            }
        }

        for(PlaceInfo info : items)
        {
            if(info.isChecked())
            {
                info.setChecked(false);
            }
        }
        for(PlaceInfo info : mSharedItems)
        {
            if(info.isChecked() == true){
                info.setChecked(false);
            }
        }
    }


    /**
     *  마커를 그린다.
     * @param position
     */
    public void drawMarker(int position, ArrayList<PlaceInfo> listShowPlace)
    {
        //마커 생성하기
        //1. 우선 모든 마커를 지운다.

        clearPlaceMarkers(position);
        String strCount = null;
        if(listShowPlace != null) {
            for (int i = 0; i < listShowPlace.size(); i++) {
                PlaceInfo info = listShowPlace.get(i);
                LatLng latlng = new LatLng(Double.parseDouble(info.getX_COORD()), Double.parseDouble(info.getY_COORD()));

                if (position == BASIC_PLACE) {
                    mPlaceMarkers.add(mMap.addMarker(new MarkerOptions()
                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            .title(info.getFAC_NAME())
                            .position(latlng)));
                } else {
                    mSharedPlaceMarkers.add(mMap.addMarker(new MarkerOptions()
                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                            .title(info.getFAC_NAME())
                            .position(latlng)));
                }
            }

            if(position == 0)
            {
                strCount = Integer.toString(listShowPlace.size()) + "/" + Integer.toString(mShowItems.size());
            }else {
                strCount = Integer.toString(listShowPlace.size()) + "/" + Integer.toString(mSharedShowItems.size());
            }
        }

        if(strCount == null)
        {
            mCount.setText("");
        }else
        {
            mCount.setText(strCount);
        }
    }

    public void drawMarker(int position)
    {
        //탭 1인경우..
        ArrayList<PlaceInfo> drawList = null;
        try {
            if (position == 0) {
                drawList = tab1.getAdapterItems();
            } else {
                drawList = tab2.getAdapterItems();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            return;
        }

        drawMarker(position, drawList);
    }


    /**
     * 탭의 위치에따라 마커를 제거한다.
     * 탭이 기본 장소이면, 공유장소 마커를제거한다. 그반대로 동작.
     * @param position
     */
    public void clearPlaceMarkers(int position)
    {
        try {
            if (mSharedPlaceMarkers != null) {
                for (Marker marker : mSharedPlaceMarkers) {
                    marker.remove();
                }
            }

            if (mPlaceMarkers != null) {
                for (Marker marker : mPlaceMarkers) {
                    marker.remove();
                }
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * onCreate 이후 호출된다..
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        //구글 맵 설정..

        mMap = googleMap;
        LatLng hcmus = new LatLng(37.566679, 126.978430);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 18));
        originMarkers.add(mMap.addMarker(new MarkerOptions()
                .title("TEST")
                .position(hcmus)));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //mMap.setMyLocationEnabled(true);
        sendRequest();
    }

    /**
     * 액션 메뉴 생성
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_rst1, menu);
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

    /**
     *  화면에 보여질 리스트검색 시작..
     */
    @Override
    public void onPlaceFinderStart() {
        String title = getResources().getString(R.string.search_rst_progress_title);
        String content = getResources().getString(R.string.search_rst_progress_content_place);
        if(progressDialog.isShowing() == true)
        {
            //no jobs..
        }else {
            progressDialog = ProgressDialog.show(this, title,
                    content, true);
        }
    }

    @Override
    public void onPlaceFinderSuccess(ArrayList<PlaceInfo> items) {

        if(progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }
        //drawsucess 발생시킨다..
        int currentTab = mViewPager.getCurrentItem();
        drawMarker(currentTab, items);

    }

    @Override
    public void onSharedPlaceFinderSuccess(ArrayList<PostSharedPlace> items) {
        if(progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }
        //drawsucess 발생시킨다..
        int currentTab = mViewPager.getCurrentItem();
        if(currentTab == SHARED_PLACE) drawMarker(currentTab, null);
    }


    /**
     * 장소 검색 실패.
     * 1. 장소 검색 실패는 요청한 페이지 즉, 현재 보여지는 리스트 뷰가 계산을 통해 경로 주변에 나타나는 장소의 개수보다 많을 때,즉 페이지가 없을 때 발생시킨다.
     */
    @Override
    public void onPlaceFinderFailed()
    {
        String content = getResources().getString(R.string.search_rst_list_not_found);
        if(progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }
        Toast.makeText(this, content,
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * 탭 선택시 호출
     * @param position
     */
    @Override
    public void onPageSelected(int position) {
        //화면에 보여질 아이템
        if(position == SHARED_PLACE)
        {
            //리스트뷰가 업데이트 되었는지 확인한다..
            if(tab2.isListRefresh == false)
            {
                try {
                    tab2.refreshShowItems(mSharedShowItems);
                    tab2.isListRefresh = true;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }
            }
        }

        drawMarker(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.fab:

                animateFAB();
                break;
            //여행 계획에 등록 하시겠습니까?

            case R.id.fab1:
                String title = getResources().getString(R.string.plan_registration_dialog_title);
                String content = getResources().getString(R.string.plan_registration_dialog_content);
                mAppPlaceInfo.createOkCancelDialog(context, title, content, SEND_PLAN_REGISTER_REQUEST);
               // Log.d("Raj", "Fab 1");
                break;
            //여행 계획을 보시겠습니까?
            case R.id.fab2:
                mAppPlaceInfo.createOkCancelDialog(context, getResources().getString(R.string.plan_show_dialog_title), getResources().getString(R.string.plan_show_dialog_content), SEND_PLAN_SHOW_REQUEST);
                break;
        }
    }

    public void registerPlace()
    {
        try {
            ArrayList<PlaceInfo> tmp = tab1.getAdapterItems();

            mAppPlaceInfo.uniqueUserInfo.uniquePlanedPlaceInfoList.clear();

            for (int i = 0; i < tmp.size(); i++) {
                PlaceInfo info = tmp.get(i);
                if (info.isChecked() == true) {
                    mAppPlaceInfo.uniqueUserInfo.uniquePlanedPlaceInfoList.add(info);
                }
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        //Debug
       // System.out.println("------planedlist : " + mAppPlaceInfo.uniqueUserInfo.uniquePlanedPlaceInfoList);
    }

    public void registerSharedPlace()
    {
        try {
            ArrayList<PlaceInfo> tmp = tab2.getAdapterItems();
            mAppPlaceInfo.uniqueUserInfo.uniquePlanedSharedPlaceInfoList.clear();
            for (int i = 0; i < tmp.size(); i++) {
                PlaceInfo info = tmp.get(i);
                if (info.isChecked() == true) {
                    mAppPlaceInfo.uniqueUserInfo.uniquePlanedSharedPlaceInfoList.add(info);
                }
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        //Debug
       // System.out.println("------planedSharedlist : " + mAppPlaceInfo.uniqueUserInfo.uniquePlanedSharedPlaceInfoList);
    }

    public void animateFAB() {

        if (isFabOpen) {

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
           // Log.d("Raj", "close");
          //  System.out.println("--SearchRst1---close");

        } else {

            if(rotate_forward != null)
                fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
           // Log.d("Raj", "open");
         //   System.out.println("--SearchRst1---open");

        }
    }

    @Override
    public void onDialogOk(final int requestCode) {

        int tabPosition = mViewPager.getCurrentItem();
       // System.out.println("----------------Dialog Ok Clicked..-------------Tab Position : " + tabPosition);

        if(requestCode == SEND_PLAN_REGISTER_REQUEST)
        {


            registerPlace();
            registerSharedPlace();


            String content = getResources().getString(R.string.search_rst_plan_register_success);
            Toast.makeText(this, content,
                    Toast.LENGTH_SHORT).show();

        }else if(requestCode == SEND_PLAN_SHOW_REQUEST)
        {
            //액티비티 이동..
            String content = getResources().getString(R.string.search_rst_planed_place_not_found);
            if(mAppPlaceInfo.uniqueUserInfo.uniquePlanedPlaceInfoList.isEmpty() && mAppPlaceInfo.uniqueUserInfo.uniquePlanedSharedPlaceInfoList.isEmpty())
            {
                Toast.makeText(this, content,
                        Toast.LENGTH_SHORT).show();

            }else {
                Intent intent2 = new Intent(getApplicationContext(), PlanStsActivity.class);

                startActivity(intent2);
            }

        }


    }

    @Override
    public void onDialogCancel() {
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    private static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";


        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_search_rst1, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            ListView listView = (ListView)rootView.findViewById(R.id.SearchRst1ListView);
            //ListView

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter  {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            switch(position) {
                case 0:

                    return tab1;
                case 1:
                    return tab2;

            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "기본 명소";
                case 1:
                    return "공유 명소";
            }
            return null;
        }
    }


}


