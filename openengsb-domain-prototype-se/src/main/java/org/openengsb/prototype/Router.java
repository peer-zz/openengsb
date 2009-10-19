/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.prototype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Router {

    private Log logger = LogFactory.getLog(getClass());
    
    private Location logger1 = new Location("urn:openengsb:evillogging", "BaseLogger");
    private Location logger2 = new Location("urn:openengsb:evillogging", "BaseLogger2");
    
    public Location resolve(String id) {
        
        if (id.equals("1")) {
            return logger1;
        }
        if (id.equals("2")) {
            return logger2;
        }
        
        logger.warn("ROUTING FOR ID: " + id);
        return new Location("hi", "Service" + id);
    }

    public static class Location {
        private final String namespace;
        private final String service;

        public Location(String namespace, String service) {
            this.namespace = namespace;
            this.service = service;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getService() {
            return service;
        }

    }
}
