package jp.co.spookies.android.rssreader.models;

import java.util.Date;

public class Entry {
	private String link;
	private String title;
	private String feedLink;
	private String feedTitle;
	private boolean isNew;
	private Date publishedDate;

	public void setLink(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setFeedTitle(String feedTitle) {
		this.feedTitle = feedTitle;
	}

	public String getFeedTitle() {
		return feedTitle;
	}

	public void setFeedLink(String feedLink) {
		this.feedLink = feedLink;
	}

	public String getFeedLink() {
		return feedLink;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public boolean isNew() {
		return isNew;
	}

	public int getNew() {
		if (isNew) {
			return 1;
		}
		return 0;
	}

	public void setPublishedDate(Date PublishdDate) {
		this.publishedDate = PublishdDate;
	}

	public Date getPublishedDate() {
		return publishedDate;
	}
}
