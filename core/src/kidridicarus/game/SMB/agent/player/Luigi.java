package kidridicarus.game.SMB.agent.player;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.AgentSupervisor;
import kidridicarus.common.agent.GameAgentObserver;
import kidridicarus.common.agent.general.Room;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.SMB.agentbody.player.LuigiBody;
import kidridicarus.game.SMB.agentsprite.player.LuigiSprite;
import kidridicarus.game.tool.QQ;

public class Luigi extends Agent implements PlayerAgent, DisposableAgent {
	public enum PowerState {
			SMALL, BIG, FIRE;
			public boolean isBigBody() { return !this.equals(SMALL); }
		}
	private enum MoveState { STAND }

	private LuigiSupervisor supervisor;
	private LuigiObserver observer;
	private LuigiBody luigiBody;
	private LuigiSprite luigiSprite;

	private MoveState curMoveState;
	private float moveStateTimer;
	private PowerState powerState;
	private boolean isFacingRight;

	public Luigi(Agency agency, ObjectProperties properties) {
		super(agency, properties);
QQ.pr("you made Luigi so happy!");
		curMoveState = MoveState.STAND;
		moveStateTimer = 0f;
		isFacingRight = false;
		powerState = PowerState.SMALL;

		luigiBody = new LuigiBody(this, agency.getWorld(), Agent.getStartPoint(properties), powerState.isBigBody());
		luigiSprite = new LuigiSprite(agency.getAtlas(), luigiBody.getPosition(), powerState);
		observer = new LuigiObserver(this);
		supervisor = new LuigiSupervisor(this);
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doUpdate(delta); }
		});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOP, new AgentDrawListener() {
			@Override
			public void draw(AgencyDrawBatch batch) { doDraw(batch); }
		});
	}

	private void doUpdate(float delta) {
		luigiSprite.update(delta, luigiBody.getPosition(), powerState);
	}

	private void doDraw(AgencyDrawBatch batch) {
		batch.draw(luigiSprite);
	}

	@Override
	public AgentSupervisor getSupervisor() {
		return supervisor;
	}

	@Override
	public GameAgentObserver getObserver() {
		return observer;
	}

	@Override
	public Room getCurrentRoom() {
		return luigiBody.getCurrentRoom();
	}

	@Override
	public Vector2 getPosition() {
		return luigiBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return luigiBody.getBounds();
	}

	@Override
	public void disposeAgent() {
		luigiBody.dispose();
	}
}
