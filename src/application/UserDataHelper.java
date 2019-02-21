package application;

import application.mediainfo.CustomMovieDb;
import application.mediainfo.CustomTvDb;
import application.mediainfo.MediaItem;
import application.mediainfo.MediaResultsPage;
import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.ProductionCompany;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.tv.Network;
import info.movito.themoviedbapi.model.tv.TvEpisode;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class UserDataHelper {
	
	static public boolean addMovieOrTvShow(File file) {
		String[] tvParsedInfo = MediaSearchHandler.parseTVShowName(file.getName(), file.getParentFile().getName());
		if (tvParsedInfo[1].isEmpty()) {
			tvParsedInfo[1] = "1";
		}
		if (tvParsedInfo[2].isEmpty()) {
			tvParsedInfo[2] = "2";
		}
		CustomTvDb series;
		TvEpisode episode;
		TvResultsPage tRes;
		MovieResultsPage mRes;
		Integer tvDistance = 100;
		series = MediaSearchHandler.getTVInfo(tvParsedInfo[0]);

		if (series != null) {
			tvDistance = StringTools.getLevenshteinDistance(series.getName(), tvParsedInfo[0]);
			if (tvDistance == series.getName().length()) {
				tvDistance = 100;
			}
		}
		String[] movieParsedInfo = MediaSearchHandler.parseMovieName(file.getName());
		if (movieParsedInfo[1] == null || movieParsedInfo[1].isEmpty()) {
			movieParsedInfo[1] = "0";
		}
		Integer movieDistance = 100;
		CustomMovieDb cm = null;
		if (movieParsedInfo[0] != null && !movieParsedInfo[0].isEmpty()) {
			cm = MediaSearchHandler.getMovieInfo(movieParsedInfo[0],
					Integer.parseInt(movieParsedInfo[1]));
			if (cm != null) {
				movieDistance =  StringTools.getLevenshteinDistance(cm.getTitle(), movieParsedInfo[0]);
				if (movieDistance == cm.getTitle().length()) {
					movieDistance = 100;
				}
				
			}
		}
		// if both results are really far off, we probably failed.  Use a temporary movie lookup to avoid errors
		if (series == null &&  cm==null) {
			if (ControllerMaster.userData.tempManualItemMatches(file.getPath())) {
				return false;
			}
			//prefer tv in bad search results
			if (tvParsedInfo[0] != null) {
				tRes = MediaSearchHandler.getTvResults(tvParsedInfo[0]);
				ControllerMaster.userData.addTempManualItem(new MediaItem(null, null, file.getPath(), file.getName(), file.getParentFile().getName()), new MediaResultsPage(tRes));
				return false;
			} else if (movieParsedInfo[0] != null){
				mRes = MediaSearchHandler.getMovieResults(movieParsedInfo[0],
						Integer.parseInt(movieParsedInfo[1]));
			} else {
				mRes = MediaSearchHandler.getMovieResults(file.getName(), 0);
			}
			ControllerMaster.userData.addTempManualItem(new MediaItem(null, null, file.getPath(), file.getName(), file.getParentFile().getName()), new MediaResultsPage(mRes));
			return false;
		} else if (series != null) {
			if (cm != null) {
				if (movieDistance<tvDistance && movieDistance < 3) {
					addMovie(cm, file);
					return true;
				} else if (tvDistance < 3) {
					episode = MediaSearchHandler.getEpisodeInfo(series.getId(), Integer.parseInt(tvParsedInfo[1]), Integer.parseInt(tvParsedInfo[2]));
					addTvShow(series, episode.getSeasonNumber(), episode.getEpisodeNumber(), file);
					return true;
				} 
			} else if (tvDistance < 3) {
				episode = MediaSearchHandler.getEpisodeInfo(series.getId(), Integer.parseInt(tvParsedInfo[1]), Integer.parseInt(tvParsedInfo[2]));
				addTvShow(series, episode.getSeasonNumber(), episode.getEpisodeNumber(), file);
				return true;
			}
			if (ControllerMaster.userData.tempManualItemMatches(file.getPath())) {
				return false;
			}
			tRes = MediaSearchHandler.getTvResults(tvParsedInfo[0]);
			ControllerMaster.userData.addTempManualItem(new MediaItem(series, null, file.getPath(), file.getName(), file.getParentFile().getName()), new MediaResultsPage(tRes));
			return false;
			
		} else {
			if (movieDistance < 3) {
				addMovie(cm, file);
				return true;
			} else {
				if (ControllerMaster.userData.tempManualItemMatches(file.getPath())) {
					return false;
				}
				mRes = MediaSearchHandler.getMovieResults(movieParsedInfo[0],
						Integer.parseInt(movieParsedInfo[1]));
				ControllerMaster.userData.addTempManualItem(new MediaItem(null, cm, file.getPath(), file.getName(), file.getParentFile().getName()), new MediaResultsPage(mRes));
				return false;
			}
		}
	}
	
	public static void addMovie(CustomMovieDb m, File filePath) {
		addMedia(null, 0, 0, m, filePath);
	}
	
	public static void addTvShow(CustomTvDb t, int seasonNum, int epNum, File filePath) {
		addMedia(t, seasonNum, epNum, null, filePath);
	}

	private static void addMedia(CustomTvDb t, int seasonNum, int epNum, CustomMovieDb m, File file) {
		boolean isMovie = (m != null);
		MediaItem mi;
		//ignore duplicates
		if (isMovie && ControllerMaster.userData.ownsMovie(m.getId()) ||
				!isMovie && ControllerMaster.userData.ownsShow(t.getId()) && ControllerMaster.userData.ownsEpisode(t.getId(), seasonNum, epNum)) {
			return;
		}
		ControllerMaster.userData.addPath(file.getPath());
		if (!isMovie && ControllerMaster.userData.ownsShow(t.getId())) {
			mi = ControllerMaster.userData.getTvById(t.getId());
			mi.tvShow.lastViewedSeason = seasonNum;
			mi.tvShow.lastViewedEpisode = epNum;
			
			mi.tvShow.addEpisode(seasonNum, epNum, file.getPath(), file.getName(), file.getParentFile().getName());
		} else {
			mi = new MediaItem(t, m, file.getPath(), file.getName(), file.getParentFile().getName());
			if (!isMovie) {
				mi.tvShow.addEpisode(seasonNum, epNum, file.getPath(), file.getName(), file.getParentFile().getName());
				mi.tvShow.lastViewedSeason = seasonNum;
				mi.tvShow.lastViewedEpisode = epNum;
			}
			ControllerMaster.userData.addMedia(mi);
			ControllerMaster.mainController.allTiles.add(ControllerMaster.mainController.addMediaTile(mi));
			//add movie to known collections if there is one
			ControllerMaster.userData.processCollectionInfo(mi, isMovie);
		}

		List<Keyword> keywords = mi.getKeywords();
		for (int i = 0; i < keywords.size(); ++i) {
			ControllerMaster.userData.addTag(keywords.get(i), mi.getId(), isMovie);
		}

		List<PersonCrew> crew = mi.getCrew();
		if (crew != null) {

			for (int i = 0; i < crew.size(); ++i) {
				ControllerMaster.userData.addPerson(crew.get(i), mi.getId(), isMovie);
			}
		}

		List<PersonCast> cast = mi.getCast();
		if (cast != null) {
			for (int i = 0; i < cast.size() && i < 15; ++i) {
				 ControllerMaster.userData.addPerson(cast.get(i), mi.getId(), isMovie);
			}
		}
		List<Genre> genres = mi.getGenres();
		if (genres != null) {
			for (int i = 0; i < genres.size(); ++i) {
				ControllerMaster.userData.addGenre(genres.get(i), mi.getId(), isMovie);
			}
		}
		List<ProductionCompany> companies = mi.getProductionCompanies();
		if (companies != null) {
			for (int i = 0; i < companies.size(); ++i) {
				ControllerMaster.userData.addProductionCompany(companies.get(i), mi.getId(), isMovie);
			}
		}
		List<Network> networks = mi.getNetworks();
		if (networks != null) {
			for (int i = 0; i < networks.size(); ++i) {
				ControllerMaster.userData.addNetwork(networks.get(i), mi.getId(), isMovie);
			}
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
		try {
			Date date = formatter.parse(mi.getReleaseDate(false));
			int year = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(date)).getYear();
			int minYear = ControllerMaster.userData.getMinYear();
			int maxYear = ControllerMaster.userData.getMaxYear();
			ControllerMaster.userData.setMinYear(year);
			ControllerMaster.userData.setMaxYear(maxYear);
			if (minYear != ControllerMaster.userData.getMinYear()) {
				ControllerMaster.mainController.fillYearCombos(minYear, maxYear);
			}
			if (maxYear != ControllerMaster.userData.getMaxYear()) {
				ControllerMaster.mainController.fillYearCombos(minYear, maxYear);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
