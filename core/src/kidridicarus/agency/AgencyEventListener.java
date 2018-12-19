package kidridicarus.agency;

public interface AgencyEventListener {
	public void onPlaySound(String soundName);
	public void onStartRoomMusic();
	public void onStopRoomMusic();
	public void onStartSinglePlayMusic(String musicName);
}
