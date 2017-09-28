package com.example.jjw.mydemo.lib.common;

import com.example.jjw.mydemo.lib.place.PlaceInfo;

import java.util.ArrayList;

/**
 * Created by JJW on 2016-10-04.
 * 사용자 정보, 사용자 장소 등록정보, 최근 출도착지 정보 관리
 */
public class UserInfo {

    private String userId;
    private String userEmail;
    public ArrayList<PlaceInfo> uniquePlanedPlaceInfoList;                  //기본 정보의 여행계획 정보
    public ArrayList<PlaceInfo> uniquePlanedSharedPlaceInfoList;      // 공유정보의 여행계획 정보
    private String recentStart;
    private String recentDestination;

    public UserInfo()
    {
        this.userEmail = null;
        this.userId = null;
        uniquePlanedPlaceInfoList = new ArrayList<PlaceInfo> ();
        uniquePlanedSharedPlaceInfoList = new ArrayList<PlaceInfo>();
        recentDestination = null;
        recentStart = null;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }




    public String getRecentStart() {
        return recentStart;
    }

    public void setRecentStart(String recentStart) {
        this.recentStart = recentStart;
    }

    public String getRecentDestination() {
        return recentDestination;
    }

    public void setRecentDestination(String recentDestination) {
        this.recentDestination = recentDestination;
    }


    public ArrayList<PlaceInfo> getMergedPlanList()
    {
        ArrayList<PlaceInfo> mergedPlanList = new ArrayList<PlaceInfo>();

        mergedPlanList.addAll(uniquePlanedPlaceInfoList);
        mergedPlanList.addAll(uniquePlanedSharedPlaceInfoList);

        return mergedPlanList;
    }


    public void signOut()
    {
        this.userEmail = null;
        this.userId = null;
        uniquePlanedPlaceInfoList.clear();
        uniquePlanedSharedPlaceInfoList.clear();
        recentDestination = null;
        recentStart = null;
    }
}
