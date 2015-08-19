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
		vm.addOut();
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
		
	}
	@Override
	public ExecutionResult runThreaded(boolean isSynchronizedReturn) {
		if (isSynchronizedReturn){
			return new ExecutionResult.Sleep(0);
		}else{
			if(vm.limited){
				vm.rerun();
				return new ExecutionResult.Sleep(0);
			}else{
				Signal signal = machine.popSignal();
				if (signal != null){
					ExecutionResult result = vm.run(signal);
					machine.update();
					return result;
				}else{
					return new ExecutionResult.Sleep(0);
				}
			}
		}
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
		return false;
	}
	
}