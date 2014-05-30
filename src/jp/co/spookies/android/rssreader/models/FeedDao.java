package jp.co.spookies.android.rssreader.models;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FeedDao {
	static final String TABLE_NAME = "feeds";
	static final String COLUMN_LINK = "link";
	static final String COLUMN_TITLE = "title";
	static final String[] COLUMNS = { COLUMN_LINK, COLUMN_TITLE };

	SQLiteDatabase db;

	public FeedDao(SQLiteDatabase db) {
		this.db = db;
	}

	public List<Feed> findAll() {
		List<Feed> list = new ArrayList<Feed>();
		Cursor c = db.query(TABLE_NAME, COLUMNS, null, null, null, null, null);
		while (c.moveToNext()) {
			Feed feed = new Feed();
			feed.setLink(c.getString(c.getColumnIndex(COLUMN_LINK)));
			feed.setTitle(c.getString(c.getColumnIndex(COLUMN_TITLE)));
			list.add(feed);
		}
		return list;
	}

	public long save(Feed feed) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_TITLE, feed.getTitle());
		if (exists(feed.getLink())) {
			String where = COLUMN_LINK + " = ?";
			String[] arg = { feed.getLink() };
			return db.update(TABLE_NAME, values, where, arg);
		} else {
			values.put(COLUMN_LINK, feed.getLink());
			return db.insert(TABLE_NAME, null, values);
		}
	}

	public long delete(String link) {
		String where = COLUMN_LINK + " = ?";
		String[] arg = { link };
		return db.delete(TABLE_NAME, where, arg);
	}

	public boolean exists(String link) {
		String selection = COLUMN_LINK + " = ?";
		String[] arg = { link };
		Cursor c = db.query(TABLE_NAME, COLUMNS, selection, arg, null, null,
				null);
		return c.moveToFirst();
	}
}
