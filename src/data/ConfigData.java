package ac.data;

public class ConfigData {
	ConfigData() {
		SetSpeedScale(0);
	}
	public int speed_scale;
	public transient boolean pause = true;
	public transient int ms_per_day = 50;
	public transient int map_layer = 0;
	
	public void SetSpeedScale(int scale) {
		speed_scale = scale;
		ms_per_day = (1 << scale) * kMaxSpeed;
	}
	
	public static int kMaxSpeed = 50;
}
