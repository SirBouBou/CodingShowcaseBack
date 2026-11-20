package org.project.common.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public class WebSocketPaths {
    public static final String ROOMS = "/topic/rooms";

    public static final String ROOM = "/topic/rooms/";

    public static String room(String roomId) {
        return ROOM + roomId;
    }
}
