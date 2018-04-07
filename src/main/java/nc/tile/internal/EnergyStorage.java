package nc.tile.internal;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList({ @Optional.Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = "tesla"), @Optional.Interface(iface = "net.darkhax.tesla.api.ITeslaHolder", modid = "tesla"), @Optional.Interface(iface = "net.darkhax.tesla.api.ITeslaProducer", modid = "tesla") })
public class EnergyStorage implements IEnergyStorage, ITeslaConsumer, ITeslaProducer, ITeslaHolder, INBTSerializable<NBTTagCompound> {

	public long maxReceive, maxExtract;
	public long energyStored, energyCapacity;
	
	public EnergyStorage(int capacity) {
		this(capacity, capacity, capacity);
	}

	public EnergyStorage(int capacity, int maxTransfer) {
		this(capacity, maxTransfer, maxTransfer);
	}

	public EnergyStorage(int capacity, int maxReceive, int maxExtract) {
		energyCapacity = capacity;
		this.maxReceive = maxReceive;
		this.maxExtract = maxExtract;
	}
	
	// Tesla Energy

	@Override
	public long getStoredPower() {
		return getEnergyStored();
	}

	@Override
	public long getCapacity() {
		return getMaxEnergyStored();
	}

	@Override
	public long takePower(long power, boolean simulated) {
		long energyExtracted = Math.min(energyStored, Math.min(maxExtract, Math.min(Integer.MAX_VALUE, power)));
		if (!simulated) energyStored -= energyExtracted;
		return energyExtracted;
	}

	@Override
	public long givePower(long power, boolean simulated) {
		long energyReceived = Math.min(energyCapacity - energyStored, Math.min(maxReceive, power));
		if (!simulated) energyStored += energyReceived;
		return energyReceived;
	}
	
	// Redstone Flux
	
	@Override
	public int getEnergyStored() {
		return (int) Math.min(energyStored, Integer.MAX_VALUE);
	}

	@Override
	public int getMaxEnergyStored() {
		return (int) Math.min(energyCapacity, Integer.MAX_VALUE);
	}

	@Override
	public boolean canExtract() {
		return true;
	}

	@Override
	public boolean canReceive() {
		return true;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulated) {
		long energyReceived = Math.min(energyCapacity - energyStored, Math.min(this.maxReceive, maxReceive));
		if (!simulated) energyStored += energyReceived;
		return (int) energyReceived;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulated) {
		long energyExtracted = Math.min(energyStored, Math.min(this.maxExtract, maxExtract));
		if (!simulated) energyStored -= energyExtracted;
		return (int) energyExtracted;
	}
	
	public void changeEnergyStored(int energy) {
		energyStored += energy;
		if (energyStored > energyCapacity) energyStored = energyCapacity;
		else if (energyStored < 0) energyStored = 0;
	}
	
	public void setEnergyStored(int energy) {
		energyStored = energy;
		if (energyStored > energyCapacity) energyStored = energyCapacity;
		else if (energyStored < 0) energyStored = 0;
	}
	
	public void setStorageCapacity(int newCapacity) {
		if(newCapacity == energyCapacity || newCapacity <= 0) return;
		energyCapacity = newCapacity;
		if(newCapacity < energyStored) setEnergyStored(newCapacity);
    }
	
	public void mergeEnergyStorages(EnergyStorage other) {
		setEnergyStored(getEnergyStored() + other.getEnergyStored());
		setStorageCapacity(getMaxEnergyStored() + other.getMaxEnergyStored());
	}
	
	public void setMaxTransfer(int newMaxTransfer) {
		if(newMaxTransfer < 0) return;
		if(newMaxTransfer != maxReceive) maxReceive = newMaxTransfer;
		if(newMaxTransfer != maxExtract) maxExtract = newMaxTransfer;
    }
	
	public void setMaxReceive(int newMaxReceive) {
		if(newMaxReceive == maxReceive || newMaxReceive < 0) return;
		maxReceive = newMaxReceive;
    }
	
	public void setMaxExtract(int newMaxExtract) {
		if(newMaxExtract == maxExtract || newMaxExtract < 0) return;
		maxReceive = newMaxExtract;
    }
	
	// NBT
	
	@Override
	public NBTTagCompound serializeNBT() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		readAll(nbt);
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if (energyStored < 0) energyStored = 0;
		nbt.setLong("Energy", energyStored);
		return nbt;
	}
	
	public final NBTTagCompound writeAll(NBTTagCompound nbt) {
		NBTTagCompound energyTag = new NBTTagCompound();
		writeToNBT(energyTag);
		nbt.setTag("energyStorage", energyTag);
		return nbt;

	}
	
	public EnergyStorage readFromNBT(NBTTagCompound nbt) {
		energyStored = nbt.getLong("Energy");
		if (energyStored > energyCapacity) energyStored = energyCapacity;
		return this;
	}
	
	public final void readAll(NBTTagCompound nbt) {
		if (nbt.hasKey("energyStorage")) readFromNBT(nbt.getCompoundTag("energyStorage"));
	}
}
