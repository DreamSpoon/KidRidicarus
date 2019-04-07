package kidridicarus.game.agent.KidIcarus.other.vanishpoof;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.KidIcarusGfx;

public class VanishPoofSprite extends Sprite {
	private static final float SML_SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SML_SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float BIG_SPRITE_WIDTH = UInfo.P2M(16);
	private static final float BIG_SPRITE_HEIGHT = UInfo.P2M(16);

	private static final float ANIM_SPEED = 2f/15f;

	private Animation<TextureRegion> poofAnim;
	private float stateTimer;

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
		stateTimer = 0f;
		setRegion(poofAnim.getKeyFrame(0f));
//		setOrigin(SPRITE_WIDTH/2f, SPRITE_HEIGHT/2f);
		setPosition(pos.x - getWidth()/2f, pos.y - getHeight()/2f);
	}

	public void update(float delta) {
		setRegion(poofAnim.getKeyFrame(stateTimer));
		stateTimer += delta;
	}
}
