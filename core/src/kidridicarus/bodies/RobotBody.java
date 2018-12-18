package kidridicarus.bodies;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.info.GameInfo;
import kidridicarus.roles.RobotRole;

public abstract class RobotBody implements Disposable {
	private float bodyWidth = 0f;
	private float bodyHeight = 0f;
	protected Body b2body = null;
	public abstract RobotRole getParent();

	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	public void setBodySize(float width, float height) {
		bodyWidth = width;
		bodyHeight = height;
	}

	public Rectangle getBounds() {
		return new Rectangle(b2body.getPosition().x - bodyWidth/2f,
				b2body.getPosition().y - bodyHeight/2f, bodyWidth, bodyHeight);
	}

	public void setActive(boolean active) {
		b2body.setActive(active);
	}

	public void makeUntouchable() {
		// other robots can now pass through the body, but body will not fall through floor
		Filter filter = new Filter();
		filter.categoryBits = GameInfo.ROBOT_BIT;
		filter.maskBits = GameInfo.BOUNDARY_BIT;
		for(Fixture fix : b2body.getFixtureList())
			fix.setFilterData(filter);
	}

	public void disableContacts() {
		// body can pass through everything, to fall off screen
		Filter filter = new Filter();
		filter.categoryBits = GameInfo.NOTHING_BIT;
		filter.maskBits = GameInfo.NOTHING_BIT;
		for(Fixture fix : b2body.getFixtureList())
			fix.setFilterData(filter);
	}

	@Override
	public void dispose() {
		if(b2body != null) {
			b2body.getWorld().destroyBody(b2body);
			b2body = null;
		}
	}
}
