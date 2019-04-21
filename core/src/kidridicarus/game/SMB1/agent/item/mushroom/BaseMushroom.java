package kidridicarus.game.SMB1.agent.item.mushroom;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.BumpTakeAgent;
import kidridicarus.game.SMB1.agent.other.floatingpoints.FloatingPoints;
import kidridicarus.game.SMB1.agent.other.sproutingpowerup.SproutingPowerup;

public abstract class BaseMushroom extends SproutingPowerup implements BumpTakeAgent {
	private static final float WALK_VEL = 0.6f;
	private static final float BUMP_UPVEL = 1.5f;

	private boolean isFacingRight;
	private boolean isBumped;
	private Vector2 bumpCenter;
	private RoomBox lastKnownRoom;

	protected abstract Animation<TextureRegion> getMushroomAnim(TextureAtlas atlas);

	public BaseMushroom(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isFacingRight = true;
		isBumped = false;
		bumpCenter = new Vector2();
		lastKnownRoom = null;

		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.POST_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doPostUpdate(); }
		});
		sprite = new BaseMushroomSprite(getMushroomAnim(agency.getAtlas()), getSproutStartPos());
	}

	@Override
	protected void finishSprout() {
		body = new BaseMushroomBody(this, agency.getWorld(), getSproutEndPos(), new Vector2(0f, 0f));
	}

	@Override
	protected void postSproutUpdate(PowerupTakeAgent powerupTaker) {
		// if this powerup is taken then make floating points and exit
		if(powerupTaker != null) {
			agency.createAgent(FloatingPoints.makeAP(1000, true, body.getPosition(), (Agent) powerupTaker));
			return;
		}

		processBumps();
		// if on ground then apply walk velocity
		if(body.getSpine().isOnGround()) {
			if(isFacingRight)
				body.setVelocity(WALK_VEL, body.getVelocity().y);
			else
				body.setVelocity(-WALK_VEL, body.getVelocity().y);
		}
		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);
	}

	private void processBumps() {
		// process bumpings
		if(isBumped) {
			isBumped = false;
			// If moving right and bumped from the right then reverse velocity,
			// if moving left and bumped from the left then reverse velocity
			if(isFacingRight && bumpCenter.x > body.getPosition().x)
				isFacingRight = false;
			else if(!isFacingRight && bumpCenter.x < body.getPosition().x)
				isFacingRight = true;
			body.applyImpulse(new Vector2(0f, BUMP_UPVEL));
		}
		// bounce off of vertical bounds and not agents
		else if(body.getSpine().isSideMoveBlocked(isFacingRight))
			isFacingRight = !isFacingRight;
	}

	private void doPostUpdate() {
		if(body == null)
			return;
		RoomBox nextRoom = body.getSpine().getCurrentRoom();
		lastKnownRoom = nextRoom != null ? nextRoom : lastKnownRoom;
	}

	@Override
	public void onTakeBump(Agent bumpingAgent) {
		// one bump per frame please
		if(isBumped)
			return;
		// if bumping agent doesn't have position then exit
		Vector2 bumpingAgentPos = AP_Tool.getCenter(bumpingAgent);
		if(bumpingAgentPos == null)
			return;

		isBumped = true;
		bumpCenter.set(bumpingAgentPos); 
	}
}
