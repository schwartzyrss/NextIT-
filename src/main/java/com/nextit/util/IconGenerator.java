package com.nextit.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IconGenerator {

    public static BufferedImage generateIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        float pad = size * 0.06f;
        float diameter = size - (pad * 2f);

        // 1. Soft ambient shadow under orb
        if (size >= 32) {
            float shadowOffset = size * 0.04f;
            g2.setColor(new Color(99, 102, 241, 45));
            g2.fill(new Ellipse2D.Float(pad, pad + shadowOffset, diameter, diameter));
        }

        // 2. Base lavender-periwinkle gradient orb
        Point2D start = new Point2D.Float(pad, pad);
        Point2D end = new Point2D.Float(pad + diameter, pad + diameter);
        Color lavender = new Color(0xC4, 0xB5, 0xFD); // #C4B5FD
        Color periwinkle = new Color(0x81, 0x8C, 0xF8); // #818CF8
        GradientPaint gradient = new GradientPaint(start, lavender, end, periwinkle);
        g2.setPaint(gradient);
        Ellipse2D.Float orb = new Ellipse2D.Float(pad, pad, diameter, diameter);
        g2.fill(orb);

        // 3. Soft claymorphic top highlight
        float hlPad = pad + diameter * 0.08f;
        float hlWidth = diameter * 0.84f;
        float hlHeight = diameter * 0.44f;
        GradientPaint hlGrad = new GradientPaint(
                new Point2D.Float(hlPad, hlPad),
                new Color(255, 255, 255, 120),
                new Point2D.Float(hlPad, hlPad + hlHeight),
                new Color(255, 255, 255, 0)
        );
        g2.setPaint(hlGrad);
        g2.fill(new Ellipse2D.Float(hlPad, hlPad, hlWidth, hlHeight));

        // 4. Stylized "N" brand monogram in crisp white
        float cx = size / 2f;
        float cy = size / 2f;
        float nWidth = diameter * 0.46f;
        float nHeight = diameter * 0.50f;
        float left = cx - (nWidth / 2f);
        float top = cy - (nHeight / 2f);
        float strokeW = Math.max(1.8f, size * 0.085f);

        Path2D.Float nPath = new Path2D.Float();
        // Left vertical stem
        nPath.moveTo(left, top + nHeight);
        nPath.lineTo(left, top);
        // Diagonal bridge
        nPath.lineTo(left + nWidth, top + nHeight);
        // Right vertical stem
        nPath.lineTo(left + nWidth, top);

        g2.setColor(new Color(255, 255, 255, 240));
        g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(nPath);

        g2.dispose();
        return image;
    }

    public static byte[] createIco(List<BufferedImage> images) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<byte[]> pngBytesList = new ArrayList<>();

        for (BufferedImage img : images) {
            ByteArrayOutputStream pngOs = new ByteArrayOutputStream();
            ImageIO.write(img, "png", pngOs);
            pngBytesList.add(pngOs.toByteArray());
        }

        int count = images.size();
        // Header: 6 bytes
        baos.write(new byte[]{0, 0, 1, 0, (byte) count, 0});

        int offset = 6 + (count * 16);
        for (int i = 0; i < count; i++) {
            BufferedImage img = images.get(i);
            byte[] pngData = pngBytesList.get(i);

            int width = img.getWidth() >= 256 ? 0 : img.getWidth();
            int height = img.getHeight() >= 256 ? 0 : img.getHeight();

            baos.write((byte) width);
            baos.write((byte) height);
            baos.write(0); // color count
            baos.write(0); // reserved
            baos.write(new byte[]{1, 0}); // planes
            baos.write(new byte[]{32, 0}); // bit count

            int dataSize = pngData.length;
            baos.write((byte) (dataSize & 0xFF));
            baos.write((byte) ((dataSize >> 8) & 0xFF));
            baos.write((byte) ((dataSize >> 16) & 0xFF));
            baos.write((byte) ((dataSize >> 24) & 0xFF));

            baos.write((byte) (offset & 0xFF));
            baos.write((byte) ((offset >> 8) & 0xFF));
            baos.write((byte) ((offset >> 16) & 0xFF));
            baos.write((byte) ((offset >> 24) & 0xFF));

            offset += dataSize;
        }

        for (byte[] pngData : pngBytesList) {
            baos.write(pngData);
        }

        return baos.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        int[] sizes = {16, 32, 48, 64, 128, 256};
        List<BufferedImage> icoImages = new ArrayList<>();

        Path resIconsDir = Path.of("src/main/resources/com/nextit/icons");
        Files.createDirectories(resIconsDir);

        for (int size : sizes) {
            BufferedImage img = generateIcon(size);
            ImageIO.write(img, "png", resIconsDir.resolve("nextit-" + size + ".png").toFile());
            if (size == 16 || size == 32 || size == 48 || size == 256) {
                icoImages.add(img);
            }
        }

        byte[] icoData = createIco(icoImages);

        Files.write(resIconsDir.resolve("nextit.ico"), icoData);
        Files.write(Path.of("nextit.ico"), icoData);

        System.out.println("Generated all icons and nextit.ico successfully!");
    }
}
