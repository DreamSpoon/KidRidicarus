package kidridicarus.game.agent.sprite.SMB;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.info.UInfo;
import kidridicarus.game.info.SMBAnim;

public class BumpTileSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.133f;

	private TextureRegion prebumpTex;
	private Animation<TextureRegion> qBlockAnim;
	private Animation<TextureRegion> emptyblockTex;
	private float totalTime;
	private boolean doNotDraw;

	// if prebumpTex is null it means this bumpable tile 
	public BumpTileSprite(TextureAtlas atlas, TextureRegion prebumpTex) {
		this.prebumpTex = prebumpTex;

		emptyblockTex = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(SMBAnim.General.QBLOCKEMPTY), PlayMode.LOOP);
		qBlockAnim = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(SMBAnim.General.QBLOCK), PlayMode.LOOP);

		doNotDraw = true;
		if(prebumpTex != null) {
			doNotDraw = false;
			setRegion(prebumpTex);
		}
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);

		totalTime = 0f;
	}

	public void update(float delta, float globalTimer, Vector2 position, boolean isQ, boolean isEmpty) {
		if(isEmpty) {
			setRegion(emptyblockTex.getKeyFrame(totalTime, true));
			doNotDraw = false;
		}
		// q block?
		else if(isQ)
			setRegion(qBlockAnim.getKeyFrame(globalTimer, true));
		// has a texture?
		else if(prebumpTex != null) {
			doNotDraw = false;
			setRegion(prebumpTex);
		}
		// inivisible bump tile
		else
			doNotDraw = true;

		setPosition(position.x-getWidth()/2f, position.y-getHeight()/2f);
		totalTime += delta;
	}

	@Override
	public void draw(Batch batch) {
		if(!doNotDraw)
			super.draw(batch);
	}
}
