package com.scienjus.smartqq;

public class TestApplication {
    public static void main(String[] args) {
        args = new String[]{};
        String s = "长沙-优势智通-阿帅";
        args = s.split("-");
        System.out.println(args.length);
    }
}