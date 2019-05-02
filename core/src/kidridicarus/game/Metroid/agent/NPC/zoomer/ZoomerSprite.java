package kidridicarus.game.Metroid.agent.NPC.zoomer;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.MetroidGfx;

public class ZoomerSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.05f;

	private Animation<TextureRegion> walkAnim;
	private Animation<TextureRegion> injuryAnim;
	private float animTimer;

	public ZoomerSprite(TextureAtlas atlas, Vector2 position) {
		walkAnim = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(MetroidGfx.NPC.ZOOMER), PlayMode.LOOP);
		injuryAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidGfx.NPC.ZOOMER_HIT), PlayMode.LOOP);
		animTimer = 0f;
		setRegion(walkAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setOrigin(SPRITE_WIDTH/2f, SPRITE_HEIGHT/2f);
		postFrameInput(SprFrameTool.place(position));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput))
			return;
		animTimer += frameInput.frameTime.time;
		SpriteFrameInput frameOut = new SpriteFrameInput(frameInput);
		// set region according to move state
		switch(((ZoomerSpriteFrameInput) frameInput).moveState) {
			case WALK:
				setRegion(walkAnim.getKeyFrame(animTimer));
				break;
			case INJURY:
				setRegion(injuryAnim.getKeyFrame(animTimer));
				break;
			case DEAD:
				break;
		}
		postFrameInput(frameOut);
	}
}
