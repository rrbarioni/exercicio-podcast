package br.ufpe.cin.if710.podcast.domain;

import java.io.Serializable;

public class ItemFeed implements Serializable {
    private final String title;
    private final String link;
    private final String pubDate;
    private final String description;
    private final String downloadLink;
    private String uri;
    private int podcastCurrentTime;

    public ItemFeed(String title, String link, String pubDate, String description, String downloadLink) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.downloadLink = downloadLink;
        this.uri = "NONE";
        this.podcastCurrentTime = 0;
    }

    public ItemFeed(String title, String link, String pubDate, String description, String downloadLink, String uri) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.downloadLink = downloadLink;
        this.uri = uri;
        this.podcastCurrentTime = 0;
    }

    public ItemFeed(String title, String link, String pubDate, String description, String downloadLink, String uri, int podcastCurrentTime) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.downloadLink = downloadLink;
        this.uri = uri;
        this.podcastCurrentTime = podcastCurrentTime;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getDescription() {
        return description;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) { this.uri = uri; }

    public int getPodcastCurrentTime() { return podcastCurrentTime; }

    public void setPodcastCurrentTime(int podcastCurrentTime) { this.podcastCurrentTime = podcastCurrentTime; }

    @Override
    public String toString() {
        return title;
    }
}