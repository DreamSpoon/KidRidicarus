package kidridicarus.game.play;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentObserver.AgentObserverListener;
import kidridicarus.agency.agent.general.AgentSpawnTrigger;
import kidridicarus.agency.agent.optional.PlayerAgent;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.SuperAdvice;
import kidridicarus.game.agent.SMB.player.Mario;
import kidridicarus.game.info.PowerupInfo.PowType;
import kidridicarus.game.info.SMBInfo;
import kidridicarus.game.tool.KeyboardMapping;
import kidridicarus.game.tool.QQ;

public class PlayCoordinator implements Disposable {
	private static final float SPAWN_TRIGGER_WIDTH = UInfo.P2M(UInfo.TILEPIX_X * 30);
	private static final float SPAWN_TRIGGER_HEIGHT = UInfo.P2M(UInfo.TILEPIX_X * 15);

	private Agency agency;

	// * two references to the same player agent - usage depends upon the functionality needed
	private Agent agent;
	private PlayerAgent playAgent;
	// * TODO ?  a private class that  extends Agent implements Playeragent  ?

	private AgentSpawnTrigger spawnTrigger;
	private SuperAdvice superAdvice;
	private OrthographicCamera gamecam;
	private Stage stageHUD;

	public PlayCoordinator(Agency agency, OrthographicCamera gamecam, Stage stageHUD) {
		this.agency = agency;
		this.gamecam = gamecam;
		this.stageHUD = stageHUD;
		spawnTrigger = null;
		playAgent = null;
		superAdvice = new SuperAdvice();
	}

	public void setPlayAgent(Agent agent) {
		if(!(agent instanceof PlayerAgent))
			throw new IllegalArgumentException("agent is not instanceof PlayerAgent: " + agent);

		this.agent = agent;
		this.playAgent = (PlayerAgent) agent;
		spawnTrigger = (AgentSpawnTrigger) agency.createAgent(AgentSpawnTrigger.makeAgentSpawnTriggerDef(
				playAgent.getObserver().getViewCenter(), SPAWN_TRIGGER_WIDTH, SPAWN_TRIGGER_HEIGHT));
		spawnTrigger.setEnabled(true);

		playAgent.getObserver().setStageHUD(stageHUD);
		playAgent.getObserver().setListener(new AgentObserverListener() {
				@Override
				public void startRoomMusic(String musicName) {
QQ.pr("start room music");
				}

				@Override
				public void startSinglePlayMusic(String musicName) {
QQ.pr("start single play music");
				}

				@Override
				public void stopAllMusic() {
QQ.pr("stop all music");				}
			});
	}

	public void handleInput() {
		superAdvice.moveRight = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RIGHT);
		superAdvice.moveUp = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_UP);
		superAdvice.moveLeft = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_LEFT);
		superAdvice.moveDown = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_DOWN);
		superAdvice.action0 = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_JUMP);
		superAdvice.action1 = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RUNSHOOT);

		if(Gdx.input.isKeyJustPressed(KeyboardMapping.DEBUG_TOGGLE))
			QQ.toggleOn();
		if(Gdx.input.isKeyJustPressed(KeyboardMapping.CHEAT_POWERUP)) {
			if(agent instanceof Mario) {
				((Mario) agent).applyPowerup(PowType.FIREFLOWER);
			}
		}
	}

	public void preUpdateAgency() {
		spawnTrigger.setTarget(playAgent.getObserver().getViewCenter());
		playAgent.getSupervisor().setFrameAdvice(superAdvice);
	}

	public void postUpdateAgency() {
		playAgent.getObserver().postUpdateAgency();
	}

	public void updateCamera() {
		// if player is not dead then use their current room to determine the gamecam position
		if(!((PlayerAgent) agent).isDead()) {
			gamecam.position.set(playAgent.getObserver().getViewCenter(), 0f);
			gamecam.update();
		}
	}

	public void drawHUD() {
		playAgent.getObserver().drawHUD();
	}

	public boolean isGameWon() {
		if(playAgent.isAtLevelEnd() && playAgent.getStateTimer() > SMBInfo.MARIO_LEVELEND_TIME)
			return true;
		return false;
	}

	public boolean isGameOver() {
		if(playAgent.isDead() && playAgent.getStateTimer() > SMBInfo.MARIO_DEAD_TIME)
			return true;
		return false;
	}

	@Override
	public void dispose() {
//		if(spawnTrigger != null)
//			spawnTrigger.dispose();
	}
}

/*
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDef;
import kidridicarus.agency.agent.general.AgentSpawnTrigger;
import kidridicarus.agency.agent.general.Room;
import kidridicarus.agency.agent.optional.PlayerAgent;
import kidridicarus.agency.info.UInfo;
import kidridicarus.game.agent.SMB.player.Mario;
import kidridicarus.game.guide.hud.Samus_Hud;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.KVInfo;
import kidridicarus.game.info.SMBInfo;
import kidridicarus.game.info.PowerupInfo.PowChar;
import kidridicarus.game.info.PowerupInfo.PowType;
import kidridicarus.game.tool.KeyboardMapping;
import kidridicarus.game.tool.QQ;

public class MainGuide implements Disposable {
	private static final float SPAWN_TRIGGER_WIDTH = UInfo.P2M(UInfo.TILEPIX_X * 30);
	private static final float SPAWN_TRIGGER_HEIGHT = UInfo.P2M(UInfo.TILEPIX_X * 15);

	private Agency agency;
	private Agent agent;
	private Batch batch;
	private OrthographicCamera gamecam;
//	private SMB_Hud smbHud;
	private String currentMusicName;
	private SuperAdvice superAdvice;
	private AgentSpawnTrigger spawnTrigger;

	public MainGuide(Agency agency, Batch batch, OrthographicCamera gamecam) {
		this.agency = agency;
		this.batch = batch;
		this.gamecam = gamecam;
		currentMusicName = "";
		superAdvice = new SuperAdvice();
		agent = null;
		spawnTrigger = null;
	}

	public void setAdviseAgent(Agent agent) {
		if(!(agent instanceof PlayerAgent))
			throw new IllegalArgumentException("agent is not an instanceof PlayerAgent: " + agent);
		this.agent = agent;
	}

	public void preUpdate() {
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

		smbHud.update();
		((PlayerAgent) agent).getSupervisor().setFrameAdvice(superAdvice);
	}

	public void postUpdate() {
		// if player is not dead then use their current room to determine the gamecam position
		if(!((PlayerAgent) agent).isDead() && getCurrentRoom() != null) {
			// set view cam position according to room
			gamecam.position.set(getViewPosition(), 0f);
			gamecam.update();
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
				agent = agency.createAgent(AgentDef.makePointBoundsDef(KVInfo.SMB.VAL_MARIO, currentPos));
				break;
			case SAMUS:
				agent = agency.createAgent(AgentDef.makePointBoundsDef(KVInfo.Metroid.VAL_SAMUS, currentPos));
				break;
			case NONE:
				break;
		}
	}

	public void drawHUD() {
		smbHud.draw();
	}

	public void handleInput() {
		superAdvice.moveRight = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RIGHT);
		superAdvice.moveUp = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_UP);
		superAdvice.moveLeft = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_LEFT);
		superAdvice.moveDown = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_DOWN);
		superAdvice.action0 = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_JUMP);
		superAdvice.action1 = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RUNSHOOT);

		if(Gdx.input.isKeyJustPressed(KeyboardMapping.DEBUG_TOGGLE))
			QQ.toggleOn();
		if(Gdx.input.isKeyJustPressed(KeyboardMapping.CHEAT_POWERUP)) {
			if(agent instanceof Mario) {
				((Mario) agent).applyPowerup(PowType.FIREFLOWER);
			}
		}
	}

	private Room getCurrentRoom() {
		if(agent == null)
			return null;
		return ((PlayerAgent) agent).getCurrentRoom();
	}

	public Vector2 getViewPosition() {
		return ((PlayerAgent) agent).getObserver().getViewCenter();
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
		agency.disposeAgent(agent);
	}
}
*/
