package com.example.jjw.mydemo.lib.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jjw.mydemo.R;
import com.example.jjw.mydemo.lib.place.PlaceInfo;
import com.example.jjw.mydemo.lib.place.PostSharedPlace;
import com.google.android.gms.location.places.Place;

import java.util.ArrayList;

/**
 * Created by JJW on 2016-10-12.
 * 해당 Adpater는 PlanStsActivity에 보여지는 리사이클러 뷰의 Adapter이다.
 */
public class PlanStsListAdapter extends RecyclerView.Adapter<PlanStsListAdapter.ViewHolder> implements View.OnClickListener {
    public static final int BASIC_PLACE = 0;
    public static final int SHARED_PLACE = 1;

    private ArrayList<PlaceInfo> mPlaceItems;           //Basic Place
    private ArrayList<Bitmap> mBitmapList;
    private Context mContext;
    private Resources res;

    private int PlaceType;

    private static int TYPE_HEADER = 0;
    private static int TYPE_FOOTER = 3;


    public PlanStsListAdapter(ArrayList<PlaceInfo> items, ArrayList<Bitmap> bitmaps, Context ctx) {
        //System.out.println("----PlanStsListAdapter... itemsize : " + items.size() + ", PlaceType : " + PlaceType);

      //  System.out.println("----PlanStsListAdapter... itemsize : " + items.size() + ", PlaceType : " + PlaceType);
        this.mPlaceItems = items;
        this.mBitmapList = bitmaps;
        this.mContext = ctx;
        //this.mSharedPlaceItems = sharedPlaceItems;
        // this.mContext = mContext;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PlanStsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan_sts, null);

        // create ViewHolder

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    //여기에 컨텐츠를 표시한다.. 확인필요..
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData
     //   System.out.println("----PlaceType...." + PlaceType);

        try {
            if (mPlaceItems.isEmpty() || mPlaceItems == null) {
                viewHolder.txtViewTitle.setText("페이지가 존재 하지 않습니다.");
                viewHolder.imgViewIcon.setImageResource(R.drawable.ic_no_photo);
                viewHolder.txtViewDesc.setText("");
            } else {
                viewHolder.txtViewTitle.setText(mPlaceItems.get(position).getFAC_NAME());
                // viewHolder.imgViewIcon.setImageResource(R.drawable.busan);
                viewHolder.imgViewIcon.setImageBitmap(mBitmapList.get(position));
                viewHolder.txtViewDesc.setText(mPlaceItems.get(position).getADDR());
            }
        }catch(Exception e)
        {
            e.printStackTrace();
            String content = mContext.getResources().getString(R.string.common_loading_error);
            Toast.makeText(mContext, content,
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {

    }


    // inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtViewTitle;
        public ImageView imgViewIcon;
        public TextView txtViewDesc;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txtViewTitle = (TextView) itemLayoutView.findViewById(R.id.txtPlanStsTitle);
            imgViewIcon = (ImageView) itemLayoutView.findViewById(R.id.imageViewPlanSts);
            txtViewDesc = (TextView) itemLayoutView.findViewById(R.id.txtPlanStsDesc);
        }
    }


    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        int count;

        count = mPlaceItems.size();

        return count;
    }
}
