package se.skltp.ei.svc.service;

class TestLog {
    private int logNr;
    private StringBuilder log;
    private String name;

    TestLog(String name) {
        this.name = name;
        log= new StringBuilder();
        logNr=0;
    }

    void logProblem(String pLog){
        logNr++;
        if(!"".equals(pLog)){
            log.append(name).append(" deviation at test no: ").append(logNr).append(" ").append(pLog);
        }
    }

    String getLogAsStr() {
        return log.toString();
    }
}
