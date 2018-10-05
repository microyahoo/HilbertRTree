package com.njupt.test;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {
	public static void main(String[] args) throws Exception {
		final int BUFFER_SIZE = 0x300000; // 缓冲区为3M
		File f = new File("c:/backupfile.sql");
		int len = 0;
		Long start = System.currentTimeMillis();
		for (int z = 8; z > 0; z--) {
			MappedByteBuffer inputBuffer = new RandomAccessFile(f, "r")
					.getChannel().map(FileChannel.MapMode.READ_ONLY,
							f.length() * (z - 1) / 8, f.length() * 1 / 8);

			byte[] dst = new byte[BUFFER_SIZE];// 每次读出3M的内容
			for (int offset = 0; offset < inputBuffer.capacity(); offset += BUFFER_SIZE) {
				if (inputBuffer.capacity() - offset >= BUFFER_SIZE) {
					for (int i = 0; i < BUFFER_SIZE; i++)
						dst[i] = inputBuffer.get(offset + i);
				} else {
					for (int i = 0; i < inputBuffer.capacity() - offset; i++)
						dst[i] = inputBuffer.get(offset + i);
				}
				int length = (inputBuffer.capacity() % BUFFER_SIZE == 0) ? BUFFER_SIZE
						: inputBuffer.capacity() % BUFFER_SIZE;
				String line = new String(dst, 0, length, "UTF-8");
				len += length;
				System.out.println(length + "-" + (z - 1) + "-" + (8 - z + 1));
			}
		}
		System.out.println(len);
		long end = System.currentTimeMillis();
		System.out.println("读取文件文件花费：" + (end - start) + "毫秒");
	}
}
