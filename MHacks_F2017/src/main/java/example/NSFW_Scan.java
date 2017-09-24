package example;
/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * EDITING INSTRUCTIONS
 * This file is referenced in READMEs. Any change to this file should be reflected in
 * the project's READMEs.
 */

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.protobuf.ByteString;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * A snippet for Google Cloud Vision API demonstrating how to determine what is shown on a picture.
 */
public class NSFW_Scan {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Robot robot;
	private ByteArrayOutputStream os;
	public String outputDirectory;
	private int numNSFWInARow;
	
	public NSFW_Scan(String outputDirectory) throws AWTException {
		this.outputDirectory = outputDirectory;
		this.robot = new Robot();
		this.os = new ByteArrayOutputStream();
		this.numNSFWInARow = 0;
	}
	
	public boolean detectUnsafeSearch(byte[] bts) throws Exception {
		List<AnnotateImageRequest> requests = new ArrayList<>();

		ByteString imgBytes = ByteString.copyFrom(bts);

		Image img = Image.newBuilder().setContent(imgBytes).build();
		Feature feat = Feature.newBuilder().setType(Type.SAFE_SEARCH_DETECTION).build();
		AnnotateImageRequest request =
				AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
		requests.add(request);

		try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
			BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
			List<AnnotateImageResponse> responses = response.getResponsesList();

			for (AnnotateImageResponse res : responses) {
				if (res.hasError()) {
					System.out.printf("Error: %s\n", res.getError().getMessage());
					return false;
				}

				SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();
				System.out.println("adult: " + annotation.getAdult());

				if(annotation.getAdultValue() > 3) {
					numNSFWInARow++;
					System.out.println("BAD THINGS");
					return true;
				}
			}

			return false;
		}
	}

	public void saveImage(byte[] bts) throws IOException {
		ImageIO.write(ImageIO.read(new ByteArrayInputStream(bts)), "JPG", new File(outputDirectory + DATE_FORMAT.format(new Date()) + ".jpeg"));
	}

	public byte[] cap() throws Exception {
		os.reset();
		BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
		ImageIO.write(screenShot, "JPG", os);

		return os.toByteArray();
	}
	
	public int getNumNSFWInARow(){
		return this.numNSFWInARow;
	}
}
