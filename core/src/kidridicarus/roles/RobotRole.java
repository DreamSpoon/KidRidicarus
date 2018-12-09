package kidridicarus.roles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public interface RobotRole extends Disposable {
	public void update(float delta);
	public void draw(Batch batch);
	public void setActive(boolean active);
	public Vector2 getPosition();
	public Rectangle getBounds();
}
