package application;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import info.movito.themoviedbapi.model.ContentRating;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.Video;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;


public class CustomTvDb implements Serializable {
	
	private static final long serialVersionUID = 1L;
	//tv shows use this number to approximate if the credits have been filled out properly, if not they'll use all credits
	private transient final int reasonableNumberOfCredits = 5;
	//season, episode num episode.  TreeMap to sort by keys
	private LinkedHashMap<Integer, LinkedHashMap<Integer, TvEpisode>> episodeList = new LinkedHashMap<Integer, LinkedHashMap<Integer, TvEpisode>>();
	private List<PersonCast> allCast = new ArrayList<PersonCast>();
	private List<PersonCrew> allCrew = new ArrayList<PersonCrew>();
	private boolean hasBeenSorted = false;
	public LocalDateTime dateAdded;
	public TvSeries series;
	public double rating = -1;	
	
	
	public CustomTvDb(TvSeries tvs) {
		series = tvs;
		dateAdded = LocalDateTime.now();
	}
	
	public List<Integer> getSeasonNumbers() {
		List<Integer> seasons = new ArrayList<Integer>();
		for (Integer k : episodeList.keySet()) {
			seasons.add(k);
		}
		Collections.sort(seasons);
		return seasons;
	}
	
	public List<Integer> getEpisodes(int season) {
		List<Integer> episodes = new ArrayList<Integer>();
		for (Integer k : episodeList.get(season).keySet()) {
			episodes.add(k);
		}
		Collections.sort(episodes);
		return episodes;
	}
	
	public List<TvEpisode> getEpisodes() {
		List<TvEpisode> episodes = new ArrayList<TvEpisode>();
		for (Integer k : episodeList.keySet()) {
			for (Integer n : episodeList.get(k).keySet()) {
				episodes.add(episodeList.get(k).get(n));
			}
		}
		return episodes;
	}
	
	//cache seasons, because they are not stored
	//also load credit info at the same time
	public void loadSeasonsInfo() {
		List<TvSeason> seasons = new ArrayList<TvSeason>();
		TvSeason season;
		List<TvEpisode> episodes;
		for (int i = 1; i <= series.getNumberOfSeasons(); ++i) {
			season = MediaSearchHandler.getSeasonInfo(getId(), i);
			episodes = new ArrayList<TvEpisode>();
			for (int j = 1; j <= season.getEpisodes().size(); ++j) {
				episodes.add(MediaSearchHandler.getEpisodeInfo(getId(), season.getSeasonNumber(), j));
			}
			season.setEpisodes(episodes);
			seasons.add(season);
		}
		series.setSeasons(seasons);
		loadCreditInfo();
	}
	
	public TvEpisode getEpisode(int seasonNum, int epNum) {
		if (series.getSeasons().get(seasonNum-1).getEpisodes() == null) {
			loadSeasonsInfo();			
		}
		if ( seasonNum > series.getNumberOfSeasons()) {
			return null;
		} else if (epNum > series.getSeasons().get(seasonNum-1).getEpisodes().size()) {
			return null;
		}
		return series.getSeasons().get(seasonNum-1).getEpisodes().get(epNum-1);
	}
	
	public void addEpisode(TvEpisode episode) {
		if (episodeList.containsKey(episode.getSeasonNumber())) {
			episodeList.get(episode.getSeasonNumber()).put(episode.getEpisodeNumber(), episode);
		} else {
			episodeList.put(episode.getSeasonNumber(), new LinkedHashMap<Integer, TvEpisode>());
			episodeList.get(episode.getSeasonNumber()).put(episode.getEpisodeNumber(), episode);
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
	
	public String getEpisodeDescription(int season, int ep) {
		return episodeList.get(season).get(ep).getOverview();
	}
	
	public String getPosterPath() {
		return series.getPosterPath();
	}
	
	//compile all credit info, then sort by appearances
	public void loadCreditInfo() {
		if (hasBeenSorted) {
			return;
		}
		Credits credits;
		int numSeasons = series.getNumberOfSeasons();
		int numEpisodes;
		TvSeason tempSeason;
		for (int i = 0; i < numSeasons; ++i) {
			numEpisodes = series.getSeasons().get(i).getEpisodes().size();
			tempSeason = series.getSeasons().get(i);
			for (int j = 0; j < numEpisodes; ++j) {
				credits = tempSeason.getEpisodes().get(j).getCredits();
				allCast.addAll(credits.getCast());
				allCrew.addAll(credits.getCrew());
			}
		}
		final Map<PersonCast, Integer> castCounter = new HashMap<PersonCast, Integer>();
		final Map<PersonCrew, Integer> crewCounter = new HashMap<PersonCrew, Integer>();
		for (PersonCast per : allCast)
		    castCounter.put(per, 1 + (castCounter.containsKey(per) ? castCounter.get(per) : 0));
		for (PersonCrew per : allCrew)
			crewCounter.put(per, 1 + (crewCounter.containsKey(per) ? crewCounter.get(per) : 0));
		allCast = new ArrayList<PersonCast>(castCounter.keySet());
		allCrew = new ArrayList<PersonCrew>(crewCounter.keySet());
		Collections.sort(allCast, new Comparator<PersonCast>() {
		    @Override
		    public int compare(PersonCast x, PersonCast y) {
		        return castCounter.get(y) - castCounter.get(x);
		    }
		});
		Collections.sort(allCrew, new Comparator<PersonCrew>() {
		    @Override
		    public int compare(PersonCrew x, PersonCrew y) {
		        return crewCounter.get(y) - crewCounter.get(x);
		    }
		});
		hasBeenSorted = true;
	}

	//if credits are empty, info in the database may not be up to date for later seasons
	//use first season if empty.  if season credits are lacking, use allcrew/allcast
	public Credits getCredits() {
		Credits credits = series.getCredits();
		
		if (credits.getCast() == null || credits.getCrew() == null || credits.getCast().isEmpty() || credits.getCrew().isEmpty()) {
			if (series.getSeasons().get(0).getEpisodes() == null) {
				loadSeasonsInfo();	
			}
			credits = series.getSeasons().get(0).getCredits();
			if (credits.getCast().size() < reasonableNumberOfCredits) {
				credits.setCast(allCast);
			}
			if (credits.getCrew().size() < reasonableNumberOfCredits) {
				credits.setCrew(allCrew);
			}
		}
		return credits;
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
	
	public List<TvSeason> getSeasons() {
		return series.getSeasons();
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

}
