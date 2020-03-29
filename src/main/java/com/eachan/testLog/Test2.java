package com.eachan.testLog;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Test2 {
    public static void main(String[] args) {
        for (int i = 0; i < 14; i++) {
        log.error(i+":This is a error log by log4j");
        }
    }
}
