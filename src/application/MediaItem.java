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
				return cMovie.movie.getTitle();
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
				return cMovie.movie.getOverview();
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
				return cMovie.movie.getId();
			}
		} else {
			return tvShow.series.getId();
		}
		return -1;
	}
	
	public String getReleaseDate() {
		if (isMovie()) {
			if (cMovie != null && cMovie.movie != null) {
				return cMovie.movie.getReleaseDate();
			} 
		} else {
			return tvShow.getFirstAirDate();
		}
		return "";
	}
	
	public int getCreditPosition(int personId) {
		if (isMovie()) {
			if (cMovie != null && cMovie.movie != null) {
				for (int i = 0; i < cMovie.movie.getCast().size(); ++i) {
					if (cMovie.movie.getCast().get(i).getId() == personId) {
						return i;
					}
				}
				for (int i = 0; i < cMovie.movie.getCrew().size(); ++i) {
					if (cMovie.movie.getCrew().get(i).getId() == personId) {
						return i;
					}
				}
			} 
		} else {
			for (int i = 0; i < tvShow.series.getCredits().getCast().size(); ++i) {
				if (tvShow.series.getCredits().getCast().get(i).getId() == personId) {
					return i;
				}
			}
			for (int i = 0; i < tvShow.series.getCredits().getCrew().size(); ++i) {
				if (tvShow.series.getCredits().getCrew().get(i).getId() == personId) {
					return i;
				}
			}
		}
		return 100;
	}
	
	public boolean belongsToCollection() {
		if (isMovie()) {
			return cMovie.movie.getBelongsToCollection() != null;
		} else {
			return false;
		}
	}
	
	public Collection getCollection() {
		if (isMovie()) {
			return cMovie.movie.getBelongsToCollection();
		} else {
			return null;
		}
	}
	
	public boolean isFullLength() {
		if (isMovie()) {
			return cMovie.movie.getRuntime() >= 50;
		} else {
			return true;
		}
	}
	
	public List<Person> getCredits() {
		if (isMovie()) {
			return cMovie.movie.getCredits().getAll();
		} else {
			return tvShow.series.getCredits().getAll();
		}
	}
	
	public List<Keyword> getKeywords() {
		if (isMovie()) {
			return cMovie.movie.getKeywords();
		} else {
			return tvShow.series.getKeywords();
		}
	}

	public List<PersonCrew> getCrew() {
		if (isMovie()) {
			return cMovie.movie.getCrew();
		} else {
			return tvShow.series.getCredits().getCrew();
		}
	}
	
	public List<PersonCast> getCast() {
		if (isMovie()) {
			return cMovie.movie.getCast();
		} else {
			return tvShow.series.getCredits().getCast();
		}
	}
	
	public List<Genre> getGenres() {
		if (isMovie()) {
			return cMovie.movie.getGenres();
		} else {
			return tvShow.series.getGenres();
		}
	}

	public String getTitle() {
		if (isMovie()) {
			return cMovie.movie.getTitle();
		} else {
			return tvShow.series.getName();
		}
	}
	
	public String getLastAirDate() {
		if (isMovie()) {
			return "";
		} else {
			return tvShow.series.getLastAirDate();
		}
	}

	public String getOverview() {
		if (isMovie()) {
			return cMovie.movie.getOverview();
		} else {
			return tvShow.series.getOverview();
		}
	}
	
	public int getNumSeasons() {
		if (isMovie()) {
			return 0;
		} else {
			return tvShow.series.getNumberOfSeasons();
		}
	}
	
	//First season is 1
	public int getNumEpisodes(int seasonNum) {
		if (isMovie()) {
			return 0;
		} else {
			if (tvShow.series.getSeasons().get(seasonNum-1).getEpisodes() == null) {
				
				//cache seasons, because they are not stored
				List<TvSeason> seasons = new ArrayList<TvSeason>();
				for (int i = 1; i <= tvShow.series.getNumberOfSeasons(); ++i) {
					seasons.add(MediaSearchHandler.getSeasonInfo(getId(), i));
				}
				tvShow.series.setSeasons(seasons);
			}
			
			return tvShow.series.getSeasons().get(seasonNum-1).getEpisodes().size();
		}
	}
	
	public TvEpisode getEpisode(int seasonNum, int epNum) {
		if (isMovie()) {
			return null;
		} else {
			if (tvShow.series.getSeasons().get(seasonNum-1).getEpisodes() == null) {
				
				//cache seasons, because they are not stored
				List<TvSeason> seasons = new ArrayList<TvSeason>();
				for (int i = 1; i <= tvShow.series.getNumberOfSeasons(); ++i) {
					seasons.add(MediaSearchHandler.getSeasonInfo(getId(), i));
				}
				tvShow.series.setSeasons(seasons);
			}
			
			return tvShow.series.getSeasons().get(seasonNum-1).getEpisodes().get(epNum-1);
		}
	}
	
	//used for manual lookup controller so it can still be a MediaItem type
	public TvEpisode getFirstAvailableEpisode() {
		if (isMovie()) {
			return null;
		} else {
			for (int i : tvShow.getSeasons()) {
				for (int j : tvShow.getEpisodes(i)) {
					return tvShow.getEpisode(i, j);
				}
			}
			return null;
		}
	}
}