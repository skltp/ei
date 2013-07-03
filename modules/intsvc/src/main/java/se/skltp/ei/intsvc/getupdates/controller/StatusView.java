package se.skltp.ei.intsvc.getupdates.controller;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.skltp.ei.intsvc.getupdates.domain.GetUpdatesStatus;
import se.skltp.ei.intsvc.getupdates.service.GetUpdatesService;

/**
 * Author: Henrik Rostam
 */
@Path("/status")
@Component("statusView")
public class StatusView {

    @Autowired
    private GetUpdatesService getUpdatesService;

    @GET
    @Produces("text/html")
    public String viewStatus() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<GetUpdatesStatus> getUpdatesTypes = getUpdatesService.fetchAll();
        String simpleTable = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n";
            simpleTable += "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n";
            simpleTable += "\t<head>\n";
            simpleTable += "\t\t<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"/>\n";
            simpleTable += "\t\t<title>";
            simpleTable += "Status page for pull-component";
            simpleTable += "</title>\n";
            simpleTable += "\t</head>\n";
            simpleTable += "\t<body>\n";
            simpleTable += "\t\t<table>\n";
            simpleTable += "\t\t\t<thead>\n";
                simpleTable += "\t\t\t\t<tr>\n";
                    simpleTable += "\t\t\t\t\t<th align=\"left\">";
                    simpleTable += "Logical address";
                    simpleTable += "</th>\n";
                    simpleTable += "\t\t\t\t\t<th align=\"left\">";
                    simpleTable += "Service domain";
                    simpleTable += "</th>\n";
                    simpleTable += "\t\t\t\t\t<th align=\"left\">";
                    simpleTable += "Errors since last success";
                    simpleTable += "</th>\n";
                    simpleTable += "\t\t\t\t\t<th align=\"left\">";
                    simpleTable += "Last success time";
                    simpleTable += "</th>\n";
                simpleTable += "\t\t\t\t</tr>\n";
            simpleTable += "\t\t\t</thead>\n";
            simpleTable += "\t\t\t<tbody>\n";
                for (GetUpdatesStatus status : getUpdatesTypes) {
                    simpleTable += "\t\t\t\t<tr>\n";
                        simpleTable += "\t\t\t\t\t<td align=\"left\">";
                            simpleTable += status.getLogicalAddress();
                        simpleTable += "</td>\n";
                        simpleTable += "\t\t\t\t\t<td align=\"left\">";
                            simpleTable += status.getServiceDomain();
                        simpleTable += "</td>\n";
                        simpleTable += "\t\t\t\t\t<td align=\"left\">";
                            simpleTable += status.getAmountOfErrorsSinceLastSuccess();
                        simpleTable += "</td>\n";
                        simpleTable += "\t\t\t\t\t<td align=\"left\">";
                        	if(status.getLastSuccess() != null){
                        		 simpleTable += simpleDateFormat.format(status.getLastSuccess());
                        	}
                        simpleTable += "</td>\n";
                    simpleTable += "\t\t\t\t</tr>\n";
                }
            simpleTable += "\t\t\t</tbody>\n";
        simpleTable += "\t\t</table>\n";
        simpleTable += "\t</body>\n";
        simpleTable += "</html>";
        return simpleTable;
    }

}
