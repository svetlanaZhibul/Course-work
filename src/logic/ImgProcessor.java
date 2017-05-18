package logic;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static logic.CommandProcessor.getFileExtension;


public class ImgProcessor {

    final static String[] paramFun = {"changebrightness", "changecontrast", "rotate", "grayshades"};

    private BufferedImage img;
    private String extension;

    ImgProcessor (File f) throws IOException {
        img = ImageIO.read(f);
        extension = getFileExtension(f);
    }
    ImgProcessor (){}

    public BufferedImage getImg() {
        return img;
    }

    public String getExtension() {
        return extension;
    }

    public void callRGBmethod (Method method, int param, ImgProcessor obj) {
        int color;
        int new_col;
        int r;
        int g;
        int b;
        int a;
        for(int i = 0; i < obj.getImg().getHeight(); i++) {
            for (int j = 0; j < obj.getImg().getWidth(); j++) {
                color = img.getRGB(j, i);
                b = color & 0xff;
                g = (color & 0xff00) >> 8;
                r = (color & 0xff0000) >> 16;
                a = (color & 0xff000000) >>> 24;
                try {
                    if (Arrays.asList(paramFun).contains(method.getName().toLowerCase()))
                        new_col = (int)method.invoke(obj, param, a, r, g, b);
                    else
                        new_col = (int)method.invoke(obj, a, r, g, b);
                    obj.getImg().setRGB(j,i,new_col);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();

                }
            }
        }
        File f = new File("./sample/"+obj.toString()+"."+obj.getExtension());
        f.getParentFile().mkdirs();
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ImageIO.write(obj.getImg(), obj.getExtension(), f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveFile (BufferedImage bi, String ext) {
        File f = new File("sample/"+(bi.toString()).substring(0,22)+"."+ext);
        f.getParentFile().mkdirs();
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ImageIO.write(bi, ext, f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rotate (double angle) {
        /*
        int len = img.getHeight();
        int wid = img.getWidth();
        int new_len = (int)(len*Math.cos(angle) + wid*Math.sin(angle));
        int new_wid = (int)(len*Math.sin(angle) + wid*Math.cos(angle));
        BufferedImage rotated;
        if (angle%90 == 0) {
            rotated = new BufferedImage(len, wid, BufferedImage.TYPE_INT_ARGB);
        } else {
            rotated = new BufferedImage(new_wid, new_wid, BufferedImage.TYPE_INT_ARGB);
        }

        for (int i = 0; i < img.getHeight(); i++){
            for (int j = 0; j < img.getWidth(); j++) {
                if (i >= (new_len - len)/2 && i <= (new_len + len)/2
                        && j >= (new_wid - wid)/2 && j <= (new_wid + wid)/2)
                rotated.setRGB(j, i, 0);
            }
        }
        */
        AffineTransform transform = AffineTransform.getRotateInstance(
                Math.toRadians(angle),
                img.getWidth()/2,
                img.getHeight()/2
        );
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        filter(op, getExtension());//filter(img, filtered);
    }
    public void sharpenImg (String ext) {
        float[] elements = {0.0f, -1.0f, 0.0f, -1.0f, 5.0f, -1.0f, 0.0f, -1.0f, 0.0f};
        Kernel kernel = new Kernel(3, 3, elements);
        ConvolveOp op = new ConvolveOp(kernel);
        filter(op, ext);
    }
    public void detectEdges (String ext) {
        float[] elements = {0.0f, -1.0f, 0.0f, -1.0f, 4.0f, -1.0f, 0.0f, -1.0f, 0.0f};
        Kernel kernel = new Kernel(3, 3, elements);
        ConvolveOp op = new ConvolveOp(kernel);
        filter(op, ext);
    }
    public void blurImg (String ext) {
        float[] elements = {1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f};
        Kernel kernel = new Kernel(3, 3, elements);
        ConvolveOp op = new ConvolveOp(kernel);
        filter(op, ext);
    }
    public void smoothImg (String ext) {
        float[] elements = {1/16f, 1/8f, 1/16f,
                             1/8f, 1/4f, 1/8f,
                            1/16f, 1/8f, 1/16f};
        Kernel kernel = new Kernel(3, 3, elements);
        ConvolveOp op = new ConvolveOp(kernel);
        filter(op, ext);
    }
    public void shape3dImg (String ext) {
        float[] elements = {-1, -1, 0, -1, 0, 1, 0, 1, 1};
        Kernel kernel = new Kernel(3, 3, elements);
        ConvolveOp op = new ConvolveOp(kernel);
        filter(op, ext);
    }
    public void motionBlurToRight (String ext) {
        float[] elements = {1, 0, 0, 0,
                            0, 1, 0, 0,
                            0, 0, 1, 0,
                            0, 0, 0, 1};
        Kernel kernel = new Kernel(4, 4, elements);
        ConvolveOp op = new ConvolveOp(kernel);
        filter(op, ext);
    }
    public void motionBlurToLeft (String ext) {
        float[] elements = {0, 0, 0, 1,
                            0, 0, 1, 0,
                            0, 1, 0, 0,
                            1, 0, 0, 0};
        Kernel kernel = new Kernel(4, 4, elements);
        ConvolveOp op = new ConvolveOp(kernel);
        filter(op, ext);
    }
    public int sepia (int a, int r, int g, int b) {
        int new_r = (int)((r * .393) + (g *.769) + (b * .189));
        int new_g = (int)((r * .349) + (g *.686) + (b * .168));
        int new_b = (int)((r * .272) + (g *.534) + (b * .131));

        int[] rgb = {new_r, new_g, new_b};

        for (int color:rgb) {
            if (color > 255)
                color = 255;
        }

        int sepiaScale = (a << 24) | rgb[0] << 16 | rgb[1] << 8 | rgb[2];
        return sepiaScale;
    }
    private void filter (BufferedImageOp op, String ext) {
        BufferedImage filtered = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        op.filter(img, filtered);
        saveFile(filtered, ext);
    }
    public static int grayLight (int a, double r, double g, double b) {
        return (a << 24)
                |((int)(r*0.18+g*0.65+b*0.8) << 16)
                |((int)(r*0.18+g*0.65+b*0.8) << 8)
                |((int)(r*0.18+g*0.65+b*0.8));
    }
    public static int grayAVG (int a, double r, double g, double b) {
        return (a << 24)
                |((int)((r+g+b)/3) << 16)
                |((int)((r+g+b)/3) << 8)
                |((int)((r+g+b)/3));
    }
    public static int grayshades (int shades, int a, double r, double g, double b) {
        double conversionFactor;
        if (shades >= 2) {
            conversionFactor = 255 / (shades - 1);
        } else {
            System.out.println("There can not be less than 2 shades.\nNumber of shades is considered to equal 2");
            conversionFactor = 255;
        }
        double avg = (r + g + b) / 3;
        int gray = (int) (((avg / conversionFactor) + 0.5) * conversionFactor);
        return (a << 24) | gray << 16 | gray << 8 | gray;
    }
    public static int changeBrightness (int br_koef, int a, double r, double g, double b) {
        if (br_koef < -127) {
            br_koef = -127;
            System.out.println("Wrong koef.\\nImage was converted due to koef = -127");
        }
        if (br_koef > 127){
            br_koef = 127;
            System.out.println("Wrong koef.\\nImage was converted due to koef = 127");
        }

        int l_brightness = br_koef;          //-127 - 127

        r += (1-(r/255))*l_brightness;
        g += (1-(g/255))*l_brightness;
        b += (1-(b/255))*l_brightness;

        if(r < 0) r = 0;
        if(r > 255) r = 255;
        if(g < 0) g = 0;
        if(g > 255) g = 255;
        if(b < 0) b = 0;
        if(b > 255) b = 255;

        return (a << 24) | (int)r << 16 | (int)g << 8 | (int)b;
    }
    public static int changeContrast (int con_koef, int a, double r, double g, double b) {
        double[] colors = {r, g, b};
        for (double c : colors) {
            c /= 255.0;
            c -= 0.5;
            c *= con_koef;
            c += 0.5;
            c *= 255.0;
        }

        if(colors[0] < 0) colors[0] = 0;
        if(colors[0] > 255) colors[0] = 255;
        if(colors[1] < 0) colors[1] = 0;
        if(colors[1] > 255) colors[1] = 255;
        if(colors[2] < 0) colors[2] = 0;
        if(colors[2] > 255) colors[2] = 255;

        return (a << 24) | (int)colors[0] << 16 | (int)colors[1] << 8 | (int)colors[2];
    }

    public static String format (String ext) {
        switch (ext.toLowerCase()){
            case "jpg": case "jpeg": return "jpg";
            case "png": return "png";
            case "bmp": return "bmp";
            case "gif": return "gif";
            default: return ext;
        }
    }

}
