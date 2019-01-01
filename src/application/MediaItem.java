package application;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import info.movito.themoviedbapi.model.Collection;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.people.Person;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;

public class MediaItem extends RecursiveTreeObject<MediaItem> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MediaListDisplayType displayType; //can't be mixed
	CustomTvDb tvShow;
	CustomMovieDb cMovie;
	public String fullFilePath;
	public String fileName;
	public String fileFolder;
	public LocalDateTime dateAdded;
	public double rating = -1;
	private List<CustomMovieDb> otherParts = new ArrayList<CustomMovieDb>();
	private List<String> otherPartPaths = new ArrayList<String>();

	private boolean isUnknown = false;
	
	public MediaItem(CustomTvDb tv) {
		this(tv, null, "", "", "");
	}
	
	public MediaItem(CustomMovieDb m) {
		this(null, m, "", "" ,"");
	}
	
	public MediaItem(CustomTvDb tv, CustomMovieDb m, String fPath, String fName, String fFolder) {
		tvShow = tv;
		cMovie = m;
		if (cMovie != null) {
			displayType = MediaListDisplayType.MOVIES;
		} else if (tv != null){
			displayType = MediaListDisplayType.TVSHOWS;
		} else {
			isUnknown = true;
		}
		fullFilePath = fPath;
		fileName = fName;
		fileFolder = fFolder;
		dateAdded = LocalDateTime.now();
	}
	
	public void addPart(CustomMovieDb newPart, String path) {
		otherParts.add(newPart);
		otherPartPaths.add(path);
	}
	
	public int numParts() {
		return otherParts.size();
	}
	
	//input the part number, not the index
	//List will start with 2
	public String getPartPath(int i) {
		if (numParts()>i-2) {
			return otherPartPaths.get(i-2);
		}
		return null;
	}
	
	public String getFullFilePath() {
		return fullFilePath;
	}
	
	public boolean isMovie() {
		if (isUnknown) {
			return false;
		}
		return displayType == MediaListDisplayType.MOVIES;
	}
	
	public boolean isKnown() {
		return !isUnknown;
	}
	
	public Object getItem() {
		if (isMovie()) {
			return cMovie;
		} else {
			return tvShow;
		}
	}
	
	public String getItemName() {
		if (isMovie()) {
			if (cMovie!=null) {
				return cMovie.getTitle();
			} else {
				return "Unknown Movie";
			}
		} else {
			if (cMovie!=null) {
				return tvShow.getName();
			} else {
				return "Unknown TV Show";
			}			
		}
	}
	
	public String getItemDescription() {
		if (isMovie()) {
			if (cMovie!=null) {
				return cMovie.getOverview();
			} else {
				return "Unknown Movie";
			}
		} else {
			if (cMovie!=null) {
				return tvShow.getOverview();
			} else {
				return "Unknown TV Show";
			}
			
		}
	}
	
	public int getId() {
		if (isMovie()) {
			if (cMovie != null) {
				return cMovie.getId();
			}
		} else {
			return tvShow.getId();
		}
		return -1;
	}
	
	public String getReleaseDate() {
		if (isMovie()) {
			if (cMovie != null && cMovie.movie != null) {
				return cMovie.getReleaseDate();
			} 
		} else {
			return tvShow.getFirstAirDate();
		}
		return "";
	}
	
	public int getCreditPosition(int personId) {
		if (isMovie()) {
			if (cMovie != null && cMovie.movie != null) {
				for (int i = 0; i < cMovie.getCast().size(); ++i) {
					if (cMovie.getCast().get(i).getId() == personId) {
						return i;
					}
				}
				for (int i = 0; i < cMovie.getCrew().size(); ++i) {
					if (cMovie.getCrew().get(i).getId() == personId) {
						return i;
					}
				}
			} 
		} else {
			for (int i = 0; i < tvShow.getCredits().getCast().size(); ++i) {
				if (tvShow.getCredits().getCast().get(i).getId() == personId) {
					return i;
				}
			}
			for (int i = 0; i < tvShow.getCredits().getCrew().size(); ++i) {
				if (tvShow.getCredits().getCrew().get(i).getId() == personId) {
					return i;
				}
			}
		}
		return 100;
	}
	
	public boolean belongsToCollection() {
		if (isMovie()) {
			return cMovie.getBelongsToCollection() != null;
		} else {
			return false;
		}
	}
	
	public Collection getCollection() {
		if (isMovie()) {
			return cMovie.getBelongsToCollection();
		} else {
			return null;
		}
	}
	
	public boolean isFullLength() {
		if (isMovie()) {
			return cMovie.getRuntime() >= 50;
		} else {
			return true;
		}
	}
	
	public List<Person> getCredits() {
		if (isMovie()) {
			return cMovie.getCredits().getAll();
		} else {
			return tvShow.getCredits().getAll();
		}
	}
	
	public List<Keyword> getKeywords() {
		if (isMovie()) {
			return cMovie.getKeywords();
		} else {
			return tvShow.getKeywords();
		}
	}

	public List<PersonCrew> getCrew() {
		if (isMovie()) {
			return cMovie.getCrew();
		} else {
			return tvShow.getCredits().getCrew();
		}
	}
	
	public List<PersonCast> getCast() {
		if (isMovie()) {
			return cMovie.getCast();
		} else {
			return tvShow.getCredits().getCast();
		}
	}
	
	public List<Genre> getGenres() {
		if (isMovie()) {
			return cMovie.getGenres();
		} else {
			return tvShow.getGenres();
		}
	}

	public String getTitle() {
		if (isMovie()) {
			return cMovie.getTitle();
		} else {
			return tvShow.getName();
		}
	}
	
	public String getLastAirDate() {
		if (isMovie()) {
			return "";
		} else {
			return tvShow.getLastAirDate();
		}
	}

	public String getOverview() {
		if (isMovie()) {
			return (!cMovie.getOverview().isEmpty())? cMovie.getOverview() : "No description available";
		} else {
			return (!tvShow.getOverview().isEmpty())? tvShow.getOverview() : "No description available";
		}
	}
	
	public int getNumSeasons() {
		if (isMovie()) {
			return 0;
		} else {
			return tvShow.getNumberOfSeasons();
		}
	}
	
	//First season is 1
	public int getNumEpisodes(int seasonNum) {
		if (isMovie()) {
			return 0;
		} else {
			if (tvShow.getSeasons().get(seasonNum-1).getEpisodes() == null) {
				
				//cache seasons, because they are not stored
				List<TvSeason> seasons = new ArrayList<TvSeason>();
				for (int i = 1; i <= tvShow.getNumberOfSeasons(); ++i) {
					seasons.add(MediaSearchHandler.getSeasonInfo(getId(), i));
				}
				tvShow.setSeasons(seasons);
			}
			
			return tvShow.getSeasons().get(seasonNum-1).getEpisodes().size();
		}
	}
	
	public TvEpisode getEpisode(int seasonNum, int epNum) {
		if (isMovie()) {
			return null;
		} else {
			return tvShow.getEpisode(seasonNum, epNum);
		}
	}
	
	//used for manual lookup controller so it can still be a MediaItem type
	public TvEpisode getFirstAvailableEpisode() {
		if (isMovie()) {
			return null;
		} else {
			for (int i : tvShow.getSeasonNumbers()) {
				for (int j : tvShow.getEpisodes(i)) {
					return tvShow.getEpisode(i, j);
				}
			}
			return null;
		}
	}
}