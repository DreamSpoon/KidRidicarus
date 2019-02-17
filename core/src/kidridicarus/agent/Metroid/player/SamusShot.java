package kidridicarus.agent.Metroid.player;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.body.Metroid.player.SamusShotBody;
import kidridicarus.agent.optional.DamageableAgent;
import kidridicarus.agent.sprite.Metroid.player.SamusShotSprite;
import kidridicarus.info.KVInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;

public class SamusShot extends Agent {
	private static final float LIVE_TIME = 0.217f;

	private Samus parent;
	private SamusShotBody shotBody;
	private SamusShotSprite shotSprite;
	private float stateTimer;

	// TODO: check if shot is being created inside solid area, and change to shot explosion if so
	public SamusShot(Agency agency, AgentDef adef) {
		super(agency, adef);

		parent = (Samus) adef.userData;

		stateTimer = 0f;

		shotBody = new SamusShotBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()), adef.velocity);
		shotSprite = new SamusShotSprite(agency.getAtlas(), shotBody.getPosition());

		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
	}

	@Override
	public void update(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// if hit a wall
		if(shotBody.isHitBound()) {
			agency.disposeAgent(this);
			return;
		}

		// check for agents needing damage, and damage the first one
		for(Agent a : shotBody.getContactAgentsByClass(DamageableAgent.class)) {
			// do not hit parent
			if(a == parent)
				continue;
			((DamageableAgent) a).onDamage(parent, 1f, shotBody.getPosition());
			agency.disposeAgent(this);
			return;
		}
	}

	private void processMove(float delta) {
		// dispose if dead
		if(stateTimer > LIVE_TIME)
			agency.disposeAgent(this);
		stateTimer += delta;
	}

	private void processSprite(float delta) {
		shotSprite.update(delta, shotBody.getPosition());
	}

	@Override
	public void draw(Batch batch) {
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
		AgentDef adef = AgentDef.makePointBoundsDef(KVInfo.VAL_SAMUS_SHOT, position);
		adef.velocity.set(velocity);
		adef.userData = parentAgent;
		return adef;
	}
}
