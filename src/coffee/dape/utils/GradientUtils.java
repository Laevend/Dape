package coffee.dape.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

/**
 * @author Laeven
 */
public class GradientUtils
{
	/**
	 * Applies a linear gradient to a string
	 * @param string The String of text to apply the gradient to
	 * @param gradient The Linear gradient applied from left to right in order of each colour
	 * @return A string with the linear gradient applied
	 */
	public static String applyGradient(String string,LinearGradient gradient)
	{
		return generateInbetweenColours(string,gradient.getGradientColours());
	}
	
	/**
	 * Generates in-between colours using the colours in the list provided into a gradient line
	 * @param lineLength The length of the line
	 * @param gradient The Linear gradient applied from left to right in order of each colour
	 * @return A string with the linear gradient applied
	 */
	public static String createLine(int lineLength,LinearGradient gradient)
	{
		return generateGradientLine(lineLength,gradient.getGradientColours());
	}
	
	/**
	 * Generates in-between colours using the colours in the list provided into a gradient line
	 * @param lineLength The length of the line
	 * @param gradient The Linear gradient applied from left to right in order of each colour
	 * @return A string with the linear gradient applied
	 */
	public static String generateGradientLine(int lineLength,LinearGradient gradient)
	{
		return generateGradientLine(lineLength,gradient.getGradientColours());
	}
	
	/**
	 * Generates in-between colours using the colours in the list provided into a gradient line
	 * @param lineLength The length of the line
	 * @param colours The list of colours used to create a linear gradient applied from left to right in order of each colour
	 * @return A string with the linear gradient applied
	 */
	public static String generateGradientLine(int lineLength,List<Color> colours)
	{
		if(colours.size() == 0 || colours.size() == 1) { Logg.error("Colours provided are not enough to generate a gradient!"); return "NULL"; }
		
		List<Color> gradColours = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
        int steps = (int) Math.ceil((double) lineLength / (colours.size() - 1));
        
        //Logg.verbose("String length: " + msgLength);
        //Logg.verbose("Steps: " + steps);
        
        for(int h = 0; h < (colours.size() - 1); h++)
        {
            for(int i = 0; i < steps; i++)
            {
                float ratio = (float) i / (float) steps;
                int red = (int) (colours.get(h + 1).getRed() * ratio + colours.get(h).getRed() * (1 - ratio));
                int green = (int) (colours.get(h + 1).getGreen() * ratio + colours.get(h).getGreen() * (1 - ratio));
                int blue = (int) (colours.get(h + 1).getBlue() * ratio + colours.get(h).getBlue() * (1 - ratio));
                gradColours.add(new Color(red, green, blue));
            }
        }
		
        Iterator<Color> it = gradColours.iterator();
        Color currentColour = null;
        
		for(int i = 0; i < lineLength; i++)
		{			
			if(it.hasNext())
			{
				currentColour = it.next();
			}
			else
			{
				Logg.error("No more colours! " + lineLength + " - " + gradColours.size());
			}
			
			sb.append(ChatColor.of(currentColour) + ColourUtils.translate("&m "));
		}
		
		return sb.toString();
    }
	
	/**
	 * Generates in-between colours using the colours in the list provided
	 * @param string The String of text to apply the gradient to
	 * @param colours The list of colours used to create a linear gradient applied from left to right in order of each colour
	 * @return A string with the linear gradient applied
	 */
	public static String generateInbetweenColours(String string,List<Color> colours)
	{
		if(colours.size() == 0 || colours.size() == 1) { Logg.error("Colours provided are not enough to generate a gradient!"); return string; }
		
		List<Color> gradColours = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		char[] charArray = string.toCharArray();
		int msgLength = string.replaceAll("\\s+","").length();
        int steps = (int) Math.ceil((double) msgLength / (colours.size() - 1));
        
        for(int h = 0; h < (colours.size() - 1); h++)
        {
            for(int i = 0; i < steps; i++)
            {
                float ratio = (float) i / (float) steps;
                int red = (int) (colours.get(h + 1).getRed() * ratio + colours.get(h).getRed() * (1 - ratio));
                int green = (int) (colours.get(h + 1).getGreen() * ratio + colours.get(h).getGreen() * (1 - ratio));
                int blue = (int) (colours.get(h + 1).getBlue() * ratio + colours.get(h).getBlue() * (1 - ratio));
                gradColours.add(new Color(red, green, blue));
            }
        }
		
        Iterator<Color> it = gradColours.iterator();
        Color currentColour = null;
        
		for(int i = 0; i < charArray.length; i++)
		{
			if(charArray[i] == ((char) 32))
			{
				sb.append(" ");
				continue;
			}
			
			if(it.hasNext())
			{
				currentColour = it.next();
			}
			else
			{
				Logg.error("No more colours! " + charArray.length + " - " + gradColours.size());
			}
			
			sb.append(ChatColor.of(currentColour) + String.valueOf(charArray[i]));
		}
		
		return sb.toString();
    }
	
	public static final LinearGradient LOGO = new LinearGradient(ColourUtils.hexToColour("#272727"),ColourUtils.hexToColour("#ff0060"));
	
	public static final LinearGradient VERTEX_GREEN = new LinearGradient(ColourUtils.hexToColour("#00efac"),ColourUtils.hexToColour("#007a76"));
	public static final LinearGradient VERTEX_GREEN_REVERSE = new LinearGradient(ColourUtils.hexToColour("#007a76"),ColourUtils.hexToColour("#00efac"));
	public static final LinearGradient DARK_RED = new LinearGradient(ColourUtils.hexToColour("#fc0200"),ColourUtils.hexToColour("#5e0f0a"));
	public static final LinearGradient LIGHT_RED = new LinearGradient(ColourUtils.hexToColour("#6a010e"),ColourUtils.hexToColour("#ff0042"));
	public static final LinearGradient SUMMER = new LinearGradient(ColourUtils.hexToColour("#22c1c3"),ColourUtils.hexToColour("#fdbb2d"));
	public static final LinearGradient PURPINK = new LinearGradient(ColourUtils.hexToColour("#7F00FF"),ColourUtils.hexToColour("#E100FF"));
	public static final LinearGradient DEEP_BLUE = new LinearGradient(ColourUtils.hexToColour("#396afc"),ColourUtils.hexToColour("#2948ff"));
	public static final LinearGradient PINKY_YELLOW = new LinearGradient(ColourUtils.hexToColour("#d9a7c7"),ColourUtils.hexToColour("#fffcdc"));
	public static final LinearGradient WATER_BLUE_WHITE = new LinearGradient(ColourUtils.hexToColour("#1c92d2"),ColourUtils.hexToColour("#f2fcfe"));
	public static final LinearGradient GRAY_TO_WHITE = new LinearGradient(ColourUtils.hexToColour("#545454"),ColourUtils.hexToColour("#e3e3e3"));
	public static final LinearGradient GOLDY = new LinearGradient(ColourUtils.hexToColour("#F7971E"),ColourUtils.hexToColour("#FFD200"));
	public static final LinearGradient FOREST_GREEN = new LinearGradient(ColourUtils.hexToColour("#34e89e"),ColourUtils.hexToColour("#0f3443"));
	public static final LinearGradient FOREST_GREEN_DESATURATED = new LinearGradient(ColourUtils.hexToColour("#44A08D"),ColourUtils.hexToColour("#093637"));
	public static final LinearGradient VERTEX_TOKEN = new LinearGradient(ColourUtils.hexToColour("#bdc3c7"),ColourUtils.hexToColour("#2c3e50"));
	public static final LinearGradient SAND_TO_BLUE = new LinearGradient(ColourUtils.hexToColour("#3E5151"),ColourUtils.hexToColour("#DECBA4"));
	public static final LinearGradient SAND_TO_BLUE_REVERSE = new LinearGradient(ColourUtils.hexToColour("#DECBA4"),ColourUtils.hexToColour("#3E5151"));
	public static final LinearGradient LITTLE_LEAF = new LinearGradient(ColourUtils.hexToColour("#76b852"),ColourUtils.hexToColour("#8dc26f"));
	public static final LinearGradient BLOOD_RED = new LinearGradient(ColourUtils.hexToColour("#f85032"),ColourUtils.hexToColour("#e73827"));
	
	public static final LinearGradient TEXT_INFO = new LinearGradient(ColourUtils.hexToColour("#8A7F8D"),ColourUtils.hexToColour("#738276"));
	public static final LinearGradient TEXT_ERROR = new LinearGradient(ColourUtils.hexToColour("#ed213a"),ColourUtils.hexToColour("#93291e"));
	public static final LinearGradient TEXT_WARNING = new LinearGradient(ColourUtils.hexToColour("#fdfc47"),ColourUtils.hexToColour("#f9d423"));
	public static final LinearGradient TEXT_SUCCESS = new LinearGradient(ColourUtils.hexToColour("#1d976c"),ColourUtils.hexToColour("#93f9b9"));
	
	public static final LinearGradient GREY_TO_DARK = new LinearGradient(ColourUtils.hexToColour("#858e96"),ColourUtils.hexToColour("#60696b"));
	public static final LinearGradient ENSOULED = new LinearGradient(ColourUtils.hexToColour("#03b7ac"),ColourUtils.hexToColour("#c64bff"));
	public static final LinearGradient CARDINAL = new LinearGradient(ColourUtils.hexToColour("#fd4f17"),ColourUtils.hexToColour("#abf473"));
	
	public static class LinearGradient
	{
		private static Pattern hexPattern = Pattern.compile("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$",Pattern.CASE_INSENSITIVE);
		private List<Color> colours;
		
		/**
		 * Creates a new linear gradient
		 * @param name Name of this gradient
		 * @param colours List of colours that make up this gradient
		 */
		public static LinearGradient of(List<Color> cols)
		{
			Objects.requireNonNull(cols,"List of colours cannot be null!");
			return new LinearGradient(cols);
		}
		
		/**
		 * Creates a new linear gradient
		 * @param name Name of this gradient
		 * @param colours List of colours that make up this gradient
		 */
		public static LinearGradient of(Color... cols)
		{
			Objects.requireNonNull(cols,"Array of colours cannot be null!");
			return new LinearGradient(cols);
		}
		
		/**
		 * Creates a new linear gradient
		 * @param name Name of this gradient
		 * @param hexColours List of colours that make up this gradient represented as hexadecimal strings
		 */
		public static LinearGradient of(String... hexadecimalCols)
		{
			Objects.requireNonNull(hexadecimalCols,"Array of hex colours cannot be null!");
			return new LinearGradient(hexadecimalCols);
		}
		
		/**
		 * Creates a new linear gradient
		 * @param name Name of this gradient
		 * @param colours List of colours that make up this gradient
		 */
		private LinearGradient(List<Color> colours)
		{
			this.colours = colours;
		}
		
		/**
		 * Creates a new linear gradient
		 * @param name Name of this gradient
		 * @param colours List of colours that make up this gradient
		 */
		private LinearGradient(Color... colours)
		{
			this.colours = List.of(colours);
		}
		
		/**
		 * Creates a new linear gradient
		 * @param name Name of this gradient
		 * @param hexColours List of colours that make up this gradient represented as hexadecimal strings
		 */
		private LinearGradient(String... hexColours)
		{
			colours = new ArrayList<>();
			
			for(String hex : hexColours)
			{
				if(!hexPattern.matcher(hex).matches())
				{
					Logg.warn("Hexadecimal colour code: " + hex + " is not hex!");
					continue;
				}
				
				colours.add(ColourUtils.hexToColour(hex));
			}
		}
		
		/**
		 * Gets the list of colours that make up this gradient
		 * @return Gradient colours
		 */
		public List<Color> getGradientColours()
		{
			return colours;
		}
	}
}