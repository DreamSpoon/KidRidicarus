package kidridicarus.game.KidIcarus.agent.other.kidicarusdoor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;

class KidIcarusDoorSpriteFrameInput extends SpriteFrameInput {
	boolean isOpened;

	KidIcarusDoorSpriteFrameInput(Vector2 position, boolean isOpened) {
		super();
		this.position = position;
		this.isOpened = isOpened;
	}
}
