package com.demo.common.util.web;

import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * Created by a on 15-3-18.
 */
public class ShellUtil {

    /**
     * 音频转码
     * @param name
     * @return
     */
    public static String  changeACCFile(String name){
        String cmdString = "ffmpeg -i "+name+" -acodec libfaac -ab 64k -ac 2 -ar 8000 -y changed_"+name+"";
        String[] cmd = {"/bin/sh", "-c", cmdString};
        Process process;
        try {
            process = Runtime.getRuntime().exec(cmd);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            process.waitFor();
            while ((line = input.readLine()) != null){
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     *
     * @param fileName 文件名
     * @param extName 扩展名
     * @param quality 压缩后的质量度 0-100  数值越低  压缩的越狠。
     * @return
     */
    public static String picCompress(String fileName,String extName,Integer quality){

        String cmdString = null;
        if("jpg".equalsIgnoreCase(extName)){
            cmdString = "convert -strip -quality "+quality+"% "+fileName+"  compressed_"+fileName;
        }else if (("gif".equalsIgnoreCase(extName))){
            cmdString = "convert "+fileName+"  -fuzz 5%  -layers Optimize compressed_"+fileName;
        }else {
            return fileName;
        }
        String[] cmd = {"/bin/sh", "-c", cmdString};
        Process process;
        try {
            process = Runtime.getRuntime().exec(cmd);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            process.waitFor();
            while ((line = input.readLine()) != null){
                stringBuilder.append(line);
            }
            int result = -1;
            result = process.exitValue();
            if(result==0){
                return "compressed_"+fileName;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }finally {
            return fileName;
        }
    }
}
