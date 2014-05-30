package jp.co.spookies.android.rssreader.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class EntryDao {
	static final String TABLE_NAME = "entries";
	static final String VIEW_NAME = "entries_view";
	static final String COLUMN_LINK = "link";
	static final String COLUMN_TITLE = "title";
	static final String COLUMN_FEED_LINK = "feed_link";
	static final String COLUMN_FEED_TITLE = "feed_title";
	static final String COLUMN_NEW = "is_new";
	static final String COLUMN_PUBLISHED_DATE = "published_date";
	static final String[] VIEW_COLUMNS = { COLUMN_LINK, COLUMN_TITLE,
			COLUMN_FEED_LINK, COLUMN_FEED_TITLE, COLUMN_NEW,
			COLUMN_PUBLISHED_DATE };

	SQLiteDatabase db;

	public EntryDao(SQLiteDatabase db) {
		this.db = db;
	}

	public List<Entry> findAll() {
		List<Entry> list = new ArrayList<Entry>();
		Cursor c = db.query(VIEW_NAME, VIEW_COLUMNS, null, null, null, null,
				COLUMN_PUBLISHED_DATE + " DESC");
		while (c.moveToNext()) {
			Entry entry = new Entry();
			entry.setLink(c.getString(c.getColumnIndex(COLUMN_LINK)));
			entry.setTitle(c.getString(c.getColumnIndex(COLUMN_TITLE)));
			entry.setFeedLink(c.getString(c.getColumnIndex(COLUMN_FEED_LINK)));
			entry.setFeedTitle(c.getString(c.getColumnIndex(COLUMN_FEED_TITLE)));
			entry.setNew(c.getInt(c.getColumnIndex(COLUMN_NEW)) != 0);
			entry.setPublishedDate(new Date(c.getLong(c
					.getColumnIndex(COLUMN_PUBLISHED_DATE))));
			list.add(entry);
		}
		return list;
	}

	public long save(Entry entry) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_TITLE, entry.getTitle());
		values.put(COLUMN_FEED_LINK, entry.getFeedLink());
		values.put(COLUMN_PUBLISHED_DATE, entry.getPublishedDate().getTime());
		values.put(COLUMN_NEW, entry.getNew());
		if (exists(entry.getLink())) {
			String where = COLUMN_LINK + " = ?";
			String[] arg = { entry.getLink() };
			return db.update(TABLE_NAME, values, where, arg);
		} else {
			values.put(COLUMN_NEW, 1);
			values.put(COLUMN_LINK, entry.getLink());
			return db.insert(TABLE_NAME, null, values);
		}
	}

	public long deleteAll(String feedLink) {
		String where = COLUMN_FEED_LINK + " = ?";
		String[] arg = { feedLink };
		return db.delete(TABLE_NAME, where, arg);
	}

	public boolean exists(String link) {
		String selection = COLUMN_LINK + " = ?";
		String[] arg = { link };
		Cursor c = db.query(VIEW_NAME, VIEW_COLUMNS, selection, arg, null,
				null, null);
		return c.moveToFirst();
	}
}
