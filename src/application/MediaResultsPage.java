package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.core.MovieResultsPage;

public class MediaResultsPage {

	private TvResultsPage tvResults = null;
	private MovieResultsPage movieResults = null;
	private int tempSeason = 0;
	private int tempEpisode = 0;
	
	public MediaResultsPage(TvResultsPage tvResults) {
		this.tvResults = tvResults;	
	}
	
	public MediaResultsPage(MovieResultsPage m) {
		this.movieResults = m;
	}
	
	//don't do lookups until results are requested
	public List<ResultsMediaItem> getResults() {
		List<ResultsMediaItem> results = new ArrayList<>();
		if (tvResults != null) {
			for (int i = 0; i < tvResults.getResults().size(); ++i) {
				results.add(new ResultsMediaItem(Objects.requireNonNull(MediaSearchHandler.getTvInfoById(tvResults.getResults().get(i).getId())).tvShow));
			}
			return results;
		} else if (movieResults != null){
			for (int i = 0; i < movieResults.getResults().size(); ++i) {
				results.add( new ResultsMediaItem(Objects.requireNonNull(MediaSearchHandler.getMovieInfoById(movieResults.getResults().get(i).getId())).cMovie));
			}
		}
		return results;
	}

	//copies results values
	public void setResults(MediaResultsPage mRes) {
		this.movieResults = mRes.movieResults;
		this.tvResults = mRes.tvResults;
		this.tempSeason = mRes.tempSeason;
		this.tempEpisode = mRes.tempEpisode;			
	}
}
