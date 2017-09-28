package com.example.jjw.mydemo.lib.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jjw.mydemo.Main3Activity;
import com.example.jjw.mydemo.R;
import com.example.jjw.mydemo.SearchDetActivity;
import com.example.jjw.mydemo.lib.common.AppPlaceInfo;
import com.example.jjw.mydemo.lib.place.PlaceInfo;
import com.google.android.gms.vision.text.Text;

import java.util.ArrayList;


/**
 * Created by JJW on 2016-10-02.
 */
public class CustomViewPagerAdapter extends PagerAdapter {

    private ArrayList<Bitmap> bitmaps;
    private int[] image_resource = {R.drawable.ic_launcher, R.drawable.ic_launcher};
    private Context ctx;
    private LayoutInflater layoutInflater;
    private boolean mIsMain;
    private boolean mIsShared;

    public CustomViewPagerAdapter(Context ctx, boolean isMain, boolean isShared)
    {
        this.mIsMain = isMain;
        this.mIsShared = isShared;
        this.ctx = ctx;
        bitmaps = new ArrayList<Bitmap>();
    }

    public void setBitmaps(Bitmap bitmap)
    {

        bitmaps.add(bitmap);
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {

//        return image_resource.length;
        return bitmaps.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == (RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutInflater.inflate(R.layout.search_det_viewpager, container, false);

        ImageView imageView = (ImageView) item_view.findViewById(R.id.imagePager);
        TextView textView = (TextView) item_view.findViewById(R.id.imageCount);
        textView.setVisibility(View.GONE);

        //imageView.setImageResource(image_resource[position]);
        imageView.setImageBitmap(bitmaps.get(position));
        String tmp = "";

        if(mIsShared == true)
        {
             tmp = "해당장소는 공유된 장소입니다. [" + (position + 1) +"/"+getCount() + "]";
        }else {
             tmp = "["+(position + 1) + "/" + getCount()+"]";
        }
       // textView.setText(tmp);

        //메인인경우..
        if(mIsMain == true)
        {
            TextView txtdesc = (TextView) item_view.findViewById(R.id.txtViewPagerImgDesc);
            if(position == 0) {
                txtdesc.setText(ctx.getResources().getString(R.string.main1_img_desc));
            }
            else if(position == 1) {
                txtdesc.setText(ctx.getResources().getString(R.string.main2_img_desc));
            }
            else {
                txtdesc.setText(ctx.getResources().getString(R.string.main3_img_desc));
            }

            item_view.setTag(position);
            item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //페이저 클릭시 Det메뉴로 이동..단, 메인에서 호출되는지 확인..

                    int position = (Integer) v.getTag();
                    PlaceInfo clickedPlace = ((AppPlaceInfo)((Main3Activity)ctx).getApplication()).uniqueMainPlaceList.get(position);
                    // 인텐트를 생성한다.
                    // 컨텍스트로 현재 액티비티를, 생성할 액티비티로 ItemClickExampleNextActivity 를 지정한다.
                    //Basic Info를 클릭한다..
                    Intent intent = new Intent(ctx, SearchDetActivity.class);
                    intent.putExtra("placeType", 0);
                    intent.putExtra("isMain", mIsMain);
                    intent.putExtra("clickedPlace", clickedPlace);


                    // 액티비티를 생성한다.
                    //ctx.startActivity(intent);
                    ((Main3Activity) ctx).startActivityForResult(intent, ((Main3Activity)ctx).REQUEST_MAIN_PLACE);
                }
            });
        }else
        {
            TextView txtdesc = (TextView) item_view.findViewById(R.id.txtViewPagerImgDesc);
            txtdesc.setGravity(Gravity.CENTER_HORIZONTAL);
            txtdesc.setText(tmp);
        }





        container.addView(item_view);

        return item_view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout)object);
    }
}
