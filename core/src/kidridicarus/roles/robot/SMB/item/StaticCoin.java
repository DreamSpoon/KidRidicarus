package kidridicarus.roles.robot.SMB.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.GameInfo;
import kidridicarus.GameInfo.SpriteDrawOrder;
import kidridicarus.bodies.SMB.StaticCoinBody;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.roles.robot.ItemBot;
import kidridicarus.sprites.SMB.StaticCoinSprite;
import kidridicarus.worldrunner.WorldRunner;

public class StaticCoin implements RobotRole, ItemBot {
	private WorldRunner runner;
	private StaticCoinSprite coinSprite;
	private StaticCoinBody coinBody;

	public StaticCoin(WorldRunner runner, MapObject object) {
		this.runner = runner;

		Rectangle bounds = ((RectangleMapObject) object).getRectangle();
		Vector2 position = new Vector2(GameInfo.P2M(bounds.getX() + bounds.getWidth() / 2f),
				GameInfo.P2M(bounds.getY() + bounds.getHeight() / 2f));
		coinSprite = new StaticCoinSprite(runner.getAtlas(), position);
		coinBody = new StaticCoinBody(this, runner.getWorld(), position);

		runner.enableRobotUpdate(this);
		runner.setRobotDrawLayer(this, SpriteDrawOrder.BOTTOM);
	}

	@Override
	public void update(float delta) {
		coinSprite.update(delta);
	}

	@Override
	public void draw(Batch batch){
		coinSprite.draw(batch);
	}

	@Override
	public void use(PlayerRole role) {
		if(role instanceof MarioRole) {
			((MarioRole) role).giveCoin();
			runner.removeRobot(this);
		}
	}

	@Override
	public void setActive(boolean active) {
		coinBody.setActive(active);
	}

	@Override
	public Vector2 getPosition() {
		return coinBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return coinBody.getBounds();
	}

	@Override
	public void dispose() {
		coinBody.dispose();
	}
}