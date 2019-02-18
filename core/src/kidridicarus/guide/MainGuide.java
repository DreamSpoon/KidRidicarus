package kidridicarus.guide;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.AdvisableAgent;
import kidridicarus.agent.Agent;
import kidridicarus.agent.PlayerAgent;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.agent.general.AgentSpawnTrigger;
import kidridicarus.agent.general.Room;
import kidridicarus.guide.hud.SMB_Hud;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.KVInfo;
import kidridicarus.info.PowerupInfo.PowChar;
import kidridicarus.info.PowerupInfo.PowType;
import kidridicarus.tool.KeyboardMapping;
import kidridicarus.tool.QQ;
import kidridicarus.info.SMBInfo;
import kidridicarus.info.UInfo;

public class MainGuide implements Disposable {
	private static final float SPAWN_TRIGGER_WIDTH = UInfo.P2M(UInfo.TILEPIX_X * 30);
	private static final float SPAWN_TRIGGER_HEIGHT = UInfo.P2M(UInfo.TILEPIX_X * 15);

	private Agency agency;
	private Agent agent;
	private Batch batch;
	private OrthographicCamera gamecam;
	private SMB_Hud smbHud;
	private String currentMusicName;
	private Advice advice;
	private AgentSpawnTrigger spawnTrigger;

	public MainGuide(Agency agency, Batch batch, OrthographicCamera gamecam) {
		this.agency = agency;
		this.batch = batch;
		this.gamecam = gamecam;
		currentMusicName = "";
		smbHud = new SMB_Hud(agency, batch, this);
		advice = new Advice();
		agent = null;
		spawnTrigger = null;
	}

	public void preUpdate(float delta) {
		// check for music change (music may change due to respawn)
		if(getCurrentRoom() != null) {
			if(!getCurrentRoom().getRoommusic().equals(currentMusicName)) {
				currentMusicName = getCurrentRoom().getRoommusic();
				agency.changeAndStartMusic(currentMusicName);
			}

			// create a spawn trigger if necessary
			if(spawnTrigger == null) {
				spawnTrigger = (AgentSpawnTrigger) agency.createAgent(AgentSpawnTrigger.makeAgentSpawnTriggerDef(this,
						getViewPosition(), SPAWN_TRIGGER_WIDTH, SPAWN_TRIGGER_HEIGHT));
				spawnTrigger.setEnabled(true);
			}
		}

		smbHud.update(delta);
		((AdvisableAgent) agent).setFrameAdvice(advice);
	}

	public void postUpdate() {
		// if player is not dead then use their current room to determine the gamecam position
		if(!((PlayerAgent) agent).isDead()) {
			if(getCurrentRoom() != null) {
				// set view cam position according to room
				gamecam.position.set(getViewPosition(), 0f);
				gamecam.update();
			}
		}

		// if mario received a maru mari then switch to samus
		PowType ncPow = ((PlayerAgent) agent).pollNonCharPowerup();
		if(ncPow == PowType.MARUMARI) {
			agency.startSinglePlayMusic(AudioInfo.Music.Metroid.METROIDITEM);
			switchAgentType(PowChar.SAMUS);
		}
	}

	private void switchAgentType(PowChar pc) {
		Vector2 currentPos = new Vector2(0f, 0f);
		if(agent != null) {
			agency.disposeAgent(agent);
			currentPos = agent.getPosition();
		}

		switch(pc) {
			default:
			case MARIO:
				agent = agency.createAgent(AgentDef.makePointBoundsDef(KVInfo.VAL_MARIO, currentPos));
				break;
			case SAMUS:
				agent = agency.createAgent(AgentDef.makePointBoundsDef(KVInfo.VAL_SAMUS, currentPos));
				break;
			case NONE:
				break;
		}
	}

	public void drawHUD() {
		smbHud.draw();
	}

	public void handleInput() {
		advice.moveRight = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RIGHT);
		advice.moveUp = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_UP);
		advice.moveLeft = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_LEFT);
		advice.moveDown = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_DOWN);
		// run and shoot share a key temporarily
		advice.run = advice.shoot = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RUNSHOOT);
		advice.jump = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_JUMP);

		if(Gdx.input.isKeyJustPressed(KeyboardMapping.DEBUG_TOGGLE))
			QQ.toggleOn();
		if(Gdx.input.isKeyJustPressed(KeyboardMapping.CHEAT_POWERUP)) {
			if(agent instanceof Mario) {
				((Mario) agent).applyPowerup(PowType.FIREFLOWER);
			}
		}
	}

	public void setAdviseAgent(Agent agent) {
		if(!(agent instanceof PlayerAgent) || !(agent instanceof AdvisableAgent)) {
			throw new IllegalArgumentException("setAgent method must be given only instances of PlayerAgent "+
					"combined with AdvisableAgent.");
		}
		this.agent = agent;
	}

	public Room getCurrentRoom() {
		if(agent == null)
			return null;
		return ((PlayerAgent) agent).getCurrentRoom();
	}

	public Vector2 getViewPosition() {
		if(getCurrentRoom() == null)
			return null;
		return getCurrentRoom().getViewCenterForPos(agent.getPosition());
	}

	public float getLevelTimeRemaining() {
		if(agent instanceof Mario)
			return ((Mario) agent).getLevelTimeRemaining();
		return -1;
	}

	public int getPointTotal() {
		if(agent instanceof Mario)
			return ((Mario) agent).getPointTotal();
		return 0;
	}

	public int getCoinTotal() {
		if(agent instanceof Mario)
			return ((Mario) agent).getCoinTotal();
		return 0;
	}

	public boolean isGameWon() {
		if(((PlayerAgent) agent).isAtLevelEnd() && ((PlayerAgent) agent).getStateTimer() > SMBInfo.MARIO_LEVELEND_TIME)
			return true;
		return false;
	}

	public boolean isGameOver() {
		if(((PlayerAgent) agent).isDead() && ((PlayerAgent) agent).getStateTimer() > SMBInfo.MARIO_DEAD_TIME)
			return true;
		return false;
	}

	public OrthographicCamera getGamecam() {
		return gamecam;
	}

	public Batch getBatch() {
		return batch;
	}

	@Override
	public void dispose() {
		smbHud.dispose();
		agent.dispose();
	}
}
