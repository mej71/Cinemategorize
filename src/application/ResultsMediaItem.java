package application;

public class ResultsMediaItem extends MediaItem {
	
	private static final long serialVersionUID = 1L;
	private int tempSeasonNum = 0;
	private int tempEpNum = 0;
	
	
	public ResultsMediaItem(CustomMovieDb m) {
		super(m);
		
	}
	
	public ResultsMediaItem(CustomTvDb tv) {
		super(tv);
	}
	
	public void setTvEp(int tempSeasonNum, int tempEpNum) {
		this.tempSeasonNum = tempSeasonNum;
		this.tempEpNum = tempEpNum;
	}
	
	public int getTempSeasonNum() {
		return tempSeasonNum;
	}
	
	public int getTempEpisodeNum() {
		return tempEpNum;
	}
	
	
}
