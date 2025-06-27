package coffee.dape.utils;

public class BitUtils
{
	public static int read(int toRead,int shift)
	{
		return (toRead >> shift) & 1;
	}
	
	public static int flip(int toSet,int shift)
	{
		int newInt = toSet ^ (1 << shift);
		return newInt;
	}
}
