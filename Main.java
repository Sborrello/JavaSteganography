import java.nio.charset.StandardCharsets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            // Load the cover image
            BufferedImage coverImage = ImageIO.read(new File("cover.png"));
            
            Scanner scanner = new Scanner(System.in);
            System.out.println("Choose Steganography type: (1) Image Steganography (2) Text Steganography");
            System.out.print("Enter 1 or 2: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    // Image Steganography
                    System.out.println("Enter the filename of the secret image to embed:");
                    String secretImageFile = scanner.nextLine();
                    BufferedImage secretImage = ImageIO.read(new File(secretImageFile));
                    System.out.println("Cover Image Dimensions: " + coverImage.getWidth() + "x" + coverImage.getHeight());
                    System.out.println("Secret Image Dimensions: " + secretImage.getWidth() + "x" + secretImage.getHeight());

                    // Embed the secret image into the cover image
                    BufferedImage embeddedImage = embedImage(coverImage, secretImage);
                    saveImage(embeddedImage, "coverWithSecret.png");
                    System.out.println("Secret image embedded successfully!");
                    break;

                case 2:
                    // Text Steganography
                    System.out.print("Enter the text you want to embed: ");
                    String secretText = scanner.nextLine();

                    // Embed the secret text into the cover image
                    BufferedImage embeddedImageWithText = embedText(coverImage, secretText);
                    saveImage(embeddedImageWithText, "coverWithText.png");
                    System.out.println("Secret text embedded successfully!");
                    break;

                default:
                    System.out.println("Invalid choice!");
                    break;
            }
            
        } catch (IOException e) {
            System.err.println("Error loading images: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Embed secret image into cover image
    public static BufferedImage embedImage(BufferedImage coverImage, BufferedImage secretImage) {
        int coverWidth = coverImage.getWidth();
        int coverHeight = coverImage.getHeight();
        int secretWidth = secretImage.getWidth();
        int secretHeight = secretImage.getHeight();

        int totalPixelsSecret = secretWidth * secretHeight;
        int totalPixelsCover = coverWidth * coverHeight;

        // Each pixel requires 32 bits (4 bytes), so cover image must be large enough
        if (totalPixelsCover < totalPixelsSecret * 32) {
            throw new IllegalArgumentException("Cover image is too small to embed the secret image.");
        }

        int[] secretRGBArray = new int[totalPixelsSecret]; // RGB values from the secret image
        int pixelIndex = 0;

        // Extract RGB values from the secret image into an array
        for (int y = 0; y < secretHeight; y++) {
            for (int x = 0; x < secretWidth; x++) {
                secretRGBArray[pixelIndex++] = secretImage.getRGB(x, y);
            }
        }

        int x = 0, y = 0; // Coordinates for the cover image

        // Embed secret image pixels into the cover image
        for (int i = 0; i < totalPixelsSecret; i++) {
            int pixel = secretRGBArray[i];

            for (int bitIndex = 0; bitIndex < 32; bitIndex++) {
                int bit = (pixel >> bitIndex) & 1; // Extract the bit

                // Ensure coordinates are within bounds
                if (x >= coverWidth) {
                    x = 0;
                    y++;
                }

                if (y >= coverHeight) {
                    throw new IllegalArgumentException("Cover image is too small to embed the secret image.");
                }

                // Embed the bit into the cover image pixel
                int coverPixel = coverImage.getRGB(x, y);
                if (bit == 1) {
                    coverImage.setRGB(x, y, coverPixel | 0x00000001); // Set the LSB to 1
                } else {
                    coverImage.setRGB(x, y, coverPixel & 0xFFFFFFFE); // Set the LSB to 0
                }

                x++;
            }
        }

        return coverImage;
    }

    // Embed secret text into cover image with a null terminator
    public static BufferedImage embedText(BufferedImage coverImage, String secretText) {
        byte[] textBytes = (secretText + '\0').getBytes(StandardCharsets.UTF_8); // Add null terminator
        int coverWidth = coverImage.getWidth();
        int coverHeight = coverImage.getHeight();
        int totalPixels = coverWidth * coverHeight;
        
        if (textBytes.length * 8 > totalPixels) {
            throw new IllegalArgumentException("Cover image is too small to embed the text.");
        }

        int x = 0, y = 0;

        for (byte b : textBytes) {
            for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                int bit = (b >> bitIndex) & 1;

                if (x >= coverWidth) {
                    x = 0;
                    y++;
                }

                int coverPixel = coverImage.getRGB(x, y);
                if (bit == 1) {
                    coverImage.setRGB(x, y, coverPixel | 0x00000001); // Set LSB to 1
                } else {
                    coverImage.setRGB(x, y, coverPixel & 0xFFFFFFFE); // Set LSB to 0
                }
                x++;
            }
        }
        return coverImage;
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