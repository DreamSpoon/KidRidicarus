package kidridicarus.game.Metroid.agent.other.deathpop;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.Metroid.MetroidGfx;

class DeathPopSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(32);
	private static final float SPRITE_HEIGHT = UInfo.P2M(32);
	private static final float ANIM_SPEED = 1f/60f;

	private Animation<TextureRegion> anim;
	private float animTimer;

	DeathPopSprite(TextureAtlas atlas, Vector2 pos) {
		anim = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(MetroidGfx.NPC.DEATH_POP),
				PlayMode.NORMAL);
		animTimer = 0f;
		setRegion(anim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		postFrameInput(SprFrameTool.place(pos));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput))
			return;
		animTimer += frameInput.frameTime.timeDelta;
		setRegion(anim.getKeyFrame(animTimer));
		postFrameInput(frameInput);
	}
}
