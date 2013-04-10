package se.skltp.ei.svc.service.api;

/**
  Header with fields primarily used for traceability in logging.
*/
public class Header {

	private String senderId;
    private String receiverId;
    private String correlationaid;

    public Header(String senderId, String receiverId, String correlationaid) {

    	this.senderId = senderId;
        this.receiverId = receiverId;
        this.correlationaid = correlationaid;
    }
    
    public String getSenderId() {
		return senderId;
	}
	public String getReceiverId() {
		return receiverId;
	}
	public String getCorrelationaid() {
		return correlationaid;
	}

}
