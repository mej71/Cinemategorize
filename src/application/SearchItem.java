package application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.movito.themoviedbapi.TmdbCompany;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.ProductionCompany;
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
	private ProductionCompany company;
	
	public SearchItem(SearchTypes type, Object obj) {
		searchType = type;
		switch (type) {
		case TITLE:
			media = (MediaItem) obj;
			break;
		case TAG:
			tag = (String) obj;
			break;
		case DIRECTOR:
			director = (PersonCrew) obj;
			break;
		case WRITER:
			writer = (PersonCrew) obj;
			break;
		case ACTOR:
			actor = (PersonCast) obj;
			break;
		case GENRE:
			genre = (Genre) obj;
			break;
		case COMPANY:
			company = (ProductionCompany) obj;
			break;
		default:
			tag = obj.toString();
			break;
		}
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
		case COMPANY:
			return company;
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
		case COMPANY:
			return company.getName();
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
		case COMPANY:

			break;
		default:
			break;
		}
		movieList.removeAll(Collections.singleton(null)); //remove all null values so we don't get an empty list
		tvList.removeAll(Collections.singleton(null)); 
		Map<String,List<Integer>> map = new HashMap<>();
		if (!movieList.isEmpty()) {
			map.put(UserData.movieIdentifier, movieList);
		}
		if (!tvList.isEmpty()) {
			map.put(UserData.tvIdentifier, tvList);
		}
		return map;		
	}
	
	public enum SearchTypes {
		TITLE,
		GENRE,
		TAG,
		DIRECTOR,
		ACTOR, 
		WRITER,
		COMPANY
	}
}
