package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.info.KidIcarusAudio;
import kidridicarus.game.info.KidIcarusKV;

/*
 * Monoeye doesn't like it when gawkers stare at Monoeye, so Monoeye will target the gawker and attempt to
 * ogle them in a downward direction.
 * QQ
 */
public class Monoeye extends Agent implements ContactDmgTakeAgent, DisposableAgent {
	private static final float GIVE_DAMAGE = 1f;

	private enum MoveState { FLY, OGLE, DEAD }

	private MonoeyeBody body;
	private MonoeyeSprite sprite;
	private MoveState moveState;
	private boolean isMovingRight;
	private boolean isMovingUp;
	private float moveStateTimer;

	private boolean isFacingRight;
	private boolean isDead;
	private boolean despawnMe;
	private PlayerAgent ogleTarget;
	private boolean isTargetRemoved;

	public Monoeye(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		moveStateTimer = 0f;
		moveState = MoveState.FLY;
		isMovingRight = true;
		isMovingUp = false;
		isFacingRight = true;
		isDead = false;
		despawnMe = false;
		ogleTarget = null;
		isTargetRemoved = false;

		Vector2 startPoint = Agent.getStartPoint(properties);
		body = new MonoeyeBody(this, agency.getWorld(), startPoint, new Vector2(0f, 0f));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new MonoeyeSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doContactUpdate() {
		// push damage to contact damage agents
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
		if((!isDead && !body.getSpine().isTouchingKeepAlive()) || body.getSpine().isContactDespawn())
			despawnMe = true;
	}

	private void processMove(float delta) {
		// if despawning then dispose and exit
		if(despawnMe) {
			agency.removeAgent(this);
			return;
		}

		processGawkers();

		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case FLY:
				doFlyMove();
				break;
			case OGLE:
				// if the target has been removed then de-target
				if(isTargetRemoved) {
					isTargetRemoved = false;
					ogleTarget = null;
				}

				doOgleMove();
				break;
			case DEAD:
				agency.createAgent(
						Agent.createPointAP(KidIcarusKV.AgentClassAlias.VAL_SMALL_POOF, body.getPosition()));
				agency.createAgent(
						Agent.createPointAP(KidIcarusKV.AgentClassAlias.VAL_HEART1, body.getPosition()));
				agency.removeAgent(this);
				agency.getEar().playSound(KidIcarusAudio.Sound.General.SMALL_POOF);
				break;
		}

		// facing direction changes when moved into accel zone
		if(body.getSpine().isFlyFarRight())
			isFacingRight = false;
		else if(body.getSpine().isFlyFarLeft())
			isFacingRight = true;
		// facing left when moving left, otherwise facing right
		else
			isFacingRight = body.getVelocity().x >= 0f;

		moveStateTimer = moveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	private void processGawkers() {

		// If a gawker is gawking the Monoeye, and if Moneye is moving down while just about to enter
		// acceleration zone, then start to target the gawker for ogling in a downward direction.
		PlayerAgent testGawker = body.getSpine().getGawker(isFacingRight);
		if(testGawker != null && !isMovingUp && body.getSpine().isFlyFarBottom()) {
			ogleTarget = testGawker;
			// if found a target then add an AgentRemoveListener to allow de-targeting on death of target
			if(ogleTarget != null) {
				agency.addAgentRemoveListener(new AgentRemoveListener(this, ogleTarget) {
						@Override
						public void removedAgent() { isTargetRemoved = true; }
					});
			}
		}
	}

	private MoveState getNextMoveState() {
		if(isDead || moveState == MoveState.DEAD)
			return MoveState.DEAD;
		else if(ogleTarget != null)
			return MoveState.OGLE;
		else
			return MoveState.FLY;
	}

	// This eye can do some pretty fly moves...
	private void doFlyMove() {
		if(isMovingRight && body.getSpine().isFlyFarRight())
			isMovingRight = false;
		else if(!isMovingRight && body.getSpine().isFlyFarLeft())
			isMovingRight = true;

		if(isMovingUp && body.getSpine().isFlyFarTop())
			isMovingUp = false;
		else if(!isMovingUp && body.getSpine().isFlyFarBottom())
			isMovingUp = true;

		body.getSpine().applyFlyMoveUpdate(isMovingRight, isMovingUp);
	}

	// ... but get it mad, and you better hope the pirate look is in this season - you might lose an eye...
	private void doOgleMove() {
		if(ogleTarget == null)
			return;

		// continuously face ogleTarget - to maximize ogling
		float ogleTargetX = ogleTarget.getPosition().x;
		isFacingRight = body.getPosition().x <= ogleTargetX;
		// check horizontal move direction against accel zones
		if(isMovingRight && body.getSpine().isOgleFarRight(ogleTargetX))
			isMovingRight = false;
		else if(!isMovingRight && body.getSpine().isOgleFarLeft(ogleTargetX))
			isMovingRight = true;

		body.getSpine().applyOgleMoveUpdate(isMovingRight, ogleTarget.getPosition().x);
	}

	private void processSprite(float delta) {
		sprite.update(body.getPosition(), isFacingRight);
	}

	private void doDraw(AgencyDrawBatch batch){
		// draw if not despawned and not dead
		if(!despawnMe && !isDead)
			batch.draw(sprite);
	}

	// assume any amount of damage kills, for now...
	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		// if dead already or the damage is from the same team then return no damage taken
		if(isDead || !(agent instanceof PlayerAgent))
			return false;

		isDead = true;
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
