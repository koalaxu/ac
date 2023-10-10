package ac.data.constant;

import java.util.TreeMap;

import ac.data.CityData;
import ac.data.base.Position;
import ac.data.constant.Improvement.SpecialImprovementType;
import ac.data.constant.Tile.Terrain;

public class ConstCityData {
	public Position coordinate;
	public String name;
	
	public double x;
	public double y;
	public String han_county;
	public String han_name;
	public String tang_county;
	public String tang_name;
	public String current_name;
	
	public int rain;
	public int temperature;
	public int flood;
	public int locust;
	public int barbarian;
	
	public int max_irrigated_farm;
	public int max_farm;
	public int max_pasture;
	public int max_ironmine;
	public int max_salt;
	public int max_fish;
	public int max_china;
	public int max_mine;
	public int max_silk;
	
	public Terrain terrain = Terrain.PLAIN;
	public SpecialImprovementType special_improvement = SpecialImprovementType.NONE;
	
	public transient TreeMap<Integer, Double> neighbor_cities = new TreeMap<Integer, Double>();  // neighbor index -> distance
	
	public final static int kMaxProfessions = CityData.Profession.values().length;
	public final static double kGarrisonRatio = 0.05;
	public final static int kMaxHappiness = 100;
	public final static int kMaxRiotPoints = 10000;
}
