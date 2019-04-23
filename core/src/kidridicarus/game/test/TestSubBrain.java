package kidridicarus.game.test;

public class TestSubBrain implements TestAgentBrain {
	@Override
	public TransferThing processFrame(float delta) {
		return new SubTransferThing(delta);
	}
}
