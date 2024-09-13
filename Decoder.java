import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Decoder {
    public static void main(String[] args) {
        try {
            // Load the image with embedded data
            File file = new File("coverWithText.png");
            if (!file.exists()) {
                System.err.println("File does not exist: " + file.getAbsolutePath());
                return;
            }

            BufferedImage embeddedImage = ImageIO.read(file);
            if (embeddedImage == null) {
                System.err.println("Failed to decode the image: " + file.getName());
                return;
            }

            // Ask the user for the type of hidden data
            System.out.println("Choose Decoding type: (1) Image Decoding (2) Text Decoding");
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    // Decode the image
                    System.out.println("Enter the width of the secret image:");
                    int secretWidth = scanner.nextInt();
                    System.out.println("Enter the height of the secret image:");
                    int secretHeight = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    BufferedImage secretImage = extractImage(embeddedImage, secretWidth, secretHeight);
                    saveImage(secretImage, "extractedSecret.png");
                    System.out.println("Secret image extracted and saved as 'extractedSecret.png'");
                    break;

                case 2:
                    // Decode the text
                    String secretText = extractText(embeddedImage);
                    System.out.println("Extracted text: " + secretText);
                    break;

                default:
                    System.out.println("Invalid choice!");
            }

        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }

    // Extract secret image from cover image
    public static BufferedImage extractImage(BufferedImage embeddedImage, int secretWidth, int secretHeight) {
        int[] secretRGBArray = new int[secretWidth * secretHeight];
        int coverWidth = embeddedImage.getWidth();
        int x = 0, y = 0;

        // Extract RGB values bit by bit from the embedded image
        for (int i = 0; i < secretRGBArray.length; i++) {
            int pixel = 0;

            for (int bitIndex = 0; bitIndex < 32; bitIndex++) {
                if (x >= coverWidth) {
                    x = 0;
                    y++;
                }

                if (y >= embeddedImage.getHeight()) {
                    throw new IllegalArgumentException("Cover image is too small to extract the secret image.");
                }

                int coverPixel = embeddedImage.getRGB(x, y);
                int bit = coverPixel & 0x00000001; // Extract the LSB
                pixel |= (bit << bitIndex); // Set the corresponding bit in the pixel

                x++;
            }

            secretRGBArray[i] = pixel;
        }

        // Create a BufferedImage to store the extracted secret image
        BufferedImage secretImage = new BufferedImage(secretWidth, secretHeight, BufferedImage.TYPE_INT_ARGB);
        int pixelIndex = 0;
        for (int yCoord = 0; yCoord < secretHeight; yCoord++) {
            for (int xCoord = 0; xCoord < secretWidth; xCoord++) {
                secretImage.setRGB(xCoord, yCoord, secretRGBArray[pixelIndex++]);
            }
        }

        return secretImage;
    }

    // Extract secret text from cover image with null terminator
    public static String extractText(BufferedImage embeddedImage) {
        int coverWidth = embeddedImage.getWidth();
        int x = 0, y = 0;

        StringBuilder extractedText = new StringBuilder();
        byte currentByte = 0;
        int bitCount = 0;

        for (int i = 0; i < embeddedImage.getHeight() * embeddedImage.getWidth(); i++) {
            if (x >= coverWidth) {
                x = 0;
                y++;
            }

            int coverPixel = embeddedImage.getRGB(x, y);
            int bit = coverPixel & 0x00000001;

            currentByte |= (bit << bitCount);
            bitCount++;

            if (bitCount == 8) {
                if (currentByte == '\0') {
                    break; // Stop extracting when null terminator is encountered
                }
                extractedText.append((char) currentByte);
                currentByte = 0;
                bitCount = 0;
            }
            x++;
        }

        return extractedText.toString();
    }

    // Save BufferedImage to a file
    public static void saveImage(BufferedImage image, String fileName) {
        try {
            File outputfile = new File(fileName);
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }
}