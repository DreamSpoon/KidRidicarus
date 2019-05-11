package kidridicarus.game.SMB1.agent.other.bumptile;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;

class BumpTileSpriteFrameInput extends SpriteFrameInput {
	boolean isEmpty;

	BumpTileSpriteFrameInput(Vector2 position, FrameTime frameTime, boolean isEmpty) {
		super(frameTime, false, false, 0f, position);
		this.isEmpty = isEmpty;
	}
}
