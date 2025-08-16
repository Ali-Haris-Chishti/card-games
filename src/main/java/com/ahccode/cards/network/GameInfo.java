package com.ahccode.cards.network;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameInfo {

    private String name;
    private String host;
    private int port;

}
