package Entities.Packages;

/**
 * Created by efridman on 14/11/15.
 */
public class ICMPResponse extends PacketType {
    public ICMPResponse(){
        this.setIsRequest(false);
    }

	@Override
	public PacketType getResponse() {
		return null;
	}
}
