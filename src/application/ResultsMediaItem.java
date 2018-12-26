package application;

public class ResultsMediaItem extends MediaItem {
	
	private int seasonNum = 0;
	private int epNum = 0;
	
	
	public ResultsMediaItem(CustomMovieDb m) {
		super(m);
		
	}
	
	public ResultsMediaItem(CustomTvDb tv, int seasonNum, int epNum) {
		super(tv);
		this.seasonNum = seasonNum;
		this.epNum = epNum;
	}
	
	
}
