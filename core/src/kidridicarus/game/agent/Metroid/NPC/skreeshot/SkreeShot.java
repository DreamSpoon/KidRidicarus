package kidridicarus.game.agent.Metroid.NPC.skreeshot;

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
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.info.MetroidKV;

public class SkreeShot extends PlacedBoundsAgent implements DisposableAgent {
	private static final float LIVE_TIME = 0.167f;
	private static final float GIVE_DAMAGE = 5f;

	private SkreeShotBody body;
	private SkreeShotSprite sprite;
	private float moveStateTimer;
	private RoomBox lastKnownRoom;

	public SkreeShot(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		moveStateTimer = 0f;
		lastKnownRoom = null;
		body = new SkreeShotBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.getVelocity(properties));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.POST_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doPostUpdate(); }
		});
		sprite = new SkreeShotSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
			@Override
			public void draw(Eye adBatch) { doDraw(adBatch); }
		});
	}

	// apply damage to all contacting agents
	private void doContactUpdate() {
		for(ContactDmgTakeAgent agent : body.getSpine().getContactDmgTakeAgents())
			agent.onTakeDamage(this, GIVE_DAMAGE, body.getPosition());
	}

	private void doUpdate(float delta) {
		if(moveStateTimer > LIVE_TIME) {
			agency.removeAgent(this);
			return;
		}

		sprite.update(body.getPosition());
		moveStateTimer += delta;

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);
	}

	private void doPostUpdate() {
		// update last known room if not dead, so dead player moving through other RoomBoxes won't cause problems
		if(moveStateTimer <= LIVE_TIME) {
			RoomBox nextRoom = body.getSpine().getCurrentRoom();
			if(nextRoom != null)
				lastKnownRoom = nextRoom;
		}
	}

	private void doDraw(Eye adBatch) {
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

	public static ObjectProperties makeAP(Vector2 position, Vector2 velocity) {
		return AP_Tool.createPointAP(MetroidKV.AgentClassAlias.VAL_SKREE_SHOT, position, velocity);
	}
}
