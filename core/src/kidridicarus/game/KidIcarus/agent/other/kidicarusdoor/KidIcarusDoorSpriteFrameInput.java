package kidridicarus.game.KidIcarus.agent.other.kidicarusdoor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;

public class KidIcarusDoorSpriteFrameInput extends SpriteFrameInput {
	public boolean isOpened;

	public KidIcarusDoorSpriteFrameInput(Vector2 position, boolean isOpened) {
		super(position);
		this.isOpened = isOpened;
	}
}
