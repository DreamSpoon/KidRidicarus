package kidridicarus.game.SMB1.agent.other.bumptile;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentSprite;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.SMB1_Gfx;

public class BumpTileSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.133f;

	private TextureRegion prebumpTex;
	private TextureRegion emptyblockTex;
	private Animation<TextureRegion> qBlockAnim;
	private boolean isQblock;

	// if prebumpTex is null then this tile is invisible until bumped
	public BumpTileSprite(TextureAtlas atlas, TextureRegion prebumpTex, Vector2 position, boolean isQblock) {
		// tile is visible if it has a pre-bump texture
		super(prebumpTex != null);
		this.prebumpTex = prebumpTex;
		emptyblockTex = atlas.findRegion(SMB1_Gfx.General.QBLOCKEMPTY);
		qBlockAnim = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(SMB1_Gfx.General.QBLOCK), PlayMode.LOOP);
		this.isQblock = isQblock;
		if(isQblock)
			setRegion(qBlockAnim.getKeyFrame(0f));
		else if(prebumpTex != null)
			setRegion(prebumpTex);
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x-getWidth()/2f, position.y-getHeight()/2f);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		// empty block?
		if(((BumpTileSpriteFrameInput) frameInput).isEmpty)
			setRegion(emptyblockTex);
		// q block?
		else if(isQblock)
			setRegion(qBlockAnim.getKeyFrame(((BumpTileSpriteFrameInput) frameInput).timeDelta));
		// block has texture?
		else if(prebumpTex != null)
			setRegion(prebumpTex);
		applyFrameInput(frameInput);
	}
}
