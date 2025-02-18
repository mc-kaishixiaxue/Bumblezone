package com.telepathicgrunt.the_bumblezone.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.the_bumblezone.blocks.HoneyCrystal;
import com.telepathicgrunt.the_bumblezone.modinit.BzBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;

public class HoneyCrystalFeature extends Feature<NoFeatureConfig> {

    public HoneyCrystalFeature(Codec<NoFeatureConfig> configFactory) {
        super(configFactory);
    }

    private static final Block CAVE_AIR = Blocks.CAVE_AIR;
    private static final Block AIR = Blocks.AIR;

    /**
     * Place crystal block attached to a block if it is buried underground or underwater
     */
    @Override
    public boolean place(ISeedReader world, ChunkGenerator generator, Random random, BlockPos position, NoFeatureConfig config) {

        BlockPos.Mutable blockpos$Mutable = new BlockPos.Mutable().set(position);
        BlockState originalBlockstate = world.getBlockState(blockpos$Mutable);
        BlockState blockstate;
        ChunkPos currentChunkPos = new ChunkPos(blockpos$Mutable);

        if (originalBlockstate.getBlock() == CAVE_AIR || originalBlockstate.getFluidState().is(FluidTags.WATER)) {

            for (Direction face : Direction.values()) {
                blockpos$Mutable.set(position);
                blockstate = world.getBlockState(blockpos$Mutable.move(face, 7));

                if (blockstate.getBlock() == AIR) {
                    return false; // too close to the outside. Refuse generation
                }
            }


            BlockState honeyCrystal = BzBlocks.HONEY_CRYSTAL.get().defaultBlockState()
                    .setValue(HoneyCrystal.WATERLOGGED, originalBlockstate.getFluidState().is(FluidTags.WATER));

            //loop through all 6 directions
            blockpos$Mutable.set(position);
            for (Direction facing : Direction.values()) {

                honeyCrystal = honeyCrystal.setValue(HoneyCrystal.FACING, facing);

                // if the block is solid, place crystal on it
                if (honeyCrystal.canSurvive(world, blockpos$Mutable)) {

                    //if the spot is invalid, we get air back
                    BlockState result = HoneyCrystal.updateFromNeighbourShapes(honeyCrystal, world, blockpos$Mutable);
                    if (result.getBlock() != AIR) {

                        //avoid placing crystal on block in other chunk as the cave hasn't carved it yet.
                        Direction directionProp = result.getValue(HoneyCrystal.FACING);
                        blockpos$Mutable.move(directionProp.getOpposite());
                        if (blockpos$Mutable.getX() >> 4 != currentChunkPos.x || blockpos$Mutable.getZ() >> 4 != currentChunkPos.z) {
                            return false; // facing side chunk. cancel spawn
                        }
                        blockpos$Mutable.move(directionProp); // move back
                        world.setBlock(blockpos$Mutable, result, 3);
                        return true; //crystal was placed
                    }
                }
            }
        }

        return false;
    }

}