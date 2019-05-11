package kidridicarus.common.agent.playeragent;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentSupervisor;
import kidridicarus.agency.agentscript.AgentScript.AgentScriptHooks;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.powerup.PowerupList;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice4x2;

public class PlayerAgentSupervisor extends AgentSupervisor {
	private RoomBox currentRoom;
	private PowerupList nonCharPowerups;
	private Vector2 lastKnownViewCenter;
	private MoveAdvice4x2 moveAdvice;
	private String nextLevelName;
	private boolean isGameOver;

	public PlayerAgentSupervisor(Agent supervisedAgent, AgentHooks supervisedAgentHooks) {
		super(supervisedAgent, supervisedAgentHooks);
		currentRoom = null;
		nonCharPowerups = new PowerupList();
		lastKnownViewCenter = null;
		moveAdvice = new MoveAdvice4x2();
		nextLevelName = null;
		isGameOver = false;
	}

	@Override
	public void postUpdateAgency() {
		super.postUpdateAgency();

		// check if player changed room, and if so, did the room music change?
		RoomBox nextRoom = ((PlayerAgent) supervisedAgent).getCurrentRoom();
		if(currentRoom != nextRoom) {
			roomChange(nextRoom);
			currentRoom = nextRoom;
		}

		// if current room is known then try to set last known view center
		RoomBox room = ((PlayerAgent) supervisedAgent).getCurrentRoom();
		Vector2 pos = AP_Tool.getCenter(supervisedAgent);
		if(room != null && pos != null)
			lastKnownViewCenter = room.getViewCenterForPos(pos, lastKnownViewCenter);
	}

	private void roomChange(RoomBox newRoom) {
		if(newRoom != null) {
			String strMusic = newRoom.getProperty(CommonKV.Room.KEY_MUSIC, null, String.class);
			if(strMusic != null)
				supervisedAgentHooks.getEar().changeAndStartMainMusic(strMusic);
		}
		// Reset the view center, so that view does not "over-scroll" if player is teleported in a one way
		// scrolling room (e.g. using doors in Kid Icarus level 1-1).
		lastKnownViewCenter = null;
	}

	public Vector2 getViewCenter() {
		RoomBox room = ((PlayerAgent) supervisedAgent).getCurrentRoom();
		if(room == null)
			return null;
		return room.getViewCenterForPos(AP_Tool.getCenter(supervisedAgent), lastKnownViewCenter);
	}

	public void receiveNonCharPowerup(Powerup pow) {
		nonCharPowerups.add(pow);
	}

	public PowerupList getNonCharPowerups() {
		return nonCharPowerups;
	}

	public void clearNonCharPowerups() {
		nonCharPowerups.clear();
	}

	@Override
	public void setMoveAdvice(MoveAdvice4x2 moveAdvice) {
		this.moveAdvice.set(moveAdvice);
	}

	@Override
	protected MoveAdvice4x2 internalPollMoveAdvice() {
		MoveAdvice4x2 adv = moveAdvice.cpy();
		moveAdvice.clear();
		return adv;
	}

	@Override
	protected ScriptedAgentState getCurrentScriptAgentState() {
		// throw exception if player doesn't have position
		Vector2 agentPos = AP_Tool.getCenter(supervisedAgent);
		if(agentPos == null)
			throw new IllegalStateException("Player Agent position cannot be null while getting state for script.");
		// get body state
		ScriptedAgentState curState = new ScriptedAgentState();
		curState.scriptedBodyState.contactEnabled = true;
		curState.scriptedBodyState.position.set(agentPos);
		// get sprite state
		curState.scriptedSpriteState.visible = true;
		curState.scriptedSpriteState.position.set(agentPos);
		curState.scriptedSpriteState.spriteState = supervisedAgent.getProperty(CommonKV.Script.KEY_SPRITE_STATE,
				SpriteState.STAND, SpriteState.class);
		if(supervisedAgent.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class) ==
				Direction4.RIGHT) {
			curState.scriptedSpriteState.isFacingRight = true;
		}
		else
			curState.scriptedSpriteState.isFacingRight = false;

		return curState;
	}

	@Override
	protected AgentScriptHooks getAgentScriptHooks() {
		return new AgentScriptHooks() {
				@Override
				public void gotoNextLevel(String name) {
					nextLevelName = name;
				}
			};
	}

	@Override
	public String getNextLevelFilename() {
		return nextLevelName;
	}

	@Override
	public boolean isAtLevelEnd() {
		return nextLevelName != null;
	}

	public void setGameOver() {
		isGameOver = true;
	}

	@Override
	public boolean isGameOver() {
		return isGameOver;
	}
}
