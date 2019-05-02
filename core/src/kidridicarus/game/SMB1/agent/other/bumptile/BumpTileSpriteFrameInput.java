package kidridicarus.game.SMB1.agent.other.bumptile;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;

public class BumpTileSpriteFrameInput extends SpriteFrameInput {
	public boolean isEmpty;

	public BumpTileSpriteFrameInput(Vector2 position, float timeDelta, boolean isEmpty) {
		super(false, timeDelta, false, false, 0f, position);
		this.isEmpty = isEmpty;
	}
}
