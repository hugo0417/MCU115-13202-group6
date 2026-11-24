package com.example.usedcardealer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CarDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "used_cars.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE = "cars";
    public static final String BRAND = "brand";
    public static final String MODEL = "model";
    public static final String YEAR = "year";
    public static final String PRICE = "price";

    public CarDatabaseHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                BRAND + " TEXT PRIMARY KEY NOT NULL, " +
                MODEL + " TEXT NOT NULL, " +
                YEAR + " INTEGER NOT NULL, " +
                PRICE + " REAL NOT NULL)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public boolean insertCar(String brand, String model, int year, double price) {
        ContentValues v = values(model, year, price);
        v.put(BRAND, brand);
        return getWritableDatabase().insert(TABLE, null, v) != -1;
    }
    public boolean updateCar(String brand, String model, int year, double price) {
        return getWritableDatabase().update(TABLE, values(model, year, price), BRAND + "=?", new String[]{brand}) > 0;
    }
    public boolean deleteCar(String brand) {
        return getWritableDatabase().delete(TABLE, BRAND + "=?", new String[]{brand}) > 0;
    }
    public Cursor getAllCars() {
        return getReadableDatabase().query(TABLE, null, null, null, null, null, BRAND + " COLLATE NOCASE ASC");
    }
    public Cursor searchCar(String keyword) {
        return getReadableDatabase().query(TABLE, null, BRAND + " LIKE ? OR " + MODEL + " LIKE ?",
                new String[]{"%" + keyword + "%", "%" + keyword + "%"}, null, null, BRAND + " ASC");
    }
    private ContentValues values(String model, int year, double price) {
        ContentValues v = new ContentValues();
        v.put(MODEL, model); v.put(YEAR, year); v.put(PRICE, price);
        return v;
    }
}
