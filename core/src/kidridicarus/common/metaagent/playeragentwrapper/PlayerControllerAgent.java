package kidridicarus.common.metaagent.playeragentwrapper;

import java.util.Collection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.keepalivebox.KeepAliveBox;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agent.scrollbox.ScrollBox;
import kidridicarus.common.agent.scrollkillbox.ScrollKillBox;
import kidridicarus.common.agent.scrollpushbox.ScrollPushBox;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.KeyboardMapping;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice4x4;
import kidridicarus.common.tool.QQ;
import kidridicarus.game.info.KidIcarusKV;
import kidridicarus.game.info.MetroidKV;
import kidridicarus.game.info.SMB1_KV;
import kidridicarus.game.powerup.KidIcarusPow;
import kidridicarus.game.powerup.MetroidPow;
import kidridicarus.game.powerup.SMB1_Pow;

public class PlayerControllerAgent extends Agent implements DisposableAgent {
	private static final float SPAWN_TRIGGER_WIDTH = UInfo.P2M(UInfo.TILEPIX_X * 20);
	private static final float SPAWN_TRIGGER_HEIGHT = UInfo.P2M(UInfo.TILEPIX_Y * 15);
	private static final float KEEP_ALIVE_WIDTH = UInfo.P2M(UInfo.TILEPIX_X * 22);
	private static final float KEEP_ALIVE_HEIGHT = UInfo.P2M(UInfo.TILEPIX_Y * 15);
	private static final Vector2 SAFETY_RESPAWN_OFFSET = UInfo.VectorP2M(0f, 8f);

	private PlayerAgent playerAgent;
	private AgentSpawnTrigger spawnTrigger;
	private KeepAliveBox keepAliveBox;
	private ScrollBox scrollBox;
	private MoveAdvice4x4 inputMoveAdvice;
	private Vector2 lastViewCenter;

	public PlayerControllerAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		inputMoveAdvice = new MoveAdvice4x4();
		lastViewCenter = new Vector2(0f, 0f);

		// create the PlayerAgent that this wrapper will control
		ObjectProperties playerAgentProperties =
				properties.get(CommonKV.Player.KEY_AGENT_PROPERTIES, null, ObjectProperties.class);
		createPlayerAgent(playerAgentProperties);

		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_AGENCY_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doPreAgencyUpdate(delta); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.POST_AGENCY_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doPostAgencyUpdate(); }
			});
	}

	private void createPlayerAgent(ObjectProperties playerAgentProperties) {
		// find main player spawner and return fail if none found
		Agent spawner = getMainPlayerSpawner();
		if(spawner == null)
			throw new IllegalStateException("Cannot spawn player, main player spawner not found.");

		// spawn player with properties at spawn location
		playerAgent = spawnPlayerAgentWithProperties(playerAgentProperties, spawner);
		// create player's associated agents (generally, they follow player)
		spawnTrigger = (AgentSpawnTrigger) agency.createAgent(
				AgentSpawnTrigger.makeAP(getViewCenter(), SPAWN_TRIGGER_WIDTH, SPAWN_TRIGGER_HEIGHT));
		spawnTrigger.setEnabled(true);
		keepAliveBox = (KeepAliveBox) agency.createAgent(
				KeepAliveBox.makeAP(getViewCenter(), KEEP_ALIVE_WIDTH, KEEP_ALIVE_HEIGHT));
	}

	// get user input
	private void doPreAgencyUpdate(float delta) {
		// ensure spawn trigger and keep alive box follow view center
		spawnTrigger.setTarget(getViewCenter());
		keepAliveBox.setTarget(getViewCenter());
		if(scrollBox != null)
			scrollBox.setTarget(getViewCenter());
		handleInput(delta);
		playerAgent.getSupervisor().preUpdateAgency(delta);
	}

	private void handleInput(float delta) {
		inputMoveAdvice.moveRight = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RIGHT);
		inputMoveAdvice.moveUp = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_UP);
		inputMoveAdvice.moveLeft = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_LEFT);
		inputMoveAdvice.moveDown = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_DOWN);
		inputMoveAdvice.action0 = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RUNSHOOT);
		inputMoveAdvice.action1 = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_JUMP);

		if(Gdx.input.isKeyJustPressed(KeyboardMapping.DEBUG_TOGGLE))
			QQ.toggleOn();
		if(Gdx.input.isKeyJustPressed(KeyboardMapping.CHEAT_POWERUP_MARIO))
			Powerup.tryPushPowerup(playerAgent, new SMB1_Pow.FireFlowerPow());
		else if(Gdx.input.isKeyJustPressed(KeyboardMapping.CHEAT_POWERUP_SAMUS))
			Powerup.tryPushPowerup(playerAgent, new MetroidPow.EnergyPow());
		else if(Gdx.input.isKeyJustPressed(KeyboardMapping.CHEAT_POWERUP_PIT))
			Powerup.tryPushPowerup(playerAgent, new KidIcarusPow.AngelHeartPow(5));

		// pass user input to player agent's supervisor
		playerAgent.getSupervisor().setMoveAdvice(inputMoveAdvice);
	}

	private void doPostAgencyUpdate() {
		// check for "out-of-character" powerup received and change to appropriate character for powerup
		Powerup nonCharPowerup = playerAgent.getSupervisor().getNonCharPowerups().getFirst();
		playerAgent.getSupervisor().clearNonCharPowerups();
		if(nonCharPowerup != null)
			switchAgentType(nonCharPowerup.getPowerupCharacter());

		playerAgent.getSupervisor().postUpdateAgency();
		checkCreateScrollBox();
		updateCamera();
	}

	/*
	 * As the player moves into and out of rooms, the scroll box may need to be created / removed / changed.
	 */
	private void checkCreateScrollBox() {
		RoomBox currentRoom = playerAgent.getCurrentRoom();
		if(currentRoom == null)
			return;

		Direction4 scrollDir = Direction4.fromString(
				currentRoom.getProperty(CommonKV.Room.KEY_SCROLL_DIR, "", String.class));
		// if current room has scroll push box property = true then create/change to scroll push box
		if(currentRoom.getProperty(CommonKV.Room.KEY_SCROLL_PUSHBOX, false, Boolean.class)) {
			if(scrollBox != null && !(scrollBox instanceof ScrollPushBox)) {
				agency.removeAgent(scrollBox);
				scrollBox.dispose();
				scrollBox = null;
			}
			// if scroll box needs to be created and a valid scroll direction is given then create push box
			if(scrollBox == null && scrollDir != Direction4.NONE)
				scrollBox = (ScrollPushBox) agency.createAgent(ScrollPushBox.makeAP(getViewCenter(), scrollDir));
		}
		// if current room has scroll kill box property = true then create/change to scroll kill box
		else if(currentRoom.getProperty(CommonKV.Room.KEY_SCROLL_KILLBOX, false, Boolean.class)) {
			if(scrollBox != null && !(scrollBox instanceof ScrollKillBox)) {
				agency.removeAgent(scrollBox);
				scrollBox.dispose();
				scrollBox = null;
			}
			// if scroll box needs to be created and a valid scroll direction is given then create kill box
			if(scrollBox == null && scrollDir != Direction4.NONE)
				scrollBox = (ScrollKillBox) agency.createAgent(ScrollKillBox.makeAP(getViewCenter(), scrollDir));
		}
		// need to remove a scroll box?
		else if(scrollBox != null) {
			agency.removeAgent(scrollBox);
			scrollBox.dispose();
			scrollBox = null;
		}
	}

	private void switchAgentType(PowChar pc) {
		Vector2 currentPos = new Vector2(0f, 0f);
		boolean facingRight = false;
		if(playerAgent != null) {
			currentPos = playerAgent.getPosition().add(SAFETY_RESPAWN_OFFSET);
			facingRight = playerAgent.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE,
					Direction4.class) == Direction4.RIGHT;
			agency.removeAgent(playerAgent);
			playerAgent.dispose();
			playerAgent = null;
		}

		switch(pc) {
			default:
			case MARIO:
				doMakeCharacter(SMB1_KV.AgentClassAlias.VAL_MARIO, currentPos, facingRight);
				break;
			case PIT:
				doMakeCharacter(KidIcarusKV.AgentClassAlias.VAL_PIT, currentPos, facingRight);
				break;
			case SAMUS:
				doMakeCharacter(MetroidKV.AgentClassAlias.VAL_SAMUS, currentPos, facingRight);
				break;
			case NONE:
				break;
		}
	}

	private void doMakeCharacter(String classAlias, Vector2 position, boolean facingRight) {
		ObjectProperties props = Agent.createPointAP(classAlias, position);
		if(facingRight)
			props.put(CommonKV.KEY_DIRECTION, Direction4.RIGHT);
		playerAgent = (PlayerAgent) agency.createAgent(props);
	}

	private void updateCamera() {
		// if player is not dead then use their current room to determine the gamecam position
		if(!playerAgent.getSupervisor().isGameOver())
			agency.getEye().setViewCenter(getViewCenter());
	}

	private Agent getMainPlayerSpawner() {
		// find main spawnpoint and spawn player there, or spawn at (0, 0) if no spawnpoint found
		Collection<Agent> spawnList = agency.getAgentsByProperties(
				new String[] { AgencyKV.Spawn.KEY_AGENT_CLASS, CommonKV.Spawn.KEY_SPAWN_MAIN },
				new String[] { CommonKV.AgentClassAlias.VAL_PLAYER_SPAWNER, CommonKV.VAL_TRUE });
		if(!spawnList.isEmpty())
			return spawnList.iterator().next();
		else
			return null;
	}

	private PlayerAgent spawnPlayerAgentWithProperties(ObjectProperties playerAgentProperties, Agent spawner) {
		// if no agent properties given then use spawner to determine player class and position
		if(playerAgentProperties == null)
			return spawnPlayerAgentWithSpawnerProperties(spawner);
		// otherwise use agent properties and set start point to main spawn point
		else {
			playerAgentProperties.put(AgencyKV.Spawn.KEY_START_POS, spawner.getPosition());
			return (PlayerAgent) agency.createAgent(playerAgentProperties);
		}
	}

	private PlayerAgent spawnPlayerAgentWithSpawnerProperties(Agent spawner) {
		String initPlayClass = spawner.getProperty(CommonKV.Spawn.KEY_PLAYER_AGENTCLASS, null, String.class);
		if(initPlayClass == null)
			return null;
		ObjectProperties playerAP = Agent.createPointAP(initPlayClass, spawner.getPosition());
		if(spawner.getProperty(CommonKV.KEY_DIRECTION, "", String.class).equals(CommonKV.VAL_RIGHT))
			playerAP.put(CommonKV.KEY_DIRECTION, Direction4.RIGHT);
		return (PlayerAgent) agency.createAgent(playerAP);
	}

	// safely get the view center - cannot return null, and tries to return a correct view center
	private Vector2 getViewCenter() {
		Vector2 vc = null;
		if(playerAgent != null)
			vc = playerAgent.getSupervisor().getViewCenter();
		if(vc == null)
			vc = lastViewCenter;
		else
			lastViewCenter.set(vc);
		return vc;
	}

	public boolean isGameWon() {
		return playerAgent.getSupervisor().isAtLevelEnd();
	}

	public boolean isGameOver() {
		return playerAgent.getSupervisor().isGameOver();
	}

	public String getNextLevelFilename() {
		return playerAgent.getSupervisor().getNextLevelFilename();
	}

	public ObjectProperties getCopyPlayerAgentProperties() {
		return playerAgent.getCopyAllProperties();
	}

	@Override
	public Vector2 getPosition() {
		return null;
	}

	@Override
	public Rectangle getBounds() {
		return null;
	}

	@Override
	public void disposeAgent() {
		if(scrollBox != null)
			scrollBox.dispose();
		if(keepAliveBox != null)
			keepAliveBox.dispose();
		if(spawnTrigger != null)
			spawnTrigger.dispose();
		if(playerAgent != null)
			playerAgent.dispose();
	}

	public static ObjectProperties makeAP(ObjectProperties playerAgentProperties) {
		ObjectProperties props = Agent.createAP(CommonKV.AgentClassAlias.VAL_PLAYER_WRAPPER);
		props.put(CommonKV.Player.KEY_AGENT_PROPERTIES, playerAgentProperties);
		return props;
	}
}
