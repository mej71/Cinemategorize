package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import info.movito.themoviedbapi.TmdbMovies.MovieMethod;
import info.movito.themoviedbapi.TmdbPeople;
import info.movito.themoviedbapi.TmdbTV.TvMethod;
import info.movito.themoviedbapi.TmdbTvEpisodes.EpisodeMethod;
import info.movito.themoviedbapi.TmdbTvSeasons.SeasonMethod;
import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.Artwork;
import info.movito.themoviedbapi.model.Collection;
import info.movito.themoviedbapi.model.CollectionInfo;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.people.Person;
import info.movito.themoviedbapi.model.people.PersonCredits;
import info.movito.themoviedbapi.model.people.PersonPeople;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public  class MediaSearchHandler {
	
	private static String moviePosterDir = "images/movie_posters";
	private static String tvPosterDir = "images/tv_posters";
	private static String personProfileDir = "images/people";
	public static Comparator<Collection> releaseDateComparator = new Comparator<Collection>(){
	     public int compare(Collection o1, Collection o2){ 
	    	 String o1Date = (o1.getReleaseDate() != null)? o1.getReleaseDate() : "";
	    	 String o2Date = (o2.getReleaseDate() != null)? o2.getReleaseDate() : "";
	    	 if (o1Date.isEmpty() && o2Date.isEmpty()) {
	    		 return 0;
	    	 } else if (o1Date.isEmpty() && !o2Date.isEmpty()) {
	    		 return 1;
	    	 } else if (!o1Date.isEmpty() && o2Date.isEmpty()) {
	    		 return -1;
	    	 }
	    	 return o1Date.compareTo(o2Date);
	     }
	};
	
	
	public static PersonCredits getPersonCombinedCredits(int personId) {
		return UserData.apiLinker.getPeople().getCombinedPersonCredits(personId);
	}
	
	public static Artwork getTopPersonImage(int personId) {
		List<Artwork> result = UserData.apiLinker.getPeople().getPersonImages(personId);
		if (result.size() > 0) {
			return result.get(0);
		} else {
			return null;
		}
	}
	
	public static PersonPeople getPersonPeople(int personId) {
		return UserData.apiLinker.getPeople().getPersonInfo(personId, TmdbPeople.TMDB_METHOD_PERSON);
	}
	
	public static CustomMovieDb getMovieInfo(String movieName, int movieYear) {
		MovieResultsPage moviePage = UserData.apiLinker.getSearch().searchMovie(movieName, movieYear, null, true, 1);
		if (moviePage.getResults().size() > 0) {
			MovieDb m = UserData.apiLinker.getMovies().getMovie(moviePage.getResults().get(0).getId(), null, MovieMethod.images, MovieMethod.credits, 
					MovieMethod.keywords, MovieMethod.videos, MovieMethod.release_dates);
			if (m == null) {
				return null;
			}
			ControllerMaster.userData.seenMovies.put(m.getId(), m);
			CustomMovieDb result = new CustomMovieDb(m);
			return result;
		}
		return null;
	}
	
	public static MovieResultsPage getMovieResults(String movieName, int movieYear) {
		return UserData.apiLinker.getSearch().searchMovie(movieName, movieYear, null, true, 1);
	}
	
	public static MediaItem getMovieInfoById(int movieId) {
		if (ControllerMaster.userData.getMovieById(movieId) != null) {
			return ControllerMaster.userData.getMovieById(movieId);
		}
		MovieDb movie = UserData.apiLinker.getMovies().getMovie(movieId, null, MovieMethod.images, MovieMethod.credits, 
				MovieMethod.keywords, MovieMethod.videos, MovieMethod.release_dates);
		if (movie == null) { 
			return null;
		}
		ControllerMaster.userData.seenMovies.put(movie.getId(), movie);
		return new MediaItem(new CustomMovieDb(movie));
	}
	
	public static MediaItem getTvInfoById(int tvId) {
		if (ControllerMaster.userData.getTvById(tvId) != null) {
			return ControllerMaster.userData.getTvById(tvId);
		}
		TvSeries result = UserData.apiLinker.getTvSeries().getSeries(tvId, null, TvMethod.images, TvMethod.credits, TvMethod.videos, 
				TvMethod.keywords, TvMethod.content_ratings);
		if (result == null) { 
			return null;
		}
		ControllerMaster.userData.seenTv.put(result.getId(), result);
		return new MediaItem(new CustomTvDb(result));
	}
	
	public static CustomTvDb getTVInfo(String seriesName) {
		TvResultsPage tvPage = UserData.apiLinker.getSearch().searchTv(seriesName, null, 1);
		if (tvPage.getResults().size() > 0) {
			TvSeries result = UserData.apiLinker.getTvSeries().getSeries(tvPage.getResults().get(0).getId(), null, TvMethod.images, 
					TvMethod.credits, TvMethod.videos, TvMethod.keywords, TvMethod.content_ratings);
			if (result == null) { 
				return null;
			}
			ControllerMaster.userData.seenTv.put(result.getId(), result);
			return new CustomTvDb(result);
		}
		return null;		
	}
	
	public static TvResultsPage getTvResults(String seriesName) {
		return UserData.apiLinker.getSearch().searchTv(seriesName, null, 1);
	}
	
	public static TvSeason getSeasonInfo(int id, int sesId) {
		TvSeason season = UserData.apiLinker.getTvSeasons().getSeason(id, sesId, "en", SeasonMethod.credits);
		return season;
	}
	
	
	public static TvEpisode getEpisodeInfo(int id, int season, int ep) {
		TvEpisode episode = UserData.apiLinker.getTvEpisodes().getEpisode(id, season, ep, "en", EpisodeMethod.credits, EpisodeMethod.images);
		return episode;
	}
	
	public static MediaItem getFirstFromCollectionMatchesPerson(int collectionId, int personId) {
		CollectionInfo ci = UserData.apiLinker.getCollections().getCollectionInfo(collectionId, null);
		Collections.sort(ci.getParts(), releaseDateComparator);
		MediaItem mi;
		for (int i = 0; i < ci.getParts().size(); ++i) {
			mi = MediaSearchHandler.getMovieInfoById(ci.getParts().get(i).getId());
			for (int j = 0; j < mi.getCredits().size(); ++j) {
				if (mi.getCredits().get(j).getId() == personId) {
					return mi;
				}
			}
		}
		return null;
	}
	
	public static String[] parseTVShowName(String filename, String foldername) {
		String tempFileName = filename.replaceAll("\\.(?=.*\\.)", " "); //remove all .'s and replace with spaces
		String tvTitle = null, episodeNum = null, seasonNum = null;
		//Look for TV show first, then movies.  TV show regex is more formulaic, so 
		//First try Title S0XE0X or Title Season 04 Episode 05
		Matcher tvTitleMatcher = Pattern.compile("^.*?(?=(Season|S)((\\s)|(-)|(_))?\\d{1,2}((\\s)|(-)|(_))?(Episode|Ep|E)((\\s)|(-)|(_))?\\d{1,3}+(?!\\d))", Pattern.CASE_INSENSITIVE).matcher(tempFileName);
		Matcher tvTitleMatcher2 = Pattern.compile("^.*?(?=(Episode|E)((\\s)|(-)|(_))?\\d{1,3}+(?!\\d))").matcher(tempFileName); //Season could be ommited if only 1 season
		Matcher tvEpisodeMatcher = Pattern.compile("(?<=(Episode|Ep|E)((\\s)|(-)|(_))?)\\d{1,3}+(?!\\d)", Pattern.CASE_INSENSITIVE).matcher(tempFileName);
		Matcher tvSeasonMatcher = Pattern.compile("(?<=(Season|S)((\\s)|(-)|(_))?)\\d{1,2}", Pattern.CASE_INSENSITIVE).matcher(tempFileName);
		//should find title in some way, and at least the season or episode
		if (tvTitleMatcher.find()) {
			tvTitle = tvTitleMatcher.group(0).trim();
		} else if (tvTitleMatcher2.find()){
			tvTitle = tvTitleMatcher2.group(0).trim();
		}
		if (tvTitle==null || tvTitle.isEmpty()) {//if the filename is something like S02E01 - Episode name, the parent folder is probably the show title
			tvTitle = foldername;
		}
		if (tvSeasonMatcher.find()) { //if the episode was specified, but no the season, probably the first season
			seasonNum = tvSeasonMatcher.group(0).trim();
		} else {
			seasonNum = "1";
		}
		if (tvEpisodeMatcher.find()) { // if the season was specified, but not the episode, probably the first episode
			episodeNum = tvEpisodeMatcher.group(0).trim();
		} else {
			tvEpisodeMatcher = Pattern.compile("(?<!\\d)\\d{1,3}+(?!\\d)").matcher(tempFileName);
			if (tvEpisodeMatcher.find()) {
				episodeNum = tvEpisodeMatcher.group(0).trim();
			} else {
				episodeNum = "1";
			}
		}
		return new String[] {tvTitle.trim(), seasonNum, episodeNum};		
	}
	
	public static String[] parseMovieName(String filename) {
		String tempFileName = filename.replaceAll("\\.(?=.*\\.)", " "); //remove all .'s and replace with spaces
		String movieTitle = null, movieYear = null;
		//try movie title (year) or movie.title.year
		Matcher movieTitleMatcher = Pattern.compile("^(.*?)(?=(\\(|\\.|\\s)\\d{4})").matcher(tempFileName);//find title, include year if the title starts with the year
		Matcher movieYearMatcher;
	    if (movieTitleMatcher.find()) {
	    	movieTitle = movieTitleMatcher.group(0).trim();
	    	String charSeq = StringUtils.replace(tempFileName, movieTitle, "");
	        movieYearMatcher = Pattern.compile("(19\\d{2}|[2-9]\\d{3})").matcher(charSeq);
		    if (movieYearMatcher.find()) {
		    	movieYear = movieYearMatcher.group(0);
		    }
	    } 
	    if (movieTitle == null){//if all else fails, remove any brackets/parenthesis and replaces .'s with spaces
	    	//strip everything in parenthesis or brackets for a less cluttered title
	    	movieTitle = StringUtils.replaceAll(filename, "(\\(.*\\)|\\[.*\\])\\s*", "").replace('.', ' ');//strip everything in parenthesis or brackets for a less cluttered title
		    movieTitle = movieTitle.substring(0, movieTitle.length()-3);
		    movieYearMatcher = Pattern.compile("(19\\d{2}|[2-9]\\d{3})").matcher(tempFileName.replaceAll(filename, ""));
		    if (movieYearMatcher.find()) {
		    	movieYear = movieYearMatcher.group(0);
		    }		
		}	    
	    return new String[] {movieTitle.trim(), movieYear};
	}
	
	public static ImageView getItemPoster(MediaItem mi) {
		if (mi.isMovie()) {
			return getItemPoster(mi.cMovie.movie, null, 500);
		} else {
			return getItemPoster(null, mi.tvShow, 500);
		}
	}
	
	
	public static ImageView getItemPoster(MediaItem mi, int size) {
		if (mi.isMovie()) {
			return getItemPoster(mi.cMovie.movie, null, size);
		} else {
			return getItemPoster(null, mi.tvShow, size);
		}
	}
	
	//dedicated method to get item posters with size variants
	//temp files don't get saved
	public static ImageView getItemPoster(MovieDb m, CustomTvDb tv, int size) {
		ImageView iView = new ImageView();
		if (m != null && m.getPosterPath() != null) {
			getPosterFromFilePath(iView, size, moviePosterDir, String.valueOf(m.getId()));
			if (iView.getImage()==null && m.getPosterPath()!=null && !m.getPosterPath().isEmpty()) {
				getPosterFromURL(iView, size, m.getPosterPath(), moviePosterDir, String.valueOf(m.getId()));
			}
		} else if (tv != null && tv.series != null){
			getPosterFromFilePath(iView, size, tvPosterDir, String.valueOf(tv.getId()));
			if (iView.getImage()==null && tv.getPosterPath() != null) {
				getPosterFromURL(iView, size, tv.getPosterPath(), tvPosterDir, String.valueOf(tv.getId()));
			}
		}	
		//if all failed, use default poster
		if (iView.getImage()==null) {
			URL url = MediaSearchHandler.class.getClassLoader().getResource("unknown_poster.png");
			BufferedImage image;
			try {
				image = ImageIO.read(url);
				iView.setImage(SwingFXUtils.toFXImage(image, null));
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		return iView;
	}
	
	public static void getPosterFromFilePath(ImageView iView, int size, String baseFolder, String id) {
		new File(baseFolder).mkdirs();
		File f = new File(baseFolder+"/"+ id + "_" + size + ".jpg");
		if (f.exists() && !f.isDirectory()) {
			try {
				if (f.toURI().toURL()!=null) {
					BufferedImage b = ImageIO.read(f);
					iView.setImage(SwingFXUtils.toFXImage(b, null));
				} 
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
	}
	
	//save image after retrieval for faster access next time
	public static void getPosterFromURL(ImageView iView, int size, String url, String basePath, String id) {
		try {
			InputStream in;
			if (size>0) {
				in = new URL("http://image.tmdb.org/t/p/w" + size + url).openStream();
			} else {
				in = new URL("http://image.tmdb.org/t/p/original" + url).openStream();
			}
			if (in!=null) {
				iView.setImage(new Image(in));
				new File(basePath).mkdirs();
				File file = new File(basePath + "/" + id + "_" + size + ".jpg");	
				BufferedImage b = SwingFXUtils.fromFXImage(iView.getImage(), null);
				ImageIO.write(b, "jpg", file);
			}
			in.close();
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static <T extends Person> ImageView getProfilePicture(T p) {
		ImageView iView = new ImageView();
		getProfilePictureFromFile(iView, p.getId());
		if (iView.getImage()==null) {
			getProfilePictureFromURL(iView, p.getId());
		}
		if (iView.getImage()==null) {
			URL url = MediaSearchHandler.class.getClassLoader().getResource("unknown_poster.png");
			
			try {
				File file = new File(personProfileDir + "/" + p.getId()+ ".jpg");	
				BufferedImage image = ImageIO.read(url);
				iView.setImage(SwingFXUtils.toFXImage(image, null));
				ImageIO.write(image, "jpg", file);
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		return iView;
	}
	
	public static void getProfilePictureFromFile(ImageView iView, int id) {
		new File(personProfileDir).mkdirs();
		File f = new File(personProfileDir+"/"+ id + ".jpg");
		if (f.exists() && !f.isDirectory()) {
			try {
				if (f.toURI().toURL()!=null) {
					BufferedImage b = ImageIO.read(f);
					iView.setImage(SwingFXUtils.toFXImage(b, null));
				} 
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
	}
	
	public static void getProfilePictureFromURL(ImageView iView, int id) {
		Artwork a = MediaSearchHandler.getTopPersonImage(id);
		if (a==null) {
			return;
		}
		InputStream in;
		try {
			URL url = new URL("https://image.tmdb.org/t/p/h632/" + a.getFilePath());
			if (url == null) {
				url = new URL("https://image.tmdb.org/t/p/original/" + a.getFilePath());
			}
			in = url.openStream();
			if (in!=null) {
				iView.setImage(new Image(in));
				File file = new File(personProfileDir + "/" + id+ ".jpg");	
				BufferedImage b = SwingFXUtils.fromFXImage(iView.getImage(), null);
				ImageIO.write(b, "jpg", file);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
