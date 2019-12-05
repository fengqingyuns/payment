package com.hanyun.platform.bill.service.test;

/**
 * Date: 2018/9/19 9:40
 * author: litao
 */
public class TestFor {
    public static void main(String[] args){
        int num = 0;
        for(int i=0;i<=100;i++){
            if(i%2==0){
                num  = num+i;
            }

        }
        System.out.println(num);
    }
}
