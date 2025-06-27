package coffee.dape.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class SoundUtils
{
	public static final SoundMixer HEAVY_CLINK;
	public static final SoundMixer FURNACE_METAL_SCRAPE;
	public static final SoundMixer HEAVY_WOOD_BANGING;
	public static final SoundMixer MECHANICAL_POSITIVE;
	public static final SoundMixer MECHANICAL_NEGATIVE;
	public static final SoundMixer AXIS_UNCLAIM;
	public static final SoundMixer AXIS_CLAIM;
	
	public static final SoundMixer HOME_SET_DEFAULT;

	public static final SoundMixer SWITCH_CLINK;
	public static final SoundMixer DUSTING;
	
	static
	{
		HEAVY_CLINK = new SoundUtils().new SoundMixer(Sound.BLOCK_BASALT_PLACE,0.1f);
		HEAVY_CLINK.addSound(Sound.BLOCK_IRON_DOOR_CLOSE,2.0f,2);
		
		FURNACE_METAL_SCRAPE = new SoundUtils().new SoundMixer(Sound.BLOCK_BAMBOO_WOOD_HANGING_SIGN_FALL,0.1f);
		FURNACE_METAL_SCRAPE.addSound(Sound.ITEM_BUNDLE_DROP_CONTENTS,0.1f,2);
		FURNACE_METAL_SCRAPE.addSound(Sound.ITEM_AXE_SCRAPE,0.1f,2);
		
		HEAVY_WOOD_BANGING = new SoundUtils().new SoundMixer(Sound.BLOCK_LODESTONE_BREAK,0.1f);
		HEAVY_WOOD_BANGING.addSound(Sound.BLOCK_BAMBOO_WOOD_HANGING_SIGN_FALL,0.7f,0);
		HEAVY_WOOD_BANGING.addSound(Sound.BLOCK_SCAFFOLDING_BREAK,0.1f,0);
		
		MECHANICAL_POSITIVE = new SoundUtils().new SoundMixer(Sound.BLOCK_PISTON_EXTEND,0.75f,0.7f);
		MECHANICAL_POSITIVE.addSound(Sound.BLOCK_CHAIN_FALL,1.0f,0);
		MECHANICAL_POSITIVE.addSound(Sound.BLOCK_LAVA_EXTINGUISH,1.5f,0.5f,0);
		MECHANICAL_POSITIVE.addSound(Sound.BLOCK_IRON_DOOR_OPEN,1.5f,0);
		
		MECHANICAL_NEGATIVE = new SoundUtils().new SoundMixer(Sound.BLOCK_PISTON_CONTRACT,0.75f,0.7f);
		MECHANICAL_NEGATIVE.addSound(Sound.BLOCK_CHAIN_FALL,1.0f,0);
		MECHANICAL_NEGATIVE.addSound(Sound.BLOCK_LAVA_EXTINGUISH,1.5f,0.5f,0);
		MECHANICAL_NEGATIVE.addSound(Sound.BLOCK_IRON_DOOR_CLOSE,1.5f,0);
		
		AXIS_CLAIM = new SoundUtils().new SoundMixer(Sound.BLOCK_NOTE_BLOCK_BIT,0.5f);
		AXIS_CLAIM.addSound(Sound.BLOCK_NETHER_GOLD_ORE_HIT,0.5f,0);
		
		AXIS_CLAIM.addSound(Sound.BLOCK_NOTE_BLOCK_BIT,1.0f,2);
		AXIS_CLAIM.addSound(Sound.BLOCK_NETHER_GOLD_ORE_HIT,1.0f,2);
		
		AXIS_CLAIM.addSound(Sound.BLOCK_NOTE_BLOCK_BIT,1.5f,4);
		AXIS_CLAIM.addSound(Sound.BLOCK_NETHER_GOLD_ORE_HIT,1.5f,4);
		
		AXIS_CLAIM.addSound(Sound.BLOCK_NOTE_BLOCK_BIT,2.0f,6);
		AXIS_CLAIM.addSound(Sound.BLOCK_NETHER_GOLD_ORE_HIT,2.0f,6);
		
		AXIS_UNCLAIM = new SoundUtils().new SoundMixer(Sound.BLOCK_NOTE_BLOCK_BIT,2.0f);
		AXIS_UNCLAIM.addSound(Sound.BLOCK_NETHER_GOLD_ORE_HIT,2.0f,0);
		
		AXIS_UNCLAIM.addSound(Sound.BLOCK_NOTE_BLOCK_BIT,1.5f,2);
		AXIS_UNCLAIM.addSound(Sound.BLOCK_NETHER_GOLD_ORE_HIT,1.5f,2);
		
		AXIS_UNCLAIM.addSound(Sound.BLOCK_NOTE_BLOCK_BIT,1.0f,4);
		AXIS_UNCLAIM.addSound(Sound.BLOCK_NETHER_GOLD_ORE_HIT,1.0f,4);
		
		AXIS_UNCLAIM.addSound(Sound.BLOCK_NOTE_BLOCK_BIT,0.5f,6);
		AXIS_UNCLAIM.addSound(Sound.BLOCK_NETHER_GOLD_ORE_HIT,0.5f,6);
		
		HOME_SET_DEFAULT = new SoundUtils().new SoundMixer(Sound.BLOCK_AMETHYST_CLUSTER_BREAK,0.5f);
		HOME_SET_DEFAULT.addSound(Sound.BLOCK_AMETHYST_CLUSTER_BREAK,2.0f,5);
		
		SWITCH_CLINK = new SoundUtils().new SoundMixer(Sound.BLOCK_COPPER_BULB_HIT,2.0f);
		SWITCH_CLINK.addSound(Sound.BLOCK_COPPER_TRAPDOOR_OPEN,2.0f,0);
		SWITCH_CLINK.addSound(Sound.BLOCK_COPPER_GRATE_HIT,1.5f,0);
		
		DUSTING = new SoundUtils().new SoundMixer(Sound.ITEM_BRUSH_BRUSHING_SAND,1.0f);
		DUSTING.addSound(Sound.ITEM_BRUSH_BRUSHING_SAND,0.5f,0);
		DUSTING.addSound(Sound.ITEM_BRUSH_BRUSHING_SAND,2.0f,4);
		DUSTING.addSound(Sound.ITEM_BRUSH_BRUSHING_SAND,2.0f,8);
	}
	
	public static void playSound(Player p,Sound s,float pitch)
	{
		p.playSound(p.getLocation(),s,SoundCategory.MASTER,1.0f,pitch);
	}
	
	public static void playSound(Location loc,Sound s,float pitch)
	{
		loc.getWorld().playSound(loc,s,1.0f,pitch);
	}
	
	public static void playSound(Player p,Sound s,float volume,float pitch)
	{
		p.playSound(p.getLocation(),s,SoundCategory.MASTER,volume,pitch);
	}
	
	public static void playSound(Location loc,Sound s,float volume,float pitch)
	{
		loc.getWorld().playSound(loc,s,volume,pitch);
	}
	
	public static void playErrorSound(Location loc)
	{
		playSound(loc,Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,1.0f,0.0f);
		playSound(loc,Sound.BLOCK_NOTE_BLOCK_GUITAR,1.0f,0.0f);
		playSound(loc,Sound.BLOCK_BONE_BLOCK_BREAK,1.0f,0.0f);
		playSound(loc,Sound.BLOCK_NOTE_BLOCK_BASEDRUM,1.0f,0.0f);
	}
	
	public static void playErrorSound(Player p)
	{
		playSound(p,Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,1.0f,0.0f);
		playSound(p,Sound.BLOCK_NOTE_BLOCK_GUITAR,1.0f,0.0f);
		playSound(p,Sound.BLOCK_BONE_BLOCK_BREAK,1.0f,0.0f);
		playSound(p,Sound.BLOCK_NOTE_BLOCK_BASEDRUM,1.0f,0.0f);
	}
	
	public static void playLongErrorSound(Location loc)
	{
		playSound(loc,Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,1.0f,1.0f);
		playSound(loc,Sound.BLOCK_NOTE_BLOCK_GUITAR,1.0f,1.0f);
		playSound(loc,Sound.BLOCK_BONE_BLOCK_BREAK,1.0f,1.0f);
		playSound(loc,Sound.BLOCK_NOTE_BLOCK_BASEDRUM,1.0f,1.0f);
		
		DelayUtils.executeDelayedTask(() ->
		{
			playSound(loc,Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,1.0f,0.0f);
			playSound(loc,Sound.BLOCK_NOTE_BLOCK_GUITAR,1.0f,0.0f);
			playSound(loc,Sound.BLOCK_BONE_BLOCK_BREAK,1.0f,0.0f);
			playSound(loc,Sound.BLOCK_NOTE_BLOCK_BASEDRUM,1.0f,0.0f);
		},3);
	}
	
	public static void playLongErrorSound(Player p)
	{
		playSound(p,Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,1.0f,1.0f);
		playSound(p,Sound.BLOCK_NOTE_BLOCK_GUITAR,1.0f,1.0f);
		playSound(p,Sound.BLOCK_BONE_BLOCK_BREAK,1.0f,1.0f);
		playSound(p,Sound.BLOCK_NOTE_BLOCK_BASEDRUM,1.0f,1.0f);
		
		DelayUtils.executeDelayedTask(() ->
		{
			playSound(p,Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,1.0f,0.0f);
			playSound(p,Sound.BLOCK_NOTE_BLOCK_GUITAR,1.0f,0.0f);
			playSound(p,Sound.BLOCK_BONE_BLOCK_BREAK,1.0f,0.0f);
			playSound(p,Sound.BLOCK_NOTE_BLOCK_BASEDRUM,1.0f,0.0f);
		},3);
	}
	
	public class SoundMixer
	{
		List<SoundTask> tasks = new ArrayList<>();
		
		public SoundMixer(Sound sound,float pitch,float volume)
		{
			tasks.add(new SoundTask(sound,pitch,volume,0));
		}
		
		public SoundMixer(Sound sound,float pitch)
		{
			tasks.add(new SoundTask(sound,pitch,1.0f,0));
		}
		
		public void addSound(Sound sound,float pitch,int ticksBeforePlay)
		{
			tasks.add(new SoundTask(sound,pitch,1.0f,ticksBeforePlay));
		}
		
		public void addSound(Sound sound,float pitch,float volume,int ticksBeforePlay)
		{
			tasks.add(new SoundTask(sound,pitch,volume,ticksBeforePlay));
		}
		
		public void play(Location loc)
		{
			for(SoundTask task : tasks)
			{
				DelayUtils.executeDelayedTask(() ->
				{
					playSound(loc,task.getSound(),task.getVolume(),task.getPitch());
				},task.getTicksBeforePlay());
			}
		}
		
		public void play(Player p)
		{
			for(SoundTask task : tasks)
			{
				DelayUtils.executeDelayedTask(() ->
				{
					playSound(p,task.getSound(),task.getVolume(),task.getPitch());
				},task.getTicksBeforePlay());
			}
		}
		
		public class SoundTask
		{
			private Sound sound;
			private float pitch;
			private float volume;
			private int ticksBeforePlay;
			
			public SoundTask(Sound sound,float pitch,float volume,int ticksBeforePlay)
			{
				this.sound = sound;
				this.pitch = pitch;
				this.volume = volume;
				this.ticksBeforePlay = ticksBeforePlay;
			}

			public Sound getSound()
			{
				return sound;
			}

			public float getPitch()
			{
				return pitch;
			}

			public float getVolume()
			{
				return volume;
			}

			public int getTicksBeforePlay()
			{
				return ticksBeforePlay;
			}
		}
	}
}
