package kidridicarus.game.agent.Metroid.NPC.rio;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.DeadReturnTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.info.MetroidAudio;
import kidridicarus.game.info.MetroidKV;

public class Rio extends Agent implements ContactDmgTakeAgent, DisposableAgent {
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

	// TODO: what if agent is removed/disposed while being targeted? Agent.isDisposed()?
	private PlayerAgent target;
	private boolean isInjured;
	private boolean isDead;
	private boolean despawnMe;

	public Rio(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		setStateFromProperties();

		body = new RioBody(this, agency.getWorld(), Agent.getStartPoint(properties), new Vector2(0f, 0f));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new RioSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
			@Override
			public void draw(AgencyDrawBatch batch) { doDraw(batch); }
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
		isInjured = false;
		isDead = false;
		despawnMe = false;
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
		if(target == null)
			target = body.getSpine().getPlayerContact();
	}

	private void processMove(float delta) {
		// if despawning then dispose and exit
		if(despawnMe) {
			agency.disposeAgent(this);
			deadReturnToSpawner();
			return;
		}

		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case FLAP:
				// if previous move state was swoop then clear some flags
				if(moveState == MoveState.SWOOP) {
					target = null;
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
				// first frame of swoop?
				if(isMoveStateChanged) {
					// if target on left then swoop left
					if(target.getPosition().x < body.getPosition().x)
						swoopDir = Direction4.LEFT;
					else
						swoopDir = Direction4.RIGHT;
				}

				// if sideways move is blocked by solid...
				if(body.getSpine().isHorizontalMoveBlocked(swoopDir.isRight(), false)) {
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
				if(body.getSpine().isTargetAboveMe(target.getPosition())) {
					isSwoopUp = true;
					swoopLowPoint = body.getPosition().cpy();
				}

				// if swooping up then move up, away from swoop low point
				if(isSwoopUp)
					body.getSpine().setSwoopVelocity(swoopLowPoint, swoopDir, true);
				// otherwise move down, hopefully toward, player target
				else
					body.getSpine().setSwoopVelocity(target.getPosition(), swoopDir, false);

				break;
			case DEAD:
				agency.getEar().playSound(MetroidAudio.Sound.NPC_BIG_HIT);
				doPowerupDrop();
				doDeathPop();
				break;
		}

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
		else if(target != null)
			return MoveState.SWOOP;
		else
			return MoveState.FLAP;
		
	}

	private void doPowerupDrop() {
		// exit if drop not allowed
		if(Math.random() > ITEM_DROP_RATE)
			return;
		agency.createAgent(Agent.createPointAP(MetroidKV.AgentClassAlias.VAL_ENERGY, body.getPosition()));
	}

	private void doDeathPop() {
		agency.createAgent(Agent.createPointAP(MetroidKV.AgentClassAlias.VAL_DEATH_POP, body.getPosition()));
		agency.disposeAgent(this);
		deadReturnToSpawner();
	}

	private void deadReturnToSpawner() {
		Agent spawnerAgent = properties.get(CommonKV.Spawn.KEY_SPAWNER_AGENT, null, Agent.class);
		if(spawnerAgent instanceof DeadReturnTakeAgent)
			((DeadReturnTakeAgent) spawnerAgent).onTakeDeadReturn(this);
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