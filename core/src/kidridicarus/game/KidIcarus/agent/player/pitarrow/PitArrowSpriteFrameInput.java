package kidridicarus.game.KidIcarus.agent.player.pitarrow;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.tool.Direction4;

class PitArrowSpriteFrameInput extends SpriteFrameInput {
	Direction4 arrowDir;

	PitArrowSpriteFrameInput(Vector2 position, Direction4 arrowDir) {
		super();
		this.position.set(position);
		this.arrowDir = arrowDir;
	}
}
