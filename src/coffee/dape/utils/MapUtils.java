package coffee.dape.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R4.map.CraftMapView;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;

import coffee.dape.Dape;

public class MapUtils
{
	public static class ImageMapper implements Listener
	{
		private static Path mapImagesPath = Dape.featureFilePath("image_maps");
		private static Path mapDataPath = Dape.featureFilePath("map_data");
		private static Map<Integer,MapImage> imgs = new HashMap<>();
		private static boolean lazyLoading = true;
		
		@EventHandler(ignoreCancelled = true)
		public void onMapInit(MapInitializeEvent e)
		{
			if(!imgs.containsKey(e.getMap().getId())) { return; }
			
			MapImage mapImage = imgs.get(e.getMap().getId());
			MapView view = e.getMap();
			
			clearRenderers(view);
			
			view.setScale(Scale.NORMAL);
			view.setUnlimitedTracking(false);
			view.setTrackingPosition(false);
			view.setLocked(true);
			view.addRenderer(new MapUtils().new ImageMapRenderer(mapImage.getImage()));
		}
		
		public static synchronized void addImageMap(MapView view,BufferedImage img)
		{
			try
			{			
				FUtils.createDirectories(mapImagesPath);
				UUID imageId = UUID.randomUUID();
				Path filePath = Paths.get(mapImagesPath.toString() + File.separator + imageId.toString() + ".png");
				ImageIO.write(img,"png",filePath.toFile());
				
				Logg.verb("Adding ImageMap " + view.getId(),Logg.VerbGroup.MAP_UTILS);
				imgs.put(view.getId(),new MapImage(imageId));
			}
			catch (Exception e)
			{
				Logg.error("Error! could not save image!",e);
			}
		}
		
		public static void load()
		{
			imgs.clear();
			List<String> csv = FUtils.readCSV(mapDataPath);
			
			for(String csvLine : csv)
			{
				try
				{
					String[] part = csvLine.split(",");
					imgs.put(Integer.parseInt(part[0]),new MapImage(UUID.fromString(part[1])));
				}
				catch(Exception e)
				{
					Logg.error("MapImage could not be created! Invalid arguments? CSVLine -> " + csvLine,e);
				}
			}
		}
		
		public static void save()
		{
			List<String> csv = new ArrayList<>();
			
			for(Entry<Integer,MapImage> entry : imgs.entrySet())
			{
				csv.add(entry.getKey() + "," + entry.getValue().getId().toString());
			}
			
			FUtils.writeCSV(mapDataPath,csv);
		}
		
		// Instance of a MapImage. Depending on lazyLoading, the buffered image will already be loaded or not
		private static class MapImage
		{
			private BufferedImage image = null;
			private final UUID id;
			private Path imagePath;
			
			public MapImage(UUID uuid)
			{
				this.id = uuid;
				this.imagePath = Paths.get(mapImagesPath.toString() + File.separator + uuid.toString() + ".png");
				
				if(lazyLoading) { return; }
				
				// No lazy loading
				new Thread()
				{{
					loadImage();
				}}.start();
			}
			
			public void loadImage()
			{
				try
		        {
		            if(!Files.exists(imagePath))
		            {
		            	Logg.error("Cannot load image at '" + imagePath.toString() + "'. File does not exist!");
		            }
		            else
		            {
		            	image = ImageIO.read(imagePath.toFile());
		            }
		        }
		        catch(IOException e)
		        {
		            Logg.error("Failed to read image from file: " + imagePath.toString(),e);
		        }
			}

			public UUID getId()
			{
				return id;
			}

			public BufferedImage getImage()
			{
				if(!isImageLoaded()) { loadImage(); }
				return image;
			}

			public boolean isImageLoaded()
			{
				return image != null;
			}
		}
	}	
	
	/**
	 * Gets a MapView containing a image renderer
	 * @param image Image to render on the map view
	 * @return MapView
	 */
	private static synchronized MapView getMapView(BufferedImage image)
	{
		try
		{
			MapView view = Bukkit.createMap(Bukkit.getWorlds().get(0));
			
			clearRenderers(view);	
			
			view.setScale(Scale.NORMAL);
			view.setUnlimitedTracking(false);
			view.setTrackingPosition(false);
			view.setLocked(true);
			view.addRenderer(new MapUtils().new ImageMapRenderer(image));
			return view;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}		
	}
	
	/**
	 * Gets a MapView containing a personal image renderer
	 * @param image Image to render on the map view
	 * @param owner Owner of map view (only player who can see the image)
	 * @return MapView
	 */
	private static synchronized MapView getPersonalMapView(BufferedImage image,UUID owner)
	{
		try
		{
			MapView view = Bukkit.createMap(Bukkit.getWorlds().get(0));
			
			clearRenderers(view);	
			
			view.setScale(Scale.NORMAL);
			view.setUnlimitedTracking(false);
			view.setTrackingPosition(false);
			view.setLocked(true);
			view.addRenderer(new MapUtils().new PersonalImageMapRenderer(image,owner));
			return view;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}	
	}
	
	/**
	 * Turns an image into a FilledMap ItemStack with that image drawn onto it (128x128 images recommended)
	 * @param url URL destination of an image
	 * @return ItemStack A Filled map with the image applied to it
	 */
	public static ItemStack urlToMap(URL url)
	{
		BufferedImage img = ImageUtils.getImageFromURL(url);
		
		if(img == null) { Logg.error("Cannot create map! Image is null!"); return new ItemStack(Material.AIR); }
		
		MapView view = getMapView(img);

		try
		{
			ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
			MapMeta meta = (MapMeta) mapItem.getItemMeta();
			meta.setMapView(view);
			mapItem.setItemMeta(meta);
			ImageMapper.addImageMap(view,img);
			
			return mapItem;
		} 
		catch (Exception e)
		{
			Logg.error("Error! could not serialise BufferedImage!",e);
			return new ItemStack(Material.AIR);
		}
	}
	
	/**
	 * Turns an image into a FilledMap ItemStack with that image drawn onto it (128x128 images recommended)
	 * @param url URL destination of an image
	 * @return ItemStack A Filled map with the image applied to it
	 */
	public static ItemStack imageToMap(BufferedImage img)
	{
		if(img == null) { Logg.error("Cannot create map! Image is null!"); return new ItemStack(Material.AIR); }
		
		// By default map has no MapView, so we must create one
		MapView view = getMapView(img);

		try
		{
			ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
			MapMeta meta = (MapMeta) mapItem.getItemMeta();
			meta.setMapView(view);
			mapItem.setItemMeta(meta);
			ImageMapper.addImageMap(view,img);
			
			return mapItem;
		} 
		catch (Exception e)
		{
			Logg.error("Error! could not serialise BufferedImage!",e);
			return new ItemStack(Material.AIR);
		}
	}
	
	/**
	 * Turns an image into a FilledMap ItemStack with that image drawn onto it (128x128 images recommended)
	 * @param url URL destination of an image
	 * @return ItemStack A Filled map with the image applied to it
	 */
	public static ItemStack personalImageToMap(BufferedImage img,UUID owner)
	{
		if(img == null) { return new ItemStack(Material.MAP); }
		
		// By default map has no MapView, so we must create one
		MapView view = getPersonalMapView(img,owner);

		try
		{
			ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
			MapMeta meta = (MapMeta) mapItem.getItemMeta();
			meta.setMapView(view);
			mapItem.setItemMeta(meta);
			
			return mapItem;
		} 
		catch (Exception e)
		{
			Logg.error("Error! could not serialise BufferedImage!",e);
			return new ItemStack(Material.AIR);
		}
	}
	
	/**
	 * Converts a BufferedImage to a base64 encoded string
	 * @param bi BufferedImage to encode
	 * @return base64 encoded string representation of this BufferedImage
	 * @throws IOException
	 */
	public static String toBase64(BufferedImage bi) throws IOException
    {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bi,"png",baos);
		String encoded = Base64.getEncoder().encodeToString(baos.toByteArray());
		return encoded;
	}
    
    /**
     * Converts an encoded image into a BufferedImage
     * @param encodedImage String of an encoded image
     * @return BufferedImage
     * @throws IOException
     */
    public static BufferedImage fromBase64(String encodedImage) throws IOException
    {
		byte[] bytes = Base64.getDecoder().decode(encodedImage);
    	InputStream is = new ByteArrayInputStream(bytes);
		BufferedImage bi = ImageIO.read(is);
		return bi;
    }
    
    /**
     * Uses reflection to clear existing render objects from this map
     * view because md_5 in his infinite fucking wisdom returns a 'COPY'
     * of the render objects instead of the original list... genius!
     * @param view Map view to clear renders from
     */
    private static void clearRenderers(MapView view)
    {
    	try
    	{
	    	CraftMapView craftView = ((CraftMapView) view);
			Field renderersField = CraftMapView.class.getDeclaredField("renderers");
			Field worldMapField = CraftMapView.class.getDeclaredField("worldMap");
			renderersField.setAccessible(true);
			worldMapField.setAccessible(true);
			
			@SuppressWarnings("unchecked")
			List<MapRenderer> renderers = (List<MapRenderer>) renderersField.get(craftView);
			renderers.clear();
	    }
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
    
    /**
     * 
     * @author Laeven
     * Render an image onto a map
     */
	private class ImageMapRenderer extends MapRenderer
	{
		private BufferedImage mapImage;
		private boolean rendered = false;
		
		public ImageMapRenderer(BufferedImage image)
		{
			super(false);
			this.mapImage = image;
		}
		
		@Override
		public void render(MapView view,MapCanvas canvas,Player player)
		{
			if(mapImage == null || rendered) { return; }
			
			DelayUtils.executeDelayedTask(() ->
			{
				canvas.drawImage(0,0,mapImage);
				rendered = true;
				// Spread out rendering
			},System.nanoTime() % 60);
		}
	}
	
	private class PersonalImageMapRenderer extends MapRenderer
	{
		private BufferedImage mapImage;
		private BufferedImage blank;
		private UUID owner;
		private boolean rendered = false;
		
		public PersonalImageMapRenderer(BufferedImage image,UUID owner)
		{
			super(true);
			this.mapImage = image;
			this.owner = owner;
			
			this.blank = new BufferedImage(128,128,BufferedImage.TYPE_INT_RGB);
			this.blank.getGraphics().setColor(Color.BLACK);
			this.blank.getGraphics().fillRect(0,0,128,128);
		}
		
		@Override
		public void render(MapView view,MapCanvas canvas,Player player)
		{
			if(mapImage == null || (rendered && player.getUniqueId().equals(owner))) { return; }
			
			DelayUtils.executeDelayedTask(() ->
			{
				if(player.getUniqueId().equals(owner))
				{
					canvas.drawImage(0,0,mapImage);
					rendered = true;
					return;
				}
				
				canvas.drawImage(0,0,blank);
				
				// Spread out rendering
			},System.nanoTime() % 60);
		}
	}
}
