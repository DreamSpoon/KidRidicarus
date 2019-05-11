package kidridicarus.game.SMB1.agent.NPC.turtle;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.BumpTakeAgent;
import kidridicarus.game.SMB1.agent.Koopa;

/*
 * TODO:
 * -do sliding turtle shells break bricks when they strike them?
 *  I couldn't find any maps in SMB 1 that would clear up this matter.
 */
public class Turtle extends CorpusAgent implements Koopa, ContactDmgTakeAgent, BumpTakeAgent {
	private TurtleBrain brain;
	private TurtleSprite sprite;

	public Turtle(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new TurtleBody(this, agentHooks.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.safeGetVelocity(properties));
		brain = new TurtleBrain(this, agentHooks, (TurtleBody) body);
		sprite = new TurtleSprite(agentHooks.getAtlas(), body.getPosition());
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((TurtleBody) body).processContactFrame());
				}
			});
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
			});
		agentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_TOP, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		return brain.onTakeDamage(agent, dmgOrigin);
	}

	@Override
	public void onTakeBump(Agent agent) {
		brain.onTakeBump(agent);
	}
}
