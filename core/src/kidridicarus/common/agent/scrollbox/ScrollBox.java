package kidridicarus.common.agent.scrollbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.followbox.FollowBox;
import kidridicarus.common.agent.followbox.FollowBoxBody;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;

public abstract class ScrollBox extends FollowBox implements Disposable {
	private static final float SHORT_DIM = UInfo.P2M(4f);
	private static final float LONG_DIM = UInfo.P2M(32f);
	private static final float OFFSET = UInfo.P2M(10f);

	private FollowBoxBody body;
	private Direction4 scrollDir;

	public abstract FollowBoxBody createScrollBoxBody(ScrollBox parent, World world, Rectangle bounds);

	public ScrollBox(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		scrollDir = properties.get(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);

		// the position is used, but the bounds width and height will be ignored
		float width;
		float height;
		switch(scrollDir) {
			case RIGHT:
			case LEFT:
				width = SHORT_DIM * UInfo.TILEPIX_X;
				height = LONG_DIM * UInfo.TILEPIX_Y;
				break;
			case UP:
			case DOWN:
				width = LONG_DIM * UInfo.TILEPIX_X;
				height = SHORT_DIM * UInfo.TILEPIX_Y;
				break;
			default:
				throw new IllegalStateException("Cannot create scroll push box with scrollDir = " + scrollDir);
		}
		Vector2 pos = Agent.getStartPoint(properties);
		Rectangle bounds = new Rectangle(pos.x, pos.y, width, height);
		body = createScrollBoxBody(this, agency.getWorld(), bounds);
	}

	@Override
	protected FollowBoxBody getFollowBoxBody() {
		return body;
	}

	/*
	 * Get view center, add offset based on scroll direction, set target from offset position.
	 */
	@Override
	public void setTarget(Vector2 position) {
		Vector2 offsetCenter = position.cpy();
		switch(scrollDir) {
			case RIGHT:
				// box is left of center, and moves right
				offsetCenter.x = position.x - OFFSET * UInfo.TILEPIX_X;
				break;
			case UP:
				// box is below center, and moves up
				offsetCenter.y = position.y - OFFSET * UInfo.TILEPIX_Y;
				break;
			case LEFT:
				// box is right of center, and moves left
				offsetCenter.x = position.x + OFFSET * UInfo.TILEPIX_X;
				break;
			case DOWN:
			default:
				// box is above center, and moves down
				offsetCenter.y = position.y + OFFSET * UInfo.TILEPIX_Y;
				break;
		}
		super.setTarget(offsetCenter);
	}

	@Override
	public void dispose() {
		body.dispose();
	}
}
