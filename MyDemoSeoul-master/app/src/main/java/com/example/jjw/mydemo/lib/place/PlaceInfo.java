package com.example.jjw.mydemo.lib.place;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by JJW on 2016-09-30.
 */
public class PlaceInfo implements Serializable {

    private String CODENAME; //장르분류명
    private String FAC_NAME ; //문화공간명
    private String ETC_DESC; //기타사항
    private String SUBJCODE; //장르분류코드
    private String SEAT_CNT; //객석수",
    private String PHNE;    //전화번호
    private String OPENHOUR;    //관람시간","
    private String X_COORD;    //X좌표","
    private String ENTRFREE;    //무료구분","
    private String MAIN_IMG;    //대표이미지","
    private String Y_COORD;    //Y좌표","
    private String ENTR_FEE;    //관람료(원)","
    private String FAC_CODE;    //문화공간코드","
    private String CLOSEDAY;    //휴관일","
    private String OPEN_DAY;    //개관일자","
    private String ADDR;    //주소","
    private String HOMEPAGE;    //홈페이지","
    private String FAX;    //팩스번호"

    private boolean isChecked;      //체크 여부
    private String imgSrc;

    //Shared용
    private String Uid;
    private String Author;
    private boolean isShared;

    //신규추가
   // private Bitmap bitmap;


    public PlaceInfo()
    {
        isShared = false;
    }


    public PlaceInfo(String CODENAME, String FAC_NAME, String ETC_DESC, String SUBJCODE, String SEAT_CNT, String PHNE, String OPENHOUR, String x_COORD, String ENTRFREE, String MAIN_IMG, String y_COORD, String ENTR_FEE, String FAC_CODE, String CLOSEDAY, String OPEN_DAY, String ADDR, String HOMEPAGE, String FAX) {
        this.CODENAME = CODENAME;
        this.FAC_NAME = FAC_NAME;
        this.ETC_DESC = ETC_DESC;
        this.SUBJCODE = SUBJCODE;
        this.SEAT_CNT = SEAT_CNT;
        this.PHNE = PHNE;
        this.OPENHOUR = OPENHOUR;
        X_COORD = x_COORD;
        this.ENTRFREE = ENTRFREE;
        this.MAIN_IMG = MAIN_IMG;
        Y_COORD = y_COORD;
        this.ENTR_FEE = ENTR_FEE;
        this.FAC_CODE = FAC_CODE;
        this.CLOSEDAY = CLOSEDAY;
        this.OPEN_DAY = OPEN_DAY;
        this.ADDR = ADDR;
        this.HOMEPAGE = HOMEPAGE;
        this.FAX = FAX;
        this.isShared = false;
    }

    public PlaceInfo(String uid, String author, String codeName, String facName, String facDesc, String x_Coord, String y_Coord, String img_Src, String addr) {

        this.Uid = uid;
        this.Author = author;
        this.CODENAME = codeName;
        this.FAC_NAME = facName;
        this.ETC_DESC = facDesc;
        this.X_COORD = x_Coord;
        this.Y_COORD = y_Coord;
        this.imgSrc = img_Src;
        this.ADDR = addr;
        this.isShared = true;

    }

    public String getCODENAME() {
        return CODENAME;
    }

    public void setCODENAME(String CODENAME) {
        this.CODENAME = CODENAME;
    }

    public String getFAC_NAME() {
        return FAC_NAME;
    }

    public void setFAC_NAME(String FAC_NAME) {
        this.FAC_NAME = FAC_NAME;
    }

    public String getETC_DESC() {
        return ETC_DESC;
    }

    public void setETC_DESC(String ETC_DESC) {
        this.ETC_DESC = ETC_DESC;
    }

    public String getSUBJCODE() {
        return SUBJCODE;
    }

    public void setSUBJCODE(String SUBJCODE) {
        this.SUBJCODE = SUBJCODE;
    }

    public String getSEAT_CNT() {
        return SEAT_CNT;
    }

    public void setSEAT_CNT(String SEAT_CNT) {
        this.SEAT_CNT = SEAT_CNT;
    }

    public String getPHNE() {
        return PHNE;
    }

    public void setPHNE(String PHNE) {
        this.PHNE = PHNE;
    }

    public String getOPENHOUR() {
        return OPENHOUR;
    }

    public void setOPENHOUR(String OPENHOUR) {
        this.OPENHOUR = OPENHOUR;
    }

    public String getX_COORD() {
        return X_COORD;
    }

    public void setX_COORD(String x_COORD) {
        X_COORD = x_COORD;
    }

    public String getENTRFREE() {
        return ENTRFREE;
    }

    public void setENTRFREE(String ENTRFREE) {
        this.ENTRFREE = ENTRFREE;
    }

    public String getMAIN_IMG() {
        return MAIN_IMG;
    }

    public void setMAIN_IMG(String MAIN_IMG) {
        this.MAIN_IMG = MAIN_IMG;
    }

    public String getY_COORD() {
        return Y_COORD;
    }

    public void setY_COORD(String y_COORD) {
        Y_COORD = y_COORD;
    }

    public String getENTR_FEE() {
        return ENTR_FEE;
    }

    public void setENTR_FEE(String ENTR_FEE) {
        this.ENTR_FEE = ENTR_FEE;
    }

    public String getFAC_CODE() {
        return FAC_CODE;
    }

    public void setFAC_CODE(String FAC_CODE) {
        this.FAC_CODE = FAC_CODE;
    }

    public String getCLOSEDAY() {
        return CLOSEDAY;
    }

    public void setCLOSEDAY(String CLOSEDAY) {
        this.CLOSEDAY = CLOSEDAY;
    }

    public String getOPEN_DAY() {
        return OPEN_DAY;
    }

    public void setOPEN_DAY(String OPEN_DAY) {
        this.OPEN_DAY = OPEN_DAY;
    }

    public String getADDR() {
        return ADDR;
    }

    public void setADDR(String ADDR) {
        this.ADDR = ADDR;
    }

    public String getHOMEPAGE() {
        return HOMEPAGE;
    }

    public void setHOMEPAGE(String HOMEPAGE) {
        this.HOMEPAGE = HOMEPAGE;
    }

    public String getFAX() {
        return FAX;
    }

    public void setFAX(String FAX) {
        this.FAX = FAX;
    }


    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }


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

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }

/*
    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
*/

    @Override
    public String toString() {
        return "PlaceInfo{" +
                "CODENAME='" + CODENAME + '\'' +
                ", FAC_NAME='" + FAC_NAME + '\'' +
                '}';
    }
}
