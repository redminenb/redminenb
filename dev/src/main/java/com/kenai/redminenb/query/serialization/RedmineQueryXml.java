/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.redminenb.query.serialization;

import com.kenai.redminenb.query.ParameterValue;
import com.kenai.redminenb.query.RedmineQuery;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="redmineQuery")
public class RedmineQueryXml {

    @XmlAttribute
    private final static int version = 1;
    private Map<String,ParameterValue[]> parameters = new HashMap<>();
    
    public RedmineQueryXml() {
    }
    
    public RedmineQueryXml(RedmineQuery rq) {
        parameters = rq.getParameters();
    }
    
    public void toRedmineQuery(RedmineQuery rq) {
        rq.setParameters(parameters);
        rq.setSaved(true);
    }

    public Map<String, ParameterValue[]> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, ParameterValue[]> parameters) {
        this.parameters = parameters;
    }

}
