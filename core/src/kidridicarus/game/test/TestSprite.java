package kidridicarus.game.test;

public abstract class TestSprite {
//	public void processFrame(TransferThing processFrame) {
//	}

	public abstract SubProcessor getFrameProcessor();

	protected abstract class Processor {
		protected abstract void innerProcessFrame(TransferThing duh);
	}
	public abstract class SubProcessor extends Processor {
		public void processFrame(SubTransferThing duh2) {
			innerProcessFrame(duh2);
		}
	}
}
