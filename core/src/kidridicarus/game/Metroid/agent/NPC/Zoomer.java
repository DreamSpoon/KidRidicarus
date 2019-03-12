package kidridicarus.game.Metroid.agent.NPC;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.game.Metroid.agentbody.NPC.ZoomerBody;
import kidridicarus.game.Metroid.agentsprite.NPC.ZoomerSprite;
import kidridicarus.game.info.GameKV;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgGiveAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.Direction4;

/*
 * The sensor code. It seems like a million cases due to the 4 possible "up" directions of the zoomer,
 * and the 4 sensor states, and the fact that the zoomer moves left or right. But, this can all be
 * collapsed down to one type of movement. Just rotate your thinking and maybe flip left/right, then
 * check the sensors.
 */
public class Zoomer extends Agent implements ContactDmgGiveAgent,
		ContactDmgTakeAgent, DisposableAgent {
	private static final float UPDIR_CHANGE_MINTIME = 0.1f;
	private static final float INJURY_TIME = 10f/60f;

	public enum MoveState { WALK, INJURY, DEAD }

	private ZoomerBody zBody;
	private ZoomerSprite zSprite;

	// walking right relative to the zoomer's up direction
	private boolean isWalkingRight;
	// the moveDir can be derived from upDir and isWalkingRight
	private Direction4 upDir;
	private float upDirChangeTimer;
	private Vector2 prevBodyPosition;

	private boolean isInjured;
	private float health;
	private boolean isDead;

	private MoveState curMoveState;
	private float stateTimer;

	public Zoomer(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isWalkingRight = false;
		upDir = Direction4.NONE;
		upDirChangeTimer = 0f;
		isInjured = false;
		health = 2f;
		isDead = false;
		curMoveState = MoveState.WALK;

		zBody = new ZoomerBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		prevBodyPosition = zBody.getPosition();

		zSprite = new ZoomerSprite(agency.getAtlas(), zBody.getPosition());

		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
			@Override
			public void draw(AgencyDrawBatch batch) { doDraw(batch); }
		});
	}

	private void doUpdate(float delta) {
		processContacts(delta);
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts(float delta) {
		// don't change up direction during injury
		if(isInjured) {
			upDirChangeTimer = 0f;
			return;
		}

		Direction4 newUpDir = upDir;
		// need to get initial up direction?
		if(upDir == Direction4.NONE)
			newUpDir = CrawlNerve.getInitialUpDir(isWalkingRight, zBody);
		// check for change in up direction if enough time has elapsed
		else if(upDirChangeTimer > UPDIR_CHANGE_MINTIME)
			newUpDir = CrawlNerve.checkUp(upDir, isWalkingRight, zBody.getPosition(), prevBodyPosition);

		upDirChangeTimer = upDir == newUpDir ? upDirChangeTimer+delta : 0f;
		upDir = newUpDir;
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case WALK:
				zBody.setVelocity(CrawlNerve.getMoveVec(isWalkingRight, upDir));
				break;
			case INJURY:
				zBody.zeroVelocity(true, true);
				if(curMoveState == nextMoveState && stateTimer > INJURY_TIME)
					isInjured = false;
				break;
			case DEAD:
				doDeathPop();
				break;
		}
		stateTimer = curMoveState == nextMoveState ? stateTimer+delta : 0f;
		curMoveState = nextMoveState;
		prevBodyPosition = zBody.getPosition().cpy();
	}

	private void doDeathPop() {
		agency.createAgent(Agent.createPointAP(GameKV.Metroid.AgentClassAlias.VAL_DEATH_POP, zBody.getPosition()));
		agency.disposeAgent(this);
	}

	private MoveState getNextMoveState() {
		if(isDead)
			return MoveState.DEAD;
		else if(isInjured)
			return MoveState.INJURY;
		else
			return MoveState.WALK;
	}

	private void processSprite(float delta) {
		zSprite.update(delta, zBody.getPosition(), curMoveState, upDir);
	}

	public void doDraw(AgencyDrawBatch batch) {
		batch.draw(zSprite);
	}

	@Override
	public void onDamage(Agent agent, float amount, Vector2 fromCenter) {
		if(isInjured || isDead)
			return;

		health -= amount;
		if(health <= 0f) {
			health = 0f;
			isDead = true;
		}
		else
			isInjured = true;
	}

	@Override
	public boolean isContactDamage() {
		return !isDead;
	}

	@Override
	public Vector2 getPosition() {
		return zBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return zBody.getBounds();
	}

	@Override
	public void disposeAgent() {
		zBody.dispose();
	}
}
