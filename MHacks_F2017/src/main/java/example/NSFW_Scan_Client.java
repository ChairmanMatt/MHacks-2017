package example;

public class NSFW_Scan_Client {

	public static void main(String... args) throws Exception {
		NSFW_Scan scanner = new NSFW_Scan("/home/matt/Downloads/");
		while(true) {
			Thread.sleep(1000);
			byte[] bts = scanner.cap();
			if(scanner.detectUnsafeSearch(bts)) {
				scanner.saveImage(bts);
			}
			if(scanner.getNumNSFWInARow() > 3) {
				System.out.println(Audio.getMasterOutputMute());
				Audio.setMasterOutputMute(false);
				Audio.setMasterOutputVolume(1);
			}
		}
	}
}
