/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compressor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

/**
 *
 * @author mxp
 */
public class Compressor {

    /**
     * @param args the command line arguments
     *
     * args[0] is the file path including the filename
     *
     *
     * args[1] is the base folder
     *
     *
     * args[2] is the path to the pdf
     * @throws java.io.IOException
     * @throws java.io.FileNotFoundException
     * @throws org.apache.pdfbox.exceptions.COSVisitorException
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, COSVisitorException {

        if (args[0] != null && args[1] != null && args[2] != null ) {

            Compressor cp = new Compressor();

            String src = args[0];

            try {

                String image_name = FilenameUtils.getBaseName(args[0]);

                // @param1 is the full filepath
                // @param2 is the base folder which is the e.g. /home/mxp/Pictures/
                // @param3 is the naked image name without extension
                cp.extract_images(src, args[1], image_name);

                // @param1 is the src extracted path
                // @param2 is the compressed path
                cp.compress_images(args[1]+"img/", args[1]);

               

            } catch (Exception e) {

               

            } finally {
                
                FileUtils.cleanDirectory(new File(args[1]+"img/"));
                FileUtils.cleanDirectory(new File(args[1]+"compressed/"));
            }

        } else {

            System.out.println("parameters are missing");
        }

    }

    void extract_images(String src, String dest, String img_name) throws IOException {
        PDDocument document = null;
        try {
            document = PDDocument.load(src);
        } catch (IOException ex) {
            System.out.println("" + ex);
        }
        List pages = document.getDocumentCatalog().getAllPages();
        Iterator iter = pages.iterator();
        int i = 1;
        String name = null;

        File file = new File(dest + "img");
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }
        dest = dest + "img/";

        while (iter.hasNext()) {
            PDPage page = (PDPage) iter.next();
            PDResources resources = page.getResources();
            Map pageImages = resources.getImages();
            if (pageImages != null) {
                Iterator imageIter = pageImages.keySet().iterator();
                while (imageIter.hasNext()) {
                    String key = (String) imageIter.next();
                    PDXObjectImage image = (PDXObjectImage) pageImages.get(key);

                    image.write2file(dest + img_name + i);
                    i++;
                }
            }
        }
        document.close();
    }

    void compress_images(String src, String dest) throws IOException {

        File f = null;
        String[] paths;

        try {
            // create new file
            f = new File(src);

            // array of files and directory
            paths = f.list();

            File file = new File(dest + "compressed");
            if (!file.exists()) {
                if (file.mkdir()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }
            dest = dest + "compressed/";

            // for each name in the path array
            for (String path : paths) {
                // prints filename and directory name

                File input = new File(src + path);
                BufferedImage image = ImageIO.read(input);

                File compressedImageFile = new File(dest + path);
                OutputStream os = new FileOutputStream(compressedImageFile);

                Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
                ImageWriter writer = (ImageWriter) writers.next();

                ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                writer.setOutput(ios);

                ImageWriteParam param = writer.getDefaultWriteParam();

                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.05f);
                writer.write(null, new IIOImage(image, null, null), param);

                os.close();
                ios.close();
                writer.dispose();

            }
        } catch (Exception e) {
        }

    }

   
}
