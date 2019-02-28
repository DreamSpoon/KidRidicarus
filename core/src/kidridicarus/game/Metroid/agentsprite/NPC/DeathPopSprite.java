package kidridicarus.game.Metroid.agentsprite.NPC;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.info.UInfo;
import kidridicarus.game.info.MetroidAnim;

public class DeathPopSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(32);
	private static final float SPRITE_HEIGHT = UInfo.P2M(32);
	private static final float ANIM_SPEED = 1f/60f;

	private Animation<TextureRegion> popAnim;
	private float stateTimer;

	public DeathPopSprite(TextureAtlas atlas, Vector2 pos) {
		popAnim = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(MetroidAnim.NPC.DEATH_POP),
				PlayMode.NORMAL);
		stateTimer = 0f;
		setRegion(popAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setOrigin(SPRITE_WIDTH/2f, SPRITE_HEIGHT/2f);
		setPosition(pos.x - getWidth()/2f, pos.y - getHeight()/2f);
	}

	public void update(float delta) {
		setRegion(popAnim.getKeyFrame(stateTimer));
		stateTimer += delta;
	}
}
