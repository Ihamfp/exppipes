package ihamfp.exppipes.tileentities.pipeconfig;

import java.util.ArrayList;
import java.util.List;

import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ISpeciesRoot;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import ihamfp.exppipes.ExppipesMod;
import nc.capability.radiation.source.IRadiationSource;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;

public class Filters {
	@SuppressWarnings("serial")
	public static List<Filter> filters = new ArrayList<Filter>() {{
		// Default filter: match on item/meta: isItemEqual()
		add(new Filter() {
			@Override
			public String getLongName() {
				return "Default";
			}

			@Override
			public String getShortName() {
				return "D";
			}
			
			@Override
			public boolean doesMatch(ItemStack reference, ItemStack stack) {
				return reference.isItemEqual(stack);
			}
		});
		
		// Fuzzy filter: match on item only
		add(new Filter() {
			@Override
			public String getLongName() {
				return "Fuzzy";
			}

			@Override
			public String getShortName() {
				return "F";
			}

			@Override
			public boolean doesMatch(ItemStack reference, ItemStack stack) {
				return reference.isItemEqualIgnoreDurability(stack);
			}
		});
		
		// Strict filter: match on item/meta/tags
		add(new Filter() {
			@Override
			public String getLongName() {
				return "Strict";
			}

			@Override
			public String getShortName() {
				return "S";
			}

			@Override
			public boolean doesMatch(ItemStack reference, ItemStack stack) {
				return reference.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(reference, stack);
			}
		});
		
		// OreDict filter: match if at least one oreDict entry matches
		add(new Filter() {
			@Override
			public String getLongName() {
				return "OreDict";
			}

			@Override
			public String getShortName() {
				return "O";
			}

			@Override
			public boolean doesMatch(ItemStack reference, ItemStack stack) {
				if (reference == null || reference.isEmpty()) return false;
				if (stack == null || stack.isEmpty()) return false;
				int[] refOreIDs = OreDictionary.getOreIDs(reference);
				int[] stackOreIDs = OreDictionary.getOreIDs(stack);
				for (int i : refOreIDs) {
					for (int j : stackOreIDs) {
						if (i==j) return true;
					}
				}
				return false;
			}
			
			@Override
			public String getMatchingHint(ItemStack reference) {
				if (reference == null || reference.isEmpty()) return "invalid";
				int[] refOreIDs = OreDictionary.getOreIDs(reference);
				String hint = "";
				for (int i=0; i<refOreIDs.length; i++) {
					hint = hint.concat((hint.equals("")?"":", ") + OreDictionary.getOreName(refOreIDs[i]));
				}
				return hint;
			}
			
			@Override
			public boolean willEverMatch(ItemStack reference) {
				if (reference == null || reference.isEmpty()) return false;
				return OreDictionary.getOreIDs(reference).length != 0;
			}
		});
		
		// Strict OreDict filter: match if all oreDict entry match
		add(new Filter() {
			@Override
			public String getLongName() {
				return "Strict OreDict";
			}

			@Override
			public String getShortName() {
				return "OS";
			}

			@Override
			public boolean doesMatch(ItemStack reference, ItemStack stack) {
				if (reference == null || reference.isEmpty()) return false;
				if (stack == null || stack.isEmpty()) return false;
				int[] refOreIDs = OreDictionary.getOreIDs(reference);
				int[] stackOreIDs = OreDictionary.getOreIDs(stack);
				if (refOreIDs.length != stackOreIDs.length) return false;
				for (int i=0;i<refOreIDs.length;i++) {
						if (refOreIDs[i] != stackOreIDs[i]) return false;
				}
				return true;
			}
			
			@Override
			public String getMatchingHint(ItemStack reference) {
				if (reference == null || reference.isEmpty()) return "invalid";
				int[] refOreIDs = OreDictionary.getOreIDs(reference);
				String hint = "";
				for (int i=0; i<refOreIDs.length; i++) {
					hint = hint.concat((hint.equals("")?"":", ") + OreDictionary.getOreName(refOreIDs[i]));
				}
				return hint;
			}
			
			@Override
			public boolean willEverMatch(ItemStack reference) {
				if (reference == null || reference.isEmpty()) return false;
				return OreDictionary.getOreIDs(reference).length != 0;
			}
		});
		
		// Shape filter: match on first oreDict word (=type), i.e. "ore", "block", "ingot"...
		add(new Filter() {
			@Override
			public String getLongName() {
				return "Shape";
			}

			@Override
			public String getShortName() {
				return "SH";
			}

			@Override
			public boolean doesMatch(ItemStack reference, ItemStack stack) {
				if (reference == null || reference.isEmpty()) return false;
				if (stack == null || stack.isEmpty()) return false;
				int[] refOreIDs = OreDictionary.getOreIDs(reference);
				int[] stackOreIDs = OreDictionary.getOreIDs(stack);
				for (int i : refOreIDs) {
					String refOreShape = OreDictionary.getOreName(i).replaceAll("[A-Z][a-z]+", "");
					for (int j : stackOreIDs) {
						String stackOreName = OreDictionary.getOreName(j);
						if (stackOreName.startsWith(refOreShape)) return true;
					}
				}
				return false;
			}
			
			@Override
			public String getMatchingHint(ItemStack reference) {
				if (reference == null || reference.isEmpty()) return "invalid";
				int[] refOreIDs = OreDictionary.getOreIDs(reference);
				String hint = "";
				for (int i=0; i<refOreIDs.length; i++) {
					hint = hint.concat((hint.equals("")?"":", ") + OreDictionary.getOreName(refOreIDs[i]).replaceAll("[A-Z][a-z]+", ""));
				}
				return hint;
			}
			
			@Override
			public boolean willEverMatch(ItemStack reference) {
				if (reference == null || reference.isEmpty()) return false;
				return OreDictionary.getOreIDs(reference).length != 0;
			}
		});
		
		// Fluid filter: match on contained fluid
		add(new Filter() {
			@Override
			public String getLongName() {
				return "Fluid";
			}

			@Override
			public String getShortName() {
				return "FL";
			}

			@Override
			public boolean doesMatch(ItemStack reference, ItemStack stack) {
				if (reference == null || reference.isEmpty()) return false;
				if (stack == null || stack.isEmpty()) return false;
				if (reference.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
					IFluidHandlerItem refFluid = reference.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
					IFluidTankProperties[] refProp = refFluid.getTankProperties();
					boolean isRefEmpty = true;
					for (IFluidTankProperties tank : refProp) {
						if (tank.getContents().amount != 0) isRefEmpty = false;
						for (IFluidTankProperties stackTank : stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).getTankProperties()) {
							if (stackTank.getContents().getFluid().equals(tank.getContents().getFluid())) return true;
						}
					}
					if (isRefEmpty) return true; // Match any fluid-storing item
				}
				return false;
			}
			
			public String getMatchingHint(ItemStack reference) {
				if (reference == null || reference.isEmpty()) return "";
				if (reference.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
					IFluidHandlerItem refFluid = reference.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
					IFluidTankProperties[] refProp = refFluid.getTankProperties();
					boolean isRefEmpty = true;
					String hint = "";
					for (IFluidTankProperties tank : refProp) {
						if (tank.getContents() != null && tank.getContents().amount != 0) {
							isRefEmpty = false;
							hint = hint.concat((hint.equals("")?"":", ") + tank.getContents().getLocalizedName());
						}
					}
					if (isRefEmpty) return "any"; // Match any fluid-storing item
					return hint;
				}
				return "";
			};
			
			@Override
			public boolean willEverMatch(ItemStack reference) {
				if (reference == null || reference.isEmpty()) return false;
				return reference.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
			};
		});
		
		add(new Filter() {
			@Override
			public String getLongName() {
				return "Greater or equal energy %";
			}

			@Override
			public String getShortName() {
				return "E>";
			}

			@Override
			public boolean doesMatch(ItemStack reference, ItemStack stack) {
				if (reference == null || reference.isEmpty()) return false;
				if (stack == null || stack.isEmpty()) return false;
				
				if (reference.hasCapability(CapabilityEnergy.ENERGY, null) && stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
					IEnergyStorage refEnergy = reference.getCapability(CapabilityEnergy.ENERGY, null);
					IEnergyStorage stackEnergy = stack.getCapability(CapabilityEnergy.ENERGY, null);
					return (stackEnergy.getEnergyStored()/(double)stackEnergy.getMaxEnergyStored() >= refEnergy.getEnergyStored()/(double)refEnergy.getMaxEnergyStored());
				} else if (Loader.isModLoaded("gregtech") && reference.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null) && stack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
					IElectricItem refElectric = reference.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
					IElectricItem stackElectric = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
					if (refElectric.getTier() != stackElectric.getTier()) return false;
					return (stackElectric.getCharge()/(double)stackElectric.getMaxCharge() >= refElectric.getCharge()/(double)refElectric.getMaxCharge());
				}
				return false;
			}
			
			public String getMatchingHint(ItemStack reference) {
				if (reference == null || reference.isEmpty()) return "";
				String hint = "";
				if (reference.hasCapability(CapabilityEnergy.ENERGY, null)) {
					IEnergyStorage refEnergy = reference.getCapability(CapabilityEnergy.ENERGY, null);
					hint += (Math.round(1000.0*refEnergy.getEnergyStored()/refEnergy.getMaxEnergyStored())/10) + " %";
				} else if (Loader.isModLoaded("gregtech") && reference.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
					IElectricItem refElectric = reference.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
					hint += (Math.round(1000.0*refElectric.getCharge()/refElectric.getMaxCharge())/10) + " %";
					hint += ", " + GTValues.VN[refElectric.getTier()];
				}
				return hint;
			};
			
			@Override
			public boolean willEverMatch(ItemStack reference) {
				if (reference == null || reference.isEmpty()) return false;
				if (Loader.isModLoaded("gregtech") && reference.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
					return true;
				}
				return reference.hasCapability(CapabilityEnergy.ENERGY, null);
			}
		});
		
		add(new Filter() {
			@Override
			public String getLongName() {
				return "Lower or equal energy %";
			}

			@Override
			public String getShortName() {
				return "E<";
			}

			@Override
			public boolean doesMatch(ItemStack reference, ItemStack stack) {
				if (reference == null || reference.isEmpty()) return false;
				if (stack == null || stack.isEmpty()) return false;
				
				if (reference.hasCapability(CapabilityEnergy.ENERGY, null) && stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
					IEnergyStorage refEnergy = reference.getCapability(CapabilityEnergy.ENERGY, null);
					IEnergyStorage stackEnergy = stack.getCapability(CapabilityEnergy.ENERGY, null);
					return (stackEnergy.getEnergyStored()/(double)stackEnergy.getMaxEnergyStored() <= refEnergy.getEnergyStored()/(double)refEnergy.getMaxEnergyStored());
				} else if (Loader.isModLoaded("gregtech") && reference.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null) && stack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
					IElectricItem refElectric = reference.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
					IElectricItem stackElectric = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
					if (refElectric.getTier() != stackElectric.getTier()) return false;
					return (stackElectric.getCharge()/(double)stackElectric.getMaxCharge() <= refElectric.getCharge()/(double)refElectric.getMaxCharge());
				}
				return false;
			}
			
			public String getMatchingHint(ItemStack reference) {
				if (reference == null || reference.isEmpty()) return "";
				String hint = "";
				if (reference.hasCapability(CapabilityEnergy.ENERGY, null)) {
					IEnergyStorage refEnergy = reference.getCapability(CapabilityEnergy.ENERGY, null);
					hint += (Math.round(1000.0*refEnergy.getEnergyStored()/refEnergy.getMaxEnergyStored())/10) + " %";
				} else if (Loader.isModLoaded("gregtech") && reference.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
					IElectricItem refElectric = reference.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
					hint += (Math.round(1000.0*refElectric.getCharge()/refElectric.getMaxCharge())/10) + " %";
					hint += ", " + GTValues.VN[refElectric.getTier()];
				}
				return hint;
			};
			
			@Override
			public boolean willEverMatch(ItemStack reference) {
				if (reference == null || reference.isEmpty()) return false;
				if (Loader.isModLoaded("gregtech") && reference.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
					return true;
				}
				return reference.hasCapability(CapabilityEnergy.ENERGY, null);
			}
		});
		
		//////////////// Thaumcraft
		
		if (Loader.isModLoaded("thaumcraft")) {
			// Aspect filter: match on common aspect
			add(new Filter() {
				@Override
				public String getLongName() {
					return "Aspect";
				}

				@Override
				public String getShortName() {
					return "A";
				}

				@Override
				public boolean doesMatch(ItemStack reference, ItemStack stack) {
					if (reference == null || reference.isEmpty()) return false;
					if (stack == null || stack.isEmpty()) return false;
					AspectList refAspectlist = AspectHelper.getObjectAspects(reference);
					AspectList stackAspectlist = AspectHelper.getObjectAspects(stack);
					for (Aspect refAspect : refAspectlist.getAspects()) {
						for (Aspect aspect : stackAspectlist.getAspects()) {
							if (aspect.equals(refAspect)) return true;
						}
					}
					return false;
				}
				
				public String getMatchingHint(ItemStack reference) {
					if (reference == null || reference.isEmpty()) return "";
					AspectList refAspectlist = AspectHelper.getObjectAspects(reference);
					String hint = "";
					for (Aspect refAspect : refAspectlist.getAspects()) {
						hint = hint.concat((hint.equals("")?"":", ") + refAspect.getName());
					}
					return hint;
				};
				
				@Override
				public boolean willEverMatch(ItemStack reference) {
					if (reference == null || reference.isEmpty()) return false;
					return AspectHelper.getObjectAspects(reference).size() > 0;
				}
			});
		}
		
		//////////////// Nuclearcraft
		
		if (Loader.isModLoaded("nuclearcraft")) {
			// Radiation > filter
			add(new Filter() {
				@Override
				public String getLongName() {
					return "More radioactive";
				}

				@Override
				public String getShortName() {
					return "R>";
				}

				@Override
				public boolean doesMatch(ItemStack reference, ItemStack stack) {
					if (reference == null || reference.isEmpty()) return false;
					if (stack == null || stack.isEmpty()) return false;
					
					IRadiationSource refRad = null;
					IRadiationSource stackRad = null;
					
					if (reference.hasCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null)) {
						refRad = reference.getCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null);
					}
					if (stack.hasCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null)) {
						stackRad = stack.getCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null);
					}

					if (refRad == null && stackRad == null) return false;
					else if (refRad == null && stackRad != null) return true;
					else if (refRad != null && stackRad == null) return false;
					else if (stackRad.getRadiationLevel() > refRad.getRadiationLevel()) return true;
					
					return false;
				}
			});
			
			// Radiation < filter
			add(new Filter() {
				@Override
				public String getLongName() {
					return "Less radioactive";
				}

				@Override
				public String getShortName() {
					return "R<";
				}

				@Override
				public boolean doesMatch(ItemStack reference, ItemStack stack) {
					if (reference == null || reference.isEmpty()) return false;
					if (stack == null || stack.isEmpty()) return false;
					
					IRadiationSource refRad = null;
					IRadiationSource stackRad = null;
					
					if (reference.hasCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null)) {
						refRad = reference.getCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null);
					}
					if (stack.hasCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null)) {
						stackRad = stack.getCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null);
					}
					
					if (refRad == null && stackRad == null) return false;
					else if (refRad == null && stackRad != null) return true;
					else if (refRad != null && stackRad == null) return false;
					else if (stackRad.getRadiationLevel() < refRad.getRadiationLevel()) return true;
					
					return false;
				}
				
				@Override
				public boolean willEverMatch(ItemStack reference) {
					if (reference == null || reference.isEmpty()) return false;
					return reference.hasCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null);
				}
			});
		}
		
		//////////////// Forestry
		
		if (Loader.isModLoaded("forestry")) {
			// Unanalyzed bees filter
			add(new Filter() {
				@Override
				public String getLongName() {
					return "Unanalyzed specimens";
				}

				@Override
				public String getShortName() {
					return "US";
				}

				@Override
				public boolean doesMatch(ItemStack reference, ItemStack stack) {
					if (reference == null || reference.isEmpty()) return false;
					if (stack == null || stack.isEmpty()) return false;
					
					if (AlleleManager.alleleRegistry.isIndividual(stack)) {
						IIndividual individual = AlleleManager.alleleRegistry.getIndividual(stack);
						if (!individual.getGenome().getSpeciesRoot().isMember(reference)) return false;
						ISpeciesRoot speciesRoot = individual.getGenome().getSpeciesRoot();
						return speciesRoot.getType(stack).equals(speciesRoot.getType(reference)) && !individual.isAnalyzed();
					}
					return false;
				}
				
				@Override
				public boolean willEverMatch(ItemStack reference) {
					if (reference == null || reference.isEmpty()) return false;
					return AlleleManager.alleleRegistry.isIndividual(reference);
				}
			});
			
			// Genetic Equal filter
			add(new Filter() {
				@Override
				public String getLongName() {
					return "Genetic Equal";
				}

				@Override
				public String getShortName() {
					return "GE";
				}

				@Override
				public boolean doesMatch(ItemStack reference, ItemStack stack) {
					if (reference == null || reference.isEmpty()) return false;
					if (stack == null || stack.isEmpty()) return false;
					
					if (AlleleManager.alleleRegistry.isIndividual(stack) && AlleleManager.alleleRegistry.isIndividual(reference)) {
						IIndividual ref = AlleleManager.alleleRegistry.getIndividual(reference);
						IIndividual individual = AlleleManager.alleleRegistry.getIndividual(stack);
						if (!ref.isAnalyzed() ||!individual.isAnalyzed()) return false;
						return individual.getGenome().isGeneticEqual(ref.getGenome());
					}
					return false;
				}
				
				@Override
				public boolean willEverMatch(ItemStack reference) {
					if (reference == null || reference.isEmpty()) return false;
					return AlleleManager.alleleRegistry.isIndividual(reference) && AlleleManager.alleleRegistry.getIndividual(reference).isAnalyzed();
				}
			});
		}
	}};
	
	public static Filter fromShortString(String shortName) {
		for (Filter f : filters) {
			if (f.getShortName().equals(shortName)) return f;
		}
		ExppipesMod.logger.error("Couldn't find filter " + shortName);
		return null;
	}
	
	public static int idFromShortString(String shortName) {
		return filters.indexOf(fromShortString(shortName));
	}
}
