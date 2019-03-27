package kidridicarus.game.agent.Metroid.NPC.zoomer;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.PlayerAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.optional.DeadReturnTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GameKV;

/*
 * The sensor code. It seems like a million cases due to the 4 possible "up" directions of the zoomer,
 * and the 4 sensor states, and the fact that the zoomer moves left or right. But, this can all be
 * collapsed down to one type of movement. Just rotate your thinking and maybe flip left/right, then
 * check the sensors.
 */
public class Zoomer extends Agent implements ContactDmgTakeAgent, DisposableAgent {
	private static final float MAX_HEALTH = 2f;
	private static final float ITEM_DROP_RATE = 3/7f;
	private static final float GIVE_DAMAGE = 8f;
	private static final float UPDIR_CHANGE_MINTIME = 0.1f;
	private static final float INJURY_TIME = 10f/60f;

	enum MoveState { WALK, INJURY, DEAD }

	private ZoomerBody body;
	private ZoomerSprite sprite;
	private MoveState moveState;
	private float moveStateTimer;

	// walking right relative to the zoomer's up direction
	private boolean isWalkingRight;
	// the moveDir can be derived from upDir and isWalkingRight
	private Direction4 upDir;
	private float upDirChangeTimer;
	private boolean isInjured;
	private float health;
	private boolean isDead;
	private boolean despawnMe;

	public Zoomer(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isWalkingRight = false;
		upDir = Direction4.NONE;
		upDirChangeTimer = 0f;
		isInjured = false;
		health = MAX_HEALTH;
		isDead = false;
		despawnMe = false;
		moveState = MoveState.WALK;

		body = new ZoomerBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.POST_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doPostUpdate(); }
		});
		sprite = new ZoomerSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
			@Override
			public void draw(AgencyDrawBatch batch) { doDraw(batch); }
		});
	}

	private void doContactUpdate() {
		for(ContactDmgTakeAgent agent : body.getSpine().getContactDmgTakeAgents())
			agent.onTakeDamage(this, GIVE_DAMAGE, body.getPosition());
	}

	private void doUpdate(float delta) {
		processContacts(delta);
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts(float delta) {
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if((!isDead && !body.getSpine().isTouchingKeepAlive()) || body.getSpine().isContactDespawn()) {
			despawnMe = true;
			return;
		}

		// don't change up direction during injury
		if(isInjured) {
			upDirChangeTimer = 0f;
			return;
		}

		Direction4 newUpDir = upDir;
		// need to get initial up direction?
		if(upDir == Direction4.NONE)
			newUpDir = body.getSpine().getInitialUpDir(isWalkingRight);
		// check for change in up direction if enough time has elapsed
		else if(upDirChangeTimer > UPDIR_CHANGE_MINTIME)
			newUpDir = body.getSpine().checkUp(upDir, isWalkingRight, body.getPrevPosition());

		upDirChangeTimer = upDir == newUpDir ? upDirChangeTimer+delta : 0f;
		upDir = newUpDir;
	}

	private void processMove(float delta) {
		// if despawning then dispose self and exit
		if(despawnMe) {
			agency.disposeAgent(this);
			deadReturnToSpawner();
			return;
		}

		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case WALK:
				body.setVelocity(body.getSpine().getMoveVec(isWalkingRight, upDir));
				break;
			case INJURY:
				body.zeroVelocity(true, true);
				// if first frame of injury then play sound
				if(isMoveStateChanged)
					agency.getEar().playSound(AudioInfo.Sound.Metroid.NPC_SMALL_HIT);
				else if(moveStateTimer > INJURY_TIME)
					isInjured = false;
				break;
			case DEAD:
				doPowerupDrop();
				doDeathPop();
				agency.getEar().playSound(AudioInfo.Sound.Metroid.NPC_SMALL_HIT);
				break;
		}
		moveStateTimer = moveState == nextMoveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		if(isDead)
			return MoveState.DEAD;
		else if(isInjured)
			return MoveState.INJURY;
		else
			return MoveState.WALK;
	}

	private void doPowerupDrop() {
		// exit if drop not allowed
		if(Math.random() > ITEM_DROP_RATE)
			return;
		agency.createAgent(Agent.createPointAP(GameKV.Metroid.AgentClassAlias.VAL_ENERGY, body.getPosition()));
	}

	private void doDeathPop() {
		agency.createAgent(Agent.createPointAP(GameKV.Metroid.AgentClassAlias.VAL_DEATH_POP, body.getPosition()));
		agency.disposeAgent(this);
		deadReturnToSpawner();
	}

	private void deadReturnToSpawner() {
		Agent spawnerAgent = properties.get(CommonKV.Spawn.KEY_SPAWNER_AGENT, null, Agent.class);
		if(spawnerAgent instanceof DeadReturnTakeAgent)
			((DeadReturnTakeAgent) spawnerAgent).onTakeDeadReturn(this);
	}

	private void doPostUpdate() {
		// let body update previous position/velocity
		body.postUpdate();
	}

	private void processSprite(float delta) {
		sprite.update(delta, body.getPosition(), moveState, upDir);
	}

	private void doDraw(AgencyDrawBatch batch) {
		// draw if not despawning
		if(!despawnMe)
			batch.draw(sprite);
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		if(isInjured || isDead || !(agent instanceof PlayerAgent))
			return false;

		health -= amount;
		if(health <= 0f) {
			health = 0f;
			isDead = true;
		}
		else
			isInjured = true;

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
