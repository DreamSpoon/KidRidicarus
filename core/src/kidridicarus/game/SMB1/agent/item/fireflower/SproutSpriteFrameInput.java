package kidridicarus.game.SMB1.agent.item.fireflower;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agentsprite.SpriteFrameInput;

public class SproutSpriteFrameInput extends SpriteFrameInput {
	public boolean finishSprout;

	public SproutSpriteFrameInput() {
		super();
		finishSprout = false;
	}

	public SproutSpriteFrameInput(Vector2 position, FrameTime frameTime, boolean finishSprout) {
		super(frameTime, false, false, 0f, position);
		this.finishSprout = finishSprout;
	}
}
