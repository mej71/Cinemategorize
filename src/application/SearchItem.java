package application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;

public class SearchItem {
	public SearchTypes searchType;
	private MediaItem media;
	private String tag;
	private PersonCrew director;
	private PersonCast actor;
	private Genre genre;
	private PersonCrew writer;
	
	public SearchItem(MediaItem mi) {
		media = mi;
		if (mi.isMovie()) {
			searchType = SearchTypes.TITLE;
		} else {
			searchType = SearchTypes.TITLE;
		}
	}
	
	public SearchItem(String t) {
		tag = t;
		searchType = SearchTypes.TAG;
	}
	
	public SearchItem(PersonCrew d, boolean b) {
		if (b) {
			director = d;
			searchType = SearchTypes.DIRECTOR;
		} else {
			writer = d;
			searchType = SearchTypes.WRITER;
		}
	}
	
	public SearchItem(PersonCast c) {
		actor = c;
		searchType = SearchTypes.ACTOR;
	}
	
	public SearchItem(Genre g) {
		genre = g;
		searchType = SearchTypes.GENRE;
	}
	
	
	public Object getItem() {
		switch (searchType) {
		case ACTOR:
			return actor;
		case DIRECTOR:
			return director;
		case TAG:
			return tag;
		case TITLE:
			return media;
		case GENRE:
			return genre;
		case WRITER:
			return writer;
		default:
			break;
		}
		return null;
	}
	
	public String getItemName() {
		switch (searchType) {
		case ACTOR:
			return actor.getName();
		case DIRECTOR:
			return director.getName();
		case TAG:
			return tag;
		case TITLE:
			return media.getItemName();
		case GENRE:
			return genre.getName();
		case WRITER:
			return writer.getName();
		default:
			break;
		}
		return "";
	}
	
	public Map<String, List<Integer>> getTargetIDs() {
		UserData userData = ControllerMaster.userData;
		List<Integer> movieList = new ArrayList<>();
		List<Integer> tvList = new ArrayList<>();
		switch (searchType) {
		case DIRECTOR:
			movieList.addAll(userData.getMoviesWithDirector(director));
			tvList.addAll(userData.getTvWithDirector(director));
			break;
		case TAG:
			movieList.addAll(userData.getMoviesWithTag(tag));
			tvList.addAll(userData.getTvWithTag(tag));
			break;
		case TITLE:
			if (media.isMovie()) {
				movieList.add(media.getId());
			} else {
				tvList.add(media.getId());
			}			
			break;
		case GENRE:
			movieList.addAll(userData.getMoviesWithGenre(genre));
			tvList.addAll(userData.getTvWithGenre(genre));
			break;
		case ACTOR:
			movieList.addAll(userData.getMoviesWithActor(actor));
			tvList.addAll(userData.getTvWithActor(actor));
			break;
		case WRITER:
			movieList.addAll(userData.getMoviesWithWriter(writer));
			tvList.addAll(userData.getTvWithWriter(writer));
			break;
		default:
			break;
		}
		movieList.removeAll(Collections.singleton(null)); //remove all null values so we don't get an empty list
		tvList.removeAll(Collections.singleton(null)); 
		Map<String,List<Integer>> map = new HashMap<String, List<Integer>>();
		map.put("movies", movieList);
		map.put("tv", tvList);
		if (movieList.isEmpty() && tvList.isEmpty()) {
			return null;
		} else {
			return map;
		}
	}
	
	public enum SearchTypes {
		TITLE,
		GENRE,
		TAG,
		DIRECTOR,
		ACTOR, 
		WRITER
	}
}
