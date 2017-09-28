package com.example.jjw.mydemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jjw.mydemo.lib.adapter.PlanStsListAdapter;
import com.example.jjw.mydemo.lib.common.AppPlaceInfo;
import com.example.jjw.mydemo.lib.logon.SignInActivity;
import com.example.jjw.mydemo.lib.place.PlaceInfo;
import com.example.jjw.mydemo.lib.place.PlaceInfoComparator;
import com.google.firebase.auth.FirebaseAuth;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


//리사이클러 뷰로 대체

/**
 *  공유장소와 일반 장소를 통합해서 관리 하기로 함.
 *  2016.10.22
 */
public class PlanStsActivity extends AppCompatActivity {

    private RecyclerView mPlanStstRecyclerView;
    private ArrayList<PlaceInfo> mCheckedPlaceList;
    private ArrayList<Bitmap> mBitmapList;
    private ProgressDialog pDialog;
    private Context mContext;
    private String mStart;
    private String mDest;
    private FirebaseAuth mAuth;
    private MenuItem mSignoutMenuItem;
    private MenuItem mSigninMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_sts);
        mContext = this;
        mCheckedPlaceList = new ArrayList<PlaceInfo>();
        mBitmapList = new ArrayList<Bitmap>();

        mStart = ((AppPlaceInfo)getApplication()).uniqueUserInfo.getRecentStart();
        mDest = ((AppPlaceInfo)getApplication()).uniqueUserInfo.getRecentDestination();
        checkPlanedPlaceInfo();

        mPlanStstRecyclerView = (RecyclerView)this.findViewById(R.id.recycler_viewPlanSts);
        mPlanStstRecyclerView.setNestedScrollingEnabled(false);
      //  mCheckedPlaceList = (ArrayList<PlaceInfo>)getIntent().getSerializableExtra("checkedPlaceList");

        sort();
        //2. setLayout
        mPlanStstRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //출발지 셋팅
        TextView txtStart = (TextView)this.findViewById(R.id.txtPlanStsDepartContent);
        txtStart.setText(mStart);
        TextView txtDest = (TextView)this.findViewById(R.id.txtPlanStsDestContent);
        txtDest.setText(mDest);

/*        //1. get Reference
        mPlanStstRecyclerView = (RecyclerView)this.findViewById(R.id.recycler_viewPlanSts);
        mCheckedPlaceList = (ArrayList<PlaceInfo>)getIntent().getSerializableExtra("checkedPlaceList");

        sort();
        //2. setLayout
        mPlanStstRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //3. Create an Adpater
        PlanStsListAdapter adapter = new PlanStsListAdapter(mCheckedPlaceList);
        //4. set adapter
        mPlanStstRecyclerView.setAdapter(adapter);
        //5. set item animator to default animator
        mPlanStstRecyclerView.setItemAnimator(new DefaultItemAnimator());*/
        initAuthentication();

        new loadMoreListView().execute();
    }

    public void checkPlanedPlaceInfo()
    {
        System.out.println("------------[PlanSts] checkPlanedPlaceInfo..");
        if(((AppPlaceInfo)getApplication()).uniqueUserInfo.uniquePlanedSharedPlaceInfoList.isEmpty()) {
            mCheckedPlaceList =  ((AppPlaceInfo)getApplication()).uniqueUserInfo.uniquePlanedPlaceInfoList;
        }else if(((AppPlaceInfo)getApplication()).uniqueUserInfo.uniquePlanedPlaceInfoList.isEmpty()){
            mCheckedPlaceList =  ((AppPlaceInfo)getApplication()).uniqueUserInfo.uniquePlanedSharedPlaceInfoList;
        }else {
            mCheckedPlaceList =  ((AppPlaceInfo)getApplication()).uniqueUserInfo.getMergedPlanList();
        }

        System.out.println("------------[PlanSts] mCheckedPlaceList : " + mCheckedPlaceList);
    }



    //받은정보를가공한다..
    public void sort()
    {
        PlaceInfoComparator comp = new PlaceInfoComparator();
        Collections.sort(mCheckedPlaceList, comp);

        System.out.println("---------Compare.... : " + mCheckedPlaceList);
    }

    public void refreshList()
    {
        //3. Create an Adpater
        PlanStsListAdapter adapter = new PlanStsListAdapter(mCheckedPlaceList, mBitmapList,this);
        //4. set adapter
        mPlanStstRecyclerView.setAdapter(adapter);
        //5. set item animator to default animator
        mPlanStstRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }





    //AsyncLoad
    private class loadMoreListView extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            // Showing progress dialog before sending http request
            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage("Please wait..");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... unused) {
            System.out.println("---------------Tab1 doInbackground..PlaceType ");

            try {
                Bitmap bitmap = null;
                for(int i = 0 ; i < mCheckedPlaceList.size() ; i++) {
                    PlaceInfo info = mCheckedPlaceList.get(i);
                    if (info.isShared() == false) {
                        if("null".equals(info.getMAIN_IMG()))
                        {
                            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_no_photo);
                        }else {
                            bitmap = BitmapFactory.decodeStream((InputStream) new URL(info.getMAIN_IMG()).getContent());
                        }
                    } else {
                        if(info.getImgSrc() == null)
                        {
                            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_no_photo);
                        }else {
                            bitmap = ((AppPlaceInfo) getApplication()).uniqueImageProcessInfo.getBitmapFromString(info.getImgSrc());
                        }
                    }
                    if(bitmap == null)
                    {
                        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_no_photo);
                    }
                    mBitmapList.add(bitmap);         //이거다시 조절필요..
                }

                System.out.println("---------bitmap : " + bitmap);
            } catch (Exception e) {
                e.printStackTrace();

            }

            return 1;
        }



        //더보기가 끝난 뒤 UI 작업
        @Override
        protected void onPostExecute(Integer value) {
            // closing progress dialog
            System.out.println("-=------------onPostExecute..");
            pDialog.dismiss();
            PlanStsActivity.this.refreshList();
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
                Intent intent = new Intent(PlanStsActivity.this, Main3Activity.class);
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
                //performMyPlan();
                return true;
            case R.id.action_about:
                // startActivity(new Intent(this, PlanStsActivity.class));
                ((AppPlaceInfo)getApplication()).createOkDialog(PlanStsActivity.this, getResources().getString(R.string.common_information),
                        getResources().getString(R.string.intro_message));
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
