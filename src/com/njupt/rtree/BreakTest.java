package com.njupt.rtree;
public class BreakTest {
    public static void main(String args[]) {
        System.out.println("循环没有开始");

        System.out.println("现在开始测试continue");
        
        for (int i = 0; i < 3; i++) {
            System.out.println("开始第" + i + "次for循环");
            if (i == 1) {
                continue;
            }
            System.out.println("看看continue后这里执行了吗？");
        }
        System.out.println("continue测试完毕\n***********************");

        System.out.println("现在开始测试break");
        for (int i = 0; i < 3; i++) {
            System.out.println("开始第" + i + "次for循环");
            if (i == 1){
                break;
            }

            System.out.println("看看break后这里执行了吗？");
        }
        System.out.println("break测试完毕\n***********************");
    }
}