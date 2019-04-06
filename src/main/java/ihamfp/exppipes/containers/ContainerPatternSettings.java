package ihamfp.exppipes.containers;

import net.minecraft.inventory.IInventory;

public class ContainerPatternSettings extends ContainerBase {
	public ContainerPatternSettings(IInventory playerInventory) {
		this.addPlayerSlots(playerInventory, 8, 84);
	}
}
