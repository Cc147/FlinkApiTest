package com.eachan.testLog;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
@Slf4j
public class Test {
    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 10; i++) {
            log.info(i+":this is a log by log4j");
        }
    }
}
