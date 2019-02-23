package kidridicarus.agency.tool;

public class DrawOrderAlias {
	public static final String NONE = "none";
	public static final String MAP_BOTTOM = "map_bottom";
	public static final String MAP_MIDDLE = "map_middle";
	public static final String MAP_TOP = "map_top";
	public static final String SPRITE_BOTTOM = "sprite_bottom";
	public static final String SPRITE_MIDDLE = "sprite_middle";
	public static final String SPRITE_TOP = "sprite_top";

	private static final DrawOrderAlias[] aliasMasterList = new DrawOrderAlias[] {
			new DrawOrderAlias(NONE, DrawOrder.NONE),
			new DrawOrderAlias(MAP_BOTTOM, DrawOrder.MAP_BOTTOM),
			new DrawOrderAlias(MAP_MIDDLE, DrawOrder.MAP_MIDDLE),
			new DrawOrderAlias(MAP_TOP, DrawOrder.MAP_TOP),
			new DrawOrderAlias(SPRITE_BOTTOM, DrawOrder.SPRITE_BOTTOM),
			new DrawOrderAlias(SPRITE_MIDDLE, DrawOrder.SPRITE_MIDDLE),
			new DrawOrderAlias(SPRITE_TOP, DrawOrder.SPRITE_TOP)
		};

	public String alias;
	public DrawOrder myDO;

	public DrawOrderAlias(String alias, DrawOrder theDO) {
		this.alias = alias;
		myDO = theDO;
	}

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
