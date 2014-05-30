package jp.co.spookies.android.rssreader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jp.co.spookies.android.rssreader.models.Entry;
import jp.co.spookies.android.rssreader.models.EntryDao;
import jp.co.spookies.android.rssreader.models.Feed;
import jp.co.spookies.android.rssreader.models.FeedDao;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;

public class RSSReaderActivity extends ListActivity {
	private final int MENU_RELOAD = 1;
	private final int MENU_ADD = 2;
	private final int MENU_FEEDS = 3;
	private ArrayAdapter<Entry> adapter = null;
	private Handler handler = new Handler();
	private FeedDao feeds = null;
	private EntryDao entries = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);
		adapter = new EntryAdapter(this, R.layout.list_row);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				Entry entry = adapter.getItem(position);
				entry.setNew(false);
				entries.save(entry);
				intent.setData(Uri.parse(entry.getLink()));
				startActivity(intent);
			}
		});
		DatabaseHelper dbHelper = new DatabaseHelper(this);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		entries = new EntryDao(db);
		feeds = new FeedDao(db);
	}

	@Override
	public void onResume() {
		super.onResume();
		reload();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_RELOAD, Menu.NONE, R.string.menu_reload)
				.setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(Menu.NONE, MENU_ADD, Menu.NONE, R.string.menu_add).setIcon(
				android.R.drawable.ic_menu_add);
		menu.add(Menu.NONE, MENU_FEEDS, Menu.NONE, R.string.menu_feeds)
				.setIcon(android.R.drawable.ic_menu_delete);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_RELOAD:
			try {
				updateFeeds();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		case MENU_ADD:
			add();
			return true;
		case MENU_FEEDS:
			showFeedList();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void reload() {
		adapter.clear();
		for (Entry entry : entries.findAll()) {
			adapter.add(entry);
		}
	}

	private void add() {
		final EditText editText = new EditText(this);
		editText.setText(getString(R.string.default_url));
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setView(editText);
		dialog.setPositiveButton(getString(R.string.ok),
				new Dialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final ProgressDialog progressDialog = new ProgressDialog(
								RSSReaderActivity.this);
						progressDialog
								.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						progressDialog.setMessage(getString(R.string.add_feed));
						progressDialog.show();
						Thread thread = new Thread() {
							@Override
							public void run() {
								try {
									URL url = new URL(editText.getText()
											.toString());
									parse(url);
									handler.post(new Runnable() {
										@Override
										public void run() {
											reload();
										}
									});
									progressDialog.dismiss();
								} catch (Exception e) {
									e.printStackTrace();
									progressDialog.cancel();
								}
							}
						};
						thread.start();
					}
				});
		dialog.setNegativeButton(getString(R.string.cancel), null);
		dialog.show();
	}

	private void showFeedList() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		final List<CharSequence> titles = new ArrayList<CharSequence>();
		final List<String> links = new ArrayList<String>();
		List<Feed> feeds = this.feeds.findAll();
		for (Feed feed : feeds) {
			titles.add(feed.getTitle());
			links.add(feed.getLink());
		}
		dialog.setTitle(R.string.feeds_title);
		dialog.setItems(titles.toArray(new CharSequence[0]),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final String link = links.get(which);
						String title = titles.get(which).toString();
						AlertDialog.Builder confirmDialog = new AlertDialog.Builder(
								RSSReaderActivity.this);
						confirmDialog.setTitle(R.string.confirm_title);
						confirmDialog.setMessage(String.format(
								getString(R.string.confirm_message), title));
						confirmDialog.setPositiveButton(R.string.ok,
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										RSSReaderActivity.this.feeds
												.delete(link);
										entries.deleteAll(link);
										reload();
									}
								});
						confirmDialog.show();
					}
				});
		dialog.show();
	}

	private void updateFeeds() throws Exception {
		setProgressBarIndeterminateVisibility(true);
		Thread thread = new Thread() {
			@Override
			public void run() {
				for (Feed feed : feeds.findAll()) {
					try {
						parse(new URL(feed.getLink()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				handler.post(new Runnable() {
					@Override
					public void run() {
						reload();
						setProgressBarIndeterminateVisibility(false);
					}
				});
			}
		};
		thread.start();
	}

	@SuppressWarnings("unchecked")
	private void parse(URL url) throws Exception {
		SyndFeed syndFeed = new HttpURLFeedFetcher().retrieveFeed(url);
		Feed feed = new Feed();
		feed.setLink(syndFeed.getLink());
		feed.setTitle(syndFeed.getTitle());
		for (SyndEntry syndEntry : (List<SyndEntry>) syndFeed.getEntries()) {
			if (entries.exists(syndEntry.getLink())) {
				continue;
			}
			Entry entry = new Entry();
			entry.setLink(syndEntry.getLink());
			entry.setTitle(syndEntry.getTitle());
			entry.setFeedLink(feed.getLink());
			entry.setPublishedDate(syndEntry.getPublishedDate());
			entry.setNew(true);
			entries.save(entry);
		}
		feeds.save(feed);
	}
}

class EntryAdapter extends ArrayAdapter<Entry> {
	private LayoutInflater inflater;

	public EntryAdapter(Context context, int resource) {
		super(context, resource);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.list_row, null);
		}
		Entry entry = getItem(position);
		TextView entryTitle = (TextView) view.findViewById(R.id.entry_title);
		entryTitle.setText(entry.getTitle());
		TextView feedTitle = (TextView) view.findViewById(R.id.feed_title);
		feedTitle.setText(entry.getFeedTitle());
		ImageView isNew = (ImageView) view.findViewById(R.id.is_new);
		if (entry.isNew()) {
			isNew.setImageResource(R.drawable.new_entry);
		} else {
			isNew.setImageResource(R.drawable.read_entry);
		}
		return view;
	}
}