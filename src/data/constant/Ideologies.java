package ac.data.constant;

import java.util.HashMap;
import java.util.TreeSet;

public class Ideologies {
	public enum Ideology {
		NONE,
		CONFUCIAN,
		LEGALISM,
		TAOISM,
		MOHISM,
		BUDDHISM,
		RETAINER,
		NOMINATION,
		NINE_RANK_SYSTEM,
		IMPERIAL_EXAM,
		AGRICULURAL_WAR,
		DUJUN,
		MILITARY_BUREAU
	}
	
	public enum IdeologyType {
		RELIGION,
		CIVIL,
		MILITARY,
	}
	
	public static final int kMaxIdeologyTypes = IdeologyType.values().length;
	
	private static IdeologyType[] types = { null, IdeologyType.RELIGION, IdeologyType.RELIGION, IdeologyType.RELIGION, IdeologyType.RELIGION, IdeologyType.RELIGION,
			IdeologyType.CIVIL, IdeologyType.CIVIL, IdeologyType.CIVIL, IdeologyType.CIVIL,
			IdeologyType.MILITARY, IdeologyType.MILITARY, IdeologyType.MILITARY,
	};
	
	public static HashMap<Ideology, IdeologyType> ideology_types = new HashMap<Ideology, IdeologyType>() {private static final long serialVersionUID = 1L; {
		for (int i = 0; i < Ideology.values().length; ++i) {
			put(Ideology.values()[i], types[i]);
		}
	}};
	public static HashMap<IdeologyType, TreeSet<Ideology>> typed_ideologies = new HashMap<IdeologyType, TreeSet<Ideology>>() {private static final long serialVersionUID = 1L; {
		for (IdeologyType type : IdeologyType.values()) {
			put(type, new TreeSet<Ideology>());
		}
		for (int i = 1; i < Ideology.values().length; ++i) {
			get(types[i]).add(Ideology.values()[i]);
		}
	}};
}
