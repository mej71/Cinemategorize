package application;

public class ResultsMediaItem extends MediaItem {
	
	private static final long serialVersionUID = 1L;
	private int seasonNum = 0;
	private int epNum = 0;
	
	
	public ResultsMediaItem(CustomMovieDb m) {
		super(m);
		
	}
	
	public ResultsMediaItem(CustomTvDb tv) {
		super(tv);
	}
	
	
}
