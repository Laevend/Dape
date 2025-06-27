package coffee.dape.utils;

/**
 * A class for predefined lore snippets to describe what a click action would do
 */
public class InputUtils
{
	public static class RightClick
	{
		public static final String RIGHT_CLICK = "&8[&cRIGHT-CLICK&8]&r";
		
		/**
		 * Returns a description of what RIGHT-CLICK will do
		 * @param actionDescription Description of this action
		 * @return {@value #RIGHT_CLICK} + action description formatted
		 */
		public static String describeAction(String actionDescription)
		{
			return ColourUtils.translate(RIGHT_CLICK) + " " + ColourUtils.applyColour(actionDescription,ColourUtils.TEXT);
		}
	}
	
	public static class LeftClick
	{
		public static final String LEFT_CLICK = "&8[&9LEFT-CLICK&8]&r";
		
		/**
		 * Returns a description of what LEFT-CLICK will do
		 * @param actionDescription Description of this action
		 * @return {@value #LEFT_CLICK} + action description formatted
		 */
		public static String describeAction(String actionDescription)
		{
			return ColourUtils.translate(LEFT_CLICK) + " " + ColourUtils.applyColour(actionDescription,ColourUtils.TEXT);
		}
	}
	
	public static class ShiftRightClick
	{
		public static final String SHIFT_RIGHT_CLICK = "&8[&dSHIFT-RIGHT-CLICK&8]&r";
		
		/**
		 * Returns a description of what SHIFT-RIGHT-CLICK will do
		 * @param actionDescription Description of this action
		 * @return {@value #SHIFT_RIGHT_CLICK} + action description formatted
		 */
		public static String describeAction(String actionDescription)
		{
			return ColourUtils.translate(SHIFT_RIGHT_CLICK) + " " + ColourUtils.applyColour(actionDescription,ColourUtils.TEXT);
		}
	}
	
	public static class ShiftLeftClick
	{
		public static final String SHIFT_LEFT_CLICK = "&8[&bSHIFT-LEFT-CLICK&8]&r";
		
		/**
		 * Returns a description of what SHIFT-LEFT-CLICK will do
		 * @param actionDescription Description of this action
		 * @return {@value #SHIFT_LEFT_CLICK} + action description formatted
		 */
		public static String describeAction(String actionDescription)
		{
			return ColourUtils.translate(SHIFT_LEFT_CLICK) + " " + ColourUtils.applyColour(actionDescription,ColourUtils.TEXT);
		}
	}
	
	public static class Sneak
	{
		public static final String SNEAK = "&8[&aSNEAK&8]&r";
		
		/**
		 * Returns a description of what SNEAK will do
		 * @param actionDescription Description of this action
		 * @return {@value #SNEAK} + action description formatted
		 */
		public static String describeAction(String actionDescription)
		{
			return ColourUtils.translate(SNEAK) + " " + ColourUtils.applyColour(actionDescription,ColourUtils.TEXT);
		}
	}
	
	public static class MiddleClick
	{
		public static final String MIDDLE_CLICK = "&8[&eMIDDLE-CLICK&8]&r";
		
		/**
		 * Returns a description of what MIDDLE-CLICK will do
		 * @param actionDescription Description of this action
		 * @return {@value #MIDDLE_CLICK} + action description formatted
		 */
		public static String describeAction(String actionDescription)
		{
			return ColourUtils.translate(MIDDLE_CLICK) + " " + ColourUtils.applyColour(actionDescription,ColourUtils.TEXT);
		}
	}
}
