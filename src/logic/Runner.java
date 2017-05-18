package logic;

/**
 * Created by Lenovo on 16.04.2017.
 */
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;

import static logic.ImgProcessor.*;
import static logic.CommandProcessor.*;

public class Runner {

    public static void main(String args[]) throws IOException {

        while (true) {
            System.out.print("> ");
            Scanner scan = new Scanner(System.in);
            String command = scan.nextLine();
            (new CommandProcessor()).readCommand(command);
            //System.out.println("\n");
        }
    }

}
