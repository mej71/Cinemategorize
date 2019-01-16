package application;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import info.movito.themoviedbapi.model.Collection;
import info.movito.themoviedbapi.model.ContentRating;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.ReleaseInfo;
import info.movito.themoviedbapi.model.Video;
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
	private MediaListDisplayType displayType; // can't be mixed
	CustomTvDb tvShow;
	CustomMovieDb cMovie;
	public String fullFilePath;
	public String fileName;
	public String fileFolder;
	public LocalDateTime dateAdded;
	public double rating = -1;
	private List<CustomMovieDb> otherParts = new ArrayList<CustomMovieDb>();
	private List<String> otherPartPaths = new ArrayList<String>();

	//quick check to see if online queries need to be made for selection view
	private boolean hasLoaded = false;
	
	public MediaItem(CustomTvDb tv) {
		this(tv, null, "", "", "");
	}

	public MediaItem(CustomMovieDb m) {
		this(null, m, "", "", "");
	}

	public MediaItem(CustomTvDb tv, CustomMovieDb m, String fPath, String fName, String fFolder) {
		tvShow = tv;
		cMovie = m;
		if (cMovie != null) {
			displayType = MediaListDisplayType.MOVIES;
		} else if (tv != null) {
			displayType = MediaListDisplayType.TVSHOWS;
		}
		fullFilePath = fPath;
		fileName = fName;
		fileFolder = fFolder;
		dateAdded = LocalDateTime.now();
	}
	
	public void setMovie(CustomMovieDb m) {
		cMovie = m;
		tvShow = null;
		displayType = MediaListDisplayType.MOVIES;
	}
	
	public void setTvShow(CustomTvDb tv) {
		cMovie = null;
		tvShow = tv;
		displayType = MediaListDisplayType.TVSHOWS;
	}

	public void setLoaded() {
		hasLoaded = true;
	}
	
	public boolean hasLoaded() {
		return hasLoaded;
	}
	
	public void addPart(CustomMovieDb newPart, String path) {
		otherParts.add(newPart);
		otherPartPaths.add(path);
	}

	public int numParts() {
		return otherParts.size();
	}

	// input the part number, not the index
	// List will start with 2
	public String getPartPath(int i) {
		if (numParts() > i - 2) {
			return otherPartPaths.get(i - 2);
		}
		return null;
	}

	public String getFullFilePath() {
		return fullFilePath;
	}

	public boolean isMovie() {
		return displayType == MediaListDisplayType.MOVIES;
	}

	public boolean isTvShow() {
		return displayType == MediaListDisplayType.TVSHOWS;
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
			if (cMovie != null) {
				return cMovie.getTitle();
			} else {
				return "Unknown Movie";
			}
		} else {
			if (cMovie != null) {
				return tvShow.getName();
			} else {
				return "Unknown TV Show";
			}
		}
	}

	public String getItemDescription() {
		if (isMovie()) {
			if (cMovie != null) {
				return cMovie.getOverview();
			} else {
				return "Unknown Movie";
			}
		} else {
			if (cMovie != null) {
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
		return getCollection() != null;
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
		return getCrew(0, 0);
	}

	public List<PersonCrew> getCrew(int seasonNum, int epNum) {
		if (isMovie()) {
			return cMovie.getCredits().getCrew();
		} else {
			if (seasonNum == 0 || epNum == 0) {
				return tvShow.getCredits().getCrew();
			} else {
				return tvShow.getEpisode(seasonNum, epNum).getCredits().getCrew();
			}
		}
	}

	public List<PersonCast> getCast() {
		return getCast(0, 0);
	}

	public List<PersonCast> getCast(int seasonNum, int epNum) {
		if (isMovie()) {
			return cMovie.getCast();
		} else {
			if (seasonNum == 0 || epNum == 0) {
				return tvShow.getCredits().getCast();
			} else {
				return tvShow.getEpisode(seasonNum, epNum).getCredits().getCast();
			}
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
		return getTitle(0, 0);
	}

	public String getTitle(int seasonNum, int epNum) {
		if (isMovie()) {
			return cMovie.getTitle();
		} else {
			if (seasonNum == 0 || epNum == 0) {
				return tvShow.getName();
			} else {
				return tvShow.getEpisode(seasonNum, epNum).getName();
			}
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
		return getOverview(0, 0);
	}

	public String getOverview(int seasonNum, int epNum) {
		if (isMovie()) {
			return (!cMovie.getOverview().isEmpty()) ? cMovie.getOverview() : "No description available";
		} else {
			if (seasonNum == 0 || epNum == 0) {
				return (!tvShow.getOverview().isEmpty()) ? tvShow.getOverview() : "No description available";
			} else {
				return tvShow.getEpisodeDescription(seasonNum, epNum);
			}
		}
	}

	public int getNumSeasons() {
		if (isMovie()) {
			return 0;
		} else {
			return tvShow.getNumberOfSeasons();
		}
	}

	// First season is 1
	public int getNumEpisodes(int seasonNum) {
		if (isMovie()) {
			return 0;
		} else {
			if (tvShow.getSeasons().get(seasonNum - 1).getEpisodes() == null) {

				// cache seasons, because they are not stored
				List<TvSeason> seasons = new ArrayList<TvSeason>();
				TvSeason tempSeason;
				for (int i = 1; i <= tvShow.getNumberOfSeasons(); ++i) {
					tempSeason = MediaSearchHandler.getSeasonInfo(getId(), i);
					if (tempSeason != null) {
						seasons.add(tempSeason);
					}
				}
				tvShow.setSeasons(seasons);
			}

			return tvShow.getSeasons().get(seasonNum - 1).getEpisodes().size();
		}
	}

	public TvEpisode getEpisode(int seasonNum, int epNum) {
		if (isMovie()) {
			return new TvEpisode();
		} else {
			return tvShow.getEpisode(seasonNum, epNum);
		}
	}

	public List<TvEpisode> getEpisodes() {
		if (isMovie()) {
			return new ArrayList<TvEpisode>();
		} else {
			return tvShow.getEpisodes();
		}
	}

	// used for manual lookup controller so it can still be a MediaItem type
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

	public List<Video> getVideos() {
		if (isMovie()) {
			return cMovie.getVideos();
		} else {
			return tvShow.getVideos();
		}
	}

	public List<ReleaseInfo> getReleases() {
		if (isMovie()) {
			return cMovie.getReleases();
		} else {
			return new ArrayList<ReleaseInfo>();
		}
	}

	public int getRuntime() {
		if (isMovie()) {
			return cMovie.getRuntime();
		} else {
			return tvShow.getEpisodeRuntime();
		}
	}

	public float getVoteAverage() {
		if (isMovie()) {
			return cMovie.getVoteAverage();
		} else {
			return tvShow.getVoteAverage();
		}
	}

	public List<ContentRating> getContentRating() {
		if (isMovie()) {
			return new ArrayList<ContentRating>();
		} else {
			return tvShow.getContentRating();
		}
	}
}