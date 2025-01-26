//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.therealm18studios.lcjsg;

import dev.tauri.jsg.blockentity.stargate.StargateAbstractBaseBE;
import dev.tauri.jsg.blockentity.stargate.StargateClassicBaseBE;
import dev.tauri.jsg.power.general.EnergyRequiredToOperate;
import dev.tauri.jsg.stargate.EnumDialingType;
import dev.tauri.jsg.stargate.EnumIrisMode;
import dev.tauri.jsg.stargate.EnumIrisType;
import dev.tauri.jsg.stargate.NearbyGate;
import dev.tauri.jsg.stargate.StargateClosedReasonEnum;
import dev.tauri.jsg.stargate.StargateOpenResult;
import dev.tauri.jsg.stargate.StargateTypeEnum;
import dev.tauri.jsg.stargate.codesender.ComputerCodeSender;
import dev.tauri.jsg.stargate.network.StargateAddressDynamic;
import dev.tauri.jsg.stargate.network.StargateNetwork;
import dev.tauri.jsg.stargate.network.StargatePos;
import dev.tauri.jsg.stargate.network.SymbolInterface;
import dev.tauri.jsg.stargate.network.SymbolPegasusEnum;
import dev.tauri.jsg.stargate.network.SymbolTypeRegistry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import li.cil.oc2.api.bus.device.object.Callback;
import li.cil.oc2.api.bus.device.object.DocumentedDevice;
import li.cil.oc2.common.bus.device.rpc.item.AbstractItemRPCDevice;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class StargateOperationsModuleDevice extends AbstractItemRPCDevice implements DocumentedDevice {

    private final BlockEntity deviceTile;
    StargateAbstractBaseBE stargate = null;

    public StargateOperationsModuleDevice(final ItemStack identity, final BlockEntity deviceTile) {
        super(identity, "stargate");
        this.deviceTile = deviceTile;
    }

    @Callback(name = "getOpenedTime")
    public Object[] getOpenedTime() {
        if (((StargateClassicBaseBE)this.deviceTile).getStargateState().engaged()) {
            float openedSeconds = (float)((StargateClassicBaseBE)this.deviceTile).getOpenedSeconds();
            int minutes = (int)Math.floor((double)(openedSeconds / 60.0F));
            int seconds = (int)(openedSeconds - (float)(60 * minutes));
            String secondsString = seconds < 10 ? "0" + seconds : "" + seconds;
            return openedSeconds > 0.0F ? new Object[]{true, "stargate_time", "" + minutes, secondsString} : new Object[]{false, "stargate_not_connected"};
        } else {
            return new Object[]{false, "stargate_not_connected"};
        }
    }

    @Callback(name = "toggleIris")
    public Object[] toggleIris() {
        if (((StargateClassicBaseBE)this.deviceTile).getIrisType() == EnumIrisType.NULL) {
            return new Object[]{false, "stargate_iris_missing", "Iris is not installed!"};
        } else if (((StargateClassicBaseBE)this.deviceTile).getIrisMode() != EnumIrisMode.OC) {
            return new Object[]{false, "stargate_iris_error_mode", "Iris mode must be set to OC"};
        } else {
            boolean result = ((StargateClassicBaseBE)this.deviceTile).toggleIris();
            ((StargateClassicBaseBE)this.deviceTile).toString();
            if (!result && ((StargateClassicBaseBE)this.deviceTile).hasShieldIris() && ((StargateClassicBaseBE)this.deviceTile).isIrisOpened() && ((StargateClassicBaseBE)this.deviceTile).getEnergyStorage().getEnergyStored() < ((StargateClassicBaseBE)this.deviceTile).shieldKeepAlive * 3) {
                return new Object[]{false, "stargate_iris_not_power", "Not enough power to close shield"};
            } else {
                return !result ? new Object[]{false, "stargate_iris_busy", "Iris is busy"} : new Object[]{true};
            }
        }
    }

    @Callback(name = "getIrisState")
    public Object[] getIrisState() {
        return new Object[]{((StargateClassicBaseBE)this.deviceTile).getIrisState().toString()};
    }

    @Callback(name = "getIrisType")
    public Object[] getIrisType() {
        return new Object[]{((StargateClassicBaseBE)this.deviceTile).getIrisType().toString()};
    }

    @Callback(name = "getIrisDurability")
    public Object[] getIrisDurability() {
        ((StargateClassicBaseBE)this.deviceTile).updateIrisDurability();
        return new Object[]{((StargateClassicBaseBE)this.deviceTile).irisDurability + "/" + ((StargateClassicBaseBE)this.deviceTile).irisMaxDurability, ((StargateClassicBaseBE)this.deviceTile).irisDurability, ((StargateClassicBaseBE)this.deviceTile).irisMaxDurability};
    }

    @Callback(name = "sendMessageToIncoming")
    public Object[] sendMessageToIncoming(String msg) {
        if (!((StargateClassicBaseBE)this.deviceTile).isMerged()) {
            return new Object[]{null, "stargate_failure_not_merged", "Stargate is not merged"};
        } else if (!((StargateClassicBaseBE)this.deviceTile).getStargateState().engaged()) {
            return new Object[]{null, "stargate_failure_not_engaged", "Stargate is not engaged"};
        } else if (msg == null) {
            return new Object[]{false, "wrong_argument_type"};
        } else if (((StargateClassicBaseBE)this.deviceTile).codeSender != null && ((StargateClassicBaseBE)this.deviceTile).codeSender.canReceiveMessage()) {
            ((StargateClassicBaseBE)this.deviceTile).codeSender.sendMessage(Component.translatable(msg));
            return new Object[]{true, "success"};
        } else {
            return new Object[]{false, "no_listener_available"};
        }
    }

    @Callback(name = "sendIrisCode")
    public Object[] sendIrisCode(String code) {
        if (code == null) {
            return new Object[]{false, "invalid_method_format", "You must enter code!"};
        } else {
            StargatePos destinationPos = StargateNetwork.INSTANCE.getStargate(((StargateClassicBaseBE)this.deviceTile).getDialedAddress());
            if (destinationPos == null) {
                return new Object[]{false, "stargate_not_engaged"};
            } else {
                StargateAbstractBaseBE te = destinationPos.getBlockEntity();
                if (te instanceof StargateClassicBaseBE) {
                    StargateClassicBaseBE classicTile = (StargateClassicBaseBE)te;
                    classicTile.receiveIrisCode(new ComputerCodeSender(StargateNetwork.INSTANCE.getStargate(((StargateClassicBaseBE)this.deviceTile).getStargateAddress(SymbolTypeRegistry.MILKYWAY))), code);
                    return new Object[]{true, "success"};
                } else {
                    return new Object[]{false, "invalid_target_gate"};
                }
            }
        }
    }

    @Callback(name = "abortDialing")
    public Object[] abortDialing() {
        if (!((StargateClassicBaseBE)this.deviceTile).isMerged()) {
            return new Object[]{null, "stargate_failure_not_merged", "Stargate is not merged"};
        } else if (!((StargateClassicBaseBE)this.deviceTile).getStargateState().dialingComputer() && !((StargateClassicBaseBE)this.deviceTile).getStargateState().idle()) {
            return new Object[]{null, "stargate_aborting_failed", "Aborting dialing failed"};
        } else {
            ((StargateClassicBaseBE)this.deviceTile).abortDialingSequence();
            ((StargateClassicBaseBE)this.deviceTile).toString();
            return new Object[]{null, "stargate_aborting", "Aborting dialing"};
        }
    }

    @Callback(name = "engageGate")
    public Object[] engageGate() {
        if (!((StargateClassicBaseBE)this.deviceTile).isMerged()) {
            return new Object[]{null, "stargate_failure_not_merged", "Stargate is not merged"};
        } else if (((StargateClassicBaseBE)this.deviceTile).getStargateState().idle()) {
            StargateOpenResult gateState = ((StargateClassicBaseBE)this.deviceTile).attemptOpenAndFail();
            return gateState.ok() ? new Object[]{"stargate_engage"} : new Object[]{null, "stargate_failure_opening", "Stargate failed to open", gateState.toString()};
        } else {
            return new Object[]{null, "stargate_failure_busy", "Stargate is busy", ((StargateClassicBaseBE)this.deviceTile).getStargateState().toString()};
        }
    }

    @Callback(name = "disengageGate")
    public Object[] disengageGate() {
        if (!((StargateClassicBaseBE)this.deviceTile).isMerged()) {
            return new Object[]{null, "stargate_failure_not_merged", "Stargate is not merged"};
        } else if (((StargateClassicBaseBE)this.deviceTile).getStargateState().engaged()) {
            if (((StargateClassicBaseBE)this.deviceTile).getStargateState().initiating()) {
                ((StargateClassicBaseBE)this.deviceTile).attemptClose(StargateClosedReasonEnum.REQUESTED);
                return new Object[]{"stargate_disengage"};
            } else {
                return new Object[]{null, "stargate_failure_wrong_end", "Unable to close the gate on this end"};
            }
        } else {
            return new Object[]{null, "stargate_failure_not_open", "The gate is closed"};
        }
    }

    @Callback(name = "engageSymbol")
    public Object[] engageSymbol(Object symbol) {
        if (!((StargateClassicBaseBE)this.deviceTile).isMerged()) {
            return new Object[]{null, "stargate_failure_not_merged", "Stargate is not merged"};
        } else if (!((StargateClassicBaseBE)this.deviceTile).getStargateState().idle()) {
            return new Object[]{null, "stargate_failure_busy", "Stargate is busy, state: " + ((StargateClassicBaseBE)this.deviceTile).getStargateState()};
        } else if (((StargateClassicBaseBE)this.deviceTile).getDialedAddress().size() == 9) {
            return new Object[]{null, "stargate_failure_full", "Already dialed 9 chevrons"};
        } else {
            SymbolInterface targetSymbol = ((StargateClassicBaseBE)this.deviceTile).getSymbolFromNameIndex(symbol);
            if (targetSymbol != SymbolPegasusEnum.UNKNOW1 && targetSymbol != SymbolPegasusEnum.UNKNOW2) {
                ((StargateClassicBaseBE)this.deviceTile).addSymbolToAddressManual(targetSymbol, "oc_used");
                ((StargateClassicBaseBE)this.deviceTile).toString();
                return new Object[]{"stargate_spin"};
            } else {
                throw new IllegalArgumentException("bad argument (symbol name/index invalid)");
            }
        }
    }

    @Callback(name = "dialAddress")
    public final Object[] dialAddress(Object... symbols) {
        if (!((StargateClassicBaseBE)this.deviceTile).isMerged()) {
            return new Object[]{null, "stargate_failure_not_merged", "Stargate is not merged"};
        } else if (!((StargateClassicBaseBE)this.deviceTile).getStargateState().idle()) {
            return new Object[]{null, "stargate_failure_busy", "Stargate is busy, state: " + ((StargateClassicBaseBE)this.deviceTile).getStargateState()};
        } else if (((StargateClassicBaseBE)this.deviceTile).getDialedAddress().size() > 0) {
            return new Object[]{null, "stargate_failure_not_empty", "Dialed address is not empty"};
        } else if (symbols.length < 7) {
            return new Object[]{null, "input_address_malformed", "Input address is malformed"};
        } else {
            int maxSymbols = Math.min(symbols.length, 9);
            StargateAddressDynamic address = new StargateAddressDynamic(((StargateClassicBaseBE)this.deviceTile).getSymbolType());

            for(int i = 0; i < maxSymbols; ++i) {
                SymbolInterface symbol = ((StargateClassicBaseBE)this.deviceTile).getSymbolFromNameIndex(symbols[i]);
                if (symbol == SymbolPegasusEnum.UNKNOW1 || symbol == SymbolPegasusEnum.UNKNOW2) {
                    throw new IllegalArgumentException("bad argument (symbol name/index invalid)");
                }

                address.addSymbol(symbol);
            }

            ((StargateClassicBaseBE)this.deviceTile).dialAddress(address, maxSymbols, false, EnumDialingType.NORMAL);
            return new Object[]{"dial_begun"};
        }
    }

    @Callback(name = "spinGate")
    public Object[] spinGate(int time, boolean changeState) {
        if (!((StargateClassicBaseBE)this.deviceTile).isMerged()) {
            return new Object[]{null, "stargate_failure_not_merged", "Stargate is not merged"};
        } else if (((StargateClassicBaseBE)this.deviceTile).getSymbolType() == SymbolTypeRegistry.PEGASUS) {
            return new Object[]{null, "stargate_not_supported", "Stargate type is not supported"};
        } else if (((StargateClassicBaseBE)this.deviceTile).getStargateState().idle()) {
            if (time != 0) {
                ((StargateClassicBaseBE)this.deviceTile).spinRing(1, changeState, true, time);
                return new Object[]{null, "stargate_spin"};
            } else {
                return new Object[]{null, "stargate_failure_wrong_usage", "Time is 0"};
            }
        } else {
            return new Object[]{null, "stargate_failure_not_idle", "The gate is not idle"};
        }
    }

    @Callback(name = "getCapacitorsInstalled")
    public Integer getCapacitorsInstalled() {
        return ((StargateClassicBaseBE)this.deviceTile).isMerged() ? ((StargateClassicBaseBE)this.deviceTile).currentPowerTier - 1 : null;
    }

    @Callback(name = "getGateType")
    public String getGateType() {
        return ((StargateClassicBaseBE)this.deviceTile).isMerged() ? ((StargateClassicBaseBE)this.deviceTile).getStargateType().toString() : null;
    }

    @Callback(name = "getGateStatus")
    public Object[] getGateStatus() {
        if (!((StargateClassicBaseBE)this.deviceTile).isMerged()) {
            return new Object[]{"not_merged"};
        } else {
            return ((StargateClassicBaseBE)this.deviceTile).getStargateState().engaged() ? new Object[]{"open", ((StargateClassicBaseBE)this.deviceTile).getStargateState().initiating()} : new Object[]{((StargateClassicBaseBE)this.deviceTile).getStargateState().toString().toLowerCase()};
        }
    }

    @Callback(name = "getSymbolsNeeded")
    public Object[] getSymbolsNeeded(String... symbols) {
        if (!((StargateClassicBaseBE)this.deviceTile).isMerged()) {
            return new Object[]{"not_merged"};
        } else {
            StargateAddressDynamic stargateAddress = new StargateAddressDynamic(((StargateClassicBaseBE)this.deviceTile).getSymbolType());

            for(Object symbolObj : Arrays.stream(symbols).toList()) {
                if (stargateAddress.size() == 9) {
                    throw new IllegalArgumentException("Too much glyphs");
                }

                SymbolInterface symbol = ((StargateClassicBaseBE)this.deviceTile).getSymbolFromNameIndex(symbolObj);
                if (stargateAddress.contains(symbol)) {
                    throw new IllegalArgumentException("Duplicate glyph");
                }

                stargateAddress.addSymbol(symbol);
            }

            if (!stargateAddress.getLast().origin() && stargateAddress.size() < 9) {
                stargateAddress.addOrigin();
            }

            if (!stargateAddress.validate()) {
                return new Object[]{"address_malformed"};
            } else if (!((StargateClassicBaseBE)this.deviceTile).canDialAddress(stargateAddress)) {
                return new Object[]{"address_malformed"};
            } else {
                StargatePos pos = StargateNetwork.INSTANCE.getStargate(stargateAddress);
                if (pos == null) {
                    return new Object[]{"gate_not_found"};
                } else {
                    int symbolsCount = ((StargateClassicBaseBE)this.deviceTile).getMinimalSymbolsToDial(pos.getGateSymbolType(), pos);
                    return new Object[]{true, symbolsCount};
                }
            }
        }
    }

    @Callback(name = "getEnergyRequiredToDial")
    public Object[] getEnergyRequiredToDial(String... symbols) {
        if (!((StargateClassicBaseBE)this.deviceTile).isMerged()) {
            return new Object[]{"not_merged"};
        } else {
            StargateAddressDynamic stargateAddress = new StargateAddressDynamic(((StargateClassicBaseBE)this.deviceTile).getSymbolType());

            for(Object symbolObj : Arrays.stream(symbols).toList()) {
                if (stargateAddress.size() == 9) {
                    throw new IllegalArgumentException("Too much glyphs");
                }

                SymbolInterface symbol = ((StargateClassicBaseBE)this.deviceTile).getSymbolFromNameIndex(symbolObj);
                if (stargateAddress.contains(symbol)) {
                    throw new IllegalArgumentException("Duplicate glyph");
                }

                stargateAddress.addSymbol(symbol);
            }

            if (!stargateAddress.getLast().origin() && stargateAddress.size() < 9) {
                stargateAddress.addOrigin();
            }

            if (!stargateAddress.validate()) {
                return new Object[]{"address_malformed"};
            } else if (!((StargateClassicBaseBE)this.deviceTile).canDialAddress(stargateAddress)) {
                return new Object[]{"address_malformed"};
            } else {
                EnergyRequiredToOperate energyRequired = ((StargateClassicBaseBE)this.deviceTile).getEnergyRequiredToDial((StargatePos)Objects.requireNonNull(StargateNetwork.INSTANCE.getStargate(stargateAddress)));
                Map<String, Object> energyMap = new HashMap(2);
                energyMap.put("open", energyRequired.energyToOpen);
                energyMap.put("keepAlive", energyRequired.keepAlive);
                energyMap.put("canOpen", ((StargateClassicBaseBE)this.deviceTile).getEnergyStorage().getEnergyStored() >= energyRequired.energyToOpen);
                return new Object[]{energyMap};
            }
        }
    }

    //TODO
    @Callback(name = "getNearbyGates")
    public Object[] getNearbyGates(String gateType, boolean checkGateType, boolean checkAddressAndEnergy) {
        if (!((StargateClassicBaseBE)this.deviceTile).isMerged()) {
            return new Object[]{null, false, "gate_not_merged", new HashMap()};
        } else {
            Map<String, Map<List<String>, Integer>> map = new HashMap();
            StargateTypeEnum symbolType = StargateTypeEnum.valueOf(gateType);

            for(NearbyGate g : ((StargateClassicBaseBE)this.deviceTile).getNearbyGates(symbolType, checkGateType, checkAddressAndEnergy)) {
                Map<List<String>, Integer> map2 = (Map)map.computeIfAbsent(g.address.getSymbolType().toString(), (k) -> new HashMap());
                map2.put(g.address.getNameList(), g.symbolsNeeded);
                map.put(g.gateType.toString(), map2);
            }

            return new Object[]{null, true, "success", map};
        }
    }

    @Override
    public void getDeviceDocumentation(final DocumentedDevice.DeviceVisitor visitor) {
        visitor.visitCallback("getOpenedTime")
            .description("This needs to be described")
            .returnValueDescription("This needs value Description")
        ;
        visitor.visitCallback("toggleIris")
            .description("function() -- close/open the iris/shield")
            .returnValueDescription("function()")
        ;
        visitor.visitCallback("getIrisState")
            .description("function() -- get info about iris")
            .returnValueDescription("function()")
        ;
        visitor.visitCallback("getIrisType")
            .description("function() -- get info about iris")
            .returnValueDescription("function()")
        ;
        visitor.visitCallback("getIrisDurability")
            .description("function() -- get info about iris")
            .returnValueDescription("function()")
        ;
        visitor.visitCallback("sendMessageToIncoming")
            .description("function(message:string) -- Sends message to last person, who sent code for iris")
            .returnValueDescription("function(message:string)")
        ;
        visitor.visitCallback("sendIrisCode")
            .description("function(code:string) -- send code like GDO (should be only numbers)")
            .returnValueDescription("function(code:string)")
        ;
        visitor.visitCallback("abortDialing")
            .description("function() - aborts dialing")
            .returnValueDescription("function()")
        ;
        visitor.visitCallback("engageGate")
            .description("function() -- Tries to open the gate")
            .returnValueDescription("function()")
        ;
        visitor.visitCallback("disengageGate")
            .description("function() -- Tries to close the gate")
            .returnValueDescription("function()")
        ;
        visitor.visitCallback("engageSymbol")
            .description("function(symbolName:string) -- Spins the ring to the given symbol and engages/locks it")
            .returnValueDescription("function(symbolName:string)")
        ;

        visitor.visitCallback("dialAddress")
            .description("function(address:string...) -- Spins the ring to the given symbol and engages/locks it")
            .returnValueDescription("function(address:string...)")
        ;
        visitor.visitCallback("spinGate")
            .description("function() -- Tries to spin mw gate")
            .returnValueDescription("function()")
        ;
        visitor.visitCallback("getCapacitorsInstalled")
            .description("function() -- Returns capacitors county")
            .returnValueDescription("function()")
        ;
        visitor.visitCallback("getGateType")
            .description("function() -- Returns gate type")
            .returnValueDescription("function()")
        ;
        visitor.visitCallback("getGateStatus")
            .description("function() -- Returns gate status")
            .returnValueDescription("function()")
        ;
        visitor.visitCallback("getSymbolsNeeded")
            .description("function(address:string...) -- Returns symbols needed to dial an address")
            .returnValueDescription("function(address:string...)")
        ;
        visitor.visitCallback("getEnergyRequiredToDial")
            .description("function(address:string...) -- Returns energy needed to dial an address")
            .returnValueDescription("function(address:string...)")
        ;
        visitor.visitCallback("getNearbyGates")
            .description("function(gateType:string, checkGateType:boolean, checkAddressAndEnergy:boolean) -- Returns nearby gates")
            .returnValueDescription("function(gateType:string, checkGateType:boolean, checkAddressAndEnergy:boolean)")
        ;

    }
}
