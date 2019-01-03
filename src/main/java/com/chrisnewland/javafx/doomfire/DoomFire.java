package com.chrisnewland.javafx.doomfire;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.IntBuffer;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DoomFire extends Application
{
	private final double width = 800;
	private final double height = 224;

	private final int imageWidth = (int) width;
	private final int imageHeight = (int) height;

	private GraphicsContext gc;

	private int[] palleteRefs;
	private int[] pixelData;

	private PixelWriter pixelWriter;
	private PixelFormat<IntBuffer> pixelFormat;

	private int[] pallete;

	private WritableImage imageFire;

	private Image imageLogo;

	public static void main(String[] args)
	{
		Application.launch(args);
	}

	@Override public void start(final Stage stage) throws Exception
	{
		BorderPane root = new BorderPane();

		Scene scene;

		scene = new Scene(root, width, height);

		Canvas canvas = new Canvas(width, height);

		gc = canvas.getGraphicsContext2D();

		setUp();

		root.setCenter(canvas);

		stage.setTitle("Doom Fire in JavaFX");

		stage.setScene(scene);

		stage.show();

		AnimationTimer timer = new AnimationTimer()
		{
			@Override public void handle(long startNanos)
			{
				clearBackground();

				if (imageLogo != null)
				{
					gc.drawImage(imageLogo, (width - imageLogo.getWidth()) / 2, 16);
				}

				render();

				writeFireImage();

				gc.drawImage(imageFire, 0, height - imageHeight);
			}
		};

		timer.start();
	}

	private void setUp()
	{
		try
		{
			imageLogo = new Image(getClass().getResourceAsStream("/javafx.png"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		imageFire = new WritableImage(imageWidth, imageHeight);

		pixelWriter = imageFire.getPixelWriter();

		pixelFormat = PixelFormat.getIntArgbPreInstance();

		int pixelCount = imageWidth * imageHeight;

		palleteRefs = new int[pixelCount];

		pixelData = new int[pixelCount];

		createPallete();

		intialisePalleteRefs();
	}

	private void createPallete()
	{
		int[] rawPalleteRGB = new int[] { 0x00, 0x00, 0x00, 0x1F, 0x07, 0x07, 0x2F, 0x0F, 0x07, 0x47, 0x0F, 0x07, 0x57, 0x17, 0x07,
				0x67, 0x1F, 0x07, 0x77, 0x1F, 0x07, 0x8F, 0x27, 0x07, 0x9F, 0x2F, 0x07, 0xAF, 0x3F, 0x07, 0xBF, 0x47, 0x07, 0xC7,
				0x47, 0x07, 0xDF, 0x4F, 0x07, 0xDF, 0x57, 0x07, 0xDF, 0x57, 0x07, 0xD7, 0x5F, 0x07, 0xD7, 0x5F, 0x07, 0xD7, 0x67,
				0x0F, 0xCF, 0x6F, 0x0F, 0xCF, 0x77, 0x0F, 0xCF, 0x7F, 0x0F, 0xCF, 0x87, 0x17, 0xC7, 0x87, 0x17, 0xC7, 0x8F, 0x17,
				0xC7, 0x97, 0x1F, 0xBF, 0x9F, 0x1F, 0xBF, 0x9F, 0x1F, 0xBF, 0xA7, 0x27, 0xBF, 0xA7, 0x27, 0xBF, 0xAF, 0x2F, 0xB7,
				0xAF, 0x2F, 0xB7, 0xB7, 0x2F, 0xB7, 0xB7, 0x37, 0xCF, 0xCF, 0x6F, 0xDF, 0xDF, 0x9F, 0xEF, 0xEF, 0xC7, 0xFF, 0xFF,
				0xFF };

		int palleteSize = rawPalleteRGB.length / 3;

		pallete = new int[palleteSize];

		for (int i = 0; i < palleteSize; i++)
		{
			int alpha = (i == 0) ? 0 : 255;

			int red = rawPalleteRGB[3 * i + 0];

			int green = rawPalleteRGB[3 * i + 1];

			int blue = rawPalleteRGB[3 * i + 2];

			int argb = (alpha << 24) + (red << 16) + (green << 8) + blue;

			pallete[i] = argb;
		}
	}

	private void intialisePalleteRefs()
	{
		int writeIndex = 0;

		for (int y = 0; y < imageHeight; y++)
		{
			for (int x = 0; x < imageWidth; x++)
			{
				if (y == imageHeight - 1)
				{
					palleteRefs[writeIndex++] = pallete.length - 1;
				}
				else
				{
					palleteRefs[writeIndex++] = 0;
				}
			}
		}
	}

	private void clearBackground()
	{
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, width, height);
	}

	private void render()
	{
		for (int x = 0; x < imageWidth; x++)
		{
			for (int y = 1; y < imageHeight; y++)
			{
				spreadFire(y * imageWidth + x);
			}
		}
	}

	private void spreadFire(int src)
	{
		int rand = (int) Math.round(Math.random() * 3.0) & 3;

		int dst = src - rand + 1;

		palleteRefs[Math.max(0, dst - imageWidth)] = Math.max(0, palleteRefs[src] - (rand & 1));
	}

	private void writeFireImage()
	{
		int pos = 0;

		for (int y = 0; y < imageHeight; y++)
		{
			for (int x = 0; x < imageWidth; x++)
			{
				pixelData[pos] = pallete[palleteRefs[pos]];

				pos++;
			}
		}

		pixelWriter.setPixels(0, 0, imageWidth, imageHeight, pixelFormat, pixelData, 0, imageWidth);
	}
}
