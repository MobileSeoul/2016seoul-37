package com.example.jjw.mydemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jjw.mydemo.lib.common.AppPlaceInfo;
import com.example.jjw.mydemo.lib.common.GpsInfo;
import com.example.jjw.mydemo.lib.inf.DialogCommonListener;
import com.example.jjw.mydemo.lib.inf.GPSFinderListener;
import com.example.jjw.mydemo.lib.logon.SignInActivity;
import com.example.jjw.mydemo.lib.place.PlaceInfo;
import com.example.jjw.mydemo.lib.place.PostSharedPlace;
import com.example.jjw.mydemo.lib.common.User;
import com.example.jjw.mydemo.logger.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

// 위치 등록 화면
public class ShareRegActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, DialogCommonListener, GPSFinderListener{

    final Context context = this;

    public static final int REQUEST_CAPTURE = 1;
    public static final int REQUEST_PICK_PICTURE = 2;
    public static final int SEND_PLACE_REGISTER_REQUEST = 3;

    private GoogleMap mMap;
    private Marker mMarkerName;

    private GpsInfo gpsInfo;
    private LatLng mCurrentPosition;

    //레이아웃 변수
    private Spinner mSpinner;
    private EditText mInputShareRegFacName;
    private EditText mInputShareRegFacDesc;
    private EditText mtxtShareRegInput;         //텍스트
    private EditText mtxtShareRegInputCurrent;
    private Button mbtnShareRegRegistration;
    private Button mBtnCapture;
    private Button mBtnFindAddress;
    private Button mBtnShareRegGallery;

    private String mX_Coord;
    private String mY_Coord;
    private String strBitmap;

    //Firebase database
    private DatabaseReference mDatabase;
    //ApplicationClass
    private AppPlaceInfo mAppPlaceInfo;
    private FirebaseAuth mAuth;
    private MenuItem mSignoutMenuItem;
    private MenuItem mSigninMenuItem;

    private boolean isClickSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_reg);

        // Initializing
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapShareReg);
        mapFragment.getMapAsync(this);

        mAppPlaceInfo = (AppPlaceInfo)getApplication();
        isClickSearchButton = false;
        initLayout();

        //Camera setting button
        mBtnCapture = (Button)this.findViewById(R.id.Bcapture);
        if(!hasCamera())
        {
            mBtnCapture.setEnabled(false);
        }
        strBitmap = null;
        //GPSInfo
        gpsInfo = ((AppPlaceInfo)getApplication()).uniqueGpsInfo;


        getCurrentPosition();
        initAuthentication();
    }


    //Camera
    public boolean hasCamera()
    {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }
    //Camera
    public void launchCamera(View v)
    {
        Intent i = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, REQUEST_CAPTURE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //System.out.println("-----ShareRegActivity -- onActivity Result-----");
        if(requestCode == REQUEST_CAPTURE && resultCode == RESULT_OK)
        {
            try {
                Bundle extras = data.getExtras();
                Bitmap photo = (Bitmap) extras.get("data");
                //System.out.println("-----ShareRegActivity - photo : " + photo);
                strBitmap = getStringFromBitmap(photo);
                //System.out.println("-----ShareRegActivity - photo str : " + strBitmap);
                //Bitmap test = getBitmapFromString(tmp);
                ImageView tmpImg = (ImageView) this.findViewById(R.id.imgShareRegCamera);

                //이미지가 있는지 확인한다..
                if (tmpImg.getDrawable() != null) {
                    mAppPlaceInfo.createOkDialog(context, getResources().getString(R.string.common_information),
                            getResources().getString(R.string.share_reg_restriction_multiple_camera));
                }
                // System.out.println("--camera width : " + photo.getWidth() + ", height : " + photo.getHeight());
                tmpImg.setImageBitmap(photo);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }else if (requestCode == REQUEST_PICK_PICTURE && resultCode == RESULT_OK) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
           // Bitmap src = BitmapFactory.decodeFile(data.getDataString(), options);
         //   Bitmap resized = Bitmap.createScaledBitmap(src, 100, 100, true);
            //Bitmap src = BitmapFactory.decodeFile("/sdcard/image.jpg", options)

            Bitmap src = null;
            Bitmap csrc = null;
            try {
                src = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
               // csrc = BitmapFactory.decodeByteArray(mAppPlaceInfo.uniqueImageProcessInfo.bitmapToByteArray(src), 600, 600, options);

                csrc = mAppPlaceInfo.uniqueImageProcessInfo.getResizedBitmap(src, 204, 115);
                strBitmap = getStringFromBitmap(csrc);

               // System.out.println("--Gallery width : " + csrc.getWidth() + ", height : " + csrc.getHeight());
                ImageView img = (ImageView)findViewById(R.id.imgShareRegCamera);
                //img.setImageURI(data.getData()); // 사진 선택한 사진URI로 연결하기

                //이미지가 있는지 확인한다..
                if(img.getDrawable() != null)
                {
                    mAppPlaceInfo.createOkDialog(context, getResources().getString(R.string.common_information),
                            getResources().getString(R.string.share_reg_restriction_multiple_camera));
                }
                img.setImageBitmap(csrc);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void initLayout()
    {
        //FacCode and FacDesc
        mInputShareRegFacName = (EditText)findViewById(R.id.inputShareRegFacName);
        mInputShareRegFacDesc = (EditText)findViewById(R.id.inputShareRegFacDesc);



        mSpinner = (Spinner)findViewById(R.id.spinnerShareRegCodeName);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.codename, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //EditText
        mtxtShareRegInput = (EditText)this.findViewById(R.id.txtShareRegInput);
        mtxtShareRegInputCurrent = (EditText)this.findViewById(R.id.txtShareRegInputCurrent);
        mbtnShareRegRegistration = (Button)this.findViewById(R.id.btnShareRegRegistration);
        mBtnFindAddress = (Button) this.findViewById(R.id.btnShareRegFindAddress);
        mBtnShareRegGallery = (Button) this.findViewById(R.id.btnShareRegGallery);

        mBtnFindAddress.setOnClickListener(this);
        mbtnShareRegRegistration.setOnClickListener(this);
        mBtnShareRegGallery.setOnClickListener(this);
    }


    /**
     * 현재 위치를 가져온다..
     */
    public void getCurrentPosition()
    {
     /*   try {

            gpsInfo.setContext(this);

            //위치 정보 얻기
            Location currentLocation = gpsInfo.getLocation();
            String strCurrentAddress = getGeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
            if (strCurrentAddress == null) {
                mtxtShareRegInput.setText("");
            } else {
                mtxtShareRegInput.setText(strCurrentAddress);
                mtxtShareRegInputCurrent.setText("현위치");

                mX_Coord = Double.toString(currentLocation.getLatitude());
                mY_Coord = Double.toString(currentLocation.getLongitude());

                mCurrentPosition = new LatLng(gpsInfo.getLatitude(), gpsInfo.getLongitude());
            }


//            mtxtShareRegInput.setText(mCurrentPosition.toString());
            //test
        }catch (Exception e)
        {
            e.printStackTrace();
        }*/
        gpsInfo.setContext(this);
        mAppPlaceInfo.uniqueGpsInfo.getGPSEnabled();
    }

    /**
     *  주소를 검색하여 위치를 가져온다..
     */


    private void postPlace() {



        final String codeName = mSpinner.getSelectedItem().toString();
        final String facName = mInputShareRegFacName.getText().toString();
        final String facdesc = mInputShareRegFacDesc.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(facName)) {
            mInputShareRegFacName.setError("REQUIRED");
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(facdesc)) {
            mInputShareRegFacDesc.setError("REQUIRED");
            return;
        }
        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();


        // [START single_value_read]
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                           // Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(ShareRegActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post

                            writeNewPost(userId, user.username, codeName, facName, facdesc);
                        }

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                       // Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });
        // [END single_value_read]

    }

    // [START write_fan_out]
    private void writeNewPost(String userId, String username, String codeName, String facName, String facDesc) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("SharedPlace").push().getKey();

        //PostSharedPlace post = new PostSharedPlace(userId, username, codeName, facName, facDesc, mX_Coord, mY_Coord, strBitmap);
        PlaceInfo post = new PlaceInfo(userId, username, codeName, facName, facDesc, mX_Coord, mY_Coord, strBitmap, mtxtShareRegInput.getText().toString());

      /*  Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/SharedPlace/", postValues);
        //childUpdates.put("/SharedPlace/" + key, postValues);
        childUpdates.put("/user-SharedPlace/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);*/
        mDatabase.child("/SharedPlace").push().setValue(post);
    }

    private void setEditingEnabled(boolean enabled) {
        mInputShareRegFacName.setEnabled(enabled);
        mInputShareRegFacDesc.setEnabled(enabled);
        if (enabled) {
            mbtnShareRegRegistration.setVisibility(View.VISIBLE);
        } else {
            mbtnShareRegRegistration.setVisibility(View.GONE);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            mMap = googleMap;


            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }


            LatLng position = mCurrentPosition;
            mMarkerName = mMap.addMarker(new MarkerOptions().position(position).title("현재위치"));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18));


           // mMap.setMyLocationEnabled(true);
        }catch(Exception e)
        {
            e.printStackTrace();
            String content = getResources().getString(R.string.share_reg_not_found_position);
            Toast.makeText(this,content, Toast.LENGTH_SHORT).show();
            return;
        }
    }


    //위치 정보를 주소로 변환한다..
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
            String content = getResources().getString(R.string.share_reg_address_not_found);
            Toast.makeText(this,content, Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "주소를 찾지 못하였습니다.");
            e.printStackTrace();
            return null;
        } catch (NullPointerException e){
            String content = getResources().getString(R.string.share_reg_address_not_found);
            Toast.makeText(this,content, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return null;
        }


        return str;

    }


    private String getStringFromBitmap(Bitmap bitmapPicture) {
 /*
 * This functions converts Bitmap picture to a string which can be
 * JSONified.
 * */
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    private Bitmap getBitmapFromString(String jsonString) {
/*
* This Function converts the String back to Bitmap
* */
        byte[] decodedString = Base64.decode(jsonString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {

            //주소 검색 버튼..
            case R.id.btnShareRegFindAddress:
                //입력창의 텍스트 호가인
                if(mtxtShareRegInput.getText().toString().isEmpty())
                {
                    //명령창
                    String content = getResources().getString(R.string.share_reg_input_address);
                    Toast.makeText(ShareRegActivity.this, content, Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    String address = mtxtShareRegInput.getText().toString();
                    //주소를 가져온다.
                    Location location = mAppPlaceInfo.uniqueGpsInfo.findGeoPoint(context, address);
                    if(location == null || location.getLongitude() == 0 || location.getLatitude() == 0)
                    {
                        //위에 해당하는 경우, 값이 없는 경우..
                        String content = getResources().getString(R.string.share_reg_address_not_found);
                        Toast.makeText(ShareRegActivity.this, content, Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        System.out.println("----------get Location : " + location);

                        //현위치 제거하기..
                        mtxtShareRegInputCurrent.setVisibility(View.GONE);

                        mX_Coord = Double.toString(location.getLatitude());
                        mY_Coord = Double.toString(location.getLongitude());
                        isClickSearchButton = true;
                    }

                    drawMarker(location);
                }
                break;
            case R.id.btnShareRegRegistration:

                if(isClickSearchButton == false)
                {
                    mAppPlaceInfo.createOkDialog(context, getResources().getString(R.string.common_information), getResources().getString(R.string.share_reg_find_button_not_clicked));

                }else {
                    mAppPlaceInfo.createOkCancelDialog(context, getResources().getString(R.string.place_register_dialog_title),
                            getResources().getString(R.string.place_register_dialog_content), SEND_PLACE_REGISTER_REQUEST);
                    isClickSearchButton = false;
                }

                break;
            case R.id.btnShareRegGallery:

                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                i.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // images on the SD card.

                // 결과를 리턴하는 Activity 호출
                startActivityForResult(i, REQUEST_PICK_PICTURE);
                break;

            default:
                //no jobs
        }

    }

    private void drawMarker(Location location) {

        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());

        mMarkerName.remove();
        mMarkerName = mMap.addMarker(new MarkerOptions().position(latlng).title(mtxtShareRegInput.getText().toString()));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18));
    }

    @Override
    public void onDialogOk(int requestCode) {

        if(requestCode == SEND_PLACE_REGISTER_REQUEST)
        {
            postPlace();
        }
    }

    @Override
    public void onDialogCancel() {

    }
    //camera
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
                Intent intent = new Intent(ShareRegActivity.this, Main3Activity.class);
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
                ((AppPlaceInfo)getApplication()).createOkDialog(ShareRegActivity.this, getResources().getString(R.string.common_information),
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


    @Override
    public void onGPSFinderSuccess() {
        try {
            //위치 정보 얻기
            Location currentLocation = gpsInfo.getLocation();
            String strCurrentAddress = getGeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
            if (strCurrentAddress == null) {
                mtxtShareRegInput.setText("");
                //현위치 제거하기..
                mtxtShareRegInputCurrent.setVisibility(View.GONE);
            } else {
                mtxtShareRegInput.setText(strCurrentAddress);
                mtxtShareRegInputCurrent.setText("현위치");

                mX_Coord = Double.toString(currentLocation.getLatitude());
                mY_Coord = Double.toString(currentLocation.getLongitude());

                mCurrentPosition = new LatLng(gpsInfo.getLatitude(), gpsInfo.getLongitude());
                mtxtShareRegInputCurrent.setVisibility(View.VISIBLE);
            }


//            mtxtShareRegInput.setText(mCurrentPosition.toString());
            //test
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onGPSFinderFailed() {
        System.out.println("_-----[ShareReg] GPSFinder Failed!");
        mtxtShareRegInput.setText("");
        mtxtShareRegInputCurrent.setVisibility(View.GONE);

        mAppPlaceInfo.uniqueGpsInfo.showSettingsAlert();
    }
}
