package com.example.jjw.mydemo.lib.common;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.support.multidex.MultiDex;

import com.example.jjw.mydemo.R;
import com.example.jjw.mydemo.lib.inf.DialogCommonListener;
import com.example.jjw.mydemo.lib.place.PlaceInfo;
import com.example.jjw.mydemo.lib.place.PostSharedPlace;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 * Created by JJW on 2016-10-05.
 * 앱에서 사용되는 모든 전역변수를 관리 한다..
 */
public class AppPlaceInfo extends Application {

    public ArrayList<PlaceInfo> uniquePlaceInfoList;				    // 서울관광 파일로부터 받은 장소정보
    public ArrayList<PlaceInfo> uniqueSharedPlaceInfoList;       // 서버로 부터 받은 공유 장소 정보

//    public ArrayList<PlaceInfo> uniquePlanedPlaceInfoList;                  //기본 정보의 여행계획 정보
//    public ArrayList<PlaceInfo> uniquePlanedSharedPlaceInfoList;      // 공유정보의 여행계획 정보

    public ArrayList<PlaceInfo> uniqueShowPlaceInfoList;		//

    public ArrayList<PlaceInfo> uniqueMainPlaceList;

    public UserInfo uniqueUserInfo;

    public GpsInfo  uniqueGpsInfo;                              //GPS 정보 클래스
    public ImageProcess uniqueImageProcessInfo;

    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mPostListener;
    private String mPostKey;

    private int mDialogRetVal;
    //Test
    private DialogCommonListener listener;

    //  public Firebase ref;

    /** onCreate()
     * 액티비티, 리시버, 서비스가 생성되기전 어플리케이션이 시작 중일때
     * Application onCreate() 메서드가 만들어 진다고 나와 있습니다.
     * by. Developer 사이트
     */
    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        System.out.println("-------------AppPlaceInfo onCreate()-------------");
        //리스트 초기화
        uniquePlaceInfoList = new ArrayList<PlaceInfo>();
        uniqueSharedPlaceInfoList = new ArrayList<PlaceInfo>();
        uniqueShowPlaceInfoList = new ArrayList<PlaceInfo>();
        uniqueMainPlaceList = new ArrayList<PlaceInfo>();
//        uniquePlanedPlaceInfoList = new ArrayList<PlaceInfo>();
//        uniquePlanedSharedPlaceInfoList = new ArrayList<PlaceInfo>();
        uniqueUserInfo = new UserInfo();
        //Firebase 초기화
       // Firebase.setAndroidContext(this);

      //  ref = new Firebase("https://myappdemo-143811.firebaseio.com/");


       // ref.setValue("TEST");
        //GPS Info
        uniqueGpsInfo = new GpsInfo();
        uniqueImageProcessInfo = new ImageProcess();

        ProcessRawData thread = new ProcessRawData();
        thread.setDaemon(true);
        thread.start();

        //테스트 데이터베이스 가져오기
    }


    /**
     * onConfigurationChanged()
     * 컴포넌트가 실행되는 동안 단말의 화면이 바뀌면 시스템이 실행 한다.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    //json 파일을 읽어 들인다.
    /* 파일로부터 데이터 로딩 */
    class ProcessRawData extends Thread {

        @Override
        public void run()
        {

            InputStream inputData = getResources().openRawResource(R.raw.seoul_place_info);
            try {
                //데이터 다운로드..

                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputData));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                // System.out.println("-----data : " +  buffer.toString());
                parsePlaceJSon(buffer.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void parsePlaceJSon(String data) throws JSONException {
            if (data == null)
                return;

            //System.out.println("-----data : " + data);

            // List<PlaceInfo> placeList = new ArrayList<>();
            JSONObject jsonData = new JSONObject(data);
            JSONArray jsonPlace = jsonData.getJSONArray("DATA");
            for (int i = 0; i < jsonPlace.length(); i++) {
                JSONObject jplace = jsonPlace.getJSONObject(i);
                PlaceInfo place = new PlaceInfo();

                place.setCODENAME(jplace.get("CODENAME").toString());
                place.setFAC_NAME(jplace.get("FAC_NAME").toString());
                place.setETC_DESC(jplace.get("ETC_DESC").toString());
                place.setSUBJCODE(jplace.get("SUBJCODE").toString());
                place.setSEAT_CNT(jplace.get("SEAT_CNT").toString());
                place.setPHNE(jplace.get("PHNE").toString());
                place.setOPENHOUR(jplace.get("OPENHOUR").toString());
                place.setX_COORD(jplace.get("X_COORD").toString());
                place.setENTRFREE(jplace.get("ENTRFREE").toString());
                place.setMAIN_IMG(jplace.get("MAIN_IMG").toString());
                place.setY_COORD(jplace.get("Y_COORD").toString());
                place.setENTR_FEE(jplace.get("ENTR_FEE").toString());
                place.setCLOSEDAY(jplace.get("CLOSEDAY").toString());
                place.setOPEN_DAY(jplace.get("OPEN_DAY").toString());
                place.setADDR(jplace.get("ADDR").toString());
                place.setHOMEPAGE(jplace.get("HOMEPAGE").toString());
                place.setFAX(jplace.get("FAX").toString());

                //test
                place.setChecked(false);
                place.setImgSrc("@drawable/namsan");

                uniquePlaceInfoList.add(place);
                //Main등록하기
                if(uniqueMainPlaceList.size() < 3)
                if(place.getFAC_NAME().equals("명동난타극장") || place.getFAC_NAME().equals("충무아트홀") || place.getFAC_NAME().equals("청계천박물관"))
                {
                    System.out.println("------------------find it!");
                    uniqueMainPlaceList.add(place);
                }
            }
            // listener.onDirectionFinderSuccess(routes);
        }
    }
    //데이터가져오기..

    public void createOkCancelDialog(Context context, String title, String contetn, int requestCode)
    {
        listener = (DialogCommonListener) context;          //되나??
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title);


        final int _requestCode = requestCode;

// AlertDialog 셋팅
        alertDialogBuilder
                .setMessage(contetn)
                .setCancelable(false)
                .setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //다음 액티비티로 이동
                               listener.onDialogOk(_requestCode);

                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //취소 버튼.
                                dialog.cancel();
                                listener.onDialogCancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void createOkDialog(Context context, String title, String content)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(content)
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(
                            DialogInterface dialog, int id) {
                        // 다이얼로그를 취소한다
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }
}
