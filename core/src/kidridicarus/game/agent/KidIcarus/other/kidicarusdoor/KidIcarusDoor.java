package kidridicarus.game.agent.KidIcarus.other.kidicarusdoor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.SolidAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.playerspawner.PlayerSpawner;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;

// note: solid when closed, non-solid when open
public class KidIcarusDoor extends Agent implements SolidAgent, DisposableAgent {
	private KidIcarusDoorBody body;
	private KidIcarusDoorSprite sprite;
	private boolean isOpened;

	public KidIcarusDoor(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		// start in the "is open" state if the agent is not supposed to expire (i.e. close) immediately
		isOpened = !properties.containsKV(CommonKV.Spawn.KEY_EXPIRE, true);

		body = new KidIcarusDoorBody(this, agency.getWorld(), Agent.getStartPoint(properties), isOpened);
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new KidIcarusDoorSprite(agency.getAtlas(), body.getPosition(), isOpened);
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
			@Override
			public void draw(Eye adBatch) { doDraw(adBatch); }
		});
	}

	private void doContactUpdate() {
		if(!isOpened)
			return;

		Agent exitSpawner = Agency.getTargetAgent(agency,
				properties.get(CommonKV.Script.KEY_TARGET_NAME, "", String.class));
		// exit this method if the exit spawner is the wrong class
		if(!(exitSpawner instanceof PlayerSpawner))
			return;

		for(PlayerAgent agent : body.getSpine().getPlayerContacts())
			agent.getSupervisor().startScript(new KidIcarusDoorScript(this, exitSpawner));
	}

	private void doUpdate(float delta) {
		sprite.update(body.getPosition(), isOpened);
	}

	private void doDraw(Eye adBatch) {
		adBatch.draw(sprite);
	}

	public void setOpened(boolean isOpened) {
		body.setOpened(isOpened);
		this.isOpened = isOpened;
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
}
