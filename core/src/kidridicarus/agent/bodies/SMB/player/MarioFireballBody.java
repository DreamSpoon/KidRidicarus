package kidridicarus.agent.bodies.SMB.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.player.MarioFireball;
import kidridicarus.agent.bodies.MobileAgentBody;
import kidridicarus.agent.bodies.optional.AgentContactBody;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;

public class MarioFireballBody extends MobileAgentBody implements AgentContactBody {
	private static final float BODY_WIDTH = UInfo.P2M(7f);
	private static final float BODY_HEIGHT = UInfo.P2M(7f);

	private MarioFireball parent;

	public MarioFireballBody(MarioFireball parent, World world, Vector2 position, Vector2 velocity) {
		this.parent = parent;
		defineBody(world, position, velocity);
	}

	private void defineBody(World world, Vector2 position, Vector2 velocity) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		BodyDef bdef = new BodyDef();
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.gravityScale = 2f;	// heavy
		bdef.type = BodyDef.BodyType.DynamicBody;
		FixtureDef fdef = new FixtureDef();
		fdef.friction = 0f;		// slippery
		fdef.restitution = 1f;	// bouncy
		fdef.filter.categoryBits = GameInfo.AGENT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT | GameInfo.AGENT_BIT;
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public Agent getParent() {
		return parent;
	}

	@Override
	protected void onContactVertBoundLine(LineSeg seg) {
		parent.onContactBoundLine(seg);
	}

	@Override
	public void onContactAgent(AgentBody agent) {
		parent.onContactAgent(agent.getParent());
	}

	public void setGravityScale(float scale) {
		b2body.setGravityScale(scale);
	}
}
