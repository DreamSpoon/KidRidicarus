package kidridicarus.game.agent.Metroid.NPC.rio;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agent.Metroid.item.energy.Energy;
import kidridicarus.game.agent.Metroid.other.deathpop.DeathPop;
import kidridicarus.game.info.MetroidAudio;

/*
 * TODO Check that Rio can re-target: as in, lose a target, then wait a bit, then gain a new target successfully.
 */
public class Rio extends PlacedBoundsAgent implements ContactDmgTakeAgent, DisposableAgent {
	private static final float MAX_HEALTH = 4f;
	private static final float ITEM_DROP_RATE = 1/3f;
	private static final float GIVE_DAMAGE = 8f;
	private static final float INJURY_TIME = 3f/20f;

	enum MoveState { FLAP, SWOOP, INJURY, DEAD }

	private RioBody body;
	private RioSprite sprite;
	private MoveState moveState;
	private float moveStateTimer;
	private float health;
	private Direction4 swoopDir;
	private boolean hasSwoopDirChanged;
	private Vector2 swoopLowPoint;
	private boolean isSwoopUp;
	private MoveState moveStateBeforeInjury;
	private Vector2 velocityBeforeInjury;
	private PlayerAgent target;
	private boolean isTargetRemoved;
	private boolean isInjured;
	private boolean isDead;
	private boolean despawnMe;
	private RoomBox lastKnownRoom;

	public Rio(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		setStateFromProperties();

		body = new RioBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.getVelocity(properties));
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
		sprite = new RioSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
			@Override
			public void draw(Eye adBatch) { doDraw(adBatch); }
		});
	}

	private void setStateFromProperties() {
		moveState = MoveState.FLAP;
		moveStateTimer = 0f;
		health = MAX_HEALTH;
		swoopDir = Direction4.NONE;
		hasSwoopDirChanged = false;
		swoopLowPoint = null;
		isSwoopUp = false;
		moveStateBeforeInjury = null;
		velocityBeforeInjury = null;
		target = null;
		isTargetRemoved = false;
		isInjured = false;
		isDead = false;
		despawnMe = false;
		lastKnownRoom = null;
	}

	// apply damage to all contacting agents
	private void doContactUpdate() {
		for(ContactDmgTakeAgent agent : body.getSpine().getContactDmgTakeAgents())
			agent.onTakeDamage(this, GIVE_DAMAGE, body.getPosition());
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if((!isDead && !body.getSpine().isTouchingKeepAlive()) || body.getSpine().isContactDespawn()) {
			despawnMe = true;
			return;
		}

		// if no target yet then check for new target
		if(target == null) {
			target = body.getSpine().getPlayerContact();
			// if found a target then add an AgentRemoveListener to allow de-targeting on death of target
			if(target != null) {
				agency.addAgentRemoveListener(new AgentRemoveListener(this, target) {
						@Override
						public void removedAgent() { isTargetRemoved = true; }
					});
			}
		}
	}

	private void processMove(float delta) {
		// if despawning then dispose and exit
		if(despawnMe) {
			agency.removeAgent(this);
			return;
		}

		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case FLAP:
				// if previous move state was swoop then zero velocity reset some flags
				if(moveState == MoveState.SWOOP) {
					body.zeroVelocity(true, true);
					target = null;
					isTargetRemoved = false;
					isSwoopUp = false;
					hasSwoopDirChanged = false;
					swoopLowPoint = null;
				}
				break;
			case INJURY:
				// first frame of injury?
				if(isMoveStateChanged) {
					moveStateBeforeInjury = moveState;
					velocityBeforeInjury = body.getVelocity().cpy();
					body.zeroVelocity(true, true);
					agency.getEar().playSound(MetroidAudio.Sound.NPC_BIG_HIT);
				}
				else if(moveStateTimer > INJURY_TIME) {
					isInjured = false;
					body.setVelocity(velocityBeforeInjury);
					// return to state before injury started
					nextMoveState = moveStateBeforeInjury;
				}
				break;
			case SWOOP:
				// If the target died / was removed then don't do the horizontal swoop check...
				if(isTargetRemoved) {
					// ... and if it's the first frame when the target has been removed then de-target and
					// initiate swoop up.
					if(target != null) {
						target = null;
						isSwoopUp = true;
						swoopLowPoint = body.getPosition().cpy();
					}
				}
				else {
					// first frame of swoop?
					if(isMoveStateChanged) {
						// if target on left then swoop left
						if(body.getSpine().isTargetOnSide(target, false))
							swoopDir = Direction4.LEFT;
						else
							swoopDir = Direction4.RIGHT;
					}
					// if sideways move is blocked by solid...
					if(body.getSpine().isSideMoveBlocked(swoopDir.isRight())) {
						// if swoop dir has changed already then don't change again, just swoop up
						if(hasSwoopDirChanged) {
							isSwoopUp = true;
							swoopLowPoint = body.getPosition().cpy();
						}
						else {
							// switch swoop direction
							swoopDir = swoopDir.isRight() ? Direction4.LEFT : Direction4.RIGHT;
							hasSwoopDirChanged = true;
						}
					}
					// if target is above rio by a certain amount then do swoop up
					if(body.getSpine().isTargetAboveMe(target)) {
						isSwoopUp = true;
						swoopLowPoint = body.getPosition().cpy();
					}
				}

				// if swooping up then move up, away from swoop low point
				if(isSwoopUp || target == null)
					body.getSpine().setSwoopVelocity(swoopLowPoint, swoopDir, true);
				// otherwise move down, hopefully toward, player target
				else
					body.getSpine().setSwoopVelocity(target, swoopDir, false);

				break;
			case DEAD:
				agency.removeAgent(this);
				agency.createAgent(DeathPop.makeAP(body.getPosition()));
				doPowerupDrop();
				agency.getEar().playSound(MetroidAudio.Sound.NPC_BIG_HIT);
				break;
		}

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);

		moveStateTimer = nextMoveState == moveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		if(isDead)
			return MoveState.DEAD;
		else if(isInjured)
			return MoveState.INJURY;
		else if(moveState == MoveState.SWOOP) {
			if(isSwoopUp && body.getSpine().isOnCeiling())
				return MoveState.FLAP;
			else
				return MoveState.SWOOP;
		}
		else if(target != null || isTargetRemoved)
			return MoveState.SWOOP;
		else
			return MoveState.FLAP;
	}

	private void doPowerupDrop() {
		// exit if drop not allowed
		if(Math.random() > ITEM_DROP_RATE)
			return;
		agency.createAgent(Energy.makeAP(body.getPosition()));
	}

	private void doPostUpdate() {
		// update last known room if not dead, so dead player moving through other RoomBoxes won't cause problems
		if(moveState != MoveState.DEAD) {
			RoomBox nextRoom = body.getSpine().getCurrentRoom();
			if(nextRoom != null)
				lastKnownRoom = nextRoom;
		}
	}

	private void processSprite(float delta) {
		sprite.update(delta, body.getPosition(), moveState);
	}

	private void doDraw(Eye adBatch) {
		// draw if not despawning
		if(!despawnMe)
			adBatch.draw(sprite);
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		// no damage during injury, or if dead
		if(isInjured || isDead || !(agent instanceof PlayerAgent))
			return false;
		// decrease health and check dead status
		health -= amount;
		if(health <= 0f) {
			isDead = true;
			health = 0f;
		}
		else
			isInjured = true;
		// took damage
		return true;
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
