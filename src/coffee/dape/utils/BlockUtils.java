package coffee.dape.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R4.block.data.CraftBlockData;
import org.bukkit.entity.Player;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockUtils
{
	private static final EnumSet<BlockFace> directRelatives = EnumSet.of(BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST,BlockFace.UP,BlockFace.DOWN);
	
	static
	{
		mapNMSBlockTypes();
	}
	
	/**
	 * Gets array of blocks relative to another block
	 * @param block Block to get relative blocks from
	 * @return Array of relative blocks
	 */
	public static Block[] getAdjacentBlocks(Block block)
	{
		return new Block[] 
		{
			block.getRelative(BlockFace.NORTH),
			block.getRelative(BlockFace.SOUTH),
			block.getRelative(BlockFace.EAST),
			block.getRelative(BlockFace.WEST),
			block.getRelative(BlockFace.UP),
			block.getRelative(BlockFace.DOWN)
		};
	}
	
	/**
	 * Gets a list of blocks relative to another block
	 * @param block Block to get relative blocks from
	 * @param excemptBlockFace BlockFace to not include
	 * @return List of relative blocks
	 */
	public static Map<BlockFace,Block> getAdjacentBlocksMap(Block block)
	{
		Map<BlockFace,Block> relativeBlocks = new EnumMap<>(BlockFace.class);
		
		for(BlockFace face : directRelatives)
		{
			relativeBlocks.put(face,block.getRelative(face));
		}
		
		return relativeBlocks;
	}
	
	/**
	 * Gets a list of blocks relative to another block with an exempt block face
	 * @param block Block to get relative blocks from
	 * @param excemptBlockFace BlockFace to not include
	 * @return List of relative blocks
	 */
	public static List<Block> getAdjacentBlocksExcept(Block block,BlockFace exemptBlockFace)
	{
		List<Block> relativeBlocks = new ArrayList<>();
		
		for(BlockFace face : directRelatives)
		{
			if(face == exemptBlockFace) { continue; }
			relativeBlocks.add(block.getRelative(face));
		}
		
		return relativeBlocks;
	}
	
	/**
	 * Gets a list of blocks relative to another block with an exempt block face
	 * @param block Block to get relative blocks from
	 * @param excemptBlockFace BlockFace to not include
	 * @return List of relative blocks
	 */
	public static Map<BlockFace,Block> getAdjacentBlocksExceptMap(Block block,BlockFace exemptBlockFace)
	{
		Map<BlockFace,Block> relativeBlocks = new EnumMap<>(BlockFace.class);
		
		for(BlockFace face : directRelatives)
		{
			if(face == exemptBlockFace) { continue; }
			relativeBlocks.put(face,block.getRelative(face));
		}
		
		return relativeBlocks;
	}
	
	private static Map<Material,net.minecraft.world.level.block.Block> nmsBlockMap;
	
	public static void mapNMSBlockTypes()
	{
		nmsBlockMap = new HashMap<>();
		
		try
		{
			for(Field f : net.minecraft.world.level.block.Blocks.class.getDeclaredFields())
			{
				// Blocks in 1.20.4 are final so need to un-final them
				f.setAccessible(true);
				
				// Also there seems to be a net.minecraft.world.level.block.state.BlockBase type which needs to be ignored
				if(!f.getClass().equals(net.minecraft.world.level.block.Block.class)) { continue; }
				
				net.minecraft.world.level.block.Block block = (net.minecraft.world.level.block.Block) f.get(null);
				String blockName = block.toString().replace("Block{","").replace("}","");
				Material mat = MaterialUtils.getSpigotMaterial(blockName);
				nmsBlockMap.put(mat,block);
				//Logg.verbose("Mapping " + blockName + " to " + mat.toString());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * import net.minecraft.world.level.World;
		import net.minecraft.world.level.block.state.IBlockData;
		import net.minecraft.world.level.chunk.Chunk;
	 */
	
	public static class FastBlockSetSession
	{
		private List<BlockState> states;
		
		public FastBlockSetSession()
		{
			states = new ArrayList<BlockState>();
		}
		
		public FastBlockSetSession(int numberOfBlocksExpected)
		{
			states = new ArrayList<BlockState>(numberOfBlocksExpected);
		}
		
		public void updateClients(Set<Player> players)
		{
			for(Player p : players)
			{
				if(!p.isOnline()) { continue; }
				p.sendBlockChanges(states);
			}
			
			states.clear();
		}
		
		public void updateAllClients()
		{
			for(Player p : Bukkit.getOnlinePlayers())
			{
				p.sendBlockChanges(states);
			}
			
			states.clear();
		}
		
		public void setBlockType(Location loc,Material mat)
		{
			setBlockInNativeDataPalette(loc.getWorld(),loc.getBlockX(),loc.getBlockY(),loc.getBlockZ(),nmsBlockMap.get(mat),false);
			states.add(loc.getBlock().getState());
		}
		
		public void setBlockType(Location loc,BlockData data)
		{
			setBlockInNativeDataPalette(loc.getWorld(),loc.getBlockX(),loc.getBlockY(),loc.getBlockZ(),data,false);
			states.add(loc.getBlock().getState());
		}
		
		public void setBlockType(World world,int x,int y,int z,BlockData data)
		{
			setBlockInNativeDataPalette(world,x,y,z,data,false);
			states.add(world.getBlockAt(x,y,z).getState());
		}
		
		public void setBlockInNativeDataPalette(World world,int x,int y,int z,net.minecraft.world.level.block.Block block,boolean applyPhysics)
		{
			//net.minecraft.server.v1_14_R1.World nmsWorld = ((CraftWorld) world).getHandle();
			net.minecraft.world.level.World nmsWorld = ((CraftWorld) world).getHandle();
			
			//net.minecraft.server.v1_14_R1.Chunk nmsChunk = nmsWorld.getChunkAt(x >> 4, z >> 4);
			net.minecraft.world.level.chunk.Chunk nmsChunk = nmsWorld.d(x >> 4, z >> 4);
		    
			BlockPosition bp = new BlockPosition(x,y,z);
			
			// Some older way used in 1.19
		    //IBlockData ibd = net.minecraft.server.v1_14_R1.Block.getByCombinedId(blockId + (data << 12));
			
			//IBlockData ibd = block.getBlockData();
			
			// 1.20.2
		    //BlockData ibd = block.n();
		    
			// 1.21.1
		    //IBlockData ibd = block.o();
			
			IBlockData ibd = block.m();
			
			// 1.21.1
		    //nmsChunk.setBlockState(bp,ibd,applyPhysics,true);
			
			// Not sure what this does but it's the new method.
			// Last argument is now an int and it's just 'i' in the mappings.
			// 'i' is used as follows:
			
            //boolean flag3 = (i & 64) != 0;
            //boolean flag4 = (i & 256) == 0;
             
			//if(!this.r.C && (i & 512) == 0 && (!this.r.captureBlockStates || block instanceof BlockTileEntity))
			//{
			//	iblockdata.a (this.r, blockposition, iblockdata1, flag3);
			//}
			
			// TODO Test with numbers to see results?
             
			nmsChunk.a(bp,ibd,1);
		}
		
		public void setBlockInNativeDataPalette(World world,int x,int y,int z,BlockData data,boolean applyPhysics)
		{
			//net.minecraft.server.v1_14_R1.World nmsWorld = ((CraftWorld) world).getHandle();
			net.minecraft.world.level.World nmsWorld = ((CraftWorld) world).getHandle();
			
			//net.minecraft.server.v1_14_R1.Chunk nmsChunk = nmsWorld.getChunkAt(x >> 4, z >> 4);
			net.minecraft.world.level.chunk.Chunk nmsChunk = nmsWorld.d(x >> 4, z >> 4);
		    
			BlockPosition bp = new BlockPosition(x,y,z);
			IBlockData iBlockData = ((CraftBlockData) data).getState();
		    
		    nmsChunk.a(bp,iBlockData,1);
		}
	}
}
