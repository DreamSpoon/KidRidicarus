package kidridicarus.game.KidIcarus.agent.other.kidicarusdoor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.SpriteFrameInput;

public class KidIcarusDoorSpriteFrameInput extends SpriteFrameInput {
	public boolean isOpened;

	public KidIcarusDoorSpriteFrameInput(boolean visible, Vector2 position, boolean flipX, boolean isOpened) {
		super(visible, position, flipX);
		this.isOpened = isOpened;
	}
}
