package se.skltp.ei.svc.service.impl.util;

public class TestLog {
    private int logNr;
    private StringBuilder log;
    private String name;

    public TestLog(String name) {
        this.name = name;
        log= new StringBuilder();
        logNr=0;
    }

    public void logProblem(String pLog){
        logNr++;
        if(!"".equals(pLog)){
            log.append(name).append(" deviation at test no: ").append(logNr).append(" ").append(pLog);
        }
    }

    public String getLogAsStr() {
        return log.toString();
    }
}
