package kidridicarus.agent.sprites.SMB;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import kidridicarus.info.TileIDs;
import kidridicarus.info.UInfo;
import kidridicarus.tools.EncapTexAtlas;

public class BumpTileSprite extends Sprite {
	private static final float QANIM_SPEED = 0.133f;

	private TextureRegion prebumpTex;
	private Animation<TextureRegion> qblockAnim;
	private TextureRegion emptyblockTex;
	private float totalTime;
	private boolean doNotDraw;

	// if prebumpTex is null it means this bumpable tile 
	public BumpTileSprite(EncapTexAtlas encapTexAtlas, TextureRegion prebumpTex) {
		if(encapTexAtlas == null)
			throw new IllegalArgumentException("tilesets to pull bumptile images from must be non-null.");

		this.prebumpTex = prebumpTex;
		emptyblockTex = encapTexAtlas.getTexForID(TileIDs.COIN_EMPTY);

		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(encapTexAtlas.getTexForID(TileIDs.ANIMQ_BLINK1));
		frames.add(encapTexAtlas.getTexForID(TileIDs.ANIMQ_BLINK2));
		frames.add(encapTexAtlas.getTexForID(TileIDs.ANIMQ_BLINK3));
		frames.add(encapTexAtlas.getTexForID(TileIDs.ANIMQ_BLINK2));
		frames.add(encapTexAtlas.getTexForID(TileIDs.ANIMQ_BLINK1));
		frames.add(encapTexAtlas.getTexForID(TileIDs.ANIMQ_BLINK1));
		qblockAnim = new Animation<TextureRegion>(QANIM_SPEED, frames);

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
			setRegion(emptyblockTex);
			doNotDraw = false;
		}
		// q block?
		else if(isQ)
			setRegion(qblockAnim.getKeyFrame(totalTime, true));
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
