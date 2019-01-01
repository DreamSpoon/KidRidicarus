package kidridicarus.agent.Metroid.enemy;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.body.Metroid.enemy.ZoomerBody;
import kidridicarus.agent.optional.ContactDmgAgent;
import kidridicarus.agent.optional.DamageableAgent;
import kidridicarus.agent.sprite.Metroid.enemy.ZoomerSprite;
import kidridicarus.info.UInfo;
import kidridicarus.info.GameInfo.DiagonalDir4;
import kidridicarus.info.GameInfo.Direction4;
import kidridicarus.info.GameInfo.SpriteDrawOrder;

/*
 * The sensor code. It seems like a million cases due to the 4 possible "up" directions of the zoomer,
 * and the 4 sensor states, and the fact that the zoomer moves left or right. But, this can all be
 * collapsed down to one type of movement. Just rotate your thinking and maybe flip left/right, then
 * check the sensors.
 */
public class Zoomer extends Agent implements ContactDmgAgent, DamageableAgent {
	public enum ZoomerState { WALK, DEAD };
	private static final Vector2 MOVEVEL = new Vector2(0.3f, 0.3f);

	private ZoomerBody zBody;
	private ZoomerSprite zSprite;

	// walking right relative to the zoomer's body orientation
	private boolean isWalkingRight;
	// the moveDir can be derived from upDir and isWalkingRight
	private Direction4 upDir; 
	private float upDirChangeTimer;
	private static final float UPDIR_CHANGE_MINTIME = 0.1f;

	private boolean isDead;
	
	private Vector2 prevBodyPosition;

	private ZoomerState curState;

	public Zoomer(Agency agency, AgentDef adef) {
		super(agency, adef);

		isWalkingRight = false;
		upDir = null;
		upDirChangeTimer = 0;
		isDead = false;
		curState = ZoomerState.WALK;

		zBody = new ZoomerBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()));
		prevBodyPosition = zBody.getPosition();

		zSprite = new ZoomerSprite(agency.getAtlas(), zBody.getPosition());

		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.BOTTOM);
	}

	@Override
	public void update(float delta) {
		ZoomerState nextState = getState();

		switch(nextState) {
			case WALK:
				if(upDir == null)
					setInitialUpDir();
				else if(upDirChangeTimer > UPDIR_CHANGE_MINTIME)
					checkYourself();

				if(isWalkingRight)
					zBody.setVelocity(getMoveRight());
				else
					zBody.setVelocity(getMoveLeft());
				break;
			case DEAD:
				agency.disposeAgent(this);
				break;
		}

		prevBodyPosition = zBody.getPosition().cpy();
		
		upDirChangeTimer += delta;

		zSprite.update(delta, zBody.getPosition(), curState, upDir);
	}

	private static final float JUKE_EPSILON1 = 0.001f;
	private static final float JUKE_EPSILON2 = UInfo.P2M(0.01f);
	private void checkYourself() {
		/* Get the movement delta between this frame and last frame, and apply transform according to upDir and
		 * isWalkingRight to get movement assuming upDir is actually "UP", and isWalkingRight is true.
		 * This transform simplifies later calculations. 
		 */
		Vector2 moveDelta = getRelPosition(zBody.getPosition().cpy().sub(prevBodyPosition), upDir, isWalkingRight);
		boolean rotateClockwise = false;
		boolean rotateCC = false;
		// if they moved downward, then they must have come to the end of a lineSeg, so rotate clockwise
		if(moveDelta.y < -JUKE_EPSILON1)
			rotateClockwise = true;
		else if(moveDelta.x < JUKE_EPSILON2)
			rotateCC = true;
		if(rotateClockwise) {
			if(isWalkingRight)
				upDir = upDir.rotate270();
			else
				upDir = upDir.rotate90();
			upDirChangeTimer = 0f;
		}
		else if(rotateCC) {
			if(isWalkingRight)
				upDir = upDir.rotate90();
			else
				upDir = upDir.rotate270();
			upDirChangeTimer = 0f;
		}
	}

	/*
	 * Basically, rotate pos according to relUpDir and flipSign (flipsign is applied after, and only to x,
	 * because it relates to isWalkingRight).
	 * 
	 * "UP" is considered zero.
	 * Moving right is considered zero.
	 */
	private Vector2 getRelPosition(Vector2 pos, Direction4 relUpDir, boolean notFlipsign) {
		switch(relUpDir) {
			case UP:
				return new Vector2(notFlipsign ? pos.x : -pos.x, pos.y);
			case DOWN:
				return new Vector2(notFlipsign ? -pos.x : pos.x, -pos.y);
			case RIGHT:
				return new Vector2(notFlipsign ? -pos.y : pos.y, pos.x);
//			case LEFT:
			default:
				return new Vector2(notFlipsign ? pos.y : -pos.y, -pos.x);
		}
	}

	private static final float REDUCE_FACTOR = 0.2f;
	private Vector2 getMoveRight() {
		switch(upDir) {
			case RIGHT:
				return new Vector2(-MOVEVEL.x*REDUCE_FACTOR, -MOVEVEL.y);
			case UP:
				return new Vector2(MOVEVEL.x, -MOVEVEL.y*REDUCE_FACTOR);
			case LEFT:
				return new Vector2(MOVEVEL.x*REDUCE_FACTOR, MOVEVEL.y);
			// DOWN, by deduction
			default:
				return new Vector2(-MOVEVEL.x, MOVEVEL.y*REDUCE_FACTOR);
		}
	}

	private Vector2 getMoveLeft() {
		switch(upDir) {
			case RIGHT:
				return new Vector2(-MOVEVEL.x*REDUCE_FACTOR, MOVEVEL.y);
			case UP:
				return new Vector2(-MOVEVEL.x, -MOVEVEL.y*REDUCE_FACTOR);
			case LEFT:
				return new Vector2(MOVEVEL.x*REDUCE_FACTOR, -MOVEVEL.y);
			// DOWN, by deduction
			default:
				return new Vector2(MOVEVEL.x, MOVEVEL.y*REDUCE_FACTOR);
		}
	}

	private static final short TOPRIGHT_BIT = 2 << 0;
	private static final short TOPLEFT_BIT = 2 << 1;
	private static final short BOTTOMLEFT_BIT = 2 << 2;
	private static final short BOTTOMRIGHT_BIT = 2 << 3;
	private void setInitialUpDir() {
		short totalSense = 0;
		if(zBody.isSensorContacting(DiagonalDir4.TOPRIGHT))
			totalSense += TOPRIGHT_BIT;
		if(zBody.isSensorContacting(DiagonalDir4.TOPLEFT))
			totalSense += TOPLEFT_BIT;
		if(zBody.isSensorContacting(DiagonalDir4.BOTTOMLEFT))
			totalSense += BOTTOMLEFT_BIT;
		if(zBody.isSensorContacting(DiagonalDir4.BOTTOMRIGHT))
			totalSense += BOTTOMRIGHT_BIT;

		if(isWalkingRight) 
			upDir = getDirRight(totalSense, upDir);
		else
			upDir = getDirLeft(totalSense, upDir);

		upDirChangeTimer = 0;
	}

	/*
	 * Check sensors to determine "up" based on assumption that Zoomer will move right initially.
	 * To explain why left/right differentiation is necessary, consider the following:
	 * E.g. 1) If the zoomer's bottom sensors are contacting, and the top sensors are not contacting,
	 * then the zoomer is on the floor - it doesn't matter which direction it is moving.
	 * E.g. 2) If 3 of the zoomer's crawl sensors are contacting then we should check it's move direction to check
	 * where it would end up after it moves a short distance. If the topleft, bottomleft, and bottomright sensors
	 * are contacting, and the zoomer is moving right, then it should be put on the floor. Because, if it were put
	 * on the left wall then it move right initially (which is "down" the wall), and immediately change direction
	 * because it hit the floor. So, just set it's initial position to the floor.
	 */
	private Direction4 getDirRight(short totalSense, Direction4 initDir) {
		switch(totalSense) {
			// all sensors contacting?
			case (TOPRIGHT_BIT | TOPLEFT_BIT | BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT):
			// no sensors contacting?
			case 0:
				// need to initialize move dir?
				if(initDir == null)
					return Direction4.UP;
				// spin later like your life depends on it!
				else
					return initDir.rotate90();
			// attach to ceiling?
			case (TOPRIGHT_BIT | TOPLEFT_BIT):
			case (TOPRIGHT_BIT | TOPLEFT_BIT | BOTTOMRIGHT_BIT):
				return Direction4.DOWN;
			// attach to left wall?
			case (TOPLEFT_BIT | BOTTOMLEFT_BIT):
			case (TOPLEFT_BIT | BOTTOMLEFT_BIT | TOPRIGHT_BIT):
				return Direction4.RIGHT;
			// attach to floor?
			case (BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT):
			case (BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT | TOPLEFT_BIT):
				return Direction4.UP;
			// attach to right wall, by deduction
			default:
				return Direction4.LEFT;
		}
	}

	/*
	 * Check sensors to determine "up" based on assumption that Zoomer will move left initially.
	 */ 
	private Direction4 getDirLeft(short totalSense, Direction4 initDir) {
		switch(totalSense) {
			// all sensors contacting?
			case (TOPRIGHT_BIT | TOPLEFT_BIT | BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT):
			// no sensors contacting?
			case 0:
				// need to initialize move dir?
				if(initDir == null)
					return Direction4.UP;
				// spin later like your life depends on it!
				else
					return initDir.rotate270();
			// attach to ceiling?
			case (TOPRIGHT_BIT | TOPLEFT_BIT):
			case (TOPRIGHT_BIT | TOPLEFT_BIT | BOTTOMLEFT_BIT):
				return Direction4.DOWN;
			// attach to left wall?
			case (TOPLEFT_BIT | BOTTOMLEFT_BIT):
			case (TOPLEFT_BIT | BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT):
				return Direction4.RIGHT;
			// attach to floor?
			case (BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT):
			case (BOTTOMLEFT_BIT | BOTTOMRIGHT_BIT | TOPRIGHT_BIT):
				return Direction4.UP;
			// attach to right wall, by deduction
			default:
				return Direction4.LEFT;
		}
	}

	private ZoomerState getState() {
		if(isDead)
			return ZoomerState.DEAD;
		else
			return ZoomerState.WALK;
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
