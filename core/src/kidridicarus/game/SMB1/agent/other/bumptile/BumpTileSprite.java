package kidridicarus.game.SMB1.agent.other.bumptile;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.SMB1_Gfx;

public class BumpTileSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.133f;

	private TextureRegion prebumpTex;
	private TextureRegion emptyblockTex;
	private Animation<TextureRegion> qbAnim;
	private boolean isQblock;

	// if prebumpTex is null then this tile is invisible until bumped
	public BumpTileSprite(TextureAtlas atlas, TextureRegion prebumpTex, Vector2 position, boolean isQblock) {
		this.prebumpTex = prebumpTex;
		emptyblockTex = atlas.findRegion(SMB1_Gfx.General.QBLOCK_EMPTY);
		qbAnim = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(SMB1_Gfx.General.QBLOCK), PlayMode.LOOP);
		this.isQblock = isQblock;
		if(isQblock)
			setRegion(qbAnim.getKeyFrame(0f));
		else if(prebumpTex != null)
			setRegion(prebumpTex);
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		// tile is visible if it has a pre-bump texture
		postFrameInput(new SpriteFrameInput(prebumpTex != null, position, false, false, false));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput.visible))
			return;
		// empty block?
		if(((BumpTileSpriteFrameInput) frameInput).isEmpty)
			setRegion(emptyblockTex);
		// q block?
		else if(isQblock)
			setRegion(qbAnim.getKeyFrame(((BumpTileSpriteFrameInput) frameInput).timeDelta));
		// block has texture?
		else if(prebumpTex != null)
			setRegion(prebumpTex);
		postFrameInput(frameInput);
	}
}
