package kidridicarus.game.agent.KidIcarus.other.smallpoof;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.KidIcarusGfx;

public class SmallPoofSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 2f/15f;

	private Animation<TextureRegion> poofAnim;
	private float stateTimer;

	public SmallPoofSprite(TextureAtlas atlas, Vector2 pos) {
		poofAnim = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.General.SMALL_POOF),
				PlayMode.NORMAL);
		stateTimer = 0f;
		setRegion(poofAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setOrigin(SPRITE_WIDTH/2f, SPRITE_HEIGHT/2f);
		setPosition(pos.x - getWidth()/2f, pos.y - getHeight()/2f);
	}

	public void update(float delta) {
		setRegion(poofAnim.getKeyFrame(stateTimer));
		stateTimer += delta;
	}
}
