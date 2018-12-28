package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCredit;
import info.movito.themoviedbapi.model.people.PersonCredits;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.people.PersonPeople;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeries;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class UserData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<MediaItem> allMedia = new ArrayList<>();
	private TreeMap<String, List<Integer>> movieTags = new TreeMap<String, List<Integer>>(String.CASE_INSENSITIVE_ORDER); // cache list of tags for search updating
	private TreeMap<String, List<Integer>> tvTags = new TreeMap<String, List<Integer>>(String.CASE_INSENSITIVE_ORDER); 
	private LinkedHashMap<PersonCrew, List<Integer>> directorsMovieList = new LinkedHashMap<PersonCrew, List<Integer>>();
	private LinkedHashMap<PersonCrew, List<Integer>> writersMovieList = new LinkedHashMap<PersonCrew, List<Integer>>();
	private LinkedHashMap<PersonCast, List<Integer>> actorsMovieList = new LinkedHashMap<PersonCast, List<Integer>>();
	private LinkedHashMap<Genre, List<Integer>> genreMovieList = new LinkedHashMap<Genre, List<Integer>>();
	private LinkedHashMap<String, List<Integer>> userMovieLists = new LinkedHashMap<String, List<Integer>>();
	private LinkedHashMap<PersonCrew, List<Integer>> directorsTvList = new LinkedHashMap<PersonCrew, List<Integer>>();
	private LinkedHashMap<PersonCrew, List<Integer>> writersTvList = new LinkedHashMap<PersonCrew, List<Integer>>();
	private LinkedHashMap<PersonCast, List<Integer>> actorsTvList = new LinkedHashMap<PersonCast, List<Integer>>();
	private LinkedHashMap<Genre, List<Integer>> genreTvList = new LinkedHashMap<Genre, List<Integer>>();
	private LinkedHashMap<String, List<Integer>> userTvLists = new LinkedHashMap<String, List<Integer>>();
	private LinkedHashMap<Integer, PersonPeople> personList = new LinkedHashMap<Integer, PersonPeople>();
	private LinkedHashMap<Integer, PersonCredits> creditsList = new LinkedHashMap<Integer, PersonCredits>();
	public LinkedHashMap<Integer, MovieDb> seenMovies = new LinkedHashMap<Integer, MovieDb>();
	public LinkedHashMap<Integer, TvSeries> seenTv = new LinkedHashMap<Integer, TvSeries>();
	public LinkedHashMap<Integer, List<PersonCredit>> knownFor = new LinkedHashMap<Integer, List<PersonCredit>>();
	public int minYear = 0;
	public int maxYear = 0;
	public final transient static TmdbApi apiLinker = new TmdbApi(getAPIKey("THE_MOVIE_DB_API_TOKEN"));  //your key here
	private double scaleFactor = 1.0;
	
	public static String getAPIKey(String keyname) {
	
		XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inputSource;
		try {
			URL url = UserData.class.getResource("api_keys.xml");
			inputSource = new InputSource(url.openStream());
			return xpath.evaluate("/resources/" + keyname, inputSource);
		} catch (IOException | XPathExpressionException e) {
			System.out.println("Invalid key in api_keys.xml for " + keyname);
			System.out.println(e);
			return "";
		}
	}
	
	
	public PersonCredits getCredits(int personId) {
		if (!creditsList.containsKey(personId)) {
			PersonCredits credits = MediaSearchHandler.getPersonCombinedCredits(personId);
			creditsList.put(personId, credits);
		}
		return creditsList.get(personId);
	}
	
	public PersonPeople getPerson(int personId) {		
		return personList.get(personId);
	}
	
	public List<PersonCredit> getKnowForCredits(int personId) {
		return knownFor.get(personId);
	}
	
	public double getScaleFactor() {
		return scaleFactor;
	}
	
	public void setScaleFactor(double sf) {
		if (sf<0.25 || sf>2) {
			return;
		}
		scaleFactor = sf;
	}
	
	public transient LinkedHashMap<MediaItem, MediaResultsPage> tempManualItems = new LinkedHashMap<MediaItem, MediaResultsPage>();
	
	public List<Integer> getMoviesWithActor(PersonCast actor) {
		if (actorsMovieList.containsKey(actor)) {
			return actorsMovieList.get(actor);
		} 
		return new ArrayList<Integer>();
	}
	
	public List<Integer> getTvWithActor(PersonCast actor) {
		if (actorsTvList.containsKey(actor)) {
			return actorsTvList.get(actor);
		} 
		return new ArrayList<Integer>();
	}

	public List<Integer> getMoviesWithDirector(PersonCrew director) {
		if (directorsMovieList.containsKey(director)) {
			return directorsMovieList.get(director);
		} 
		return new ArrayList<Integer>();
	}
	
	public List<Integer> getTvWithDirector(PersonCrew director) {
		if (directorsTvList.containsKey(director)) {
			return directorsTvList.get(director);
		} 
		return new ArrayList<Integer>();
	}

	public List<Integer> getMoviesWithTag(String tag) {
		if (movieTags.containsKey(tag)) {
			return movieTags.get(tag);
		}
		return new ArrayList<Integer>();
	}
	
	public List<Integer> getTvWithTag(String tag) {
		if (tvTags.containsKey(tag)) {
			return tvTags.get(tag);
		}
		return new ArrayList<Integer>();
	}
	
	public List<Integer> getMoviesWithWriter(PersonCrew writer) {
		if (writersMovieList.containsKey(writer)) {
			return writersMovieList.get(writer);
		} 
		return new ArrayList<Integer>();
	}
	
	public List<Integer> getTvWithWriter(PersonCrew writer) {
		if (writersTvList.containsKey(writer)) {
			return writersTvList.get(writer);
		} 
		return new ArrayList<Integer>();
	}
	
	public List<Integer> getMoviesWithGenre(Genre g) {
		if (genreMovieList.containsKey(g)) {
			return genreMovieList.get(g);
		} 
		return new ArrayList<Integer>();
	}
	
	public List<Integer> getTvWithGenre(Genre g) {
		if (genreTvList.containsKey(g)) {
			return genreTvList.get(g);
		} 
		return new ArrayList<Integer>();
	}
	
	public boolean ownsMovie(int iD) {
		for (int i = 0; i < allMedia.size(); ++i) {
			if (allMedia.get(i).isMovie() && allMedia.get(i).getId()==iD) {
				return true;
			}
		}
		return false;
	}
	
	public MediaItem getMovieById(int iD) {
		for (int i = 0; i < allMedia.size(); ++i) {
			if (allMedia.get(i).isMovie() && allMedia.get(i).getId()==iD) {
				return allMedia.get(i);
			}
		}
		if (seenMovies.containsKey(iD)) {
			return new MediaItem(new CustomMovieDb(seenMovies.get(iD)));
		}
		return null;
	}
	
	public boolean ownsShow(int iD) {
		for (int i = 0; i < allMedia.size(); ++i) {
			if (!allMedia.get(i).isMovie() && allMedia.get(i).getId()==iD) {
				return true;
			}
		}
		return false;
	}
	
	public MediaItem getTvById(int iD) {
		for (int i = 0; i < allMedia.size(); ++i) {
			if (!allMedia.get(i).isMovie() && allMedia.get(i).getId()==iD) {
				return allMedia.get(i);
			}
		}
		if (seenTv.containsKey(iD)) {
			return new MediaItem(new CustomTvDb(seenTv.get(iD)));
		}
		return null;
	}
	
	public int numMediaItems() {
		return allMedia.size() + allMedia.size();
	}

	public UserData() {
		tryLoadFile();
	}


	public void saveAll() {
		File file = new File("save_data/userdata.dat");

		try {
			FileOutputStream saveFile = new FileOutputStream(file);
			ObjectOutputStream save = new ObjectOutputStream(saveFile);
			save.writeObject(this);
			save.close();
			saveFile.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void tryLoadFile() {
		InputStream inputStream = null;
		ObjectInputStream objectInputStream = null;
		File file = new File("save_data/userdata.dat");
		URL url;
		try {
			url = file.toURI().toURL();
			if (url!=null) {
				inputStream = url.openStream();
				objectInputStream = new ObjectInputStream(inputStream);
				UserData tempDat = (UserData)objectInputStream.readObject();
				objectInputStream.close();
				inputStream.close();
				allMedia = tempDat.allMedia;
				movieTags = tempDat.movieTags;
				directorsMovieList = tempDat.directorsMovieList;
				writersMovieList = tempDat.writersMovieList;
				actorsMovieList = tempDat.actorsMovieList;
				genreMovieList = tempDat.genreMovieList;
				userMovieLists= tempDat.userMovieLists;
				directorsTvList = tempDat.directorsTvList;
				writersTvList = tempDat.writersTvList;
				actorsTvList = tempDat.actorsTvList;
				genreTvList = tempDat.genreTvList;
				userTvLists= tempDat.userTvLists;
				scaleFactor = tempDat.scaleFactor;
				personList = tempDat.personList;
				creditsList = tempDat.creditsList;
				seenMovies = tempDat.seenMovies;
				seenTv = tempDat.seenTv;
				knownFor = tempDat.knownFor;
				minYear = tempDat.minYear;
				maxYear = tempDat.maxYear;
			} 
			return;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}		
	}
	
	final String regExSpecialChars = "<([{\\^-=$!|]})?*+.>";
	final String regExSpecialCharsRE = regExSpecialChars.replaceAll( ".", "\\\\$0");
	final Pattern reCharsREP = Pattern.compile( "[" + regExSpecialCharsRE + "]");

	//escape all special characters in strings for use with regex
	String quoteRegExSpecialChars( String s)
	{
	    Matcher m = reCharsREP.matcher( s);
	    return m.replaceAll( "\\\\$0");
	}

	public ObservableList<SearchItem> getAutoCompleteItems(String userInput) {
		ObservableList<SearchItem> suggestions = FXCollections.observableArrayList();
		boolean detailedLength = userInput.length()>1;
		if (userInput.equals("")) {
			return suggestions;
		}
		userInput = quoteRegExSpecialChars(userInput); // escape special characters so users can search literally
		Pattern pattern = Pattern.compile("(?i)(^|\\s|\\()" + userInput);
		for (int i = 0; i < allMedia.size(); ++i) {
			if (pattern.matcher(allMedia.get(i).getItemName()).find()) {
				suggestions.add(new SearchItem(allMedia.get(i) ));
			}
		}
		for (Genre g : genreMovieList.keySet()) {
			if (pattern.matcher(g.getName()).find()) {
				suggestions.add(new SearchItem(g));
			}
		}
		if (detailedLength || suggestions.size()==0) {
			for (String t : movieTags.keySet()) {
				if (pattern.matcher(t).find()) {
					suggestions.add(new SearchItem(t));
				}
			}
			for (PersonCrew d : directorsMovieList.keySet()) {
				if (pattern.matcher(d.getName()).find()) {
					suggestions.add(new SearchItem(d, true));
				}
			}
			for (PersonCast c : actorsMovieList.keySet()) {
				if (pattern.matcher(c.getName()).find()) {
					suggestions.add(new SearchItem(c));
				}
			}
			for (PersonCrew c : writersMovieList.keySet()) {
				if (pattern.matcher(c.getName()).find()) {
					suggestions.add(new SearchItem(c, false));
				}
			}
		}

		return suggestions;
	}

	public boolean addMovieOrTvShow(File file) {
		
		String[] tvParsedInfo = MediaSearchHandler.parseTVShowName(file.getName(), file.getParentFile().getName());
		CustomTvDb series = null;
		TvEpisode episode = null;
		TvResultsPage tRes = null;
		MovieResultsPage mRes = null;
		Integer tvDistance = 100;
		series = MediaSearchHandler.getTVInfo(tvParsedInfo[0]);
		if (series != null) {
			tvDistance = StringTools.getLevenshteinDistance(series.getName(), tvParsedInfo[0]);
			if (tvDistance == series.getName().length()) {
				tvDistance = 100;
			}
			tRes = MediaSearchHandler.getTvResults(tvParsedInfo[0]);
		}
		String[] movieParsedInfo = MediaSearchHandler.parseMovieName(file.getName());
		Integer movieDistance = 100;
		CustomMovieDb cm = null;
		if (!movieParsedInfo[0].isEmpty()) {
			if (!movieParsedInfo[1].isEmpty()) {
				cm = MediaSearchHandler.getMovieInfo(movieParsedInfo[0],
						Integer.parseInt(movieParsedInfo[1]));
				if (cm != null) {
					movieDistance =  StringTools.getLevenshteinDistance(cm.movie.getTitle(), movieParsedInfo[0]);
					if (movieDistance == cm.movie.getTitle().length()) {
						movieDistance = 100;
					}
					mRes = MediaSearchHandler.getMovieResults(movieParsedInfo[0],
						Integer.parseInt(movieParsedInfo[1]));
				}
			}
			if (cm == null) {
				cm = MediaSearchHandler.getMovieInfo(movieParsedInfo[0], 0);
				if (cm != null) {
					movieDistance =  StringTools.getLevenshteinDistance(cm.movie.getTitle(), movieParsedInfo[0]);
					if (movieDistance == cm.movie.getTitle().length()) {
						movieDistance = 100;
					}
					mRes = MediaSearchHandler.getMovieResults(movieParsedInfo[0],
							0);
				}
			}
		}
		
		// if both results are really far off, we probably failed
		if (series == null &&  cm==null) { 		
			for (MediaItem m: tempManualItems.keySet()) {
				if (m.fullFilePath.equals(file.getPath())) {
					return false;
				}
			}
			tempManualItems.put(new MediaItem(null, null, file.getPath(), file.getName(), file.getParentFile().getName()), null);
			return false;
		} else if (series != null) {
			if (cm != null) {
				if (movieDistance<tvDistance && movieDistance < 3) {
					addMovie(cm, file);
					return true;
				} else if (tvDistance < 3) {
					episode = MediaSearchHandler.getEpisodeInfo(series.getId(), Integer.parseInt(tvParsedInfo[1]), Integer.parseInt(tvParsedInfo[2]));
					addTvShow(series, episode, file);
					return true;
				} 
			} else if (tvDistance < 3) {
				episode = MediaSearchHandler.getEpisodeInfo(series.getId(), Integer.parseInt(tvParsedInfo[1]), Integer.parseInt(tvParsedInfo[2]));
				addTvShow(series, episode, file);
				return true;
			}
			for (MediaItem m: tempManualItems.keySet()) {
				if (m.fullFilePath.equals(file.getPath())) {
					return false;
				}
			}
			if (tvParsedInfo[1].isEmpty()) {
				tvParsedInfo[1] = "1";
			}
			if (tvParsedInfo[2].isEmpty()) {
				tvParsedInfo[2] = "2";
			}
			tempManualItems.put(new MediaItem(series, null, file.getPath(), file.getName(), file.getParentFile().getName()), new MediaResultsPage(tRes, Integer.parseInt(tvParsedInfo[1]), Integer.parseInt(tvParsedInfo[2])));
			return false;
			
		} else if (cm!=null) {
			if (movieDistance < 3) {
				addMovie(cm, file);
				return true;
			} else {
				for (MediaItem m: tempManualItems.keySet()) {
					if (m.fullFilePath.equals(file.getPath())) {
						return false;
					}
				}
				tempManualItems.put(new MediaItem(null, cm, file.getPath(), file.getName(), file.getParentFile().getName()), new MediaResultsPage(mRes));
				return false;
			}
		}
		return true;
	}
	
	public void addMovie(CustomMovieDb m, File filePath) {
		addMedia(null, null, m, filePath);
	}
	
	public void addTvShow(CustomTvDb t, TvEpisode e, File filePath) {
		addMedia(t, e, null, filePath);
	}

	public void addMedia(CustomTvDb t, TvEpisode episode, CustomMovieDb m, File file) {
		boolean isMovie = (m != null)? true : false;
		MediaItem mi;
		if (!isMovie && ControllerMaster.userData.ownsShow(t.getId())) {
			mi = ControllerMaster.userData.getTvById(t.getId());
			mi.tvShow.addEpisode(episode);
		} else {
			mi = new MediaItem(t, m, file.getPath(), file.getName(), file.getParentFile().getName());
			allMedia.add(mi);
			ControllerMaster.mainController.allTiles.add(ControllerMaster.mainController.addMediaTile(mi));
		}

		
		Keyword tag;
		for (int i = 0; i < mi.getKeywords().size(); ++i) {
			tag = mi.getKeywords().get(i);
			if (isMovie) {
				if (movieTags.containsKey(StringTools.capitalize(tag.getName()))) {
					movieTags.get(StringTools.capitalize(tag.getName())).add(mi.getId());
				} else {
					movieTags.put(StringTools.capitalize(tag.getName()), new ArrayList<>(Arrays.asList(mi.getId())));
				}
			} else {
				if (tvTags.containsKey(StringTools.capitalize(tag.getName()))) {
					tvTags.get(StringTools.capitalize(tag.getName())).add(mi.getId());
				} else {
					tvTags.put(StringTools.capitalize(tag.getName()), new ArrayList<>(Arrays.asList(mi.getId())));
				}
			}
		}
		if (mi.getCrew() != null) {
			PersonCrew crew;
			for (int i = 0; i < mi.getCrew().size(); ++i) {
				crew = mi.getCrew().get(i);
				if (crew.getJob().equalsIgnoreCase("Director")) {
					personList.put(crew.getId(), MediaSearchHandler.getPersonPeople(crew.getId()));
					if (isMovie) {
						if (!directorsMovieList.isEmpty() && directorsMovieList.containsKey(crew)) {
							directorsMovieList.get(crew).add(mi.getId());
						} else {
							directorsMovieList.put(crew, new ArrayList<>(Arrays.asList(mi.getId())));
						}
					} else {
						if (!directorsTvList.isEmpty() && directorsMovieList.containsKey(crew)) {
							directorsTvList.get(crew).add(mi.getId());
						} else {
							directorsTvList.put(crew, new ArrayList<>(Arrays.asList(mi.getId())));
						}
					}
				} else if (crew.getJob().equalsIgnoreCase("Screenplay") || crew.getJob().equalsIgnoreCase("Writer") || 
						crew.getJob().equalsIgnoreCase("Story") || crew.getJob().equalsIgnoreCase("Author")) {
					personList.put(crew.getId(), MediaSearchHandler.getPersonPeople(crew.getId()));
					if (isMovie) {
						if (writersMovieList.containsKey(crew)) {
							writersMovieList.get(crew).add(m.movie.getId());
						} else {
							writersMovieList.put(crew, new ArrayList<>(Arrays.asList(m.movie.getId())));
						}					
					} else {
						if (writersTvList.containsKey(crew)) {
							writersTvList.get(crew).add(mi.getId());
						} else {
							writersTvList.put(crew, new ArrayList<>(Arrays.asList(mi.getId())));
						}	
					}
				}
			}
		}
		if (mi.getCast() != null) {
			PersonCast actor;
			for (int i = 0; i<mi.getCast().size() && i<15; ++i) {
				 actor = mi.getCast().get(i);
				personList.put(actor.getId(), MediaSearchHandler.getPersonPeople(actor.getId()));
				if (isMovie) {
					if (actorsMovieList.containsKey(actor)) {
						actorsMovieList.get(actor).add(m.movie.getId());
					} else {
						actorsMovieList.put(actor, new ArrayList<>(Arrays.asList(m.movie.getId())));
					}
				} else {
					if (actorsTvList.containsKey(actor)) {
						actorsTvList.get(actor).add(mi.getId());
					} else {
						actorsTvList.put(actor, new ArrayList<>(Arrays.asList(mi.getId())));
					}
				}
			}
		}
		if (mi.getGenres() != null) {
			Genre g;
			for (int i = 0; i < mi.getGenres().size(); ++i) {
				g = mi.getGenres().get(i);
				if (isMovie) {
					if (genreMovieList.containsKey(g)) {
						genreMovieList.get(g).add(mi.getId());
					} else {
						genreMovieList.put(g, new ArrayList<>(Arrays.asList(mi.getId())));
					}
				} else {
					if (genreTvList.containsKey(g)) {
						genreTvList.get(g).add(mi.getId());
					} else {
						genreTvList.put(g, new ArrayList<>(Arrays.asList(mi.getId())));
					}
				}
			}
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
		try {
			Date date = formatter.parse(mi.getReleaseDate());
			int year = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(date)).getYear();
			if (minYear == 0 || minYear > year) {
				minYear = year;
				ControllerMaster.mainController.fillYearCombos(minYear, maxYear);
			} 
			if (maxYear == 0 || maxYear < year) {
				maxYear = year;
				ControllerMaster.mainController.fillYearCombos(minYear, maxYear);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	

	public void removeMovie(MovieDb m) {

	}
	
	public void sortShownItems() {
		ObservableList<Node> workingCollection = FXCollections.observableArrayList((ControllerMaster.mainController.tilePane.getChildren()));
		switch (ControllerMaster.mainController.sortCombo.getValue()) {
		case NAME_ASC:
			Collections.sort(workingCollection, (o1, o2) -> ((JFXMediaRippler)o1).linkedItem.getTitle().compareTo(((JFXMediaRippler)o2).linkedItem.getTitle()));
			break;
		case NAME_DESC:
			Collections.sort(workingCollection, (o2, o1) -> ((JFXMediaRippler)o1).linkedItem.getTitle().compareTo(((JFXMediaRippler)o2).linkedItem.getTitle()));
			break;
		case ADDED_DATE_ASC:
			Collections.sort(workingCollection, (o1, o2) -> ((JFXMediaRippler)o1).linkedItem.dateAdded.compareTo(((JFXMediaRippler)o2).linkedItem.dateAdded));
			break;
		case ADDED_DATE_DESC:
			Collections.sort(workingCollection, (o2, o1) -> ((JFXMediaRippler)o1).linkedItem.dateAdded.compareTo(((JFXMediaRippler)o2).linkedItem.dateAdded));
			break;
		case RELEASE_DATE_ASC:
			Collections.sort(workingCollection, (o1, o2) -> ((JFXMediaRippler)o1).linkedItem.getReleaseDate().compareTo(((JFXMediaRippler)o2).linkedItem.getReleaseDate()));
			break;
		case RELEASE_DATE_DESC:
			Collections.sort(workingCollection, (o1, o2) -> ((JFXMediaRippler)o2).linkedItem.getReleaseDate().compareTo(((JFXMediaRippler)o1).linkedItem.getReleaseDate()));
			break;
		default:
			break;
		}
		ControllerMaster.mainController.tilePane.getChildren().setAll(workingCollection);
	}

	public void refreshViewingList(Map<String, List<Integer>> map, boolean retainScrollPos) {
		List<Integer> moviesList = null;
		List<Integer> tvList = null;
		if (map != null && map.containsKey("movies")) {
			moviesList = map.get("movies");
		} 
		if (map != null && map.containsKey("tv")) {
			tvList = map.get("tv");
		}
		ObservableList<Node> workingCollection = FXCollections.observableArrayList();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
		Date startDate = null;
		Date endDate = null;
		try {
			startDate = (ControllerMaster.mainController.startYearComboBox.getValue()==null)? null : formatter.parse(ControllerMaster.mainController.startYearComboBox.getValue() +"-01-01");
			endDate = (ControllerMaster.mainController.endYearComboBox.getValue()==null)? null : formatter.parse(ControllerMaster.mainController.endYearComboBox.getValue() +"-12-31");
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		
		Date parsedDate;
		JFXMediaRippler mRip;
		String type;
		ControllerMaster.mainController.showingMedia.clear();
		for (int i = 0; i < ControllerMaster.mainController.allTiles.size(); ++i) {
			mRip = ControllerMaster.mainController.allTiles.get(i);
			if (  (map == null) || (mRip.linkedItem.isMovie() && moviesList.contains(mRip.linkedItem.getId())) ||
					(!mRip.linkedItem.isMovie() && tvList.contains(mRip.linkedItem.getId())) ) {
				try {
					parsedDate = formatter.parse(mRip.linkedItem.getReleaseDate());
					if (startDate==null || startDate.before(parsedDate)) {
						if (endDate==null || endDate.after(parsedDate)) {
							if (mRip.linkedItem.isMovie()) {
								type = "movie";
							} else {
								type = "tv";
							}
							ControllerMaster.mainController.showingMedia.put(type, mRip.linkedItem);
							workingCollection.add(mRip);
						}
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}	
			} else if ((moviesList == null && tvList == null) || tvList.contains(mRip.linkedItem.getId())){
				try {
					parsedDate = formatter.parse(mRip.linkedItem.getReleaseDate());
					if (startDate==null || startDate.before(parsedDate)) {
						if (endDate==null || endDate.after(parsedDate)) {
							ControllerMaster.mainController.showingMedia.put("tv", mRip.linkedItem);
							workingCollection.add(mRip);
						}
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}	
			}
		}
		ControllerMaster.mainController.tilePane.getChildren().setAll(workingCollection);
		sortShownItems();
		//force resize of nodes to account for size changes made while searching
		ControllerMaster.mainController.tilePane.setVgap(15*getScaleFactor());
		ControllerMaster.mainController.tilePane.setHgap(10*getScaleFactor());
		StackPane n;
		for (int i = 0; i < ControllerMaster.mainController.tilePane.getChildren().size(); ++i) {
			n = ((JFXMediaRippler)ControllerMaster.mainController.tilePane.getChildren().get(i)).getPane();
			n.setMaxWidth(139*getScaleFactor());
			n.setMaxHeight(208*getScaleFactor());
			n.resize(139*getScaleFactor(), 208*getScaleFactor());
		}
	}
	
	public void createAllRipplers() {
		for (int i = 0; i < allMedia.size(); ++i) {
			ControllerMaster.mainController.allTiles.add(ControllerMaster.mainController.addMediaTile(allMedia.get(i)));
		}
	}
	
	public boolean hasPath(String path) {
		return allMedia.stream().map(MediaItem::getFullFilePath).filter(path::equals).findFirst().isPresent();
	}	

}