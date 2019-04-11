package kidridicarus.game.agent.KidIcarus.player.pit;

import kidridicarus.agency.agentscript.AgentScript.AgentScriptHooks;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.common.agent.playeragent.PlayerAgentSupervisor;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice4x4;

public class PitSupervisor extends PlayerAgentSupervisor {
	private MoveAdvice4x4 moveAdvice;
	private String nextLevelName;
	private boolean isGameOver;

	public PitSupervisor(Pit pit) {
		super(pit);

		moveAdvice = new MoveAdvice4x4();
		nextLevelName = null;
		isGameOver = false;
	}

	@Override
	public void setMoveAdvice(MoveAdvice4x4 moveAdvice) {
		this.moveAdvice.set(moveAdvice);
	}

	@Override
	protected MoveAdvice4x4 internalPollMoveAdvice() {
		MoveAdvice4x4 userAdvice = moveAdvice.cpy();
		moveAdvice.clear();
		return userAdvice;
	}

	@Override
	protected ScriptedAgentState getCurrentScriptAgentState() {
		ScriptedAgentState curState = new ScriptedAgentState();
		curState.scriptedBodyState.contactEnabled = true;
		curState.scriptedBodyState.position.set(playerAgent.getPosition());
		curState.scriptedSpriteState.visible = true;
		curState.scriptedSpriteState.position.set(playerAgent.getPosition());
		curState.scriptedSpriteState.spriteState =
				playerAgent.getProperty(CommonKV.Script.KEY_SPRITE_STATE, SpriteState.STAND, SpriteState.class);
		if(playerAgent.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class) == Direction4.RIGHT)
			curState.scriptedSpriteState.isFacingRight = true;
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

	public void setGameOver() {
		isGameOver = true;
	}

	@Override
	public boolean isAtLevelEnd() {
		return nextLevelName != null;
	}

	@Override
	public boolean isGameOver() {
		return isGameOver;
	}
}
