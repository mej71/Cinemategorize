package application;

import java.net.URL;
import java.util.*;

import org.apache.commons.lang3.SerializationUtils;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.controls.JFXTabPane;

import info.movito.themoviedbapi.model.people.Person;
import info.movito.themoviedbapi.model.people.PersonCredit;
import info.movito.themoviedbapi.model.people.PersonPeople;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

public class PersonViewController extends LoadingControllerBase implements Initializable {

	private final int maxKnownMovies = 8;
	//resize tiles to fill the space
	private final float scaleWFactor = 0.83f;
	private final float scaleHFactor = 0.7f;
	
    
	
    @FXML private ImageView personImageView;
    @FXML private Label nameLabel;
    @FXML private Label famousLabel;
    @FXML private ScrollPane bioScrollPane;
    @FXML private Label bioLabel;
    @FXML private TilePane famousTilePane;
    @FXML private JFXTabPane tabPane;
    @FXML private Tab directorTab;
    @FXML private Tab writerTab;
    @FXML private Tab actorTab;
    @FXML private Tab producerTab;
    @FXML private ScrollPane dirScrollPane;
    @FXML private ListFlowPane<CreditCell<PersonCredit>, PersonCredit> dirFlowPane;
    @FXML private ScrollPane writScrollPane;
    @FXML private ListFlowPane<CreditCell<PersonCredit>, PersonCredit> writFlowPane;
    @FXML private ScrollPane actScrollPane;
    @FXML private ListFlowPane<CreditCell<PersonCredit>, PersonCredit> actFlowPane;
    @FXML private ScrollPane prodScrollPane;
    @FXML private ListFlowPane<CreditCell<PersonCredit>, PersonCredit> prodFlowPane;
    private PersonPeople person;
    private List<PersonCredit> crewCredits;
    private List<PersonCredit> castCredits;
	private Comparator<PersonCredit> dateComparator;
    private Comparator<PersonCredit> knownComprator;
    private List<JFXMediaRippler> knownForRipplers;
    private boolean observing = false;
    
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		super.initialize(url, rb);
		JFXScrollPane.smoothScrolling(bioScrollPane);
		dateComparator = (o1, o2) -> {
			String o1Date = (o1.getReleaseDate() != null)? o1.getReleaseDate() : (o1.getFirstAirDate() != null)? o1.getFirstAirDate() : "";
			String o2Date = (o2.getReleaseDate() != null)? o2.getReleaseDate() : (o2.getFirstAirDate() != null)? o2.getFirstAirDate() : "";
			if (o1Date.isEmpty() && o2Date.isEmpty()) {
				return 0;
			} else if (o1Date.isEmpty()) {
				return -1;
			} else if (o2Date.isEmpty()) {
				return 1;
			}
			return o2Date.compareTo(o1Date);
		};
		knownComprator = (o1, o2) -> o2.getVoteAvg().compareTo(o1.getVoteAvg());
		famousTilePane.setVgap(15*scaleHFactor);
		famousTilePane.setHgap(10*scaleWFactor);
		famousTilePane.setMaxHeight(208 * scaleHFactor * 2 + 15 * scaleHFactor * 2);
		
		knownForRipplers = new ArrayList<>();
		JFXMediaRippler mRip;
		for (int i = 0; i < maxKnownMovies; ++i) {
			mRip = JFXMediaRippler.createBasicRippler(famousTilePane, null);
			mRip.getPane().prefHeightProperty().bind(mainGrid.heightProperty().multiply(0.05));
			knownForRipplers.add(mRip);
		}
		personImageView.fitWidthProperty().bind(mainGrid.widthProperty().multiply(0.30));
		personImageView.fitHeightProperty().bind(mainGrid.heightProperty().multiply(0.50));
		dirFlowPane.bindWidthToNode(dirScrollPane);
		writFlowPane.bindWidthToNode(writScrollPane);
		actFlowPane.bindWidthToNode(actScrollPane);
		prodFlowPane.bindWidthToNode(prodScrollPane);
	}
	
	<T extends Person> void showPerson(JFXDialog d, T pc, MediaItem mi) {
		person = ControllerMaster.userData.getPerson(pc.getId());
		showPerson(d);
	}
	
	void showPerson(JFXDialog d) {
		nameLabel.setText(person.getName());
		if (person.getBiography()!=null && !person.getBiography().equals("")) {
			bioLabel.setText(person.getBiography());
		} else {
			bioLabel.setText("No bio available");
		}
		resetCreditTabs();
		super.setDialogLink(d);	
		dLink.show();	
	}
	
	@Override
	protected void runTasks() {
		super.runTasks();
		observeNodes();
		personImageView.setImage(MediaSearchHandler.getProfilePicture(person).getImage());	
		creditSort();
		setCredits();
		enableTabs();
		loadInfo();
		resizeTiles();
	}

	@Override
	protected void closeTasks() {
		super.closeTasks();
		unobserveNodes();
	}

	private void observeNodes() {
		if (observing) {
			unobserveNodes();
		}
		ControllerMaster.tileAnimator.observe(famousTilePane.getChildren());
		observing = true;
	}

	private void unobserveNodes() {
		ControllerMaster.tileAnimator.unobserve(famousTilePane.getChildren());
		observing = false;
	}

	private void resetCreditTabs() {
		dirFlowPane.getItems().clear();
		writFlowPane.getItems().clear();
		actFlowPane.getItems().clear();
		prodFlowPane.getItems().clear();
		dirFlowPane.getChildren().clear();
		writFlowPane.getChildren().clear();
		actFlowPane.getChildren().clear();
		prodFlowPane.getChildren().clear();
		dirScrollPane.setVvalue(0);
		writScrollPane.setVvalue(0);
		actScrollPane.setVvalue(0);
		prodScrollPane.setVvalue(0);
		bioScrollPane.setVvalue(0);
	}

	private void creditSort() {
		crewCredits = ControllerMaster.userData.getCredits(person.getId()).getCrew();
		castCredits = ControllerMaster.userData.getCredits(person.getId()).getCast();
		
		crewCredits.sort(dateComparator);
		castCredits.sort(dateComparator);
	}


	private void addToList(ListFlowPane<CreditCell<PersonCredit>, PersonCredit> listFlowPane, PersonCredit credit) {
		listFlowPane.addItem(credit);
		listFlowPane.getChildren().add(new CreditCell<>(credit, listFlowPane));
		listFlowPane.setPrefHeight(listFlowPane.getChildren().size() * CreditCell.prefCellHeight);
	}

	private void enableTabs() {
		directorTab.setDisable(dirFlowPane.getItems().size() == 0);
		writerTab.setDisable(writFlowPane.getItems().size() == 0);
		actorTab.setDisable(actFlowPane.getItems().size() == 0);
		producerTab.setDisable(prodFlowPane.getItems().size() == 0);
	}

	private void setCredits() {
		for (PersonCredit crewCredit : crewCredits) {
			if (crewCredit.getDepartment().equalsIgnoreCase("Directing")) {
				addToList(dirFlowPane, crewCredit);
			} else if (crewCredit.getDepartment().equalsIgnoreCase("Writing")) {
				addToList(writFlowPane, crewCredit);
			} else if (crewCredit.getDepartment().equalsIgnoreCase("Production")) {
				addToList(prodFlowPane, crewCredit);
			}
		}


		for (PersonCredit castCredit : castCredits) {
			addToList(actFlowPane, castCredit);
		}
		
		switch (person.getKnownForDepartment()) {
			case "Writing":
				tabPane.getSelectionModel().select(writerTab);
				break;
			case "Directing":
				tabPane.getSelectionModel().select(directorTab);
				break;
			case "Production":
				tabPane.getSelectionModel().select(producerTab);
				break;
			case "Acting":  //use acting as default in future prep
			default	:
				tabPane.getSelectionModel().select(actorTab);
				break;
		}
	}
	
	private void loadInfo() {
		
		ObservableList<Node> workingKnownForCollection = FXCollections.observableArrayList();
		if (!ControllerMaster.userData.knownFor.containsKey(person.getId())) {
			List<Integer> tempKnownList = new ArrayList<>();
			List<PersonCredit> knownForList = new ArrayList<>(crewCredits);
			knownForList.addAll(castCredits);
			knownForList.sort(knownComprator);
			int known = 0;
			MediaItem mi;
			int mId;
			boolean isKnownDep;
			final String knownDep = person.getKnownForDepartment();
			HashMap<PersonCredit, MediaItem> lesserKnown = new HashMap<>();
			for (PersonCredit personCredit : knownForList) {
				isKnownDep = (personCredit.getDepartment() == null && knownDep.equalsIgnoreCase("Acting")) || (personCredit.getDepartment() != null && personCredit.getDepartment().equals(knownDep));
				int voteCountThreshold = 100;
				int popularityThreshold = 3;//number of episodes someone must be involved in to count as "Known For"
				int episodeCountThreshold = 5;
				if (personCredit.getPopularity() > popularityThreshold && personCredit.getVoteCount() > voteCountThreshold &&
						(personCredit.getEpisodeCount() == 0 || personCredit.getEpisodeCount() > episodeCountThreshold) &&
						personCredit.getReleaseDate() != null && isKnownDep) {
					if (personCredit.getMediaType().equalsIgnoreCase("movie")) {
						mi = SerializationUtils.clone(MediaSearchHandler.getMovieInfoById(personCredit.getMediaId()));
						//if part of collection, just use first
						if (Objects.requireNonNull(mi).belongsToCollection()) {
							mi = SerializationUtils.clone(MediaSearchHandler.getFirstFromCollectionMatchesPerson(mi.getCollection().getId(), person.getId(), (personCredit.getDepartment() == null ||
									personCredit.getDepartment().equalsIgnoreCase("Acting"))));
							//if we change movie, then change credit
							for (PersonCredit tempCredit : knownForList) {
								if (tempCredit.getMediaType().equalsIgnoreCase("movie") && tempCredit.getMediaId() == mi.getId()) {
									personCredit = tempCredit;
									break;
								}
							}
						}
					} else {
						mi = SerializationUtils.clone(MediaSearchHandler.getTvInfoById(personCredit.getMediaId()));
					}
					mId = Objects.requireNonNull(mi).getId();
				} else {
					continue;
				}

				//item credit should match what the person is known for, been released already, and bias against being in a low count of episodes for a series
				if (!tempKnownList.contains(mId)) {
					//prefer people higher up in the cast bill and in full features or tv shows (bias against short films)
					//order in the cast list in which to be credited
					int castPositionThreshold = 5;
					if ((!knownDep.equalsIgnoreCase("Acting") || mi.getCreditPosition(person.getId()) < castPositionThreshold) && mi.isFullLength()) {
						knownForRipplers.get(known).setItem(mi);
						workingKnownForCollection.add(knownForRipplers.get(known));
						if (ControllerMaster.userData.knownFor.containsKey(person.getId())) {
							ControllerMaster.userData.knownFor.get(person.getId()).add(personCredit);
						} else {
							ControllerMaster.userData.knownFor.put(person.getId(), new ArrayList<>(Arrays.asList(personCredit)));
						}
						tempKnownList.add(mId);
						++known;
					} else {
						lesserKnown.put(personCredit, mi);
					}
				}
				if (known == maxKnownMovies) {
					break;

				}
			}
			//if the known for tiles haven't been filled, fill them up with lesser known roles.  If short of even the minimum, don't fill up two rows
			if (known < maxKnownMovies) {
				for (PersonCredit credit : lesserKnown.keySet()) {
					if (!tempKnownList.contains(credit.getId() ) ) {
						knownForRipplers.get(known).setItem(lesserKnown.get(credit));
						workingKnownForCollection.add(knownForRipplers.get(known));
						if (ControllerMaster.userData.knownFor.containsKey(person.getId())) {
							ControllerMaster.userData.knownFor.get(person.getId()).add(credit);
						} else {
							ControllerMaster.userData.knownFor.put(person.getId(), new ArrayList<>(Collections.singletonList(credit)));
						}
						tempKnownList.add( lesserKnown.get(credit).getId() );
						++known;
						//try and get at least this many movies for person, including lesser known if they have to
						int minKnownMovies = 4;
						if (known == maxKnownMovies || known == minKnownMovies) {
							break;
						}
					}
				}
			}
		} else {
			List<PersonCredit> knownCredits = ControllerMaster.userData.getKnowForCredits(person.getId());
			for (int i = 0; i < knownCredits.size(); ++i) {

				if (knownCredits.get(i).getMediaType().equalsIgnoreCase("Movie")) {
					System.out.println(knownCredits.get(i).getMediaId());
					knownForRipplers.get(i).setItem(MediaSearchHandler.getMovieInfoById( knownCredits.get(i).getMediaId() ));
				} else {
					knownForRipplers.get(i).setItem(MediaSearchHandler.getTvInfoById( knownCredits.get(i).getMediaId() ));
				}
				workingKnownForCollection.add(knownForRipplers.get(i));
				if (i == maxKnownMovies) {
					break;
				}
			}
		}
		//add blank array for empty person
		if (workingKnownForCollection.isEmpty()) {
			ControllerMaster.userData.knownFor.put(person.getId(), new ArrayList<>());
		}
		famousTilePane.getChildren().setAll(workingKnownForCollection);
		famousLabel.setVisible(famousTilePane.getChildren().size()>0);
	}

	private void resizeTiles() {
		//keep tilepane from getting taller than two rows
		StackPane n;
		for (int i = 0; i < famousTilePane.getChildren().size(); ++i) {
			n = ((JFXMediaRippler)famousTilePane.getChildren().get(i)).getPane();
			n.setMaxWidth(139*scaleWFactor);
			n.setMaxHeight(208*scaleHFactor);
			n.resize(139*scaleWFactor, 208*scaleHFactor);
		}
	}
}
