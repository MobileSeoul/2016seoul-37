package com.example.jjw.mydemo.lib.direction;

import java.util.List;



/**
 * Created by Mai Thanh Hiep on 4/3/2016.
 * 오픈소스 사용
 */
public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
