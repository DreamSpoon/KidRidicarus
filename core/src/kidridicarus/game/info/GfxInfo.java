package kidridicarus.game.info;

import kidridicarus.agency.tool.DrawOrder;
import kidridicarus.agency.tool.DrawOrderAlias;

public class GfxInfo {
	public static final int V_WIDTH = 256;
	public static final int V_HEIGHT = 240;
	// DEBUG: used to quickly change size of screen on desktop without affecting aspect ratio
	public static final int DESKTOP_SCALE = 2;

	public static class LayerDrawOrder {
		public static final DrawOrder NONE = new DrawOrder(false, 0f);
		public static final DrawOrder MAP_BOTTOM = new DrawOrder(true, 1f);
		public static final DrawOrder MAP_MIDDLE = new DrawOrder(true, 3f);
		public static final DrawOrder MAP_TOP = new DrawOrder(true, 5f);
		public static final DrawOrder SPRITE_BOTTOM = new DrawOrder(true, 2f);
		public static final DrawOrder SPRITE_MIDDLE = new DrawOrder(true, 4f);
		public static final DrawOrder SPRITE_TOP = new DrawOrder(true, 6f);
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
}
