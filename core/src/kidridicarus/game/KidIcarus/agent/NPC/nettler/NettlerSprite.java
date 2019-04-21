package kidridicarus.game.KidIcarus.agent.NPC.nettler;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.KidIcarus.agent.NPC.nettler.NettlerCharacter.NettlerCharacterFrameOutput;
import kidridicarus.game.info.KidIcarusGfx;

public class NettlerSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 2/15f;

	private Animation<TextureRegion> walkAnim;
	private float stateTimer;
	private boolean isVisible;

	public NettlerSprite(TextureAtlas atlas, Vector2 position) {
		walkAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(KidIcarusGfx.NPC.NETTLER), PlayMode.LOOP);
		stateTimer = 0;
		isVisible = true;
		setRegion(walkAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void processFrame(NettlerCharacterFrameOutput characterFrame) {
		isVisible = characterFrame.visible;
		setRegion(walkAnim.getKeyFrame(stateTimer));
		if((characterFrame.isFacingRight && isFlipX()) || (!characterFrame.isFacingRight && !isFlipX()))
			flip(true,  false);
		setPosition(characterFrame.position.x - getWidth()/2f, characterFrame.position.y - getHeight()/2f);
		stateTimer += characterFrame.timeDelta;
	}

	@Override
	public void draw(Batch batch) {
		if(isVisible)
			super.draw(batch);
	}
}
