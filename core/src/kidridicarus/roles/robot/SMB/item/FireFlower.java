package kidridicarus.roles.robot.SMB.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.GameInfo;
import kidridicarus.GameInfo.SpriteDrawOrder;
import kidridicarus.InfoSMB.PowerupType;
import kidridicarus.bodies.SMB.FireFlowerBody;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.roles.robot.ItemBot;
import kidridicarus.sprites.SMB.FireFlowerSprite;
import kidridicarus.worldrunner.WorldRunner;

public class FireFlower implements RobotRole, ItemBot {
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = GameInfo.P2M(-13f);

	private WorldRunner runner;
	private FireFlowerSprite flowerSprite;
	private FireFlowerBody ffbody;
	private float stateTimer;
	private boolean isSprouting;
	private Vector2 sproutingPosition;

	public FireFlower(WorldRunner runner, Vector2 position) {
		this.runner = runner;

		flowerSprite = new FireFlowerSprite(runner.getAtlas(), position.cpy().add(0f, SPROUT_OFFSET));

		sproutingPosition = position;

		stateTimer = 0f;
		isSprouting = true;
		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.BOTTOM);
	}

	@Override
	public void update(float delta) {
		float yOffset = 0f;
		if(isSprouting) {
			if(stateTimer > SPROUT_TIME) {
				isSprouting = false;
				runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
				ffbody = new FireFlowerBody(this, runner.getWorld(), sproutingPosition);
			}
			else
				yOffset = SPROUT_OFFSET * (SPROUT_TIME - stateTimer) / SPROUT_TIME;

			flowerSprite.update(delta, sproutingPosition.cpy().add(0f, yOffset));
		}
		else
			flowerSprite.update(delta, ffbody.getPosition());

		// increment state timer
		stateTimer += delta;
	}

	@Override
	public void draw(Batch batch){
		flowerSprite.draw(batch);
	}

	@Override
	public void use(PlayerRole role) {
		if(stateTimer > SPROUT_TIME && role instanceof MarioRole) {
			((MarioRole) role).applyPowerup(PowerupType.FIREFLOWER);
			runner.removeRobot(this);
		}
	}

	@Override
	public void setActive(boolean active) {
		ffbody.setActive(active);
	}

	@Override
	public Vector2 getPosition() {
		return ffbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return ffbody.getBounds();
	}

	@Override
	public void dispose() {
		ffbody.dispose();
	}
}