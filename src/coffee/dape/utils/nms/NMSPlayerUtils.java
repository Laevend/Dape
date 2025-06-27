package coffee.dape.utils.nms;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import coffee.dape.utils.DelayUtils;
import coffee.dape.utils.Logg;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.a;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;

public class NMSPlayerUtils
{
	/**
	 * Gets an NMS player object
	 * @param p Player
	 * @return EntityPlayer
	 */
	public static EntityPlayer getNMSPlayer(Player p)
	{
		return ((CraftPlayer) p).getHandle();
	}
	
	public static EntityPlayer[] getNMSPlayers(Player... ps)
	{
		EntityPlayer[] eps = new EntityPlayer[ps.length];
		
		for(int i = 0; i < ps.length; i++)
		{
			eps[i] = getNMSPlayer(ps[i]);
		}
		
		return eps;
	}
	
	public static EntityPlayer[] getNMSPlayers(Collection<? extends Player> ps)
	{
		EntityPlayer[] eps = new EntityPlayer[ps.size()];
		
		int aIterator = 0;
		Iterator<? extends Player> it = ps.iterator();
		
		while(it.hasNext())
		{
			eps[aIterator] = getNMSPlayer(it.next());
			aIterator++;
		}
		
		return eps;
	}
	
	/**
	 * Sends a packet to a collection of players
	 * @param packet Packet to send
	 * @param players Collection of players
	 */
	public static void sendPacket(Packet<? extends PacketListener> packet,Collection<? extends Player> players)
	{
		players.forEach(p -> sendPacket(packet,p));
	}
	
	/**
	 * Sends a packet to a player
	 * @param packet Packet to send
	 * @param player Player to send packet to
	 */
	public static void sendPacket(Packet<? extends PacketListener> packet,Player player)
	{
		EntityPlayer entityPlayer = NMSPlayerUtils.getNMSPlayer(player);
		PlayerConnection connection = entityPlayer.f;
		connection.a(packet);
		//connection.a(packet,null);
		Logg.verb("Packet " + packet.getClass().getSimpleName() + " sent to " + player.getName(),Logg.VerbGroup.NMSPLAYER);
		
		// 1.20.2
		//NMSPlayerUtils.getNMSPlayer(player).c.a(packet,null);
		
		// 1.19.4
		//NMSPlayerUtils.getNMSPlayer(player).b.a(packet);
		//NMSPlayerUtils.getNMSPlayer(player).playerConnection.sendPacket(packet) (estimated)
	}
	
	/**
	 * Sends a list of packets to a collection of players
	 * @param packets List of packets to send
	 * @param players Collection of players
	 */
	public static void sendPackets(List<Packet<? extends PacketListener>> packets,Collection<? extends Player> players)
	{
		players.forEach(p -> sendPackets(packets,p));
	}
	
	/**
	 * Sends a list of packets to a player
	 * @param packets List of packets to send
	 * @param player Player to send packets to
	 */
	public static void sendPackets(List<Packet<? extends PacketListener>> packets,Player player)
	{
		packets.forEach(p -> sendPacket(p,player));
	}
	
	/**
	 * Sends a packet to all but one player on the server
	 * @param packet Packet to send
	 * @param player Player who is exempt from receiving this packet
	 */
	public static void sendPacketExceptOne(Packet<? extends PacketListener> packet,Player player)
	{
		Bukkit.getOnlinePlayers().forEach(p -> 
		{
			if(!p.getUniqueId().equals(player.getUniqueId()))
			{
				sendPacket(packet,p);
			}
		});
	}
	
	/**
	 * Sends a packet to all but one player on the server
	 * @param packet Packet to send
	 * @param player Player who is exempt from receiving this packet
	 */
	public static void sendPacketExceptOne(Packet<? extends PacketListener> packet,EntityPlayer player)
	{
		Bukkit.getOnlinePlayers().forEach(p -> 
		{
			if(!p.getUniqueId().equals(player.cG()))
			{
				sendPacket(packet,p);
			}
		});
	}
	
	/**
	 * Sends a list of packets to all but one player on the server
	 * @param packets List of packets to send
	 * @param player Player who is exempt from receiving these packets
	 */
	public static void sendPacketsExceptOne(List<Packet<? extends PacketListener>> packets,Player player)
	{
		Bukkit.getOnlinePlayers().forEach(p -> 
		{
			if(!p.getUniqueId().equals(player.getUniqueId()))
			{
				sendPackets(packets,p);
			}
		});
	}
	
	/**
	 * Sends a list of packets to all but one player on the server
	 * @param packets List of packets to send
	 * @param player Player who is exempt from receiving these packets
	 */
	public static void sendPacketsExceptOne(List<Packet<? extends PacketListener>> packets,EntityPlayer player)
	{
		Bukkit.getOnlinePlayers().forEach(p -> 
		{
			if(!p.getUniqueId().equals(player.cG()))
			{
				sendPackets(packets,p);
			}
		});
	}
	
	/**
	 * Sends a packet to all players on the server
	 * @param packet Packet to send
	 */
	public static void sendPacketToAll(Packet<? extends PacketListener> packet)
	{
		Bukkit.getOnlinePlayers().forEach(p -> sendPacket(packet,p));
	}
	
	/**
	 * Sends a list of packets to all players on the server
	 * @param packets List of packets to send
	 */
	public static void sendPacketsToAll(List<Packet<? extends PacketListener>> packets)
	{
		Bukkit.getOnlinePlayers().forEach(p -> sendPackets(packets,p));
	}
	
	/**
	 * Updates the player
	 * 
	 * <p>Whenever you change something about the players GameProfile, you must update them using this method.
	 * @param player Player to update to all other players
	 */
	public static void updatePlayer(Player player)
	{
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		int id = entityPlayer.ao();
		
		//1.20.4
		// an() is just the method that returns entity id 'o'
		//int id = entityPlayer.an();
		
		//1.20.2
		// ah() is just the method that returns entity id 'q'
		//int id = entityPlayer.ah();
		
		//1.20.1
		//int id = entityPlayer.af();
		
		UUID uuid = entityPlayer.cG();
		
		//1.20.4
		//UUID uuid = entityPlayer.cw();
		
		//1.20.2
		//UUID uuid = entityPlayer.cv();
		
		//1.20.1
		//UUID uuid = entityPlayer.ct();
		
		//ClientboundPlayerInfoUpdatePacket playerRemove = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket);
		
		// 1.19.2
		//PacketPlayOutPlayerInfo playerRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e,entityPlayer); // REMOVE_PLAYER
		//PacketPlayOutPlayerInfo playerAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a,entityPlayer); // ADD_PLAYER
		
		// 1.19.4 to 1.20.4
		//ClientboundPlayerInfoRemovePacket playerRemove = new ClientboundPlayerInfoRemovePacket(List.of(uuid));
		//ClientboundPlayerInfoUpdatePacket playerAdd = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.a,entityPlayer);
		
		// 1.21
		ClientboundPlayerInfoRemovePacket playerRemove = new ClientboundPlayerInfoRemovePacket(List.of(uuid));
		ClientboundPlayerInfoUpdatePacket playerAdd = new ClientboundPlayerInfoUpdatePacket(a.a,entityPlayer);
		
		
		sendPacketsToAll(Lists.newArrayList(playerRemove,playerAdd));
		
		DelayUtils.executeDelayedTask(new Runnable()
		{
			@Override
			public void run()
			{
				PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(id);
				//PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(entityPlayer);
				PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(entityPlayer,null);
				
				DataWatcher watcher = entityPlayer.ar();
				
				//1.20.2
	            //DataWatcher watcher = entityPlayer.al();
	            watcher.a(new DataWatcherObject<>(16, DataWatcherRegistry.a),(byte)127);
	            
	            // TODO fix this
	            PacketPlayOutEntityMetadata skinLayer = new PacketPlayOutEntityMetadata(id,watcher.c());
				
	            sendPacketsExceptOne(Lists.newArrayList(destroyPacket,spawnPacket,skinLayer),player);
			}
		},1L);
	}
	
	/**
	 * Finding Entity id (Reverse Engineering Lookup)
	 * 
	 * 1.20.1
	 * 
	 * Declared in net.minecraft.world.entity;
	 * 
	 * private static final AxisAlignedBB k = new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
	 * private static final double l = 0.014;
	 * private static final double m = 0.007;
	 * private static final double n = 0.0023333333333333335;
	 * public static final String G = "UUID";
	 * private static double o = 1.0;
	 * private final EntityTypes<?> p;
	 * private int q;
	 * 
	 * public int af()
	 * {
	 * 		return this.q; // this is the id
	 * }
	 * 
	 * 1.20.2
	 * 
	 * Declared in net.minecraft.world.entity;
	 * 
	 * private static final AxisAlignedBB k = new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
	 * private static final double l = 0.014;
	 * private static final double m = 0.007;
	 * private static final double n = 0.0023333333333333335;
	 * public static final String G = "UUID";
	 * private static double o = 1.0;
	 * private final EntityTypes<?> p;
	 * private int q;
	 * 
	 * public int ah()
	 * {
	 * 		return this.q;	// this is the id
	 * }
	 */
	
	/**
	 * Gets the game profile of a player
	 * @param p Player
	 * @return GameProfile of a player
	 */
	public static GameProfile getGameProfile(Player p)
	{
		EntityPlayer entityPlayer = getNMSPlayer(p);
		return entityPlayer.gi();
		// return entityPlayer.fQ(); 1.20.2
		// return entityPlayer.fM(); 1.20.1
		//return entityPlayer.fI(); 1.19.4
		//return entityPlayer.fz(); 1.19
	}
}
