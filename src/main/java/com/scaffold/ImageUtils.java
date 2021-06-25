package com.scaffold;



import com.sun.media.jai.codec.ByteArraySeekableStream;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author tianjl
 * @Date 2021/6/24 15:28
 * @Discription disc
 */
public class ImageUtils {


    private static final int COVER_WIDTH = 220;
    /**
     * 首页的高度
     */
    private static final int COVER_HEIGHT = 165;
    /**
     * 相册的宽度
     */
    private static final int ABSTRCT_COVER_WIDTH = 250;
    /**
     * 相册的高度
     */
    private static final int ABSTRACT_COVER_HEIGHT = 200;

    /**
     * 剪切图片
     *
     * @param inputStream        待剪切图片路径
     * @param x                起始横坐标
     * @param y                起始纵坐标
     * @param width            剪切宽度
     * @param height        剪切高度
     * @param ratio 压缩比例
     * @returns            裁剪后保存路径（图片后缀根据图片本身类型生成）
     * @throws IOException
     */
    public static byte[] cutImage(InputStream inputStream, String formatName, int x , int y , int width , int height,float ratio) throws IOException{
        x=Math.round(x*ratio);
        y=Math.round(y*ratio);
        width=Math.round(width*ratio);
        height=Math.round(height*ratio);
        formatName = formatName.toLowerCase();
        // GIF需要特殊处理
        if("gif".equals(formatName)){
            GifDecoder decoder = new GifDecoder();
            int status = decoder.read(inputStream);
            if (status != GifDecoder.STATUS_OK) {
                throw new IOException("read image  error!");
            }
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
            encoder.start(outputStream);
            encoder.setRepeat(decoder.getLoopCount());
            for (int i = 0; i < decoder.getFrameCount(); i ++) {
                encoder.setDelay(decoder.getDelay(i));
                BufferedImage childImage = decoder.getFrame(i);
                BufferedImage image = childImage.getSubimage(x, y, width, height);
                encoder.addFrame(image);
            }
            return outputStream.toByteArray();
        }else{
            byte[] img= fetchImage(inputStream);
            ByteArraySeekableStream stream = new ByteArraySeekableStream(img);
            PlanarImage image = JAI.create("stream", stream);
            BufferedImage bufimg = image.getAsBufferedImage();
            bufimg = bufimg.getSubimage(x, y, width, height);
            ByteArrayOutputStream os= bufferedImageToInputStream(bufimg,formatName);
            assert os != null;
            return os.toByteArray();
        }
    }

    /**
     * 对相册首页的图片进行缩放
     * @param image 图片
     * @param isVertical 是否是垂直的
     * @param flag flag
     * @return 缩放之后的图片
     * @throws IOException 异常
     */
    public static BufferedImage coverGroupImage(BufferedImage image, boolean isVertical, int flag) throws IOException {
        int WIDTH = (flag == 1) ? ABSTRCT_COVER_WIDTH : COVER_WIDTH;
        int HEIGHT = (flag == 1) ? ABSTRACT_COVER_HEIGHT : COVER_HEIGHT;
        int targetW = isVertical ? HEIGHT : WIDTH;
        int targetH = isVertical ? WIDTH : HEIGHT;
        if (image.getWidth() != targetW || image.getHeight() != targetH) {
            image = zoom(image, targetW, targetH);
        }
        return image;
    }

    /**
     * 压缩图片
     * @param width            压缩宽度
     * @param height        压缩高度
     *
     * @returns                   裁剪后保存路径（图片后缀根据图片本身类型生成）
     * @throws IOException
     */
    public static BufferedImage zoom(BufferedImage sourceImage,int width , int height) throws IOException{
        // GIF需要特殊处理
        GifDecoder decoder = new GifDecoder();
        ByteArrayOutputStream os=bufferedImageToInputStream(sourceImage,"gif");
        ByteArrayInputStream is=new ByteArrayInputStream(os.toByteArray());
        int status = decoder.read(is);
        if (status != GifDecoder.STATUS_OK) {
            throw new IOException("read image error!");
        }
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(outputStream);
        encoder.setRepeat(decoder.getLoopCount());
        for (int i = 0; i < decoder.getFrameCount(); i ++) {
            encoder.setDelay(decoder.getDelay(i));
            BufferedImage image = zoomGif(decoder.getFrame(i), width , height);
            encoder.addFrame(image);
        }
        ByteArraySeekableStream stream = new ByteArraySeekableStream(outputStream.toByteArray());
        PlanarImage image = JAI.create("stream", stream);
        sourceImage = image.getAsBufferedImage();
        return sourceImage;
    }


    /**
     * 逐页压缩图片
     * @param sourceImage    待压缩图片
     * @param width          压缩图片高度
     * @param height          压缩图片宽度
     */
    private static BufferedImage zoomGif(BufferedImage sourceImage , int width , int height){
        BufferedImage zoomImage = new BufferedImage(width, height, sourceImage.getType());
        Image image = sourceImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        Graphics gc = zoomImage.getGraphics();
        gc.setColor(Color.WHITE);
        gc.drawImage( image , 0, 0, null);
        return zoomImage;
    }

    /**
     * 字节流转化成字节码
     * @param inputStream inputStream
     * @return byte字节数组
     */
    private static byte[] fetchImage(InputStream inputStream){
        //其他图片
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream(102400);
        try{
            int BUFF_SIZE = 4096;
            byte[] buffer = new byte[BUFF_SIZE];
            int byteRead = 0;
            while ((byteRead = inputStream.read(buffer)) > 0) {
                dataOut.write(buffer, 0, byteRead);
            }
            return dataOut.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null){
                    inputStream.close();
                }
                dataOut.close();
            } catch (IOException e) {
            }
        }
        return null;
    }

    /**
     * bufferedImage转化未stream
     * @param image 图片
     * @param type 图片类型
     * @return outPutStream
     */
    public static ByteArrayOutputStream bufferedImageToInputStream(BufferedImage image, String type){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            switch (type){
                case "png":
                    type="png";
                    break;
                case "gif":
                    type="gif";
                    break;
                case "webb":
                    type="webp";
                    break;
                default:
                    type="jpg";
                    break;
            }
            ImageIO.write(image, type, os);
            return os;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
