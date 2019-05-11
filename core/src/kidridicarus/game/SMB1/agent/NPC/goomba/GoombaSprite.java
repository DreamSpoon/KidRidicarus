package kidridicarus.game.SMB1.agent.NPC.goomba;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.SMB1_Gfx;

class GoombaSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.4f;

	private Animation<TextureRegion> walkAnim;
	private TextureRegion squish;
	private float stateTimer;

	GoombaSprite(TextureAtlas atlas, Vector2 position) {
		walkAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMB1_Gfx.NPC.GOOMBA_WALK), PlayMode.LOOP);
		squish = atlas.findRegion(SMB1_Gfx.NPC.GOOMBA_SQUISH);
		stateTimer = 0f;
		setRegion(walkAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		postFrameInput(SprFrameTool.place(position));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput))
			return;
		stateTimer += frameInput.frameTime.timeDelta;
		switch(((GoombaSpriteFrameInput) frameInput).moveState) {
			case DEAD_SQUISH:
				setRegion(squish);
				break;
			case DEAD_BUMP:
				// no walking after bopping
				setRegion(walkAnim.getKeyFrame(0f));
				// upside down when bopped
				frameInput.flipY = true;
				break;
			case WALK:
			default:
				setRegion(walkAnim.getKeyFrame(stateTimer));
				break;
		}
		postFrameInput(frameInput);
	}
}
