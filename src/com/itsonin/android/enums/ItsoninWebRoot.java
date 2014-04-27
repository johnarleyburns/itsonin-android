package com.itsonin.android.enums;

/**
* Created with IntelliJ IDEA.
* User: johnarleyburns
* Date: 4/27/14
* Time: 6:18 PM
* To change this template use File | Settings | File Templates.
*/
public enum ItsoninWebRoot {
    WELCOME("welcome"),
    INVITE("i"),
    EVENT("e"),
    OTHER("");
    public String pathRoot;
    ItsoninWebRoot(String pathRoot) {
        this.pathRoot = pathRoot;
    }
}
