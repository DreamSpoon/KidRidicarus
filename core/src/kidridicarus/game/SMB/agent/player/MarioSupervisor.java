package kidridicarus.game.SMB.agent.player;

import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.agency.tool.SuperAdvice;
import kidridicarus.common.agent.GameAgentSupervisor;
import kidridicarus.game.info.GameKV;
import kidridicarus.game.info.PowerupInfo.PowType;
import kidridicarus.game.play.GameAdvice;

public class MarioSupervisor extends GameAgentSupervisor {
	private GameAdvice advice;
	private boolean switchToOtherChar;
	private Mario mario;

	public MarioSupervisor(Mario mario) {
		this.mario = mario;
		advice = new GameAdvice();
		switchToOtherChar = false;
	}

	@Override
	public void setFrameAdvice(SuperAdvice superAdvice) {
		advice.fromSuperAdvice(superAdvice);
	}

	@Override
	public GameAdvice pollFrameAdvice() {
		GameAdvice adv = advice.cpy();
		advice.clear();
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
				mario.getProperty(GameKV.Script.KEY_SPRITESTATE, SpriteState.STAND, SpriteState.class);
		curState.scriptedSpriteState.facingRight =
				mario.getProperty(GameKV.Script.KEY_FACINGRIGHT, false, Boolean.class);

		return curState;
	}
}
