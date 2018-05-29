package com.release.eancis.trackingonmap.controllers;

/**
 * Created by Enrico Ancis on 29/05/18.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.release.eancis.trackingonmap.data.TracksContract;
import com.release.eancis.trackingonmap.data.TracksDbHelper;
import com.release.eancis.trackingonmap.models.Track;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The controller to handle the datas from the model to the views
 * */

public class DataController {
    private static final String TAG = DataController.class.getSimpleName();

    private Track mCurrentTrack;
    private TracksDbHelper mTrackDbHelper;
    private Cursor mCursor;

    public DataController(Context context){
        mCurrentTrack = new Track();
        mTrackDbHelper = new TracksDbHelper(context);
        mCursor = null;
    }

    /**
     * Return the current track instantiated in this controller
     *
     * @return mCurrentTrack current track
     */
    public Track getCurrentTrack(){
        return mCurrentTrack;
    }

    /**
     * Return the cursor of the tracks list query on db
     *
     * @return mCursor tracks list cursor
     */
    public Cursor getTracksListCursor(){
        return mCursor;
    }

    /**
     * Query the mDb and get all tracks from the Tracks table.
     * In this app the tracks are inserted simultaneously to the recording stop
     * The Id order corresponds to the timing order
     *
     * @return Cursor containing the list of tracks
     */
    public Cursor getAllTracks() {
        Cursor cursor = null;
        SQLiteDatabase db = mTrackDbHelper.getWritableDatabase();

        try{
            mCursor = db.query(
                    TracksContract.TracksEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    TracksContract.TracksEntry._ID
            );
            cursor = mCursor;
        }
        catch(Exception e){
            Log.d(TAG, e.getMessage());
        }

        return cursor;
    }

    /**
     *Get track data from db by id and create Track object
     *
     *
     * @return track track created form the data retrived from db
     *
     */
    public Track getTrackById(long id){
        Track track;
        Cursor cursor = null;
        SQLiteDatabase db = mTrackDbHelper.getWritableDatabase();

        try{
            cursor = db.query(
                    TracksContract.TracksEntry.TABLE_NAME,
                    null,
                    TracksContract.TracksEntry._ID + " = " + id,
                    null,
                    null,
                    null,
                    null
            );
            track = new Track();
            track.setID(id);
            track.setStartTime(cursor.getString(cursor.getColumnIndex(TracksContract.TracksEntry.COLUMN_START_TIME)));
            track.setEndTime(cursor.getString(cursor.getColumnIndex(TracksContract.TracksEntry.COLUMN_END_TIME)));
            track.setPathFromString(cursor.getString(cursor.getColumnIndex(TracksContract.TracksEntry.COLUMN_PATH)));
            track.setPathColor(cursor.getInt(cursor.getColumnIndex(TracksContract.TracksEntry.COLUMN_PATH_COLOR)));
        }
        catch(Exception e){
            track = null;
            Log.d(TAG, e.getMessage());
        }

        return track;
    }

    /**
     * Adds a new guest to the mDb including the party count and the current timestamp
     *
     * @return id of new record added
     */
    public long addNewTrack() {
        long newId = -1;
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = mTrackDbHelper.getWritableDatabase();

        try{
            cv.put(TracksContract.TracksEntry.COLUMN_START_TIME, mCurrentTrack.getStartTime());
            cv.put(TracksContract.TracksEntry.COLUMN_END_TIME, mCurrentTrack.getEndTime());
            cv.put(TracksContract.TracksEntry.COLUMN_PATH, mCurrentTrack.pathToString());
            cv.put(TracksContract.TracksEntry.COLUMN_PATH_COLOR, mCurrentTrack.getPathColor());
            newId = db.insert(TracksContract.TracksEntry.TABLE_NAME, null, cv);

        }
        catch (Exception e){
            Log.d(TAG, e.getMessage());
        }
        return newId;
    }

    /**
     * Adds a new guest to the mDb including the party count and the current timestamp
     *
     * @param id  track id to retrive
     * @param endTime  track end time to update to the track in db
     * @return rowAffected number of rows updated in db
     */
    public long closeTrack(long id, String endTime, List<LatLng> path) {
        int rowAffected;
        ContentValues cv = new ContentValues();

        mCurrentTrack.setEndTime(endTime);
        mCurrentTrack.setPath(path);

        cv.put(TracksContract.TracksEntry.COLUMN_END_TIME, mCurrentTrack.getEndTime() );
        cv.put(TracksContract.TracksEntry.COLUMN_PATH, mCurrentTrack.pathToString() );
        SQLiteDatabase db = mTrackDbHelper.getWritableDatabase();

        try{
            rowAffected = db.update(TracksContract.TracksEntry.TABLE_NAME,
                    cv,
                    TracksContract.TracksEntry._ID + "=" + id,
                    null);

        }
        catch (Exception e){
            rowAffected = -1;
            Log.d(TAG, e.getMessage());
        }
        return rowAffected;
    }

    /**
     * Adds a new guest to the mDb including the party count and the current timestamp
     *
     * @param startTime track start time
     * @param endTime  track end time
     * @param path map path track
     * @param pathColor path color
     * @return track just created
     */
    public long createTrack(String startTime, String endTime, List<LatLng> path, int pathColor) {

        Track track = new Track();

        track.setStartTime(startTime);
        track.setEndTime(endTime);
        track.setPath(path);
        track.setPathColor(pathColor);

        track.setID(addNewTrack());
        mCurrentTrack = track;

        return track.getID();
    }

    /**
     *It load a dictionary all the paths with their polylone color
     * */
    public Map<Integer, List<LatLng>> getAllTracksList(Context context){
        Gson gson = new Gson();

        //Here it' used a dictionary to store the list of color and its list ov laglng togheter
        Map<Integer, List<LatLng>> tracksDictionary =  new HashMap<Integer, List<LatLng>>();
        if(mCursor != null){
            //to debug
            mCursor.moveToFirst();
        }else{
            mCursor = getAllTracks();
            try{
                while (mCursor.moveToNext()){
                    String json = mCursor.getString(mCursor.getColumnIndex(TracksContract.TracksEntry.COLUMN_PATH));
                    int color = mCursor.getInt(mCursor.getColumnIndex(TracksContract.TracksEntry.COLUMN_PATH_COLOR));
                    List<LatLng> points = gson.fromJson(json, new TypeToken<List<LatLng>>(){}.getType());
                    tracksDictionary.put(color, points);
                }
            }
            catch(Exception e){
                Log.d(TAG, e.getMessage());
            }
        }

        return tracksDictionary;
    }
}

