
package com.kinakomochimilk.generatooooor;

/**
 * Egu Generator用のlong型エネルギーインターフェース。
 */
public interface ILongEnergyStorage {

    long receiveLongEnergy(long maxReceive, boolean simulate);

    long extractLongEnergy(long maxExtract, boolean simulate);

    long getLongEnergyStored();

    long getMaxLongEnergyStored();

    boolean canReceiveLongEnergy();

    boolean canExtractLongEnergy();
}
