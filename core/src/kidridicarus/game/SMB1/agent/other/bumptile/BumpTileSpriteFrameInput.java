package kidridicarus.game.SMB1.agent.other.bumptile;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.AnimSpriteFrameInput;

public class BumpTileSpriteFrameInput extends AnimSpriteFrameInput {
	public boolean isEmpty;

	public BumpTileSpriteFrameInput(boolean visible, Vector2 position, float timeDelta, boolean isEmpty) {
		super(visible, position, true, timeDelta);
		this.isEmpty = isEmpty;
	}
}
