package com.example.jjw.mydemo.lib.place;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JJW on 2016-10-09.
 */
public class PostSharedPlace implements Serializable {

    private String Uid;
    private String Author;
    private String CodeName;
    private String FacName;
    private String FacDesc;

    private String X_Coord;
    private String Y_Coord;

    private String Img_Src;

    private boolean isChecked;      //체크 여부

    //private Bitmap bitmap;

    public PostSharedPlace() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public PostSharedPlace(String uid, String author, String codeName, String facName, String facDesc, String x_Coord, String y_Coord, String img_Src) {
        Uid = uid;
        Author = author;
        CodeName = codeName;
        FacName = facName;
        FacDesc = facDesc;
        X_Coord = x_Coord;
        Y_Coord = y_Coord;
        Img_Src = img_Src;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Uid", Uid);
        result.put("Author", Author);
        result.put("CodeName", CodeName);
        result.put("FacName", FacName);
        result.put("FacDesc", FacDesc);
        result.put("X_Coord", X_Coord);
        result.put("Y_Coord", Y_Coord);

        return result;
    }
    // [END post_to_map]


    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getCodeName() {
        return CodeName;
    }

    public void setCodeName(String codeName) {
        CodeName = codeName;
    }

    public String getFacName() {
        return FacName;
    }

    public void setFacName(String facName) {
        FacName = facName;
    }

    public String getFacDesc() {
        return FacDesc;
    }

    public void setFacDesc(String facDesc) {
        FacDesc = facDesc;
    }

    public String getX_Coord() {
        return X_Coord;
    }

    public void setX_Coord(String x_Coord) {
        X_Coord = x_Coord;
    }

    public String getY_Coord() {
        return Y_Coord;
    }

    public void setY_Coord(String y_Coord) {
        Y_Coord = y_Coord;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getImg_Src() {
        return Img_Src;
    }

    public void setImg_Src(String img_Src) {
        Img_Src = img_Src;
    }

/*    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }*/
}
