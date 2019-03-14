package kidridicarus.game.agent.SMB.other.flagpole;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;

public class Flagpole extends Agent implements DisposableAgent {
	public static final float FLAGDROP_TIME = 1.35f;

	// offset is from top-left of flagpole bounds
	private static final Vector2 FLAG_START_OFFSET = new Vector2(UInfo.P2M(-4), UInfo.P2M(-16));
	private FlagpoleBody fpBody;
	private PoleFlagSprite flagSprite;
	private Vector2 flagPos;
	private Vector2 initFlagPos;
	private boolean isAtBottom;
	private float dropTimer;

	public Flagpole(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		fpBody = new FlagpoleBody(this, agency.getWorld(), Agent.getStartBounds(properties));
		isAtBottom = false;
		dropTimer = 0f;
		initFlagPos = FLAG_START_OFFSET.cpy().add(fpBody.getBounds().x,
				fpBody.getBounds().y+fpBody.getBounds().height);
		flagPos = initFlagPos;
		flagSprite = new PoleFlagSprite(agency.getAtlas(), flagPos);

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
		if(isAtBottom)
			return;
		if(dropTimer > 0f) {
			flagPos = initFlagPos.cpy().add(0f,
					-(fpBody.getBounds().height - UInfo.P2M(32)) * (FLAGDROP_TIME - dropTimer) / FLAGDROP_TIME);

			dropTimer -= delta;
			if(dropTimer <= 0f)
				isAtBottom = true;
		}
		else
			flagPos = initFlagPos;

		flagSprite.update(flagPos);
	}

	public void doDraw(AgencyDrawBatch batch) {
		batch.draw(flagSprite);
	}

	private void startDrop() {
		isAtBottom = false;
		dropTimer = FLAGDROP_TIME;
	}

	// check if the user is a player agent, if so then give the agent's supervisor a Flagpole script to run 
	public boolean use(Agent agent) {
		if(!(agent instanceof PlayerAgent))
			return false;

		// if the supervisor starts (uses) the script then start the flag drop
		if(((PlayerAgent) agent).getSupervisor().startScript(new FlagpoleScript(fpBody.getBounds(),
				agent.getProperty(CommonKV.Script.KEY_SPRITESIZE, null, Vector2.class)))) {
			startDrop();
			return true;
		}
		else
			return false;
	}

	@Override
	public Vector2 getPosition() {
		return fpBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return fpBody.getBounds();
	}

	@Override
	public void disposeAgent() {
		fpBody.dispose();
	}
}
