package kidridicarus.game.Metroid.agent.NPC.skree;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.Metroid.agent.NPC.skreeshot.SkreeShot;
import kidridicarus.game.Metroid.agent.item.energy.Energy;
import kidridicarus.game.Metroid.agent.other.deathpop.DeathPop;
import kidridicarus.game.info.MetroidAudio;

public class SkreeBrain {
	private static final float MAX_HEALTH = 2f;
	private static final float ITEM_DROP_RATE = 1/3f;
	private static final float GIVE_DAMAGE = 8f;
	private static final float INJURY_TIME = 10f/60f;
	private static final float EXPLODE_WAIT = 1f;
	private static final Vector2[] EXPLODE_VEL = new Vector2[] {
			new Vector2(-1f, 2f), new Vector2(1f, 2f),
			new Vector2(-2f, 0f), new Vector2(2f, 0f) };
	private static final Vector2[] EXPLODE_OFFSET = new Vector2[] {
			UInfo.VectorP2M(-4f, 8f), UInfo.VectorP2M(4f, 8f),
			UInfo.VectorP2M(-8f, 0f), UInfo.VectorP2M(8f, 0f) };

	enum MoveState { SLEEP, FALL, ONGROUND, INJURY, EXPLODE, DEAD }

	private Skree parent;
	private SkreeBody body;
	private MoveState moveState;
	private float moveStateTimer;
	private float health;
	private boolean isInjured;
	private MoveState moveStateBeforeInjury;
	private Vector2 velocityBeforeInjury;
	private boolean isDead;
	private boolean despawnMe;
	private PlayerAgent target;
	private boolean isTargetRemoved;
	private RoomBox lastKnownRoom;

	public SkreeBrain(Skree parent, SkreeBody body) {
		this.parent = parent;
		this.body = body;
		moveState = MoveState.SLEEP;
		moveStateTimer = 0f;
		isInjured = false;
		moveStateBeforeInjury = null;
		velocityBeforeInjury = null;
		health = MAX_HEALTH;
		isDead = false;
		despawnMe = false;
		target = null;
		isTargetRemoved = false;
		lastKnownRoom = null;
	}

	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : ((ContactDmgBrainContactFrameInput) cFrameInput).contactDmgTakeAgents)
			agent.onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if(!isDead && (!cFrameInput.isKeepAlive || cFrameInput.isDespawn))
			despawnMe = true;
		// if not dead or despawning, and if room is known, then update last known room
		if(moveState != MoveState.DEAD && !despawnMe && cFrameInput.room != null)
			lastKnownRoom = cFrameInput.room;
		// if no target yet then check for new target
		if(target == null) {
			target = body.getSpine().getPlayerContact();
			// if found a target then add an AgentRemoveListener to allow de-targeting on death of target
			if(target != null) {
				parent.getAgency().addAgentRemoveListener(new AgentRemoveListener(parent, target) {
						@Override
						public void preRemoveAgent() { isTargetRemoved = true; }
					});
			}
		}
	}

	public SkreeSpriteFrameInput processFrame(FrameTime frameTime) {
		// if despawning then dispose and exit
		if(despawnMe) {
			parent.getAgency().removeAgent(parent);
			return null;
		}

		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChange = nextMoveState != moveState;
		switch(nextMoveState) {
			case SLEEP:
				break;
			case FALL:
				// if the target has been removed then de-target
				if(isTargetRemoved) {
					isTargetRemoved = false;
					target = null;
				}
				// continue the fall, if the target is null then it will be ignored, and down move will continue
				body.getSpine().doFall((Agent) target);
				break;
			case INJURY:
				// first frame of injury?
				if(isMoveStateChange) {
					moveStateBeforeInjury = moveState;
					velocityBeforeInjury = body.getVelocity().cpy();
					body.zeroVelocity(true, true);
					parent.getAgency().getEar().playSound(MetroidAudio.Sound.NPC_SMALL_HIT);
				}
				else if(moveStateTimer > INJURY_TIME) {
					isInjured = false;
					body.setVelocity(velocityBeforeInjury);
					// return to state before injury started
					nextMoveState = moveStateBeforeInjury;
				}
				break;
			case ONGROUND:
				body.zeroVelocity(true, true);
				break;
			case EXPLODE:
				doExplode();
				break;
			case DEAD:
				parent.getAgency().removeAgent(parent);
				parent.getAgency().createAgent(DeathPop.makeAP(body.getPosition()));
				doPowerupDrop();
				parent.getAgency().getEar().playSound(MetroidAudio.Sound.NPC_SMALL_HIT);
				return null;
		}

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);

		moveStateTimer = isMoveStateChange ? 0f : moveStateTimer+frameTime.timeDelta;
		moveState = nextMoveState;

		return new SkreeSpriteFrameInput(body.getPosition(), frameTime, moveState);
	}

	private MoveState getNextMoveState() {
		if(isDead)
			return MoveState.DEAD;
		else if(moveState == MoveState.EXPLODE)
			return MoveState.EXPLODE;
		else if(moveState == MoveState.ONGROUND && moveStateTimer > EXPLODE_WAIT)
			return MoveState.EXPLODE;
		else if(isInjured)
			return MoveState.INJURY;
		else if(body.getSpine().isOnGround())
			return MoveState.ONGROUND;
		// if something was targeted, even if it was removed, then do FALL state
		else if(target != null || isTargetRemoved)
			return MoveState.FALL;
		return MoveState.SLEEP;
	}

	private void doExplode() {
		if(EXPLODE_OFFSET.length != EXPLODE_VEL.length)
			throw new IllegalStateException("The Skree explosion offset array length does not equal the " +
					"explode velocity array length.");
		for(int i=0; i<EXPLODE_OFFSET.length; i++)
			parent.getAgency().createAgent(SkreeShot.makeAP(body.getPosition().cpy().add(EXPLODE_OFFSET[i]), EXPLODE_VEL[i]));
		parent.getAgency().removeAgent(parent);
	}

	private void doPowerupDrop() {
		// exit if drop not allowed
		if(Math.random() > ITEM_DROP_RATE)
			return;
		parent.getAgency().createAgent(Energy.makeAP(body.getPosition()));
	}

	public boolean onTakeDamage(Agent agent, float amount) {
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
}
