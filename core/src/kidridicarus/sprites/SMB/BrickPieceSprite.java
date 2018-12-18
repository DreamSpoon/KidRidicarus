package kidridicarus.sprites.SMB;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import kidridicarus.info.GameInfo;
import kidridicarus.tools.EncapTexAtlas;

public class BrickPieceSprite extends Sprite {
	private static final float ANIM_SPEED = 0.2f;
	private Animation<TextureRegion> spinAnimation;
	private float stateTimer;

	public BrickPieceSprite(EncapTexAtlas encapTexAtlas, Vector2 position, float size, int startFrame) {
		// add the frames in the correct order
		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_BRICKPIECES, 0, 0, 8, 8));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_BRICKPIECES, 8, 8, 8, 8));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_BRICKPIECES, 8, 0, 8, 8));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_BRICKPIECES, 0, 8, 8, 8));
		spinAnimation = new Animation<TextureRegion>(ANIM_SPEED, frames);

		stateTimer = (float) startFrame * ANIM_SPEED;

		setRegion(spinAnimation.getKeyFrame(0f));
		setBounds(getX(), getY(), size, size);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(Vector2 position, float delta) {
		setRegion(spinAnimation.getKeyFrame(stateTimer, true));
		stateTimer += delta;

		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}
