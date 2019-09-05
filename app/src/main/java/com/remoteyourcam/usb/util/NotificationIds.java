/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.remoteyourcam.usb.util;

import java.util.HashMap;
import java.util.Map;

public class NotificationIds {

    private static NotificationIds instance = new NotificationIds();

    public static NotificationIds getInstance() {
        return instance;
    }

    private final Map<String, Integer> map = new HashMap<String, Integer>();
    private int counter;

    public int getUniqueIdentifier(String name) {
        Integer i = map.get(name);
        if (i != null) {
            return i.intValue();
        }
        ++counter;
        map.put(name, counter);
        return counter;
    }
}
