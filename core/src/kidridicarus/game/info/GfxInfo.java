package kidridicarus.game.info;

import kidridicarus.agency.tool.DrawOrder;
import kidridicarus.agency.tool.DrawOrderAlias;

public class GfxInfo {
	public static final int V_WIDTH = 400;
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

	public static class LayerDrawOrderAlias {
		public static final String NONE = "none";
		public static final String MAP_BOTTOM = "map_bottom";
		public static final String MAP_MIDDLE = "map_middle";
		public static final String MAP_TOP = "map_top";
		public static final String SPRITE_BOTTOM = "sprite_bottom";
		public static final String SPRITE_MIDDLE = "sprite_middle";
		public static final String SPRITE_TOP = "sprite_top";

		private static final DrawOrderAlias[] aliasMasterList = new DrawOrderAlias[] {
				new DrawOrderAlias(NONE, LayerDrawOrder.NONE),
				new DrawOrderAlias(MAP_BOTTOM, LayerDrawOrder.MAP_BOTTOM),
				new DrawOrderAlias(MAP_MIDDLE, LayerDrawOrder.MAP_MIDDLE),
				new DrawOrderAlias(MAP_TOP, LayerDrawOrder.MAP_TOP),
				new DrawOrderAlias(SPRITE_BOTTOM, LayerDrawOrder.SPRITE_BOTTOM),
				new DrawOrderAlias(SPRITE_MIDDLE, LayerDrawOrder.SPRITE_MIDDLE),
				new DrawOrderAlias(SPRITE_TOP, LayerDrawOrder.SPRITE_TOP)
			};

		public static DrawOrderAlias getByString(String aliasStr) {
			// find the enum value with matching alias string
			for(int i=0; i<aliasMasterList.length; i++) {
				if(aliasStr.equals(aliasMasterList[i].alias))
					return aliasMasterList[i];
			}
			// no enum value found, so return null
			return null;
		}
	}
}
