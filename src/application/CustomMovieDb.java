package application;

import java.io.Serializable;

import info.movito.themoviedbapi.model.MovieDb;

public class CustomMovieDb implements Serializable {

	private static final long serialVersionUID = 1L;	
	public MovieDb movie;
	
	public CustomMovieDb(MovieDb m) {
		movie = m;
	}
	
}
