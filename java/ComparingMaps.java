package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ComparingMaps {

    public static void main(String... args) {
        //difference in after
        Map<String,Object> result = compareDiffColumns(input1(),input2());
        print(result,"difference in the after map");
        //difference in equal
        result = compareDiffColumns(input01(),input02());
        print(result,"Equal both map");
        //difference in before
        result = compareDiffColumns(input001(),input002());
        print(result,"difference in the before map");


    }

    public static void print(Map<String,Object> result,String message){
        System.out.println(message);
        result.forEach((k,v)->{
            System.out.println(k +" - "+v);
        });
        System.out.println("----------------------");

    }
    public static Map<String,Object> input1(){
        Map<String, Object> right = new HashMap<>();
        right.put("COL1","1");
        right.put("COL2","2");
        right.put("COL3","3");
        right.put("COL4","3");
        right.put("COL5","5");
        return right;
    }

    public static Map<String,Object> input2(){
        Map<String, Object> left = new HashMap<>();
        left.put("COL1","1");
        left.put("COL2","2");
        left.put("COL3","3");
        left.put("COL4","4");
        left.put("COL5","5");
        return left;
    }

    public static Map<String,Object> input01(){
        Map<String, Object> right = new HashMap<>();
        right.put("COL1","1");
        right.put("COL2","2");
        right.put("COL3","3");
        right.put("COL4","4");
        right.put("COL5","5");
        return right;
    }

    public static Map<String,Object> input02(){
        Map<String, Object> left = new HashMap<>();
        left.put("COL1","1");
        left.put("COL2","2");
        left.put("COL3","3");
        left.put("COL4","4");
        left.put("COL5","5");
        return left;
    }

    public static Map<String,Object> input001(){
        Map<String, Object> right = new HashMap<>();
        right.put("COL1","1");
        right.put("COL2","2");
        right.put("COL3","3");
        right.put("COL4","3");
        right.put("COL5","5");
        return right;
    }

    public static Map<String,Object> input002(){
        Map<String, Object> left = new HashMap<>();
        left.put("COL1","1");
        left.put("COL2","2");
        left.put("COL3","3");
        left.put("COL4","4");
        left.put("COL5","5");
        return left;
    }

    public static Map<String,Object> compareDiffColumns(Map<String,Object> before,Map<String,Object> after){

        Map<String,Object> difference = new HashMap<>();
        if(before.isEmpty() || after.isEmpty()){
            return null;
        }
        if(before.equals(after)){
            return difference;
        }

        before.keySet().forEach((key)->{
            if(!after.containsKey(key)){
                difference.put(key,"B|"+before.get(key)+";A|"+after.get(key));
            }else if(after.containsKey(key)){
                if(!Objects.equals(before.get(key),after.get(key))){
                    difference.put(key,"B|"+before.get(key)+";A|"+after.get(key));
                }
            }
        });
        return difference;
    }
}
