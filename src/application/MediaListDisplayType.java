package application;

import java.io.Serializable;

public enum MediaListDisplayType implements Serializable {
	MOVIES("Movies"),
	TVSHOWS("Tv Shows"),
	ALL("All");
	
	private final String toString;
	
	MediaListDisplayType(String toString) {
		this.toString = toString;
	}
	
	@Override
	public String toString() {
		return this.toString;
	}
}
