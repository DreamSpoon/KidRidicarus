package kidridicarus.common.info;

import kidridicarus.common.tool.AllowOrder;
import kidridicarus.common.tool.DrawOrderAlias;

public class GfxInfo {
	public static final int V_WIDTH = 256;
	public static final int V_HEIGHT = 240;
	// DEBUG: used to quickly change size of screen on desktop without affecting aspect ratio
	public static final int DESKTOP_SCALE = 2;

	public static class LayerDrawOrder {
		public static final AllowOrder NONE = new AllowOrder(false, 0f);
		public static final AllowOrder MAP_BOTTOM = new AllowOrder(true, 1f);
		public static final AllowOrder MAP_MIDDLE = new AllowOrder(true, 3f);
		public static final AllowOrder MAP_TOP = new AllowOrder(true, 5f);
		public static final AllowOrder SPRITE_BOTTOM = new AllowOrder(true, 2f);
		public static final AllowOrder SPRITE_MIDDLE = new AllowOrder(true, 4f);
		public static final AllowOrder SPRITE_TOP = new AllowOrder(true, 6f);
	}

	public static final DrawOrderAlias[] KIDRID_DRAWORDER_ALIAS = new DrawOrderAlias[] {
			new DrawOrderAlias("none", LayerDrawOrder.NONE),
			new DrawOrderAlias("map_bottom", LayerDrawOrder.MAP_BOTTOM),
			new DrawOrderAlias("map_middle", LayerDrawOrder.MAP_MIDDLE),
			new DrawOrderAlias("map_top", LayerDrawOrder.MAP_TOP),
			new DrawOrderAlias("sprite_bottom", LayerDrawOrder.SPRITE_BOTTOM),
			new DrawOrderAlias("sprite_middle", LayerDrawOrder.SPRITE_MIDDLE),
			new DrawOrderAlias("sprite_top", LayerDrawOrder.SPRITE_TOP)
		};

	public static class AgentUpdateOrder {
		public static final AllowOrder NONE = new AllowOrder(false, 0f);
	}
}
