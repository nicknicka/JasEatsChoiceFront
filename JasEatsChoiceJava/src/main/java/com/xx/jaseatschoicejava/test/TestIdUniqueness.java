package com.xx.jaseatschoicejava.test;

import com.xx.jaseatschoicejava.util.IdGenerator;

import java.util.HashSet;
import java.util.Set;

/**
 * 测试ID生成器的唯一性
 */
public class TestIdUniqueness {

    public static void main(String[] args) {
        int testCount = 1000000; // 测试生成100万条ID
        Set<Long> idSet = new HashSet<>();
        long startTime = System.currentTimeMillis();

        System.out.println("开始生成" + testCount + "条ID...");

        for (int i = 0; i < testCount; i++) {
            Long id = IdGenerator.generateId();

            // 检查是否重复
            if (idSet.contains(id)) {
                System.err.println("发现重复ID: " + id + "，在第" + (i + 1) + "次生成");
                return;
            }

            idSet.add(id);

            // 每生成10万条输出一次进度
            if ((i + 1) % 100000 == 0) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                System.out.println("已生成" + (i + 1) + "条ID，耗时" + elapsedTime + "ms");
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("生成" + testCount + "条ID完成，耗时" + totalTime + "ms");
        System.out.println("所有ID均唯一，无重复！");
    }
}
