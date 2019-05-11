package kidridicarus.game.SMB1.agentsprite;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;

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
