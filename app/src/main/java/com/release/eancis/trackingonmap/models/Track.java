package com.release.eancis.trackingonmap.models;

/**
 * Created by Enrico Ancis on 29/05/18.
 */

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * The data model to retain the data of the tracks
 * */

public class Track {

    private long mID;
    //For the goals of the app it only needs to show the start and the end of the track time,
    //no operations have needed on then, times can be saved as strings
    private String mStartTime;
    private String mEndTime;
    private List<LatLng> mPath;
    private int mPathColor;

    public Track(){
        mID = -1;
        mStartTime = null;
        mEndTime = null;
        mPath = null;
        mPathColor = -1;
    }

    public Track(long _id,
                 String _sTime,
                 String _eTime,
                 List<LatLng> _path,
                 int _pColor){

        mID = _id;
        mStartTime = _sTime;
        mEndTime = _eTime;
        mPath = _path;
        mPathColor = _pColor;
    }

    /**
     *  ID
     * */
    public void setID(long _id){
        mID = _id;
    }

    public long getID(){
        return mID;
    }

    /**
     *  Start time
     * */
    public void setStartTime(String _sTime){
        mStartTime = _sTime;
    }

    public String getStartTime(){
        return mStartTime;
    }

    /**
     *  End time
     * */
    public void setEndTime(String _eTime){
        mEndTime = _eTime;
    }

    public String getEndTime(){
        return mEndTime;
    }

    /**
     *  Path
     * */
    public void setPath(List<LatLng> _path){
        mPath = _path;
    }

    public List<LatLng> getPath(){
        return mPath;
    }

    //Type converter
    public String pathToString() {

        Gson gson = new Gson();

        String json = gson.toJson(mPath);

        return json;

    }

    public void setPathFromString(String json){
        Gson gson = new Gson();

        mPath = gson.fromJson(json, new TypeToken<List<LatLng>>(){}.getType());
    }

    /**
     /*     *  Path color
     * */
    public void setPathColor(int _pColor){
        mPathColor = _pColor;
    }

    public int getPathColor(){
        return mPathColor;
    }

}

