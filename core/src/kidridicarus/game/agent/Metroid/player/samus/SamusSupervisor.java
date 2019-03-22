package kidridicarus.game.agent.Metroid.player.samus;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentscript.AgentScript.AgentScriptHooks;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.common.agent.PlayerAgentSupervisor;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.MoveAdvice;
import kidridicarus.game.agent.Metroid.player.samus.HUD.SamusHUD;

public class SamusSupervisor extends PlayerAgentSupervisor {
	private MoveAdvice moveAdvice;
	private Samus samus;
	private TextureAtlas atlas;
	private SamusHUD samusHUD;
	private String nextLevelName;
	private boolean isGameOver;

	public SamusSupervisor(Agency agency, Samus samus, TextureAtlas atlas) {
		super(agency, samus);
		this.samus = samus;
		this.atlas = atlas;

		moveAdvice = new MoveAdvice();
		nextLevelName = null;
		isGameOver = false;
	}

	@Override
	public void setMoveAdvice(MoveAdvice moveAdvice) {
		this.moveAdvice.set(moveAdvice);
	}

	@Override
	protected MoveAdvice internalPollMoveAdvice() {
		MoveAdvice userAdvice = moveAdvice.cpy();
		moveAdvice.clear();
		return userAdvice;
	}

	@Override
	protected ScriptedAgentState getCurrentScriptAgentState() {
		ScriptedAgentState scriptedState = new ScriptedAgentState();
		scriptedState.scriptedBodyState.contactEnabled = true;
		scriptedState.scriptedBodyState.position.set(samus.getPosition());
		scriptedState.scriptedSpriteState.visible = true;
		scriptedState.scriptedSpriteState.position.set(samus.getPosition());
		scriptedState.scriptedSpriteState.spriteState =
				samus.getProperty(CommonKV.Script.KEY_SPRITESTATE, SpriteState.STAND, SpriteState.class);
		scriptedState.scriptedSpriteState.isFacingRight =
				samus.getProperty(CommonKV.Script.KEY_FACINGRIGHT, false, Boolean.class);
		return scriptedState;
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

	@Override
	public void setStageHUD(Stage stageHUD) {
		samusHUD = new SamusHUD((Samus) playerAgent, atlas, stageHUD);
	}

	@Override
	public void drawHUD() {
		samusHUD.draw();
	}
}
