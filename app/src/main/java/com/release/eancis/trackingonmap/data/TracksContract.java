package com.release.eancis.trackingonmap.data;

import android.provider.BaseColumns;

/**
 * Created by Enrico Ancis on 29/05/18.
 */

public class TracksContract {

    /**
     * It's the Database contract to define the structure of the Databese and its table
     * (in this case it's needed only one table)
     * */
    public static final class TracksEntry implements BaseColumns {

        public static final String TABLE_NAME = "tracks";
        public static final String COLUMN_START_TIME = "startTime";
        public static final String COLUMN_END_TIME = "endTime";
        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_PATH_COLOR = "pathColor";

    }
}