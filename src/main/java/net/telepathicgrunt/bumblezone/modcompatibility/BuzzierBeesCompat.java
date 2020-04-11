package net.telepathicgrunt.bumblezone.modcompatibility;

import java.util.Random;

import com.bagel.buzzierbees.common.entities.HoneySlimeEntity;
import com.bagel.buzzierbees.core.registry.BBBlocks;
import com.bagel.buzzierbees.core.registry.BBEntities;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.telepathicgrunt.bumblezone.blocks.BzBlocksInit;
import net.telepathicgrunt.bumblezone.generation.BzChunkGenerator;

public class BuzzierBeesCompat
{
	
	
	public static void setupBuzzierBees() 
	{
		ModChecking.buzzierBeesPresent = true;
		BzChunkGenerator.MOBS_SLIME_ENTRY = new Biome.SpawnListEntry(BBEntities.HONEY_SLIME.get(), 1, 1, 1);
		//BzBiomes.biomes.forEach(biome -> ((BzBaseBiome)biome).addModMobs(EntityClassification.CREATURE, BBEntities.HONEY_SLIME.get(), 1, 4, 8));
		
	}
	
	//1/10th of bees spawning will also spawn honey slime
	@SuppressWarnings("deprecation")
	public static void BBMobSpawnEvent(LivingSpawnEvent.CheckSpawn event)
	{
		MobEntity entity = (MobEntity)event.getEntity();
		IWorld world = event.getWorld();
		
		if(entity.getType() == EntityType.BEE && world.getRandom().nextInt(10) == 0) {
			MobEntity slimeentity = new HoneySlimeEntity(BBEntities.HONEY_SLIME.get(), entity.world);
			
			//move down to first non-air block
			BlockPos.Mutable blockpos = new BlockPos.Mutable(entity.getPosition());
			while(world.getBlockState(blockpos).isAir()) {
				blockpos.move(Direction.DOWN);
			}
			blockpos.move(Direction.UP);
			
			slimeentity.setLocationAndAngles(blockpos.getX(), blockpos.getY(), blockpos.getZ(), world.getRandom().nextFloat() * 360.0F, 0.0F);
			ILivingEntityData ilivingentitydata = null;
			ilivingentitydata = slimeentity.onInitialSpawn(world, world.getDifficultyForLocation(new BlockPos(slimeentity)), event.getSpawnReason(), ilivingentitydata, (CompoundNBT) null);
			world.addEntity(slimeentity);
		}
	}

	
	//New surface builder to use when Buzzier Bees is on
	private static final BlockState STONE = Blocks.STONE.getDefaultState();
	private static final BlockState FILLED_POROUS_HONEYCOMB = BzBlocksInit.FILLED_POROUS_HONEYCOMB.get().getDefaultState();
	private static final BlockState POROUS_HONEYCOMB = BzBlocksInit.POROUS_HONEYCOMB.get().getDefaultState();
	private static final BlockState HONEYCOMB_BLOCK = Blocks.HONEYCOMB_BLOCK.getDefaultState();
	private static final BlockState WAX_BLOCK = BBBlocks.WAX_BLOCK.get().getDefaultState();
	private static final BlockState CRYSTALLIZED_HONEY_BLOCK = BBBlocks.CRYSTALLIZED_HONEY_BLOCK.get().getDefaultState();
	
	public static void buildSurface(Random random, IChunk chunk, Biome biome, int x, int z, int startHeight, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, long seed, SurfaceBuilderConfig config)
	{
		int xpos = x & 15;
		int zpos = z & 15;
		BlockPos.Mutable blockpos$Mutable = new BlockPos.Mutable();
		boolean topMostBlock = false;

		//makes stone below sea level into end stone
		for (int ypos = 255; ypos >= 0; --ypos)
		{
			blockpos$Mutable.setPos(xpos, ypos, zpos);
			BlockState currentBlockState = chunk.getBlockState(blockpos$Mutable);

			if (currentBlockState.getBlock() != null)
			{
				if(currentBlockState.getMaterial() == Material.AIR || currentBlockState.getMaterial() == Material.WATER) {
					topMostBlock = true;
				}
				else {
					if (currentBlockState == STONE)
					{
						chunk.setBlockState(blockpos$Mutable, HONEYCOMB_BLOCK, false);
					}
					else if (currentBlockState == POROUS_HONEYCOMB)
					{
						if(topMostBlock) {
							if (ypos <= seaLevel + 2 + Math.max(noise, 0) + random.nextInt(2))
							{
								chunk.setBlockState(blockpos$Mutable, WAX_BLOCK, false);
							}
							else {
								chunk.setBlockState(blockpos$Mutable, CRYSTALLIZED_HONEY_BLOCK, false);
							}
						}
						else {
							if (ypos <= seaLevel + 2 + Math.max(noise, 0) + random.nextInt(2))
							{
								chunk.setBlockState(blockpos$Mutable, FILLED_POROUS_HONEYCOMB, false);
							}
						}
					}
					else if (currentBlockState.getMaterial() == Material.AIR)
					{
						if (ypos < seaLevel)
						{
							chunk.setBlockState(blockpos$Mutable, defaultFluid, false);
						}
					}
					
					topMostBlock = false;
				}
			}
		}
	}
	
	
	private static final BlockState HIVE_PLANKS = BBBlocks.HIVE_PLANKS.get().getDefaultState();
	
	//use hive planks for roof and floor
	public static void makeBedrock(IChunk chunk, Random random) {
		BlockPos.Mutable blockpos$Mutable = new BlockPos.Mutable();
		int xStart = chunk.getPos().getXStart();
		int zStart = chunk.getPos().getZStart();
		int roofHeight = 253;
		int floorHeight = 2;

		for (BlockPos blockpos : BlockPos.getAllInBoxMutable(xStart, 0, zStart, xStart + 15, 0, zStart + 15)) 
		{
			//fills in gap between top of terrain gen and y = 255 with solid blocks
			for (int ceilingY = roofHeight; ceilingY >= roofHeight - random.nextInt(2); --ceilingY) 
			{
				chunk.setBlockState(blockpos$Mutable.setPos(blockpos.getX(), ceilingY, blockpos.getZ()), HIVE_PLANKS, false);
			}
		
			//single layer of solid blocks
			for (int floorY = floorHeight; floorY<= floorHeight + random.nextInt(2); ++floorY) 
			{
				chunk.setBlockState(blockpos$Mutable.setPos(blockpos.getX(), floorY, blockpos.getZ()), HIVE_PLANKS, false);
			}
		}

	}
}