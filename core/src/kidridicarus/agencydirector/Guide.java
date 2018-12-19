package kidridicarus.agencydirector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.ADefFactory;
import kidridicarus.agency.Agency;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.info.UInfo;
import kidridicarus.tools.KeyboardMapping;

public class Guide implements Disposable {
	private static final float SPAWN_TRIGGER_WIDTH = UInfo.P2M(UInfo.TILEPIX_X * 30);
	private static final float SPAWN_TRIGGER_HEIGHT = UInfo.P2M(UInfo.TILEPIX_X * 15);

	private Mario agent;
	private BasicInputs bi;

	public Guide(Agency agency, Vector2 position) {
		agent = (Mario) agency.createAgent(ADefFactory.makeMarioDef(this, position));
		bi = new BasicInputs();
		agency.createAgent(ADefFactory.makeAgentSpawnTriggerDef(this, position,
				SPAWN_TRIGGER_WIDTH, SPAWN_TRIGGER_HEIGHT));
	}

	public void handleInput() {
		bi.wantsToGoRight = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RIGHT);
		bi.wantsToGoUp = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_UP);
		bi.wantsToGoLeft = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_LEFT);
		bi.wantsToGoDown = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_DOWN);
		bi.wantsToRun = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RUN);
		bi.wantsToJump = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_JUMP);
	}

	public Mario getAgent() {
		return agent;
	}

	public int getPointTotal() {
		if(agent instanceof Mario)
			return ((Mario) agent).getPointTotal();
		return 0;
	}

	public int getCoinTotal() {
		if(agent instanceof Mario)
			return ((Mario) agent).getCoinTotal();
		return 0;
	}

	public void update(float delta) {
		agent.setFrameInputs(bi);
	}

	@Override
	public void dispose() {
		agent.dispose();
	}
}
