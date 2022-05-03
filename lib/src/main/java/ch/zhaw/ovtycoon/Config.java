package ch.zhaw.ovtycoon;

public class Config {
	public static final int MAX_DICE_VALUE = 6;
	public static final int MAX_THROWABLE_DICE = 3;
	public static final int MAX_ATTACKERS = 3;
	public static final int MIN_ATTACKERS = 1;
	public static final int REGION_BONUS = 2;
	public static final int MAX_PLAYERS = 6;
	public static final int MIN_PLAYERS = 2;
	public static final int NUMBER_OF_ZONES = 42;
	public static final int NUMBER_OF_REGIONS = 5;
	public static final int NUMBER_OF_TROOPS_TOTAL_IN_GAME = 80;
	public static final int MIN_NUMBER_OF_TROOPS_IN_ZONE = 1;
	
	public enum RegionName{
		Unterland("Unterland"),
		Weinland("Weinland"),
		Oberland("Oberland"),
		HorgenAlbis("HorgenAlbis"),
		MeilenZurich("MeilenZurich");
		
		private String name;

	    private RegionName(String name) {
	      this.name = name;
	    }

	    @Override
	    public String toString() {
	      return name;
	    }
	}
	
	public enum ZoneName{
		Zone110("Zone110"),
		Zone111("Zone111"),
		Zone112("Zone112"),
		Zone113("Zone113"),
		Zone114("Zone114"),
		Zone115("Zone115"),
		Zone116("Zone116"),
		Zone117("Zone117"),
		Zone118("Zone118"),
		Zone120("Zone120"),
		Zone121("Zone121"),
		Zone122("Zone122"),
		Zone123("Zone123"),
		Zone124("Zone124"),
		Zone130("Zone130"),
		Zone131("Zone131"),
		Zone132("Zone132"),
		Zone133("Zone133"),
		Zone134("Zone134"),
		Zone135("Zone135"),
		Zone140("Zone140"),
		Zone141("Zone141"),
		Zone142("Zone142"),
		Zone143("Zone143"),
		Zone150("Zone150"),
		Zone151("Zone151"),
		Zone152("Zone152"),
		Zone153("Zone153"),
		Zone154("Zone154"),
		Zone155("Zone155"),
		Zone156("Zone156"),
		Zone160("Zone160"),
		Zone161("Zone161"),
		Zone162("Zone162"),
		Zone163("Zone163"),
		Zone164("Zone164"),
		Zone170("Zone170"),
		Zone171("Zone171"),
		Zone172("Zone172"),
		Zone173("Zone173"),
		Zone180("Zone180"),
		Zone181("Zone181"),
		Zone184("Zone184");
		
		private String name;

	    private ZoneName(String name) {
	      this.name = name;
	    }

	    @Override
	    public String toString() {
	      return name;
	    }
	}

	public enum PlayerColor{
		BLACK, BLUE, GREEN, RED, WHITE, YELLOW
	}
	
}
