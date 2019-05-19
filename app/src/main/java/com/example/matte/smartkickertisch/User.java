package com.example.matte.smartkickertisch;

import android.net.Uri;

public class User {
    private String uid;
    private String nickname;
    private int position;
    private String wins;
    private String games;
    private Uri profilePicture;

    User(String uid, String nickname, int position, String wins, String games) {
        this.uid = uid;
        this.nickname = nickname;
        this.position = position;
        this.wins = wins;
        this.games = games;
        this.profilePicture = null;
    }

    public String getUid() {
        return uid;
    }

    public String getNickname() {
        return nickname;
    }

    public int getPosition() {
        return position;
    }

    public String getWins() {
        return wins;
    }

    public String getGames() {
        return games;
    }

    public Uri getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Uri profilePicture) {
        this.profilePicture = profilePicture;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", nickname='" + nickname + '\'' +
                ", position=" + position +
                ", wins='" + wins + '\'' +
                ", games='" + games + '\'' +
                ", profilePicture=" + profilePicture +
                '}';
    }
}
