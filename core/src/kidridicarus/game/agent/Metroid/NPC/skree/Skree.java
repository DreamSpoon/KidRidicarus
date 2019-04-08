package kidridicarus.game.agent.Metroid.NPC.skree;

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
import kidridicarus.common.info.UInfo;
import kidridicarus.game.agent.Metroid.NPC.skreeshot.SkreeShot;
import kidridicarus.game.agent.Metroid.item.energy.Energy;
import kidridicarus.game.agent.Metroid.other.deathpop.DeathPop;
import kidridicarus.game.info.MetroidAudio;

public class Skree extends Agent implements ContactDmgTakeAgent, DisposableAgent {
	private static final float MAX_HEALTH = 2f;
	private static final float ITEM_DROP_RATE = 1/3f;
	private static final float GIVE_DAMAGE = 8f;
	private static final Vector2 SPECIAL_OFFSET = UInfo.VectorP2M(0f, -4f);
	private static final float INJURY_TIME = 10f/60f;
	private static final float EXPLODE_WAIT = 1f;
	private static final Vector2[] EXPLODE_VEL = new Vector2[] {
			new Vector2(-1f, 2f), new Vector2(1f, 2f),
			new Vector2(-2f, 0f), new Vector2(2f, 0f) };
	private static final Vector2[] EXPLODE_OFFSET = new Vector2[] {
			UInfo.VectorP2M(-4f, 8f), UInfo.VectorP2M(4f, 8f),
			UInfo.VectorP2M(-8f, 0f), UInfo.VectorP2M(8f, 0f) };

	enum MoveState { SLEEP, FALL, ONGROUND, INJURY, EXPLODE, DEAD }

	private SkreeBody body;
	private SkreeSprite sprite;
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

	public Skree(Agency agency, ObjectProperties properties) {
		super(agency, properties);

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

		body = new SkreeBody(this, agency.getWorld(), Agent.getStartPoint(properties).cpy().add(SPECIAL_OFFSET),
				new Vector2(0f, 0f));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new SkreeSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
			@Override
			public void draw(AgencyDrawBatch batch) { doDraw(batch); }
		});
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
				if(isMoveStateChanged) {
					moveStateBeforeInjury = moveState;
					velocityBeforeInjury = body.getVelocity().cpy();
					body.zeroVelocity(true, true);
					agency.getEar().playSound(MetroidAudio.Sound.NPC_SMALL_HIT);
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
				agency.removeAgent(this);
				agency.createAgent(DeathPop.makeAP(body.getPosition()));
				doPowerupDrop();
				agency.getEar().playSound(MetroidAudio.Sound.NPC_SMALL_HIT);
				break;
		}

		moveStateTimer = nextMoveState == moveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
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
			agency.createAgent(SkreeShot.makeAP(body.getPosition().cpy().add(EXPLODE_OFFSET[i]), EXPLODE_VEL[i]));
		agency.removeAgent(this);
	}

	private void doPowerupDrop() {
		// exit if drop not allowed
		if(Math.random() > ITEM_DROP_RATE)
			return;
		agency.createAgent(Energy.makeAP(body.getPosition()));
	}

	private void processSprite(float delta) {
		sprite.update(delta, body.getPosition(), moveState);
	}

	private void doDraw(AgencyDrawBatch batch) {
		// draw if not despawning
		if(!despawnMe)
			batch.draw(sprite);
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
