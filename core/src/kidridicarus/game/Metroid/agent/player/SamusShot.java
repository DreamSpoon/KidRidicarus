package kidridicarus.game.Metroid.agent.player;

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
import kidridicarus.game.Metroid.agentbody.player.SamusShotBody;
import kidridicarus.game.Metroid.agentsprite.player.SamusShotSprite;
import kidridicarus.game.info.GameKV;

public class SamusShot extends Agent implements DisposableAgent {
	private static final float LIVE_TIME = 0.217f;
	private static final float EXPLODE_TIME = 3f/60f;

	public enum MoveState { LIVE, EXPLODE, DEAD }

	private Samus parent;
	private SamusShotBody shotBody;
	private SamusShotSprite shotSprite;
	private MoveState curMoveState;
	private boolean isExploding;
	private float stateTimer;

	public SamusShot(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		parent = properties.get(AgencyKV.Spawn.KEY_START_PARENTAGENT, null, Samus.class);

		// check the definition properties, maybe the shot needs to expire immediately
		isExploding = properties.containsKey(CommonKV.Spawn.KEY_EXPIRE);
		if(isExploding)
			curMoveState = MoveState.EXPLODE;
		else
			curMoveState = MoveState.LIVE;
		stateTimer = 0f;

		shotBody = new SamusShotBody(this, agency.getWorld(), Agent.getStartPoint(properties),
				Agent.getStartVelocity(properties));
		shotSprite = new SamusShotSprite(agency.getAtlas(), shotBody.getPosition());

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
		// check for agents needing damage, and damage the first one
		for(ContactDmgTakeAgent agent : shotBody.getContactAgentsByClass(ContactDmgTakeAgent.class)) {
			// do not hit parent
			if(agent == parent)
				continue;
			agent.onDamage(parent, 1f, shotBody.getPosition());
			isExploding = true;
			return;
		}
		// if hit a wall then explode
		if(shotBody.isHitBound())
			isExploding = true;
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case LIVE:
				break;
			case EXPLODE:
				shotBody.disableAllContacts();
				shotBody.zeroVelocity(true, true);
				break;
			case DEAD:
				// call disable contacts, just to be safe
				shotBody.disableAllContacts();
				agency.disposeAgent(this);
				break;
		}

		stateTimer = curMoveState == nextMoveState ? stateTimer+delta : 0f;
		curMoveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		// is it dead?
		if(curMoveState == MoveState.DEAD ||
				(curMoveState == MoveState.EXPLODE && stateTimer > EXPLODE_TIME) ||
				(curMoveState == MoveState.LIVE && stateTimer > LIVE_TIME))
			return MoveState.DEAD;
		// if not dead, then is it exploding?
		else if(isExploding || curMoveState == MoveState.EXPLODE)
			return MoveState.EXPLODE;
		// alive by deduction
		return MoveState.LIVE;
	}

	private void processSprite(float delta) {
		shotSprite.update(delta, shotBody.getPosition(), curMoveState);
	}

	public void doDraw(AgencyDrawBatch batch) {
		if(curMoveState != MoveState.DEAD)
			batch.draw(shotSprite);
	}

	@Override
	public Vector2 getPosition() {
		return shotBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return shotBody.getBounds();
	}

	@Override
	public void disposeAgent() {
		shotBody.dispose();
	}

	// make the AgentProperties (AP) for this class of Agent
	public static ObjectProperties makeAP(Samus parentAgent, Vector2 position, Vector2 velocity) {
		ObjectProperties props = Agent.createPointAP(GameKV.Metroid.AgentClassAlias.VAL_SAMUS_SHOT,
				position, velocity);
		props.put(AgencyKV.Spawn.KEY_START_PARENTAGENT, parentAgent);
		return props;
	}
}
