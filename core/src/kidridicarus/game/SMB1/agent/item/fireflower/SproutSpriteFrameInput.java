package kidridicarus.game.SMB1.agent.item.fireflower;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;

public class SproutSpriteFrameInput extends SpriteFrameInput {
	public boolean finishSprout;

	public SproutSpriteFrameInput() {
		super();
		finishSprout = false;
	}

	public SproutSpriteFrameInput(Vector2 position, float timeDelta, boolean finishSprout) {
		super(false, timeDelta, false, false, 0f, position);
		this.finishSprout = finishSprout;
	}
}
