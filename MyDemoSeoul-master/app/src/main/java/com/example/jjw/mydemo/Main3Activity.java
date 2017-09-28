package com.example.jjw.mydemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jjw.mydemo.lib.common.AppPlaceInfo;
import com.example.jjw.mydemo.lib.inf.GPSFinderListener;
import com.example.jjw.mydemo.lib.logon.SignInActivity;
import com.example.jjw.mydemo.lib.adapter.CustomViewPagerAdapter;
import com.example.jjw.mydemo.lib.place.PlaceInfo;
import com.example.jjw.mydemo.lib.place.PostSharedPlace;
import com.example.jjw.mydemo.logger.Log;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Main3Activity extends AppCompatActivity implements View.OnClickListener, GPSFinderListener{

    /**
     * Request code for the autocomplete activity. This will be used to identify results from the
     * autocomplete activity in onActivityResult.
     */
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;   //검색 요청 코드
    private static final int REQUEST_CODE_SIGNIN = 2;           // 로그인 요청 코드
    public static final int REQUEST_MAIN_PLACE = 3;           // 메인 화면 정보 요청
    public static final int REQUEST_FROM_ORIGIN = 4;
    public static final int REQUEST_FROM_DESTINATION = 5;


    //Layout Components
    private EditText mInputPlaceOrigin;
    private EditText mInputPlaceDestination;
    private EditText mInputPlaceOriginCurrent;
    private EditText mInputTmp;

    //출도착지 위경도 가져오기
    private SearchPlaceInfo mSearchPlaceInfoOrigin;
    private SearchPlaceInfo mSearchPlaceInfoDestination;
    private int mSearchPlaceRequestType;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    //메인 이미지 가져오기
    private ViewPager mViewPager;
    private CustomViewPagerAdapter mCustomViewPagerAdapter;

    //Firebase
    //Autentication
    private FirebaseAuth mAuth;
    //데이터베이스 연동
    private DatabaseReference mDatabase;

    //ApplicationClass
    private AppPlaceInfo mAppPlaceInfo;

    MenuItem mSignoutMenuItem;
    MenuItem mSigninMenuItem;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main3);
        mContext = this;

        mSearchPlaceInfoDestination = new SearchPlaceInfo();
        mSearchPlaceInfoOrigin = new SearchPlaceInfo();
        mSearchPlaceRequestType = 0;

        initLayout();           //레이아웃 설정
        mAppPlaceInfo = (AppPlaceInfo)getApplication();

        getCurrentPosition();
        //initAuthentication();

        //뷰페이저
        mViewPager = (ViewPager)this.findViewById(R.id.viewpagerMain);       //ViewPager 로딩
        mCustomViewPagerAdapter = new CustomViewPagerAdapter(this, true, false);
        mViewPager.setAdapter(mCustomViewPagerAdapter);

        //아래는 메인화면 그냥 소스에 넣기..
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.main_2);
        Bitmap bm3 = BitmapFactory.decodeResource(getResources(), R.drawable.main_3);
        Bitmap bm4 = BitmapFactory.decodeResource(getResources(), R.drawable.main_4);


        mCustomViewPagerAdapter.setBitmaps(bm);
        mCustomViewPagerAdapter.setBitmaps(bm3);
        mCustomViewPagerAdapter.setBitmaps(bm4);

        initSharedDatabase();
    }

    /**
     *  레이아웃을 설정한다.
     */
    public void initLayout()
    {
        Button btnLogin = (Button)this.findViewById(R.id.btnMainIntLogin);
        Button btnShare = (Button)this.findViewById(R.id.btnMainIntShare);
        Button btnCurrentPostion = (Button)this.findViewById(R.id.btnMainIntCurrentPosition);
        Button btnSearchButton = (Button) findViewById(R.id.btnMainIntSearch);

        btnShare.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnCurrentPostion.setOnClickListener(this);
        btnSearchButton.setOnClickListener(this);

        btnLogin.setVisibility(View.GONE);

        //Init EditText;
        mInputPlaceOrigin = (EditText) findViewById(R.id.inputMainOrigin);
        mInputPlaceDestination = (EditText) findViewById(R.id.inputMainDestination);
        mInputPlaceOriginCurrent = (EditText)findViewById(R.id.inputMainOriginCurrent);

        mInputPlaceOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputTmp = mInputPlaceOrigin;
                mSearchPlaceRequestType = REQUEST_FROM_ORIGIN;
                openAutocompleteActivity();
                mInputPlaceOriginCurrent.setText("");
            }
        });
        mInputPlaceDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchPlaceRequestType = REQUEST_FROM_DESTINATION;
                mInputTmp = mInputPlaceDestination;
                openAutocompleteActivity();
            }
        });

    }

    /**
     *  로그인 및 인증 절차 셋팅
     */
    public void initAuthentication()
    {
        //로그온 체크하기
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            //계정이 연결되어 있는 상태
            // onAuthSuccess(mAuth.getCurrentUser())..
            TextView textView =  (TextView)findViewById(R.id.btnMainIntLogin);
            textView.setText("로그아웃");
           // mSigninMenuItem.setVisible(false);
           // mSignoutMenuItem.setVisible(true);
            if(mSigninMenuItem != null && mSignoutMenuItem != null) {
                mSigninMenuItem.setVisible(false);
                mSignoutMenuItem.setVisible(true);
            }
            mAppPlaceInfo.uniqueUserInfo.setUserId(mAuth.getCurrentUser().toString());
        }else
        {
            TextView textView =  (TextView)findViewById(R.id.btnMainIntLogin);
            textView.setText("로그인");
            //mSigninMenuItem.setVisible(true);
           // mSignoutMenuItem.setVisible(false);
            if(mSigninMenuItem != null && mSignoutMenuItem != null) {
                mSigninMenuItem.setVisible(true);
                mSignoutMenuItem.setVisible(false);
            }
        }
    }


    /**
     * 위치 정보를 주소 정보로 변환한다.
     * @param lat
     * @param lng
     * @return
     */
    public String getGeoLocation(double lat, double lng){
        String str = null;
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);

        List<Address> address;
        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(lat, lng, 1);
                if (address != null && address.size() > 0) {
                    str = address.get(0).getAddressLine(0).toString();
                }
            }
        } catch (IOException e) {
            Log.e("Main3Activity", "주소를 찾지 못하였습니다.");
            Toast.makeText(Main3Activity.this, "주소를 찾지 못하였습니다.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }


        return str;

    }

    /**
     *  GPS 정보를 이용하여 현재 위치 정보를 가져와서, 출발지 상태를 현재 위치로 변경시킨다.
     */
    public void getCurrentPosition()
    {

        mAppPlaceInfo.uniqueGpsInfo.setContext(this);
        mAppPlaceInfo.uniqueGpsInfo.getGPSEnabled();


    }



    //데이터 베이스 초기화
    public void initSharedDatabase()
    {

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("SharedPlace").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                PlaceInfo data = dataSnapshot.getValue(PlaceInfo.class);
                System.out.println("----getUser : " + data.getAuthor() + ", FacName : " + data.getFAC_NAME() + ", Etc : " + data.getETC_DESC() + ", addr : " + data.getADDR());
                data.setShared(true);
                mAppPlaceInfo.uniqueSharedPlaceInfoList.add(data);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //장소명을 가져온다..
    private void openAutocompleteActivity() {

        Intent intent = new Intent(this, MainSrcActivity.class);

        startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);

    }

    //장소 검색 이 후 결과 화면으로 리턴한다..
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.


        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            //다른 액티비티로 부터 받음 코드..
            if (resultCode == RESULT_OK) {      //-1
                // Get the user's selected place from the Intent.
                String placeName = data.getStringExtra("placeName");
                String placeAddress = data.getStringExtra("placeAddress");
                LatLng placeLatLng = (LatLng)(data.getParcelableExtra("placeLatLng"));

                System.out.println("----------placeLatLng : " + placeLatLng);

                try{
                    if(mInputTmp != null) {

                        if (!placeAddress.contains("서울")) {
                            //서울 특별시가 아니면 메시지를 출력한다.
                            String content = getResources().getString(R.string.main_not_seoul);
                            Toast.makeText(this, content,
                                    Toast.LENGTH_SHORT).show();
                            mInputTmp.setText("");

                        } else {
                            mInputTmp.setText(placeName);
                            //여기서 출도착지 작업..
                            //1. 요청이출발지에서 발생된 경우..
                            if(mSearchPlaceRequestType == REQUEST_FROM_ORIGIN)
                            {
                                mSearchPlaceInfoOrigin.placeAddress = placeAddress;
                                mSearchPlaceInfoOrigin.placeName = placeName;
                                mSearchPlaceInfoOrigin.placeLatLng = placeLatLng;

                                System.out.println("------REQUEST FROM ORIGIN : " + mSearchPlaceInfoOrigin.toString());
                            }else if(mSearchPlaceRequestType == REQUEST_FROM_DESTINATION)
                            {
                                mSearchPlaceInfoDestination.placeAddress = placeAddress;
                                mSearchPlaceInfoDestination.placeName = placeName;
                                mSearchPlaceInfoDestination.placeLatLng = placeLatLng;
                                System.out.println("------REQUEST FROM DESINATION : " + mSearchPlaceInfoOrigin.toString());
                            }


                        }
                    }
                }catch(NullPointerException e)
                {
                    e.printStackTrace();
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                // no jobs
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }else if(requestCode == REQUEST_CODE_SIGNIN)
        {
            //  no jobs
        }else if(requestCode == REQUEST_MAIN_PLACE)
        {
            //메인화면에서 이달의 추천장소 정보 요청후, 목적지 등록이 클릭된 경우..
            // no jobs
            if(resultCode != 0) {
                String placeName = data.getStringExtra("placeName");
                String placeAddr = data.getStringExtra("placeAddress");
                LatLng placeLatLng = (LatLng)(data.getParcelableExtra("placeLatLng"));
                mInputPlaceDestination.setText(placeAddr);

                mSearchPlaceInfoDestination.placeAddress = placeAddr;
                mSearchPlaceInfoDestination.placeName = placeName;
                mSearchPlaceInfoDestination.placeLatLng = placeLatLng;
            }
        }
    }


    //앱 종료시 계정 연결을 종료 한다..


    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initAuthentication();

        //계정 관리..하기

    }

    //화면 클릭 이벤트
    public void onClick(View v){
        switch(v.getId()) {
            case R.id.btnMainIntLogin:
                Button tmp = (Button) findViewById(R.id.btnMainIntLogin);
                try {
                    if ("로그인".equals(tmp.getText().toString())) {

                        Intent intent = new Intent(this, SignInActivity.class);     //Sigin Activity 이동
                        startActivityForResult(intent, REQUEST_CODE_SIGNIN);

                        //submitPost();
                    } else {
                        System.out.println("로그아웃");
                        //위치 등록으로 넘어가기
                        FirebaseAuth.getInstance().signOut();
                        //UserInfo 제거
                        mAppPlaceInfo.uniqueUserInfo.signOut();
                        startActivity(new Intent(this, SignInActivity.class));
                    }
                }catch (NullPointerException e)
                {
                    e. printStackTrace();
                }
                break;
            case R.id.btnMainIntShare:

                //로그온 체크하기
                System.out.println("BtnMainIntShare Clicked..");
                if (mAuth != null) {
                    if (mAuth.getCurrentUser() != null) {
                        //계정이 연결되어 있는 상태
                        // 위치등록하기 다이얼로그 현시
                        //위치 등록으로 넘어가기
                        Intent intent = new Intent(this, ShareRegActivity.class);
                        startActivity(intent);

                    } else {
                        mAppPlaceInfo.createOkDialog(mContext, getResources().getString(R.string.common_information),
                                getResources().getString(R.string.main_login));
                    }
                }
                break;
            case R.id.btnMainIntCurrentPosition:
                //현재 위치설정하기..
                getCurrentPosition();

                break;
            case R.id.btnMainIntSearch :
                if (mInputPlaceOrigin.getText().toString().isEmpty()) {
                    Toast.makeText(Main3Activity.this, "출발지를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mInputPlaceDestination.getText().toString().isEmpty()) {
                    Toast.makeText(Main3Activity.this, "도착지를 입력하세요.", Toast.LENGTH_SHORT).show();

                    return;
                }

                Intent intent2 = new Intent(getApplicationContext(), SearchRst1Activity.class);

                Bundle extras = new Bundle();
                //먼저 위경도가있는지 확인한다.
                if(mSearchPlaceInfoOrigin.placeLatLng == null) {
                    extras.putString("origin", mInputPlaceOrigin.getText().toString());
                    extras.putString("originName", mInputPlaceOrigin.getText().toString());
                }else
                {
                    extras.putString("origin", mSearchPlaceInfoOrigin.getPlaceLatLng());
                    extras.putString("originName", mInputPlaceOrigin.getText().toString());
                }

                if(mSearchPlaceInfoDestination.placeLatLng == null) {
                    extras.putString("destination", mInputPlaceDestination.getText().toString());
                    extras.putString("destinationName", mInputPlaceDestination.getText().toString());
                }else{
                    extras.putString("destination", mSearchPlaceInfoDestination.getPlaceLatLng());
                    extras.putString("destinationName", mInputPlaceDestination.getText().toString());
                }

                // 위에서 만든 Bundle을 인텐트에 넣는다.
                intent2.putExtras(extras);

                startActivity(intent2);
                //Pause 상태로 들어간다..
                break;

        }
    }


    //아래는 액션 메뉴 관련
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
                Intent intent = new Intent(this, Main3Activity.class);
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
                mAppPlaceInfo.uniqueUserInfo.signOut();
                startActivity(new Intent(this, SignInActivity.class));

                return true;
            case R.id.action_myplan:
               // startActivity(new Intent(this, PlanStsActivity.class));
                performMyPlan();
//                Intent intent2 = new Intent(getApplicationContext(), PlanStsActivity.class);
//
//                startActivity(intent2);
                return true;

            case R.id.action_about:
                // startActivity(new Intent(this, PlanStsActivity.class));
                mAppPlaceInfo.createOkDialog(Main3Activity.this, getResources().getString(R.string.common_information),
                        getResources().getString(R.string.intro_message));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void performMyPlan() {
        //액티비티 이동..

        String content = getResources().getString(R.string.search_rst_planed_place_not_found);
        if (mAppPlaceInfo.uniqueUserInfo.uniquePlanedPlaceInfoList.isEmpty() && mAppPlaceInfo.uniqueUserInfo.uniquePlanedSharedPlaceInfoList.isEmpty()) {
            Toast.makeText(this, content,
                    Toast.LENGTH_SHORT).show();

        } else {
            Intent intent2 = new Intent(getApplicationContext(), PlanStsActivity.class);
            startActivity(intent2);
        }
    }


    @Override
    public void onGPSFinderSuccess() {

        //GPS가 정상적으로 켜져 있는 경우..
        String strCurrentAddress = null;
        System.out.println("-----GPSFinder Success!-----");
        try{
            //위치 정보 얻기

            Location currentLocation = mAppPlaceInfo.uniqueGpsInfo.getLocation();
            strCurrentAddress = getGeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
            if(strCurrentAddress == null)
            {
                strCurrentAddress = "";
                mInputPlaceOriginCurrent.setText("");
                //출발지 LatLng 초기화

            }else {
                strCurrentAddress = strCurrentAddress.replace("대한민국", "");

              //  mOriginLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            }
            mInputPlaceOrigin.setText(strCurrentAddress);
            mInputPlaceOriginCurrent.setText("현위치");

            //SearchPlaceInfo에 저장
            mSearchPlaceInfoOrigin.placeAddress = strCurrentAddress;
            mSearchPlaceInfoOrigin.placeLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        }catch(NullPointerException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onGPSFinderFailed() {
        System.out.println("-----GPSFinder Failed!------");
        mInputPlaceOrigin.setText("");
        mInputPlaceOriginCurrent.setText("");

        mAppPlaceInfo.uniqueGpsInfo.showSettingsAlert();

    }


    //출도착지 관리를 위한 클래스
    public class SearchPlaceInfo
    {
        public String placeName;
        public String placeAddress;
        public LatLng placeLatLng;

        public SearchPlaceInfo()
        {
            this.placeAddress = null;
            this.placeName = null;
            this.placeLatLng = null;
        }

        public String getPlaceLatLng()
        {
            String retVal = Double.toString(placeLatLng.latitude) +","+Double.toString(placeLatLng.longitude);
            return retVal;
        }

        @Override
        public String toString() {
            return "SearchPlaceInfo{" +
                    "placeName='" + placeName + '\'' +
                    ", placeAddress='" + placeAddress + '\'' +
                    ", placeLatLng=" + placeLatLng +
                    '}';
        }
    }
}
