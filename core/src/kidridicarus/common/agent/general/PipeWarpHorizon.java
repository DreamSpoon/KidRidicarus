package kidridicarus.common.agent.general;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.tool.Direction4;

public class PipeWarpHorizon {
	public Direction4 direction;
	public Rectangle bounds; 

	public PipeWarpHorizon(Direction4 direction, Rectangle bounds) {
		this.direction = direction;
		this.bounds = bounds;
	}
}
