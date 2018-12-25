package kidridicarus.guide;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.ADefFactory;
import kidridicarus.agency.Agency;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.agent.general.GuideSpawner;
import kidridicarus.agent.general.Room;
import kidridicarus.guide.hud.SMB_Hud;
import kidridicarus.info.SMBInfo;
import kidridicarus.info.UInfo;
import kidridicarus.tools.KeyboardMapping;

public class SMBGuide implements Disposable {
	private static final float SPAWN_TRIGGER_WIDTH = UInfo.P2M(UInfo.TILEPIX_X * 30);
	private static final float SPAWN_TRIGGER_HEIGHT = UInfo.P2M(UInfo.TILEPIX_X * 15);

	private Agency agency;
	private Mario agent;
	private Batch batch;
	private OrthographicCamera gamecam;
	private SMB_Hud smbHud;
	private String currentMusicName;
	private Advice advice;

	public SMBGuide(Agency agency, Mario agent, Batch batch, OrthographicCamera gamecam) {
		this.agency = agency;
		this.agent = agent;
		this.batch = batch;
		this.gamecam = gamecam;
		currentMusicName = "";
		smbHud = new SMB_Hud(batch, agency.getAtlas(), this);
		advice = new Advice();
		agency.createAgent(ADefFactory.makeAgentSpawnTriggerDef(this, agent.getPosition(),
				SPAWN_TRIGGER_WIDTH, SPAWN_TRIGGER_HEIGHT));
	}

	public void preUpdate(float delta) {
		// check for warp movement and respawn if necessary
		GuideSpawner sp = agent.getWarpSpawnpoint(); 
		if(sp != null)
			agent.respawn(sp);

		smbHud.update(delta);
		agent.setFrameAdvice(advice);

		// check for music change
		if(getCurrentRoom() != null && !getCurrentRoom().getRoommusic().equals(currentMusicName)) {
			currentMusicName = getCurrentRoom().getRoommusic();
			agency.changeAndStartMusic(currentMusicName);
		}
	}

	public void postUpdate() {
		// if player is not dead then use their current room to determine the gamecam position
		if(!agent.isDead()) {
			if(getCurrentRoom() != null) {
				// set view cam position according to room
				gamecam.position.set(getViewPosition(), 0f);
				gamecam.update();
			}
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
		advice.run = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RUN);
		advice.jump = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_JUMP);
	}

	public Room getCurrentRoom() {
		return ((Mario) agent).getCurrentRoom();
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
		if(agent.isAtLevelEnd() && agent.getStateTimer() > SMBInfo.MARIO_LEVELEND_TIME)
			return true;
		return false;
	}

	public boolean isGameOver() {
		if(agent.isDead() && agent.getStateTimer() > SMBInfo.MARIO_DEAD_TIME)
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
