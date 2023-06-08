package com.example.covid_sym_monitor;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "fsdfsd" ;

    public DatabaseHandler(@Nullable Context context) {
        super(context, "covid.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists symptoms ("
                + " ID integer PRIMARY KEY autoincrement, "
                + " Nausea float default 0.0,"
                + " Headache float default 0.0, "
                + " Diarhhea float default 0.0, "
                + " SoreThroat float default 0.0, "
                + " Fever float default 0.0, "
                + " MuscleAche float default 0.0, "
                + " ShortnessOfBreath float default 0.0, "
                + " SmellTasteLoss float default 0.0, "
                + " Cough float default 0.0, "
                + " FeelingTired float default 0.0, "
                + " HeartRate float default 0.0,"
                + " RespiratoryRate float default 0.0);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db , int i, int i1) {

    }

    public boolean addOne( HashMap<String, Float> symptom)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("Nausea",symptom.get("Nausea"));
        cv.put("Headache", symptom.get("Headache"));
        cv.put("Diarhhea",symptom.get("Diarrhea"));
        cv.put("SoreThroat", symptom.get("Sore Throat"));
        cv.put("Fever", symptom.get("Fever"));
        cv.put("MuscleAche", symptom.get("Muscle Ache"));
        cv.put("ShortnessOfBreath", symptom.get("Shortness of breath"));
        cv.put("SmellTasteLoss", symptom.get("Smell Taste Loss"));
        cv.put("Cough", symptom.get("Cough"));
        cv.put("FeelingTired", symptom.get("Feeling tired"));
        cv.put("HeartRate", symptom.get("Heart Rate"));
        cv.put("RespiratoryRate",symptom.get("Respiratory Rate"));

        long insert = db.insert("symptoms", null, cv);
        if(insert== -1) {
            Log.d(TAG,"Record inserted in db: =" + false);
            return false;
        }
        else {
            Log.d(TAG,"Record inserted in db: =" + true);
            return true;
        }
    }

}