package com.telepathicgrunt.the_bumblezone.entities.goals;

import com.telepathicgrunt.the_bumblezone.entities.controllers.HoneySlimeMoveHelperController;
import com.telepathicgrunt.the_bumblezone.entities.mobs.HoneySlimeEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

public class HopGoal extends Goal {
    private final HoneySlimeEntity slime;

    public HopGoal(HoneySlimeEntity slimeIn) {
        this.slime = slimeIn;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean canUse() {
        return !this.slime.isPassenger() && this.slime.getMoveControl() instanceof HoneySlimeMoveHelperController;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        ((HoneySlimeMoveHelperController) this.slime.getMoveControl()).setSpeed(1.0D);
    }
}