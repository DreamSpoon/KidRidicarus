package kidridicarus.common.agent.proactoragent;

import com.badlogic.gdx.graphics.g2d.Sprite;

import kidridicarus.common.agentsprite.SpriteFrameInput;

public abstract class ActorAgentSprite extends Sprite {
	public abstract void processFrame(SpriteFrameInput frameInput);
}
