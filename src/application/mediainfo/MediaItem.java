package application.mediainfo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import application.MediaListDisplayType;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import info.movito.themoviedbapi.model.*;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.people.Person;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.tv.Network;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;

public class MediaItem extends RecursiveTreeObject<MediaItem> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MediaListDisplayType displayType; // can't be mixed
	public CustomTvDb tvShow;
	public CustomMovieDb cMovie;
	private FilePathInfo filePathInfo;
	public LocalDateTime dateAdded;
	public double rating = -1;
	private List<CustomMovieDb> otherParts = new ArrayList<>();
	private List<String> otherPartPaths = new ArrayList<>();

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
		filePathInfo = new FilePathInfo(fPath, fName, fFolder);
		dateAdded = LocalDateTime.now();
	}

	@Override
	public boolean equals(Object mi) {
		if (!(mi instanceof MediaItem)) {
			return false;
		}
		return equalsMediaItem((MediaItem)mi);
	}

	private boolean equalsMediaItem(MediaItem mi) {
		if (this.isMovie() && mi.isMovie() && this.getId() == mi.getId()) {
			return true;
		}
		return this.isTvShow() && mi.isTvShow() && this.getId() == mi.getId();
	}
	
	public void setMovie(CustomMovieDb m) {
		cMovie = m;
		tvShow = null;
		displayType = MediaListDisplayType.MOVIES;
	}
	
	public void setTvShow(CustomTvDb tv) {
		this.cMovie = null;
		this.tvShow = tv;
		tvShow.tempFilePathInfo = filePathInfo;
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

	private int numParts() {
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

	public String getFullFilePath(int seasonNum, int epNum) {
		if (tvShow != null) {
			return tvShow.getFilePath(seasonNum, epNum);
		}
		return "";
	}

	public String getFullFilePath() {
		return (isMovie() || tvShow == null)? filePathInfo.fPath : tvShow.getFilePath();
	}

	public void setFilePathInfo(FilePathInfo fpi) {
		this.filePathInfo = fpi;
	}

	public String getTempFilePath() {
		return filePathInfo.fPath;
	}

	public String getFileName(int seasonNum, int epNum) {
		if (tvShow != null) {
			return tvShow.getFileName(seasonNum, epNum);
		}
		return "";
	}

	public String getFileName() {
		return (isMovie() || tvShow == null)? filePathInfo.fName : tvShow.getFileName();
	}

	public String getFolder(int seasonNum, int epNum) {
		if (tvShow != null) {
			return tvShow.getFileFolder(seasonNum, epNum);
		}
		return "";
	}

	public String getFolder() {
		return (isMovie() || tvShow == null)? filePathInfo.fFolder : tvShow.getFileFolder();
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

	private int getCreditPosition(int personId, boolean useEpisode) {
		List<PersonCrew> crew;
		List<PersonCast> cast;
		if (isMovie()) {
			if (cMovie != null && cMovie.movie != null) {
				cast = cMovie.getCast();
				for (int i = 0; i < cast.size(); ++i) {
					if (cast.get(i).getId() == personId) {
						return i;
					}
				}
				crew = cMovie.getCrew();
				for (int i = 0; i < crew.size(); ++i) {
					if (crew.get(i).getId() == personId) {
						return i;
					}
				}
			}
		} else {
			Credits credits = (useEpisode)? tvShow.getEpisode(tvShow.lastViewedSeason, tvShow.lastViewedEpisode).getCredits() : tvShow.getCredits();
			cast = credits.getCast();
			for (int i = 0; i < cast.size(); ++i) {
				if (cast.get(i).getId() == personId) {
					return i;
				}
			}
			crew = credits.getCrew();
			for (int i = 0; i < crew.size(); ++i) {
				if (crew.get(i).getId() == personId) {
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
	
	private List<Person> getCredits(boolean useEpisode) {
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
			return new ArrayList<>();
		} else {
			return tvShow.getAllEpisodes();
		}
	}

	public List<TvEpisode> getEpisodes(int seasonNum) {
		if (isMovie()) {
			return new ArrayList<>();
		} else {
			return tvShow.getEpisodes(seasonNum);
		}
	}

	// used for manual lookup controller so it can still be a MediaItem type
	private TvEpisode getFirstAvailableEpisode() {
		if (isMovie()) {
			return null;
		} else {
			return tvShow.getFirstAvailableEpisode(tvShow.getFirstAvailableSeason().getSeasonNumber());
		}
	}
	
	private TvSeason getFirstAvailableSeason() {
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
			return new ArrayList<>();
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
		return getVoteAverage(false);
	}
	
	private float getVoteAverage(boolean useEpisode) {
		if (isMovie()) {
			return cMovie.getVoteAverage();
		} else {
			return (useEpisode)? tvShow.getEpisode(tvShow.lastViewedSeason, tvShow.lastViewedEpisode).getVoteAverage() : tvShow.getVoteAverage();
		}
	}

	public List<ContentRating> getContentRating() {
		if (isMovie()) {
			return new ArrayList<>();
		} else {
			return tvShow.getContentRating();
		}
	}

	public List<ProductionCompany> getProductionCompanies() {
		if (isMovie()) {
			return cMovie.getProductionCompanies();
		} else {
			return new ArrayList<>();
		}
	}

	public List<Network> getNetworks() {
		if (isMovie()) {
			return null;
		} else {
			return tvShow.getNetworks();
		}
	}
}