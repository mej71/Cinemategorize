package application;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

public class CustomTvDb implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public LocalDateTime dateAdded;
	public TvSeries series;
	public double rating = -1;
	//season, episode num episode.  TreeMap to sort by keys
	private LinkedHashMap<Integer, LinkedHashMap<Integer, TvEpisode>> episodeList = new LinkedHashMap<Integer, LinkedHashMap<Integer, TvEpisode>>();
	
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
	
	public TvEpisode getEpisode(int seasonNum, int epNum) {
		if (series.getSeasons().get(seasonNum-1).getEpisodes() == null) {
			
			//cache seasons, because they are not stored
			List<TvSeason> seasons = new ArrayList<TvSeason>();
			for (int i = 1; i <= series.getNumberOfSeasons(); ++i) {
				seasons.add(MediaSearchHandler.getSeasonInfo(getId(), i));
			}
			series.setSeasons(seasons);
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

	public Credits getCredits() {
		return series.getCredits();
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

}
