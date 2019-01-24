package application;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

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
	
	private final int minKnownMovies = 4;  //try and get at least this many movies for person, including lesser known if they have to
	private final int maxKnownMovies = 8;
	private final int episodeCountThreshold = 5; //number of episodes someone must be involved in to count as "Known For"
	private final int castPositionThreshold = 5; //order in the cast list in which to be credited
	private final int popularityThreshold = 3;
	private final int voteCountThreshold = 100;
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
    private List<PersonCredit> knownForList;
    private Comparator<PersonCredit> dateComparator;
    private Comparator<PersonCredit> knownComprator;
    private List<JFXMediaRippler> knownForRipplers;
    
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		super.initialize(url, rb);
		JFXScrollPane.smoothScrolling(bioScrollPane);
		dateComparator = new Comparator<PersonCredit>(){
		     public int compare(PersonCredit o1, PersonCredit o2){ 
		    	 String o1Date = (o1.getReleaseDate() != null)? o1.getReleaseDate() : (o1.getFirstAirDate() != null)? o1.getFirstAirDate() : "";
		    	 String o2Date = (o2.getReleaseDate() != null)? o2.getReleaseDate() : (o2.getFirstAirDate() != null)? o2.getFirstAirDate() : "";
		    	 if (o1Date.isEmpty() && o2Date.isEmpty()) {
		    		 return 0;
		    	 } else if (o1Date.isEmpty() && !o2Date.isEmpty()) {
		    		 return -1;
		    	 } else if (!o1Date.isEmpty() && o2Date.isEmpty()) {
		    		 return 1;
		    	 }
		    	 return o2Date.compareTo(o1Date);
		     }
		};	
		knownComprator = new Comparator<PersonCredit>(){
		     public int compare(PersonCredit o1, PersonCredit o2){ 
		    	 return o2.getVoteAvg().compareTo(o1.getVoteAvg());
		     }
		};	
		famousTilePane.setVgap(15*scaleHFactor);
		famousTilePane.setHgap(10*scaleWFactor);
		famousTilePane.setMaxHeight(208 * scaleHFactor * 2 + 15 * scaleHFactor * 2);
		
		knownForRipplers = new ArrayList<JFXMediaRippler>();
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
		TileAnimator tileAnimator = new TileAnimator();
		tileAnimator.observe(famousTilePane.getChildren());
		
	}
	
	public <T extends Person> void showPerson(JFXDialog d, T pc, MediaItem mi) {
		person = ControllerMaster.userData.getPerson(pc.getId());
		showPerson(d);
	}
	
	public void showPerson(JFXDialog d) {
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
		personImageView.setImage(MediaSearchHandler.getProfilePicture(person).getImage());	
		creditSort();
		setCredits();
		enableTabs();
		loadInfo();
		resizeTiles();
	}
	
	public void resetCreditTabs() {
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
	
	public void creditSort() {
		crewCredits = ControllerMaster.userData.getCredits(person.getId()).getCrew();
		castCredits = ControllerMaster.userData.getCredits(person.getId()).getCast();
		
		Collections.sort(crewCredits, dateComparator);
		Collections.sort(castCredits, dateComparator);
	}
	
	
	public void addToList(ListFlowPane<CreditCell<PersonCredit>, PersonCredit> listFlowPane, PersonCredit credit) {
		listFlowPane.addItem(credit);
		listFlowPane.getChildren().add(new CreditCell<PersonCredit>(credit, listFlowPane));
		listFlowPane.setPrefHeight(listFlowPane.getChildren().size() * CreditCell.prefCellHeight);
	}
	
	public void enableTabs() {
		directorTab.setDisable( (dirFlowPane.getItems().size() == 0)? true : false);
		writerTab.setDisable( (writFlowPane.getItems().size() == 0)? true : false);
		actorTab.setDisable( (actFlowPane.getItems().size() == 0)? true : false);
		producerTab.setDisable( (prodFlowPane.getItems().size() == 0)? true : false);		
	}
	
	public void setCredits() {
		for (int i = 0; i < crewCredits.size(); i++) {
			if (crewCredits.get(i).getDepartment().equalsIgnoreCase("Directing")) {
				addToList(dirFlowPane, crewCredits.get(i));
			} else if (crewCredits.get(i).getDepartment().equalsIgnoreCase("Writing")) {
				addToList(writFlowPane, crewCredits.get(i));				
			}	else if (crewCredits.get(i).getDepartment().equalsIgnoreCase("Production")){
				addToList(prodFlowPane, crewCredits.get(i));
			}
	    }
		
		
		for (int i = 0; i <castCredits.size(); ++i) {
			addToList(actFlowPane, castCredits.get(i));
		}
		
		switch (person.getKnownForDepartment()) {
		default:
		case "Acting":
			tabPane.getSelectionModel().select(actorTab);
			break;
		case "Writing":
			tabPane.getSelectionModel().select(writerTab);
			break;
		case "Directing":
			tabPane.getSelectionModel().select(directorTab);
			break;
		case "Production":
			tabPane.getSelectionModel().select(producerTab);
			break;
		}
	}
	
	public void loadInfo() {
		
		ObservableList<Node> workingKnownForCollection = FXCollections.observableArrayList();
		if (!ControllerMaster.userData.knownFor.containsKey(person.getId())) {
			List<Integer> tempKnownList = new ArrayList<Integer>(); 
			knownForList = new ArrayList<PersonCredit>(crewCredits);
			knownForList.addAll(castCredits);
			Collections.sort(knownForList, knownComprator);
			int known = 0;
			MediaItem mi;
			boolean isKnownDep;
			final String knownDep = person.getKnownForDepartment();
			HashMap<PersonCredit, MediaItem> lesserKnown = new HashMap<PersonCredit, MediaItem>();
			PersonCredit tempCredit;
			for (int i = 0; i < knownForList.size(); ++i) {
				tempCredit = knownForList.get(i);
				isKnownDep = ( (tempCredit.getDepartment() == null && knownDep.equalsIgnoreCase("Acting")) || (tempCredit.getDepartment() != null && tempCredit.getDepartment().equals(knownDep)) )? true : false;
				if (tempCredit.getPopularity() > popularityThreshold && tempCredit.getVoteCount() > voteCountThreshold &&
						(tempCredit.getEpisodeCount()==0 || tempCredit.getEpisodeCount() > episodeCountThreshold) && 
						tempCredit.getReleaseDate() != null && isKnownDep ) {
					if (knownForList.get(i).getMediaType().equalsIgnoreCase("movie")) {
						mi = SerializationUtils.clone(MediaSearchHandler.getMovieInfoById(knownForList.get(i).getMediaId()));
						//if part of collection, just use first
						if (mi.belongsToCollection()) {
							mi = SerializationUtils.clone(MediaSearchHandler.getFirstFromCollectionMatchesPerson(mi.getCollection().getId(), person.getId()));
						}
					} else {
						mi = SerializationUtils.clone(MediaSearchHandler.getTvInfoById(knownForList.get(i).getMediaId()));
					}
				} else {
					continue;
				}	
				//item credit should match what the person is known for, been released already, and bias against being in a low count of episodes for a series
				if (mi!=null && !tempKnownList.contains(mi.getId())) {
					//prefer people higher up in the cast bill and in full features or tv shows (bias against short films)
					if ( (!knownDep.equalsIgnoreCase("Acting") || mi.getCreditPosition(person.getId()) < castPositionThreshold) && mi.isFullLength())  {
						knownForRipplers.get(known).setItem(mi);
						workingKnownForCollection.add(knownForRipplers.get(known));
						if (ControllerMaster.userData.knownFor.containsKey(person.getId())) {
							ControllerMaster.userData.knownFor.get(person.getId()).add( knownForList.get(i) );
							
						} else {
							ControllerMaster.userData.knownFor.put(person.getId(), new ArrayList<>(Arrays.asList( knownForList.get(i) )));
						}
						tempKnownList.add(mi.getId());
						++known;
					} else {
						lesserKnown.put(knownForList.get(i), mi);
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
							ControllerMaster.userData.knownFor.put(person.getId(), new ArrayList<>(Arrays.asList(credit)));
						}
						tempKnownList.add( lesserKnown.get(credit).getId() );
						++known;
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
	
	public void resizeTiles() {
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
