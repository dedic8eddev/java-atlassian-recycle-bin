package de.t2consult.atlassian.jira.recyclebin.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringUtils {
    public static String EmptyString = "";
    public static String[] EmptyStringArray = new String[0];
    public static boolean IsNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static ArrayList<String> removeDuplicates(String[] arr){
        Set<String> h = new HashSet<>(Arrays.asList(arr));
        return new ArrayList<>(h);
    }
}
