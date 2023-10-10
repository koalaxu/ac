package ac.engine.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import ac.data.GameData;
import ac.data.base.Pair;
import ac.data.base.Position;
import ac.data.constant.Improvement;
import ac.data.constant.Tile;
import ac.data.constant.Tile.Terrain;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Unit.UnitType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.util.TileUtil;

public class TransportationUtil extends BaseUtil {

	protected TransportationUtil(DataAccessor data, Utils utils) {
		super(data, utils);
	}

	public void UpdateTransportation(City city, HashMap<City, Integer> transportation_cost) {
		HashMap<Position, City> city_positions = new HashMap<Position, City>();
		int max_time = Integer.MAX_VALUE;
		for (Pair<City, Double> neighbor : city.GetDescription().GetNeighbors()) {
			transportation_cost.put(neighbor.first, Integer.MAX_VALUE);
			city_positions.put(neighbor.first.GetCoordinate(), neighbor.first);
		}
		HashMap<Position, Integer> costs = new HashMap<Position, Integer>();
		Queue<Position> queue = new LinkedList<Position>();
		queue.add(city.GetCoordinate());
		costs.put(city.GetCoordinate(), 0);
		int[] transpotation_time = param.march_time[UnitType.SIEGE.ordinal()];
		int cross_river_time = param.cross_river_time[UnitType.SIEGE.ordinal()];
		while (!queue.isEmpty()) {
			Position pos = queue.poll();
			int current_time = costs.get(pos);		
			if (current_time >= max_time) continue;
			Tile tile = GameData.const_data.GetTile(pos);
			for (int i = 0; i < 6; ++i) {
				int time = GetTileTravelTime(null, tile, i, transpotation_time, cross_river_time);
				if (time == Integer.MAX_VALUE) continue;
				Position neighbor = TileUtil.GetNeighborPosition(pos, i);
				time += current_time;
				if (time < costs.getOrDefault(neighbor, Integer.MAX_VALUE)) {
					costs.put(neighbor, time);
					queue.add(neighbor);
					City neighbor_city = city_positions.get(neighbor);
					if (neighbor_city != null && time < transportation_cost.getOrDefault(neighbor_city, -1)) {
						transportation_cost.put(neighbor_city, time);
						max_time = Collections.max(transportation_cost.values());
					}
				}
			}
		}
	}
	
	private int GetTileTravelTime(State state, Tile tile, int direction, int[] transpotation_time, int cross_river_time) {
		Position neighbor = TileUtil.GetNeighborPosition(tile.coordinate, direction);
		Tile neighbor_tile = GameData.const_data.GetTile(neighbor);
		if (neighbor_tile == null || IsAreaEmbargoed(state, neighbor)) return Integer.MAX_VALUE;
		// if (neighbor != null && neighbor.x == 32 && neighbor.y == 14) System.err.println(state);
		int terrain_index = neighbor_tile.terrain.ordinal();
		int time = transpotation_time[terrain_index];
		boolean has_river = BorderHasRiver(tile, direction);
		if (time < Integer.MAX_VALUE) time += (has_river ? cross_river_time : 0);
		int left_direction = Tile.kReverseBorderOrder[(Tile.kBorderOrder[direction] + 5) % 6];
		int right_direction = Tile.kReverseBorderOrder[(Tile.kBorderOrder[direction] + 1) % 6];
		boolean left_area_embargoed = IsAreaEmbargoed(state, TileUtil.GetNeighborPosition(tile.coordinate, left_direction));
		boolean right_area_embargoed = IsAreaEmbargoed(state, TileUtil.GetNeighborPosition(tile.coordinate, right_direction));
		int river_logistic_time = param.river_logistic_time[terrain_index] +
				param.river_logistic_time[tile.terrain.ordinal()];
		if (BorderHasRiver(tile, left_direction) && !left_area_embargoed) {// left has river
			if (BorderHasRiver(neighbor_tile, Tile.kReverseBorderOrder[(Tile.kBorderOrder[5 - direction] + 1) % 6])) {
				time = river_logistic_time / 2;
			} else if (has_river && BorderHasRiver(neighbor_tile, Tile.kReverseBorderOrder[(Tile.kBorderOrder[5 - direction] + 5) % 6])
					&& !right_area_embargoed) {
				time = river_logistic_time;
			}
		} else if (BorderHasRiver(tile, right_direction) && !right_area_embargoed) {  // right has river
			if (BorderHasRiver(neighbor_tile, Tile.kReverseBorderOrder[(Tile.kBorderOrder[5 - direction] + 5) % 6])) {
				time = river_logistic_time / 2;
			} else if (has_river &&
					BorderHasRiver(neighbor_tile, Tile.kReverseBorderOrder[(Tile.kBorderOrder[5 - direction] + 1) % 6])
							&& !left_area_embargoed) {
				time = river_logistic_time;
			}
		}
		return time;
	}
	
	private boolean BorderHasRiver(Tile tile, int direction) {
		if (data.GetOverrides().IsLingAqudectBuilt() && tile.coordinate.x == 33 && tile.coordinate.y == 37) return true;
		return tile.border_has_river[direction];
	}
	
	private boolean IsAreaEmbargoed(State state, Position pos) {
		if (state == null) return false;
		for (Army army : data.GetArmiesByPosition(pos)) {
			if (utils.diplomacy_util.AtEmbargo(state, army.GetState())) return true;
		}
		City city = data.GetCityTerritoryByTile(GameData.const_data.GetTile(pos));
		if (city != null && city.GetPosition().equals(pos)) {
			if (utils.diplomacy_util.AtEmbargo(state, city.GetOwner())) return true;
		}
		return false;
	}
	
	private static class CityTradeInfo {
		public static class Route {
			public Route(int max_cost) {
				this.max_cost = max_cost;
			}
			public ArrayList<City> path = new ArrayList<City>();
			public int cost = 0;
			public int max_cost;
		}
		public HashMap<ImprovementType, Route> trade_routes = new HashMap<ImprovementType, Route>();
	}
	
	public void UpdateTradeRoutes() {
		HashMap<City, CityTradeInfo> city_info = new HashMap<City, CityTradeInfo>();
		for (City city : data.GetAllCities()) {
			city.GetCommerce().Clear();
			CityTradeInfo city_trade_info = new CityTradeInfo();
			for (ImprovementType type : Improvement.kIndustryImprovements) {
				int count = city.GetImprovements().GetCount(type);
				if (count > 0) {
					city_trade_info.trade_routes.put(type, new CityTradeInfo.Route(utils.city_util.MaxRouteLength(city, type)));
				}
			}
			city_info.put(city, city_trade_info);
		}
		for (int i = 0; i < 5; ++i) {
			for (City city : data.GetAllCities()) {
				if (!city.GetOwner().Playable()) continue;
				CityTradeInfo city_trade_info = city_info.get(city);
				for (Entry<ImprovementType, CityTradeInfo.Route> produce_route : city_trade_info.trade_routes.entrySet()) {
					ImprovementType type = produce_route.getKey();
					CityTradeInfo.Route current_route = produce_route.getValue();
					if (current_route.path.size() > i) continue;
					for (Entry<City, Integer> neighbor : city.GetNeighborAndTransportation().entrySet()) {
						City neighbor_city = neighbor.getKey();
						if (!neighbor_city.GetOwner().Playable()) continue;
						if (utils.diplomacy_util.AtEmbargo(city.GetOwner(), neighbor_city.GetOwner())) continue;
						int cost = current_route.cost + neighbor.getValue();
						if (cost > current_route.max_cost) continue;
						CityTradeInfo neighbor_trade_info = city_info.get(neighbor_city);
						CityTradeInfo.Route route = neighbor_trade_info.trade_routes.get(type);
						if (route == null) {
							route = new CityTradeInfo.Route(current_route.max_cost);
							route.cost = Integer.MAX_VALUE;
							neighbor_trade_info.trade_routes.put(type, route);
						}
						if (route.cost > cost) {
							route.max_cost = current_route.max_cost;
							route.cost = cost;
							route.path.clear();
							route.path.addAll(current_route.path);
							route.path.add(city);
						}
					}
				}
			}
		}
		
		for (City city : data.GetAllCities()) {
			CityTradeInfo city_trade_info = city_info.get(city);
			for (Entry<ImprovementType, CityTradeInfo.Route> produce_route : city_trade_info.trade_routes.entrySet()) {
				if (produce_route.getValue().path.isEmpty()) continue;
				ImprovementType type = produce_route.getKey();
				for (int i = 0; i < produce_route.getValue().path.size(); ++i) {
					City node = produce_route.getValue().path.get(i);
					if (i == 0) {
						city.GetCommerce().GetImports().put(type, node);
						node.GetCommerce().IncreaseExport(type);
					} else {
						node.GetCommerce().IncreaseTransfer(type);
					}
				}
			}
		}
	}
	
	public void FindShortestPaths(City city, HashMap<City, Integer> dest_cities) {
		HashSet<City> city_to_reevaluate = new HashSet<City>();
		for (Entry<City, Integer> kv : dest_cities.entrySet()) {
			if (kv.getKey() == city) {
				kv.setValue(0);
				city_to_reevaluate.add(city);
			} else {
				kv.setValue(Integer.MAX_VALUE);
			}
		}
		while (!city_to_reevaluate.isEmpty()) {
			City c = city_to_reevaluate.iterator().next();
			city_to_reevaluate.remove(c);
			int current_cost = dest_cities.get(c);
			for (Entry<City, Integer> kv : c.GetNeighborAndTransportation().entrySet()) {
				City n = kv.getKey();
				Integer cost = dest_cities.get(n);
				if (cost == null) continue;
				if (current_cost + kv.getValue() < cost) {
					dest_cities.put(n, current_cost + kv.getValue());
					city_to_reevaluate.add(city);
				}
			}
		}
	}
	
	public static class Path {
		public int shortest_time = kMaxShortestTime;
		public int next_direction;
		public int next_step_time;
		public static final int kMaxShortestTime = 365;
	}
	
	public Path FindShortestPath(Army army, Position dest) {
		HashMap<Terrain, Integer> terrain_travel_time = new HashMap<Terrain, Integer>();
		HashMap<Tile, Integer> tile_travel_time = new HashMap<Tile, Integer>();
		Path path = new Path();
		Queue<Position> queue = new LinkedList<Position>();
		HashMap<Position, Integer> pos_times = new HashMap<Position, Integer>();
		HashMap<Position, Integer> pos_directions = new HashMap<Position, Integer>();
		pos_times.put(army.GetPosition(), 0);
		queue.add(army.GetPosition());
		while (!queue.isEmpty()) {
			Position pos = queue.poll();
			int current_time = pos_times.get(pos);
			for (int i = 0; i < 6; ++i) {
				Position neighbor = TileUtil.GetNeighborPosition(pos, i);
				Tile tile = data.GetConstData().GetTile(neighbor);
				if (tile == null) continue;
				int time = GetTileTravelTime(terrain_travel_time, tile_travel_time, army, tile);
				if (time == Integer.MAX_VALUE) continue;
				time += current_time;
				if (time >= path.shortest_time) continue;
				if (neighbor.equals(dest)) {
					path.shortest_time = time;
					pos_times.put(neighbor, time);
					pos_directions.put(neighbor, i);
				} else if (pos_times.getOrDefault(neighbor, Integer.MAX_VALUE) > time) {
					pos_times.put(neighbor, time);
					pos_directions.put(neighbor, i);
					queue.add(neighbor);
				}
			}
		}
		if (path.shortest_time >= Path.kMaxShortestTime) return null;
		Position pos = new Position(dest);
		Position cur = null;
		while (!army.GetPosition().equals(pos)) {
			cur = pos;
			path.next_direction = pos_directions.get(cur);
			pos = TileUtil.GetNeighborPosition(cur, 5 - path.next_direction);
		}
		if (!pos_times.containsKey(cur)) {
//			System.err.println(army.GetState().GetName());
//			System.err.println(army.GetPosition());
//			System.err.println(pos);
//			System.err.println(dest);
//			System.err.println(cur);
//			for (Entry<Position, Integer> kv : pos_times.entrySet()) {
//				System.err.println(kv.getKey() + " = " + kv.getValue());
//			}
//			System.err.println("---");
//			for (Entry<Position, Integer> kv : pos_directions.entrySet()) {
//				System.err.println(kv.getKey() + " = " + kv.getValue());
//			}
//			System.err.println("---");
			pos = new Position(dest);
			cur = null;
			while (!army.GetPosition().equals(pos)) {
				cur = pos;
				path.next_direction = pos_directions.get(cur);
				pos = TileUtil.GetNeighborPosition(cur, 5 - path.next_direction);
			}
		}
		if (cur == null) return null;
		path.next_step_time = pos_times.get(cur);
		if (path.next_step_time >= Path.kMaxShortestTime) return null;
		return path;
	}
	
	public boolean IsTileAccessible(Army army, Tile tile) {
		if (army.GetStatus() == Army.Status.RETREAT) return true;
		State state = army.GetState();
		City city = data.GetCityTerritoryByTile(tile);
		if (city != null) {
			State other_state = city.GetOwner();
			if (state != other_state) {
				if (!(other_state == null ||
						utils.diplomacy_util.BorderOpened(other_state, state) || utils.diplomacy_util.DeclareWar(state, other_state))) {
					return false;
				} else {
					if (utils.diplomacy_util.DeclareWar(state, other_state)) {
						if (city.GetPosition().equals(tile.coordinate) &&
								city.GetMilitary().GetGarrison() != army.GetTarget()) {  // Can not pass other city
							return false;
						}
					}
					Collection<Army> armies = data.GetArmiesByPosition(tile.coordinate);
					int foreign_army_count = 0;
					for (Army other_army : armies) {
						if (other_army.GetState() != other_state && ++foreign_army_count >= Tile.kMaxArmies) {
							return false;
						}
					}
					for (Army other_army : armies) {
						other_state = other_army.GetState();
						if (state != other_state && !utils.diplomacy_util.DeclareWar(state, other_state)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	private int GetTileTravelTime(HashMap<Terrain, Integer> terrain_travel_time, HashMap<Tile, Integer> tile_travel_time,
			Army army, Tile tile) {
		Integer time = tile_travel_time.get(tile);
		if (time != null) return time;
		time = terrain_travel_time.get(tile.terrain);
		if (time == null) {
			time = utils.army_util.GetTerrainTravelTime(army, tile.terrain);
			terrain_travel_time.put(tile.terrain, time);
		}
		if (!IsTileAccessible(army, tile)) {
			time = Integer.MAX_VALUE;
		}
//		State state = army.GetState();
//		City city = data.GetCityTerritoryByTile(tile);
//		if (city != null) {
//			State other_state = city.GetOwner();
//			if (state != other_state) {
//				if (!(other_state == null ||
//						utils.diplomacy_util.BorderOpened(other_state, state) || utils.diplomacy_util.DeclareWar(state, other_state))) {
//					time = Integer.MAX_VALUE;
//				} else {
//					for (Army other_army : data.GetArmiesByPosition(tile.coordinate)) {
//						other_state = other_army.GetState();
//						if (state != other_state && !utils.diplomacy_util.DeclareWar(state, other_state)) {
//							time = Integer.MAX_VALUE;
//							break;
//						}
//					}
//				}
//			}
//		}
		tile_travel_time.put(tile, time);
		return time;
	}
	
	public Pair<City, Integer> GetSupplyRoute(Army army) {
		City nearest_city = null;
		int min_cost = Integer.MAX_VALUE;
		State state = army.GetState();
		HashMap<Position, Integer> pos_costs = new HashMap<Position, Integer>();
		// LinkedList<Position> queue = new LinkedList<Position>();
		
		PriorityQueue<Position> queue = new PriorityQueue<Position>(
				new Comparator<Position>() {
					@Override
					public int compare(Position o1, Position o2) {
						return pos_costs.get(o1) - pos_costs.get(o2);
					}
					
		});
		//queue.add(new Pair<Position, Integer>(army.GetPosition(), 0));
		pos_costs.put(army.GetPosition(), 0);
		queue.add(army.GetPosition());
		int[] transpotation_time = param.march_time[UnitType.SIEGE.ordinal()];
		int cross_river_time = param.cross_river_time[UnitType.SIEGE.ordinal()];
		while(!queue.isEmpty()) {
			//Pair<Position, Integer> iter = queue.poll();
			Position pos = queue.poll();
			int cost = pos_costs.get(pos);
			if (cost >= min_cost) break;
			Tile tile = data.GetConstData().GetTile(pos);
			City city = data.GetCityTerritoryByTile(tile);
			// if (city != null && city.GetName().equals("夏阳")) System.err.println(city.GetPosition());
			if (city != null && city.GetOwner() == army.GetState() && city.GetPosition().equals(pos)) {
				nearest_city = city;
				min_cost = cost;
				continue;
			}
			
			for (int i = 0; i < 6; ++i) {
				int time = GetTileTravelTime(state, tile, i, transpotation_time, cross_river_time);
				if (time == Integer.MAX_VALUE) continue;
				Position neighbor = TileUtil.GetNeighborPosition(pos, i);
				time += cost;
				if (time < min_cost && time < pos_costs.getOrDefault(neighbor, Integer.MAX_VALUE)) {
					queue.remove(neighbor);
					pos_costs.put(neighbor, time);
					queue.add(neighbor);
				}
			}
		}
		if (nearest_city == null) return null;
		return new Pair<City, Integer>(nearest_city, min_cost);
	}

	public City GetNearestCity(State state, City target_city) {
		int min_cost = Integer.MAX_VALUE;
		City nearest_city = null;
		for (City city : state.GetOwnedCities()) {
			int cost = city.GetTransportation(target_city);
			if (cost == Integer.MAX_VALUE || cost >= min_cost) continue;
			nearest_city = city;
		}
		return nearest_city;
	}
}
