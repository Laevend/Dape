package coffee.dape.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * 
 * @author Laeven
 *
 */
public class WebUtils
{
	public static String sendGet(String url)
	{
		try
		{
			URL obj = URI.create(url).toURL();
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			con.setRequestMethod("GET");
	
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}
			
			in.close();
			return response.toString();
		}
		catch(Exception e)
		{
			Logg.error("Could not send GET web request at '" + url + "'",e);
			return null;
		}
	}
	
	public static String sendPost(String url,String data)
	{
		try
		{
			URL obj = URI.create(url).toURL();
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			con.setRequestMethod("POST");
			
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(data);
			wr.flush();
			wr.close();
	
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}
			
			in.close();
			return response.toString();
		}
		catch(Exception e)
		{
			Logg.error("Could not send POST web request at '" + url + "'",e);
			return null;
		}
	}
}
