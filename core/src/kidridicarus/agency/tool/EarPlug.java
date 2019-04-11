package kidridicarus.agency.tool;

/*
 * A wrapper for the ear class, with getEar method that always returns non-null.
 * If no "real" ear is set for this earplug then incoming calls to onRegisterMusic, playSound, etc. will be ignored.
 * Advantage: Code that uses getEar() in this way doesn't need to check if ear == null.
 * TODO create register music catalog inside EarPlug, so every ear that is added will receive already registered music - also, EarPlug can't track any new registered music, and pass it on to curent Ears)
 */
public class EarPlug {
	private Ear realEar;
	private Ear fakeEar;

	public EarPlug() {
		realEar = null;
		fakeEar = new Ear() {
			@Override
			public void registerMusic(String musicName) {
				if(realEar != null) realEar.registerMusic(musicName);
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

	public Ear getEar() {
		return fakeEar;
	}

	public void setRealEar(Ear ear) {
		realEar = ear;
	}
}
