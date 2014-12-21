/*
 * Copyright 2014 Matthias Bläsing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    private int version = 2;
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

    public int getVersion() {
        return version;
    }
}
