package kidridicarus.agency.guide;

public class SuperAdvice extends BasicAdvice {
	public boolean action0;
	public boolean action1;

	public SuperAdvice() {
		clear();
	}

	@Override
	public void clear() {
		super.clear();
		action0 = false;
		action1 = false;
	}
}
