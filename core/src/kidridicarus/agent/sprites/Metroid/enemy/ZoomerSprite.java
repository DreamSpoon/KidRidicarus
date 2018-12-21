package kidridicarus.agent.sprites.Metroid.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import kidridicarus.agent.Metroid.enemy.Zoomer.ZoomerState;
import kidridicarus.info.GameInfo;
import kidridicarus.info.GameInfo.Direction4;
import kidridicarus.info.UInfo;
import kidridicarus.tools.EncapTexAtlas;

public class ZoomerSprite extends Sprite {
	private static final float ANIM_SPEED = 0.05f;

	private Animation<TextureRegion> walkAnimation;
	private float stateTimer;

	public ZoomerSprite(EncapTexAtlas encapTexAtlas, Vector2 position) {
		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_M_ZOOMER, 0, 0, 16, 16));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_M_ZOOMER, 16, 0, 16, 16));
		walkAnimation = new Animation<TextureRegion>(ANIM_SPEED, frames);

		stateTimer = 0;

		setRegion(walkAnimation.getKeyFrame(0f));
		setBounds(getX(), getY(), UInfo.P2M(16), UInfo.P2M(16));
		setOrigin(UInfo.P2M(8f), UInfo.P2M(8f));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	// TODO: change angle of sprite based on upDir
	public void update(float delta, Vector2 position, ZoomerState curState, Direction4 upDir) {
		switch(curState) {
			case WALK:
				setRegion(walkAnimation.getKeyFrame(stateTimer, true));
				break;
			case DEAD:
				break;
		}

		stateTimer += delta;
		switch(upDir) {
			case RIGHT:
				setRotation(270);
				break;
			case UP:
				setRotation(0);
				break;
			case LEFT:
				setRotation(90);
				break;
			case DOWN:
				setRotation(180);
				break;
		}
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}
