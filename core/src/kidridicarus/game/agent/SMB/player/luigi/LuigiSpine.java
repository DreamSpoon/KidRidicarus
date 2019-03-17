package kidridicarus.game.agent.SMB.player.luigi;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.despawnbox.DespawnBox;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.OnGroundSpine;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.collision.CollisionTiledMapAgent;
import kidridicarus.game.agent.SMB.TileBumpTakeAgent;
import kidridicarus.game.agent.SMB.other.bumptile.BumpTile.TileBumpStrength;
import kidridicarus.game.agent.SMB.player.mario.MarioBody;

/*
 * A "control center" for the body, to apply move impulses, etc. in an organized manner.
 * This class has multiple functions:
 *   1) Interpret the World through contacts.
 *   2) Relay information to the body from the World, with filtering/organization.
 *   3) Take blocks of information and translate into body impulses, and apply those body impulses
 *     (e.g. move up, move left, apply jump).
 */
public class LuigiSpine extends OnGroundSpine {
	private static final float WALKMOVE_XIMP = 0.025f;
	private static final float MAX_STAND_VEL = LuigiSpine.WALKMOVE_XIMP * 0.01f;
	private static final float MIN_WALKVEL = WALKMOVE_XIMP * 2f;
	private static final float MAX_WALKVEL = WALKMOVE_XIMP * 42f;
	private static final float DECEL_XIMP = WALKMOVE_XIMP * 1.37f;
	private static final float BRAKE_XIMP = WALKMOVE_XIMP * 2.75f;
	private static final float RUNMOVE_XIMP = WALKMOVE_XIMP * 1.5f;
	private static final float MAX_RUNVEL = MAX_WALKVEL * 1.65f;
	private static final float JUMP_IMPULSE = 1.75f;
	private static final float JUMP_FORCE = 25.25f;
	private static final float JUMPFORCE_MAXTIME = 0.5f;
	private static final float MAX_FALL_VELOCITY = UInfo.P2M(5f * 60f);
	private static final float AIRMOVE_XIMP = WALKMOVE_XIMP;
	private static final float MAX_DUCKSLIDE_VEL = MAX_WALKVEL * 0.65f;
	private static final float DUCKSLIDE_XIMP = WALKMOVE_XIMP * 1f;
	// TODO: test this with different values to the best
	private static final float MIN_HEADBANG_VEL = 0.01f;
	private static final float MARIO_HEADBOUNCE_VEL = 2.8f;	// up velocity

	private LuigiBody body;
	private AgentContactHoldSensor acSensor;
	private AgentContactHoldSensor btSensor;

	public LuigiSpine(LuigiBody body) {
		this.body = body;
		acSensor = null;
		btSensor = null;
	}

	public AgentContactHoldSensor createAgentSensor() {
		acSensor = new AgentContactHoldSensor(body);
		return acSensor;
	}

	public AgentContactHoldSensor createBumpTileSensor() {
		btSensor = new AgentContactHoldSensor(body);
		return btSensor;
	}

	/*
	 * Apply walk impulse and cap horizontal velocity.
	 */
	public void applyWalkMove(boolean moveRight, boolean run) {
		// run impulse or walk impulse?
		float impulse = run ? RUNMOVE_XIMP : WALKMOVE_XIMP;
		applyHorizontalImpulse(moveRight, impulse);
		// max run velocity or max walk velocity?
		float max = run ? MAX_RUNVEL : MAX_WALKVEL;
		capHorizontalVelocity(max);
	}

	/*
	 * Apply decel impulse and check for min velocity.
	 */
	public void applyDecelMove(boolean facingRight, boolean ducking) {
		// if moving right...
		if(body.getVelocity().x > MIN_WALKVEL) {
			// ... and facing right or ducking, then decelerate with soft impulse to the left
			if(facingRight && !ducking)
				applyHorizontalImpulse(true, -DECEL_XIMP);
			// ... and facing left, then decelerate with hard impulse to the left
			else
				applyHorizontalImpulse(true, -BRAKE_XIMP);
		}
		// if moving left...
		else if(body.getVelocity().x < -MIN_WALKVEL) {
			// ... and facing left or ducking, then decelerate with soft impulse to the right
			if(!facingRight && !ducking)
				applyHorizontalImpulse(false, -DECEL_XIMP);
			// ... and facing right, then decelerate with hard impulse to the right
			else
				applyHorizontalImpulse(false, -BRAKE_XIMP);
		}
		// not moving right or left fast enough, set horizontal velocity to zero to avoid wobbling
		else
			body.setVelocity(0f, body.getVelocity().y);
	}

	private void applyHorizontalImpulse(boolean moveRight, float amt) {
		if(moveRight)
			body.applyBodyImpulse(new Vector2(amt, 0f));
		else
			body.applyBodyImpulse(new Vector2(-amt, 0f));
	}

	/*
	 * Ensure horizontal velocity is within -max to +max.
	 */
	private void capHorizontalVelocity(float max) {
		if(body.getVelocity().x > max)
			body.setVelocity(max, body.getVelocity().y);
		else if(body.getVelocity().x < -max)
			body.setVelocity(-max, body.getVelocity().y);
	}

	private static final float MARIO_RUNJUMP_MULT = 0.25f;
	private static final float MARIO_MAX_RUNJUMPVEL = MarioBody.MARIO_MAX_RUNVEL;
	public void applyJumpImpulse() {
		// the faster mario is moving, the higher he jumps, up to a max
		float mult = Math.abs(body.getVelocity().x) / MARIO_MAX_RUNJUMPVEL;
		// cap the multiplier
		if(mult > 1f)
			mult = 1f;
		// 
		mult = 1f + mult * MARIO_RUNJUMP_MULT;

		body.applyBodyImpulse(new Vector2 (0f, JUMP_IMPULSE * mult));
	}

	public void applyJumpForce(float jumpForceTimer) {
		float t = jumpForceTimer;
		if(t < 0)
			t = 0;
		else if(t > JUMPFORCE_MAXTIME)
			t = JUMPFORCE_MAXTIME;
		body.applyForce(new Vector2(0, JUMP_FORCE * (JUMPFORCE_MAXTIME - t) / JUMPFORCE_MAXTIME));
	}

	public void applyAirMove(boolean moveRight) {
		applyHorizontalImpulse(moveRight, AIRMOVE_XIMP);
		capHorizontalVelocity(MAX_RUNVEL);
	}

	public void applyHeadBounceMove() {
		body.setVelocity(body.getVelocity().x, 0f);
		body.applyBodyImpulse(new Vector2(0f, MARIO_HEADBOUNCE_VEL));
	}

	public void applyDuckSlideMove(boolean isDuckSlideRight) {
		applyHorizontalImpulse(isDuckSlideRight, DUCKSLIDE_XIMP);
		capHorizontalVelocity(MAX_DUCKSLIDE_VEL);
	}

	public void capFallVelocity() {
		if(body.getVelocity().y < -MAX_FALL_VELOCITY)
			body.setVelocity(body.getVelocity().x, -MAX_FALL_VELOCITY);
	}

	/*
	 * If moving up fast enough, then check tiles currently contacting head for closest tile to take a bump.
	 * Tile bump is applied if needed.
	 * Returns true if tile bump is applied. Otherwise returns false.
	 */
	public boolean checkDoHeadBump(TileBumpStrength bumpStrength) {
		// exit if not moving up fast enough in this frame or previous frame
		if(body.getVelocity().y < MIN_HEADBANG_VEL || body.getPrevVelocity().y < MIN_HEADBANG_VEL)
			return false;
		// create list of bumptiles, in order from closest to luigi to farthest from luigi
		TreeSet<TileBumpTakeAgent> closestTilesList = new TreeSet<TileBumpTakeAgent>(new Comparator<TileBumpTakeAgent>() {
				@Override
				public int compare(TileBumpTakeAgent o1, TileBumpTakeAgent o2) {
					float dist1 = Math.abs(((Agent) o1).getPosition().x - body.getPosition().x);
					float dist2 = Math.abs(((Agent) o2).getPosition().x - body.getPosition().x);
					if(dist1 < dist2)
						return -1;
					else if(dist1 > dist2)
						return 1;
					return 0;
				}
			});
		for(TileBumpTakeAgent bumpTile : btSensor.getContactsByClass(TileBumpTakeAgent.class))
			closestTilesList.add(bumpTile);

		// iterate through sorted list of bump tiles, exiting upon successful bump
		Iterator<TileBumpTakeAgent> tileIter = closestTilesList.iterator();
		while(tileIter.hasNext()) {
			TileBumpTakeAgent bumpTile = tileIter.next();
			// did the tile "take" the bump?
			if(bumpTile.onTakeTileBump(body.getParent(), bumpStrength))
				return true;
		}

		// no head bumps
		return false;
	}

	public boolean isGiveHeadBounceAllowed(Rectangle otherBounds) {
		// check bounds
		Rectangle myBounds = body.getBounds();
		Vector2 myPrevPosition = body.getPrevPosition();
		float otherCenterY = otherBounds.y+otherBounds.height/2f;
		if(myBounds.y >= otherCenterY || myPrevPosition.y-myBounds.height/2f >= otherCenterY)
			return true;
		return false;
	}

	public boolean isStandingStill() {
		return (body.getVelocity().x >= -MAX_STAND_VEL && body.getVelocity().x <= MAX_STAND_VEL);
	}

	public boolean isBraking(boolean facingRight) {
		if(facingRight && body.getVelocity().x < -MIN_WALKVEL)
			return true;
		else if(!facingRight && body.getVelocity().x > MIN_WALKVEL)
			return true;
		return false;
	}

	public boolean isMovingUp() {
		return body.getVelocity().y > UInfo.VEL_EPSILON;
	}

	public boolean isMovingDown() {
		return body.getVelocity().y < -UInfo.VEL_EPSILON;
	}

	public boolean isMovingRight() {
		return body.getVelocity().x > MIN_WALKVEL;
	}

	public boolean isMovingLeft() {
		return body.getVelocity().x < MIN_WALKVEL;
	}

	public CollisionTiledMapAgent getCollisionTiledMap() {
		return acSensor.getFirstContactByClass(CollisionTiledMapAgent.class);
	}

	public RoomBox getCurrentRoom() {
		return (RoomBox) acSensor.getFirstContactByClass(RoomBox.class);
	}

	public boolean isContactDespawn() {
		return acSensor.getFirstContactByClass(DespawnBox.class) != null;
	}
}
