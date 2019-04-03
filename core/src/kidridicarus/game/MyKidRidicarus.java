package kidridicarus.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import kidridicarus.agency.AgentClassList;
import kidridicarus.common.agencydirector.AgencyDirector;
import kidridicarus.common.info.CommonAgentClassList;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.info.KidIcarusAgentClassList;
import kidridicarus.game.info.KidIcarusAudio;
import kidridicarus.game.info.MetroidAgentClassList;
import kidridicarus.game.info.MetroidAudio;
import kidridicarus.game.info.SMB1_AgentClassList;
import kidridicarus.game.info.SMB1_Audio;
import kidridicarus.game.screen.InstructionsScreen;

/*
 * Main game asset loader class.
 */
public class MyKidRidicarus extends Game {
	public SpriteBatch batch;
	public AssetManager manager;
	public AgencyDirector director;

	private TextureAtlas atlas;

	@Override
	public void create () {
		batch = new SpriteBatch();

		atlas = new TextureAtlas(CommonInfo.TA_MAIN_FILENAME);

		manager = new AssetManager();
		// other music files may be loaded later when a space is loaded
		manager.load(KidIcarusAudio.Music.PIT_DIE, Music.class);
		manager.load(MetroidAudio.Music.GET_ITEM, Music.class);
		manager.load(MetroidAudio.Music.SAMUS_DIE, Music.class);
		manager.load(SMB1_Audio.Music.LEVELEND, Music.class);
		manager.load(SMB1_Audio.Music.STARPOWER, Music.class);

		manager.load(KidIcarusAudio.Sound.Pit.HURT, Sound.class);
		manager.load(KidIcarusAudio.Sound.Pit.JUMP, Sound.class);
		manager.load(KidIcarusAudio.Sound.Pit.SHOOT, Sound.class);
		manager.load(KidIcarusAudio.Sound.General.HEART_PICKUP, Sound.class);
		manager.load(KidIcarusAudio.Sound.General.SMALL_POOF, Sound.class);
		manager.load(MetroidAudio.Sound.DOOR, Sound.class);
		manager.load(MetroidAudio.Sound.ENERGY_PICKUP, Sound.class);
		manager.load(MetroidAudio.Sound.HURT, Sound.class);
		manager.load(MetroidAudio.Sound.JUMP, Sound.class);
		manager.load(MetroidAudio.Sound.NPC_BIG_HIT, Sound.class);
		manager.load(MetroidAudio.Sound.NPC_SMALL_HIT, Sound.class);
		manager.load(MetroidAudio.Sound.SHOOT, Sound.class);
		manager.load(MetroidAudio.Sound.STEP, Sound.class);
		manager.load(SMB1_Audio.Sound.BREAK, Sound.class);
		manager.load(SMB1_Audio.Sound.BUMP, Sound.class);
		manager.load(SMB1_Audio.Sound.COIN, Sound.class);
		manager.load(SMB1_Audio.Sound.FIREBALL, Sound.class);
		manager.load(SMB1_Audio.Sound.FLAGPOLE, Sound.class);
		manager.load(SMB1_Audio.Sound.KICK, Sound.class);
		manager.load(SMB1_Audio.Sound.MARIO_DIE, Sound.class);
		manager.load(SMB1_Audio.Sound.MARIO_SMLJUMP, Sound.class);
		manager.load(SMB1_Audio.Sound.MARIO_BIGJUMP, Sound.class);
		manager.load(SMB1_Audio.Sound.POWERDOWN, Sound.class);
		manager.load(SMB1_Audio.Sound.POWERUP_SPAWN, Sound.class);
		manager.load(SMB1_Audio.Sound.POWERUP_USE, Sound.class);
		manager.load(SMB1_Audio.Sound.STOMP, Sound.class);
		manager.load(SMB1_Audio.Sound.UP1, Sound.class);
		manager.finishLoading();

		director = new AgencyDirector(manager, batch, atlas,
				new AgentClassList(CommonAgentClassList.CORE_AGENT_CLASS_LIST,
						SMB1_AgentClassList.SMB_AGENT_CLASSLIST,
						MetroidAgentClassList.METROID_AGENT_CLASSLIST,
						KidIcarusAgentClassList.KIDICARUS_AGENT_CLASSLIST));

		// show intro/instructions screen
		setScreen(new InstructionsScreen(this, CommonInfo.GAMEMAP_FILENAME1));
	}

	@Override
	public void dispose () {
		super.dispose();
		if(getScreen() != null)
			getScreen().dispose();
		director.dispose();
		batch.dispose();
		manager.dispose();
	}
}
