package com.bazzi.pre.tests.service;

import com.bazzi.core.util.DigestUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileCompareTest {
    public static void main(String[] args) {
        Map<String, String> orgMap = getSignByDir("C:\\Users\\hhxd1\\Desktop\\log_20");
        Map<String, String> downMap = getSignByDir("C:\\Users\\hhxd1\\Downloads\\1702537428842");
        if (orgMap.isEmpty() || downMap.isEmpty() || orgMap.size() != downMap.size())
            System.out.println("result = compare fail");

        for (String k : orgMap.keySet()) {
            if (!downMap.containsKey(k)) {
                System.out.println("result = " + k + " not find");
            }

            if (!orgMap.get(k).equals(downMap.get(k)))
                System.out.println("result = " + k + " not equal : " + orgMap.get(k) + " , " + downMap.get(k));
        }


        System.out.println("result ======= equal");
    }

    public static Map<String, String> getSignByDir(String dir) {
        Map<String, String> resMap = new HashMap<>();
        File file = new File(dir);
        if (!file.exists() || !file.isDirectory())
            return resMap;
        File[] files = file.listFiles();
        if (files == null)
            return resMap;
        for (File f : files) {
            resMap.put(f.getName(), DigestUtil.fileToMd5(f));
        }
        return resMap;
    }
}
