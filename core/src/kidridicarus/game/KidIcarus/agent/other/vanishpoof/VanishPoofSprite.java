package kidridicarus.game.KidIcarus.agent.other.vanishpoof;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.KidIcarusGfx;

public class VanishPoofSprite extends AgentSprite {
	private static final float SML_SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SML_SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float BIG_SPRITE_WIDTH = UInfo.P2M(16);
	private static final float BIG_SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 2f/15f;

	private Animation<TextureRegion> poofAnim;
	private float animTimer;

	public VanishPoofSprite(TextureAtlas atlas, Vector2 pos, Boolean isBig) {
		if(isBig) {
			poofAnim = new Animation<TextureRegion>(ANIM_SPEED,
					atlas.findRegions(KidIcarusGfx.General.BIG_POOF), PlayMode.NORMAL);
			setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
		}
		else {
			poofAnim = new Animation<TextureRegion>(ANIM_SPEED,
					atlas.findRegions(KidIcarusGfx.General.SMALL_POOF), PlayMode.NORMAL);
			setBounds(getX(), getY(), SML_SPRITE_WIDTH, SML_SPRITE_HEIGHT);
		}
		animTimer = 0f;
		setRegion(poofAnim.getKeyFrame(0f));
		postFrameInput(SprFrameTool.place(pos));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput))
			return;
		animTimer += frameInput.frameTime.time;
		setRegion(poofAnim.getKeyFrame(animTimer));
		postFrameInput(frameInput);
	}
}
