package kidridicarus.agency.tool;

import java.util.LinkedList;

/*
 * A wrapper for the ear class, with getEar method that always returns non-null.
 * If no "real" ear is set for this earplug then incoming calls to onRegisterMusic, playSound, etc. will be ignored.
 * Advantage: Code that uses getEar() in this way doesn't need to check if ear == null.
 */
public class EarPlug {
	private Ear fakeEar;
	private Ear realEar;
	private LinkedList<String> musicCatalog;

	public EarPlug() {
		musicCatalog = new LinkedList<String>();
		realEar = null;
		// fake Ear will pass information to real Ear if real Ear exists
		fakeEar = new Ear() {
			@Override
			public void registerMusic(String musicName) {
				doRegisterMusic(musicName);
			}
			@Override
			public void startSinglePlayMusic(String musicName) {
				if(realEar != null) realEar.startSinglePlayMusic(musicName);
			}
			@Override
			public void changeAndStartMainMusic(String musicName) {
				if(realEar != null) realEar.changeAndStartMainMusic(musicName);
			}
			@Override
			public void stopAllMusic() { if(realEar != null) realEar.stopAllMusic(); }
			@Override
			public void playSound(String soundName) { if(realEar != null) realEar.playSound(soundName); }
		};
	}

	private void doRegisterMusic(String musicName) {
		// add music to catalog if necessary
		if(musicName.equals("") || musicCatalog.contains(musicName))
			return;
		musicCatalog.add(musicName);
		// if real Ear exists then register music with Ear
		if(realEar != null)
			realEar.registerMusic(musicName);
	}

	/*
	 * The fake Ear will receive info, and pass to real Ear if real Ear exists. Method does not return null.
	 */
	public Ear getEar() {
		return fakeEar;
	}

	public void setEar(Ear ear) {
		realEar = ear;
		if(ear == null)
			return;

		// copy all currently registered music to the new ear
		for(String musicName : musicCatalog)
			realEar.registerMusic(musicName);
	}
}
