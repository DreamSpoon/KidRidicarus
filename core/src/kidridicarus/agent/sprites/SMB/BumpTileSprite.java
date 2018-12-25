package kidridicarus.agent.sprites.SMB;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.info.SMBAnim;
import kidridicarus.info.UInfo;

public class BumpTileSprite extends Sprite {
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

//		Array<TextureRegion> frames = new Array<TextureRegion>();
//		frames.add(encapTexAtlas.getTexForID(TileIDs.ANIMQ_BLINK1));
//		frames.add(encapTexAtlas.getTexForID(TileIDs.ANIMQ_BLINK2));
//		frames.add(encapTexAtlas.getTexForID(TileIDs.ANIMQ_BLINK3));
//		frames.add(encapTexAtlas.getTexForID(TileIDs.ANIMQ_BLINK2));
//		frames.add(encapTexAtlas.getTexForID(TileIDs.ANIMQ_BLINK1));
//		frames.add(encapTexAtlas.getTexForID(TileIDs.ANIMQ_BLINK1));
//		qblockAnim = new Animation<TextureRegion>(QANIM_SPEED, frames);
// QANIM_SPEED = 0.133f

		doNotDraw = true;
		if(prebumpTex != null) {
			doNotDraw = false;
			setRegion(prebumpTex);
		}
		setBounds(getX(), getY(), UInfo.P2M(UInfo.TILEPIX_X), UInfo.P2M(UInfo.TILEPIX_Y));

		totalTime = 0f;
	}

	public void update(float delta, Vector2 position, boolean isQ, boolean isEmpty) {
		if(isEmpty) {
			setRegion(emptyblockTex.getKeyFrame(totalTime, true));
			doNotDraw = false;
		}
		// q block?
		else if(isQ)
			setRegion(qBlockAnim.getKeyFrame(totalTime, true));
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
