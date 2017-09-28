package com.example.jjw.mydemo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.jjw.mydemo.lib.adapter.CustomViewPagerAdapter;
import com.example.jjw.mydemo.lib.common.AppPlaceInfo;
import com.example.jjw.mydemo.lib.logon.SignInActivity;
import com.example.jjw.mydemo.lib.place.PlaceInfo;
import com.example.jjw.mydemo.lib.place.PostSharedPlace;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.io.InputStream;
import java.net.URL;

public class SearchDetActivity extends AppCompatActivity  {

    //어떤 정보를 클릭했는지 확인한다.
    public static final int BASIC_PLACE = 0;
    public static final int SHARED_PLACE = 1;

    //layout 관련
    private ViewPager viewPager;
    private CustomViewPagerAdapter customViewPagerAdapter;

    //TextView
    private TextView mPlaceName;
    private TextView mPlaceDesc;

    //RankDialog
    private Dialog rankDialog;
    private RatingBar ratingBar;


    private Bitmap mBitmap;
    private float useRankValue;
    //ClickedPlace
    private PlaceInfo placeInfo;
    //PostSharedPlace mClickedSharedPlaceInfo;

    private int mPlaceType;
    private boolean mIsMain;
    private FirebaseAuth mAuth;
    private MenuItem mSignoutMenuItem;
    private MenuItem mSigninMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_det);

        Intent intent = getIntent();
        mPlaceType = intent.getIntExtra("placeType",BASIC_PLACE);
        placeInfo = (PlaceInfo) intent.getSerializableExtra("clickedPlace");
        mIsMain = intent.getBooleanExtra("isMain", false);
        viewPager = (ViewPager)this.findViewById(R.id.viewpager);       //ViewPager 로딩
        customViewPagerAdapter = new CustomViewPagerAdapter(this, false, placeInfo.isShared());
        viewPager.setAdapter(customViewPagerAdapter);



        if(mIsMain == false)
        {
            Button btnRegi = (Button) this.findViewById(R.id.btnSearchDetRegister);
            btnRegi.setVisibility(View.GONE);
        }else
        {
            Button btnRegi = (Button) this.findViewById(R.id.btnSearchDetRegister);
            btnRegi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("placeName", placeInfo.getFAC_NAME());
                    intent.putExtra("placeAddress", placeInfo.getADDR());
                    LatLng placeLatLng = new LatLng(Double.parseDouble(placeInfo.getX_COORD()), Double.parseDouble(placeInfo.getY_COORD()));
                    intent.putExtra("placeLatLng", placeLatLng);
                    setResult(1, intent);
                    finish();
                }
            });

        }

        //뷰 페이저 아래 레이아웃..
        mPlaceName = (TextView)this.findViewById(R.id.txtSearchDetPlaceName);
        mPlaceName.setText(placeInfo.getFAC_NAME());


        useRankValue = 1;
        initLayout();
        initAuthentication();
        //이미지 불러오기..
        if(mPlaceType == BASIC_PLACE)
            new LoadImage().execute(placeInfo.getMAIN_IMG());
        else
            new LoadImage().execute(placeInfo.getImgSrc());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }


    public void initLayout()
    {
        //래크 평가하기 버튼클릭시
        Button btnRank = (Button)this.findViewById(R.id.btnSearchDetRate);
        btnRank.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                rankDialog = new Dialog(SearchDetActivity.this, R.style.FullHeightDialog);
                rankDialog.setContentView(R.layout.rank_dialog);
                rankDialog.setCancelable(true);
                ratingBar = (RatingBar)rankDialog.findViewById(R.id.dialog_ratingbar);
                ratingBar.setRating(useRankValue);

                TextView text = (TextView)rankDialog.findViewById(R.id.rank_dialog_text1);
                text.setText("평가하기");

                Button updateButton = (Button)rankDialog.findViewById(R.id.rank_dialog_button);
                updateButton.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        rankDialog.cancel();
                        ((AppPlaceInfo)getApplication()).createOkDialog(SearchDetActivity.this, getResources().getString(R.string.common_information),
                                getResources().getString(R.string.share_det_restriction_vote));
                        //여기 알람 메시지 호출..
                    }
                });

                rankDialog.show();
            }
        });


        //공유 버튼클릭시
        Button btnShare = (Button) this.findViewById(R.id.btnSearchDetShare);
        btnShare.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String share = "명소명:" + placeInfo.getFAC_NAME() + "\n" + "주소:" + placeInfo.getADDR();
                if(placeInfo.isShared() == false)
                {
                    if("null".equals(placeInfo.getHOMEPAGE())){
                        share = share + "\n" + "홈페이지:정보없음";
                    }else {
                        share = share + "\n" + "홈페이지:" + placeInfo.getHOMEPAGE();
                    }
                }
                Intent msg = new Intent(Intent.ACTION_SEND);
                msg.addCategory(Intent.CATEGORY_DEFAULT);
                msg.putExtra(Intent.EXTRA_SUBJECT, "주제");
                msg.putExtra(Intent.EXTRA_TEXT, share);
                msg.putExtra(Intent.EXTRA_TITLE, "제목");
                msg.setType("text/plain");

                startActivity(Intent.createChooser(msg, "공유"));
            }
        });

        Button btnMap = (Button) this.findViewById(R.id.btnSearchDetMap);
        btnMap.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //구글 지도 띄우기..
                Intent intent = new Intent(SearchDetActivity.this, MapsActivity.class);
                intent.putExtra("clickedPlace", placeInfo);
                // 액티비티를 생성한다.
                SearchDetActivity.this.startActivity(intent);
            }
        });

        TextView txtReply =  (TextView)findViewById(R.id.txtSearchDetReply);
        txtReply.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                ((AppPlaceInfo)getApplication()).createOkDialog(SearchDetActivity.this, getResources().getString(R.string.common_information),
                        getResources().getString(R.string.share_det_restriction_reply));
            }
        });
    }

    public void setDetailContents()
    {
//        if(mPlaceType == BASIC_PLACE) {
            //Code Name
        try {
            TextView codeName = (TextView) this.findViewById(R.id.txtSearchDetCodeNameContent);
            codeName.setText((placeInfo.getCODENAME().isEmpty() || placeInfo.getCODENAME().equals("null")) ? "정보 없음 " : (placeInfo.getCODENAME()));

            //Etc Desc
            TextView etcDesc = (TextView) this.findViewById(R.id.txtSearchDetEtc_DescContent);
            etcDesc.setText((placeInfo.getETC_DESC().isEmpty() || placeInfo.getETC_DESC().equals("null")) ? "정보 없음 " : (placeInfo.getETC_DESC()));
            //Seat
            if(placeInfo.isShared() == true) {
                TextView addr = (TextView) this.findViewById(R.id.txtSearchDetAddrContent);
                addr.setText((placeInfo.getADDR() == null || placeInfo.getADDR().isEmpty() || placeInfo.getADDR().equals("null")) ? "정보 없음 " : (placeInfo.getADDR()));

                TextView txtSeatTitle = (TextView) this.findViewById(R.id.txtSearchDetSeat_Cnt);
                txtSeatTitle.setText("작성자");
                TextView seat = (TextView) this.findViewById(R.id.txtSearchDetSeat_CntContent);

                seat.setText((placeInfo.getAuthor().isEmpty() || placeInfo.getAuthor().equals("null")) ? "정보 없음 " : (placeInfo.getAuthor()));
                System.out.println("-----AUthor : " + placeInfo.getAuthor());
            }else {
                TextView seat = (TextView) this.findViewById(R.id.txtSearchDetSeat_CntContent);
                seat.setText((placeInfo.getSEAT_CNT().isEmpty() || placeInfo.getSEAT_CNT().equals("null")) ? "정보 없음 " : (placeInfo.getSEAT_CNT()));
                //PhoneNumber
                TextView phone = (TextView) this.findViewById(R.id.txtSearchDetEtc_DescPhneContent);
                phone.setText((placeInfo.getPHNE().isEmpty() || placeInfo.getPHNE().equals("null")) ? "정보 없음 " : (placeInfo.getPHNE()));

                //openHour
                TextView openHour = (TextView) this.findViewById(R.id.txtSearchDetOpenhourContent);
                openHour.setText((placeInfo.getOPENHOUR().isEmpty() || placeInfo.getOPENHOUR().equals("null")) ? "정보 없음 " : (placeInfo.getOPENHOUR()));

                //Free
                TextView free = (TextView) this.findViewById(R.id.txtSearchDetEntrfreeContent);
                free.setText((placeInfo.getENTRFREE().isEmpty() || placeInfo.getENTRFREE().equals("null")) ? "정보 없음 " : (placeInfo.getENTRFREE()));

                //Entry Fee
                TextView entryFee = (TextView) this.findViewById(R.id.txtSearchDetEntrfreeContent);
                entryFee.setText((placeInfo.getENTR_FEE().isEmpty() || placeInfo.getENTR_FEE().equals("null")) ? "정보 없음 " : (placeInfo.getENTR_FEE()));

                //CloseDay
                TextView closeDay = (TextView) this.findViewById(R.id.txtSearchDetClosedayContent);
                closeDay.setText((placeInfo.getCLOSEDAY().isEmpty() || placeInfo.getCLOSEDAY().equals("null")) ? "정보 없음 " : (placeInfo.getCLOSEDAY()));

                //OpenDay
                TextView openDay = (TextView) this.findViewById(R.id.txtSearchDetOpen_DayContent);
                openDay.setText((placeInfo.getOPEN_DAY().isEmpty() || placeInfo.getOPEN_DAY().equals("null")) ? "정보 없음 " : (placeInfo.getOPEN_DAY()));


                TextView addr = (TextView) this.findViewById(R.id.txtSearchDetAddrContent);
                addr.setText((placeInfo.getADDR().isEmpty() || placeInfo.getADDR().equals("null")) ? "정보 없음 " : (placeInfo.getADDR()));

                //CloseDay
                TextView hp = (TextView) this.findViewById(R.id.txtSearchDetHomepageContent);
                if(placeInfo.getHOMEPAGE().isEmpty() || placeInfo.getHOMEPAGE().equals("null"))
                {
                    hp.setText("정보 없음");
                }else
                {
                    hp.setText(placeInfo.getHOMEPAGE());
                    Linkify.addLinks(hp, Linkify.WEB_URLS);
                }
               // hp.setText((placeInfo.getHOMEPAGE().isEmpty() || placeInfo.getHOMEPAGE().equals("null")) ? "정보 없음 " : Html.fromHtml("<b>"+placeInfo.getHOMEPAGE()+"</b>"));
            }
        }catch(NullPointerException e)
        {
            e.printStackTrace();
            return;
        }
        /*}else
        {
            //Shared Type..
            //Code Name
            TextView codeName = (TextView) this.findViewById(R.id.txtSearchDetCodeNameContent);
            codeName.setText((mClickedSharedPlaceInfo.getCodeName().isEmpty() || mClickedSharedPlaceInfo.getCodeName().equals("null")) ? "정보 없음 " : (mClickedSharedPlaceInfo.getCodeName()));

            //Etc Desc
            TextView etcDesc = (TextView) this.findViewById(R.id.txtSearchDetEtc_DescContent);
            etcDesc.setText((mClickedSharedPlaceInfo.getFacDesc().isEmpty() || mClickedSharedPlaceInfo.getFacDesc().equals("null")) ? "정보 없음 " : (mClickedSharedPlaceInfo.getFacDesc()));

        }*/

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
                Intent intent = new Intent(SearchDetActivity.this, Main3Activity.class);
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
                ((AppPlaceInfo)getApplication()).createOkDialog(SearchDetActivity.this, getResources().getString(R.string.common_information),
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





    //이미지 불러오기..
    private class LoadImage extends AsyncTask<String, String, Bitmap>{

        ProgressDialog pDialog;

        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(SearchDetActivity.this);
            pDialog.setMessage("잠시만 기다려 주세요..");
            pDialog.show();
        }


        @Override
        protected Bitmap doInBackground(String... params) {

            //System.out.println("----Param : " + params[0]);

            try{
                if(mPlaceType == BASIC_PLACE) {
                    if("null".equals(params[0]))
                    {
                        mBitmap = BitmapFactory.decodeResource(SearchDetActivity.this.getResources(), R.drawable.img_no_photo);
                    }else {
                        mBitmap = BitmapFactory.decodeStream((InputStream) new URL(params[0]).getContent());
                    }
                }
                else {
                    if("null".equals(params[0]) || params[0] ==null)
                    {
                        mBitmap = BitmapFactory.decodeResource(SearchDetActivity.this.getResources(), R.drawable.img_no_photo);
                    }else {
                        mBitmap = ((AppPlaceInfo) getApplication()).uniqueImageProcessInfo.getBitmapFromString(params[0]);
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }

            return mBitmap;
        }

        protected void onPostExecute(Bitmap image) {

            if (image != null) {
                customViewPagerAdapter.setBitmaps(image);
                pDialog.dismiss();

            } else {
                pDialog.dismiss();
                Toast.makeText(SearchDetActivity.this, "이미지가 존재하지 않습니다.",
                        Toast.LENGTH_SHORT).show();

            }
            SearchDetActivity.this.setDetailContents();
        }
    }
}
