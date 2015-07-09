package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "NumberplateCodesManager.sqlite";

    private static final String TABLE_NUMBERPLATE_CODES = "numberplate_codes";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CODE = "code";
    private static final String COLUMN_DISTRICT = "district";
    private static final String COLUMN_DISTRICT_CENTER = "district_center";
    private static final String COLUMN_STATE = "state";
    private static final String COLUMN_DISTRICT_WIKIPEDIA_URL = "district_wikipedia_url";
    private static final String COLUMN_JOKES = "jokes";
    public static final String WIKIPEDIA_BASE_URL = "http://de.wikipedia.org/wiki/";
    private static final List<String[]> data = new ArrayList<String[]>();

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/kfzkennzeichen.gisbertamm.de.kfz_kennzeichen/databases/";

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     *
     * @param context
     */
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
        // copy database implicitly when class instance is created for the first time
        createDataBase();
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    public void createDataBase() {

        boolean dbExist = checkDataBase();

        if (dbExist) {
            //do nothing - database already exist
        } else {

            //By calling this method an empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            copyDataBase();

        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {

            //database does't exist yet.

        }

        if (checkDB != null) {

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    private void copyDataBase() {
        try {
            //Open your local db as the input stream
            InputStream myInput = myContext.getAssets().open(DATABASE_NAME);

            // Path to the just created empty db
            String outFileName = DB_PATH + DATABASE_NAME;

            //Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);

            //transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            //Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy database", e);
        }

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DATABASE_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if (myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // do nothing because database is provided from assets
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing because database is provided from assets
    }

    public SavedEntry searchForCode(String code) {
        try {
            this.openDataBase();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        Cursor cursor = myDataBase.query(TABLE_NUMBERPLATE_CODES, new String[]{COLUMN_ID,
                        COLUMN_CODE, COLUMN_DISTRICT, COLUMN_DISTRICT_CENTER, COLUMN_STATE,
                        COLUMN_DISTRICT_WIKIPEDIA_URL, COLUMN_JOKES}, COLUMN_CODE + "=?",
                new String[]{String.valueOf(code)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        SavedEntry savedEntry = null;

        if (cursor.getCount() > 0) {
            savedEntry = new SavedEntry(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), cursor.getString(5), cursor.getString(6));
        }

        return savedEntry;
    }
}
