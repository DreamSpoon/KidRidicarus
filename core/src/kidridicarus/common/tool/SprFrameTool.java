package kidridicarus.common.tool;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;

/*
 * Factory with convenience methods for SpriteFrameInput creation.
 * This class, hopefully, creates a clean separation from Agency.AgentSprite - so that isFacingRight can be used
 * instead of flipX. Prevent confusion via !flipX and flipX and facingRight and !facingRight.
 * Also, more method name options are available since the name of the method is not limited by being a constructor.   
 */
public class SprFrameTool {
	public static SpriteFrameInput place(Vector2 position) {
		return new SpriteFrameInput(false, 0f, false, false, 0f, position);
	}

	public static SpriteFrameInput placeFaceR(Vector2 position, boolean isFacingRight) {
		return new SpriteFrameInput(false, 0f, !isFacingRight, false, 0f, position);
	}

	public static SpriteFrameInput placeAnimFaceR(Vector2 position, float time, boolean isFacingRight) {
		return new SpriteFrameInput(false, time, !isFacingRight, false, 0f, position);
	}

	public static SpriteFrameInput placeAnim(Vector2 position, float time) {
		return new SpriteFrameInput(false, time, false, false, 0f, position);
	}
}
