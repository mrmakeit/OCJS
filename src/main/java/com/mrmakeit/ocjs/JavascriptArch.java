package com.mrmakeit.ocjs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import li.cil.oc.api.machine.Architecture;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.Signal;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.Node;
import net.minecraft.nbt.NBTTagCompound;

@Architecture.Name("JavaScript")
public class JavascriptArch implements Architecture {

	private Machine machine;
	private Context vm;
	
	public JavascriptArch(Machine machine) {
		this.machine = machine;
	}
	
	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public void recomputeMemory() {
	}

	@Override
	public boolean initialize() {
		vm = Context.enter();
		Scriptable scope = vm.initStandardObjects();
		//TODO: init signals.
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
			Signal signal = machine.popSignal();
			//TODO: Add some signals
			//Signals are just a function to call.
			if (signal != null){
				System.out.println(signal.name());
				System.out.println(signal.args());
			}
			return new ExecutionResult.Sleep(0);
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
	
}