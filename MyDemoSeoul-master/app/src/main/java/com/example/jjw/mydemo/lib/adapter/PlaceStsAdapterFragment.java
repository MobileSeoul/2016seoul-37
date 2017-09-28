package com.example.jjw.mydemo.lib.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jjw.mydemo.R;
import com.example.jjw.mydemo.lib.inf.PlaceFinderListener;
import com.example.jjw.mydemo.lib.inf.RefreshEventHandler;
import com.example.jjw.mydemo.lib.place.PlaceInfo;
import com.example.jjw.mydemo.lib.place.PostSharedPlace;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by JJW on 2016-10-21.
 */
public class PlaceStsAdapterFragment extends Fragment{


    private ArrayList<PlaceInfo> mShowItem;             //여기는 액티비티에서 받아온 실제로 보여줄 아이템들이다.
       //Layout 설정
    private Context mContext;

    private PlanStsTab1ListAdapter adapter;
    private RecyclerView mPlanStstRecyclerView;
    private PlaceFinderListener listener;
    private ArrayList<Bitmap> mBitmapList;

    //화면 갱신 개수
    public static int LIST_VIEW_COUNT = 5;
    public static boolean isPlaceChanged = true;

    public PlaceStsAdapterFragment(PlaceFinderListener listener, Context context, ArrayList<PlaceInfo> listShowItem)
    {
        mContext = context;
        mShowItem = listShowItem;
        this.listener = listener;

        mBitmapList = new ArrayList<Bitmap>();      //배열 객체 생성.
        //System.out.println("------------Tab1 Constructor ShowItems count : " + mShowItem.size() + ", Item Address : " + mShowItem);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        View rootView = inflater.inflate(R.layout.fragment_plan_sts2, container, false);

        mPlanStstRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_viewPlanSts2);

        mPlanStstRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new PlanStsTab1ListAdapter(mShowItem, 0, mContext);

        //4. set adapter
        mPlanStstRecyclerView.setAdapter(adapter);

        //5. set item animator to default animator
        mPlanStstRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    public void adapterRefresh()
    {
        mBitmapList.clear();

    }

    public void refreshShowItems(ArrayList<PlaceInfo> items) throws UnsupportedEncodingException {


        mShowItem = items;
        System.out.println("------------Tab1 ShowItems count : " + mShowItem.size()  + ", Item Address : " + mShowItem);
        // new loadMoreListView().execute();

        execute();
    }

    public void refreshShowItemsbyTabChange(ArrayList<PlaceInfo> items) throws UnsupportedEncodingException {


        mShowItem = items;
        System.out.println("------------Tab2 ShowItems count : " + mShowItem.size()  + ", Item Address : " + mShowItem);
        // new loadMoreListView().execute();
        //isFirstLoad가 true인경우
        // 1. 최초 생성 이후, 탭 2를 클릭한 경우.
        // 2. 탭 에서 거리를 변경한후, 탭을 옮긴 경우는 새로 계산 없이 뷰만 리프레쉬 해준다.
        // 3. 만약 1,2 사항이 아니면, 아무 변화 없음.. 이미 뷰의 아이템이 셋팅 된 상태이므로.
        if(isPlaceChanged) {
            execute();
            isPlaceChanged = false;
        }
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onPlaceFinderStart();
        new loadMoreListView().execute();
    }


    public class PlanStsTab1ListAdapter extends RecyclerView.Adapter<PlanStsTab1ListAdapter.ViewHolder> implements View.OnClickListener {

        /**
         * Created by JJW on 2016-10-12.
         * 해당 Adpater는 PlanStsActivity에 보여지는 리사이클러 뷰의 Adapter이다.
         */
        public static final int BASIC_PLACE = 0;
        public static final int SHARED_PLACE = 1;

        private ArrayList<PlaceInfo> mPlaceItems;           //Basic Place
        private ArrayList<Bitmap> mBitmapList;              //Bitmap..

        //private Context mContext;
        private Resources res;

        private int PlaceType;

        private Context mContext;

        public PlanStsTab1ListAdapter(ArrayList<PlaceInfo> items, int PlaceType, Context context) {
            System.out.println("----PlanStsListAdapter... itemsize : " + items.size() + ", PlaceType : " + PlaceType);

            this.mPlaceItems = items;
            //this.mSharedPlaceItems = sharedPlaceItems;
            this.mBitmapList = new ArrayList<Bitmap>();
            this.PlaceType = PlaceType;
            this.mContext = context;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public PlanStsTab1ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View itemLayoutView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_plan_sts, null);

            // create ViewHolder

            ViewHolder viewHolder = new ViewHolder(itemLayoutView);
            return viewHolder;
        }

        // Replace the contents of a view (invoked by the layout manager)
        //여기에 컨텐츠를 표시한다..
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {

            // - get data from your itemsData at this position
            // - replace the contents of the view with that itemsData
            System.out.println("----PlaceType...." + PlaceType);
            if (PlaceType == BASIC_PLACE) {
                if (mPlaceItems.isEmpty() || mPlaceItems == null) {
                    viewHolder.txtViewTitle.setText("페이지가 존재 하지 않습니다.");
                    viewHolder.imgViewIcon.setImageResource(R.drawable.ic_launcher);
                } else {
                    viewHolder.txtViewTitle.setText(mPlaceItems.get(position).getFAC_NAME());
                    viewHolder.imgViewIcon.setImageResource(R.drawable.ic_launcher);
                }
            }
        }

        @Override
        public void onClick(View v) {

        }

        public void execute() {
            new loadMoreListView().execute();
        }


        // inner class to hold a reference to each item of RecyclerView
        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView txtViewTitle;
            public ImageView imgViewIcon;

            public ViewHolder(View itemLayoutView) {
                super(itemLayoutView);
                txtViewTitle = (TextView) itemLayoutView.findViewById(R.id.txtPlanStsTitle);
                imgViewIcon = (ImageView) itemLayoutView.findViewById(R.id.imageViewPlanSts);
            }
        }


        // Return the size of your itemsData (invoked by the layout manager)
        @Override
        public int getItemCount() {

            return mPlaceItems.size();
        }
    }


    //AsyncLoad
    private class loadMoreListView extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            // Showing progress dialog before sending http request
/*            pDialog = new ProgressDialog(mContext);
        pDialog.setMessage("Please wait..");
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);
        pDialog.show();*/
        }

        @Override
        protected Integer doInBackground(Void... unused) {
            System.out.println("---------------Tab1 doInbackground..");
            // Next page request

            for (int i = 0; i < mShowItem.size(); i++) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(mShowItem.get(i).getMAIN_IMG()).getContent());
                    if (bitmap == null) {
                        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_no_photo);
                    }
                    mBitmapList.add(bitmap);         //이거다시 조절필요..
                    System.out.println("---------bitmap : " + bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return 1;
        }

        //더보기가 끝난 뒤 UI 작업
        @Override
        protected void onPostExecute(Integer value) {
            // closing progress dialog
/*            System.out.println("-=------------onPostExecute..");
        if (value == -1) {
            listener.onPlaceFinderFailed();
        } else {
            adapter.refreshAdater();
            listener.onPlaceFinderSuccess(mListShowItems);
        }*/


        }
    }

}
