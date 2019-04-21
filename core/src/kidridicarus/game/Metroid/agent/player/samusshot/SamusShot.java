package kidridicarus.game.Metroid.agent.player.samusshot;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.Metroid.agent.player.samus.Samus;
import kidridicarus.game.info.MetroidKV;

public class SamusShot extends PlacedBoundsAgent implements DisposableAgent {
	private static final float LIVE_TIME = 0.217f;
	private static final float EXPLODE_TIME = 3f/60f;
	private static final float GIVE_DAMAGE = 1f;

	enum MoveState { LIVE, EXPLODE, DEAD }

	private Samus parent;
	private SamusShotBody body;
	private SamusShotSprite sprite;
	private MoveState moveState;
	private float moveStateTimer;
	private boolean isExploding;
	private RoomBox lastKnownRoom;
	private Vector2 startVelocity;

	public SamusShot(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		moveStateTimer = 0f;
		parent = properties.get(CommonKV.KEY_PARENT_AGENT, null, Samus.class);
		lastKnownRoom = null;

		// check the definition properties, maybe the shot needs to expire immediately
		isExploding = properties.containsKey(CommonKV.Spawn.KEY_EXPIRE);
		if(isExploding)
			moveState = MoveState.EXPLODE;
		else
			moveState = MoveState.LIVE;

		startVelocity = AP_Tool.getVelocity(properties);
		body = new SamusShotBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				startVelocity);
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doUpdate(delta); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.POST_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doPostUpdate(); }
		});
		sprite = new SamusShotSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
			@Override
			public void draw(Eye adBatch) { doDraw(adBatch); }
		});
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// check for agents needing damage, and damage the first one
		for(ContactDmgTakeAgent agent : body.getSpine().getContactDmgTakeAgents()) {
			// do not hit parent
			if(agent == parent)
				continue;
			agent.onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
			isExploding = true;
			return;
		}
		// if hit a wall then explode
		if(body.getSpine().isMoveBlocked(startVelocity))
			isExploding = true;
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case LIVE:
				break;
			case EXPLODE:
				body.disableAllContacts();
				body.zeroVelocity(true, true);
				break;
			case DEAD:
				agency.removeAgent(this);
				break;
		}

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);

		moveStateTimer = moveState == nextMoveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		// is it dead?
		if(moveState == MoveState.DEAD ||
				(moveState == MoveState.EXPLODE && moveStateTimer > EXPLODE_TIME) ||
				(moveState == MoveState.LIVE && moveStateTimer > LIVE_TIME))
			return MoveState.DEAD;
		// if not dead, then is it exploding?
		else if(isExploding || moveState == MoveState.EXPLODE)
			return MoveState.EXPLODE;
		else
			return MoveState.LIVE;
	}

	private void doPostUpdate() {
		// update last known room if not dead, so dead player moving through other RoomBoxes won't cause problems
		if(moveState != MoveState.DEAD) {
			RoomBox nextRoom = body.getSpine().getCurrentRoom();
			if(nextRoom != null)
				lastKnownRoom = nextRoom;
		}
	}

	private void processSprite(float delta) {
		sprite.update(delta, body.getPosition(), moveState);
	}

	private void doDraw(Eye adBatch) {
		if(moveState != MoveState.DEAD)
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
	public static ObjectProperties makeAP(Samus parentAgent, Vector2 position, Vector2 velocity,
			boolean isExpireImmediately) {
		ObjectProperties props = AP_Tool.createPointAP(MetroidKV.AgentClassAlias.VAL_SAMUS_SHOT,
				position, velocity);
		props.put(CommonKV.KEY_PARENT_AGENT, parentAgent);
		if(isExpireImmediately)
			props.put(CommonKV.Spawn.KEY_EXPIRE, true);
		return props;
	}
}
