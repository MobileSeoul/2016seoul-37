package com.example.jjw.mydemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jjw.mydemo.lib.inf.PlaceFinderListener;
import com.example.jjw.mydemo.lib.inf.RefreshEventHandler;
import com.example.jjw.mydemo.lib.inf.RefreshEventListener;
import com.example.jjw.mydemo.lib.place.PlaceInfo;
import com.google.android.gms.location.places.PlaceTypes;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

//@SuppressLint("ValidFragment")
public class Tab1 extends Fragment implements RefreshEventListener {


    public final int PAGE_FULLY_NOT_FOUND = -1;
    public final int PAGE_NOT_FOUND = 0;
    public final int PAGE_FOUND = 1;



	private ArrayList<PlaceInfo> mShowItem;             //여기는 액티비티에서 받아온 실제로 보여줄 아이템들이다. (기본장소,공유 장소 상관없이)
    private ArrayList<PlaceInfo> mListShowItems;        //리스트에 보여지는 Place정보 (요청시 갱신되는 정보..)

    private ArrayList<PlaceInfo> mListShowItemsTmp;

    //Layout 설정
	private Context mContext;
	private ListView listView;
	private ProgressDialog pDialog;
	private CustomAdapter adapter;

    private PlaceFinderListener listener;
    private HashMap<String, Bitmap> mBitmapList;

    //화면 갱신 개수
    public static int LIST_VIEW_COUNT = 5;
    public boolean isPlaceAroundDistanceChanged = false;               // 주변 반경이 변경되었는 경우..
    public boolean isListRefresh = false;                              //리스트 뷰가 업데이트 되었는지 여부 ..
    public int placeType;                                                 //장소 타입에 따라 다르게 동작하도록 설정..

	public Tab1(PlaceFinderListener listener, Context context, ArrayList<PlaceInfo> listShowItem, int PlaceType)
	{
		mContext = context;
        //mListShowItems = listShowItem;
        mListShowItems = new ArrayList<PlaceInfo>();            //보여지는 부분은 새로 생성..
        mListShowItemsTmp  = new ArrayList<PlaceInfo>();
        this.listener = listener;
        RefreshEventHandler.addListener(this);
        mBitmapList = new HashMap<String, Bitmap>();      //배열 객체 생성.
        this.placeType = PlaceType;
	}




	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// TODO Auto-generated method stub

        //리스트 뷰생성하기..
		View view = inflater.inflate(R.layout.activity_tab1, container,false);
		listView = (ListView)view.findViewById(R.id.viewTab1);
        listView.addFooterView(((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listview_footer, null));

		adapter = new CustomAdapter(mContext, 0, mListShowItems);
		listView.setAdapter(adapter);

        TextView txtLoadmore = (TextView)view.findViewById(R.id.listview_footer);

        txtLoadmore.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                listener.onPlaceFinderStart();
                new loadMoreListView().execute();
            }
        });

		return view;
	}

    //리스트 어뎁터를 리프레쉬 한다..
    public void adapterRefresh()
    {
        //비트맵 정보를모두 제거한다.
        mBitmapList.clear();
        //어뎁터의 리스트를 모두 제거한다.
        adapter.refreshClearAdater();
    }

	public void refreshShowItems(ArrayList<PlaceInfo> items) throws UnsupportedEncodingException {


        mShowItem = items;

        execute();
    }

    public void refreshShowItemsbyTabChange(ArrayList<PlaceInfo> items) throws UnsupportedEncodingException {


        mShowItem = items;
        // new loadMoreListView().execute();
        //isFirstLoad가 true인경우
        // 1. 최초 생성 이후, 탭 2를 클릭한 경우.
        // 2. 탭 에서 거리를 변경한후, 탭을 옮긴 경우는 새로 계산 없이 뷰만 리프레쉬 해준다.
        // 3. 만약 1,2 사항이 아니면, 아무 변화 없음.. 이미 뷰의 아이템이 셋팅 된 상태이므로.
        if(isPlaceAroundDistanceChanged) {
            execute();
            isPlaceAroundDistanceChanged = false;
        }
    }


    @Override
    public void onRefreshEvent(String event) {
        listener.onPlaceFinderStart();
        new loadMoreListView().execute();
    }


   public void execute() throws UnsupportedEncodingException {
       listener.onPlaceFinderStart();
        new loadMoreListView().execute();
    }

    public ArrayList<PlaceInfo> getAdapterItems()
    {
        return adapter.getAllItems();
    }

    public Bitmap getBitmapFromString(String jsonString) {
/*
* This Function converts the String back to Bitmap
* */
        byte[] decodedString = Base64.decode(jsonString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //배열 정리 하기ㅣ..

        mBitmapList.clear();
    }






    //리스트 뷰 아답터..

    private class CustomAdapter extends ArrayAdapter<PlaceInfo>   {

		private ArrayList<PlaceInfo> items;         //private Vector<PlaceInfo> items;
		//private ArrayList<Bitmap> bitmapList;
		private Context mContext;

		//private String[] items;
		public CustomAdapter(Context context, int textViewResourceId, ArrayList<PlaceInfo> objects) {
			super(context, textViewResourceId, objects);
			//this.items = objects;
			this.items = objects;
			this.mContext = context;
		}

		@Override
		public int getCount() {

           // System.out.println("------------Tab1 customAdapter.. getCount - items count : " + items.size() + ", placeType : " + placeType);
            int count = 0;
            try {
                count = items.size();
            }catch(IllegalStateException e)
            {
                e.printStackTrace();
              //  System.out.println("----------------------ilelgalstate exception " );
                return 0;
            }
            return count;
		}

        public void addItems(int s, int e)
        {
            for(int i = s ; i < e ; i++)
            {
                items.add(mShowItem.get(i));
            }
            //items = mShowItems;
            notifyDataSetChanged();
        }



		//우선 하드코딩으로 하기..
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null) {
				LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.custom_lit_item, null);

                //리스트뷰 클릭 시 이벤트 발생..
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //perform action
                        int position = (Integer) v.getTag();
                        PlaceInfo clickedPlace = items.get(position);
                        // 인텐트를 생성한다.
                        // 컨텍스트로 현재 액티비티를, 생성할 액티비티로 ItemClickExampleNextActivity 를 지정한다.
                        //Basic Info를 클릭한다..
                        Intent intent = new Intent(mContext, SearchDetActivity.class);
                        intent.putExtra("placeType", placeType);
                        intent.putExtra("clickedPlace", clickedPlace);
                        // 액티비티를 생성한다.
                        mContext.startActivity(intent);

                    }
                });
			}
			v.setTag(position);

            ImageView imageView = (ImageView)v.findViewById(R.id.imageView);

            //리스트뷰의 아이템에 이미지를 변경한다.

            PlaceInfo info = items.get(position);

            TextView textCnt = (TextView)v.findViewById(R.id.listCnt);
            textCnt.setText(Integer.toString(position));

            if(mBitmapList == null)
            {
                //비트맵 리스트에 아무것도 없는경우..처리 하지 않는다..
            }else {

                //해쉬맵에서 해당 시설이름에 해당되는 이미지를 가져온다..
                imageView.setImageBitmap(mBitmapList.get(items.get(position).getFAC_NAME()));

            }

            //imageView.setImageBitmap(info.getBitmap());

            TextView textView = (TextView)v.findViewById(R.id.textView);
            textView.setText(info.getFAC_NAME());
            TextView txtCodeName = (TextView)v.findViewById(R.id.textView2);
            txtCodeName.setText(info.getCODENAME());
            CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkTest);
            checkBox.setChecked(info.isChecked());
            checkBox.setTag(position);
            checkBox.setOnClickListener(buttonClickListener);

            return v;
		}
        //CheckBox클릭시 이벤트 설정
        private View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                switch (v.getId()) {

                    // 이미지 클릭
                    case R.id.checkTest:
                        CheckBox tmp = (CheckBox)v.findViewById(R.id.checkTest);
                        System.out.println("------------Checkbox clicekd..status : " + tmp.isChecked() + ", position : " +position);
                        items.get(position).setChecked(tmp.isChecked());
                        break;
                }
            }
        };

        public ArrayList<PlaceInfo> getAllItems()
        {
            return this.items;
        }

//        public void setBitmapList(ArrayList<Bitmap> bitmapList) {
//            mBitmapList = bitmapList;
//            refreshAdater();
//        }

        //리스트 뷰를 리프레쉬 한다.
        public void refreshAdater() {

            notifyDataSetChanged();
        }

        //아이템을 받아서 해당 아이템으로 리프레시 한다.
        public void refreshAdater(ArrayList<PlaceInfo> items) {

           // System.out.println("-------------refreshAdapter after asynprocess.... param items : " + items + ", inner items : " + this.items);
//            this.items.clear();
//            refreshAdater();

            this.items = items;
           // System.out.println("-------------refreshAdapter after inputting.... param items : " + items + ", inner items : " + this.items);
            refreshAdater();
        }

        //아이템을 클리어 한뒤 리프레쉬 한다..
        public void refreshClearAdater()
        {
            this.items.clear();
            refreshAdater();
        }
    }



    //AsyncLoad
    private class loadMoreListView extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... unused) {
            int currentListViewCount = mListShowItems.size();
            if(mListShowItems.size() > 0)
                mListShowItemsTmp.addAll(mListShowItems);
            //1. 현재 뷰에보여지는 갯수가 전체 주변경로의 개수와 같으면 더이상 페이지가 존재 하지않는다.
            if(currentListViewCount >= mShowItem.size())
            {
                return PAGE_FULLY_NOT_FOUND;          //페이지가 없는 경우.
            }

            //2. 페이지를 검색한다.
            // - 현재 보여지는 리스트의 개수에서 매번 LIST_VIEW_COUNT 개수 만큼 리스트를 추가한다.
            for(int i = currentListViewCount ; i < currentListViewCount + LIST_VIEW_COUNT ; i++)
            {
                Bitmap bitmap = null;
                //오버바운드 체크
                if(i >= mShowItem.size()){

                    return PAGE_NOT_FOUND;       //로딩 하다가 페이지가 없는 경우..
                }

                try {

                    if(placeType == 0) {
                        //System.out.println("---------main img : " + mShowItem.get(i).getMAIN_IMG());
                        if("null".equals(mShowItem.get(i).getMAIN_IMG()))
                        {
                            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_no_photo);
                        }else
                        {
                            bitmap = BitmapFactory.decodeStream((InputStream) new URL(mShowItem.get(i).getMAIN_IMG()).getContent());
                        }
                    }else{
                       // System.out.println("--placeTYpe " + placeType + ", FacName : " + mShowItem.get(i).getFAC_NAME());

                        if(("null".equals(mShowItem.get(i).getImgSrc())) || mShowItem.get(i).getImgSrc() == null){
                            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_no_photo);
                        }else {
                            bitmap = getBitmapFromString(mShowItem.get(i).getImgSrc());
                        }
                    }

                    if(bitmap == null)
                    {
                        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_no_photo);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return PAGE_FULLY_NOT_FOUND;
                }
                //여기에, FAC_NAME기반의 해쉬맵을 사용한다..
                mBitmapList.put(mShowItem.get(i).getFAC_NAME(), bitmap);
                mListShowItemsTmp.add(mShowItem.get(i));
            }

              return PAGE_FOUND;
        }



        //더보기가 끝난 뒤 UI 작업
        @Override
        protected void onPostExecute(Integer value) {
            // closing progress dialog
            if(value == PAGE_FULLY_NOT_FOUND) {
                mListShowItems.clear();
                mListShowItems.addAll(mListShowItemsTmp);
                //...check..

                adapter.refreshAdater(mListShowItems);
                listener.onPlaceFinderFailed();
                mListShowItemsTmp.clear();
            }else
            {
                mListShowItems.clear();
                mListShowItems.addAll(mListShowItemsTmp);
                //...check..

                adapter.refreshAdater(mListShowItems);
                listener.onPlaceFinderSuccess(mListShowItems);
                mListShowItemsTmp.clear();
            }

        }
    }
}