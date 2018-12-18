package kidridicarus.tools;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.info.KVInfo;
import kidridicarus.info.SMBInfo;
import kidridicarus.info.SMBInfo.PointAmount;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.worldrunner.Player;
import kidridicarus.worldrunner.RobotRoleDef;

public class RRDefFactory {
	public static RobotRoleDef makeGoombaDef(Vector2 position) {
		RobotRoleDef rdef = makePointBoundsDef(position);
		rdef.properties.put(KVInfo.KEY_ROBOTROLECLASS, KVInfo.VAL_GOOMBA);

		return rdef;
	}

	public static RobotRoleDef makeTurtleDef(Vector2 position) {
		RobotRoleDef rdef = makePointBoundsDef(position);
		rdef.properties.put(KVInfo.KEY_ROBOTROLECLASS, KVInfo.VAL_TURTLE);

		return rdef;
	}

	public static RobotRoleDef makePowerMushroomDef(Vector2 position) {
		RobotRoleDef rdef = makePointBoundsDef(position);
		rdef.properties.put(KVInfo.KEY_ROBOTROLECLASS, KVInfo.VAL_MUSHROOM);
		return rdef;
	}

	public static RobotRoleDef makeFireFlowerDef(Vector2 position) {
		RobotRoleDef rdef = makePointBoundsDef(position);
		rdef.properties.put(KVInfo.KEY_ROBOTROLECLASS, KVInfo.VAL_FIREFLOWER);
		return rdef;
	}

	public static RobotRoleDef makeMushroom1UPDef(Vector2 position) {
		RobotRoleDef rdef = makePointBoundsDef(position);
		rdef.properties.put(KVInfo.KEY_ROBOTROLECLASS, KVInfo.VAL_MUSH1UP);
		return rdef;
	}

	public static RobotRoleDef makeBrickPieceDef(Vector2 position, Vector2 velocity, int startFrame) {
		RobotRoleDef rdef = makePointBoundsDef(position);
		rdef.velocity.set(velocity);
		rdef.properties.put(KVInfo.KEY_ROBOTROLECLASS, KVInfo.VAL_BRICKPIECE);
		rdef.properties.put(KVInfo.KEY_STARTFRAME, startFrame);
		return rdef;
	}

	public static RobotRoleDef makeSpinCoinDef(Vector2 position) {
		RobotRoleDef rdef = makePointBoundsDef(position);
		rdef.properties.put(KVInfo.KEY_ROBOTROLECLASS, KVInfo.VAL_SPINCOIN);
		return rdef;
	}

	public static RobotRoleDef makeMarioFireballDef(Vector2 position, boolean right,
			MarioRole player) {
		RobotRoleDef rdef = makePointBoundsDef(position);
		rdef.userData = player;
		rdef.properties.put(KVInfo.KEY_ROBOTROLECLASS, KVInfo.VAL_MARIOFIREBALL);
		if(right)
			rdef.properties.put(KVInfo.KEY_DIRECTION, KVInfo.VAL_RIGHT);
		else
			rdef.properties.put(KVInfo.KEY_DIRECTION, KVInfo.VAL_LEFT);
		return rdef;
	}

	public static RobotRoleDef makeRobotSpawnTriggerDef(Player player, Vector2 position, float width,
			float height) {
		RobotRoleDef rdef = makeBoxBoundsDef(position, width, height);
		rdef.properties.put(KVInfo.KEY_ROBOTROLECLASS, KVInfo.VAL_ROBOTSPAWN_TRIGGER);
		rdef.userData = player;
		return rdef;
	}

	public static RobotRoleDef makeFloatingPointsDef(PointAmount amt, boolean relative,
			Vector2 position, float yOffset, MarioRole player) {
		RobotRoleDef rdef = makePointBoundsDef(position.cpy().add(0f, yOffset));
		rdef.properties.put(KVInfo.KEY_ROBOTROLECLASS, KVInfo.VAL_FLOATINGPOINTS);
		rdef.userData = player;
		rdef.properties.put(KVInfo.KEY_POINTAMOUNT, SMBInfo.pointAmountToStr(amt));
		if(relative)
			rdef.properties.put(KVInfo.KEY_RELPOINTAMOUNT, KVInfo.VAL_TRUE);
		return rdef;
	}

	private static RobotRoleDef makePointBoundsDef(Vector2 position) {
		RobotRoleDef rdef = new RobotRoleDef();
		rdef.bounds = new Rectangle(position.x, position.y, 0f, 0f);
		return rdef;
	}

	private static RobotRoleDef makeBoxBoundsDef(Vector2 position, float width, float height) {
		RobotRoleDef rdef = new RobotRoleDef();
		rdef.bounds = new Rectangle(position.x, position.y, width, height);
		return rdef;
	}
}
