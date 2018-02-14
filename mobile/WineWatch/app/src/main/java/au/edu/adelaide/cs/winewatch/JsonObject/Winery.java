package au.edu.adelaide.cs.winewatch.JsonObject;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Arthur on 25/04/15.
 */
public class Winery {
	private int wid;
	private String name;
	private String description;
	private List<Ferment> ferments;

	public Winery() {
	}

	public Winery(int wid, String name) {
		this.wid = wid;
		this.name = name;
	}

	public Winery(int wid, String name, List<Ferment> ferments) {
		this.wid = wid;
		this.name = name;
		this.ferments = ferments;
	}

	public int getWid() {
		return wid;
	}

	public void setWid(int wid) {
		this.wid = wid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Ferment> getFerments() {
		return ferments;
	}

	public void setFerments(List<Ferment> ferments) {
		this.ferments = ferments;
	}
}
