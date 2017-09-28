package com.example.jjw.mydemo.lib.inf;

import com.example.jjw.mydemo.lib.place.PlaceInfo;
import com.example.jjw.mydemo.lib.place.PostSharedPlace;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by JJW on 2016-10-17.
 */
public interface PlaceFinderListener {
    void onPlaceFinderStart();              //PlaceFinder 시작
    void onPlaceFinderSuccess(ArrayList<PlaceInfo> items);            //Place Finder 성공
    void onSharedPlaceFinderSuccess(ArrayList<PostSharedPlace> items);       //SharedPlace 성공시..
    void onPlaceFinderFailed();
}
