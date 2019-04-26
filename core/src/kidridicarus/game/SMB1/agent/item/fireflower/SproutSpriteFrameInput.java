package kidridicarus.game.SMB1.agent.item.fireflower;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.AnimSpriteFrameInput;

public class SproutSpriteFrameInput extends AnimSpriteFrameInput {
	public boolean finishSprout;

	public SproutSpriteFrameInput(boolean visible, Vector2 position, boolean flipX, float timeDelta,
			boolean finishSprout) {
		super(visible, position, flipX, timeDelta);
		this.finishSprout = finishSprout;
	}
}
