package kidridicarus.game.SMB1.agent.item.fireflower;

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
import kidridicarus.game.info.SMB1_Gfx;

public class FireFlowerSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.2f;

	private FireFlower parent;
	private AgentDrawListener myDrawListener;
	private Animation<TextureRegion> anim;
	private float animTimer;

	public FireFlowerSprite(FireFlower parent, TextureAtlas atlas, Vector2 position) {
		this.parent = parent;
		anim = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(SMB1_Gfx.Item.FIRE_FLOWER), PlayMode.LOOP);
		animTimer = 0f;
		final FireFlowerSprite self = this;
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
		if(((SproutSpriteFrameInput) frameInput).finishSprout)
			finishSprout();
		animTimer += frameInput.frameTime.timeDelta;
		setRegion(anim.getKeyFrame(animTimer));
		postFrameInput(frameInput);
	}

	private void finishSprout() {
		final FireFlowerSprite self = this;
		// change from bottom to middle sprite draw order
		parent.getAgency().removeAgentDrawListener(parent, myDrawListener);
		myDrawListener = new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(self); }
			};
		parent.getAgency().addAgentDrawListener(parent, CommonInfo.DrawOrder.SPRITE_MIDDLE, myDrawListener);
	}
}
