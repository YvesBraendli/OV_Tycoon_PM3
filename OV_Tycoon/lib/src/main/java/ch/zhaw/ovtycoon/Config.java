package ch.zhaw.ovtycoon;

public class Config {
	public static final int MAX_DICE_VALUE = 6;
	public static final int MAX_THROWABLE_DICE = 3;
	public static final int MAX_ATTACKERS = 3;
	public static final int MIN_ATTACKERS = 1;
	public static final int REGION_BONUS = 2;
	public static final int MAX_PLAYERS = 6;
	public static final int MIN_PLAYERS = 2;
	
	public enum RegionName{
		Zurich,
		Winterthur,
		Horgen,
		Affoltern,
		Andelfingen,
		Bulach,
		Dielsdorf,
		Dietikon,
		Hinwil,
		Meilen,
		Pfaeffikon,
		Uster
	}
	
	public enum ZoneName{
		Zone110,
		Zone111,
		Zone112,
		Zone113,
		Zone114,
		Zone115,
		Zone116,
		Zone117,
		Zone118,
		Zone120,
		Zone121,
		Zone122,
		Zone123,
		Zone124,
		Zone130,
		Zone131,
		Zone132,
		Zone133,
		Zone134,
		Zone135,
		Zone140,
		Zone141,
		Zone142,
		Zone143,
		Zone150,
		Zone151,
		Zone152,
		Zone153,
		Zone154,
		Zone155,
		Zone156,
		Zone160,
		Zone161,
		Zone162,
		Zone163,
		Zone164,
		Zone170,
		Zone171,
		Zone172,
		Zone173,
		Zone180,
		Zone181,
		Zone184
	}

	public enum PlayerColor{
		BLACK, BLUE, GREEN, RED, WHITE, YELLOW
	}
	
}
