package application;

import java.io.Serializable;
import java.util.List;

import info.movito.themoviedbapi.model.Collection;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.ReleaseInfo;
import info.movito.themoviedbapi.model.Video;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;

public class CustomMovieDb implements Serializable {

	private static final long serialVersionUID = 1L;	
	public MovieDb movie;
	
	public CustomMovieDb(MovieDb m) {
		movie = m;
	}
	
	public int getId() {
		return movie.getId();
	}

	public String getTitle() {
		return movie.getTitle();
	}

	public String getOverview() {
		return movie.getOverview();
	}
	
	public List<Genre> getGenres() {
		return movie.getGenres();
	}

	public List<PersonCast> getCast() {
		return movie.getCast();
	}

	public List<PersonCrew> getCrew() {
		return movie.getCrew();
	}
	
	public Credits getCredits() {
		return movie.getCredits();
	}

	public List<Keyword> getKeywords() {
		return movie.getKeywords();
	}
	

	public int getRuntime() {
		return movie.getRuntime();
	}

	public Collection getBelongsToCollection() {
		return movie.getBelongsToCollection();
	}

	public String getReleaseDate() {
		return movie.getReleaseDate();
	}

	public List<Video> getVideos() {
		return movie.getVideos();
	}

	public List<ReleaseInfo> getReleases() {
		return movie.getReleases();
	}

	public float getVoteAverage() {
		return movie.getVoteAverage();
	}

	

	
	
}
