package kidridicarus.game.agent.SMB.NPC.turtle;

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
import kidridicarus.game.tool.QQ;

/*
 * TODO:
 * -do sliding turtle shells break bricks when they strike them?
 *  I couldn't find any maps in SMB 1 that would clear up this matter.
 */
public class Turtle extends Agent implements ContactDmgTakeAgent, BumpTakeAgent, DisposableAgent {
	private static final float GIVE_DAMAGE = 1f;
	private static final float DIE_FALL_TIME = 6f;

	public enum MoveState { WALK, FALL, DEAD }

	private TurtleBody body;
	private TurtleSprite sprite;

	private float moveStateTimer;
	private MoveState moveState;
	private boolean isFacingRight;
	private boolean isDead;
	private boolean deadBumpRight;
	private Agent perp;

	public Turtle(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		moveStateTimer = 0f;
		moveState = MoveState.WALK;
		isFacingRight = false;
		isDead = false;
		deadBumpRight = false;
		perp = null;
		body = new TurtleBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		sprite = new TurtleSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
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
				break;
			case DEAD:
				// newly deceased?
				if(moveStateChanged)
					doStartDeath();
				// check the old deceased for timeout or despawn touch
				else if(moveStateTimer > DIE_FALL_TIME || body.getSpine().isContactDespawn()) {
QQ.pr("turtle dispose, despawnbox contact="+body.getSpine().isContactDespawn());
					agency.disposeAgent(this);
				}
				break;
		}

		// increment state timer if state stayed the same, otherwise reset timer
		moveStateTimer = moveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		if(isDead)
			return MoveState.DEAD;
		else if(body.getSpine().isOnGround())
			return MoveState.WALK;
		else
			return MoveState.FALL;
	}

	private void doStartDeath() {
		body.getSpine().doBumpAndDisableAllContacts(deadBumpRight);
		if(perp == null)
			return;
		agency.createAgent(FloatingPoints.makeAP(PointAmount.P500, false, body.getPosition(), UInfo.P2M(16), perp));
	}

	private void processSprite(float delta) {
		// update sprite position and graphic
		sprite.update(delta, body.getPosition(), moveState, isFacingRight);
	}

	private void doDraw(AgencyDrawBatch batch){
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
/*
public class Turtle extends Agent implements ContactDmgTakeAgent, BumpTakeAgent, DisposableAgent {
	private static final float WALK_VEL = 0.4f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 0.4f;
	private static final float SLIDE_VEL = 2f;
	private static final float WAKING_TIME = 3f;
	private static final float WAKE_UP_DELAY = 1.7f;
	private static final float DIE_FALL_TIME = 6f;

	public enum MoveState { NONE, WALK, HIDE, WAKE_UP, SLIDE, DEAD }

	private TurtleBody body;
	private TurtleSprite sprite;

	private float moveStateTimer;
	private MoveState moveState;
	private boolean isFacingRight;
	private boolean isHiding;	// after player bounces on head, turtle hides in shell
	private boolean isWaking;
	private boolean isHeadBounced;
	private boolean isSliding;
	private PointAmount slidingTotal;
	private boolean isDead;
	private Vector2 deadVelocity;
	private Agent perp;

	public Turtle(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		moveStateTimer = 0f;
		moveState = MoveState.NONE;
		isFacingRight = false;
		isHiding = false;
		isWaking = false;
		isHeadBounced = false;
		isSliding = false;
		// the more sequential hits while sliding the higher the points per hit
		slidingTotal = PointAmount.ZERO;
		isDead = false;
		deadVelocity = new Vector2(0f, 0f);
		perp = null;

		body = new TurtleBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		sprite = new TurtleSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// check for head bounces
		if(isHeadBounced) {
			if(isSliding)
				disableSlide();
			else if(isHiding) {
				if(perp.getPosition().x > body.getPosition().x)
					enableSlide(false);	// slide right
				else
					enableSlide(true);	// slide left
			}
			else
				isHiding = true;
		}

		List<Agent> contBeginAgents = body.getSpine().getAndResetContactBeginAgents();
		boolean nowDead = false;
		if(isSliding) {
			// check the list of contacting agents, if there are damageable agents then slide damage them
			for(Agent a : contBeginAgents) {
				// if hit another sliding turtle, then both die
				if(a instanceof Turtle && ((Turtle) a).isSliding) {
					// Team is player because a player kicked this turtle,
					// essentially this turtle is a shot from the player.
					((ContactDmgTakeAgent) a).onTakeDamage(perp, AgentTeam.PLAYER, 1f, body.getPosition());
					onTakeDamage(perp, AgentTeam.PLAYER, 1f, a.getPosition());
					nowDead = true;
					break;
				}
				// hit non-turtle, so continue sliding and apply damage to other agent
				else if(a instanceof ContactDmgTakeAgent)
					((ContactDmgTakeAgent) a).onTakeDamage(perp, AgentTeam.PLAYER, 1f, body.getPosition());
			}
		}
		else if(!isHeadBounced) {
			// check the list of contacting agents, if there is a player agent then be kicked by them
			for(Agent a : contBeginAgents) {
				if(a instanceof PlayerAgent) {
					onPlayerContact(a);
					break;
				}
			}
		}

		// if not dead then check if move is blocked and reverse direction if necessary
		if(!nowDead && ((isSliding  && body.getSpine().isMoveBlocked(isFacingRight)) ||
				(!isHiding && body.getSpine().isMoveBlockedByAgent(isFacingRight)))) {
			bounceOffThing();
		}

		isHeadBounced = false;
	}

	private void processMove(float delta) {
		MoveState oldMoveState = moveState;
		moveState = getMoveState();
		switch(moveState) {
			case DEAD:
				// newly deceased?
				if(moveState != oldMoveState)
					doStartDeath();
				// check the old deceased for timeout
				else if(moveStateTimer > DIE_FALL_TIME)
					agency.disposeAgent(this);
				break;
			case HIDE:
				// wait a short time and reappear
				if(moveState != oldMoveState) {
					isWaking = false;
					doStartHide();
				}
				else if(moveStateTimer > WAKE_UP_DELAY)
					isWaking = true;
				break;
			case WAKE_UP:
				if(moveState == oldMoveState && moveStateTimer > WAKING_TIME)
					doEndHide();
				break;
			case SLIDE:
				if(moveState != oldMoveState)
					doStartSlide();
				if(body.getSpine().isOnGround()) {
					if(isFacingRight)
						body.setVelocity(SLIDE_VEL, body.getVelocity().y);
					else
						body.setVelocity(-SLIDE_VEL, body.getVelocity().y);
				}
				break;
			case WALK:
			case NONE:
				if(body.getSpine().isOnGround()) {
					if(isFacingRight)
						body.setVelocity(WALK_VEL, body.getVelocity().y);
					else
						body.setVelocity(-WALK_VEL, body.getVelocity().y);
				}
				break;
		}

		// increment state timer if state stayed the same, otherwise reset timer
		moveStateTimer = moveState == oldMoveState ? moveStateTimer+delta : 0f;
	}

	private MoveState getMoveState() {
		if(isDead)
			return MoveState.DEAD;
		else if(isSliding)
			return MoveState.SLIDE;
		else if(isHiding) {
			if(isWaking)
				return MoveState.WAKE_UP;
			else
				return MoveState.HIDE;
		}
		else
			return MoveState.WALK;
	}

	private void bounceOffThing() {
		// bounce off of vertical bounds
		isFacingRight = !isFacingRight;
		// check do slide sound
		if(isSliding)
			agency.playSound(AudioInfo.Sound.SMB.BUMP);
	}

	private void enableSlide(boolean right) {
		isSliding = true;
		isFacingRight = right;
	}

	private void disableSlide() {
		isSliding = false;
	}

	private void doStartSlide() {
		agency.playSound(AudioInfo.Sound.SMB.KICK);
		slidingTotal = PointAmount.P400;
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeAP(slidingTotal, isHeadBounced,
					body.getPosition(), UInfo.P2M(16), perp));
		}
	}

	private void doStartHide() {
		// stop moving
		body.zeroVelocity(true, true);
		agency.playSound(AudioInfo.Sound.SMB.STOMP);
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeAP(PointAmount.P100, isHeadBounced,
					body.getPosition(), UInfo.P2M(16), perp));
		}
	}

	private void doEndHide() {
		isWaking = false;
		isHiding = false;
		if(body.getSpine().isOnGround())
			body.setVelocity(WALK_VEL, body.getVelocity().y);
	}

	private void doStartDeath() {
		body.disableAllContacts();
		body.setVelocity(deadVelocity);
		if(perp != null) {
			agency.createAgent(FloatingPoints.makeAP(PointAmount.P500, isHeadBounced,
					body.getPosition(), UInfo.P2M(16), perp));
		}
	}

	private void processSprite(float delta) {
		// update sprite position and graphic
		sprite.update(delta, body.getPosition(), moveState, isFacingRight);
	}

	private void doDraw(AgencyDrawBatch batch){
		batch.draw(sprite);
	}

	@Override
	public void onHeadBounce(Agent perp) {
		if(isDead || isHeadBounced)
			return;
		this.perp = perp;
		isHeadBounced = true;
	}

	@Override
	public boolean isBouncy() {
		return !isDead;
	}

	// assume any amount of damage kills, for now...
	@Override
	public boolean onTakeDamage(Agent perp, AgentTeam aTeam, float amount, Vector2 fromCenter) {
		if(isDead || aTeam == AgentTeam.NPC)
			return false;

		this.perp = perp;
		isDead = true;
		if(fromCenter.x < body.getPosition().x)
			deadVelocity.set(BUMP_SIDE_VEL, BUMP_UP_VEL);
		else
			deadVelocity.set(-BUMP_SIDE_VEL, BUMP_UP_VEL);

		return true;
	}

	@Override
	public void onBump(Agent perp) {
		this.perp = perp;
		isDead = true;
		if(perp.getPosition().x < body.getPosition().x)
			deadVelocity.set(BUMP_SIDE_VEL, BUMP_UP_VEL);
		else
			deadVelocity.set(-BUMP_SIDE_VEL, BUMP_UP_VEL);
	}

	 // the player can "kick" a turtle hiding in its shell
	private void onPlayerContact(Agent player) {
		if(isDead)
			return;

		// a living turtle hiding in the shell and not sliding can be "pushed" to slide
		if(isHiding && !isSliding) {
			perp = player;
			// pushed from left?
			if(player.getPosition().x < body.getPosition().x)
				enableSlide(true);	// slide right
			else
				enableSlide(false);	// slide left
		}
	}

	@Override
	public boolean isContactDamage() {
		if(isDead || (isHiding && !isSliding))
			return false;
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
}*/