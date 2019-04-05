package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

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
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.info.KidIcarusAudio;
import kidridicarus.game.info.KidIcarusKV;

public class Monoeye extends Agent implements ContactDmgTakeAgent, DisposableAgent {
	private static final float GIVE_DAMAGE = 1f;

	private enum MoveState { FLY, DEAD }

	private MonoeyeBody body;
	private MonoeyeSprite sprite;
	private MoveState moveState;
	private boolean isMovingRight;
	private boolean isMovingUp;
	private float moveStateTimer;

	private boolean isFacingRight;
	private boolean isDead;
	private boolean despawnMe;

	public Monoeye(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		moveStateTimer = 0f;
		moveState = MoveState.FLY;
		isMovingRight = true;
		isMovingUp = false;
		isFacingRight = true;
		isDead = false;
		despawnMe = false;

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

		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case FLY:
				doFlyMove();
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

		if(body.getSpine().isFarRight())
			isFacingRight = false;
		else if(body.getSpine().isFarLeft())
			isFacingRight = true;
		// facing left when moving left, otherwise facing right
		else
			isFacingRight = body.getVelocity().x >= 0f;

		moveStateTimer = moveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	// this eye can do some pretty fly moves
	private void doFlyMove() {
		if(isMovingRight && body.getSpine().isFarRight())
			isMovingRight = false;
		else if(!isMovingRight && body.getSpine().isFarLeft())
			isMovingRight = true;

		if(isMovingUp && body.getSpine().isFarTop())
			isMovingUp = false;
		else if(!isMovingUp && body.getSpine().isFarBottom())
			isMovingUp = true;

		body.getSpine().applyMoveBodyUpdate(isMovingRight, isMovingUp);
	}

	private MoveState getNextMoveState() {
		if(isDead || moveState == MoveState.DEAD)
			return MoveState.DEAD;
		else
			return MoveState.FLY;
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
