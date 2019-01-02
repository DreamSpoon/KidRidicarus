package kidridicarus.agent.Metroid.player;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.AdvisableAgent;
import kidridicarus.agent.Agent;
import kidridicarus.agent.PlayerAgent;
import kidridicarus.agent.body.Metroid.player.SamusBody;
import kidridicarus.agent.general.Room;
import kidridicarus.agent.sprite.Metroid.player.SamusSprite;
import kidridicarus.guide.Advice;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.PowerupInfo.PowType;

public class Samus extends Agent implements AdvisableAgent, PlayerAgent {
	private static final float RUN_VEL = 0.8f;
	private static final float RUN_AIR_VEL = RUN_VEL * 0.6f;
	public enum SamusState { NONE, STAND, RUN, JUMP };

	private Advice advice;
	private SamusBody sBody;
	private SamusSprite sSprite;

	private SamusState curState;
	private float stateTimer;

	private boolean isFacingRight;
	private boolean isRunning;

	public Samus(Agency agency, AgentDef adef) {
		super(agency, adef);

		advice = new Advice();
		curState = SamusState.STAND;
		stateTimer = 0f;
		isFacingRight = true;
		isRunning = false;

		sBody = new SamusBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()));
		sSprite = new SamusSprite(agency.getAtlas(), sBody.getPosition());

		agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
		agency.enableAgentUpdate(this);
	}

	@Override
	public void update(float delta) {
		processContacts();
		processMoveState();

		SamusState nextState = getNextState();

		stateTimer = nextState == curState ? stateTimer + delta : 0f;
		curState = nextState;

		sSprite.update(delta, sBody.getPosition(), curState, isFacingRight);

		advice.clear();
	}

	private void processContacts() {
	}

	private void processMoveState() {
		if(advice.moveRight && !advice.moveLeft) {
			isFacingRight = true;
			if(sBody.isOnGround()) {
				isRunning = true;
				sBody.setVelocity(RUN_VEL, sBody.getVelocity().y);
			}
			else
				sBody.setVelocity(RUN_AIR_VEL, sBody.getVelocity().y);
		}
		else if(advice.moveLeft && !advice.moveRight) {
			isFacingRight = false;
			if(sBody.isOnGround()) {
				isRunning = true;
				sBody.setVelocity(-RUN_VEL, sBody.getVelocity().y);
			}
			else
				sBody.setVelocity(-RUN_AIR_VEL, sBody.getVelocity().y);
		}
		else {
			sBody.setVelocity(0f, sBody.getVelocity().y);
			isRunning = false;
		}
		
		if(advice.jump) {
			if(sBody.isOnGround()) {
				
			}
		}
	}

	private SamusState getNextState() {
		if(isRunning)
			return SamusState.RUN;
		else
			return SamusState.STAND;
	}

	@Override
	public void draw(Batch batch) {
		sSprite.draw(batch);
	}

	@Override
	public void setFrameAdvice(Advice advice) {
		this.advice = advice.cpy();
	}

	@Override
	public PowType pollNonCharPowerup() {
		return PowType.NONE;
	}

	@Override
	public boolean isDead() {
		return false;
	}

	@Override
	public boolean isAtLevelEnd() {
		return false;
	}

	@Override
	public float getStateTimer() {
		return stateTimer;
	}

	@Override
	public Room getCurrentRoom() {
		return sBody.getCurrentRoom();
	}

	@Override
	public Vector2 getPosition() {
		return sBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return sBody.getBounds();
	}

	@Override
	public Vector2 getVelocity() {
		return sBody.getVelocity();
	}

	@Override
	public void applyPowerup(PowType pt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		sBody.dispose();
	}
}
