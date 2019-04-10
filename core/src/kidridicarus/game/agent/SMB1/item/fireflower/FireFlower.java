package kidridicarus.game.agent.SMB1.item.fireflower;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.agent.SMB1.other.floatingpoints.FloatingPoints;
import kidridicarus.game.info.SMB1_KV;
import kidridicarus.game.powerup.SMB1_Pow;

public class FireFlower extends Agent implements DisposableAgent {
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);

	private enum MoveState { SPROUT, WALK, END }

	private FireFlowerSprite sprite;
	private FireFlowerBody body;
	private AgentDrawListener myDrawListener;
	private Vector2 initSpawnPosition;
	// powerup can not be used until body is created, body is created after sprout time is finished
	private boolean isPowerupUsed;
	private float moveStateTimer;
	private MoveState moveState;
	private Agent powerupTaker;

	public FireFlower(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isPowerupUsed = false;
		powerupTaker = null;
		initSpawnPosition = Agent.getStartPoint(properties);
		moveStateTimer = 0f;
		moveState = MoveState.SPROUT;

		// no body at spawn time, body will be created later
		body = null;
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		// sprout from bottom layer and switch to next layer on sprout finish
		sprite = new FireFlowerSprite(agency.getAtlas(), initSpawnPosition.cpy().add(0f, SPROUT_OFFSET));
		myDrawListener = new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch adBatch) { doDraw(adBatch); }
			};
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, myDrawListener);
	}

	// if any agents touching this powerup are able to take it, then push it to them
	private void doContactUpdate() {
		// exit if not used or body not created yet
		if(isPowerupUsed || body == null)
			return;
		// any takers?
		PowerupTakeAgent taker = body.getSpine().getTouchingPowerupTaker();
		if(taker == null)
			return;
		// if powerup is taken then set used flag
		if(taker.onTakePowerup(new SMB1_Pow.FireFlowerPow())) {
			powerupTaker = (Agent) taker;
			isPowerupUsed = true;
		}
	}

	private void doUpdate(float delta) {
		processMove(delta);
		processSprite(delta);
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case SPROUT:
				break;
			case WALK:
				// if just finished sprouting then create agent body and change sprite draw order
				if(moveStateChanged) {
					// change from bottom to middle sprite draw order
					agency.removeAgentDrawListener(this, myDrawListener);
					myDrawListener = new AgentDrawListener() {
							@Override
							public void draw(AgencyDrawBatch adBatch) { doDraw(adBatch); }
						};
					agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, myDrawListener);
					body = new FireFlowerBody(this, agency.getWorld(), initSpawnPosition);
				}
				break;
			case END:
				agency.createAgent(FloatingPoints.makeAP(1000, true, body.getPosition(), powerupTaker));
				// powerup used, so dispose this agent
				agency.removeAgent(this);
				break;
		}

		moveStateTimer = moveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		if(isPowerupUsed)
			return MoveState.END;
		else if(moveState == MoveState.WALK || (moveState == MoveState.SPROUT && moveStateTimer > SPROUT_TIME))
			return MoveState.WALK;
		else
			return MoveState.SPROUT;
	}

	private void processSprite(float delta) {
		Vector2 position = new Vector2();
		switch(moveState) {
			case SPROUT:
				position.set(initSpawnPosition.cpy().add(0f,
						SPROUT_OFFSET * (SPROUT_TIME - moveStateTimer) / SPROUT_TIME));
				break;
			case WALK:
			case END:
				position.set(body.getPosition());
				break;
		}
		sprite.update(delta, position);
	}

	private void doDraw(AgencyDrawBatch adBatch){
		// do not draw sprite if powerup is used 
		if(isPowerupUsed)
			return;
		adBatch.draw(sprite);
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

	public static ObjectProperties makeAP(Vector2 position) {
		return Agent.createPointAP(SMB1_KV.AgentClassAlias.VAL_FIREFLOWER, position);
	}
}