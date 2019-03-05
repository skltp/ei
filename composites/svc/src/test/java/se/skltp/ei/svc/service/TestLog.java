package se.skltp.ei.svc.service;

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
            log.append(name + " deviation at test no: "+logNr+ " "+pLog);
        }
    }

    public String getLogAsStr() {
        return log.toString();
    }
}
