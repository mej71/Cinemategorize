package application;

import application.SearchItem.SearchTypes;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.Collection;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.people.*;
import info.movito.themoviedbapi.model.tv.TvSeries;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class UserData implements Serializable {

	transient static TmdbApi apiLinker;  //your key goes in api_keys.xml
	final transient static String movieIdentifier = "movie";
	final transient static String tvIdentifier = "tv";

	private static final long serialVersionUID = 1L;
	private List<MediaItem> allMedia = new ArrayList<>();
	private TreeMap<String, LinkedHashMap<String, List<Integer>>> mediaTags = new TreeMap<>(String.CASE_INSENSITIVE_ORDER); // cache list of tags for search updating
	private LinkedHashMap<PersonCrew, LinkedHashMap<String, List<Integer>>> directorList = new LinkedHashMap<>();
	private LinkedHashMap<PersonCrew, LinkedHashMap<String, List<Integer>>> writerList = new LinkedHashMap<>();
	private LinkedHashMap<PersonCast, LinkedHashMap<String, List<Integer>>> actorList = new LinkedHashMap<>();
	private LinkedHashMap<Genre, LinkedHashMap<String, List<Integer>>> genreList = new LinkedHashMap<>();
	private LinkedHashMap<Integer, PersonPeople> personList = new LinkedHashMap<>();
	private LinkedHashMap<Integer, PersonCredits> creditsList = new LinkedHashMap<>();
	private List<String> allPaths = new ArrayList<>();
	private double scaleFactor = 1.0;
	LinkedHashMap<Integer, MovieDb> seenMovies = new LinkedHashMap<>();
	LinkedHashMap<Integer, TvSeries> seenTv = new LinkedHashMap<>();
	LinkedHashMap<Integer, List<PersonCredit>> knownFor = new LinkedHashMap<>();
	List<MediaPlaylist> userPlaylists = new ArrayList<>();
	LinkedHashMap<Collection, List<MediaItem>> ownedCollections = new LinkedHashMap<>();
	transient LinkedHashMap<MediaItem, MediaResultsPage> tempManualItems = new LinkedHashMap<>();
	int minYear = 0;
	int maxYear = 0;
	
	//settings
	boolean useAutoLookup = true;
	ThemeSelection themeSelection = ThemeSelection.themes.get(0);

	//get key from api_keys.xml by name
	public static String getAPIKey(String keyname) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inputSource;
		try {
			URL url = UserData.class.getClassLoader().getResource("api_keys.xml");
			inputSource = new InputSource(Objects.requireNonNull(url).openStream());
			return xpath.evaluate("/resources/" + keyname, inputSource);
		} catch (IOException | XPathExpressionException e) {
			System.out.println("Invalid key in api_keys.xml for " + keyname);
			System.out.println(e);
			return "";
		}
	}

	public UserData() {
		updateApiLinker();
		tryLoadFile();
		if (userPlaylists.isEmpty()) {
			//Create favorites list if empty
			MediaPlaylist favoriteList = new MediaPlaylist("Favorites");
			favoriteList.canDelete = false;
			userPlaylists.add(favoriteList);
		}
	}

	void updateApiLinker() {
		try {
			apiLinker = new TmdbApi(getAPIKey("THE_MOVIE_DB_API_TOKEN"));
		} catch (Exception e) {
			apiLinker = null; //offline mode
		}
	}

	MediaPlaylist getPlaylistByName(String name) {
		for (int i = 0; i < userPlaylists.size(); ++i) {
			if (userPlaylists.get(i).getName().equalsIgnoreCase(name)) {
				return userPlaylists.get(i);
			}
		}
		return null;
	}

	private static final String favoritesName = "Favorites";
	boolean favoritesContains(MediaItem mi) {
		MediaPlaylist favorites = getPlaylistByName(favoritesName);
		return favorites != null && favorites.getItems().contains(mi);
	}

	MediaPlaylist getFavoritesList() {
		return getPlaylistByName(favoritesName);
	}
	
	
	public PersonCredits getCredits(int personId) {
		if (!creditsList.containsKey(personId)) {
			updateApiLinker();
			if (UserData.apiLinker != null) {
				PersonCredits credits = MediaSearchHandler.getPersonCombinedCredits(personId);
				creditsList.put(personId, credits);
			} else {
				return null;
			}
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
		if (actorList.containsKey(actor) && actorList.get(actor).get(movieIdentifier) != null && !actorList.get(actor).get(movieIdentifier).isEmpty()) {
			return actorList.get(actor).get(movieIdentifier);
		} 
		return new ArrayList<>();
	}
	
	public List<Integer> getTvWithActor(PersonCast actor) {
		if (actorList.containsKey(actor) && actorList.get(actor).get(tvIdentifier) != null && !actorList.get(actor).get(tvIdentifier).isEmpty()) {
			return actorList.get(actor).get(tvIdentifier);
		}
		return new ArrayList<>();
	}

	public List<Integer> getMoviesWithDirector(PersonCrew director) {
		if (directorList.containsKey(director) && directorList.get(director).get(movieIdentifier) != null && !directorList.get(director).get(movieIdentifier).isEmpty()) {
			return directorList.get(director).get(movieIdentifier);
		}
		return new ArrayList<>();
	}
	
	public List<Integer> getTvWithDirector(PersonCrew director) {
		if (directorList.containsKey(director) && directorList.get(director).get(tvIdentifier) != null && !directorList.get(director).get(tvIdentifier).isEmpty()) {
			return directorList.get(director).get(tvIdentifier);
		}
		return new ArrayList<>();
	}

	public List<Integer> getMoviesWithTag(String tag) {
		if (mediaTags.containsKey(tag) && mediaTags.get(tag).get(movieIdentifier) != null && !mediaTags.get(tag).get(movieIdentifier).isEmpty()) {
			return mediaTags.get(tag).get(movieIdentifier);
		}
		return new ArrayList<>();
	}
	
	public List<Integer> getTvWithTag(String tag) {
		if (mediaTags.containsKey(tag) && mediaTags.get(tag).get(tvIdentifier) != null && !mediaTags.get(tag).get(tvIdentifier).isEmpty()) {
			return mediaTags.get(tag).get(tvIdentifier);
		}
		return new ArrayList<>();
	}
	
	public List<Integer> getMoviesWithWriter(PersonCrew writer) {
		if (writerList.containsKey(writer) && writerList.get(writer).get(movieIdentifier) != null && !writerList.get(writer).get(movieIdentifier).isEmpty()) {
			return writerList.get(writer).get(movieIdentifier);
		}
		return new ArrayList<>();
	}
	
	public List<Integer> getTvWithWriter(PersonCrew writer) {
		if (writerList.containsKey(writer) && writerList.get(writer).get(tvIdentifier) != null && !writerList.get(writer).get(tvIdentifier).isEmpty()) {
			return writerList.get(writer).get(tvIdentifier);
		}
		return new ArrayList<>();
	}
	
	public List<Integer> getMoviesWithGenre(Genre g) {
		if (genreList.containsKey(g) && genreList.get(g).get(movieIdentifier) != null && !genreList.get(g).get(movieIdentifier).isEmpty()) {
			return genreList.get(g).get(movieIdentifier);
		}
		return new ArrayList<>();
	}
	
	public List<Integer> getTvWithGenre(Genre g) {
		if (genreList.containsKey(g) && genreList.get(g).get(tvIdentifier) != null && !genreList.get(g).get(tvIdentifier).isEmpty()) {
			return genreList.get(g).get(tvIdentifier);
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

	public void saveAll() {
		String base = "";
		try {
			base = new File(".").getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new File(base + "/save_data").mkdirs();
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
		InputStream inputStream;
		ObjectInputStream objectInputStream;
		new File("save_data").mkdirs();
		File file = new File("save_data/userdata.dat");
		URL url;
		try {
			url = file.toURI().toURL();
			if (file.exists()) {
				inputStream = url.openStream();
				objectInputStream = new ObjectInputStream(inputStream);
				UserData tempDat = (UserData)objectInputStream.readObject();
				objectInputStream.close();
				inputStream.close();
				allMedia = tempDat.getAllMedia();
				mediaTags = tempDat.mediaTags;
				directorList = tempDat.directorList;
				actorList = tempDat.actorList;
				writerList = tempDat.writerList;
				genreList = tempDat.genreList;
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
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}		
	}
	
	public void clearSaveData() {
		getAllMedia().clear();
		mediaTags.clear();
		directorList.clear();
		genreList.clear();
		actorList.clear();
		writerList.clear();
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

	public List<SearchItem> getAutoCompleteItems(String userInput) {
		List<SearchItem> suggestions = new ArrayList<>();
		boolean detailedLength = userInput.length() > 1;
		if (userInput.equals("")) {
			return suggestions;
		}
		userInput = quoteRegExSpecialChars(userInput); // escape special characters so users can search literally
		Pattern pattern = Pattern.compile("(?i)(^|\\s|\\()" + userInput);
		Matcher matcher = pattern.matcher("");
		for (int i = 0; i < getAllMedia().size(); ++i) {
			matcher.reset(getAllMedia().get(i).getItemName());
			if (matcher.find()) {
				suggestions.add(new SearchItem(SearchTypes.TITLE, getAllMedia().get(i)));
			}
		}
		Collections.addAll(suggestions, addGenreSuggestion(genreList.keySet(), pattern, matcher));
		if (detailedLength || suggestions.size()==0) {
			Collections.addAll(suggestions, addTagSuggestion(mediaTags.keySet(), pattern, matcher));
			Collections.addAll(suggestions, addPeopleSuggestion(directorList.keySet(), pattern, matcher, SearchTypes.DIRECTOR));
			Collections.addAll(suggestions, addPeopleSuggestion(actorList.keySet(), pattern, matcher, SearchTypes.ACTOR));
			Collections.addAll(suggestions, addPeopleSuggestion(writerList.keySet(), pattern, matcher, SearchTypes.WRITER));
		}
		return suggestions;
	}

	public SearchItem[] addGenreSuggestion(Set<Genre> set, Pattern pattern, Matcher matcher) {
		List<SearchItem> suggestions = new ArrayList<>();
		for (Genre t : set) {
			matcher.reset(t.getName());
			if (matcher.find()) {
				suggestions.add(new SearchItem(SearchTypes.GENRE, t));
			}
		}
		return suggestions.toArray(new SearchItem[suggestions.size()]);
	}
	
	public SearchItem[]  addTagSuggestion(Set<String> set, Pattern pattern, Matcher matcher) {
		List<SearchItem> suggestions = new ArrayList<>();
		for (String t : set) {
			matcher.reset(t);
			if (matcher.find()) {
				suggestions.add(new SearchItem(SearchTypes.TAG, t));
			}
		}
		return suggestions.toArray(new SearchItem[suggestions.size()]);
	}
	
	public <T extends Person> SearchItem[]  addPeopleSuggestion(Set<T> set, Pattern pattern, Matcher matcher, SearchTypes type) {
		List<SearchItem> suggestions = new ArrayList<>();
		for (T t : set) {
			matcher.reset(t.getName());
			if (matcher.find()) {
				suggestions.add(new SearchItem(type, t));
			}
		}
		return suggestions.toArray(new SearchItem[suggestions.size()]);
	}
	
	public void addTag(Keyword tag, int mId, boolean isMovie) {
		String mediaType = (isMovie)? movieIdentifier : tvIdentifier;
		if (!mediaTags.containsKey(tag.getName())) {
			LinkedHashMap<String, List<Integer>> map = new LinkedHashMap<>();
			map.put(mediaType, new ArrayList<>(Collections.singletonList(mId)));
			mediaTags.put(tag.getName(), map);
		} else if (!mediaTags.get(tag.getName()).containsKey(mediaType)) {
			mediaTags.get(tag.getName()).put(mediaType, new ArrayList<>(Collections.singletonList(mId)));
		} else if (!mediaTags.get(tag.getName()).get(mediaType).contains(mId)){
			mediaTags.get(tag.getName()).get(mediaType).add(mId);
		}
	}
	
	public void addPerson(PersonCast p, int mId, boolean isMovie) {
		if (!personList.containsKey(p.getId())) {
			personList.put(p.getId(), MediaSearchHandler.getPersonPeople(p.getId()));
		}
		addToList(actorList, p, mId, isMovie);
	}
	
	public void addPerson(PersonCrew p, int mId, boolean isMovie) {
		if (!personList.containsKey(p.getId())) {
			personList.put(p.getId(), MediaSearchHandler.getPersonPeople(p.getId()));
		}
		if (p.getJob().equalsIgnoreCase("Director")) {
			addToList(directorList, p, mId, isMovie);
		} else if (p.getJob().equalsIgnoreCase("Screenplay") || p.getJob().equalsIgnoreCase("Writer") || 
				p.getJob().equalsIgnoreCase("Story") || p.getJob().equalsIgnoreCase("Author")) {
			addToList(writerList, p, mId, isMovie);
		}
	}

	void addGenre(Genre g, int mId, boolean isMovie) {
		addToList(genreList, g, mId, isMovie);
	}
	
	<T> void addToList(LinkedHashMap<T, LinkedHashMap<String, List<Integer>>> list, T g, int mId, boolean isMovie) {
		String mediaType = (isMovie)? movieIdentifier : tvIdentifier;
		if (!list.containsKey(g)) {
			LinkedHashMap<String, List<Integer>> map = new LinkedHashMap<>();
			map.put(mediaType, new ArrayList<>(Collections.singletonList(mId)));
			list.put(g, map);
		} else if (!list.get(g).containsKey(mediaType)) {
			list.get(g).put(mediaType, new ArrayList<>(Collections.singletonList(mId)));
		} else if (!list.get(g).get(mediaType).contains(mId)){
			list.get(g).get(mediaType).add(mId);
		}
	}

	private void removeTag(TreeMap<String, LinkedHashMap<String, List<Integer>>> tagList, int mId, boolean isMovie) {
		List<String> tagsToRemove = new ArrayList<>();
		for (String tag : tagList.keySet()) {
			if (isMovie) {
				if (tagList.get(tag).get(movieIdentifier).contains(mId)) {
					tagsToRemove.add(tag);
				}
			} else {
				if (tagList.get(tag).get(tvIdentifier).contains(mId)) {
					tagsToRemove.add(tag);
				}
			}
		}
		for (String tag : tagsToRemove) {
			if (isMovie) {
				tagList.get(tag).get(movieIdentifier).removeAll(Arrays.asList(mId));
				if (tagList.get(tag).get(movieIdentifier).isEmpty()) {
					tagList.get(tag).remove(movieIdentifier);
				}
			} else {
				tagList.get(tag).get(tvIdentifier).removeAll(Arrays.asList(mId));
				if (tagList.get(tag).get(tvIdentifier).isEmpty()) {
					tagList.get(tag).remove(tvIdentifier);
				}
			}
		}
	}


	private <T> void removeFromList(LinkedHashMap<T, LinkedHashMap<String, List<Integer>>> list, int mId, boolean isMovie) {
		List<T> itemsToRemove = new ArrayList<>();
		for (T item : list.keySet()) {
			if (isMovie) {
				if (list.get(item).get(movieIdentifier).contains(mId)) {
					itemsToRemove.add(item);
				}
			} else {
				if (list.get(item).get(tvIdentifier).contains(mId)) {
					itemsToRemove.add(item);
				}
			}
		}
		for (T item : itemsToRemove) {
			if (isMovie) {
				list.get(item).get(movieIdentifier).removeAll(Arrays.asList(mId));
				if (list.get(item).get(movieIdentifier).isEmpty()) {
					list.get(item).remove(movieIdentifier);
				}
			} else {
				list.get(item).get(tvIdentifier).removeAll(Arrays.asList(mId));
				if (list.get(item).get(tvIdentifier).isEmpty()) {
					list.get(item).remove(tvIdentifier);
				}
			}
		}
	}

	//remove movie from media list and from tilepane
	void removeMedia(MediaItem m) {
		removePath(m.getFullFilePath());
		if (m.isTvShow()) {
			List<String> paths = m.tvShow.getAllFullPaths();
			for (String path : paths) {
				removePath(path);
			}
		}
		allMedia.remove(m);
		for (JFXMediaRippler mRip: ControllerMaster.mainController.allTiles) {
			if (mRip.linkedItem == m) {
				ControllerMaster.mainController.allTiles.remove(mRip);
				ControllerMaster.mainController.refreshSearch();
				break;
			}
		}
		//remove search results, collections, and playlists
		int mId = m.getId();
		removeTag(mediaTags, mId, m.isMovie());
		removeFromList(actorList, mId, m.isMovie());
		removeFromList(directorList, mId, m.isMovie());
		removeFromList(writerList, mId, m.isMovie());
		removeFromList(genreList, mId, m.isMovie());
		for (MediaPlaylist playlist : userPlaylists) {
			playlist.removeMedia(m);
		}
		if (m.belongsToCollection()) {
			ownedCollections.get(m.getCollection()).remove(m);
			if (ownedCollections.get(m.getCollection()).isEmpty()) {
				ownedCollections.remove(m.getCollection());
			}
		}
	}

	boolean removeTvEpisode(MediaItem m, int seasonNum, int epNum) {
		removePath(m.getFullFilePath(seasonNum, epNum));
		getTvById(m.getId()).tvShow.removeEpisode(seasonNum, epNum);
		//remove show if no episodes are left
		if (m.tvShow.getFirstAvailableSeason() == null) {
			removeMedia(m);
			return false;
		}
		return true;
	}
	
	void sortShownItems() {
		ObservableList<Node> workingCollection = FXCollections.observableArrayList((ControllerMaster.mainController.tilePane.getChildren()));
		switch (ControllerMaster.mainController.sortCombo.getValue()) {
		case NAME_ASC:
			workingCollection.sort((o1, o2) -> ((JFXMediaRippler) o1).linkedItem.getTitle(false).compareTo(((JFXMediaRippler) o2).linkedItem.getTitle(false)));
			break;
		case NAME_DESC:
			workingCollection.sort((o2, o1) -> ((JFXMediaRippler) o1).linkedItem.getTitle(false).compareTo(((JFXMediaRippler) o2).linkedItem.getTitle(false)));
			break;
		case ADDED_DATE_ASC:
			workingCollection.sort((o1, o2) -> ((JFXMediaRippler) o1).linkedItem.dateAdded.compareTo(((JFXMediaRippler) o2).linkedItem.dateAdded));
			break;
		case ADDED_DATE_DESC:
			workingCollection.sort((o2, o1) -> ((JFXMediaRippler) o1).linkedItem.dateAdded.compareTo(((JFXMediaRippler) o2).linkedItem.dateAdded));
			break;
		case RELEASE_DATE_ASC:
			workingCollection.sort((o1, o2) -> ((JFXMediaRippler) o1).linkedItem.getReleaseDate(false).compareTo(((JFXMediaRippler) o2).linkedItem.getReleaseDate(false)));
			break;
		case RELEASE_DATE_DESC:
			workingCollection.sort((o1, o2) -> ((JFXMediaRippler) o2).linkedItem.getReleaseDate(false).compareTo(((JFXMediaRippler) o1).linkedItem.getReleaseDate(false)));
			break;
		default:
			break;
		}
		ControllerMaster.mainController.tilePane.getChildren().setAll(workingCollection);
	}

	void refreshViewingList(Map<String, List<Integer>> map) {
		List<Integer> moviesList = new ArrayList<>();
		List<Integer> tvList = new ArrayList<>();
		if (map.containsKey(UserData.movieIdentifier)) {
			moviesList = map.get(UserData.movieIdentifier);
		}
		if (map.containsKey(UserData.tvIdentifier)) {
			tvList = map.get(UserData.tvIdentifier);
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
				new ArrayList<>() : ControllerMaster.mainController.playlistCombo.getValue().getItems();
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
	boolean hasPath(String path) {
		return allPaths.contains(path);
	}
	
	void addPath(String path) {
		if (!hasPath(path)) {
			allPaths.add(path);
		}
	}

	void removePath(String path) {
		allPaths.remove(path);
	}

	List<MediaItem> getAllMedia() {
		return allMedia;
	}
	

}