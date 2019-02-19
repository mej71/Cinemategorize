package application;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.tv.TvEpisode;

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
			for (MediaItem m: ControllerMaster.userData.tempManualItems.keySet()) {
				if (m.getFullFilePath().equals(file.getPath())) {
					return false;
				}
			}
			//prefer tv in bad search results
			if (tvParsedInfo[0] != null) {
				tRes = MediaSearchHandler.getTvResults(tvParsedInfo[0]);
				ControllerMaster.userData.tempManualItems.put(new MediaItem(null, null, file.getPath(), file.getName(), file.getParentFile().getName()), new MediaResultsPage(tRes));
				return false;
			} else if (movieParsedInfo[0] != null){
				mRes = MediaSearchHandler.getMovieResults(movieParsedInfo[0],
						Integer.parseInt(movieParsedInfo[1]));
			} else {
				mRes = MediaSearchHandler.getMovieResults(file.getName(), 0);
			}
			ControllerMaster.userData.tempManualItems.put(new MediaItem(null, null, file.getPath(), file.getName(), file.getParentFile().getName()), new MediaResultsPage(mRes));
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
			for (MediaItem m: ControllerMaster.userData.tempManualItems.keySet()) {
				if (m.getFullFilePath().equals(file.getPath())) {
					return false;
				}
			}
			tRes = MediaSearchHandler.getTvResults(tvParsedInfo[0]);
			ControllerMaster.userData.tempManualItems.put(new MediaItem(series, null, file.getPath(), file.getName(), file.getParentFile().getName()), new MediaResultsPage(tRes));
			return false;
			
		} else {
			if (movieDistance < 3) {
				addMovie(cm, file);
				return true;
			} else {
				for (MediaItem m: ControllerMaster.userData.tempManualItems.keySet()) {
					if (m.getFullFilePath().equals(file.getPath())) {
						return false;
					}
				}
				mRes = MediaSearchHandler.getMovieResults(movieParsedInfo[0],
						Integer.parseInt(movieParsedInfo[1]));
				ControllerMaster.userData.tempManualItems.put(new MediaItem(null, cm, file.getPath(), file.getName(), file.getParentFile().getName()), new MediaResultsPage(mRes));
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

	public static void addMedia(CustomTvDb t, int seasonNum, int epNum, CustomMovieDb m, File file) {
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
			ControllerMaster.userData.getAllMedia().add(mi);
			ControllerMaster.mainController.allTiles.add(ControllerMaster.mainController.addMediaTile(mi));
			//add movie to known collections if there is one
			if (isMovie && mi.belongsToCollection()) {
				if (!ControllerMaster.userData.ownedCollections.containsKey(mi.getCollection())) {
					ControllerMaster.userData.ownedCollections.put(mi.getCollection(), new ArrayList<>());
				}
				ControllerMaster.userData.ownedCollections.get(mi.getCollection()).add(mi);
				ControllerMaster.mainController.updateCollectionCombo();
			}
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
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
		try {
			Date date = formatter.parse(mi.getReleaseDate(false));
			int year = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(date)).getYear();
			if (ControllerMaster.userData.minYear == 0 || ControllerMaster.userData.minYear > year) {
				ControllerMaster.userData.minYear = year;
				ControllerMaster.mainController.fillYearCombos(ControllerMaster.userData.minYear, ControllerMaster.userData.maxYear);
			} 
			if (ControllerMaster.userData.maxYear == 0 || ControllerMaster.userData.maxYear < year) {
				ControllerMaster.userData.maxYear = year;
				ControllerMaster.mainController.fillYearCombos(ControllerMaster.userData.minYear, ControllerMaster.userData.maxYear);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
