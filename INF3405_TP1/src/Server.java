import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Server {
	private static String ipAddr_;
	private static int port_;

	public static void main(String[] args) throws Exception {
		int clientId = 0;
		System.out.println("Enter the server settings");
		choosePort();
		chooseIPAddr();
		InetAddress ipAddr = InetAddress.getByName(ipAddr_);
		InetSocketAddress socketAddr = new InetSocketAddress(ipAddr, port_);

		ServerSocket listener = new ServerSocket();
		listener.setReuseAddress(true);
		listener.bind(socketAddr);

		try {
			while (true) {
				new ServerHost(listener.accept(), clientId++).start();
			}
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			listener.close();
		}
	}

	private static void choosePort() {
		Scanner sc = new Scanner(System.in);
		boolean validPort = false;
		while (!validPort) {
			port_ = 0;
			System.out.print("Choose a listening port between 5000 & 5050: ");
			port_ = sc.nextInt();
			validPort = checkPort(port_);
		}
	}

	private static void chooseIPAddr() {
		Scanner sc = new Scanner(System.in);
		boolean validIP = false;
		while (!validIP) {
			ipAddr_ = "";
			System.out.print("Enter a valid IP address; format x.x.x.x where x is between 1 & 256: ");
			ipAddr_ = sc.nextLine();
			validIP = checkIPAddr(ipAddr_);
		}
	}

	private static boolean checkPort(int port) {
		if (port < 5000 || port > 5050) {
			System.out.println("Not a valid listening port");
			return false;
		}
		return true;
	}

	private static boolean checkIPAddr(String ipAddr) {
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

	private static class ServerHost extends Thread {
		private Socket socket_;
		private int clientId_;

		public ServerHost(Socket socket, int clientId) {
			this.socket_ = socket;
			this.clientId_ = clientId;
			System.out.println("Client " + this.clientId_ + " is connected");
		}

		public void processImage() {
			try {
				byte[] size = new byte[4];
				InputStream inputStream = this.socket_.getInputStream();
				inputStream.read(size);
				byte[] paquet = new byte[ByteBuffer.wrap(size).asIntBuffer().get()];
				inputStream.read(paquet);
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(paquet);
				BufferedImage buffImg = ImageIO.read(byteArrayInputStream);
				BufferedImage processedImage = Sobel.process(buffImg);
				OutputStream outputStream = this.socket_.getOutputStream();
				ImageToByte(processedImage, outputStream);
			} catch (IOException e) {
				System.out.println(e);
			}
		}

		private void SendImageToByte(BufferedImage sobelImage, OutputStream &outputStream) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			try {
				ImageIO.write(sobelImage, "JPEG", byteArrayOutputStream);
				byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
				byte[] paquet = byteArrayOutputStream.toByteArray();
				outputStream.write(size);
				outputStream.write(paquet);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

}
