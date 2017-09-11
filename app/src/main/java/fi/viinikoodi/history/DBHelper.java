/*
 * Copyright (C) 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fi.viinikoodi.history;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;

/**
 * @author Sean Owen
 */
public final class DBHelper extends SQLiteOpenHelper {

  private static final int DB_VERSION = 3;
  private static final String DB_NAME = "barcode_scanner_history.db";
  public static final String TABLE_NAME = "history";
  public static final String ID_COL = "id";
  static final String ALKOID_COL = "alko_id";
  static final String EAN_COL = "ean";
  static final String NAME_COL = "name";
  static final String PRICE_COL = "price";
  static final String COUNTRY_COL = "country";
  static final String TYPE_COL = "type";
  static final String URL_COL = "url";
  static final String IMAGEURL_COL = "image_url";
  static final String TEXT_COL = "text";
  static final String GRAPES_COL = "grapes";
  static final String REGION_COL = "region";
  public static final String NOTES_COL = "notes";
  public static final String RATING_COL = "rating";
  public static final String TIMESTAMP_COL = "timestamp";

  public DBHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.execSQL(
            "CREATE TABLE " + TABLE_NAME + " (" +
            ID_COL + " INTEGER PRIMARY KEY, " +
            ALKOID_COL + " TEXT, " +
            EAN_COL + " TEXT, " +
            NAME_COL + " TEXT, " +
            PRICE_COL + " TEXT, " +
            COUNTRY_COL + " TEXT, " +
            TYPE_COL + " TEXT, " +
            URL_COL + " TEXT, " +
            IMAGEURL_COL + " TEXT, " +
            TEXT_COL + " TEXT, " +
            GRAPES_COL + " TEXT, " +
            REGION_COL + " TEXT, " +
            NOTES_COL + " TEXT, " +
            RATING_COL + " INTEGER, " +
            TIMESTAMP_COL + " INTEGER" +
            ");");
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(sqLiteDatabase);
  }

}
