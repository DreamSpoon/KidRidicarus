package kidridicarus.worldrunner;

public interface RoleEventListener {
	public void onPlaySound(String soundName);
	public void onStartRoomMusic();
	public void onStopRoomMusic();
	public void onStartSinglePlayMusic(String musicName);
}
