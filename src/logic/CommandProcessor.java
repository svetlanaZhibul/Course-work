package logic;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;


public class CommandProcessor {

    final static String[] paramFun = {"bright", "contrast", "rotate", "grayshades"};
    final static String[] noParamFun = {"sharp", "edges", "blurimg", "blurtoright", "blurtoleft", "smooth", "shape3d"};

    public void readCommand (String cmd) throws IOException {

        List<ImgProcessor> imgArr = new ArrayList<>();

        String format;
        boolean executed = false;

        int k = 0;
        StringTokenizer tokenizer = new StringTokenizer(cmd);
        while(tokenizer.hasMoreTokens()) {
            k++;
            tokenizer.nextToken();
        }
        String[] cmds = new String[k];
        k = 0;
        tokenizer = new StringTokenizer(cmd);
        while(tokenizer.hasMoreTokens()) {
            k++;
            cmds[k-1] = new String(tokenizer.nextToken());
        }        //String[] cmds = cmd.split("\\s");

        if(cmd.equals(""))
            return;
        if ((cmds[0].toLowerCase()).equals("exit"))
            System.exit(1);
        else if ((cmds[0].toLowerCase()).equals("man")) {
            showManual();
        } else {
            File f = new File(cmds[0]);
            if (f.exists()){
                try {
                    imgArr = listFilesForFolder(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else System.out.println("Check your file name or path. No such file or directory");

            if(imgArr != null) {
                if (cmds.length > 4)
                    System.out.println("Check your request commands. Use \"man\" for help.");
                else {
                    switch (cmds.length) {
                        case 2:
                            if(Arrays.asList(paramFun).contains(cmds[1].toLowerCase())) {
                                try {
                                    callMethod(cmds[1], 0, imgArr);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    callMethod(cmds[1], imgArr);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case 3:
                            if(Arrays.asList(paramFun).contains(cmds[1].toLowerCase())
                                    && isInteger(cmds[2]))
                                try {
                                    callMethod(cmds[1], (int)Double.parseDouble(cmds[2]), imgArr);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                            else
                                System.out.println("Check command writing");
                            break;
                        case 4:
                            System.out.println("Check command writing");
                            break;
                        default:
                            break;
                    }
                }
            } else
                System.out.println("No image to process");
        }
        if(executed) saveCommand(cmd);
    }

    private void process () {

    }

    private void showManual () throws FileNotFoundException {
        BufferedReader fin = new BufferedReader(new FileReader("recall.txt"));
        String line;
        try {
            while ((line = fin.readLine()) != null) System.out.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void callMethod(String methodName, int param, List<ImgProcessor> imgArr) throws ClassNotFoundException, NoSuchMethodException, IOException {
        Class c = (new ImgProcessor()).getClass();
        Method[] methods = c.getMethods();
        for(Method method : methods) {
            if ((method.getName().toLowerCase()).contains(methodName.toLowerCase())) {
                if ((method.getName().toLowerCase()).equals("rotate")){
                    for(ImgProcessor iproc : imgArr){
                        iproc.rotate(param);
                    }
                } else {
                    for(ImgProcessor iproc : imgArr){
                        iproc.callRGBmethod(method, param, iproc);
                    }
                }
                //method.invoke(Class.forName("ImgProcessor").newInstance());
                break;
            }
            //else System.out.println("No such method "+methodName+". Try \"man\" to see available functions");
        }
    }
    public void callMethod(String methodName, List<ImgProcessor> imgArr) throws ClassNotFoundException, NoSuchMethodException, IOException {
        Class c = (new ImgProcessor()).getClass();
        try {
            Method[] methods = c.getMethods();
            for(Method method : methods) {
                if ((method.getName().toLowerCase()).contains(methodName.toLowerCase())) {
                    if (Arrays.asList(noParamFun).contains(methodName.toLowerCase())){
                        for(ImgProcessor iproc : imgArr){
                            method.invoke(iproc, iproc.getExtension());
                        }
                        break;
                    } else {
                        for(ImgProcessor iproc : imgArr){
                            iproc.callRGBmethod(method, 0, iproc);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public List<ImgProcessor> listFilesForFolder(final File f) throws IOException {
        String[] extensions = {"jpg", "png", "bmp"};

        if(f.isDirectory()) {
            List<ImgProcessor> imgs = new ArrayList<ImgProcessor>();
            for (final File fileEntry : f.listFiles()) {
                if (fileEntry.isDirectory()) {
                    //listFilesForFolder(fileEntry);
                } else {
                    //System.out.println(fileEntry.getName());
                    if ( Arrays.asList(extensions).contains(getFileExtension(fileEntry).toLowerCase()) ) {
                        File finner = new File(fileEntry.getPath());
                        ImgProcessor img = new ImgProcessor(finner);
                        imgs.add(img);
                    }
                }
            }
            return imgs;
        } else {
            List<ImgProcessor> imgs = new ArrayList<ImgProcessor>();
            switch (getFileExtension(f).toLowerCase()){
                case "jpg":case "png":case "bmp":
                    imgs.add(new ImgProcessor(f));
                    break;
                case "txt":
                    BufferedReader fin = new BufferedReader(new FileReader(f));
                    String line;
                    while ((line = fin.readLine()) != null) {
                        File finner = new File(line);
                        imgs.add(new ImgProcessor(finner));
                    }
                    break;
                default:
                    System.out.println("Check yor path.");
                    break;
            }
            return imgs;
        }
    }

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        // если в имени файла есть точка и она не является первым символом в названии файла
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            // то вырезаем все знаки после последней точки в названии файла, то есть ХХХХХ.txt -> txt
            return fileName.substring(fileName.lastIndexOf(".")+1);
            // в противном случае возвращаем заглушку, то есть расширение не найдено
        else return "";
    }

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }
    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    private void saveCommand(String cmd) throws IOException {
        //Files.write(Paths.get("myfile.txt"), "the text".getBytes(), StandardOpenOption.APPEND);
        File f = new File("story.txt");
        f.getParentFile().mkdirs();
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter fout = new BufferedWriter(new FileWriter(f));
        PrintWriter out = new PrintWriter(fout);
        out.println(cmd);
        fout.flush();
        //fout.close();
    }
}
