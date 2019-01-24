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
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.Collection;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;
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

	public final transient static TmdbApi apiLinker = new TmdbApi(getAPIKey("THE_MOVIE_DB_API_TOKEN"));  //your key goes in api_keys.xml
	
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
	private List<String> allPaths = new ArrayList<String>();
	private double scaleFactor = 1.0;
	public LinkedHashMap<Integer, MovieDb> seenMovies = new LinkedHashMap<Integer, MovieDb>();
	public LinkedHashMap<Integer, TvSeries> seenTv = new LinkedHashMap<Integer, TvSeries>();
	public LinkedHashMap<Integer, List<PersonCredit>> knownFor = new LinkedHashMap<Integer, List<PersonCredit>>();
	public MediaPlaylist userPlaylists = new MediaPlaylist();
	public LinkedHashMap<Collection, List<MediaItem>> ownedCollections = new LinkedHashMap<Collection, List<MediaItem>>();
	public transient LinkedHashMap<MediaItem, MediaResultsPage> tempManualItems = new LinkedHashMap<MediaItem, MediaResultsPage>();
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
	
	public boolean ownsEpisode(int iD, TvEpisode episode) {
		for (int i = 0; i < getAllMedia().size(); ++i) {
			if (getAllMedia().get(i).isTvShow() && getAllMedia().get(i).getId()==iD) {
				List<TvEpisode> episodes = getAllMedia().get(i).getEpisodes();
				for (TvEpisode ep : episodes) {
					if (ep.getId() == episode.getId()) {
						return true;
					}
				}
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
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
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
	}	
	
	final String regExSpecialChars = "<([{\\^-=$!|]})?*+.>";
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
				suggestions.add(new SearchItem(getAllMedia().get(i) ));
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
			tagList.put(StringTools.capitalize(tag.getName()), new ArrayList<>(Arrays.asList(mId)));
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
			list.put(g, new ArrayList<>(Arrays.asList(mId)));
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
		
		Date parsedDate = null;
		JFXMediaRippler mRip;
		String type;
		ControllerMaster.mainController.showingMedia.clear();
		for (int i = 0; i < ControllerMaster.mainController.allTiles.size(); ++i) {
			mRip = ControllerMaster.mainController.allTiles.get(i);
			if (  (map == null) || (mRip.linkedItem.isMovie() && moviesList.contains(mRip.linkedItem.getId())) ||
					(!mRip.linkedItem.isMovie() && tvList.contains(mRip.linkedItem.getId())) ) {
				try {
					parsedDate = formatter.parse(mRip.linkedItem.getReleaseDate());
				} catch (ParseException e) {
					e.printStackTrace();
					startDate = null;
				}	
				if (ControllerMaster.mainController.playlistCombo.getValue() == null || userPlaylists.getPlaylist(ControllerMaster.mainController.playlistCombo.getValue()).contains(mRip.linkedItem)) {
					if (ControllerMaster.mainController.collectionsCombo.getValue() == null || ownedCollections.get(ControllerMaster.mainController.collectionsCombo.getValue()).contains(mRip.linkedItem)) {
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
					}
				}
				
			} else if ((moviesList == null && tvList == null) || tvList.contains(mRip.linkedItem.getId())){
				if (ControllerMaster.mainController.playlistCombo.getValue() == null || userPlaylists.getPlaylist(ControllerMaster.mainController.playlistCombo.getValue()).contains(mRip.linkedItem)) {
					if (ControllerMaster.mainController.collectionsCombo.getValue() == null || ownedCollections.get(ControllerMaster.mainController.collectionsCombo.getValue()).contains(mRip.linkedItem)) {
						try {
							parsedDate = formatter.parse(mRip.linkedItem.getReleaseDate());
						} catch (ParseException e) {
							e.printStackTrace();
							startDate = null;
						}						
						if (startDate==null || startDate.before(parsedDate)) {
							if (endDate==null || endDate.after(parsedDate)) {
								ControllerMaster.mainController.showingMedia.put("tv", mRip.linkedItem);
								workingCollection.add(mRip);
							}
						}	
					}
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