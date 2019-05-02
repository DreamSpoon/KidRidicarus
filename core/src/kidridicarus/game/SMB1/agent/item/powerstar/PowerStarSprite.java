package kidridicarus.game.SMB1.agent.item.powerstar;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.SMB1.agent.item.fireflower.SproutSpriteFrameInput;
import kidridicarus.game.info.SMB1_Gfx;

public class PowerStarSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.075f;

	private PowerStar parent;
	private AgentDrawListener myDrawListener;
	private Animation<TextureRegion> anim;
	private float animTimer;

	public PowerStarSprite(PowerStar parent, TextureAtlas atlas, Vector2 position) {
		this.parent = parent;
		anim = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(SMB1_Gfx.Item.POWER_STAR), PlayMode.LOOP);
		animTimer = 0f;
		final PowerStarSprite self = this;
		myDrawListener = new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(self); }
			};
		parent.getAgency().addAgentDrawListener(parent, CommonInfo.DrawOrder.SPRITE_BOTTOM, myDrawListener);
		setRegion(anim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		postFrameInput(SprFrameTool.place(position));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput))
			return;
		animTimer += frameInput.frameTime.time;
		setRegion(anim.getKeyFrame(animTimer));
		// if finished sprouting then change from bottom draw order to middle draw order
		if(((SproutSpriteFrameInput) frameInput).finishSprout) {
			final PowerStarSprite self = this;
			parent.getAgency().removeAgentDrawListener(parent, myDrawListener);
			myDrawListener = new AgentDrawListener() {
					@Override
					public void draw(Eye eye) { eye.draw(self); }
				};
			parent.getAgency().addAgentDrawListener(parent, CommonInfo.DrawOrder.SPRITE_MIDDLE, myDrawListener);
		}
		postFrameInput(frameInput);
	}
}
