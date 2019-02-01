package application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import info.movito.themoviedbapi.model.ContentRating;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.Video;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;


public class CustomTvDb implements Serializable {
	
	private static final long serialVersionUID = 1L;
	//season, episode num episode.  TreeMap to sort by keys
	private LinkedHashMap<Integer, LinkedHashMap<Integer, FilePathInfo>> episodePaths= new LinkedHashMap<>();
	public TvSeries series;
	public double rating = -1;	
	public int lastViewedSeason = 0;
	public int lastViewedEpisode = 0;
	public transient FilePathInfo tempFilePathInfo;
	
	
	public CustomTvDb(TvSeries tvs) {
		series = tvs;
		//init path list (FilePathInfo members are null until specific episode is loaded)
		for (int i = 0; i < series.getNumberOfSeasons(); ++i) {
			if (series.getSeasons().get(i) != null) {
				loadSeason(i+1);
			}
		}
		for (int i = 0; i < series.getNumberOfSeasons(); ++i) {
			if (series.getSeasons().get(i).getEpisodes() != null) {
				for (int j = 0; j < series.getSeasons().get(i).getEpisodes().size(); ++j) {
					episodePaths.get(i+1).put(j+1, null);
				}
			}
		}
	}
	
	public void setTempFileInfo(String fPath, String fName, String fFolder) {
		tempFilePathInfo = new FilePathInfo(fPath, fName, fFolder);
	}
	
	//get sorted array of seasons that starts with 1
	public List<Integer> getSeasonNumbers() {
		return new ArrayList<>(episodePaths.keySet());
	}
	
	public List<Integer> getOwnedSeasonNumbers() {
		List<Integer> seasons = new ArrayList<>();
		for (Integer i : episodePaths.keySet()) {
			for (Integer k : getEpisodeNumbers(i)) {
				if (episodePaths.get(i).get(k) != null) {
					seasons.add(i);
					break;
				}
			}
		}
		return seasons;
	}
	
	public List<Integer> getEpisodeNumbers(int seasonNum) {
		return new ArrayList<>(episodePaths.get(seasonNum).keySet());
	}
	
	public List<Integer> getOwnedEpisodeNumbers(int seasonNum) {
		List<Integer> episodes = new ArrayList<>();
		for (Integer i : getEpisodeNumbers(seasonNum)) {
			if (episodePaths.get(seasonNum).get(i) != null) {
				episodes.add(i);
			}
		}
		return episodes;
	}
	
	public TvEpisode getFirstAvailableEpisode(int seasonNum) {
		for (Integer i : getEpisodeNumbers(seasonNum)) {
			if (episodePaths.get(seasonNum).get(i) != null) {
				return getEpisode(seasonNum, i);
			}
		}
		return null; //should never return null unless all episodes have been manually removed
	}
	
	//returns first available season
	public TvSeason getFirstAvailableSeason() {
		for (Integer i : getSeasonNumbers()) {
			for (Integer k : getEpisodeNumbers(i)) {
				if (episodePaths.get(i).get(k) != null) {
					return series.getSeasons().get(i-1);
				}
			}
		}
		//should never return null
		return null;
	}
	
	public void loadSeason(int seasonNum) {
		TvSeason season = MediaSearchHandler.getSeasonInfo(series.getId(), seasonNum);
		if (season != null) {
			getSeasons().set(seasonNum-1, season);
		}
		episodePaths.put(seasonNum, new LinkedHashMap<>());
	}
	
	//loads episode info for single episode
	//args should be the aired number, not index (start with 1, no 0s)
	//Cache both season and episode credits
	public void loadEpisode(int seasonNum, int epNum) {
		TvSeason season = getSeason(seasonNum);
		if (season.getEpisodes() == null || season.getEpisodes().isEmpty()) {
			 loadSeason(seasonNum);
		}
		if (season.getCredits() != null) {
			if (season.getCredits().getCrew() != null) {
				for (int i = 0; i < season.getCredits().getCrew().size(); ++i) {
					ControllerMaster.userData.addPerson(season.getCredits().getCrew().get(i), series.getId(), false);
				}
			}
			if (season.getCredits().getCast() != null) {
				for (int i = 0; i < season.getCredits().getCast().size(); ++i) {
					ControllerMaster.userData.addPerson(season.getCredits().getCast().get(i), series.getId(), false);
				}
			}
		}
		TvEpisode episode = MediaSearchHandler.getEpisodeInfo(getId(), seasonNum, epNum);
		getEpisodes(seasonNum).set(epNum-1, episode);
		if (episode.getCredits() != null) {
			if (episode.getCredits().getCrew() != null) {
				for (int i = 0; i < episode.getCredits().getCrew().size(); ++i) {
					ControllerMaster.userData.addPerson(episode.getCredits().getCrew().get(i), series.getId(), false);
				}
			}
			if (episode.getCredits().getCast() != null) {
				for (int i = 0; i < episode.getCredits().getCast().size(); ++i) {
					ControllerMaster.userData.addPerson(episode.getCredits().getCast().get(i), series.getId(), false);
				}
			}
		}
	}
	
	public List<TvEpisode> getAllEpisodes() {
		List<TvEpisode> episodes = new ArrayList<>();
		for (int i : getSeasonNumbers()) {
			episodes.addAll(getEpisodes(i));
		}
		return episodes;
	}
	
	public List<TvEpisode> getEpisodes(int seasonNum) {
		return getSeason(seasonNum).getEpisodes();
	}
	
	public TvEpisode getEpisode(int seasonNum, int epNum) {
		return series.getSeasons().get(seasonNum-1).getEpisodes().get(epNum-1);
	}
	
	public List<TvSeason> getSeasons() {
		return series.getSeasons();
	}
	
	public TvSeason getSeason(int seasonNum) {
		return series.getSeasons().get(seasonNum-1);
	}
	
	//add file location info and force credit lookup
	//episode should always be added before calling any episode specific lookups
	public void addEpisode(int seasonNum, int epNum, String filePath, String fileName, String fileFolder) {
		FilePathInfo fInfo = new FilePathInfo(filePath, fileName, fileFolder);
		if (episodePaths.containsKey(seasonNum)) {
			episodePaths.get(seasonNum).put(epNum, fInfo);
		} else {
			episodePaths.put(seasonNum, new LinkedHashMap<>());
			episodePaths.get(seasonNum).put(epNum, fInfo);
		}
		loadEpisode(seasonNum, epNum);
	}

	//nulls fileinfo
	void removeEpisode(int seasonNum, int epNum) {
		if (episodePaths.containsKey(seasonNum)) {
			episodePaths.get(seasonNum).put(epNum, null);
		} else {
			episodePaths.put(seasonNum, new LinkedHashMap<>());
			episodePaths.get(seasonNum).put(epNum, null);
		}
	}
	
	public String getName() {
		return series.getName();
	}
	
	public String getOverview() {
		return series.getOverview();
	}
	
	public int getId() {
		return series.getId();
	}
	
	public String getFirstAirDate() {
		return series.getFirstAirDate();
	}
	
	public String getEpisodeDescription(int seasonNum, int epNum) {
		return getEpisode(seasonNum, epNum).getOverview();
	}
	
	public String getPosterPath() {
		return series.getPosterPath();
	}

	public Credits getCredits() {
		return getCredits(1, 1);
	}
	
	//don't use credits as a whole, only on an episode basis
	public Credits getCredits(int seasonNum, int epNum) {
		return getEpisode(seasonNum, epNum).getCredits();
	}

	public List<Genre> getGenres() {
		return series.getGenres();
	}

	public List<Keyword> getKeywords() {
		return series.getKeywords();
	}

	public String getLastAirDate() {
		return series.getLastAirDate();
	}

	public int getNumberOfSeasons() {
		return series.getNumberOfSeasons();
	}

	public void setSeasons(List<TvSeason> seasons) {
		series.setSeasons(seasons);
	}

	public float getVoteAverage() {
		return series.getVoteAverage();
	}

	public int getEpisodeRuntime() {
		return (int)series.getEpisodeRuntime().stream().mapToDouble(val -> val).average().orElse(0.0);
	}

	public List<ContentRating> getContentRating() {
		return series.getContentRatings();		
	}

	public List<Video> getVideos() {
		return series.getVideos();
	}

	void setFilePathInfo(FilePathInfo fpi, int seasonNum, int epNum) {
		episodePaths.get(seasonNum).put(epNum, fpi);
	}

	List<String> getAllFullPaths() {
		List<String> allPaths = new ArrayList<>();
		List<Integer> seasons = getOwnedSeasonNumbers();
		List<Integer> episodes;
		for (int season : seasons) {
			episodes = getOwnedEpisodeNumbers(season);
			for (int episode : episodes) {
				allPaths.add(getFilePath(season, episode));
			}
		}
		return allPaths;
	}

	public String getFilePath() {
		return getFilePath(lastViewedSeason, lastViewedEpisode);
	}

	public String getFilePath(int seasonNum, int epNum) {
		if (seasonNum == 0 || epNum == 0) {
			return tempFilePathInfo.fPath;
		}
		if (episodePaths.get(seasonNum).get(epNum) != null) {
			return episodePaths.get(seasonNum).get(epNum).fPath;
		} else {
			return "";
		}
	}

	public String getFileName() {
		return getFileName(lastViewedSeason, lastViewedEpisode);
	}

	public String getFileName(int seasonNum, int epNum) {
		if (seasonNum == 0 || epNum == 0) {
			return tempFilePathInfo.fName;
		}
		if (episodePaths.get(seasonNum).get(epNum) != null) {
			return episodePaths.get(seasonNum).get(epNum).fName;
		} else {
			return "";
		}
	}

	public String getFileFolder() {
		return getFileFolder(lastViewedSeason, lastViewedEpisode);
	}

	public String getFileFolder(int seasonNum, int epNum) {
		if (seasonNum == 0 || epNum == 0) {
			return tempFilePathInfo.fFolder;
		}
		if (episodePaths.get(seasonNum).get(epNum) != null) {
			return episodePaths.get(seasonNum).get(epNum).fFolder;
		} else {
			return "";
		}
	}

	
	public int getNumEpisodes() {
		return series.getNumberOfEpisodes();
	}

}
