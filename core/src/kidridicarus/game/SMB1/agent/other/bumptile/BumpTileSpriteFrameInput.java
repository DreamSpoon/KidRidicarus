package kidridicarus.game.SMB1.agent.other.bumptile;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.AnimSpriteFrameInput;

public class BumpTileSpriteFrameInput extends AnimSpriteFrameInput {
	public boolean isEmpty;

	public BumpTileSpriteFrameInput(boolean visible, Vector2 position, boolean flipX, float timeDelta,
			boolean isEmpty) {
		super(visible, position, flipX, timeDelta);
		this.isEmpty = isEmpty;
	}
}
