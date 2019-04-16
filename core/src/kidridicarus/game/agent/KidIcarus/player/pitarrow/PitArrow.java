package kidridicarus.game.agent.KidIcarus.player.pitarrow;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agent.KidIcarus.player.pit.Pit;
import kidridicarus.game.info.KidIcarusKV;

public class PitArrow extends Agent implements DisposableAgent {
	private static final float LIVE_TIME = 0.217f;
	private static final float GIVE_DAMAGE = 1f;

	private Pit parent;
	private PitArrowBody body;
	private PitArrowSprite sprite;
	private float moveStateTimer;
	private boolean isDead;
	private Direction4 arrowDir;
	private RoomBox lastKnownRoom;

	public PitArrow(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		moveStateTimer = 0f;
		lastKnownRoom = null;
		parent = properties.get(AgencyKV.Spawn.KEY_START_PARENT_AGENT, null, Pit.class);
		// check the definition properties, maybe the shot needs to expire immediately
		isDead = properties.containsKey(CommonKV.Spawn.KEY_EXPIRE);
		arrowDir = properties.get(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);
		body = new PitArrowBody(this, agency.getWorld(), Agent.getStartPoint(properties),
				Agent.getStartVelocity(properties), arrowDir);
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doUpdate(delta); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.POST_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doPostUpdate(); }
		});
		sprite = new PitArrowSprite(agency.getAtlas(), body.getPosition(), arrowDir);
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
			@Override
			public void draw(Eye adBatch) { doDraw(adBatch); }
		});
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite();
	}

	private void processContacts() {
		// check for agents needing damage, and damage the first one
		for(ContactDmgTakeAgent agent : body.getSpine().getContactDmgTakeAgents()) {
			// do not hit parent
			if(agent == parent)
				continue;
			agent.onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
			isDead = true;
			return;
		}
		// if hit a solid then despawn
		if(body.getSpine().isMoveBlocked(arrowDir))
			isDead = true;
	}

	private void processMove(float delta) {
		if(moveStateTimer > LIVE_TIME)
			isDead = true;
		if(isDead)
			agency.removeAgent(this);
		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);
		moveStateTimer += delta;
	}

	private void doPostUpdate() {
		RoomBox nextRoom = body.getSpine().getCurrentRoom();
		if(nextRoom != null)
			lastKnownRoom = nextRoom;
	}

	private void processSprite() {
		sprite.update(body.getPosition(), arrowDir);
	}

	private void doDraw(Eye adBatch) {
		if(!isDead)
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

	// make the AgentProperties (AP) for this class of Agent
	public static ObjectProperties makeAP(Pit parentAgent, Vector2 position, Vector2 velocity,
			Direction4 arrowDir, boolean isExpireImmediately) {
		ObjectProperties props = Agent.createPointAP(KidIcarusKV.AgentClassAlias.VAL_PIT_ARROW,
				position, velocity);
		props.put(AgencyKV.Spawn.KEY_START_PARENT_AGENT, parentAgent);
		props.put(CommonKV.KEY_DIRECTION, arrowDir);
		if(isExpireImmediately)
			props.put(CommonKV.Spawn.KEY_EXPIRE, true);
		return props;
	}
}
