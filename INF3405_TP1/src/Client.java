import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Client {
	private String ipAddr_;
	private String path_;
	private int port_;
	public BufferedImage buffImg_;

	public static void main(String[] args) throws Exception {
		Client client = new Client();
		client.connectToServer();
	}

	public Client() {
		this.ipAddr_ = "";
		this.port_ = 0;
		this.buffImg_ = null;
		this.path_ = System.getProperty("user.dir") + "\\src\\";
	}

	public void connectToServer() throws IOException {
		System.out.println("Enter the connection settings");
		choosePort();
		chooseIPAddr();
		if (!LoginModule.connection()) {
			System.out.println("Failed to logging in, exiting program");
			System.exit(0);
		}
		Socket socket = new Socket(this.ipAddr_, this.port_);
		OutputStream outputStream = socket.getOutputStream();
		importPicture();
		sendImageToByte(outputStream);
		InputStream inputStream = socket.getInputStream();
		recieveSobelImage(inputStream);
		socket.close();
	}

	private void choosePort() {
		Scanner sc = new Scanner(System.in);
		boolean validPort = false;
		while (!validPort) {
			this.port_ = 0;
			System.out.print("Choose a listening port between 5000 & 5050: ");
			this.port_ = sc.nextInt();
			validPort = checkPort(this.port_);
		}
	}

	private void chooseIPAddr() {
		Scanner sc = new Scanner(System.in);
		boolean validIP = false;
		while (!validIP) {
			this.ipAddr_ = "";
			System.out.print("Enter a valid IP address; format x.x.x.x where x is between 1 & 256: ");
			this.ipAddr_ = sc.nextLine();
			validIP = checkIPAddr(this.ipAddr_);
		}
	}

	private boolean checkPort(int port) {
		if (port < 5000 || port > 5050) {
			System.out.println("Not a valid listening port");
			return false;
		}
		return true;
	}

	private boolean checkIPAddr(String ipAddr) {
		String[] addrSegment;
		addrSegment = ipAddr.split("\\.");
		if (addrSegment.length > 4) {
			return false;
		}
		for (int i = 0; i < 4; i++) {
			int byteSeg = Integer.parseInt(addrSegment[i]);
			if (byteSeg < 0 || byteSeg > 256) {
				System.out.println("Not a valid IP address");
				return false;
			}
		}
		return true;
	}

	private void importPicture() {
		String path = "";
		String imageName = "";
		Scanner sc = new Scanner(System.in);
		boolean isPath = false;
		while (!isPath) {
			System.out.print("Enter the image's name: ");
			imageName = sc.nextLine();
			path = this.path_ + imageName + ".jpg";
			try {
				this.buffImg_ = ImageIO.read(new File(path));
				isPath = true;
			} catch (Exception e) {
				System.out.println(e);
				isPath = false;
			}
		}
	}

	private void sendImageToByte(OutputStream outputStream) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(this.buffImg_, "JPEG", byteArrayOutputStream);
			byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
			byte[] paquet = byteArrayOutputStream.toByteArray();
			outputStream.write(size);
			outputStream.write(paquet);
			System.out.println("Image was sent to the server");
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void recieveSobelImage(InputStream inputStream) {
		try {
			byte[] size = new byte[4];
			inputStream.read(size);
			byte[] paquet = new byte[ByteBuffer.wrap(size).asIntBuffer().get()];
			inputStream.read(paquet);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(paquet);
			BufferedImage sobelImage = ImageIO.read(byteArrayInputStream);
			String path = this.path_ + "sobelImage.jpg";
			File outputfile = new File(path);
			outputfile.createNewFile();
			ImageIO.write(sobelImage, "jpg", outputfile);
			System.out.println("Image was successfully recieved by the client");
			System.out.println("Filtered image was saved to url: " + path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
