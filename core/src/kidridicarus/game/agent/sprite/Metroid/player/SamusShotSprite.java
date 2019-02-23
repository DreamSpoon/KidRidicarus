package kidridicarus.game.agent.sprite.Metroid.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.info.UInfo;
import kidridicarus.game.agent.Metroid.player.SamusShot.MoveState;
import kidridicarus.game.info.MetroidAnim;

public class SamusShotSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(8);
	private static final float ANIM_SPEED = 1f/60f;

	private Animation<TextureRegion> liveAnim;
	private Animation<TextureRegion> explodeAnim;

	private float stateTimer;
	private MoveState lastMoveState;

	public SamusShotSprite(TextureAtlas atlas, Vector2 position) {
		liveAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.SHOT), PlayMode.LOOP);
		explodeAnim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidAnim.Player.SHOT_EXP), PlayMode.LOOP);
		stateTimer = 0f;
		lastMoveState = null;
		setRegion(liveAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position, MoveState curMoveState) {
		stateTimer = curMoveState == lastMoveState ? stateTimer : 0f;
		lastMoveState = curMoveState;
		switch(curMoveState) {
			case LIVE:
			case DEAD:
				setRegion(liveAnim.getKeyFrame(stateTimer));
				break;
			case EXPLODE:
				setRegion(explodeAnim.getKeyFrame(stateTimer));
				break;
		}
		stateTimer += delta;
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}
