package kidridicarus.game.SMB.agentbody.player;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.agency.tool.Direction4;
import kidridicarus.common.agent.general.DespawnBox;
import kidridicarus.common.agent.general.Room;
import kidridicarus.common.agent.general.PipeWarp;
import kidridicarus.common.agent.optional.ContactDmgGiveAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.PowerupGiveAgent;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentbody.sensor.AgentContactBeginSensor;
import kidridicarus.common.agentbody.sensor.AgentContactSensor;
import kidridicarus.common.agentbody.sensor.OnGroundSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.game.SMB.agent.TileBumpTakeAgent;
import kidridicarus.game.SMB.agent.HeadBounceTakeAgent;
import kidridicarus.game.SMB.agent.other.Flagpole;
import kidridicarus.game.SMB.agent.other.LevelEndTrigger;
import kidridicarus.game.SMB.agent.player.Mario;
import kidridicarus.game.SMB.agent.player.Mario.MarioPowerState;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.play.GameAdvice;

/*
 * Major TODO: Move a lot of the code out of this class and push it somewhere else like Mario class. There is
 * so much stuff here that should NOT be in the agent body class.
 */
public class MarioBody extends MobileAgentBody {
	private static final Vector2 BIG_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(26f));
	private static final Vector2 SML_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(12f));

	private static final CFBitSeq GROUND_AND_PIPE_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq GROUND_AND_PIPE_SENSOR_CFMASK =
			new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT, CommonCF.Alias.PIPE_BIT);

	private static final CFBitSeq SIDE_PIPE_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq SIDE_PIPE_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.PIPE_BIT);

	private static final CFBitSeq BUMPTILE_AND_PIPE_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq BUMPTILE_AND_PIPE_SENSOR_CFMASK =
			new CFBitSeq(CommonCF.Alias.BUMPABLE_BIT, CommonCF.Alias.PIPE_BIT);

	private static final CFBitSeq AGENT_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AGENT_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.ROOM_BIT,
			CommonCF.Alias.ITEM_BIT, CommonCF.Alias.DESPAWN_BIT);

	private static final float MARIO_WALKMOVE_XIMP = 0.025f;
	private static final float MARIO_MIN_WALKSPEED = MARIO_WALKMOVE_XIMP * 2;
	private static final float MARIO_RUNMOVE_XIMP = MARIO_WALKMOVE_XIMP * 1.5f;
	private static final float DECEL_XIMP = MARIO_WALKMOVE_XIMP * 1.37f;
	private static final float MARIO_BRAKE_XIMP = MARIO_WALKMOVE_XIMP * 2.75f;
	private static final float MARIO_BRAKE_TIME = 0.2f;
	private static final float MARIO_MAX_WALKVEL = MARIO_WALKMOVE_XIMP * 42f;
	private static final float MARIO_MAX_RUNVEL = MARIO_MAX_WALKVEL * 1.65f;
	private static final float MARIO_MAX_DUCKSLIDE_VEL = MARIO_MAX_WALKVEL * 0.65f;
	private static final float MARIO_DUCKSLIDE_XIMP = MARIO_WALKMOVE_XIMP * 1f;
	private static final float MARIO_JUMP_IMPULSE = 1.75f;
	private static final float MARIO_JUMP_FORCE = 14f;
	private static final float MARIO_AIRMOVE_XIMP = 0.04f;
	private static final float MARIO_RUNJUMP_MULT = 0.25f;
	private static final float MARIO_MAX_RUNJUMPVEL = MARIO_MAX_RUNVEL;
	private static final float MARIO_JUMP_GROUNDCHECK_DELAY = 0.05f;
	private static final float MARIO_JUMPFORCE_TIME = 0.5f;
	private static final float MARIO_HEADBOUNCE_VEL = 1.75f;	// up velocity
	private static final float MIN_HEADBANG_VEL = 0.01f;	// TODO: test this with different values to the best

	public enum MarioBodyState { STAND, WALKRUN, BRAKE, JUMP, FALL, DUCK, DEAD }

	private Mario parent;
	private Agency agency;

	private boolean isFacingRight;
	private boolean isBig;
	private boolean isBrakeAvailable;
	private float brakeTimer;
	private boolean isNewJumpAllowed;
	private float jumpGroundCheckTimer;
	private boolean isJumping;
	private float jumpForceTimer;
	private boolean isDucking;
	private boolean isDuckSliding;
	private boolean isLastVelocityRight;
	private boolean isDuckSlideRight;
	private boolean isTakeDamage;
	private boolean canHeadBang;
	private Vector2 prevVelocity;
	private Vector2 prevPosition;

	private PipeWarp pipeToEnter;
	private Flagpole flagpoleContacted;
	private LevelEndTrigger levelendContacted;

	private OnGroundSensor ogSensor;
	private AgentContactSensor wpSensor;
	private AgentContactSensor btSensor;
	private AgentContactSensor acSensor;
	private AgentContactBeginSensor acBeginSensor;
	private Fixture acSensorFixture;

	private MarioBodyState curState;
	private float stateTimer;

	public MarioBody(Mario parent, Agency agency, Vector2 position, boolean isFacingRight, boolean isBig) {
		this.parent = parent;
		this.agency = agency;

		this.isFacingRight = isFacingRight;
		this.isBig = isBig;
		isBrakeAvailable = true;
		brakeTimer = 0f;
		isNewJumpAllowed = false;
		jumpGroundCheckTimer = 0f;
		isJumping = false;
		jumpForceTimer = 0f;
		isDucking = false;
		isLastVelocityRight = false;
		isDuckSliding = false;
		isDuckSlideRight = false;
		isTakeDamage = false;
		canHeadBang = true;

		pipeToEnter = null;
		flagpoleContacted = null;
		levelendContacted = null;

		curState = MarioBodyState.STAND;
		stateTimer = 0f;

		// physic
		prevVelocity = new Vector2(0f, 0f);
		prevPosition = position.cpy();
		defineBody(position, prevVelocity);
	}

	private void defineBody(Vector2 position, Vector2 velocity) {
		createBody(position, velocity);

		// the warp pipe sensor is chained to other sensors, so create it here
		wpSensor = new AgentContactSensor(this);
		createGroundAndPipeSensor();
		createSidePipeSensors();
		createBumpTileAndPipeSensor();

		// warp pipe sensor is not chained to this sensor
		createAgentSensor();
	}

	private void createBody(Vector2 position, Vector2 velocity) {
		if(b2body != null)
			agency.getWorld().destroyBody(b2body);

		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		FixtureDef fdef = new FixtureDef();
		fdef.friction = 0.01f;	// (default is 0.2f)
		Vector2 size = getBodySize();
		b2body = B2DFactory.makeSpecialBoxBody(agency.getWorld(), bdef, fdef, null, CommonCF.SOLID_BODY_CFCAT,
				CommonCF.SOLID_BODY_CFMASK, size.x, size.y);
	}

	// "bottom" sensors
	private void createGroundAndPipeSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();

		// foot sensor for detecting onGround and warp pipes
		if(!isBig || isDucking)
			boxShape.setAsBox(UInfo.P2M(5f), UInfo.P2M(2f), new Vector2(0f, UInfo.P2M(-6)), 0f);
		else
			boxShape.setAsBox(UInfo.P2M(5f), UInfo.P2M(2f), new Vector2(0f, UInfo.P2M(-16)), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		ogSensor = new OnGroundSensor(null);
		// the og sensor chains to the wp sensor, because the wp sensor will be attached to other fixtures
		ogSensor.chainTo(wpSensor);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(GROUND_AND_PIPE_SENSOR_CFCAT,
				GROUND_AND_PIPE_SENSOR_CFMASK, ogSensor));
	}

	private void createSidePipeSensors() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();

		// right side sensor for detecting warp pipes
		boxShape.setAsBox(UInfo.P2M(1f), UInfo.P2M(5f), UInfo.P2MVector(7, 0), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(SIDE_PIPE_SENSOR_CFCAT,
				SIDE_PIPE_SENSOR_CFMASK, wpSensor));

		// left side sensor for detecting warp pipes
		boxShape.setAsBox(UInfo.P2M(1f), UInfo.P2M(5f), UInfo.P2MVector(-7, 0), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(SIDE_PIPE_SENSOR_CFCAT,
				SIDE_PIPE_SENSOR_CFMASK, wpSensor));
	}

	// "top" sensors
	private void createBumpTileAndPipeSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape sensorShape = new PolygonShape();

		// head sensor for detecting head banging behavior
		if(!isBig || isDucking)
			sensorShape.setAsBox(UInfo.P2M(5f), UInfo.P2M(1f), new Vector2(UInfo.P2M(0f), UInfo.P2M(8f)), 0f);
		else
			sensorShape.setAsBox(UInfo.P2M(5f), UInfo.P2M(1f), new Vector2(UInfo.P2M(0f), UInfo.P2M(16f)), 0f);
		fdef.shape = sensorShape;
		fdef.isSensor = true;
		btSensor = new AgentContactSensor(this);
		btSensor.chainTo(wpSensor);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(BUMPTILE_AND_PIPE_SENSOR_CFCAT,
				BUMPTILE_AND_PIPE_SENSOR_CFMASK, btSensor));
	}

	private void createAgentSensor() {
		PolygonShape boxShape = new PolygonShape();
		Vector2 bs = getBodySize();
		boxShape.setAsBox(bs.x/2f, bs.y/2f);
		FixtureDef fdef = new FixtureDef();
		fdef.shape = boxShape;
		fdef.isSensor = true;
		acSensor = new AgentContactSensor(this);
		acBeginSensor = new AgentContactBeginSensor(this);
		acBeginSensor.chainTo(acSensor);
		acSensorFixture = b2body.createFixture(fdef);
		acSensorFixture.setUserData(new AgentBodyFilter(AGENT_SENSOR_CFCAT, AGENT_SENSOR_CFMASK, acBeginSensor));
	}

	public MarioBodyState update(float delta, GameAdvice advice, MarioPowerState curPowerState) {
		MarioBodyState nextState;
		boolean isVelocityLeft, isVelocityRight;
		boolean doDuckSlideMove;
		boolean doWalkRunMove;
		boolean doDecelMove;
		boolean doBrakeMove;

		processPipes(advice);
		processOtherContacts();

		nextState = MarioBodyState.STAND;
		isVelocityRight = b2body.getLinearVelocity().x > MARIO_MIN_WALKSPEED;
		isVelocityLeft = b2body.getLinearVelocity().x < -MARIO_MIN_WALKSPEED;

		// If mario's velocity is below min walking speed while on ground and he is not duck sliding then
		// zero his velocity
		if(ogSensor.isOnGround() && !isDuckSliding && !isVelocityRight && !isVelocityLeft &&
				!advice.moveRight && !advice.moveLeft)
			b2body.setLinearVelocity(0f, b2body.getLinearVelocity().y);

		// multiple concurrent body impulses may be necessary
		doDuckSlideMove = false;
		doWalkRunMove = false;
		doDecelMove = false;
		doBrakeMove = false;

		// make a note of the last direction in which mario was moving, for duck sliding
		if(isVelocityRight)
			isLastVelocityRight = true;
		else if(isVelocityLeft)
			isLastVelocityRight = false;

		// eligible for duck/unduck?
		if(curPowerState != MarioPowerState.SMALL && ogSensor.isOnGround()) {
			Vector2 bodyTilePos = UInfo.getM2PTileForPos(b2body.getPosition());

			// first time duck check
			if(advice.moveDown && !isDucking) {
				// quack
				isDucking = true;
				if(isDuckSliding)
					isDuckSliding = false;
				else {
					// mario's body's height is reduced when ducking, so recreate the body in a slightly lower pos
					defineBody(b2body.getPosition().cpy().sub(0f, UInfo.P2M(8f)), b2body.getLinearVelocity());
				}
			}
			// first time unduck check
			else if(!advice.moveDown && isDucking) {
				isDucking = false;

				// Check the space above and around mario to test if mario can unduck normally, or if he is in a
				// tight spot

				// if the tile above ducking mario is solid ...
				if(agency.isMapTileSolid(bodyTilePos.cpy().add(0, 1))) {
					Vector2 subTilePos = UInfo.getSubTileCoordsForMPos(b2body.getPosition());
					// If the player's last velocity direction was rightward, and their position is in the left half
					// of the tile, and the tile above and to the left of them is solid, then the player should
					// duckslide right.
					if((isLastVelocityRight && subTilePos.x <= 0.5f && agency.isMapTileSolid(bodyTilePos.cpy().add(-1, 1))) ||
							(subTilePos.x > 0.5f && !agency.isMapTileSolid(bodyTilePos.cpy().add(1, 1))) ||
							(isLastVelocityRight && subTilePos.x > 0.5f && agency.isMapTileSolid(bodyTilePos.cpy().add(1, 1)))) {
						isDuckSlideRight = true;
					}
					// the only other option is to duckslide left
					else
						isDuckSlideRight = false;

					// tile above is solid so must be ducksliding
					isDuckSliding = true;
				}
				else
					defineBody(b2body.getPosition().cpy().add(0f, UInfo.P2M(8f)), b2body.getLinearVelocity());
			}

			if(isDuckSliding) {
				// if the player was duck sliding but the space above them is now nonsolid then end duckslide
				if(!agency.isMapTileSolid(bodyTilePos.cpy().add(0, 1))) {
					isDuckSliding = false;
					defineBody(b2body.getPosition().cpy().add(0f, UInfo.P2M(8f)), b2body.getLinearVelocity());
				}
				else
					doDuckSlideMove = true;
			}
		}

		// want to move left or right? (but not both! because they would cancel each other)
		if((advice.moveRight && !advice.moveLeft) || (!advice.moveRight && advice.moveLeft)) {
			doWalkRunMove = true;

			// mario can change facing direction, but not while airborne
			if(ogSensor.isOnGround()) {
				// brake becomes available again when facing direction changes
				if(isFacingRight != advice.moveRight) {
					isBrakeAvailable = true;
					brakeTimer = 0f;
				}

				// can't run/walk on ground while ducking, only slide
				if(isDucking) {
					doWalkRunMove = false;
					doDecelMove = true;
				}
				else	// update facing direction
					isFacingRight = advice.moveRight;
			}
		}
		// decelerate if on ground and not wanting to move left or right
		else if(ogSensor.isOnGround() && (isVelocityRight || isVelocityLeft))
			doDecelMove = true;

		// check for brake application
		if(!isDucking && ogSensor.isOnGround() && isBrakeAvailable &&
				((isFacingRight && isVelocityLeft) || (!isFacingRight && isVelocityRight))) {
			isBrakeAvailable = false;
			brakeTimer = MARIO_BRAKE_TIME;
		}
		// this catches brake applications from this update() call and previous update() calls
		if(brakeTimer > 0f) {
			doBrakeMove = true;
			brakeTimer -= delta;
		}

		// apply impulses if necessary
		if(doDuckSlideMove) {
			duckSlideLeftRight(isDuckSlideRight);
		}
		else if(doBrakeMove) {
			brakeLeftRight(isFacingRight);
			nextState = MarioBodyState.BRAKE;
		}
		else if(doWalkRunMove) {
			moveBodyLeftRight(advice.moveRight, advice.runShoot);
			nextState = MarioBodyState.WALKRUN;
		}
		else if(doDecelMove) {
			decelLeftRight();
			nextState = MarioBodyState.WALKRUN;
		}

		// Do not check mario's "on ground" state for a short time after mario jumps, because his foot sensor
		// might still be contacting the ground even after his body enters the air.
		if(jumpGroundCheckTimer > delta)
			jumpGroundCheckTimer -= delta;
		else {
			jumpGroundCheckTimer = 0f;
			// The player can jump once per press of the jump key, so let them jump again when they release the
			// button but, they need to be on the ground with the button released.
			if(ogSensor.isOnGround()) {
				isJumping = false;
				if(!advice.jump)
					isNewJumpAllowed = true;
			}
		}

		// jump?
		if(advice.jump && isNewJumpAllowed) {	// do jump
			isNewJumpAllowed = false;
			isJumping = true;
			// start a timer to delay checking for onGround state
			jumpGroundCheckTimer = MARIO_JUMP_GROUNDCHECK_DELAY;
			nextState = MarioBodyState.JUMP;

			// the faster mario is moving, the higher he jumps, up to a max
			float mult = Math.abs(b2body.getLinearVelocity().x) / MARIO_MAX_RUNJUMPVEL;
			// cap the multiplier
			if(mult > 1f)
				mult = 1f;

			mult *= MARIO_RUNJUMP_MULT;
			mult += 1f;

			// apply initial (and only) jump impulse
			moveBodyY(MARIO_JUMP_IMPULSE * mult);
			// the remainder of the jump up velocity is achieved through mid-air up-force
			jumpForceTimer = MARIO_JUMPFORCE_TIME;
			if(curPowerState != MarioPowerState.SMALL)
				agency.playSound(AudioInfo.Sound.SMB.MARIO_BIGJUMP);
			else
				agency.playSound(AudioInfo.Sound.SMB.MARIO_SMLJUMP);
		}
		else if(isJumping) {	// jumped and is mid-air
			nextState = MarioBodyState.JUMP;
			// jump force stops, and cannot be restarted, if the player releases the jump key
			if(!advice.jump)
				jumpForceTimer = 0f;
			// The longer the player holds the jump key, the higher they go,
			// if mario is moving up (no jump force allowed while mario is moving down)
			// TODO: what if mario is initally moving down because he jumped from an elevator?
			else if(b2body.getLinearVelocity().y > 0f && jumpForceTimer > 0f) {
				jumpForceTimer -= delta;
				// the force was strong to begin and tapered off over time - some said it became irrelevant
				applyForce(new Vector2(0, MARIO_JUMP_FORCE * jumpForceTimer / MARIO_JUMPFORCE_TIME));
			}
		}
		// finally, if mario is not on the ground (for reals) then he is falling since he is not jumping
		else if(!ogSensor.isOnGround() && jumpGroundCheckTimer <= 0f) {
			// cannot jump while falling
			isNewJumpAllowed = false;
			nextState = MarioBodyState.FALL;
		}

		if(isDucking)
			nextState = MarioBodyState.DUCK;

		stateTimer = nextState == curState ? stateTimer + delta : 0f;
		curState = nextState;
		prevVelocity = b2body.getLinearVelocity().cpy();
		prevPosition = b2body.getPosition().cpy();

		return nextState;
	}

	private void processPipes(GameAdvice advice) {
		// check for pipe entry 
		Direction4 dir;
		if(advice.moveRight)
			dir = Direction4.RIGHT;
		else if(advice.moveUp)
			dir = Direction4.UP;
		else if(advice.moveLeft)
			dir = Direction4.LEFT;
		else if(advice.moveDown)
			dir = Direction4.DOWN;
		else
			return;
		for(Agent pw : wpSensor.getContactsByClass(PipeWarp.class)) {
			if(((PipeWarp) pw).canBodyEnterPipe(getBounds(), dir)) {
				// player can enter pipe, so save a ref to the pipe
				pipeToEnter = (PipeWarp) pw;
				return;
			}
		}
	}

	private void processOtherContacts() {
		// despawn contact?
		if(acSensor.getFirstContactByClass(DespawnBox.class) != null) {
			parent.die();
			return;
		}

		// end of level flagpole contact?
		if(flagpoleContacted == null)
			flagpoleContacted = (Flagpole) acSensor.getFirstContactByClass(Flagpole.class);
		if(flagpoleContacted != null)
			return;

		// end of level trigger contact?
		if(levelendContacted == null)
			levelendContacted = (LevelEndTrigger) acSensor.getFirstContactByClass(LevelEndTrigger.class);
		if(levelendContacted != null) {
			// the level end will trigger the castle flag
			levelendContacted.trigger();
			return;
		}

		processHeadContacts();	// hitting bricks with his head

		// item contact?
		PowerupGiveAgent item = (PowerupGiveAgent) acSensor.getFirstContactByClass(PowerupGiveAgent.class);
		if(item != null)
			item.use(parent);

		if(parent.isPowerStarOn()) {
			// apply powerstar damage
			List<ContactDmgTakeAgent> list = acSensor.getContactsByClass(ContactDmgTakeAgent.class);
			for(ContactDmgTakeAgent agent : list) {
				// playSound should go in the processBody method, but... this is so much easier!
				agency.playSound(AudioInfo.Sound.SMB.KICK);
				agent.onDamage(parent, 1f, b2body.getPosition());
			}

			// Remove any agents that accumulate in the begin queue, to prevent begin contacts during
			// power star time being ignored - which would cause mario to take damage when power star time ends. 
			acBeginSensor.getAndResetContacts();
		}
		else {
			// check for headbounces
			List<Agent> list = acBeginSensor.getAndResetContacts();
			LinkedList<Agent> bouncedAgents = new LinkedList<Agent>();
			for(Agent agent : list) {
				// skip the agent if not bouncy :)
				if(!(agent instanceof HeadBounceTakeAgent) || !((HeadBounceTakeAgent) agent).isBouncy())
					continue;
				// If the bottom of mario's bounds box is at least as high as the middle of the agent then bounce.
				// (i.e. if mario's foot is at least as high as midway up the other agent...)
				// Note: check this frame postiion and previous frame postiion in case mario is travelling quickly...
				if(b2body.getPosition().y - getBodySize().y/2f >= agent.getPosition().y ||
						prevPosition.y - getBodySize().y/2f >= agent.getPosition().y) {
					bouncedAgents.add(agent);
					((HeadBounceTakeAgent) agent).onHeadBounce(parent);
				}
			}
			if(!bouncedAgents.isEmpty()) {
				b2body.setLinearVelocity(b2body.getLinearVelocity().x, 0f);
				b2body.applyLinearImpulse(new Vector2(0f, MARIO_HEADBOUNCE_VEL), b2body.getWorldCenter(), true);
			}

			// if not invincible then check for incoming damage
			if(!parent.isDmgInvincibleOn()) {
				// check for contact damage
				for(Agent a : list) {
					if(!(a instanceof ContactDmgGiveAgent))
						continue;
					// if the agent does contact damage and they were not head bounced
					if(((ContactDmgGiveAgent) a).isContactDamage() && !bouncedAgents.contains(a))
						isTakeDamage = true;
				}
			}
		}
	}

	/*
	 * Process the head contact add and remove queues, then check the list of current contacts for a head bang.
	 *
	 * NOTE: After banging his head while moving up, mario cannot bang his head again until he has moved down a
	 * sufficient amount. Also, mario can only break one block per head bang - but if his head contacts multiple
	 * blocks when he hits, then choose the block closest to mario on the x axis.
	 */
	private void processHeadContacts() {
		// if can head bang and is moving upwards fast enough then ...
		if(canHeadBang && (b2body.getLinearVelocity().y > MIN_HEADBANG_VEL || prevVelocity.y > MIN_HEADBANG_VEL)) {
			// check the list of tiles for the closest to mario
			float closest = 0;
			TileBumpTakeAgent closestTile = null;
			for(TileBumpTakeAgent thingHit : btSensor.getContactsByClass(TileBumpTakeAgent.class)) {
				float dist = Math.abs(((Agent) thingHit).getPosition().x - b2body.getPosition().x);
				if(closestTile == null || dist < closest) {
					closest = dist;
					closestTile = thingHit;
				}
			}

			// we have a weiner!
			if(closestTile != null) {
				canHeadBang = false;
				((TileBumpTakeAgent) closestTile).onBumpTile(parent);
			}
		}
		// mario can headbang once per up/down cycle of movement, so re-enable head bang when mario moves down
		else if(b2body.getLinearVelocity().y < 0f)
			canHeadBang = true;
	}

	private void decelLeftRight() {
		float vx = b2body.getLinearVelocity().x;
		if(vx == 0f)
			return;

		if(vx > 0f)
			b2body.applyLinearImpulse(new Vector2(-DECEL_XIMP, 0f), b2body.getWorldCenter(), true);
		else if(vx < 0f)
			b2body.applyLinearImpulse(new Vector2(DECEL_XIMP, 0f), b2body.getWorldCenter(), true);

		// do not decel so hard he moves in opposite direction
		if((vx > 0f && b2body.getLinearVelocity().x < 0f) || (vx < 0f && b2body.getLinearVelocity().x > 0f))
			b2body.setLinearVelocity(0f, b2body.getLinearVelocity().y);
	}

	public void moveBodyLeftRight(boolean right, boolean doRunRun) {
		float speed, max;
		if(ogSensor.isOnGround())
			speed = doRunRun ? MARIO_RUNMOVE_XIMP : MARIO_WALKMOVE_XIMP;
		else {
			speed = MARIO_AIRMOVE_XIMP;
			if(isDucking)
				speed /= 2f;
		}
		if(doRunRun)
			max = MARIO_MAX_RUNVEL;
		else
			max = MARIO_MAX_WALKVEL;
		if(right && b2body.getLinearVelocity().x <= max)
			b2body.applyLinearImpulse(new Vector2(speed, 0f), b2body.getWorldCenter(), true);
		else if(!right && b2body.getLinearVelocity().x >= -max)
			b2body.applyLinearImpulse(new Vector2(-speed, 0f), b2body.getWorldCenter(), true);
	}

	private void brakeLeftRight(boolean right) {
		float vx = b2body.getLinearVelocity().x;
		if(vx == 0f)
			return;

		if(right && vx < 0f)
			b2body.applyLinearImpulse(new Vector2(MARIO_BRAKE_XIMP, 0f), b2body.getWorldCenter(),  true);
		else if(!right && vx > 0f)
			b2body.applyLinearImpulse(new Vector2(-MARIO_BRAKE_XIMP, 0f), b2body.getWorldCenter(),  true);

		// do not brake so hard he moves in opposite direction
		if((vx > 0f && b2body.getLinearVelocity().x < 0f) || (vx < 0f && b2body.getLinearVelocity().x > 0f))
			b2body.setLinearVelocity(0f, b2body.getLinearVelocity().y);
	}

	private void duckSlideLeftRight(boolean right) {
		if(right && b2body.getLinearVelocity().x <= MARIO_MAX_DUCKSLIDE_VEL)
			b2body.applyLinearImpulse(new Vector2(MARIO_DUCKSLIDE_XIMP, 0f), b2body.getWorldCenter(), true);
		else if(!right && b2body.getLinearVelocity().x >= -MARIO_MAX_DUCKSLIDE_VEL)
			b2body.applyLinearImpulse(new Vector2(-MARIO_DUCKSLIDE_XIMP, 0f), b2body.getWorldCenter(), true);
	}

	private void moveBodyY(float value) {
		b2body.applyLinearImpulse(new Vector2(0, value),
				b2body.getWorldCenter(), true);
	}

	public float getStateTimer() {
		return stateTimer;
	}

	public PipeWarp getPipeToEnter() {
		return pipeToEnter;
	}

	public void resetPipeToEnter() {
		pipeToEnter = null;
	}

	public Flagpole getFlagpoleContacted() {
		return flagpoleContacted;
	}

	public void resetFlagpoleContacted() {
		flagpoleContacted = null;
	}

	public LevelEndTrigger getLevelEndContacted() {
		return levelendContacted;
	}

	public boolean isOnGround() {
		return ogSensor.isOnGround();
	}

	public boolean isFacingRight() {
		return isFacingRight;
	}

	public void setFacingRight(boolean isFacingRight) {
		this.isFacingRight = isFacingRight; 
	}

	public void setPosAndVel(Vector2 pos, Vector2 vel) {
		defineBody(pos, vel);
	}

	public void setBodyPosVelAndSize(Vector2 pos, Vector2 vel, boolean isBig) {
		this.isBig = isBig;
		isDucking = false;
		defineBody(pos, vel);
	}

	public void enableGravity() {
		b2body.setGravityScale(1f);
	}

	public void disableGravity() {
		b2body.setGravityScale(0f);
	}

	public boolean isDucking() {
		return isDucking;
	}

	public boolean isBigBody() {
		if(!isBig || isDucking || isDuckSliding)
			return false;
		else
			return true;
	}

	public Vector2 getBodySize() {
		if(isBigBody())
			return BIG_BODY_SIZE;
		else
			return SML_BODY_SIZE;
	}

	public boolean getAndResetTakeDamage() {
		boolean t = isTakeDamage;
		isTakeDamage = false;
		return t;
	}

	public Room getCurrentRoom() {
		return (Room) acSensor.getFirstContactByClass(Room.class);
	}

	@Override
	public Agent getParent() {
		return parent;
	}

	@Override
	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		Vector2 s = getBodySize();
		return new Rectangle(b2body.getPosition().x - s.x/2f, b2body.getPosition().y - s.y/2f, s.x, s.y);
	}

	@Override
	public void dispose() {
		b2body.getWorld().destroyBody(b2body);
		b2body = null;
	}
}
