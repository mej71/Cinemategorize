package application;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import info.movito.themoviedbapi.model.Collection;
import info.movito.themoviedbapi.model.ContentRating;
import info.movito.themoviedbapi.model.Credits;
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
	private FileInfo fileInfo;
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
			tv.setTempFileInfo(fPath, fName, fFolder);
		}
		fileInfo = new FileInfo(fPath, fName, fFolder);
		dateAdded = LocalDateTime.now();
	}
	
	public void setMovie(CustomMovieDb m) {
		cMovie = m;
		tvShow = null;
		displayType = MediaListDisplayType.MOVIES;
	}
	
	public void setTvShow(CustomTvDb tv) {
		this.cMovie = null;
		this.tvShow = tv;
		tvShow.tempFileInfo = fileInfo;
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
		return (isMovie())? fileInfo.fPath : tvShow.getFilePath();
	}
	
	public String getFileName() {
		return (isMovie())? fileInfo.fName : tvShow.getFileName();
	}
	
	public String getFolder() {
		return (isMovie())? fileInfo.fFolder : tvShow.getFileFolder();
	}
	
	//makes sure lastview* fields are set properly
	public void ensureLastViewed() {
		if (isMovie()) {
			return;
		}
		if (tvShow.lastViewedSeason == 0) {
			tvShow.lastViewedSeason = getFirstAvailableSeason().getSeasonNumber();
		}
		if (tvShow.lastViewedEpisode == 0) {
			tvShow.lastViewedEpisode = getFirstAvailableEpisode().getEpisodeNumber();
		}
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
			if (tvShow != null) {
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
			if (tvShow != null) {
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
	
	public int getEpisodeId() {
		if (isMovie()) {
			if (cMovie != null) {
				return cMovie.getId();
			}
		} else {
			return tvShow.getEpisode(tvShow.lastViewedSeason, tvShow.lastViewedEpisode).getId();
		}
		return -1;
	}

	public String getReleaseDate() {
		return getReleaseDate(true);
	}
	
	public String getReleaseDate(boolean useEpisode) {
		if (isMovie()) {
			if (cMovie != null && cMovie.movie != null) {
				return cMovie.getReleaseDate();
			}
		} else {
			if (useEpisode) {
				return tvShow.getEpisode(tvShow.lastViewedSeason, tvShow.lastViewedEpisode).getAirDate();
			} else {
				return tvShow.getFirstAirDate();
			}
		}
		return "";
	}
	
	public int getCreditPosition(int personId) {
		return getCreditPosition(personId, true);
	}

	public int getCreditPosition(int personId, boolean useEpisode) {
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
			Credits credits = (useEpisode)? tvShow.getEpisode(tvShow.lastViewedSeason, tvShow.lastViewedEpisode).getCredits() : tvShow.getCredits(); 
			for (int i = 0; i < credits.getCast().size(); ++i) {
				if (credits.getCast().get(i).getId() == personId) {
					return i;
				}
			}
			for (int i = 0; i < credits.getCrew().size(); ++i) {
				if (credits.getCrew().get(i).getId() == personId) {
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
		return getCredits(true);
	}
	
	public List<Person> getCredits(boolean useEpisode) {
		if (isMovie()) {
			return cMovie.getCredits().getAll();
		} else {
			return (useEpisode)? tvShow.getEpisode(tvShow.lastViewedSeason, tvShow.lastViewedEpisode).getCredits().getAll() : tvShow.getCredits().getAll();
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
		return getCrew(true);
	}

	public List<PersonCrew> getCrew(boolean useEpisode) {
		if (isMovie()) {
			return cMovie.getCredits().getCrew();
		} else {
			if (useEpisode) {
				if (tvShow.getEpisode(tvShow.lastViewedSeason, tvShow.lastViewedEpisode).getCredits() != null) {
					return tvShow.getEpisode(tvShow.lastViewedSeason, tvShow.lastViewedEpisode).getCredits().getCrew();
				}
			}
			return tvShow.getCredits().getCrew();
		}
	}

	public List<PersonCast> getCast() {
		return getCast(true);
	}

	public List<PersonCast> getCast(boolean useEpisode) {
		if (isMovie()) {
			return cMovie.getCast();
		} else {
			return (useEpisode)? tvShow.getEpisode(tvShow.lastViewedSeason, tvShow.lastViewedEpisode).getCredits().getCast() : tvShow.getCredits().getCast();
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
		if (tvShow != null) {
			return getTitle(true, tvShow.lastViewedSeason, tvShow.lastViewedEpisode);
		} else {
			return getTitle(false);
		}
	}

	public String getTitle(boolean useEpisode) {
		return getTitle(false, 0, 0);
	}
	
	public String getTitle(boolean useEpisode, int season, int episode) {
		if (isMovie()) {
			return cMovie.getTitle();
		} else {
			return (useEpisode)? tvShow.getEpisode(season, episode).getName() : tvShow.getName();
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
		return getOverview(true);
	}

	public String getOverview(boolean useEpisode) {
		if (isMovie()) {
			return (!cMovie.getOverview().isEmpty()) ? cMovie.getOverview() : "No description available";
		} else {
			return (useEpisode)? tvShow.getEpisodeDescription(tvShow.lastViewedSeason, tvShow.lastViewedEpisode) : ((!tvShow.getOverview().isEmpty()) ? tvShow.getOverview() : "No description available");
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
			return tvShow.getEpisodeNumbers(seasonNum).size();
		}
	}

	public TvEpisode getEpisode(int seasonNum, int epNum) {
		if (isMovie()) {
			return new TvEpisode();
		} else {
			return tvShow.getEpisode(seasonNum, epNum);
		}
	}
	
	public List<TvEpisode> getAllEpisodes() {
		if (isMovie()) {
			return new ArrayList<TvEpisode>();
		} else {
			return tvShow.getAllEpisodes();
		}
	}

	public List<TvEpisode> getEpisodes(int seasonNum) {
		if (isMovie()) {
			return new ArrayList<TvEpisode>();
		} else {
			return tvShow.getEpisodes(seasonNum);
		}
	}

	// used for manual lookup controller so it can still be a MediaItem type
	public TvEpisode getFirstAvailableEpisode() {
		if (isMovie()) {
			return null;
		} else {
			return tvShow.getFirstAvailableEpisode(tvShow.getFirstAvailableSeason().getSeasonNumber());
		}
	}
	
	public TvSeason getFirstAvailableSeason() {
		if (isMovie()) {
			return null;
		} else {
			return tvShow.getFirstAvailableSeason();
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
		return getVoteAverage(true);
	}
	
	public float getVoteAverage(boolean useEpisode) {
		if (isMovie()) {
			return cMovie.getVoteAverage();
		} else {
			return (useEpisode)? tvShow.getEpisode(tvShow.lastViewedSeason, tvShow.lastViewedEpisode).getVoteAverage() : tvShow.getVoteAverage();
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