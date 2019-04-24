package kidridicarus.game.Metroid.agent.NPC.skree;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.game.Metroid.agent.NPC.skree.SkreeBrain.MoveState;

public class SkreeSpriteFrameInput extends AnimSpriteFrameInput {
	public MoveState moveState;

	public SkreeSpriteFrameInput(boolean visible, Vector2 position, boolean flipX, float timeDelta,
			MoveState moveState) {
		super(visible, position, flipX, timeDelta);
		this.moveState = moveState;
	}
}
