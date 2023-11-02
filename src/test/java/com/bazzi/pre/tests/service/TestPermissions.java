package com.bazzi.pre.tests.service;

public class TestPermissions {
    public static void main(String[] args) {
        int execute = 1;
        int write = 2;
        int read = 4;
        int[] permissions = {7, 5, 6, 1, 2, 4, 3};

        for (int permission : permissions) {
            int rp = permission & read;
            int wp = permission & write;
            int ep = permission & execute;
            System.out.printf("%d === Read: %b Execute: %b Write: %b\n", permission, rp != 0, ep != 0, wp != 0);
        }

    }
}
