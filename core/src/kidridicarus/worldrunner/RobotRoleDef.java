package kidridicarus.worldrunner;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class RobotRoleDef {
	public Rectangle bounds;
	public Vector2 velocity;
	public TextureRegion tileTexRegion;
	// the loader will derive the robot class from the properties
	public MapProperties properties;
	public Object userData;

	public RobotRoleDef() {
		bounds = new Rectangle();
		velocity = new Vector2(0f, 0f);
		properties = new MapProperties();
		tileTexRegion = null;
		userData = null;
	}
}
