package ac.engine.util;

import ac.engine.data.DataAccessor;

public class Utils {
	public Utils(DataAccessor data) {
		army_util = new ArmyUtil(data, this);
		city_util = new CityUtil(data, this);
		diplomacy_util= new DiplomacyUtil(data, this);
		person_util = new PersonUtil(data, this);
		pop_util = new PopulationUtil(data, this);
		prod_util = new ProductionUtil(data, this);
		state_util = new StateUtil(data, this);
		trans_util= new TransportationUtil(data, this);
		player_util = new PlayerUtil(data, this);
	}
	
	public ArmyUtil army_util;
	public CityUtil city_util;
	public DiplomacyUtil diplomacy_util;
	public PersonUtil person_util;
	public PopulationUtil pop_util;
	public ProductionUtil prod_util;
	public StateUtil state_util;
	public TransportationUtil trans_util;
	public PlayerUtil player_util;
}
