package kidridicarus.game.Metroid.agent.NPC.skree;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.game.Metroid.agent.NPC.skree.SkreeBrain.MoveState;

public class SkreeSpriteFrameInput extends SpriteFrameInput {
	public MoveState moveState;

	public SkreeSpriteFrameInput(Vector2 position, float timeDelta, MoveState moveState) {
		super(false, timeDelta, false, false, 0f, position);
		this.moveState = moveState;
	}
}
