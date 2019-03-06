package kidridicarus.game.SMB.agent.other;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DrawableAgent;
import kidridicarus.agency.agent.UpdatableAgent;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agentscript.FlagpoleScript;
import kidridicarus.game.SMB.agentbody.other.FlagpoleBody;
import kidridicarus.game.SMB.agentsprite.other.PoleFlagSprite;
import kidridicarus.game.info.GameKV;
import kidridicarus.game.info.GfxInfo;

public class Flagpole extends Agent implements UpdatableAgent, DrawableAgent {
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

		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_MIDDLE);
		agency.enableAgentUpdate(this);
	}

	@Override
	public void update(float delta) {
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

	@Override
	public void draw(Batch batch) {
		flagSprite.draw(batch);
	}

	public void startDrop() {
		isAtBottom = false;
		dropTimer = FLAGDROP_TIME;
	}

	public boolean isAtBottom() {
		return isAtBottom;
	}

	// check if the user is a player agent, if so then give the agent's supervisor a Flagpole script to run 
	public boolean use(Agent agent) {
		if(!(agent instanceof PlayerAgent))
			return false;

		// if the supervisor starts (uses) the script then start the flag drop
		if(((PlayerAgent) agent).getSupervisor().startScript(new FlagpoleScript(fpBody.getBounds(),
				agent.getProperty(GameKV.Script.KEY_SPRITESIZE, null, Vector2.class)))) {
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
	public void dispose() {
		fpBody.dispose();
	}
}
