package kidridicarus.game.agent.SMB.NPC.goomba;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.AgentTeam;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.agent.SMB.BumpTakeAgent;
import kidridicarus.game.agent.SMB.other.floatingpoints.FloatingPoints;
import kidridicarus.game.info.SMBInfo.PointAmount;

public class Goomba extends Agent implements ContactDmgTakeAgent, BumpTakeAgent, DisposableAgent {
//	private static final float GOOMBA_SQUISH_TIME = 2f;
	private static final float GOOMBA_BUMP_FALL_TIME = 6f;
	private static final float GIVE_DAMAGE = 1f;

	public enum MoveState { WALK, FALL, DEAD_BUMP }

	private float moveStateTimer;
	private MoveState moveState;
	private GoombaBody body;
	private GoombaSprite sprite;

	private boolean isFacingRight;
	private boolean isDead;
	private boolean deadBumpRight;
	private Agent perp;	// perpetrator of squish, bump, and damage

	public Goomba(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isFacingRight = false;
		isDead = false;
		deadBumpRight = false;
		perp = null;
		moveStateTimer = 0f;
		moveState = MoveState.WALK;

		body = new GoombaBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new GoombaSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doContactUpdate() {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : body.getSpine().getContactAgentsByClass(ContactDmgTakeAgent.class))
			agent.onTakeDamage(this, AgentTeam.NPC, GIVE_DAMAGE, body.getPosition());
	}

	private void doUpdate(float delta) {
		processMove(delta);
		processSprite(delta);
	}

	private void processMove(float delta) {
		if(body.getSpine().checkReverseVelocity(isFacingRight))
			isFacingRight = !isFacingRight;

		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case WALK:
				body.getSpine().doWalkMove(isFacingRight);
				break;
			case FALL:
				break;	// do nothing if falling
			case DEAD_BUMP:
				// new bump?
				if(moveStateChanged)
					startBump();
				// wait a short time and disappear
				else if(moveStateTimer > GOOMBA_BUMP_FALL_TIME)
					agency.disposeAgent(this);
				break;
/*			case DEAD_SQUISH:
				// new squish?
				if(moveStateChanged)
					startSquish();
				// wait a short time and disappear
				else if(moveStateTimer > GOOMBA_SQUISH_TIME)
					agency.disposeAgent(this);
				break;
*/
		}

		moveStateTimer = moveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		if(isDead)
			return MoveState.DEAD_BUMP;
		else if(body.getSpine().isOnGround())
			return MoveState.WALK;
		else
			return MoveState.FALL;
	}

/*	private void startSquish() {
		body.getSpine().doStopAndDisableAgentContacts();
		agency.playSound(AudioInfo.Sound.SMB.STOMP);
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeAP(PointAmount.P100, true,
					body.getPosition(), UInfo.P2M(16), perp));
		}
	}
*/

	private void startBump() {
		body.getSpine().doBumpAndDisableAllContacts(deadBumpRight);
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeAP(PointAmount.P100, false,
					body.getPosition(), UInfo.P2M(16), perp));
		}
	}

	private void processSprite(float delta) {
		// update sprite position and graphic
		sprite.update(delta, body.getPosition(), moveState);
	}

	public void doDraw(AgencyDrawBatch batch){
		batch.draw(sprite);
	}

	// assume any amount of damage kills, for now...
	@Override
	public boolean onTakeDamage(Agent agent, AgentTeam aTeam, float amount, Vector2 dmgOrigin) {
		if(isDead || aTeam == AgentTeam.NPC)
			return false;

		this.perp = agent;
		isDead = true;
		deadBumpRight = body.getSpine().isDeadBumpRight(dmgOrigin);
		return true;
	}

	@Override
	public void onBump(Agent agent) {
		if(isDead)
			return;

		this.perp = agent;
		isDead = true;
		deadBumpRight = body.getSpine().isDeadBumpRight(perp.getPosition());
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
