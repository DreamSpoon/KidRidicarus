package kidridicarus.game.SMB1.agent.item.fireflower;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.SMB1.agent.other.sproutingpowerup.SproutingPowerupBody;

public class FireFlowerBody extends SproutingPowerupBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);

	public FireFlowerBody(FireFlower parent, World world, Vector2 position) {
		super(parent, world, new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f,
				BODY_WIDTH, BODY_HEIGHT), new Vector2(0f, 0f));
	}
}
