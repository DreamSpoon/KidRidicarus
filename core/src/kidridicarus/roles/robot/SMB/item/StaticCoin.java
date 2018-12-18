package kidridicarus.roles.robot.SMB.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.bodies.SMB.StaticCoinBody;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.roles.robot.ItemBot;
import kidridicarus.sprites.SMB.StaticCoinSprite;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class StaticCoin implements RobotRole, ItemBot {
	private MapProperties properties;
	private RoleWorld runner;
	private StaticCoinSprite coinSprite;
	private StaticCoinBody coinBody;

	public StaticCoin(RoleWorld runner, RobotRoleDef rdef) {
		properties = rdef.properties;
		this.runner = runner;
		coinSprite = new StaticCoinSprite(runner.getEncapTexAtlas(), rdef.bounds.getCenter(new Vector2()));
		coinBody = new StaticCoinBody(this, runner.getWorld(), rdef.bounds.getCenter(new Vector2()));

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
			runner.destroyRobot(this);
		}
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
	public MapProperties getProperties() {
		return properties;
	}

	@Override
	public void dispose() {
		coinBody.dispose();
	}
}