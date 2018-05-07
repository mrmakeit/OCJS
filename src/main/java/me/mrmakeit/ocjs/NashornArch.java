package me.mrmakeit.ocjs;

import li.cil.oc.api.machine.Architecture;
import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@Architecture.Name("Nashorn:ES5")
public class NashornArch implements Architecture {
  private Machine machine;
  private NashornAPI vm;
  
  public NashornArch(Machine machine) {
    this.machine = machine;
  }
  
  @Override
  public boolean isInitialized() {
    if(vm==null){
      return false;
    }
    return vm.initialized;
  }

  @Override
  public boolean initialize() {
    vm = new NashornAPI(machine,false);
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
    
  }
  
}
