package com.example.jjw.mydemo.lib.place;

import java.util.Comparator;

/**
 * Created by JJW on 2016-10-22.
 */
public class PlaceInfoComparator implements Comparator<PlaceInfo>{

    @Override
    public int compare(PlaceInfo lhs, PlaceInfo rhs) {
        String firstName = lhs.getFAC_NAME();
        String secondName = rhs.getFAC_NAME();

        if(firstName.compareTo(secondName) > 0)
        {
            return -1;
        }else if(firstName.compareTo(secondName) < 0)
        {
            return 1;
        }else {
            return 0;
        }
    }
}
