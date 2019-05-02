package kidridicarus.game.SMB1.agent.player.mario;

import java.util.LinkedList;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentproperties.GetPropertyListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.playeragent.PlayerAgentSupervisor;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentproperties.GetPropertyListenerDirection4;
import kidridicarus.common.agentproperties.GetPropertyListenerVector2;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.powerup.PowerupList;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice4x4;
import kidridicarus.game.SMB1.agent.HeadBounceGiveAgent;
import kidridicarus.game.SMB1.agent.TileBumpTakeAgent.TileBumpStrength;
import kidridicarus.game.SMB1.agent.other.pipewarp.PipeWarp;
import kidridicarus.game.SMB1.agent.player.mario.HUD.MarioHUD;
import kidridicarus.game.SMB1.agent.player.mariofireball.MarioFireball;
import kidridicarus.game.info.SMB1_Audio;
import kidridicarus.game.info.SMB1_KV;
import kidridicarus.game.info.SMB1_Pow;

public class Mario extends PlayerAgent implements ContactDmgTakeAgent, HeadBounceGiveAgent, PowerupTakeAgent {
	private static final Vector2 DUCK_OFFSET = new Vector2(0f, UInfo.P2M(7f));
	private static final Vector2 GROW_OFFSET = DUCK_OFFSET;
	private static final float DEAD_DELAY_TIME = 3f;
	private static final float FIREBALL_OFFSET = UInfo.P2M(8f);
	private static final float SHOOT_COOLDOWN = 1/20f;
	private static final float NO_DAMAGE_TIME = 3f;
	private static final Vector2 DEAD_BOUNCE_IMP = new Vector2(0, 6f);
	private static final float POWERSTAR_MAXTIME = 15f;
	private static final float STARPOWER_DAMAGE = 1f;

	enum PowerState {
		SMALL, BIG, FIRE;
		public boolean isBigBody() { return !this.equals(SMALL); }
	}
	enum MoveState {
		STAND, RUN, BRAKE, FALL, DUCK, DUCKSLIDE, DUCKFALL, DUCKJUMP, JUMP, DEAD, DEAD_BOUNCE, CLIMB;
		public boolean equalsAny(MoveState ...otherStates) {
			for(MoveState state : otherStates) { if(this.equals(state)) return true; } return false;
		}
		public boolean isDuck() { return this.equalsAny(DUCK, DUCKFALL, DUCKJUMP, DUCKSLIDE); }
		public boolean isDuckNoSlide() { return this.equalsAny(DUCK, DUCKFALL, DUCKJUMP); }
		public boolean isJump() { return this.equalsAny(JUMP, DUCKJUMP); }
		public boolean isOnGround() { return this.equalsAny(STAND, RUN, BRAKE, DUCK, DUCKSLIDE); }
		public boolean isDead() { return this.equalsAny(DEAD, DEAD_BOUNCE); }
	}

	private PlayerAgentSupervisor supervisor;
	private MarioBody body;
	private MarioSprite sprite;
	private MarioHUD playerHUD;

	private MoveState moveState;
	private float moveStateTimer;
	private PowerState powerState;
	private boolean isFacingRight;
	private float noDamageCooldown;
	private float starPowerCooldown;

	private boolean isNextJumpAllowed;
	private boolean isNextJumpDelayed;
	private boolean isJumpForceContinue;
	// next head bump is denied immediately following headbump and lasts until Agent moves downward
	private boolean isHeadBumped;
	private int activeFireballCount;
	private float shootCooldown;
	private boolean shootAdviceReset;
	private boolean didShootFireballThisFrame;
	private boolean gaveHeadBounce;
	// list of powerups received during contact update
	private LinkedList<Powerup> powerupsReceived;
	private boolean didTakeDamage;
	private boolean isDeadBounce;
	private Direction4 lastHorizontalMoveDir;
	private boolean isDuckSlideRight;
	private RoomBox lastKnownRoom;

	public Mario(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		setStateFromProperties(properties);
		createGetPropertyListeners();

		body = new MarioBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				properties.get(CommonKV.KEY_VELOCITY, new Vector2(0f, 0f), Vector2.class),
				powerState.isBigBody(), false);
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doContactUpdate(); }
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.POST_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doPostUpdate(); }
			});
		sprite = new MarioSprite(agency.getAtlas(), body.getPosition(), powerState, isFacingRight);
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
			});
		playerHUD = new MarioHUD(this, agency.getAtlas());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.PLAYER_HUD, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDrawHUD(adBatch); }
			});

		supervisor = new PlayerAgentSupervisor(this);
	}

	private void setStateFromProperties(ObjectProperties properties) {
		moveState = MoveState.STAND;
		moveStateTimer = 0f;

		isFacingRight = false;
		if(properties.get(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class) == Direction4.RIGHT)
			isFacingRight = true;
		Object temp = properties.get(CommonKV.Powerup.KEY_POWERUP_LIST, null, PowerupList.class);
		PowerupList powList = (PowerupList) temp;
		if(powList == null)
			powerState = PowerState.SMALL;
		else if(powList.containsPowClass(SMB1_Pow.MushroomPow.class))
			powerState = PowerState.BIG;
		else if(powList.containsPowClass(SMB1_Pow.FireFlowerPow.class))
			powerState = PowerState.FIRE;
		else
			powerState = PowerState.SMALL;

		noDamageCooldown = 0f;
		starPowerCooldown = 0f;

		isDeadBounce = false;
		isNextJumpAllowed = false;
		isNextJumpDelayed = false;
		isJumpForceContinue = false;
		isHeadBumped = false;
		activeFireballCount = 0;
		shootCooldown = 0f;
		shootAdviceReset = true;
		didShootFireballThisFrame = false;
		gaveHeadBounce = false;
		powerupsReceived = new LinkedList<Powerup>();
		didTakeDamage = false;
		lastHorizontalMoveDir = Direction4.NONE;
		isDuckSlideRight = false;
		lastKnownRoom = null;
	}

	private void createGetPropertyListeners() {
		addGetPropertyListener(CommonKV.Script.KEY_SPRITE_SIZE, new GetPropertyListenerVector2() {
				@Override
				public Vector2 getVector2() { return new Vector2(sprite.getWidth(), sprite.getHeight()); }
			});
		addGetPropertyListener(CommonKV.KEY_DIRECTION, new GetPropertyListenerDirection4() {
				@Override
				public Direction4 getDirection4() { return isFacingRight ? Direction4.RIGHT : Direction4.LEFT; }
			});
		addGetPropertyListener(CommonKV.Powerup.KEY_POWERUP_LIST, new GetPropertyListener(PowerupList.class) {
				@Override
				public Object get() {
					PowerupList powList = new PowerupList();
					if(powerState == PowerState.BIG)
						powList.add(new SMB1_Pow.MushroomPow());
					else if(powerState == PowerState.FIRE)
						powList.add(new SMB1_Pow.FireFlowerPow());
					return powList;
				}
			});
	}

	/*
	 * Check for and do head bumps during contact update, so bump tiles can show results of bump immediately
	 * by way of regular update. Also apply star power damage if needed.
	 */
	private void doContactUpdate() {
		if(supervisor.isRunningScriptNoMoveAdvice())
			return;
		// exit if head bump flag hasn't reset
		if(isHeadBumped)
			return;
		// if mario is big then hit hard
		if(powerState.isBigBody())
			isHeadBumped = body.getSpine().checkDoHeadBump(TileBumpStrength.HARD);
		else
			isHeadBumped = body.getSpine().checkDoHeadBump(TileBumpStrength.SOFT);

		// if star powered then apply star power damage
		if(starPowerCooldown > 0f) {
			for(Agent agent : body.getSpine().getPushDamageContacts()) {
				// if they take contact damage then push it
				if(agent instanceof ContactDmgTakeAgent)
					((ContactDmgTakeAgent) agent).onTakeDamage(this, STARPOWER_DAMAGE, body.getPosition());
			}
		}
	}

	private void doUpdate(float delta) {
		processMove(delta, supervisor.pollMoveAdvice());
		processSprite(delta);
	}

	private void processMove(float delta, MoveAdvice4x4 moveAdvice) {
		if(!moveState.isDead() && !supervisor.isRunningScriptNoMoveAdvice()) {
			processPowerupsReceived();
			processFireball(moveAdvice);
			processDamageTaken(delta);
			processHeadBouncesGiven();
			processPipeWarps(moveAdvice);

			// make a note of the last direction in which mario was moving, for duck sliding
			if(body.getSpine().isMovingInDir(Direction4.RIGHT))
				lastHorizontalMoveDir = Direction4.RIGHT;
			else if(body.getSpine().isMovingInDir(Direction4.LEFT))
				lastHorizontalMoveDir = Direction4.LEFT;
		}

		// if a script is running with no move advice then switch to scripted body state and exit
		if(supervisor.isRunningScriptNoMoveAdvice()) {
			ScriptedAgentState scriptedState = supervisor.getScriptAgentState();
			body.useScriptedBodyState(scriptedState.scriptedBodyState, powerState.isBigBody());
			isFacingRight = scriptedState.scriptedSpriteState.isFacingRight;
			return;
		}

		MoveState nextMoveState = getNextMoveState(moveAdvice);
		boolean moveStateChanged = nextMoveState != moveState;
		// if mario is dead...
		if(nextMoveState.isDead())
			processDeadMove(moveStateChanged, nextMoveState);
		else {
			// if on ground...
			if(nextMoveState.isOnGround())
				processGroundMove(moveAdvice, nextMoveState);
			// else do air move
			else
				processAirMove(moveAdvice, nextMoveState);

			// do space wrap last so that contacts are maintained
			body.getSpine().checkDoSpaceWrap(lastKnownRoom);
		}

		// decrement fireball shoot cooldown timer
		shootCooldown = shootCooldown < delta ? 0f : shootCooldown-delta;
		// decrement starpower cooldown timer
		starPowerCooldown = starPowerCooldown < delta ? 0f : starPowerCooldown-delta;

		moveStateTimer = moveState == nextMoveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private void processHeadBouncesGiven() {
		// if a head bounce was given in the update frame then reset the flag and do bounce move
		if(gaveHeadBounce) {
			gaveHeadBounce = false;
			body.getSpine().applyHeadBounce();
		}
	}

	private void processPipeWarps(MoveAdvice4x4 moveAdvice) {
		PipeWarp pw = body.getSpine().getEnterPipeWarp(moveAdvice.getMoveDir4());
		if(pw != null)
			pw.use(this);
	}

	private void processDeadMove(boolean moveStateChanged, MoveState nextMoveState) {
		// if newly dead then disable contacts and start dead sound
		if(moveStateChanged) {
			body.allowOnlyDeadContacts();
			body.zeroVelocity(true, true);
			agency.getEar().stopAllMusic();
			agency.getEar().playSound(SMB1_Audio.Sound.MARIO_DIE);

			// do bounce up if needed
			if(nextMoveState == MoveState.DEAD_BOUNCE)
				body.applyImpulse(DEAD_BOUNCE_IMP);
		}
		// ... and if died a long time ago then do game over
		else if(moveStateTimer > DEAD_DELAY_TIME)
				supervisor.setGameOver();
		// ... else do nothing.
	}

	private void processPowerupsReceived() {
		for(Powerup pu : powerupsReceived) {
			if(pu.getPowerupCharacter() != PowChar.MARIO)
				supervisor.receiveNonCharPowerup(pu);

			applyPowerup(pu);
		}
		powerupsReceived.clear();
	}

	private void applyPowerup(Powerup pu) {
		PowerState newPowerState = powerState;
		if(pu instanceof SMB1_Pow.MushroomPow) {
			// if small then power up to big
			if(powerState == PowerState.SMALL)
				newPowerState = PowerState.BIG;
			agency.getEar().playSound(SMB1_Audio.Sound.POWERUP_USE);
		}
		else if(pu instanceof SMB1_Pow.FireFlowerPow) {
			// if small then power up to big
			if(powerState == PowerState.SMALL)
				newPowerState = PowerState.BIG;
			// if big then power up to fire
			else if(powerState == PowerState.BIG)
				newPowerState = PowerState.FIRE;
			agency.getEar().playSound(SMB1_Audio.Sound.POWERUP_USE);
		}
		else if(pu instanceof SMB1_Pow.Mush1UpPow) {
			// TODO apply 1-UP mushroom
		}
		else if(pu instanceof SMB1_Pow.PowerStarPow) {
			// Reset the begin contacts list, so that any turtles that were head bounced don't instantly die
			// when mario gets a powerstar.
			body.getSpine().getPushDamageContacts();
			starPowerCooldown = POWERSTAR_MAXTIME;
			agency.getEar().startSinglePlayMusic(SMB1_Audio.Music.STARPOWER);
		}
		else if(pu instanceof SMB1_Pow.CoinPow) {
			int coinTotal = properties.get(SMB1_KV.KEY_COINAMOUNT, 0, Integer.class);
			coinTotal += 1;
			properties.put(SMB1_KV.KEY_COINAMOUNT, coinTotal);
		}
		else if(pu instanceof SMB1_Pow.PointsPow) {
			int pointsTotal = properties.get(SMB1_KV.KEY_POINTAMOUNT, 0, Integer.class);
			pointsTotal += 100;
			properties.put(SMB1_KV.KEY_POINTAMOUNT, pointsTotal);
		}

		// if growing then increase body size
		if(newPowerState.isBigBody() && !powerState.isBigBody())
			body.setMarioBodyStuff(body.getPosition().cpy().add(GROW_OFFSET), body.getVelocity(), true, false);
		powerState = newPowerState;
	}

	private void processFireball(MoveAdvice4x4 moveAdvice) {
		// check do shoot fireball
		didShootFireballThisFrame = false;
		if(!moveAdvice.action0) {
			shootAdviceReset = true;
			return;
		}
		if(!isFireBallAllowed())
			return;

		activeFireballCount++;
		shootCooldown += SHOOT_COOLDOWN;
		didShootFireballThisFrame = true;
		// do not shoot again until shoot advice has been deactivated for at least 1 frame
		shootAdviceReset = false;

		Vector2 offset;
		if(isFacingRight)
			offset = body.getPosition().cpy().add(FIREBALL_OFFSET, 0f);
		else
			offset = body.getPosition().cpy().add(-FIREBALL_OFFSET, 0f);

		// create fireball with remove listener attached, so new fireballs can be created when old ones expire
		MarioFireball fireball =
				(MarioFireball) agency.createAgent(MarioFireball.makeAP(offset, isFacingRight, this));
		agency.addAgentRemoveListener(new AgentRemoveListener(this, fireball) {
				@Override
				public void removedAgent() { activeFireballCount--; }
			});
		// boom goes the dynamite
		agency.getEar().playSound(SMB1_Audio.Sound.FIREBALL);
	}

	private void processDamageTaken(float delta) {
		// exit if invulnerable to next damage
		if(noDamageCooldown > 0f) {
			noDamageCooldown -= delta;
			didTakeDamage = false;
			return;
		}
		// do not take damage during star power
		else if(starPowerCooldown > 0f)
			return;
		// exit if no damage taken this frame
		else if(!didTakeDamage)
			return;

		// apply damage and modify body if needed
		didTakeDamage = false;
		switch(powerState) {
			case SMALL:
				isDeadBounce = true;
				break;
			case BIG:
			case FIRE:
				powerState = PowerState.SMALL;
				noDamageCooldown = NO_DAMAGE_TIME;
				body.setMarioBodyStuff(body.getPosition().cpy().sub(GROW_OFFSET), body.getVelocity(), false, false);
				agency.getEar().playSound(SMB1_Audio.Sound.POWERDOWN);
				break;
		}
	}

	private void processGroundMove(MoveAdvice4x4 moveAdvice, MoveState nextMoveState) {
		// if was not ducking and is now ducking then define ducking body
		if(!moveState.isDuck() && nextMoveState.isDuck()) {
			body.setMarioBodyStuff(body.getPosition().cpy().sub(DUCK_OFFSET), body.getVelocity(),
					powerState.isBigBody(), true);
		}
		// if was ducking and is now now ducking then define regular body
		else if(moveState.isDuck() && !nextMoveState.isDuck()) {
			body.setMarioBodyStuff(body.getPosition().cpy().add(DUCK_OFFSET), body.getVelocity(),
					powerState.isBigBody(), false);
		}

		// If current move type is air and next move type is ground and jump is advised then delay jump until
		// jump advice is released.
		if(!moveState.isOnGround() && nextMoveState.isOnGround() && moveAdvice.action1)
			isNextJumpDelayed = true;

		// if not moving up (i.e. resting on ground) then allow next jump...
		if(!body.getSpine().isMovingInDir(Direction4.UP)) {
			isNextJumpAllowed = true;
			// ... and if jump advice is released then turn off jump delay
			if(!moveAdvice.action1)
				isNextJumpDelayed = false;
		}

		// do other ground move changes
		Direction4 moveDir = getMarioMoveDir(moveAdvice);
		boolean doHorizontalImpulse = false;
		boolean doDecelImpulse = false;
		boolean doDuckSlideImpulse = false;
		switch(nextMoveState) {
			case STAND:
				if(moveDir.isHorizontal())
					doHorizontalImpulse = true;
				else
					doDecelImpulse = true;
				break;
			case RUN:
				if(moveDir.isHorizontal())
					doHorizontalImpulse = true;
				else
					doDecelImpulse = true;
				break;
			case BRAKE:
				doDecelImpulse = true;
				break;
			case DUCK:
				doDecelImpulse = true;
				break;
			case DUCKSLIDE:
				doDuckSlideImpulse = true;
				break;
			default:
				throw new IllegalStateException("Only ground move states allowed, wrong nextMoveState="+nextMoveState);
		}

		// if not ducking then apply move advice to facing direction
		if(moveDir.isHorizontal() && !moveState.isDuckNoSlide()) {
			if(moveDir == Direction4.RIGHT)
				isFacingRight = true;
			else if(moveDir == Direction4.LEFT)
				isFacingRight = false;
		}

		if(doHorizontalImpulse)
			body.getSpine().applyWalkMove(isFacingRight, moveAdvice.action0);
		if(doDecelImpulse)
			body.getSpine().applyDecelMove(isFacingRight, nextMoveState.isDuck());
		if(doDuckSlideImpulse)
			body.getSpine().applyDuckSlideMove(isDuckSlideRight);

		// reset head bump flag while on ground and not moving up
		if(body.getVelocity().y < UInfo.VEL_EPSILON)
			isHeadBumped = false;
	}

	private void processAirMove(MoveAdvice4x4 moveAdvice, MoveState nextMoveState) {
		if(!body.getSpine().isMovingInDir(Direction4.UP))
			isJumpForceContinue = false;

		boolean isMoveStateChange = (moveState != nextMoveState);
		switch(nextMoveState) {
			case JUMP:
			case DUCKJUMP:
				if(isMoveStateChange) {
					isNextJumpAllowed = false;
					isJumpForceContinue = true;
					body.getSpine().applyJumpImpulse();

					if(powerState.isBigBody())
						agency.getEar().playSound(SMB1_Audio.Sound.MARIO_BIGJUMP);
					else
						agency.getEar().playSound(SMB1_Audio.Sound.MARIO_SMLJUMP);
				}
				else {
					if(!moveAdvice.action1)
						isJumpForceContinue = false;
				}
				break;
			case FALL:
			case DUCKFALL:
				break;
			default:
				throw new IllegalStateException("Only air move states allowed, wrong nextMoveState="+nextMoveState);
		}

		// mario might be ducking and moving right/left
		if(moveAdvice.moveRight && !moveAdvice.moveLeft)
			body.getSpine().applyAirMove(true);
		else if(moveAdvice.moveLeft && !moveAdvice.moveRight)
			body.getSpine().applyAirMove(false);

		// if jump force must continue then apply it, unless head bumped something
		if(isHeadBumped) {
			body.getSpine().applyHeadBumpMove();
			isHeadBumped = false;
		}
		else if(isJumpForceContinue) {
			if(isMoveStateChange)
				body.getSpine().applyJumpForce(0f);
			else
				body.getSpine().applyJumpForce(moveStateTimer);
		}

		body.getSpine().capFallVelocity();
	}

	private MoveState getNextMoveState(MoveAdvice4x4 moveAdvice) {
		if(moveState.isDead())
			return moveState;
		else if(body.getSpine().isContactDespawn() || body.getSpine().isContactScrollKillBox())
			return MoveState.DEAD;
		else if(isDeadBounce)
			return MoveState.DEAD_BOUNCE;
		// if [on ground flag is true] and agent isn't [moving upward while in air move state], then do ground move
		else if(body.getSpine().isOnGround() && !(body.getSpine().isMovingInDir(Direction4.UP) &&
				!moveState.isOnGround())) {
			return getNextMoveStateGround(moveAdvice);
		}
		// do air move
		else
			return getNextMoveStateAir();
	}

	private boolean isFireBallAllowed() {
		return activeFireballCount < 2 && powerState == PowerState.FIRE && shootCooldown <= 0f &&
				!moveState.isDuckNoSlide() && shootAdviceReset;
	}

	private MoveState getNextMoveStateGround(MoveAdvice4x4 moveAdvice) {
		Direction4 moveDir = getMarioMoveDir(moveAdvice);

		// if advised to jump and jumping is okay...
		if(moveAdvice.action1 && isNextJumpAllowed && !isNextJumpDelayed) {
			// if ducking already then duck jump
			if(moveState.isDuck())
				return MoveState.DUCKJUMP;
			// otherwise regular jump
			else
				return MoveState.JUMP;
		}
		else if(moveState.isDuck()) {
			// if not advising move down then check for unduck - if can't unduck then duckslide
			if(moveDir == Direction4.DOWN)
				return MoveState.DUCK;
			if(!moveState.isOnGround())
				return MoveState.STAND;
			Vector2 bodyTilePos = UInfo.VectorM2T(body.getPosition());

			// Check the space above and around mario to test if mario can unduck normally, or if he is in a
			// tight spot:

			// if the tile above ducking mario is not solid then allow stand
			if(!body.getSpine().isMapTileSolid(bodyTilePos.cpy().add(0, 1)))
				return MoveState.STAND;

			Vector2 subTilePos = UInfo.VectorM2SubT(body.getPosition());
			// If the player's last velocity direction was rightward, and their position is in the left half
			// of the tile, and the tile above and to the left of them is solid, then the player should
			// duckslide right.
			if((lastHorizontalMoveDir == Direction4.RIGHT &&
					subTilePos.x <= 0.5f && body.getSpine().isMapTileSolid(bodyTilePos.cpy().add(-1, 1))) ||
					(subTilePos.x > 0.5f && !body.getSpine().isMapTileSolid(bodyTilePos.cpy().add(1, 1))) ||
					(lastHorizontalMoveDir == Direction4.RIGHT &&
					subTilePos.x > 0.5f && body.getSpine().isMapTileSolid(bodyTilePos.cpy().add(1, 1)))) {
				isDuckSlideRight = true;
			}
			// the only other option is to duckslide left
			else
				isDuckSlideRight = false;

			return MoveState.DUCKSLIDE;
		}
		// if big body mario and move down is advised then duck
		else if(powerState.isBigBody() && moveDir == Direction4.DOWN)
			return MoveState.DUCK;
		// moving too slowly?
		else if(body.getSpine().isStandingStill())
			return MoveState.STAND;
		// moving in wrong direction?
		else if(body.getSpine().isBraking(isFacingRight))
			return MoveState.BRAKE;
		else
			return MoveState.RUN;
	}

	private MoveState getNextMoveStateAir() {
		// if in jump state then continue jump state
		if(moveState.isJump())
			return moveState;
		// not in jump state
		else {
			// if is ducking then do duck fall 
			if(moveState.isDuck())
				return MoveState.DUCKFALL;
			// not ducking so do regular fall
			else
				return MoveState.FALL;
		}
	}

	private Direction4 getMarioMoveDir(MoveAdvice4x4 moveAdvice) {
		// if no left/right move then return unmodified direction from move advice
		if(moveAdvice.moveLeft^moveAdvice.moveRight == false) {
			// down takes priority over up advice
			if(moveAdvice.moveDown)
				return Direction4.DOWN;
			else if(moveAdvice.moveUp)
				return Direction4.UP;
			else
				return Direction4.NONE;
		}

		// if advising move down while advising left/right then return no direction
		if(moveAdvice.moveDown)
			return Direction4.NONE;
		// ignore move up advice and return horizontal move direction
		else {
			if(moveAdvice.moveRight)
				return Direction4.RIGHT;
			else
				return Direction4.LEFT;
		}
	}

	private void doPostUpdate() {
		// let body update previous position/velocity
		body.postUpdate();
		// update last known room if not dead, so dead player moving through other RoomBoxes won't cause problems
		if(!moveState.isDead()) {
			RoomBox nextRoom = body.getSpine().getCurrentRoom();
			if(nextRoom != null)
				lastKnownRoom = nextRoom;
		}
	}

	private void processSprite(float delta) {
		if(supervisor.isRunningScriptNoMoveAdvice()) {
			MoveState scriptedMoveState;
			ScriptedSpriteState sss = supervisor.getScriptAgentState().scriptedSpriteState;
			switch(sss.spriteState) {
				case MOVE:
					scriptedMoveState = MoveState.RUN;
					break;
				case CLIMB:
					scriptedMoveState = MoveState.CLIMB;
					break;
				case STAND:
				default:
					scriptedMoveState = MoveState.STAND;
					break;
			}
			sprite.update(delta, sss.position, scriptedMoveState, powerState, sss.isFacingRight, false, false,
					(starPowerCooldown > 0f), sss.moveDir);
		}
		else {
			sprite.update(delta, body.getPosition(), moveState, powerState, isFacingRight,
					didShootFireballThisFrame, (noDamageCooldown > 0f), (starPowerCooldown > 0f),
					Direction4.NONE);
		}
	}

	private void doDraw(Eye adBatch) {
		// exit if using scripted sprite state and script says don't draw
		if(supervisor.isRunningScriptNoMoveAdvice() &&
				!supervisor.getScriptAgentState().scriptedSpriteState.visible)
			return;
		adBatch.draw(sprite);
	}

	private void doDrawHUD(Eye adBatch) {
		playerHUD.update(agency.getGlobalTimer());
		playerHUD.draw(adBatch);
	}

	@Override
	public boolean onTakePowerup(Powerup pu) {
		if(moveState == MoveState.DEAD)
			return false;
		powerupsReceived.add(pu);
		return true;
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		// no damage taken if already took damage this frame, or if invulnerable, or if star powered, or if dead
		if(didTakeDamage || noDamageCooldown > 0f || starPowerCooldown > 0f || moveState == MoveState.DEAD)
			return false;
		didTakeDamage = true;
		return true;
	}

	@Override
	public boolean onGiveHeadBounce(Agent agent) {
		// no head bouncing while star powered or dead
		if(starPowerCooldown > 0f || moveState == MoveState.DEAD)
			return false;
		// if other agent has bounds and head bounce is allowed then give head bounce to agent
		Rectangle otherBounds = AP_Tool.getBounds(agent);
		if(otherBounds != null && body.getSpine().isGiveHeadBounceAllowed(otherBounds)) {
			gaveHeadBounce = true;
			return true;
		}
		return false;
	}

	@Override
	public PlayerAgentSupervisor getSupervisor() {
		return supervisor;
	}

	@Override
	public RoomBox getCurrentRoom() {
		return lastKnownRoom;
	}

	@Override
	protected Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	protected Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	protected Vector2 getVelocity() {
		return body.getVelocity();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
