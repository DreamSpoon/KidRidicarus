package kidridicarus.game.Metroid.agent.player;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDef;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.common.agent.optional.DamageableAgent;
import kidridicarus.game.Metroid.agentbody.player.SamusShotBody;
import kidridicarus.game.Metroid.agentsprite.player.SamusShotSprite;
import kidridicarus.game.info.GfxInfo;
import kidridicarus.game.info.GameKV;

public class SamusShot extends Agent {
	private static final float LIVE_TIME = 0.217f;
	private static final float EXPLODE_TIME = 3f/60f;

	public enum MoveState { LIVE, EXPLODE, DEAD }

	private Samus parent;
	private SamusShotBody shotBody;
	private SamusShotSprite shotSprite;
	private MoveState curMoveState;
	private boolean isExploding;
	private float stateTimer;

	public SamusShot(Agency agency, AgentDef adef) {
		super(agency, adef);

		parent = (Samus) adef.userData;

		// check the definition properties, maybe the shot needs to expire immediately
		isExploding = properties.containsKey(AgencyKV.Spawn.KEY_EXPIRE);
		if(isExploding)
			curMoveState = MoveState.EXPLODE;
		else
			curMoveState = MoveState.LIVE;
		stateTimer = 0f;

		shotBody = new SamusShotBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()), adef.velocity);
		shotSprite = new SamusShotSprite(agency.getAtlas(), shotBody.getPosition());

		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_MIDDLE);
	}

	@Override
	public void update(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// check for agents needing damage, and damage the first one
		for(Agent a : shotBody.getContactAgentsByClass(DamageableAgent.class)) {
			// do not hit parent
			if(a == parent)
				continue;
			((DamageableAgent) a).onDamage(parent, 1f, shotBody.getPosition());
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

	@Override
	public void draw(Batch batch) {
		if(curMoveState != MoveState.DEAD)
			shotSprite.draw(batch);
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
	public Vector2 getVelocity() {
		return shotBody.getVelocity();
	}

	@Override
	public void dispose() {
		shotBody.dispose();
	}

	public static AgentDef makeSamusShotDef(Vector2 position, Vector2 velocity, Samus parentAgent) {
		AgentDef adef = AgentDef.makePointBoundsDef(GameKV.Metroid.VAL_SAMUS_SHOT, position);
		adef.velocity.set(velocity);
		adef.userData = parentAgent;
		return adef;
	}
}
