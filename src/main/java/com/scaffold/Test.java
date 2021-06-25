package com.scaffold;

import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @Author tianjl
 * @Date 2021/6/25 8:43
 * @Discription disc
 */
public class Test {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        try {
            // 起始坐标，剪切大小
            int x = 0;
            int y = 0;
            int width = 100;
            int height = 100;
            // 参考图像大小
            int clientWidth = 300;
            int clientHeight = 250;


            File file = new File("C:\\Users\\tianjingle\\Desktop\\0.gif");
            BufferedImage image = ImageIO.read(file);
            double destWidth = image.getWidth();
            double destHeight = image.getHeight();

            if(destWidth < width || destHeight < height) {
                throw new Exception("源图大小小于截取图片大小!");
            }

            double widthRatio = destWidth / clientWidth;
            double heightRatio = destHeight / clientHeight;

            x = Double.valueOf(x * widthRatio).intValue();
            y = Double.valueOf(y * heightRatio).intValue();
            width = Double.valueOf(width * widthRatio).intValue();
            height = Double.valueOf(height * heightRatio).intValue();

            System.out.println("裁剪大小  x:" + x + ",y:" + y + ",width:" + width + ",height:" + height);
            float ratio = ((float) image.getWidth()) / image.getWidth();
            String formatName = "gif";
            String pathSuffix = "." + formatName;
            String pathPrefix = "C:\\Users\\tianjingle\\Desktop\\";
            String targetPath = pathPrefix  + System.currentTimeMillis() + pathSuffix;
            byte[] target = ImageUtils.cutImage(new FileInputStream(file.getPath()), "gif", x , y , width, height,ratio);
            FileUtils.writeByteArrayToFile(new File(targetPath),target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
