package kidridicarus.game.agent.SMB1.player.mario;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentscript.AgentScript.AgentScriptHooks;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.common.agent.playeragent.PlayerAgentSupervisor;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice4x4;

public class MarioSupervisor extends PlayerAgentSupervisor {
	private MoveAdvice4x4 userMoveAdvice;
	private String nextLevelFilename;
	private boolean isGameOver;

	public MarioSupervisor(Agency agency, Mario playerAgent) {
		super(agency, playerAgent);
		this.playerAgent = playerAgent;
		userMoveAdvice = new MoveAdvice4x4();
		nextLevelFilename = null;
		isGameOver = false;
	}

	@Override
	public void setMoveAdvice(MoveAdvice4x4 moveAdvice) {
		userMoveAdvice.set(moveAdvice);
	}

	@Override
	public MoveAdvice4x4 internalPollMoveAdvice() {
		MoveAdvice4x4 adv = userMoveAdvice.cpy();
		userMoveAdvice.clear();
		return adv;
	}

	@Override
	protected ScriptedAgentState getCurrentScriptAgentState() {
		ScriptedAgentState curState = new ScriptedAgentState();
		curState.scriptedBodyState.contactEnabled = true;
		curState.scriptedBodyState.position.set(playerAgent.getPosition());

		curState.scriptedSpriteState.position.set(playerAgent.getPosition());
		curState.scriptedSpriteState.visible = true;
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
					nextLevelFilename = name;
				}
			};
	}

	@Override
	public String getNextLevelFilename() {
		return nextLevelFilename;
	}

	@Override
	public boolean isAtLevelEnd() {
		return nextLevelFilename != null;
	}

	public void setGameOver() {
		this.isGameOver = true;
	}

	@Override
	public boolean isGameOver() {
		return isGameOver;
	}
}
