package ac.data.constant;

import java.util.ArrayList;

import ac.data.base.Resource;
import ac.data.constant.Improvement.ImprovementType;

public class Parameters {
	
	// Population Parameters
	public double pop_growth_cutoff = 0.75;
	public double pop_decrease_slope = 0.004;
	public double pop_increase_slope = 0.002;
	public double pop_temperature_discount = 0.0005;
	public double pop_increase_cap = 0.003;
	public double pop_decrease_cutoff_for_unhappiness = -0.001;
	public double labor_ratio_for_unhappiness = 0.25;
	public int happiness_pop_decrease_threshold = 80;
	public double pop_decrease_from_unhappiness = 0.001;
	public double profession_shift_ratio = 0.001;
	public int base_min_peasant_pct = 70;
	public int base_max_worker_pct = 10;
	public long city_min_population = 1000;
	public double conquered_city_min_race = 0.05;
	public int max_migration_quantity = 20000;
	public int migration_cost_base = 16;
	public int migration_cost_denominator = 160;
	public double max_migration_cost = 0.95;
	public double[] weight_coefficient_per_city_type = { 1, 3.0, 2.0, 0.5 };
	
	
	// Climate
	public double[] rain_level_prob = { 0.15, 0.7 };  // -1, 0, 1
	public double adjustment_propagate_prob = 0.4;
	public double disaster_risk_multiplier = 0.1;
	public double disaster_base_prob = 0.1;
	public double flood_min_severity = 0.2;
	public double flood_max_severity = 0.7;
	public double locust_min_severity = 0.3;
	public double locust_max_severity = 0.8;
	
	// Tax
	public double tax_efficiency_base = 0.5;
	public double feudal_tax_efficiency = 0.2;
	public double jimi_county_tax_efficiency = 0.1;
	public double county_tax_efficiency = 0.9;
	public double county_tax_efficiency_transportation_penalty = 0.02;
	public double min_county_tax_efficiency = 0.1;
	public double tax_stability_penalty = 0.2;
	public double tax_foreigner_discount = 0.4;
	public double nine_rank_system_tax_nonplayable_foreigner_discount = 0.8;
	public int base_allowed_county_num = 1;
	public double governor_tax_efficiency_boost = 0.2;
	
	// Agriculture
	public int population_per_farm = 3000;
	public double natural_base = 0.4;
	public double ox_bonus = 0.5;
	public double iron_bonus = 1.0;
	public int climate_month = 7;
	public double[] peasant_productivity = { 1.2, 0.4, 0.15 };
	public double labor_punishment_multiplier = 0.3;
	public double max_labor_punishment = 0.8;
	public double governor_algriculture_boost = 0.05;
	
	// Industry	
	public int population_per_industry_improvement = 1000;
	public Resource<Double> GetImprovementYield(ImprovementType type) {
		return improvement_yields.get(type.ordinal());
	}
	public ArrayList<Resource<Double>> improvement_yields = new ArrayList<Resource<Double>>(){
		private static final long serialVersionUID = 1L; {
		add(new Resource<Double>(0.0){{ }});  // NONE
		add(new Resource<Double>(0.0){{ food = 0.0;  }});  // FARM
		add(new Resource<Double>(0.0){{ food = 0.3;  }});  // AQEDUCTED_FARM
		add(new Resource<Double>(0.0){{ food = 0.4;  }});  // IRRIGATED_FARM
		add(new Resource<Double>(0.0){{ food = 0.5;  }});  // OXED_FARM  (multiplier)
		add(new Resource<Double>(0.0){{ food = 1.0;  }});  // IRONED_FARM  (multiplier)
		add(new Resource<Double>(0.0){{ gold = 0.5; hammer = 0.6; }});  // MINE
		add(new Resource<Double>(0.0){{ gold = 1.5; hammer = 0.1; }});  // SILK
		add(new Resource<Double>(0.0){{ gold = 1.0; hammer = 0.5; }});  // CHINA
		add(new Resource<Double>(0.0){{ gold = 1.0; food = 2.0; }});  // FISH
		add(new Resource<Double>(0.0){{ gold = 2.0; food = 0.8; }});  // SALT
		add(new Resource<Double>(0.0){{ iron = 0.5; hammer = 1.0; }});  // IRONMINE
		add(new Resource<Double>(0.0){{ food = 1.5; hammer = 0.2; horse = 0.5;}});  // PASTURE
		add(new Resource<Double>(0.0){{ gold = 0.1; hammer = 0.2; }});  // WORKSHOP
	}};
	public Resource<Long> GetImprovementCost(ImprovementType type) {
		return improvement_cost.get(type.ordinal());
	}

	public ArrayList<Resource<Long>> improvement_cost = new ArrayList<Resource<Long>>(){
		private static final long serialVersionUID = 1L; {
		add(new Resource<Long>(0L){{ }});  // NONE
		add(new Resource<Long>(0L){{ hammer =  1000L; gold = 50L; }});  // FARM
		add(new Resource<Long>(0L){{ hammer = 20000L; gold = 200L;  }});  // AQEDUCTED_FARM
		add(new Resource<Long>(0L){{ }});  // IRRIGATED_FARM
		add(new Resource<Long>(0L){{ hammer =  5000L; food = 1000L;  }});  // OXED_FARM
		add(new Resource<Long>(0L){{ hammer = 20000L; iron = 1000L;  }});  // IRONED_FARM
		add(new Resource<Long>(0L){{ hammer =  2000L; gold = 100L; }});  // MINE
		add(new Resource<Long>(0L){{ hammer =  1000L; gold = 50L; food = 50L;  }});  // SILK
		add(new Resource<Long>(0L){{ hammer =  3000L; gold = 150L; }});  // CHINA
		add(new Resource<Long>(0L){{ hammer =  4000L; gold = 100L; }});  // FISH
		add(new Resource<Long>(0L){{ hammer =  2000L; gold = 200L; }});  // SALT
		add(new Resource<Long>(0L){{ hammer =  4000L; gold = 150L; }});  // IRONMINE
		add(new Resource<Long>(0L){{ hammer =  1000L; food = 1000L; gold = 200L;  }});  // PASTURE
		add(new Resource<Long>(0L){{ }});  // WORKSHOP
		add(new Resource<Long>(0L){{ hammer = 10000L; gold = 50L; food = 1000L;  }});  // DAM
		add(new Resource<Long>(0L){{ hammer = 50000L; gold = 100L; food = 5000L;  }});  // WALL	
		add(new Resource<Long>(0L){{ hammer = 100000L; gold = 500L; food = 50000L;  }});  // SPECIAL		
	}};
	
	public long[] building_labor_cost = {  -1, 0, 1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1000, 5000, 50000 };
	
	public int[] improvement_construction_days = { -1, 180, 90, -1, 30, 30, 60, 60, 60, 60, 60, 90, 30, -1, 30, 30, 30
	};
	
	// Commerce
	public double import_commerce_point = 0.5;
	public double export_commerce_point = 0.5;
	public double transfer_commerce_point = 1.0;
	public int merchant_pct_commerce_point_coverage = 2;
	public double base_commerce_income = 0.2;  // was 0.5
	public double bonus_commerce_income = 0.6;  // was 1.5
	
	// Tech
	public double base_tech_multiplier = 0.1;
	public double tech_penality_half_life_year = 50;
	
	// Building
	public int[] max_auxiliary_improvments = {  0, 10, 20  };
	public int[] auxiliary_improvement_maintenance_cost = { 2, 1, 3 };
	public int[][] special_aqeduct_benefitting_cities = {
			{}, {}, {}, {3, 4}, {104, 105}, {55}, {}, 
	};
	
	// Military
	public int[][] march_time = {
			// PLAIN,HILL,MOUNTAIN,SWAMP,RIVER_VALLEY,FORREST,HIGH_MOUNTAIN,SEA,DESERT
			{ 3, 6, 8, 6, 8, 12, Integer.MAX_VALUE, Integer.MAX_VALUE, 6 },  // MELEE
			{ 3, 6, 8, 6, 8, 12, Integer.MAX_VALUE, Integer.MAX_VALUE, 6 },  // ARCHERY
			{ 1, 4, 12, 12, 12, 12, Integer.MAX_VALUE, Integer.MAX_VALUE, 6 },  // MOUNTED
			{ 4, 6, 12, 12, 12, 12, Integer.MAX_VALUE, Integer.MAX_VALUE, 8 },  // SIEGE
	};
	public int[] river_logistic_time = {
		// PLAIN,HILL,MOUNTAIN,SWAMP,RIVER_VALLEY,FORREST,HIGH_MOUNTAIN,SEA,DESERT
		1, 2, 3, 1, 3, 1, 3, 1, 2
	};
	public int[] cross_river_time = { 2, 2, 4, 4 };
	public double unit_maintenance_cost_ratio = 0.001;
	public double unit_training_monthly_change = 0.01;
	public double unit_morale_monthly_change = 0.01;
	public double unit_morale_policy_increase = 0.1;
	public double unit_training_policy_increase = 0.1;
	public int[] army_num_thresholds = { 1, 5, 10, 16, 23, 31 };
	public long base_reinforcement = 500;
	public long base_army_reinforcement = 100;
	public long base_recruitment = 1000;
	public int base_recruitment_days = 30;
	public double base_wage = 0.01;
	public double food_shortage_morale_decrease = 0.1;
	public double food_shortage_army_decrease = 0.1;
	
	public double city_base_defence_buffer = 0.2;
	public double wall_defence_buffer = 0.1;
	public double fort_defence_buffer = 0.8;
	public double supply_multiplier = 4.0;
	public double mobilize_base = 0.2;
	public double foreign_mobilize_multiplier = 0.1;
	
	// Battle
	public long[] max_soldier_on_command = {  // By Ability
			1000, 2000, 5000, 10000, 50000, 80000, 100000, 150000, 200000, 250000, 300000,
	};
	public long[] terrain_max_soldier = {
			// PLAIN,HILL,MOUNTAIN,SWAMP,RIVER_VALLEY,FORREST,HIGH_MOUNTAIN,SEA,DESERT
		100000, 40000, 20000, 50000, 20000, 50000, 0, 0, 50000,
	};
	public long siege_max_soldier = 40000;
	public double[] combat_soldier_type_multiplier = {  0.3, 0.7, 1.0  };
	public double combat_militia_multiplier = 0.3;
	public double combat_siege_attack_base = 2;
	public double siege_max_wall_damage_prob = 0.5;
	public double[][] combat_unit_type_multipliers = {
			{ 1.0, 2.0, 1.0, 2.0 },  // MELEE
			{ 1.0, 1.0, 2.0, 1.0 },  // ARCHERY
			{ 2.0, 2.0, 1.0, 2.0 },  // MOUNTED
			{ 1.0, 1.0, 1.0, 1.0 },  // SIEGE
	};
	public double[][] combat_unit_terrain_multipliers = {
			// PLAIN,HILL,MOUNTAIN,SWAMP,RIVER_VALLEY,FORREST,HIGH_MOUNTAIN,SEA,DESERT
			{ 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 },  // MELEE
			{ 1.0, 1.2, 1.2, 1.2, 1.2, 0.8, 1.2, 1.0, 1.0 },  // ARCHERY
			{ 1.0, 0.8, 0.5, 0.5, 0.5, 0.8, 0.5, 0.5, 0.8 },  // MOUNTED
			{ 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 },  // SIEGE
	};
	public double[] combat_unit_river_multipliers = { 1.0, 1.2, 0.5, 1.0 };
	public double combat_power_by_training = 0.8;
	public double combat_power_by_morale = 0.6;
	public double combat_general_base = 0.25;
	public double combat_general_multiplier = 0.15;
	public double combat_damage_base = 0.005;
	public double combat_counter_attack_multiplier = 0.5;
	
	// Battle Side Effect
	public double combat_general_death_prob = 0.2;
	public double combat_govener_death_prob = 0.3;
	public int conquered_city_happiness_sum = 150;
	public int max_conquered_city_happiness = 80;
	public double siege_improvement_destroy_prob = 0.1;
	public double siege_agriculture_multiplier = 0.5;
	public double combat_labor_food_consumption = 0.1;
	public double combat_army_food_consumption = 1.0;
	public double combat_demostic_army_food_consumption = 0.5;
	public double combat_siege_army_food_consumption = 0.5;
	public double morale_decrease_from_food_shortage = 0.01;
	public double city_loot_multiplier = 0.8;
	
	// Barbarian
	public double barbarian_prob_multiplier = 0.005;
	public double greatwall_barbarian_defence_prob = 0.8;
	public double wall_barbarian_defence_prob = 0.05;
	public long min_soldier_defend_barbarian = 10000;
	public double barbarian_loot_improvement_severity_threshold = 0.2;
	public double barbarian_loot_improvement_prob = 0.2;
	public int barbarian_loot_happiness_decrease = 5;
	public double min_barbarian_loot_multiplier = 0.4;
	
	// Person
	public int earlist_available_year_prior_to_death = 40;
	public int base_ability = 3;
	public int ability_var_num = 5;
	public double[] ability_var_prob = { 0.15, 0.7 };  // -1, 0, 1
	public double[] ability_var_prob_nomination = { 0.1, 0.6 };  // -1, 0, 1
	public double[] ability_var_prob_exam = { 0.05, 0.5 };  // -1, 0, 1
	public int age_mean = 30;
	public int age_std = 8;
	
	public int hiring_cost = 100;
	public int minister_salary = 10;
	public int base_officer_number = 8;
	public int retainer_officer_number_boost = 12;
	public int ideology_officer_number_boost = 7;
	
	// State
	public double stability_cost_multiplier = 0.05;
	public double[] riot_non_huaxia_foreign_multiplier = { 1, 0, 1.5, 0.2 };
	public int[] riot_point_multiplier_by_city_type = { 10, 0, 15, 10 };
	public double riot_base_points = 0;
	public double riot_points_with_military_bureau = -0.5;
	public double riot_stability_penalty = 0.5;
	public double riot_prestige_multiplier = 0.005;
	public double riot_prestige_threshold = 20;
	public int riot_happiness_min_threshold = 60;
	public int riot_happiness_max_threshold = 80;
	public int suppress_revolt_riot_decrease = 2000;
	public int suppress_revolt_happiness_decrease = 5;
	
	// Diplomacy
	public int max_attitude_for_embargo = -50;
	public int min_attitude_for_ally = 50;
	public int min_attitude_for_open_border = 100;
	public int min_attitude_for_alliance = 150;
	public int max_attitude_without_ally = 100;
	public int max_attitude_with_ally = 160;
	public int delta_attitude_for_free_cancellation = 50;
	public int delta_attitude_for_maintanence = 100;
	public int min_prestige_for_suzerainy = 50;
	public double prestige_boost_multiplier_for_treaty = 0.005;
	public double prestige_penalty_multiplier_for_treaty = 0.05;
	public int treaty_expire_days = 90;
	public int prestige_decrease_for_treaty_cancellation = 10;
	public int prestige_decrease_for_trespassing = 100;
	public int prestige_decrease_for_treaty_rejection = 10;
	
	// Combat Diplomacy
	public int defeat_attitude_decrease = 5;
	public int defend_helper_attitude_increase = 5;
	public int siege_defend_helper_attitude_increase = 10;
	public int defeat_prestige_decrease = 2;
	public int winner_prestige_increase = 2;
	public int defend_helper_prestige_increase = 1;
	public int siege_defend_helper_prestige_increase = 2;
	public int assist_prestige_increase = 2;
	public int assist_fail_prestige_decrease = 2;
	
	// Policies
	public double policy_convert_foreigner_ratio = 0.01;
	public int policy_happiness_increase = 10;
	public int policy_attitude_increase = 50;
	public int policy_attitude_self_increase = 2;
	public int policy_attitude_decrease = 5;
	public int policy_prestige_increase_cap = 20;
	public int policy_prestige_increase = 2;
	
	// Market
	public double max_market_price_increase = 1.02;
	//public double base_market_ratio = 0.0001;
	public Resource<Long> resource_on_market_base = new Resource<Long>(0L){{
		food = 0L;  horse = 0L; iron = 0L;
	}};
	public double market_harvest_ratio = 0.0001;
	public double market_produce_ratio = 0.01;
	public double min_resource_price = 0.1;
	public int max_export_amount = 1000000;
	public double max_export_ratio = 0.5;
}
