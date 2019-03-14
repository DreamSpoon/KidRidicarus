package kidridicarus.game.agent.SMB.player.mario;

import kidridicarus.agency.agentscript.AgentScript.AgentScriptHooks;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.common.agent.GameAgentSupervisor;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.MoveAdvice;
import kidridicarus.game.info.PowerupInfo.PowType;

public class MarioSupervisor extends GameAgentSupervisor {
	private MoveAdvice curMoveAdvice;
	private boolean switchToOtherChar;
	private Mario mario;
	private String nextLevelName;
	private boolean isGameOver;

	public MarioSupervisor(Mario mario) {
		this.mario = mario;
		curMoveAdvice = new MoveAdvice();
		switchToOtherChar = false;
		nextLevelName = null;
		isGameOver = false;
	}

	@Override
	public void setMoveAdvice(MoveAdvice moveAdvice) {
		curMoveAdvice.set(moveAdvice);
	}

	@Override
	public MoveAdvice pollMoveAdvice() {
		MoveAdvice adv = curMoveAdvice.cpy();
		curMoveAdvice.clear();
		return adv;
	}

	/*
	 * This should only be called with non-mario powerups TODO: fix this!
	 */
	public void applyNonMarioPowerup(PowType powerupRec) {
		switchToOtherChar = true;
	}

	@Override
	public boolean isSwitchToOtherChar() {
		return switchToOtherChar;
	}

	@Override
	protected ScriptedAgentState getCurrentScriptAgentState() {
		ScriptedAgentState curState = new ScriptedAgentState();
		curState.scriptedBodyState.contactEnabled = true;
		curState.scriptedBodyState.position.set(mario.getPosition());

		curState.scriptedSpriteState.position.set(mario.getPosition());
		curState.scriptedSpriteState.visible = true;
		curState.scriptedSpriteState.spriteState =
				mario.getProperty(CommonKV.Script.KEY_SPRITESTATE, SpriteState.STAND, SpriteState.class);
		curState.scriptedSpriteState.facingRight =
				mario.getProperty(CommonKV.Script.KEY_FACINGRIGHT, false, Boolean.class);

		return curState;
	}

	@Override
	public String getNextLevelName() {
		return null;
	}

	@Override
	protected AgentScriptHooks getAgentScriptHooks() {
		return new AgentScriptHooks() {
				@Override
				public void gotoNextLevel(String levelName) {
					nextLevelName = levelName;
				}
			};
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
