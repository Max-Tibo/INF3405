import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Client {
	private String ipAddr_;
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
		outputStream.write(ImageToByte());
		InputStream inputStream = socket.getInputStream();
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
		Scanner sc = new Scanner(System.in);
		boolean isPath = false;
		while (!isPath) {
			System.out.print("Enter the image's path: ");
			path = sc.nextLine();
			try {
				this.buffImg_ = ImageIO.read(new File(path));
				isPath = true;
			} catch (Exception e) {
				System.out.println(e);
				isPath = false;
			}
		}
	}

	private byte[] ImageToByte() {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] paquet = null;
		try {
			ImageIO.write(this.buffImg_, "JPEG", byteArrayOutputStream);
			paquet = byteArrayOutputStream.toByteArray();
		} catch (Exception e) {
			System.out.println(e);
		}
		return paquet;
	}
}
