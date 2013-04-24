package se.skltp.ei.svc.service.api;

/**
  Header with fields primarily used for traceability in logging.
 */
public class Header {

    private String senderId;
    private String receiverId;
    private String correlationId;

    public Header(String senderId, String receiverId, String correlationId) {

        this.senderId = senderId;
        this.receiverId = receiverId;
        this.correlationId = correlationId;
    }

    public String getSenderId() {
        return senderId;
    }
    public String getReceiverId() {
        return receiverId;
    }
    public String getCorrelationaid() {
        return correlationId;
    }

}
