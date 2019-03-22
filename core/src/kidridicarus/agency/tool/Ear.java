package kidridicarus.agency.tool;

/*
 * Ear lets Agents exchange audio information.
 * If an Agent needs to send audio info (e.g. playsound) then it uses the ear's onPlaySound method.
 * If an Agent needs to receive audio info then it creates an Ear and gives the Ear to Agency for listening. 
 */
public interface Ear {
	public void onRegisterMusic(String musicName);
	public void onStartSinglePlayMusic(String musicName);
	public void onChangeAndStartMainMusic(String musicName);
	public void stopAllMusic();
	public void onPlaySound(String soundName);
}
