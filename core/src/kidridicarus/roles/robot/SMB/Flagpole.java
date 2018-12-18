package kidridicarus.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.bodies.SMB.FlagpoleBody;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.UInfo;
import kidridicarus.roles.RobotRole;
import kidridicarus.sprites.SMB.PoleFlagSprite;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class Flagpole implements RobotRole {
	private static final float DROP_TIME = 1.35f;

	private MapProperties properties;
	// offset is from top-left of flagpole bounds
	private static final Vector2 FLAG_START_OFFSET = new Vector2(UInfo.P2M(-4), UInfo.P2M(-16));
	private FlagpoleBody fpbody;
	private PoleFlagSprite flagSprite;
	private Vector2 flagPos;
	private Vector2 initFlagPos;
	private boolean isAtBottom;
	private float dropTimer;

	public Flagpole(RoleWorld runner, RobotRoleDef rdef) {
		properties = rdef.properties;
		fpbody = new FlagpoleBody(this, runner.getWorld(), rdef.bounds);
		isAtBottom = false;
		dropTimer = 0f;
		initFlagPos = FLAG_START_OFFSET.cpy().add(rdef.bounds.x, rdef.bounds.y+rdef.bounds.height);
		flagPos = initFlagPos;
		flagSprite = new PoleFlagSprite(runner.getEncapTexAtlas(), flagPos);

		runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
		runner.enableRobotUpdate(this);
	}

	@Override
	public void update(float delta) {
		if(isAtBottom)
			return;
		if(dropTimer > 0f) {
			flagPos = initFlagPos.cpy().add(0f, -(fpbody.getBounds().height - UInfo.P2M(32)) * (DROP_TIME - dropTimer) / DROP_TIME);

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
		dropTimer = DROP_TIME;
	}

	public boolean isAtBottom() {
		return isAtBottom;
	}

	@Override
	public Vector2 getPosition() {
		return fpbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return fpbody.getBounds();
	}

	@Override
	public MapProperties getProperties() {
		return properties;
	}

	@Override
	public void dispose() {
		fpbody.dispose();
	}
}
