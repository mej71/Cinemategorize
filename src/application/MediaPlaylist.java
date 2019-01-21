package application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MediaPlaylist implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private LinkedHashMap<String, List<MediaItem>> playlists;
	
	public MediaPlaylist() {
		playlists = new LinkedHashMap<String, List<MediaItem>>();
	}
	
	public boolean nameAlreadyUsed(String name) {
		return playlists.containsKey(name);
	}
	
	public void addPlaylist(String name, List<MediaItem> items) {
		if (items == null || items.isEmpty()) { //no empty playlists
			return;
		}
		playlists.put(name, items);
	}
	
	public void addItemToPlaylist(String name, MediaItem item) {
		if (!playlists.containsKey(name)) {
			playlists.put(name, new ArrayList<MediaItem>());
		}
		playlists.get(name).add(item);
	}
	
	public LinkedHashMap<String, List<MediaItem>> getPlaylists() {
		return playlists;
	}
	
	public List<MediaItem> getPlaylist(String name) {
		return playlists.get(name);
	}
	
	public List<String> getPlaylistNames() {
		return new ArrayList<String>(playlists.keySet());
	}
	
	public void clear() {
		playlists.clear();
	}

}
