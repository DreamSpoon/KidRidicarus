package kidridicarus.agency;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.guide.SMBGuide;
import kidridicarus.info.KVInfo;
import kidridicarus.info.SMBInfo;
import kidridicarus.info.SMBInfo.PointAmount;

public class ADefFactory {
	public static AgentDef makeGoombaDef(Vector2 position) {
		AgentDef adef = makePointBoundsDef(position);
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_GOOMBA);

		return adef;
	}

	public static AgentDef makeTurtleDef(Vector2 position) {
		AgentDef adef = makePointBoundsDef(position);
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_TURTLE);

		return adef;
	}

	public static AgentDef makePowerMushroomDef(Vector2 position) {
		AgentDef adef = makePointBoundsDef(position);
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_MUSHROOM);
		return adef;
	}

	public static AgentDef makeFireFlowerDef(Vector2 position) {
		AgentDef adef = makePointBoundsDef(position);
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_FIREFLOWER);
		return adef;
	}

	public static AgentDef makeMushroom1UPDef(Vector2 position) {
		AgentDef adef = makePointBoundsDef(position);
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_MUSH1UP);
		return adef;
	}

	public static AgentDef makePowerStarDef(Vector2 position) {
		AgentDef adef = makePointBoundsDef(position);
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_POWERSTAR);
		return adef;
	}

	public static AgentDef makeBrickPieceDef(Vector2 position, Vector2 velocity, int startFrame) {
		AgentDef adef = makePointBoundsDef(position);
		adef.velocity.set(velocity);
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_BRICKPIECE);
		adef.properties.put(KVInfo.KEY_STARTFRAME, startFrame);
		return adef;
	}

	public static AgentDef makeSpinCoinDef(Vector2 position) {
		AgentDef adef = makePointBoundsDef(position);
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_SPINCOIN);
		return adef;
	}

	public static AgentDef makeMarioFireballDef(Vector2 position, boolean right,
			Mario parentAgent) {
		AgentDef adef = makePointBoundsDef(position);
		adef.userData = parentAgent;
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_MARIOFIREBALL);
		if(right)
			adef.properties.put(KVInfo.KEY_DIRECTION, KVInfo.VAL_RIGHT);
		else
			adef.properties.put(KVInfo.KEY_DIRECTION, KVInfo.VAL_LEFT);
		return adef;
	}

	public static AgentDef makeAgentSpawnTriggerDef(SMBGuide parentGuide, Vector2 position, float width,
			float height) {
		AgentDef adef = makeBoxBoundsDef(position, width, height);
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_AGENTSPAWN_TRIGGER);
		adef.userData = parentGuide;
		return adef;
	}

	public static AgentDef makeFloatingPointsDef(PointAmount amt, boolean relative,
			Vector2 position, float yOffset, Mario parentAgent) {
		AgentDef adef = makePointBoundsDef(position.cpy().add(0f, yOffset));
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_FLOATINGPOINTS);
		adef.userData = parentAgent;
		adef.properties.put(KVInfo.KEY_POINTAMOUNT, SMBInfo.pointAmountToStr(amt));
		if(relative)
			adef.properties.put(KVInfo.KEY_RELPOINTAMOUNT, KVInfo.VAL_TRUE);
		return adef;
	}

	public static AgentDef makeMarioDef(Vector2 position) {
		AgentDef adef = makePointBoundsDef(position);
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_MARIO);
		return adef;
	}

	public static AgentDef makeZoomerDef(Vector2 position) {
		AgentDef adef = makePointBoundsDef(position);
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_ZOOMER);

		return adef;
	}

	public static AgentDef makeSkreeDef(Vector2 position) {
		AgentDef adef = makePointBoundsDef(position);
		adef.properties.put(KVInfo.KEY_AGENTCLASS, KVInfo.VAL_SKREE);

		return adef;
	}

	private static AgentDef makePointBoundsDef(Vector2 position) {
		AgentDef adef = new AgentDef();
		adef.bounds = new Rectangle(position.x, position.y, 0f, 0f);
		return adef;
	}

	private static AgentDef makeBoxBoundsDef(Vector2 position, float width, float height) {
		AgentDef adef = new AgentDef();
		adef.bounds = new Rectangle(position.x, position.y, width, height);
		return adef;
	}
}
