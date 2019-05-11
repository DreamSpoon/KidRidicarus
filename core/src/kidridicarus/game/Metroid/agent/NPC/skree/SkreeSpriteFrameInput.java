package kidridicarus.game.Metroid.agent.NPC.skree;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.game.Metroid.agent.NPC.skree.SkreeBrain.MoveState;

class SkreeSpriteFrameInput extends SpriteFrameInput {
	MoveState moveState;

	SkreeSpriteFrameInput(Vector2 position, FrameTime frameTime, MoveState moveState) {
		super(frameTime, false, false, 0f, position);
		this.moveState = moveState;
	}
}
