package coffee.dape.utils;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.md_5.bungee.api.ChatColor;

/**
 * 
 * @author Laeven
 *
 */
public class StringUtils
{
	private static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	private static DateFormat dateFileNameFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
	private static Calendar cal;
	private static Map<Character,String> upsideDownCharacterMap = new HashMap<>();
	
	/**
	 * Returns a string that represents this date
	 * @param date Date
	 * @return String representation of this date
	 */
	public static String dateToString(Date date)
	{
		return dateFormat.format(date);
	}
	
	/**
	 * Returns a Date object expressed by the formatted date string
	 * @param date Formatted string date
	 * @return Date
	 */
	public static Date stringToDate(String date)
	{
		try
		{
			return dateFormat.parse(date);
		}
		catch (ParseException e)
		{
			Logg.error("Date Parsing failed!",e);
		}
		
		return null;
	}
	
	/**
	 * Takes a string array and returns a string with
	 * the contents of the string array with each value
	 * comma separated
	 * @param s String array
	 * @return CSV String
	 */
	public static String arrayToCSV(String[] s)
	{
		StringBuilder sb = new StringBuilder();
		
		for(String value : s)
		{
			sb.append(value + ", ");
		}
		
		return sb.toString().substring(0,sb.toString().length() - 2);
	}
	
	/**
	 * Takes a string list and returns a string with
	 * the contents of the string array with each value
	 * comma separated
	 * @param s String array
	 * @return CSV String
	 */
	public static String arrayToCSV(List<String> s)
	{
		StringBuilder sb = new StringBuilder();
		
		for(String value : s)
		{
			sb.append(value + ", ");
		}
		
		return sb.toString().substring(0,sb.toString().length() - 2);
	}
	
	public static boolean isNotNullEmptyOrBlank(String s)
	{
		return s != null && !s.isEmpty() && !s.isBlank();
	}
	
	/**
	 * Gets a date represented as a string
	 * Example: 20-8-2020-1-56-54-755
	 * Day-Month-Year-Hour-Minute-Second-Millisecond
	 * @return Date string
	 */
	public static String getDateAsString()
	{
		cal = Calendar.getInstance();
		String fileName = dateFileNameFormat.format(cal.getTime()) + "-" + cal.get(Calendar.MILLISECOND);
		return fileName;
	}
	
	/**
	 * Capitalises the first letter of the string
	 * @param s String
	 * @return String with first letter capitalised
	 */
	public static String capitaliseFirstLetter(String s)
	{
		return s.substring(0,1).toUpperCase() + s.substring(1,s.length());
	}
	
	/**
	 * Formats a syntax string by turning anything with <> or [] into
	 * its respected colours gold and gray (Used for the /help command)
	 * @param syntax Syntax string
	 * @return formatted syntax string
	 */
	public static String formatSyntaxString(String syntax)
	{
		StringBuilder sb = new StringBuilder();
		boolean colourChange = false;
		boolean changeBack = false;
		
		String[] parts = syntax.split(" ");
		
		sb.append("&3");
		
		for(String part : parts)
		{
			if(part.startsWith("<"))
			{
				sb.append("&9");
				colourChange = true;
				changeBack = false;
			}
			else if(part.startsWith("["))
			{
				sb.append("&b");
				colourChange = true;
				changeBack = false;
			}
			else if(colourChange && (part.endsWith(">") || part.endsWith("]")))
			{
				colourChange = false;
				changeBack = true;
			}
			else if(changeBack)
			{
				sb.append("&3");
				changeBack = false;
			}
			
			sb.append(part);
			sb.append(" ");
		}
		
		return sb.toString().substring(0,sb.toString().length() - 1);
	}
	
	public static String toSnakecase(String s)
	{
		return s.toLowerCase().replaceAll("\\s+","_");
	}
	
	private static final String[] romanLetters = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"}; 
	private static final int[] values = {1000,900,500,400,100,90,50,40,10,9,5,4,1};  
	
	public static String numberToRomanNumerals(int number)
	{
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < values.length; i++)
		{
			while(number >= values[i])
			{
				number = number - values[i];
				sb.append(romanLetters[i]);
			}
		}
		
		return sb.toString();
	}
	
	/**
     * Constructs lines of lore using a single line of lore. Each line length is specified by numberOfCharactersPerLine
     * @param loreList Existing lore list
     * @param numberOfCharactersPerLine Maximum number of characters that should be displayed in each line of lore
     * @param overflow If complete words should have the exception to overflow instead of being added to the next line
     * @param loreString The single line of lore to be divided up into multiple lines
     * @return ItemBuilder
     */  
	public static List<String> constructLines(List<String> loreList,int numberOfCharactersPerLine,boolean overflow,String loreString)
    {
    	StringBuilder colourBuffer = new StringBuilder();
    	StringBuilder finalLoreLineBuffer = new StringBuilder();
    	
    	char[] loreCharArray = loreString.toCharArray();
    	
    	// index of last space in loreLineBuffer
    	int lastSpaceCharIndex = 0;
    	
    	// if we're collecting colour codes
    	boolean isCollectingColour = false;
    	
    	// colour code offset to have the colour code characters be not included in the numberOfCharactersPerLine
    	int colourLengthOffset = 0;
    	
    	for(int i = 0; i < loreCharArray.length; i++)
    	{
    		// Get next character
    		char c = loreCharArray[i];
    		
    		// If character is & of colour symbol then we're collecting a colour
    		if((c == ((char) 38) || (c == ((char) 167))) && ((i + 1) < loreCharArray.length))
    		{
    			// If we're not already collecting colours
    			if(!isCollectingColour)
    			{    				
    				// Empty colour buffer as we're about to fill it with new colours
    				colourBuffer.setLength(0);
        			isCollectingColour = true;
    			}
    			
    			// add another colour to buffer
    			colourBuffer.append(c);
    			colourBuffer.append(loreCharArray[i + 1]);
    			i++;
    			continue;
    		}
    		else if(isCollectingColour)
    		{
    			// If we were collecting colour, disable
    			isCollectingColour = false;
    			finalLoreLineBuffer.append(colourBuffer.toString());
				colourLengthOffset += colourBuffer.length();
    		}
    		
    		// Add another character as long as it's less than numberOfCharactersPerLine
    		if((finalLoreLineBuffer.length() - colourLengthOffset) <= numberOfCharactersPerLine)
    		{    			
    			finalLoreLineBuffer.append(c);
    			
    			if(c == ((char) 32))
    			{
    				lastSpaceCharIndex = finalLoreLineBuffer.length() - 1;
    			}
    		}
    		// If overflow is allowed, the current word will be completed before a line return
    		else if(overflow)
    		{
    			if(c == ((char) 32))
    			{
    				loreList.add(finalLoreLineBuffer.toString());
    				finalLoreLineBuffer.setLength(0);
    				lastSpaceCharIndex = 0;
    				colourLengthOffset = 0;
    			}
    			else
    			{
    				finalLoreLineBuffer.append(c);
    			}
    		}
    		// No overflow allowed, current word will be pushed onto next line
    		else
    		{
    			finalLoreLineBuffer.append(c);
    			
    			// Grab text from last space to the end of the lore line buffer
    			String extra = finalLoreLineBuffer.substring(lastSpaceCharIndex + 1,finalLoreLineBuffer.length());
    			
    			// Grab text from start to the last space of the lore line buffer
    			String toAdd = finalLoreLineBuffer.substring(0,lastSpaceCharIndex);
    			
    			// Add colour and lore line to lore list
    			loreList.add(ColourUtils.translate(toAdd));
    			
    			// reset buffers
        		finalLoreLineBuffer.setLength(0);
        		lastSpaceCharIndex = 0;
        		colourLengthOffset = 0;
        		
        		for(int j = 0; j < extra.length(); j++)
        		{
        			if((extra.charAt(j) == ((char) 38) || (extra.charAt(j) == ((char) 167))))
        			{
        				colourLengthOffset+=2;
        				j++;
        			}
        		}
        		
        		colourLengthOffset += colourBuffer.length();
        		
        		// Add extra to new line
        		finalLoreLineBuffer.append(colourBuffer.toString() + extra);
    		}
    	}
    	
    	if(finalLoreLineBuffer.length() > 0)
    	{
    		loreList.add(ColourUtils.translate(finalLoreLineBuffer.toString()));
    	}
        
        return loreList;
    }
	
	public static final char AND_SIGN = ((char) 38);
	public static final char SECTION_SIGN = ((char) 167);
	public static final char BACKSLASH_SIGN = '\\';
	public static final Set<Character> COLOUR_CODES = new HashSet<>();
	
	static
	{
		for(char c : ChatColor.ALL_CODES.toCharArray())
		{
			COLOUR_CODES.add(c);
		}
	}
	
	/**
	 * Wraps text into multiple lines
	 * @param text Text to wrap
	 * @return ArrayList of lore
	 */
	public static List<String> wrapColouredText(String lore)
	{
		return wrapColouredText(lore,60,false,false);
	}
	
	/**
	 * Wraps text into multiple lines
	 * @param text Text to wrap
	 * @param charsPerLine Number of characters of text per line (default:60)
	 * @return
	 */
	public static List<String> wrapColouredText(String lore,int charsPerLine)
	{
		return wrapColouredText(lore,charsPerLine,false,false);
	}
	
	/**
	 * Wraps text into multiple lines
	 * @param text Text to wrap
	 * @param charsPerLine Number of characters of text per line (default:60)
	 * @param overflow If a wrap should happen every 'charsPerLine' + extra characters until the end of a word (default:false)
	 * @return ArrayList of lore
	 */
	public static List<String> wrapColouredText(String lore,int charsPerLine,boolean overflow)
	{
		return wrapColouredText(lore,charsPerLine,overflow,false);
	}
	
	/**
	 * Wraps text into multiple lines
	 * @param text Text to wrap
	 * @param charsPerLine Number of characters of text per line (default:60)
	 * @param overflow If a wrap should happen every 'charsPerLine' + extra characters until the end of a word (default:false)
	 * @param wrapWords If a wrap should happen every 'charsPerLine' regardless if the wrap happens in the middle of a word (default:false)
	 * @return ArrayList of lore
	 */
	public static List<String> wrapColouredText(String text,int charsPerLine,boolean overflow,boolean wrapWords)
	{
		if(text.length() <= charsPerLine) { return List.of(text); }
		
		List<String> wrappedLore = new ArrayList<>();
		char[] loreArr = text.toCharArray();
		int currentCharsInLine = 0;
		int subStringFromIndex = 0;
		int lastWhitespaceIndex = 0;
		
		// Holds the combination of colour codes that make up the last colour found in the text
		StringBuilder lastColourBuffer = new StringBuilder(60);
		
		char nextChar;
		
		char nextSign;
		char nextColour;
		
		for(int i = 0; i < loreArr.length; i++)
		{
			// Get next character
			nextChar = loreArr[i];
			
			// If character is an and or section sign (meaning next character might be a colour code) 
			if((nextChar == AND_SIGN || nextChar == SECTION_SIGN)
					// Check the and or section sign is not being negated by a backslash (its functional use should be ignored)
					&& loreArr[i == 0 ? 0 : (i-1)] != BACKSLASH_SIGN 
					// Check the following character after the and or section sign is a valid colour code
					&& COLOUR_CODES.contains(loreArr[i + 1 > (loreArr.length - 1) ? (loreArr.length - 1) : i + 1]))
			{
				// Set colour buffer to 0
				lastColourBuffer.setLength(0);
				
				// Capture next colour
				nextSign = nextChar;
				nextColour = loreArr[i + 1 > (loreArr.length - 1) ? (loreArr.length - 1) : i + 1];
				
				// Check ahead in the array as gradients and custom colours use many colour codes 1 after another
				while((nextSign == AND_SIGN || nextSign == SECTION_SIGN) && COLOUR_CODES.contains(nextColour))
				{
					lastColourBuffer.append(String.valueOf(SECTION_SIGN) + String.valueOf(nextColour));
					
					i+=2;
					nextSign = loreArr[i];
					nextColour = loreArr[i + 1 > (loreArr.length - 1) ? (loreArr.length - 1) : i + 1];
				}
				
				// Roll back 2 because the nextSign and nextColour is not a colour code
				// -1 here instead of -2 as the iterator will add 1 on the next loop
				i-=1;
				continue;
			}
			
			currentCharsInLine++;
			
			//Logg.debug("Current Chars in Line = " + currentCharsInLine + " with char [ " + nextChar + " ]");
			
			// Make a note of the last point we encountered whitespace
			if(Character.isWhitespace(nextChar)) { lastWhitespaceIndex = i; }
			
			// Wrap text
			if(currentCharsInLine >= charsPerLine)
			{
				// If we want to wrap on a word we can split and wrap immediately
				if(wrapWords)
				{
					wrappedLore.add((lastColourBuffer.isEmpty() ? "" : lastColourBuffer.toString()) + text.substring(subStringFromIndex,subStringFromIndex + charsPerLine));
					subStringFromIndex = subStringFromIndex + charsPerLine;
					currentCharsInLine = 0;
				}
				// If overflow is allowed, we need to also make sure we land on a whitespace character before we can split
				else if(overflow && Character.isWhitespace(nextChar))
				{
					wrappedLore.add((lastColourBuffer.isEmpty() ? "" : lastColourBuffer.toString()) + text.substring(subStringFromIndex,i));
					subStringFromIndex = i;
					currentCharsInLine = 0;
				}
				// No word wrapping over overflow allowed, split and wrap at the last whitespace index
				// +1 as to not include the space at the start of the new line
				else if(!overflow)
				{
					wrappedLore.add((lastColourBuffer.isEmpty() ? "" : lastColourBuffer.toString()) + text.substring(subStringFromIndex,lastWhitespaceIndex + 1));
					subStringFromIndex = lastWhitespaceIndex + 1;
					currentCharsInLine = 0;
				}
			}
		}
		
		wrappedLore.add((lastColourBuffer.isEmpty() ? "" : lastColourBuffer.toString()) + text.substring(subStringFromIndex,loreArr.length));
		
		// Removal of back slashes
		for(int i = 0; i < wrappedLore.size(); i++)
		{
			wrappedLore.set(i,wrappedLore.get(i).replaceAll("\\\\",""));
		}
		
		return wrappedLore;
	}
	
	/**
	 * Wraps text into multiple lines. Will not work with colours!
	 * @param text Text to wrap
	 * @return ArrayList of lore
	 */
	public static List<String> wrapText(String text)
	{
		return wrapText(text,60,false,false);
	}
	
	/**
	 * Wraps text into multiple lines. Will not work with colours!
	 * @param text Text to wrap
	 * @param charsPerLine Number of characters of text per line (default:60)
	 * @return ArrayList of lore
	 */
	public static List<String> wrapText(String text,int charsPerLine)
	{
		return wrapText(text,charsPerLine,false,false);
	}
	
	/**
	 * Wraps text into multiple lines. Will not work with colours!
	 * @param text Text to wrap
	 * @param charsPerLine Number of characters of text per line (default:60)
	 * @param overflow If a wrap should happen every 'charsPerLine' + extra characters until the end of a word (default:false)
	 * @return ArrayList of lore
	 */
	public static List<String> wrapText(String text,int charsPerLine,boolean overflow)
	{
		return wrapText(text,charsPerLine,overflow,false);
	}
	
	/**
	 * Wraps text into multiple lines. Will not work with colours!
	 * @param text Text to wrap
	 * @param charsPerLine Number of characters of text per line (default:60)
	 * @param overflow If a wrap should happen every 'charsPerLine' + extra characters until the end of a word (default:false)
	 * @param wrapWords If a wrap should happen every 'charsPerLine' regardless if the wrap happens in the middle of a word (default:true)
	 * @return ArrayList of lore
	 */
	public static List<String> wrapText(String text,int charsPerLine,boolean overflow,boolean wrapWords)
	{
		if(text.length() <= charsPerLine) { return List.of(text); }
		
		List<String> wrappedText = new ArrayList<>();
		char[] textArr = text.toCharArray();
		int lowerPointer = 0;
		int upperPointer = 0;
		int lineNum = 1;
		
		while(upperPointer != textArr.length)
		{
			upperPointer = MathUtils.clamp(0,textArr.length,lineNum * charsPerLine);
			
			if(upperPointer == textArr.length)
			{
				wrappedText.add(text.substring(lowerPointer,textArr.length));
				return wrappedText;
			}
			
			if(!wrapWords)
			{
				while(!Character.isWhitespace(textArr[upperPointer]) && upperPointer > 0)
				{
					upperPointer = overflow ? upperPointer + 1 : upperPointer - 1;
				}
			}
			
			wrappedText.add(text.substring(lowerPointer,upperPointer));
			lowerPointer = upperPointer;
			lineNum++;
		}
		
		return wrappedText;
	}
	
	private static String ALPHANUMERIC = "123456789abcdefghijklmnopqrstuvwxyz";
	
	public static String getRandomAlphaNumeric(int chars)
	{
		StringBuilder sb = new StringBuilder();
		SecureRandom sRand = new SecureRandom();
		
		for(int i = 0; i < chars; i++)
		{
			sb.append(ALPHANUMERIC.charAt(sRand.nextInt(ALPHANUMERIC.length())));
		}
		
		return sb.toString();
	}
	
	/**
	 * Flips text to upside down
	 * @param text
	 * @return
	 */
	public static String flipText(String text)
	{
		StringBuilder sb = new StringBuilder(text.length());
		char[] textArr = text.toCharArray();
		
		for(int i = (textArr.length - 1); i >= 0; i--)
		{
			if(!upsideDownCharacterMap.containsKey(textArr[i]))
			{
				sb.append(textArr[i]);
				continue;
			}
			
			sb.append(upsideDownCharacterMap.get(textArr[i]));
		}
		
		return sb.toString();
	}
	
	static
	{
		upsideDownCharacterMap.put('A',new String(Character.toChars(0x2c6f)));
		upsideDownCharacterMap.put('B',new String(Character.toChars(0xa4ed)));
		upsideDownCharacterMap.put('C',new String(Character.toChars(0x0186)));
		upsideDownCharacterMap.put('D',new String(Character.toChars(0xa4f7)));
		upsideDownCharacterMap.put('E',new String(Character.toChars(0x018e)));
		upsideDownCharacterMap.put('F',new String(Character.toChars(0xa4de)));
		upsideDownCharacterMap.put('G',new String(Character.toChars(0xa4e8)));
		upsideDownCharacterMap.put('H',new String(Character.toChars(0x0048)));
		upsideDownCharacterMap.put('I',new String(Character.toChars(0x0049)));
		upsideDownCharacterMap.put('J',new String(Character.toChars(0x017f)));
		upsideDownCharacterMap.put('K',new String(Character.toChars(0xa4d8)));
		upsideDownCharacterMap.put('L',new String(Character.toChars(0xa4f6)));
		upsideDownCharacterMap.put('M',new String(Character.toChars(0x0057)));
		upsideDownCharacterMap.put('N',new String(Character.toChars(0x004e)));
		upsideDownCharacterMap.put('O',new String(Character.toChars(0x004f)));
		upsideDownCharacterMap.put('P',new String(Character.toChars(0xa4d2)));
		upsideDownCharacterMap.put('Q',new String(Character.toChars(0x1ff8)));
		upsideDownCharacterMap.put('R',new String(Character.toChars(0xa4e4)));
		upsideDownCharacterMap.put('S',new String(Character.toChars(0x0053)));
		upsideDownCharacterMap.put('T',new String(Character.toChars(0xa4d5)));
		upsideDownCharacterMap.put('U',new String(Character.toChars(0xa4f5)));
		upsideDownCharacterMap.put('V',new String(Character.toChars(0x039b)));
		upsideDownCharacterMap.put('W',new String(Character.toChars(0x004d)));
		upsideDownCharacterMap.put('X',new String(Character.toChars(0x0058)));
		upsideDownCharacterMap.put('Y',new String(Character.toChars(0x2144)));
		upsideDownCharacterMap.put('Z',new String(Character.toChars(0x005a)));
		
		upsideDownCharacterMap.put('a',new String(Character.toChars(0x0250)));
		upsideDownCharacterMap.put('b',new String(Character.toChars(0x0071)));
		upsideDownCharacterMap.put('c',new String(Character.toChars(0x0254)));
		upsideDownCharacterMap.put('d',new String(Character.toChars(0x0070)));
		upsideDownCharacterMap.put('e',new String(Character.toChars(0x01dd)));
		upsideDownCharacterMap.put('f',new String(Character.toChars(0x025f)));
		upsideDownCharacterMap.put('g',new String(Character.toChars(0x0183)));
		upsideDownCharacterMap.put('h',new String(Character.toChars(0x0265)));
		upsideDownCharacterMap.put('i',new String(Character.toChars(0x1d09)));
		upsideDownCharacterMap.put('j',new String(Character.toChars(0x027e)));
		upsideDownCharacterMap.put('k',new String(Character.toChars(0x029e)));
		upsideDownCharacterMap.put('l',new String(Character.toChars(0x006c)));
		upsideDownCharacterMap.put('m',new String(Character.toChars(0x026f)));
		upsideDownCharacterMap.put('n',new String(Character.toChars(0x0075)));
		upsideDownCharacterMap.put('o',new String(Character.toChars(0x006f)));
		upsideDownCharacterMap.put('p',new String(Character.toChars(0x0064)));
		upsideDownCharacterMap.put('q',new String(Character.toChars(0x0062)));
		upsideDownCharacterMap.put('r',new String(Character.toChars(0x0279)));
		upsideDownCharacterMap.put('s',new String(Character.toChars(0x0073)));
		upsideDownCharacterMap.put('t',new String(Character.toChars(0x0287)));
		upsideDownCharacterMap.put('u',new String(Character.toChars(0x006e)));
		upsideDownCharacterMap.put('v',new String(Character.toChars(0x028c)));
		upsideDownCharacterMap.put('w',new String(Character.toChars(0x028d)));
		upsideDownCharacterMap.put('x',new String(Character.toChars(0x0078)));
		upsideDownCharacterMap.put('y',new String(Character.toChars(0x028e)));
		upsideDownCharacterMap.put('z',new String(Character.toChars(0x007a)));
		
		upsideDownCharacterMap.put('?',new String(Character.toChars(0x00bf)));
		upsideDownCharacterMap.put('!',new String(Character.toChars(0x00a1)));
		upsideDownCharacterMap.put('\'',new String(Character.toChars(0x002c)));
		upsideDownCharacterMap.put(',',new String(Character.toChars(0x0027)));
		upsideDownCharacterMap.put('>',new String(Character.toChars(0x003c)));
		upsideDownCharacterMap.put('<',new String(Character.toChars(0x003e)));
		upsideDownCharacterMap.put('_',new String(Character.toChars(0x203e)));
		upsideDownCharacterMap.put('&',new String(Character.toChars(0x214b)));
	}
}