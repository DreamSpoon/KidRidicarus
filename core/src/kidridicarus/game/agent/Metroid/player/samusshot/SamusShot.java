package kidridicarus.game.agent.Metroid.player.samusshot;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.game.agent.Metroid.player.samus.Samus;
import kidridicarus.game.info.MetroidKV;

public class SamusShot extends Agent implements DisposableAgent {
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

	public SamusShot(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		moveStateTimer = 0f;
		parent = properties.get(AgencyKV.Spawn.KEY_START_PARENT_AGENT, null, Samus.class);

		// check the definition properties, maybe the shot needs to expire immediately
		isExploding = properties.containsKey(CommonKV.Spawn.KEY_EXPIRE);
		if(isExploding)
			moveState = MoveState.EXPLODE;
		else
			moveState = MoveState.LIVE;

		body = new SamusShotBody(this, agency.getWorld(), Agent.getStartPoint(properties),
				Agent.getStartVelocity(properties));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doUpdate(delta); }
		});
		sprite = new SamusShotSprite(agency.getAtlas(), body.getPosition());
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
		// check for agents needing damage, and damage the first one
		for(ContactDmgTakeAgent agent : body.getContactAgentsByClass(ContactDmgTakeAgent.class)) {
			// do not hit parent
			if(agent == parent)
				continue;
			agent.onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
			isExploding = true;
			return;
		}
		// if hit a wall then explode
		if(body.isHitBound())
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

	private void processSprite(float delta) {
		sprite.update(delta, body.getPosition(), moveState);
	}

	private void doDraw(AgencyDrawBatch batch) {
		if(moveState != MoveState.DEAD)
			batch.draw(sprite);
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
		ObjectProperties props = Agent.createPointAP(MetroidKV.AgentClassAlias.VAL_SAMUS_SHOT,
				position, velocity);
		props.put(AgencyKV.Spawn.KEY_START_PARENT_AGENT, parentAgent);
		if(isExpireImmediately)
			props.put(CommonKV.Spawn.KEY_EXPIRE, true);
		return props;
	}
}
