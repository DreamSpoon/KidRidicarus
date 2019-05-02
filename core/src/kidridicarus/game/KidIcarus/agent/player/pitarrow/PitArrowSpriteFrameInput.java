package kidridicarus.game.KidIcarus.agent.player.pitarrow;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.tool.Direction4;

public class PitArrowSpriteFrameInput extends SpriteFrameInput {
	public Direction4 arrowDir;

	public PitArrowSpriteFrameInput(Vector2 position, Direction4 arrowDir) {
		super();
		this.position.set(position);
		this.arrowDir = arrowDir;
	}
}
