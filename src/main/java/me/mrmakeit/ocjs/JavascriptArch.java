package me.mrmakeit.ocjs;

import li.cil.oc.api.machine.Architecture;
import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.Signal;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@Architecture.Name("JavaScript")
public class JavascriptArch implements Architecture {
	private boolean ready=false;
	private Machine machine;
	private javascriptAPI vm;
	
	public JavascriptArch(Machine machine) {
		this.machine = machine;
	}
	
	@Override
	public boolean isInitialized() {
		return ready;
	}

	@Override
	public boolean initialize() {
		vm = new javascriptAPI(machine);
		vm.addComputer();
		vm.init();
		ready=true;
		return true;
	}

	@Override
	public void close() {
		vm = null;
	}

	@Override
	public void runSynchronized() {
    vm.runSync();		
	}
	@Override
	public ExecutionResult runThreaded(boolean isSynchronizedReturn) {
    return vm.runThreaded(isSynchronizedReturn);
	}

	@Override
	public void onConnect() {
	}

	@Override
	public void load(NBTTagCompound nbt) {
	}

	@Override
	public void save(NBTTagCompound nbt) {
	}

	@Override
	public boolean recomputeMemory(Iterable<ItemStack> arg0) {
		return true;
	}

	@Override
	public void onSignal() {
		// TODO Auto-generated method stub
		
	}
	
}
