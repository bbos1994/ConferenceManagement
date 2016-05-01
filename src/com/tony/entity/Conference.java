package com.tony.entity;

import java.util.List;

/**
 * Created by ton on 16-4-23.
 * 活动
 */
public class Conference{
    private List<Track> trackList;

    public List<Track> getTrackList() {
        return trackList;
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
    }

    /*********Constructor***********/
    public Conference() {
    }

    public Conference(List<Track> trackList) {
        this.trackList = trackList;
    }

}
