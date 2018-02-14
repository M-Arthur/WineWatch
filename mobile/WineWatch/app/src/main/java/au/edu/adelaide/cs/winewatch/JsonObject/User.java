package au.edu.adelaide.cs.winewatch.JsonObject;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arthur on 25/04/15.
 */
public class User {

	private int uid;
	private String token;
	private String username;
	private String password;
	private String type;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// TODO: add more attributes: name, description, portrait and so on.


	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public User() {
	}

	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public User(int uid, String username, String password, String type) {
		this.uid = uid;
		this.username = username;
		this.password = password;
		this.type = type;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
