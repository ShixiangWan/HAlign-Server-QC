package com.malab.utils;

import java.io.File;

public class DeleteUtil {
	public static void main(String[] args) {
		new DeleteUtil().clear(new File("D:\\test\\shixiang.zip"));
	}
	
	/**
     * 递归删除目录下的所有文件及子目录下所有文件,也可以删除单个文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
	public boolean clear(File dir) {
	    if (dir.isFile() && dir.exists()) {  
	    	dir.delete();  
	        return true;  
	    }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = clear(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
}
