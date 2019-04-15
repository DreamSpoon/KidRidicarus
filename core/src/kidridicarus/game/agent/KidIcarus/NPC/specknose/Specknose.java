package kidridicarus.game.agent.KidIcarus.NPC.specknose;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.agent.KidIcarus.item.angelheart.AngelHeart;
import kidridicarus.game.agent.KidIcarus.other.vanishpoof.VanishPoof;
import kidridicarus.game.agentspine.KidIcarus.FlyBallSpine.AxisGoState;
import kidridicarus.game.info.KidIcarusAudio;

public class Specknose extends Agent implements ContactDmgTakeAgent, DisposableAgent {
	private static final float GIVE_DAMAGE = 1f;
	private static final int DROP_HEART_COUNT = 10;
	private static final float HORIZONTAL_ONLY_CHANCE = 1/6f;

	private enum MoveState { APPEAR, FLY, DEAD }

	private SpecknoseBody body;
	private SpecknoseSprite sprite;
	private MoveState moveState;
	private float moveStateTimer;
	private AxisGoState horizGoState;
	private AxisGoState vertGoState;
	private boolean isHorizontalOnly;
	private boolean isAppearing;

	private boolean isDead;
	private boolean despawnMe;

	public Specknose(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		moveStateTimer = 0f;
		moveState = MoveState.APPEAR;
		// Start move right, and
		if(Math.random() <= 0.5f)
			horizGoState = AxisGoState.VEL_PLUS;
		else
			horizGoState = AxisGoState.VEL_MINUS;
		// move down.
		vertGoState = AxisGoState.VEL_MINUS;
		isDead = false;
		despawnMe = false;
		isHorizontalOnly = Math.random() <= HORIZONTAL_ONLY_CHANCE;
		isAppearing = true;

		// when the poof finishes, this Specknose will finish spawn
		Agent poofAgent = agency.createAgent(VanishPoof.makeAP(Agent.getStartPoint(properties), true));
		agency.addAgentRemoveListener(new AgentRemoveListener(this, poofAgent) {
				@Override
				public void removedAgent() { isAppearing = false; }
			});

		body = null;
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = null;
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
		// exit if not finished appearing
		if(moveState == MoveState.APPEAR)
			return;
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
		boolean isMoveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case APPEAR:
				break;
			case FLY:
				if(isMoveStateChanged)
					finishSpawn();

				horizGoState = getNextAxisGoState(true, horizGoState);
				vertGoState = getNextAxisGoState(false, vertGoState);
				if(isHorizontalOnly)
					body.getSpine().applyAxisMoves(horizGoState, null);
				else
					body.getSpine().applyAxisMoves(horizGoState, vertGoState);

				break;
			case DEAD:
				agency.createAgent(VanishPoof.makeAP(body.getPosition(), true));
				agency.createAgent(AngelHeart.makeAP(body.getPosition(), DROP_HEART_COUNT));
				agency.removeAgent(this);
				agency.getEar().playSound(KidIcarusAudio.Sound.General.SMALL_POOF);
				break;
		}

		moveStateTimer = isMoveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	private void finishSpawn() {
		body = new SpecknoseBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		sprite = new SpecknoseSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
			});
	}

	private AxisGoState getNextAxisGoState(boolean isHorizontal, AxisGoState currentGoState) {
		if(currentGoState == AxisGoState.ACCEL_PLUS) {
			if(body.getSpine().isContinueAcceleration(isHorizontal, true))
				return AxisGoState.ACCEL_PLUS;
			else
				return AxisGoState.VEL_PLUS;
		}
		else if(currentGoState == AxisGoState.VEL_PLUS) {
			if(body.getSpine().isChangeDirection(isHorizontal, true))
				return AxisGoState.ACCEL_MINUS;
			else
				return AxisGoState.VEL_PLUS;
		}
		else if(currentGoState == AxisGoState.ACCEL_MINUS) {
			if(body.getSpine().isContinueAcceleration(isHorizontal, false))
				return AxisGoState.ACCEL_MINUS;
			else
				return AxisGoState.VEL_MINUS;
		}
		// VEL_MINUS
		else {
			if(body.getSpine().isChangeDirection(isHorizontal, false))
				return AxisGoState.ACCEL_PLUS;
			else
				return AxisGoState.VEL_MINUS;
		}
	}

	private MoveState getNextMoveState() {
		if(isDead || moveState == MoveState.DEAD)
			return MoveState.DEAD;
		else if(isAppearing)
			return MoveState.APPEAR;
		else
			return MoveState.FLY;
	}

	private void processSprite(float delta) {
		if(moveState != MoveState.APPEAR)
			sprite.update(delta, false, body.getPosition());
	}

	private void doDraw(Eye adBatch){
		// draw if not despawned and not dead
		if(!despawnMe && !isDead)
			adBatch.draw(sprite);
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
