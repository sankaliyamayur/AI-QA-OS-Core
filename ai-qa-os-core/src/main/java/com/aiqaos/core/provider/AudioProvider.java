package com.aiqaos.core.provider;

public interface AudioProvider {
    byte[] textToSpeech(String text);
    String speechToText(byte[] audioBytes);
}