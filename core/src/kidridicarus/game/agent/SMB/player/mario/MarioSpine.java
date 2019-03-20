package kidridicarus.game.agent.SMB.player.mario;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.despawnbox.DespawnBox;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentsensor.AgentContactBeginSensor;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.collision.CollisionTiledMapAgent;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agent.SMB.TileBumpTakeAgent;
import kidridicarus.game.agent.SMB.other.bumptile.BumpTile.TileBumpStrength;
import kidridicarus.game.agent.SMB.other.pipewarp.PipeWarp;
import kidridicarus.game.agentspine.SMB.PlayerSpine;

/*
 * A "control center" for the body, to apply move impulses, etc. in an organized manner.
 * This class has multiple functions:
 *   1) Interpret the World through contacts.
 *   2) Relay information to the body from the World, with filtering/organization.
 *   3) Take blocks of information and translate into body impulses, and apply those body impulses
 *     (e.g. move up, move left, apply jump).
 */
public class MarioSpine extends PlayerSpine {
	private static final float WALKMOVE_XIMP = 0.025f;
	private static final float MIN_WALKVEL = WALKMOVE_XIMP * 2f;
	private static final float MAX_WALKVEL = WALKMOVE_XIMP * 42f;
	private static final float DECEL_XIMP = WALKMOVE_XIMP * 1.37f;
	private static final float BRAKE_XIMP = WALKMOVE_XIMP * 2.75f;
	private static final float RUNMOVE_XIMP = WALKMOVE_XIMP * 1.5f;
	private static final float MAX_RUNVEL = MAX_WALKVEL * 1.65f;
	private static final float RUNJUMP_MULT = 0.25f;
	private static final float MAX_RUNJUMPVEL = MAX_RUNVEL;
	private static final float JUMP_IMPULSE = 1.75f;
	private static final float JUMP_FORCE = 25.25f;
	private static final float JUMPFORCE_MAXTIME = 0.5f;
	private static final float MAX_FALL_VELOCITY = UInfo.P2M(5f * 60f);
	private static final float AIRMOVE_XIMP = WALKMOVE_XIMP;
	private static final float MAX_DUCKSLIDE_VEL = MAX_WALKVEL * 0.65f;
	private static final float DUCKSLIDE_XIMP = WALKMOVE_XIMP * 1f;
	// TODO: test this with different values to the best
	private static final float MIN_HEADBANG_VEL = 0.01f;
	private static final float HEADBOUNCE_VEL = 2.8f;	// up velocity

	private AgentContactHoldSensor agentSensor;
	private AgentContactHoldSensor tileBumpPushSensor;
	private AgentContactBeginSensor damagePushSensor; 
	private AgentContactHoldSensor pipeWarpSensor;

	public MarioSpine(MarioBody body) {
		super(body);
		agentSensor = null;
		tileBumpPushSensor = null;
		damagePushSensor = null;
		pipeWarpSensor = null;
	}

	// main sensor for detecting general agent contacts and damage push begin contacts
	public AgentContactHoldSensor createMainSensor() {
		agentSensor = new AgentContactHoldSensor(body);
		damagePushSensor = new AgentContactBeginSensor(body);
		agentSensor.chainTo(damagePushSensor);
		return agentSensor;
	}

	public AgentContactHoldSensor createTileBumpPushSensor() {
		tileBumpPushSensor = new AgentContactHoldSensor(body);
		return tileBumpPushSensor;
	}

	public AgentContactHoldSensor createPipeWarpSensor() {
		pipeWarpSensor = new AgentContactHoldSensor(body);
		return pipeWarpSensor;
	}

	/*
	 * Apply walk impulse and cap horizontal velocity.
	 */
	public void applyWalkMove(boolean moveRight, boolean run) {
		// run impulse or walk impulse?
		float impulse = run ? RUNMOVE_XIMP : WALKMOVE_XIMP;
		// max run velocity or max walk velocity?
		float max = run ? MAX_RUNVEL : MAX_WALKVEL;
		applyHorizImpulseAndCapVel(moveRight, impulse, max);
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
			// if decelerated too hard to the left then set x velocity to zero
			if(body.getVelocity().x <= UInfo.VEL_EPSILON)
				body.setVelocity(0f, body.getVelocity().y);
		}
		// if moving left...
		else if(body.getVelocity().x < -MIN_WALKVEL) {
			// ... and facing left or ducking, then decelerate with soft impulse to the right
			if(!facingRight && !ducking)
				applyHorizontalImpulse(false, -DECEL_XIMP);
			// ... and facing right, then decelerate with hard impulse to the right
			else
				applyHorizontalImpulse(false, -BRAKE_XIMP);
			// if decelerated too hard to the right then set x velocity to zero
			if(body.getVelocity().x >= -UInfo.VEL_EPSILON)
				body.setVelocity(0f, body.getVelocity().y);
		}
		// not moving right or left fast enough, set horizontal velocity to zero to avoid wobbling
		else
			body.setVelocity(0f, body.getVelocity().y);
	}

	public void applyJumpImpulse() {
		// the faster mario is moving, the higher he jumps, up to a max
		float mult = Math.abs(body.getVelocity().x) / MAX_RUNJUMPVEL;
		// cap the multiplier
		if(mult > 1f)
			mult = 1f;
		// 
		mult = 1f + mult * RUNJUMP_MULT;

		body.applyImpulse(new Vector2 (0f, JUMP_IMPULSE * mult));
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
		applyHorizImpulseAndCapVel(moveRight, AIRMOVE_XIMP, MAX_RUNVEL);
	}

	public void applyDuckSlideMove(boolean moveRight) {
		applyHorizImpulseAndCapVel(moveRight, DUCKSLIDE_XIMP, MAX_DUCKSLIDE_VEL);
	}

	public void applyHeadBounceMove() {
		body.setVelocity(body.getVelocity().x, 0f);
		body.applyImpulse(new Vector2(0f, HEADBOUNCE_VEL));
	}

	/*
	 * If moving up fast enough, then check tiles currently contacting head for closest tile to take a bump.
	 * Tile bump is applied if needed.
	 * Returns true if tile bump is applied. Otherwise returns false.
	 */
	public boolean checkDoHeadBump(TileBumpStrength bumpStrength) {
		// exit if not moving up fast enough in this frame or previous frame
		if(body.getVelocity().y < MIN_HEADBANG_VEL || ((MarioBody) body).getPrevVelocity().y < MIN_HEADBANG_VEL)
			return false;
		// create list of bumptiles, in order from closest to mario to farthest from mario
		TreeSet<TileBumpTakeAgent> closestTilesList = 
				new TreeSet<TileBumpTakeAgent>(new Comparator<TileBumpTakeAgent>() {
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
		for(TileBumpTakeAgent bumpTile : tileBumpPushSensor.getContactsByClass(TileBumpTakeAgent.class))
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
		Vector2 myPrevPosition = ((MarioBody) body).getPrevPosition();
		float otherCenterY = otherBounds.y+otherBounds.height/2f;
		if(myBounds.y >= otherCenterY || myPrevPosition.y-myBounds.height/2f >= otherCenterY)
			return true;
		return false;
	}

	public boolean isBraking(boolean facingRight) {
		if(facingRight && body.getVelocity().x < -MIN_WALKVEL)
			return true;
		else if(!facingRight && body.getVelocity().x > MIN_WALKVEL)
			return true;
		return false;
	}

	public List<Agent> getPushDamageContacts() {
		return damagePushSensor.getAndResetContacts();
	}

	public PipeWarp getEnterPipeWarp(Direction4 moveDir) {
		if(moveDir == null)
			return null;
		for(PipeWarp pw : pipeWarpSensor.getContactsByClass(PipeWarp.class)) {
			if(pw.canBodyEnterPipe(body.getBounds(), moveDir))
				return (PipeWarp) pw;
		}
		return null;
	}

	public boolean isMapTileSolid(Vector2 tileCoords) {
		CollisionTiledMapAgent ctMap = agentSensor.getFirstContactByClass(CollisionTiledMapAgent.class);
		return ctMap == null ? false : ctMap.isMapTileSolid(tileCoords); 
	}

	public RoomBox getCurrentRoom() {
		return (RoomBox) agentSensor.getFirstContactByClass(RoomBox.class);
	}

	public boolean isContactDespawn() {
		return agentSensor.getFirstContactByClass(DespawnBox.class) != null;
	}

	public void capFallVelocity() {
		capFallVelocity(MAX_FALL_VELOCITY);
	}

	public boolean isStandingStill() {
		return isStandingStill(MIN_WALKVEL);
	}
}
