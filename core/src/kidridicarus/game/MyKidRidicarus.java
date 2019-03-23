package kidridicarus.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import kidridicarus.agency.AgentClassList;
import kidridicarus.common.agencydirector.AgencyDirector;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GameInfo;
import kidridicarus.game.info.MetroidInfo;
import kidridicarus.game.info.SMBInfo;
import kidridicarus.game.screen.PlayScreen;

/*
 * Main game asset loader class - or at least, it should be...
 * TODO: Load level assets here?
 */
public class MyKidRidicarus extends Game {
	public SpriteBatch batch;
	public ShapeRenderer sr;
	public AssetManager manager;
	public AgencyDirector director;

	private TextureAtlas atlas;

	@Override
	public void create () {
		batch = new SpriteBatch();
		sr = new ShapeRenderer();

		atlas = new TextureAtlas(GameInfo.TA_MAIN_FILENAME);

		manager = new AssetManager();
		// other music files may be loaded later when a space is loaded
		manager.load(AudioInfo.Music.SMB.LEVELEND, Music.class);
		manager.load(AudioInfo.Music.SMB.STARPOWER, Music.class);
		manager.load(AudioInfo.Music.Metroid.METROIDITEM, Music.class);
		manager.load(AudioInfo.Sound.SMB.COIN, Sound.class);
		manager.load(AudioInfo.Sound.SMB.BUMP, Sound.class);
		manager.load(AudioInfo.Sound.SMB.BREAK, Sound.class);
		manager.load(AudioInfo.Sound.SMB.POWERUP_SPAWN, Sound.class);
		manager.load(AudioInfo.Sound.SMB.POWERUP_USE, Sound.class);
		manager.load(AudioInfo.Sound.SMB.POWERDOWN, Sound.class);
		manager.load(AudioInfo.Sound.SMB.STOMP, Sound.class);
		manager.load(AudioInfo.Sound.SMB.MARIO_DIE, Sound.class);
		manager.load(AudioInfo.Sound.SMB.MARIO_SMLJUMP, Sound.class);
		manager.load(AudioInfo.Sound.SMB.MARIO_BIGJUMP, Sound.class);
		manager.load(AudioInfo.Sound.SMB.KICK, Sound.class);
		manager.load(AudioInfo.Sound.SMB.FIREBALL, Sound.class);
		manager.load(AudioInfo.Sound.SMB.FLAGPOLE, Sound.class);
		manager.load(AudioInfo.Sound.SMB.UP1, Sound.class);
		manager.load(AudioInfo.Sound.Metroid.JUMP, Sound.class);
		manager.load(AudioInfo.Sound.Metroid.STEP, Sound.class);
		manager.load(AudioInfo.Sound.Metroid.HURT, Sound.class);
		manager.load(AudioInfo.Sound.Metroid.SHOOT, Sound.class);
		manager.load(AudioInfo.Sound.Metroid.ENERGY_PICKUP, Sound.class);
		manager.finishLoading();

		director = new AgencyDirector(manager, batch, atlas,
				new AgentClassList(CommonInfo.CORE_AGENT_CLASS_LIST, SMBInfo.SMB_AGENT_CLASSLIST,
						MetroidInfo.METROID_AGENT_CLASSLIST));

		// start playing first level 
		setScreen(new PlayScreen(this, GameInfo.GAMEMAP_FILENAME1, null));
	}

	@Override
	public void dispose () {
		super.dispose();
		if(getScreen() != null)
			getScreen().dispose();
		director.dispose();
		batch.dispose();
		sr.dispose();
		manager.dispose();
	}
}
