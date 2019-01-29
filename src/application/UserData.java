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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

import application.SearchItem.SearchTypes;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.Collection;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.people.Person;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCredit;
import info.movito.themoviedbapi.model.people.PersonCredits;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.people.PersonPeople;
import info.movito.themoviedbapi.model.tv.TvSeries;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public class UserData implements Serializable {

	public final transient static TmdbApi apiLinker = new TmdbApi(getAPIKey("THE_MOVIE_DB_API_TOKEN"));  //your key goes in api_keys.xml
	
	private static final long serialVersionUID = 1L;
	private List<MediaItem> allMedia = new ArrayList<>();
	private TreeMap<String, List<Integer>> movieTags = new TreeMap<>(String.CASE_INSENSITIVE_ORDER); // cache list of tags for search updating
	private TreeMap<String, List<Integer>> tvTags = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private LinkedHashMap<PersonCrew, List<Integer>> directorsMovieList = new LinkedHashMap<>();
	private LinkedHashMap<PersonCrew, List<Integer>> writersMovieList = new LinkedHashMap<>();
	private LinkedHashMap<PersonCast, List<Integer>> actorsMovieList = new LinkedHashMap<>();
	private LinkedHashMap<Genre, List<Integer>> genreMovieList = new LinkedHashMap<>();
	private LinkedHashMap<String, List<Integer>> userMovieLists = new LinkedHashMap<>();
	private LinkedHashMap<PersonCrew, List<Integer>> directorsTvList = new LinkedHashMap<>();
	private LinkedHashMap<PersonCrew, List<Integer>> writersTvList = new LinkedHashMap<>();
	private LinkedHashMap<PersonCast, List<Integer>> actorsTvList = new LinkedHashMap<>();
	private LinkedHashMap<Genre, List<Integer>> genreTvList = new LinkedHashMap<>();
	private LinkedHashMap<String, List<Integer>> userTvLists = new LinkedHashMap<>();
	private LinkedHashMap<Integer, PersonPeople> personList = new LinkedHashMap<>();
	private LinkedHashMap<Integer, PersonCredits> creditsList = new LinkedHashMap<>();
	private List<String> allPaths = new ArrayList<>();
	private double scaleFactor = 1.0;
	public LinkedHashMap<Integer, MovieDb> seenMovies = new LinkedHashMap<>();
	public LinkedHashMap<Integer, TvSeries> seenTv = new LinkedHashMap<>();
	public LinkedHashMap<Integer, List<PersonCredit>> knownFor = new LinkedHashMap<>();
	public MediaPlaylist userPlaylists = new MediaPlaylist();
	public LinkedHashMap<Collection, List<MediaItem>> ownedCollections = new LinkedHashMap<>();
	public transient LinkedHashMap<MediaItem, MediaResultsPage> tempManualItems = new LinkedHashMap<>();
	public int minYear = 0;
	public int maxYear = 0;
	
	//settings
	public boolean useAutoLookup = true;
	public ThemeSelection themeSelection = ThemeSelection.themes.get(0);

	
	
	//get key from api_keys.xml by name
	public static String getAPIKey(String keyname) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inputSource;
		try {
			URL url = UserData.class.getClassLoader().getResource("api_keys.xml");
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
	
	public List<Integer> getMoviesWithActor(PersonCast actor) {
		if (actorsMovieList.containsKey(actor)) {
			return actorsMovieList.get(actor);
		} 
		return new ArrayList<>();
	}
	
	public List<Integer> getTvWithActor(PersonCast actor) {
		if (actorsTvList.containsKey(actor)) {
			return actorsTvList.get(actor);
		} 
		return new ArrayList<>();
	}

	public List<Integer> getMoviesWithDirector(PersonCrew director) {
		if (directorsMovieList.containsKey(director)) {
			return directorsMovieList.get(director);
		} 
		return new ArrayList<>();
	}
	
	public List<Integer> getTvWithDirector(PersonCrew director) {
		if (directorsTvList.containsKey(director)) {
			return directorsTvList.get(director);
		} 
		return new ArrayList<>();
	}

	public List<Integer> getMoviesWithTag(String tag) {
		if (movieTags.containsKey(tag)) {
			return movieTags.get(tag);
		}
		return new ArrayList<>();
	}
	
	public List<Integer> getTvWithTag(String tag) {
		if (tvTags.containsKey(tag)) {
			return tvTags.get(tag);
		}
		return new ArrayList<>();
	}
	
	public List<Integer> getMoviesWithWriter(PersonCrew writer) {
		if (writersMovieList.containsKey(writer)) {
			return writersMovieList.get(writer);
		} 
		return new ArrayList<>();
	}
	
	public List<Integer> getTvWithWriter(PersonCrew writer) {
		if (writersTvList.containsKey(writer)) {
			return writersTvList.get(writer);
		} 
		return new ArrayList<>();
	}
	
	public List<Integer> getMoviesWithGenre(Genre g) {
		if (genreMovieList.containsKey(g)) {
			return genreMovieList.get(g);
		} 
		return new ArrayList<>();
	}
	
	public List<Integer> getTvWithGenre(Genre g) {
		if (genreTvList.containsKey(g)) {
			return genreTvList.get(g);
		} 
		return new ArrayList<>();
	}
	
	public boolean ownsMovie(int iD) {
		for (int i = 0; i < getAllMedia().size(); ++i) {
			if (getAllMedia().get(i).isMovie() && getAllMedia().get(i).getId()==iD) {
				return true;
			}
		}
		return false;
	}
	
	public MediaItem getMovieById(int iD) {
		for (int i = 0; i < getAllMedia().size(); ++i) {
			if (getAllMedia().get(i).isMovie() && getAllMedia().get(i).getId()==iD) {
				return getAllMedia().get(i);
			}
		}
		if (seenMovies.containsKey(iD)) {
			return new MediaItem(new CustomMovieDb(seenMovies.get(iD)));
		}
		return null;
	}
	
	public boolean ownsShow(int iD) {
		for (int i = 0; i < getAllMedia().size(); ++i) {
			if (getAllMedia().get(i).isTvShow() && getAllMedia().get(i).getId()==iD) {
				return true;
			}
		}
		return false;
	}
	
	public boolean ownsEpisode(int iD, int seasonNum, int epNum) {
		for (int i = 0; i < getAllMedia().size(); ++i) {
			if (getAllMedia().get(i).isTvShow() && getAllMedia().get(i).getId()==iD) {
				List<Integer> episodes = getAllMedia().get(i).tvShow.getOwnedEpisodeNumbers(seasonNum);
				return episodes.contains(epNum);
			}
		}
		return false;
	}
	
	public MediaItem getTvById(int iD) {
		for (int i = 0; i < getAllMedia().size(); ++i) {
			if (getAllMedia().get(i).isTvShow() && getAllMedia().get(i).getId()==iD) {
				return getAllMedia().get(i);
			}
		}
		if (seenTv.containsKey(iD)) {
			return new MediaItem(new CustomTvDb(seenTv.get(iD)));
		}
		return null;
	}
	
	public int numMediaItems() {
		return getAllMedia().size() + getAllMedia().size();
	}

	public UserData() {
		tryLoadFile();
	}


	public void saveAll() {
		new File("save_data").mkdirs();
		File file = new File("save_data/userdata.dat");

		try {
			FileOutputStream saveFile = new FileOutputStream(file);
			ObjectOutputStream save = new ObjectOutputStream(saveFile);
			save.writeObject(this);
			save.close();
			saveFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void tryLoadFile() {
		InputStream inputStream = null;
		ObjectInputStream objectInputStream = null;
		new File("save_data").mkdirs();
		File file = new File("save_data/userdata.dat");
		URL url;
		try {
			url = file.toURI().toURL();
			if (url!=null && file.exists()) {
				inputStream = url.openStream();
				objectInputStream = new ObjectInputStream(inputStream);
				UserData tempDat = (UserData)objectInputStream.readObject();
				objectInputStream.close();
				inputStream.close();
				allMedia = tempDat.getAllMedia();
				movieTags = tempDat.movieTags;
				directorsMovieList = tempDat.directorsMovieList;
				writersMovieList = tempDat.writersMovieList;
				actorsMovieList = tempDat.actorsMovieList;
				genreMovieList = tempDat.genreMovieList;
				userMovieLists = tempDat.userMovieLists;
				directorsTvList = tempDat.directorsTvList;
				writersTvList = tempDat.writersTvList;
				actorsTvList = tempDat.actorsTvList;
				genreTvList = tempDat.genreTvList;
				userTvLists = tempDat.userTvLists;
				scaleFactor = tempDat.scaleFactor;
				personList = tempDat.personList;
				creditsList = tempDat.creditsList;
				seenMovies = tempDat.seenMovies;
				seenTv = tempDat.seenTv;
				knownFor = tempDat.knownFor;
				minYear = tempDat.minYear;
				maxYear = tempDat.maxYear;
				userPlaylists = tempDat.userPlaylists;
				ownedCollections = tempDat.ownedCollections;
				useAutoLookup = tempDat.useAutoLookup;
				themeSelection = tempDat.themeSelection;
			} 
			return;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}		
	}
	
	public void clearSaveData() {
		getAllMedia().clear();
		movieTags.clear();
		directorsMovieList.clear();
		writersMovieList.clear();
		actorsMovieList.clear();
		genreMovieList.clear();
		userMovieLists.clear();
		directorsTvList.clear();
		writersTvList.clear();
		actorsTvList.clear();
		genreTvList.clear();
		userTvLists.clear();
		scaleFactor = 1;
		personList.clear();
		creditsList.clear();
		seenMovies.clear();
		seenTv.clear();
		knownFor.clear();
		minYear = 0;
		maxYear = 0;
		userPlaylists.clear();
		ownedCollections.clear();
	}	
	
	final String regExSpecialChars = "<([{\\^-=$!|]})?*+.>";
	@SuppressWarnings("ReplaceAllDot")
	final String regExSpecialCharsRE = regExSpecialChars.replaceAll( ".", "\\\\$0");
	final Pattern reCharsREP = Pattern.compile( "[" + regExSpecialCharsRE + "]");

	//escape all special characters in strings for use with regex
	String quoteRegExSpecialChars(String s) {
	    Matcher m = reCharsREP.matcher(s);
	    return m.replaceAll("\\\\$0");
	}

	public ObservableList<SearchItem> getAutoCompleteItems(String userInput) {
		ObservableList<SearchItem> suggestions = FXCollections.observableArrayList();
		boolean detailedLength = userInput.length()>1;
		if (userInput.equals("")) {
			return suggestions;
		}
		userInput = quoteRegExSpecialChars(userInput); // escape special characters so users can search literally
		Pattern pattern = Pattern.compile("(?i)(^|\\s|\\()" + userInput);
		for (int i = 0; i < getAllMedia().size(); ++i) {
			if (pattern.matcher(getAllMedia().get(i).getItemName()).find()) {
				suggestions.add(new SearchItem(SearchTypes.TITLE, getAllMedia().get(i)));
			}
		}
		addGenreSuggestion(genreMovieList.keySet(), pattern, suggestions);
		addGenreSuggestion(genreTvList.keySet(), pattern, suggestions);
		if (detailedLength || suggestions.size()==0) {
			addTagSuggestion(movieTags.keySet(), pattern, suggestions);
			addTagSuggestion(tvTags.keySet(), pattern, suggestions);
			addPeopleSuggestion(directorsMovieList.keySet(), pattern, suggestions, SearchTypes.DIRECTOR);
			addPeopleSuggestion(directorsTvList.keySet(), pattern, suggestions, SearchTypes.DIRECTOR);
			addPeopleSuggestion(actorsMovieList.keySet(), pattern, suggestions, SearchTypes.ACTOR);
			addPeopleSuggestion(actorsTvList.keySet(), pattern, suggestions, SearchTypes.ACTOR);
			addPeopleSuggestion(writersMovieList.keySet(), pattern, suggestions, SearchTypes.WRITER);
			addPeopleSuggestion(writersTvList.keySet(), pattern, suggestions, SearchTypes.WRITER);
		}

		return suggestions;
	}
	
	public void addGenreSuggestion(Set<Genre> set, Pattern pattern, ObservableList<SearchItem> suggestions) {
		for (Genre t : set) {
			if (pattern.matcher(t.getName()).find()) {
				suggestions.add(new SearchItem(SearchTypes.GENRE, t));
			}
		}
	}
	
	public void addTagSuggestion(Set<String> set, Pattern pattern, ObservableList<SearchItem> suggestions) {
		for (String t : set) {
			if (pattern.matcher(t).find()) {
				suggestions.add(new SearchItem(SearchTypes.TAG, t));
			}
		}
	}
	
	public <T extends Person> void addPeopleSuggestion(Set<T> set, Pattern pattern, ObservableList<SearchItem> suggestions, SearchTypes type) {
		for (T t : set) {
			if (pattern.matcher(t.getName()).find()) {
				suggestions.add(new SearchItem(type, t));
			}
		}
	}
	
	public void addTag(Keyword tag, int mId, boolean isMovie) {
		if (isMovie) {
			addTag(movieTags, tag, mId);
		} else {
			addTag(tvTags, tag, mId);
		}
	}
	
	private void addTag(TreeMap<String, List<Integer>> tagList, Keyword tag, int mId) {
		if (tagList.containsKey(StringTools.capitalize(tag.getName()))) {
			tagList.get(StringTools.capitalize(tag.getName())).add(mId);
		} else {
			tagList.put(StringTools.capitalize(tag.getName()), new ArrayList<>(Collections.singletonList(mId)));
		}
	}
	
	public void addPerson(PersonCast p, int mId, boolean isMovie) {
		if (!personList.containsKey(p.getId())) {
			personList.put(p.getId(), MediaSearchHandler.getPersonPeople(p.getId()));
		}
		if (isMovie) {
			addToList(actorsMovieList, p, mId);
		} else {
			addToList(actorsTvList, p, mId);
		}
	}
	
	public void addPerson(PersonCrew p, int mId, boolean isMovie) {
		if (!personList.containsKey(p.getId())) {
			personList.put(p.getId(), MediaSearchHandler.getPersonPeople(p.getId()));
		}
		if (p.getJob().equalsIgnoreCase("Director")) {
			if (isMovie) {
				addToList(directorsMovieList, p, mId);
			} else {
				addToList(directorsTvList, p, mId);
			}
		} else if (p.getJob().equalsIgnoreCase("Screenplay") || p.getJob().equalsIgnoreCase("Writer") || 
				p.getJob().equalsIgnoreCase("Story") || p.getJob().equalsIgnoreCase("Author")) {
			if (isMovie) {
				addToList(writersMovieList, p, mId);
			} else {
				addToList(writersTvList, p, mId);
			}
		}
	}
	
	public void addGenre(Genre g, int mId, boolean isMovie) {
		if (isMovie) {
			addToList(genreMovieList, g, mId);
		} else {
			addToList(genreTvList, g, mId);
		}
	}
	
	public <T> void addToList(LinkedHashMap<T, List<Integer>> list, T g, int mId) {
		if (list.containsKey(g)) {
			list.get(g).add(mId);
		} else {
			list.put(g, new ArrayList<>(Collections.singletonList(mId)));
		}
	}

	public void removeMovie(MovieDb m) {

	}
	
	public void sortShownItems() {
		ObservableList<Node> workingCollection = FXCollections.observableArrayList((ControllerMaster.mainController.tilePane.getChildren()));
		switch (ControllerMaster.mainController.sortCombo.getValue()) {
		case NAME_ASC:
			workingCollection.sort((o1, o2) -> ((JFXMediaRippler) o1).linkedItem.getTitle().compareTo(((JFXMediaRippler) o2).linkedItem.getTitle()));
			break;
		case NAME_DESC:
			workingCollection.sort((o2, o1) -> ((JFXMediaRippler) o1).linkedItem.getTitle().compareTo(((JFXMediaRippler) o2).linkedItem.getTitle()));
			break;
		case ADDED_DATE_ASC:
			workingCollection.sort((o1, o2) -> ((JFXMediaRippler) o1).linkedItem.dateAdded.compareTo(((JFXMediaRippler) o2).linkedItem.dateAdded));
			break;
		case ADDED_DATE_DESC:
			workingCollection.sort((o2, o1) -> ((JFXMediaRippler) o1).linkedItem.dateAdded.compareTo(((JFXMediaRippler) o2).linkedItem.dateAdded));
			break;
		case RELEASE_DATE_ASC:
			workingCollection.sort((o1, o2) -> ((JFXMediaRippler) o1).linkedItem.getReleaseDate().compareTo(((JFXMediaRippler) o2).linkedItem.getReleaseDate()));
			break;
		case RELEASE_DATE_DESC:
			workingCollection.sort((o1, o2) -> ((JFXMediaRippler) o2).linkedItem.getReleaseDate().compareTo(((JFXMediaRippler) o1).linkedItem.getReleaseDate()));
			break;
		default:
			break;
		}
		ControllerMaster.mainController.tilePane.getChildren().setAll(workingCollection);
	}

	public void refreshViewingList(Map<String, List<Integer>> map) {
		List<Integer> moviesList = new ArrayList<>();
		List<Integer> tvList = new ArrayList<>();
		if (map.containsKey("movies")) {
			moviesList = map.get("movies");
		}
		if (map.containsKey("tv")) {
			tvList = map.get("tv");
		}
		ObservableList<Node> workingCollection = FXCollections.observableArrayList();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = null;
		Date endDate = null;
		try {
			startDate = (ControllerMaster.mainController.startYearComboBox.getValue() == null) ? null : formatter.parse(ControllerMaster.mainController.startYearComboBox.getValue() + "-01-01");
			endDate = (ControllerMaster.mainController.endYearComboBox.getValue() == null) ? null : formatter.parse(ControllerMaster.mainController.endYearComboBox.getValue() + "-12-31");
		} catch (ParseException e1) {
			e1.printStackTrace();
		}

		boolean noSearchIds = moviesList.isEmpty() && tvList.isEmpty();
		boolean canShowMovies = ControllerMaster.mainController.mediaTypeCombo.getValue() != MediaListDisplayType.TVSHOWS;
		boolean canShowTv = ControllerMaster.mainController.mediaTypeCombo.getValue() != MediaListDisplayType.MOVIES;
		List<MediaItem> selectedPlaylist = (ControllerMaster.mainController.playlistCombo.getValue() == null) ?
				new ArrayList<>() : userPlaylists.getPlaylist(ControllerMaster.mainController.playlistCombo.getValue());
		List<MediaItem> selectedCollection = (ControllerMaster.mainController.collectionsCombo.getValue() == null) ?
				new ArrayList<>() : ownedCollections.get(ControllerMaster.mainController.collectionsCombo.getValue());
		Date parsedDate = null;
		JFXMediaRippler mRip;
		MediaItem mi;
		int mId;
		String type;
		ControllerMaster.mainController.showingMedia.clear();
		for (int i = 0; i < ControllerMaster.mainController.allTiles.size(); ++i) {
			mRip = ControllerMaster.mainController.allTiles.get(i);
			mi = mRip.linkedItem;
			mId = mi.getId();
			//if it fits search criteria
			if (noSearchIds || (tvList.contains(mId) && !mi.isMovie()) || (moviesList.contains(mId) && mi.isMovie())) {
				//if it can be shown
				if ((mi.isMovie() && canShowMovies) || (!mi.isMovie() && canShowTv)) {
					//if no playlist is selected or it contains this
					if (selectedPlaylist.isEmpty() || selectedPlaylist.contains(mi)) {
						if (selectedCollection.isEmpty() || selectedCollection.contains(mi)) {
							try {
								parsedDate = formatter.parse(mRip.linkedItem.getReleaseDate());
							} catch (ParseException e) {
								//e.printStackTrace(); happens occasionally from improperly maintained db items
							}
							//if it's in the proper date selection
							if (startDate == null || parsedDate == null || startDate.before(parsedDate)) {
								if (endDate == null || parsedDate == null || endDate.after(parsedDate)) {
									if (mRip.linkedItem.isMovie()) {
										type = "movie";
									} else {
										type = "tv";
									}
									ControllerMaster.mainController.showingMedia.put(type, mRip.linkedItem);
									workingCollection.add(mRip);
								}
							}
						}
					}
				}
			}
		}
		ControllerMaster.mainController.tilePane.getChildren().setAll(workingCollection);
		sortShownItems();
		ControllerMaster.mainController.updateScale();
	}
	
	//check if file has already been added.  Used to avoid unnecessary lookups
	public boolean hasPath(String path) {
		return allPaths.contains(path);
	}
	
	public void addPath(String path) {
		allPaths.add(path);
	}

	public List<MediaItem> getAllMedia() {
		return allMedia;
	}
	

}