package kidridicarus.agent.Metroid.NPC;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.body.Metroid.NPC.ZoomerBody;
import kidridicarus.agent.optional.ContactDmgAgent;
import kidridicarus.agent.optional.DamageableAgent;
import kidridicarus.agent.sprite.Metroid.NPC.ZoomerSprite;
import kidridicarus.info.GameInfo.Direction4;
import kidridicarus.info.GameInfo.SpriteDrawOrder;

/*
 * The sensor code. It seems like a million cases due to the 4 possible "up" directions of the zoomer,
 * and the 4 sensor states, and the fact that the zoomer moves left or right. But, this can all be
 * collapsed down to one type of movement. Just rotate your thinking and maybe flip left/right, then
 * check the sensors.
 * 
 * NOTE:
 * -zoomer is immune for 10/60 second after being hit by Samus' shot
 * -zoomer changes colors when immunity starts and ends
 */
public class Zoomer extends Agent implements ContactDmgAgent, DamageableAgent {
	private static final float UPDIR_CHANGE_MINTIME = 0.1f;

	public enum MoveState { WALK, DEAD }

	private ZoomerBody zBody;
	private ZoomerSprite zSprite;

	// walking right relative to the zoomer's up direction
	private boolean isWalkingRight;
	// the moveDir can be derived from upDir and isWalkingRight
	private Direction4 upDir;
	private float upDirChangeTimer;

	private boolean isDead;

	private Vector2 prevBodyPosition;

	private MoveState curState;

	private CrawlNerve crawlNerve;

	public Zoomer(Agency agency, AgentDef adef) {
		super(agency, adef);

		isWalkingRight = false;
		upDir = null;
		upDirChangeTimer = 0;
		isDead = false;
		curState = MoveState.WALK;

		zBody = new ZoomerBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()));
		prevBodyPosition = zBody.getPosition();

		zSprite = new ZoomerSprite(agency.getAtlas(), zBody.getPosition());

		crawlNerve = new CrawlNerve();

		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, SpriteDrawOrder.BOTTOM);
	}

	@Override
	public void update(float delta) {
		processContacts(delta);
		processMove();
		processSprite(delta);
	}

	private void processContacts(float delta) {
		Direction4 newUpDir = upDir;
		// need to get initial up direction?
		if(upDir == null)
			newUpDir = crawlNerve.getInitialUpDir(isWalkingRight, zBody);
		// check for change in up direction if enough time has elapsed
		else if(upDirChangeTimer > UPDIR_CHANGE_MINTIME)
			newUpDir = crawlNerve.checkUp(upDir, isWalkingRight, zBody.getPosition(), prevBodyPosition);

		upDirChangeTimer = upDir == newUpDir ? upDirChangeTimer+delta : 0f;
		upDir = newUpDir;
	}

	private void processMove() {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case WALK:
				zBody.setVelocity(crawlNerve.getMoveVec(isWalkingRight, upDir));
				break;
			case DEAD:
				agency.disposeAgent(this);
				break;
		}
		curState = nextMoveState;
		prevBodyPosition = zBody.getPosition().cpy();
	}

	private MoveState getNextMoveState() {
		if(isDead)
			return MoveState.DEAD;
		else
			return MoveState.WALK;
	}

	private void processSprite(float delta) {
		zSprite.update(delta, zBody.getPosition(), curState, upDir);
	}

	@Override
	public void draw(Batch batch) {
		zSprite.draw(batch);
	}

	@Override
	public void onDamage(Agent agent, float amount, Vector2 fromCenter) {
		isDead = true;
	}

	@Override
	public boolean isContactDamage() {
		return true;
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
	public Vector2 getVelocity() {
		return zBody.getVelocity();
	}

	@Override
	public void dispose() {
		zBody.dispose();
	}
}
