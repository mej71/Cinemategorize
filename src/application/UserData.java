package application;

import application.SearchItem.SearchTypes;
import application.controls.JFXMediaRippler;
import application.flowcells.PlaylistCell;
import application.mediainfo.CustomMovieDb;
import application.mediainfo.CustomTvDb;
import application.mediainfo.MediaItem;
import application.mediainfo.MediaResultsPage;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.*;
import info.movito.themoviedbapi.model.Collection;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.people.*;
import info.movito.themoviedbapi.model.tv.Network;
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

public class UserData implements Serializable {

    public final transient static String movieIdentifier = "movie";
    public final transient static String tvIdentifier = "tv";
    private static final long serialVersionUID = 1L;
    private static final String favoritesName = "Favorites";
    public transient static TmdbApi apiLinker;  //your key goes in api_keys.xml
    private final String regExSpecialChars = "<([{\\^-=$!|]})?*+.>";
    @SuppressWarnings("ReplaceAllDot")
    private final String regExSpecialCharsRE = regExSpecialChars.replaceAll(".", "\\\\$0");
    private final Pattern reCharsREP = Pattern.compile("[" + regExSpecialCharsRE + "]");
    private List<MediaItem> allMedia = new ArrayList<>();
    private TreeMap<String, LinkedHashMap<String, List<Integer>>> mediaTags = new TreeMap<>(String.CASE_INSENSITIVE_ORDER); // cache list of tags for search updating
    private LinkedHashMap<PersonCrew, LinkedHashMap<String, List<Integer>>> directorList = new LinkedHashMap<>();
    private LinkedHashMap<PersonCrew, LinkedHashMap<String, List<Integer>>> writerList = new LinkedHashMap<>();
    private LinkedHashMap<PersonCast, LinkedHashMap<String, List<Integer>>> actorList = new LinkedHashMap<>();
    private LinkedHashMap<Genre, LinkedHashMap<String, List<Integer>>> genreList = new LinkedHashMap<>();
    private LinkedHashMap<ProductionCompany, LinkedHashMap<String, List<Integer>>> companyList = new LinkedHashMap<>();
    private LinkedHashMap<Network, LinkedHashMap<String, List<Integer>>> networkList = new LinkedHashMap<>();
    private LinkedHashMap<Integer, PersonPeople> personList = new LinkedHashMap<>();
    private LinkedHashMap<Integer, PersonCredits> creditsList = new LinkedHashMap<>();
    private List<String> allPaths = new ArrayList<>();
    private double scaleFactor = 1.0;
    private LinkedHashMap<Integer, MovieDb> seenMovies = new LinkedHashMap<>();
    private LinkedHashMap<Integer, TvSeries> seenTv = new LinkedHashMap<>();
    private LinkedHashMap<Integer, List<PersonCredit>> knownFor = new LinkedHashMap<>();
    private List<MediaPlaylist> userPlaylists = new ArrayList<>();
    private LinkedHashMap<Collection, List<MediaItem>> ownedCollections = new LinkedHashMap<>();
    private transient LinkedHashMap<MediaItem, MediaResultsPage> tempManualItems = new LinkedHashMap<>();
    private int minYear = 0;
    private int maxYear = 0;
    //settings
    private boolean useAutoLookup = true;
    private ThemeSelection themeSelection = ThemeSelection.themes.get(0);

    public UserData() {
        updateApiLinker();
        if (userPlaylists.isEmpty()) {
            //Create favorites list if empty
            MediaPlaylist favoriteList = new MediaPlaylist("Favorites");
            favoriteList.canDelete = false;
            userPlaylists.add(favoriteList);
        }
    }

    //get key from api_keys.xml by name
    private static String getAPIKey(String keyname) {
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

    public List<MediaPlaylist> getUserPlaylists() { return userPlaylists; }

    public LinkedHashMap<Collection, List<MediaItem>> getOwnedCollections() { return ownedCollections; }

    public List<MediaItem> getAllMedia() { return allMedia; }

    public int getMinYear() { return minYear; }

    public int getMaxYear() { return maxYear; }

    public void setMinYear(int year) { minYear = (year > 0 && year < minYear)? year : minYear;}

    public void setMaxYear(int year) { maxYear = (year > 0 && year > maxYear)? year : maxYear;}

    public ThemeSelection getThemeSelection() { return themeSelection; }

    public boolean isUseAutoLookup() { return useAutoLookup; }

    public void updateApiLinker() {
        try {
            apiLinker = new TmdbApi(getAPIKey("THE_MOVIE_DB_API_TOKEN"));
        } catch (Exception e) {
            apiLinker = null; //offline mode
        }
    }

    private MediaPlaylist getPlaylistByName(String name) {
        for (int i = 0; i < userPlaylists.size(); ++i) {
            if (userPlaylists.get(i).getName().equalsIgnoreCase(name)) {
                return userPlaylists.get(i);
            }
        }
        return null;
    }

    public boolean favoritesContains(MediaItem mi) {
        MediaPlaylist favorites = getPlaylistByName(favoritesName);
        return favorites != null && favorites.getItems().contains(mi);
    }

    public MediaPlaylist getFavoritesList() {
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
        if (!personList.containsKey(personId)) {
            personList.put(personId, MediaSearchHandler.getPersonPeople(personId));
        }
        return personList.get(personId);
    }

    public List<PersonCredit> getKnowForCredits(int personId) {
        return knownFor.get(personId);
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double sf) {
        if (sf < 0.25 || sf > 2) {
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

    //Movies have production companies, tv shows have networks
    public List<Integer> getMoviesWithProductionCompany(ProductionCompany company) {
        if (companyList.containsKey(company) && companyList.get(company).get(movieIdentifier) != null && !companyList.get(company).get(movieIdentifier).isEmpty()) {
            return companyList.get(company).get(movieIdentifier);
        }
        return new ArrayList<>();
    }

    public List<Integer> getTvWithNetwork(Network network) {
        if (networkList.containsKey(network) && networkList.get(network).get(tvIdentifier) != null && !networkList.get(network).get(tvIdentifier).isEmpty()) {
            return networkList.get(network).get(tvIdentifier);
        }
        return new ArrayList<>();
    }

    public boolean ownsMovie(int iD) {
        for (int i = 0; i < allMedia.size(); ++i) {
            if (allMedia.get(i).isMovie() && allMedia.get(i).getId() == iD) {
                return true;
            }
        }
        return false;
    }

    public MediaItem getMovieById(int iD) {
        for (int i = 0; i < allMedia.size(); ++i) {
            if (allMedia.get(i).isMovie() && allMedia.get(i).getId() == iD) {
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
            if (allMedia.get(i).isTvShow() && allMedia.get(i).getId() == iD) {
                return true;
            }
        }
        return false;
    }

    public boolean ownsEpisode(int iD, int seasonNum, int epNum) {
        for (int i = 0; i < allMedia.size(); ++i) {
            if (allMedia.get(i).isTvShow() && allMedia.get(i).getId() == iD) {
                List<Integer> episodes = allMedia.get(i).tvShow.getOwnedEpisodeNumbers(seasonNum);
                return episodes.contains(epNum);
            }
        }
        return false;
    }

    public MediaItem getTvById(int iD) {
        for (int i = 0; i < allMedia.size(); ++i) {
            if (allMedia.get(i).isTvShow() && allMedia.get(i).getId() == iD) {
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

    //escape all special characters in strings for use with regex
    private String quoteRegExSpecialChars(String s) {
        Matcher m = reCharsREP.matcher(s);
        return m.replaceAll("\\\\$0");
    }

    public List<SearchItem> getAutoCompleteItems(String userInput) {
        List<SearchItem> suggestions = new ArrayList<>();
        userInput = userInput.trim(); //remove whitespace
        boolean detailedLength = userInput.length() > 1;
        if (userInput.equals("")) {
            return suggestions;
        }
        userInput = quoteRegExSpecialChars(userInput); // escape special characters so users can search literally
        Pattern pattern = Pattern.compile("(?i)(^|\\s|\\()" + userInput);
        Matcher matcher = pattern.matcher("");
        int titleMatchLimiter = 5;
        for (int i = 0; i < allMedia.size(); ++i) {
            matcher.reset(allMedia.get(i).getItemName());
            if (matcher.find()) {
                suggestions.add(new SearchItem(SearchTypes.TITLE, allMedia.get(i)));
                if (suggestions.size() == titleMatchLimiter) {
                    break;
                }
            }
        }
        Collections.addAll(suggestions, addGenreSuggestion(genreList.keySet(), matcher));
        if (detailedLength || suggestions.size() == 0) {
            Collections.addAll(suggestions, addTagSuggestion(mediaTags.keySet(), matcher));
            Collections.addAll(suggestions, addPeopleSuggestion(directorList.keySet(), matcher, SearchTypes.DIRECTOR));
            Collections.addAll(suggestions, addPeopleSuggestion(actorList.keySet(), matcher, SearchTypes.ACTOR));
            Collections.addAll(suggestions, addPeopleSuggestion(writerList.keySet(), matcher, SearchTypes.WRITER));
            Collections.addAll(suggestions, addProductionCompanySuggestion(companyList.keySet(), matcher));
            Collections.addAll(suggestions, addNetworkSuggestion(networkList.keySet(), matcher));
        }
        return suggestions;
    }

    private SearchItem[] addGenreSuggestion(Set<Genre> set, Matcher matcher) {
        List<SearchItem> suggestions = new ArrayList<>();
        int matchLimit = 2;
        for (Genre t : set) {
            matcher.reset(t.getName());
            if (matcher.find()) {
                suggestions.add(new SearchItem(SearchTypes.GENRE, t));
                if (suggestions.size() == matchLimit) {
                    break;
                }
            }
        }
        return suggestions.toArray(new SearchItem[suggestions.size()]);
    }

    private SearchItem[] addTagSuggestion(Set<String> set, Matcher matcher) {
        int matchLimit = 3;
        List<SearchItem> suggestions = new ArrayList<>();
        for (String t : set) {
            matcher.reset(t);
            if (matcher.find()) {
                suggestions.add(new SearchItem(SearchTypes.TAG, t));
                if (suggestions.size() == matchLimit) {
                    break;
                }
            }
        }
        return suggestions.toArray(new SearchItem[suggestions.size()]);
    }

    private <T extends Person> SearchItem[] addPeopleSuggestion(Set<T> set, Matcher matcher, SearchTypes type) {
        int matchLimit = 3;
        List<SearchItem> suggestions = new ArrayList<>();
        for (T t : set) {
            matcher.reset(t.getName());
            if (matcher.find()) {
                suggestions.add(new SearchItem(type, t));
                if (suggestions.size() == matchLimit) {
                    break;
                }
            }
        }
        return suggestions.toArray(new SearchItem[suggestions.size()]);
    }

    private SearchItem[] addProductionCompanySuggestion(Set<ProductionCompany> set, Matcher matcher) {
        int matchLimit = 3;
        List<SearchItem> suggestions = new ArrayList<>();
        for (ProductionCompany t : set) {
            matcher.reset(t.getName());
            if (matcher.find()) {
                suggestions.add(new SearchItem(SearchTypes.COMPANY, t));
                if (suggestions.size() == matchLimit) {
                    break;
                }
            }
        }
        return suggestions.toArray(new SearchItem[suggestions.size()]);
    }

    private SearchItem[] addNetworkSuggestion(Set<Network> set, Matcher matcher) {
        int matchLimit = 3;
        List<SearchItem> suggestions = new ArrayList<>();
        for (Network t : set) {
            matcher.reset(t.getName());
            if (matcher.find()) {
                suggestions.add(new SearchItem(SearchTypes.NETWORK, t));
                if (suggestions.size() == matchLimit) {
                    break;
                }
            }
        }
        return suggestions.toArray(new SearchItem[suggestions.size()]);
    }

    public void addTag(Keyword tag, int mId, boolean isMovie) {
        String mediaType = (isMovie) ? movieIdentifier : tvIdentifier;
        if (!mediaTags.containsKey(tag.getName())) {
            LinkedHashMap<String, List<Integer>> map = new LinkedHashMap<>();
            map.put(mediaType, new ArrayList<>(Collections.singletonList(mId)));
            mediaTags.put(tag.getName(), map);
        } else if (!mediaTags.get(tag.getName()).containsKey(mediaType)) {
            mediaTags.get(tag.getName()).put(mediaType, new ArrayList<>(Collections.singletonList(mId)));
        } else if (!mediaTags.get(tag.getName()).get(mediaType).contains(mId)) {
            mediaTags.get(tag.getName()).get(mediaType).add(mId);
        }
    }

    public void addPerson(PersonCast p, int mId, boolean isMovie) {
        addToList(actorList, p, mId, isMovie);
    }

    public void addPerson(PersonCrew p, int mId, boolean isMovie) {
        if (p.getJob().equalsIgnoreCase("Director")) {
            addToList(directorList, p, mId, isMovie);
        } else if (p.getJob().equalsIgnoreCase("Screenplay") || p.getJob().equalsIgnoreCase("Writer") ||
                p.getJob().equalsIgnoreCase("Story") || p.getJob().equalsIgnoreCase("Author")) {
            addToList(writerList, p, mId, isMovie);
        }
    }

    public void addGenre(Genre g, int mId, boolean isMovie) {
        addToList(genreList, g, mId, isMovie);
    }

    public void addProductionCompany(ProductionCompany company, int mId, boolean isMovie) {
        addToList(companyList, company, mId, isMovie);
    }

    void addNetwork(Network network, int mId, boolean isMovie) {
        addToList(networkList, network, mId, isMovie);
    }

    private <T> void addToList(LinkedHashMap<T, LinkedHashMap<String, List<Integer>>> list, T g, int mId, boolean isMovie) {
        String mediaType = (isMovie) ? movieIdentifier : tvIdentifier;
        if (!list.containsKey(g)) {
            LinkedHashMap<String, List<Integer>> map = new LinkedHashMap<>();
            map.put(mediaType, new ArrayList<>(Collections.singletonList(mId)));
            list.put(g, map);
        } else if (!list.get(g).containsKey(mediaType)) {
            list.get(g).put(mediaType, new ArrayList<>(Collections.singletonList(mId)));
        } else if (!list.get(g).get(mediaType).contains(mId)) {
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
                tagList.get(tag).get(movieIdentifier).removeAll(Collections.singletonList(mId));
                if (tagList.get(tag).get(movieIdentifier).isEmpty()) {
                    tagList.get(tag).remove(movieIdentifier);
                }
            } else {
                tagList.get(tag).get(tvIdentifier).removeAll(Collections.singletonList(mId));
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
                list.get(item).get(movieIdentifier).removeAll(Collections.singletonList(mId));
                if (list.get(item).get(movieIdentifier).isEmpty()) {
                    list.get(item).remove(movieIdentifier);
                }
            } else {
                list.get(item).get(tvIdentifier).removeAll(Collections.singletonList(mId));
                if (list.get(item).get(tvIdentifier).isEmpty()) {
                    list.get(item).remove(tvIdentifier);
                }
            }
        }
    }

    //remove movie from media list and from tilepane
    public void removeMedia(MediaItem m) {
        removePath(m.getFullFilePath());
        if (m.isTvShow()) {
            List<String> paths = m.tvShow.getAllFullPaths();
            for (String path : paths) {
                removePath(path);
            }
        }
        allMedia.remove(m);
        for (JFXMediaRippler mRip : ControllerMaster.mainController.allTiles) {
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

    public boolean removeTvEpisode(MediaItem m, int seasonNum, int epNum) {
        removePath(m.getFullFilePath(seasonNum, epNum));
        getTvById(m.getId()).tvShow.removeEpisode(seasonNum, epNum);
        //remove show if no episodes are left
        if (m.tvShow.getFirstAvailableSeason() == null) {
            removeMedia(m);
            return false;
        }
        return true;
    }

    public void sortShownItems() {
        ObservableList<Node> workingCollection = FXCollections.observableArrayList((ControllerMaster.mainController.tilePane.getChildren()));
        switch (ControllerMaster.mainController.getSortCombo().getValue()) {
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

    public void refreshViewingList(Map<String, List<Integer>> map) {
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
            startDate = (ControllerMaster.mainController.getStartYearComboBox().getValue() == null) ? null : formatter.parse(ControllerMaster.mainController.getStartYearComboBox().getValue() + "-01-01");
            endDate = (ControllerMaster.mainController.getEndYearComboBox().getValue() == null) ? null : formatter.parse(ControllerMaster.mainController.getEndYearComboBox().getValue() + "-12-31");
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        boolean noSearchIds = moviesList.isEmpty() && tvList.isEmpty();
        boolean canShowMovies = ControllerMaster.mainController.getMediaTypeCombo().getValue() != MediaListDisplayType.TVSHOWS;
        boolean canShowTv = ControllerMaster.mainController.getMediaTypeCombo().getValue() != MediaListDisplayType.MOVIES;
        List<MediaItem> selectedPlaylist = (ControllerMaster.mainController.getPlaylistCombo().getValue() == null) ?
                new ArrayList<>() : ControllerMaster.mainController.getPlaylistCombo().getValue().getItems();
        List<MediaItem> selectedCollection = (ControllerMaster.mainController.getCollectionsCombo().getValue() == null) ?
                new ArrayList<>() : ownedCollections.get(ControllerMaster.mainController.getCollectionsCombo().getValue());
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
        if (!hasPath(path)) {
            allPaths.add(path);
        }
    }

    private void removePath(String path) {
        allPaths.remove(path);
    }

    public void processCollectionInfo(MediaItem mi, boolean isMovie) {
        if (isMovie && mi.belongsToCollection()) {
            if (!ownedCollections.containsKey(mi.getCollection())) {
                ownedCollections.put(mi.getCollection(), new ArrayList<>());
            }
            ownedCollections.get(mi.getCollection()).add(mi);
            ControllerMaster.mainController.updateCollectionCombo();
        }
    }

    public void removeTempManualItem(MediaItem mi) {
        for (MediaItem mik : tempManualItems.keySet()) {
            if (mi.equals(mik)) {
                tempManualItems.remove(mik);
                break;
            }
        }
    }

    public void removeTempManualItem(String filePath) {
        for (MediaItem mik : tempManualItems.keySet()) {
            if (mik.getTempFilePath().equals(filePath)) {
                tempManualItems.remove(mik);
                break;
            }
        }
    }

    public void updateKnownFor(int id, PersonCredit personCredit) {
        if (personCredit == null) {
            knownFor.put(id, new ArrayList<>());
            return;
        }
        if (knownFor.containsKey(id)) {
            knownFor.get(id).add(personCredit);
        } else {
            knownFor.put(id, new ArrayList<>(Collections.singletonList(personCredit)));
        }
    }

    public boolean hasKnownFor(int id) {
        return knownFor.containsKey(id);
    }

    public void addUserPlaylist(MediaPlaylist mediaPlaylist) {
        userPlaylists.add(mediaPlaylist);
    }

    public List<PlaylistCell<MediaPlaylist>> createPlaylistCells() {
        return PlaylistCell.createCells(userPlaylists);
    }

    public List<MediaItem> getCollectionMedia(Collection item) {
        return ownedCollections.get(item);
    }

    public void addMedia(MediaItem mi) {
        allMedia.add(mi);
    }

    public boolean tempManualItemMatches(String path) {
        for (MediaItem m : tempManualItems.keySet()) {
            if (m.getFullFilePath().equals(path)) {
                return true;
            }
        }
        return false;
    }

    public void addTempManualItem(MediaItem mediaItem, MediaResultsPage mediaResultsPage) {
        tempManualItems.put(mediaItem, mediaResultsPage);
    }

    public int getTempManualSize() {
        return tempManualItems.size();
    }

    public void showTempManualItems() {
        showTempManualItems(0, 0, 0);
    }

    public void showTempManualItems(int id, int selectedSeason, int selectedEpisode) {
        ControllerMaster.showManualLookupDialog(tempManualItems, id, selectedSeason, selectedEpisode);
    }

    public boolean hasPlaylist(MediaPlaylist curList) {
        return userPlaylists.contains(curList);
    }

    public void addSeenMovie(int id, MovieDb m) {
        seenMovies.put(id, m);
    }

    public void addSeenTv(int id, TvSeries tv) {
        seenTv.put(id, tv);
    }

    public void setTheme(ThemeSelection theme) {
        if (themeSelection != null) {
            //remove last stylesheet so small modifications aren't overridden by it
            ControllerMaster.mainController.cinemaScene.getStylesheets().remove(themeSelection.getClass().getClassLoader().getResource(themeSelection.getFileName()).toExternalForm());
        }
        if (theme == null) {
            theme = ThemeSelection.themes.get(0);
        }
        themeSelection = theme;
        ControllerMaster.mainController.cinemaScene.getStylesheets().add(ControllerMaster.userData.getClass().getClassLoader().getResource(themeSelection.getFileName()).toExternalForm());
    }

    public void setUseAutoLookup(Boolean newVal) {
        if (newVal != null) {
            useAutoLookup = newVal;
        }
    }
}