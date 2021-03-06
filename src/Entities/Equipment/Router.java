package Entities.Equipment;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import Entities.Network.IPAddressV4;
import Entities.Osystem.NetworkOs;
import Entities.Packages.*;
import Exceptions.AssociateEquipmentError;

/**
 * Created by efridman on 14/11/15.
 */
public class Router extends NetEquipment {

	private NavigableMap<Integer, IPAddressV4> routingTable = new TreeMap<>();
	private Equipment defaultEquipment; // boca por defecto?
	private NetworkOs operatingSystem;
	

	public Router(IPAddressV4 ip){
		super(ip);
	}

	public Router(IPAddressV4 ip, Equipment defaultEquipment, NetworkOs os){
		super(ip);
		this.defaultEquipment = defaultEquipment;
		this.operatingSystem = os;
	}
	
	public void updateRoutingTable(Integer port, IPAddressV4 newIp) {
		this.routingTable.put(port, newIp);
	}
	
	@Override
	public void sendPacket(Packet packet) {
		if(this.equipments != null && !this.equipments.isEmpty()) {
			for (Equipment equipment : this.equipments) {
				if (equipment.associatedIp.sameNetwork(packet.getDestination())) {
					equipment.receivePacket(packet);
					return;
				}
			}
		}		
	}


	@Override
	public void receivePacket(Packet packet) {
		ServicePacket responsePacket = new ServicePacket(packet.getDestination(),packet.getSource(),new Sendmsg(),packet.getTtl() -1,"");
		
		if(packet instanceof RoutePacket){
			Packet pktReceived = ((RoutePacket) packet).getPacket();
			if (packetReceived(pktReceived)){
				packet.setTtl(pktReceived.getTtl() -1);
				this.sendPacket(pktReceived);
			}
			else{
				RoutePacket routePacket = new RoutePacket(this.getAssociatedIp(), 
						packet.getDestination(), packet.getServiceType(),packet.getTtl(), "", pktReceived);
				defaultEquipment.receivePacket(routePacket);
				responsePacket.setText("Could not send the packet");
				responsePacket.setServiceType(new Sendmsg());
				this.sendPacket(responsePacket);
			}
		}else if(packet.getServiceType() instanceof Who){
			if (packetReceived(packet)){
				String message = operatingSystem.getDataVersion();
				message += " Route table: " + routingTable.toString();
				responsePacket.setText(message);
				responsePacket.setServiceType(new Sendmsg());
				this.sendPacket(responsePacket);
			}
		}		
	}
	
	
	@Override
	public void associateEquipment(Equipment equipment) throws AssociateEquipmentError{
		
		if(this.isConnNumberExceeded())
		{
			throw new AssociateEquipmentError();
		}else{
			
			if(equipment instanceof Hub){
				
						if(!((Hub)equipment).isConnNumberExceeded()){
									this.addEquipment(equipment);
									equipment.addEquipment(this);
									this.addNetToRoutingTable(equipment.getAssociatedIp());
									
						}else{
							throw new AssociateEquipmentError();
						}
			}else{
				this.addEquipment(equipment);
				equipment.addEquipment(this);				
			}
			
		}
		
	}
	
	private void addNetToRoutingTable(IPAddressV4 ip){
		this.routingTable.put(this.getNextKey(), ip);
	}
	
	private Integer getNextKey(){
		 Entry <Integer, IPAddressV4> lastEntry = this.routingTable.lastEntry();
		 return lastEntry == null ? 1 : lastEntry.getKey() + 1;
	}
	
	private boolean packetReceived(Packet packet){
		boolean received = false;
		for (Equipment equipment : equipments) {	
			
			if(equipment.getAssociatedIp().sameNetwork(packet.getDestination())){
					received = true;
					break;
				} 
			
		}
		return received;		
	}
}
