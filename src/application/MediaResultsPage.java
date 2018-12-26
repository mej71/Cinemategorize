package application;

import java.util.ArrayList;
import java.util.List;

import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.core.MovieResultsPage;

public class MediaResultsPage {

	private TvResultsPage tvResults = null;
	private MovieResultsPage movieResults = null;
	private int tempSeason = 0;
	private int tempEpisode = 0;
	
	public MediaResultsPage(TvResultsPage tvResults, int tempSeason, int tempEpisode) {
		this.tvResults = tvResults;
		this.tempSeason = tempSeason;
		this.tempEpisode = tempEpisode;		
	}
	
	public MediaResultsPage(MovieResultsPage m) {
		movieResults = m;
	}
	
	//don't do lookups until results are requested
	public List<ResultsMediaItem> getResults() {
		List<ResultsMediaItem> results = new ArrayList<ResultsMediaItem>();
		if (tvResults != null) {
			for (int i = 0; i < tvResults.getResults().size(); ++i) {
				results.add(new ResultsMediaItem(MediaSearchHandler.getTvInfoById(tvResults.getResults().get(i).getId()).tvShow, tempSeason, tempEpisode));
			}
			return results;
		} else {
			for (int i = 0; i < movieResults.getResults().size(); ++i) {
				results.add( new ResultsMediaItem(MediaSearchHandler.getMovieInfoById(movieResults.getResults().get(i).getId()).cMovie));
			}
			return results;
		}
	}
}
