package jp.co.spookies.android.rssreader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "data";
	private static final int DATABASE_VERSION = 1;
	private static final String CREATE_FEED_TABLE_SQL = "CREATE TABLE feeds (link TEXT PRIMARY KEY, title TEXT)";
	private static final String CREATE_ENTRY_TABLE_SQL = "CREATE TABLE entries (link TEXT PRIMARY KEY, title TEXT, feed_link TEXT, is_new INTEGER, published_date INTEGER)";
	private static final String CREATE_ENTRY_VIEW_SQL = "CREATE VIEW entries_view AS SELECT entries.*, feeds.title AS feed_title FROM entries JOIN feeds ON entries.feed_link = feeds.link";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_FEED_TABLE_SQL);
		db.execSQL(CREATE_ENTRY_TABLE_SQL);
		db.execSQL(CREATE_ENTRY_VIEW_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
