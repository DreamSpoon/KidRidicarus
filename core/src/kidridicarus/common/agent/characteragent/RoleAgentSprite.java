package kidridicarus.common.agent.characteragent;

import com.badlogic.gdx.graphics.g2d.Sprite;

import kidridicarus.common.agent.characteragent.RoleAgentCharacter.RoleCharacterFrameOutput;

public abstract class RoleAgentSprite extends Sprite {
	public abstract void processFrame(RoleCharacterFrameOutput charFrameOutput);
}
