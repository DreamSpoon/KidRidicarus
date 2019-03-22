package kidridicarus.game.agent.SMB.player.mario;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentscript.AgentScript.AgentScriptHooks;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.common.agent.PlayerAgentSupervisor;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.MoveAdvice;
import kidridicarus.game.agent.SMB.player.mario.HUD.MarioHUD;

public class MarioSupervisor extends PlayerAgentSupervisor {
	private Mario mario;
	private MoveAdvice userMoveAdvice;
	private String nextLevelFilename;
	private boolean isGameOver;
	private MarioHUD marioHUD;
	private TextureAtlas atlas;

	public MarioSupervisor(Agency agency, Mario mario, TextureAtlas atlas) {
		super(agency, mario);
		this.mario = mario;
		this.atlas = atlas;
		userMoveAdvice = new MoveAdvice();
		nextLevelFilename = null;
		isGameOver = false;
	}

	@Override
	public void setMoveAdvice(MoveAdvice moveAdvice) {
		userMoveAdvice.set(moveAdvice);
	}

	@Override
	public MoveAdvice internalPollMoveAdvice() {
		MoveAdvice adv = userMoveAdvice.cpy();
		userMoveAdvice.clear();
		return adv;
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
		curState.scriptedSpriteState.isFacingRight = mario.getProperty(CommonKV.Script.KEY_FACINGRIGHT, false,
				Boolean.class);
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

	@Override
	public void setStageHUD(Stage stageHUD) {
		marioHUD = new MarioHUD((Mario) playerAgent, atlas, stageHUD);
	}

	@Override
	public void drawHUD() {
		marioHUD.draw();
	}
}
