package kidridicarus.game.SMB1.agent.NPC.turtle;

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

public class TurtleSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(24);
	private static final float ANIM_SPEED = 0.25f;

	private Animation<TextureRegion> walkAnim;
	private TextureRegion insideShell;
	private Animation<TextureRegion> wakeUpAnim;
	private float animTimer;

	public TurtleSprite(TextureAtlas atlas, Vector2 position) {
		walkAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMB1_Gfx.NPC.TURTLE_WALK), PlayMode.LOOP);
		wakeUpAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMB1_Gfx.NPC.TURTLE_WAKEUP), PlayMode.LOOP);
		insideShell = atlas.findRegion(SMB1_Gfx.NPC.TURTLE_HIDE);
		animTimer = 0f;
		setRegion(walkAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		postFrameInput(SprFrameTool.place(position));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput))
			return;
		SpriteFrameInput frameOut = new SpriteFrameInput(frameInput);
		animTimer += frameInput.frameTime.time;
		switch(((TurtleSpriteFrameInput) frameInput).moveState) {
			case WALK:
			case FALL:
				setRegion(walkAnim.getKeyFrame(animTimer));
				break;
			case HIDE:
			case SLIDE:
				setRegion(insideShell);
				break;
			case WAKE:
				setRegion(wakeUpAnim.getKeyFrame(animTimer));
				break;
			case DEAD:
				setRegion(insideShell);
				// upside down when dead
				frameOut.flipY = true;
				break;
		}
		postFrameInput(frameOut);
	}
}
